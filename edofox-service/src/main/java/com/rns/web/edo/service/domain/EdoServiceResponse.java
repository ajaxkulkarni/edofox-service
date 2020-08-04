package com.rns.web.edo.service.domain;

import java.util.Date;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_EMPTY)
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
	private List<EdoFeedback> feedbacks;
	private String token;
	private List<EdoVideoLectureMap> lectures;
	private List<EdoTestStudentMap> maps;
	private List<EdoSuggestion> suggestions;
	private Date currentTime;
	
	public EdoServiceResponse() {
		setStatus(new EdoApiStatus());
		setCurrentTime(new Date());
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

	public List<EdoFeedback> getFeedbacks() {
		return feedbacks;
	}

	public void setFeedbacks(List<EdoFeedback> feedbacks) {
		this.feedbacks = feedbacks;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public List<EdoVideoLectureMap> getLectures() {
		return lectures;
	}

	public void setLectures(List<EdoVideoLectureMap> lectures) {
		this.lectures = lectures;
	}

	public void setMaps(List<EdoTestStudentMap> maps) {
		this.maps = maps;
	}
	
	public List<EdoTestStudentMap> getMaps() {
		return maps;
	}

	public List<EdoSuggestion> getSuggestions() {
		return suggestions;
	}

	public void setSuggestions(List<EdoSuggestion> suggestions) {
		this.suggestions = suggestions;
	}

	public Date getCurrentTime() {
		return currentTime;
	}

	public void setCurrentTime(Date currentTime) {
		this.currentTime = currentTime;
	}

}
