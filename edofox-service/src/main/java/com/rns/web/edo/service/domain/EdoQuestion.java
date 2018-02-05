package com.rns.web.edo.service.domain;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EdoQuestion {
	
	private Integer id;
	private Integer qn_id;
	private String question;
	private EdoChapter chapter;
	private String option1;
	private String option2;
	private String option3;
	private String option4;
	private String response;
	private Integer result;
	private String subject;
	private String answer;
	private boolean isCorrect;
	private Integer flagged;
	private String correctAnswer;
	private Integer weightage;
	private Integer negativeMarks;
	
	
	public String getOption1() {
		return option1;
	}
	public void setOption1(String option1) {
		this.option1 = option1;
	}
	public String getOption2() {
		return option2;
	}
	public void setOption2(String option2) {
		this.option2 = option2;
	}
	public String getOption3() {
		return option3;
	}
	public void setOption3(String option3) {
		this.option3 = option3;
	}
	public String getOption4() {
		return option4;
	}
	public void setOption4(String option4) {
		this.option4 = option4;
	}
	public String getResponse() {
		return response;
	}
	public void setResponse(String response) {
		this.response = response;
	}
	public Integer getResult() {
		return result;
	}
	public void setResult(Integer result) {
		this.result = result;
	}
	public String getQuestion() {
		return question;
	}
	public void setQuestion(String question) {
		this.question = question;
	}
	
	public EdoChapter getChapter() {
		return chapter;
	}
	
	public void setChapter(EdoChapter chapter) {
		this.chapter = chapter;
	}
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getQn_id() {
		return qn_id;
	}
	public void setQn_id(Integer qn_id) {
		this.qn_id = qn_id;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getAnswer() {
		return answer;
	}
	public void setAnswer(String answer) {
		this.answer = answer;
	}
	public boolean isCorrect() {
		return isCorrect;
	}
	public void setCorrect(boolean isCorrect) {
		this.isCorrect = isCorrect;
	}
	public Integer getFlagged() {
		return flagged;
	}
	public void setFlagged(Integer flagged) {
		this.flagged = flagged;
	}
	public String getCorrectAnswer() {
		return correctAnswer;
	}
	public void setCorrectAnswer(String correctAnswer) {
		this.correctAnswer = correctAnswer;
	}
	public Integer getWeightage() {
		return weightage;
	}
	public void setWeightage(Integer weightage) {
		this.weightage = weightage;
	}
	public Integer getNegativeMarks() {
		return negativeMarks;
	}
	public void setNegativeMarks(Integer negativeMarks) {
		this.negativeMarks = negativeMarks;
	}

}
