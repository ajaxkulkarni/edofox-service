package com.rns.web.edo.service.util;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.rns.web.edo.service.domain.EDOInstitute;
import com.rns.web.edo.service.domain.EdoMailer;
import com.rns.web.edo.service.domain.EdoQuestion;
import com.rns.web.edo.service.domain.EdoStudent;
import com.rns.web.edo.service.domain.EdoTest;
import com.rns.web.edo.service.domain.EdoVideoLectureMap;

public class EdoMailUtil implements Runnable, EdoConstants {

	// private static final String MAIL_HOST = "smtp.gmail.com";
	// private static final String MAIL_ID = "visionlaturpattern@gmail.com";
	// private static final String MAIL_PASSWORD = "Vision2018!";

	private static final String MAIL_AUTH = "true";
	// private static final String MAIL_HOST = "smtp.zoho.com";

	private static final String MAIL_HOST = EdoPropertyUtil.getProperty(EdoPropertyUtil.MAIL_HOST);
	private static final String MAIL_ID = EdoPropertyUtil.getProperty(EdoPropertyUtil.MAIL_ID);
	private static final String MAIL_PASSWORD = EdoPropertyUtil.getProperty(EdoPropertyUtil.MAIL_PASSWORD);
	private static final String MAIL_PORT = EdoPropertyUtil.getProperty(EdoPropertyUtil.MAIL_PORT);

	private String type;
	private String mailSubject;
	private EdoStudent student;
	private EDOInstitute institute;
	private EdoTest exam;
	private List<EdoStudent> students;
	private EdoVideoLectureMap classwork;
	private EdoQuestion feedbackData;
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

	public EdoMailUtil() {

	}

	public void sendMail() {

		Session session = prepareMailSession();

		try {
			Message message = new MimeMessage(session);
			//if (institute != null) {
			//	message.setFrom(new InternetAddress(MAIL_ID, institute.getName()));
			//} else {
			message.setFrom(new InternetAddress(MAIL_ID, "Edofox"));
			//}
			if (CollectionUtils.isNotEmpty(students)) {
				for (EdoStudent stu : students) {
					student = stu;
					sendMail(message);
				}
			} else {
				sendMail(message);
			}

		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e), LoggingUtil.emailLogger);
		}
	}

	private boolean isValid(String email) {
		String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\." + "[a-zA-Z0-9_+&*-]+)*@" + "(?:[a-zA-Z0-9-]+\\.)+[a-z" + "A-Z]{2,7}$";

		Pattern pat = Pattern.compile(emailRegex);
		if (StringUtils.isBlank(email)) {
			return false;
		}
		return pat.matcher(email).matches();
	}

	private void sendMail(Message message) throws MessagingException {
		if (student == null || StringUtils.isBlank(student.getEmail())) {
			return;
		}
		if(!isValid(student.getEmail())) {
			return;
		}
		prepareMailContent(message);
		Transport.send(message);
		LoggingUtil.logMessage("Sent email " + type + " to .." + student.getEmail(), LoggingUtil.emailLogger);
	}

	private static Session prepareMailSession() {
		Properties props = new Properties();

		props.put("mail.smtp.auth", MAIL_AUTH);
		props.put("mail.smtp.socketFactory.port", "465"); // PROD
		props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory"); // PROD
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
			// boolean attachCv = false;
			String result = readMailContent(message);
			
			if(mailer != null) {
				result = StringUtils.replace(result, "{additionalMessage}", CommonUtils.getStringValue(mailer.getAdditionalMessage()));
			} else {
				result = StringUtils.replace(result, "{additionalMessage}", "");
			}
			
			result = CommonUtils.prepareStudentNotification(result, student);
			result = CommonUtils.prepareInstituteNotification(result, institute);
			result = CommonUtils.prepareTestNotification(result, exam, institute, "");
			result = CommonUtils.prepareClassworkNotification(result, classwork);
			result = CommonUtils.prepareFeedbackNotification(result, feedbackData, student);
			
			//Set action URL depending on notification type
			result = StringUtils.replace(result, "{actionUrl}", "test.edofox.com");
			
			// message.setContent(result, "text/html");
			message.setContent(result, "text/html; charset=utf-8");
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(student.getEmail()));

			// Format subject
			if (message.getSubject() != null) {
				String subjectText = CommonUtils.prepareInstituteNotification(message.getSubject(), institute);
				subjectText = CommonUtils.prepareTestNotification(subjectText, exam, institute, "");
				subjectText = CommonUtils.prepareClassworkNotification(subjectText, classwork);
				message.setSubject(subjectText);
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
			put(MAIL_TYPE_INVITE, "student_invite.html");
			put(MAIL_TYPE_NEW_EXAM, "exam_notification.html");
			put(MAIL_TYPE_DOUBT_RESOLVED, "doubt_notification.html");
			put(MAIL_TYPE_NEW_CLASSWORK, "classwork_notification.html");
		}
	});

	private static Map<String, String> MAIL_SUBJECTS = Collections.unmodifiableMap(new HashMap<String, String>() {
		{
			put(MAIL_TYPE_SUBSCRIPTION, "Thank you for registering to {instituteName}");
			put(MAIL_TYPE_ACTIVATED, "Your course for {instituteName} is now active");
			put(MAIL_TYPE_INVITE, "{instituteName} invites you to Edofox");
			put(MAIL_TYPE_NEW_EXAM, "Today's exam {testName}");
			put(MAIL_TYPE_NEW_CLASSWORK, "New {contentType} {title} added for you");
			put(MAIL_TYPE_DOUBT_RESOLVED, "Doubt resolved by teacher");
		}
	});

	public static void main(String[] args) {
		EdoMailUtil edoMailUtil = new EdoMailUtil(MAIL_TYPE_SUBSCRIPTION);
		EdoStudent s = new EdoStudent();
		s.setEmail("ajinkyashiva@gmail.com");
		edoMailUtil.setStudent(s);
		;
		edoMailUtil.sendMail();
	}

	public EdoTest getExam() {
		return exam;
	}

	public void setExam(EdoTest exam) {
		this.exam = exam;
	}

	public List<EdoStudent> getStudents() {
		return students;
	}

	public void setStudents(List<EdoStudent> students) {
		this.students = students;
	}

	public void setClasswork(EdoVideoLectureMap map) {
		this.classwork = map;
	}

	public void setFeedbackData(EdoQuestion feedbackData) {
		this.feedbackData = feedbackData;
	}

	public void setMailer(EdoMailer mailer) {
		this.mailer = mailer;
	}
}
