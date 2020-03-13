package com.rns.web.edo.service.bo.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.rns.web.edo.service.bo.api.EdoAdminBo;
import com.rns.web.edo.service.dao.EdoTestsDao;
import com.rns.web.edo.service.domain.EDOInstitute;
import com.rns.web.edo.service.domain.EDOPackage;
import com.rns.web.edo.service.domain.EDOQuestionAnalysis;
import com.rns.web.edo.service.domain.EdoAdminRequest;
import com.rns.web.edo.service.domain.EdoApiStatus;
import com.rns.web.edo.service.domain.EdoQuestion;
import com.rns.web.edo.service.domain.EdoServiceRequest;
import com.rns.web.edo.service.domain.EdoServiceResponse;
import com.rns.web.edo.service.domain.EdoStudent;
import com.rns.web.edo.service.domain.EdoStudentSubjectAnalysis;
import com.rns.web.edo.service.domain.EdoTest;
import com.rns.web.edo.service.domain.EdoTestQuestionMap;
import com.rns.web.edo.service.domain.EdoTestStudentMap;
import com.rns.web.edo.service.domain.jpa.EdoAnswerEntity;
import com.rns.web.edo.service.domain.jpa.EdoQuestionEntity;
import com.rns.web.edo.service.domain.jpa.EdoTestStatusEntity;
import com.rns.web.edo.service.domain.jpa.EdoUplinkStatus;
import com.rns.web.edo.service.util.CommonUtils;
import com.rns.web.edo.service.util.EdoConstants;
import com.rns.web.edo.service.util.EdoFirebaseUtil;
import com.rns.web.edo.service.util.EdoPDFUtil;
import com.rns.web.edo.service.util.EdoPropertyUtil;
import com.rns.web.edo.service.util.EdoSMSUtil;
import com.rns.web.edo.service.util.LoggingUtil;
import com.rns.web.edo.service.util.QuestionParser;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;

public class EdoAdminBoImpl implements EdoAdminBo, EdoConstants {
	
	private ThreadPoolTaskExecutor executor;
	private EdoTestsDao testsDao;
	private SessionFactory sessionFactory;
	private DataSourceTransactionManager txManager;
	
	public void setTxManager(DataSourceTransactionManager txManager) {
		this.txManager = txManager;
	}
	
	public DataSourceTransactionManager getTxManager() {
		return txManager;
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
	

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}



	public EdoServiceResponse getTestAnalysis(EdoTest test) {
		EdoServiceResponse response = new EdoServiceResponse();
		if(test == null || test.getId() == null) {
			response.setStatus(new EdoApiStatus(STATUS_ERROR, ERROR_INCOMPLETE_REQUEST));
			return response;
		}
		try {
			List<EdoTest> records = testsDao.getExamAnalysis(test.getId());
			
			if(CollectionUtils.isEmpty(records)) {
				return response;
			}
			
			EdoTest analysis = records.get(0);
			response.setTest(analysis);
			
			if(analysis == null || analysis.getId() == null || analysis.getAnalysis() == null || analysis.getAnalysis().getStudentsAppeared() == null) {
				LoggingUtil.logMessage("No test result found for ID .." + test.getId());
				//response.setStatus(new EdoApiStatus(STATUS_ERROR, ERROR_RESULT_NOT_FOUND));
				return response;
			}
			List<EdoQuestion> questionAnalysis = testsDao.getQuestionAnalysis(test.getId());
			if(CollectionUtils.isNotEmpty(questionAnalysis)) {
				Map<Integer, EdoQuestion> questionMap = new HashMap<Integer, EdoQuestion>();
				for(EdoQuestion question: questionAnalysis) {
					EdoQuestion existing = questionMap.get(question.getQn_id());
					if(existing == null) {
						existing = question;
					}
					EDOQuestionAnalysis qAnalysis = existing.getAnalysis();
					if(qAnalysis == null) {
						qAnalysis = new EDOQuestionAnalysis();
					}
					EDOQuestionAnalysis currentAnalysis = question.getAnalysis();
					if(currentAnalysis != null && currentAnalysis.getOptionCount() != null) {
						if(StringUtils.equalsIgnoreCase(ATTR_OPTION1, currentAnalysis.getOptionSelected())) {
							qAnalysis.setOption1Count(currentAnalysis.getOptionCount());
							qAnalysis.setOption1percent(CommonUtils.getPercent(currentAnalysis.getOptionCount(), analysis.getAnalysis().getStudentsAppeared()));
							if(StringUtils.equalsIgnoreCase(ATTR_OPTION1, question.getCorrectAnswer())) {
								qAnalysis.setCorrectCount(currentAnalysis.getOption1Count());
								qAnalysis.setCorrectPercent(qAnalysis.getOption1percent());
							}
						} else if(StringUtils.equalsIgnoreCase(ATTR_OPTION2, currentAnalysis.getOptionSelected())) {
							qAnalysis.setOption2Count(currentAnalysis.getOptionCount());
							qAnalysis.setOption2percent(CommonUtils.getPercent(currentAnalysis.getOptionCount(), analysis.getAnalysis().getStudentsAppeared()));
							if(StringUtils.equalsIgnoreCase(ATTR_OPTION2, question.getCorrectAnswer())) {
								qAnalysis.setCorrectCount(currentAnalysis.getOption1Count());
								qAnalysis.setCorrectPercent(qAnalysis.getOption1percent());
							}
						} else if(StringUtils.equalsIgnoreCase(ATTR_OPTION3, currentAnalysis.getOptionSelected())) {
							qAnalysis.setOption3Count(currentAnalysis.getOptionCount());
							qAnalysis.setOption3percent(CommonUtils.getPercent(currentAnalysis.getOptionCount(), analysis.getAnalysis().getStudentsAppeared()));
							if(StringUtils.equalsIgnoreCase(ATTR_OPTION3, question.getCorrectAnswer())) {
								qAnalysis.setCorrectCount(currentAnalysis.getOption1Count());
								qAnalysis.setCorrectPercent(qAnalysis.getOption1percent());
							}
						} else if(StringUtils.equalsIgnoreCase(ATTR_OPTION4, currentAnalysis.getOptionSelected())) {
							qAnalysis.setOption4Count(currentAnalysis.getOptionCount());
							qAnalysis.setOption4percent(CommonUtils.getPercent(currentAnalysis.getOptionCount(), analysis.getAnalysis().getStudentsAppeared()));
							if(StringUtils.equalsIgnoreCase(ATTR_OPTION4, question.getCorrectAnswer())) {
								qAnalysis.setCorrectCount(currentAnalysis.getOption1Count());
								qAnalysis.setCorrectPercent(qAnalysis.getOption1percent());
							}
						} 
					}
					existing.setAnalysis(qAnalysis);
					CommonUtils.setQuestionURLs(existing);
					QuestionParser.fixQuestion(existing);
					questionMap.put(question.getQn_id(), existing);
				}
				if(CollectionUtils.isNotEmpty(questionMap.values())) {
					analysis.setTest(new ArrayList<EdoQuestion>(questionMap.values()));
				}
				response.setTest(analysis);
			}
		} catch (Exception e) {
			LoggingUtil.logMessage(ExceptionUtils.getStackTrace(e));
		}
		return response;
	}

