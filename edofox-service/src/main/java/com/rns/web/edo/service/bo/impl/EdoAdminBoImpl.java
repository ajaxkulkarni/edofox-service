package com.rns.web.edo.service.bo.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.annotation.Transactional;

import com.rns.web.edo.service.bo.api.EdoAdminBo;
import com.rns.web.edo.service.dao.EdoTestsDao;
import com.rns.web.edo.service.domain.EDOInstitute;
import com.rns.web.edo.service.domain.EDOQuestionAnalysis;
import com.rns.web.edo.service.domain.EdoApiStatus;
import com.rns.web.edo.service.domain.EdoQuestion;
import com.rns.web.edo.service.domain.EdoServiceRequest;
import com.rns.web.edo.service.domain.EdoServiceResponse;
import com.rns.web.edo.service.domain.EdoStudent;
import com.rns.web.edo.service.domain.EdoStudentSubjectAnalysis;
import com.rns.web.edo.service.domain.EdoTest;
import com.rns.web.edo.service.domain.EdoTestQuestionMap;
import com.rns.web.edo.service.domain.EdoTestStudentMap;
import com.rns.web.edo.service.util.CommonUtils;
import com.rns.web.edo.service.util.EdoConstants;
import com.rns.web.edo.service.util.EdoSMSUtil;
import com.rns.web.edo.service.util.LoggingUtil;
import com.rns.web.edo.service.util.QuestionParser;

public class EdoAdminBoImpl implements EdoAdminBo, EdoConstants {
	
	private ThreadPoolTaskExecutor executor;
	private EdoTestsDao testsDao;

	public void setExecutor(ThreadPoolTaskExecutor executor) {
		this.executor = executor;
	}

	public EdoTestsDao getTestsDao() {
		return testsDao;
	}

	public void setTestsDao(EdoTestsDao testsDao) {
		this.testsDao = testsDao;
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
						List<EdoStudentSubjectAnalysis> subjectAnalysis = new ArrayList<EdoStudentSubjectAnalysis>();
						for(EdoTestStudentMap map: subjectScores) {
							if(map.getStudent() != null && map.getStudent().getId().intValue() == student.getId().intValue() && map.getSubjectScore() != null ) {
								subjectAnalysis.add(map.getSubjectScore());
								if( !existing.getSubjects().contains(map.getSubjectScore().getSubject())) {
									existing.getSubjects().add(map.getSubjectScore().getSubject());
								}
							}
						}
						student.getAnalysis().setSubjectScores(subjectAnalysis);
						if(StringUtils.equalsIgnoreCase("SMS", request.getRequestType())) {
							//Send rank SMS to student and parents
							EdoSMSUtil smsUtil = new EdoSMSUtil(MAIL_TYPE_TEST_RESULT_RANK);
							smsUtil.setStudent(student);
							smsUtil.setTest(existing);
							smsUtil.setInstitute(institute);
							smsUtil.setAdditionalMessage(request.getSmsMessage());
							executor.execute(smsUtil);
						}
					}
				}
			}
			
			if(!StringUtils.equalsIgnoreCase("SMS", request.getRequestType())) {
				response.setTest(existing);
				response.setStudents(students);
			}
		} catch (Exception e) {
			LoggingUtil.logMessage(ExceptionUtils.getStackTrace(e));
		}
		return response;
	}

	public EdoServiceResponse getAllStudents(EDOInstitute institute) {
		EdoServiceResponse response = new EdoServiceResponse();
		if(institute == null || institute.getId() == null) {
			response.setStatus(new EdoApiStatus(STATUS_ERROR, ERROR_INCOMPLETE_REQUEST));
			return response;
		}
		try {
			
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
		}
		return null;
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
			for(EdoQuestion question : questions) {
				if(StringUtils.equalsIgnoreCase("bonus", question.getCorrectAnswer())) {
					bonus = bonus + question.getWeightage();
				}
			}
			
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
			if(bonus != null) {
				BigDecimal finalScore = request.getTest().getScore().add(new BigDecimal(bonus));
				finalScore.setScale(2, RoundingMode.HALF_UP);
				request.getTest().setScore(finalScore);
				request.getTest().setSolvedCount(request.getTest().getSolvedCount() + bonusCount);
				LoggingUtil.logMessage("Added bonus .." + bonus + " so total is - " + request.getTest().getScore());
			}
			testsDao.updateTestStatus(request);
			if(CollectionUtils.isNotEmpty(request.getTest().getTest())) {
				for(EdoQuestion question: request.getTest().getTest()) {
					Map<String, Object> requestMap = new HashMap<String, Object>();
					requestMap.put("marks", question.getMarks());
					requestMap.put("test", request.getTest().getId());
					requestMap.put("student", request.getStudent().getId());
					requestMap.put("question", question.getQn_id());
					testsDao.updateTestResult(requestMap);
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
				List<EdoStudent> existingStudent = testsDao.getStudentByPhoneNumber(student);
				
				if(CollectionUtils.isEmpty(existingStudent)) {
					testsDao.saveStudent(student);
				} else {
					student.setId(existingStudent.get(0).getId());
				}
				
				LoggingUtil.logMessage("Student ID is =>" + student.getId());
				if(student.getId() != null) {
					testsDao.deleteExistingPackages(student);
					testsDao.createStudentPackage(student);
				}
				LoggingUtil.logMessage("Added student == " + student.getRollNo() + " successfully!");
			}
			
		} catch (Exception e) {
			LoggingUtil.logMessage(ExceptionUtils.getStackTrace(e));
			status.setResponseText(ERROR_IN_PROCESSING);
			status.setStatusCode(STATUS_OK);
		}
		return status;
	}


}
