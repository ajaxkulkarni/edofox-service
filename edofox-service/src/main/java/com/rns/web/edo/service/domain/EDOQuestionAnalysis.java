package com.rns.web.edo.service.domain;

import java.math.BigDecimal;

public class EDOQuestionAnalysis {
	

	private Integer optionCount;
	private String optionSelected;
	private Integer option1Count;
	private Integer option2Count;
	private Integer option3Count;
	private Integer option4Count;
	private Integer correctCount;
	private BigDecimal option1percent;
	private BigDecimal option2percent;
	private BigDecimal option3percent;
	private BigDecimal option4percent;
	private BigDecimal correctPercent;
	
	private Integer questionsAddedBySubject;
	private Integer questionsAddedByChapter;
	private Integer questionsAddedByDate;
	
	private Integer hardQuestionsCount;
	private Integer mediumQuestionsCount;
	private Integer easyQuestionsCount;
	
	private Integer typeCount;
	private String questionType;
	
	
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
	
	

}
