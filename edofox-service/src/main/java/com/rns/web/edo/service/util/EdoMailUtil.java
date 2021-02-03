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
import org.hibernate.criterion.Restrictions;

import com.rns.web.edo.service.domain.EDOInstitute;
import com.rns.web.edo.service.domain.EDOPackage;
import com.rns.web.edo.service.domain.EdoMailer;
import com.rns.web.edo.service.domain.EdoStudent;
import com.rns.web.edo.service.domain.jpa.EdoConfig;


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
	private EdoMailer mailer;
	
	
	
	public void setInstitute(EDOInstitute institute) {
		this.institute = institute;
	}

	public void setStudent(EdoStudent student) {
		this.student = student;
	}

	public EdoMailUtil(String mailType) {
		this.type = mailType;
	}
	
	public void setMailer(EdoMailer mailer) {
		this.mailer = mailer;
	}

	
	public String getMailId() {
		if(mailer == null || mailer.getMail() == null) {
			return MAIL_ID;
		}
		return mailer.getMail();
	}
	
	public String getMailHost() {
		if(mailer == null || mailer.getHost() == null) {
			return MAIL_HOST;
		}
		return mailer.getHost();
	}
	
	public String getMailPassword() {
		if(mailer == null || mailer.getPassword() == null) {
			return MAIL_PASSWORD;
		}
		return mailer.getPassword();
	}
	
	public String getMailFrom() {
		if(mailer == null || mailer.getSender() == null) {
			return "Edofox";
		}
		return mailer.getSender();
	}

	public void sendMail() {

		Session session = prepareMailSession();

		try {
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(getMailId(), getMailFrom()));
			prepareMailContent(message);
			Transport.send(message);
			LoggingUtil.logMessage("Sent email to .." + student.getEmail());
			
		} catch (MessagingException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	private Session prepareMailSession() {
		Properties props = new Properties();

		props.put("mail.smtp.auth", MAIL_AUTH);
		props.put("mail.smtp.socketFactory.port", "465"); //PROD
		props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory"); //PROD
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", getMailHost());
		props.put("mail.smtp.port", MAIL_PORT);
		/*props.put("mail.debug", "true");
		props.put("mail.smtp.user", getMailId());
		props.put("ssl-verify", "ignore");*/
		

		Session session = Session.getInstance(props, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(getMailId(), getMailPassword());
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
			
			//Format URLs
			if(mailer != null) {
				result = StringUtils.replace(result, "{actionUrl}", CommonUtils.getStringValue(mailer.getActionUrl()));
				result = StringUtils.replace(result, "{fileUrl}", CommonUtils.getStringValue(mailer.getFileUrl()));
				result = StringUtils.replace(result, "{supportMail}", CommonUtils.getStringValue(mailer.getSupportMail()));
			}
			
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
			put(MAIL_TYPE_INVITE, "student_invitation.html");
			put(MAIL_TYPE_APPOINTMENT, "student_admission.html");
		}
	});

	private static Map<String, String> MAIL_SUBJECTS = Collections.unmodifiableMap(new HashMap<String, String>() {
		{
			put(MAIL_TYPE_SUBSCRIPTION, "Thank you for registering to {instituteName}");
			put(MAIL_TYPE_ACTIVATED, "Your course for {instituteName} is now active");
			put(MAIL_TYPE_INVITE, "You have been invited by your institute {instituteName} on the online learning portal");
			put(MAIL_TYPE_APPOINTMENT, "St. Xavier's School - Schedule for LKG Document Verification Process");
		}
	});

	public static void main(String[] args) {
		EdoMailUtil edoMailUtil = new EdoMailUtil(MAIL_TYPE_SUBSCRIPTION);
		EdoStudent s = new EdoStudent();
		s.setEmail("ajinkyashiva@gmail.com");
		edoMailUtil.setStudent(s);;
		edoMailUtil.sendMail();
	}

	public static EdoMailer prepareMailer(org.hibernate.Session session, Integer id) {
		if(session != null && id != null) {
			String mailId = CommonUtils.getConfig("mail_id", session, id);
			if(StringUtils.isNotBlank(mailId)) {
				EdoMailer mailer = new EdoMailer();
				mailer.setMail(mailId);
				mailer.setHost(CommonUtils.getConfig("mail_host", session, id));
				mailer.setPassword(CommonUtils.getConfig("mail_password", session, id));
				mailer.setSender(CommonUtils.getConfig("mail_sender", session, id));
				mailer.setActionUrl(CommonUtils.getConfig("mail_redirect", session, id));
				return mailer;
			}
		}
		return null;
	}
}


