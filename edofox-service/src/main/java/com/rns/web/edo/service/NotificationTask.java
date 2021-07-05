package com.rns.web.edo.service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import com.rns.web.edo.service.bo.impl.EdoAdminBoImpl;
import com.rns.web.edo.service.dao.EdoTestsDao;
import com.rns.web.edo.service.domain.EdoAdminRequest;
import com.rns.web.edo.service.domain.EdoTest;
import com.rns.web.edo.service.util.CommonUtils;
import com.rns.web.edo.service.util.EdoConstants;
import com.rns.web.edo.service.util.EdoNotificationsManager;
import com.rns.web.edo.service.util.EdoPropertyUtil;
import com.rns.web.edo.service.util.LoggingUtil;

@EnableScheduling
public class NotificationTask implements SchedulingConfigurer {

	// private static Logger logger = Logger.getLogger("scheduler");

	private SessionFactory sessionFactory;
	private ThreadPoolTaskExecutor executor;
	private EdoTestsDao testsDao;
	// private Integer autoResponseSequence = 0;
	// private Integer autoResponseSenderSequence = 0;

	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {

		// Add classwork notification task
		taskRegistrar.addTriggerTask(new Runnable() {
			public void run() {
				notificationsJob();
			}

		}, new Trigger() {
			public Date nextExecutionTime(TriggerContext arg0) {
				return nextNotificationsJob();
			}
		});

		// Add weekly report task
		taskRegistrar.addTriggerTask(new Runnable() {
			public void run() {
				weeklyReportJob();
			}

		}, new Trigger() {
			public Date nextExecutionTime(TriggerContext arg0) {
				return nextWeeklyReportJob();
			}

		});

		// Add weekly report task
		taskRegistrar.addTriggerTask(new Runnable() {
			public void run() {
				newContentJob();
			}


		}, new Trigger() {
			public Date nextExecutionTime(TriggerContext arg0) {
				return nextNewContentJob();
			}

		});

	}

	private void notificationsJob() {
		Session session = null;
		try {
			session = this.sessionFactory.openSession();
			String today = CommonUtils.convertDate(new Date(), "yyyy-MM-dd");
			LoggingUtil.logMessage("Finding exams for " + today, LoggingUtil.schedulerLogger);
			EdoNotificationsManager mgr = new EdoNotificationsManager();
			mgr.setNotificationType(EdoConstants.MAIL_TYPE_NEW_EXAM);
			mgr.setTestsDao(testsDao);
			mgr.setSessionFactory(sessionFactory);
			examNotifications(session, today, mgr);

		} catch (Exception e) {
			// logger.error(ExceptionUtils.getStackTrace(e));
		} finally {
			CommonUtils.closeSession(session);
		}
		LoggingUtil.logMessage("... End of notifications job ..", LoggingUtil.videoLogger);
	}

	private void examNotifications(Session session, String date, EdoNotificationsManager mgr) {
		try {
			List<EdoTest> exams = testsDao.getExamsForDate(date);
			if (CollectionUtils.isNotEmpty(exams)) {
				for (EdoTest exam : exams) {
					mgr.setExam(exam);
					mgr.broadcastNotification();
					LoggingUtil.logMessage("Sent notification for exam " + exam.getName() + " of ID " + exam.getId(), LoggingUtil.schedulerLogger);
				}
			}
		} catch (Exception e) {
			// logger.error(ExceptionUtils.getStackTrace(e));
		}
	}

	private Date nextNotificationsJob() {
		Date time = null;
		try {
			Calendar cal = Calendar.getInstance();
			String notificationsJob = EdoPropertyUtil.getProperty(EdoPropertyUtil.NOTIFICATIONS_JOB);
			if (StringUtils.isBlank(notificationsJob)) {
				return null;
			}
			// logger.info("-------- Notifications job variable:" +
			// notificationsJob);
			String times[] = StringUtils.split(notificationsJob, ":");
			if (ArrayUtils.isNotEmpty(times)) {
				Integer hour = new Integer(times[0]);
				Integer minute = new Integer(times[1]);
				if (hour > cal.get(Calendar.HOUR_OF_DAY) || (hour == cal.get(Calendar.HOUR_OF_DAY) && minute > cal.get(Calendar.MINUTE))) {
					cal.set(Calendar.SECOND, 0);
					cal.set(Calendar.HOUR_OF_DAY, hour);
					cal.set(Calendar.MINUTE, minute);
				} else {
					cal.add(Calendar.DATE, 1);
					cal.set(Calendar.SECOND, 0);
					cal.set(Calendar.HOUR_OF_DAY, hour);
					cal.set(Calendar.MINUTE, minute);
				}
				time = cal.getTime();
			}
			LoggingUtil.logMessage("-------- Next notification job Time:" + time, LoggingUtil.schedulerLogger);

		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
		}
		return time;
	}

