package com.rns.web.edo.service.domain;

import java.util.Date;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;


@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_EMPTY)
public class EdoAdminRequest {
	
	private List<EdoTestQuestionMap> testQuestionMaps;
	private List<EdoTestStudentMap> testStudentMaps;
	private String hostName;
	private Date date;
	private Integer buffer;
	private String questionPrefix;
	private String questionSuffix;
	private EdoTest test;
	private EDOInstitute institute;
	private boolean cet;
	private boolean jee;
	private boolean neet;
	private EdoStudent student;
	private Integer fromQuestion;
	private Integer toQuestion;
	private String cropWidth;
	private String filePath;
	
	public List<EdoTestQuestionMap> getTestQuestionMaps() {
		return testQuestionMaps;
	}
	public void setTestQuestionMaps(List<EdoTestQuestionMap> testQuestionMaps) {
		this.testQuestionMaps = testQuestionMaps;
	}
	public List<EdoTestStudentMap> getTestStudentMaps() {
		return testStudentMaps;
	}
	public void setTestStudentMaps(List<EdoTestStudentMap> testStudentMaps) {
		this.testStudentMaps = testStudentMaps;
	}
	public String getHostName() {
		return hostName;
	}
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public Integer getBuffer() {
		return buffer;
	}
	public void setBuffer(Integer buffer) {
		this.buffer = buffer;
	}
	public String getQuestionPrefix() {
		return questionPrefix;
	}
	public void setQuestionPrefix(String questionPrefix) {
		this.questionPrefix = questionPrefix;
	}
	public EdoTest getTest() {
		return test;
	}
	public void setTest(EdoTest test) {
		this.test = test;
	}
	public String getQuestionSuffix() {
		return questionSuffix;
	}
	public void setQuestionSuffix(String questionSuffix) {
		this.questionSuffix = questionSuffix;
	}
	public EDOInstitute getInstitute() {
		return institute;
	}
	public void setInstitute(EDOInstitute institute) {
		this.institute = institute;
	}
	public boolean isCet() {
		return cet;
	}
	public void setCet(boolean cet) {
		this.cet = cet;
	}
	public boolean isJee() {
		return jee;
	}
	public void setJee(boolean jee) {
		this.jee = jee;
	}
	public boolean isNeet() {
		return neet;
	}
	public void setNeet(boolean neet) {
		this.neet = neet;
	}
	public EdoStudent getStudent() {
		return student;
	}
	public void setStudent(EdoStudent student) {
		this.student = student;
	}
	public Integer getFromQuestion() {
		return fromQuestion;
	}
	public void setFromQuestion(Integer fromQuestion) {
		this.fromQuestion = fromQuestion;
	}
	public Integer getToQuestion() {
		return toQuestion;
	}
	public void setToQuestion(Integer toQuestion) {
		this.toQuestion = toQuestion;
	}
	public String getCropWidth() {
		return cropWidth;
	}
	public void setCropWidth(String cropWidth) {
		this.cropWidth = cropWidth;
	}
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

}
