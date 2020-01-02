package com.rns.web.edo.service.bo.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.annotation.Transactional;

import com.rns.web.edo.service.bo.api.EdoAdminBo;
import com.rns.web.edo.service.dao.EdoTestsDao;
import com.rns.web.edo.service.domain.EDOInstitute;
import com.rns.web.edo.service.domain.EDOQuestionAnalysis;
import com.rns.web.edo.service.domain.EdoApiStatus;
import com.rns.web.edo.service.domain.EdoFeedback;
import com.rns.web.edo.service.domain.EdoQuestion;
import com.rns.web.edo.service.domain.EdoServiceRequest;
import com.rns.web.edo.service.domain.EdoServiceResponse;
import com.rns.web.edo.service.domain.EdoStudent;
import com.rns.web.edo.service.domain.EdoStudentSubjectAnalysis;
import com.rns.web.edo.service.domain.EdoTest;
import com.rns.web.edo.service.domain.EdoTestQuestionMap;
import com.rns.web.edo.service.domain.EdoTestStudentMap;
import com.rns.web.edo.service.domain.jpa.EdoQuestionEntity;
import com.rns.web.edo.service.util.CommonUtils;
import com.rns.web.edo.service.util.EdoConstants;
import com.rns.web.edo.service.util.EdoFirebaseUtil;
import com.rns.web.edo.service.util.EdoSMSUtil;
import com.rns.web.edo.service.util.LoggingUtil;
import com.rns.web.edo.service.util.QuestionParser;

public class EdoAdminBoImpl implements EdoAdminBo, EdoConstants {
	
	private ThreadPoolTaskExecutor executor;
	private EdoTestsDao testsDao;
	private SessionFactory sessionFactory;

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

}
