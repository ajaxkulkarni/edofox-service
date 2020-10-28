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
	private String feedbackResolutionImageUrl;
	private String sourceVideoUrl;
	private String sourceVideoName;
	private Integer videoId;
	private Float percentViewed;
	private Float durationViewed;
	private Integer foundationId;
	private Integer activityCount;
	private Long totalDuration;
	private String createdDateString;
	private String attachment;
	private String answeredBy;
	private String askedBy;
	private Date lastUpdated;
	
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
	public Integer getVideoId() {
		return videoId;
	}
	public void setVideoId(Integer videoId) {
		this.videoId = videoId;
	}
	public Float getPercentViewed() {
		return percentViewed;
	}
	public void setPercentViewed(Float percentViewed) {
		this.percentViewed = percentViewed;
	}
	public Float getDurationViewed() {
		return durationViewed;
	}
	public void setDurationViewed(Float durationViewed) {
		this.durationViewed = durationViewed;
	}
	public Integer getFoundationId() {
		return foundationId;
	}
	public void setFoundationId(Integer foundationId) {
		this.foundationId = foundationId;
	}
	public Integer getActivityCount() {
		return activityCount;
	}
	public void setActivityCount(Integer activityCount) {
		this.activityCount = activityCount;
	}
	public Long getTotalDuration() {
		return totalDuration;
	}
	public void setTotalDuration(Long totalDuration) {
		this.totalDuration = totalDuration;
	}
	public String getCreatedDateString() {
		return createdDateString;
	}
	public void setCreatedDateString(String createdDateString) {
		this.createdDateString = createdDateString;
	}
	public String getAttachment() {
		return attachment;
	}
	public void setAttachment(String attachment) {
		this.attachment = attachment;
	}
	public String getAnsweredBy() {
		return answeredBy;
	}
	public void setAnsweredBy(String answeredBy) {
		this.answeredBy = answeredBy;
	}
	public String getFeedbackResolutionImageUrl() {
		return feedbackResolutionImageUrl;
	}
	public void setFeedbackResolutionImageUrl(String feedbackResolutionImageUrl) {
		this.feedbackResolutionImageUrl = feedbackResolutionImageUrl;
	}
	public Date getLastUpdated() {
		return lastUpdated;
	}
	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}
	public String getAskedBy() {
		return askedBy;
	}
	public void setAskedBy(String askedBy) {
		this.askedBy = askedBy;
	}
	

}
