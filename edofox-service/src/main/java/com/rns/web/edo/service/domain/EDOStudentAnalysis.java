package com.rns.web.edo.service.domain;

import java.math.BigDecimal;
import java.util.List;

public class EDOStudentAnalysis {
	
	private Integer solvedCount;
	private Integer correctCount;
	private Integer flaggedCount;
	private BigDecimal score;
	private List<EdoStudentSubjectAnalysis> subjectScores;
	private Integer rank;
	private Integer totalStudents;
	private BigDecimal percentile;
	
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
	public Integer getRank() {
		return rank;
	}
	public void setRank(Integer rank) {
		this.rank = rank;
	}
	public Integer getTotalStudents() {
		return totalStudents;
	}
	public void setTotalStudents(Integer totalStudents) {
		this.totalStudents = totalStudents;
	}
	public BigDecimal getPercentile() {
		return percentile;
	}
	public void setPercentile(BigDecimal percentile) {
		this.percentile = percentile;
	}
	
}
