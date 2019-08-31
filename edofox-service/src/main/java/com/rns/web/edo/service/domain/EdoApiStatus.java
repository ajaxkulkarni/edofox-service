package com.rns.web.edo.service.domain;

import com.rns.web.edo.service.util.EdoConstants;

public class EdoApiStatus {
	
	private String responseText;
	private Integer statusCode;
	
	public EdoApiStatus() {
		setStatusCode(EdoConstants.STATUS_OK);
		setResponseText(EdoConstants.RESPONSE_OK);
	}
	
	public EdoApiStatus(int code, String message) {
		setStatusCode(code);
		setResponseText(message);
	}
	
	public String getResponseText() {
		return responseText;
	}
	public void setResponseText(String responseText) {
		this.responseText = responseText;
	}
	public Integer getStatusCode() {
		return statusCode;
	}
	public void setStatusCode(Integer statusCode) {
		this.statusCode = statusCode;
	}
	
	public void setStatus(int code, String message) {
		setStatusCode(code);
		setResponseText(message);
	}

}
