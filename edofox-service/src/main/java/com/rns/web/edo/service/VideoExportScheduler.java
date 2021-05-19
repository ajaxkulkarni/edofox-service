package com.rns.web.edo.service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.Calendar;import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jettison.json.JSONObject;
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
import com.rns.web.edo.service.domain.EdoApiStatus;
import com.rns.web.edo.service.domain.EdoFeedback;
import com.rns.web.edo.service.domain.EdoServiceRequest;
import com.rns.web.edo.service.domain.EdoServiceResponse;
import com.rns.web.edo.service.domain.EdoStudent;
import com.rns.web.edo.service.domain.ext.EdoImpartusResponse;
import com.rns.web.edo.service.domain.jpa.EdoLiveSession;
import com.rns.web.edo.service.domain.jpa.EdoLiveToken;
import com.rns.web.edo.service.domain.jpa.EdoVideoLecture;
import com.rns.web.edo.service.util.CommonUtils;
import com.rns.web.edo.service.util.EdoLiveUtil;
import com.rns.web.edo.service.util.EdoPropertyUtil;
import com.rns.web.edo.service.util.LoggingUtil;
import com.rns.web.edo.service.util.VideoUtil;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;

@EnableScheduling
public class VideoExportScheduler implements SchedulingConfigurer {

	
	private static String recordedFolderPath = "/usr/local/FlashphonerWebCallServer/records/";
	private static String outputFolder = "/var/www/html/recorded/";
	public static String FIXED_URL = "https://dev.edofox.com/recorded/";
	private static String FIX_URL = "https://dev.edofox.com:8443/edofox/admin/fixRecordedFile";
	

	private SessionFactory sessionFactory;
	private ThreadPoolTaskExecutor executor;
	private EdoTestsDao testsDao;
	// private Integer autoResponseSequence = 0;
	// private Integer autoResponseSenderSequence = 0;

	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {

		// Add session clean up task
		taskRegistrar.addTriggerTask(new Runnable() {
			public void run() {
				//videoExportJob();
				analysisExportJob();
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
			LoggingUtil.logMessage("... Start of video export job ..", LoggingUtil.schedulerLogger);
			session = this.sessionFactory.openSession();
			exportVideos(session);
			
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e), LoggingUtil.videoLogger);
		} finally {
			CommonUtils.closeSession(session);
		}
		LoggingUtil.logMessage("... End of video export job ..", LoggingUtil.schedulerLogger);
	}
	

