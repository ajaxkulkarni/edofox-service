package com.rns.web.edo.service.util;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
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
	//public static String SMS_URL = "http://weberleads.in/http-tokenkeyapi.php?authentic-key={auth}&senderid=MHEXAM&route=2&number={phoneNo}&message={message}";
	
	private EdoStudent student;
	private String type;
	private EdoTest test;
	private EDOInstitute institute;
	private String additionalMessage;
	private boolean copyParent;
	private boolean copyAdmin;
	private List<EdoStudent> students;

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
		if(CollectionUtils.isNotEmpty(students)) {
			for(EdoStudent stu: students) {
				student = stu;
				sendSMS();
			}
		} else {
			sendSMS();
		}
		
	}

	public void sendSMS() {

		if (student == null) {
			return;
		}
		
		if(!validatePhoneNumber(student)) {
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
			
			if(mailer != null) {
				message = StringUtils.replace(message, "{additionalMessage}", CommonUtils.getStringValue(mailer.getAdditionalMessage()));
			} else {
				message = StringUtils.replace(message, "{additionalMessage}", "");
			}

			if (test != null) {
				message = CommonUtils.prepareTestNotification(message, test, institute, additionalMessage);
			} else if (institute != null) {
				message = CommonUtils.prepareInstituteNotification(message, institute);
			}
			if(mailer != null) {
				
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

	private static boolean validatePhoneNumber(EdoStudent currStudent) {
		if(currStudent == null || StringUtils.isBlank(currStudent.getPhone())) {
			return false;
		}
		try {
			Pattern p = Pattern.compile("(0/91)?[7-9][0-9]{9}"); 
		    // Pattern class contains matcher() method 
		    // to find matching between given number  
		    // and regular expression 
		    Matcher m = p.matcher(currStudent.getPhone()); 
		    return (m.find() && m.group().equals(currStudent.getPhone())); 
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
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
			LoggingUtil.logError("SMS sending Failed : HTTP error code : " + response.getStatus());
		}
		LoggingUtil.logMessage("Output from SMS .... " + response.getStatus() + " \n");
	}

	public static void main(String[] args) {
		EdoSMSUtil mail = new EdoSMSUtil(MAIL_TYPE_TEST_RESULT);
		EdoStudent s = new EdoStudent();
		s.setPhone("4423040642");
		s.setName("Ajinkya C Kulkarni");
		EdoTest t = new EdoTest();
		t.setSolvedCount(11);
		t.setCorrectCount(11);
		t.setTotalMarks(1800);
		t.setName("JEE 2018");
		t.setScore(new BigDecimal(123));
		mail.setTest(t);
		mail.setStudent(s);
		//mail.sendSMS();
		
		System.out.println(validatePhoneNumber(s));
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

	public List<EdoStudent> getStudents() {
		return students;
	}

	public void setStudents(List<EdoStudent> students) {
		this.students = students;
	}

	private static Map<String, String> SMS_TEMPLATES = Collections.unmodifiableMap(new HashMap<String, String>() {
		{
			put(MAIL_TYPE_SUBSCRIPTION,
					"Hi {name}, Thank you for registering to {instituteName}. Login to {webUrl} using the username and password you provided to have access to its features. "
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
			put(MAIL_TYPE_INVITE, "Hello {name}, Your Edofox login credentials for {instituteName} exam are - username - {username} and password - {password}. Please use this credentials to login to {webUrl}. Use latest Google chrome on laptop for best exam experience. You can also download our Android app {appUrl} "
					+ "{additionalMessage}");
			put(MAIL_TYPE_NEW_EXAM, "Today's exam {testName} will start on {startDate}");
			
			
		}
	});

	public void setMailer(EdoMailer mailer) {
		this.mailer = mailer;
	}

}
