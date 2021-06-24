package com.rns.web.edo.service.bo.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.clickntap.vimeo.VimeoResponse;
import com.rns.web.edo.service.VideoExportScheduler;
import com.rns.web.edo.service.bo.api.EdoFile;
import com.rns.web.edo.service.bo.api.EdoUserBo;
import com.rns.web.edo.service.dao.EdoHibernateDao;
import com.rns.web.edo.service.dao.EdoTestsDao;
import com.rns.web.edo.service.domain.EDOInstitute;
import com.rns.web.edo.service.domain.EDOPackage;
import com.rns.web.edo.service.domain.EDOTestAnalysis;
import com.rns.web.edo.service.domain.EdoApiStatus;
import com.rns.web.edo.service.domain.EdoChapter;
import com.rns.web.edo.service.domain.EdoComplexOption;
import com.rns.web.edo.service.domain.EdoFeedback;
import com.rns.web.edo.service.domain.EdoMailer;
import com.rns.web.edo.service.domain.EdoPaymentStatus;
import com.rns.web.edo.service.domain.EdoQuestion;
import com.rns.web.edo.service.domain.EdoServiceRequest;
import com.rns.web.edo.service.domain.EdoServiceResponse;
import com.rns.web.edo.service.domain.EdoStudent;
import com.rns.web.edo.service.domain.EdoStudentSubjectAnalysis;
import com.rns.web.edo.service.domain.EdoSubject;
import com.rns.web.edo.service.domain.EdoSuggestion;
import com.rns.web.edo.service.domain.EdoTest;
import com.rns.web.edo.service.domain.EdoTestQuestionMap;
import com.rns.web.edo.service.domain.EdoTestStudentMap;
import com.rns.web.edo.service.domain.EdoVideoLectureMap;
import com.rns.web.edo.service.domain.ext.EdoFaceScore;
import com.rns.web.edo.service.domain.ext.EdoImpartusResponse;
import com.rns.web.edo.service.domain.jpa.EdoAnswerEntity;
import com.rns.web.edo.service.domain.jpa.EdoAnswerFileEntity;
import com.rns.web.edo.service.domain.jpa.EdoContentMap;
import com.rns.web.edo.service.domain.jpa.EdoDeviceId;
import com.rns.web.edo.service.domain.jpa.EdoInstituteEntity;
import com.rns.web.edo.service.domain.jpa.EdoKeyword;
import com.rns.web.edo.service.domain.jpa.EdoLiveSession;
import com.rns.web.edo.service.domain.jpa.EdoLiveToken;
import com.rns.web.edo.service.domain.jpa.EdoProctorImages;
import com.rns.web.edo.service.domain.jpa.EdoProfileEntity;
import com.rns.web.edo.service.domain.jpa.EdoStudentEntity;
import com.rns.web.edo.service.domain.jpa.EdoTestEntity;
import com.rns.web.edo.service.domain.jpa.EdoTestStatusEntity;
import com.rns.web.edo.service.domain.jpa.EdoVideoLecture;
import com.rns.web.edo.service.util.CommonUtils;
import com.rns.web.edo.service.util.EdoAwsUtil;
import com.rns.web.edo.service.util.EdoConstants;
import com.rns.web.edo.service.util.EdoFaceDetection;
import com.rns.web.edo.service.util.EdoImageUtil;
import com.rns.web.edo.service.util.EdoLiveUtil;
import com.rns.web.edo.service.util.EdoMailUtil;
import com.rns.web.edo.service.util.EdoMyBatisUtil;
import com.rns.web.edo.service.util.EdoPDFUtil;
import com.rns.web.edo.service.util.EdoPropertyUtil;
import com.rns.web.edo.service.util.EdoSMSUtil;
import com.rns.web.edo.service.util.LoggingUtil;
import com.rns.web.edo.service.util.PaymentUtil;
import com.rns.web.edo.service.util.QuestionParser;
import com.rns.web.edo.service.util.VideoUtil;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.BodyPartEntity;
import com.sun.jersey.multipart.FormDataBodyPart;

public class EdoUserBoImpl implements EdoUserBo, EdoConstants {

	private ThreadPoolTaskExecutor executor;
	private EdoTestsDao testsDao;
	private String filePath;
	private DataSourceTransactionManager txManager;
	private SessionFactory sessionFactory;
	private Map<Integer, Integer> testSubmissions = new ConcurrentHashMap<Integer, Integer>();
	
	public void setTxManager(DataSourceTransactionManager txManager) {
		this.txManager = txManager;
	}
	
	public DataSourceTransactionManager getTxManager() {
		return txManager;
	}
	
