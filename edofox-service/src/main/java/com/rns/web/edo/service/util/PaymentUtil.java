package com.rns.web.edo.service.util;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

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
import com.sun.jersey.multipart.FormDataMultiPart;

public class PaymentUtil implements EdoConstants {

	public static EdoPaymentStatus paymentRequest(Double amount, EdoStudent student, Integer transactionId, String clientId, String clientSecret, String paymentPurpose) {
		//String paymentPurpose = "Edofox payment";
		if(paymentPurpose == null) {
			paymentPurpose =  "Edofox payment";
		}
		if(clientId == null) {
			clientId = EdoPropertyUtil.getProperty(EdoPropertyUtil.CLIENT_ID);
		}
		if(clientSecret == null) {
			clientSecret = EdoPropertyUtil.getProperty(EdoPropertyUtil.CLIENT_SECRET);
		}
		
		EdoPaymentStatus status = new EdoPaymentStatus();
		
		PaymentOrder order = new PaymentOrder();

		order.setName(student.getName());
		order.setEmail(student.getEmail());
		order.setPhone(student.getPhone());
		order.setCurrency("INR");
		order.setAmount(amount);
		order.setDescription(paymentPurpose);
		order.setRedirectUrl(EdoPropertyUtil.getProperty(EdoPropertyUtil.HOST_URL) + "processPayment");
		order.setWebhookUrl(EdoPropertyUtil.getProperty(EdoPropertyUtil.HOST_URL) + "paymentWebhook");
		order.setTransactionId("T" + transactionId);

		Instamojo api = null;

		try {
			// gets the reference to the instamojo api
			api = InstamojoImpl.getApi(clientId, clientSecret, EdoPropertyUtil.getProperty(EdoPropertyUtil.API_ENDPOINT), EdoPropertyUtil.getProperty(EdoPropertyUtil.AUTH_ENDPOINT));
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

	public static boolean getPaymentStatus(String id, String clientId, String clientSecret, EdoPaymentStatus status) {
		try {
			if(clientId == null) {
				clientId = EdoPropertyUtil.getProperty(EdoPropertyUtil.CLIENT_ID);
			}
			if(clientSecret == null) {
				clientSecret = EdoPropertyUtil.getProperty(EdoPropertyUtil.CLIENT_SECRET);
			}
			Instamojo api = InstamojoImpl.getApi(clientId, clientSecret, EdoPropertyUtil.getProperty(EdoPropertyUtil.API_ENDPOINT), EdoPropertyUtil.getProperty(EdoPropertyUtil.AUTH_ENDPOINT));
			PaymentOrderDetailsResponse paymentOrderDetailsResponse = api.getPaymentOrderDetails(id);
			// print the status of the payment order.
			LoggingUtil.logMessage("Payment status for id " + id + " is - " + paymentOrderDetailsResponse.getStatus());
			if (StringUtils.equalsIgnoreCase(PAYMENT_STATUS_COMPLETED, paymentOrderDetailsResponse.getStatus())) {
				status.setAmount(paymentOrderDetailsResponse.getAmount());
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

	
	
	private static String hashCal(String type, String str) {
        byte[] hashseq = str.getBytes();
        StringBuffer hexString = new StringBuffer();
        try {
            MessageDigest algorithm = MessageDigest.getInstance(type);
            algorithm.reset();
            algorithm.update(hashseq);
            byte messageDigest[] = algorithm.digest();
            for (int i = 0; i < messageDigest.length; i++) {
                String hex = Integer.toHexString(0xFF & messageDigest[i]);
                if (hex.length() == 1) {
                    hexString.append("0");
                }
                hexString.append(hex);
            }

        } catch (NoSuchAlgorithmException nsae) {
        }
        return hexString.toString();
    }

    public static EdoPaymentStatus getHash(String txnid, Double amount, EdoStudent student, String paymentPurpose) {
    	EdoPaymentStatus status = new EdoPaymentStatus();
        String key = EdoPropertyUtil.getProperty("deeper.insta.client.id");
        String salt = EdoPropertyUtil.getProperty("deeper.insta.client.secret");
		
        //String action1 = "";
        //String base_url = "https://sandboxsecure.payu.in/";
        String base_url = "https://secure.payu.in/";
        String hash = "";
        //String otherPostParamSeq = "phone|surl|furl|lastname|curl|address1|address2|city|state|country|zipcode|pg";
        String hashSequence = "{key}|{txnid}|{amount}|{productinfo}|{firstname}|{email}|||||||||||{salt}";
        
        //Prepare hash
        hashSequence = StringUtils.replace(hashSequence, "{key}", key);
        hashSequence = StringUtils.replace(hashSequence, "{txnid}", txnid);
        hashSequence = StringUtils.replace(hashSequence, "{amount}", String.format("%.2f", amount));
        hashSequence = StringUtils.replace(hashSequence, "{productinfo}", paymentPurpose);
        hashSequence = StringUtils.replace(hashSequence, "{firstname}", student.getName());
        hashSequence = StringUtils.replace(hashSequence, "{email}", student.getEmail());
        hashSequence = StringUtils.replace(hashSequence, "{salt}", salt);
        
        
        hash = hashCal("SHA-512", hashSequence);
        
        status.setTxnid(txnid);
        status.setAmount(amount);
        status.setHash(hash);
        status.setHashString(hashSequence);
        status.setRedirectUrl(EdoPropertyUtil.getProperty(EdoPropertyUtil.HOST_URL) + "payUSuccess");
        status.setFailureUrl(EdoPropertyUtil.getProperty(EdoPropertyUtil.HOST_URL) + "payUFailure");
        status.setPaymentUrl(base_url + "_payment");
        status.setKey(key);
        status.setProductInfo(paymentPurpose);
        status.setStatusCode(200);
        
        LoggingUtil.logMessage("Payment Hash " + hash + " for string " + hashSequence, LoggingUtil.paymentLogger);
        
        return status;
    }
    
    public static void main(String[] args) {
		//System.out.println(hashCal("SHA-512", "005QTvo8|11aa|100.00|myproduct|ajinkya|ajinkyashiva@gmail.com|||||||||||YpI1nBbIqJ"));
		//System.out.println(hashCal("SHA-512", "005QTvo8|11aa|100.00|myproduct|ajinkya|ajinkyashiva@gmail.com|||||||||||YpI1nBbIqJ"));
		System.out.println(String.format("%.2f", new Double(100.12331)));
    }
	
}
