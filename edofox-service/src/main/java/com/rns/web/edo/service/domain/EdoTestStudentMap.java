package com.rns.web.edo.service.domain;

import java.util.Date;

public class EdoTestStudentMap {

	private EdoTest test;
	private EdoStudent student;
	private String status;
	private Integer mapId;
	private Date registerDate;
	private String studentAccess;
	private EdoStudentSubjectAnalysis subjectScore;
	private String testEndDateString;
	
	public EdoTest getTest() {
		return test;
	}
	public void setTest(EdoTest test) {
		this.test = test;
	}
	public EdoStudent getStudent() {
		return student;
	}
	public void setStudent(EdoStudent student) {
		this.student = student;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Integer getMapId() {
		return mapId;
	}
	public void setMapId(Integer mapId) {
		this.mapId = mapId;
	}
	public Date getRegisterDate() {
		return registerDate;
	}
	public void setRegisterDate(Date registerDate) {
		this.registerDate = registerDate;
	}
	public String getStudentAccess() {
		return studentAccess;
	}
	public void setStudentAccess(String studentAccess) {
		this.studentAccess = studentAccess;
	}
	public EdoStudentSubjectAnalysis getSubjectScore() {
		return subjectScore;
	}
	public void setSubjectScore(EdoStudentSubjectAnalysis subjectScore) {
		this.subjectScore = subjectScore;
	}
	public String getTestEndDateString() {
		return testEndDateString;
	}
	public void setTestEndDateString(String testEndDateString) {
		this.testEndDateString = testEndDateString;
	}
	
}