	private Date nextWeeklyReportJob() {

		Date time = null;
		try {
			String weeklyJob = EdoPropertyUtil.getProperty(EdoPropertyUtil.WEEKLY_JOB);
			if (StringUtils.isBlank(weeklyJob)) {
				return null;
			}
			LoggingUtil.logMessage("-------- Weekly job variable found:" + weeklyJob, LoggingUtil.schedulerLogger);
			String times[] = StringUtils.split(weeklyJob, ":");

			// Weekly report job has to run only on Mondays
			// Find the next Monday
			Calendar cal = Calendar.getInstance();

			while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
				cal.add(Calendar.DATE, 1);
			}

			if (ArrayUtils.isNotEmpty(times)) {
				Integer hour = new Integer(times[0]);
				Integer minute = new Integer(times[1]);
				cal.set(Calendar.SECOND, 0);
				cal.set(Calendar.HOUR_OF_DAY, hour);
				cal.set(Calendar.MINUTE, minute);
				time = cal.getTime();
				if (time.getTime() < new Date().getTime()) {
					// Shift to next Monday
					time = DateUtils.addDays(time, 7);
				}
			}

			LoggingUtil.logMessage("-------- Next Weekly job Time:" + time, LoggingUtil.schedulerLogger);

		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e), LoggingUtil.schedulerLogger);
		}
		return time;
	}

	private void weeklyReportJob() {
		try {
			EdoAdminBoImpl adminBo = new EdoAdminBoImpl();
			adminBo.setSessionFactory(sessionFactory);
			adminBo.setExecutor(executor);
			adminBo.setMailExecutor(executor);
			adminBo.setTestsDao(testsDao);
			EdoAdminRequest request = new EdoAdminRequest();
			request.setFromDate(CommonUtils.convertDate(DateUtils.addWeeks(new Date(), -1)));
			request.setToDate(CommonUtils.convertDate(new Date()));
			request.setRequestType("EMAIL");
			LoggingUtil.logMessage("Running weekly report Job ... From " + request.getFromDate() + " To " + request.getToDate(), LoggingUtil.schedulerLogger);
			adminBo.getExamSummary(request);
			LoggingUtil.logMessage("Done with weekly report Job ... ", LoggingUtil.schedulerLogger);
		} catch (Exception e) {
			LoggingUtil.logMessage("Failed weekly report Job ... " + ExceptionUtils.getStackTrace(e), LoggingUtil.schedulerLogger);
		}
	}
	
	private Date nextNewContentJob() {
		Date time = null;
		try {
			Calendar cal = Calendar.getInstance();
			String notificationsJob = EdoPropertyUtil.getProperty(EdoPropertyUtil.NEW_CONTENT_JOB);
			if (StringUtils.isBlank(notificationsJob)) {
				return null;
			}
			// logger.info("-------- Notifications job variable:" +
			// notificationsJob);
			LoggingUtil.logMessage("-------- New content job variable:" + time, LoggingUtil.schedulerLogger);
			String times[] = StringUtils.split(notificationsJob, ":");
			if (ArrayUtils.isNotEmpty(times)) {
				Integer hour = new Integer(times[0]);
				Integer minute = new Integer(times[1]);
				if (hour > cal.get(Calendar.HOUR_OF_DAY) || (hour == cal.get(Calendar.HOUR_OF_DAY) && minute > cal.get(Calendar.MINUTE))) {
					cal.set(Calendar.SECOND, 0);
					cal.set(Calendar.HOUR_OF_DAY, hour);
					cal.set(Calendar.MINUTE, minute);
				} else {
					cal.add(Calendar.DATE, 1);
					cal.set(Calendar.SECOND, 0);
					cal.set(Calendar.HOUR_OF_DAY, hour);
					cal.set(Calendar.MINUTE, minute);
				}
				time = cal.getTime();
			}
			LoggingUtil.logMessage("-------- Next new content job Time:" + time, LoggingUtil.schedulerLogger);

		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e), LoggingUtil.schedulerLogger);
		}
		return time;
	}

	private void newContentJob() {
		try {
			EdoAdminBoImpl adminBo = new EdoAdminBoImpl();
			adminBo.setSessionFactory(sessionFactory);
			adminBo.setExecutor(executor);
			adminBo.setMailExecutor(executor);
			adminBo.setTestsDao(testsDao);
			EdoAdminRequest request = new EdoAdminRequest();
			request.setFromDate(CommonUtils.convertDate(DateUtils.addDays(new Date(), -1), "yyyy-MM-dd HH:mm"));
			request.setToDate(CommonUtils.convertDate(new Date() , "yyyy-MM-dd HH:mm"));
			LoggingUtil.logMessage("Running new content daily Job ... From Date " + request.getFromDate() + " To Date" + request.getToDate(), LoggingUtil.schedulerLogger);
			adminBo.notifyForContent(request);
			LoggingUtil.logMessage("Done with new content daily Job ... ", LoggingUtil.schedulerLogger);
		} catch (Exception e) {
			LoggingUtil.logError("New Content Job failed ....  " + ExceptionUtils.getStackTrace(e), LoggingUtil.schedulerLogger);
		}
	}
	
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public ThreadPoolTaskExecutor getExecutor() {
		return executor;
	}

	public void setExecutor(ThreadPoolTaskExecutor executor) {
		this.executor = executor;
	}

	public EdoTestsDao getTestsDao() {
		return testsDao;
	}

	public void setTestsDao(EdoTestsDao testsDao) {
		this.testsDao = testsDao;
	}

}