package com.project.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.project.entity.Contractor;
import com.project.entity.Employee;
import com.project.entity.UploadedFileColumns;
import com.project.entity.UploadedFileData;
import com.project.entity.UploadedFiles;
import com.project.repository.UploadedFileColumnsRepository;
import com.project.repository.UploadedFileDataRepository;
import com.project.repository.UploadedFileRepository;
import com.project.repository.ReportConfigurationRepository;
import com.project.repository.ReportConfigurationFileRepository;
import com.project.entity.ReportConfiguration;
import com.project.entity.ReportConfigurationFile;
import com.project.service.EmployeeService;

import javax.servlet.http.HttpSession;
@Controller
public class PaymentsController {

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
    private EmployeeService employeeService;
    
    @Autowired
    private ReportConfigurationRepository configRepo;
    
    @Autowired
    private ReportConfigurationFileRepository configFileRepo;

    private Long getCurrentContractorId(HttpSession session) {
        Contractor contractor = (Contractor) session.getAttribute("loggedInContractor");
        return (contractor != null) ? contractor.getContractorId() : null;
    }

    private UploadedFileColumns findIdColumn(List<UploadedFileColumns> cols) {
        List<UploadedFileColumns> filtered = cols.stream()
                .filter(c -> c.isParse() != null && c.isParse())
                .collect(java.util.stream.Collectors.toList());

        // 1. Priority: Check for explicit Key column
        UploadedFileColumns specific = filtered.stream()
                .filter(c -> c.getIsKey() != null && c.getIsKey())
                .findFirst()
                .orElse(null);
        if (specific != null) return specific;

        // 2. Priority: Check for columns containing both (EMP/EMPLOYEE) and (ID/CODE)
        specific = filtered.stream()
                .filter(c -> {
                    String name = c.getColumnName().toUpperCase();
                    return (name.contains("EMP") || name.contains("EMPLOYEE")) && (name.contains("ID") || name.contains("CODE"));
                })
                .min(java.util.Comparator.comparingInt(UploadedFileColumns::getColumnPosition))
                .orElse(null);
        if (specific != null) return specific;

        // 3. Priority: Fallback to columns containing just ID or CODE
        return filtered.stream()
                .filter(c -> {
                    String name = c.getColumnName().toUpperCase();
                    return name.contains("ID") || name.contains("CODE");
                })
                .min(java.util.Comparator.comparingInt(UploadedFileColumns::getColumnPosition))
                .orElse(null);
    }

    private Double parseAmount(Object val) {
        if (val == null) return 0.0;
        try {
            if (val instanceof Double) return (Double) val;
            String clean = val.toString().replaceAll("[^0-9.-]", "").trim();
            if (clean.isEmpty()) return 0.0;
            return Double.parseDouble(clean);
        } catch (Exception e) {
            return 0.0;
        }
    }

