package com.rns.web.edo.service.util;

import java.net.URLEncoder;
import java.rmi.server.SkeletonMismatchException;
import java.util.Base64.Encoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.rns.web.edo.service.domain.EdoStudent;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

public class EdoSMSUtil implements Runnable, EdoConstants {
	
	public static String SMS_URL = "http://bhashsms.com/api/sendmsg.php?user=7350182285&pass=a5c84b9&sender=EDOFOX&phone={phoneNo}&text={message}&priority=ndnd&smstype=normal";
	private EdoStudent student;
	private String type;
	
	public EdoSMSUtil() {
	
	}
	
	public EdoSMSUtil(String type) {
		this.type = type;
	}
	
	public void setStudent(EdoStudent student) {
		this.student = student;
	}

	public void run() {
		sendSMS();
	}

	private void sendSMS() {
		
		if(student == null) {
			return;
		}
		
		try {
			
			String url = StringUtils.replace(SMS_URL, "{phoneNo}", student.getPhone());
			
			String message = SMS_TEMPLATES.get(type);
			/*message = StringUtils.replace(message, "{name}", student.getName());
			message = StringUtils.replace(message, "{transactionId}", CommonUtils.getStringValue(student.getTransactionId()));*/
			message = CommonUtils.prepareStudentNotification(message, student);
			url = StringUtils.replace(url, "{message}", URLEncoder.encode(message, "UTF-8"));
			ClientConfig config = new DefaultClientConfig();
			Client client = Client.create(config);
			WebResource webResource = client.resource(url);
			
			LoggingUtil.logMessage("Calling SMS URL:" + url);

			ClientResponse response = webResource.get(ClientResponse.class);

			if (response.getStatus() != 200) {
				LoggingUtil.logError("SMS sending Failed : HTTP error code : " + response.getStatus());
			}
			LoggingUtil.logMessage("Output from SMS .... " + response.getStatus() + " \n");
			
		} catch (Exception e) {
			LoggingUtil.logMessage(ExceptionUtils.getStackTrace(e));
		}
		
	}

	public static void main(String[] args) {
		EdoSMSUtil mail = new EdoSMSUtil(MAIL_TYPE_SUBSCRIPTION);
		EdoStudent s = new EdoStudent();
		s.setPhone("9423040642");
		s.setName("Ajinkya C Kulkarni");
		mail.setStudent(s);
		mail.sendSMS();
	}
	
	private static Map<String, String> SMS_TEMPLATES = Collections.unmodifiableMap(new HashMap<String, String>() {
		{
			put(MAIL_TYPE_SUBSCRIPTION, "Hi {name}, Welcome to Vision Latur. Please complete the payment in order to have full access to Vision Latur features.");
			put(MAIL_TYPE_ACTIVATED, "Hi {name}, your Vision Latur package {packages} is activated. Transaction ID - {transactionId}.");
		}
	});
	
	
}
