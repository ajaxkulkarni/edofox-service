package com.rns.web.edo.service.domain;

import java.util.Date;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class EdoFeedback {
	
	private Integer id;
	private String feedback;
	private String type;
	private Date createdDate;
	private String resolution;
	private Integer questionId;
	private Integer studentId;
	private Integer frequency;
	private String feedbackResolutionText;
	private String feedbackVideoUrl;
	private String sourceVideoUrl;
	private String sourceVideoName;
	
	
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
	public String getResolution() {
		return resolution;
	}
	public void setResolution(String resolution) {
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
	public Integer getFrequency() {
		return frequency;
	}
	public void setFrequency(Integer frequency) {
		this.frequency = frequency;
	}
	public String getFeedbackResolutionText() {
		return feedbackResolutionText;
	}
	public void setFeedbackResolutionText(String feedbackResolutionText) {
		this.feedbackResolutionText = feedbackResolutionText;
	}
	public String getFeedbackVideoUrl() {
		return feedbackVideoUrl;
	}
	public void setFeedbackVideoUrl(String feedbackVideoUrl) {
		this.feedbackVideoUrl = feedbackVideoUrl;
	}
	public String getSourceVideoUrl() {
		return sourceVideoUrl;
	}
	public void setSourceVideoUrl(String sourceVideoUrl) {
		this.sourceVideoUrl = sourceVideoUrl;
	}
	public String getSourceVideoName() {
		return sourceVideoName;
	}
	public void setSourceVideoName(String sourceVideoName) {
		this.sourceVideoName = sourceVideoName;
	}
	

}
