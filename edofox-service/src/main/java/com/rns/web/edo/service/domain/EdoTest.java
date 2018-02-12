package com.rns.web.edo.service.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class EdoTest {

	private Integer id;
	private String name;
	private Integer noOfQuestions;
	private Integer totalMarks;
	private Integer duration;
	private EdoQuestion currentQuestion;
	private List<EdoQuestion> test;
	private List<String> subjects;
	private Integer solvedCount;
	private Integer correctCount;
	private Integer flaggedCount;
	private BigDecimal score;
	private boolean submitted;
	private EDOTestAnalysis analysis;
	
	public EdoTest() {
		
	}
	
	public EdoTest(Integer testId) {
		setId(testId);
	}

	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	
	public List<EdoQuestion> getTest() {
		if(test == null) {
			test = new ArrayList<EdoQuestion>();
		}
		return test;
	}

	public void setTest(List<EdoQuestion> questions) {
		this.test = questions;
	}

	public EdoQuestion getCurrentQuestion() {
		return currentQuestion;
	}

	public void setCurrentQuestion(EdoQuestion currentQuestion) {
		this.currentQuestion = currentQuestion;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getNoOfQuestions() {
		return noOfQuestions;
	}

	public void setNoOfQuestions(Integer noOfQuestions) {
		this.noOfQuestions = noOfQuestions;
	}

	public Integer getTotalMarks() {
		return totalMarks;
	}

	public void setTotalMarks(Integer totalMarks) {
		this.totalMarks = totalMarks;
	}

	public Integer getDuration() {
		return duration;
	}

	public void setDuration(Integer duration) {
		this.duration = duration;
	}

	public List<String> getSubjects() {
		if(subjects == null) {
			subjects = new ArrayList<String>();
		}
		return subjects;
	}

	public void setSubjects(List<String> subjects) {
		this.subjects = subjects;
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

	public boolean isSubmitted() {
		return submitted;
	}

	public void setSubmitted(boolean submitted) {
		this.submitted = submitted;
	}

	public EDOTestAnalysis getAnalysis() {
		return analysis;
	}

	public void setAnalysis(EDOTestAnalysis analysis) {
		this.analysis = analysis;
	}
	
}
