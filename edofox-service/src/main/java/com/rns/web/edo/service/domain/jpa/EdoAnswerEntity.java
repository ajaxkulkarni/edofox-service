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
@Table(name = "test_result")
@DynamicUpdate
public class EdoAnswerEntity {
	
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true)
	private Integer id;
	@Column(name = "question_id", unique = true)
	private Integer questionId;
	@Column(name = "test_id", unique = true)
	private Integer testId;
	@Column(name = "student_id", unique = true)
	private Integer studentId;
	@Column(name = "option_selected", unique = true)
	private String optionSelected;
	@Column(name = "flagged", unique = true)
	private Integer flagged;
	@Column(name = "time_taken", unique = true)
	private Integer timeTaken;
	@Column(name = "marks", unique = true)
	private BigDecimal marks;
	@Column(name = "created_date", unique = true)
	private Date createdDate;
	@Column(name = "updated_date", unique = true)
	private Date updatedDate;
	@Column(name = "question_number", unique = true)
	private Integer questionNumber;
	
	
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
	public Integer getQuestionId() {
		return questionId;
	}
	public void setQuestionId(Integer questionId) {
		this.questionId = questionId;
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
	public String getOptionSelected() {
		return optionSelected;
	}
	public void setOptionSelected(String optionSelected) {
		this.optionSelected = optionSelected;
	}
	public Integer getFlagged() {
		return flagged;
	}
	public void setFlagged(Integer flagged) {
		this.flagged = flagged;
	}
	public Integer getTimeTaken() {
		return timeTaken;
	}
	public void setTimeTaken(Integer timeTaken) {
		this.timeTaken = timeTaken;
	}
	public BigDecimal getMarks() {
		return marks;
	}
	public void setMarks(BigDecimal marks) {
		this.marks = marks;
	}
	public void setUpdatedDate(Date updatedDate) {
		this.updatedDate = updatedDate;
	}
	public Date getUpdatedDate() {
		return updatedDate;
	}
	public void setQuestionNumber(Integer questionNumber) {
		this.questionNumber = questionNumber;
	}
	public Integer getQuestionNumber() {
		return questionNumber;
	}
	
}
