package com.rns.web.edo.service.bo.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
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
import org.hibernate.criterion.Restrictions;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.clickntap.vimeo.VimeoResponse;
import com.rns.web.edo.service.bo.api.EdoFile;
import com.rns.web.edo.service.bo.api.EdoUserBo;
import com.rns.web.edo.service.dao.EdoTestsDao;
import com.rns.web.edo.service.domain.EDOInstitute;
import com.rns.web.edo.service.domain.EDOPackage;
import com.rns.web.edo.service.domain.EDOTestAnalysis;
import com.rns.web.edo.service.domain.EdoApiStatus;
import com.rns.web.edo.service.domain.EdoComplexOption;
import com.rns.web.edo.service.domain.EdoPaymentStatus;
import com.rns.web.edo.service.domain.EdoQuestion;
import com.rns.web.edo.service.domain.EdoServiceRequest;
import com.rns.web.edo.service.domain.EdoServiceResponse;
import com.rns.web.edo.service.domain.EdoStudent;
import com.rns.web.edo.service.domain.EdoStudentSubjectAnalysis;
import com.rns.web.edo.service.domain.EdoTest;
import com.rns.web.edo.service.domain.EdoTestQuestionMap;
import com.rns.web.edo.service.domain.EdoTestStudentMap;
import com.rns.web.edo.service.domain.jpa.EdoAnswerEntity;
import com.rns.web.edo.service.domain.jpa.EdoLiveSession;
import com.rns.web.edo.service.domain.jpa.EdoTestStatusEntity;
import com.rns.web.edo.service.util.CommonUtils;
import com.rns.web.edo.service.util.EdoConstants;
import com.rns.web.edo.service.util.EdoMailUtil;
import com.rns.web.edo.service.util.EdoPDFUtil;
import com.rns.web.edo.service.util.EdoPropertyUtil;
import com.rns.web.edo.service.util.EdoSMSUtil;
import com.rns.web.edo.service.util.LoggingUtil;
import com.rns.web.edo.service.util.PaymentUtil;
import com.rns.web.edo.service.util.QuestionParser;
import com.rns.web.edo.service.util.VideoUtil;

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
			
			for(EdoTestQuestionMap mapper: map) {
				EdoQuestion question = mapper.getQuestion();
				LoggingUtil.logMessage("Solution image ==> " + question.getSolutionImageUrl());
				CommonUtils.setQuestionURLs(question);
				QuestionParser.fixQuestion(question);
				
				//Add only if not disabled
				if(question.getDisabled() != null && question.getDisabled() == 1) {
					if(StringUtils.isBlank(question.getAnswer())) {
						continue;
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
			
			response.setTest(test);
		}
		
		return response;
	}

	public EdoServiceResponse getTest(Integer testId, Integer studenId) {
		EdoServiceResponse response = new EdoServiceResponse();
		if(testId == null) {
			response.setStatus(new EdoApiStatus(STATUS_ERROR, ERROR_INCOMPLETE_REQUEST));
			return response;
		}
		
		try {
			EdoTestStudentMap inputMap = new EdoTestStudentMap();
			inputMap.setTest(new EdoTest(testId));
			if(studenId != null) {
				inputMap.setStudent(new EdoStudent(studenId));
				List<EdoTestStudentMap> studentMaps = testsDao.getTestStatus(inputMap);
				EdoTestStudentMap studentMap = null;
				if(CollectionUtils.isNotEmpty(studentMaps)) {
					studentMap = studentMaps.get(0);
				}
				if(studentMap != null && StringUtils.equals(TEST_STATUS_COMPLETED, studentMap.getStatus())) {
					response.setStatus(new EdoApiStatus(STATUS_TEST_SUBMITTED, ERROR_TEST_ALREADY_SUBMITTED));
					return response;
				}
				
				//Added on 11/12/19
				if(studentMap == null) {
					//Add test status as 'STARTED' to track students who logged in
					EdoServiceRequest request = new EdoServiceRequest();
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
					testsDao.saveTestStatus(request);
				}
				//Added on 11/12/19
				
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
				
				if(!StringUtils.equalsIgnoreCase(studentMap.getStudentAccess(), ACCESS_LEVEL_ADMIN)) {
					if(!StringUtils.equalsIgnoreCase(STATUS_ACTIVE, studentMap.getStatus())) {
						response.setStatus(new EdoApiStatus(STATUS_TEST_NOT_ACTIVE, ERROR_TEST_NOT_ACTIVE));
						return response;
					}
					
					EdoTest mapTest = studentMap.getTest();
					if(mapTest != null) {
						if(mapTest.getStartDate() != null && mapTest.getStartDate().getTime() > new Date().getTime()) {
							response.setStatus(new EdoApiStatus(STATUS_TEST_NOT_OPENED, "Test will be availble on " + CommonUtils.convertDate(mapTest.getStartDate())));
							return response;
						}
						if(mapTest.getEndDate() != null && mapTest.getEndDate().getTime() < new Date().getTime()) {
							if(studentMap.getRegisterDate() != null && studentMap.getRegisterDate().getTime() < mapTest.getEndDate().getTime()) {
								response.setStatus(new EdoApiStatus(STATUS_TEST_EXPIRED, ERROR_TEST_EXPIRED));
								return response;
							}
						}
					}
				}
				
			}
			
			List<EdoTestQuestionMap> map = testsDao.getExam(testId);
			
			if(CollectionUtils.isNotEmpty(map)) {
				EdoTest result = map.get(0).getTest();
				//Check if time constraint is present
				if(StringUtils.equals("1", result.getTimeConstraint())) {
					Date startTime = result.getStartDate();
					if(startTime != null) {
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
						if(studenId != null) {
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
				if(isRandomizeQuestions(result) && studenId != null) {
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
					saveAnswer(saveAnswerRequest, session);
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
			}
			return file;
		} catch (Exception e) {
			LoggingUtil.logMessage(ExceptionUtils.getStackTrace(e));
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
					if(StringUtils.isBlank(pkg.getStatus())) {
						pkg.setStatus(status);
					}
				}
			}
			//student.setCurrentPackage(studentPackage);
			student.setRollNo(""); //For new student
			if(student.getId() == null) {
				//Check if the student with same phone exists
				List<EdoStudent> existingStudent = testsDao.getStudentByPhoneNumber(student);
				
				if(CollectionUtils.isEmpty(existingStudent)) {
					testsDao.saveStudent(student);
				} else {
					student.setId(existingStudent.get(0).getId());
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
			EDOInstitute institute = null;
			if(CollectionUtils.isNotEmpty(student.getPackages()) && student.getPackages().get(0).getInstitute() != null) {
				institute = testsDao.getInstituteById(student.getPackages().get(0).getInstitute().getId());
			}
			notifyStudent(student, MAIL_TYPE_SUBSCRIPTION, institute);
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			response.setStatus(new EdoApiStatus(STATUS_ERROR, ERROR_IN_PROCESSING));
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
		try {
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
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			response.setStatus(new EdoApiStatus(-111, ERROR_IN_PROCESSING));
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
					 live = createLiveSession(classroomId, live.getSessionName());
					 session.persist(live);
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
			
			int read;
			byte[] bytes = new byte[1024];

			while ((read = data.read(bytes)) != -1) {
				fileOutputStream.write(bytes, 0, read);
			}
			
			fileOutputStream.close();
			
			//Update meta data file
			FileWriter fileWriter = new FileWriter(path + "list.txt", true); //Set true for append mode
		    PrintWriter printWriter = new PrintWriter(fileWriter);
		    printWriter.println("file '" + filePath + "'");  //New line
		    printWriter.close();
		    
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

			// Output folder is not required as vimdeo is integrated

			/*
			 * String outputFolder =
			 * EdoPropertyUtil.getProperty(EdoPropertyUtil.VIDEO_OUTPUT); String
			 * fileExtension = request.getRequestType();
			 * if(StringUtils.isBlank(fileExtension)) { fileExtension = "webm";
			 * } outputFolder = outputFolder + "media/" + sessionId + "/"; File
			 * outputDir = new File(outputFolder); if(!outputDir.exists()) {
			 * outputDir.mkdirs(); } outputFolder = outputFolder + "merged." +
			 * fileExtension;
			 */

			String outputFolder = VIDEOS_PATH + sessionId + "/" + "merged.webm";

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
						currentPackage.setVideoUrl("view-session.php?sessionId=" + request.getStudent().getCurrentPackage().getId() + "&sessionName=" + edoLiveSession.getSessionName());
						List<EDOPackage> packages = new ArrayList<EDOPackage>();
						packages.add(currentPackage);
						response.setPackages(packages);
						tx.commit();
					}
				}

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
			EDOPackage liveSession = testsDao.getLiveSession(request.getStudent().getCurrentPackage().getId());
			if(liveSession != null) {
				liveSession.setVideoUrl(StringUtils.replace(liveSession.getVideoUrl(), "vimeo.com", "player.vimeo.com/video"));
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

}
