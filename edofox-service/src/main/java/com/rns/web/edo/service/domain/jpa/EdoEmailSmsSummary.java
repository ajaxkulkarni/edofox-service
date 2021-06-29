package com.rns.web.edo.service.domain.jpa;

import static javax.persistence.GenerationType.IDENTITY;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicUpdate;

@Entity
@Table(name = "email_sms_summary")
@DynamicUpdate
public class EdoEmailSmsSummary {
	
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true)
	private Integer id;
	@Column(name = "institute_id", unique = true)
	private Integer instituteId;
	@Column(name = "created_date", unique = true)
	private Date createdDate;
	@Column(name = "notification_type", unique = true)
	private String notificationType;
	@Column(name = "channel", unique = true)
	private String channel;
	@Column(name = "exam_id", unique = true)
	private Integer examId;
	@Column(name = "classwork_id", unique = true)
	private Integer classworkId;
	@Column(name = "no_of_students", unique = true)
	private Integer noOfStudents;
	@Column(name = "success_count", unique = true)
	private Integer successCount;
	@Column(name = "failure_count", unique = true)
	private Integer failureCount;
	
	
	public Integer getSuccessCount() {
		return successCount;
	}
	public void setSuccessCount(Integer successCount) {
		this.successCount = successCount;
	}
	public Integer getFailureCount() {
		return failureCount;
	}
	public void setFailureCount(Integer failureCount) {
		this.failureCount = failureCount;
	}
	public Date getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getInstituteId() {
		return instituteId;
	}
	public void setInstituteId(Integer instituteId) {
		this.instituteId = instituteId;
	}
	
	public String getNotificationType() {
		return notificationType;
	}
	public void setNotificationType(String notificationType) {
		this.notificationType = notificationType;
	}
	public String getChannel() {
		return channel;
	}
	public void setChannel(String channel) {
		this.channel = channel;
	}
	public Integer getExamId() {
		return examId;
	}
	public void setExamId(Integer examId) {
		this.examId = examId;
	}
	public Integer getClassworkId() {
		return classworkId;
	}
	public void setClassworkId(Integer classworkId) {
		this.classworkId = classworkId;
	}
	public Integer getNoOfStudents() {
		return noOfStudents;
	}
	public void setNoOfStudents(Integer noOfStudents) {
		this.noOfStudents = noOfStudents;
	}
}