	public EdoServiceResponse getTestResults(EdoServiceRequest request) {
		EdoServiceResponse response = new EdoServiceResponse();
		EdoTest test = request.getTest();
		if(test == null || test.getId() == null) {
			response.setStatus(new EdoApiStatus(STATUS_ERROR, ERROR_INCOMPLETE_REQUEST));
			return response;
		}
		try {
			List<EdoTest> records = testsDao.getExamAnalysis(test.getId());
			
			if(CollectionUtils.isEmpty(records)) {
				return response;
			}
			
			EdoTest analysis = records.get(0);
			
			if(analysis == null || analysis.getId() == null || analysis.getAnalysis() == null || analysis.getAnalysis().getStudentsAppeared() == null) {
				LoggingUtil.logMessage("No test result found for ID .." + test.getId());
				response.setStatus(new EdoApiStatus(STATUS_ERROR, ERROR_RESULT_NOT_FOUND));
				return response;
			}
			
			EdoTest existing = testsDao.getTest(test.getId());
			if(existing == null) {
				return response;
			}
			
			EDOInstitute institute = null;
			if(request.getInstitute() != null) {
				institute = testsDao.getInstituteById(request.getInstitute().getId());
			}
			
			List<EdoStudent> students = testsDao.getStudentResults(test.getId());
			
			if(CollectionUtils.isNotEmpty(students)) {
				List<EdoTestStudentMap> subjectScores = testsDao.getSubjectwiseScore(test.getId());
				if(CollectionUtils.isNotEmpty(subjectScores)) {
					Integer rank = 0;
					for(EdoStudent student: students) {
						if(student.getAnalysis() == null) {
							continue;
						}
						rank++;
						student.getAnalysis().setRank(rank);
						student.getAnalysis().setTotalStudents(students.size());
						if(request.getStudent() != null && request.getStudent().getId() != student.getId()) {
							continue;
						}
						List<EdoStudentSubjectAnalysis> subjectAnalysis = CommonUtils.getSubjectAnalysis(existing, subjectScores, student);
						student.getAnalysis().setSubjectScores(subjectAnalysis);
						if(StringUtils.equalsIgnoreCase("SMS", request.getRequestType())) {
							//Send rank SMS to student and parents
							EdoSMSUtil smsUtil = new EdoSMSUtil(MAIL_TYPE_TEST_RESULT_RANK);
							smsUtil.setStudent(student);
							smsUtil.setTest(existing);
							smsUtil.setInstitute(institute);
							smsUtil.setAdditionalMessage(request.getSmsMessage());
							smsUtil.setCopyParent(true);
							executor.execute(smsUtil);
						}
						if(StringUtils.equalsIgnoreCase(REQUEST_FIREBASE_UPDATE, request.getRequestType())) {
							EdoFirebaseUtil.updateStudentResult(student, existing, institute.getFirebaseId());
						}
					}
				}
			}
			
			if(!StringUtils.equalsIgnoreCase("SMS", request.getRequestType())) {
				formatQuestions(existing);
				response.setTest(existing);
				response.setStudents(students);
			}
		} catch (Exception e) {
			LoggingUtil.logMessage(ExceptionUtils.getStackTrace(e));
		}
		return response;
	}


	private void formatQuestions(EdoTest existing) {
		if(existing != null && CollectionUtils.isNotEmpty(existing.getTest())) {
			for(EdoQuestion question: existing.getTest()) {
				QuestionParser.fixQuestion(question);
				CommonUtils.setQuestionURLs(question);
			}
		}
	}

	public EdoServiceResponse getAllStudents(EdoServiceRequest request) {
		EdoServiceResponse response = new EdoServiceResponse();
		EDOInstitute institute = request.getInstitute();
		if(institute == null || institute.getId() == null) {
			response.setStatus(new EdoApiStatus(STATUS_ERROR, ERROR_INCOMPLETE_REQUEST));
			return response;
		}
		try {
			List<EdoStudent> students = testsDao.getAllStudents(institute.getId());
			if(CollectionUtils.isNotEmpty(students)) {
				for(EdoStudent student: students) {
					if(StringUtils.equals(REQUEST_FIREBASE_UPDATE, request.getRequestType())) {
						EdoFirebaseUtil.updateStudent(student, institute.getFirebaseId());
					}
				}
			}
			if(!StringUtils.equals(REQUEST_FIREBASE_UPDATE, request.getRequestType())) {
				response.setStudents(students);
			}
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
		}
		return response;
	}

	@Transactional
	public EdoApiStatus fileUploadTestQuestions(String filePath, Integer firstQuestion, EdoTest test, Integer subjectId, String solutionPath) {
		EdoApiStatus status = new EdoApiStatus();
		if(StringUtils.isBlank(filePath) || firstQuestion == null || test == null || test.getId() == null) {
			status.setStatusCode(STATUS_ERROR);
			status.setResponseText(ERROR_INCOMPLETE_REQUEST);
			return status;
		}
		try {
			//String exceptionAn = null;
			EdoQuestion currentQuestion = test.getCurrentQuestion();
			if(currentQuestion == null || currentQuestion.getWeightage() == null) {
				currentQuestion = new EdoQuestion();
				currentQuestion.setWeightage(new Float(4));
				currentQuestion.setNegativeMarks(new Float(1));
			}
			
			List<EdoQuestion> questions = QuestionParser.parseQuestionPaper(filePath, firstQuestion, solutionPath);
			if(CollectionUtils.isNotEmpty(questions)) {
				LoggingUtil.logMessage("Total questions parsed =>" + questions.size());
				for(EdoQuestion question: questions) {
					question.setSubjectId(subjectId);
					question.setWeightage(currentQuestion.getWeightage());
					question.setNegativeMarks(currentQuestion.getNegativeMarks());
					testsDao.saveQuestion(question);
					if(question.getQn_id() != null) {
						test.setCurrentQuestion(question);
						testsDao.saveTestQuestion(test);
						LoggingUtil.logMessage("... Saved question .. " + question.getId() + ":" + question.getQn_id());
					}
				}
			}
		} catch (Exception e) {
			LoggingUtil.logMessage(ExceptionUtils.getStackTrace(e));
			status.setStatusCode(STATUS_ERROR);
			status.setResponseText(ERROR_IN_PROCESSING);
		}
		return status;
	}

	public EdoApiStatus fileUploadTestSolutions(String filePath, EdoTest test, Integer subjectId) {
		EdoApiStatus status = new EdoApiStatus();
		if(StringUtils.isBlank(filePath) || test == null || test.getId() == null) {
			status.setStatusCode(STATUS_ERROR);
			status.setResponseText(ERROR_INCOMPLETE_REQUEST);
			return status;
		}
		try {
			List<EdoQuestion> testQuestions = testsDao.getExamQuestions(test.getId());
			//String exceptionAn = null;
			if(CollectionUtils.isNotEmpty(testQuestions)) {
				for(EdoQuestion question: testQuestions) {
					if(question.getQuestionNumber() == null) {
						continue;
					}
					if(question.getSubjectId() != subjectId) {
						continue;
					}
					QuestionParser.parseSolution(question.getQuestionNumber(), question, filePath);
					testsDao.updateSolution(question);
					LoggingUtil.logMessage("Uploaded solution for .." + question.getQuestionNumber() + " : " + question.getQn_id());
				}
			}
		} catch (Exception e) {
			LoggingUtil.logMessage(ExceptionUtils.getStackTrace(e));
			status.setStatusCode(STATUS_ERROR);
			status.setResponseText(ERROR_IN_PROCESSING);
		}
		return status;
	}

