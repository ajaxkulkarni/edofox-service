package com.rns.web.edo.service.bo.api;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Date;

import com.rns.web.edo.service.domain.EdoAdminRequest;
import com.rns.web.edo.service.domain.EdoApiStatus;
import com.rns.web.edo.service.domain.EdoServiceRequest;
import com.rns.web.edo.service.domain.EdoServiceResponse;
import com.rns.web.edo.service.domain.EdoTest;
import com.sun.jersey.multipart.FormDataBodyPart;

public interface EdoAdminBo {
	
	EdoServiceResponse getTestAnalysis(EdoTest test);
	EdoServiceResponse getTestResults(EdoServiceRequest request);
	EdoServiceResponse getAllStudents(EdoServiceRequest request);
	EdoApiStatus fileUploadTestQuestions(String filePath, Integer firstQuestion, EdoTest test, Integer subjectId, String solutionPath);
	EdoApiStatus fileUploadTestSolutions(String filePath, EdoTest test, Integer subjectId);
	EdoApiStatus revaluateResult(EdoServiceRequest request);
	EdoApiStatus bulkUploadStudents(EdoServiceRequest request);
	EdoApiStatus registerStudent(EdoServiceRequest request);
	EdoServiceResponse parseQuestion(EdoServiceRequest request);
	EdoServiceResponse getDataEntrySummary(EdoServiceRequest request);
	EdoServiceResponse automateTest(EdoServiceRequest request);
	EdoServiceResponse addFeedbackResolution(EdoServiceRequest request);
	EdoServiceResponse getFeedbackData(EdoServiceRequest request);
	EdoServiceResponse getFeedbackSummary(EdoServiceRequest request);
	EdoServiceResponse getQuestionFeedbacks(EdoServiceRequest request);
	void fixQuestions();
	EdoApiStatus cropQuestionImage(EdoServiceRequest request, InputStream fileData);
	EdoServiceResponse parsePdf(EdoAdminRequest request, InputStream fileData);
	EdoServiceResponse loadParsedQuestions(EdoServiceRequest request);
	EdoApiStatus saveParsedQuestions(EdoServiceRequest request);
	EdoServiceResponse loadQuestionBank(EdoServiceRequest request);
	EdoApiStatus sendNotification(EdoServiceRequest request);
	EdoApiStatus updateStudentScore(EdoServiceRequest request);
	EdoServiceResponse uploadQuestionImage(EdoServiceRequest request, InputStream fileData);
	EdoApiStatus uploadEvaluation(FormDataBodyPart bodyParts, Integer answerId, BigDecimal marks, Integer evaluatorId, BigDecimal questionMarks, BigDecimal testMarks); 
	EdoApiStatus calculateProctoringScore(EdoServiceRequest request);
	EdoApiStatus fixStudentScore(EdoServiceRequest request);
	EdoServiceResponse getSubjectAnalysis(EdoServiceRequest request);
	
	//Super admin
	EdoApiStatus backupData(EdoAdminRequest request);
	EdoApiStatus uplinkData(EdoAdminRequest request);
	EdoAdminRequest downloadData(EdoAdminRequest request);
	EdoApiStatus downlinkData(EdoAdminRequest request);
	EdoAdminRequest getLastUplinkDate();
	EdoServiceResponse createInstitute(EdoAdminRequest request);
	EdoApiStatus savePendingVideos(EdoServiceRequest request);
	EdoApiStatus upgradeClient(EdoServiceRequest request);
	EdoApiStatus updateClientSales(EdoServiceRequest request);
	EdoServiceResponse fixRecordedFile(EdoServiceRequest request);
	EdoApiStatus fixRecordedLectures(EdoServiceRequest request);
	EdoServiceResponse getLiveAnalysis(EdoServiceRequest request);
	EdoServiceResponse savePackage(EdoServiceRequest request);
	EdoServiceResponse updateEdofoxTokens(EdoServiceRequest request);
	EdoServiceResponse getProctoringVideo(EdoServiceRequest request);
	EdoApiStatus updateEmailStatus(String request, String string);
	EdoServiceResponse getExamSummary(EdoAdminRequest request);
	EdoApiStatus notifyForContent(EdoAdminRequest request);
	
}
