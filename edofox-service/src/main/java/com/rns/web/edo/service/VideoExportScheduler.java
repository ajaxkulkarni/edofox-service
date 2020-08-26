package com.rns.web.edo.service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
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
import com.rns.web.edo.service.dao.EdoTestsDao;
import com.rns.web.edo.service.domain.EDOInstitute;
import com.rns.web.edo.service.domain.EDOPackage;
import com.rns.web.edo.service.domain.jpa.EdoLiveSession;
import com.rns.web.edo.service.util.CommonUtils;
import com.rns.web.edo.service.util.EdoPropertyUtil;
import com.rns.web.edo.service.util.LoggingUtil;
import com.rns.web.edo.service.util.VideoUtil;

@EnableScheduling
public class VideoExportScheduler implements SchedulingConfigurer {

	private static Logger logger = Logger.getLogger("scheduler");
	
	private static String recordedFolderPath = "/usr/local/FlashphonerWebCallServer/records/";
	private static String outputFolder = "/var/www/html/recorded/";
	public static String FIXED_URL = "https://dev.edofox.com/recorded/";
	

	private SessionFactory sessionFactory;
	private ThreadPoolTaskExecutor executor;
	private EdoTestsDao testsDao;
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
				
				Float fileSize = VideoUtil.downloadRecordedFile(live.getClassroomId(), live.getId());
				Transaction tx = session.beginTransaction();
				if (fileSize != null && fileSize > 0) {
					// Upload to Vimeo
					//TODO removed as file format not supported by vimeo
					/*VimeoResponse vimeoResponse = VideoUtil.uploadFile(outputFile, live.getSessionName(), "");
					if (vimeoResponse != null && vimeoResponse.getJson() != null && StringUtils.isNotBlank(vimeoResponse.getJson().getString("link"))) {
						live.setStatus("Completed");
						live.setRecording_url(vimeoResponse.getJson().getString("link"));
					}
					count++;*/
					live.setFileSize(fileSize);
					live.setStatus("Completed");
					String urlStr = EdoPropertyUtil.getProperty(EdoPropertyUtil.RECORDED_URL) + URLEncoder.encode(live.getClassroomId() + "-" + live.getId() + ".mp4", "UTF-8");
					live.setRecording_url(urlStr);
					LoggingUtil.logMessage("Exported video " + live.getId() + " successfully ..", LoggingUtil.videoLogger);
				} else {
					live.setStatus("Failed");
					LoggingUtil.logMessage("Could not export video " + live.getId() + " successfully ..", LoggingUtil.videoLogger);
				}
				tx.commit();
				EDOInstitute institute = new EDOInstitute();
				EDOPackage pkg = testsDao.getPackage(live.getClassroomId());
				if(pkg != null && pkg.getInstitute() != null && live.getFileSize() != null) {
					institute.setId(pkg.getInstitute().getId());
					BigDecimal bd = CommonUtils.calculateStorageUsed(live.getFileSize());
					institute.setStorageQuota(bd.doubleValue());
					//Deduct quota from institute
					LoggingUtil.logMessage("Deducting quota " + institute.getStorageQuota() + " GBs from " + institute.getId(), LoggingUtil.videoLogger);
					testsDao.deductQuota(institute);
				}
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

	public EdoTestsDao getTestsDao() {
		return testsDao;
	}

	public void setTestsDao(EdoTestsDao testsDao) {
		this.testsDao = testsDao;
	}
	
	
	public static boolean fixFile(String sessionName) {
		try {
			
			LoggingUtil.logMessage("Fixing the file for " + sessionName);
			File recordedFile = new File(recordedFolderPath + ".mp4");
			if(recordedFile.exists() && recordedFile.length() > 0) {
				FileUtils.moveFileToDirectory(recordedFile, new File(outputFolder), false);
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			LoggingUtil.logError("Error in fixing recorde file " + sessionName + " -- " + ExceptionUtils.getStackTrace(e), LoggingUtil.videoLogger);
		}
		return false;
	}

}
