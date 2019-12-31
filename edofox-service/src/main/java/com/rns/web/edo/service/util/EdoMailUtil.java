package com.rns.web.edo.service.util;


import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.rns.web.edo.service.domain.EDOInstitute;
import com.rns.web.edo.service.domain.EDOPackage;
import com.rns.web.edo.service.domain.EdoStudent;


public class EdoMailUtil implements Runnable, EdoConstants {

	
	//private static final String MAIL_HOST = "smtp.gmail.com";
	//private static final String MAIL_ID = "visionlaturpattern@gmail.com";
	//private static final String MAIL_PASSWORD = "Vision2018!";
	
	private static final String MAIL_AUTH = "true";
	//private static final String MAIL_HOST = "smtp.zoho.com";
	
	
	private static final String MAIL_HOST = EdoPropertyUtil.getProperty(EdoPropertyUtil.MAIL_HOST);
	private static final String MAIL_ID = EdoPropertyUtil.getProperty(EdoPropertyUtil.MAIL_ID);
	private static final String MAIL_PASSWORD = EdoPropertyUtil.getProperty(EdoPropertyUtil.MAIL_PASSWORD);
	private static final String MAIL_PORT = EdoPropertyUtil.getProperty(EdoPropertyUtil.MAIL_PORT);
	
	

	private String type;
	private String mailSubject;
	private EdoStudent student;
	private EDOInstitute institute;
	
	
	public void setInstitute(EDOInstitute institute) {
		this.institute = institute;
	}

	public void setStudent(EdoStudent student) {
		this.student = student;
	}

	public EdoMailUtil(String mailType) {
		this.type = mailType;
	}

	public EdoMailUtil() {
		
	}

	public void sendMail() {

		Session session = prepareMailSession();

		try {
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(MAIL_ID, "Edofox"));
			prepareMailContent(message);
			Transport.send(message);
			LoggingUtil.logMessage("Sent email to .." + student.getEmail());
			
		} catch (MessagingException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	private static Session prepareMailSession() {
		Properties props = new Properties();

		props.put("mail.smtp.auth", MAIL_AUTH);
		props.put("mail.smtp.socketFactory.port", "465"); //PROD
		props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory"); //PROD
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", MAIL_HOST);
		props.put("mail.smtp.port", MAIL_PORT);
		

		Session session = Session.getInstance(props, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(MAIL_ID, MAIL_PASSWORD);
			}
		});
		return session;
	}

	private String prepareMailContent(Message message) {

		try {
			//boolean attachCv = false;
			String result = readMailContent(message);
			result = CommonUtils.prepareStudentNotification(result, student);
			result = CommonUtils.prepareInstituteNotification(result, institute);
			
			//message.setContent(result, "text/html");
			message.setContent(result, "text/html; charset=utf-8");
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(student.getEmail()));
			
			//Format subject
			if(message.getSubject() != null) {
				if(institute != null) {
					message.setSubject(StringUtils.replace(message.getSubject(), "{instituteName}", institute.getName()));
				}
			}
			return result;

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}

		return "";
	}

	public void run() {
		sendMail();
	}

	private String readMailContent(Message message) throws FileNotFoundException, MessagingException {
		String contentPath = "";
		contentPath = "email/" + MAIL_TEMPLATES.get(type);
		message.setSubject(MAIL_SUBJECTS.get(type));
		return CommonUtils.readFile(contentPath);
	}

	
	public String getMailSubject() {
		return mailSubject;
	}

	public void setMailSubject(String mailSubject) {
		this.mailSubject = mailSubject;
	}

	private static Map<String, String> MAIL_TEMPLATES = Collections.unmodifiableMap(new HashMap<String, String>() {
		{
			put(MAIL_TYPE_SUBSCRIPTION, "subscription_mail.html");
			put(MAIL_TYPE_ACTIVATED, "package_active.html");
		}
	});

	private static Map<String, String> MAIL_SUBJECTS = Collections.unmodifiableMap(new HashMap<String, String>() {
		{
			put(MAIL_TYPE_SUBSCRIPTION, "Thank you for registering to {instituteName}");
			put(MAIL_TYPE_ACTIVATED, "Your course for {instituteName} is now active");
		}
	});

	public static void main(String[] args) {
		EdoMailUtil edoMailUtil = new EdoMailUtil(MAIL_TYPE_SUBSCRIPTION);
		EdoStudent s = new EdoStudent();
		s.setEmail("ajinkyashiva@gmail.com");
		edoMailUtil.setStudent(s);;
		edoMailUtil.sendMail();
	}
}


