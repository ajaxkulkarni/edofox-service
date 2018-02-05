package com.rns.web.edo.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.rns.web.edo.service.bo.api.EdoUserBo;
import com.rns.web.edo.service.domain.EdoServiceRequest;
import com.rns.web.edo.service.domain.EdoServiceResponse;
import com.rns.web.edo.service.domain.EdoStudent;
import com.rns.web.edo.service.domain.EdoTest;
import com.rns.web.edo.service.util.CommonUtils;
import com.rns.web.edo.service.util.LoggingUtil;

@Component
@Path("/service")
public class EdoUserController {

	private static final String APPLICATION_PDF = "application/pdf";
	@Autowired(required = true)
	@Qualifier(value = "userBo")
	EdoUserBo userBo;
	
	public void setUserBo(EdoUserBo userBo) {
		this.userBo = userBo;
	}
	
	public EdoUserBo getUserBo() {
		return userBo;
	}
	
	@POST
	@Path("/getTestResult")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse getTestResults(EdoServiceRequest request) {
		LoggingUtil.logMessage("Get Test result Request :" + request);
		EdoServiceResponse response = CommonUtils.initResponse();
		try {
			response =  userBo.getTestResult(request);
		} catch (Exception e) {
			e.printStackTrace();
		}
		LoggingUtil.logMessage("Get Test result Response");
		return response;
	}
	
	@GET
	@Path("/getTest/{testId}/{studentId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse getTest(@PathParam("testId") Integer testId, @PathParam ("studentId") Integer studentId) {
		LoggingUtil.logMessage("Get Test Request :" + testId);
		EdoServiceResponse response = CommonUtils.initResponse();
		try {
			EdoTest test =  userBo.getTest(testId, studentId);
			response.setTest(test);
		} catch (Exception e) {
			e.printStackTrace();
		}
		LoggingUtil.logMessage("Get Test Response");
		return response;
	}
	
	@POST
	@Path("/saveTest")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse saveTest(EdoServiceRequest request) {
		LoggingUtil.logMessage("Save Test result Request :" + request);
		EdoServiceResponse response = CommonUtils.initResponse();
		try {
			response.setStatus(userBo.saveTest(request));
		} catch (Exception e) {
			e.printStackTrace();
		}
		LoggingUtil.logMessage("Save Test result Response");
		return response;
	}
	
}