	public EdoApiStatus revaluateResult(EdoServiceRequest request) {
		EdoApiStatus status = new EdoApiStatus();
		if(request.getTest().getId() == null || request.getTest() == null || request.getTest().getId() == null) {
			status.setResponseText(ERROR_INCOMPLETE_REQUEST);
			status.setStatusCode(STATUS_ERROR);
		}
		try {
		
			List<EdoQuestion> questions = testsDao.getExamQuestions(request.getTest().getId());
			Float bonus = new Float(0);
			Integer bonusCount = 0;
			/*for(EdoQuestion question : questions) {
				if(StringUtils.equalsIgnoreCase("bonus", question.getCorrectAnswer())) {
					bonus = bonus + question.getWeightage();
				}
			}*/
			
			EDOInstitute institute = null;
			if(request.getInstitute() != null) {
				institute = testsDao.getInstituteById(request.getInstitute().getId());
			}
			
			if(request.getStudent() == null) {
				List<EdoStudent> students = testsDao.getStudentResults(request.getTest().getId());
				if(CollectionUtils.isNotEmpty(students)) {
					for(EdoStudent student: students) {
						EdoServiceRequest req = new EdoServiceRequest();
						req.setTest(request.getTest());
						req.setStudent(student);
						evalulateStudent(req, questions, bonus, bonusCount, request.getRequestType(), institute, request.getSmsMessage());
					}
				}
			} else {
				evalulateStudent(request, questions, bonus, bonusCount, request.getRequestType(), institute, request.getSmsMessage());
			}
			
			
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			status.setStatusCode(STATUS_ERROR);
			status.setResponseText(ERROR_IN_PROCESSING);
		}
		return status;
	}

	private void evalulateStudent(EdoServiceRequest request, List<EdoQuestion> questions, Float bonus, Integer bonusCount, String type, EDOInstitute institute, String smsTxt) {
		LoggingUtil.logMessage("---- Evaluating => " + request.getStudent().getId());
		List<EdoTestQuestionMap> map = testsDao.getExamResult(request);
		List<EdoQuestion> solved = new ArrayList<EdoQuestion>();
		if(CollectionUtils.isNotEmpty(map)) {
			for(EdoTestQuestionMap mapper: map) {
				solved.add(mapper.getQuestion());
			}
			request.getTest().setName(map.get(0).getTest().getName());
			request.getTest().setTotalMarks(map.get(0).getTest().getTotalMarks());
			request.getTest().setTest(solved);
			CommonUtils.calculateTestScore(request.getTest(), questions);
			/*if(bonus != null) {
				BigDecimal finalScore = request.getTest().getScore().add(new BigDecimal(bonus));
				finalScore.setScale(2, RoundingMode.HALF_UP);
				request.getTest().setScore(finalScore);
				request.getTest().setSolvedCount(request.getTest().getSolvedCount() + bonusCount);
				LoggingUtil.logMessage("Added bonus .." + bonus + " so total is - " + request.getTest().getScore());
			}*/
			testsDao.updateTestStatus(request);
			if(CollectionUtils.isNotEmpty(request.getTest().getTest())) {
				for(EdoQuestion question: request.getTest().getTest()) {
					Map<String, Object> requestMap = new HashMap<String, Object>();
					requestMap.put("marks", question.getMarks());
					requestMap.put("test", request.getTest().getId());
					requestMap.put("student", request.getStudent().getId());
					requestMap.put("question", question.getQn_id());
					requestMap.put("answered", "");
					requestMap.put("flagged", 0);
					if(StringUtils.equals("bonus", question.getResponse())) {
						LoggingUtil.logMessage("Adding result for question => " + question.getQn_id() + " for " + request.getStudent().getId());
						testsDao.addTestResult(requestMap);
					} else {
						testsDao.updateTestResult(requestMap);
					}
						
				}
			}
			
			//testsDao.updateTestResult(request);
			if(StringUtils.equalsIgnoreCase("SMS", type)) {
				EdoSMSUtil smsUtil = new EdoSMSUtil(MAIL_TYPE_TEST_RESULT);
				smsUtil.setTest(request.getTest());
				EdoStudent student = testsDao.getStudentById(request.getStudent().getId());
				smsUtil.setStudent(student);
				smsUtil.setInstitute(institute);
				smsUtil.setAdditionalMessage(smsTxt);
				executor.execute(smsUtil);
			}
		}
	}

	public EdoApiStatus bulkUploadStudents(EdoServiceRequest request) {
		
		if(CollectionUtils.isEmpty(request.getStudents())) {
			return new EdoApiStatus(STATUS_ERROR, ERROR_INCOMPLETE_REQUEST);
		}
		
		EdoApiStatus status = new EdoApiStatus();
		
		try {
			LoggingUtil.logMessage("Adding total students ==> " + request.getStudents().size());
			for(EdoStudent student: request.getStudents()) {
				addStudent(request, student);
			}
			
		} catch (Exception e) {
			LoggingUtil.logMessage(ExceptionUtils.getStackTrace(e));
			status.setResponseText(ERROR_IN_PROCESSING);
			status.setStatusCode(STATUS_OK);
		}
		return status;
	}

	private void addStudent(EdoServiceRequest request, EdoStudent student) {
		List<EdoStudent> existingStudent = testsDao.getStudentByPhoneNumber(student);
		
		if(CollectionUtils.isEmpty(existingStudent)) {
			testsDao.saveStudent(student);
		} else {
			LoggingUtil.logMessage("Student already exists with phone number ... " + student.getPhone() + " and roll no " + student.getRollNo());
			student.setId(existingStudent.get(0).getId());
			testsDao.updateStudent(student);
		}
		
		LoggingUtil.logMessage("Student ID is =>" + student.getId());
		if(student.getId() != null) {
			testsDao.deleteExistingPackages(student);
			testsDao.createStudentPackage(student);
		}
		EDOInstitute currentInstitute = testsDao.getInstituteById(request.getInstitute().getId());
		EdoFirebaseUtil.updateStudent(student, currentInstitute.getFirebaseId());
		LoggingUtil.logMessage("Added student == " + student.getRollNo() + " successfully!");
	}

