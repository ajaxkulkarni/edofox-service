package com.rns.web.edo.service.domain;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.rns.web.edo.service.domain.jpa.EdoVideoLecture;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_EMPTY)
public class EdoVideoLectureMap {

	private EdoVideoLecture lecture;
	private EdoSubject subject;
	private EDOPackage classroom;
	
	public EdoVideoLecture getLecture() {
		return lecture;
	}
	public void setLecture(EdoVideoLecture lecture) {
		this.lecture = lecture;
	}
	public EdoSubject getSubject() {
		return subject;
	}
	public void setSubject(EdoSubject subject) {
		this.subject = subject;
	}
	public EDOPackage getClassroom() {
		return classroom;
	}
	public void setClassroom(EDOPackage classroom) {
		this.classroom = classroom;
	}
	
	
}