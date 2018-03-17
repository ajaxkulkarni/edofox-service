package com.rns.web.edo.service.domain;

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class EdoStudent {

	private Integer id;
	private String name;
	private String email;
	private String rollNo;
	private EDOStudentAnalysis analysis;
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

	public EdoStudent() {

	}

	public EdoStudent(Integer studenId) {
		setId(studenId);
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

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
	
}
