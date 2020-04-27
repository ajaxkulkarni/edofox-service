package com.rns.web.edo.service;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import com.clickntap.vimeo.VimeoException;
import com.clickntap.vimeo.VimeoResponse;
import com.rns.web.edo.service.domain.jpa.EdoLiveSession;
import com.rns.web.edo.service.util.CommonUtils;
import com.rns.web.edo.service.util.EdoConstants;
import com.rns.web.edo.service.util.EdoPropertyUtil;
import com.rns.web.edo.service.util.LoggingUtil;
import com.rns.web.edo.service.util.VideoUtil;

@EnableScheduling
public class VideoExportScheduler implements SchedulingConfigurer {

	private static Logger logger = Logger.getLogger("scheduler");

	private SessionFactory sessionFactory;
	private ThreadPoolTaskExecutor executor;
	// private Integer autoResponseSequence = 0;
	// private Integer autoResponseSenderSequence = 0;

	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {

		// Add session clean up task
		taskRegistrar.addTriggerTask(new Runnable() {
			public void run() {
				videoExportJob();
			}

		}, new Trigger() {
			public Date nextExecutionTime(TriggerContext arg0) {
				return nextExecutionSessionCleanup();
			}
		});

	}

	private void videoExportJob() {
		Session session = null;
		try {
			LoggingUtil.logMessage("... Start of video export job ..", LoggingUtil.videoLogger);
			session = this.sessionFactory.openSession();
			exportVideos(session);
			
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
		} finally {
			CommonUtils.closeSession(session);
		}
		LoggingUtil.logMessage("... End of video export job ..", LoggingUtil.videoLogger);
	}

	public void exportVideos(Session session) throws IOException, InterruptedException, VimeoException {
		String maxIdleTime = EdoPropertyUtil.getProperty(EdoPropertyUtil.VIDEO_IDLE_TIME);
		if (StringUtils.isBlank(maxIdleTime)) {
			maxIdleTime = "15";
		}
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, -new Integer(maxIdleTime));
		Date minTime = cal.getTime();
		LoggingUtil.logMessage("... Checking for date .." + minTime, LoggingUtil.videoLogger);
		List<EdoLiveSession> liveSessions = session.createCriteria(EdoLiveSession.class).add(Restrictions.le("lastUpdated", minTime))
				.add(Restrictions.eq("status", "Active")).list();
		if (CollectionUtils.isNotEmpty(liveSessions)) {
			LoggingUtil.logMessage("Exporting videos for " + liveSessions.size() + " lectures ..", LoggingUtil.videoLogger);
			int count = 0;
			for (EdoLiveSession live : liveSessions) {
				
				String folderLocation = EdoConstants.VIDEOS_PATH + live.getId();
				String outputFile = folderLocation + "/merged.webm";
				boolean result = VideoUtil.mergeFiles(folderLocation + "/", outputFile);
				Transaction tx = session.beginTransaction();
				if (result) {
					// Upload to Vimeo
					VimeoResponse vimeoResponse = VideoUtil.uploadFile(outputFile, live.getSessionName(), "");
					if (vimeoResponse != null && vimeoResponse.getJson() != null && StringUtils.isNotBlank(vimeoResponse.getJson().getString("link"))) {
						live.setStatus("Completed");
						live.setRecording_url(vimeoResponse.getJson().getString("link"));
					}
					count++;
					LoggingUtil.logMessage("Exported video " + live.getId() + " successfully ..", LoggingUtil.videoLogger);
				} else {
					live.setStatus("Failed");
					LoggingUtil.logMessage("Could not export video " + live.getId() + " successfully ..", LoggingUtil.videoLogger);
				}
				tx.commit();
			}
			LoggingUtil.logMessage("Exported videos for " + count + " lectures ..", LoggingUtil.videoLogger);
		} else {
			LoggingUtil.logMessage("No pending videos found for " + minTime, LoggingUtil.videoLogger);
		}
	}

	private Date nextExecutionSessionCleanup() {
		Date time = null;
		try {
			String frequency = EdoPropertyUtil.getProperty(EdoPropertyUtil.VIDEO_UPLOAD_FREQUENCY);
			if (StringUtils.isNotBlank(frequency)) {
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.MINUTE, new Integer(frequency));
				time = cal.getTime();
				LoggingUtil.logMessage("Next video export time:" + time, LoggingUtil.videoLogger);
			}

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

}
