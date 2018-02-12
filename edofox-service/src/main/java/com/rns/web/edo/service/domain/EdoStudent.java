package com.rns.web.edo.service.domain;

public class EdoStudent {

	private Integer id;
	private String name;
	private String email;
	private String rollNo;
	private EDOStudentAnalysis analysis;

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

}
