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
@Table(name = "email_sms_logs")
@DynamicUpdate
public class EdoEmailSmsLog {
	
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true)
	private Integer id;
	@Column(name = "institute_id", unique = true)
	private Integer instituteId;
	@Column(name = "created_date", unique = true)
	private Date createdDate;
	@Column(name = "last_updated", unique = true)
	private Date updatedDate;
	@Column(name = "status", unique = true)
	private String status;
	@Column(name = "sent_to", unique = true)
	private String sentTo;
	@Column(name = "notification_type", unique = true)
	private String notificationType;
	@Column(name = "channel", unique = true)
	private String channel;
	@Column(name = "text", unique = true)
	private String text;
	@Column(name = "sms_id", unique = true)
	private String smsId;
	
	@Column(name = "student_id", unique = true)
	private Integer studentId;
	@Column(name = "exam_id", unique = true)
	private Integer examId;
	@Column(name = "classwork_id", unique = true)
	private Integer classworkId;
	
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
	public Date getUpdatedDate() {
		return updatedDate;
	}
	public void setUpdatedDate(Date updatedDate) {
		this.updatedDate = updatedDate;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getSentTo() {
		return sentTo;
	}
	public void setSentTo(String sentTo) {
		this.sentTo = sentTo;
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
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public Integer getStudentId() {
		return studentId;
	}
	public void setStudentId(Integer studentId) {
		this.studentId = studentId;
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
	public String getSmsId() {
		return smsId;
	}
	public void setSmsId(String smsId) {
		this.smsId = smsId;
	}
	
}