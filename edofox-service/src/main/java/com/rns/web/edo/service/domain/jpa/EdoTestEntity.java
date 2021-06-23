package com.rns.web.edo.service.domain.jpa;

import static javax.persistence.GenerationType.IDENTITY;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicUpdate;

@Entity
@Table(name = "test")
@DynamicUpdate
public class EdoTestEntity {
	
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "test_id", unique = true)
	private Integer id;
	@Column(name = "test_name")
	private String name;
	@Column(name = "no_of_questions")
	private Integer noOfQuestions;
	@Column(name = "total_marks")
	private Integer totalMarks;
	@Column(name = "duration")
	private Integer duration;
	@Column(name = "start_date")
	private Date startDate;
	@Column(name = "end_date")
	private Date endDate;
	@Column(name = "test_ui")
	private String testUi;
	@Column(name = "random_questions")
	private String randomQuestions;
	@Column(name = "show_result")
	private String showResult;
	@Column(name = "time_constraint")
	private String timeConstraint;
	@Column(name = "student_time_constraint")
	private String studentTimeConstraint;
	/*@Column(name = "student_time_constraint")
	private String status;*/
	@Column(name = "created_date")
	private Date createdDate;
	@Column(name = "show_question_paper")
	private String showQuestionPaper;
	@Column(name = "pause_timeout_seconds")
	private Integer pauseTimeout;
	@Column(name = "max_allowed_test_starts")
	private Integer maxStarts;
	@Column(name = "offline_conduction")
	private Integer offlineConduction;
	@Column(name = "accept_location")
	private Integer acceptLocation;
	@Column(name = "force_update")
	private Integer forceUpdate;
	@Column(name = "institute_id")
	private Integer instituteId;
	@Column(name = "custom_instructions")
	private String instructions;
	@Column(name = "random_pool")
	private Integer randomPool;
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
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
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	public Date getEndDate() {
		return endDate;
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	public String getTestUi() {
		return testUi;
	}
	public void setTestUi(String testUi) {
		this.testUi = testUi;
	}
	public String getRandomQuestions() {
		return randomQuestions;
	}
	public void setRandomQuestions(String randomQuestions) {
		this.randomQuestions = randomQuestions;
	}
	public String getShowResult() {
		return showResult;
	}
	public void setShowResult(String showResult) {
		this.showResult = showResult;
	}
	public String getTimeConstraint() {
		return timeConstraint;
	}
	public void setTimeConstraint(String timeConstraint) {
		this.timeConstraint = timeConstraint;
	}
	public String getStudentTimeConstraint() {
		return studentTimeConstraint;
	}
	public void setStudentTimeConstraint(String studentTimeConstraint) {
		this.studentTimeConstraint = studentTimeConstraint;
	}
	public Date getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	public String getShowQuestionPaper() {
		return showQuestionPaper;
	}
	public void setShowQuestionPaper(String showQuestionPaper) {
		this.showQuestionPaper = showQuestionPaper;
	}
	public Integer getPauseTimeout() {
		return pauseTimeout;
	}
	public void setPauseTimeout(Integer pauseTimeout) {
		this.pauseTimeout = pauseTimeout;
	}
	public Integer getMaxStarts() {
		return maxStarts;
	}
	public void setMaxStarts(Integer maxStarts) {
		this.maxStarts = maxStarts;
	}
	public Integer getOfflineConduction() {
		return offlineConduction;
	}
	public void setOfflineConduction(Integer offlineConduction) {
		this.offlineConduction = offlineConduction;
	}
	public Integer getAcceptLocation() {
		return acceptLocation;
	}
	public void setAcceptLocation(Integer acceptLocation) {
		this.acceptLocation = acceptLocation;
	}
	public Integer getForceUpdate() {
		return forceUpdate;
	}
	public void setForceUpdate(Integer forceUpdate) {
		this.forceUpdate = forceUpdate;
	}
	public Integer getInstituteId() {
		return instituteId;
	}
	public void setInstituteId(Integer instituteId) {
		this.instituteId = instituteId;
	}
	public String getInstructions() {
		return instructions;
	}
	public void setInstructions(String instructions) {
		this.instructions = instructions;
	}
	public Integer getRandomPool() {
		return randomPool;
	}
	public void setRandomPool(Integer randomPool) {
		this.randomPool = randomPool;
	}
	
}
