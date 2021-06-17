package com.rns.web.edo.service.domain;

import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

public class EDOStudentAnalysis {
	
	private Integer solvedCount;
	private Integer correctCount;
	private Integer flaggedCount;
	private BigDecimal score;
	private List<EdoStudentSubjectAnalysis> subjectScores;
	private Integer rank;
	private Integer totalStudents;
	private String status;
	
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
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public BigDecimal getSubjectScore(String subject) {
		if(StringUtils.isBlank(subject) || CollectionUtils.isEmpty(this.subjectScores)) {
			return null;
		}
		for(EdoStudentSubjectAnalysis sa: subjectScores) {
			if(StringUtils.equals(sa.getSubject(), subject)) {
				return sa.getScore();
			}
		}
		return null;
	}
	
}
