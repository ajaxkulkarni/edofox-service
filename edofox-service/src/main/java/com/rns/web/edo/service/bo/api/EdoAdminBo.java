package com.rns.web.edo.service.bo.api;

import com.rns.web.edo.service.domain.EdoServiceResponse;
import com.rns.web.edo.service.domain.EdoTest;

public interface EdoAdminBo {
	
	EdoServiceResponse getTestAnalysis(EdoTest test);

}
