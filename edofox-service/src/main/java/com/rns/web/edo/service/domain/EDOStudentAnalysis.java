package com.rns.web.edo.service.domain;

import java.math.BigDecimal;

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class EDOStudentAnalysis {
	
	private Integer solvedCount;
	private Integer correctCount;
	private Integer flaggedCount;
	private BigDecimal score;
	private List<EdoStudentSubjectAnalysis> subjectScores;
	private Integer rank;
	private Integer totalStudents;
	private BigDecimal percentile;
	private Integer categoryRank;
	private Integer districtRank;
	private Integer districtCategoryRank;
	private Integer regionRank;
	private Integer regionCategoryRank;
	private Integer instituteRank;
	private Integer instituteCategoryRank;
	
	
	public Integer getCategoryRank() {
		return categoryRank;
	}
	public void setCategoryRank(Integer categoryRank) {
		this.categoryRank = categoryRank;
	}
	public Integer getDistrictRank() {
		return districtRank;
	}
	public void setDistrictRank(Integer districtRank) {
		this.districtRank = districtRank;
	}
	public Integer getDistrictCategoryRank() {
		return districtCategoryRank;
	}
	public void setDistrictCategoryRank(Integer districtCategoryRank) {
		this.districtCategoryRank = districtCategoryRank;
	}
	public Integer getRegionRank() {
		return regionRank;
	}
	public void setRegionRank(Integer regionRank) {
		this.regionRank = regionRank;
	}
	public Integer getRegionCategoryRank() {
		return regionCategoryRank;
	}
	public void setRegionCategoryRank(Integer regionCategoryRank) {
		this.regionCategoryRank = regionCategoryRank;
	}
	public Integer getInstituteRank() {
		return instituteRank;
	}
	public void setInstituteRank(Integer instituteRank) {
		this.instituteRank = instituteRank;
	}
	public Integer getInstituteCategoryRank() {
		return instituteCategoryRank;
	}
	public void setInstituteCategoryRank(Integer instituteCategoryRank) {
		this.instituteCategoryRank = instituteCategoryRank;
	}
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
