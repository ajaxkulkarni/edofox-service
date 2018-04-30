package com.rns.web.edo.service.domain;

import java.math.BigDecimal;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class EdoQuestion {
	
	private Integer id;
	private Integer qn_id;
	private String question;
	private EdoChapter chapter;
	private String option1;
	private String option2;
	private String option3;
	private String option4;
	private String option5;
	private String response;
	private Integer result;
	private String subject;
	private String answer;
	private boolean isCorrect;
	private Integer flagged;
	private String correctAnswer;
	private Integer weightage;
	private Integer negativeMarks;
	private Integer subjectId;
	private String questionImageUrl;
	private String option1ImageUrl;
	private String option2ImageUrl;
	private String option3ImageUrl;
	private String option4ImageUrl;
	private String solution;
	private String solutionImageUrl;
	private Integer questionNumber;
	private String type;
	private String metaData;
	private String metaDataImageUrl;
	private EDOQuestionAnalysis analysis;
	private List<EdoComplexOption> complexOptions;
	private String section;
	private BigDecimal marks;
	private String alternateAnswer;
	
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
	public String getQuestionImageUrl() {
		return questionImageUrl;
	}
	public void setQuestionImageUrl(String questionImageUrl) {
		this.questionImageUrl = questionImageUrl;
	}
	public String getOption1ImageUrl() {
		return option1ImageUrl;
	}
	public void setOption1ImageUrl(String option1ImageUrl) {
		this.option1ImageUrl = option1ImageUrl;
	}
	public String getOption2ImageUrl() {
		return option2ImageUrl;
	}
	public void setOption2ImageUrl(String option2ImageUrl) {
		this.option2ImageUrl = option2ImageUrl;
	}
	public String getOption3ImageUrl() {
		return option3ImageUrl;
	}
	public void setOption3ImageUrl(String option3ImageUrl) {
		this.option3ImageUrl = option3ImageUrl;
	}
	public String getOption4ImageUrl() {
		return option4ImageUrl;
	}
	public void setOption4ImageUrl(String option4ImageUrl) {
		this.option4ImageUrl = option4ImageUrl;
	}
	
	public EDOQuestionAnalysis getAnalysis() {
		return analysis;
	}
	public void setAnalysis(EDOQuestionAnalysis analysis) {
		this.analysis = analysis;
	}
	public Integer getSubjectId() {
		return subjectId;
	}
	public void setSubjectId(Integer subjectId) {
		this.subjectId = subjectId;
	}
	public String getSolution() {
		return solution;
	}
	public void setSolution(String solution) {
		this.solution = solution;
	}
	public String getSolutionImageUrl() {
		return solutionImageUrl;
	}
	public void setSolutionImageUrl(String solutionImageUrl) {
		this.solutionImageUrl = solutionImageUrl;
	}
	public Integer getQuestionNumber() {
		return questionNumber;
	}
	public void setQuestionNumber(Integer questionNumber) {
		this.questionNumber = questionNumber;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getMetaData() {
		return metaData;
	}
	public void setMetaData(String metaData) {
		this.metaData = metaData;
	}
	public String getOption5() {
		return option5;
	}
	public void setOption5(String option5) {
		this.option5 = option5;
	}
	public List<EdoComplexOption> getComplexOptions() {
		return complexOptions;
	}
	public void setComplexOptions(List<EdoComplexOption> complexOptions) {
		this.complexOptions = complexOptions;
	}
	public String getMetaDataImageUrl() {
		return metaDataImageUrl;
	}
	public void setMetaDataImageUrl(String metaDataImageUrl) {
		this.metaDataImageUrl = metaDataImageUrl;
	}
	public String getSection() {
		return section;
	}
	public void setSection(String section) {
		this.section = section;
	}
	public BigDecimal getMarks() {
		return marks;
	}
	public void setMarks(BigDecimal marks) {
		this.marks = marks;
	}
	public String getAlternateAnswer() {
		return alternateAnswer;
	}
	public void setAlternateAnswer(String alternateAnswer) {
		this.alternateAnswer = alternateAnswer;
	}

}
