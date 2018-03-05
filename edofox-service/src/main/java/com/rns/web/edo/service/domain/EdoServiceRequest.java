package com.rns.web.edo.service.domain;

public class EdoServiceRequest {
	
	private EdoStudent student;
	private EdoTest test;
	private EDOInstitute institute;
	
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

	public EDOInstitute getInstitute() {
		return institute;
	}

	public void setInstitute(EDOInstitute institute) {
		this.institute = institute;
	}
	
	
	
}
