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

}
