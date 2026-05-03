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
import org.springframework.web.multipart.MultipartFile;

import com.project.entity.Contractor;
import com.project.entity.UploadedFileColumns;
import com.project.entity.UploadedFileData;
import com.project.entity.UploadedFiles;
import com.project.entity.ReportConfiguration;
import com.project.entity.ReportConfigurationColumn;
import com.project.repository.UploadedFileColumnsRepository;
import com.project.repository.UploadedFileDataRepository;
import com.project.repository.UploadedFileRepository;
import com.project.repository.ReportConfigurationRepository;
import com.project.repository.ReportConfigurationColumnRepository;
import com.project.service.ExcelService;

import org.springframework.beans.factory.annotation.Value;
import java.nio.file.*;
import java.io.IOException;
import javax.servlet.http.HttpSession;
@Controller
public class FilesController {
	 //time stamp
    String time = java.time.LocalDateTime.now().toString();
    
    @Autowired
    private UploadedFileDataRepository dataRepo;
    
    @Autowired
    private UploadedFileColumnsRepository columnRepo;
    
    @Autowired
    private UploadedFileRepository fileRepo;
    
    @Autowired
    private ReportConfigurationRepository configRepo;
    
    @Autowired
    private ReportConfigurationColumnRepository configColRepo;

    @Autowired
    private ExcelService excelService;

    @Value("${app.upload.dir}")
    private String uploadDir;
    
    private Long getCurrentContractorId(HttpSession session) {
        Contractor contractor = (Contractor) session.getAttribute("loggedInContractor");
        return (contractor != null) ? contractor.getContractorId() : null;
    }
    
