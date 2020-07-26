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
@Table(name = "exam_results")
@DynamicUpdate
public class EdoTestStatusEntity {
	
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true)
	private Integer id;
	@Column(name = "exam_id", unique = true)
	private Integer testId;
	@Column(name = "stu_id", unique = true)
	private Integer studentId;
	@Column(name = "status", unique = true)
	private String status;
	@Column(name = "solved", unique = true)
	private Integer solved;
	@Column(name = "flagged", unique = true)
	private Integer flagged;
	@Column(name = "correct_ans", unique = true)
	private Integer correct;
	@Column(name = "score", unique = true)
	private BigDecimal score;
	@Column(name = "created_date", unique = true)
	private Date createdDate;
	@Column(name = "time_left", unique = true)
	private Long timeLeft;
	@Column(name = "updated_date", unique = true)
	private Date updatedDate;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getTestId() {
		return testId;
	}
	public void setTestId(Integer testId) {
		this.testId = testId;
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
	public Integer getSolved() {
		return solved;
	}
	public void setSolved(Integer solved) {
		this.solved = solved;
	}
	public Integer getFlagged() {
		return flagged;
	}
	public void setFlagged(Integer flagged) {
		this.flagged = flagged;
	}
	public Integer getCorrect() {
		return correct;
	}
	public void setCorrect(Integer correct) {
		this.correct = correct;
	}
	public BigDecimal getScore() {
		return score;
	}
	public void setScore(BigDecimal score) {
		this.score = score;
	}
	public Date getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	public void setTimeLeft(Long timeLeft) {
		this.timeLeft = timeLeft;
	}
	public Long getTimeLeft() {
		return timeLeft;
	}
	public void setUpdatedDate(Date updatedDate) {
		this.updatedDate = updatedDate;
	}
	public Date getUpdatedDate() {
		return updatedDate;
	}
	
}
