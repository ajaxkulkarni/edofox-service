package com.rns.web.edo.service.domain;

import java.math.BigDecimal;
import java.util.List;

public class EDOStudentAnalysis {
	
	private Integer solvedCount;
	private Integer correctCount;
	private Integer flaggedCount;
	private BigDecimal score;
	private List<EdoStudentSubjectAnalysis> subjectScores;
	
	public Integer getSolvedCount() {
		return solvedCount;
	}
	public void setSolvedCount(Integer solvedCount) {
		this.solvedCount = solvedCount;
	}
	public Integer getCorrectCount() {
		return correctCount;
	}
	public void setCorrectCount(Integer correctCount) {
		this.correctCount = correctCount;
	}
	public Integer getFlaggedCount() {
		return flaggedCount;
	}
	public void setFlaggedCount(Integer flaggedCount) {
		this.flaggedCount = flaggedCount;
	}
	public BigDecimal getScore() {
		return score;
	}
	public void setScore(BigDecimal score) {
		this.score = score;
	}
	public List<EdoStudentSubjectAnalysis> getSubjectScores() {
		return subjectScores;
	}
	public void setSubjectScores(List<EdoStudentSubjectAnalysis> subjectScores) {
		this.subjectScores = subjectScores;
	}
	
}
