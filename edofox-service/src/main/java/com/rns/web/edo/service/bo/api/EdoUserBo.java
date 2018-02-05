package com.rns.web.edo.service.bo.api;

import com.rns.web.edo.service.domain.EdoApiStatus;
import com.rns.web.edo.service.domain.EdoServiceRequest;
import com.rns.web.edo.service.domain.EdoServiceResponse;
import com.rns.web.edo.service.domain.EdoTest;

public interface EdoUserBo {

	EdoServiceResponse getTestResult(EdoServiceRequest request);
	EdoTest getTest(Integer testId, Integer studentId);
	EdoApiStatus saveTest(EdoServiceRequest request);
	
}
