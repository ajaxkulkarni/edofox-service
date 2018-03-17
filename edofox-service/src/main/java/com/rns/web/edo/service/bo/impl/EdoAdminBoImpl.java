package com.rns.web.edo.service.bo.impl;

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
import com.rns.web.edo.service.domain.EdoServiceResponse;
import com.rns.web.edo.service.domain.EdoStudent;
import com.rns.web.edo.service.domain.EdoTest;
import com.rns.web.edo.service.util.CommonUtils;
import com.rns.web.edo.service.util.EdoConstants;
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
			EdoTest analysis = testsDao.getExamAnalysis(test.getId());
			if(analysis == null || analysis.getId() == null || analysis.getAnalysis() == null || analysis.getAnalysis().getStudentsAppeared() == null) {
				LoggingUtil.logMessage("No test result found for ID .." + test.getId());
				response.setStatus(new EdoApiStatus(STATUS_ERROR, ERROR_RESULT_NOT_FOUND));
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

	public EdoServiceResponse getTestResults(EdoTest test) {
		EdoServiceResponse response = new EdoServiceResponse();
		if(test == null || test.getId() == null) {
			response.setStatus(new EdoApiStatus(STATUS_ERROR, ERROR_INCOMPLETE_REQUEST));
			return response;
		}
		try {
			EdoTest analysis = testsDao.getExamAnalysis(test.getId());
			if(analysis == null || analysis.getId() == null || analysis.getAnalysis() == null || analysis.getAnalysis().getStudentsAppeared() == null) {
				LoggingUtil.logMessage("No test result found for ID .." + test.getId());
				response.setStatus(new EdoApiStatus(STATUS_ERROR, ERROR_RESULT_NOT_FOUND));
				return response;
			}
			List<EdoStudent> students = testsDao.getStudentResults(test.getId());
			response.setStudents(students);
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
			List<EdoQuestion> questions = QuestionParser.parseQuestionPaper(filePath, firstQuestion, solutionPath);
			if(CollectionUtils.isNotEmpty(questions)) {
				LoggingUtil.logMessage("Total questions parsed =>" + questions.size());
				for(EdoQuestion question: questions) {
					question.setSubjectId(subjectId);
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


}
