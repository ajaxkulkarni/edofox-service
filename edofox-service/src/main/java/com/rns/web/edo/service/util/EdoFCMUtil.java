package com.rns.web.edo.service.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.rns.web.edo.service.domain.EdoQuestion;
import com.rns.web.edo.service.domain.EdoStudent;
import com.rns.web.edo.service.domain.EdoTest;
import com.rns.web.edo.service.domain.ext.EdoGoogleNotification;
import com.rns.web.edo.service.domain.ext.EdoGoogleNotificationData;
import com.rns.web.edo.service.domain.ext.EdoGoogleNotificationRequest;
import com.rns.web.edo.service.domain.jpa.EdoEmailSmsSummary;
import com.rns.web.edo.service.domain.jpa.EdoVideoLecture;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;

public class EdoFCMUtil implements Runnable, EdoConstants {
	
	private static final String DEFAULT_INTENT = "commonIntent";
	private static final String NOTIFICATION_ICON = "edofox_logo";
	private static final String CHANNEL_EDOFOX = "Edofox";
	
	private List<EdoStudent> devices;
	private String notificationType;
	private SessionFactory sessionFactory;
	private Integer instituteId;
	private EdoVideoLecture classworkInfo;
	private EdoTest exam;
	private EdoQuestion feedbackData;

	public EdoFCMUtil(List<EdoStudent> devices, String notificationType) {
		setDevices(devices);
		this.notificationType = notificationType;
	}
	
	public void setDevices(List<EdoStudent> devices) {
		this.devices = devices;
	}
	
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	public EdoTest getExam() {
		return exam;
	}
	
	public void setExam(EdoTest exam) {
		this.exam = exam;
	}
	
	public void setClassworkInfo(EdoVideoLecture classworkInfo) {
		this.classworkInfo = classworkInfo;
	}
	
	public void setFeedbackData(EdoQuestion feedbackData) {
		this.feedbackData = feedbackData;
	}
	
	
/*static {
		
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

			
	}*/
	
	public void run() {
		Session session = null;
		try {
			EdoGoogleNotificationRequest request = new EdoGoogleNotificationRequest();
			String bodyText = NOTIFICATION_BODY.get(notificationType);
			String titleText = NOTIFICATION_TITLE.get(notificationType);
			if(bodyText == null || titleText == null) {
				return;
			}
			
			if(classworkInfo != null) {
				bodyText = CommonUtils.prepareClassworkNotification(bodyText, classworkInfo);
			}
			
			if(exam != null) {
				bodyText = CommonUtils.prepareTestNotification(bodyText, exam, null, "");
				titleText = CommonUtils.prepareTestNotification(titleText, exam, null, "");
			}
			
			if(feedbackData != null) {
				bodyText = CommonUtils.prepareFeedbackNotification(bodyText, feedbackData, null);
			}
			
			List<String> registrationIds = prepareRegistrationIds(devices);
			//More than 1000 IDs are not allowed
			if(CollectionUtils.isNotEmpty(registrationIds)) {
				session = this.sessionFactory.openSession();
				Transaction tx = session.beginTransaction();
				if(registrationIds.size() < 1000) {
					request.setRegistration_ids(registrationIds);
					notify(request, bodyText, titleText);
				} else {
					int i = 0;
					while(i < registrationIds.size()) {
						request.setRegistration_ids(registrationIds.subList(i, i + 999));
						notify(request, bodyText, titleText);
						i = i + 1000;
					}
				}
				EdoEmailSmsSummary summary = new EdoEmailSmsSummary();
				summary.setChannel("fcm");
				summary.setCreatedDate(new Date());
				summary.setNoOfStudents(devices.size());
				summary.setInstituteId(instituteId);
				if(exam != null) {
					summary.setExamId(exam.getId());
				}
				if(classworkInfo != null) {
					summary.setClassworkId(classworkInfo.getId());
				}
				summary.setNotificationType(notificationType);
				session.persist(summary);
				
				tx.commit();
			}
			
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e), LoggingUtil.emailLogger);
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

	public void setInstituteId(Integer instituteId) {
		this.instituteId = instituteId;
	}

}
