package com.project.controller;

import com.project.entity.Employee;
import com.project.entity.UploadedFileColumns;
import com.project.entity.UploadedFileData;
import com.project.entity.UploadedFiles;
import com.project.repository.UploadedFileColumnsRepository;
import com.project.repository.UploadedFileDataRepository;
import com.project.repository.UploadedFileRepository;
import com.project.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/employee")
public class EmployeeUserController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private UploadedFileDataRepository dataRepo;

    @Autowired
    private UploadedFileColumnsRepository columnRepo;

    @Autowired
    private UploadedFileRepository fileRepo;

    private Employee getLoggedInEmployee(HttpSession session) {
        return (Employee) session.getAttribute("loggedInEmployee");
    }

    private UploadedFileColumns findIdColumn(List<UploadedFileColumns> cols) {
        List<UploadedFileColumns> filtered = cols.stream()
                .filter(c -> c.isParse() != null && c.isParse())
                .collect(Collectors.toList());

        // 1. Priority: Check for explicit Key column
        UploadedFileColumns specific = filtered.stream()
                .filter(c -> c.getIsKey() != null && c.getIsKey())
                .findFirst()
                .orElse(null);
        if (specific != null) return specific;

        // Level 1: Very specific
        specific = filtered.stream()
                .filter(c -> {
                    String name = c.getColumnName().toUpperCase();
                    return (name.contains("EMP") || name.contains("EMPLOYEE")) && (name.contains("ID") || name.contains("CODE"));
                })
                .min(java.util.Comparator.comparingInt(UploadedFileColumns::getColumnPosition))
                .orElse(null);
        if (specific != null) return specific;

        // Level 2: Generic ID/CODE
        return filtered.stream()
                .filter(c -> {
                    String name = c.getColumnName().toUpperCase();
                    return name.contains("ID") || name.contains("CODE");
                })
                .min(java.util.Comparator.comparingInt(UploadedFileColumns::getColumnPosition))
                .orElse(null);
    }

    private Double parseAmount(Object val, String actualCol) {
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

    private String extractMonthYearFromFileName(String fileName) {
        if (fileName == null) return null;
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("(?i)(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)[a-z]*[-_\\s]*\\d{2,4}");
        java.util.regex.Matcher m = p.matcher(fileName);
        if (m.find()) {
            return m.group().toUpperCase().replaceAll("[-_\\s]+", " ");
        }
        return null;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        Employee employee = getLoggedInEmployee(session);
        if (employee == null) return "redirect:/employee/login";

        // Fetch all payslips for this employee
        List<UploadedFileData> allData = dataRepo.findByContractorId(employee.getContractor().getContractorId());
        List<Map<String, Object>> payslips = new ArrayList<>();

        // Group by fileId to find columns
        Map<Long, List<UploadedFileData>> dataByFile = allData.stream()
                .collect(Collectors.groupingBy(UploadedFileData::getFileId));

        for (Map.Entry<Long, List<UploadedFileData>> entry : dataByFile.entrySet()) {
            Long fileId = entry.getKey();
            List<UploadedFileColumns> cols = columnRepo.findByFileId(fileId);
            if (cols.isEmpty()) continue;

            // Find ID and Salary columns
            UploadedFileColumns idCol = findIdColumn(cols);

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

                for (UploadedFileData row : entry.getValue()) {
                    try {
                        java.lang.reflect.Method mId = UploadedFileData.class.getMethod(idGetter);
                        Object idVal = mId.invoke(row);
                        if (idVal != null && employee.getEmpCode().equalsIgnoreCase(idVal.toString().trim())) {
                            java.lang.reflect.Method mSal = UploadedFileData.class.getMethod(salaryGetter);
                            Double amount = parseAmount(mSal.invoke(row), salaryCol.getActualColumn());
                            
                            if (overtimeCol != null) {
                                String ovtGetter = "get" + overtimeCol.getActualColumn().substring(0, 1).toUpperCase() + overtimeCol.getActualColumn().substring(1);
                                java.lang.reflect.Method mOvt = UploadedFileData.class.getMethod(ovtGetter);
                                amount += parseAmount(mOvt.invoke(row), overtimeCol.getActualColumn());
                            }

                            Map<String, Object> p = new HashMap<>();
                            p.put("id", row.getId());
                            p.put("amount", amount);
                            p.put("uploadDate", file.getUploadDate());
                            
                            String fileName = file.getFileName();
                            String extractedMonthYear = extractMonthYearFromFileName(fileName);
                            if (extractedMonthYear == null) {
                                extractedMonthYear = java.time.format.DateTimeFormatter.ofPattern("MMM-yyyy").format(file.getUploadDate());
                            }
                            p.put("displayName", "Payslip for " + extractedMonthYear);
                            p.put("fileName", fileName); // Keep original for reference if needed
                            payslips.add(p);
                        }
                    } catch (Exception e) {}
                }
            }
        }

        model.addAttribute("employee", employee);
        model.addAttribute("payslips", payslips);
        return "employee/dashboard";
    }

    @GetMapping("/payslip/{id}")
    public String viewPayslip(@PathVariable Long id, Model model, HttpSession session) {
        Employee employee = getLoggedInEmployee(session);
        if (employee == null) return "redirect:/employee/login";

        UploadedFileData data = dataRepo.findById(id).orElse(null);
        if (data == null) return "redirect:/employee/dashboard";

        // Security check: Ensure this data belongs to the logged in employee
        UploadedFileColumns idCol = findIdColumn(columnRepo.findByFileId(data.getFileId()));

        if (idCol != null) {
            try {
                java.lang.reflect.Method mId = UploadedFileData.class.getMethod("get" + idCol.getActualColumn().substring(0, 1).toUpperCase() + idCol.getActualColumn().substring(1));
                Object idVal = mId.invoke(data);
                if (idVal == null || !employee.getEmpCode().equalsIgnoreCase(idVal.toString().trim())) {
                    System.err.println("[SECURITY] Unauthorized access attempt by " + employee.getEmpCode() + " for data ID " + id);
                    return "redirect:/employee/dashboard";
                }
            } catch (Exception e) {
                return "redirect:/employee/dashboard";
            }
        }

        // Reuse logic from PaymentsController
        UploadedFiles file = fileRepo.findById(data.getFileId()).orElse(null);
        List<UploadedFileColumns> columns = columnRepo.findByFileId(data.getFileId());
        columns.sort(java.util.Comparator.comparingInt(UploadedFileColumns::getColumnPosition));

        String employeeName = employee.getName();
        String employeeCode = employee.getEmpCode();
        Double totalAmount = 0.0;
        Double totalEarnings = 0.0;
        Double totalDeductions = 0.0;
        Double totalPayableFromColumn = null;
        Double overtimeAmount = null;

        List<Map<String, Object>> components = new ArrayList<>();

        for (UploadedFileColumns col : columns) {
            if (col.isParse() != null && col.isParse()) {
                String colName = col.getColumnName().toUpperCase();
                UploadedFileColumns idColLocal = findIdColumn(columns);
                boolean isId = (idColLocal != null && col.getId().equals(idColLocal.getId()));
                boolean isName = colName.contains("NAME");
                if (isId || isName) continue;

                String valStr = "";
                Double valNum = 0.0;
                boolean isNumber = false;

                try {
                    java.lang.reflect.Method m = UploadedFileData.class.getMethod("get" + col.getActualColumn().substring(0, 1).toUpperCase() + col.getActualColumn().substring(1));
                    Object result = m.invoke(data);
                    valNum = parseAmount(result, col.getActualColumn());
                    valStr = result != null ? result.toString().trim() : "";
                    
                    // If it's a numeric column OR it successfully parsed as a non-zero number, treat as number
                    if (col.getActualColumn().startsWith("num") || valNum != 0.0 || "E".equalsIgnoreCase(col.getSalaryType()) || "D".equalsIgnoreCase(col.getSalaryType())) {
                        isNumber = true;
                    }
                } catch (Exception e) {}

                Map<String, Object> comp = new HashMap<>();
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

        totalAmount = totalEarnings - totalDeductions;

        model.addAttribute("contractorName", employee.getContractor().getName());
        model.addAttribute("employeeName", employeeName);
        model.addAttribute("employeeCode", employeeCode);
        String fileName = file.getFileName();
        String extractedMonthYear = extractMonthYearFromFileName(fileName);
        if (extractedMonthYear == null) {
            extractedMonthYear = java.time.format.DateTimeFormatter.ofPattern("MMM yyyy").format(file.getUploadDate());
        }
        model.addAttribute("monthYearUi", extractedMonthYear);
        
        String shortMonthYear = extractedMonthYear.replaceAll("[-_\\s]+", "");
        if (shortMonthYear.length() >= 7) {
            shortMonthYear = shortMonthYear.substring(0, 1).toUpperCase() + shortMonthYear.substring(1, 3).toLowerCase() + shortMonthYear.substring(shortMonthYear.length() - 4);
        }
        model.addAttribute("monthYear", shortMonthYear);
        model.addAttribute("components", components);
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("totalEarnings", totalEarnings);
        model.addAttribute("totalDeductions", totalDeductions);

        return "employee/view-payslip";
    }

    @GetMapping("/profile")
    public String profile(Model model, HttpSession session) {
        Employee employee = getLoggedInEmployee(session);
        if (employee == null) return "redirect:/employee/login";
        model.addAttribute("employee", employee);
        return "employee/profile";
    }

    @PostMapping("/update-profile")
    @ResponseBody
    public org.springframework.http.ResponseEntity<?> updateProfile(@RequestParam String name, @RequestParam String mobile, HttpSession session) {
        Employee employee = getLoggedInEmployee(session);
        if (employee == null) return org.springframework.http.ResponseEntity.status(401).body(java.util.Map.of("status", "error", "message", "Unauthorized"));
        
        Employee dbEmp = employeeService.getById(employee.getEmployeeId());
        if (dbEmp != null) {
            dbEmp.setName(name);
            dbEmp.setMobileNo(mobile);
            employeeService.save(dbEmp);
            session.setAttribute("loggedInEmployee", dbEmp);
        }
        return org.springframework.http.ResponseEntity.ok(java.util.Map.of("status", "success", "message", "Profile updated successfully"));
    }

    @GetMapping("/change-password")
    public String changePasswordPage(Model model, HttpSession session) {
        Employee employee = getLoggedInEmployee(session);
        if (employee == null) return "redirect:/employee/login";
        model.addAttribute("employee", employee);
        return "employee/change-password";
    }

    @PostMapping("/update-password")
    @ResponseBody
    public org.springframework.http.ResponseEntity<?> updatePassword(@RequestParam(required = false) String currentPassword, @RequestParam String newPassword, @RequestParam String confirmPassword, HttpSession session) {
        Employee employee = getLoggedInEmployee(session);
        if (employee == null) return org.springframework.http.ResponseEntity.status(401).body(java.util.Map.of("status", "error", "message", "Unauthorized"));

        Employee dbEmp = employeeService.getById(employee.getEmployeeId());
        if (dbEmp == null) return org.springframework.http.ResponseEntity.status(401).body(java.util.Map.of("status", "error", "message", "Unauthorized"));

        if (Boolean.TRUE.equals(dbEmp.getPasswordChanged())) {
            if (currentPassword == null || !dbEmp.getPassword().equals(currentPassword)) {
                return org.springframework.http.ResponseEntity.badRequest().body(java.util.Map.of("status", "error", "message", "Current password is incorrect"));
            }
        }
        
        if (!newPassword.equals(confirmPassword)) {
            return org.springframework.http.ResponseEntity.badRequest().body(java.util.Map.of("status", "error", "message", "New passwords do not match"));
        }

        dbEmp.setPassword(newPassword);
        dbEmp.setPasswordChanged(true);
        employeeService.save(dbEmp);
        session.setAttribute("loggedInEmployee", dbEmp);

        return org.springframework.http.ResponseEntity.ok(java.util.Map.of("status", "success", "message", "Password updated successfully", "redirect", "/employee/dashboard"));
    }
}
