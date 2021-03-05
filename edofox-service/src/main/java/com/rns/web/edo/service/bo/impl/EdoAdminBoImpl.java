package com.rns.web.edo.service.bo.impl;

import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
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

import com.rns.web.edo.service.VideoExportScheduler;
import com.rns.web.edo.service.bo.api.EdoAdminBo;
import com.rns.web.edo.service.dao.EdoTestsDao;
import com.rns.web.edo.service.domain.EDOInstitute;
import com.rns.web.edo.service.domain.EDOPackage;
import com.rns.web.edo.service.domain.EDOQuestionAnalysis;
import com.rns.web.edo.service.domain.EDOStudentAnalysis;
import com.rns.web.edo.service.domain.EdoAdminRequest;
import com.rns.web.edo.service.domain.EdoApiStatus;
import com.rns.web.edo.service.domain.EdoPaymentStatus;
import com.rns.web.edo.service.domain.EdoQuestion;
import com.rns.web.edo.service.domain.EdoServiceRequest;
import com.rns.web.edo.service.domain.EdoServiceResponse;
import com.rns.web.edo.service.domain.EdoStudent;
import com.rns.web.edo.service.domain.EdoStudentSubjectAnalysis;
import com.rns.web.edo.service.domain.EdoSubject;
import com.rns.web.edo.service.domain.EdoTest;
import com.rns.web.edo.service.domain.EdoTestQuestionMap;
import com.rns.web.edo.service.domain.EdoTestStudentMap;
import com.rns.web.edo.service.domain.EdoVideoLectureMap;
import com.rns.web.edo.service.domain.ext.EdoImpartusResponse;
import com.rns.web.edo.service.domain.jpa.EdoAnswerEntity;
import com.rns.web.edo.service.domain.jpa.EdoAnswerFileEntity;
import com.rns.web.edo.service.domain.jpa.EdoLiveSession;
import com.rns.web.edo.service.domain.jpa.EdoLiveToken;
import com.rns.web.edo.service.domain.jpa.EdoQuestionEntity;
import com.rns.web.edo.service.domain.jpa.EdoSalesDetails;
import com.rns.web.edo.service.domain.jpa.EdoTestStatusEntity;
import com.rns.web.edo.service.domain.jpa.EdoUplinkStatus;
import com.rns.web.edo.service.domain.jpa.EdoVideoLecture;
import com.rns.web.edo.service.util.CommonUtils;
import com.rns.web.edo.service.util.EdoAwsUtil;
import com.rns.web.edo.service.util.EdoConstants;
import com.rns.web.edo.service.util.EdoFirebaseUtil;
import com.rns.web.edo.service.util.EdoLiveUtil;
import com.rns.web.edo.service.util.EdoMailUtil;
import com.rns.web.edo.service.util.EdoNotificationsManager;
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
import com.sun.jersey.multipart.BodyPartEntity;
import com.sun.jersey.multipart.FormDataBodyPart;

public class EdoAdminBoImpl implements EdoAdminBo, EdoConstants {
	
