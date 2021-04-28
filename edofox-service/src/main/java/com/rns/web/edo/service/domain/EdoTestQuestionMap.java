package com.rns.web.edo.service.domain;

import java.util.Date;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class EdoTestQuestionMap {
	
	private EdoTest test;
	private EdoQuestion question;
	private EdoStudent student;
	
	public EdoTest getTest() {
		return test;
	}
	public void setTest(EdoTest test) {
		this.test = test;
	}
	public EdoQuestion getQuestion() {
		return question;
	}
	public void setQuestion(EdoQuestion question) {
		this.question = question;
	}
	public EdoStudent getStudent() {
		return student;
	}
	public void setStudent(EdoStudent student) {
		this.student = student;
	}
	
}
