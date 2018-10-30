package com.rns.web.edo.service.domain;

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class EdoStudentFirebase {
	
	@JsonProperty("Class")
	private String className;
	@JsonProperty("Div")
	private String division;
	private Integer id;
	@JsonProperty("Name")
	private String name;
	private String email;
	@JsonProperty("RollNo")
	private String rollNo;
	private EDOStudentAnalysis analysis;
	@JsonProperty("StudentContact")
	private String phone;
	private String password;
	private List<EDOPackage> packages;
	private Integer transactionId;
	private EdoPaymentStatus payment;
	private String gender;
	private String schoolDistrict;
	private String dob;
	private String casteCategory;
	private String examMode;
	private EDOPackage currentPackage;
	@JsonProperty("profilePicURL")
	private String profilePic;
	@JsonProperty("ParentContact")
	private String parentMobileNo;
	private String instituteId;
	
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public String getDivision() {
		return division;
	}
	public void setDivision(String division) {
		this.division = division;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	@JsonProperty("Name")
	public String getName() {
		return name;
	}
	@JsonProperty("StudentContact")
	public void setName(String name) {
		this.name = name;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getRollNo() {
		return rollNo;
	}
	public void setRollNo(String rollNo) {
		this.rollNo = rollNo;
	}
	public EDOStudentAnalysis getAnalysis() {
		return analysis;
	}
	public void setAnalysis(EDOStudentAnalysis analysis) {
		this.analysis = analysis;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public List<EDOPackage> getPackages() {
		return packages;
	}
	public void setPackages(List<EDOPackage> packages) {
		this.packages = packages;
	}
	public Integer getTransactionId() {
		return transactionId;
	}
	public void setTransactionId(Integer transactionId) {
		this.transactionId = transactionId;
	}
	public EdoPaymentStatus getPayment() {
		return payment;
	}
	public void setPayment(EdoPaymentStatus payment) {
		this.payment = payment;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	public String getSchoolDistrict() {
		return schoolDistrict;
	}
	public void setSchoolDistrict(String schoolDistrict) {
		this.schoolDistrict = schoolDistrict;
	}
	public String getDob() {
		return dob;
	}
	public void setDob(String dob) {
		this.dob = dob;
	}
	public String getCasteCategory() {
		return casteCategory;
	}
	public void setCasteCategory(String casteCategory) {
		this.casteCategory = casteCategory;
	}
	public String getExamMode() {
		return examMode;
	}
	public void setExamMode(String examMode) {
		this.examMode = examMode;
	}
	public EDOPackage getCurrentPackage() {
		return currentPackage;
	}
	public void setCurrentPackage(EDOPackage currentPackage) {
		this.currentPackage = currentPackage;
	}
	public String getProfilePic() {
		return profilePic;
	}
	public void setProfilePic(String profilePic) {
		this.profilePic = profilePic;
	}
	public String getParentMobileNo() {
		return parentMobileNo;
	}
	public void setParentMobileNo(String parentMobileNo) {
		this.parentMobileNo = parentMobileNo;
	}
	public String getInstituteId() {
		return instituteId;
	}
	public void setInstituteId(String instituteId) {
		this.instituteId = instituteId;
	}
	

}
