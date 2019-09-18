package com.rns.web.edo.service.domain.jpa;

import static javax.persistence.GenerationType.IDENTITY;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicUpdate;

@Entity
@Table(name = "test_questions")
@DynamicUpdate
public class EdoQuestionEntity {
	
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true)
	private Integer id;
	@Column(name = "question")
	private String question;
	@Column(name = "chapter")
	private Integer chapter;
	@Column(name = "option1")
	private String option1;
	@Column(name = "option2")
	private String option2;
	@Column(name = "option3")
	private String option3;
	@Column(name = "option4")
	private String option4;
	@Column(name = "option5")
	private String option5;
	@Column(name = "weightage")
	private Float weightage;
	@Column(name = "negative_marks")
	private Float negativeMarks;
	@Column(name = "subject_id")
	private Integer subjectId;
	@Column(name = "question_img_url")
	private String questionImageUrl;
	@Column(name = "option1_img_url")
	private String option1ImageUrl;
	@Column(name = "option2_img_url")
	private String option2ImageUrl;
	@Column(name = "option3_img_url")
	private String option3ImageUrl;
	@Column(name = "option4_img_url")
	private String option4ImageUrl;
	@Column(name = "solution")
	private String solution;
	@Column(name = "solution_img_url")
	private String solutionImageUrl;
	@Column(name = "question_type")
	private String type;
	@Column(name = "meta_data")
	private String metaData;
	@Column(name = "meta_data_img_url")
	private String metaDataImageUrl;
	@Column(name = "alt_answer")
	private String alternateAnswer;
	@Column(name = "partial")
	private String partialCorrection;
	@Column(name = "exam_type")
	private String examType;
	@Column(name = "level")
	private Integer level;
	@Column(name = "ref_id")
	private String referenceId;
	@Column(name = "status")
	private String status;
	@Column(name = "correct_answer")
	private String correctAnswer;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getQuestion() {
		return question;
	}
	public void setQuestion(String question) {
		this.question = question;
	}
	public Integer getChapter() {
		return chapter;
	}
	public void setChapter(Integer chapter) {
		this.chapter = chapter;
	}
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
	public String getOption5() {
		return option5;
	}
	public void setOption5(String option5) {
		this.option5 = option5;
	}
	public Float getWeightage() {
		return weightage;
	}
	public void setWeightage(Float weightage) {
		this.weightage = weightage;
	}
	public Float getNegativeMarks() {
		return negativeMarks;
	}
	public void setNegativeMarks(Float negativeMarks) {
		this.negativeMarks = negativeMarks;
	}
	public Integer getSubjectId() {
		return subjectId;
	}
	public void setSubjectId(Integer subjectId) {
		this.subjectId = subjectId;
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
	public String getMetaDataImageUrl() {
		return metaDataImageUrl;
	}
	public void setMetaDataImageUrl(String metaDataImageUrl) {
		this.metaDataImageUrl = metaDataImageUrl;
	}
	public String getAlternateAnswer() {
		return alternateAnswer;
	}
	public void setAlternateAnswer(String alternateAnswer) {
		this.alternateAnswer = alternateAnswer;
	}
	public String getPartialCorrection() {
		return partialCorrection;
	}
	public void setPartialCorrection(String partialCorrection) {
		this.partialCorrection = partialCorrection;
	}
	public String getExamType() {
		return examType;
	}
	public void setExamType(String examType) {
		this.examType = examType;
	}
	public Integer getLevel() {
		return level;
	}
	public void setLevel(Integer level) {
		this.level = level;
	}
	public String getReferenceId() {
		return referenceId;
	}
	public void setReferenceId(String referenceId) {
		this.referenceId = referenceId;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getStatus() {
		return status;
	}
	public void setCorrectAnswer(String correctAnswer) {
		this.correctAnswer = correctAnswer;
	}
	public String getCorrectAnswer() {
		return correctAnswer;
	}
}
