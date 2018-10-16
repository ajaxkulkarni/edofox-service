package com.rns.web.edo.service.bo.api;

import com.rns.web.edo.service.domain.EDOInstitute;
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
	EdoFile getQuestionImage(Integer questionId, String imageType);
	EdoServiceResponse getPackages(EDOInstitute institute);
	EdoServiceResponse registerStudent(EdoStudent student);
	EdoApiStatus processPayment(String id, String transactionId, String paymentId);
	EdoPaymentStatus completePayment(EdoTest test, EdoStudent student);
	EdoFile getStudentImage(Integer studentId);
	
	
}