	public EdoServiceResponse parseQuestion(EdoServiceRequest request) {
		EdoServiceResponse response = CommonUtils.initResponse();
		try {
			EdoQuestion question = QuestionParser.parseHtml(request.getQuestion());
			response.setQuestion(question);
			if(question == null) {
				EdoApiStatus status = new EdoApiStatus();
				status.setResponseText("Problem in parsing question! Check the question HTML again ..");
				status.setStatusCode(STATUS_ERROR);
				response.setStatus(status);
				return response;
			}
			if(StringUtils.equals("JEE", question.getExamType()) || StringUtils.equals("JEEA", question.getExamType())) {
				question.setWeightage(4f);
				question.setNegativeMarks(1f);
			}
			testsDao.addQuestion(question);
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			EdoApiStatus status = new EdoApiStatus();
			status.setResponseText(ERROR_IN_PROCESSING);
			status.setStatusCode(STATUS_ERROR);
			response.setStatus(status);
		}
		return response;
	}

	public EdoServiceResponse getDataEntrySummary(EdoServiceRequest request) {
		EdoServiceResponse response = CommonUtils.initResponse();
		try {
			EDOQuestionAnalysis questionAnalysis = new EDOQuestionAnalysis();
			questionAnalysis.setQuestionsAddedByChapter(testsDao.getNoOfQuestionsByChapter(request.getQuestion().getChapter().getChapterId()));
			/*Calendar cal = Calendar.getInstance();
			cal.setTime(new Date());
			cal.set(Calendar.HOUR, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);*/
			String date = CommonUtils.convertDate(new Date());
			questionAnalysis.setQuestionsAddedByDate(testsDao.getNoOfQuestionsByDate(date));
			response.setQuestionAnalysis(questionAnalysis);
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
		}
		return response;
	}

	public EdoServiceResponse automateTest(EdoServiceRequest request) {
		EdoServiceResponse response = CommonUtils.initResponse();
		try {
			if(request.getTest() != null) {
				EdoQuestion question = request.getQuestion();
				if(question != null && question.getAnalysis() != null) {
					List<EdoQuestion> examQuestions = new ArrayList<EdoQuestion>();
					int startId = 1;
					Integer lastQuestion = testsDao.getLastQuestionNumber(request.getTest().getId());
					if(lastQuestion != null && lastQuestion > 0) {
						startId = lastQuestion;
					}
					if(StringUtils.equals("CET", question.getExamType())) {
						question.setType("SINGLE");
					 	question.setCorrect(true);
					} else if (StringUtils.isNotBlank(question.getAnalysis().getQuestionType())) {
						//Not select questions of provided type in normal case
						question.setType(question.getAnalysis().getQuestionType());
						question.setCorrect(false);
					}
					//List<EdoQuestion> typeBased = new ArrayList<EdoQuestion>();
					List<EdoQuestion> questions = new ArrayList<EdoQuestion>();
					int totalQuestions = 0;
					//Fetch hard level questions
					if(question.getAnalysis().getHardQuestionsCount() != null && question.getAnalysis().getHardQuestionsCount().intValue() != 0) {
						totalQuestions = totalQuestions + question.getAnalysis().getHardQuestionsCount();
						//question.setType(originalType);
						question.setLevel(5);
						question.setQuestionNumber(question.getAnalysis().getHardQuestionsCount());
						questions = testsDao.getNextQuestion(question);
						if(CollectionUtils.isNotEmpty(questions) && questions.size() == question.getAnalysis().getHardQuestionsCount().intValue()) {
							//addQuestionsToExam(request, questions, startId, request.getTest().getName());
							examQuestions.addAll(questions);
							//Fetch type based questions
							addTypeBasedQuestions(question, examQuestions);
							
						} /*else {
							response.setStatus(new EdoApiStatus(-111, "Insufficient hard type questions! Please change the count.."));
							return response;
						}*/
					}
					
					if(question.getAnalysis().getMediumQuestionsCount() != null && question.getAnalysis().getMediumQuestionsCount().intValue() != 0) {
						//Fetch medium level questions
						totalQuestions = totalQuestions + question.getAnalysis().getMediumQuestionsCount();
						//question.setType(originalType);
						question.setLevel(3);
						question.setQuestionNumber(question.getAnalysis().getMediumQuestionsCount());
						questions = testsDao.getNextQuestion(question);
						if(CollectionUtils.isNotEmpty(questions) && questions.size() == question.getAnalysis().getMediumQuestionsCount().intValue()) {
							examQuestions.addAll(questions);
							//Fetch type based questions
							addTypeBasedQuestions(question, examQuestions);
						} /*else {
							response.setStatus(new EdoApiStatus(-111, "Insufficient medium type questions! Please change the count.."));
							return response;
						}*/
						
					}
					
					if(question.getAnalysis().getEasyQuestionsCount() != null && question.getAnalysis().getEasyQuestionsCount().intValue() != 0) {
						//Fetch easy level questions
						totalQuestions = totalQuestions + question.getAnalysis().getEasyQuestionsCount();
						//question.setType(originalType);
						question.setLevel(1);
						question.setQuestionNumber(question.getAnalysis().getEasyQuestionsCount());
						questions = testsDao.getNextQuestion(question);
						if(CollectionUtils.isNotEmpty(questions) && questions.size() == question.getAnalysis().getEasyQuestionsCount().intValue()) {
							examQuestions.addAll(questions);
							//Fetch type based questions
							addTypeBasedQuestions(question, examQuestions);
						} /*else {
							response.setStatus(new EdoApiStatus(-111, "Insufficient easy type questions! Please change the count.."));
							return response;
						}*/
					}
					
					//Fill with random questions if insufficient
					
					if(examQuestions.size() < totalQuestions) {
						int buffer = (totalQuestions - examQuestions.size());
						EdoQuestion remaining = new EdoQuestion();
						remaining.setSubjectId(question.getSubjectId());
						remaining.setChapter(question.getChapter());
						remaining.setType(question.getType());
						remaining.setCorrect(question.isCorrect());
						remaining.setQuestionNumber(buffer);
						List<EdoQuestion> remainingQuestions = testsDao.getNextQuestion(remaining);
						if (CollectionUtils.isNotEmpty(remainingQuestions)) {
							examQuestions.addAll(remainingQuestions);
						}
					} else if (examQuestions.size() > totalQuestions) {
						//Remove some unwanted questions from the bottom
						List<EdoQuestion> finalQuestions = new ArrayList<EdoQuestion>();
						if(StringUtils.isNotBlank(question.getAnalysis().getQuestionType())) {
							for(EdoQuestion q: examQuestions) {
								if(StringUtils.equals(question.getAnalysis().getQuestionType(), q.getType())) {
									finalQuestions.add(q);
								}
							}
						}
						int buffer = totalQuestions - finalQuestions.size();
						int count = 0;
						for(EdoQuestion q: examQuestions) {
							if(!StringUtils.equals(question.getAnalysis().getQuestionType(), q.getType())) {
								finalQuestions.add(q);
								count++;
							}
							if(count >= buffer) {
								break;
							}
						}
						examQuestions = finalQuestions;
					}
					
					//shuffle questions before adding
					Collections.shuffle(examQuestions);
					addQuestionsToExam(request, examQuestions, startId, request.getTest().getName(), question);
					
				} else {
					question = new EdoQuestion();
					question.setExamType(request.getTest().getName());
					//Physics
					question.setSubjectId(1);
					List<EdoQuestion> questions = testsDao.getQuestionsByExam(question);
					Integer startId = 1;
					addQuestionsToExam(request, questions, startId, "Physics", null);
					//Chemistry
					startId = startId + questions.size();
					question.setSubjectId(3);
					question.setSubject("Chemistry");
					questions = testsDao.getQuestionsByExam(question);
					addQuestionsToExam(request, questions, startId, "Chemistry", null);
					//Maths
					startId = startId + questions.size();
					question.setSubjectId(2);
					question.setSubject("Maths");
					questions = testsDao.getQuestionsByExam(question);
					addQuestionsToExam(request, questions, startId, "Maths", null);
					//Biology
					startId = startId + questions.size();
					question.setSubjectId(4);
					question.setSubject("Biology");
					questions = testsDao.getQuestionsByExam(question);
					addQuestionsToExam(request, questions, startId, "Biology", null);
				}
			}
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			response.setStatus(new EdoApiStatus(-111, ERROR_IN_PROCESSING));
		}
		return response;
	}

