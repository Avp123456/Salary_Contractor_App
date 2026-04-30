package com.project.entity;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
	@Entity
	@Table(name = "file_data")
	public class UploadedFileData {

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
	    
	    private String status = "Pending";
	    private Boolean structureViewed = false;
	    private Boolean payslipGenerated = false;

	    public String getStatus() { return status; }
	    public void setStatus(String status) { this.status = status; }

	    public Boolean getStructureViewed() { return structureViewed; }
	    public void setStructureViewed(Boolean structureViewed) { this.structureViewed = structureViewed; }

	    public Boolean getPayslipGenerated() { return payslipGenerated; }
	    public void setPayslipGenerated(Boolean payslipGenerated) { this.payslipGenerated = payslipGenerated; }

	    // STRING columns
	    private String str1;
	    private String str2;
	    private String str3;
	    private String str4;
	    private String str5;
	    private String str6;
	    private String str7;
	    private String str8;
	    private String str9;
	    private String str10;
	    private String str11;
	    private String str12;
	    private String str13;
	    private String str14;
	    private String str15;
	    private String str16;
	    private String str17;
	    private String str18;
	    private String str19;
	    private String str20;
	    private String str21;
	    private String str22;
	    private String str23;
	    private String str24;
	    private String str25;
	    private String str26;
	    private String str27;
	    private String str28;
	    private String str29;
	    private String str30;
	    private String str31;
	    private String str32;
	    private String str33;
	    private String str34;
	    private String str35;
	    private String str36;
	    private String str37;
	    private String str38;
	    private String str39;
	    private String str40;
	    private String str41;
	    private String str42;
	    private String str43;
	    private String str44;
	    private String str45;
	    private String str46;
	    private String str47;
	    private String str48;
	    private String str49;
	    private String str50;

	    // NUMBER columns
	    private Double num1;
	    private Double num2;
	    private Double num3;
	    private Double num4;
	    private Double num5;
	    private Double num6;
	    private Double num7;
	    private Double num8;
	    private Double num9;
	    private Double num10;
	    private Double num11;
	    private Double num12;
	    private Double num13;
	    private Double num14;
	    private Double num15;
	    private Double num16;
	    private Double num17;
	    private Double num18;
	    private Double num19;
	    private Double num20;
	    private Double num21;
	    private Double num22;
	    private Double num23;
	    private Double num24;
	    private Double num25;
	    private Double num26;
	    private Double num27;
	    private Double num28;
	    private Double num29;
	    private Double num30;
	    private Double num31;
	    private Double num32;
	    private Double num33;
	    private Double num34;
	    private Double num35;
	    private Double num36;
	    private Double num37;
	    private Double num38;
	    private Double num39;
	    private Double num40;
	    private Double num41;
	    private Double num42;
	    private Double num43;
	    private Double num44;
	    private Double num45;
	    private Double num46;
	    private Double num47;
	    private Double num48;
	    private Double num49;
	    private Double num50;
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
		public String getStr1() {
			return str1;
		}
		public void setStr1(String str1) {
			this.str1 = str1;
		}
		public String getStr2() {
			return str2;
		}
		public void setStr2(String str2) {
			this.str2 = str2;
		}
		public String getStr3() {
			return str3;
		}
		public void setStr3(String str3) {
			this.str3 = str3;
		}
		public String getStr4() {
			return str4;
		}
		public void setStr4(String str4) {
			this.str4 = str4;
		}
		public String getStr5() {
			return str5;
		}
		public void setStr5(String str5) {
			this.str5 = str5;
		}
		public String getStr6() {
			return str6;
		}
		public void setStr6(String str6) {
			this.str6 = str6;
		}
		public String getStr7() {
			return str7;
		}
		public void setStr7(String str7) {
			this.str7 = str7;
		}
		public String getStr8() {
			return str8;
		}
		public void setStr8(String str8) {
			this.str8 = str8;
		}
		public String getStr9() {
			return str9;
		}
		public void setStr9(String str9) {
			this.str9 = str9;
		}
		public String getStr10() {
			return str10;
		}
		public void setStr10(String str10) {
			this.str10 = str10;
		}
		public String getStr11() {
			return str11;
		}
		public void setStr11(String str11) {
			this.str11 = str11;
		}
		public String getStr12() {
			return str12;
		}
		public void setStr12(String str12) {
			this.str12 = str12;
		}
		public String getStr13() {
			return str13;
		}
		public void setStr13(String str13) {
			this.str13 = str13;
		}
		public String getStr14() {
			return str14;
		}
		public void setStr14(String str14) {
			this.str14 = str14;
		}
		public String getStr15() {
			return str15;
		}
		public void setStr15(String str15) {
			this.str15 = str15;
		}
		public String getStr16() {
			return str16;
		}
		public void setStr16(String str16) {
			this.str16 = str16;
		}
		public String getStr17() {
			return str17;
		}
		public void setStr17(String str17) {
			this.str17 = str17;
		}
		public String getStr18() {
			return str18;
		}
		public void setStr18(String str18) {
			this.str18 = str18;
		}
		public String getStr19() {
			return str19;
		}
		public void setStr19(String str19) {
			this.str19 = str19;
		}
		public String getStr20() {
			return str20;
		}
		public void setStr20(String str20) {
			this.str20 = str20;
		}
		public String getStr21() {
			return str21;
		}
		public void setStr21(String str21) {
			this.str21 = str21;
		}
		public String getStr22() {
			return str22;
		}
		public void setStr22(String str22) {
			this.str22 = str22;
		}
		public String getStr23() {
			return str23;
		}
		public void setStr23(String str23) {
			this.str23 = str23;
		}
		public String getStr24() {
			return str24;
		}
		public void setStr24(String str24) {
			this.str24 = str24;
		}
		public String getStr25() {
			return str25;
		}
		public void setStr25(String str25) {
			this.str25 = str25;
		}
		public String getStr26() {
			return str26;
		}
		public void setStr26(String str26) {
			this.str26 = str26;
		}
		public String getStr27() {
			return str27;
		}
		public void setStr27(String str27) {
			this.str27 = str27;
		}
		public String getStr28() {
			return str28;
		}
		public void setStr28(String str28) {
			this.str28 = str28;
		}
		public String getStr29() {
			return str29;
		}
		public void setStr29(String str29) {
			this.str29 = str29;
		}
		public String getStr30() {
			return str30;
		}
		public void setStr30(String str30) {
			this.str30 = str30;
		}
		public String getStr31() {
			return str31;
		}
		public void setStr31(String str31) {
			this.str31 = str31;
		}
		public String getStr32() {
			return str32;
		}
		public void setStr32(String str32) {
			this.str32 = str32;
		}
		public String getStr33() {
			return str33;
		}
		public void setStr33(String str33) {
			this.str33 = str33;
		}
		public String getStr34() {
			return str34;
		}
		public void setStr34(String str34) {
			this.str34 = str34;
		}
		public String getStr35() {
			return str35;
		}
		public void setStr35(String str35) {
			this.str35 = str35;
		}
		public String getStr36() {
			return str36;
		}
		public void setStr36(String str36) {
			this.str36 = str36;
		}
		public String getStr37() {
			return str37;
		}
		public void setStr37(String str37) {
			this.str37 = str37;
		}
		public String getStr38() {
			return str38;
		}
		public void setStr38(String str38) {
			this.str38 = str38;
		}
		public String getStr39() {
			return str39;
		}
		public void setStr39(String str39) {
			this.str39 = str39;
		}
		public String getStr40() {
			return str40;
		}
		public void setStr40(String str40) {
			this.str40 = str40;
		}
		public String getStr41() {
			return str41;
		}
		public void setStr41(String str41) {
			this.str41 = str41;
		}
		public String getStr42() {
			return str42;
		}
		public void setStr42(String str42) {
			this.str42 = str42;
		}
		public String getStr43() {
			return str43;
		}
		public void setStr43(String str43) {
			this.str43 = str43;
		}
		public String getStr44() {
			return str44;
		}
		public void setStr44(String str44) {
			this.str44 = str44;
		}
		public String getStr45() {
			return str45;
		}
		public void setStr45(String str45) {
			this.str45 = str45;
		}
		public String getStr46() {
			return str46;
		}
		public void setStr46(String str46) {
			this.str46 = str46;
		}
		public String getStr47() {
			return str47;
		}
		public void setStr47(String str47) {
			this.str47 = str47;
		}
		public String getStr48() {
			return str48;
		}
		public void setStr48(String str48) {
			this.str48 = str48;
		}
		public String getStr49() {
			return str49;
		}
		public void setStr49(String str49) {
			this.str49 = str49;
		}
		public String getStr50() {
			return str50;
		}
		public void setStr50(String str50) {
			this.str50 = str50;
		}
		public Double getNum1() {
			return num1;
		}
		public void setNum1(Double num1) {
			this.num1 = num1;
		}
		public Double getNum2() {
			return num2;
		}
		public void setNum2(Double num2) {
			this.num2 = num2;
		}
		public Double getNum3() {
			return num3;
		}
		public void setNum3(Double num3) {
			this.num3 = num3;
		}
		public Double getNum4() {
			return num4;
		}
		public void setNum4(Double num4) {
			this.num4 = num4;
		}
		public Double getNum5() {
			return num5;
		}
		public void setNum5(Double num5) {
			this.num5 = num5;
		}
		public Double getNum6() {
			return num6;
		}
		public void setNum6(Double num6) {
			this.num6 = num6;
		}
		public Double getNum7() {
			return num7;
		}
		public void setNum7(Double num7) {
			this.num7 = num7;
		}
		public Double getNum8() {
			return num8;
		}
		public void setNum8(Double num8) {
			this.num8 = num8;
		}
		public Double getNum9() {
			return num9;
		}
		public void setNum9(Double num9) {
			this.num9 = num9;
		}
		public Double getNum10() {
			return num10;
		}
		public void setNum10(Double num10) {
			this.num10 = num10;
		}
		public Double getNum11() {
			return num11;
		}
		public void setNum11(Double num11) {
			this.num11 = num11;
		}
		public Double getNum12() {
			return num12;
		}
		public void setNum12(Double num12) {
			this.num12 = num12;
		}
		public Double getNum13() {
			return num13;
		}
		public void setNum13(Double num13) {
			this.num13 = num13;
		}
		public Double getNum14() {
			return num14;
		}
		public void setNum14(Double num14) {
			this.num14 = num14;
		}
		public Double getNum15() {
			return num15;
		}
		public void setNum15(Double num15) {
			this.num15 = num15;
		}
		public Double getNum16() {
			return num16;
		}
		public void setNum16(Double num16) {
			this.num16 = num16;
		}
		public Double getNum17() {
			return num17;
		}
		public void setNum17(Double num17) {
			this.num17 = num17;
		}
		public Double getNum18() {
			return num18;
		}
		public void setNum18(Double num18) {
			this.num18 = num18;
		}
		public Double getNum19() {
			return num19;
		}
		public void setNum19(Double num19) {
			this.num19 = num19;
		}
		public Double getNum20() {
			return num20;
		}
		public void setNum20(Double num20) {
			this.num20 = num20;
		}
		public Double getNum21() {
			return num21;
		}
		public void setNum21(Double num21) {
			this.num21 = num21;
		}
		public Double getNum22() {
			return num22;
		}
		public void setNum22(Double num22) {
			this.num22 = num22;
		}
		public Double getNum23() {
			return num23;
		}
		public void setNum23(Double num23) {
			this.num23 = num23;
		}
		public Double getNum24() {
			return num24;
		}
		public void setNum24(Double num24) {
			this.num24 = num24;
		}
		public Double getNum25() {
			return num25;
		}
		public void setNum25(Double num25) {
			this.num25 = num25;
		}
		public Double getNum26() {
			return num26;
		}
		public void setNum26(Double num26) {
			this.num26 = num26;
		}
		public Double getNum27() {
			return num27;
		}
		public void setNum27(Double num27) {
			this.num27 = num27;
		}
		public Double getNum28() {
			return num28;
		}
		public void setNum28(Double num28) {
			this.num28 = num28;
		}
		public Double getNum29() {
			return num29;
		}
		public void setNum29(Double num29) {
			this.num29 = num29;
		}
		public Double getNum30() {
			return num30;
		}
		public void setNum30(Double num30) {
			this.num30 = num30;
		}
		public Double getNum31() {
			return num31;
		}
		public void setNum31(Double num31) {
			this.num31 = num31;
		}
		public Double getNum32() {
			return num32;
		}
		public void setNum32(Double num32) {
			this.num32 = num32;
		}
		public Double getNum33() {
			return num33;
		}
		public void setNum33(Double num33) {
			this.num33 = num33;
		}
		public Double getNum34() {
			return num34;
		}
		public void setNum34(Double num34) {
			this.num34 = num34;
		}
		public Double getNum35() {
			return num35;
		}
		public void setNum35(Double num35) {
			this.num35 = num35;
		}
		public Double getNum36() {
			return num36;
		}
		public void setNum36(Double num36) {
			this.num36 = num36;
		}
		public Double getNum37() {
			return num37;
		}
		public void setNum37(Double num37) {
			this.num37 = num37;
		}
		public Double getNum38() {
			return num38;
		}
		public void setNum38(Double num38) {
			this.num38 = num38;
		}
		public Double getNum39() {
			return num39;
		}
		public void setNum39(Double num39) {
			this.num39 = num39;
		}
		public Double getNum40() {
			return num40;
		}
		public void setNum40(Double num40) {
			this.num40 = num40;
		}
		public Double getNum41() {
			return num41;
		}
		public void setNum41(Double num41) {
			this.num41 = num41;
		}
		public Double getNum42() {
			return num42;
		}
		public void setNum42(Double num42) {
			this.num42 = num42;
		}
		public Double getNum43() {
			return num43;
		}
		public void setNum43(Double num43) {
			this.num43 = num43;
		}
		public Double getNum44() {
			return num44;
		}
		public void setNum44(Double num44) {
			this.num44 = num44;
		}
		public Double getNum45() {
			return num45;
		}
		public void setNum45(Double num45) {
			this.num45 = num45;
		}
		public Double getNum46() {
			return num46;
		}
		public void setNum46(Double num46) {
			this.num46 = num46;
		}
		public Double getNum47() {
			return num47;
		}
		public void setNum47(Double num47) {
			this.num47 = num47;
		}
		public Double getNum48() {
			return num48;
		}
		public void setNum48(Double num48) {
			this.num48 = num48;
		}
		public Double getNum49() {
			return num49;
		}
		public void setNum49(Double num49) {
			this.num49 = num49;
		}
		public Double getNum50() {
			return num50;
		}
		public void setNum50(Double num50) {
			this.num50 = num50;
		}
	    
	    
	    
	    
	}