	private ThreadPoolTaskExecutor executor;
	private ThreadPoolTaskExecutor mailExecutor;
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
								qAnalysis.setCorrectCount(currentAnalysis.getOption2Count());
								qAnalysis.setCorrectPercent(qAnalysis.getOption2percent());
							}
						} else if(StringUtils.equalsIgnoreCase(ATTR_OPTION3, currentAnalysis.getOptionSelected())) {
							qAnalysis.setOption3Count(currentAnalysis.getOptionCount());
							qAnalysis.setOption3percent(CommonUtils.getPercent(currentAnalysis.getOptionCount(), analysis.getAnalysis().getStudentsAppeared()));
							if(StringUtils.equalsIgnoreCase(ATTR_OPTION3, question.getCorrectAnswer())) {
								qAnalysis.setCorrectCount(currentAnalysis.getOption3Count());
								qAnalysis.setCorrectPercent(qAnalysis.getOption3percent());
							}
						} else if(StringUtils.equalsIgnoreCase(ATTR_OPTION4, currentAnalysis.getOptionSelected())) {
							qAnalysis.setOption4Count(currentAnalysis.getOptionCount());
							qAnalysis.setOption4percent(CommonUtils.getPercent(currentAnalysis.getOptionCount(), analysis.getAnalysis().getStudentsAppeared()));
							if(StringUtils.equalsIgnoreCase(ATTR_OPTION4, question.getCorrectAnswer())) {
								qAnalysis.setCorrectCount(currentAnalysis.getOption4Count());
								qAnalysis.setCorrectPercent(qAnalysis.getOption4percent());
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
				response.setInstitute(institute);
			} else if (StringUtils.equals(request.getRequestType(), "SMS")) {
				if(existing.getCurrentQuestion() != null && existing.getCurrentQuestion().getInstituteId() != null) {
					institute = testsDao.getInstituteById(existing.getCurrentQuestion().getInstituteId());
				}
			}
			
			//Get subjects to avoid blank result for subject
			List<EdoSubject> subjects = testsDao.getTestSubjects(test.getId());
			if(CollectionUtils.isNotEmpty(subjects)) {
				List<String> subjectsList = new ArrayList<String>();
				for(EdoSubject sub: subjects) {
					subjectsList.add(sub.getSubjectName());
				}
				existing.setSubjects(subjectsList);
			}
			
			List<EdoStudent> students = null;
			if(StringUtils.equals(request.getRequestType(), "SHOW_ABSENT")) {
				students = testsDao.getStudentResultsWithAbsent(test.getId());
			} else {
				students = testsDao.getStudentResults(test.getId());
			}
			
			if(CollectionUtils.isNotEmpty(students)) {
				List<EdoTestStudentMap> subjectScores = testsDao.getSubjectwiseScore(test.getId());
				if(CollectionUtils.isNotEmpty(subjectScores)) {
					Integer rank = 0;
					for(EdoStudent student: students) {
						if(student.getAnalysis() == null) {
							List<EdoStudentSubjectAnalysis> subjectAnalysis = new ArrayList<EdoStudentSubjectAnalysis>();
							CommonUtils.setupSubjectAnalysis(existing, subjectAnalysis);
							EDOStudentAnalysis studentAnalysis = new EDOStudentAnalysis();
							studentAnalysis.setSubjectScores(subjectAnalysis);
							studentAnalysis.setStatus("Absent");
							student.setAnalysis(studentAnalysis);
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
							if(request.getMailer() != null) {
								smsUtil.setAdditionalMessage(request.getMailer().getAdditionalMessage());
							}
							smsUtil.setMailer(request.getMailer());
							smsUtil.setCopyParent(true);
							executor.execute(smsUtil);
							EdoMailUtil mailUtil = new EdoMailUtil(MAIL_TYPE_TEST_RESULT_RANK);
							mailUtil.setStudent(student);
							mailUtil.setInstitute(institute);
							mailUtil.setMailer(request.getMailer());
							mailUtil.setExam(existing);
							executor.execute(mailUtil);
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
						//LoggingUtil.logMessage("... Saved question .. " + question.getId() + ":" + question.getQn_id());
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
					//LoggingUtil.logMessage("Uploaded solution for .." + question.getQuestionNumber() + " : " + question.getQn_id());
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
			
			EdoTest test = testsDao.getTest(request.getTest().getId());
			if(test != null) {
				if(request.getStudent() == null) {
					List<EdoStudent> students = testsDao.getStudentResults(request.getTest().getId());
					if(CollectionUtils.isNotEmpty(students)) {
						for(EdoStudent student: students) {
							EdoServiceRequest req = new EdoServiceRequest();
							req.setTest(test);
							req.setStudent(student);
							evalulateStudent(req, questions, bonus, bonusCount, request.getRequestType(), institute, request.getSmsMessage());
						}
					}
				} else {
					request.setTest(test);
					evalulateStudent(request, questions, bonus, bonusCount, request.getRequestType(), institute, request.getSmsMessage());
				}
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

	private String addStudent(EdoServiceRequest request, EdoStudent student) {
		List<EdoStudent> existingStudent = null;
		
		if(StringUtils.isBlank(student.getRollNo())) {
			return ERROR_INCOMPLETE_REQUEST;
		}
		
		/*if(StringUtils.equals(request.getRequestType(), "ROLL")) {
			existingStudent = testsDao.getStudentByRollNo(student);
		} else {
			existingStudent = testsDao.getStudentByPhoneNumber(student);
		}*/
		
		boolean update = false;
		if(StringUtils.equals(request.getRequestType(), "ADD_PKG") || StringUtils.equals(request.getRequestType(), "OVERWRITE_PKG") || StringUtils.equals(request.getRequestType(), "REMOVE")) {
			update = true;
		}
		
		if(!update) {
			//Check for max students limit
			if(CollectionUtils.isNotEmpty(student.getPackages())) {
				EDOInstitute institute = testsDao.getStudentStats(student.getPackages().get(0).getInstitute().getId());
				if(institute != null && institute.getMaxStudents() != null && institute.getCurrentCount() != null) {
					if((institute.getCurrentCount() + 1) >= institute.getMaxStudents()) {
						LoggingUtil.logMessage("Max students limit reached ... " + student.getPhone() + " for institute " + student.getPackages().get(0).getInstitute().getId() + " count " +institute.getCurrentCount() + " and max " + institute.getMaxStudents());
						return "Maximum students limit reached. Please upgrade your plan to add more students.";
					}
				}
			}
		}
		
		existingStudent = testsDao.getStudentLogin(student);
		
		if(!update) {
			//If add new student request
			if(CollectionUtils.isEmpty(existingStudent)) {
				testsDao.saveStudent(student);
			} else {
				LoggingUtil.logMessage("Student already exists with phone number (Not updating) ... " + student.getPhone() + " and roll no " + student.getRollNo());
				//student.setId(existingStudent.get(0).getId());
				//testsDao.updateStudent(student);
				return "Student already exists with the same username";
			}
			
		} else {
			if(CollectionUtils.isEmpty(existingStudent)) {
				return ERROR_STUDENT_NOT_FOUND;
			}
			student.setId(existingStudent.get(0).getId());
			if(StringUtils.isNotBlank(student.getToken())) {
				//If REMOVE request..remove student login from table
				if(StringUtils.equals("REMOVE", request.getRequestType())) {
					//Remove student login
					testsDao.deleteLogin(student);
				} else {
					//Update student details also
					testsDao.updateStudent(student);
				}
			}
		}
		
		
		LoggingUtil.logMessage("Student ID is =>" + student.getId());
		if(student.getId() != null) {
			if(CollectionUtils.isNotEmpty(student.getPackages())) {
				if(!update) {
					student.setInstituteId(student.getPackages().get(0).getInstitute().getId().toString());
					student.setToken(CommonUtils.createUniversalToken(student));
					LoggingUtil.logMessage("Adding student login for =>" + student.getId());
					testsDao.saveLogin(student);
				}
				for(EDOPackage pkg: student.getPackages()) {
					if(StringUtils.isBlank(pkg.getStatus())) {
						pkg.setStatus("Completed");
					}
				}
				//if(update) {
				//LoggingUtil.logMessage("Removing student package IF PRESENT for =>" + student.getId());
				//Delete package only for overwrite request
				testsDao.deleteExistingPackages(student);
				if(StringUtils.equals(request.getRequestType(), "OVERWRITE_PKG")) {
					testsDao.deleteStudentPackages(student);
				}
				//}
				if(!StringUtils.equals("REMOVE", request.getRequestType())) {
					LoggingUtil.logMessage("Adding student package for =>" + student.getId());
					testsDao.createStudentPackage(student);
				}
			}
		}
		EDOInstitute currentInstitute = testsDao.getInstituteById(request.getInstitute().getId());
		EdoFirebaseUtil.updateStudent(student, currentInstitute.getFirebaseId());
		LoggingUtil.logMessage("Added/Updated student == " + student.getRollNo() + " successfully!");
		return RESPONSE_OK;
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
					if(request.getFirstQuestion() == null) {
						Integer lastQuestion = testsDao.getLastQuestionNumber(request.getTest().getId());
						if(lastQuestion != null && lastQuestion > 0) {
							startId = lastQuestion;
						}
					} else {
						startId = request.getFirstQuestion();
					}
					
					//Old logic
					/*if(StringUtils.equals("CET", question.getExamType())) {
						question.setType("SINGLE");
					 	question.setCorrect(true);
					} else if (StringUtils.isNotBlank(question.getAnalysis().getQuestionType())) {
						//Not select questions of provided type in normal case
						question.setType(question.getAnalysis().getQuestionType());
						question.setCorrect(false);
					}*/
					if(StringUtils.isNotBlank(question.getAnalysis().getQuestionType())) {
						question.setType(question.getAnalysis().getQuestionType());
					 	question.setCorrect(true);
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
							//addTypeBasedQuestions(question, examQuestions);
							
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
							//addTypeBasedQuestions(question, examQuestions);
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
							//addTypeBasedQuestions(question, examQuestions);
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
				q.setSection(question.getSection());
				startId++;
			}
			request.getTest().setTest(questions);
			testsDao.createExam(request);
		}
	}

	public EdoServiceResponse addFeedbackResolution(EdoServiceRequest request) {
		EdoServiceResponse response = CommonUtils.initResponse();
		if(request.getFeedback() == null) {
			response.setStatus(new EdoApiStatus(-111, ERROR_INCOMPLETE_REQUEST));
			return response;
		}
		if(request.getFeedback().getId() == null && request.getFeedback().getVideoId() == null && request.getFeedback().getQuestionId() == null) {
			response.setStatus(new EdoApiStatus(-111, ERROR_INCOMPLETE_REQUEST));
			return response;
		}
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
			setDates(request);
			//request.setFromDate(CommonUtils.getStartDate(request.getFromDate()));
			//request.setToDate(CommonUtils.getEndDate(request.getToDate()));
			if(StringUtils.equals(request.getSearchFilter(), "Videos")) {
				List<EdoQuestion> videoFeedback = testsDao.getVideoFeedback(request);
				if(CollectionUtils.isNotEmpty(videoFeedback)) {
					for(EdoQuestion edoFeedback: videoFeedback) {
						CommonUtils.setupFeedbackAttachment(edoFeedback);
					}
				}
				test.setTest(videoFeedback);
			} else if (StringUtils.equals(request.getSearchFilter(), "General")) { 
				List<EdoQuestion> videoFeedback = testsDao.getGeneralFeedback(request);
				if(CollectionUtils.isNotEmpty(videoFeedback)) {
					for(EdoQuestion edoFeedback: videoFeedback) {
						CommonUtils.setupFeedbackAttachment(edoFeedback);
					}
				}
				test.setTest(videoFeedback);
			} else {
				List<EdoQuestion> feedbackData = testsDao.getFeedbackData(request);
				if(CollectionUtils.isNotEmpty(feedbackData)) {
					for(EdoQuestion edoFeedback: feedbackData) {
						CommonUtils.setQuestionURLs(edoFeedback);
						QuestionParser.fixQuestion(edoFeedback);
						CommonUtils.setupFeedbackAttachment(edoFeedback);
					}
				}
				test.setTest(feedbackData);
			}
			response.setTest(test); 
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			response.setStatus(new EdoApiStatus(-111, ERROR_IN_PROCESSING));
		}
		return response;
	}

	private void setDates(EdoServiceRequest request) {
		String startTime = request.getStartTime();
		if(StringUtils.isNotBlank(startTime)) {
			//Set start and end times
			if(StringUtils.contains(startTime, "last")) {
				String days = StringUtils.removeStart(startTime, "last");
				if(StringUtils.isNotBlank(days)) {
					request.setEndTime(CommonUtils.convertDate(CommonUtils.getEndDate(new Date())));
					Calendar cal = Calendar.getInstance();
					cal.add(Calendar.DATE, - new Integer(days));
					request.setStartTime(CommonUtils.convertDate(CommonUtils.getStartDate(cal.getTime())));
				}
			} else if (StringUtils.contains(startTime, "old")) {
				String days = StringUtils.removeStart(startTime, "old");
				if(StringUtils.isNotBlank(days)) {
					Calendar cal = Calendar.getInstance();
					cal.add(Calendar.DATE, - new Integer(days));
					request.setEndTime(CommonUtils.convertDate(CommonUtils.getEndDate(cal.getTime())));
				}
			}
		}
	}
	
	public EdoServiceResponse getFeedbackSummary(EdoServiceRequest request) {
		EdoServiceResponse response = CommonUtils.initResponse();
		try {
			setDates(request);
			//request.setFromDate(CommonUtils.getStartDate(request.getFromDate()));
			//request.setToDate(CommonUtils.getEndDate(request.getToDate()));
			EdoQuestion feedbackData = testsDao.getFeedbackSummary(request);
			request.setRequestType(null);
			EdoQuestion total = testsDao.getFeedbackSummary(request);
			response.setSubjects(testsDao.getDoubtSubjects(request));
			feedbackData.setResult(total.getFeedback().getFrequency());
			response.setQuestion(feedbackData);
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			response.setStatus(new EdoApiStatus(-111, ERROR_IN_PROCESSING));
		}
		return response;
	}


	public EdoServiceResponse getQuestionFeedbacks(EdoServiceRequest request) {
		EdoServiceResponse response = CommonUtils.initResponse();
		try {
			List<EdoTestStudentMap> questionFeedbacks = testsDao.getQuestionFeedbacks(request.getFeedback());
			if(CollectionUtils.isNotEmpty(questionFeedbacks)) {
				//EdoTestStudentMap map = questionFeedbacks.get(0);
				for(EdoTestStudentMap map: questionFeedbacks) {
					if(StringUtils.isNotBlank(map.getTest().getCurrentQuestion().getQuestion())) {
						QuestionParser.fixQuestion(map.getTest().getCurrentQuestion());
					}
					CommonUtils.setupFeedbackAttachment(map.getTest().getCurrentQuestion());
				}
			}
			response.setMaps(questionFeedbacks);
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			response.setStatus(new EdoApiStatus(-111, ERROR_IN_PROCESSING));
		}
		return response;
	}

	public EdoApiStatus registerStudent(EdoServiceRequest request) {
		EdoApiStatus status = new EdoApiStatus();
		//TransactionStatus txStatus = txManager.getTransaction(new DefaultTransactionDefinition());
		try {
			if(request.getInstitute() == null || request.getInstitute().getId() == null) {
				status.setStatus(-111, "Please provide valid institute information ..");
				return status;
			}
			
			EdoStudent student = request.getStudent();
			if(student == null) {
				status.setStatus(-111, "Please provide valid student information ..");
				return status;
			}
			if(StringUtils.isBlank(student.getToken())) {
				
				if(StringUtils.isBlank(student.getName())) {
					status.setStatus(-111, "Please provide valid student name ..");
					return status;
				}
				
				if(StringUtils.isBlank(student.getPhone()) ) {
					status.setStatus(-111, "Please provide valid student phone number ..");
					return status;
				}
				
				if(StringUtils.isBlank(student.getRollNo())) {
					status.setStatus(-111, "Please provide valid student roll number/username ..");
					return status;
				}
				
				if(CollectionUtils.isEmpty(student.getPackages())) {
					status.setStatus(-111, "Please provide atleast one valid package ..");
					return status;
				}
			}
			
			if(StringUtils.isBlank(student.getPassword())) {
				student.setPassword("12345");
			}
			
			/*List<EdoStudent> existingStudent = testsDao.getStudentByPhoneNumber(student);
			if(CollectionUtils.isNotEmpty(existingStudent)) {
				status.setStatus(-222, "This mobile number is already registered ..");
				return status;
			}*/
			
			String result = addStudent(request, student);
			if(!StringUtils.equals(RESPONSE_OK, result)) {
				status.setStatus(-111, result);
				return status;
			}
			if(StringUtils.isNotBlank(student.getToken())) {
				status.setResponseText(student.getToken());
			}
			//txManager.commit(txStatus);
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			status = new EdoApiStatus(-111, ERROR_IN_PROCESSING);
			//txManager.rollback(txStatus);
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
			request.getTest().setTest(EdoPDFUtil.pdfBox(request, fileData, folderPath));
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
						String type = ATTR_TEMP_QUESTION;
						Integer questionNumber = new Integer(StringUtils.removeEnd(values[1], ".png"));
						if(values[0].equalsIgnoreCase("S")) {
							type = ATTR_TEMP_SOLUTION;
							//Add solution image to existing question
							if(CollectionUtils.isNotEmpty(questions)) {
								for(EdoQuestion question: questions) {
									if(questionNumber != null && question.getQuestionNumber() != null && question.getQuestionNumber().intValue() == questionNumber.intValue()) {
										question.setSolutionImageUrl(EdoPDFUtil.getQuestionUrl(request.getTest().getId(), questionNumber, type));
										break;
									}
								}
							}
						} else {
							EdoQuestion question = new EdoQuestion();
							question.setQuestionNumber(questionNumber);
							question.setQuestionImageUrl(EdoPDFUtil.getQuestionUrl(request.getTest().getId(), questionNumber, type));
							questions.add(question);
						}
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
			EdoTest test = testsDao.getTest(request.getTest().getId());
			String sourceDir = TEMP_QUESTION_PATH + request.getTest().getId();
			File source = new File(sourceDir);
			String destinationDir = TEST_QUESTION_PATH + request.getTest().getId();
			File destination = new File(destinationDir);
			if(!destination.exists()) {
				destination.mkdirs();
				//FileUtils.deleteDirectory(destination);
			}
			if(ArrayUtils.isNotEmpty(source.listFiles())) {
			
				LoggingUtil.logMessage("Moving all files from directory " + sourceDir + " to " + destinationDir);
				//FileUtils.copyDirectoryToDirectory(source, destination);
				int count = 0;
				for(File file: source.listFiles()) {
					FileUtils.copyFile(file, new File(destinationDir + "/" + file.getName()));
					count++;
				}
				LoggingUtil.logMessage("Moved " + count + "  files from directory " + sourceDir + " to " + destinationDir);
				
				for(EdoQuestion question: request.getTest().getTest()) {
					question.setQuestionImageUrl(destinationDir + "/" + EdoPDFUtil.QUESTION_PREFIX + question.getQuestionNumber() + ".png");
					if(StringUtils.isNotBlank(question.getSolutionImageUrl())) {
						question.setSolutionImageUrl(destinationDir + "/" + EdoPDFUtil.SOLUTION_PREFIX + question.getQuestionNumber() + ".png");
					}
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
					} else if (StringUtils.equals(question.getOption1(), "A)")) {
						question.setOption1("A)");
						question.setOption2("B)");
						question.setOption3("C)");
						question.setOption4("D)");
					} else if (StringUtils.equals(question.getOption1(), "NA")) {
						question.setOption1("");
						question.setOption2("");
						question.setOption3("");
						question.setOption4("");
					}
					if(question.getQn_id() == null) {
						question.setQn_id(question.getQuestionNumber());
					}
					question.setInstituteId(test.getCurrentQuestion().getInstituteId());
					question.setTeacher(request.getStudent());
					testsDao.addQuestion(question);
				}
				testsDao.createExam(request);
				txManager.commit(txStatus);
				FileUtils.deleteDirectory(source);
			}
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

	public EdoServiceResponse createInstitute(EdoAdminRequest request) {
		EdoServiceResponse response = new EdoServiceResponse();
		if(request.getInstitute() == null || StringUtils.isBlank(request.getInstitute().getName())) {
			EdoApiStatus status = new EdoApiStatus(-111, ERROR_INCOMPLETE_REQUEST);
			response.setStatus(status);
			return response;
		}
		//Is admin login already present
		Integer result = testsDao.isAdminLogin(request.getInstitute());
		if(result != null && result > 0) {
			LoggingUtil.logMessage("Admin login already present for " + request.getInstitute().getUsername());
			EdoApiStatus status = new EdoApiStatus(-111, "Edofox login already present with this username. Please choose another username.");
			response.setStatus(status);
			return response;
		}
		//Don't add if student is already present with same number
		EdoStudent requestStudent = new EdoStudent();
		requestStudent.setPhone(request.getInstitute().getContact());
		List<EdoStudent> students = testsDao.getStudentByPhoneNumber(requestStudent);
		if(CollectionUtils.isNotEmpty(students)) {
			LoggingUtil.logMessage("Mobile number already registered  " + request.getInstitute().getContact());
			EdoApiStatus status = new EdoApiStatus(-111, "Mobile number " + request.getInstitute().getContact() + " is already registered. Please go to login page.");
			response.setStatus(status);
			return response;
		}
		EdoApiStatus status = new EdoApiStatus();
		TransactionStatus txStatus = txManager.getTransaction(new DefaultTransactionDefinition());
		try {
			int days = 7;
			if(StringUtils.isBlank(request.getInstitute().getPurchase())) {
				request.getInstitute().setPurchase("Free");
			}
			if(StringUtils.equalsIgnoreCase("Free", request.getInstitute().getPurchase())) {
				days = 300;
			}
			Date expiryDate = DateUtils.addDays(new Date(), days);
			EDOInstitute institute = request.getInstitute();
			institute.setExpiryDate(expiryDate);
			String expiryDateString = CommonUtils.convertDate(expiryDate);
			institute.setExpiryDateString(expiryDateString);
			//Set limits based on plans
			institute.setMaxStudents(MAX_STUDENTS.get(institute.getPurchase()));
			institute.setStorageQuota(MAX_STORAGE.get(institute.getPurchase()));
			//Create institute and login
			testsDao.saveInstitute(institute);
			testsDao.createAdminLogin(institute);
			LoggingUtil.logMessage("Institute " + institute.getId() + " and login " + institute.getAdminId() + " created ..");
			//Add default package
			EDOPackage pkg = new EDOPackage();
			pkg.setInstitute(institute);
			pkg.setName("Default classroom");
			testsDao.createPackage(pkg);
			LoggingUtil.logMessage("Institute package " + pkg.getId() + " created ..");
			//Add first student
			EdoStudent student = request.getStudent();
			if(student == null) {
				student = new EdoStudent();
				student.setPhone(institute.getContact());
				student.setRollNo(institute.getContact());
				student.setName("Demo student");	
				student.setPassword(institute.getPassword());
				//student.setAccessType("Teacher");
				request.setStudent(student);
			}
			if(request.getStudent() != null && pkg.getId() != null) {
				if(student.getPhone() == null) {
					student.setPhone(institute.getContact());
				}
				if(student.getRollNo() == null) {
					student.setRollNo(institute.getContact());
				}
				EdoServiceRequest addStudentRequest = new EdoServiceRequest();
				addStudentRequest.setInstitute(institute);
				List<EDOPackage> packages = new ArrayList<EDOPackage>();
				pkg.setStatus("Completed");
				EdoPaymentStatus payment = new EdoPaymentStatus();
				payment.setMode("Offline");
				student.setExamMode("Online");
				student.setPayment(payment);
				packages.add(pkg);
				student.setPackages(packages);
				String addStudentResult = addStudent(addStudentRequest, student);
				if(!StringUtils.equals(RESPONSE_OK, addStudentResult)) {
					txManager.rollback(txStatus);
					LoggingUtil.logMessage("Mobile number already registered  " + request.getInstitute().getContact());
					status = new EdoApiStatus(-111, addStudentResult);
					response.setStatus(status);
					return response;
				}
				
			}
			//Add a test
			if(pkg.getId() != null && student != null) {
				EdoTestStudentMap map = new EdoTestStudentMap();
				student.setCurrentPackage(pkg);
				map.setStudent(student);
				EdoTest test = new EdoTest();
				test.setStartDate(new Date());
				//test.setEndDate(expiryDate);
				map.setTest(test);
				if(request.isCet()) {
					map.getTest().setName("CET Practice test");
					//50 maths 50 phy 50 che
					map.getTest().setNoOfQuestions(150);
					map.getTest().setDuration(10800);
					map.getTest().setTestUi("JEE");
					map.getTest().setTotalMarks(200);
					map.setTestEndDateString(expiryDateString);
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
					map.getTest().setTotalMarks(200);
					testsDao.addTest(map);
					EdoServiceRequest automateTestRequest = new EdoServiceRequest();
					EdoTest automateTest = new EdoTest();
					automateTest.setId(map.getTest().getId());
					automateTest.setName("JEE19AF");
					automateTestRequest.setTest(automateTest);
					//Prepare a CET sample test
					automateTest(automateTestRequest);
					LoggingUtil.logMessage("JEE Mains Test created ..");
				}
			}
			
			txManager.commit(txStatus);
			LoggingUtil.logMessage("Admin account created successfully .....");
			//Notify with SMS
			String mailType = MAIL_TYPE_SIGN_UP;
			/*if(StringUtils.equals("Demo", institute.getPurchase())) {
				mailType = MAIL_TYPE_SIGN_UP_DEMO;
			}*/
			EdoSMSUtil edoSMSUtil = new EdoSMSUtil(mailType);
			edoSMSUtil.setCopyAdmin(true);
			edoSMSUtil.setInstitute(institute);
			edoSMSUtil.setStudent(student);
			edoSMSUtil.sendSMS();
			//executor.execute(edoSMSUtil);
			response.setStudent(student);
			response.setInstitute(institute);
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			status.setStatus(-111,ERROR_IN_PROCESSING);
			try {
				txManager.rollback(txStatus);
			} catch (Exception e2) {
				LoggingUtil.logError(ExceptionUtils.getStackTrace(e2));
			}
		}
		response.setStatus(status);
		return response;
	}

	public EdoApiStatus savePendingVideos(EdoServiceRequest request) {
		EdoApiStatus status = new EdoApiStatus();
		Session session = null;
		try {
			session = this.sessionFactory.openSession();
			new VideoExportScheduler().exportVideos(session);
			
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			status.setStatus(-111, ERROR_IN_PROCESSING);
		} finally {
			CommonUtils.closeSession(session);
		}
		return status;
	}

	public EdoApiStatus upgradeClient(EdoServiceRequest request) {
		EdoApiStatus status = new EdoApiStatus();
		if(request.getInstitute() == null || StringUtils.isBlank(request.getInstitute().getPurchase())) {
			status.setStatus(-111, ERROR_INCOMPLETE_REQUEST);
			return status;
		}
		try {
			EDOInstitute institute = testsDao.getInstituteById(request.getInstitute().getId());
			institute.setPurchase(request.getInstitute().getPurchase());
			int days = 30;
			if(StringUtils.equals(request.getRequestType(), "TRIAL")) {
				days = 7;
			}
			if(StringUtils.equals(request.getInstitute().getPurchase(), "Free")) {
				days = 300;
			}
			Date expiryDate = DateUtils.addDays(new Date(), days);
			institute.setExpiryDate(expiryDate);
			String expiryDateString = CommonUtils.convertDate(expiryDate);
			institute.setExpiryDateString(expiryDateString);
			//Set limits based on plans
			institute.setMaxStudents(MAX_STUDENTS.get(institute.getPurchase()));
			institute.setStorageQuota(MAX_STORAGE.get(institute.getPurchase()));
			testsDao.upgradeInstitute(institute);
			
			LoggingUtil.logMessage("Institute updated successfully " + institute.getId() + " by a plan " + institute.getPurchase());
			//Notify with SMS
			String mailType = MAIL_TYPE_UPGRADE;
			/*if(StringUtils.equals("Demo", institute.getPurchase())) {
				mailType = MAIL_TYPE_SIGN_UP_DEMO;
			}*/
			EdoSMSUtil edoSMSUtil = new EdoSMSUtil(mailType);
			edoSMSUtil.setCopyAdmin(true);
			EdoStudent student = new EdoStudent();
			student.setPhone(institute.getContact());
			edoSMSUtil.setStudent(student);
			edoSMSUtil.setInstitute(institute);
			edoSMSUtil.sendSMS();
			//executor.execute(edoSMSUtil);
			
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			status.setStatus(-111, ERROR_IN_PROCESSING);
		}
		return status;
	}

	public EdoApiStatus updateClientSales(EdoServiceRequest request) {
		EdoApiStatus status = new EdoApiStatus();
		if(request.getInstitute() == null || request.getInstitute().getId() == null) {
			status.setStatus(-111, ERROR_INCOMPLETE_REQUEST);
			return status;
		}
		Session session = null;
		try {
			session = this.sessionFactory.openSession();
			Transaction tx = session.beginTransaction();
			List<EdoSalesDetails> records = session.createCriteria(EdoSalesDetails.class).add(Restrictions.eq("instituteId", request.getInstitute().getId())).list();
			if(CollectionUtils.isEmpty(records)) {
				EdoSalesDetails details = new EdoSalesDetails();
				details.setComments(request.getInstitute().getComments());
				details.setStatus(request.getInstitute().getStatus());
				details.setInstituteId(request.getInstitute().getId());
				details.setLastUpdated(new Date());
				session.persist(details);
			} else {
				EdoSalesDetails details = records.get(0);
				details.setComments(request.getInstitute().getComments());
				details.setStatus(request.getInstitute().getStatus());
				details.setLastUpdated(new Date());
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

	public EdoServiceResponse loadQuestionBank(EdoServiceRequest request) {
		EdoServiceResponse response = new EdoServiceResponse();
		try {
			List<EdoQuestion> questions = testsDao.getQuestionBank(request.getQuestion());
			if(CollectionUtils.isNotEmpty(questions)) {
				for(EdoQuestion question: questions) {
					QuestionParser.fixQuestion(question);
					CommonUtils.setQuestionURLs(question);
				}
			}
			EdoTest test = new EdoTest();
			test.setTest(questions);
			response.setTest(test);
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
		}
		return response;
	}

	public EdoServiceResponse fixRecordedFile(EdoServiceRequest request) {
		EdoServiceResponse response = new EdoServiceResponse();
		try {
			if(request.getLecture() != null) {
				long fixed = VideoExportScheduler.fixFile(request.getLecture().getVideoName());
				if(fixed > 0) {
					List<EdoVideoLectureMap> lectures = new ArrayList<EdoVideoLectureMap>();
					EdoVideoLectureMap lecture = new EdoVideoLectureMap();
					EdoVideoLecture lecture2 = new EdoVideoLecture();
					lecture2.setVideo_url(VideoExportScheduler.FIXED_URL + request.getLecture().getVideoName() + ".mp4");
					lecture2.setSize(new Double(fixed));
					lecture.setLecture(lecture2);
					lectures.add(lecture);
					response.setLectures(lectures);
				} else {
					response.setStatus(new EdoApiStatus(-111, ERROR_IN_PROCESSING));
				}
			}
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e), LoggingUtil.videoLogger);
			response.setStatus(new EdoApiStatus(-111, ERROR_IN_PROCESSING));
		}
		return response;
	}
	
	public EdoApiStatus fixRecordedLectures(EdoServiceRequest request) {
		EdoApiStatus status = new EdoApiStatus();
		Session session = null;
		try {
			session = this.sessionFactory.openSession();
			Criteria liveCriteria = session.createCriteria(EdoLiveSession.class);
			List<EdoLiveSession> failedSessions = liveCriteria.add(Restrictions.eq("status", "Failed")).list();
			if(CollectionUtils.isNotEmpty(failedSessions)) {
				LoggingUtil.logMessage("Fixing the sessions - " + failedSessions.size());
				for(EdoLiveSession live: failedSessions) {
					if(StringUtils.isBlank(live.getRecording_url())) {
						String videoName = live.getClassroomId() + "-" + live.getId();
						LoggingUtil.logMessage("Fixing the file " + videoName, LoggingUtil.videoLogger);
						EdoVideoLecture lecture = VideoExportScheduler.callFixFileApi(videoName);
						if(lecture != null && lecture.getSize() != null) {
							Transaction tx = session.beginTransaction();
							live.setRecording_url(lecture.getVideo_url());
							live.setFileSize(lecture.getSize().floatValue());
							tx.commit();
							LoggingUtil.logMessage("Fixed the file " + videoName + " with " + live.getRecording_url(), LoggingUtil.videoLogger);
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
					}
					
				}
			}
			LoggingUtil.logMessage("Finished fixing the sessions ..", LoggingUtil.videoLogger);
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e), LoggingUtil.videoLogger);
		} finally {
			CommonUtils.closeSession(session);
		}
		return status;
	}

	public EdoServiceResponse getLiveAnalysis(EdoServiceRequest request) {
		EdoServiceResponse response = new EdoServiceResponse();
		Session session = null;
		try {
			if (request.getLecture() != null && request.getLecture().getId() != null) {
				List<EdoLiveSession> sessions = session.createCriteria(EdoLiveSession.class).add(Restrictions.eq("id", request.getLecture().getId())).list();
				EdoLiveSession live = null;
				if (CollectionUtils.isEmpty(sessions)) {
					response.setStatus(new EdoApiStatus(-111, "No such live session found"));
					return response;
				}
				live = sessions.get(0);
				// Call Impartus API
				List<EdoLiveToken> tokens = session.createCriteria(EdoLiveToken.class).addOrder(org.hibernate.criterion.Order.desc("id"))
						.add(Restrictions.ge("lastUpdated", DateUtils.addHours(new Date(), -2))).setMaxResults(1).list();
				String tokenString = null;
				if (CollectionUtils.isEmpty(tokens)) {
					EdoImpartusResponse tokenResponse = EdoLiveUtil.adminLogin();
					if (tokenResponse == null) {
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
			}
		} catch (Exception e) {

		} finally {
			CommonUtils.closeSession(session);
		}
		return null;
	}
	
	public EdoApiStatus sendNotification(EdoServiceRequest request) {
		EdoApiStatus status = new EdoApiStatus();
		try {
			
			EdoNotificationsManager mgr = new EdoNotificationsManager(this.sessionFactory);
			mgr.setNotificationType(request.getRequestType());
			mgr.setClasswork(request.getLecture());
			mgr.setExam(request.getTest());
			mgr.setMailExecutor(mailExecutor);
			mgr.setFeedback(request.getFeedback());
			mgr.setInstitute(request.getInstitute());
			mgr.setMailer(request.getMailer());
			mgr.setTestsDao(testsDao);
			if(request.getStudent() != null) {
				mgr.setStudent(request.getStudent());
			}
			
			if(request.getLecture() != null) {
				//Schedule video notification at a delay since Vimeo will take some time to reflect
				ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
				String videoDelay = EdoPropertyUtil.getProperty(EdoPropertyUtil.FCM_VIDEO_DELAY);
				int delay = 15; //Default 15 mins delay
				if(StringUtils.isNotBlank(videoDelay)) {
					delay = new Integer(videoDelay);
				}
				LoggingUtil.logMessage("Executing task with delay " + delay, LoggingUtil.emailLogger);
				scheduler.schedule(mgr, delay, TimeUnit.MINUTES);
				scheduler.shutdown();
			} else {
				LoggingUtil.logMessage("Executing notification task " + request.getRequestType(), LoggingUtil.emailLogger);
				executor.execute(mgr);
			}
			
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			status.setStatus(-111, ERROR_IN_PROCESSING);
		} finally {
			//CommonUtils.closeSession(session);
		}
		return status;
	}

	public EdoServiceResponse savePackage(EdoServiceRequest request) {
		EdoServiceResponse response = new EdoServiceResponse();
		try {
			EDOPackage currentPackage = request.getStudent().getCurrentPackage();
			if(currentPackage.getId() != null) {
				if(currentPackage.getStatus().equals("D")) {
					currentPackage.setDisabled(1);
				}
				testsDao.updatePackage(currentPackage);
			} else {
				testsDao.createPackage(currentPackage);
				response.setStudent(request.getStudent());
			}
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			response.setStatus(new EdoApiStatus(-11, ERROR_IN_PROCESSING));
		}
		return response;
	}
	
	public EdoServiceResponse updateEdofoxTokens(EdoServiceRequest request) {
		EdoServiceResponse response = new EdoServiceResponse();
		try {
			List<EdoStudent> students = testsDao.getAllStudents(request.getInstitute().getId());
			if(CollectionUtils.isNotEmpty(students)) {
				for(EdoStudent student: students) {
					student.setToken(CommonUtils.createUniversalToken(student));
					testsDao.updateStudentToken(student);
				}
			}
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			response.setStatus(new EdoApiStatus(-11, ERROR_IN_PROCESSING));
		}
		return response;
	}

	public ThreadPoolTaskExecutor getMailExecutor() {
		return mailExecutor;
	}

	public void setMailExecutor(ThreadPoolTaskExecutor mailExecutor) {
		this.mailExecutor = mailExecutor;
	}

	public EdoApiStatus uploadEvaluation(FormDataBodyPart bodyParts, Integer answerId, BigDecimal marks) {
		Session session = null;
		EdoApiStatus status = new EdoApiStatus();
		try {
			BodyPartEntity bodyPartEntity = null;
			if(bodyParts != null) {
				bodyPartEntity = (BodyPartEntity) bodyParts.getEntity();
			}
			
			session = this.sessionFactory.openSession();
			EdoAnswerFileEntity answerFileEntity = (EdoAnswerFileEntity) session.createCriteria(EdoAnswerFileEntity.class)
					.add(Restrictions.eq("id", answerId)).uniqueResult();
			if(answerFileEntity != null) {
				Transaction tx = session.beginTransaction();
				if(bodyPartEntity != null) {
					String localFileName = bodyParts.getContentDisposition().getFileName();
					if(localFileName == null) {
						localFileName = "";
					}
					String fileName = "evaluated_" + answerFileEntity.getId() +  ".png";
					answerFileEntity.setCorrectionUrl(EdoAwsUtil.uploadToAws(fileName, null, bodyPartEntity.getInputStream(), bodyParts.getContentDisposition().getType(), "answerFilesEdofox"));
				}
				if(marks != null) {
					answerFileEntity.setCorrectionMarks(marks);
				}
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
	
	public EdoApiStatus updateStudentScore(EdoServiceRequest request) {
		EdoApiStatus status = new EdoApiStatus();
		if (request.getStudent() == null || request.getStudent().getId() == null || request.getTest() == null || request.getTest().getId() == null) {
			status.setStatus(STATUS_ERROR, ERROR_INCOMPLETE_REQUEST);
			return status;
		}
		Session session = null;
		EdoTest test = request.getTest();
		try {
			session = this.sessionFactory.openSession();
			
			List<EdoTestStatusEntity> maps = /*testsDao.getTestStatus(inputMap)*/ session.createCriteria(EdoTestStatusEntity.class)
					.add(Restrictions.eq("testId", test.getId()))
					.add(Restrictions.eq("studentId", request.getStudent().getId()))
					.list();
			EdoTestStatusEntity map = null;
			if(CollectionUtils.isNotEmpty(maps)) {
				map = maps.get(0);
			}

			if(map == null) {
				status.setStatus(-111, "No record found of this student for selected exam");
				return status;
			}



			Transaction tx = session.beginTransaction();
			
			map.setSolved(test.getSolvedCount());
			map.setCorrect(test.getCorrectCount());
			//map.setFlagged(test.getFlaggedCount());
			map.setScore(test.getScore());
			map.setStatus(TEST_STATUS_COMPLETED);
			map.setUpdatedDate(new Date());
			//Commit the transaction
			//txManager.commit(txStatus);
			tx.commit();
			
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			status.setStatus(-111, ERROR_IN_PROCESSING);
		} finally {
			CommonUtils.closeSession(session);
		}
		return status;
	}

	public EdoServiceResponse uploadQuestionImage(EdoServiceRequest request, InputStream fileData) {
		EdoServiceResponse response = new EdoServiceResponse();
		FileOutputStream fos = null;
		ImageOutputStream ios = null;
		try {
			
			File output = new File(EdoConstants.TEST_QUESTION_PATH + request.getTest().getId());
			if(!output.exists()) {
				output.mkdirs();
			}
			
			//ImageIOUtil.writeImage(bim, outputFolder + QUESTION_PREFIX + questionNumber + ".png", 300);
		
			//Compress image before saving
			ImageWriter writer =  ImageIO.getImageWritersByFormatName("jpg").next();
	        String outputPath = output.getAbsolutePath() + "/" + request.getRequestType() + request.getQuestion().getQuestionNumber() + ".png";
			fos = new FileOutputStream(outputPath);
			ios = ImageIO.createImageOutputStream(fos);
	        writer.setOutput(ios);

	        ImageWriteParam param = writer.getDefaultWriteParam();
	        if (param.canWriteCompressed()){
	            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
	            //param.setCompressionQuality(0.05f);
	        }

	        writer.write(null, new IIOImage(ImageIO.read(fileData), null, null), param);
	        
	        LoggingUtil.logMessage("Uploaded file to path " + outputPath);
			
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			response.setStatus(new EdoApiStatus(-111, ERROR_IN_PROCESSING));
			
			try {
				if(fos != null) {
					fos.close();
				}
				if(ios != null) {
					ios.close();
				}
			} catch (IOException e1) {
				LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
				response.setStatus(new EdoApiStatus(-111, ERROR_IN_PROCESSING));
			}
		}
			
		return response;
	}
}
