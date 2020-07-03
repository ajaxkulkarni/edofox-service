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

import com.rns.web.edo.service.dao.EdoTestsDao;
import com.rns.web.edo.service.domain.jpa.EdoClasswork;
import com.rns.web.edo.service.domain.jpa.EdoNotice;
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
			Date fromDate = CommonUtils.getDateWithoutTime(new Date());
			Date toDate = CommonUtils.getTomorrowDate(fromDate);
			logger.info("Finding classwork between " + fromDate + " and " + toDate);
			EdoNotificationsManager mgr = new EdoNotificationsManager();
			mgr.setNotificationType(EdoConstants.MAIL_TYPE_NEW_CLASSWORK);
			mgr.setTestsDao(testsDao);
			mgr.setSessionFactory(sessionFactory);
			classworkNotifications(session, fromDate, toDate, mgr);
			mgr.setNotificationType(EdoConstants.MAIL_TYPE_NEW_NOTICE);
			noticeNotifications(session, fromDate, toDate, mgr);
			
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
		} finally {
			CommonUtils.closeSession(session);
		}
		LoggingUtil.logMessage("... End of video export job ..", LoggingUtil.videoLogger);
	}

	private void noticeNotifications(Session session, Date fromDate, Date toDate, EdoNotificationsManager mgr) {
		try {
			List<EdoNotice> classworkList = session.createCriteria(EdoNotice.class).add(Restrictions.lt("startDate", toDate))
					.add(Restrictions.ge("startDate", fromDate)).add(Restrictions.eq("status", "A"))
					.list();
			if (CollectionUtils.isNotEmpty(classworkList)) {
				for (EdoNotice notice : classworkList) {
					mgr.setNotice(notice);
					mgr.broadcastNotification();
					logger.info("Sent notification for notice " + notice.getTitle() + " of ID " + notice.getId());
				}
			}
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
		}
	}

	private void classworkNotifications(Session session, Date fromDate, Date toDate, EdoNotificationsManager mgr) {
		try {
			List<EdoClasswork> classworkList = session.createCriteria(EdoClasswork.class).add(Restrictions.lt("startDate", toDate))
					.add(Restrictions.ge("startDate", fromDate)).add(Restrictions.eq("disabled", 0)).add(Restrictions.eq("status", "Approved")).list();
			if (CollectionUtils.isNotEmpty(classworkList)) {
				for (EdoClasswork classwork : classworkList) {
					mgr.setClasswork(classwork);
					mgr.broadcastNotification();
					logger.info("Sent notification for classwork " + classwork.getTitle() + " of ID " + classwork.getId());
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
