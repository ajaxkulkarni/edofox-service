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
import com.rns.web.edo.service.domain.EdoAdminRequest;
import com.rns.web.edo.service.domain.EdoApiStatus;
import com.rns.web.edo.service.domain.EdoServiceRequest;
import com.rns.web.edo.service.domain.EdoServiceResponse;
import com.rns.web.edo.service.domain.EdoTest;
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
			@FormDataParam("instituteId") Integer instituteId, @FormDataParam("type") String type, @FormDataParam("packageId") Integer packageId) {
		LoggingUtil.logMessage("Upload Students Excel :" + instituteId);
		EdoServiceResponse response = CommonUtils.initResponse();
		try {
			EdoServiceRequest request = new EdoServiceRequest();
			EDOInstitute institute = new EDOInstitute();
			institute.setId(instituteId);
			request.setInstitute(institute);
			request.setStudents(EdoExcelUtil.extractStudents(studentData, instituteId, packageId));
			request.setRequestType(type);
			response.setStatus(adminBo.bulkUploadStudents(request));
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			response.setStatus(new EdoApiStatus(-111, "There was some error parsing your excel. Please make sure that all fields are text fields and not number fields"));
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
	
	@POST
	@Path("/getFeedbackData")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse getFeedbackData(EdoServiceRequest request) {
		LoggingUtil.logMessage("Feedback data Request :" + request);
		EdoServiceResponse response = adminBo.getFeedbackData(request);
		LoggingUtil.logObject("Feedback data Response", response);
		return response;
	}
	
	@POST
	@Path("/getQuestionFeedbacks")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse getQuestionFeedbacks(EdoServiceRequest request) {
		LoggingUtil.logMessage("QuestionFeedback data Request :" + request);
		EdoServiceResponse response = adminBo.getQuestionFeedbacks(request);
		LoggingUtil.logMessage("QuestionFeedback data Response");
		return response;
	}
	
	@POST
	@Path("/registerStudent")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse registerStudent(EdoServiceRequest request) {
		LoggingUtil.logMessage("Student info Request :" + request);
		EdoServiceResponse response = CommonUtils.initResponse();
		response.setStatus(adminBo.registerStudent(request));
		LoggingUtil.logMessage("Student info Response " + response.getStatus().getResponseText());
		return response;
	}
	
	@POST
	@Path("/fixQuestions")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse fixQuestions(EdoServiceRequest request) {
		LoggingUtil.logMessage("Fix questions Request :" + request);
		EdoServiceResponse response = CommonUtils.initResponse();
		adminBo.fixQuestions();
		LoggingUtil.logMessage("Fix questions Response " + response.getStatus().getResponseText());
		return response;
	}
	
	@POST
	@Path("/cropQuestionImage")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public EdoServiceResponse cropQuestionImage(@FormDataParam("data") InputStream fileData, @FormDataParam("data") FormDataContentDisposition customerDataDetails,
			@FormDataParam("testId") Integer testId, @FormDataParam("fileName") String fileName) {
		LoggingUtil.logMessage("Crop image request :" + testId + " for " + fileName);
		EdoServiceResponse response = CommonUtils.initResponse();
		try {
			EdoServiceRequest request = new EdoServiceRequest();
			EdoTest test = new EdoTest();
			test.setId(testId);
			request.setFilePath(fileName);
			request.setTest(test);
			adminBo.cropQuestionImage(request, fileData);
			LoggingUtil.logMessage("Crop image completed ..");
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
		}
		return response;
	}
	
	@POST
	@Path("/backup")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse fixQuestions(EdoAdminRequest request) {
		LoggingUtil.logMessage("Backup Request :" + request);
		EdoServiceResponse response = CommonUtils.initResponse();
		response.setStatus(adminBo.backupData(request));
		LoggingUtil.logMessage("Backup Response " + response.getStatus().getResponseText());
		return response;
	}
	
	@POST
	@Path("/uplink")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse uplink(EdoAdminRequest request) {
		LoggingUtil.logMessage("Uplink Request :" + request);
		EdoServiceResponse response = CommonUtils.initResponse();
		response.setStatus(adminBo.uplinkData(request));
		LoggingUtil.logMessage("Uplink Response " + response.getStatus().getResponseText());
		return response;
	}
	
	@POST
	@Path("/download")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoAdminRequest downloadData(EdoAdminRequest request) {
		LoggingUtil.logMessage("Download Request :" + request);
		return adminBo.downloadData(request);
	}
	
	@POST
	@Path("/downlink")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse downlink(EdoAdminRequest request) {
		LoggingUtil.logMessage("Downlink Request :" + request);
		EdoServiceResponse response = CommonUtils.initResponse();
		response.setStatus(adminBo.downlinkData(request));
		LoggingUtil.logMessage("Downlink Response " + response.getStatus().getResponseText());
		return response;
	}
	
	@POST
	@Path("/parsePdf")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public EdoServiceResponse parsePdf(@FormDataParam("data") InputStream fileData, @FormDataParam("data") FormDataContentDisposition fileDetails,
			@FormDataParam("testId") Integer testId, @FormDataParam("buffer") Integer buffer, 
			@FormDataParam("questionSuffix") String questionSuffix, @FormDataParam("questionPrefix") String questionPrefix,
			@FormDataParam("fromQuestion") Integer fromQuestion, @FormDataParam("toQuestion") Integer toQuestion,
			@FormDataParam("cropWidth") String cropWidth
			) {
		LoggingUtil.logMessage("Parse PDF request :" + testId + " for " + fileDetails.getFileName());
		EdoServiceResponse response = CommonUtils.initResponse();
		try {
			EdoAdminRequest request = new EdoAdminRequest();
			EdoTest test = new EdoTest();
			test.setId(testId);
			request.setTest(test);
			request.setBuffer(buffer);
			request.setQuestionPrefix(questionPrefix);
			request.setQuestionSuffix(questionSuffix);
			request.setFromQuestion(fromQuestion);
			request.setToQuestion(toQuestion);
			request.setCropWidth(cropWidth);
			response = adminBo.parsePdf(request, fileData);
			LoggingUtil.logMessage("Parsing PDF for " + testId + " completed ..");
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
		}
		return response;
	}
	
	@POST
	@Path("/loadParsedPdf")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse loadParsedPdf(EdoServiceRequest request) {
		LoggingUtil.logMessage("Load Parsed PDF request :" + request);
		EdoServiceResponse response = CommonUtils.initResponse();
		try {
			response =  adminBo.loadParsedQuestions(request);
		} catch (Exception e) {
			e.printStackTrace();
		}
		LoggingUtil.logMessage("Load Parsed PDF Response");
		return response;
	}
	
	@POST
	@Path("/saveParsedQuestionPaper")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse saveParsedQuestionPaper(EdoServiceRequest request) {
		LoggingUtil.logMessage("Save Parsed paper request :" + request);
		EdoServiceResponse response = CommonUtils.initResponse();
		try {
			response.setStatus(adminBo.saveParsedQuestions(request));
		} catch (Exception e) {
			e.printStackTrace();
		}
		LoggingUtil.logMessage("Save Parsed paper Response");
		return response;
	}
	
	@POST
	@Path("/createAdmin")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse createAdmin(EdoAdminRequest request) {
		LoggingUtil.logMessage("Create admin Request :" + request);
		//EdoServiceResponse response = CommonUtils.initResponse();
		return adminBo.createInstitute(request);
		//LoggingUtil.logMessage("Create admin response " + response.getStatus().getResponseText());
		//return response;
	}
	
	@POST
	@Path("/savePendingVideos")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse savePendingVideos(EdoServiceRequest request) {
		LoggingUtil.logMessage("Export videos :" + request);
		EdoServiceResponse response = CommonUtils.initResponse();
		response.setStatus(adminBo.savePendingVideos(request));
		return response;
	}
	
	@POST
	@Path("/upgradeClient")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse upgradeClient(EdoServiceRequest request) {
		LoggingUtil.logMessage("Upgrade client :" + request);
		EdoServiceResponse response = CommonUtils.initResponse();
		response.setStatus(adminBo.upgradeClient(request));
		return response;
	}
	
	@POST
	@Path("/updateClientSales")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse updateClientSales(EdoServiceRequest request) {
		LoggingUtil.logMessage("Update client sales :" + request);
		EdoServiceResponse response = CommonUtils.initResponse();
		response.setStatus(adminBo.updateClientSales(request));
		return response;
	}
}
