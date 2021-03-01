package com.rns.web.edo.service.util;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.rns.web.edo.service.domain.EDOInstitute;
import com.rns.web.edo.service.domain.EdoMailer;
import com.rns.web.edo.service.domain.EdoStudent;
import com.rns.web.edo.service.domain.EdoTest;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

public class EdoSMSUtil implements Runnable, EdoConstants {

	//public static String SMS_URL = "http://bhashsms.com/api/sendmsg.php?user=7350182285&pass=a5c84b9&sender=EDOFOX&phone={phoneNo}&text={message}&priority=ndnd&smstype=normal";
	public static String SMS_URL = "http://api.msg91.com/api/sendhttp.php?country=91&sender=MTRSFT&route=4&mobiles={phoneNo}&authkey={auth}&message={message}";
	
	/*String apiKey = "apikey=" + URLEncoder.encode("yourapiKey", "UTF-8");
	String message = "&message=" + URLEncoder.encode("This is your message", "UTF-8");
	String sender = "&sender=" + URLEncoder.encode("TXTLCL", "UTF-8");
	String numbers = "&numbers=" + URLEncoder.encode("918123456789", "UTF-8");
	*/
	// Send data
	//textlocal
	//public static String SMS_URL = "https://api.textlocal.in/send/?apikey={auth}&numbers={phoneNo}&message={message}&sender={sender}";
	
	private EdoStudent student;
	private String type;
	private EdoTest test;
	private EDOInstitute institute;
	private String additionalMessage;
	private boolean copyParent;
	private boolean copyAdmin;
	private EdoMailer mailer;

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

	public void sendSMS() {

		if (student == null) {
			return;
		}

		try {

			String url = initUrl(student.getPhone());

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
				url = initUrl(student.getParentMobileNo());
				url = StringUtils.replace(url, "{message}", URLEncoder.encode(message, "UTF-8"));
				sendSMS(url);
			}
			
			if(copyAdmin) {
				// Send same SMS to admin
				if(ArrayUtils.isNotEmpty(ADMIN_NUMBERS)) {
					for(String number: ADMIN_NUMBERS) {
						url = initUrl(number);
						url = StringUtils.replace(url, "{message}", URLEncoder.encode(message, "UTF-8"));
						sendSMS(url);
					}
				}
			}

		} catch (Exception e) {
			LoggingUtil.logMessage(ExceptionUtils.getStackTrace(e));
		}

	}

	private String initUrl(String phone) {
		String url = StringUtils.replace(SMS_URL, "{phoneNo}", phone);
		url = StringUtils.replace(url, "{auth}", EdoPropertyUtil.getProperty(EdoPropertyUtil.SMS_AUTH_KEY));
		return url;
	}

	private void sendSMS(String url) {
		ClientConfig config = new DefaultClientConfig();
		Client client = Client.create(config);
		WebResource webResource = client.resource(url);
		LoggingUtil.logMessage("Calling SMS URL:" + url);
		ClientResponse response = webResource.get(ClientResponse.class);
		if (response.getStatus() != 200) {
			LoggingUtil.logError("SMS sending Failed : HTTP error code : " + response.getStatus(), LoggingUtil.emailLogger);
		}
		LoggingUtil.logMessage("Output from SMS .... " + response.getStatus() + " \n", LoggingUtil.emailLogger);
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

	public boolean isCopyAdmin() {
		return copyAdmin;
	}

	public void setCopyAdmin(boolean copyAdmin) {
		this.copyAdmin = copyAdmin;
	}

	public EdoMailer getMailer() {
		return mailer;
	}

	public void setMailer(EdoMailer mailer) {
		this.mailer = mailer;
	}

	private static Map<String, String> SMS_TEMPLATES = Collections.unmodifiableMap(new HashMap<String, String>() {
		{
			put(MAIL_TYPE_SUBSCRIPTION,
					"Hi {name}, Thank you for registering to {instituteName}. Login to test.edofox.com using the username and password you provided to have access to its features. "
					+ "{paymentMessage}");
			put(MAIL_TYPE_ACTIVATED, "Hi {name}, your {instituteName} package {packages} is activated. Transaction ID - {transactionId}.");
			put(MAIL_TYPE_TEST_RESULT, "Hi {name}, your {instituteName} {testName} final result is - "
					+ "\nSolved  - {solved} \nCorrect answers - {correctCount} \nScore - {score} \nOut of - {totalMarks}" + "\n{additionalMessage}");
			put(MAIL_TYPE_TEST_RESULT_RANK,
					"Hi {name}, your {instituteName} {testName} score is {score} " + "\nYour final rank is {rank} out of {totalStudents}\n{additionalMessage}");
			put(MAIL_TYPE_SIGN_UP,
					"Thank you for signing up {instituteName} for {purchase} plan at Edofox.\nFeel free to check out admin panel at test.edofox.com/test-adminPanel.\nYour login credentials are login ID - {username}, password - {password}."
					+ "\nUse your registered mobile number and the same password to login as teacher at test.edofox.com. Our associate will contact you for further assistance. {expiryMessage}");
			put(MAIL_TYPE_SIGN_UP_DEMO,
					"Thank you for signing up {instituteName} at Edofox Live."
					+ "\nUse your registered mobile number and password to login as student at live.edofox.com. Your free trial includes live classroom feature with unlimited time upto 100 students. "
					+ "You can upgrade your plan anytime to enable other premium features.");
			put(MAIL_TYPE_UPGRADE,
					"Congratulations {instituteName}. Your account has been successfully updated to {purchase} plan at Edofox. {expiryMessage}");
			//put(MAIL_TYPE_APPOINTMENT, "Hi {name}, {instituteName} has invited you on {date} at {time} for verification round. Please bring confirmation letter and application form with you");
			put(MAIL_TYPE_APPOINTMENT, "Dear Parents,\nYou are scheduled to come for the Document Verification of your child {name} on {date} at {time}.Please bring the documents mentioned in the Application Confirmation Letter.\nRegards,\n{instituteName}");
			put(MAIL_TYPE_ADM_SELECTED, "Dear Parents,\nWe are pleased to inform you that your child {name} has been selected for LKG - academic year 2021-22. We request you to kindly login to receive a follow up message for further instructions on Fee Payment and Submissions of Documents.\nRegards,\n{instituteName}");
			put(MAIL_TYPE_ADM_REJECTED, "Dear Parents,\nWe regret to inform you that your child {name} has not been selected for LKG - academic year 2021-22.\nRegards,\n{instituteName}");
			
		}
	});

}
