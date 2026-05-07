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
    
    private Long getCurrentContractorId(HttpSession session) {
        Contractor contractor = (Contractor) session.getAttribute("loggedInContractor");
        return (contractor != null) ? contractor.getContractorId() : null;
    }

    @GetMapping("/contractor/salary")
    public String salary(Model model, HttpSession session) {
        Long contractorId = getCurrentContractorId(session);
        model.addAttribute("files", fileRepo.findByContractorId(contractorId));
        System.out.println("[INFO] Salary Page Visited "+getTime());

        return "contractor/salary";
    }

    @GetMapping("/contractor/show-salary-data")
    public String showSalaryData(@RequestParam Long fileId, Model model, HttpSession session) {
        Long contractorId = getCurrentContractorId(session);
        UploadedFiles file = fileRepo.findById(fileId).orElse(null);
        if (file == null || !file.getContractorId().equals(contractorId)) {
            System.out.println("[INFO]Salary Page Visited "+getTime());

        	return"redirect:/contractor/salary";}

        List<UploadedFileColumns> columns = columnRepo.findByFileId(fileId);
        columns.sort(java.util.Comparator.comparingInt(UploadedFileColumns::getColumnPosition));
        
        List<UploadedFileColumns> displayColumns = new java.util.ArrayList<>();
        for(UploadedFileColumns c : columns) if(c.isParse() != null && c.isParse()) displayColumns.add(c);

        List<UploadedFileData> dataList = dataRepo.findByFileId(fileId);
        List<List<String>> rows = new java.util.ArrayList<>();
        // Identify ID column for filtering
        final UploadedFileColumns idCol = displayColumns.stream()
            .filter(c -> {
                String name = c.getColumnName().toUpperCase();
                return name.contains("ID") || name.contains("CODE") || name.contains("EMP");
            })
            .min(java.util.Comparator.comparingInt(UploadedFileColumns::getColumnPosition))
            .orElse(null);

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

        model.addAttribute("files", fileRepo.findByContractorId(contractorId));
        model.addAttribute("selectedFileId", fileId);
        model.addAttribute("columns", displayColumns);
        model.addAttribute("rows", rows);
        System.out.println("[INFO] Salary Page Visited "+getTime());

        return "contractor/salary";
    }


}
