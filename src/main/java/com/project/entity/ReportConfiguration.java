package com.project.entity;

import javax.persistence.*;
import java.util.List;
import java.time.LocalDateTime;

@Entity
@Table(name = "report_configurations")
public class ReportConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long contractorId;
    private String configName;
    private String month;
    private Integer year;
    private Integer headerCount;
    private Integer trailerCount;
    private Integer totalPayableColumn;
    private Integer overtimeTotalAmountColumn;
    private LocalDateTime lastUpdated;

    @PrePersist
    @PreUpdate
    public void onUpdate() {
        this.lastUpdated = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getContractorId() {
        return contractorId;
    }

    public void setContractorId(Long contractorId) {
        this.contractorId = contractorId;
    }

    public String getConfigName() {
        return configName;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
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

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
