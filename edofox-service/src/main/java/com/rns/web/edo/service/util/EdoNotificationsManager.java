package com.rns.web.edo.service.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.AndroidNotification.Visibility;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.rns.web.edo.service.dao.EdoTestsDao;
import com.rns.web.edo.service.domain.EDOInstitute;
import com.rns.web.edo.service.domain.EdoFeedback;
import com.rns.web.edo.service.domain.EdoMailer;
import com.rns.web.edo.service.domain.EdoQuestion;
import com.rns.web.edo.service.domain.EdoServiceRequest;
import com.rns.web.edo.service.domain.EdoStudent;
import com.rns.web.edo.service.domain.EdoTest;
import com.rns.web.edo.service.domain.EdoVideoLectureMap;
import com.rns.web.edo.service.domain.ext.EdoGoogleNotification;
import com.rns.web.edo.service.domain.ext.EdoGoogleNotificationData;
import com.rns.web.edo.service.domain.ext.EdoGoogleNotificationRequest;
import com.rns.web.edo.service.domain.jpa.EdoVideoLecture;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;

public class EdoNotificationsManager implements Runnable, EdoConstants {

	private static final String DEFAULT_INTENT = "commonIntent";
	private static final String NOTIFICATION_ICON = "edofox_logo";
	private static final String CHANNEL_EDOFOX = "Edofox";
	private SessionFactory sessionFactory;
	private String notificationType;
	private EdoVideoLecture classwork;
	private EdoTestsDao testsDao;
	private EdoTest exam;
	private EdoFeedback feedback;
	private String medium;
	private ThreadPoolTaskExecutor mailExecutor;
	private EDOInstitute institute;
	private EdoMailer mailer;
	
