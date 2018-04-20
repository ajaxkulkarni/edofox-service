package com.rns.web.edo.service.bo.impl;

import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
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
import com.rns.web.edo.service.domain.EdoTest;
import com.rns.web.edo.service.domain.EdoTestQuestionMap;
import com.rns.web.edo.service.domain.EdoTestStudentMap;
import com.rns.web.edo.service.util.CommonUtils;
import com.rns.web.edo.service.util.EdoConstants;
import com.rns.web.edo.service.util.EdoMailUtil;
import com.rns.web.edo.service.util.EdoSMSUtil;
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
		List<EdoTestQuestionMap> map = testsDao.getExamResult(request);
		
		Map<String, BigDecimal> subjectWiseScore = new HashMap<String, BigDecimal>();
		
		if(CollectionUtils.isNotEmpty(map)) {
			EdoTest test = map.get(0).getTest();
			for(EdoTestQuestionMap mapper: map) {
				EdoQuestion question = mapper.getQuestion();
				test.getTest().add(question);
				if(StringUtils.isBlank(StringUtils.trimToEmpty(question.getAnswer()))) {
					continue;
				}
				BigDecimal score = subjectWiseScore.get(question.getSubject());
				if(score == null) {
					score = new BigDecimal(0);
				}
				if(StringUtils.equalsIgnoreCase(question.getAnswer(), question.getCorrectAnswer())) {
					if (question.getWeightage() != null) {
						score = score.add(new BigDecimal(question.getWeightage()));
					}
				} else if (question.getNegativeMarks() != null) {
					score = score.subtract(new BigDecimal(question.getNegativeMarks()));
				}
				subjectWiseScore.put(question.getSubject(), score);
				
			}
			if(!subjectWiseScore.isEmpty()) {
				EDOTestAnalysis analysis = new EDOTestAnalysis();
				analysis.setSubjectWiseScore(subjectWiseScore);
				test.setAnalysis(analysis);
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
			inputMap.setStudent(new EdoStudent(studenId));
			EdoTestStudentMap studentMap = testsDao.getTestStatus(inputMap);
			if(studentMap != null && StringUtils.equals(TEST_STATUS_COMPLETED, studentMap.getStatus())) {
				response.setStatus(new EdoApiStatus(STATUS_TEST_SUBMITTED, ERROR_TEST_ALREADY_SUBMITTED));
				return response;
			}
			
			studentMap = testsDao.getStudentActivePackage(inputMap);
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
			
			List<EdoTestQuestionMap> map = testsDao.getExam(testId);
			
			if(CollectionUtils.isNotEmpty(map)) {
				EdoTest result = map.get(0).getTest();
				Integer count = 1;
				for(EdoTestQuestionMap mapper: map) {
					EdoQuestion question = mapper.getQuestion();
					if(question != null) {
						question.setId(count);
						setQuestionURLs(question);
						prepareMatchTypeQuestion(question);
						if(!result.getSubjects().contains(question.getSubject())) {
							result.getSubjects().add(question.getSubject());
						}
						if(StringUtils.isNotBlank(question.getSection()) && !result.getSections().contains(question.getSection())) {
							result.getSections().add(question.getSection());
						}
						result.getTest().add(question);
						count++;
					}
				}
				response.setTest(result);
			}
			
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			response.setStatus(new EdoApiStatus(STATUS_ERROR, ERROR_IN_PROCESSING));
		}
		
		return response;
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

	private void setQuestionURLs(EdoQuestion question) {
		if(StringUtils.isNotBlank(question.getQuestionImageUrl())) {
			question.setQuestionImageUrl(HOST_NAME + "getImage/" + question.getQn_id() + "/" + ATTR_QUESTION);
		}
		if(StringUtils.isNotBlank(question.getOption1ImageUrl())) {
			question.setOption1ImageUrl(HOST_NAME + "getImage/" + question.getQn_id() + "/" + ATTR_OPTION1);
		}
		if(StringUtils.isNotBlank(question.getOption2ImageUrl())) {
			question.setOption2ImageUrl(HOST_NAME + "getImage/" + question.getQn_id() + "/" + ATTR_OPTION2);
		}
		if(StringUtils.isNotBlank(question.getOption3ImageUrl())) {
			question.setOption3ImageUrl(HOST_NAME + "getImage/" + question.getQn_id() + "/" + ATTR_OPTION3);
		}
		if(StringUtils.isNotBlank(question.getOption4ImageUrl())) {
			question.setOption4ImageUrl(HOST_NAME + "getImage/" + question.getQn_id() + "/" + ATTR_OPTION4);
		}
		if(StringUtils.isNotBlank(question.getMetaDataImageUrl())) {
			question.setMetaDataImageUrl(HOST_NAME + "getImage/" + question.getQn_id() + "/" + ATTR_META_DATA);
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
			CommonUtils.calculateTestScore(test, questions);
			
			if(request != null && request.getTest() != null && CollectionUtils.isNotEmpty(request.getTest().getTest())) {
				testsDao.saveTestResult(request);
				testsDao.saveTestStatus(request);
				
				EdoSMSUtil smsUtil = new EdoSMSUtil(MAIL_TYPE_TEST_RESULT);
				smsUtil.setTest(test);
				EdoStudent student = testsDao.getStudentById(request.getStudent().getId());
				smsUtil.setStudent(student);
				executor.execute(smsUtil);
			}
			
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
			} else if (StringUtils.equals(imageType, ATTR_META_DATA)) {
				path = question.getMetaDataImageUrl();
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

}
