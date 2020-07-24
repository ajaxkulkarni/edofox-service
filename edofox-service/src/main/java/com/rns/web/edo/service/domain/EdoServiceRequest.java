package com.rns.web.edo.service.domain;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.rns.web.edo.service.domain.jpa.EdoClasswork;
import com.rns.web.edo.service.domain.jpa.EdoDeviceId;
import com.rns.web.edo.service.domain.jpa.EdoNotice;
import com.rns.web.edo.service.domain.jpa.EdoVideoLecture;


@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_EMPTY)
public class EdoServiceRequest {
	
	private EdoStudent student;
	private EdoTest test;
	private EDOInstitute institute;
	private String filePath;
	private Integer firstQuestion;
	private Integer subjectId;
	private String solutionPath;
	private List<EdoStudent> students;
	private String requestType;
	private String smsMessage;
	private EdoQuestion question;
	private EdoFeedback feedback;
	private Date fromDate;
	private Date toDate;
	private EdoVideoLecture lecture;

	private Integer startIndex;
	private String searchFilter;

	private EdoMailer mailer;
	private EdoDeviceId deviceId;
	private EdoClasswork classwork;
	private EdoNotice notice;

	
	public EdoStudent getStudent() {
		return student;
	}

	public void setStudent(EdoStudent student) {
		this.student = student;
	}

	public EdoTest getTest() {
		return test;
	}

	public void setTest(EdoTest test) {
		this.test = test;
	}

	public EDOInstitute getInstitute() {
		return institute;
	}

	public void setInstitute(EDOInstitute institute) {
		this.institute = institute;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public Integer getFirstQuestion() {
		return firstQuestion;
	}

	public void setFirstQuestion(Integer firstQuestion) {
		this.firstQuestion = firstQuestion;
	}

	public Integer getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(Integer subjectId) {
		this.subjectId = subjectId;
	}

	public String getSolutionPath() {
		return solutionPath;
	}

	public void setSolutionPath(String solutionPath) {
		this.solutionPath = solutionPath;
	}

	public List<EdoStudent> getStudents() {
		return students;
	}

	public void setStudents(List<EdoStudent> students) {
		this.students = students;
	}
	
	@Override
	public String toString() {
		
		try {
			return new ObjectMapper().writeValueAsString(this);
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}

	public String getRequestType() {
		return requestType;
	}

	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}

	public String getSmsMessage() {
		return smsMessage;
	}

	public void setSmsMessage(String smsMessage) {
		this.smsMessage = smsMessage;
	}

	public EdoQuestion getQuestion() {
		return question;
	}

	public void setQuestion(EdoQuestion question) {
		this.question = question;
	}

	public EdoFeedback getFeedback() {
		return feedback;
	}

	public void setFeedback(EdoFeedback feedback) {
		this.feedback = feedback;
	}

	public Date getFromDate() {
		return fromDate;
	}

	public void setFromDate(Date fromDate) {
		this.fromDate = fromDate;
	}

	public Date getToDate() {
		return toDate;
	}

	public void setToDate(Date toDate) {
		this.toDate = toDate;
	}

	public EdoVideoLecture getLecture() {
		return lecture;
	}

	public void setLecture(EdoVideoLecture lecture) {
		this.lecture = lecture;
	}


	public Integer getStartIndex() {
		return startIndex;
	}

	public void setStartIndex(Integer startIndex) {
		this.startIndex = startIndex;
	}

	public String getSearchFilter() {
		return searchFilter;
	}

	public void setSearchFilter(String searchFilter) {
		this.searchFilter = searchFilter;
	}

	public EdoMailer getMailer() {
		return mailer;
	}

	public void setMailer(EdoMailer mailer) {
		this.mailer = mailer;
	}

	public EdoDeviceId getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(EdoDeviceId deviceId) {
		this.deviceId = deviceId;
	}

	public EdoClasswork getClasswork() {
		return classwork;
	}

	public void setClasswork(EdoClasswork classwork) {
		this.classwork = classwork;
	}
	
	public EdoNotice getNotice() {
		return notice;
	}
	
	public void setNotice(EdoNotice notice) {
		this.notice = notice;

	}
	
}
