package com.rns.web.edo.service.util;

import java.io.IOException;
import java.math.BigDecimal;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.instamojo.wrapper.api.Instamojo;
import com.instamojo.wrapper.api.InstamojoImpl;
import com.instamojo.wrapper.exception.ConnectionException;
import com.instamojo.wrapper.exception.InvalidPaymentOrderException;
import com.instamojo.wrapper.model.PaymentOrder;
import com.instamojo.wrapper.response.CreatePaymentOrderResponse;
import com.instamojo.wrapper.response.PaymentOrderDetailsResponse;
import com.rns.web.edo.service.domain.EDOInstitute;
import com.rns.web.edo.service.domain.EdoPaymentStatus;
import com.rns.web.edo.service.domain.EdoStudent;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jersey.multipart.BodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.MultiPart;

public class PaymentUtil implements EdoConstants {

	public static EdoPaymentStatus paymentRequest(Double amount, EdoStudent student, Integer transactionId) {
		EdoPaymentStatus status = new EdoPaymentStatus();
		
		PaymentOrder order = new PaymentOrder();

		order.setName(student.getName());
		order.setEmail(student.getEmail());
		order.setPhone(student.getPhone());
		order.setCurrency("INR");
		order.setAmount(amount);
		order.setDescription("Edofox payment");
		order.setRedirectUrl(EdoPropertyUtil.getProperty(EdoPropertyUtil.HOST_URL) + "processPayment");
		order.setWebhookUrl(EdoPropertyUtil.getProperty(EdoPropertyUtil.HOST_URL) + "paymentWebhook");
		order.setTransactionId("T" + transactionId);

		Instamojo api = null;

		try {
			// gets the reference to the instamojo api
			api = InstamojoImpl.getApi(EdoPropertyUtil.getProperty(EdoPropertyUtil.CLIENT_ID), EdoPropertyUtil.getProperty(EdoPropertyUtil.CLIENT_SECRET), EdoPropertyUtil.getProperty(EdoPropertyUtil.API_ENDPOINT), EdoPropertyUtil.getProperty(EdoPropertyUtil.AUTH_ENDPOINT));
		} catch (ConnectionException e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			LoggingUtil.logMessage(ExceptionUtils.getStackTrace(e), LoggingUtil.paymentLogger);
			setPaymentStatus(status, EdoConstants.ERROR_IN_PROCESSING);
		}

		boolean isOrderValid = order.validate();

		if (isOrderValid) {
			try {
				CreatePaymentOrderResponse createPaymentOrderResponse = api.createNewPaymentOrder(order);
				// print the status of the payment order.
				LoggingUtil.logMessage("Successful Payment response => " + createPaymentOrderResponse.getJsonResponse(), LoggingUtil.paymentLogger);
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
				
				LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
				LoggingUtil.logMessage(ExceptionUtils.getStackTrace(e), LoggingUtil.paymentLogger);
				setPaymentStatus(status, EdoConstants.ERROR_IN_PROCESSING);
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
			Instamojo api = InstamojoImpl.getApi(EdoPropertyUtil.getProperty(EdoPropertyUtil.CLIENT_ID), EdoPropertyUtil.getProperty(EdoPropertyUtil.CLIENT_SECRET), EdoPropertyUtil.getProperty(EdoPropertyUtil.API_ENDPOINT), EdoPropertyUtil.getProperty(EdoPropertyUtil.AUTH_ENDPOINT));
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

	public static void settle(EdoStudent student, EDOInstitute institute, BigDecimal amount, String paymentId) {
		try {
			ClientConfig config = new DefaultClientConfig();
			config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
			Client client = Client.create(config);
			
			String url = EdoPropertyUtil.getProperty(EdoPropertyUtil.SETTLEMENT_URL) + "";
			WebResource webResource = client.resource(url);

			/*MultivaluedMap<String, String> request = new MultivaluedMapImpl();
			request.add("businessPhone", institute.getContact());
			request.add("customerPhone", student.getPhone());
			request.add("customerEmail", student.getEmail());
			request.add("customerName", student.getName());
			request.add("amount", amount.toString());
			request.add("businessPhone", paymentId);*/
			
			FormDataMultiPart request = new FormDataMultiPart();
			request.field("businessPhone", institute.getContact());
			request.field("customerPhone", student.getPhone());
			request.field("customerEmail", student.getEmail());
			request.field("customerName", student.getName());
			request.field("amount", amount.toString());
			request.field("paymentId", paymentId);
			LoggingUtil.logMessage("Calling settlement with URL =>" + url + " AND request=>" + request.getFields());
			
			ClientResponse response = webResource.header("Token", EdoPropertyUtil.getProperty(EdoPropertyUtil.SETTLEMENT_TOKEN)).
					type(MediaType.MULTIPART_FORM_DATA).post(ClientResponse.class, request);
			//ClientResponse response = webResource.queryParam("receiverId", id.toString()).post(ClientResponse.class);

			String output = response.getEntity(String.class);
			if (response.getStatus() != 200) {
				throw new RuntimeException("Failed : HTTP error code : " + response.getStatus() + " RESP:" + output);
			}
			LoggingUtil.logMessage("Output from settlement URL ...." + response.getStatus() + " RESP:" + output + " \n");

		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
		}
	}

}
