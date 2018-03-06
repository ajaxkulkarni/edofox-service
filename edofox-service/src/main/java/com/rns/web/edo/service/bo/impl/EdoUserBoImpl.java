package com.rns.web.edo.service.bo.impl;

import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.rns.web.edo.service.bo.api.EdoFile;
import com.rns.web.edo.service.bo.api.EdoUserBo;
import com.rns.web.edo.service.dao.EdoTestsDao;
import com.rns.web.edo.service.domain.EDOInstitute;
import com.rns.web.edo.service.domain.EDOPackage;
import com.rns.web.edo.service.domain.EdoApiStatus;
import com.rns.web.edo.service.domain.EdoPaymentStatus;
import com.rns.web.edo.service.domain.EdoQuestion;
import com.rns.web.edo.service.domain.EdoServiceRequest;
import com.rns.web.edo.service.domain.EdoServiceResponse;
import com.rns.web.edo.service.domain.EdoStudent;
import com.rns.web.edo.service.domain.EdoTest;
import com.rns.web.edo.service.domain.EdoTestQuestionMap;
import com.rns.web.edo.service.domain.EdoTestStudentMap;
import com.rns.web.edo.service.util.CommonUtils;
import com.rns.web.edo.service.util.EdoConstants;
import com.rns.web.edo.service.util.LoggingUtil;
import com.rns.web.edo.service.util.PaymentUtil;

public class EdoUserBoImpl implements EdoUserBo, EdoConstants {

	private ThreadPoolTaskExecutor executor;
	private EdoTestsDao testsDao;
	private String filePath;

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

	public EdoServiceResponse getTestResult(EdoServiceRequest request) {
		EdoServiceResponse response = new EdoServiceResponse();
		if (request.getStudent() == null || request.getStudent().getId() == null || request.getTest() == null || request.getTest().getId() == null) {
			response.setStatus(new EdoApiStatus(STATUS_ERROR, ERROR_INCOMPLETE_REQUEST));
			return response;
		}
		// create ObjectMapper instance
		/*ObjectMapper mapper = new ObjectMapper();
		EdoTest result = null;
		// convert json string to object
		try {
			String fileName = "test_" + student.getTest().getId() + ".json";
			result = mapper.readValue(new File(filePath + fileName), EdoTest.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (result == null || CollectionUtils.isEmpty(result.getTest())) {
			return student;
		}
		student.getTest().setTest(result.getTest());
		// Get solved questions
		// List<EdoQuestion> unsolved = testsDao.getTestUnsolved(student);
		for (EdoQuestion question : result.getTest()) {
			EdoQuestion currentQuestion = testsDao.getQuestion(question.getQn_id());
			if (currentQuestion != null) {
				question.setAnswer(currentQuestion.getAnswer());
				question.setChapter(currentQuestion.getChapter());
				student.getTest().setCurrentQuestion(currentQuestion);
				List<EdoQuestion> solved = testsDao.getTestResult(student);
				if (CollectionUtils.isNotEmpty(solved)) {
					question.setResponse(solved.get(0).getResponse());
					//System.out.println("Response:" + question.getResponse());
					question.setResult(solved.get(0).getResult());
					if(StringUtils.equals(StringUtils.trim(question.getResponse()), StringUtils.trim(question.getAnswer()))) {
						question.setCorrect(true);
					}
				}
			}
		}*/
		List<EdoTestQuestionMap> map = testsDao.getExamResult(request);
		
		if(CollectionUtils.isNotEmpty(map)) {
			EdoTest test = map.get(0).getTest();
			for(EdoTestQuestionMap mapper: map) {
				test.getTest().add(mapper.getQuestion());
			}
			response.setTest(test);
		}
		
		return response;
	}

	public EdoTest getTest(Integer testId, Integer studenId) {
		if(testId == null) {
			return null;
		}
		
		try {
			EdoTestStudentMap inputMap = new EdoTestStudentMap();
			inputMap.setTest(new EdoTest(testId));
			inputMap.setStudent(new EdoStudent(studenId));
			EdoTestStudentMap studentMap = testsDao.getTestStatus(inputMap);
			if(studentMap != null && StringUtils.equals(TEST_STATUS_COMPLETED, studentMap.getStatus())) {
				EdoTest result = new EdoTest();
				result.setSubmitted(true);
				return result;
			}
			
			List<EdoTestQuestionMap> map = testsDao.getExam(testId);
			
			if(CollectionUtils.isNotEmpty(map)) {
				EdoTest result = map.get(0).getTest();
				Integer count = 1;
				for(EdoTestQuestionMap mapper: map) {
					EdoQuestion question = mapper.getQuestion();
					if(question != null) {
						question.setId(count);
						setQuestionURLs(question);
						if(!result.getSubjects().contains(question.getSubject())) {
							result.getSubjects().add(question.getSubject());
						}
						result.getTest().add(question);
						count++;
					}
				}
				return result;
			}
			
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
		}
		
		return null;
	}

