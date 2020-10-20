package com.rns.web.edo.service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import com.google.api.Logging;
import com.rns.web.edo.service.dao.EdoTestsDao;
import com.rns.web.edo.service.domain.EdoTest;
import com.rns.web.edo.service.util.CommonUtils;
import com.rns.web.edo.service.util.EdoConstants;
import com.rns.web.edo.service.util.EdoNotificationsManager;
import com.rns.web.edo.service.util.EdoPropertyUtil;
import com.rns.web.edo.service.util.LoggingUtil;

@EnableScheduling
public class NotificationTask implements SchedulingConfigurer {

	private static Logger logger = Logger.getLogger("scheduler");

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


	}

	private void notificationsJob() {
		Session session = null;
		try {
			session = this.sessionFactory.openSession();
			String today = CommonUtils.convertDate(new Date(), "yyyy-MM-dd");
			logger.info("Finding exams for " + today);
			EdoNotificationsManager mgr = new EdoNotificationsManager();
			mgr.setNotificationType(EdoConstants.MAIL_TYPE_NEW_EXAM);
			mgr.setTestsDao(testsDao);
			mgr.setSessionFactory(sessionFactory);
			examNotifications(session, today, mgr);
			
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
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
					logger.info("Sent notification for exam " + exam.getName() + " of ID " + exam.getId());
				}
			}
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
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
			logger.info("-------- Notifications job variable:" + notificationsJob);
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
			logger.info("-------- Next notification job Time:" + time);

		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
		}
		return time;
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