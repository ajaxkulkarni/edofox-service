package com.rns.web.edo.service.bo.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.clickntap.vimeo.VimeoResponse;
import com.rns.web.edo.service.bo.api.EdoFile;
import com.rns.web.edo.service.bo.api.EdoUserBo;
import com.rns.web.edo.service.dao.EdoTestsDao;
import com.rns.web.edo.service.domain.EDOInstitute;
import com.rns.web.edo.service.domain.EDOPackage;
import com.rns.web.edo.service.domain.EDOTestAnalysis;
import com.rns.web.edo.service.domain.EdoApiStatus;
import com.rns.web.edo.service.domain.EdoComplexOption;
import com.rns.web.edo.service.domain.EdoFeedback;
import com.rns.web.edo.service.domain.EdoPaymentStatus;
import com.rns.web.edo.service.domain.EdoQuestion;
import com.rns.web.edo.service.domain.EdoServiceRequest;
import com.rns.web.edo.service.domain.EdoServiceResponse;
import com.rns.web.edo.service.domain.EdoStudent;
import com.rns.web.edo.service.domain.EdoStudentSubjectAnalysis;
import com.rns.web.edo.service.domain.EdoSuggestion;
import com.rns.web.edo.service.domain.EdoTest;
import com.rns.web.edo.service.domain.EdoTestQuestionMap;
import com.rns.web.edo.service.domain.EdoTestStudentMap;
import com.rns.web.edo.service.domain.EdoVideoLectureMap;
import com.rns.web.edo.service.domain.jpa.EdoActivityLogs;
import com.rns.web.edo.service.domain.jpa.EdoAnswerEntity;
import com.rns.web.edo.service.domain.jpa.EdoAnswerFileEntity;
import com.rns.web.edo.service.domain.jpa.EdoClasswork;
import com.rns.web.edo.service.domain.jpa.EdoClassworkActivity;
import com.rns.web.edo.service.domain.jpa.EdoClassworkMap;
import com.rns.web.edo.service.domain.jpa.EdoConfig;
import com.rns.web.edo.service.domain.jpa.EdoDeviceId;
import com.rns.web.edo.service.domain.jpa.EdoKeyword;
import com.rns.web.edo.service.domain.jpa.EdoLiveSession;
import com.rns.web.edo.service.domain.jpa.EdoTestStatusEntity;
import com.rns.web.edo.service.domain.jpa.EdoVideoLecture;
import com.rns.web.edo.service.util.CommonUtils;
import com.rns.web.edo.service.util.EdoConstants;
import com.rns.web.edo.service.util.EdoMailUtil;
import com.rns.web.edo.service.util.EdoNotificationsManager;
import com.rns.web.edo.service.util.EdoPDFUtil;
import com.rns.web.edo.service.util.EdoPropertyUtil;
import com.rns.web.edo.service.util.EdoSMSUtil;
import com.rns.web.edo.service.util.LoggingUtil;
import com.rns.web.edo.service.util.PaymentUtil;
import com.rns.web.edo.service.util.QuestionParser;
import com.rns.web.edo.service.util.VideoUtil;
import com.sun.jersey.multipart.BodyPartEntity;
import com.sun.jersey.multipart.FormDataBodyPart;

public class EdoUserBoImpl implements EdoUserBo, EdoConstants {

	private ThreadPoolTaskExecutor executor;
	private EdoTestsDao testsDao;

	private DataSourceTransactionManager txManager;
	private SessionFactory sessionFactory;
	private Map<Integer, Integer> testSubmissions = new ConcurrentHashMap<Integer, Integer>();
	
	public void setTxManager(DataSourceTransactionManager txManager) {
		this.txManager = txManager;
	}
	
