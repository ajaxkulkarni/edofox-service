package com.rns.web.edo.service.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.rns.web.edo.service.dao.EdoTestsDao;
import com.rns.web.edo.service.domain.EDOInstitute;
import com.rns.web.edo.service.domain.EdoFeedback;
import com.rns.web.edo.service.domain.EdoMailer;
import com.rns.web.edo.service.domain.EdoQuestion;
import com.rns.web.edo.service.domain.EdoServiceRequest;
import com.rns.web.edo.service.domain.EdoStudent;
import com.rns.web.edo.service.domain.EdoTest;
import com.rns.web.edo.service.domain.EdoVideoLectureMap;
import com.rns.web.edo.service.domain.jpa.EdoVideoLecture;

public class EdoNotificationsManager implements Runnable, EdoConstants {

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
	private EdoStudent student;
	

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
			EdoVideoLecture classworkInfo = null;
			
			List<EdoStudent> devices = null;
			List<EdoStudent> mailers = null;
			
			EdoMailUtil mailUtil = new EdoMailUtil(notificationType);
			EdoFCMUtil fcmUtil = new EdoFCMUtil(devices, notificationType);
			EdoSMSUtil smsUtil = null;
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
					
					//bodyText = CommonUtils.prepareClassworkNotification(bodyText, classworkInfo);
					
					if(map != null && map.getLecture() != null) {
						mailUtil.setInstituteId(map.getLecture().getInstituteId());
						fcmUtil.setClassworkInfo(map.getLecture());
						fcmUtil.setInstituteId(map.getLecture().getInstituteId());
					}
				}
			} else if (exam != null) {
				exam = testsDao.getTest(exam.getId());
				if(exam != null && exam.getStartDate() != null) {
					if (!DateUtils.isSameDay(exam.getStartDate(), new Date())) {
						return;
					}
					if(exam.getCurrentQuestion() != null && exam.getCurrentQuestion().getInstituteId() != null) {
						exam.setInstituteId(exam.getCurrentQuestion().getInstituteId());
					}
					//bodyText = CommonUtils.prepareTestNotification(bodyText, exam, null, "");
					//titleText = CommonUtils.prepareTestNotification(titleText, exam, null, "");
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
					mailUtil.setInstituteId(exam.getInstituteId());
					
					smsUtil = new EdoSMSUtil(notificationType);
					smsUtil.setTest(exam);
					smsUtil.setInstituteId(exam.getInstituteId());
					
					fcmUtil.setExam(exam);
					fcmUtil.setInstituteId(exam.getInstituteId());
					
					//LoggingUtil.logMessage("Found " + mailers + " mailers ", LoggingUtil.emailLogger);
				}
			} else if (feedback != null) {
				
				//Check if its academic feedback or helpdesk ticket
				if(StringUtils.equals(notificationType, EdoConstants.MAIL_TYPE_TICKET_ACK) || StringUtils.equals(notificationType, EdoConstants.MAIL_TYPE_TICKET_REPLY)) {
					
				} else {
					EdoServiceRequest req = new EdoServiceRequest();
					req.setFeedback(feedback);
					feedbackData = testsDao.getFeedbackDetails(req);
					if(feedbackData != null && feedbackData.getFeedback() != null) {
						feedbackData.getFeedback().setVideoId(feedback.getVideoId());
						feedbackData.getFeedback().setQuestionId(feedback.getQuestionId());
						feedbackData.getFeedback().setId(feedback.getId());
						//bodyText = CommonUtils.prepareFeedbackNotification(bodyText, feedbackData, null);
						devices = testsDao.getStudentDevicesForDoubt(feedback);
						mailers = testsDao.getStudentContactsForDoubt(feedback);
						
						fcmUtil.setFeedbackData(feedbackData);
					}
				}
				
			} else if (institute != null) {
				if(StringUtils.isBlank(institute.getName())) {
					institute = testsDao.getInstituteById(institute.getId());
				}
				if(institute != null) {
					if(student == null || student.getCurrentPackage() == null || student.getCurrentPackage().getId() == null) {
						mailers = testsDao.getAllStudents(institute.getId());
					} else {
						student.setInstituteId(institute.getId().toString());
						mailers = testsDao.getAllPackageStudents(student);
					}
				}
				smsUtil = new EdoSMSUtil(notificationType);
			}
			
			
			//Send mails
			if(CollectionUtils.isNotEmpty(mailers)) {
				mailUtil.setStudents(mailers);
				mailUtil.setClasswork(map);
				mailUtil.setFeedbackData(feedbackData);
				mailUtil.setMailer(mailer);
				mailUtil.setInstitute(institute);
				mailUtil.setSessionFactory(sessionFactory);
				mailExecutor.execute(mailUtil);
				if(smsUtil != null) {
					smsUtil.setStudents(mailers);
					smsUtil.setInstitute(institute);
					smsUtil.setMailer(mailer);
					smsUtil.setSessionFactory(sessionFactory);
					mailExecutor.execute(smsUtil);
				}
			}
			
			if(CollectionUtils.isNotEmpty(devices)) {
				// Fetch students based on classwork maps
				fcmUtil.setDevices(devices);
				fcmUtil.setSessionFactory(sessionFactory);
				mailExecutor.execute(fcmUtil);
				//send(titleText, bodyText, registrationIds);
			}
			
			
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
		} finally {
			CommonUtils.closeSession(session);
		}

	}

	
	
	/*public void send(String title, String body, List<String> tokens) {
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
*/
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

	public void setStudent(EdoStudent student) {
		this.student = student;
	}


}