package com.rns.web.edo.service.util;

import java.io.IOException;
import java.util.ArrayList;
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

import com.rns.web.edo.service.dao.EdoTestsDao;
import com.rns.web.edo.service.domain.EdoStudent;
import com.rns.web.edo.service.domain.ext.EdoGoogleNotification;
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

	private static final String DEFAULT_INTENT = "transactionsIntent";
	private static final String NOTIFICATION_ICON = "edofox_logo";
	private static final String CHANNEL_PAY_PER_BILL = "Edofox";
	private SessionFactory sessionFactory;
	private String notificationType;
	private EdoClasswork classwork;
	private EdoTestsDao testsDao;
	private EdoNotice notice;

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

			// Fetch students based on classwork maps
			Map<String, Object> input = new HashMap<String, Object>();
			input.put("classes", StringUtils.removeEnd(classes.toString(), ","));
			input.put("divisions", StringUtils.removeEnd(divisions.toString(), ","));
			List<EdoStudent> devices = testsDao.getStudentDevices(input);
			request.setRegistration_ids(prepareRegistrationIds(devices));
			notify(request, bodyText, titleText);
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
		// notification.setAndroid_channel_id(android_channel_id);
		request.setNotification(notification);
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
				.header("Authorization", "key=" + EdoPropertyUtil.getProperty(EdoPropertyUtil.FCM_SERVER_KEY)).post(ClientResponse.class, request);

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
