package com.rns.web.edo.service.bo.api;

import com.rns.web.edo.service.domain.EdoApiStatus;
import com.rns.web.edo.service.domain.EdoServiceRequest;
import com.rns.web.edo.service.domain.EdoServiceResponse;
import com.rns.web.edo.service.domain.EdoTest;

public interface EdoAdminBo {
	
	EdoServiceResponse getTestAnalysis(EdoTest test);
	EdoServiceResponse getTestResults(EdoServiceRequest request);
	EdoServiceResponse getAllStudents(EdoServiceRequest request);
	EdoApiStatus fileUploadTestQuestions(String filePath, Integer firstQuestion, EdoTest test, Integer subjectId, String solutionPath);
	EdoApiStatus fileUploadTestSolutions(String filePath, EdoTest test, Integer subjectId);
	EdoApiStatus revaluateResult(EdoServiceRequest request);
	EdoApiStatus bulkUploadStudents(EdoServiceRequest request);
	EdoServiceResponse parseQuestion(EdoServiceRequest request);
}
