package com.project.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.project.entity.Contractor;
import com.project.entity.UploadedFileColumns;
import com.project.entity.UploadedFileData;
import com.project.entity.UploadedFiles;
import com.project.repository.UploadedFileColumnsRepository;
import com.project.repository.UploadedFileDataRepository;
import com.project.repository.UploadedFileRepository;
import com.project.repository.ReportConfigurationRepository;

import javax.servlet.http.HttpSession;
@Controller
public class SalaryController {
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
    
    private Long getCurrentContractorId(HttpSession session) {
        Contractor contractor = (Contractor) session.getAttribute("loggedInContractor");
        return (contractor != null) ? contractor.getContractorId() : null;
    }

    @GetMapping("/contractor/salary")
    public String salary(Model model, HttpSession session) {
        System.out.println("[Page Visited]:- Contractor Salary");
        Long contractorId = getCurrentContractorId(session);
        model.addAttribute("configurations", configRepo.findByContractorId(contractorId));
        System.out.println("[INFO] Salary Page Visited "+getTime());

        return "contractor/salary";
    }

    @GetMapping("/contractor/show-salary-data")
    public String showSalaryData(@RequestParam(required = false) Long fileId, 
                                 @RequestParam(required = false) Long configId,
                                 Model model, HttpSession session) {
        System.out.println("[Button clicked]:- Show Salary Data");
        Long contractorId = getCurrentContractorId(session);
        List<UploadedFileColumns> columns = null;
        List<UploadedFileData> dataList = null;

        String selectedConfigName = "";
        if (configId != null) {
            com.project.entity.ReportConfiguration config = configRepo.findById(configId).orElse(null);
            if (config == null || !config.getContractorId().equals(contractorId)) return "redirect:/contractor/salary";
            selectedConfigName = config.getConfigName();
            
            dataList = dataRepo.findByConfigId(configId);
            columns = columnRepo.findByConfigId(configId);
            
            if (columns.isEmpty() && !dataList.isEmpty()) {
                columns = columnRepo.findByFileId(dataList.get(0).getFileId());
            }
            
            if (columns.isEmpty()) {
                model.addAttribute("error", "No column mappings found for configuration: " + selectedConfigName + ". Please go to 'Configuration' -> 'Preview' -> 'Confirm & Save' to generate data.");
            }
        } else if (fileId != null) {
            UploadedFiles file = fileRepo.findById(fileId).orElse(null);
            if (file == null || !file.getContractorId().equals(contractorId)) return "redirect:/contractor/salary";
            columns = columnRepo.findByFileId(fileId);
            dataList = dataRepo.findByFileId(fileId);
        }

        if (columns == null || columns.isEmpty()) {
            model.addAttribute("configurations", configRepo.findByContractorId(contractorId));
            model.addAttribute("selectedConfigId", configId);
            return "contractor/salary";
        }

        columns.sort(java.util.Comparator.comparingInt(UploadedFileColumns::getColumnPosition));
        
        List<UploadedFileColumns> displayColumns = new java.util.ArrayList<>();
        for(UploadedFileColumns c : columns) {
            if(c.isParse() != null && c.isParse()) {
                displayColumns.add(c);
            }
        }

        List<List<String>> rows = new java.util.ArrayList<>();
        // Identify the primary key column (isKey=true) or fall back to ID/CODE columns
        UploadedFileColumns idCol = displayColumns.stream()
            .filter(c -> c.getIsKey() != null && c.getIsKey())
            .findFirst()
            .orElse(null);

        if (idCol == null) {
            idCol = displayColumns.stream()
                .filter(c -> {
                    String name = c.getColumnName().toUpperCase();
                    return (name.contains("EMP") || name.contains("EMPLOYEE")) && (name.contains("ID") || name.contains("CODE"));
                })
                .min(java.util.Comparator.comparingInt(UploadedFileColumns::getColumnPosition))
                .orElse(null);
        }

        if (idCol == null) {
            idCol = displayColumns.stream()
                .filter(c -> {
                    String name = c.getColumnName().toUpperCase();
                    return name.contains("ID") || name.contains("CODE");
                })
                .min(java.util.Comparator.comparingInt(UploadedFileColumns::getColumnPosition))
                .orElse(null);
        }

        for (UploadedFileData data : dataList) {
            // Skip rows where ID is empty (usually these are 'Total' rows in Excel)
            if (idCol != null) {
                try {
                    java.lang.reflect.Method method = UploadedFileData.class.getMethod("get" + idCol.getActualColumn().substring(0, 1).toUpperCase() + idCol.getActualColumn().substring(1));
                    Object val = method.invoke(data);
                    String sVal = val != null ? val.toString().trim() : "";
                    if (sVal.isEmpty() || sVal.equals("0") || sVal.equals("0.0")) continue;
                } catch (Exception e) {}
            }
            List<String> row = new java.util.ArrayList<>();
            for (UploadedFileColumns col : displayColumns) {
                String value = "";
                try {
                    if (col.getActualColumn().startsWith("str")) {
                        java.lang.reflect.Method method = UploadedFileData.class.getMethod("get" + col.getActualColumn().substring(0, 1).toUpperCase() + col.getActualColumn().substring(1));
                        Object val = method.invoke(data);
                        value = val != null ? val.toString() : "";
                    } else if (col.getActualColumn().startsWith("num")) {
                        java.lang.reflect.Method method = UploadedFileData.class.getMethod("get" + col.getActualColumn().substring(0, 1).toUpperCase() + col.getActualColumn().substring(1));
                        Object val = method.invoke(data);
                        if (val != null) {
                            Double d = (Double) val;
                            if (d == d.longValue()) value = String.format("%d", d.longValue());
                            else value = String.format("%s", d);
                        }
                    }
                } catch (Exception e) {}
                row.add(value);
            }
            rows.add(row);
        }

        model.addAttribute("configurations", configRepo.findByContractorId(contractorId));
        model.addAttribute("selectedConfigId", configId);
        model.addAttribute("selectedFileId", fileId);
        model.addAttribute("columns", displayColumns);
        model.addAttribute("rows", rows);
        return "contractor/salary";
    }


}
