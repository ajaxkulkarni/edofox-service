package com.rns.web.edo.service.bo.api;

import java.io.InputStream;

import com.rns.web.edo.service.domain.EDOInstitute;
import com.rns.web.edo.service.domain.EDOPackage;
import com.rns.web.edo.service.domain.EdoApiStatus;
import com.rns.web.edo.service.domain.EdoPaymentStatus;
import com.rns.web.edo.service.domain.EdoServiceRequest;
import com.rns.web.edo.service.domain.EdoServiceResponse;
import com.rns.web.edo.service.domain.EdoStudent;
import com.rns.web.edo.service.domain.EdoTest;

public interface EdoUserBo {

	EdoServiceResponse getTestResult(EdoServiceRequest request);
	EdoServiceResponse getTest(Integer testId, Integer studentId);
	EdoApiStatus saveTest(EdoServiceRequest request);
	EdoFile getQuestionImage(Integer questionId, String imageType, Integer testId);
	EdoServiceResponse getPackages(EDOInstitute institute);
	EdoServiceResponse getPackages(EdoStudent student);
	EdoServiceResponse registerStudent(EdoStudent student);
	EdoApiStatus processPayment(String id, String transactionId, String paymentId);
	EdoPaymentStatus completePayment(EdoTest test, EdoStudent student);
	EdoFile getStudentImage(Integer studentId);
	EdoServiceResponse getAllSubjects();
	EdoServiceResponse getNextQuestion(EdoServiceRequest request);
	EdoServiceResponse submitAnswer(EdoServiceRequest request);
	EdoServiceResponse raiseDoubt(EdoServiceRequest request);
	EdoApiStatus saveAnswer(EdoServiceRequest request);
	EdoServiceResponse getSolved(EdoServiceRequest request);
	EdoServiceResponse uploadRecording(Integer sessionId, InputStream data, Integer packageId);
	EdoServiceResponse startLiveSession(EdoServiceRequest request);
	EdoServiceResponse getLiveSessions(EdoServiceRequest request);
	EdoServiceResponse finishRecording(EdoServiceRequest request);
}