	public DataSourceTransactionManager getTxManager() {
		return txManager;
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
		
		if(CollectionUtils.isNotEmpty(map)) {
			EdoTest test = map.get(0).getTest();
			
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
			List<EdoQuestion> questionCorrectness = null;
			if(StringUtils.equals(test.getShowResult(), "Y")) {
				questionCorrectness = testsDao.getQuestionCorrectness(test.getId());
			}
			
			test.setSections(new ArrayList<String>());


			for(EdoTestQuestionMap mapper: map) {
				EdoQuestion question = mapper.getQuestion();
				LoggingUtil.logMessage("Solution image ==> " + question.getSolutionImageUrl());
				CommonUtils.setQuestionURLs(question);
				QuestionParser.fixQuestion(question);

				prepareMatchTypeQuestion(question);

				
				//Add only if not disabled
				if(question.getDisabled() != null && question.getDisabled() == 1) {
					if(StringUtils.isBlank(question.getAnswer())) {
						continue;
					}
				}
				

				if(CollectionUtils.isNotEmpty(questionCorrectness)) {
					for(EdoQuestion correctness: questionCorrectness) {
						if(correctness.getQn_id() != null && question.getQn_id() != null && correctness.getQn_id().intValue() == question.getQn_id().intValue()) {
							question.setAnalysis(correctness.getAnalysis());
							break;
						}
					}
				}
				
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
			//TODO later if needed response.setLectures(testsDao.getTestVideoLectures(test.getId()));
			
			//Fetch answer files if descriptive test
			if(StringUtils.equals(test.getTestUi(), "DESCRIPTIVE")) {
				test.setAnswerFiles(testsDao.getAnswerFiles(request));
			}
			
			response.setTest(test);

		} else {
			response.setStatus(new EdoApiStatus(-111, "Result not found. Please submit your exam first."));

		}
		
		return response;
	}

	public EdoServiceResponse getTest(Integer testId, Integer studentId) {
		EdoServiceResponse response = new EdoServiceResponse();
		if(testId == null) {
			response.setStatus(new EdoApiStatus(STATUS_ERROR, ERROR_INCOMPLETE_REQUEST));
			return response;
		}
		
		try {
			EdoTestStudentMap inputMap = new EdoTestStudentMap();
			inputMap.setTest(new EdoTest(testId));

			Date startedDate = null;

			if(studentId != null) {
				inputMap.setStudent(new EdoStudent(studentId));
				List<EdoTestStudentMap> studentMaps = testsDao.getTestStatus(inputMap);
				EdoTestStudentMap studentMap = null;
				if(CollectionUtils.isNotEmpty(studentMaps)) {
					studentMap = studentMaps.get(0);
				}
				if(studentMap != null && StringUtils.equals(TEST_STATUS_COMPLETED, studentMap.getStatus())) {
					response.setStatus(new EdoApiStatus(STATUS_TEST_SUBMITTED, ERROR_TEST_ALREADY_SUBMITTED));
					return response;
				}
				

				
				if(studentMap != null) {
					startedDate = studentMap.getCreatedDate();
				}
				

				//Added on 11/12/19
				if(studentMap == null) {
					//Add test status as 'STARTED' to track students who logged in
					EdoServiceRequest request = new EdoServiceRequest();
					EdoStudent student = new EdoStudent();
					student.setId(studentId);
					request.setStudent(student);
					EdoTest test = new EdoTest();
					test.setId(testId);
					request.setTest(test);
					request.setRequestType(TEST_STATUS_STARTED);
					test.setSolvedCount(0);
					test.setCorrectCount(0);
					test.setFlaggedCount(0);
					test.setScore(BigDecimal.ZERO);
					testsDao.saveTestStatus(request);
				}
				//Added on 11/12/19
				
				/*studentMaps = testsDao.getStudentActivePackage(inputMap);
				
				if(CollectionUtils.isNotEmpty(studentMaps)) {
					studentMap = studentMaps.get(0);
				} else {
					studentMap = null;
				}
				
				if(studentMap == null) {
					//Test or package not active
					response.setStatus(new EdoApiStatus(STATUS_TEST_NOT_PAID, ERROR_TEST_NOT_PAID));
					return response;
				}*/
				
				/*if(!StringUtils.equalsIgnoreCase(studentMap.getStudentAccess(), ACCESS_LEVEL_ADMIN)) {
					if(!StringUtils.equalsIgnoreCase(STATUS_ACTIVE, studentMap.getStatus())) {
						response.setStatus(new EdoApiStatus(STATUS_TEST_NOT_ACTIVE, ERROR_TEST_NOT_ACTIVE));
						return response;
					}
					
					EdoTest mapTest = studentMap.getTest();
					if(mapTest != null) {
						if(mapTest.getStartDate() != null && mapTest.getStartDate().getTime() > new Date().getTime()) {
							response.setStatus(new EdoApiStatus(STATUS_TEST_NOT_OPENED, "Test will be available on " + CommonUtils.convertDate(mapTest.getStartDate())));
							return response;
						}
						if(mapTest.getEndDate() != null && mapTest.getEndDate().getTime() < new Date().getTime()) {
							if(studentMap.getRegisterDate() != null && studentMap.getRegisterDate().getTime() < mapTest.getEndDate().getTime()) {
								response.setStatus(new EdoApiStatus(STATUS_TEST_EXPIRED, ERROR_TEST_EXPIRED));
								return response;
							}
						}
					}
				}*/
				
			}
			
			List<EdoTestQuestionMap> map = testsDao.getExam(testId);
			
			if(CollectionUtils.isNotEmpty(map)) {
				EdoTest result = map.get(0).getTest();
				
				//Check if test exists
				if(result != null && studentId != null) {
					if(result.getStartDate() != null && result.getStartDate().getTime() > new Date().getTime()) {
						response.setStatus(new EdoApiStatus(STATUS_TEST_NOT_OPENED, "Test will be available on " + CommonUtils.convertDate(result.getStartDate())));
						return response;
					}
					if(result.getEndDate() != null && result.getEndDate().getTime() < new Date().getTime()) {
						response.setStatus(new EdoApiStatus(STATUS_TEST_EXPIRED, ERROR_TEST_EXPIRED));
						return response;
					}
				}
				
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
						if(studentId != null) {
							question.setCorrectAnswer(null);
							question.setAlternateAnswer(null);
							question.setSolution(null);
							question.setSolutionImageUrl(null);
						}
						if(!result.getSubjects().contains(question.getSubject())) {
							result.getSubjects().add(question.getSubject());
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
				if(isRandomizeQuestions(result) && studentId != null) {
					//Randomize only for student NOT for Admin
					randomizeQuestions(result, sectionSets);
				}
				response.setTest(result);
			}
			
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			response.setStatus(new EdoApiStatus(STATUS_ERROR, ERROR_IN_PROCESSING));
		}
		
		return response;
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


	private void randomizeQuestions(EdoTest result, Map<String, List<EdoQuestion>> sectionSets) {
		if(CollectionUtils.isNotEmpty(sectionSets.keySet()) && result != null) {
			List<EdoQuestion> shuffled = new ArrayList<EdoQuestion>();
			Integer qNo = 1;
			for(String section: result.getSections()) {
				List<EdoQuestion> set = sectionSets.get(section);
				if(!StringUtils.contains(set.get(0).getType(), QUESTION_TYPE_PASSAGE)) {
					//No shuffle for comprehension type questions
					LoggingUtil.logMessage("Shuffling for section .." + section + " - list - " + set.size());
					Collections.shuffle(set);
				}
				for(EdoQuestion question: set) {
					question.setQuestionNumber(qNo);
					qNo++;
					shuffled.add(question);
				}
			}
			result.setTest(shuffled);
			LoggingUtil.logMessage("Shuffled the questions for test .." + result.getId());
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

	private void saveAnswer(EdoServiceRequest request, Session session) {
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
		answer.setTimeTaken(request.getQuestion().getTimeSpent());
		//Will update only for save test
		answer.setMarks(request.getQuestion().getMarks());
		answer.setUpdatedDate(new Date());
		
		System.out.println("Saving answer .. " + answer.getOptionSelected());
		
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
				status.setResponseText(ERROR_TEST_ALREADY_SUBMITTED);
				status.setStatusCode(STATUS_ERROR);
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
			if(map != null && StringUtils.equals(TEST_STATUS_COMPLETED, map.getStatus())) {
				status.setResponseText(ERROR_TEST_ALREADY_SUBMITTED);
				status.setStatusCode(STATUS_ERROR);
				LoggingUtil.logMessage("Already submitted this test for student=>" + request.getStudent().getId(), LoggingUtil.saveTestLogger);
				return status;
			}
			
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
			CommonUtils.calculateTestScore(test, questions);
			
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
					//To avoid blank submissions
					if(StringUtils.isNotBlank(question.getAnswer())) {
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
			if(map.getId() == null) {
				session.persist(map);
			}
			//Commit the transaction
			//txManager.commit(txStatus);
			tx.commit();
			LoggingUtil.logMessage("Submitted the test " + test.getId() +  " for .. " + request.getStudent().getId(), LoggingUtil.saveTestLogger);
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
			EdoQuestion question = testsDao.getQuestion(questionId);
			if(question == null && !StringUtils.equals(imageType, "TEMP")) {
				return null;
			}
			String path = null;
			if(StringUtils.equals(imageType, ATTR_QUESTION)) {
				path = question.getQuestionImageUrl();
			} else if (StringUtils.equals(imageType, ATTR_OPTION1)) {
				path = question.getOption1ImageUrl();
			} else if (StringUtils.equals(imageType, ATTR_OPTION2)) {
				path = question.getOption2ImageUrl();
			} else if (StringUtils.equals(imageType, ATTR_OPTION3)) {
				path = question.getOption3ImageUrl();
			} else if (StringUtils.equals(imageType, ATTR_OPTION4)) {
				path = question.getOption4ImageUrl();
			} else if (StringUtils.equals(imageType, ATTR_META_DATA)) {
				path = question.getMetaDataImageUrl();
			} else if (StringUtils.equals(imageType, "TEMP")) {
				path = TEMP_QUESTION_PATH + testId + "/" + EdoPDFUtil.QUESTION_PREFIX + questionId + ".png";
			} else if (StringUtils.equals(imageType, ATTR_VIDEO_QUESTION)) {
				session = this.sessionFactory.openSession();
				List<EdoVideoLecture> lecs = session.createCriteria(EdoVideoLecture.class).add(Restrictions.eq("id", questionId)).list();
				if(CollectionUtils.isNotEmpty(lecs) && StringUtils.isNotBlank(lecs.get(0).getQuestionImg())) {
					path = lecs.get(0).getQuestionImg();
				}
			} else if (StringUtils.equals(imageType, ATTR_ANSWER)) {
				session = this.sessionFactory.openSession();
				List<EdoAnswerFileEntity> answers = session.createCriteria(EdoAnswerFileEntity.class).add(Restrictions.eq("id", questionId)).list();
				if(CollectionUtils.isNotEmpty(answers) && StringUtils.isNotBlank(answers.get(0).getFilePath())) {
					path = answers.get(0).getFilePath();
				}
			}
			
			if(path != null) {
				/*if(StringUtils.contains(path, "http")) {
					path = QuestionParser.downloadFile(path, imageType, questionId);
					if(StringUtils.equals(imageType, ATTR_OPTION1)) {
						question.setOption1ImageUrl(path);
					} else if (StringUtils.equals(imageType, ATTR_OPTION2)) {
						question.setOption2ImageUrl(path);
					} else if (StringUtils.equals(imageType, ATTR_OPTION3)) {
						question.setOption3ImageUrl(path);
					} else if (StringUtils.equals(imageType, ATTR_OPTION4)) {
						question.setOption4ImageUrl(path);
					}
					testsDao.updateQuestion(question);
				}*/
				
				InputStream is = new FileInputStream(path);
				file.setContent(is);
				file.setFileName(imageType + "." + CommonUtils.getFileExtension(path));
				LoggingUtil.logMessage("Found path as " + path);
			} else {
				LoggingUtil.logMessage("Path not found for " + questionId);
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
		TransactionStatus txStatus = txManager.getTransaction(new DefaultTransactionDefinition());
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
					LoggingUtil.logMessage("Max students limit reached ... " + student.getPhone() + " for institute " + student.getPackages().get(0).getInstitute().getId() + " count " +institute.getCurrentCount() + " and max " + institute.getMaxStudents());
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
						LoggingUtil.logMessage("Adding student login for =>" + student.getId());
						student.setInstituteId(student.getPackages().get(0).getInstitute().getId().toString());
						testsDao.saveLogin(student);
					}
				} else {
					//student.setId(existingStudent.get(0).getId());
					response.setStatus(new EdoApiStatus(STATUS_ERROR, "Student already exists with given username/roll number .."));
					return response;
				}
				
				LoggingUtil.logMessage("Student ID is =>" + student.getId());
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
			txManager.commit(txStatus);
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			response.setStatus(new EdoApiStatus(STATUS_ERROR, ERROR_IN_PROCESSING));
			txManager.rollback(txStatus);
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
			
			EdoPaymentStatus paymentResponse = PaymentUtil.paymentRequest(amount.doubleValue(), student, student.getTransactionId());
			if(paymentResponse != null && paymentResponse.getPaymentId() != null) {
				student.setPayment(paymentResponse);
				testsDao.updatePaymentId(student);
				LoggingUtil.logMessage("Got the payment Id as =>" + paymentResponse.getPaymentId());
			}
			response.setPaymentStatus(paymentResponse);
		}
	}

	private void updateStudentPackages(EdoStudent student) {
		testsDao.deleteExistingPackages(student);
		testsDao.createStudentPackage(student);
	}

	public EdoApiStatus processPayment(String id, String transactionId, String paymentId) {
		EdoApiStatus status = new EdoApiStatus();
		try {
			boolean validPayment = PaymentUtil.getPaymentStatus(id);
			if(validPayment) {
				List<EdoStudent> studentPackages = testsDao.getStudentByPayment(id);
				EDOInstitute institute = null;
				if(CollectionUtils.isNotEmpty(studentPackages)) {
					EdoStudent edoStudent = new EdoStudent();
					List<EDOPackage> packages = new ArrayList<EDOPackage>();
					boolean incompletePackageFound = false;
					for(EdoStudent student: studentPackages) {
						if(student.getCurrentPackage() != null) {
							packages.add(student.getCurrentPackage());
							if(student.getCurrentPackage().getInstitute() != null) {
								institute = new EDOInstitute();
								institute.setName(student.getCurrentPackage().getInstitute().getName());
							}
							if(!StringUtils.equals(student.getCurrentPackage().getStatus(), "Completed")) {
								incompletePackageFound = true;
							}
						}
						edoStudent.setName(student.getName());
						edoStudent.setPhone(student.getPhone());
						edoStudent.setEmail(student.getEmail());
					}
					if(transactionId != null) {
						edoStudent.setTransactionId(new Integer(StringUtils.removeStart(transactionId, "T")));
					}
					
					if(incompletePackageFound) {
						EdoPaymentStatus paymentStatus = new EdoPaymentStatus();
						paymentStatus.setPaymentId(id);
						paymentStatus.setResponseText("Completed");
						paymentStatus.setOffline(false);
						testsDao.updatePayment(paymentStatus);
						
						edoStudent.setPackages(packages);
						edoStudent.setPayment(paymentStatus);
						notifyStudent(edoStudent, MAIL_TYPE_ACTIVATED, institute);
					}
				}
			} else {
				EdoPaymentStatus paymentStatus = new EdoPaymentStatus();
				paymentStatus.setPaymentId(id);
				paymentStatus.setResponseText("Failed");
				testsDao.updatePayment(paymentStatus);
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
				EdoServiceResponse response = new EdoServiceResponse();
				completePayment(existing, response);
				return response.getPaymentStatus();
			}
			
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
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

	public EdoServiceResponse getAllSubjects() {
		EdoServiceResponse response = new EdoServiceResponse();
		try {
			 response.setSubjects(testsDao.getAllSubjects());
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

	public EdoServiceResponse raiseDoubt(EdoServiceRequest request) {
		EdoServiceResponse response = CommonUtils.initResponse();
		Session session = null;
		try {
			if(!StringUtils.equals("video", request.getRequestType())) {
				EdoQuestion currentQuestion = request.getTest().getCurrentQuestion();
				if(currentQuestion != null) {
					if(currentQuestion.getChapter() == null || currentQuestion.getChapter().getChapterId() == null || currentQuestion.getSubjectId() == null) {
						List<EdoQuestion> question = testsDao.getNextQuestion(currentQuestion);
						if(CollectionUtils.isNotEmpty(question)) {
							currentQuestion.setSubjectId(question.get(0).getSubjectId());
							currentQuestion.setChapter(question.get(0).getChapter());
						}
						EdoTestStudentMap map = new EdoTestStudentMap();
						map.setTest(request.getTest());
						map.setStudent(request.getStudent());
						testsDao.addQuestionQuery(map);
					}
				} 
			} else {
				//Video doubt
				session = this.sessionFactory.openSession();
				List<EdoVideoLecture> lectures = session.createCriteria(EdoVideoLecture.class).add(Restrictions.eq("id", request.getFeedback().getId())).list();
				if(CollectionUtils.isNotEmpty(lectures)) {
					EdoVideoLecture lecture = lectures.get(0);
					EdoTestStudentMap map = new EdoTestStudentMap();
					EdoTest test = new EdoTest();
					EdoQuestion currentQuestion = new EdoQuestion();
					currentQuestion.setSubjectId(lecture.getSubjectId());
					EdoFeedback feedback = request.getFeedback();
					feedback.setId(lecture.getId());
					currentQuestion.setFeedback(feedback);
					test.setCurrentQuestion(currentQuestion);
					map.setTest(test);
					map.setStudent(request.getStudent());
					testsDao.addQuestionQuery(map);
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

	public EdoServiceResponse getSolved(EdoServiceRequest request) {
		if(request.getTest() == null || request.getStudent() == null || request.getStudent().getId() == null) {
			LoggingUtil.logMessage("Invalid test input", LoggingUtil.saveTestLogger);
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
				 EdoLiveSession live = createLiveSession(classroomId, "Live session_" + new Date().getTime());
				 session.persist(live);
				 sessionId = live.getId();
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
				LoggingUtil.logMessage("Directory created for " + folder.getAbsolutePath() + " result is " + mkdirResult);
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
			
			writeFile(data, fileOutputStream);
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
				    LoggingUtil.logMessage("Uploaded file of " + length + " bytes at " + filePath + " for session " + sessionId);
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

	private EdoLiveSession createLiveSession(Integer classroomId, String sessionName) {
		EdoLiveSession live = new EdoLiveSession();
		live.setSessionName(sessionName);
		live.setClassroomId(classroomId);
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
			EdoLiveSession live = createLiveSession(channel.getId(), channel.getName());
			session.persist(live);
			List<EDOPackage> packages = new ArrayList<EDOPackage>();
			channel.setId(live.getId());
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
			response.setPackages(testsDao.getLiveSessions(request.getStudent().getCurrentPackage()));
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
				Float fileSize = VideoUtil.downloadRecordedFile(live.getClassroomId(), live.getId());
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
			
			/*String outputFolder = VIDEOS_PATH + sessionId + "/" + "merged.webm";

			boolean result = VideoUtil.mergeFiles(VIDEOS_PATH + sessionId + "/", outputFolder);
			if (result) {
				// Prepare video URL
				session = this.sessionFactory.openSession();
				List<EdoLiveSession> sessions = session.createCriteria(EdoLiveSession.class).add(Restrictions.eq("id", sessionId)).list();
				if (CollectionUtils.isNotEmpty(sessions)) {
					EdoLiveSession edoLiveSession = sessions.get(0);
					// Upload to Vimeo
					VimeoResponse vimeoResponse = VideoUtil.uploadFile(outputFolder, edoLiveSession.getSessionName(), "");
					if (vimeoResponse != null && vimeoResponse.getJson() != null && StringUtils.isNotBlank(vimeoResponse.getJson().getString("link"))) {

						Transaction tx = session.beginTransaction();
						edoLiveSession.setStatus("Completed");
						// sessions.get(0).setRecording_url(StringUtils.replace(outputFolder,
						// EdoPropertyUtil.getProperty(EdoPropertyUtil.VIDEO_OUTPUT),
						// EdoPropertyUtil.getProperty(EdoPropertyUtil.HOST_NAME)));
						edoLiveSession.setRecording_url(vimeoResponse.getJson().getString("link"));
						currentPackage.setVideoUrl("view-session.php?sessionId=" + sessionId + "&sessionName=" + edoLiveSession.getSessionName());
						List<EDOPackage> packages = new ArrayList<EDOPackage>();
						packages.add(currentPackage);
						response.setPackages(packages);
						tx.commit();
					}
				}

			} else {
				response.setStatus(new EdoApiStatus(-111, "Could not process video!"));
			}*/

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


	public EdoServiceResponse uploadVideo(InputStream videoData, EdoClasswork classwork, String maps, String fileName) {
		EdoServiceResponse response = new EdoServiceResponse();
		// EdoApiStatus status = new EdoApiStatus();
		Session session = null;
		try {

			// Check for quota first
			// TODO Add Later
			/*
			 * EDOInstitute institute = testsDao.getInstituteById(instituteId);
			 * if(institute != null && institute.getStorageQuota() != null &&
			 * institute.getStorageQuota() < 0) {
			 * LoggingUtil.logMessage("Video storage quota exceeded for " +
			 * instituteId, LoggingUtil.videoLogger); response.setStatus(new
			 * EdoApiStatus(-111,
			 * "Video storage quota exceeded .. Please upgrade your plan .."));
			 * return response; }
			 */
			String fileUrl = null;
			double length = 0;
			File savedFile = null;
			String filePath = null;
			if(StringUtils.isBlank(classwork.getFileUrl())) {
				String documentFolder = "temp/";
				if (StringUtils.equals(classwork.getType(), CONTENT_TYPE_DOCUMENT) || StringUtils.equals(classwork.getType(), CONTENT_TYPE_IMAGE)) {
					documentFolder = "documents/" + classwork.getInstituteId() + "/";
				}
				String path = VIDEOS_PATH + documentFolder;
				File folder = new File(path);
				Integer noOfFiles = null;
				if (!folder.exists()) {
					boolean mkdirResult = folder.mkdirs();
					LoggingUtil.logMessage("Directory created for " + folder.getAbsolutePath() + " result is " + mkdirResult, LoggingUtil.videoLogger);
					noOfFiles = 0;
				}
				String title = StringUtils.replace(fileName, "/", " ");
				title = StringUtils.replace(title, "\\", " ");
				if (StringUtils.equals(classwork.getType(), CONTENT_TYPE_DOCUMENT) || StringUtils.equals(classwork.getType(), CONTENT_TYPE_IMAGE)) {
					title = new Date().getTime() + "_" + title;
				}
				filePath = path + title;
				FileOutputStream fileOutputStream = new FileOutputStream(filePath);
				// IOUtils.copy(data, fileOutputStream);

				writeFile(videoData, fileOutputStream);
				
				session = this.sessionFactory.openSession();
				
				// Check size of file saved
				savedFile = new File(filePath);
				if (savedFile.exists()) {
					length = savedFile.length();
					LoggingUtil.logMessage("Uploading file " + title + " of " + length + " at " + filePath, LoggingUtil.videoLogger);
					// If video..upload to vimeo
					if (StringUtils.equals(classwork.getType(), CONTENT_TYPE_VIDEO)) {
						//Get embed domain for given institute
						String embedUrl = "erp.edofox.com";
						List<EdoConfig> configs = session.createCriteria(EdoConfig.class)
								.add(Restrictions.eq("name", "app_url"))
								.add(Restrictions.eq("instituteId", classwork.getInstituteId()))
								.list();
						if(CollectionUtils.isNotEmpty(configs)) {
							if(StringUtils.isNotBlank(configs.get(0).getValue())) {
								embedUrl = CommonUtils.getDomainName(configs.get(0).getValue());
							}
						}
						VimeoResponse vimeoResponse = VideoUtil.uploadFile(filePath, classwork.getTitle(), classwork.getDescription(), embedUrl);
						if (vimeoResponse != null && vimeoResponse.getJson() != null && StringUtils.isNotBlank(vimeoResponse.getJson().getString("link"))) {
							fileUrl = vimeoResponse.getJson().getString("link");
						}
					}
				}
			} else {
				fileUrl = classwork.getFileUrl();
			}
			
			if(StringUtils.isBlank(fileUrl) && StringUtils.isBlank(filePath)) {
				LoggingUtil.logMessage("File URL empty " + fileUrl + " for " + classwork.getTitle());
				response.setStatus(new EdoApiStatus(-111, "File URL not found"));
				return response;
			}
			
			if(session == null) {
				session = this.sessionFactory.openSession();
			}
			
			Transaction tx = session.beginTransaction();
			classwork.setDisabled(0);
			classwork.setSize(length);
			classwork.setCreatedDate(new Date());
			classwork.setFileUrl(StringUtils.trimToEmpty(fileUrl));
			classwork.setFileLoc(filePath);
			//Set date time to default 0
			if(classwork.getStartDate() != null) {
				classwork.setStartDate(CommonUtils.setZeroDate(classwork.getStartDate()));
			}
			if(classwork.getEndDate() != null) {
				classwork.setEndDate(CommonUtils.setZeroDate(classwork.getEndDate()));
			}
			classwork.setStatus("Pending");
			
			session.persist(classwork);
			//Add log
			EdoActivityLogs log = new EdoActivityLogs();
			log.setLoginId(classwork.getUploader());
			log.setModule("Classwork");
			log.setLogTime(new Date());
			log.setInstituteId(classwork.getInstituteId());
			String liveMessage = "";
			if(classwork.getStartDate() != null && classwork.getEndDate() != null) {
				liveMessage = " live from " + CommonUtils.convertDate(classwork.getStartDate()) + " to " + CommonUtils.convertDate(classwork.getEndDate());
			}
			log.setComment("Added new classwork " + classwork.getTitle() + " of type " + classwork.getType() + liveMessage);
			session.persist(log);
			
			// Add URL
			if (!StringUtils.equals(classwork.getType(), CONTENT_TYPE_VIDEO)) {
				String hostUrl = EdoPropertyUtil.getProperty(EdoPropertyUtil.HOST_URL);
				fileUrl = hostUrl + "document/" + classwork.getId();
				classwork.setFileUrl(fileUrl);
			}
			// Add mappings
			if (StringUtils.isNotBlank(maps)) {
				String[] divs = StringUtils.split(maps, ",");
				if (ArrayUtils.isNotEmpty(divs)) {
					for (String div : divs) {
						EdoClassworkMap map = new EdoClassworkMap();
						map.setClasswork(classwork.getId());
						if (StringUtils.contains(div, "C")) {
							// Add class
							Integer classId = new Integer(StringUtils.removeStart(div, "C"));
							map.setCourse(classId);
						} else {
							map.setDivision(new Integer(div));
						}
						session.persist(map);
					}
				}
			}

			// Remove the temp file
			if (StringUtils.equals(classwork.getType(), CONTENT_TYPE_VIDEO) && savedFile != null) {
				boolean delete = savedFile.delete();
				LoggingUtil.logMessage("Deleted the saved file " + delete + " at " + filePath, LoggingUtil.videoLogger);
			}
			tx.commit();

			// Update quota
			// TODO Add later
			/*
			 * EDOInstitute edoInstitute = new EDOInstitute();
			 * edoInstitute.setId(instituteId); BigDecimal bd =
			 * CommonUtils.calculateStorageUsed(new Float(length));
			 * edoInstitute.setStorageQuota(bd.doubleValue()); //Deduct quota
			 * from institute LoggingUtil.logMessage("Deducting quota " +
			 * edoInstitute.getStorageQuota() + " GBs from " +
			 * edoInstitute.getId(), LoggingUtil.videoLogger);
			 * testsDao.deductQuota(edoInstitute);
			 */


		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			response.setStatus(new EdoApiStatus(-111, ERROR_IN_PROCESSING));
		} finally {
			CommonUtils.closeSession(session);
		}
		return response;
	}

	private void writeFile(InputStream inputStream, FileOutputStream fileOutputStream) throws IOException {
		int read;
		byte[] bytes = new byte[1024];

		while ((read = inputStream.read(bytes)) != -1) {
			fileOutputStream.write(bytes, 0, read);
		}

		fileOutputStream.close();
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
				if (StringUtils.equals(student.getPassword(), edoStudent.getPassword())) {
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
		if(StringUtils.isBlank(request.getRequestType()) || request.getStudent() == null) {
			status.setStatus(-111, ERROR_INCOMPLETE_REQUEST);
			return status;
		}

		Session session = null;
		try {
			session = this.sessionFactory.openSession();
			Transaction tx = session.beginTransaction();
			List<EdoClassworkActivity> activity = session.createCriteria(EdoClassworkActivity.class)
					.add(Restrictions.eq("contentId", request.getFeedback().getId()))
					.add(Restrictions.eq("studentId", request.getFeedback().getStudentId())).list();
			if(CollectionUtils.isNotEmpty(activity)) {
				EdoClassworkActivity act = activity.get(0);
				setActivity(request, act);
			} else {
				EdoClassworkActivity act = new EdoClassworkActivity();
				act.setContentId(request.getFeedback().getId());
				act.setReadBy(request.getStudent().getId());
				act.setStudentId(request.getFeedback().getStudentId());
				setActivity(request, act);
				session.persist(act);
			}
			tx.commit();
			//testsDao.saveVideoActiviy(request);
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
		} finally {
			CommonUtils.closeSession(session);

		}
		return status;
	}

	private void setActivity(EdoServiceRequest request, EdoClassworkActivity act) {
		if(StringUtils.equalsIgnoreCase(act.getStatus(), "Completed")) {
			//Don't update if already marked Completed
			return;
		}
		act.setReadAt(new Date());
		if(request.getFeedback().getPercentViewed() != null) {
			act.setPercent(request.getFeedback().getPercentViewed());
		}
		if(request.getFeedback().getDurationViewed() != null) {
			act.setDuration(request.getFeedback().getDurationViewed());
		}
		if(StringUtils.isNotBlank(request.getRequestType())) {
			act.setStatus(request.getRequestType());
		}
		if(request.getFeedback().getDownloaded() > 0) {
			act.setDownloaded(request.getFeedback().getDownloaded());
		}
		
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
			} else {
				lecture.setVideoName(request.getLecture().getVideoName());
				lecture.setClassroomId(request.getLecture().getClassroomId());
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


	public EdoFile getDocument(Integer docId) {
		if(docId == null) {
			return null;
		}
		Session session = null;
		EdoFile file = null;
		try {
			session = this.sessionFactory.openSession();
			EdoClasswork classwork = (EdoClasswork) session.createCriteria(EdoClasswork.class).add(Restrictions.eq("id", docId)).uniqueResult();
			if(classwork != null) {
				file = new EdoFile();
				FileInputStream content = new FileInputStream(classwork.getFileLoc());
				file.setContent(content);
				file.setSize(new File(classwork.getFileLoc()).length());
				String extension = StringUtils.substringAfterLast(classwork.getFileLoc(), ".");
				file.setFileName(classwork.getTitle() + "." + extension);
				if(extension != null) {
					file.setContentType(CONTENT_TYPES.get(extension));
				}
			}
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
		} finally {
			CommonUtils.closeSession(session);
		}
		return file;
	}

	public EdoApiStatus sendEmail(EdoServiceRequest request) {
		EdoApiStatus status = new EdoApiStatus();
		try {
			List<EdoStudent> students = testsDao.getAllStudents(request.getInstitute().getId());
			EDOInstitute institute = testsDao.getInstituteById(request.getInstitute().getId());
			if(CollectionUtils.isNotEmpty(students)) {
				for(EdoStudent student: students) {
					student.setPassword("registered mobile number");
					EdoMailUtil mailUtil = new EdoMailUtil(MAIL_TYPE_INVITE);
					mailUtil.setStudent(student);
					mailUtil.setInstitute(institute);
					mailUtil.setMailer(request.getMailer());
					if(StringUtils.isNotBlank(student.getEmail())) {
						mailUtil.sendMail();
					}
				}
			}
			
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			status.setStatus(-111, ERROR_IN_PROCESSING);
		}
		return status;
	}

	public EdoFile getVideo(Integer docId) {
		if(docId == null) {
			return null;
		}
		Session session = null;
		EdoFile file = null;
		try {
			session = this.sessionFactory.openSession();
			EdoClasswork classwork = (EdoClasswork) session.createCriteria(EdoClasswork.class).add(Restrictions.eq("id", docId)).uniqueResult();
			if(classwork != null) {
				if(StringUtils.contains(classwork.getFileUrl(), "vimeo")) {
					file = new EdoFile();
					file.setDownloadUrl(VideoUtil.getDownloadUrl(classwork.getFileUrl()));
					file.setFileName(classwork.getTitle() + ".mp4");
					file.setContentType("video/mp4");
				}
			}
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
		} finally {
			CommonUtils.closeSession(session);
		}
		return file;
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
			EdoDeviceId deviceId = (EdoDeviceId) session.createCriteria(EdoDeviceId.class).add(Restrictions.eq("token", deviceIdInput.getToken())).uniqueResult();
			if(deviceId == null) {
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

	public EdoApiStatus sendNotification(EdoServiceRequest request) {
		EdoApiStatus status = new EdoApiStatus();
		try {
			EdoNotificationsManager mgr = new EdoNotificationsManager(this.sessionFactory);
			mgr.setNotificationType(request.getRequestType());
			mgr.setClasswork(request.getClasswork());
			mgr.setNotice(request.getNotice());
			mgr.setTestsDao(testsDao);
			LoggingUtil.logMessage("Executing notification task " + request.getRequestType(), LoggingUtil.emailLogger);
			executor.execute(mgr);
			
			//Send mail also
			if(request.getStudent() != null) {
				EdoStudent student = testsDao.getStudentById(request.getStudent().getId());
				student.setPassword("registered mobile number");
				EdoMailUtil mailUtil = new EdoMailUtil(request.getRequestType());
				mailUtil.setStudent(student);
				mailUtil.setInstitute(testsDao.getInstituteById(request.getInstitute().getId()));
				mailUtil.setMailer(request.getMailer());
				if(StringUtils.isNotBlank(student.getEmail())) {
					mailUtil.sendMail();
				}
			}
			
			
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			status.setStatus(-111, ERROR_IN_PROCESSING);
		}
		return status;
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
					String answersPath = ANSWERS_PATH + testId + "/";
					File folder = new File(answersPath);
					if(!folder.exists()) {
						folder.mkdirs();
					}
					EdoAnswerFileEntity answerFileEntity = new EdoAnswerFileEntity();
					answerFileEntity.setTestId(testId);
					answerFileEntity.setStudentId(studentId);
					answerFileEntity.setCreatedDate(new Date());
					session.persist(answerFileEntity);
					String filePath = answersPath + answerFileEntity.getId() +  "_" + bodyPart.getContentDisposition().getFileName();
					//answersPath = answersPath
					writeFile(bodyPartEntity.getInputStream(), new FileOutputStream(filePath));
					answerFileEntity.setFilePath(filePath);
					answerFileEntity.setFileUrl(CommonUtils.prepareAnswerURLs(answerFileEntity.getId()));
					
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
			test.setAnswerFiles(testsDao.getAnswerFiles(request));
			response.setTest(test);
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
		}
		return response;
	}

}
