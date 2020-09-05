package com.rns.web.edo.service.bo.api;

import java.io.InputStream;

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
	EdoFile getQuestionImage(Integer questionId, String imageType, Integer testId);
	EdoServiceResponse getPackages(EDOInstitute institute);
	EdoServiceResponse getPackages(EdoStudent student);
	EdoServiceResponse registerStudent(EdoStudent student);
	EdoPaymentStatus processPayment(String id, String transactionId, String paymentId, String txStatus);
	EdoPaymentStatus completePayment(EdoTest test, EdoStudent student);
	EdoFile getStudentImage(Integer studentId);
	EdoServiceResponse getAllSubjects(EdoServiceRequest request);
	EdoServiceResponse getNextQuestion(EdoServiceRequest request);
	EdoServiceResponse submitAnswer(EdoServiceRequest request);
	EdoServiceResponse raiseDoubt(EdoServiceRequest request);
	EdoApiStatus saveAnswer(EdoServiceRequest request);
	EdoServiceResponse getSolved(EdoServiceRequest request);
	EdoServiceResponse uploadRecording(Integer sessionId, InputStream data, Integer packageId);
	EdoServiceResponse startLiveSession(EdoServiceRequest request);
	EdoServiceResponse getLiveSessions(EdoServiceRequest request);
	EdoServiceResponse finishRecording(EdoServiceRequest request);
	EdoServiceResponse getSession(EdoServiceRequest request);
	EdoServiceResponse uploadVideo(InputStream videoData, String title, Integer instituteId, 
			Integer subjectId, Integer packageId, Integer topicId, String keywords, InputStream questionFile, String fileName, String classrooms);
	EdoServiceResponse getVideoLectures(EdoServiceRequest request);
	EdoServiceResponse login(EdoServiceRequest request);
	EdoApiStatus updateStudentActivity(EdoServiceRequest request);
	EdoApiStatus updateVideoLecture(EdoServiceRequest request);
	EdoServiceResponse getTags(Integer instituteId, String query);
	EdoServiceResponse getDeeperRegistration(String rollNo);
	EdoFile getVideo(Integer videoId);
	EdoServiceResponse getStudentExams(EdoServiceRequest request);
	EdoServiceResponse getQuestionAnalysis(EdoServiceRequest request);
	EdoApiStatus addDeviceId(EdoServiceRequest request);
	EdoServiceResponse getStudentSubjects(EdoServiceRequest request);
	EdoServiceResponse getStudentChapters(EdoServiceRequest request);
	EdoServiceResponse getChapterContent(EdoServiceRequest request);
	
}
