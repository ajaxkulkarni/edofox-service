package com.rns.web.edo.service.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.rns.web.edo.service.domain.jpa.EdoAnswerFileEntity;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_EMPTY)
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
	private Integer visitedCount;
	private BigDecimal score;
	private boolean submitted;
	private EDOTestAnalysis analysis;
	private Date startDate;
	private Date endDate;
	private List<String> sections;
	private String testUi;
	private String randomQuestions;
	private String showResult;
	private String firebaseId;
	private String timeConstraint;
	private String studentTimeConstraint;
	private Long minLeft;
	private Long secLeft;
	private String solutionUrl;
	private String status;
	private String studentStatus;
	private Integer rank;
	private Date createdDate;
	private String showQuestionPaper;
	private Integer pauseTimeout;
	private Integer maxStarts;
	private Integer packageId;
	private List<EdoAnswerFileEntity> answerFiles;
	private Integer offlineConduction;
	private String deviceInfo;
	private String device;
	private String submissionType;
	private Date startedDate;
	private Integer acceptLocation;
	private String locationLat;
	private String locationLong;
	private Integer forceUpdate;
	private Integer instituteId;
	private String instructions;
	private List<String> jeeNewFormatSections;
	private Integer jeeMaxNumeric;
	private Integer showRank;
	private String proctoringImage;
	private Integer randomPool;
	private Integer adminReset;
	
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

	public List<String> getSections() {
		if(sections == null) {
			sections = new ArrayList<String>();
		}
		return sections;
	}

	public void setSections(List<String> sections) {
		this.sections = sections;
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

	public String getFirebaseId() {
		return firebaseId;
	}

	public void setFirebaseId(String firebaseId) {
		this.firebaseId = firebaseId;
	}

	public String getTimeConstraint() {
		return timeConstraint;
	}
	
	public void setTimeConstraint(String timeConstraint) {
		this.timeConstraint = timeConstraint;
	}
	
	public Long getMinLeft() {
		return minLeft;
	}

	public void setMinLeft(Long minLeft) {
		this.minLeft = minLeft;
	}

	public Long getSecLeft() {
		return secLeft;
	}

	public void setSecLeft(Long secLeft) {
		this.secLeft = secLeft;
	}

	public String getSolutionUrl() {
		return solutionUrl;
	}

	public void setSolutionUrl(String solutionUrl) {
		this.solutionUrl = solutionUrl;
	}

	public String getStudentTimeConstraint() {
		return studentTimeConstraint;
	}

	public void setStudentTimeConstraint(String studentTimeConstraint) {
		this.studentTimeConstraint = studentTimeConstraint;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getStudentStatus() {
		return studentStatus;
	}

	public void setStudentStatus(String studentStatus) {
		this.studentStatus = studentStatus;
	}

	public Integer getRank() {
		return rank;
	}

	public void setRank(Integer rank) {
		this.rank = rank;
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

	public Integer getPackageId() {
		return packageId;
	}

	public void setPackageId(Integer packageId) {
		this.packageId = packageId;
	}

	public List<EdoAnswerFileEntity> getAnswerFiles() {
		return answerFiles;
	}

	public void setAnswerFiles(List<EdoAnswerFileEntity> answerFiles) {
		this.answerFiles = answerFiles;
	}

	public Integer getOfflineConduction() {
		return offlineConduction;
	}

	public void setOfflineConduction(Integer offlineConduction) {
		this.offlineConduction = offlineConduction;
	}

	public String getDeviceInfo() {
		return deviceInfo;
	}

	public void setDeviceInfo(String deviceInfo) {
		this.deviceInfo = deviceInfo;
	}

	public String getDevice() {
		return device;
	}

	public void setDevice(String device) {
		this.device = device;
	}

	public String getSubmissionType() {
		return submissionType;
	}

	public void setSubmissionType(String submissionType) {
		this.submissionType = submissionType;
	}

	public Date getStartedDate() {
		return startedDate;
	}

	public void setStartedDate(Date startedDate) {
		this.startedDate = startedDate;
	}

	public Integer getAcceptLocation() {
		return acceptLocation;
	}

	public void setAcceptLocation(Integer acceptLocation) {
		this.acceptLocation = acceptLocation;
	}

	public String getLocationLat() {
		return locationLat;
	}

	public void setLocationLat(String locationLat) {
		this.locationLat = locationLat;
	}

	public String getLocationLong() {
		return locationLong;
	}

	public void setLocationLong(String locationLong) {
		this.locationLong = locationLong;
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

	public List<String> getJeeNewFormatSections() {
		return jeeNewFormatSections;
	}

	public void setJeeNewFormatSections(List<String> jeeNewFormatSections) {
		this.jeeNewFormatSections = jeeNewFormatSections;
	}

	public Integer getJeeMaxNumeric() {
		return jeeMaxNumeric;
	}

	public void setJeeMaxNumeric(Integer jeeMaxNumeric) {
		this.jeeMaxNumeric = jeeMaxNumeric;
	}

	public Integer getShowRank() {
		return showRank;
	}

	public void setShowRank(Integer showRank) {
		this.showRank = showRank;
	}

	public String getProctoringImage() {
		return proctoringImage;
	}
	
	public void setProctoringImage(String proctoringImage) {
		this.proctoringImage = proctoringImage;
	}

	public Integer getRandomPool() {
		return randomPool;
	}

	public void setRandomPool(Integer randomPool) {
		this.randomPool = randomPool;
	}

	public Integer getAdminReset() {
		return adminReset;
	}

	public void setAdminReset(Integer adminReset) {
		this.adminReset = adminReset;
	}

	public Integer getVisitedCount() {
		return visitedCount;
	}

	public void setVisitedCount(Integer visitedCount) {
		this.visitedCount = visitedCount;
	}
	
}