	static {
		
		try {
			FileInputStream serviceAccount = new FileInputStream(EdoPropertyUtil.getProperty(EdoPropertyUtil.FIREBASE_CREDENTIALS));//"/home/service/properties/edofox-key.json"
			GoogleCredentials credentials;
			credentials = GoogleCredentials.fromStream(serviceAccount);
			FirebaseOptions options = 
					FirebaseOptions.builder()
					  .setCredentials(credentials)
					  .setProjectId(EdoPropertyUtil.getProperty(EdoPropertyUtil.FIREBASE_PROJECT))//"edofox-management-module"
					  .build();
			FirebaseApp.initializeApp(options);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		
		/*FirebaseOptions options = new FirebaseOptions.Builder()
			    .setCredentials(GoogleCredentials.getApplicationDefault())
			    .setDatabaseUrl("https://<DATABASE_NAME>.firebaseio.com/")
			    .build();*/

			
	}

	public void setClasswork(EdoVideoLecture classwork) {
		this.classwork = classwork;
	}


	public void setExam(EdoTest exam) {
		this.exam = exam;
	}
	
	public void setTestsDao(EdoTestsDao testsDao) {
		this.testsDao = testsDao;
	}

	public EdoNotificationsManager() {

	}

	public EdoNotificationsManager(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public void setNotificationType(String notificationType) {
		this.notificationType = notificationType;
	}

	public void setFeedback(EdoFeedback feedback) {
		this.feedback = feedback;
	}
	
	public void broadcastNotification() {

		Session session = null;
		try {
			session = this.sessionFactory.openSession();
			EdoGoogleNotificationRequest request = new EdoGoogleNotificationRequest();
			EdoVideoLecture classworkInfo = null;
			String bodyText = NOTIFICATION_BODY.get(notificationType);
			String titleText = NOTIFICATION_TITLE.get(notificationType);
			if(bodyText == null || titleText == null) {
				return;
			}
			List<EdoStudent> devices = null;
			List<EdoStudent> mailers = null;
			
			EdoMailUtil mailUtil = new EdoMailUtil(notificationType);
			EdoVideoLectureMap map = null;
			EdoQuestion feedbackData = null;
			
			if (classwork != null) {
				classworkInfo = classwork;
				if (classworkInfo.getVideoName() == null) {
					//classworkInfo = (EdoVideoLecture) session.createCriteria(EdoVideoLecture.class).add(Restrictions.eq("id", classwork.getId())).uniqueResult();
					List<EdoVideoLectureMap> maps = testsDao.getVideoLecture(classworkInfo.getId());
					if(CollectionUtils.isNotEmpty(maps)) {
						 map = maps.get(0);
					}
				}
				if (map != null) {
					classworkInfo = map.getLecture();
					/*List<EdoClassworkMap> maps = session.createCriteria(EdoClassworkMap.class).add(Restrictions.eq("classwork", classwork.getId())).list();
					if (CollectionUtils.isNotEmpty(maps)) {
						for (EdoClassworkMap clsMap : maps) {
							if (clsMap.getCourse() != null) {
								classes.append(clsMap.getCourse()).append(",");
							} else if (clsMap.getDivision() != null) {
								divisions.append(clsMap.getDivision()).append(",");
							}
						}
					}*/
					//Fetch relevant students
					if(StringUtils.isBlank(classworkInfo.getType())) {
						devices = testsDao.getStudentDevicesForPackage(classworkInfo.getClassroomId());
						mailers = testsDao.getStudentContactsForPackage(classworkInfo.getClassroomId());
					} else {
						devices = testsDao.getStudentDevicesForVideo(classworkInfo);
						mailers = testsDao.getStudentContactsForVideo(classworkInfo);
					}
					
					bodyText = CommonUtils.prepareClassworkNotification(bodyText, classworkInfo);
				}
			} else if (exam != null) {
				exam = testsDao.getTest(exam.getId());
				if(exam != null && exam.getStartDate() != null) {
					if (!DateUtils.isSameDay(exam.getStartDate(), new Date())) {
						return;
					}
					bodyText = CommonUtils.prepareTestNotification(bodyText, exam, null, "");
					titleText = CommonUtils.prepareTestNotification(titleText, exam, null, "");
					devices = testsDao.getStudentDevicesForPackage(exam.getPackageId());
					List<EdoStudent> devices2 = testsDao.getStudentDevicesForExam(exam);
					if (CollectionUtils.isNotEmpty(devices2)) {
						devices.addAll(devices2);
					}
					//Fetch mailing list
					mailers = testsDao.getStudentContactsForExam(exam);
					if(mailers == null) {
						mailers = new ArrayList<EdoStudent>();
					}
					List<EdoStudent> mailers2 = testsDao.getStudentContactsForPackage(exam.getPackageId());
					if(CollectionUtils.isNotEmpty(mailers2)) {
						mailers.addAll(mailers2);
					}
					mailUtil.setExam(exam);
					LoggingUtil.logMessage("Found " + mailers + " mailers ", LoggingUtil.emailLogger);
				}
			} else if (feedback != null) {
				EdoServiceRequest req = new EdoServiceRequest();
				req.setFeedback(feedback);
				feedbackData = testsDao.getFeedbackDetails(req);
				if(feedbackData != null && feedbackData.getFeedback() != null) {
					feedbackData.getFeedback().setVideoId(feedback.getVideoId());
					feedbackData.getFeedback().setQuestionId(feedback.getQuestionId());
					feedbackData.getFeedback().setId(feedback.getId());
					bodyText = CommonUtils.prepareFeedbackNotification(bodyText, feedbackData, null);
					devices = testsDao.getStudentDevicesForDoubt(feedback);
					mailers = testsDao.getStudentContactsForDoubt(feedback);
				}
			} else if (institute != null) {
				if(StringUtils.isBlank(institute.getName())) {
					institute = testsDao.getInstituteById(institute.getId());
				}
				if(institute != null) {
					mailers = testsDao.getAllStudents(institute.getId());
				}
			}
			
			if(CollectionUtils.isNotEmpty(devices)) {
				// Fetch students based on classwork maps
				List<String> registrationIds = prepareRegistrationIds(devices);
				request.setRegistration_ids(registrationIds);
				notify(request, bodyText, titleText);
				//send(titleText, bodyText, registrationIds);
			}
			
			//Send mails
			if(CollectionUtils.isNotEmpty(mailers)) {
				mailUtil.setStudents(mailers);
				mailUtil.setClasswork(map);
				mailUtil.setFeedbackData(feedbackData);
				mailUtil.setMailer(mailer);
				mailUtil.setInstitute(institute);
				mailExecutor.execute(mailUtil);
			}
			
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
		} finally {
			CommonUtils.closeSession(session);
		}

	}

	private void notify(EdoGoogleNotificationRequest request, String bodyText, String titleText) throws IOException, JsonGenerationException, JsonMappingException {
		EdoGoogleNotification notification = new EdoGoogleNotification();
		notification.setBody(bodyText);
		notification.setTitle(titleText);
		notification.setIcon(NOTIFICATION_ICON);
		notification.setVisibility("VISIBILITY_PUBLIC"); //Public
		notification.setNotification_priority("PRIORITY_HIGH");
		notification.setAndroid_channel_id("Edofox");
		notification.setClick_action(".MainActivity");
		request.setNotification(notification);
		
		EdoGoogleNotificationData data = new EdoGoogleNotificationData();
		data.setTitle(titleText);
		data.setText(bodyText);
		request.setData(data);
		postNotification(request);
	}

	/*
	 * private String setStatus(String bodyText) {
	 * if(StringUtils.equals(BillConstants.PAYMENT_STATUS_CREDIT,
	 * invoice.getStatus())) { bodyText = StringUtils.replace(bodyText,
	 * "{status}", "Successful"); } else { bodyText =
	 * StringUtils.replace(bodyText, "{status}", "Failed"); } return bodyText; }
	 */

	private void postNotification(EdoGoogleNotificationRequest request) throws IOException, JsonGenerationException, JsonMappingException {
		String url = EdoPropertyUtil.getProperty(EdoPropertyUtil.FCM_URL);
		ClientConfig config = new DefaultClientConfig();
		config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
		Client client = Client.create(config);
		
		
		WebResource webResource = client.resource(url);
		LoggingUtil.logMessage("Calling FCM URL :" + url + " request:" + new ObjectMapper().writeValueAsString(request), LoggingUtil.emailLogger);

		ClientResponse response = webResource.type("application/json")
				.header("Authorization", "key=" + EdoPropertyUtil.getProperty(EdoPropertyUtil.FCM_SERVER_KEY))
				//.header("Authorization ", "Bearer " + EdoFirebaseUtil.accessToken)
				.post(ClientResponse.class, request);

		if (response.getStatus() != 200) {
			LoggingUtil.logMessage("Failed in FCM URL : HTTP error code : " + response.getStatus(), LoggingUtil.emailLogger);
		}
		String output = response.getEntity(String.class);
		LoggingUtil.logMessage("Output from FCM URL : " + response.getStatus() + ".... \n " + output, LoggingUtil.emailLogger);
	}

	private List<String> prepareRegistrationIds(List<EdoStudent> devices) {
		if (CollectionUtils.isNotEmpty(devices)) {
			List<String> deviceIds = new ArrayList<String>();
			for (EdoStudent device : devices) {
				deviceIds.add(device.getToken());
			}
			return deviceIds;
		}
		return null;
	}
	
	public void send(String title, String body, List<String> tokens) {
		try {
			AndroidNotification notification = AndroidNotification.builder()
					.setTitle(title)
					.setBody(body)
					.setIcon(NOTIFICATION_ICON)
					.setChannelId(CHANNEL_EDOFOX)
					.setVisibility(Visibility.PUBLIC)
					.setPriority(com.google.firebase.messaging.AndroidNotification.Priority.HIGH)
					.build();
			Notification notificationMain = Notification.builder().setBody(body).setTitle(title).build();
			MulticastMessage message = MulticastMessage.builder()
					//.setNotification(notificationMain)
					//.setAndroidConfig(AndroidConfig.builder().setNotification(notification)
					//		.build())
					.putData("title", title)
					.addAllTokens(tokens)
					.build();
			FirebaseMessaging.getInstance().sendMulticast(message);
			System.out.println("Done!");
		} catch (FirebaseMessagingException e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			e.printStackTrace();
		}
	}

	public void run() {
		broadcastNotification();
	}

	public String getMedium() {
		return medium;
	}


	public void setMedium(String medium) {
		this.medium = medium;
	}

	public ThreadPoolTaskExecutor getMailExecutor() {
		return mailExecutor;
	}


	public void setMailExecutor(ThreadPoolTaskExecutor mailExecutor) {
		this.mailExecutor = mailExecutor;
	}

	public EDOInstitute getInstitute() {
		return institute;
	}


	public void setInstitute(EDOInstitute institute) {
		this.institute = institute;
	}

	public EdoMailer getMailer() {
		return mailer;
	}


	public void setMailer(EdoMailer mailer) {
		this.mailer = mailer;
	}

	private static Map<String, String> NOTIFICATION_BODY = Collections.unmodifiableMap(new HashMap<String, String>() {
		{
			// put(MAIL_TYPE_PAYMENT_RESULT, "Your Bill payment for {month}
			// {year} of Rs. {payable} to {businessName} is {status} \nPayment
			// ID: {paymentId} \nBill No: {invoiceId}\nGet exciting offers on
			// this bill now - {offersUrl}");
			put(MAIL_TYPE_NEW_CLASSWORK, "New video lecture {title} added for you");
			//put(MAIL_TYPE_GENERIC, "{message}");
			put(MAIL_TYPE_NEW_EXAM, "Exam will be available from {startDate} onwards");
			put(MAIL_TYPE_DOUBT_RESOLVED, "Your doubt {doubtFor} is resolved. Check your doubts section for more info.");
			put(MAIL_TYPE_INVITE, "Login using username {username}");
			
		}
	});

	private static Map<String, String> NOTIFICATION_TITLE = Collections.unmodifiableMap(new HashMap<String, String>() {
		{
			// put(MAIL_TYPE_PAYMENT_RESULT, "Your Bill payment for {month}
			// {year} of Rs. {payable} to {businessName} is {status} \nPayment
			// ID: {paymentId} \nBill No: {invoiceId}\nGet exciting offers on
			// this bill now - {offersUrl}");
			put(MAIL_TYPE_NEW_CLASSWORK, "New video added");
			//put(MAIL_TYPE_GENERIC, "{message}");
			put(MAIL_TYPE_NEW_EXAM, "Today's exam {testName}");
			put(MAIL_TYPE_DOUBT_RESOLVED, "Doubt resolved");
			put(MAIL_TYPE_INVITE, "Your Login credentials for Edofox");
		}
	});

}