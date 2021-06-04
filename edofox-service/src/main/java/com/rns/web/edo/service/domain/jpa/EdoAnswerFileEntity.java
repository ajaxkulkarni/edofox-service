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
@Table(name = "exam_answer_files")
@DynamicUpdate
public class EdoAnswerFileEntity {
	
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true)
	private Integer id;
	//@Column(name = "question_id", unique = true)
	//private Integer questionId;
	@Column(name = "exam_id", unique = true)
	private Integer testId;
	@Column(name = "student_id", unique = true)
	private Integer studentId;
	@Column(name = "file_path", unique = true)
	private String filePath;
	@Column(name = "created_date", unique = true)
	private Date createdDate;
	@Column(name = "file_url", unique = true)
	private String fileUrl;
	@Column(name = "correction_url", unique = true)
	private String correctionUrl;
	@Column(name = "correction_marks", unique = true)
	private BigDecimal correctionMarks;
	@Column(name = "evaluator", unique = true)
	private Integer evaluator;
	@Column(name = "question_id", unique = true)
	private Integer questionId;
	
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
	/*public Integer getQuestionId() {
		return questionId;
	}
	public void setQuestionId(Integer questionId) {
		this.questionId = questionId;
	}*/
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
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	public String getFileUrl() {
		return fileUrl;
	}
	public void setFileUrl(String fileUrl) {
		this.fileUrl = fileUrl;
	}
	public String getCorrectionUrl() {
		return correctionUrl;
	}
	public void setCorrectionUrl(String correctionUrl) {
		this.correctionUrl = correctionUrl;
	}
	public BigDecimal getCorrectionMarks() {
		return correctionMarks;
	}
	public void setCorrectionMarks(BigDecimal correctionMarks) {
		this.correctionMarks = correctionMarks;
	}
	public Integer getEvaluator() {
		return evaluator;
	}
	public void setEvaluator(Integer evaluator) {
		this.evaluator = evaluator;
	}
	public void setQuestionId(Integer questionId) {
		this.questionId = questionId;
	}
	public Integer getQuestionId() {
		return questionId;
	}
}