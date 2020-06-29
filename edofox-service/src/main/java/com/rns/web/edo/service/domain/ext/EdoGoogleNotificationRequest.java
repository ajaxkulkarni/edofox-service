package com.rns.web.edo.service.domain.ext;

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_EMPTY)
public class EdoGoogleNotificationRequest {
	
	private List<String> registration_ids;
	private String to;
	private EdoGoogleNotification notification;
	
	public List<String> getRegistration_ids() {
		return registration_ids;
	}
	public void setRegistration_ids(List<String> registration_ids) {
		this.registration_ids = registration_ids;
	}
	public String getTo() {
		return to;
	}
	public void setTo(String to) {
		this.to = to;
	}
	public EdoGoogleNotification getNotification() {
		return notification;
	}
	public void setNotification(EdoGoogleNotification notification) {
		this.notification = notification;
	}
	
}