	public void setFilePath(String filePath) {
		this.filePath = filePath;
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
	
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public EdoServiceResponse getTestResult(EdoServiceRequest request) {
		EdoServiceResponse response = new EdoServiceResponse();
		if (request.getStudent() == null || request.getStudent().getId() == null || request.getTest() == null || request.getTest().getId() == null) {
			response.setStatus(new EdoApiStatus(STATUS_ERROR, ERROR_INCOMPLETE_REQUEST));
			return response;
		}
		List<EdoTestQuestionMap> map = testsDao.getExamResult(request);
		
		Map<String, BigDecimal> subjectWiseScore = new HashMap<String, BigDecimal>();
		
		EdoTest test = null;
		if(CollectionUtils.isEmpty(map)) {
			//If the exam is expired..show the solutions anyway
			test = testsDao.getTest(request.getTest().getId());
			if(test != null && test.getEndDate() != null && test.getEndDate().getTime() < new Date().getTime() && StringUtils.equals(test.getShowResult(), "Y")) {
				request.setRequestType("Solution");
				map = testsDao.getExamResult(request);
			}
		}
		
		
		if(CollectionUtils.isNotEmpty(map)) {
			test = map.get(0).getTest();
			
			Map<String, Object> input = new HashMap<String, Object>();
			input.put("test", test.getId());
			input.put("student", request.getStudent().getId());
			List<EdoTestStudentMap> resultMap = testsDao.getSubjectwiseScoreStudent(input);
			List<EdoStudentSubjectAnalysis> subjectAnalysis = CommonUtils.getSubjectAnalysis(test, resultMap, request.getStudent());
			EDOTestAnalysis analysis = new EDOTestAnalysis();
			analysis.setSubjectAnalysis(subjectAnalysis);
			test.setAnalysis(analysis);
			
			//Set URL for solution PDF
			if(StringUtils.isNotBlank(test.getSolutionUrl())) {
				test.setSolutionUrl(CommonUtils.prepareUrl(test.getSolutionUrl()));
			}
			
			//Get question correctness analysis
			//TODO should be added question wise
			/*List<EdoQuestion> questionCorrectness = null;
			if(StringUtils.equals(test.getShowResult(), "Y")) {
				questionCorrectness = testsDao.getQuestionCorrectness(test.getId());
			}*/
			
			//If show result is enabled, show rank and topper score
			//Show rank only if flag is enabled
			if(StringUtils.equals(test.getShowResult(), "Y") && (test.getShowRank() == null || test.getShowRank() != 0)) {
				List<EdoTest> rankResponse = testsDao.getStudentRank(request);
				if(CollectionUtils.isNotEmpty(rankResponse)) {
					test.setRank(rankResponse.get(0).getRank());
				}
				List<EdoTest> topScoreResponse = testsDao.getTopperScore(test.getId());
				if(CollectionUtils.isNotEmpty(topScoreResponse) && topScoreResponse.get(0).getAnalysis() != null) {
					if(test.getAnalysis() == null) {
						test.setAnalysis(new EDOTestAnalysis());
					}
					test.getAnalysis().setTopScore(topScoreResponse.get(0).getAnalysis().getTopScore());
					test.getAnalysis().setStudentsAppeared(topScoreResponse.get(0).getAnalysis().getStudentsAppeared());
				}
			}
			
			test.setSections(new ArrayList<String>());

			for(EdoTestQuestionMap mapper: map) {
				EdoQuestion question = mapper.getQuestion();
				CommonUtils.setQuestionURLs(question);
				QuestionParser.fixQuestion(question);
				prepareMatchTypeQuestion(question);
				
				//Add only if not disabled
				if(question.getDisabled() != null && question.getDisabled() == 1) {
					if(StringUtils.isBlank(question.getAnswer())) {
						continue;
					}
				}
				
				if(test.getRandomPool() != null && test.getRandomPool() == 1) {
					//For random pool, only show solved questions
					if(question.getMapId() == null) {
						continue;
					}
				}
				
				/*if(CollectionUtils.isNotEmpty(questionCorrectness)) {
					for(EdoQuestion correctness: questionCorrectness) {
						if(correctness.getQn_id() != null && question.getQn_id() != null && correctness.getQn_id().intValue() == question.getQn_id().intValue()) {
							question.setAnalysis(correctness.getAnalysis());
							break;
						}
					}
				}*/
				
				if(StringUtils.isNotBlank(question.getSection())) {
					if(!test.getSections().contains(question.getSection())) {
						test.getSections().add(question.getSection());
					}
				}
				
				test.getTest().add(question);
				/*if(!CommonUtils.isBonus(question) && StringUtils.isBlank(StringUtils.trimToEmpty(question.getAnswer()))) {
					continue;
				}
				BigDecimal score = subjectWiseScore.get(question.getSubject());
				if(score == null) {
					score = new BigDecimal(0);
				}
				if(CommonUtils.isBonus(question) || StringUtils.equalsIgnoreCase(question.getAnswer(), question.getCorrectAnswer())) {
					if (question.getWeightage() != null) {
						score = score.add(new BigDecimal(question.getWeightage()));
					}
				} else if (question.getNegativeMarks() != null) {
					score = score.subtract(new BigDecimal(question.getNegativeMarks()));
				}
				subjectWiseScore.put(question.getSubject(), score);*/
			}
			
			//Fetch video lectures for test (if any)
			response.setLectures(testsDao.getTestVideoLectures(test.getId()));
			
			if(test != null && StringUtils.equals(test.getTestUi(), "DESCRIPTIVE")) {
				List<EdoAnswerFileEntity> answerFiles = testsDao.getAnswerFiles(request);
				if(CollectionUtils.isNotEmpty(answerFiles)) {
					if(test.getSolvedCount() == null) {
						test.setSolvedCount(answerFiles.size());
					}
					for(EdoAnswerFileEntity file: answerFiles) {
						file.setFileUrl(file.getFileUrl() + "?ver=" + System.currentTimeMillis());
						if(StringUtils.isNotBlank(file.getCorrectionUrl())) {
							file.setCorrectionUrl(file.getCorrectionUrl() + "?ver=" + System.currentTimeMillis());
						}
					}
				}
				test.setAnswerFiles(answerFiles);
			} 
			
			response.setTest(test);
		} else {
			response.setStatus(new EdoApiStatus(-111, "Result not found. Please submit your exam first."));
		}
		
		return response;
	}

	public EdoServiceResponse getTest(EdoServiceRequest req) {
		
		long time0 = System.currentTimeMillis();
		long initTime = 0, fetchTime = 0, fetchQueryTime = 0, processTime = 0;
		
		EdoServiceResponse response = new EdoServiceResponse();
		Integer studenId = null;
		Integer testId = null;
		if(req.getTest() != null) {
			testId = req.getTest().getId();
		}
		if(req.getStudent() != null) {
			studenId = req.getStudent().getId();
		}
		if(testId == null) {
			response.setStatus(new EdoApiStatus(STATUS_ERROR, ERROR_INCOMPLETE_REQUEST));
			return response;
		}
		
		Session session = null;
		Session writeSession = null;
		EdoHibernateDao hDao = null;
		try {
			
			session = this.sessionFactory.openSession();
			
			hDao = new EdoHibernateDao(session);
			
			//EdoTest result = testsDao.getTest(testId);
			EdoTest result = EdoMyBatisUtil.convertToTest(hDao.getEntityByKey(EdoTestEntity.class, "id", testId));
			if(result == null) {
				response.setStatus(new EdoApiStatus(STATUS_ERROR, ERROR_INCOMPLETE_REQUEST));
				return response;
			}
			
			
			EdoTestStudentMap inputMap = new EdoTestStudentMap();
			inputMap.setTest(new EdoTest(testId));
			Date startedDate = null;
			EdoTestStatusEntity studentMap = null;
			Integer adminReset = null;
			Integer timeLeft = null;
			if(studenId != null) {
				inputMap.setStudent(new EdoStudent(studenId));
				//List<EdoTestStudentMap> studentMaps = testsDao.getTestStatus(inputMap);
				
				List<EdoTestStatusEntity> studentMaps = writeSession.createCriteria(EdoTestStatusEntity.class)
														.add(Restrictions.eq("studentId", studenId))
														.add(Restrictions.eq("testId", testId)).list();
				
				if(CollectionUtils.isNotEmpty(studentMaps)) {
					studentMap = studentMaps.get(0);
				}
				if(studentMap != null && StringUtils.equals(TEST_STATUS_COMPLETED, studentMap.getStatus())) {
					response.setStatus(new EdoApiStatus(STATUS_TEST_SUBMITTED, ERROR_TEST_ALREADY_SUBMITTED));
					return response;
				}
				
				String status = "";
				if(studentMap != null) {
					startedDate = studentMap.getCreatedDate();
					if(DateUtils.isSameDay(startedDate, new Date())) {
						status = studentMap.getStatus();
					}
				}
				Integer startedCount = 0;
				
				writeSession = this.sessionFactory.openSession();
				Transaction tx = writeSession.beginTransaction();
				//Added on 11/12/19
				if(studentMap == null) {
					//Add test status as 'STARTED' to track students who logged in
					/*EdoServiceRequest request = new EdoServiceRequest();
					EdoStudent student = new EdoStudent();
					student.setId(studenId);
					request.setStudent(student);
					EdoTest test = new EdoTest();
					test.setId(testId);
					request.setTest(test);
					request.setRequestType(TEST_STATUS_STARTED);
					test.setSolvedCount(0);
					test.setCorrectCount(0);
					test.setFlaggedCount(0);
					test.setScore(BigDecimal.ZERO);
					test.setDevice(req.getTest().getDevice());
					test.setDeviceInfo(StringUtils.substring(req.getTest().getDeviceInfo(), 0, 100));
					test.setLocationLat(req.getTest().getLocationLat());
					test.setLocationLong(req.getTest().getLocationLong());*/
					//TODO Add later testsDao.saveTestStatus(request);
					
					EdoTestStatusEntity testStatus = new EdoTestStatusEntity();
					testStatus.setTestId(testId);
					testStatus.setStudentId(studenId);
					testStatus.setCreatedDate(new Date());
					testStatus.setSolved(0);
					testStatus.setCorrect(0);
					testStatus.setFlagged(0);
					testStatus.setScore(BigDecimal.ZERO);
					testStatus.setDevice(req.getTest().getDevice());
					testStatus.setDeviceInfo(req.getTest().getDeviceInfo());
					testStatus.setLatitude(req.getTest().getLocationLat());
					testStatus.setLongitude(req.getTest().getLocationLong());
					testStatus.setStartedCount(1);
					testStatus.setStatus(TEST_STATUS_STARTED);
					
					writeSession.persist(testStatus);
					
					tx.commit();
					
				} else {
					//Update test status for timestamp and exam started count
					/*EdoServiceRequest request = new EdoServiceRequest();
					EdoStudent student = new EdoStudent();
					student.setId(studenId);
					request.setStudent(student);
					EdoTest test = new EdoTest();
					test.setId(testId);
					if(req.getTest().getLocationLat() != null && req.getTest().getLocationLong() != null) {
						test.setLocationLat(req.getTest().getLocationLat());
						test.setLocationLong(req.getTest().getLocationLong());
					}
					request.setTest(test);*/
					
					studentMap.setStartedCount(studentMap.getStartedCount() + 1);
					studentMap.setUpdatedDate(new Date());
					if(req.getTest().getLocationLat() != null && req.getTest().getLocationLong() != null) {
						studentMap.setLatitude(req.getTest().getLocationLat());
						studentMap.setLongitude(req.getTest().getLocationLong());
					}
					
					tx.commit();
					
					//TODO ADD later testsDao.updateTestStatus(request);
					if(studentMap.getStartedCount() != null) {
						startedCount = studentMap.getStartedCount();
					}
					/*if(studentMap.getTest() != null) {
						adminReset = studentMap.getTest().getAdminReset();
					}*/
					if(studentMap.getAdminReset()  != null) {
						adminReset = studentMap.getAdminReset();
					}
					//timeLeft = studentMap.getTimeLeft();
					if(studentMap.getTimeLeft() != null) {
						timeLeft = studentMap.getTimeLeft().intValue();
					}
					
					//Check for max allowed start attempts
					if(studentMap != null && studentMap.getStartedCount() != null && result.getMaxStarts() != null) {
						if(result.getMaxStarts() <= studentMap.getStartedCount()) {
							response.setStatus(new EdoApiStatus(STATUS_ERROR, "You have reached maximum no of test attempts. Please contact your admin for more info."));
							return response;
						}
					}
					
				}
				
				
				
				//TODO add later addTestActivity(testId, studenId, "STARTED", req.getTest());
				//Added on 11/12/19
				
				//Check against test start and end date
				EdoTest mapTest = result;
				if(mapTest != null) {
					if(mapTest.getStartDate() != null && mapTest.getStartDate().getTime() > new Date().getTime()) {
						response.setStatus(new EdoApiStatus(STATUS_TEST_NOT_OPENED, "Test will be available on " + CommonUtils.convertDate(mapTest.getStartDate())));
						return response;
					}
					if(mapTest.getEndDate() != null && mapTest.getEndDate().getTime() < new Date().getTime() && !StringUtils.equals(status, TEST_STATUS_STARTED)) {
						response.setStatus(new EdoApiStatus(STATUS_TEST_EXPIRED, ERROR_TEST_EXPIRED));
						return response;
						
					}
				}
				
				//TODO discuss and decide if it's needed
				/*
				 studentMaps = testsDao.getStudentActivePackage(inputMap);
				
				if(CollectionUtils.isNotEmpty(studentMaps)) {
					studentMap = studentMaps.get(0);
				} else {
					studentMap = null;
				}
				
				if(studentMap == null) {
					//Test or package not active
					response.setStatus(new EdoApiStatus(STATUS_TEST_NOT_PAID, ERROR_TEST_NOT_PAID));
					return response;
				}
				
				studentMap.setStartedCount(startedCount);
				 * if(!StringUtils.equalsIgnoreCase(studentMap.getStudentAccess(), ACCESS_LEVEL_ADMIN)) {
					if(!StringUtils.equalsIgnoreCase(STATUS_ACTIVE, result.getStatus())) {
						response.setStatus(new EdoApiStatus(STATUS_TEST_NOT_ACTIVE, ERROR_TEST_NOT_ACTIVE));
						return response;
					}
					
					EdoTest mapTest = result;
					if(mapTest != null) {
						if(mapTest.getStartDate() != null && mapTest.getStartDate().getTime() > new Date().getTime()) {
							response.setStatus(new EdoApiStatus(STATUS_TEST_NOT_OPENED, "Test will be available on " + CommonUtils.convertDate(mapTest.getStartDate())));
							return response;
						}
						if(mapTest.getEndDate() != null && mapTest.getEndDate().getTime() < new Date().getTime() && !StringUtils.equals(status, TEST_STATUS_STARTED)) {
							if(studentMap.getRegisterDate() != null && studentMap.getRegisterDate().getTime() < mapTest.getEndDate().getTime()) {
								response.setStatus(new EdoApiStatus(STATUS_TEST_EXPIRED, ERROR_TEST_EXPIRED));
								return response;
							}
						}
					}
				}*/
				
			/*} else if (StringUtils.equals("DOWNLOAD_EXAM", req.getRequestType())) {
				String downloadExam = EdoPropertyUtil.getProperty(EdoPropertyUtil.DOWNLOAD_EXAM);
				if(StringUtils.isBlank(downloadExam) || !StringUtils.equalsIgnoreCase("Y", downloadExam)) {
					response.setStatus(new EdoApiStatus(-111, "This feature is disabled at this moment. Please try again later."));
					return response;
				}
			}*/
			
				
			}
			
			initTime = System.currentTimeMillis() - time0;
			time0 = System.currentTimeMillis();
			
			fetchQueryTime = System.currentTimeMillis();
			
			//List<EdoTestQuestionMap> map = testsDao.getExam(testId);
			
			SQLQuery query = session.createSQLQuery("SELECT test_questions_map.question_id,question,option1,option2,option3,option4,option5,test_subjects.subject, " +
					"test_questions.status as qStatus,question_img_url,option1_img_url,option2_img_url,option3_img_url,option4_img_url, " +
					"test_questions.question_type,meta_data,meta_data_img_url,test_questions_map.section,test_questions_map.question_number, " +
					"test_questions_map.weightage,test_questions_map.negative_marks,test_questions.correct_answer,alt_answer,partial,solution, " +
					"solution_img_url,test_questions.chapter,chapters.chapter_name,test_questions.level FROM test" +
					" join test_questions_map on test.test_id =" +
					" test_questions_map.test_id" +
					" join test_questions on" +
					" test_questions_map.question_id = test_questions.id" +
					" join test_subjects" +
					" on test_questions.subject_id = test_subjects.subject_id" +
					" left join chapters" +
					" on test_questions.chapter = chapters.id" +
					" where" +
					" test.test_id =:testId" + 
					" AND (test_questions_map.question_disabled is NULL or test_questions_map.question_disabled = 0)" +
					" order by test_questions_map.question_number,test_questions_map.id asc");
			query.setInteger("testId", testId);
			
			List<Object[]> list = query.list();
			List<EdoTestQuestionMap> map = EdoMyBatisUtil.convertHibernateExamMap(list);
			fetchQueryTime = System.currentTimeMillis() - fetchQueryTime;
			
			//Check for random pool property..if set to 1, random questions need to be picked out of total questions added
			Map<String, Integer> examQuestionCount = null;
			Map<String, List<EdoQuestion>> solvedSet = null;
			
			if(CollectionUtils.isNotEmpty(map)) {
				//EdoTest result = map.get(0).getTest();
				
				if(studenId != null) {
					//Check if time constraint is present
					if(StringUtils.equals("1", result.getTimeConstraint())) {
						Date startTime = result.getStartDate();
						if(startTime != null) {
							calculateTimeLeft(result, startTime);
							
						}
					} else if (StringUtils.equals("1", result.getStudentTimeConstraint())) {
						if(startedDate != null) {
							calculateTimeLeft(result, startedDate);
						}
					}
					
					//Check if location is compulsory and location is sent
					if(result.getAcceptLocation() != null && result.getAcceptLocation() == 1) {
						if(StringUtils.isBlank(req.getTest().getLocationLat()) || StringUtils.isBlank(req.getTest().getLocationLong())) {
							response.setStatus(new EdoApiStatus(STATUS_NO_LOCATION, ERROR_NO_LOCATION));
							return response;
						}
					}
					
					//Check if force update is set, if yes.. check user device info and ask for app update if required
					if(result.getForceUpdate() != null && result.getForceUpdate() == 1) {
						if(StringUtils.isBlank(req.getTest().getDevice()) || StringUtils.isBlank(req.getTest().getDeviceInfo())) {
							response.setStatus(new EdoApiStatus(STATUS_WRONG_VERSION, ERROR_WRONG_VERSION));
							return response;
						}
						if(result.getInstituteId() != null && StringUtils.equals(req.getTest().getDevice(), "app")) {
							String[] values = StringUtils.split(req.getTest().getDeviceInfo(), ",");
							String userAppVersion = "";
							if(ArrayUtils.isNotEmpty(values)) {
								String[] versionKeys = StringUtils.split(values[0], "=");
								if(ArrayUtils.isNotEmpty(versionKeys)) {
									userAppVersion = StringUtils.trimToEmpty(versionKeys[1]);
								}
							}
							//EDOInstitute institute = testsDao.getInstituteById(result.getInstituteId());
							EDOInstitute institute = EdoMyBatisUtil.convertToInstitute(hDao.getEntityByKey(EdoInstituteEntity.class, "id", result.getInstituteId()));
							
							if(institute != null && StringUtils.isNotBlank(institute.getAppVersion())) {
								//Compare app version with users version and show error if older version
								if(userAppVersion.compareTo(StringUtils.trimToEmpty(institute.getAppVersion())) < 0) {
									response.setInstitute(institute);
									response.setStatus(new EdoApiStatus(STATUS_WRONG_VERSION, ERROR_WRONG_VERSION));
									return response;
								}
							} else {
								String appVersion = StringUtils.trimToEmpty(EdoPropertyUtil.getProperty(EdoPropertyUtil.APP_VERSION));
								if(StringUtils.isNotBlank(appVersion)) {
									//Compare app version with users version and show error if older version
									if(userAppVersion.compareTo(appVersion) < 0) {
										EDOInstitute insti = new EDOInstitute();
										insti.setAppUrl("https://play.google.com/store/apps/details?id=com.mattersoft.edofoxapp&hl=en_IN&gl=US");
										response.setInstitute(insti);
										response.setStatus(new EdoApiStatus(STATUS_WRONG_VERSION, ERROR_WRONG_VERSION));
										return response;
									}
								}
							}
						}
					}
					
					if(result.getRandomPool() != null && result.getRandomPool() == 1) {
						//Check how many question attempted by student..if equals test no of questions..skip randomizing
						EdoServiceRequest request = new EdoServiceRequest();
						EdoStudent student = new EdoStudent();
						student.setId(studenId);
						request.setStudent(student);
						request.setTest(result);
						List<EdoTestQuestionMap> answersMap = testsDao.getExamSolved(request);
						if(CollectionUtils.isNotEmpty(answersMap)) {
							//if(answersMap.size() < result.getNoOfQuestions()) {
								//Find out section wise question count
								examQuestionCount = extractSectionswiseCount(map, examQuestionCount);
								solvedSet = new HashMap<String, List<EdoQuestion>>();
								for(EdoTestQuestionMap solved: answersMap) {
									if(solved.getQuestion() != null && solved.getQuestion().getSection() != null) {
										String section = solved.getQuestion().getSection();
										if(StringUtils.isBlank(section)) {
											solved.getQuestion().setSection("General");
											section = "General";
										}
										if(examQuestionCount.get(section) != null) {
											examQuestionCount.put(section, examQuestionCount.get(section) - 1);
											if(solvedSet.get(section) == null) {
												solvedSet.put(section, new ArrayList<EdoQuestion>());
											}
											solvedSet.get(section).add(solved.getQuestion());
										}
									}
								}
								
							//}
						} else {
							//Find out section wise question count
							examQuestionCount = extractSectionswiseCount(map, examQuestionCount);
						}
					}
					
					
				}
				
				fetchTime = System.currentTimeMillis() - time0;
				time0 = System.currentTimeMillis();
				
				//TODO New code
				result.setSubjects(new ArrayList<String>());
				
				
				Integer count = 1;
				Map<String, List<EdoQuestion>> sectionSets = new HashMap<String, List<EdoQuestion>>();
				for(EdoTestQuestionMap mapper: map) {
					EdoQuestion question = mapper.getQuestion();
					if(question != null) {
						question.setId(count);
						CommonUtils.setQuestionURLs(question);
						prepareMatchTypeQuestion(question);
						QuestionParser.fixQuestion(question);
						//Don't show answers in case of student getTest call
						if(studenId != null) {
							question.setCorrectAnswer(null);
							question.setAlternateAnswer(null);
							question.setSolution(null);
							question.setSolutionImageUrl(null);
						}
						if(!result.getSubjects().contains(question.getSubject())) {
							result.getSubjects().add(question.getSubject());
						}
						if(StringUtils.isBlank(question.getSection())) {
							question.setSection("General");
						}
						if(StringUtils.isNotBlank(question.getSection())) {
							if(!result.getSections().contains(question.getSection())) {
								result.getSections().add(question.getSection());
							}
							if(isRandomizeQuestions(result)) {
								List<EdoQuestion> questionList = sectionSets.get(question.getSection());
								if(questionList == null) {
									questionList = new ArrayList<EdoQuestion>();
								}
								questionList.add(question);
								sectionSets.put(question.getSection(), questionList);
							}
						}
						result.getTest().add(question);
						count++;
					}
				}
				if(isRandomizeQuestions(result) && studenId != null) {
					//Randomize only for student NOT for Admin
					randomizeQuestions(result, sectionSets, examQuestionCount, solvedSet);
					
					//If exam has random pool..make entries in test_result for student on first test load
					if(examQuestionCount != null && CollectionUtils.isNotEmpty(examQuestionCount.entrySet()) && (solvedSet == null || CollectionUtils.isEmpty(solvedSet.entrySet()))) {
						EdoServiceRequest request = new EdoServiceRequest();
						EdoStudent student = new EdoStudent();
						student.setId(studenId);
						request.setStudent(student);
						request.setTest(result);
						testsDao.saveTestResult(request);
					}
					
				}
				//Get JEE sections eligible for JEE format
				if(studenId != null) {
					result.setJeeNewFormatSections(CommonUtils.sectionsEligibleForNewJeeFormat(result, result.getTest()));
					if(CollectionUtils.isNotEmpty(result.getJeeNewFormatSections())) {
						result.setJeeMaxNumeric(JEE_NEW_FORMAT_BEST_OF_VALUE);
					}
					//Check for proctoring
					if(StringUtils.equalsIgnoreCase(result.getTestUi(), "PROCTORING")) {
						//EdoStudent student = testsDao.getStudentById(studenId);
						EdoStudentEntity student = hDao.getEntityByKey(EdoStudentEntity.class, "id", studenId);
						if(student != null && StringUtils.isNotBlank(student.getProctorImageRef())) {
							result.setProctoringImage(student.getProctorImageRef());
						} else {
							response.setStatus(new EdoApiStatus(STATUS_PROCTOR_ERROR, ERROR_PROCTOR));
							return response;
						}
					}
					
					//Set admin reset value and time left
					result.setAdminReset(adminReset);
					if(adminReset != null && adminReset == 1 && timeLeft != null && timeLeft > 0 && result.getMinLeft() == null && result.getSecLeft() == null) {
						result.setSecLeft(timeLeft.longValue() % 60); //seconds left
						result.setMinLeft(timeLeft.longValue() / 60);
					}
					
				}
				response.setTest(result);
			}
			
			processTime = System.currentTimeMillis() - time0;
			
			//LoggingUtil.logMessage("Get Test Processing time ==> init: " + initTime + " query fetch:" + fetchQueryTime + " fetch:" + fetchTime + " process:" + processTime, LoggingUtil.debugLogger);
			
			
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			response.setStatus(new EdoApiStatus(STATUS_ERROR, ERROR_IN_PROCESSING));
		} finally {
			CommonUtils.closeSession(session);
			CommonUtils.closeSession(writeSession);
		}
		
		return response;
	}

	private Map<String, Integer> extractSectionswiseCount(List<EdoTestQuestionMap> map, Map<String, Integer> examQuestionCount) {
		if(CollectionUtils.isNotEmpty(map)) {
			Map<String, Integer> sectionWiseCount = new HashMap<String, Integer>();
			for(EdoTestQuestionMap m: map) {
				EdoQuestion question = m.getQuestion();
				if(question.getSection() == null) {
					continue;
				}
				if(StringUtils.isBlank(question.getSection())) {
					question.setSection("General");
				}
				if(sectionWiseCount.get(question.getSection()) != null) {
					sectionWiseCount.put(question.getSection(), sectionWiseCount.get(question.getSection()) + 1);
				} else {
					sectionWiseCount.put(question.getSection(), 1);
				}
			}
			//Find section wise proportions
			examQuestionCount = new HashMap<String, Integer>();
			float proportion = (float) map.get(0).getTest().getNoOfQuestions() / map.size();
			for(java.util.Map.Entry<String, Integer> e: sectionWiseCount.entrySet()) {
				Integer examProportion = Double.valueOf(e.getValue() * proportion).intValue();
				examQuestionCount.put(e.getKey(), examProportion);
			}
		}
		return examQuestionCount;
	}

	private void addTestActivity(Integer testId, Integer studenId, String type, EdoTest edoTest) {
		EdoServiceRequest req = new EdoServiceRequest();
		EdoStudent stu = new EdoStudent();
		stu.setId(studenId);
		req.setStudent(stu);
		EdoTest tst = new EdoTest();
		tst.setId(testId);
		if(edoTest != null) {
			tst.setDevice(edoTest.getDevice());
			tst.setDeviceInfo(StringUtils.substring(edoTest.getDeviceInfo(), 0, 100));
		}
		req.setTest(tst);
		req.setRequestType(type);
		testsDao.saveStudentTestActivity(req);
	}

	private void calculateTimeLeft(EdoTest result, Date startTime) {
		Long durationInMs = new Long(result.getDuration() * 1000);
		long timeDifference = System.currentTimeMillis() - startTime.getTime();
		if(timeDifference > 0) {
			long timeLeft = durationInMs - timeDifference;
			if(timeLeft < 0) {
				result.setSecLeft(0L);
				result.setMinLeft(0L);
			} else {
				long secondsDiff = timeLeft / 1000;
				result.setSecLeft(secondsDiff % 60); //seconds left
				result.setMinLeft(secondsDiff / 60);
			}
		}
	}

	private void randomizeQuestions(EdoTest result, Map<String, List<EdoQuestion>> sectionSets, Map<String,Integer> allowedCounts, Map<String,List<EdoQuestion>> solvedSets) {
		if(CollectionUtils.isNotEmpty(sectionSets.keySet()) && result != null) {
			List<EdoQuestion> shuffled = new ArrayList<EdoQuestion>();
			Integer qNo = 1;
			for(String section: result.getSections()) {
				List<EdoQuestion> set = sectionSets.get(section);
				if(!StringUtils.contains(set.get(0).getType(), QUESTION_TYPE_PASSAGE)) {
					//No shuffle for comprehension type questions
					//LoggingUtil.logMessage("Shuffling for section .." + section + " - list - " + set.size());
					Collections.shuffle(set);
				}
				
				//Populate already solved questions in case of random pool
				if(solvedSets != null && CollectionUtils.isNotEmpty(solvedSets.entrySet()) && solvedSets.get(section) != null) {
					List<EdoQuestion> solvedQuestions = solvedSets.get(section);
					if(CollectionUtils.isNotEmpty(solvedQuestions)) {
						for(EdoQuestion solvedQ: solvedQuestions) {
							solvedQ.setQuestionNumber(qNo);
							//Remove this question from above set
							if(CollectionUtils.isNotEmpty(set)) {
								for(EdoQuestion existing: set) {
									if(solvedQ.getQn_id() != null && existing.getQn_id() != null && existing.getQn_id().intValue() == solvedQ.getQn_id().intValue()) {
										set.remove(existing);
										existing.setQuestionNumber(qNo);
										if(StringUtils.isBlank(existing.getAnswer())) {
											existing.setAnswer(solvedQ.getAnswer());
										}
										if(existing.getFlagged() == null) {
											existing.setFlagged(solvedQ.getFlagged());
										}
										if(existing.getTimeSpent() == null) {
											existing.setTimeSpent(solvedQ.getTimeSpent());
										}
										if(existing.getTtl() == null) {
											existing.setTtl(solvedQ.getTtl());
										}
										shuffled.add(existing);
										break;
									}
								}
							}
							qNo++;
						}
						//shuffled.addAll(solvedQuestions);
					}
					
				}
				
				Map<String, Integer> sectionWiseCount = new HashMap<String, Integer>();
				for(EdoQuestion question: set) {
					if(allowedCounts != null && allowedCounts.get(section) != null) {
						if(allowedCounts.get(section) <= 0 || (sectionWiseCount.get(section) != null && allowedCounts.get(section) <= sectionWiseCount.get(section))) {
							//If questions in the section cross allowed count, don't add
							break;
						}
						if(sectionWiseCount.get(section) == null) {
							sectionWiseCount.put(section, 0);
						}
						sectionWiseCount.put(section, sectionWiseCount.get(section) + 1);
						if(question.getFlagged() == null) {
							question.setFlagged(0);
						}
						question.setMarks(BigDecimal.ZERO);
						if(question.getTimeSpent() == null) {
							question.setTimeSpent(0);
						}
					}
					question.setQuestionNumber(qNo);
					qNo++;
					shuffled.add(question);
					
				}
			}
			result.setTest(shuffled);
			//LoggingUtil.logMessage("Shuffled the questions for test .." + result.getId());
		}
	}
	

	private boolean isRandomizeQuestions(EdoTest result) {
		return StringUtils.equals("Y", result.getRandomQuestions());
	}

	private void prepareMatchTypeQuestion(EdoQuestion question) {
		if(question != null && StringUtils.equalsIgnoreCase(QUESTION_TYPE_MATCH, question.getType())) {
			if(StringUtils.isBlank(question.getOption1()) || StringUtils.isBlank(question.getOption2())) {
				return;
			}
			String[] leftCol = StringUtils.split(question.getOption1(), ",");
			String[] rightCol = StringUtils.split(question.getOption2(), ",");
			if(ArrayUtils.isNotEmpty(leftCol) && ArrayUtils.isNotEmpty(rightCol)) {
				List<EdoComplexOption> options = new ArrayList<EdoComplexOption>();
				for(String left: leftCol) {
					if(StringUtils.isNotBlank(StringUtils.trimToEmpty(left))) {
						EdoComplexOption option = new EdoComplexOption();
						option.setOptionName(left);
						List<EdoComplexOption> subOptions = new ArrayList<EdoComplexOption>();
						for(String right: rightCol) {
							EdoComplexOption subOption = new EdoComplexOption();
							subOption.setOptionName(right);
							if(StringUtils.contains(question.getAnswer(), left + "-" + right)) {
								subOption.setSelected(true);
							}
							subOptions.add(subOption);
						}
						if(CollectionUtils.isNotEmpty(subOptions)) {
							option.setMatchOptions(subOptions);
						}
						options.add(option);
					}
				}
				question.setComplexOptions(options);
			}
				
		}
		
	}

	//As of 11/12/19
	public EdoApiStatus saveAnswer(EdoServiceRequest request) {
		EdoApiStatus status = new EdoApiStatus();
		if(request.getTest() == null || request.getTest().getId() == null || request.getStudent() == null || request.getStudent().getId() == null) {
			status.setStatus(-111, ERROR_INCOMPLETE_REQUEST);
			return status;
		}
		Session session = null;
		try {
			session = this.sessionFactory.openSession();
			Transaction tx = session.beginTransaction();
			saveAnswer(request, session);
			Long minLeft = request.getTest().getMinLeft();
			Long secLeft = request.getTest().getSecLeft();
			if(minLeft != null && secLeft != null) {
				List<EdoTestStatusEntity> maps = /*testsDao.getTestStatus(inputMap)*/ session.createCriteria(EdoTestStatusEntity.class)
						.add(Restrictions.eq("testId", request.getTest().getId()))
						.add(Restrictions.eq("studentId", request.getStudent().getId()))
						.list();
				if(CollectionUtils.isNotEmpty(maps)) {
					maps.get(0).setTimeLeft((minLeft * 60) + secLeft);
				}
			}
			tx.commit();
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e), LoggingUtil.saveAnswerErrorLogger);
			status.setStatus(-111, ERROR_IN_PROCESSING);
		} finally {
			CommonUtils.closeSession(session);
		}
		return status;
	}
	//As of 11/12/19

	public void saveAnswer(EdoServiceRequest request, Session session) {
		EdoAnswerEntity answer = new EdoAnswerEntity();
		List<EdoAnswerEntity> existing = session.createCriteria(EdoAnswerEntity.class)
						.add(Restrictions.eq("testId", request.getTest().getId()))
						.add(Restrictions.eq("studentId", request.getStudent().getId()))
						.add(Restrictions.eq("questionId", request.getQuestion().getQn_id()))
						.list();
		if(CollectionUtils.isNotEmpty(existing)) {
			answer = existing.get(0);
		} else {
			answer.setQuestionId(request.getQuestion().getQn_id());
			answer.setStudentId(request.getStudent().getId());
			answer.setTestId(request.getTest().getId());
			answer.setCreatedDate(new Date());
		}
		
		if (StringUtils.equalsIgnoreCase(EdoConstants.QUESTION_TYPE_MATCH, request.getQuestion().getType())) {
			CommonUtils.setComplexAnswer(request.getQuestion());
		}
		
		//Save flagged and answered only if not time taken request
		if(!StringUtils.equals(request.getRequestType(), "SAVE_TIME")) {
			answer.setFlagged(request.getQuestion().getFlagged());
			if(answer.getFlagged() == null) {
				answer.setFlagged(0);
			}
			if(request.getQuestion().getAnswer() != null) {
				String pattern = EdoPropertyUtil.getProperty(EdoPropertyUtil.ALLOWED_CHARS);
				if(StringUtils.isBlank(pattern)) {
					pattern = "[^a-zA-Z0-9\\s\\-\\,\\+\\*\\/\\^\\~\\.]";
				}
				answer.setOptionSelected(StringUtils.replacePattern(request.getQuestion().getAnswer(), pattern, ""));
			} else {
				answer.setOptionSelected("");
			}
			if(answer.getOptionSelected() == null) {
				answer.setOptionSelected("");
			}
		} else if (answer.getId() == null) {
			//New submission..first time submission
			answer.setFlagged(0);
			answer.setOptionSelected("");
		}
		
		
		answer.setTimeTaken(request.getQuestion().getTimeSpent());
		//Will update only for save test
		answer.setMarks(request.getQuestion().getMarks());
		answer.setUpdatedDate(new Date());
		
		//Save random question number
		if(request.getQuestion().getQuestionNumber() != null) {
			answer.setQuestionNumber(request.getQuestion().getQuestionNumber());
		}
		//Save time taken to load question image
		if(answer.getTtl() == null && request.getQuestion().getTtl() != null) {
			answer.setTtl(request.getQuestion().getTtl());
		}
		
		if(answer.getId() == null) {
			session.persist(answer);
		}
	}
	
	public EdoApiStatus saveTest(EdoServiceRequest request) {
		EdoTest test = request.getTest();
		if(request.getStudent() == null || request.getStudent().getId() == null || test == null || test.getId() == null) {
			LoggingUtil.logMessage("Invalid test input", LoggingUtil.saveTestLogger);
			return new EdoApiStatus(-111, ERROR_INVALID_PROFILE);
		}
		EdoApiStatus status = new EdoApiStatus();
		Session session = null;
		//TransactionStatus txStatus = txManager.getTransaction(new DefaultTransactionDefinition());
		
		try {
			
			CommonUtils.saveJson(request);
			
			Integer currentTest = testSubmissions.get(request.getStudent().getId());
			if(currentTest != null && request.getTest().getId() == currentTest) {
				//Don't return already submitted as ERROR, consider it a success
				//status.setResponseText(ERROR_TEST_ALREADY_SUBMITTED);
				//status.setStatusCode(STATUS_ERROR);
				LoggingUtil.logMessage("Test " + currentTest + " being submitted for student=>" + request.getStudent().getId(), LoggingUtil.saveTestLogger);
				return status;
			}
			LoggingUtil.logMessage("Submitting test .. " + request.getTest().getId() + " by student .. " + request.getStudent().getId() + " map =>" + testSubmissions, LoggingUtil.saveTestLogger);
			testSubmissions.put(request.getStudent().getId(), request.getTest().getId());
			
			/*EdoTestStudentMap inputMap = new EdoTestStudentMap();
			inputMap.setTest(test);
			inputMap.setStudent(request.getStudent());*/
			
			//New flow
			session = sessionFactory.openSession();
			
			List<EdoTestStatusEntity> maps = /*testsDao.getTestStatus(inputMap)*/ session.createCriteria(EdoTestStatusEntity.class)
											.add(Restrictions.eq("testId", test.getId()))
											.add(Restrictions.eq("studentId", request.getStudent().getId()))
											.list();
			EdoTestStatusEntity map = null;
			if(CollectionUtils.isNotEmpty(maps)) {
				map = maps.get(0);
			}
			
			//Already submitted error removed from the code Jan 05 21 .. allow student to overwrite and submit again
			/*if(map != null && StringUtils.equals(TEST_STATUS_COMPLETED, map.getStatus())) {
				status.setResponseText(ERROR_TEST_ALREADY_SUBMITTED);
				status.setStatusCode(STATUS_ERROR);
				LoggingUtil.logMessage("Already submitted this test for student=>" + request.getStudent().getId(), LoggingUtil.saveTestLogger);
				return status;
			}*/
			
			if(map == null) {
				map = new EdoTestStatusEntity();
				map.setCreatedDate(new Date());
				map.setTestId(test.getId());
				map.setStudentId(request.getStudent().getId());
			}
			
			List<EdoQuestion> questions = testsDao.getExamQuestions(test.getId());
			
			if(CollectionUtils.isEmpty(questions)) {
				status.setResponseText(ERROR_IN_PROCESSING);
				status.setStatusCode(STATUS_ERROR);
				return status;
			}
			
			//EdoTest existing = testsDao.getTest(test.getId());
			
			//if(existing == null || StringUtils.isBlank(existing.getShowResult()) || StringUtils.equalsIgnoreCase("Y", existing.getShowResult())) {
			CommonUtils.calculateTestScore(test, questions);
			//}
			
			/*if(request != null && request.getTest() != null && CollectionUtils.isNotEmpty(request.getTest().getTest())) {
				testsDao.saveTestResult(request);
				testsDao.saveTestStatus(request);
				
				//TODO: Temporary
				EdoSMSUtil smsUtil = new EdoSMSUtil(MAIL_TYPE_TEST_RESULT);
				smsUtil.setTest(test);
				EdoStudent student = testsDao.getStudentById(request.getStudent().getId());
				smsUtil.setStudent(student);
				executor.execute(smsUtil);
			}*/
			Transaction tx = session.beginTransaction();
			if(CollectionUtils.isNotEmpty(test.getTest())) {
				for(EdoQuestion question: test.getTest()) {
					EdoServiceRequest saveAnswerRequest = new EdoServiceRequest();
					saveAnswerRequest.setTest(test);
					saveAnswerRequest.setStudent(request.getStudent());
					saveAnswerRequest.setQuestion(question);
					if(StringUtils.equalsIgnoreCase(EdoConstants.QUESTION_TYPE_MATCH, question.getType()) || StringUtils.isNotBlank(question.getAnswer())) {
						saveAnswer(saveAnswerRequest, session);
					}
				}
			}
			
			map.setSolved(test.getSolvedCount());
			map.setCorrect(test.getCorrectCount());
			map.setFlagged(test.getFlaggedCount());
			map.setScore(test.getScore());
			map.setStatus(TEST_STATUS_COMPLETED);
			map.setUpdatedDate(new Date());
			map.setSubmissionType(test.getSubmissionType());
			if(test.getMinLeft() != null && test.getSecLeft() != null) {
				map.setTimeLeft((test.getMinLeft() * 60) + test.getSecLeft());
			} else if (map.getTimeLeft() != null) {
				map.setTimeLeft(map.getTimeLeft());
			}
			if(map.getId() == null) {
				session.persist(map);
			}
			//Commit the transaction
			//txManager.commit(txStatus);
			tx.commit();
			LoggingUtil.logMessage("Submitted the test " + test.getId() +  " for .. " + request.getStudent().getId(), LoggingUtil.saveTestLogger);
			addTestActivity(test.getId(), request.getStudent().getId(), "COMPLETED", test);
		} catch (Exception e) {
			status.setStatusCode(STATUS_ERROR);
			status.setResponseText(ERROR_IN_PROCESSING);
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e), LoggingUtil.saveTestErrorLogger);
			//rollback
			/*try {
				txManager.rollback(txStatus);
			} catch (Exception e2) {
				LoggingUtil.logError(ExceptionUtils.getStackTrace(e2));
			}*/
			
		} finally {
			CommonUtils.closeSession(session);
			if(request.getStudent() != null && request.getStudent().getId() != null) {
				testSubmissions.remove(request.getStudent().getId());
				LoggingUtil.logMessage("Submitted test .. " + request.getTest().getId() + " by student .. " + request.getStudent().getId() + " map =>" + testSubmissions, LoggingUtil.saveTestLogger);
			}
		}
		return status;
	}
	
	public EdoFile getQuestionImage(Integer questionId, String imageType, Integer testId) {

		Session session = null;
		try {
			EdoFile file = new EdoFile();
			String path = null;
			if (StringUtils.equals(imageType, ATTR_QUESTION)) {
				EdoQuestion question = testsDao.getQuestion(questionId);
				if (question != null) {
					path = question.getQuestionImageUrl();
				}
			} else if (StringUtils.equals(imageType, ATTR_OPTION1)) {
				EdoQuestion question = testsDao.getQuestion(questionId);
				if (question != null) {
					path = question.getOption1ImageUrl();
				}
			} else if (StringUtils.equals(imageType, ATTR_OPTION2)) {
				EdoQuestion question = testsDao.getQuestion(questionId);
				if (question != null) {
					path = question.getOption2ImageUrl();
				}
			} else if (StringUtils.equals(imageType, ATTR_OPTION3)) {
				EdoQuestion question = testsDao.getQuestion(questionId);
				if (question != null) {
					path = question.getOption3ImageUrl();
				}
			} else if (StringUtils.equals(imageType, ATTR_OPTION4)) {
				EdoQuestion question = testsDao.getQuestion(questionId);
				if (question != null) {
					path = question.getOption4ImageUrl();
				}
			} else if (StringUtils.equals(imageType, ATTR_SOLUTION)) {
				EdoQuestion question = testsDao.getQuestion(questionId);
				if (question != null) {
					path = question.getSolutionImageUrl();
				}
			}  else if (StringUtils.equals(imageType, ATTR_META_DATA)) {
				EdoQuestion question = testsDao.getQuestion(questionId);
				if (question != null) {
					path = question.getMetaDataImageUrl();
				}
			} else if (StringUtils.contains(imageType, "TEMP")) {
				String prefix = EdoPDFUtil.QUESTION_PREFIX;
				if(StringUtils.equals(imageType, ATTR_TEMP_SOLUTION)) {
					prefix = EdoPDFUtil.SOLUTION_PREFIX;
				}
				path = TEMP_QUESTION_PATH + testId + "/" + prefix + questionId + ".png";
			} else if (StringUtils.equals(imageType, ATTR_VIDEO_QUESTION)) {
				session = this.sessionFactory.openSession();
				List<EdoVideoLecture> lecs = session.createCriteria(EdoVideoLecture.class).add(Restrictions.eq("id", questionId)).list();
				if (CollectionUtils.isNotEmpty(lecs) && StringUtils.isNotBlank(lecs.get(0).getQuestionImg())) {
					path = lecs.get(0).getQuestionImg();
				}
			} else if (StringUtils.equals(imageType, ATTR_DOUBT_IMAGE)) {
				EdoFeedback feedback = testsDao.getFeedback(questionId);
				if (feedback != null) {
					path = feedback.getAttachment();
				}
			}

			if (path != null) {
				InputStream is = new FileInputStream(path);
				file.setContent(is);
				file.setFileName(imageType + "." + CommonUtils.getFileExtension(path));
			}
			return file;
		} catch (Exception e) {
			LoggingUtil.logMessage(ExceptionUtils.getStackTrace(e));
		} finally {
			CommonUtils.closeSession(session);
		}
		return null;
	}

	public EdoServiceResponse getPackages(EDOInstitute institute) {
		if(institute == null || institute.getId() == null) {
			return new EdoServiceResponse(new EdoApiStatus(STATUS_ERROR, ERROR_INCOMPLETE_REQUEST));
		}
		EdoServiceResponse response = new EdoServiceResponse();
		try {
			response.setPackages(testsDao.getInstituePackages(institute.getId()));
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			response.setStatus(new EdoApiStatus(STATUS_ERROR, ERROR_IN_PROCESSING));
		}
		return response;
	}
	
	public EdoServiceResponse getPackages(EdoStudent student) {
		if(student == null || student.getId() == null) {
			return new EdoServiceResponse(new EdoApiStatus(STATUS_ERROR, ERROR_INCOMPLETE_REQUEST));
		}
		EdoServiceResponse response = new EdoServiceResponse();
		try {
			response.setPackages(testsDao.getStudentPackages(student.getId()));
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			response.setStatus(new EdoApiStatus(STATUS_ERROR, ERROR_IN_PROCESSING));
		}
		return response;
	}

	public EdoServiceResponse registerStudent(EdoStudent student) {
		if(student == null || StringUtils.isBlank(student.getPhone()) || CollectionUtils.isEmpty(student.getPackages()) || StringUtils.isBlank(student.getExamMode())) {
			return new EdoServiceResponse(new EdoApiStatus(STATUS_ERROR, ERROR_INCOMPLETE_REQUEST));
		}
		EdoServiceResponse response = new EdoServiceResponse();
		//TransactionStatus txStatus = txManager.getTransaction(new DefaultTransactionDefinition());
		try {
			
			String status = "Completed";
			if(student.getPayment() != null && student.getPayment().isOffline()) {
				student.getPayment().setMode("Offline");
			} else {
				EdoPaymentStatus payment = new EdoPaymentStatus();
				payment.setMode("Online");
				status = "Created";
				student.setPayment(payment);
			}
			//Update package status
			if(CollectionUtils.isNotEmpty(student.getPackages())) {
				for(EDOPackage pkg: student.getPackages()) {
					//if(StringUtils.isNotBlank(pkg.getStatus())) {
					pkg.setStatus(status);
					//}
				}
			} else {
				response.setStatus(new EdoApiStatus(STATUS_ERROR, "Please select at least one package"));
				return response;
			}
			
			EDOInstitute institute = testsDao.getStudentStats(student.getPackages().get(0).getInstitute().getId());
			if(institute != null && institute.getMaxStudents() != null && institute.getCurrentCount() != null) {
				if((institute.getCurrentCount() + 1) >= institute.getMaxStudents()) {
					LoggingUtil.logMessage("Max students limit reached ... " + student.getPhone() + " for institute " + student.getPackages().get(0).getInstitute().getId() + " count " +institute.getCurrentCount() + " and max " + institute.getMaxStudents(), LoggingUtil.paymentLogger);
					response.setStatus(new EdoApiStatus(-111, "Maximum students limit reached. Please contact your organization to upgrade the plan."));
					return response;
				}
			}
			
			//student.setCurrentPackage(studentPackage);
			//student.setRollNo(""); //For new student
			
			if(StringUtils.isBlank(student.getRollNo())) {
				response.setStatus(new EdoApiStatus(STATUS_ERROR, "Please provide a valid username/roll number which will be used for login .."));
				return response;
			}
			
			if(student.getId() == null) {
				//Check if the student with same phone exists
				List<EdoStudent> existingStudent = testsDao.getStudentLogin(student);
				
				if(CollectionUtils.isEmpty(existingStudent)) {
					testsDao.saveStudent(student);
					if(CollectionUtils.isNotEmpty(student.getPackages())) {
						LoggingUtil.logMessage("Adding student login for =>" + student.getId(), LoggingUtil.paymentLogger);
						student.setInstituteId(student.getPackages().get(0).getInstitute().getId().toString());
						student.setToken(CommonUtils.createUniversalToken(student));
						testsDao.saveLogin(student);
					}
				} else {
					//student.setId(existingStudent.get(0).getId());
					response.setStatus(new EdoApiStatus(STATUS_ERROR, "Student already exists with given username/roll number .."));
					return response;
				}
				
				LoggingUtil.logMessage("Student ID is =>" + student.getId(), LoggingUtil.paymentLogger);
				if(student.getId() != null) {
					updateStudentPackages(student);
				}
			} else {
				updateStudentPackages(student);
			}
			completePayment(student, response);
			
			//SMS and email
			//Get institute details
			/*EDOInstitute institute = null;
			if(CollectionUtils.isNotEmpty(student.getPackages()) && student.getPackages().get(0).getInstitute() != null) {
				institute = testsDao.getInstituteById(student.getPackages().get(0).getInstitute().getId());
			}*/
			notifyStudent(student, MAIL_TYPE_SUBSCRIPTION, institute);
			//txManager.commit(txStatus);
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			response.setStatus(new EdoApiStatus(STATUS_ERROR, ERROR_IN_PROCESSING));
			//txManager.rollback(txStatus);
		}
		return response;
	}

	private void notifyStudent(EdoStudent student, String mailType, EDOInstitute institute) {
		EdoMailUtil edoMailUtil = new EdoMailUtil(mailType);
		edoMailUtil.setStudent(student);
		edoMailUtil.setInstitute(institute);
		executor.execute(edoMailUtil);
		
		EdoSMSUtil edoSMSUtil = new EdoSMSUtil(mailType);
		edoSMSUtil.setStudent(student);
		edoSMSUtil.setInstitute(institute);
		executor.execute(edoSMSUtil);
	}

	private void completePayment(EdoStudent student, EdoServiceResponse response) {
		LoggingUtil.logMessage("Transaction ID is =>" + student.getTransactionId(), LoggingUtil.paymentLogger);
		BigDecimal amount = BigDecimal.ZERO;
		for(EDOPackage p: student.getPackages()) {
			if(p.getPrice() != null && StringUtils.equals(student.getExamMode(), "Online")) {
				amount = p.getPrice().add(amount);
			} else if (p.getOfflinePrice() != null) {
				amount = p.getOfflinePrice().add(amount);
			}
		}
		if(!student.getPayment().isOffline()) {
			//Set transaction ID to be unique
			String clientId = null, clientSecret = null, purpose = null;
			if(student.getPackages().get(0).getInstitute().getId() == 22) {
				purpose = "Deeper exams";
				//Use PayU In case of Deeper payment
				String txnid = "T" + student.getTransactionId();
				EdoPaymentStatus paymentStatus = PaymentUtil.getHash(txnid, amount.doubleValue(), student, purpose);
				response.setPaymentStatus(paymentStatus);
				paymentStatus.setPaymentId(txnid);
				student.setPayment(paymentStatus);
				testsDao.updatePaymentId(student);
				return;
			}
			EdoPaymentStatus paymentResponse = PaymentUtil.paymentRequest(amount.doubleValue(), student, student.getTransactionId(), clientId, clientSecret, purpose);
			if(paymentResponse != null && paymentResponse.getPaymentId() != null) {
				student.setPayment(paymentResponse);
				testsDao.updatePaymentId(student);
				LoggingUtil.logMessage("Got the payment Id as =>" + paymentResponse.getPaymentId(), LoggingUtil.paymentLogger);
			}
			response.setPaymentStatus(paymentResponse);
		}
	}

	private void updateStudentPackages(EdoStudent student) {
		testsDao.deleteExistingPackages(student);
		testsDao.createStudentPackage(student);
	}

	public EdoPaymentStatus processPayment(String id, String transactionId, String paymentId, String txStatus) {
		//EdoApiStatus status = new EdoApiStatus();
		EdoPaymentStatus status = new EdoPaymentStatus();
		try {

			List<EdoStudent> studentPackages = testsDao.getStudentByPayment(id);
			EDOInstitute institute = null;
			if (CollectionUtils.isNotEmpty(studentPackages)) {
				EdoStudent edoStudent = new EdoStudent();
				List<EDOPackage> packages = new ArrayList<EDOPackage>();
				boolean incompletePackageFound = false;
				for (EdoStudent student : studentPackages) {
					if (student.getCurrentPackage() != null) {
						packages.add(student.getCurrentPackage());
						if (student.getCurrentPackage().getInstitute() != null) {
							institute = new EDOInstitute();
							institute.setName(student.getCurrentPackage().getInstitute().getName());
						}
						if (!StringUtils.equals(student.getCurrentPackage().getStatus(), "Completed")) {
							incompletePackageFound = true;
						}
					}
					edoStudent.setName(student.getName());
					edoStudent.setPhone(student.getPhone());
					edoStudent.setEmail(student.getEmail());
				}
				if (transactionId != null) {
					edoStudent.setTransactionId(new Integer(StringUtils.removeStart(transactionId, "T")));
				}

				if (incompletePackageFound) {
					String clientId = null, clientSecret = null;
					boolean validPayment = false;
					if(studentPackages.get(0).getCurrentPackage().getInstitute().getId() == 22) {
						clientId = EdoPropertyUtil.getProperty("deeper.insta.client.id");
						clientSecret = EdoPropertyUtil.getProperty("deeper.insta.client.secret");
						if(StringUtils.equalsIgnoreCase("success", txStatus)) {
							validPayment = true;
						}
						
					} else {
						validPayment = PaymentUtil.getPaymentStatus(id, clientId, clientSecret, status);
					}
					
					if (validPayment) {
						EdoPaymentStatus paymentStatus = new EdoPaymentStatus();
						paymentStatus.setPaymentId(id);
						paymentStatus.setResponseText("Completed");
						paymentStatus.setOffline(false);
						testsDao.updatePayment(paymentStatus);

						edoStudent.setPackages(packages);
						edoStudent.setPayment(paymentStatus);
						notifyStudent(edoStudent, MAIL_TYPE_ACTIVATED, institute);
					} else {
						EdoPaymentStatus paymentStatus = new EdoPaymentStatus();
						paymentStatus.setPaymentId(id);
						paymentStatus.setResponseText("Failed");
						testsDao.updatePayment(paymentStatus);
					}

				}
			}
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			LoggingUtil.logMessage(ExceptionUtils.getStackTrace(e), LoggingUtil.paymentLogger);
			status.setStatusCode(STATUS_ERROR);
			status.setResponseText(ERROR_IN_PROCESSING);
		}
		return status;
	}

	public EdoPaymentStatus completePayment(EdoTest test, EdoStudent student) {
		EdoPaymentStatus status = new EdoPaymentStatus();
		if(test == null || test.getId() == null || student == null || student.getId() == null) {
			status.setStatusCode(STATUS_ERROR);
			status.setResponseText(ERROR_INCOMPLETE_REQUEST);
			return status;
		}
		try {
			EdoStudent existing = testsDao.getStudentById(student.getId());
			if(existing == null) {
				status.setStatusCode(STATUS_ERROR);
				status.setResponseText(ERROR_STUDENT_NOT_FOUND);
				return status;
			}
			EDOPackage pkg = testsDao.getTestPackage(test.getId());
			if(pkg != null) {
				List<EDOPackage> packages = new ArrayList<EDOPackage>();
				packages.add(pkg);
				EdoPaymentStatus payment = new EdoPaymentStatus();
				payment.setMode("Online");
				payment.setOffline(false);
				existing.setPayment(payment);
				existing.setPackages(packages);
				existing.setExamMode(student.getExamMode());
				updateStudentPackages(existing);
				//For Deeper go to register page again
				if(pkg.getInstitute() != null && pkg.getInstitute().getId() == 22) {
					payment.setPaymentUrl(EdoPropertyUtil.getProperty(EdoPropertyUtil.HOST_NAME) + "registration/deeper.html");
					return payment;
				}
				EdoServiceResponse response = new EdoServiceResponse();
				completePayment(existing, response);
				return response.getPaymentStatus();
			}
			
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e), LoggingUtil.paymentLogger);
		}
		return null;
	}

	public EdoFile getStudentImage(Integer studentId) {
		try {
			EdoFile file = new EdoFile();
			EdoStudent student = testsDao.getStudentById(studentId);
			if(student == null) {
				return null;
			}
			if(StringUtils.isNotBlank(student.getProfilePic())) {
				InputStream is = new FileInputStream(student.getProfilePic());
				file.setContent(is);
				file.setFileName("profilePic" + "." + CommonUtils.getFileExtension(student.getProfilePic()));
			}
			return file;
		} catch (Exception e) {
			LoggingUtil.logMessage(ExceptionUtils.getStackTrace(e));
		}
		return null;
	}

	public EdoServiceResponse getAllSubjects(EdoServiceRequest request) {
		EdoServiceResponse response = new EdoServiceResponse();
		try {
			Integer instituteId = null;
			if(request.getInstitute() != null && request.getInstitute().getId() != null) {
				instituteId = request.getInstitute().getId();
			}
			response.setSubjects(testsDao.getAllSubjects(instituteId));
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
		}
		return response;
	}

	public EdoServiceResponse getNextQuestion(EdoServiceRequest request) {
		EdoServiceResponse response = new EdoServiceResponse();
		try {
			request.getQuestion().setQuestionNumber(1);
			List<EdoQuestion> questions = testsDao.getNextQuestion(request.getQuestion());
			if(CollectionUtils.isNotEmpty(questions)) {
				EdoQuestion question = questions.get(0);
				if(question != null) {
					CommonUtils.setQuestionURLs(question);
					prepareMatchTypeQuestion(question);
					QuestionParser.fixQuestion(question);
					question.setChapter(request.getQuestion().getChapter());
					question.setSubjectId(request.getQuestion().getSubjectId());
					question.setQuestionNumber(1);
				}
				response.setQuestion(question);
			}
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
		}
		return response;
	}

	public EdoServiceResponse submitAnswer(EdoServiceRequest request) {
		EdoServiceResponse response = new EdoServiceResponse();
		try {
			EdoQuestion question = request.getQuestion();
			if(question != null) {
				if(StringUtils.equals(QUESTION_TYPE_MATCH, question.getType())) {
					CommonUtils.setComplexAnswer(question);
				}
				Float ans = CommonUtils.calculateAnswer(question, question);
				if(ans != null) {
					question.setMarks(new BigDecimal(ans));
				}
				response.setQuestion(question);
			}
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
		}
		return response;
	}

	public EdoServiceResponse raiseDoubt(EdoServiceRequest request, InputStream data, FormDataContentDisposition fileDataDetails) {
		EdoServiceResponse response = CommonUtils.initResponse();
		Session session = null;
		try {
			if(request.getTest() != null && request.getTest().getCurrentQuestion() != null && request.getTest().getCurrentQuestion().getFeedback() != null) {
				EdoQuestion currQ = request.getTest().getCurrentQuestion();
				currQ.getFeedback().setFeedback(CommonUtils.escapeQuotes(currQ.getFeedback().getFeedback()));
				if(StringUtils.isBlank(request.getRequestType())) {
					if(currQ.getId() == null && (currQ.getFeedback().getId() != null || (request.getFeedback() != null && request.getFeedback().getId() != null) )) {
						request.setRequestType("video");
					}
				}
			}
			
			if(!StringUtils.equals("video", request.getRequestType())) {
				EdoQuestion currentQuestion = request.getTest().getCurrentQuestion();
				if(currentQuestion != null) {
					//Fix for Android bug
					if(currentQuestion.getQn_id() != null && currentQuestion.getQn_id() > currentQuestion.getId()) {
						currentQuestion.setId(currentQuestion.getQn_id());
					}
					
					if(currentQuestion.getChapter() == null || currentQuestion.getChapter().getChapterId() == null || currentQuestion.getSubjectId() == null) {
						if(currentQuestion.getId() != null) {
							List<EdoQuestion> question = testsDao.getNextQuestion(currentQuestion);
							if(CollectionUtils.isNotEmpty(question)) {
								currentQuestion.setSubjectId(question.get(0).getSubjectId());
								currentQuestion.setChapter(question.get(0).getChapter());
							}
						}
					}
					EdoTestStudentMap map = new EdoTestStudentMap();
					map.setTest(request.getTest());
					map.setStudent(request.getStudent());
					testsDao.addQuestionQuery(map);
					addDoubtFile(data, fileDataDetails, map);
				} 
			} else {
				//Video doubt
				session = this.sessionFactory.openSession();
				EdoFeedback feedback = request.getFeedback();
				if(feedback == null) {
					feedback = request.getTest().getCurrentQuestion().getFeedback();
				}
				List<EdoVideoLecture> lectures = session.createCriteria(EdoVideoLecture.class).add(Restrictions.eq("id", feedback.getId())).list();
				if(CollectionUtils.isNotEmpty(lectures)) {
					EdoVideoLecture lecture = lectures.get(0);
					EdoTestStudentMap map = new EdoTestStudentMap();
					EdoTest test = new EdoTest();
					EdoQuestion currentQuestion = new EdoQuestion();
					if (StringUtils.equalsIgnoreCase("DLPVIDEO", lecture.getType())) {
						List<EdoSubject> subjects = testsDao.getDlpContentSubject(lecture.getId());
						if(CollectionUtils.isNotEmpty(subjects)) {
							EdoSubject edoSubject = subjects.get(0);
							if(edoSubject != null) {
								currentQuestion.setSubjectId(edoSubject.getId());
								if(edoSubject.getChapterId() != null) {
									EdoChapter chapter = new EdoChapter();
									chapter.setChapterId(edoSubject.getChapterId());
									currentQuestion.setChapter(chapter);
								}
							}
						}
					} else if(lecture.getSubjectId() != null) {
						currentQuestion.setSubjectId(lecture.getSubjectId());
					}
					feedback.setId(lecture.getId());
					currentQuestion.setFeedback(feedback);
					test.setCurrentQuestion(currentQuestion);
					map.setTest(test);
					map.setStudent(request.getStudent());
					testsDao.addQuestionQuery(map);
					if(map.getMapId() != null && data != null) {
						addDoubtFile(data, fileDataDetails, map);
					}
				}
			}
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			response.setStatus(new EdoApiStatus(-111, ERROR_IN_PROCESSING));
		} finally {
			CommonUtils.closeSession(session);
		}
		return response;
	}

	private void addDoubtFile(InputStream data, FormDataContentDisposition fileDataDetails, EdoTestStudentMap map) {
		if(map.getMapId() != null && data != null) {
			LoggingUtil.logMessage("Adding file attachment to doubt " + map.getMapId());
			CommonUtils.saveFile(data, DOUBTS_PATH, map.getMapId() + "_" + fileDataDetails.getFileName());
			EdoFeedback feedback = new EdoFeedback();
			feedback.setId(map.getMapId());
			feedback.setAttachment(DOUBTS_PATH + map.getMapId() + "_" + fileDataDetails.getFileName());
			testsDao.updateDoubtFile(feedback);
		}
	}

	public EdoServiceResponse getSolved(EdoServiceRequest request) {
		if(request.getTest() == null || request.getStudent() == null || request.getStudent().getId() == null) {
			EdoApiStatus edoApiStatus = new EdoApiStatus(-111, ERROR_INVALID_PROFILE);
			return new EdoServiceResponse(edoApiStatus);
		}
		Session session = null;
		EdoServiceResponse response = new EdoServiceResponse();
		try {
			session = this.sessionFactory.openSession();
			List<EdoAnswerEntity> answers = session.createCriteria(EdoAnswerEntity.class)
					.add(Restrictions.eq("studentId", request.getStudent().getId()))
					.add(Restrictions.eq("testId", request.getTest().getId()))
					.list();
		
			if(CollectionUtils.isNotEmpty(answers)) {
				EdoTest test = new EdoTest();
				List<EdoQuestion> solved = new ArrayList<EdoQuestion>();
				Integer solvedCount = 0;
				for(EdoAnswerEntity answer: answers) {
					EdoQuestion q = new EdoQuestion();
					q.setId(answer.getQuestionId());
					q.setAnswer(answer.getOptionSelected());
					if(StringUtils.isNotBlank(answer.getOptionSelected())) {
						solvedCount ++;
					}
					if(StringUtils.contains(answer.getOptionSelected(), "-")) {
						EdoQuestion original = testsDao.getQuestion(answer.getQuestionId());
						if(original != null && StringUtils.equals(QUESTION_TYPE_MATCH, original.getType())) {
							q = CommonUtils.getComplexAnswer(q);
						}
					}
					q.setTimeSpent(answer.getTimeTaken());
					q.setFlagged(answer.getFlagged());
					solved.add(q);
				}
				test.setTest(solved);
				test.setSolvedCount(solvedCount);
				List<EdoTestStatusEntity> maps = /*testsDao.getTestStatus(inputMap)*/ session.createCriteria(EdoTestStatusEntity.class)
						.add(Restrictions.eq("testId", request.getTest().getId()))
						.add(Restrictions.eq("studentId", request.getStudent().getId()))
						.list();
				if(CollectionUtils.isNotEmpty(maps)) {
					if(maps.get(0).getTimeLeft() != null && maps.get(0).getTimeLeft() > 0) {
						test.setMinLeft(maps.get(0).getTimeLeft() / 60);
						test.setSecLeft(maps.get(0).getTimeLeft() % 60);
					}
				}
				response.setTest(test);
			}
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
		} finally {
			CommonUtils.closeSession(session);
		}
		return response;
	}

	public EdoServiceResponse uploadRecording(Integer sessionId, InputStream data, Integer classroomId) {
		EdoServiceResponse response = new EdoServiceResponse();
		//EdoApiStatus status = new EdoApiStatus();
		Session session = null;
		try {
			/*if(data.available() <= 0) {
				response.setStatus(new EdoApiStatus(-111, ERROR_INCOMPLETE_REQUEST));
				return response;
			}*/
			session = this.sessionFactory.openSession();
			Transaction tx = session.beginTransaction();
			List<EdoLiveSession> sessions = session.createCriteria(EdoLiveSession.class).add(Restrictions.eq("id", sessionId))
			 								.list();
			 if(CollectionUtils.isEmpty(sessions)) {
				 //Create new
				 /*EdoLiveSession live = createLiveSession(classroomId, "Live session_" + new Date().getTime());
				 session.persist(live);
				 sessionId = live.getId();*/
			 } else {
				 EdoLiveSession live = sessions.get(0);
				 if(!StringUtils.equals("Active", live.getStatus())) {
					 //Not active.. so create new with same name
					 live.setStatus("Active");
					 //live = createLiveSession(classroomId, live.getSessionName());
					 //session.persist(live);
					 sessionId = live.getId();
				 } else {
					 live.setLastUpdated(new Date());
				 }
				 
			 }
			String path = VIDEOS_PATH + sessionId + "/";
			File folder = new File(path);
			Integer noOfFiles = null;
			if (!folder.exists()) {
				boolean mkdirResult = folder.mkdirs();
				LoggingUtil.logMessage("Directory created for " + folder.getAbsolutePath() + " result is " + mkdirResult, LoggingUtil.videoLogger);
				noOfFiles = 0;
			}
			if(noOfFiles == null) {
				File[] fileList = folder.listFiles();
				if(ArrayUtils.isNotEmpty(fileList)) {
					noOfFiles = fileList.length;
				} else {
					noOfFiles = 0;
				}
			}
			String filePath = path + "video" + (noOfFiles + 1) + ".webm";
			FileOutputStream fileOutputStream = new FileOutputStream(filePath);
			//IOUtils.copy(data, fileOutputStream);
			
			int read;
			byte[] bytes = new byte[1024];

			while ((read = data.read(bytes)) != -1) {
				fileOutputStream.write(bytes, 0, read);
			}
			
			fileOutputStream.close();
			//Check size of file saved
			File savedFile = new File(filePath);
			if(savedFile.exists()) {
				double length = savedFile.length();
				if(length > 10000) {
					//If less than 10 KB, ignore it
					//Update meta data file
					FileWriter fileWriter = new FileWriter(path + "list.txt", true); //Set true for append mode
				    PrintWriter printWriter = new PrintWriter(fileWriter);
				    printWriter.println("file '" + filePath + "'");  //New line
				    printWriter.close();
				    LoggingUtil.logMessage("Uploaded file of " + length + " bytes at " + filePath + " for session " + sessionId, LoggingUtil.videoLogger);
				}
			} 
			
		    tx.commit();
		    
		    List<EDOPackage> packages = new ArrayList<EDOPackage>();
		    EDOPackage currPackage = new EDOPackage();
		    currPackage.setId(sessionId);
			packages.add(currPackage);
		    response.setPackages(packages);

		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			response.setStatus(new EdoApiStatus(-111, ERROR_IN_PROCESSING));
		} finally {
			CommonUtils.closeSession(session);
		}
		return response;
	}

	private EdoLiveSession createLiveSession(EDOPackage channel, EdoServiceRequest request, EdoStudent host) {
		EdoLiveSession live = new EdoLiveSession();
		live.setSessionName(channel.getName());
		live.setClassroomId(channel.getId());
		live.setStartDate(CommonUtils.parseDate("yyyy-MM-dd HH:mm", request.getStartTime()));
		live.setEndDate(CommonUtils.parseDate("yyyy-MM-dd HH:mm", request.getEndTime()));
		if(request.getStudent() != null && host != null) {
			live.setCreatedBy(request.getStudent().getId());
		}
		live.setCreatedDate(new Date());
		live.setLastUpdated(new Date());
		live.setStatus("Active");
		return live;
	}

	public EdoServiceResponse startLiveSession(EdoServiceRequest request) {
		EdoServiceResponse response = new EdoServiceResponse();
		if(request.getStudent() == null || request.getStudent().getCurrentPackage() == null) {
			response.setStatus(new EdoApiStatus(-111, ERROR_INCOMPLETE_REQUEST));
			return response;
		}
		Session session = null;
		try {
			session = this.sessionFactory.openSession();
			Transaction tx = session.beginTransaction();
			EDOPackage channel = request.getStudent().getCurrentPackage();
			if(request.getStartTime() == null || request.getStartTime() == null) {
				response.setStatus(new EdoApiStatus(-111, "Start and end time is mandatory"));
				return response;
			}
			/*if(DateUtils.isSameDay(channel.getFromDate(), channel.getToDate())) {
				response.setStatus(new EdoApiStatus(-111, "Start and end time cannot be of different dates"));
				return response;
			}*/
			/*if(channel.getFromDate().getTime() >= channel.getToDate().getTime()) {
				response.setStatus(new EdoApiStatus(-111, "Start time cannot be more than end time"));
				return response;
			}*/
			//Get presenter
			EdoStudent host = testsDao.getStudentById(request.getStudent().getId());
			EdoLiveSession live = createLiveSession(channel, request, host);
			//Call Impartus API
			List<EdoLiveToken> tokens = session.createCriteria(EdoLiveToken.class).addOrder(org.hibernate.criterion.Order.desc("id"))
					.add(Restrictions.ge("lastUpdated", DateUtils.addHours(new Date(), -2)))
					.setMaxResults(1).list();
			String tokenString = null;
			if(CollectionUtils.isEmpty(tokens)) {
				EdoImpartusResponse tokenResponse = EdoLiveUtil.adminLogin();
				if(tokenResponse == null) {
					response.setStatus(new EdoApiStatus(-111, ERROR_IN_PROCESSING));
					LoggingUtil.logMessage("Could not generate token for live classroom .. " + live.getSessionName());
					return response;
				}
				EdoLiveToken token = new EdoLiveToken();
				token.setLastUpdated(new Date());
				token.setToken(tokenResponse.getToken());
				tokenString = tokenResponse.getToken();
				session.persist(token);
			} else {
				tokenString = tokens.get(0).getToken();
			}
			
			EDOPackage pkg = testsDao.getPackage(channel.getId());
			//Create course
			EdoImpartusResponse impartusResponse = EdoLiveUtil.createCourse(tokenString, pkg);
			if(!impartusResponse.isSuccess()) {
				LoggingUtil.logMessage("Could not create course for live classroom .. " + live.getSessionName());
				return response;
			}
			
			if(host == null) {
				host = new EdoStudent();
				host.setId(request.getStudent().getId());
				if(request.getStudent().getName() == null) {
					host.setName("Admin");
				}
			}
			impartusResponse = EdoLiveUtil.createUser(tokenString, host, 2);
			if(!impartusResponse.isSuccess()) {
				LoggingUtil.logMessage("Could not create presenter for live classroom .. " + live.getSessionName());
				return response;
			}
			
			impartusResponse = EdoLiveUtil.createLiveSession(tokenString, live, host);
			if(!impartusResponse.isSuccess() || StringUtils.isBlank(impartusResponse.getLiveStreamUrl2())) {
				LoggingUtil.logMessage("Could not create live schedule for live classroom .. " + live.getSessionName());
				return response;
			}
			
			live.setLiveUrl(impartusResponse.getLiveStreamUrl2());
			live.setScheduleId(impartusResponse.getScheduleId());
			live.setRecording_url(impartusResponse.getPlaybackUrl2());
			live.setHlsUrl(impartusResponse.getHlsPlaybackUrl());
			live.setPptUrl(impartusResponse.getPptUrl());
			
			//If proctoring session..make respective settings
			if(StringUtils.equals("PROCTORING", request.getRequestType()) && live.getScheduleId() != null) {
				EdoLiveUtil.scheduleSettings(tokenString, live.getScheduleId());
			}
			
			impartusResponse = EdoLiveUtil.join(tokenString, channel.getId(), request.getStudent().getId());
			if(!impartusResponse.isSuccess()) {
				LoggingUtil.logMessage("Could not join course .. " + live.getSessionName());
				return response;
			}
			
			impartusResponse = EdoLiveUtil.ssoToken(request.getStudent().getId());
			if(!impartusResponse.isSuccess()) {
				LoggingUtil.logMessage("Could not get SSO token .. " + live.getSessionName());
				return response;
			}
			
			session.persist(live);
			List<EDOPackage> packages = new ArrayList<EDOPackage>();
			channel.setId(live.getId());
			channel.setVideoUrl(live.getLiveUrl() + "&token=" + impartusResponse.getToken());
			packages.add(channel);
			response.setPackages(packages);
			
			tx.commit();
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			response.setStatus(new EdoApiStatus(-111, ERROR_IN_PROCESSING));
		} finally {
			CommonUtils.closeSession(session);
		}
		return response;
	}

	public EdoServiceResponse getLiveSessions(EdoServiceRequest request) {
		EdoServiceResponse response = new EdoServiceResponse();
		if(request.getStudent() == null || request.getStudent().getCurrentPackage() == null) {
			response.setStatus(new EdoApiStatus(-111, ERROR_INCOMPLETE_REQUEST));
			return response;
		}
		try {
			List<EDOPackage> liveSessions = testsDao.getLiveSessions(request.getStudent().getCurrentPackage());
			if(CollectionUtils.isNotEmpty(liveSessions)) {
				for(EDOPackage liveSession: liveSessions) {
					if(liveSession.getFromDate() != null && liveSession.getFromDate().compareTo(new Date()) > 0) {
						liveSession.setStatus("Pending");
						liveSession.setTimeLeft(liveSession.getFromDate().getTime() -  new Date().getTime());
					} else if (liveSession.getToDate() != null && liveSession.getToDate().compareTo(new Date()) >= 0) {
						liveSession.setStatus("Active");
					} else {
						liveSession.setStatus("Completed");
					}
				}
			}
			response.setPackages(liveSessions);
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			response.setStatus(new EdoApiStatus(-111, ERROR_IN_PROCESSING));
		} finally {
			
		}
		return response;
	}

	public EdoServiceResponse finishRecording(EdoServiceRequest request) {
		EdoServiceResponse response = new EdoServiceResponse();

		if (request.getStudent() == null || request.getStudent().getCurrentPackage() == null) {
			response.setStatus(new EdoApiStatus(-111, ERROR_INCOMPLETE_REQUEST));
			return response;
		}
		Session session = null;
		try {
			EDOPackage currentPackage = request.getStudent().getCurrentPackage();
			Integer sessionId = currentPackage.getId();
			
			session = this.sessionFactory.openSession();
			List<EdoLiveSession> sessions = session.createCriteria(EdoLiveSession.class).add(Restrictions.eq("id", sessionId)).list();
			
			if (CollectionUtils.isNotEmpty(sessions)) {
				Transaction tx = session.beginTransaction();
				EdoLiveSession live = sessions.get(0);
				//Float fileSize = VideoUtil.downloadRecordedFile(live.getClassroomId(), live.getId());
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
					LoggingUtil.logMessage("Exported video " + live.getId() + " successfully ..", LoggingUtil.videoLogger);
				} else {*/
					//Check if file can be fixed
					EdoVideoLecture lecture = VideoExportScheduler.callFixFileApi(live.getClassroomId() + "-" + live.getId());
					if(lecture != null && lecture.getSize() != null) {
						live.setStatus("Completed");
						live.setRecording_url(lecture.getVideo_url());
						live.setFileSize(lecture.getSize().floatValue());
					} else {
						live.setStatus("Failed");
						LoggingUtil.logMessage("Could not export video " + live.getId() + " successfully ..", LoggingUtil.videoLogger);
					}
				//}
				tx.commit();
				EDOInstitute institute = new EDOInstitute();
				EDOPackage pkg = testsDao.getPackage(live.getClassroomId());
				if(pkg != null && pkg.getInstitute() != null && live.getFileSize() != null && live.getFileSize() > 0) {
					institute.setId(pkg.getInstitute().getId());
					BigDecimal bd = CommonUtils.calculateStorageUsed(live.getFileSize());
					institute.setStorageQuota(bd.doubleValue());
					//Deduct quota from institute
					LoggingUtil.logMessage("Deducting quota " + institute.getStorageQuota() + " GBs from " + institute.getId(), LoggingUtil.videoLogger);
					testsDao.deductQuota(institute);
				}
				currentPackage.setVideoUrl("view-session.php?sessionId=" + sessionId + "&sessionName=" + live.getSessionName());
				List<EDOPackage> packages = new ArrayList<EDOPackage>();
				packages.add(currentPackage);
				response.setPackages(packages);
			}

		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			response.setStatus(new EdoApiStatus(-111, ERROR_IN_PROCESSING));
		} finally {

		}
		return response;
	}

	public EdoServiceResponse getSession(EdoServiceRequest request) {
		EdoServiceResponse response = new EdoServiceResponse();
		if(request.getStudent() == null || request.getStudent().getCurrentPackage() == null) {
			response.setStatus(new EdoApiStatus(-111, ERROR_INCOMPLETE_REQUEST));
			return response;
		}
		try {
			
			EDOPackage liveSession = null;
			if(StringUtils.equals(request.getRequestType(), "package")) {
				request.getStudent().getCurrentPackage().setStatus("Active");
				List<EDOPackage> liveSessions = testsDao.getLiveSessions(request.getStudent().getCurrentPackage());
				if(CollectionUtils.isNotEmpty(liveSessions)) {
					liveSession = liveSessions.get(0);
				}
			} else {
				liveSession = testsDao.getLiveSession(request.getStudent().getCurrentPackage().getId());
			}
			
			if(liveSession != null) {
				liveSession.setVideoUrl(prepareVimeoEmbedLink(liveSession.getVideoUrl()));
				List<EDOPackage> pgs = new ArrayList<EDOPackage>();
				pgs.add(liveSession);
				response.setPackages(pgs);
			}
			
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			response.setStatus(new EdoApiStatus(-111, ERROR_IN_PROCESSING));
		} finally {
			
		}
		return response;
	}

	private String prepareVimeoEmbedLink(String url) {
		return StringUtils.replace(url, "vimeo.com", "player.vimeo.com/video");
	}

	public EdoServiceResponse uploadVideo(InputStream videoData, String title, Integer instituteId, Integer subjectId, Integer packageId, Integer topicId, 
			String keywords, InputStream questionFile, String questionFileName, String classrooms, String type) {
		EdoServiceResponse response = new EdoServiceResponse();
		//EdoApiStatus status = new EdoApiStatus();
		Session session = null;
		try {
			/*if(data.available() <= 0) {
				response.setStatus(new EdoApiStatus(-111, ERROR_INCOMPLETE_REQUEST));
				return response;
			}*/
			
			//Check for quota first
			EDOInstitute institute = testsDao.getInstituteById(instituteId);
			if(institute != null && institute.getStorageQuota() != null && institute.getStorageQuota() < 0) {
				LoggingUtil.logMessage("Video storage quota exceeded for " + instituteId, LoggingUtil.videoLogger);
				response.setStatus(new EdoApiStatus(-111, "Video storage quota exceeded .. Please upgrade your plan .."));
				return response;
			}
			
			String path = VIDEOS_PATH + "temp/";
			File folder = new File(path);
			Integer noOfFiles = null;
			if (!folder.exists()) {
				boolean mkdirResult = folder.mkdirs();
				LoggingUtil.logMessage("Directory created for " + folder.getAbsolutePath() + " result is " + mkdirResult, LoggingUtil.videoLogger);
				noOfFiles = 0;
			}
			title = StringUtils.replace(title, "/", " ");
			title = StringUtils.replace(title, "\\", " ");
			String filePath = path + title;
			FileOutputStream fileOutputStream = new FileOutputStream(filePath);
			//IOUtils.copy(data, fileOutputStream);
			
			int read;
			byte[] bytes = new byte[1024];

			while ((read = videoData.read(bytes)) != -1) {
				fileOutputStream.write(bytes, 0, read);
			}
			
			fileOutputStream.close();
			//Check size of file saved
			File savedFile = new File(filePath);
			if(savedFile.exists()) {
				double length = savedFile.length();
				LoggingUtil.logMessage("Uploading file " + title + " of " + length + " to vimeo ..", LoggingUtil.videoLogger);
				VimeoResponse vimeoResponse = VideoUtil.uploadFile(filePath, title, title);
				if (vimeoResponse != null && vimeoResponse.getJson() != null && StringUtils.isNotBlank(vimeoResponse.getJson().getString("link"))) {
					session = this.sessionFactory.openSession();
					Transaction tx = session.beginTransaction();
					EdoVideoLecture lectures = new EdoVideoLecture();
					lectures.setVideoName(title);
					lectures.setSubjectId(subjectId);
					lectures.setInstituteId(instituteId);
					lectures.setCreatedDate(new Date());
					lectures.setClassroomId(packageId);
					lectures.setType(type);
					//lectures.setTopicId(topicId);
					if(StringUtils.isNotBlank(keywords)) {
						lectures.setKeywords(StringUtils.removeEnd(keywords, ","));
					}
					String vimeoLink = vimeoResponse.getJson().getString("link");
					//Prepare embed link
					lectures.setVideo_url(prepareVimeoEmbedLink(vimeoLink));
					lectures.setSize(length);
					session.persist(lectures);
					//Save question file if present
					if(questionFile != null) {
						CommonUtils.saveFile(questionFile, EdoConstants.VIDEO_QUESTION_FILE_PATH + lectures.getId() + "/", questionFileName);
						lectures.setQuestionImg(EdoConstants.VIDEO_QUESTION_FILE_PATH + lectures.getId() + "/" + questionFileName);
					}
					//Add keywords to repo
					if(StringUtils.isNotBlank(keywords)) {
						updateKeywords(instituteId, keywords, session);
					}
					
					//Add multiple classrroms (if any)
					if(StringUtils.isNotBlank(classrooms)) {
						String[] classroomArray = StringUtils.split(classrooms, ",");
						if(ArrayUtils.isNotEmpty(classroomArray)) {
							for(String classroom: classroomArray) {
								if(StringUtils.isNotBlank(classroom)) {
									EdoContentMap map = new EdoContentMap();
									map.setContentId(lectures.getId());
									map.setClassroomId(new Integer(classroom));
									map.setChapterId(topicId);
									map.setCreatedDate(new Date());
									session.persist(map);
								}
							}
						}
					}
					
					//Remove the temp file
					boolean delete = savedFile.delete();
					LoggingUtil.logMessage("Deleted the saved file " + delete + " at " + filePath, LoggingUtil.videoLogger);
					tx.commit();
					updateQuota(instituteId, length);
				}
				
			} 
	
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			response.setStatus(new EdoApiStatus(-111, ERROR_IN_PROCESSING));
		} finally {
			CommonUtils.closeSession(session);
		}
		return response;
	}

	private void updateQuota(Integer instituteId, double length) {
		try {
			//Update quota
			EDOInstitute edoInstitute = new EDOInstitute();
			edoInstitute.setId(instituteId);
			BigDecimal bd = CommonUtils.calculateStorageUsed(new Float(length));
			edoInstitute.setStorageQuota(bd.doubleValue());
			//Deduct quota from institute
			LoggingUtil.logMessage("Deducting quota " + edoInstitute.getStorageQuota() + " GBs from " + edoInstitute.getId(), LoggingUtil.videoLogger);
			testsDao.deductQuota(edoInstitute);
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
		}
	}

	private void updateKeywords(Integer instituteId, String keywords, Session session) {
		String[] values = StringUtils.split(keywords, ",");
		if(ArrayUtils.isNotEmpty(values)) {
			for(String value: values) {
				String trimmed = StringUtils.trimToEmpty(value);
				List<EdoKeyword> matching = session.createCriteria(EdoKeyword.class).add(Restrictions.ilike("keyword", trimmed)).list();
				if(CollectionUtils.isEmpty(matching)) {
					EdoKeyword keyword = new EdoKeyword();
					keyword.setInstituteId(instituteId);
					keyword.setKeyword(trimmed);
					keyword.setCreatedDate(new Date());
					session.persist(keyword);
				}
			}
		}
	}

	public EdoServiceResponse getVideoLectures(EdoServiceRequest request) {
		EdoServiceResponse response = new EdoServiceResponse();
		if(/*request.getStudent() == null ||*/ request.getInstitute() == null) {
			response.setStatus(new EdoApiStatus(-111, ERROR_IN_PROCESSING));
			return response;
		}
		try {
			List<EdoVideoLectureMap> videoLectures = testsDao.getVideoLectures(request);
			if(CollectionUtils.isNotEmpty(videoLectures)) {
				for(EdoVideoLectureMap lecture: videoLectures) {
					if(StringUtils.isNotBlank(lecture.getLecture().getKeywords())) {
						String[] values = StringUtils.split(lecture.getLecture().getKeywords(), ",");
						if(ArrayUtils.isNotEmpty(values)) {
							List<EdoKeyword> keywords = new ArrayList<EdoKeyword>();
							for(String value: values) {
								EdoKeyword keyword = new EdoKeyword();
								keyword.setKeyword(value);
								keywords.add(keyword);
							}
							lecture.setKeywords(keywords);
						}
					}
					if(StringUtils.isNotBlank(lecture.getLecture().getQuestionImg())) {
						String hostUrl = EdoPropertyUtil.getProperty(EdoPropertyUtil.HOST_URL);
						lecture.getLecture().setQuestionImg(hostUrl + "getImage/" + lecture.getLecture().getId() + "/" + ATTR_VIDEO_QUESTION);
					}
				}
			}
			response.setLectures(videoLectures);
			response.setSubjects(testsDao.getVideoSubjects(request));
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			response.setStatus(new EdoApiStatus(-111, ERROR_IN_PROCESSING));
		}
		return response;
	}

	public EdoServiceResponse login(EdoServiceRequest request) {
		EdoServiceResponse response = new EdoServiceResponse();
		try {
			
			EdoStudent student = request.getStudent();
			List<EdoStudent> existing = testsDao.getStudentLogin(student);
			if(CollectionUtils.isEmpty(existing)) {
				response.setStatus(new EdoApiStatus(-111, ERROR_NO_STUDENT));
				return response;
			} else if (existing.size() > 1) {
				response.setStatus(new EdoApiStatus(-111, ERROR_INVALID_PROFILE));
				return response;
			} else {
				EdoStudent edoStudent = existing.get(0);
				if (StringUtils.equals(student.getPassword(), edoStudent.getPassword()) || StringUtils.isNotBlank(student.getToken())) {
					response.setStudent(edoStudent);
				} else {
					response.setStatus(new EdoApiStatus(-111, "Username and password incorrect. Please try again."));
					return response;
				}
			}
			
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			response.setStatus(new EdoApiStatus(-111, ERROR_IN_PROCESSING));
		}
		return response;
	}

	public EdoApiStatus updateStudentActivity(EdoServiceRequest request) {
		EdoApiStatus status = new EdoApiStatus();
		if(StringUtils.isBlank(request.getRequestType()) || request.getStudent() == null || request.getFeedback() == null) {
			status.setStatus(-111, ERROR_INCOMPLETE_REQUEST);
			return status;
		}
		if(request.getStudent().getId() == null && StringUtils.isBlank(request.getStudent().getRollNo())) {
			status.setStatus(-111, ERROR_INCOMPLETE_REQUEST);
			return status;
		}
		if(request.getFeedback().getId() == null && request.getFeedback().getVideoId() == null) {
			status.setStatus(-111, ERROR_INCOMPLETE_REQUEST);
			return status;
		}
		
		try {
			
			if(request.getStudent().getId() != null) {
				request.getStudent().setRollNo(null);
			}
			
			if(StringUtils.equals(request.getRequestType(), "UPDATE_WATCH_TIME")) {
				testsDao.updateActivityWatchTime(request);
				return status;
			}
			

			testsDao.saveVideoActiviy(request);
			//Update activity summary
			Integer activityCount = 1;
			Integer watchedTimes = 0;
			if(StringUtils.equals(request.getRequestType(), "VIDEO_ENDED")) {
				watchedTimes = 1;
			}
			Long watchDuration = 0L; 
			EdoFeedback feedback = request.getFeedback();
			if(feedback != null && feedback.getTotalDuration() != null) {
				watchDuration = feedback.getTotalDuration();
			}
			List<EdoFeedback> activiy = testsDao.getStudentActivity(request);
			if(CollectionUtils.isNotEmpty(activiy)) {
				EdoFeedback existing = activiy.get(0);
				if(existing.getActivityCount() != null) {
					activityCount = existing.getActivityCount() + 1;
				}
				if(existing.getFrequency() != null) {
					watchedTimes = existing.getFrequency() + watchedTimes;
				}
				if(existing.getTotalDuration() != null && watchDuration != null) {
					watchDuration = existing.getTotalDuration() + watchDuration;
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
			
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e), LoggingUtil.activityLogger);
		}
		return status;
	}

	public EdoApiStatus updateVideoLecture(EdoServiceRequest request) {
		EdoApiStatus status = new EdoApiStatus();
		if(request.getLecture() == null || request.getLecture().getId() == null) {
			status.setStatus(-111, ERROR_INCOMPLETE_REQUEST);
			return status;
		}
		Session session = null;
		try {
			session = this.sessionFactory.openSession();
			Transaction tx = session.beginTransaction();
			
				List<EdoVideoLecture> lectures = session.createCriteria(EdoVideoLecture.class).add(Restrictions.eq("id", request.getLecture().getId())).list();
				if(CollectionUtils.isEmpty(lectures)) {
					status.setStatus(-111, ERROR_IN_PROCESSING);
					return status;
				}
				EdoVideoLecture lecture = lectures.get(0);
				if(request.getLecture().getDisabled() == 1) {
					lecture.setDisabled(1);
					//Add quota
					EDOInstitute edoInstitute = new EDOInstitute();
					edoInstitute.setId(lecture.getInstituteId());
					if(lecture.getSize() != null) {
						BigDecimal bd = CommonUtils.calculateStorageUsed(new Float(lecture.getSize()));
						edoInstitute.setStorageQuota(bd.doubleValue());
						//Deduct quota from institute
						LoggingUtil.logMessage("Adding quota " + edoInstitute.getStorageQuota() + " GBs from " + edoInstitute.getId(), LoggingUtil.videoLogger);
						testsDao.addQuota(edoInstitute);
					}
				} else if (request.getLecture().getProgress() != null)  {
					//Update only progress
					if(StringUtils.isNotBlank(request.getLecture().getStatus())) {
						lecture.setStatus(request.getLecture().getStatus());
					}
					if(request.getLecture().getProgress() == 100) {
						lecture.setStatus("Completed");
					}
					if(request.getLecture().getProgress() != null && (lecture.getProgress() == null || request.getLecture().getProgress() > lecture.getProgress())) {
						lecture.setProgress(request.getLecture().getProgress());
					}
				} else {
					lecture.setVideoName(request.getLecture().getVideoName());
					if(request.getLecture().getClassroomId() == 0) {
						lecture.setClassroomId(null);
					} else {
						lecture.setClassroomId(request.getLecture().getClassroomId());
					}
					lecture.setSubjectId(request.getLecture().getSubjectId());
					if(StringUtils.isNotBlank(request.getLecture().getKeywords())) {
						lecture.setKeywords(StringUtils.removeEnd(request.getLecture().getKeywords(), ","));
						updateKeywords(lecture.getInstituteId(), request.getLecture().getKeywords(), session);
					}
				}
			
			tx.commit();
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			status.setStatus(-111, ERROR_IN_PROCESSING);
		} finally {
			CommonUtils.closeSession(session);
		}
		return status;
	}

	public EdoServiceResponse getTags(Integer instituteId, String query) {
		Session session = null;
		EdoServiceResponse response = new EdoServiceResponse();
		try {
			session = this.sessionFactory.openSession();
			List<EdoKeyword> keywords = session.createCriteria(EdoKeyword.class)
					.add(Restrictions.eq("instituteId", instituteId))
					.add(Restrictions.like("keyword", query, MatchMode.ANYWHERE))
					.list();
			if(CollectionUtils.isNotEmpty(keywords)) {
				List<EdoSuggestion> suggestions = new ArrayList<EdoSuggestion>();
				for(EdoKeyword keyword: keywords) {
					EdoSuggestion sugg = new EdoSuggestion();
					sugg.setData(keyword.getId().toString());
					sugg.setValue(keyword.getKeyword());
					suggestions.add(sugg);
				}
				response.setSuggestions(suggestions);
			}
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
		} finally {
			CommonUtils.closeSession(session);
		}
		return response;
	}

	public EdoServiceResponse getDeeperRegistration(String rollNo) {
		EdoServiceResponse response = new EdoServiceResponse();
		try {
			boolean unpaidPackageFound = true;
			EdoStudent student = testsDao.getDeeperRegistration(rollNo);
			if(student != null && student.getId() != null) {
				List<EDOPackage> packages = testsDao.getStudentPackages(student.getId());
				if(CollectionUtils.isNotEmpty(packages)) {
					unpaidPackageFound = false;
					for(EDOPackage pkg: packages) {
						if(!StringUtils.equals(pkg.getStatus(), "Completed")) {
							unpaidPackageFound = true;
						}
					}
				}
			}
			if(unpaidPackageFound) {
				response.setStudent(student);
			} else {
				response.setStatus(new EdoApiStatus(-111, "You have already registered for this course. Please login to test.edofox.com"));
			}
			
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			response.setStatus(new EdoApiStatus(-111, ERROR_IN_PROCESSING));
		} finally {
			
		}
		return response;
	}

	public EdoFile getVideo(Integer videoId, String requestType) {
		if(videoId == null) {
			return null;
		}
		Session session = null;
		EdoFile file = null;
		try {
			session = this.sessionFactory.openSession();
			EdoVideoLecture classwork = (EdoVideoLecture) session.createCriteria(EdoVideoLecture.class).add(Restrictions.eq("id", videoId)).uniqueResult();
			if(classwork != null) {
				if(StringUtils.contains(classwork.getVideo_url(), "vimeo")) {
					return VideoUtil.getDownloadUrl(classwork.getVideo_url(), classwork.getVideoName() + ".mp4", classwork.getInstituteId());
				} else if (StringUtils.contains(classwork.getVideo_url(), "streaming.edofox.com")) {
					return VideoUtil.getStreamingUrls(classwork.getVideo_url(), requestType);
				}
			}
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
		} finally {
			CommonUtils.closeSession(session);
		}
		return file;
	}

	public EdoServiceResponse getStudentExams(EdoServiceRequest request) {
		EdoServiceResponse response = new EdoServiceResponse();
		try {
			List<EdoTest> studentExams = testsDao.getStudentExams(request);
			setExams(studentExams);
			response.setExams(studentExams);
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			response.setStatus(new EdoApiStatus(-111, ERROR_IN_PROCESSING));
		}
		return response;
	}

	private void setExams(List<EdoTest> studentExams) {
		if(CollectionUtils.isNotEmpty(studentExams)) {
			for(EdoTest studentExam: studentExams) {
				if(studentExam.getStartDate() != null && studentExam.getEndDate() != null) {
					if(new Date().compareTo(studentExam.getStartDate()) < 0) {
						studentExam.setStatus("PENDING");
						if(DateUtils.isSameDay(studentExam.getStartDate(), new Date())) {
							studentExam.setSecLeft((studentExam.getStartDate().getTime() - new Date().getTime()) / 1000);
						}
					} else if (new Date().compareTo(studentExam.getEndDate()) > 0) {
						//Show expired only if exam is not started today
						if(studentExam.getStartedDate() != null && DateUtils.isSameDay(new Date(), studentExam.getStartedDate())) {
							studentExam.setStatus("ACTIVE");
						} else {
							studentExam.setStatus("EXPIRED");
						}
					} else {
						studentExam.setStatus("ACTIVE");
					}
				}
			}
		}
	}

	public EdoServiceResponse getQuestionAnalysis(EdoServiceRequest request) {
		EdoServiceResponse response = new EdoServiceResponse();
		try {
			List<EdoQuestion> questionCorrectness = testsDao.getQuestionCorrectness(request.getQuestion().getId());
			if(CollectionUtils.isNotEmpty(questionCorrectness)) {
				response.setQuestion(questionCorrectness.get(0));
			}
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
		}
		return response;
	}

	public EdoApiStatus addDeviceId(EdoServiceRequest request) {
		EdoDeviceId deviceIdInput = request.getDeviceId();
		if(deviceIdInput == null) {
			return new EdoApiStatus(-111, ERROR_INCOMPLETE_REQUEST);
		}
		EdoApiStatus status = new EdoApiStatus();
		Session session = null;
		try {
			session = this.sessionFactory.openSession();
			List<EdoDeviceId> deviceId = session.createCriteria(EdoDeviceId.class).add(Restrictions.eq("token", deviceIdInput.getToken())).list();
			if(CollectionUtils.isEmpty(deviceId)) {
				Transaction tx = session.beginTransaction();
				deviceIdInput.setCreatedDate(new Date());
				session.persist(deviceIdInput);
				tx.commit();
			}
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			status.setStatus(-111, ERROR_IN_PROCESSING);
		} finally {
			CommonUtils.closeSession(session);
		}
		return status;
	}

	public EdoServiceResponse getStudentSubjects(EdoServiceRequest request) {
		EdoServiceResponse response = new EdoServiceResponse();
		try {
			response.setSubjects(testsDao.getStudentSubjects(request));
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			response.setStatus(new EdoApiStatus(-111, ERROR_IN_PROCESSING));
		}
		return response;
	}
	
	public EdoServiceResponse getStudentChapters(EdoServiceRequest request) {
		EdoServiceResponse response = new EdoServiceResponse();
		try {
			EdoSubject studentChapters = testsDao.getStudentChapters(request);
			List<EdoSubject> subjects = new ArrayList<EdoSubject>();
			subjects.add(studentChapters);
			response.setSubjects(subjects);
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			response.setStatus(new EdoApiStatus(-111, ERROR_IN_PROCESSING));
		}
		return response;
	}
	
	public EdoServiceResponse getChapterContent(EdoServiceRequest request) {
		EdoServiceResponse response = new EdoServiceResponse();
		try {
			List<EdoVideoLectureMap> chapterContent = testsDao.getChapterContent(request);
			if(CollectionUtils.isNotEmpty(chapterContent)) {
				for(EdoVideoLectureMap map: chapterContent) {
					if(map.getLecture() != null && StringUtils.equals("DOC", map.getLecture().getType())) {
						//Temp changes for reliance doc URL
						if(map.getLecture().getInstituteId() != null && map.getLecture().getInstituteId().intValue() == 9) {
							if(!StringUtils.contains(map.getLecture().getVideo_url(), "reliancedlp")) {
								map.getLecture().setVideo_url("/var/www/reliancedlp.edofox.com/public_html/" + map.getLecture().getVideo_url());
							}
						}
						map.getLecture().setVideo_url(CommonUtils.prepareUrl(map.getLecture().getVideo_url()));
					}
				}
			}
			response.setLectures(chapterContent);
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			response.setStatus(new EdoApiStatus(-111, ERROR_IN_PROCESSING));
		}
		return response;
	}

	public EdoServiceResponse getChapterExams(EdoServiceRequest request) {
		EdoServiceResponse response = new EdoServiceResponse();
		try {
			List<EdoTest> studentExams = testsDao.getChapterExams(request);
			setExams(studentExams);
			response.setExams(studentExams);
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			response.setStatus(new EdoApiStatus(-111, ERROR_IN_PROCESSING));
		}
		return response;
	}

	public EdoServiceResponse joinSession(EdoServiceRequest request) {
		EdoServiceResponse response = new EdoServiceResponse();
		EDOPackage channel = request.getStudent().getCurrentPackage();
		if(request.getStudent() == null || channel == null) {
			response.setStatus(new EdoApiStatus(-111, ERROR_INCOMPLETE_REQUEST));
			return response;
		}
		Session session = null;
		try {
			session = this.sessionFactory.openSession();
			Transaction tx = session.beginTransaction();
			Integer sessionId = channel.getId();
			List<EdoLiveSession> sessions = session.createCriteria(EdoLiveSession.class).add(Restrictions.eq("id", sessionId))
						.list();
			EdoLiveSession live = null;
			if(CollectionUtils.isEmpty(sessions)) {
				response.setStatus(new EdoApiStatus(-111, "No such live session found"));
				return response;
			} 
			live = sessions.get(0);
			//Call Impartus API
			List<EdoLiveToken> tokens = session.createCriteria(EdoLiveToken.class)
					.addOrder(org.hibernate.criterion.Order.desc("id"))
					.add(Restrictions.ge("lastUpdated", DateUtils.addHours(new Date(), -2)))
					.setMaxResults(1).list();
			String tokenString = null;
			if(CollectionUtils.isEmpty(tokens)) {
				EdoImpartusResponse tokenResponse = EdoLiveUtil.adminLogin();
				if(tokenResponse == null) {
					response.setStatus(new EdoApiStatus(-111, ERROR_IN_PROCESSING));
					LoggingUtil.logMessage("Could not generate token for live classroom .. " + live.getSessionName());
					return response;
				}
				EdoLiveToken token = new EdoLiveToken();
				token.setLastUpdated(new Date());
				token.setToken(tokenResponse.getToken());
				tokenString = tokenResponse.getToken();
				session.persist(token);
			} else {
				tokenString = tokens.get(0).getToken();
			}
			
			//Create user if not present
			EdoStudent host = testsDao.getStudentById(request.getStudent().getId());
			Integer userType = 2;
			if(host != null) {
				if(host.getRollNo() != null) {
					List<EdoStudent> existing = testsDao.getStudentLogin(host);
					if(CollectionUtils.isNotEmpty(existing) && StringUtils.equals("Teacher", existing.get(0).getAccessType())) {
						host.setAccessType(existing.get(0).getAccessType());
					}
				}
				if(StringUtils.isBlank(host.getAccessType())) {
					userType = 6;
				}
			} else {
				host = request.getStudent();
				host.setName("Admin");
			}
			
			
			EdoImpartusResponse impartusResponse = EdoLiveUtil.createUser(tokenString, host, userType);
			if(!impartusResponse.isSuccess()) {
				LoggingUtil.logMessage("Could not create user for live classroom .. " + live.getSessionName());
				return response;
			}
			
			//If Teacher..add professor
			if(userType == 2 && request.getStudent().getId() != null && live.getCreatedBy() != null && request.getStudent().getId().intValue() != live.getCreatedBy().intValue()) {
				LoggingUtil.logMessage("Adding professor access to user " + request.getStudent().getId() + " for  " + live.getSessionName() + " started by  " + live.getCreatedBy());
				EdoImpartusResponse joinResponse = EdoLiveUtil.addProfressorAccess(tokenString, live.getClassroomId(), request.getStudent().getId(), live.getCreatedBy());
				if(joinResponse == null || !joinResponse.isSuccess()) {
					LoggingUtil.logMessage("Could not add professor .. " + live.getSessionName());
				}
			}
			
			impartusResponse = EdoLiveUtil.join(tokenString, live.getClassroomId(), request.getStudent().getId());
			if(!impartusResponse.isSuccess()) {
				LoggingUtil.logMessage("Could not join course .. " + live.getSessionName());
				return response;
			}
			
			impartusResponse = EdoLiveUtil.ssoToken(request.getStudent().getId());
			if(!impartusResponse.isSuccess()) {
				LoggingUtil.logMessage("Could not get SSO token .. " + live.getSessionName());
				return response;
			}
			
			session.persist(live);
			List<EDOPackage> packages = new ArrayList<EDOPackage>();
			channel.setId(live.getId());
			if(live.getEndDate() != null && live.getEndDate().compareTo(new Date()) > 0) {
				channel.setVideoUrl(live.getLiveUrl() + "&token=" + impartusResponse.getToken());
			} else {
				channel.setVideoUrl(live.getRecording_url() + "&token=" + impartusResponse.getToken());
			}
			
			packages.add(channel);
			response.setPackages(packages);
			
			tx.commit();
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			response.setStatus(new EdoApiStatus(-111, ERROR_IN_PROCESSING));
		} finally {
			CommonUtils.closeSession(session);
		}
		return response;
	}

	public EdoServiceResponse getStudentPerformance(EdoServiceRequest request) {
		EdoServiceResponse response = new EdoServiceResponse();
		try {
			if(request.getStudent() != null && request.getStudent().getId() != null) {
				List<EdoTest> tests = testsDao.getStudentPerformance(request.getStudent());
				List<EdoTestStudentMap> subjectPerformance = testsDao.getSubjectwisePerformanceStudent(request.getStudent().getId());
				if(CollectionUtils.isNotEmpty(tests)) {
					for(EdoTest test: tests) {
						if(CollectionUtils.isNotEmpty(subjectPerformance)) {
							for(EdoTestStudentMap sa: subjectPerformance) {
								if(test.getId().intValue() == sa.getTest().getId().intValue()) {
									if(test.getAnalysis() == null) {
										EDOTestAnalysis analysis = new EDOTestAnalysis();
										analysis.setSubjectAnalysis(new ArrayList<EdoStudentSubjectAnalysis>());
										test.setAnalysis(analysis);
									}
									if(test.getAnalysis() != null && test.getAnalysis().getSubjectAnalysis() == null) {
										test.getAnalysis().setSubjectAnalysis(new ArrayList<EdoStudentSubjectAnalysis>());
									}
									EdoStudentSubjectAnalysis subjectAnalysis = sa.getSubjectScore();
									test.getAnalysis().getSubjectAnalysis().add(subjectAnalysis);
									/*if(!CommonUtils.isIntEnabled(test.getShowRank())) {
										test.setRank(null);
									}*/
								}
							}
						}
					}
				}
				response.setExams(tests);
			}
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			response.setStatus(new EdoApiStatus(-111, ERROR_IN_PROCESSING));
		}
		return response;
	}

	public EdoApiStatus updateStudentTestActivity(EdoServiceRequest request) {
		EdoApiStatus status = new EdoApiStatus();
		if(StringUtils.isBlank(request.getRequestType()) || request.getStudent() == null || request.getTest() == null) {
			status.setStatus(-111, ERROR_INCOMPLETE_REQUEST);
			return status;
		}
		if(request.getTest().getId() == null && request.getStudent().getId() == null) {
			status.setStatus(-111, ERROR_INCOMPLETE_REQUEST);
			return status;
		}
		
		try {
			testsDao.saveStudentTestActivity(request);
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e), LoggingUtil.testActivityErrorLogger);
		}
		return status;
	}

	public EdoServiceResponse createVideoLecture(EdoServiceRequest request) {
		Session session = null;
		EdoServiceResponse response = new EdoServiceResponse();
		try {
			session = this.sessionFactory.openSession();
			Transaction tx = session.beginTransaction();
			EdoVideoLecture lectures = request.getLecture();
			lectures.setCreatedDate(new Date());
			lectures.setStatus("Uploading");
			Integer topicId = lectures.getTopicId();
			lectures.setTopicId(null);
			// lectures.setType
			// lectures.setTopicId(topicId);
			/*
			 * if(StringUtils.isNotBlank(keywords)) {
			 * lectures.setKeywords(StringUtils.removeEnd(keywords, ",")); }
			 */
			// String vimeoLink = vimeoResponse.getJson().getString("link");
			// Prepare embed link
			// lectures.setSize(length);
			session.persist(lectures);

			lectures.setVideo_url(StringUtils.replace(VIDEO_BASE_URL, "{fileName}", lectures.getId() + ".mp4"));
			// Save question file if present
			/*
			 * if(questionFile != null) { CommonUtils.saveFile(questionFile,
			 * EdoConstants.VIDEO_QUESTION_FILE_PATH + lectures.getId() + "/",
			 * questionFileName);
			 * lectures.setQuestionImg(EdoConstants.VIDEO_QUESTION_FILE_PATH +
			 * lectures.getId() + "/" + questionFileName); }
			 */
			// Add keywords to repo
			/*
			 * if(StringUtils.isNotBlank(keywords)) {
			 * updateKeywords(instituteId, keywords, session); }
			 */

			// Add multiple classrroms (if any)
			if (StringUtils.isNotBlank(request.getClassrooms())) {
				String[] classroomArray = StringUtils.split(request.getClassrooms(), ",");
				if (ArrayUtils.isNotEmpty(classroomArray)) {
					for (String classroom : classroomArray) {
						if (StringUtils.isNotBlank(classroom)) {
							EdoContentMap map = new EdoContentMap();
							map.setContentId(lectures.getId());
							map.setClassroomId(new Integer(classroom));
							map.setChapterId(topicId);
							map.setCreatedDate(new Date());
							session.persist(map);
						}
					}
				}
			}
			tx.commit();
			updateQuota(lectures.getInstituteId(), lectures.getSize());
			List<EdoVideoLectureMap> lectureArray = new ArrayList<EdoVideoLectureMap>();
			EdoVideoLectureMap map = new EdoVideoLectureMap();
			map.setLecture(lectures);
			lectureArray.add(map);
			response.setLectures(lectureArray);
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			response.setStatus(new EdoApiStatus(-111, ERROR_IN_PROCESSING));
		} finally {
			CommonUtils.closeSession(session);
		}
		return response;
	}

	public EdoServiceResponse getFeedbackDetails(EdoServiceRequest request) {
		EdoServiceResponse response = new EdoServiceResponse();
		try {
			EdoTest test = new EdoTest();
			ArrayList<EdoQuestion> questions = new ArrayList<EdoQuestion>();
			EdoQuestion feedbackDetails = testsDao.getFeedbackDetails(request);
			CommonUtils.setQuestionURLs(feedbackDetails);
			QuestionParser.fixQuestion(feedbackDetails);
			CommonUtils.setupFeedbackAttachment(feedbackDetails);
			questions.add(feedbackDetails);
			test.setTest(questions);
			response.setTest(test);
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
		}
		return response;
	}
	
	public EdoApiStatus uploadAnswers(List<FormDataBodyPart> bodyParts, Integer testId, Integer studentId) {
		EdoApiStatus status = new EdoApiStatus();
		Session session = null;
		try {
			if(CollectionUtils.isNotEmpty(bodyParts)) {
				session = this.sessionFactory.openSession();
				Transaction tx = session.beginTransaction();
				for (FormDataBodyPart bodyPart: bodyParts) {
					/*
					 * Casting FormDataBodyPart to BodyPartEntity, which can give us
					 * InputStream for uploaded file
					 */
					BodyPartEntity bodyPartEntity = (BodyPartEntity) bodyPart.getEntity();
					//String fileName = bodyParts.get(i).getContentDisposition().getFileName();
					/*String answersPath = ANSWERS_PATH + testId + "/";
					File folder = new File(answersPath);
					if(!folder.exists()) {
						folder.mkdirs();
					}*/
					EdoAnswerFileEntity answerFileEntity = new EdoAnswerFileEntity();
					answerFileEntity.setTestId(testId);
					answerFileEntity.setStudentId(studentId);
					answerFileEntity.setCreatedDate(new Date());
					session.persist(answerFileEntity);
					String fileName = answerFileEntity.getId() +  "_" + bodyPart.getContentDisposition().getFileName();
					//String filePath = answersPath + fileName;
					//answersPath = answersPath
					//writeFile(bodyPartEntity.getInputStream(), new FileOutputStream(filePath));*/
					answerFileEntity.setFileUrl(EdoAwsUtil.uploadToAws(fileName, null, bodyPartEntity.getInputStream(), bodyPart.getContentDisposition().getType(), "answerFilesEdofox"));
					//answerFileEntity.setFilePath(filePath);
					//answerFileEntity.setFileUrl(CommonUtils.prepareAnswerURLs(answerFileEntity.getId()));
					
				}
				tx.commit();
			}
			
			
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e), LoggingUtil.saveAnswerErrorLogger);
			e.printStackTrace();
			status.setStatus(-111, ERROR_IN_PROCESSING);
		} finally {
			CommonUtils.closeSession(session);
		}
		return status;
	}
	
	public EdoServiceResponse getUploadedAnswers(EdoServiceRequest request) {
		EdoServiceResponse response = new EdoServiceResponse();
		if (request.getStudent() == null || request.getStudent().getId() == null || request.getTest() == null || request.getTest().getId() == null) {
			response.setStatus(new EdoApiStatus(STATUS_ERROR, ERROR_INCOMPLETE_REQUEST));
			return response;
		}
		try {
			EdoTest test = new EdoTest();
			List<EdoAnswerFileEntity> answerFiles = testsDao.getAnswerFiles(request);
			//To avoid cache
			/*if(CollectionUtils.isNotEmpty(answerFiles)) {
				for(EdoAnswerFileEntity answerFile: answerFiles) {
					if(StringUtils.isNotBlank(answerFile.getCorrectionUrl())) {
						answerFile.setCorrectionUrl(answerFile.getCorrectionUrl() + "v=" + System.currentTimeMillis());
					}
				}
			}*/
			test.setAnswerFiles(answerFiles);
			response.setTest(test);
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
		}
		return response;
	}

	public EdoServiceResponse getAppVersion(EdoServiceRequest request) {
		EdoServiceResponse response = new EdoServiceResponse();
		try {
			EDOInstitute institute = null;
			if(request.getInstitute() != null) {
				institute = testsDao.getInstituteById(request.getInstitute().getId());
			}
			if(institute != null && StringUtils.isNotBlank(institute.getAppVersion())) {
				//Compare app version with users version and show error if older version
				response.setInstitute(institute);
			} else {
				String appVersion = StringUtils.trimToEmpty(EdoPropertyUtil.getProperty(EdoPropertyUtil.APP_VERSION));
				if(StringUtils.isNotBlank(appVersion)) {
					//Compare app version with users version and show error if older version
					EDOInstitute insti = new EDOInstitute();
					insti.setAppUrl("https://play.google.com/store/apps/details?id=com.mattersoft.edofoxapp&hl=en_IN&gl=US");
					insti.setAppVersion(appVersion);
					response.setInstitute(insti);
					response.setStatus(new EdoApiStatus(STATUS_WRONG_VERSION, ERROR_WRONG_VERSION));
				}
			}
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
		}
		return response;
	}

	public EdoApiStatus updateProfile(EdoServiceRequest request) {
		EdoApiStatus response = new EdoApiStatus();
		Session session = null;
		try {
			session = this.sessionFactory.openSession();
			EdoStudent student = request.getStudent();
			List<EdoProfileEntity> profile = session.createCriteria(EdoProfileEntity.class).add(Restrictions.eq("id", student.getId())).list();
			if(CollectionUtils.isNotEmpty(profile)) {
				Transaction tx = session.beginTransaction();
				EdoProfileEntity existing = profile.get(0);
				if(StringUtils.isNotBlank(student.getName())) {
					existing.setName(student.getName());
				}
				if(StringUtils.isNotBlank(student.getPhone())) {
					existing.setPhone(student.getPhone());
				}
				if(StringUtils.isNotBlank(student.getEmail())) {
					existing.setEmail(student.getEmail());
				}
				tx.commit();
			}
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			response.setStatus(111, ERROR_IN_PROCESSING);
		} finally {
			CommonUtils.closeSession(session);
		}
		return response;
	}

	public EdoServiceResponse matchFaces(EdoServiceRequest request, InputStream file, FormDataContentDisposition fileDetails) {
		EdoServiceResponse response = new EdoServiceResponse();
		Session session = null;
		try {
			//InputStream inputStream = new FileInputStream(new File(sourceImage));
			//Target Image from camera
			
			byte[] byteArray = IOUtils.toByteArray(file);
			
			File compressedFile = null;
			//If length of file is greater than max allowed by Amazon, compress it
			float f = 4194304; //5 MBs
			if(byteArray.length > f) {
				String outputPath = PROCTORING_TEMP;
				if(!new File(outputPath).exists()) {
					new File(outputPath).mkdirs();
				}
				outputPath = outputPath + "/" + request.getStudent().getId() + "_" + System.currentTimeMillis() + ".jpg";
				InputStream backupInputStream = new ByteArrayInputStream(byteArray);
				compressedFile = EdoImageUtil.compressImage(backupInputStream, outputPath, 0.5f);
				byteArray = FileUtils.readFileToByteArray(compressedFile);
			}		
			
			ByteBuffer destinationImageBytes = ByteBuffer.wrap(byteArray);
			InputStream backupStream = new ByteArrayInputStream(byteArray);
			
			
			//TODO replace with DB student image
			//String sourceImage = "F:\\Resoneuronance\\Edofox\\Document\\Director_Pic.jpg";
			//String hostName = EdoPropertyUtil.getProperty(EdoPropertyUtil.HOST_NAME);
			//Get student profile pic for comparison
			EdoStudent student = testsDao.getStudentById(request.getStudent().getId());
			//"uploads/profilePics/6c1c3cf7940ac21b0438a39ace76cfba.jpg"
			String sourceImage = CommonUtils.setUrl(student.getProctorImageRef());
			
			//FileInputStream fileInputStream = new FileInputStream(sourceImage);
			ByteBuffer sourceImageBytes = ByteBuffer.wrap(IOUtils.toByteArray(new URL(sourceImage).openStream()));
			
			
			EdoFaceScore score = EdoFaceDetection.compareFaceImages(sourceImageBytes, destinationImageBytes);
			//System.out.print("Score is " + score);
			
			//Upload file to AWS bucket first
			String filePath = EdoAwsUtil.uploadToAws(request.getTest().getId() + "_" + request.getStudent().getId() + "_" + System.currentTimeMillis() + ".jpg", null, backupStream, "image/jpeg", "proctoring");
			session = this.sessionFactory.openSession();
			Transaction tx = session.beginTransaction();
			EdoProctorImages images = new EdoProctorImages();
			if(score != null) {
				images.setScore(score.getScore());
				images.setRemarks(score.getRemarks());
			}
			images.setCreatedDate(new Date());
			images.setTestId(request.getTest().getId());
			images.setStudentId(request.getStudent().getId());
			images.setImageUrl(filePath);
			session.persist(images);
			
			if(StringUtils.isNotBlank(score.getRemarks())) {
				List<EdoTestStatusEntity> maps = /*testsDao.getTestStatus(inputMap)*/ session.createCriteria(EdoTestStatusEntity.class)
						.add(Restrictions.eq("testId", request.getTest().getId()))
						.add(Restrictions.eq("studentId", request.getStudent().getId()))
						.list();
				if(CollectionUtils.isNotEmpty(maps)) {
					EdoTestStatusEntity edoTestStatusEntity = maps.get(0);
					if(StringUtils.isBlank(edoTestStatusEntity.getProctoringRemarks())) {
						edoTestStatusEntity.setProctoringRemarks(score.getRemarks());
					} else if (!StringUtils.contains(edoTestStatusEntity.getProctoringRemarks(), score.getRemarks())) {
						edoTestStatusEntity.setProctoringRemarks(edoTestStatusEntity.getProctoringRemarks() + "," + score.getRemarks());
					}
				}
			}
			
			tx.commit();
			
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			response.setStatus(new EdoApiStatus(-111, ERROR_IN_PROCESSING));
		} finally {
			CommonUtils.closeSession(session);
		}
		return response;
	}

	public EdoServiceResponse uploadProctorRef(EdoServiceRequest request, InputStream recordingData, FormDataContentDisposition recordingDataDetails) {
		EdoServiceResponse response = new EdoServiceResponse();
		try {
			EdoStudent student = request.getStudent();
			String url = EdoAwsUtil.uploadToAws(student.getId() + "_" + System.currentTimeMillis() + ".jpg", null, recordingData, "image/jpeg", "proctorRef");
			//Update proctor_ref url in DB
			student.setProctorImageRef(url);
			testsDao.updateProctorUrl(student);
			response.setStudent(request.getStudent());
			
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			response.setStatus(new EdoApiStatus(-111, ERROR_IN_PROCESSING));
		}
		return response;
	}
	
	public EdoServiceResponse saveProctorRefImageUrl(EdoServiceRequest request) {
		EdoServiceResponse response = new EdoServiceResponse();
		try {
			EdoStudent student = request.getStudent();
			//String url = EdoAwsUtil.uploadToAws(student.getId() + "_" + System.currentTimeMillis() + ".jpg", null, recordingData, "image/jpeg", "proctorRef");
			//Update proctor_ref url in DB
			//student.setProctorImageRef(url);
			testsDao.updateProctorUrl(student);
			response.setStudent(request.getStudent());
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			response.setStatus(new EdoApiStatus(-111, ERROR_IN_PROCESSING));
		}
		return response;
	}
	
	public EdoServiceResponse saveProctorImage(EdoServiceRequest request) {
		EdoServiceResponse response = new EdoServiceResponse();
		Session session = null;
		try {
			session = this.sessionFactory.openSession();
			Transaction tx = session.beginTransaction();
			EdoProctorImages images = new EdoProctorImages();
			/*if(score != null) {
				images.setScore(score.getScore());
				images.setRemarks(score.getRemarks());
			}*/
			images.setCreatedDate(new Date());
			images.setTestId(request.getTest().getId());
			images.setStudentId(request.getStudent().getId());
			images.setImageUrl(request.getStudent().getProctorImageRef());
			session.persist(images);
			
			/*if(StringUtils.isNotBlank(score.getRemarks())) {
				List<EdoTestStatusEntity> maps = testsDao.getTestStatus(inputMap) session.createCriteria(EdoTestStatusEntity.class)
						.add(Restrictions.eq("testId", request.getTest().getId()))
						.add(Restrictions.eq("studentId", request.getStudent().getId()))
						.list();
				if(CollectionUtils.isNotEmpty(maps)) {
					EdoTestStatusEntity edoTestStatusEntity = maps.get(0);
					if(StringUtils.isBlank(edoTestStatusEntity.getProctoringRemarks())) {
						edoTestStatusEntity.setProctoringRemarks(score.getRemarks());
					} else if (!StringUtils.contains(edoTestStatusEntity.getProctoringRemarks(), score.getRemarks())) {
						edoTestStatusEntity.setProctoringRemarks(edoTestStatusEntity.getProctoringRemarks() + "," + score.getRemarks());
					}
				}
			}*/
			
			tx.commit();
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			response.setStatus(new EdoApiStatus(-111, ERROR_IN_PROCESSING));
		} finally {
			CommonUtils.closeSession(session);
		}
		return response;
	}

	public EdoApiStatus forgotPassword(EdoServiceRequest request) {
		EdoApiStatus status = new EdoApiStatus();
		try {
		 	List<EdoStudent> existing = testsDao.getStudentLogin(request.getStudent());
		 	if(CollectionUtils.isNotEmpty(existing)) {
		 		EdoStudent student = existing.get(0);
		 		if(StringUtils.isBlank(student.getToken())) {
		 			status.setResponseText("Your profile does not have access to password reset. Please contact your admin.");
		 			return status;
		 		}
		 		boolean email = false , sms= false;
		 		if(StringUtils.isNotBlank(student.getEmail())) {
		 			//Send email for password reset request
		 			EdoMailUtil mailUtil = new EdoMailUtil(MAIL_TYPE_PASSWORD_RESET);
		 			mailUtil.setStudent(student);
		 			EdoMailer mailer = new EdoMailer();
		 			mailer.setActionUrl(EdoPropertyUtil.getProperty(EdoPropertyUtil.HOST_NAME) + "reset_password.php?id=" + student.getToken());
					mailUtil.setMailer(mailer);
					executor.execute(mailUtil);
					email = true;
		 		}
		 		if(StringUtils.isNotBlank(student.getPhone())) {
		 			//Send email for password reset request
		 			EdoSMSUtil smsUtil = new EdoSMSUtil(MAIL_TYPE_PASSWORD_RESET);
		 			smsUtil.setStudent(student);
		 			EdoMailer mailer = new EdoMailer();
		 			mailer.setActionUrl(EdoPropertyUtil.getProperty(EdoPropertyUtil.HOST_NAME) + "reset_password.php?id=" + student.getToken());
		 			smsUtil.setMailer(mailer);
					smsUtil.sendSMS();
					sms = true;
		 		}
		 		if(!email && !sms) {
		 			status.setStatus(-111, "No valid email ID or mobile number found for this user");
		 		} else {
		 			String successMsg = "We have send the reset password link on your ";
		 			if(email) {
		 				String maskedEmail = StringUtils.substring(student.getEmail(), 0, 3) + "****  ";
		 				successMsg = successMsg + " email at " + maskedEmail;
		 			}
		 			if(sms) {
		 				String maskedPhone = StringUtils.substring(student.getPhone(), 0, 1) + "****" + StringUtils.substring(student.getPhone(), student.getPhone().length() - 1, student.getPhone().length());
		 				successMsg = successMsg + " phone at " + maskedPhone;
		 			}
		 			status.setResponseText(successMsg);
		 		}
		 	} else {
		 		status.setStatus(-111, ERROR_INVALID_PROFILE);
		 	}
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			status.setStatus(-111, ERROR_IN_PROCESSING);
		}
		return status;
	}
	
	//TODO Temporary..To be removed
	public EdoApiStatus saveTestNoCommit(EdoServiceRequest request) {
		EdoTest test = request.getTest();
		if(request.getStudent() == null || request.getStudent().getId() == null || test == null || test.getId() == null) {
			LoggingUtil.logMessage("Invalid test input", LoggingUtil.saveTestLogger);
			return new EdoApiStatus(-111, ERROR_INVALID_PROFILE);
		}
		EdoApiStatus status = new EdoApiStatus();
		Session session = null;
		//TransactionStatus txStatus = txManager.getTransaction(new DefaultTransactionDefinition());
		
		try {
			
			CommonUtils.saveJson(request);
			
			Integer currentTest = testSubmissions.get(request.getStudent().getId());
			if(currentTest != null && request.getTest().getId() == currentTest) {
				//Don't return already submitted as ERROR, consider it a success
				//status.setResponseText(ERROR_TEST_ALREADY_SUBMITTED);
				//status.setStatusCode(STATUS_ERROR);
				LoggingUtil.logMessage("Test " + currentTest + " being submitted for student=>" + request.getStudent().getId(), LoggingUtil.saveTestLogger);
				return status;
			}
			LoggingUtil.logMessage("Submitting test .. " + request.getTest().getId() + " by student .. " + request.getStudent().getId() + " map =>" + testSubmissions, LoggingUtil.saveTestLogger);
			testSubmissions.put(request.getStudent().getId(), request.getTest().getId());
			
			/*EdoTestStudentMap inputMap = new EdoTestStudentMap();
			inputMap.setTest(test);
			inputMap.setStudent(request.getStudent());*/
			
			//New flow
			session = sessionFactory.openSession();
			
			List<EdoTestStatusEntity> maps = /*testsDao.getTestStatus(inputMap)*/ session.createCriteria(EdoTestStatusEntity.class)
											.add(Restrictions.eq("testId", test.getId()))
											.add(Restrictions.eq("studentId", request.getStudent().getId()))
											.list();
			EdoTestStatusEntity map = null;
			if(CollectionUtils.isNotEmpty(maps)) {
				map = maps.get(0);
			}
			
			//Already submitted error removed from the code Jan 05 21 .. allow student to overwrite and submit again
			/*if(map != null && StringUtils.equals(TEST_STATUS_COMPLETED, map.getStatus())) {
				status.setResponseText(ERROR_TEST_ALREADY_SUBMITTED);
				status.setStatusCode(STATUS_ERROR);
				LoggingUtil.logMessage("Already submitted this test for student=>" + request.getStudent().getId(), LoggingUtil.saveTestLogger);
				return status;
			}*/
			
			if(map == null) {
				map = new EdoTestStatusEntity();
				map.setCreatedDate(new Date());
				map.setTestId(test.getId());
				map.setStudentId(request.getStudent().getId());
			}
			
			List<EdoQuestion> questions = testsDao.getExamQuestions(test.getId());
			
			if(CollectionUtils.isEmpty(questions)) {
				status.setResponseText(ERROR_IN_PROCESSING);
				status.setStatusCode(STATUS_ERROR);
				return status;
			}
			
			//EdoTest existing = testsDao.getTest(test.getId());
			
			//if(existing == null || StringUtils.isBlank(existing.getShowResult()) || StringUtils.equalsIgnoreCase("Y", existing.getShowResult())) {
			CommonUtils.calculateTestScore(test, questions);
			//}
			
			/*if(request != null && request.getTest() != null && CollectionUtils.isNotEmpty(request.getTest().getTest())) {
				testsDao.saveTestResult(request);
				testsDao.saveTestStatus(request);
				
				//TODO: Temporary
				EdoSMSUtil smsUtil = new EdoSMSUtil(MAIL_TYPE_TEST_RESULT);
				smsUtil.setTest(test);
				EdoStudent student = testsDao.getStudentById(request.getStudent().getId());
				smsUtil.setStudent(student);
				executor.execute(smsUtil);
			}*/
			Transaction tx = session.beginTransaction();
			if(CollectionUtils.isNotEmpty(test.getTest())) {
				for(EdoQuestion question: test.getTest()) {
					EdoServiceRequest saveAnswerRequest = new EdoServiceRequest();
					saveAnswerRequest.setTest(test);
					saveAnswerRequest.setStudent(request.getStudent());
					saveAnswerRequest.setQuestion(question);
					if(StringUtils.equalsIgnoreCase(EdoConstants.QUESTION_TYPE_MATCH, question.getType()) || StringUtils.isNotBlank(question.getAnswer())) {
						saveAnswer(saveAnswerRequest, session);
					}
				}
			}
			
			map.setSolved(test.getSolvedCount());
			map.setCorrect(test.getCorrectCount());
			map.setFlagged(test.getFlaggedCount());
			map.setScore(test.getScore());
			map.setStatus(TEST_STATUS_COMPLETED);
			map.setUpdatedDate(new Date());
			map.setSubmissionType(test.getSubmissionType());
			if(test.getMinLeft() != null && test.getSecLeft() != null) {
				map.setTimeLeft((test.getMinLeft() * 60) + test.getSecLeft());
			} else if (map.getTimeLeft() != null) {
				map.setTimeLeft(map.getTimeLeft());
			}
			if(map.getId() == null) {
				session.persist(map);
			}
			//Commit the transaction
			//txManager.commit(txStatus);
			//tx.commit();
			LoggingUtil.logMessage("Submitted the test " + test.getId() +  " for .. " + request.getStudent().getId(), LoggingUtil.saveTestLogger);
			addTestActivity(test.getId(), request.getStudent().getId(), "COMPLETED", test);
		} catch (Exception e) {
			status.setStatusCode(STATUS_ERROR);
			status.setResponseText(ERROR_IN_PROCESSING);
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e), LoggingUtil.saveTestErrorLogger);
			//rollback
			/*try {
				txManager.rollback(txStatus);
			} catch (Exception e2) {
				LoggingUtil.logError(ExceptionUtils.getStackTrace(e2));
			}*/
			
		} finally {
			CommonUtils.closeSession(session);
			if(request.getStudent() != null && request.getStudent().getId() != null) {
				testSubmissions.remove(request.getStudent().getId());
				LoggingUtil.logMessage("Submitted test .. " + request.getTest().getId() + " by student .. " + request.getStudent().getId() + " map =>" + testSubmissions, LoggingUtil.saveTestLogger);
			}
		}
		return status;
	}

	public EdoServiceResponse getStudentActivity(EdoServiceRequest request) {
		EdoServiceResponse edoServiceResponse = new EdoServiceResponse();
		try {
			List<EdoTestQuestionMap> activity = testsDao.getStudentTestActivity(request);
			if(CollectionUtils.isNotEmpty(activity)) {
				EdoTest test = new EdoTest();
				test.setTest(new ArrayList<EdoQuestion>());
				for(EdoTestQuestionMap map: activity) {
					EdoQuestion q = map.getQuestion();
					CommonUtils.setQuestionURLs(q);
					if (ACTIVITY_INFO.get(q.getActivityType()) != null) {
						q.setActivityType(ACTIVITY_INFO.get(q.getActivityType()));
					} else {
						q.setActivityType("You clicked on " + q.getActivityType());
					}
					test.getTest().add(q);
				}
				edoServiceResponse.setTest(test);
			}
			
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			edoServiceResponse.setStatus(new EdoApiStatus(-111, ERROR_IN_PROCESSING));
		}
		return edoServiceResponse;
	}

}
