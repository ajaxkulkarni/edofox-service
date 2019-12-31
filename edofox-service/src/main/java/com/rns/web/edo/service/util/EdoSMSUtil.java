package com.rns.web.edo.service.util;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.rns.web.edo.service.domain.EDOInstitute;
import com.rns.web.edo.service.domain.EdoStudent;
import com.rns.web.edo.service.domain.EdoTest;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

public class EdoSMSUtil implements Runnable, EdoConstants {

	public static String SMS_URL = "http://bhashsms.com/api/sendmsg.php?user=7350182285&pass=a5c84b9&sender=EDOFOX&phone={phoneNo}&text={message}&priority=ndnd&smstype=normal";
	private EdoStudent student;
	private String type;
	private EdoTest test;
	private EDOInstitute institute;
	private String additionalMessage;
	private boolean copyParent;

	public EdoSMSUtil() {

	}

	public EdoSMSUtil(String type) {
		this.type = type;
	}

	public void setStudent(EdoStudent student) {
		this.student = student;
	}

	public void setCopyParent(boolean copyParent) {
		this.copyParent = copyParent;
	}

	public void setInstitute(EDOInstitute institute) {
		this.institute = institute;
	}

	public void run() {
		sendSMS();
	}

	private void sendSMS() {

		if (student == null) {
			return;
		}

		try {

			String url = StringUtils.replace(SMS_URL, "{phoneNo}", student.getPhone());

			String message = SMS_TEMPLATES.get(type);
			/*
			 * message = StringUtils.replace(message, "{name}",
			 * student.getName()); message = StringUtils.replace(message,
			 * "{transactionId}",
			 * CommonUtils.getStringValue(student.getTransactionId()));
			 */
			message = CommonUtils.prepareStudentNotification(message, student);

			if (test != null) {
				message = CommonUtils.prepareTestNotification(message, test, institute, additionalMessage);
			} else if (institute != null) {
				message = CommonUtils.prepareInstituteNotification(message, institute);
			}
			url = StringUtils.replace(url, "{message}", URLEncoder.encode(message, "UTF-8"));
			sendSMS(url);

			if (copyParent && StringUtils.isNotBlank(student.getParentMobileNo())) {
				// Send same SMS to parents
				url = StringUtils.replace(SMS_URL, "{phoneNo}", student.getParentMobileNo());
				url = StringUtils.replace(url, "{message}", URLEncoder.encode(message, "UTF-8"));
				sendSMS(url);
			}

		} catch (Exception e) {
			LoggingUtil.logMessage(ExceptionUtils.getStackTrace(e));
		}

	}

	private void sendSMS(String url) {
		ClientConfig config = new DefaultClientConfig();
		Client client = Client.create(config);
		WebResource webResource = client.resource(url);
		LoggingUtil.logMessage("Calling SMS URL:" + url);
		ClientResponse response = webResource.get(ClientResponse.class);
		if (response.getStatus() != 200) {
			LoggingUtil.logError("SMS sending Failed : HTTP error code : " + response.getStatus());
		}
		LoggingUtil.logMessage("Output from SMS .... " + response.getStatus() + " \n");
	}

	public static void main(String[] args) {
		EdoSMSUtil mail = new EdoSMSUtil(MAIL_TYPE_TEST_RESULT);
		EdoStudent s = new EdoStudent();
		s.setPhone("9423040642");
		s.setName("Ajinkya C Kulkarni");
		EdoTest t = new EdoTest();
		t.setSolvedCount(11);
		t.setCorrectCount(11);
		t.setTotalMarks(1800);
		t.setName("JEE 2018");
		t.setScore(new BigDecimal(123));
		mail.setTest(t);
		mail.setStudent(s);
		mail.sendSMS();
	}

	public EdoTest getTest() {
		return test;
	}

	public void setTest(EdoTest test) {
		this.test = test;
	}

	public String getAdditionalMessage() {
		return additionalMessage;
	}

	public void setAdditionalMessage(String additionalMessage) {
		this.additionalMessage = additionalMessage;
	}

	private static Map<String, String> SMS_TEMPLATES = Collections.unmodifiableMap(new HashMap<String, String>() {
		{
			put(MAIL_TYPE_SUBSCRIPTION,
					"Hi {name}, Thank you for registering for {instituteName}. Please complete the payment in order to have full access to {instituteName} courses.");
			put(MAIL_TYPE_ACTIVATED, "Hi {name}, your {instituteName} package {packages} is activated. Transaction ID - {transactionId}.");
			put(MAIL_TYPE_TEST_RESULT, "Hi {name}, your {instituteName} {testName} final result is - "
					+ "\nSolved  - {solved} \nCorrect answers - {correctCount} \nScore - {score} \nOut of - {totalMarks}" + "\n{additionalMessage}");
			put(MAIL_TYPE_TEST_RESULT_RANK,
					"Hi {name}, your {instituteName} {testName} score is {score} " + "\nYour final rank is {rank} out of {totalStudents}\n{additionalMessage}");
		}
	});

}
