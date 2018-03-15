package com.rns.web.edo.service.bo.api;

import com.rns.web.edo.service.domain.EDOInstitute;
import com.rns.web.edo.service.domain.EdoApiStatus;
import com.rns.web.edo.service.domain.EdoServiceResponse;
import com.rns.web.edo.service.domain.EdoTest;

public interface EdoAdminBo {
	
	EdoServiceResponse getTestAnalysis(EdoTest test);
	EdoServiceResponse getTestResults(EdoTest test);
	EdoServiceResponse getAllStudents(EDOInstitute institute);
	EdoApiStatus fileUploadTestQuestions(String filePath, Integer firstQuestion, EdoTest test, Integer subjectId, String solutionPath);

}
