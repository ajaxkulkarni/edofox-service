package com.rns.web.edo.service.domain;

import java.util.List;

public class EdoServiceResponse {
	
	private EdoApiStatus status;
	private EdoStudent student;
	private EdoTest test;
	private List<EdoStudent> students;
	private EDOInstitute institute;
	private List<EDOPackage> packages;
	private EdoPaymentStatus paymentStatus;
	private EdoQuestion question;
	private List<EdoSubject> subjects;
	private EDOQuestionAnalysis questionAnalysis;
	
	public EdoServiceResponse() {
		setStatus(new EdoApiStatus());
	}
	
	public EdoServiceResponse(EdoApiStatus edoApiStatus) {
		setStatus(edoApiStatus);
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

	public EDOInstitute getInstitute() {
		return institute;
	}

	public void setInstitute(EDOInstitute institute) {
		this.institute = institute;
	}

	public List<EDOPackage> getPackages() {
		return packages;
	}

	public void setPackages(List<EDOPackage> packages) {
		this.packages = packages;
	}

	public EdoPaymentStatus getPaymentStatus() {
		return paymentStatus;
	}

	public void setPaymentStatus(EdoPaymentStatus paymentStatus) {
		this.paymentStatus = paymentStatus;
	}

	public EdoQuestion getQuestion() {
		return question;
	}

	public void setQuestion(EdoQuestion question) {
		this.question = question;
	}

	public List<EdoSubject> getSubjects() {
		return subjects;
	}

	public void setSubjects(List<EdoSubject> subjects) {
		this.subjects = subjects;
	}

	public EDOQuestionAnalysis getQuestionAnalysis() {
		return questionAnalysis;
	}

	public void setQuestionAnalysis(EDOQuestionAnalysis questionAnalysis) {
		this.questionAnalysis = questionAnalysis;
	}

}
