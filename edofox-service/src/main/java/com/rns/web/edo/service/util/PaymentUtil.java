package com.rns.web.edo.service.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.instamojo.wrapper.api.Instamojo;
import com.instamojo.wrapper.api.InstamojoImpl;
import com.instamojo.wrapper.exception.ConnectionException;
import com.instamojo.wrapper.exception.InvalidPaymentOrderException;
import com.instamojo.wrapper.model.PaymentOrder;
import com.instamojo.wrapper.response.CreatePaymentOrderResponse;
import com.instamojo.wrapper.response.PaymentOrderDetailsResponse;
import com.rns.web.edo.service.domain.EdoPaymentStatus;
import com.rns.web.edo.service.domain.EdoStudent;

public class PaymentUtil implements EdoConstants {

	public static EdoPaymentStatus paymentRequest(Double amount, EdoStudent student, Integer transactionId) {
		EdoPaymentStatus status = new EdoPaymentStatus();
		
		PaymentOrder order = new PaymentOrder();

		order.setName(student.getName());
		order.setEmail(student.getEmail());
		order.setPhone(student.getPhone());
		order.setCurrency("INR");
		order.setAmount(amount);
		order.setDescription("Vision Latur Payment");
		order.setRedirectUrl(HOST_NAME + "processPayment");
		order.setWebhookUrl("http://www.someurl.com/");
		order.setTransactionId("T" + transactionId);

		Instamojo api = null;

		try {
			// gets the reference to the instamojo api
			api = InstamojoImpl.getApi(CLIENT_ID, CLIENT_SECRET, API_ENDPOINT, AUTH_ENDPOINT);
		} catch (ConnectionException e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			setPaymentStatus(status, EdoConstants.ERROR_IN_PROCESSING);
		}

		boolean isOrderValid = order.validate();

		if (isOrderValid) {
			try {
				CreatePaymentOrderResponse createPaymentOrderResponse = api.createNewPaymentOrder(order);
				// print the status of the payment order.
				System.out.println(createPaymentOrderResponse.getPaymentOrder().getStatus());
				LoggingUtil.logMessage("Payment response => " + createPaymentOrderResponse.getJsonResponse());
				status.setPaymentUrl(createPaymentOrderResponse.getPaymentOptions().getPaymentUrl());
				status.setPaymentId(createPaymentOrderResponse.getPaymentOrder().getId());
			} catch (InvalidPaymentOrderException e) {
				e.printStackTrace();

				if (order.isTransactionIdInvalid()) {
					setPaymentStatus(status, "Transaction id is invalid. This is mostly due to duplicate  transaction id.");
				}
				if (order.isCurrencyInvalid()) {
					setPaymentStatus(status, "Currency is invalid.");
				}
			} catch (ConnectionException e) {
				LoggingUtil.logMessage(ExceptionUtils.getStackTrace(e));
				setPaymentStatus(status, EdoConstants.ERROR_IN_PROCESSING);
			}
		} else {
			// inform validation errors to the user.
			if (order.isTransactionIdInvalid()) {
				setPaymentStatus(status, "Transaction id is invalid.");
			}
			if (order.isAmountInvalid()) {
				setPaymentStatus(status, "Amount can not be less than 9.00.");
			}
			if (order.isCurrencyInvalid()) {
				setPaymentStatus(status, "Please provide the currency.");
			}
			if (order.isDescriptionInvalid()) {
				setPaymentStatus(status, "Description can not be greater than 255 characters.");
			}
			if (order.isEmailInvalid()) {
				setPaymentStatus(status, "Please provide valid Email Address.");
			}
			if (order.isNameInvalid()) {
				setPaymentStatus(status, "Name can not be greater than 100 characters.");
			}
			if (order.isPhoneInvalid()) {
				setPaymentStatus(status, "Phone is invalid.");
			}
			if (order.isRedirectUrlInvalid()) {
				setPaymentStatus(status, "Please provide valid Redirect url.");
			}

			if (order.isWebhookInvalid()) {
				setPaymentStatus(status, "Provide a valid webhook url");
			}
		}
		return status;
	}

	private static void setPaymentStatus(EdoPaymentStatus status, String message) {
		LoggingUtil.logMessage(message);
		status.setStatusCode(-555);
		status.setResponseText(message);
	}

	public static boolean getPaymentStatus(String id) {
		try {
			Instamojo api = InstamojoImpl.getApi(CLIENT_ID, CLIENT_SECRET, API_ENDPOINT, AUTH_ENDPOINT);
			PaymentOrderDetailsResponse paymentOrderDetailsResponse = api.getPaymentOrderDetails(id);
			// print the status of the payment order.
			LoggingUtil.logMessage("Payment status for id " + id + " is - " + paymentOrderDetailsResponse.getStatus());
			if (StringUtils.equalsIgnoreCase(PAYMENT_STATUS_COMPLETED, paymentOrderDetailsResponse.getStatus())) {
				return true;
			}
		} catch (ConnectionException e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
		}
		return false;
	}

}
