package com.rns.web.edo.service.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidConfig.Priority;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.AndroidNotification.Visibility;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.ApnsFcmOptions;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.FcmOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.WebpushConfig;
import com.google.firebase.messaging.WebpushNotification;
import com.rns.web.edo.service.dao.EdoTestsDao;
import com.rns.web.edo.service.domain.EdoStudent;
import com.rns.web.edo.service.domain.ext.EdoGoogleNotification;
import com.rns.web.edo.service.domain.ext.EdoGoogleNotificationData;
import com.rns.web.edo.service.domain.ext.EdoGoogleNotificationRequest;
import com.rns.web.edo.service.domain.jpa.EdoClasswork;
import com.rns.web.edo.service.domain.jpa.EdoClassworkMap;
import com.rns.web.edo.service.domain.jpa.EdoNotice;
import com.rns.web.edo.service.domain.jpa.EdoNoticeMap;
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
	private EdoClasswork classwork;
	private EdoTestsDao testsDao;
	private EdoNotice notice;
	
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

	public void setClasswork(EdoClasswork classwork) {
		this.classwork = classwork;
	}

	public void setNotice(EdoNotice notice) {
		this.notice = notice;
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

	public void broadcastNotification() {

		Session session = null;
		try {
			session = this.sessionFactory.openSession();
			EdoGoogleNotificationRequest request = new EdoGoogleNotificationRequest();
			StringBuilder classes = new StringBuilder();
			StringBuilder divisions = new StringBuilder();
			EdoClasswork classworkInfo = null;
			String bodyText = NOTIFICATION_BODY.get(notificationType);
			String titleText = NOTIFICATION_TITLE.get(notificationType);
			if(bodyText == null || titleText == null) {
				return;
			}
			
			if (classwork != null) {
				classworkInfo = classwork;
				if (classworkInfo.getTitle() == null) {
					classworkInfo = (EdoClasswork) session.createCriteria(EdoClasswork.class).add(Restrictions.eq("id", classwork.getId())).uniqueResult();
				}
				if (classworkInfo != null) {
					List<EdoClassworkMap> maps = session.createCriteria(EdoClassworkMap.class).add(Restrictions.eq("classwork", classwork.getId())).list();
					if (CollectionUtils.isNotEmpty(maps)) {
						for (EdoClassworkMap clsMap : maps) {
							if (clsMap.getCourse() != null) {
								classes.append(clsMap.getCourse()).append(",");
							} else if (clsMap.getDivision() != null) {
								divisions.append(clsMap.getDivision()).append(",");
							}
						}
					}
					bodyText = CommonUtils.prepareClassworkNotification(bodyText, classworkInfo);
				}
			} else if (notice != null) {
				notice = (EdoNotice) session.createCriteria(EdoNotice.class).add(Restrictions.eq("id", notice.getId())).uniqueResult();
				if (notice != null) {
					List<EdoNoticeMap> maps = session.createCriteria(EdoNoticeMap.class).add(Restrictions.eq("notice", notice.getId())).list();
					if (CollectionUtils.isNotEmpty(maps)) {
						for (EdoNoticeMap clsMap : maps) {
							if (clsMap.getCourse() != null) {
								classes.append(clsMap.getCourse()).append(",");
							} else if (clsMap.getDivision() != null) {
								divisions.append(clsMap.getDivision()).append(",");
							}
						}
					}
					bodyText = CommonUtils.prepareNoticeNotification(bodyText, notice);
					titleText = CommonUtils.prepareNoticeNotification(titleText, notice);
				}
			}
			
			if(StringUtils.isBlank(classes) && StringUtils.isBlank(divisions)) {
				return;
			}

			// Fetch students based on classwork maps
			Map<String, Object> input = new HashMap<String, Object>();
			input.put("classes", StringUtils.removeEnd(classes.toString(), ","));
			input.put("divisions", StringUtils.removeEnd(divisions.toString(), ","));
			List<EdoStudent> devices = testsDao.getStudentDevices(input);
			List<String> registrationIds = prepareRegistrationIds(devices);
			request.setRegistration_ids(registrationIds);
			notify(request, bodyText, titleText);
			//send(titleText, bodyText, registrationIds);
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

	private static Map<String, String> NOTIFICATION_BODY = Collections.unmodifiableMap(new HashMap<String, String>() {
		{
			// put(MAIL_TYPE_PAYMENT_RESULT, "Your Bill payment for {month}
			// {year} of Rs. {payable} to {businessName} is {status} \nPayment
			// ID: {paymentId} \nBill No: {invoiceId}\nGet exciting offers on
			// this bill now - {offersUrl}");
			put(MAIL_TYPE_NEW_CLASSWORK, "New classwork {title} added for you");
			put(MAIL_TYPE_GENERIC, "{message}");
			put(MAIL_TYPE_NEW_NOTICE, "{description}");
			
		}
	});

	private static Map<String, String> NOTIFICATION_TITLE = Collections.unmodifiableMap(new HashMap<String, String>() {
		{
			// put(MAIL_TYPE_PAYMENT_RESULT, "Your Bill payment for {month}
			// {year} of Rs. {payable} to {businessName} is {status} \nPayment
			// ID: {paymentId} \nBill No: {invoiceId}\nGet exciting offers on
			// this bill now - {offersUrl}");
			put(MAIL_TYPE_NEW_CLASSWORK, "New classwork added");
			put(MAIL_TYPE_GENERIC, "{message}");
			put(MAIL_TYPE_NEW_NOTICE, "{title}");
		}
	});

}
