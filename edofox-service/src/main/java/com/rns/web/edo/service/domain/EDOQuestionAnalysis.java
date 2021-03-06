package com.rns.web.edo.service.domain;

import java.math.BigDecimal;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class EDOQuestionAnalysis {
	

	private Integer optionCount;
	private String optionSelected;
	private Integer option1Count;
	private Integer option2Count;
	private Integer option3Count;
	private Integer option4Count;
	private Integer correctCount;
	private Integer solvedCount;
	private Integer wrongCount;
	private Integer unattemptedCount;
	
	private BigDecimal option1percent;
	private BigDecimal option2percent;
	private BigDecimal option3percent;
	private BigDecimal option4percent;
	private BigDecimal correctPercent;
	private BigDecimal wrongPercent;
	private BigDecimal attemptedPercent;
	private BigDecimal unattemptedPercent;
	private BigDecimal subAvg;
	
	//For subjectwise avg
	private BigDecimal avgSolved;
	private BigDecimal avgCorrect;
	private BigDecimal avgUnsolved;
	private BigDecimal avgWrong;
	private BigDecimal subjectTopper;
	private BigDecimal subjectTotal;
	
	
	private Integer questionsAddedBySubject;
	private Integer questionsAddedByChapter;
	private Integer questionsAddedByDate;
	
	private Integer hardQuestionsCount;
	private Integer mediumQuestionsCount;
	private Integer easyQuestionsCount;
	
	private Integer typeCount;
	private String questionType;
	
	public BigDecimal getAvgSolved() {
		return avgSolved;
	}
	public void setAvgSolved(BigDecimal avgSolved) {
		this.avgSolved = avgSolved;
	}
	public BigDecimal getAvgCorrect() {
		return avgCorrect;
	}
	public void setAvgCorrect(BigDecimal avgCorrect) {
		this.avgCorrect = avgCorrect;
	}
	public BigDecimal getAvgUnsolved() {
		return avgUnsolved;
	}
	public void setAvgUnsolved(BigDecimal avgUnsolved) {
		this.avgUnsolved = avgUnsolved;
	}
	public BigDecimal getAvgWrong() {
		return avgWrong;
	}
	public void setAvgWrong(BigDecimal avgWrong) {
		this.avgWrong = avgWrong;
	}
	
	public Integer getHardQuestionsCount() {
		return hardQuestionsCount;
	}
	public void setHardQuestionsCount(Integer hardQuestionsCount) {
		this.hardQuestionsCount = hardQuestionsCount;
	}
	public Integer getMediumQuestionsCount() {
		return mediumQuestionsCount;
	}
	public void setMediumQuestionsCount(Integer mediumQuestionsCount) {
		this.mediumQuestionsCount = mediumQuestionsCount;
	}
	public Integer getEasyQuestionsCount() {
		return easyQuestionsCount;
	}
	public void setEasyQuestionsCount(Integer easyQuestionsCount) {
		this.easyQuestionsCount = easyQuestionsCount;
	}
	public Integer getQuestionsAddedBySubject() {
		return questionsAddedBySubject;
	}
	public void setQuestionsAddedBySubject(Integer questionsAddedBySubject) {
		this.questionsAddedBySubject = questionsAddedBySubject;
	}
	public Integer getQuestionsAddedByChapter() {
		return questionsAddedByChapter;
	}
	public void setQuestionsAddedByChapter(Integer questionsAddedByChapter) {
		this.questionsAddedByChapter = questionsAddedByChapter;
	}
	public Integer getQuestionsAddedByDate() {
		return questionsAddedByDate;
	}
	public void setQuestionsAddedByDate(Integer questionsAddedByDate) {
		this.questionsAddedByDate = questionsAddedByDate;
	}
	public Integer getOptionCount() {
		return optionCount;
	}
	public void setOptionCount(Integer optionCount) {
		this.optionCount = optionCount;
	}
	public String getOptionSelected() {
		return optionSelected;
	}
	public void setOptionSelected(String optionSelected) {
		this.optionSelected = optionSelected;
	}
	public Integer getOption1Count() {
		return option1Count;
	}
	public void setOption1Count(Integer option1Count) {
		this.option1Count = option1Count;
	}
	public Integer getOption2Count() {
		return option2Count;
	}
	public void setOption2Count(Integer option2Count) {
		this.option2Count = option2Count;
	}
	public Integer getOption3Count() {
		return option3Count;
	}
	public void setOption3Count(Integer option3Count) {
		this.option3Count = option3Count;
	}
	public Integer getOption4Count() {
		return option4Count;
	}
	public void setOption4Count(Integer option4Count) {
		this.option4Count = option4Count;
	}
	public Integer getCorrectCount() {
		return correctCount;
	}
	public void setCorrectCount(Integer correctCount) {
		this.correctCount = correctCount;
	}
	public BigDecimal getOption1percent() {
		return option1percent;
	}
	public void setOption1percent(BigDecimal option1percent) {
		this.option1percent = option1percent;
	}
	public BigDecimal getOption2percent() {
		return option2percent;
	}
	public void setOption2percent(BigDecimal option2percent) {
		this.option2percent = option2percent;
	}
	public BigDecimal getOption3percent() {
		return option3percent;
	}
	public void setOption3percent(BigDecimal option3percent) {
		this.option3percent = option3percent;
	}
	public BigDecimal getOption4percent() {
		return option4percent;
	}
	public void setOption4percent(BigDecimal option4percent) {
		this.option4percent = option4percent;
	}
	public BigDecimal getCorrectPercent() {
		return correctPercent;
	}
	public void setCorrectPercent(BigDecimal correctPercent) {
		this.correctPercent = correctPercent;
	}
	public Integer getTypeCount() {
		return typeCount;
	}
	public void setTypeCount(Integer typeCount) {
		this.typeCount = typeCount;
	}
	public String getQuestionType() {
		return questionType;
	}
	public void setQuestionType(String questionType) {
		this.questionType = questionType;
	}
	public Integer getSolvedCount() {
		return solvedCount;
	}
	public void setSolvedCount(Integer solvedCount) {
		this.solvedCount = solvedCount;
	}
	public Integer getWrongCount() {
		return wrongCount;
	}
	public void setWrongCount(Integer wrongCount) {
		this.wrongCount = wrongCount;
	}
	public Integer getUnattemptedCount() {
		return unattemptedCount;
	}
	public void setUnattemptedCount(Integer unattemptedCount) {
		this.unattemptedCount = unattemptedCount;
	}
	public BigDecimal getWrongPercent() {
		return wrongPercent;
	}
	public void setWrongPercent(BigDecimal wrongPercent) {
		this.wrongPercent = wrongPercent;
	}
	public BigDecimal getAttemptedPercent() {
		return attemptedPercent;
	}
	public void setAttemptedPercent(BigDecimal attemptedPercent) {
		this.attemptedPercent = attemptedPercent;
	}
	public BigDecimal getSubAvg() {
		return subAvg;
	}
	public void setSubAvg(BigDecimal subAvg) {
		this.subAvg = subAvg;
	}
	public BigDecimal getSubjectTopper() {
		return subjectTopper;
	}
	public void setSubjectTopper(BigDecimal subjectTopper) {
		this.subjectTopper = subjectTopper;
	}
	public BigDecimal getSubjectTotal() {
		return subjectTotal;
	}
	public void setSubjectTotal(BigDecimal subjectTotal) {
		this.subjectTotal = subjectTotal;
	}
	public BigDecimal getUnattemptedPercent() {
		return unattemptedPercent;
	}
	public void setUnattemptedPercent(BigDecimal unattemptedPercent) {
		this.unattemptedPercent = unattemptedPercent;
	}
	
	

}
