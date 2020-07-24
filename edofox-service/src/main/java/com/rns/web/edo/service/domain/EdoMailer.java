package com.rns.web.edo.service.domain;

public class EdoMailer {

	private String host;
	private String mail;
	private String password;
	private String sender;
	private String fileUrl;
	private String actionUrl;
	private String supportMail;
	
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public String getMail() {
		return mail;
	}
	public void setMail(String mail) {
		this.mail = mail;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getSender() {
		return sender;
	}
	public void setSender(String sender) {
		this.sender = sender;
	}
	public String getFileUrl() {
		return fileUrl;
	}
	public void setFileUrl(String fileUrl) {
		this.fileUrl = fileUrl;
	}
	public String getActionUrl() {
		return actionUrl;
	}
	public void setActionUrl(String actionUrl) {
		this.actionUrl = actionUrl;
	}
	public String getSupportMail() {
		return supportMail;
	}
	public void setSupportMail(String supportMail) {
		this.supportMail = supportMail;
	}
	
}
