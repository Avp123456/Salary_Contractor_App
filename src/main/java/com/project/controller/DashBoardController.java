package com.project.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.project.entity.Contractor;
import com.project.entity.UploadedFileColumns;
import com.project.entity.UploadedFileData;
import com.project.repository.UploadedFileColumnsRepository;
import com.project.repository.UploadedFileDataRepository;
import com.project.repository.UploadedFileRepository;
import com.project.repository.ReportConfigurationRepository;
import com.project.repository.ReportConfigurationFileRepository;
import com.project.entity.ReportConfiguration;
import com.project.entity.ReportConfigurationFile;
import com.project.entity.Employee;
import com.project.service.EmployeeService;

import javax.servlet.http.HttpSession;
/*
==========================
    DASHBOARD CONTROLLER
==========================
*/
@Controller
public class DashBoardController {
	 //time stamp
    private String getTime() {
        return java.time.LocalDateTime.now().toString();
    }
    
    @Autowired
    private UploadedFileDataRepository dataRepo;
    
    @Autowired
    private UploadedFileColumnsRepository columnRepo;
    
    @Autowired
    private UploadedFileRepository fileRepo;
    
    @Autowired
    private ReportConfigurationRepository configRepo;
    
    @Autowired
    private ReportConfigurationFileRepository configFileRepo;
    
    @Autowired
    private EmployeeService employeeService;
    
    /*
    ===============================
     Get Current Contractor ID
    ===============================
    */
    private Long getCurrentContractorId(HttpSession session) {
        Contractor contractor = (Contractor) session.getAttribute("loggedInContractor");
        return (contractor != null) ? contractor.getContractorId() : null;
    }
    /*
    ==========================
       Contractor Dashboard 
    ==========================
    */
    @GetMapping("/contractor/dashboard")
    public String contractorDashboard(Model model, HttpSession session) {
        System.out.println("[Page Visited]:- Contractor Dashboard");
        Contractor contractor = (Contractor) session.getAttribute("loggedInContractor");
        if (contractor == null) return "redirect:/contractor/login?reason=dash_null_id";
        Long contractorId = contractor.getContractorId();

        List<UploadedFileData> allData = dataRepo.findByContractorId(contractorId);
        List<Employee> registeredEmployees = employeeService.getByContractor(contractor);
        
        long totalEmployees = 0;
        double totalSalary = 0.0;
        int registeredCount = 0;
        int notRegisteredCount = 0;
        
        java.util.Map<String, Double> monthlyPayouts = new java.util.LinkedHashMap<>();
        
        Map<Long, List<UploadedFileData>> dataByFile = allData.stream()
            .collect(Collectors.groupingBy(UploadedFileData::getFileId));

        for (java.util.Map.Entry<Long, List<UploadedFileData>> entry : dataByFile.entrySet()) {
            Long fileId = entry.getKey();
            List<UploadedFileData> fileRows = entry.getValue();
            
            com.project.entity.UploadedFiles file = fileRepo.findById(fileId).orElse(null);
            java.util.List<String> targetMonthYears = new java.util.ArrayList<>();
            if (file != null) {
                List<ReportConfigurationFile> configFiles = configFileRepo.findByFileName(file.getFileName());
                if (configFiles != null && !configFiles.isEmpty()) {
                    for (ReportConfigurationFile rcf : configFiles) {
                        ReportConfiguration config = configRepo.findById(rcf.getConfigId()).orElse(null);
                        if (config != null && config.getMonth() != null && !config.getMonth().isEmpty()) {
                            String mY = config.getMonth();
                            if (config.getYear() != null) {
                                mY += " " + config.getYear();
                            } else {
                                int currentYear = java.time.Year.now().getValue();
                                mY += " " + currentYear;
                            }
                            if (!targetMonthYears.contains(mY)) {
                                targetMonthYears.add(mY.toUpperCase());
                            }
                        }
                    }
                }
                
                if (targetMonthYears.isEmpty()) {
                    String extractedMonth = extractMonthYearFromFileName(file.getFileName());
                    if (extractedMonth == null) {
                        extractedMonth = java.time.format.DateTimeFormatter.ofPattern("MMM-yyyy").format(file.getUploadDate());
                    }
                    if (extractedMonth != null && !extractedMonth.matches(".*\\d{4}.*")) {
                        int currentYear = java.time.Year.now().getValue();
                        targetMonthYears.add((extractedMonth.substring(0, Math.min(3, extractedMonth.length())).toUpperCase() + " " + currentYear));
                    } else if (extractedMonth != null) {
                        targetMonthYears.add(extractedMonth.toUpperCase());
                    } else {
                        targetMonthYears.add("UNKNOWN");
                    }
                }
            }
            
            List<UploadedFileColumns> cols = columnRepo.findByFileId(fileId);
            if (cols.isEmpty()) continue;
            
            UploadedFileColumns idCol = findIdColumn(cols);
            
            UploadedFileColumns salaryCol = null;
            if (file != null && file.getTotalPayableColumn() != null && file.getTotalPayableColumn() > 0) {
                final int totalPos = file.getTotalPayableColumn();
                salaryCol = cols.stream().filter(c -> c.isParse() != null && c.isParse() && c.getColumnPosition() == totalPos).findFirst().orElse(null);
            }
            if (salaryCol == null) {
                salaryCol = cols.stream().filter(c -> c.isParse() != null && c.isParse()).max(java.util.Comparator.comparingInt(UploadedFileColumns::getColumnPosition)).orElse(null);
            }

            UploadedFileColumns nameCol = cols.stream()
                .filter(c -> c.isParse() != null && c.isParse())
                .filter(c -> c.getColumnName() != null && c.getColumnName().toUpperCase().contains("NAME"))
                .findFirst()
                .orElse(null);

            if (salaryCol != null && salaryCol.getActualColumn() != null) {
                String idGetter = (idCol != null) ? "get" + idCol.getActualColumn().substring(0, 1).toUpperCase() + idCol.getActualColumn().substring(1) : null;
                String salaryGetter = "get" + salaryCol.getActualColumn().substring(0, 1).toUpperCase() + salaryCol.getActualColumn().substring(1);
                String nameGetter = (nameCol != null) ? "get" + nameCol.getActualColumn().substring(0, 1).toUpperCase() + nameCol.getActualColumn().substring(1) : null;

                for (String monthYear : targetMonthYears) {
                    for (UploadedFileData row : fileRows) {
                    try {
                        String empIdInFile = "";
                        boolean isValidRow = true;
                        if (idGetter != null) {
                            java.lang.reflect.Method mId = UploadedFileData.class.getMethod(idGetter);
                            Object idVal = mId.invoke(row);
                            if (idVal == null || idVal.toString().trim().isEmpty()) {
                                isValidRow = false;
                            } else {
                                empIdInFile = idVal.toString().trim();
                            }
                        }

                        if (isValidRow) {
                            totalEmployees++;
                            
                            if (!empIdInFile.isEmpty()) {
                                final String searchId = empIdInFile;
                                boolean isRegistered = registeredEmployees.stream().anyMatch(e -> e.getEmpCode().equalsIgnoreCase(searchId));
                                if (isRegistered) registeredCount++;
                                else notRegisteredCount++;
                            }
                            
                            Double amount = 0.0;
                            java.lang.reflect.Method mSal = UploadedFileData.class.getMethod(salaryGetter);
                            Object salVal = mSal.invoke(row);
                            if (salVal != null) {
                                if (salVal instanceof Double) amount = (Double) salVal;
                                else {
                                    String strVal = salVal.toString().replace(",", "").trim();
                                    if (!strVal.isEmpty()) amount = Double.parseDouble(strVal);
                                }
                            }
                            totalSalary += amount;
                            
                            monthlyPayouts.put(monthYear, monthlyPayouts.getOrDefault(monthYear, 0.0) + amount);
                        }
                    } catch (Exception e) {}
                }
                } // End outer targetMonthYears loop
            }
        }
        
        model.addAttribute("totalEmployees", totalEmployees);
        model.addAttribute("totalSalary", totalSalary);
        model.addAttribute("totalFiles", fileRepo.findByContractorId(contractorId).size());
        
        model.addAttribute("registeredCount", registeredCount);
        model.addAttribute("notRegisteredCount", notRegisteredCount);
        
        model.addAttribute("chartLabels", new java.util.ArrayList<>(monthlyPayouts.keySet()));
        model.addAttribute("chartData", new java.util.ArrayList<>(monthlyPayouts.values()));

        System.out.println("[INFO] Dashboard page visited "+getTime());
        return "contractor/dashboard";
    }

