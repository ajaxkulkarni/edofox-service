package com.rns.web.edo.service.bo.api;

import java.io.InputStream;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.rns.web.edo.service.domain.EDOInstitute;
import com.rns.web.edo.service.domain.EdoApiStatus;
import com.rns.web.edo.service.domain.EdoPaymentStatus;
import com.rns.web.edo.service.domain.EdoServiceRequest;
import com.rns.web.edo.service.domain.EdoServiceResponse;
import com.rns.web.edo.service.domain.EdoStudent;
import com.rns.web.edo.service.domain.EdoTest;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataBodyPart;

public interface EdoUserBo {

	EdoServiceResponse getTestResult(EdoServiceRequest request);
	EdoServiceResponse getTest(EdoServiceRequest request);
	EdoApiStatus saveTest(EdoServiceRequest request);
	EdoFile getQuestionImage(Integer questionId, String imageType, Integer testId);
	EdoServiceResponse getPackages(EdoServiceRequest request);
	EdoServiceResponse getPackages(EdoStudent student);
	EdoServiceResponse registerStudent(EdoStudent student);
	EdoPaymentStatus processPayment(String id, String transactionId, String paymentId, String txStatus);
	EdoPaymentStatus completePayment(EdoTest test, EdoStudent student);
	EdoFile getStudentImage(Integer studentId);
	EdoServiceResponse getAllSubjects(EdoServiceRequest request);
	EdoServiceResponse getNextQuestion(EdoServiceRequest request);
	EdoServiceResponse submitAnswer(EdoServiceRequest request);
	EdoServiceResponse raiseDoubt(EdoServiceRequest request, InputStream fileData, FormDataContentDisposition fileDataDetails);
	EdoApiStatus saveAnswer(EdoServiceRequest request);
	EdoServiceResponse getSolved(EdoServiceRequest request);
	EdoServiceResponse uploadRecording(Integer sessionId, InputStream data, Integer packageId);
	EdoServiceResponse startLiveSession(EdoServiceRequest request);
	EdoServiceResponse getLiveSessions(EdoServiceRequest request);
	EdoServiceResponse finishRecording(EdoServiceRequest request);
	EdoServiceResponse getSession(EdoServiceRequest request);
	EdoServiceResponse uploadVideo(InputStream videoData, String title, Integer instituteId, 
			Integer subjectId, Integer packageId, Integer topicId, String keywords, InputStream questionFile, String fileName, String classrooms, String type);
	EdoServiceResponse getVideoLectures(EdoServiceRequest request);
	EdoServiceResponse login(EdoServiceRequest request);
	EdoApiStatus updateStudentActivity(EdoServiceRequest request);
	EdoApiStatus updateVideoLecture(EdoServiceRequest request);
	EdoServiceResponse getTags(Integer instituteId, String query);
	EdoServiceResponse getDeeperRegistration(String rollNo);
	EdoFile getVideo(Integer videoId, String requestType);
	EdoServiceResponse getStudentExams(EdoServiceRequest request);
	EdoServiceResponse getQuestionAnalysis(EdoServiceRequest request);
	EdoApiStatus addDeviceId(EdoServiceRequest request);
	EdoServiceResponse getStudentSubjects(EdoServiceRequest request);
	EdoServiceResponse getStudentChapters(EdoServiceRequest request);
	EdoServiceResponse getChapterContent(EdoServiceRequest request);
	EdoServiceResponse getChapterExams(EdoServiceRequest request);
	EdoServiceResponse joinSession(EdoServiceRequest request);
	EdoServiceResponse getStudentPerformance(EdoServiceRequest request);
	EdoApiStatus updateStudentTestActivity(EdoServiceRequest request, HttpServletRequest servletRequest);
	EdoServiceResponse createVideoLecture(EdoServiceRequest request);
	EdoServiceResponse getFeedbackDetails(EdoServiceRequest request);
	EdoServiceResponse getUploadedAnswers(EdoServiceRequest request);
	EdoApiStatus uploadAnswers(List<FormDataBodyPart> bodyParts, Integer testId, Integer studentId, Integer questionId);
	EdoServiceResponse saveSubjectiveAnswer(EdoServiceRequest request);
	EdoServiceResponse getAppVersion(EdoServiceRequest request);
	EdoApiStatus updateProfile(EdoServiceRequest request);
	EdoServiceResponse matchFaces(EdoServiceRequest request, InputStream file, FormDataContentDisposition fileDetails);
	EdoServiceResponse saveProctorImage(EdoServiceRequest request);
	EdoServiceResponse uploadProctorRef(EdoServiceRequest request, InputStream recordingData, FormDataContentDisposition recordingDataDetails);
	EdoServiceResponse saveProctorRefImageUrl(EdoServiceRequest request);
	EdoApiStatus forgotPassword(EdoServiceRequest request);
	EdoServiceResponse getStudentActivity(EdoServiceRequest student);
	
	//TODO To be removed
	EdoApiStatus saveTestNoCommit(EdoServiceRequest request);
	
}
