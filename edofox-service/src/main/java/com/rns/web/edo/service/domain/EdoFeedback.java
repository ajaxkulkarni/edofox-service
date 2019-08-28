package com.rns.web.edo.service.domain;

import java.util.Date;

public class EdoFeedback {
	
	private Integer id;
	private String feedback;
	private String type;
	private Date createdDate;
	private Integer resolution;
	private Integer questionId;
	private Integer studentId;
	
	
	
	public String getFeedback() {
		return feedback;
	}
	public void setFeedback(String feedback) {
		this.feedback = feedback;
	}
	public Date getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getType() {
		return type;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getResolution() {
		return resolution;
	}
	public void setResolution(Integer resolution) {
		this.resolution = resolution;
	}
	public Integer getQuestionId() {
		return questionId;
	}
	public void setQuestionId(Integer questionId) {
		this.questionId = questionId;
	}
	public Integer getStudentId() {
		return studentId;
	}
	public void setStudentId(Integer studentId) {
		this.studentId = studentId;
	}
	

}
