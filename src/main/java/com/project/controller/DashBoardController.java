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

import javax.servlet.http.HttpSession;
/*
==========================
    DASHBOARD CONTROLLER
==========================
*/
@Controller
public class DashBoardController {
	 //time stamp
    String time = java.time.LocalDateTime.now().toString();
    
    @Autowired
    private UploadedFileDataRepository dataRepo;
    
    @Autowired
    private UploadedFileColumnsRepository columnRepo;
    
    @Autowired
    private UploadedFileRepository fileRepo;
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
        Long contractorId = getCurrentContractorId(session);
        if (contractorId == null) return "redirect:/login";

        List<UploadedFileData> allData = dataRepo.findByContractorId(contractorId);
        long totalEmployees = 0;
        double totalSalary = 0.0;
        
     // Group all uploaded data by fileId
     // This helps handle multiple uploaded files separately
       Map<Long, List<UploadedFileData>> dataByFile = allData.stream()
            .collect(Collectors.groupingBy(UploadedFileData::getFileId));

        for (java.util.Map.Entry<Long, List<UploadedFileData>> entry : dataByFile.entrySet()) {
            Long fileId = entry.getKey();// current file id
            List<UploadedFileData> fileRows = entry.getValue();//that file data
         // Get column configuration for this file
            List<UploadedFileColumns> cols = columnRepo.findByFileId(fileId);
            if (cols.isEmpty()) continue;
            
            // Find first column that represents employee ID
            UploadedFileColumns idCol = cols.stream()
                .filter(c -> c.isParse() != null && c.isParse())
                .filter(c -> {
                    String name = c.getColumnName().toUpperCase();
                    return name.contains("ID") || name.contains("CODE") || name.contains("EMP");
                })
                .min(java.util.Comparator.comparingInt(UploadedFileColumns::getColumnPosition))
                .orElse(null);

            UploadedFileColumns salaryCol = cols.stream()
                .filter(c -> c.isParse() != null && c.isParse())
                .max(java.util.Comparator.comparingInt(UploadedFileColumns::getColumnPosition))
                .orElse(null);

            if (salaryCol != null && salaryCol.getActualColumn() != null) {
                String idGetter = (idCol != null) ? "get" + idCol.getActualColumn().substring(0, 1).toUpperCase() + idCol.getActualColumn().substring(1) : null;
                String salaryGetter = "get" + salaryCol.getActualColumn().substring(0, 1).toUpperCase() + salaryCol.getActualColumn().substring(1);

                for (UploadedFileData row : fileRows) {
                    try {
                        // Check if it's a valid employee row (ID is not empty)
                        boolean isValidRow = true;
                        if (idGetter != null) {
                            java.lang.reflect.Method mId = UploadedFileData.class.getMethod(idGetter);
                            Object idVal = mId.invoke(row);
                            if (idVal == null || idVal.toString().trim().isEmpty()) {
                                isValidRow = false;
                            }
                        }

                        if (isValidRow) {
                            totalEmployees++;
                            java.lang.reflect.Method mSal = UploadedFileData.class.getMethod(salaryGetter);
                            Object salVal = mSal.invoke(row);
                            if (salVal != null) {
                                if (salVal instanceof Double) {
                                    totalSalary += (Double) salVal;
                                } else {
                                    String strVal = salVal.toString().replace(",", "").trim();
                                    if (!strVal.isEmpty()) {
                                        totalSalary += Double.parseDouble(strVal);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {}
                }
            }
        }

        model.addAttribute("totalEmployees", totalEmployees);
        model.addAttribute("totalSalary", totalSalary);
        model.addAttribute("totalFiles", fileRepo.findByContractorId(contractorId).size());
        System.out.println("[INFO] Dashboard page visited "+time);
        return "contractor/dashboard";
    }
    /*
    ==========================
       Employee Dashboard 
    ==========================
    */
    @GetMapping("/employee/dashboard")
    public String employeeDashboard() {
        System.out.println("[INFO] Dashboard page visited "+time);
        return "employee/dashboard";
    }

}
