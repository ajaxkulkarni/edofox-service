package com.rns.web.edo.service.bo.api;

import java.io.InputStream;
import java.util.List;

import com.rns.web.edo.service.domain.EDOInstitute;
import com.rns.web.edo.service.domain.EdoApiStatus;
import com.rns.web.edo.service.domain.EdoPaymentStatus;
import com.rns.web.edo.service.domain.EdoServiceRequest;
import com.rns.web.edo.service.domain.EdoServiceResponse;
import com.rns.web.edo.service.domain.EdoStudent;
import com.rns.web.edo.service.domain.EdoTest;
import com.rns.web.edo.service.domain.jpa.EdoClasswork;
import com.sun.jersey.multipart.FormDataBodyPart;

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
	EdoApiStatus uploadAnswers(List<FormDataBodyPart> bodyParts, Integer testId, Integer studentId, String requestType);
	EdoServiceResponse getUploadedAnswers(EdoServiceRequest request);
	EdoServiceResponse getSolved(EdoServiceRequest request);
	EdoServiceResponse uploadRecording(Integer sessionId, InputStream data, Integer packageId);
	EdoServiceResponse startLiveSession(EdoServiceRequest request);
	EdoServiceResponse getLiveSessions(EdoServiceRequest request);
	EdoServiceResponse finishRecording(EdoServiceRequest request);
	EdoServiceResponse getSession(EdoServiceRequest request);
	/*EdoServiceResponse uploadVideo(InputStream videoData, String title, Integer instituteId, 
			Integer subjectId, Integer packageId, Integer topicId, String keywords, InputStream questionFile, String fileName);
	*/
	EdoServiceResponse getVideoLectures(EdoServiceRequest request);
	EdoServiceResponse login(EdoServiceRequest request);
	EdoApiStatus updateStudentActivity(EdoServiceRequest request);
	EdoApiStatus updateVideoLecture(EdoServiceRequest request);
	EdoServiceResponse getTags(Integer instituteId, String query);
	
	//ERP changes
	EdoServiceResponse uploadVideo(InputStream videoData, EdoClasswork classwork, String maps, String fileName);
	EdoFile getDocument(Integer docId);
	EdoApiStatus sendEmail(EdoServiceRequest request);
	EdoFile getVideo(Integer docId);
	EdoApiStatus addDeviceId(EdoServiceRequest request);
	EdoApiStatus sendNotification(EdoServiceRequest request);
	
}