	private void addTypeBasedQuestions(EdoQuestion question, List<EdoQuestion> questions) {
		Integer limit = question.getAnalysis().getTypeCount();
		if(StringUtils.isNotBlank(question.getAnalysis().getQuestionType()) && limit != null) {
			EdoQuestion typeQ = new EdoQuestion();
			typeQ.setLevel(question.getLevel());
			typeQ.setType(question.getAnalysis().getQuestionType());
			typeQ.setCorrect(true);
			typeQ.setSubjectId(question.getSubjectId());
			typeQ.setChapter(question.getChapter());
			//Calculate existing type questions
			int count = 0;
			if(CollectionUtils.isNotEmpty(questions)) {
				for(EdoQuestion q: questions) {
					if(StringUtils.equals(typeQ.getType(), q.getType())) {
						count++;
					}
				}
			}
			if(count >= limit) {
				return;
			}
			typeQ.setQuestionNumber(limit - count);
			List<EdoQuestion> typeQs = testsDao.getNextQuestion(typeQ);
			if(CollectionUtils.isNotEmpty(typeQs)) {
				questions.addAll(typeQs);
			}
		}
	}

	private void addQuestionsToExam(EdoServiceRequest request, List<EdoQuestion> questions, Integer startId, String subject, EdoQuestion question) {
		if(CollectionUtils.isNotEmpty(questions)) {
			for(EdoQuestion q: questions) {
				if(question == null || question.getWeightage() == null) {
					if(q.getWeightage() == null) {
						if(StringUtils.contains(request.getTest().getName(), "CET")) {
							if(q.getSubjectId() == 2) {
								q.setWeightage(2f);
							} else {
								q.setWeightage(1f);
							}
							q.setNegativeMarks(0f);
						} else {
							q.setWeightage(4f);
							q.setNegativeMarks(1f);
						}
					}
				} else {
					q.setWeightage(question.getWeightage());
					q.setNegativeMarks(question.getNegativeMarks());
				}
				q.setQn_id(startId);
				q.setSubject(subject);
				startId++;
			}
			request.getTest().setTest(questions);
			testsDao.createExam(request);
		}
	}

	public EdoServiceResponse addFeedbackResolution(EdoServiceRequest request) {
		EdoServiceResponse response = CommonUtils.initResponse();
		try {
			testsDao.addResolution(request.getFeedback());
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			response.setStatus(new EdoApiStatus(-111, ERROR_IN_PROCESSING));
		}
		return response;
	}

	public EdoServiceResponse getFeedbackData(EdoServiceRequest request) {
		EdoServiceResponse response = CommonUtils.initResponse();
		try {
			EdoTest test = new EdoTest();
			request.setFromDate(CommonUtils.getStartDate(request.getFromDate()));
			request.setToDate(CommonUtils.getEndDate(request.getToDate()));
			List<EdoQuestion> feedbackData = testsDao.getFeedbackData(request);
			if(CollectionUtils.isNotEmpty(feedbackData)) {
				for(EdoQuestion edoFeedback: feedbackData) {
					QuestionParser.fixQuestion(edoFeedback);
				}
			}
			test.setTest(feedbackData);
			response.setTest(test); 
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			response.setStatus(new EdoApiStatus(-111, ERROR_IN_PROCESSING));
		}
		return response;
	}

	public EdoServiceResponse getQuestionFeedbacks(EdoServiceRequest request) {
		EdoServiceResponse response = CommonUtils.initResponse();
		try {
			if(request.getQuestion() != null) {
				response.setFeedbacks(testsDao.getQuestionFeedbacks(request.getQuestion().getId()));
			}
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			response.setStatus(new EdoApiStatus(-111, ERROR_IN_PROCESSING));
		}
		return response;
	}

	public EdoApiStatus registerStudent(EdoServiceRequest request) {
		EdoApiStatus status = new EdoApiStatus();
		try {
			if(request.getInstitute() == null || request.getInstitute().getId() == null || StringUtils.isBlank(request.getInstitute().getFirebaseId())) {
				status.setStatus(-111, "Please provide valid institute information ..");
				return status;
			}
			
			EdoStudent student = request.getStudent();
			if(student == null) {
				status.setStatus(-111, "Please provide valid student information ..");
				return status;
			}
			
			if(StringUtils.isBlank(student.getName())) {
				status.setStatus(-111, "Please provide valid student name ..");
				return status;
			}
			
			if(StringUtils.isBlank(student.getPhone())) {
				status.setStatus(-111, "Please provide valid student phone number ..");
				return status;
			}
			
			if(CollectionUtils.isEmpty(student.getPackages())) {
				status.setStatus(-111, "Please provide atleast one valid package ..");
				return status;
			}
			
			if(StringUtils.isBlank(student.getPassword())) {
				student.setPassword("12345");
			}
			
			List<EdoStudent> existingStudent = testsDao.getStudentByPhoneNumber(student);
			if(CollectionUtils.isNotEmpty(existingStudent)) {
				status.setStatus(-222, "This mobile number is already registered ..");
				return status;
			}
			
			addStudent(request, student);
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			status = new EdoApiStatus(-111, ERROR_IN_PROCESSING);
		}
		return status;
	}
	
	public void fixQuestions() {
		Session session = null;
		try {
			session = this.sessionFactory.openSession();
			int i = 3;
			for(i = 3; i <= 100; i++) {
				LoggingUtil.logMessage("................ Started for chapter .. " + i);
				List<EdoQuestionEntity> noAnswerList = session.createCriteria(EdoQuestionEntity.class)
						.add(Restrictions.isNull("correctAnswer")).add(Restrictions.eq("chapter", i)).add(Restrictions.eq("status", "A")).list();
				List<EdoQuestionEntity> hasAnswerList = session.createCriteria(EdoQuestionEntity.class)
						.add(Restrictions.isNotNull("correctAnswer")).add(Restrictions.eq("chapter", i)).add(Restrictions.eqOrIsNull("status", "D")).list();
				
				if(CollectionUtils.isNotEmpty(noAnswerList)) {
					Transaction tx = session.beginTransaction();
					for(EdoQuestionEntity noAnswer: noAnswerList) {
						if(CollectionUtils.isNotEmpty(hasAnswerList)) {
							for(EdoQuestionEntity hasAnswer: hasAnswerList) {
								if (StringUtils.equals(hasAnswer.getReferenceId(), noAnswer.getReferenceId())) {
									noAnswer.setCorrectAnswer(hasAnswer.getCorrectAnswer());
									LoggingUtil.logMessage("Setting answer for " + noAnswer.getId() + " as " + hasAnswer.getCorrectAnswer());
									break;
								}
							}
						}
					}
					tx.commit();
					LoggingUtil.logMessage("................ Committed for chapter .. " + i);
				}
			}
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
		} finally {
			if(session != null) {
				session.close();
			}
		}
	}

