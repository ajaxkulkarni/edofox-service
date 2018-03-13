package com.rns.web.edo.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.rns.web.edo.service.bo.api.EdoAdminBo;
import com.rns.web.edo.service.domain.EdoServiceRequest;
import com.rns.web.edo.service.domain.EdoServiceResponse;
import com.rns.web.edo.service.util.CommonUtils;
import com.rns.web.edo.service.util.LoggingUtil;

@Component
@Path("/admin")
public class EdoAdminController {
	
	@Autowired(required = true)
	@Qualifier(value = "adminBo")
	EdoAdminBo adminBo;
	
	public void setAdminBo(EdoAdminBo adminBo) {
		this.adminBo = adminBo;
	}
	public EdoAdminBo getAdminBo() {
		return adminBo;
	}
	
	@POST
	@Path("/getTestAnalysis")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse getTestAnalysis(EdoServiceRequest request) {
		LoggingUtil.logMessage("Get Test Analysis Request :" + request);
		EdoServiceResponse response = CommonUtils.initResponse();
		try {
			response =  adminBo.getTestAnalysis(request.getTest());
		} catch (Exception e) {
			e.printStackTrace();
		}
		LoggingUtil.logMessage("Get Test analysis Response");
		return response;
	}
	
	@POST
	@Path("/getTestResults")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse getTestResults(EdoServiceRequest request) {
		LoggingUtil.logMessage("Get Test Results Request :" + request);
		EdoServiceResponse response = CommonUtils.initResponse();
		try {
			response =  adminBo.getTestResults(request.getTest());
		} catch (Exception e) {
			e.printStackTrace();
		}
		LoggingUtil.logMessage("Get Test Results Response");
		return response;
	}
	
	@POST
	@Path("/uploadTest")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse uploadTest(EdoServiceRequest request) {
		LoggingUtil.logMessage("Upload Test Request :" + request);
		EdoServiceResponse response = CommonUtils.initResponse();
		try {
			response.setStatus(adminBo.fileUploadTestQuestions(request.getFilePath(), request.getFirstQuestion(), request.getTest(), request.getSubjectId()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		LoggingUtil.logMessage("Upload Test Response");
		return response;
	}
	

}
