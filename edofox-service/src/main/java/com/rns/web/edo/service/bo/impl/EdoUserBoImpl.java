package com.rns.web.edo.service.bo.impl;

import java.io.FileInputStream;
import java.io.InputStream;
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
import com.rns.web.edo.service.domain.jpa.EdoTestStatusEntity;
import com.rns.web.edo.service.util.CommonUtils;
import com.rns.web.edo.service.util.EdoConstants;
import com.rns.web.edo.service.util.EdoMailUtil;
import com.rns.web.edo.service.util.EdoSMSUtil;
import com.rns.web.edo.service.util.LoggingUtil;
import com.rns.web.edo.service.util.PaymentUtil;
import com.rns.web.edo.service.util.QuestionParser;

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
		Session session = null;
		try {
			session = this.sessionFactory.openSession();
			Transaction tx = session.beginTransaction();
			saveAnswer(request, session);
			
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
			answer.setOptionSelected(StringUtils.replacePattern(request.getQuestion().getAnswer(), "[^a-zA-Z0-9\\s\\-\\,]", ""));
		}
		if(answer.getOptionSelected() == null) {
			answer.setOptionSelected("");
		}
		answer.setTimeTaken(request.getQuestion().getTimeSpent());
		//Will update only for save test
		answer.setMarks(request.getQuestion().getMarks());
		
		System.out.println("Saving answer .. " + answer.getOptionSelected());
		
		if(answer.getId() == null) {
			session.persist(answer);
		}
	}
	
	public EdoApiStatus saveTest(EdoServiceRequest request) {
		EdoTest test = request.getTest();
		if(request.getStudent() == null || test == null) {
			LoggingUtil.logMessage("Invalid test input", LoggingUtil.saveTestLogger);
			return new EdoApiStatus(-111, ERROR_IN_PROCESSING);
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
	
	public EdoFile getQuestionImage(Integer questionId, String imageType) {
		
		try {
			EdoFile file = new EdoFile();
			EdoQuestion question = testsDao.getQuestion(questionId);
			if(question == null) {
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
			if(student.getPayment() != null && student.getPayment().isOffline()) {
				student.getPayment().setMode("Offline");
			} else {
				EdoPaymentStatus payment = new EdoPaymentStatus();
				payment.setMode("Online");
				student.setPayment(payment);
			}
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
			notifyStudent(student, MAIL_TYPE_SUBSCRIPTION);
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			response.setStatus(new EdoApiStatus(STATUS_ERROR, ERROR_IN_PROCESSING));
		}
		return response;
	}

	private void notifyStudent(EdoStudent student, String mailType) {
		EdoMailUtil edoMailUtil = new EdoMailUtil(mailType);
		edoMailUtil.setStudent(student);
		executor.execute(edoMailUtil);
		
		EdoSMSUtil edoSMSUtil = new EdoSMSUtil(mailType);
		edoSMSUtil.setStudent(student);
		executor.execute(edoSMSUtil);
	}

	private void completePayment(EdoStudent student, EdoServiceResponse response) {
		LoggingUtil.logMessage("Transaction ID is =>" + student.getTransactionId());
		BigDecimal amount = BigDecimal.ZERO;
		for(EDOPackage p: student.getPackages()) {
			if(p.getPrice() != null && StringUtils.equals(student.getExamMode(), "Online")) {
				amount = p.getPrice().add(amount);
			} else if (p.getOfflinePrice() != null) {
				amount = p.getOfflinePrice().add(amount);
			}
		}
		if(!student.getPayment().isOffline()) {
			EdoPaymentStatus paymentResponse = PaymentUtil.paymentRequest(amount.doubleValue(), student, student.getTransactionId());
			if(paymentResponse != null && paymentResponse.getPaymentId() != null) {
				student.setPayment(paymentResponse);
				testsDao.updatePaymentId(student);
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
				EdoPaymentStatus paymentStatus = new EdoPaymentStatus();
				paymentStatus.setPaymentId(id);
				paymentStatus.setResponseText("Completed");
				paymentStatus.setOffline(false);
				testsDao.updatePayment(paymentStatus);
				List<EdoStudent> studentPackages = testsDao.getStudentByPayment(id);
				if(CollectionUtils.isNotEmpty(studentPackages)) {
					EdoStudent edoStudent = new EdoStudent();
					List<EDOPackage> packages = new ArrayList<EDOPackage>();
					for(EdoStudent student: studentPackages) {
						if(student.getCurrentPackage() != null) {
							packages.add(student.getCurrentPackage());
						}
						edoStudent.setName(student.getName());
						edoStudent.setPhone(student.getPhone());
						edoStudent.setEmail(student.getEmail());
					}
					if(transactionId != null) {
						edoStudent.setTransactionId(new Integer(StringUtils.removeStart(transactionId, "T")));
					}
					edoStudent.setPackages(packages);
					edoStudent.setPayment(paymentStatus);
					notifyStudent(edoStudent, MAIL_TYPE_ACTIVATED);
				}
			} else {
				EdoPaymentStatus paymentStatus = new EdoPaymentStatus();
				paymentStatus.setPaymentId(id);
				paymentStatus.setResponseText("Failed");
				testsDao.updatePayment(paymentStatus);
			}
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
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

}
