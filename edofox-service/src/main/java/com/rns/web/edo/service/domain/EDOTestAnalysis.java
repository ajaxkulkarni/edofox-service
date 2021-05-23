package com.rns.web.edo.service.domain;

import java.math.BigDecimal;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_EMPTY)
public class EDOTestAnalysis {
	
	private Integer studentsAppeared;
	private BigDecimal averageScore;
	private BigDecimal averageCorrect;
	private BigDecimal averageAttempted;
	private List<EdoStudentSubjectAnalysis> subjectAnalysis;
	private Integer topScore;
	
	private Integer mcqSolved;
	private Integer mcqCorrect;
	private Integer mcqWrong;
	private BigDecimal mcqScore;
	
	
	public Integer getMcqSolved() {
		return mcqSolved;
	}
	public void setMcqSolved(Integer mcqSolved) {
		this.mcqSolved = mcqSolved;
	}
	public Integer getMcqCorrect() {
		return mcqCorrect;
	}
	public void setMcqCorrect(Integer mcqCorrect) {
		this.mcqCorrect = mcqCorrect;
	}
	public Integer getMcqWrong() {
		return mcqWrong;
	}
	public void setMcqWrong(Integer mcqWrong) {
		this.mcqWrong = mcqWrong;
	}
	public BigDecimal getMcqScore() {
		return mcqScore;
	}
	public void setMcqScore(BigDecimal mcqScore) {
		this.mcqScore = mcqScore;
	}
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
	
}
