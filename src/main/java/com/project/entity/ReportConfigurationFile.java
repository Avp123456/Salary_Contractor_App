package com.project.entity;

import javax.persistence.*;

@Entity
@Table(name = "report_configuration_files")
public class ReportConfigurationFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long configId;
    private String fileName;
    
    private Integer headerCount;
    private Integer trailerCount;
    private Integer totalPayableColumn;
    private Integer overtimeTotalAmountColumn;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getConfigId() {
        return configId;
    }

    public void setConfigId(Long configId) {
        this.configId = configId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Integer getHeaderCount() {
        return headerCount;
    }

    public void setHeaderCount(Integer headerCount) {
        this.headerCount = headerCount;
    }

    public Integer getTrailerCount() {
        return trailerCount;
    }

    public void setTrailerCount(Integer trailerCount) {
        this.trailerCount = trailerCount;
    }

    public Integer getTotalPayableColumn() {
        return totalPayableColumn;
    }

    public void setTotalPayableColumn(Integer totalPayableColumn) {
        this.totalPayableColumn = totalPayableColumn;
    }

    public Integer getOvertimeTotalAmountColumn() {
        return overtimeTotalAmountColumn;
    }

    public void setOvertimeTotalAmountColumn(Integer overtimeTotalAmountColumn) {
        this.overtimeTotalAmountColumn = overtimeTotalAmountColumn;
    }
}
