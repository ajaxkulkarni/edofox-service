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
@Table(name = "classwork_submissions")
@DynamicUpdate
public class EdoClassworkSubmission {
	
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true)
	private Integer id;
	@Column(name = "classwork_id", unique = true)
	private Integer classworkId;
	@Column(name = "student_id", unique = true)
	private Integer studentId;
	@Column(name = "status", unique = true)
	private String status;
	@Column(name = "created_date", unique = true)
	private Date createdDate;
	@Column(name = "last_updated", unique = true)
	private Date updatedDate;
	@Column(name = "reviewed_date", unique = true)
	private Date reviewedDate;
	@Column(name = "comment", unique = true)
	private String comment;
	@Column(name = "checked_by", unique = true)
	private Integer checkedBy;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	
	public Integer getStudentId() {
		return studentId;
	}
	public void setStudentId(Integer studentId) {
		this.studentId = studentId;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Date getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	public void setUpdatedDate(Date updatedDate) {
		this.updatedDate = updatedDate;
	}
	public Date getUpdatedDate() {
		return updatedDate;
	}
	public Integer getClassworkId() {
		return classworkId;
	}
	public void setClassworkId(Integer classworkId) {
		this.classworkId = classworkId;
	}
	public Date getReviewedDate() {
		return reviewedDate;
	}
	public void setReviewedDate(Date reviewedDate) {
		this.reviewedDate = reviewedDate;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public Integer getCheckedBy() {
		return checkedBy;
	}
	public void setCheckedBy(Integer checkedBy) {
		this.checkedBy = checkedBy;
	}
	
}