	private void setQuestionURLs(EdoQuestion question) {
		if(StringUtils.isNotBlank(question.getQuestionImageUrl())) {
			question.setQuestionImageUrl(HOST_NAME + "getImage/" + question.getQn_id() + "/" + ATTR_QUESTION);
		}
		if(StringUtils.isNotBlank(question.getOption1ImageUrl())) {
			question.setQuestionImageUrl(HOST_NAME + "getImage/" + question.getQn_id() + "/" + ATTR_OPTION1);
		}
		if(StringUtils.isNotBlank(question.getOption2ImageUrl())) {
			question.setQuestionImageUrl(HOST_NAME + "getImage/" + question.getQn_id() + "/" + ATTR_OPTION2);
		}
		if(StringUtils.isNotBlank(question.getOption3ImageUrl())) {
			question.setQuestionImageUrl(HOST_NAME + "getImage/" + question.getQn_id() + "/" + ATTR_OPTION3);
		}
		if(StringUtils.isNotBlank(question.getOption4ImageUrl())) {
			question.setQuestionImageUrl(HOST_NAME + "getImage/" + question.getQn_id() + "/" + ATTR_OPTION4);
		}
	}

	public EdoApiStatus saveTest(EdoServiceRequest request) {
		EdoTest test = request.getTest();
		if(request.getStudent() == null || test == null) {
			return new EdoApiStatus(-111, ERROR_IN_PROCESSING);
		}
		
		EdoApiStatus status = new EdoApiStatus();
		try {
			EdoTestStudentMap inputMap = new EdoTestStudentMap();
			inputMap.setTest(test);
			inputMap.setStudent(request.getStudent());
			EdoTestStudentMap map = testsDao.getTestStatus(inputMap);
			if(map != null && StringUtils.equals(TEST_STATUS_COMPLETED, map.getStatus())) {
				status.setResponseText(ERROR_TEST_ALREADY_SUBMITTED);
				status.setStatusCode(STATUS_ERROR);
				LoggingUtil.logMessage("Already submitted this test for student=>" + request.getStudent().getId());
				return status;
			}
			
			List<EdoQuestion> questions = testsDao.getExamQuestions(test.getId());
			if(CollectionUtils.isEmpty(questions)) {
				status.setResponseText(ERROR_IN_PROCESSING);
				status.setStatusCode(STATUS_ERROR);
				return status;
			}
			
			Integer solvedCount = 0;
			Integer correctCount = 0;
			Integer flaggedCount = 0;
			BigDecimal score = BigDecimal.ZERO;
			for(EdoQuestion answered: test.getTest()) {
				if(StringUtils.isNotBlank(answered.getAnswer())) {
					for(EdoQuestion question: questions) {
						if(question.getQn_id() == answered.getQn_id() && StringUtils.equalsIgnoreCase(answered.getAnswer(), question.getCorrectAnswer())) {
							correctCount++;
							if(question.getWeightage() != null) {
								score = score.add(new BigDecimal(question.getWeightage()));
							}
							break;
						} else {
							if(question.getNegativeMarks() != null) {
								score = score.subtract(new BigDecimal(question.getNegativeMarks()));
							}
							break;
						}
					}
					solvedCount++;
				}
				if(answered.getFlagged() != null && answered.getFlagged() == 1) {
					flaggedCount++;
				}
			}
			test.setCorrectCount(correctCount);
			test.setFlaggedCount(flaggedCount);
			test.setSolvedCount(solvedCount);
			test.setScore(score);
			testsDao.saveTestResult(request);
			testsDao.saveTestStatus(request);
		} catch (Exception e) {
			status.setStatusCode(STATUS_ERROR);
			status.setResponseText(ERROR_IN_PROCESSING);
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
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
			}
			
			if(path != null) {
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
					testsDao.createStudentPackage(student);
				}
			} else {
				testsDao.createStudentPackage(student);
			}
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
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			response.setStatus(new EdoApiStatus(STATUS_ERROR, ERROR_IN_PROCESSING));
		}
		return response;
	}

	public EdoApiStatus processPayment(String id, String transactionId, String paymentId) {
		EdoApiStatus status = new EdoApiStatus();
		try {
			boolean validPayment = PaymentUtil.getPaymentStatus(id);
			if(validPayment) {
				EdoPaymentStatus paymentStatus = new EdoPaymentStatus();
				paymentStatus.setPaymentId(id);
				paymentStatus.setResponseText("Completed");
				testsDao.updatePayment(paymentStatus);
			} else {
				EdoPaymentStatus paymentStatus = new EdoPaymentStatus();
				paymentStatus.setPaymentId(id);
				paymentStatus.setResponseText("Failed");
				testsDao.updatePayment(paymentStatus);
			}
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
		}
		return status;
	}

}