    private UploadedFileColumns findIdColumn(List<UploadedFileColumns> cols) {
        List<UploadedFileColumns> filtered = cols.stream()
                .filter(c -> c.isParse() != null && c.isParse())
                .collect(java.util.stream.Collectors.toList());

        UploadedFileColumns specific = filtered.stream()
                .filter(c -> c.getIsKey() != null && c.getIsKey())
                .findFirst()
                .orElse(null);
        if (specific != null) return specific;

        specific = filtered.stream()
                .filter(c -> {
                    String name = c.getColumnName().toUpperCase();
                    return (name.contains("EMP") || name.contains("EMPLOYEE")) && (name.contains("ID") || name.contains("CODE"));
                })
                .min(java.util.Comparator.comparingInt(UploadedFileColumns::getColumnPosition))
                .orElse(null);
        if (specific != null) return specific;

        return filtered.stream()
                .filter(c -> {
                    String name = c.getColumnName().toUpperCase();
                    return name.contains("ID") || name.contains("CODE");
                })
                .min(java.util.Comparator.comparingInt(UploadedFileColumns::getColumnPosition))
                .orElse(null);
    }
    
    private String extractMonthYearFromFileName(String fileName) {
        if (fileName == null) return null;
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("(?i)(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)[a-z]*[-_\\s]*\\d{2,4}");
        java.util.regex.Matcher m = p.matcher(fileName);
        if (m.find()) {
            return m.group().toUpperCase().replaceAll("[-_\\s]+", " ");
        }
        return null;
    }
}