    @PostMapping("/contractor/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file, HttpSession session) {
        List<List<String>> data = excelService.parseExcel(file);
        session.setAttribute("excelData", data);

        Long contractorId = getCurrentContractorId(session);
        String originalName = file.getOriginalFilename();
        
        // Ensure upload directory exists
        try {
            Files.createDirectories(Paths.get(uploadDir));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Check if a file with the same name already exists for this contractor
        UploadedFiles existing = fileRepo.findFirstByContractorIdAndFileNameOrderByUploadDateDesc(contractorId, originalName);
        
        String savedPath = uploadDir + contractorId + "_" + System.currentTimeMillis() + "_" + originalName;

        if (existing != null) {
            // Delete old file if exists
            if (existing.getFilePath() != null) {
                try { Files.deleteIfExists(Paths.get(existing.getFilePath())); } catch (Exception e) {}
            }
            
            // Save new file to disk
            try {
                Files.write(Paths.get(savedPath), file.getBytes());
                existing.setFilePath(savedPath);
            } catch (Exception e) { e.printStackTrace(); }
            
            existing.setSize(file.getSize());
            existing.setUploadDate(java.time.LocalDateTime.now());
            fileRepo.save(existing);
            System.out.println("[ACTION] Duplicate file updated on disk. "+ time);

        } else {
            // New file — create fresh record
            UploadedFiles fileObj = new UploadedFiles();
            fileObj.setFileName(originalName);
            fileObj.setSize(file.getSize());
            fileObj.setUploadDate(java.time.LocalDateTime.now());
            fileObj.setContractorId(contractorId);
            
            // Save to disk
            try {
                Files.write(Paths.get(savedPath), file.getBytes());
                fileObj.setFilePath(savedPath);
            } catch (Exception e) { e.printStackTrace(); }
            
            fileRepo.save(fileObj);
            System.out.println("[INFO] Uploaded File Saved to disk");
        }
        System.out.println("[INFO] Reports Page Visited");

        return "redirect:/contractor/files";
    }
    
    @GetMapping("/contractor/files")
    public String reports(Model model, HttpSession session) {
        Long contractorId = getCurrentContractorId(session);
        model.addAttribute("files", fileRepo.findByContractorId(contractorId));
        System.out.println("[INFO] Files Page Visited");
        return "contractor/files";
    }
    
    @PostMapping("/contractor/save-columns-and-config")
    @ResponseBody
    public String saveColumnsAndConfig(@RequestBody java.util.Map<String, Object> payload, HttpSession session) {
        Object fileIdObj = payload.get("fileId");
        if (fileIdObj == null) return "Error: Missing fileId";
        
        Long fileId = Long.valueOf(fileIdObj.toString());
        int headerCount = payload.get("headerCount") != null ? Integer.parseInt(payload.get("headerCount").toString()) : 0;
        int trailerCount = payload.get("trailerCount") != null ? Integer.parseInt(payload.get("trailerCount").toString()) : 0;
        int totalPayableColumn = payload.get("totalPayableColumn") != null ? Integer.parseInt(payload.get("totalPayableColumn").toString()) : 0;
        List<java.util.Map<String, Object>> columnData = (List<java.util.Map<String, Object>>) payload.get("columns");
        
        Long contractorId = getCurrentContractorId(session);
        UploadedFiles file = fileRepo.findById(fileId).orElse(null);
        if (file == null || !file.getContractorId().equals(contractorId)) return "Error: Unauthorized";
        
        file.setHeaderCount(headerCount);
        file.setTrailerCount(trailerCount);
        file.setTotalPayableColumn(totalPayableColumn);
        fileRepo.save(file);

        columnRepo.deleteByFileId(fileId);
        List<UploadedFileColumns> columns = new java.util.ArrayList<>();
        int strCount = 1;
        int numCount = 1;

        for (java.util.Map<String, Object> colMap : columnData) {
            UploadedFileColumns c = new UploadedFileColumns();
            c.setFileId(fileId);
            c.setContractorId(contractorId);
            c.setColumnName(colMap.get("columnName").toString());
            c.setDataType(colMap.get("dataType").toString());
            c.setColumnPosition(Integer.parseInt(colMap.get("columnPosition").toString()));
            c.setParse(Boolean.parseBoolean(colMap.get("parse").toString()));
            c.setSalaryType(colMap.get("salaryType") != null ? colMap.get("salaryType").toString() : null);
            
            if (c.isParse() != null && c.isParse()) {
                if ("STRING".equalsIgnoreCase(c.getDataType())) {
                    c.setActualColumn("str" + strCount++);
                } else {
                    c.setActualColumn("num" + numCount++);
                }
            }
            columns.add(c);
        }
        columnRepo.saveAll(columns);
        System.out.println("[INFO]Columns Added");
        session.setAttribute("fileId", fileId);
        return "OK";
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
    
    
    
    @PostMapping("/contractor/save-data")
    public String saveData(HttpSession session) {
        List<List<String>> data = (List<List<String>>) session.getAttribute("processedData");
        Long fileId = (Long) session.getAttribute("fileId");
        Long contractorId = getCurrentContractorId(session);
        List<UploadedFileColumns> columns = columnRepo.findByFileIdAndContractorId(fileId, contractorId);
        // CRITICAL: Sort columns by position to match the order in processedData
        columns.sort(java.util.Comparator.comparingInt(UploadedFileColumns::getColumnPosition));
        
        if (data == null && fileId != null) {
            UploadedFiles file = fileRepo.findById(fileId).orElse(null);
            if (file == null || !file.getContractorId().equals(contractorId)) return "redirect:/contractor/files";
            List<List<String>> excelData = (List<List<String>>) session.getAttribute("excelData");
            if (excelData == null && file != null && file.getFilePath() != null) {
                try {
                    byte[] fileBytes = Files.readAllBytes(Paths.get(file.getFilePath()));
                    excelData = excelService.parseExcel(fileBytes);
                } catch (IOException e) { e.printStackTrace(); }
            }
            if (excelData != null && file != null) {
                data = new java.util.ArrayList<>();
                int start = file.getHeaderCount() != null ? file.getHeaderCount() : 0;
                int end = excelData.size() - (file.getTrailerCount() != null ? file.getTrailerCount() : 0);
                for (int i = start; i < end && i < excelData.size(); i++) {
                    List<String> row = excelData.get(i);
                    List<String> newRow = new java.util.ArrayList<>();
                    for (UploadedFileColumns col : columns) {
                        if (col.isParse() != null && col.isParse()) {
                            int pos = col.getColumnPosition() - 1;
                            String val = pos >= 0 && pos < row.size() ? row.get(pos) : "";
                            newRow.add(val);
                        }
                    }
                    boolean allEmpty = true;
                    for(String s : newRow) if(s != null && !s.trim().isEmpty()) { allEmpty = false; break; }
                    if(!allEmpty) data.add(newRow);
                }
            }
        }

        List<UploadedFileColumns> parseColumns = new java.util.ArrayList<>();
        for(UploadedFileColumns c : columns) if(c.isParse() != null && c.isParse()) parseColumns.add(c);

        // Delete existing data for this file before saving to avoid duplicates
        dataRepo.deleteByFileId(fileId);

        // Identify the ID column to skip rows with empty IDs (totals)
        final UploadedFileColumns idCol = parseColumns.stream()
            .filter(c -> {
                String name = c.getColumnName().toUpperCase();
                return name.contains("ID") || name.contains("CODE") || name.contains("EMP");
            })
            .min(java.util.Comparator.comparingInt(UploadedFileColumns::getColumnPosition))
            .orElse(null);
        
        int idIdx = -1;
        if (idCol != null) {
            for (int i = 0; i < parseColumns.size(); i++) {
                if (parseColumns.get(i).getColumnPosition() == idCol.getColumnPosition()) {
                    idIdx = i;
                    break;
                }
            }
        }

        if (data == null) return "redirect:/contractor/files";
        for (List<String> row : data) {
            // Skip rows where ID is empty (usually these are 'Total' rows in Excel)
            if (idIdx != -1) {
                String idVal = idIdx < row.size() ? row.get(idIdx) : "";
                if (idVal == null || idVal.trim().isEmpty() || idVal.trim().equals("0") || idVal.trim().equals("0.0")) {
                    continue;
                }
            }

            UploadedFileData d = new UploadedFileData();
            d.setFileId(fileId);
            d.setContractorId(contractorId);

            for (int i = 0; i < parseColumns.size(); i++) {
                UploadedFileColumns col = parseColumns.get(i);
                String value = i < row.size() ? row.get(i) : "";
                if (col.getActualColumn().startsWith("str")) {
                    setString(d, col.getActualColumn(), value);
                } else if (col.getActualColumn().startsWith("num")) {
                    setNumber(d, col.getActualColumn(), value);
                }
            }
            dataRepo.save(d);
        }
        System.out.println("[INFO] Data Saved for file: " + fileId);
        return "redirect:/contractor/files?success=Data confirmed and saved successfully!";
    }
    
  
    
    @GetMapping("/contractor/columns")
    public String columns(@RequestParam Long fileId, Model model, HttpSession session) {
        Long contractorId = getCurrentContractorId(session);
        UploadedFiles file = fileRepo.findById(fileId).orElse(null);
        
        if (file == null || !file.getContractorId().equals(contractorId)) {
        	 System.out.println("[INFO] Files Page Visited "+time);
            return "redirect:/contractor/files";
        }

        List<UploadedFileColumns> existingColumns = columnRepo.findByFileIdAndContractorId(fileId, contractorId);
        List<ReportConfiguration> configurations = configRepo.findByContractorId(contractorId);
        
        model.addAttribute("fileId", fileId);
        model.addAttribute("headerCount", file.getHeaderCount() != null ? file.getHeaderCount() : 0);
        model.addAttribute("trailerCount", file.getTrailerCount() != null ? file.getTrailerCount() : 0);
        model.addAttribute("totalPayableColumn", file.getTotalPayableColumn() != null ? file.getTotalPayableColumn() : 0);
        model.addAttribute("existingColumns", existingColumns);
        model.addAttribute("configurations", configurations);
        
        session.setAttribute("fileId", fileId);
        return "contractor/columns";
    }
    
    
    @GetMapping("/contractor/preview")
    public String preview(Model model, HttpSession session) {
        Long fileId = (Long) session.getAttribute("fileId");
        Long contractorId = getCurrentContractorId(session);
        UploadedFiles file = fileRepo.findById(fileId).orElse(null);
        if (file == null || !file.getContractorId().equals(contractorId)) return "redirect:/contractor/files";
        
        List<List<String>> excelData = (List<List<String>>) session.getAttribute("excelData");
        
        System.out.println("Preview - File ID: " + fileId);
        if (file != null) {
            System.out.println("File found in DB. Path exists? " + (file.getFilePath() != null));
        }

        // Always re-parse if we have the file on disk to ensure latest logic is applied
        if (file != null && file.getFilePath() != null) {
            try {
                byte[] fileBytes = Files.readAllBytes(Paths.get(file.getFilePath()));
                excelData = excelService.parseExcel(fileBytes);
                session.setAttribute("excelData", excelData);
            } catch (IOException e) { e.printStackTrace(); }
        }
        
        if (excelData == null) {
System.out.println("[INFO] Report Page Visited "+time);
            return "redirect:/contractor/files?error=Excel data not found. Please re-upload.";
        }
        
        List<UploadedFileColumns> columns = columnRepo.findByFileId(fileId);
        // Sort columns by position (Excel) so they appear in order from left to right
        columns.sort(java.util.Comparator.comparingInt(UploadedFileColumns::getColumnPosition));

        List<List<String>> filteredData = new java.util.ArrayList<>();
        if (excelData != null && file != null) {
            int start = file.getHeaderCount() != null ? file.getHeaderCount() : 0;
            int end = excelData.size() - (file.getTrailerCount() != null ? file.getTrailerCount() : 0);
            
            System.out.println("Processing file: " + file.getFileName());
            System.out.println("Excel Data Size: " + excelData.size());
            System.out.println("Start: " + start + ", End: " + end);

            for (int i = start; i < end && i < excelData.size(); i++) {
                List<String> row = excelData.get(i);
                
                // Check if the whole original row is empty
                boolean originalRowEmpty = true;
                for (String s : row) {
                    if (s != null && !s.trim().isEmpty()) {
                        originalRowEmpty = false;
                        break;
                    }
                }
                if (originalRowEmpty) continue; // Skip truly empty rows

                List<String> newRow = new java.util.ArrayList<>();
                for (UploadedFileColumns col : columns) {
                    if (col.isParse() != null && col.isParse()) {
                        int pos = col.getColumnPosition() - 1;
                        String value = pos >= 0 && pos < row.size() ? row.get(pos) : "";
                        newRow.add(value);
                    }
                }
                filteredData.add(newRow);
            }
            System.out.println("Filtered Data Rows: " + filteredData.size());
        } else {
            System.out.println("ExcelData or File is NULL. excelData null? " + (excelData == null) + " file null? " + (file == null));
        }

        List<UploadedFileColumns> displayColumns = new java.util.ArrayList<>();
        for(UploadedFileColumns c : columns) if(c.isParse() != null && c.isParse()) displayColumns.add(c);

        model.addAttribute("data", filteredData);
        model.addAttribute("columns", displayColumns);
        session.setAttribute("processedData", filteredData);
        System.out.println("[INFO] Preview Page Visited "+time);
        return "contractor/preview";
    }

    @GetMapping("/contractor/delete-file/{id}")
    public String deleteFile(@PathVariable Long id, HttpSession session) {
        Long contractorId = getCurrentContractorId(session);
        UploadedFiles file = fileRepo.findById(id).orElse(null);
        if (file != null && contractorId != null && contractorId.equals(file.getContractorId())) {
            // Delete from disk
            if (file.getFilePath() != null) {
                try { Files.deleteIfExists(Paths.get(file.getFilePath())); } catch (Exception e) {}
            }
            dataRepo.deleteByFileId(id);
            columnRepo.deleteByFileId(id);
            fileRepo.delete(file);
            System.out.println("[INFO] File and data deleted from disk and DB. "+time);

        }
        System.out.println("[INFO] Files Page Visited "+time);

        return "redirect:/contractor/files";
    }

    @GetMapping("/contractor/configurations")
    public String configurations(Model model, HttpSession session) {
        Long contractorId = getCurrentContractorId(session);
        model.addAttribute("configurations", configRepo.findByContractorId(contractorId));
        return "contractor/configuration";
    }

    @PostMapping("/contractor/save-configuration")
    @ResponseBody
    public String saveConfiguration(@RequestBody java.util.Map<String, Object> payload, HttpSession session) {
        Long contractorId = getCurrentContractorId(session);
        Object idObj = payload.get("id");
        Long id = (idObj != null) ? Long.valueOf(idObj.toString()) : null;
        
        String name = payload.get("configName").toString();
        int header = Integer.parseInt(payload.get("headerCount").toString());
        int trailer = Integer.parseInt(payload.get("trailerCount").toString());
        int totalPos = Integer.parseInt(payload.get("totalPayableColumn").toString());
        List<java.util.Map<String, Object>> columns = (List<java.util.Map<String, Object>>) payload.get("columns");

        ReportConfiguration config;
        if (id != null) {
            config = configRepo.findById(id).orElse(new ReportConfiguration());
            // Clear existing columns for update
            configColRepo.deleteByConfigId(id);
        } else {
            config = new ReportConfiguration();
        }

        config.setContractorId(contractorId);
        config.setConfigName(name);
        config.setHeaderCount(header);
        config.setTrailerCount(trailer);
        config.setTotalPayableColumn(totalPos);
        configRepo.save(config);

        for (java.util.Map<String, Object> colMap : columns) {
            ReportConfigurationColumn col = new ReportConfigurationColumn();
            col.setConfigId(config.getId());
            col.setColumnName(colMap.get("columnName").toString());
            col.setDataType(colMap.get("dataType").toString());
            col.setColumnPosition(Integer.parseInt(colMap.get("columnPosition").toString()));
            col.setSalaryType(colMap.get("salaryType") != null ? colMap.get("salaryType").toString() : null);
            configColRepo.save(col);
        }

        return "OK";
    }

    @GetMapping("/contractor/get-configuration/{id}")
    @ResponseBody
    public java.util.Map<String, Object> getConfiguration(@PathVariable Long id, HttpSession session) {
        Long contractorId = getCurrentContractorId(session);
        ReportConfiguration config = configRepo.findById(id).orElse(null);
        if (config == null || !config.getContractorId().equals(contractorId)) return null;

        java.util.Map<String, Object> res = new java.util.HashMap<>();
        res.put("configName", config.getConfigName());
        res.put("headerCount", config.getHeaderCount());
        res.put("trailerCount", config.getTrailerCount());
        res.put("totalPayableColumn", config.getTotalPayableColumn());
        List<ReportConfigurationColumn> cols = configColRepo.findByConfigId(id);
        res.put("columns", cols);
        return res;
    }

    @GetMapping("/contractor/delete-configuration/{id}")
    public String deleteConfiguration(@PathVariable Long id, HttpSession session) {
        Long contractorId = getCurrentContractorId(session);
        ReportConfiguration config = configRepo.findById(id).orElse(null);
        if (config != null && config.getContractorId().equals(contractorId)) {
            configColRepo.deleteByConfigId(id);
            configRepo.delete(config);
        }
        return "redirect:/contractor/configurations";
    }
}
