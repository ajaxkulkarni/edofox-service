package com.rns.web.edo.service.domain;

import java.math.BigDecimal;

import com.rns.web.edo.service.util.EdoConstants;

public class EdoPaymentStatus extends EdoApiStatus {

	private String paymentUrl;
	private String paymentId;
	private boolean offline;
	private String mode;
	private Double amount;
	
	public EdoPaymentStatus() {
		setStatusCode(EdoConstants.STATUS_OK);
		setResponseText(EdoConstants.RESPONSE_OK);
	}
	
	public String getPaymentUrl() {
		return paymentUrl;
	}
	public void setPaymentUrl(String paymentUrl) {
		this.paymentUrl = paymentUrl;
	}
	public String getPaymentId() {
		return paymentId;
	}
	public void setPaymentId(String paymentId) {
		this.paymentId = paymentId;
	}

	public boolean isOffline() {
		return offline;
	}

	public void setOffline(boolean offline) {
		this.offline = offline;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}
	
	
}
