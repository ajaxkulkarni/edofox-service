package com.rns.web.edo.service.domain;

import java.util.List;

public class EdoServiceResponse {
	
	private EdoApiStatus status;
	private EdoStudent student;
	private EdoTest test;
	private List<EdoStudent> students;
	
	public EdoServiceResponse() {
		setStatus(new EdoApiStatus());
	}
	
	public EdoStudent getStudent() {
		return student;
	}
	public void setStudent(EdoStudent student) {
		this.student = student;
	}
	public EdoTest getTest() {
		return test;
	}
	public void setTest(EdoTest test) {
		this.test = test;
	}
	public EdoApiStatus getStatus() {
		return status;
	}
	public void setStatus(EdoApiStatus status) {
		this.status = status;
	}

	public List<EdoStudent> getStudents() {
		return students;
	}

	public void setStudents(List<EdoStudent> students) {
		this.students = students;
	}

}