	private void analysisExportJob() {
		Session session = null;
		try {
			LoggingUtil.logMessage("... Start of analysis export job ..", LoggingUtil.schedulerLogger);
			session = this.sessionFactory.openSession();
			List<EdoLiveSession> liveSessions = session.createCriteria(EdoLiveSession.class)
					.add(Restrictions.le("endDate", new Date()))
					.add(Restrictions.eq("status", "Active")).list();
			if (CollectionUtils.isNotEmpty(liveSessions)) {
				LoggingUtil.logMessage("... Found " + liveSessions.size() + " sessions ..", LoggingUtil.schedulerLogger);
				
				//Call Impartus API to generate token
				List<EdoLiveToken> tokens = session.createCriteria(EdoLiveToken.class)
						.addOrder(org.hibernate.criterion.Order.desc("id"))
						.add(Restrictions.ge("lastUpdated", DateUtils.addHours(new Date(), -2)))
						.setMaxResults(1).list();
				String tokenString = null;
				if(CollectionUtils.isEmpty(tokens)) {
					EdoImpartusResponse tokenResponse = EdoLiveUtil.adminLogin();
					if(tokenResponse == null) {
						LoggingUtil.logMessage("... Could not get token ..", LoggingUtil.schedulerLogger);
						return;
					}
					EdoLiveToken token = new EdoLiveToken();
					token.setLastUpdated(new Date());
					token.setToken(tokenResponse.getToken());
					tokenString = tokenResponse.getToken();
					session.persist(token);
				} else {
					tokenString = tokens.get(0).getToken();
				}
				
				Transaction tx = session.beginTransaction();
				for(EdoLiveSession liveSession: liveSessions) {
					if(liveSession.getScheduleId() != null) {
						List<LinkedHashMap<String, Object>> resp = EdoLiveUtil.getUsage(new Integer(liveSession.getScheduleId()), tokenString);
						if(CollectionUtils.isNotEmpty(resp)) {
							for(LinkedHashMap<String, Object> student: resp) {
								EdoServiceRequest request = new EdoServiceRequest();
								EdoFeedback feedback = new EdoFeedback();
								feedback.setActivityCount(1);
								feedback.setFrequency(1);
								Float durationViewed = new Float(student.get("duration").toString());
								feedback.setDurationViewed(durationViewed);
								feedback.setTotalDuration(durationViewed.longValue());
								feedback.setCreatedDateString(student.get("date").toString());
								feedback.setId(liveSession.getId());
								EdoStudent stu = new EdoStudent();
								stu.setId(new Integer(student.get("externalId").toString()));
								request.setStudent(stu);
								request.setRequestType("LIVE_JOINED");
								//feedback.setTotalDuration(durationViewed);
								request.setFeedback(feedback);
								testsDao.saveVideoActiviy(request);
								//Update summary
								Integer activityCount = 1;
								Integer watchedTimes = 0;
								if(StringUtils.equals(request.getRequestType(), "VIDEO_ENDED")) {
									watchedTimes = 1;
								}
								Long watchDuration = durationViewed.longValue();
								List<EdoFeedback> activiy = testsDao.getStudentActivity(request);
								if(CollectionUtils.isNotEmpty(activiy)) {
									EdoFeedback existing = activiy.get(0);
									if(existing.getActivityCount() != null) {
										activityCount = existing.getActivityCount() + 1;
									}
									if(existing.getFrequency() != null) {
										watchedTimes = existing.getFrequency() + watchedTimes;
									}
									if(existing.getTotalDuration() != null && durationViewed != null) {
										watchDuration = existing.getTotalDuration() + durationViewed.longValue();
									}
									if(StringUtils.equals(existing.getType(), "VIDEO_ENDED")) {
										request.setRequestType(existing.getType());
									}
									feedback.setActivityCount(activityCount);
									feedback.setTotalDuration(watchDuration);
									feedback.setFrequency(watchedTimes);
									testsDao.updateActivitySummary(request);
								} else {
									feedback.setActivityCount(activityCount);
									feedback.setTotalDuration(watchDuration);
									feedback.setFrequency(watchedTimes);
									testsDao.saveActivitySummary(request);
								}
								
							}
						}
					}
					liveSession.setStatus("Completed");
				}
				tx.commit();
			}
			
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e), LoggingUtil.schedulerLogger);
		} finally {
			CommonUtils.closeSession(session);
		}
		LoggingUtil.logMessage("... End of analysis export job ..", LoggingUtil.schedulerLogger);
		
	}

	public void exportVideos(Session session) throws IOException, InterruptedException, VimeoException {
		String maxIdleTime = EdoPropertyUtil.getProperty(EdoPropertyUtil.VIDEO_IDLE_TIME);
		if (StringUtils.isBlank(maxIdleTime)) {
			maxIdleTime = "15";
		}
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, -new Integer(maxIdleTime));
		Date minTime = cal.getTime();
		LoggingUtil.logMessage("... Checking for date .." + minTime, LoggingUtil.schedulerLogger);
		List<EdoLiveSession> liveSessions = session.createCriteria(EdoLiveSession.class).add(Restrictions.le("lastUpdated", minTime))
				.add(Restrictions.eq("status", "Active")).list();
		if (CollectionUtils.isNotEmpty(liveSessions)) {
			LoggingUtil.logMessage("Exporting videos for " + liveSessions.size() + " lectures ..", LoggingUtil.schedulerLogger);
			int count = 0;
			for (EdoLiveSession live : liveSessions) {
				
				//Float fileSize = VideoUtil.downloadRecordedFile(live.getClassroomId(), live.getId());
				Transaction tx = session.beginTransaction();
				/*if (fileSize != null && fileSize > 0) {
					// Upload to Vimeo
					//TODO removed as file format not supported by vimeo
					VimeoResponse vimeoResponse = VideoUtil.uploadFile(outputFile, live.getSessionName(), "");
					if (vimeoResponse != null && vimeoResponse.getJson() != null && StringUtils.isNotBlank(vimeoResponse.getJson().getString("link"))) {
						live.setStatus("Completed");
						live.setRecording_url(vimeoResponse.getJson().getString("link"));
					}
					count++;
					live.setFileSize(fileSize);
					live.setStatus("Completed");
					String urlStr = EdoPropertyUtil.getProperty(EdoPropertyUtil.RECORDED_URL) + URLEncoder.encode(live.getClassroomId() + "-" + live.getId() + ".mp4", "UTF-8");
					live.setRecording_url(urlStr);
					LoggingUtil.logMessage("Exported video " + live.getId() + " successfully ..", LoggingUtil.schedulerLogger);
				} else {*/
					//Try fixing the file
					LoggingUtil.logMessage("Calling the fix lecture API for " + live.getSessionName(), LoggingUtil.schedulerLogger);
					EdoVideoLecture lecture = callFixFileApi(live.getClassroomId() + "-" + live.getId());
					if(lecture != null && lecture.getSize() != null) {
						live.setStatus("Completed");
						live.setRecording_url(lecture.getVideo_url());
						live.setFileSize(lecture.getSize().floatValue());
					} else {
						live.setStatus("Failed");
						LoggingUtil.logMessage("Could not export video " + live.getId() + " successfully ..", LoggingUtil.schedulerLogger);
					}
				//}
				tx.commit();
				EDOInstitute institute = new EDOInstitute();
				EDOPackage pkg = testsDao.getPackage(live.getClassroomId());
				if(pkg != null && pkg.getInstitute() != null && live.getFileSize() != null) {
					institute.setId(pkg.getInstitute().getId());
					BigDecimal bd = CommonUtils.calculateStorageUsed(live.getFileSize());
					institute.setStorageQuota(bd.doubleValue());
					//Deduct quota from institute
					LoggingUtil.logMessage("Deducting quota " + institute.getStorageQuota() + " GBs from " + institute.getId(), LoggingUtil.schedulerLogger);
					testsDao.deductQuota(institute);
				}
			}
			LoggingUtil.logMessage("Exported videos for " + count + " lectures ..", LoggingUtil.schedulerLogger);
		} else {
			LoggingUtil.logMessage("No pending videos found for " + minTime, LoggingUtil.schedulerLogger);
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
				LoggingUtil.logMessage("Next video export time:" + time, LoggingUtil.schedulerLogger);
			}

		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e), LoggingUtil.schedulerLogger);
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
	
	
	public static long fixFile(String sessionName) {
		try {
			
			File recordedFile = new File(recordedFolderPath + sessionName + ".mp4");
			LoggingUtil.logMessage("Fixing the file for " + recordedFile.getAbsolutePath(), LoggingUtil.videoLogger);
			if(recordedFile.exists() && recordedFile.length() > 0) {
				FileUtils.copyFileToDirectory(recordedFile, new File(outputFolder), false);
				return recordedFile.length();
			} else {
				LoggingUtil.logMessage("Could not find file " + recordedFile.getAbsolutePath(), LoggingUtil.videoLogger);
				return -1;
			}
		} catch (Exception e) {
			LoggingUtil.logError("Error in fixing recorde file " + sessionName + " -- " + ExceptionUtils.getStackTrace(e), LoggingUtil.videoLogger);
		}
		return -1;
	}
	
	public static EdoVideoLecture callFixFileApi(String videoName) throws JsonGenerationException, JsonMappingException, IOException {
		try {
			ClientConfig config = new DefaultClientConfig();
			config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
			Client client = Client.create(config);
			WebResource webResource = client.resource(FIX_URL);
			EdoServiceRequest request = new EdoServiceRequest();
			EdoVideoLecture lecture = new EdoVideoLecture();
			lecture.setVideoName(videoName);
			request.setLecture(lecture);
			LoggingUtil.logMessage("Calling fix URL with request:" + videoName, LoggingUtil.videoLogger);
			ClientResponse response = webResource.type("application/json").post(ClientResponse.class, request);

			if (response.getStatus() != 200) {
				LoggingUtil.logMessage("Failed in fix URL URL : HTTP error code : " + response.getStatus(), LoggingUtil.videoLogger);
			}
			String output = response.getEntity(String.class);
			LoggingUtil.logMessage("Output from fix URL : " + response.getStatus() + ".... \n " + output, LoggingUtil.videoLogger);

			EdoServiceResponse serviceResponse = new ObjectMapper().readValue(output, EdoServiceResponse.class);
			if(serviceResponse != null && serviceResponse.getLectures() != null && serviceResponse.getLectures().size() > 0) {
				return serviceResponse.getLectures().get(0).getLecture();
			}
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e), LoggingUtil.videoLogger);
		}
		
		return null;
	}

}
