package com.rns.web.edo.service.domain;

import java.math.BigDecimal;
import java.util.List;

public class EDOTestAnalysis {
	
	private Integer studentsAppeared;
	private BigDecimal averageScore;
	private BigDecimal averageCorrect;
	private BigDecimal averageAttempted;
	private List<EdoStudentSubjectAnalysis> subjectAnalysis;
	private BigDecimal topScore;
	private BigDecimal percentile;
	private BigDecimal deviation;
	private BigDecimal marksLost;
	
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
	public BigDecimal getTopScore() {
		return topScore;
	}
	public void setTopScore(BigDecimal topScore) {
		this.topScore = topScore;
	}
	public BigDecimal getPercentile() {
		return percentile;
	}
	public void setPercentile(BigDecimal percentile) {
		this.percentile = percentile;
	}
	public BigDecimal getDeviation() {
		return deviation;
	}
	public void setDeviation(BigDecimal deviation) {
		this.deviation = deviation;
	}
	public BigDecimal getMarksLost() {
		return marksLost;
	}
	public void setMarksLost(BigDecimal marksLost) {
		this.marksLost = marksLost;
	}
	
}
