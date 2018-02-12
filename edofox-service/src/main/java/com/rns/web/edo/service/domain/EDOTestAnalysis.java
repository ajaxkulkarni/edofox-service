package com.rns.web.edo.service.domain;

import java.math.BigDecimal;

public class EDOTestAnalysis {
	
	private Integer studentsAppeared;
	private BigDecimal averageScore;
	private BigDecimal averageCorrect;
	private BigDecimal averageAttempted;
	
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
	
}
