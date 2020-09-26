package com.rns.web.edo.service.domain;

import java.math.BigDecimal;

public class EdoStudentSubjectAnalysis {

	private String subject;
	private BigDecimal score;
	private Integer totalQuestions;
	private BigDecimal deductions;
	private Integer solvedCount;
	private Integer deductionsCount; //only negatives
	private Integer incorrectCount; //not neccessarily negative
	private Integer totalMarks;
	
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public BigDecimal getScore() {
		return score;
	}
	public void setScore(BigDecimal score) {
		this.score = score;
	}
	public BigDecimal getDeductions() {
		return deductions;
	}
	public void setDeductions(BigDecimal deductions) {
		this.deductions = deductions;
	}
	public Integer getSolvedCount() {
		return solvedCount;
	}
	public void setSolvedCount(Integer solvedCount) {
		this.solvedCount = solvedCount;
	}
	public Integer getDeductionsCount() {
		return deductionsCount;
	}
	public void setDeductionsCount(Integer deductionsCount) {
		this.deductionsCount = deductionsCount;
	}
	public Integer getIncorrectCount() {
		return incorrectCount;
	}
	public void setIncorrectCount(Integer incorrectCount) {
		this.incorrectCount = incorrectCount;
	}
	public Integer getTotalQuestions() {
		return totalQuestions;
	}
	public void setTotalQuestions(Integer totalQuestions) {
		this.totalQuestions = totalQuestions;
	}
	public Integer getTotalMarks() {
		return totalMarks;
	}
	public void setTotalMarks(Integer totalMarks) {
		this.totalMarks = totalMarks;
	}
	
	
	
}
