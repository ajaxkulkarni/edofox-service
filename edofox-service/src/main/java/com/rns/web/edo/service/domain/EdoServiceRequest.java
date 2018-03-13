package com.rns.web.edo.service.domain;

public class EdoServiceRequest {
	
	private EdoStudent student;
	private EdoTest test;
	private EDOInstitute institute;
	private String filePath;
	private Integer firstQuestion;
	private Integer subjectId;
	
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

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public Integer getFirstQuestion() {
		return firstQuestion;
	}

	public void setFirstQuestion(Integer firstQuestion) {
		this.firstQuestion = firstQuestion;
	}

	public Integer getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(Integer subjectId) {
		this.subjectId = subjectId;
	}
	
}
