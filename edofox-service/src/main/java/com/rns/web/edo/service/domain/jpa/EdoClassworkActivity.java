package com.rns.web.edo.service.domain.jpa;

import static javax.persistence.GenerationType.IDENTITY;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicUpdate;

@Entity
@Table(name = "classwork_activity")
@DynamicUpdate
public class EdoClassworkActivity {
	
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true)
	private Integer id;
	@Column(name = "content_id")
	private Integer contentId;
	@Column(name = "read_by")
	private Integer readBy;
	@Column(name = "read_at")
	private Date readAt;
	@Column(name = "status")
	private String status;
	@Column(name = "downloaded")
	private Integer downloaded;
	@Column(name = "percentage")
	private Float percent;
	@Column(name = "duration")
	private Float duration;
	@Column(name = "student_id")
	private Integer studentId;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getContentId() {
		return contentId;
	}
	public void setContentId(Integer contentId) {
		this.contentId = contentId;
	}
	public Integer getReadBy() {
		return readBy;
	}
	public void setReadBy(Integer readBy) {
		this.readBy = readBy;
	}
	public Date getReadAt() {
		return readAt;
	}
	public void setReadAt(Date readAt) {
		this.readAt = readAt;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Integer getDownloaded() {
		return downloaded;
	}
	public void setDownloaded(Integer downloaded) {
		this.downloaded = downloaded;
	}
	public Float getPercent() {
		return percent;
	}
	public void setPercent(Float percent) {
		this.percent = percent;
	}
	public Float getDuration() {
		return duration;
	}
	public void setDuration(Float duration) {
		this.duration = duration;
	}
	public Integer getStudentId() {
		return studentId;
	}
	public void setStudentId(Integer studentId) {
		this.studentId = studentId;
	}
	
}
