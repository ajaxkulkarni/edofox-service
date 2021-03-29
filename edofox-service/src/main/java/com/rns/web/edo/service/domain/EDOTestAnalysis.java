package com.rns.web.edo.service.domain;

import java.math.BigDecimal;
import java.util.List;

public class EDOTestAnalysis {
	
	private Integer studentsAppeared;
	private BigDecimal averageScore;
	private BigDecimal averageCorrect;
	private BigDecimal averageAttempted;
	private List<EdoStudentSubjectAnalysis> subjectAnalysis;
	private Integer topScore;
	private BigDecimal percentile;
	
	public Integer getStudentsAppeared() {
		return studentsAppeared;
	}
	public void setStudentsAppeared(Integer studentsAppeared) {
		this.studentsAppeared = studentsAppeared;
	}
	public BigDecimal getAverageScore() {
		return averageScore;
	}
	public void setAverageScore(BigDecimal averageScore) {
		this.averageScore = averageScore;
	}
	public BigDecimal getAverageCorrect() {
		return averageCorrect;
	}
	public void setAverageCorrect(BigDecimal averageCorrect) {
		this.averageCorrect = averageCorrect;
	}
	public BigDecimal getAverageAttempted() {
		return averageAttempted;
	}
	public void setAverageAttempted(BigDecimal averageAttempted) {
		this.averageAttempted = averageAttempted;
	}
	public void setSubjectAnalysis(List<EdoStudentSubjectAnalysis> subjectAnalysis) {
		this.subjectAnalysis = subjectAnalysis;
	}
	public List<EdoStudentSubjectAnalysis> getSubjectAnalysis() {
		return subjectAnalysis;
	}
	public Integer getTopScore() {
		return topScore;
	}
	public void setTopScore(Integer topScore) {
		this.topScore = topScore;
	}
	public BigDecimal getPercentile() {
		return percentile;
	}
	public void setPercentile(BigDecimal percentile) {
		this.percentile = percentile;
	}
	
}