    @GetMapping("/contractor/payments")
    public String payments(Model model, HttpSession session) {
        System.out.println("[Page Visited]:- Contractor Payments");
        Contractor contractor = (Contractor) session.getAttribute("loggedInContractor");
        if (contractor == null) return "redirect:/contractor/login";
        Long contractorId = contractor.getContractorId();

        // 1. Get all registered employees
        List<Employee> registeredEmployees = employeeService.getByContractor(contractor);
        
        // 2. Get all uploaded data
        List<UploadedFileData> allData = dataRepo.findByContractorId(contractorId);
        
        List<java.util.Map<String, Object>> paymentList = new java.util.ArrayList<>();
        
        // 3. Group data by fileId to handle different mappings
        java.util.Map<Long, List<UploadedFileData>> dataByFile = allData.stream()
            .collect(java.util.stream.Collectors.groupingBy(UploadedFileData::getFileId));

        for (java.util.Map.Entry<Long, List<UploadedFileData>> entry : dataByFile.entrySet()) {
            Long fileId = entry.getKey();
            List<UploadedFileData> fileRows = entry.getValue();

            List<UploadedFileColumns> cols = columnRepo.findByFileId(fileId);
            if (cols.isEmpty()) continue;

            // Find ID and Salary columns
            UploadedFileColumns idCol = findIdColumn(cols);
            
            UploadedFileColumns nameCol = cols.stream()
                .filter(c -> c.isParse() != null && c.isParse())
                .filter(c -> c.getColumnName() != null && c.getColumnName().toUpperCase().contains("NAME"))
                .findFirst()
                .orElse(null);

            UploadedFiles file = fileRepo.findById(fileId).orElse(null);
            UploadedFileColumns salaryCol = null;
            
            if (file != null && file.getTotalPayableColumn() != null && file.getTotalPayableColumn() > 0) {
                final int totalPos = file.getTotalPayableColumn();
                salaryCol = cols.stream()
                    .filter(c -> c.isParse() != null && c.isParse())
                    .filter(c -> c.getColumnPosition() == totalPos)
                    .findFirst()
                    .orElse(null);
            }
            
            if (salaryCol == null) {
                salaryCol = cols.stream()
                    .filter(c -> c.isParse() != null && c.isParse())
                    .max(java.util.Comparator.comparingInt(UploadedFileColumns::getColumnPosition))
                    .orElse(null);
            }
            
            UploadedFileColumns overtimeCol = null;
            if (file != null && file.getOvertimeTotalAmountColumn() != null && file.getOvertimeTotalAmountColumn() > 0) {
                final int ovtPos = file.getOvertimeTotalAmountColumn();
                overtimeCol = cols.stream()
                    .filter(c -> c.isParse() != null && c.isParse())
                    .filter(c -> c.getColumnPosition() == ovtPos)
                    .findFirst()
                    .orElse(null);
            }

            if (idCol != null && salaryCol != null) {
                String idGetter = "get" + idCol.getActualColumn().substring(0, 1).toUpperCase() + idCol.getActualColumn().substring(1);
                String salaryGetter = "get" + salaryCol.getActualColumn().substring(0, 1).toUpperCase() + salaryCol.getActualColumn().substring(1);

                java.util.List<String> targetMonthYears = new java.util.ArrayList<>();
                
                // Try to get month from configuration first
                List<ReportConfigurationFile> configFiles = configFileRepo.findByFileName(file.getFileName());
                if (configFiles != null && !configFiles.isEmpty()) {
                    for (ReportConfigurationFile rcf : configFiles) {
                        ReportConfiguration config = configRepo.findById(rcf.getConfigId()).orElse(null);
                        if (config != null && config.getMonth() != null && !config.getMonth().isEmpty()) {
                            String mY = config.getMonth();
                            if (config.getYear() != null) {
                                mY += " " + config.getYear();
                            }
                            if (!targetMonthYears.contains(mY)) {
                                targetMonthYears.add(mY);
                            }
                        }
                    }
                }
                
                // Fallback to extraction
                if (targetMonthYears.isEmpty()) {
                    String extractedMonthYear = extractMonthYearFromFileName(file.getFileName());
                    if (extractedMonthYear == null) {
                        extractedMonthYear = java.time.format.DateTimeFormatter.ofPattern("MMM-yyyy").format(file.getUploadDate());
                    }
                    targetMonthYears.add(extractedMonthYear);
                }

                for (String extractedMonthYear : targetMonthYears) {
                    for (UploadedFileData row : fileRows) {
                    try {
                        java.lang.reflect.Method mId = UploadedFileData.class.getMethod(idGetter);
                        Object idVal = mId.invoke(row);
                        String empIdInFile = idVal != null ? idVal.toString().trim() : "";

                        if (!empIdInFile.isEmpty()) {
                            // Find matching registered employee
                            Employee match = registeredEmployees.stream()
                                .filter(e -> empIdInFile.equalsIgnoreCase(e.getEmpCode()))
                                .findFirst()
                                .orElse(null);

                            java.util.Map<String, Object> p = new java.util.HashMap<>();
                            p.put("id", row.getId()); // Store the data ID
                            p.put("empCode", empIdInFile);
                            p.put("monthYear", extractedMonthYear);
                            p.put("contractorName", contractor.getName());
                            
                            String empNameInFile = "";
                            if (nameCol != null) {
                                try {
                                    String nameGetter = "get" + nameCol.getActualColumn().substring(0, 1).toUpperCase() + nameCol.getActualColumn().substring(1);
                                    java.lang.reflect.Method mName = UploadedFileData.class.getMethod(nameGetter);
                                    Object nameVal = mName.invoke(row);
                                    empNameInFile = nameVal != null ? nameVal.toString().trim() : "";
                                } catch (Exception e) {}
                            }
                            
                            if (!empNameInFile.isEmpty()) {
                                String[] words = empNameInFile.toLowerCase().split("\\s+");
                                StringBuilder titleCaseName = new StringBuilder();
                                for (String word : words) {
                                    if (word.length() > 0) {
                                        titleCaseName.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
                                    }
                                }
                                empNameInFile = titleCaseName.toString().trim();
                            }
                            
                            if (match != null) {
                                p.put("name", !empNameInFile.isEmpty() ? empNameInFile : match.getName());
                                p.put("registered", true);
                            } else {
                                p.put("name", !empNameInFile.isEmpty() ? empNameInFile : "Not Registered");
                                p.put("registered", false);
                            }
                            
                            java.lang.reflect.Method mSal = UploadedFileData.class.getMethod(salaryGetter);
                            Double amount = parseAmount(mSal.invoke(row));
                            
                            if (overtimeCol != null) {
                                String ovtGetter = "get" + overtimeCol.getActualColumn().substring(0, 1).toUpperCase() + overtimeCol.getActualColumn().substring(1);
                                java.lang.reflect.Method mOvt = UploadedFileData.class.getMethod(ovtGetter);
                                amount += parseAmount(mOvt.invoke(row));
                            }
                            
                            p.put("amount", amount);
                            p.put("status", row.getStatus() != null ? row.getStatus() : "Pending");
                            p.put("structureViewed", row.getStructureViewed() != null ? row.getStructureViewed() : false);
                            p.put("payslipGenerated", row.getPayslipGenerated() != null ? row.getPayslipGenerated() : false);
                            paymentList.add(p);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                } // End outer targetMonthYears loop
            }
        }

        java.util.Map<String, java.util.Map<String, Object>> uniquePayments = new java.util.HashMap<>();
        for (java.util.Map<String, Object> p : paymentList) {
            String key = p.get("empCode") + "_" + p.get("monthYear") + "_" + p.get("amount");
            if (!uniquePayments.containsKey(key)) {
                uniquePayments.put(key, p);
            }
        }
        paymentList = new java.util.ArrayList<>(uniquePayments.values());

        paymentList.sort((p1, p2) -> {
            Boolean r1 = (Boolean) p1.get("registered");
            Boolean r2 = (Boolean) p2.get("registered");
            if (r1 == null) r1 = false;
            if (r2 == null) r2 = false;
            
            if (!r1.equals(r2)) {
                return r1 ? -1 : 1; // true comes first
            }
            
            String n1 = (String) p1.get("name");
            String n2 = (String) p2.get("name");
            if (n1 == null) n1 = "";
            if (n2 == null) n2 = "";
            return n1.compareToIgnoreCase(n2);
        });

        model.addAttribute("payments", paymentList);
        
        java.util.Set<String> availableMonthYears = new java.util.HashSet<>();
        
        for (java.util.Map<String, Object> p : paymentList) {
            String monthYearStr = (String) p.get("monthYear");
            if (monthYearStr != null && !monthYearStr.isEmpty()) {
                availableMonthYears.add(monthYearStr.toUpperCase());
            }
        }
        
        java.util.List<String> monthYearList = new java.util.ArrayList<>(availableMonthYears);
        java.util.Collections.sort(monthYearList); // Sort alphabetically, or parse date? Simple string sort for now.
        
        model.addAttribute("availableMonthYears", monthYearList);
        
System.out.println("[INFO] Payments Page Visited "+getTime());
        return "contractor/payments";
    }

    @GetMapping("/contractor/get-payment-details")
    @ResponseBody
    public java.util.Map<String, Object> getPaymentDetails(@RequestParam Long id, HttpSession session) {
        System.out.println("[Button clicked]:- View Payment Details");
        Long contractorId = getCurrentContractorId(session);
        UploadedFileData data = dataRepo.findById(id).orElse(null);
        if (data == null || !data.getContractorId().equals(contractorId)) return null;
        
        // Mark as viewed
        if (data.getStructureViewed() == null || !data.getStructureViewed()) {
            data.setStructureViewed(true);
            dataRepo.save(data);
        }

        List<UploadedFileColumns> columns = columnRepo.findByFileId(data.getFileId());
        columns.sort(java.util.Comparator.comparingInt(UploadedFileColumns::getColumnPosition));

        UploadedFiles file = fileRepo.findById(data.getFileId()).orElse(null);
        Integer totalPos = (file != null) ? file.getTotalPayableColumn() : 0;

        List<java.util.Map<String, Object>> fields = new java.util.ArrayList<>();
        java.util.Map<String, Object> totalField = null;

        for (UploadedFileColumns col : columns) {
            if (col.isParse() != null && col.isParse()) {
                java.util.Map<String, Object> f = new java.util.HashMap<>();
                boolean isTotal = totalPos != null && totalPos > 0 && col.getColumnPosition() == totalPos;
                
                f.put("name", isTotal ? "Total Payable Amount" : col.getColumnName());
                f.put("actual", col.getActualColumn());
                f.put("type", col.getDataType());
                f.put("isTotal", isTotal);
                
                String val = "";
                try {
                    java.lang.reflect.Method m = UploadedFileData.class.getMethod("get" + col.getActualColumn().substring(0, 1).toUpperCase() + col.getActualColumn().substring(1));
                    Object result = m.invoke(data);
                    if (result != null) val = result.toString();
                } catch (Exception e) {}
                f.put("value", val);

                if (isTotal) totalField = f;
                else fields.add(f);
            }
        }
        
        // Add the total field at the very end
        if (totalField != null) fields.add(totalField);

        java.util.Map<String, Object> res = new java.util.HashMap<>();
        res.put("id", data.getId());
        res.put("fields", fields);
        return res;
    }

    
    private void setString(UploadedFileData d, String col, String val) {
        try {
            java.lang.reflect.Method method = UploadedFileData.class.getMethod("set" + col.substring(0, 1).toUpperCase() + col.substring(1), String.class);
            method.invoke(d, val);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void setNumber(UploadedFileData d, String col, String val) {
        Double num = 0.0;
        try {
            num = val == null || val.isEmpty() ? 0 : Double.parseDouble(val);
            java.lang.reflect.Method method = UploadedFileData.class.getMethod("set" + col.substring(0, 1).toUpperCase() + col.substring(1), Double.class);
            method.invoke(d, num);
        } catch (Exception e) {}
    }
    
    @PostMapping("/contractor/update-payment-data")
    @ResponseBody
    public String updatePaymentData(@RequestBody java.util.Map<String, Object> payload, HttpSession session) {
        System.out.println("[Button clicked]:- Update Payment Data");
        Long contractorId = getCurrentContractorId(session);
        Long id = Long.valueOf(payload.get("id").toString());
        UploadedFileData data = dataRepo.findById(id).orElse(null);
        if (data == null || !data.getContractorId().equals(contractorId)) return "Error";

        java.util.Map<String, String> updates = (java.util.Map<String, String>) payload.get("updates");
        for (java.util.Map.Entry<String, String> entry : updates.entrySet()) {
            String col = entry.getKey();
            String val = entry.getValue();
            if (col.startsWith("str")) setString(data, col, val);
            else if (col.startsWith("num")) setNumber(data, col, val);
        }
        dataRepo.save(data);
        System.out.println("[ACTION] Payment Data Updated "+ getTime());
        return "OK";
    }

    @PostMapping("/contractor/update-payment-status")
    @ResponseBody
    public String updatePaymentStatus(@RequestBody java.util.Map<String, Object> payload, HttpSession session) {
        System.out.println("[Button clicked]:- Update Payment Status");
        Long contractorId = getCurrentContractorId(session);
        Long id = Long.valueOf(payload.get("id").toString());
        String status = payload.get("status").toString();
        
        UploadedFileData data = dataRepo.findById(id).orElse(null);
        if (data == null || !data.getContractorId().equals(contractorId)) return "Error";
        
        data.setStatus(status);
        dataRepo.save(data);
        System.out.println("[ACTION] Payment Status Updated "+ getTime());

        
        return "OK";
    }

    @PostMapping("/contractor/generate-payslip")
    @ResponseBody
    public String generatePayslipEndpoint(@RequestBody java.util.Map<String, Object> payload, HttpSession session) {
        System.out.println("[Button clicked]:- Generate Payslip");
        Long contractorId = getCurrentContractorId(session);
        Long id = Long.valueOf(payload.get("id").toString());
        
        UploadedFileData data = dataRepo.findById(id).orElse(null);
        if (data == null || !data.getContractorId().equals(contractorId)) return "Error";
        
        data.setPayslipGenerated(true);
        dataRepo.save(data);
        System.out.println("[ACTION] Payment PaySlip Generated "+ getTime());

        return "OK";
    }

    @GetMapping("/contractor/payslip/{id}")
    public String viewPayslip(@PathVariable Long id, Model model, HttpSession session) {
        System.out.println("[Page Visited]:- Contractor Payslip");
        Contractor contractor = (Contractor) session.getAttribute("loggedInContractor");
        if (contractor == null) return "redirect:/contractor/login";
        Long contractorId = contractor.getContractorId();

        UploadedFileData data = dataRepo.findById(id).orElse(null);
        if (data == null || !data.getContractorId().equals(contractorId)) return "redirect:/contractor/payments";

        UploadedFiles file = fileRepo.findById(data.getFileId()).orElse(null);
        if (file == null) return "redirect:/contractor/payments";

        List<UploadedFileColumns> columns = columnRepo.findByFileId(data.getFileId());
        columns.sort(java.util.Comparator.comparingInt(UploadedFileColumns::getColumnPosition));

        String employeeCode = "";
        String employeeName = "";
        Double totalAmount = 0.0;
        Double totalEarnings = 0.0;
        Double totalDeductions = 0.0;
        Double totalPayableFromColumn = null;
        Double overtimeAmount = null;

        List<java.util.Map<String, Object>> components = new java.util.ArrayList<>();

        for (UploadedFileColumns col : columns) {
            if (col.isParse() != null && col.isParse()) {
                String colName = col.getColumnName().toUpperCase();
                UploadedFileColumns idColLocal = findIdColumn(columns);
                boolean isId = (idColLocal != null && col.getId().equals(idColLocal.getId()));
                boolean isName = colName.contains("NAME");
                
                String valStr = "";
                Double valNum = 0.0;
                boolean isNumber = false;

                try {
                    java.lang.reflect.Method m = UploadedFileData.class.getMethod("get" + col.getActualColumn().substring(0, 1).toUpperCase() + col.getActualColumn().substring(1));
                    Object result = m.invoke(data);
                    valNum = parseAmount(result);
                    valStr = result != null ? result.toString().trim() : "";
                    if (col.getActualColumn().startsWith("num") || valNum != 0.0 || "E".equalsIgnoreCase(col.getSalaryType()) || "D".equalsIgnoreCase(col.getSalaryType())) {
                        isNumber = true;
                    }
                } catch (Exception e) {}

                if (isId && employeeCode.isEmpty()) employeeCode = valStr;
                else if (isName && employeeName.isEmpty()) employeeName = valStr;
                else {
                    java.util.Map<String, Object> comp = new java.util.HashMap<>();
                    comp.put("name", col.getColumnName());
                    
                    if (isNumber) {
                        comp.put("value", valNum);
                        comp.put("isString", false);
                    } else {
                        comp.put("value", valStr);
                        comp.put("isString", true);
                    }
                    
                    boolean isTotal = false;
                    boolean isOvertime = false;
                    if (file.getTotalPayableColumn() != null && file.getTotalPayableColumn() > 0) {
                        isTotal = (col.getColumnPosition() == file.getTotalPayableColumn());
                    } else {
                        isTotal = colName.contains("TOTAL") || colName.contains("PAYABLE") || colName.contains("NET") || colName.contains("AMOUNT");
                    }
                    
                    if (file.getOvertimeTotalAmountColumn() != null && file.getOvertimeTotalAmountColumn() > 0) {
                        isOvertime = (col.getColumnPosition() == file.getOvertimeTotalAmountColumn());
                    }

                    if ("E".equalsIgnoreCase(col.getSalaryType())) {
                        comp.put("type", "Earnings");
                        if (isNumber) totalEarnings += valNum;
                        if (isTotal) totalPayableFromColumn = valNum;
                        if (isOvertime) overtimeAmount = valNum;
                    } else if ("D".equalsIgnoreCase(col.getSalaryType())) {
                        comp.put("type", "Deductions");
                        if (isNumber) totalDeductions += valNum;
                        if (isTotal) totalPayableFromColumn = valNum;
                    } else if (isTotal) {
                        comp.put("type", "Total");
                        totalPayableFromColumn = valNum;
                    } else if (isOvertime) {
                        comp.put("type", "Earnings");
                        overtimeAmount = valNum;
                        if (isNumber) totalEarnings += valNum;
                    } else if (colName.contains("DEDUCT") || colName.contains("TDS") || colName.contains("TAX") || colName.contains("PF") || colName.contains("FINE") || colName.contains("FUND") || colName.contains("ESI") || colName.contains("LOAN") || colName.contains("PROF") || colName.contains("LWF") || colName.contains("PTAX")) {
                        comp.put("type", "Deductions");
                        if (isNumber && !colName.contains("TOTAL")) totalDeductions += valNum;
                    } else {
                        comp.put("type", "Earnings");
                        if (isNumber && !colName.contains("TOTAL")) totalEarnings += valNum;
                    }
                    components.add(comp);
                }
            }
        }

        totalAmount = totalEarnings - totalDeductions;

        if (!employeeCode.isEmpty()) {
            final String finalEmployeeCode = employeeCode;
            Employee emp = employeeService.getByContractor(contractor).stream()
                .filter(e -> finalEmployeeCode.equalsIgnoreCase(e.getEmpCode()))
                .findFirst()
                .orElse(null);
            if (emp != null) {
                if (employeeName.isEmpty()) employeeName = emp.getName();
                model.addAttribute("employeeMobileNo", emp.getMobileNo());
            }
        }

        model.addAttribute("contractorName", contractor.getName());
        model.addAttribute("employeeName", employeeName);
        model.addAttribute("employeeCode", employeeCode);
        
        String fileName = file.getFileName();
        String extractedMonthYear = extractMonthYearFromFileName(fileName);
        if (extractedMonthYear == null) {
            extractedMonthYear = java.time.format.DateTimeFormatter.ofPattern("MMM-yyyy").format(file.getUploadDate());
        }
        model.addAttribute("monthYear", extractedMonthYear.replace(" ", "_")); // Use underscores for safe PDF naming if needed, or keep spaces. Let's keep original for UI and replace in HTML.
        model.addAttribute("monthYearUi", extractedMonthYear);
        
        model.addAttribute("uploadDate", file.getUploadDate());
        model.addAttribute("components", components);
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("totalEarnings", totalEarnings);
        model.addAttribute("totalDeductions", totalDeductions);
        System.out.println("[INFO] Payment Payslip Viewed "+ getTime());

        return "contractor/payslip";
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