	public EdoApiStatus cropQuestionImage(EdoServiceRequest request, InputStream fileData) {
		EdoApiStatus status = new EdoApiStatus();
		if (request.getTest() == null || request.getTest().getId() == null || StringUtils.isBlank(request.getFilePath())) {
			status.setStatus(-111, ERROR_INCOMPLETE_REQUEST);
			return status;
		}
		try {
			// byte[] buffer = new byte[fileData.available()];
			// fileData.read(buffer);
			FileOutputStream fileOutputStream = new FileOutputStream(TEMP_QUESTION_PATH + request.getTest().getId() + "/" + request.getFilePath());
			//IOUtils.copy(fileData, fileOutputStream);
			//FileOutputStream out = new FileOutputStream(new File(fileLocation));
			int read = 0;
			byte[] bytes = new byte[1024];
			while ((read = fileData.read(bytes)) != -1) {
				fileOutputStream.write(bytes, 0, read);
			}
			fileOutputStream.flush();
			fileOutputStream.close();

		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			status.setStatus(-111, ERROR_IN_PROCESSING);
		}
		return status;
	}

	public EdoApiStatus backupData(EdoAdminRequest request) {
		if(CollectionUtils.isEmpty(request.getTestQuestionMaps())) {
			return new EdoApiStatus(-111, ERROR_INCOMPLETE_REQUEST);
		}
		
		Session session = null;
		EdoApiStatus status = new EdoApiStatus();
		try {
			
			LoggingUtil.logMessage(".... Uplink started for " + request.getHostName());
			
			session = this.sessionFactory.openSession();
			Transaction tx = session.beginTransaction();
			
			saveData(request, session);

			tx.commit();
			
			LoggingUtil.logMessage(".... Uplink successful for " + request.getHostName() + " ..........");
			
		} catch (Exception e) {
			LoggingUtil.logError("Error in uplink of " + request.getHostName() + " ==== " + ExceptionUtils.getStackTrace(e));
			status.setStatus(-111, ERROR_IN_PROCESSING);
		} finally {
			CommonUtils.closeSession(session);
		}
		return status;
	}
	
	private void saveData(EdoAdminRequest request, Session session) {
		
		int statusCount = 0;
		int solvedCount = 0;
		
		if(CollectionUtils.isNotEmpty(request.getTestStudentMaps())) {
			for(EdoTestStudentMap map: request.getTestStudentMaps()) {
				
				if(map.getTest() == null || map.getStudent() == null || map.getStudent().getId() == null) {
					continue;
				}
				
				//Check if student exists..if not .. skip the student
				EdoStudent existingStudent = testsDao.getStudentById(map.getStudent().getId());
				if(existingStudent == null) {
					LoggingUtil.logMessage("Student does not exist for " + map.getStudent().getId() + " .. so skipping ..");
					continue;
				}
				
				List<EdoTestStatusEntity> maps = /*testsDao.getTestStatus(inputMap)*/ session.createCriteria(EdoTestStatusEntity.class)
						.add(Restrictions.eq("testId", map.getTest().getId()))
						.add(Restrictions.eq("studentId", map.getStudent().getId()))
						.list();
				EdoTestStatusEntity dbMap = null;
				if(CollectionUtils.isNotEmpty(maps)) {
					dbMap = maps.get(0);
				} else {
					dbMap = new EdoTestStatusEntity();
					dbMap.setCreatedDate(new Date());
					dbMap.setTestId(map.getTest().getId());
					dbMap.setStudentId(map.getStudent().getId());
				}
				dbMap.setStatus(map.getStatus());
				dbMap.setFlagged(map.getTest().getFlaggedCount());
				dbMap.setSolved(map.getTest().getSolvedCount());
				if(dbMap.getId() == null) {
					session.persist(dbMap);
				}
				statusCount++;
			}
		}
		
		if(CollectionUtils.isNotEmpty(request.getTestQuestionMaps())) {
			for(EdoTestQuestionMap map: request.getTestQuestionMaps()) {
				saveAnswer(map, session);
				solvedCount++;
			}
		}
		
		EdoUplinkStatus uplinkStatus = new EdoUplinkStatus();
		uplinkStatus.setCreatedDate(new Date());
		uplinkStatus.setHostLocation(request.getHostName());
		uplinkStatus.setQuestionsUpdated(solvedCount);
		uplinkStatus.setStudentsUpdated(statusCount);
		session.persist(uplinkStatus);
		
		LoggingUtil.logMessage(".... Backup done for " + request.getHostName() + " for questions " + solvedCount + " of students ... " + statusCount);
		
	}
	
	private void saveAnswer(EdoTestQuestionMap request, Session session) {
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
		
		System.out.println("Saving answer .. " + answer.getOptionSelected());
		
		if(answer.getId() == null) {
			session.persist(answer);
		}
	}
	
	public EdoApiStatus uplinkData(EdoAdminRequest request) {
		if(request.getDate() == null) {
			return new EdoApiStatus(-111, ERROR_INCOMPLETE_REQUEST);
		}
		Session session = null;
		EdoApiStatus status = new EdoApiStatus();
		try {
			session = this.sessionFactory.openSession();
			//Fetch test status after specied date
			EdoAdminRequest uplinkRequest = getDataForBackup(request, session);
			if(uplinkRequest != null && (CollectionUtils.isNotEmpty(uplinkRequest.getTestQuestionMaps()) || CollectionUtils.isNotEmpty(uplinkRequest.getTestStudentMaps()))) {
				String hostLocation = EdoPropertyUtil.getProperty(EdoPropertyUtil.UPLINK_LOCATION);
				uplinkRequest.setHostName(hostLocation);
				//Send data uplink
				EdoServiceResponse uplinkResponse = connectToServer(uplinkRequest, "backup", EdoServiceResponse.class);
				if(uplinkResponse != null && uplinkResponse.getStatus() != null && uplinkResponse.getStatus().getStatusCode() == 200) {
					Transaction tx = session.beginTransaction();
					
					EdoUplinkStatus uplinkStatus = new EdoUplinkStatus();
					uplinkStatus.setCreatedDate(new Date());
					uplinkStatus.setHostLocation("local-" + hostLocation);
					uplinkStatus.setHostLocation(request.getHostName());
					if(uplinkRequest.getTestQuestionMaps() != null) {
						uplinkStatus.setQuestionsUpdated(uplinkRequest.getTestQuestionMaps().size());
					}
					if(uplinkRequest.getTestStudentMaps() != null) {
						uplinkStatus.setStudentsUpdated(uplinkRequest.getTestStudentMaps().size());
					}
					session.persist(uplinkStatus);
					
					tx.commit();
				} else {
					status.setStatus(-111, ERROR_IN_PROCESSING);
				}
			}
			
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			status.setStatus(-111, ERROR_IN_PROCESSING);
		} finally {
			CommonUtils.closeSession(session);
		}
		return status;
	}

