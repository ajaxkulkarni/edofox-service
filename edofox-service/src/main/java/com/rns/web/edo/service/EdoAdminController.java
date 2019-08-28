package com.rns.web.edo.service;

import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.rns.web.edo.service.bo.api.EdoAdminBo;
import com.rns.web.edo.service.domain.EDOInstitute;
import com.rns.web.edo.service.domain.EdoServiceRequest;
import com.rns.web.edo.service.domain.EdoServiceResponse;
import com.rns.web.edo.service.util.CommonUtils;
import com.rns.web.edo.service.util.EdoExcelUtil;
import com.rns.web.edo.service.util.LoggingUtil;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

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
			response =  adminBo.getTestResults(request);
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
			response.setStatus(adminBo.fileUploadTestQuestions(request.getFilePath(), request.getFirstQuestion(), request.getTest(), request.getSubjectId(), request.getSolutionPath()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		LoggingUtil.logMessage("Upload Test Response");
		return response;
	}
	
	@POST
	@Path("/uploadSolution")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse uploadSolution(EdoServiceRequest request) {
		LoggingUtil.logMessage("Upload Solutions Request :" + request);
		EdoServiceResponse response = CommonUtils.initResponse();
		try {
			response.setStatus(adminBo.fileUploadTestSolutions(request.getFilePath(), request.getTest(), request.getSubjectId()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		LoggingUtil.logMessage("Upload Solutions Response");
		return response;
	}
	
	@POST
	@Path("/revaluateResult")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse revaluateResult(EdoServiceRequest request) {
		LoggingUtil.logMessage("Revaluate Solutions Request :" + request);
		EdoServiceResponse response = CommonUtils.initResponse();
		try {
			response.setStatus(adminBo.revaluateResult(request));
		} catch (Exception e) {
			e.printStackTrace();
		}
		LoggingUtil.logMessage("Revaluate Solutions Response");
		return response;
	}
	
	@POST
	@Path("/uploadStudents")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse uploadStudents(EdoServiceRequest request) {
		LoggingUtil.logMessage("Upload Students Request :" + request);
		EdoServiceResponse response = CommonUtils.initResponse();
		try {
			response.setStatus(adminBo.bulkUploadStudents(request));
		} catch (Exception e) {
			e.printStackTrace();
		}
		LoggingUtil.logMessage("Upload Students Response");
		return response;
	}
	
	@POST
	@Path("/uploadStudentsExcel")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public EdoServiceResponse uploadStudentsExcel(@FormDataParam("data") InputStream studentData, @FormDataParam("data") FormDataContentDisposition customerDataDetails,
			@FormDataParam("instituteId") Integer instituteId) {
		LoggingUtil.logMessage("Upload Students Excel :" + instituteId);
		EdoServiceResponse response = CommonUtils.initResponse();
		try {
			EdoServiceRequest request = new EdoServiceRequest();
			EDOInstitute institute = new EDOInstitute();
			institute.setId(instituteId);
			request.setInstitute(institute);
			request.setStudents(EdoExcelUtil.extractStudents(studentData, instituteId));
			response.setStatus(adminBo.bulkUploadStudents(request));
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
		}
		LoggingUtil.logMessage("Upload Students Excel Response");
		return response;
	}
	
	@POST
	@Path("/getAllStudents")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse getAllStudents(EdoServiceRequest request) {
		LoggingUtil.logMessage("Get All Students Request :" + request);
		EdoServiceResponse response = CommonUtils.initResponse();
		response = adminBo.getAllStudents(request);
		LoggingUtil.logMessage("Get All Students Response");
		return response;
	}
	
	@POST
	@Path("/parseQuestion")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse parseQuestion(EdoServiceRequest request) {
		LoggingUtil.logMessage("Parse question Request :" + request);
		EdoServiceResponse response = CommonUtils.initResponse();
		response = adminBo.parseQuestion(request);
		LoggingUtil.logMessage("Parse question Response");
		return response;
	}
	
	@POST
	@Path("/getDataEntrySummary")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse getDataEntrySummary(EdoServiceRequest request) {
		LoggingUtil.logMessage("Data entry summary Request :" + request);
		EdoServiceResponse response = CommonUtils.initResponse();
		response = adminBo.getDataEntrySummary(request);
		LoggingUtil.logMessage("Data entry summary Response");
		return response;
	}
	
	@POST
	@Path("/autoCreateExam")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse createExam(EdoServiceRequest request) {
		LoggingUtil.logMessage("Create exam Request :" + request);
		EdoServiceResponse response = CommonUtils.initResponse();
		try {
			response = adminBo.automateTest(request);
		} catch (Exception e) {
			e.printStackTrace();
		}
		LoggingUtil.logMessage("Create exam Response");
		return response;
	}
	
	@POST
	@Path("/resolveDoubt")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse resolveDoubt(EdoServiceRequest request) {
		LoggingUtil.logMessage("Doubt resolve Request :" + request);
		EdoServiceResponse response = adminBo.addFeedbackResolution(request);
		LoggingUtil.logMessage("Doubt resolve Response");
		return response;
	}
	
}
