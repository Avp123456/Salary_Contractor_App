package com.project.entity;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
	@Table(name = "uploaded_files")
	public class UploadedFiles {
	    @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    private String fileName;
	    private Long size;
	    private LocalDateTime uploadDate;
	    private Integer headerCount;
	    private Integer trailerCount;
	    private Integer totalPayableColumn; // Index of total payable amount column
	    private Integer overtimeTotalAmountColumn;
	    @javax.persistence.Column(name = "file_path")
	    private String filePath;
	    private Long contractorId;
	    
		public Long getContractorId() {
			return contractorId;
		}
		public void setContractorId(Long contractorId) {
			this.contractorId = contractorId;
		}
		public Long getId() {
			return id;
		}
		public void setId(Long id) {
			this.id = id;
		}
		public String getFileName() {
			return fileName;
		}
		public void setFileName(String fileName) {
			this.fileName = fileName;
		}
		public Long getSize() {
			return size;
		}
		public void setSize(Long size) {
			this.size = size;
		}
		public LocalDateTime getUploadDate() {
			return uploadDate;
		}
		public void setUploadDate(LocalDateTime uploadDate) {
			this.uploadDate = uploadDate;
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
		public String getFilePath() {
			return filePath;
		}
		public void setFilePath(String filePath) {
			this.filePath = filePath;
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