	private EdoAdminRequest getDataForBackup(EdoAdminRequest request, Session session) {
		List<EdoTestStatusEntity> statuses = session.createCriteria(EdoTestStatusEntity.class)
				.add(Restrictions.ge("updatedDate", request.getDate())).list();
		
		EdoAdminRequest uplinkRequest = new EdoAdminRequest();
		if(CollectionUtils.isNotEmpty(statuses)) {
			List<EdoTestStudentMap> studentMaps = new ArrayList<EdoTestStudentMap>();
			for(EdoTestStatusEntity ts: statuses) {
				EdoTestStudentMap studentMap = new EdoTestStudentMap();
				//student
				EdoStudent student = new EdoStudent();
				student.setId(ts.getStudentId());
				studentMap.setStudent(student);
				//test
				EdoTest test = new EdoTest();
				test.setId(ts.getTestId());
				test.setSolvedCount(ts.getSolved());
				test.setFlaggedCount(ts.getFlagged());
				studentMap.setTest(test);
				studentMap.setStatus(ts.getStatus());
				studentMaps.add(studentMap);
			}
			uplinkRequest.setTestStudentMaps(studentMaps);
		}
		
		//Fetch test questions after specied date
		List<EdoAnswerEntity> answers = session.createCriteria(EdoAnswerEntity.class)
				.add(Restrictions.ge("updatedDate", request.getDate())).list();
		if(CollectionUtils.isNotEmpty(answers)) {
			List<EdoTestQuestionMap> questionMaps = new ArrayList<EdoTestQuestionMap>();
			for(EdoAnswerEntity answer: answers) {
				EdoTestQuestionMap questionMap = new EdoTestQuestionMap();
				EdoQuestion question = new EdoQuestion();
				question.setId(answer.getQuestionId());
				question.setQn_id(question.getId());
				question.setAnswer(answer.getOptionSelected());
				question.setTimeSpent(answer.getTimeTaken());
				question.setFlagged(answer.getFlagged());
				questionMap.setQuestion(question);
				EdoTest test = new EdoTest();
				test.setId(answer.getTestId());
				questionMap.setTest(test);
				EdoStudent student = new EdoStudent();
				student.setId(answer.getStudentId());
				questionMap.setStudent(student);
				questionMaps.add(questionMap);
			}
			uplinkRequest.setTestQuestionMaps(questionMaps);
		}
		return uplinkRequest;
	}
	
	public <T> T  connectToServer(EdoAdminRequest request, String method, Class<T> type) {
		try {
			ClientConfig config = new DefaultClientConfig();
			config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
			Client client = Client.create(config);
			client.setConnectTimeout(10000); //10 seconds
			String url = EdoPropertyUtil.getProperty(EdoPropertyUtil.UPLINK_SERVER) + method;
			WebResource webResource = client.resource(url);
			LoggingUtil.logMessage("Calling uplink with URL =>" + url);
			
			ClientResponse response = webResource.type(MediaType.APPLICATION_JSON).post(ClientResponse.class, request);
			//ClientResponse response = webResource.queryParam("receiverId", id.toString()).post(ClientResponse.class);

			String output = response.getEntity(String.class);
			if (response.getStatus() != 200) {
				throw new RuntimeException("Failed : HTTP error code : " + response.getStatus() + " RESP:" + output);
			}
			LoggingUtil.logMessage("Output from uplink URL ...." + response.getStatus() + " RESP:" + output + " \n");
			return new ObjectMapper().readValue(output, type);

		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
		}
		return null;
	}

	public EdoAdminRequest downloadData(EdoAdminRequest request) {
		if(request.getDate() == null) {
			return null;
		}
		Session session = null;
		EdoAdminRequest uplinkRequest = new EdoAdminRequest();
		try {
			session = this.sessionFactory.openSession();
			//Fetch test status after specied date
			uplinkRequest = getDataForBackup(request, session);
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
		} finally {
			CommonUtils.closeSession(session);
		}
		return uplinkRequest;
	}

