package com.rns.web.edo.service.domain;

import com.rns.web.edo.service.bo.domain.EdoStudent;

public class EdoServiceResponse {
	
	private String responseText;
	private Integer status;
	private EdoStudent student;
	
	public String getResponseText() {
		return responseText;
	}
	public void setResponseText(String responseText) {
		this.responseText = responseText;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public EdoStudent getStudent() {
		return student;
	}
	public void setStudent(EdoStudent student) {
		this.student = student;
	}

}
