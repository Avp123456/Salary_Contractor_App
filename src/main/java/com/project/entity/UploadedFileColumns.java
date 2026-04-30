package com.project.entity;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
	@Entity
	@Table(name = "uploaded_file_columns")
	public class UploadedFileColumns {

	    @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    private Long fileId;
	    private Long contractorId;
	    
	    public Long getContractorId() {
	    	return contractorId;
	    }
	    public void setContractorId(Long contractorId) {
	    	this.contractorId = contractorId;
	    }

	    private String columnName;       // user given name
	    private int columnPosition;      // excel index
	    private String dataType;         // STRING / NUMBER
	    @javax.persistence.Column(name = "actual_col_name")
	    private String actualColumn;     // str1 / num1
	    private Boolean parse;           // include in parsing
		public Long getId() {
			return id;
		}
		public void setId(Long id) {
			this.id = id;
		}
		public Long getFileId() {
			return fileId;
		}
		public void setFileId(Long fileId) {
			this.fileId = fileId;
		}
		public String getColumnName() {
			return columnName;
		}
		public void setColumnName(String columnName) {
			this.columnName = columnName;
		}
		public int getColumnPosition() {
			return columnPosition;
		}
		public void setColumnPosition(int columnPosition) {
			this.columnPosition = columnPosition;
		}
		public String getDataType() {
			return dataType;
		}
		public void setDataType(String dataType) {
			this.dataType = dataType;
		}
		public String getActualColumn() {
			return actualColumn;
		}
		public void setActualColumn(String actualColumn) {
			this.actualColumn = actualColumn;
		}
		public Boolean isParse() {
			return parse;
		}
		public void setParse(Boolean parse) {
			this.parse = parse;
		}
	    
	    
	}