	public EdoApiStatus downlinkData(EdoAdminRequest request) {
		if(request.getDate() == null) {
			return new EdoApiStatus(-111, ERROR_INCOMPLETE_REQUEST);
		}
		Session session = null;
		EdoApiStatus status = new EdoApiStatus();
		try {
			session = this.sessionFactory.openSession();
			//download data after specied date
			EdoAdminRequest response = connectToServer(request, "download", EdoAdminRequest.class);
			if(response != null) {
				Transaction tx = session.beginTransaction();
				saveData(response, session);
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

	public EdoAdminRequest getLastUplinkDate() {
		Session session = null;
		EdoAdminRequest response = new EdoAdminRequest();
		try {
			session = this.sessionFactory.openSession();
			//Last uplink date
			Criteria latestUplinks = session.createCriteria(EdoUplinkStatus.class).addOrder(org.hibernate.criterion.Order.desc("createdDate"));
			latestUplinks.setMaxResults(1);
			List<EdoUplinkStatus> statuses = latestUplinks.list();
			if(CollectionUtils.isNotEmpty(statuses)) {
				response.setDate(statuses.get(0).getCreatedDate());
			}
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
		} finally {
			CommonUtils.closeSession(session);
		}
		return response;
	}

	public EdoServiceResponse parsePdf(EdoAdminRequest request, InputStream fileData) {
		EdoServiceResponse response = new EdoServiceResponse();
		try {
			String folderPath = TEMP_QUESTION_PATH + request.getTest().getId() + "/";
			File dir = new File(folderPath);
			if(dir.exists()) {
				FileUtils.cleanDirectory(dir);
			}
			if(request.getBuffer() == null) {
				request.setBuffer(10);
			}
			if(request.getQuestionPrefix() == null) {
				request.setQuestionPrefix("");
			}
			if(request.getQuestionSuffix() == null) {
				request.setQuestionSuffix("");
			}
			request.getTest().setTest(EdoPDFUtil.pdfBox(request.getQuestionSuffix(), request.getQuestionPrefix(), fileData, folderPath , request.getBuffer(), request.getTest().getId(), request.getFromQuestion(), request.getToQuestion()));
			response.setTest(request.getTest()); 
		} catch (Exception e) {
			response.setStatus(new EdoApiStatus(-111, ERROR_IN_PROCESSING));
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
		}
		return response;
	}

	public EdoServiceResponse loadParsedQuestions(EdoServiceRequest request) {
		EdoServiceResponse response = new EdoServiceResponse();
		try {
			String folderPath = TEMP_QUESTION_PATH + request.getTest().getId() + "/";
			File folder = new File(folderPath);
			if(folder.exists()) {
				List<EdoQuestion> questions = new ArrayList<EdoQuestion>();
				for(File file: folder.listFiles()) {
					String[] values = StringUtils.split(file.getName(), "-");
					if(ArrayUtils.isNotEmpty(values) && values.length > 1) {
						EdoQuestion question = new EdoQuestion();
						Integer questionNumber = new Integer(StringUtils.removeEnd(values[1], ".png"));
						question.setQuestionNumber(questionNumber);
						question.setQuestionImageUrl(EdoPDFUtil.getQuestionUrl(request.getTest().getId(), questionNumber));
						questions.add(question);
					}
				}
				Collections.sort(questions, new Comparator<EdoQuestion>() {

					public int compare(EdoQuestion o1, EdoQuestion o2) {
						if(o1 != null && o2 != null && o1.getQuestionNumber() != null && o2.getQuestionNumber() != null) {
							if(o1.getQuestionNumber() < o2.getQuestionNumber()) {
								return -1;
							} else if (o1.getQuestionNumber() > o2.getQuestionNumber()) {
								return 1;
							} else if (o1.getQuestionNumber() == o2.getQuestionNumber()) {
								return 0;
							}
						}
						return 0;
					}
				});
				request.getTest().setTest(questions);
				response.setTest(request.getTest());
			}
		} catch (Exception e) {
			response.setStatus(new EdoApiStatus(-111, ERROR_IN_PROCESSING));
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
		}
		return response;
	}

	public EdoApiStatus saveParsedQuestions(EdoServiceRequest request) {
		if(request.getTest() == null || CollectionUtils.isEmpty(request.getTest().getTest())) {
			return new EdoApiStatus(-111, ERROR_INCOMPLETE_REQUEST);
		}
		EdoApiStatus status = new EdoApiStatus();
		TransactionStatus txStatus = txManager.getTransaction(new DefaultTransactionDefinition());
		try {
			String sourceDir = TEMP_QUESTION_PATH + request.getTest().getId();
			File source = new File(sourceDir);
			String destinationDir = TEST_QUESTION_PATH /*+ request.getTest().getId()*/;
			File destination = new File(destinationDir);
			if(destination.exists()) {
				//destination.mkdirs();
				FileUtils.deleteDirectory(destination);
			}
			LoggingUtil.logMessage("Moving temp directory " + sourceDir + " to " + destinationDir);
			FileUtils.copyDirectoryToDirectory(source, destination);
			for(EdoQuestion question: request.getTest().getTest()) {
				question.setQuestionImageUrl(destinationDir + request.getTest().getId()  + "/" + EdoPDFUtil.QUESTION_PREFIX + question.getQuestionNumber() + ".png");
				if(StringUtils.isBlank(question.getType())) {
					question.setType("SINGLE");
				}
				if(question.getExamType() == null) {
					question.setExamType("");
				}
				if(question.getCorrectAnswer() == null) {
					question.setCorrectAnswer("");
				}
				if(question.getNegativeMarks() == null) {
					question.setNegativeMarks(0f);
				}
				if(question.getWeightage() == null) {
					question.setWeightage(1f);
				}
				if(StringUtils.isBlank(question.getOption1())) {
					question.setOption1("1)");
					question.setOption2("2)");
					question.setOption3("3)");
					question.setOption4("4)");
				}
				if(question.getQn_id() == null) {
					question.setQn_id(question.getQuestionNumber());
				}
				testsDao.addQuestion(question);
			}
			testsDao.createExam(request);
			txManager.commit(txStatus);
			FileUtils.deleteDirectory(source);
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			status.setStatus(-111,ERROR_IN_PROCESSING);
			try {
				txManager.rollback(txStatus);
			} catch (Exception e2) {
				LoggingUtil.logError(ExceptionUtils.getStackTrace(e2));
			}
		}
		return status;
	}

	public EdoApiStatus createInstitute(EdoAdminRequest request) {
		if(request.getInstitute() == null || StringUtils.isBlank(request.getInstitute().getName())) {
			return new EdoApiStatus(-111, ERROR_INCOMPLETE_REQUEST);
		}
		EdoApiStatus status = new EdoApiStatus();
		TransactionStatus txStatus = txManager.getTransaction(new DefaultTransactionDefinition());
		try {
			Date expiryDate = DateUtils.addDays(new Date(), 30);
			EDOInstitute institute = request.getInstitute();
			institute.setExpiryDate(expiryDate);
			//Create institute and login
			testsDao.saveInstitute(institute);
			testsDao.createAdminLogin(institute);
			LoggingUtil.logMessage("Institute " + institute.getId() + " and login " + institute.getUsername() + " created ..");
			//Add default package
			EDOPackage pkg = new EDOPackage();
			pkg.setInstitute(institute);
			pkg.setName("Default");
			testsDao.createPackage(pkg);
			LoggingUtil.logMessage("Institute package " + pkg.getId() + " created ..");
			//Add first student
			EdoStudent student = request.getStudent();
			if(request.getStudent() != null && pkg.getId() != null) {
				if(student.getPhone() == null) {
					student.setPhone(institute.getContact());
				}
				EdoServiceRequest addStudentRequest = new EdoServiceRequest();
				addStudentRequest.setInstitute(institute);
				List<EDOPackage> packages = new ArrayList<EDOPackage>();
				packages.add(pkg);
				student.setPackages(packages);
				addStudent(addStudentRequest, student);
			}
			//Add a test
			if(pkg.getId() != null && student != null) {
				EdoTestStudentMap map = new EdoTestStudentMap();
				student.setCurrentPackage(pkg);
				map.setStudent(student);
				EdoTest test = new EdoTest();
				test.setStartDate(new Date());
				test.setEndDate(expiryDate);
				map.setTest(test);
				if(request.isCet()) {
					map.getTest().setName("CET Practice test");
					map.getTest().setNoOfQuestions(200);
					map.getTest().setDuration(10800);
					map.getTest().setTestUi("JEE");
					testsDao.addTest(map);
					EdoServiceRequest automateTestRequest = new EdoServiceRequest();
					EdoTest automateTest = new EdoTest();
					automateTest.setId(map.getTest().getId());
					automateTest.setName("CET19");
					automateTestRequest.setTest(automateTest);
					//Prepare a CET sample test
					automateTest(automateTestRequest);
					LoggingUtil.logMessage("CET Test created ..");
				}
				if(request.isJee()) {
					map.getTest().setName("JEE Practice test");
					map.getTest().setNoOfQuestions(90);
					map.getTest().setDuration(10800);
					map.getTest().setTestUi("JEEM");
					testsDao.addTest(map);
					EdoServiceRequest automateTestRequest = new EdoServiceRequest();
					EdoTest automateTest = new EdoTest();
					automateTest.setId(map.getTest().getId());
					automateTest.setName("JEE19");
					automateTestRequest.setTest(automateTest);
					//Prepare a CET sample test
					automateTest(automateTestRequest);
					LoggingUtil.logMessage("JEE Mains Test created ..");
				}
			}
			
			txManager.commit(txStatus);
			LoggingUtil.logMessage("Admin account created successfully .....");
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			status.setStatus(-111,ERROR_IN_PROCESSING);
			try {
				txManager.rollback(txStatus);
			} catch (Exception e2) {
				LoggingUtil.logError(ExceptionUtils.getStackTrace(e2));
			}
		}
		return status;
	}
}
