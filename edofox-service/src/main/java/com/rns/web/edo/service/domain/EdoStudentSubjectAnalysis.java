package com.rns.web.edo.service.domain;

import java.math.BigDecimal;

public class EdoStudentSubjectAnalysis {

	private String subject;
	private BigDecimal score;
	
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
	
}
