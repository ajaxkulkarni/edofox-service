package com.rns.web.edo.service;

import java.io.InputStream;
import java.math.BigDecimal;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
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
import com.sun.jersey.multipart.FormDataBodyPart;
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
		//LoggingUtil.logMessage("Get Test Analysis Request :" + request);
		EdoServiceResponse response = CommonUtils.initResponse();
		try {
			response =  adminBo.getTestAnalysis(request.getTest());
		} catch (Exception e) {
			e.printStackTrace();
		}
		//LoggingUtil.logMessage("Get Test analysis Response");
		return response;
	}
	
	@POST
	@Path("/getTestResults")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse getTestResults(EdoServiceRequest request) {
		//LoggingUtil.logMessage("Get Test Results Request :" + request);
		EdoServiceResponse response = CommonUtils.initResponse();
		try {
			response =  adminBo.getTestResults(request);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//LoggingUtil.logMessage("Get Test Results Response");
		return response;
	}
	
	@POST
	@Path("/uploadTest")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse uploadTest(EdoServiceRequest request) {
		LoggingUtil.logMessage("Upload Test Request :" + request, LoggingUtil.requestLogger);
		EdoServiceResponse response = CommonUtils.initResponse();
		try {
			response.setStatus(adminBo.fileUploadTestQuestions(request.getFilePath(), request.getFirstQuestion(), request.getTest(), request.getSubjectId(), request.getSolutionPath()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		//LoggingUtil.logMessage("Upload Test Response");
		return response;
	}
	
	@POST
	@Path("/uploadSolution")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse uploadSolution(EdoServiceRequest request) {
		LoggingUtil.logMessage("Upload Solutions Request :" + request, LoggingUtil.requestLogger);
		EdoServiceResponse response = CommonUtils.initResponse();
		try {
			response.setStatus(adminBo.fileUploadTestSolutions(request.getFilePath(), request.getTest(), request.getSubjectId()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		//LoggingUtil.logMessage("Upload Solutions Response");
		return response;
	}
	
	@POST
	@Path("/revaluateResult")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse revaluateResult(EdoServiceRequest request) {
		LoggingUtil.logMessage("Revaluate Solutions Request :" + request, LoggingUtil.requestLogger);
		EdoServiceResponse response = CommonUtils.initResponse();
		try {
			response.setStatus(adminBo.revaluateResult(request));
		} catch (Exception e) {
			e.printStackTrace();
		}
		//LoggingUtil.logMessage("Revaluate Solutions Response");
		return response;
	}
	
	@POST
	@Path("/uploadStudents")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse uploadStudents(EdoServiceRequest request) {
		LoggingUtil.logMessage("Upload Students Request :" + request, LoggingUtil.requestLogger);
		EdoServiceResponse response = CommonUtils.initResponse();
		try {
			response.setStatus(adminBo.bulkUploadStudents(request));
		} catch (Exception e) {
			e.printStackTrace();
		}
		//LoggingUtil.logMessage("Upload Students Response");
		return response;
	}
	
	@POST
	@Path("/uploadStudentsExcel")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public EdoServiceResponse uploadStudentsExcel(@FormDataParam("data") InputStream studentData, @FormDataParam("data") FormDataContentDisposition customerDataDetails,
			@FormDataParam("instituteId") Integer instituteId, @FormDataParam("type") String type, @FormDataParam("packageId") Integer packageId) {
		LoggingUtil.logMessage("Upload Students Excel :" + instituteId, LoggingUtil.requestLogger);
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
		//LoggingUtil.logMessage("Upload Students Excel Response");
		return response;
	}
	
	@POST
	@Path("/getAllStudents")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse getAllStudents(EdoServiceRequest request) {
		EdoServiceResponse response = CommonUtils.initResponse();
		response = adminBo.getAllStudents(request);
		return response;
	}
	
	@POST
	@Path("/parseQuestion")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse parseQuestion(EdoServiceRequest request) {
		EdoServiceResponse response = CommonUtils.initResponse();
		response = adminBo.parseQuestion(request);
		return response;
	}
	
	@POST
	@Path("/getDataEntrySummary")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse getDataEntrySummary(EdoServiceRequest request) {
		EdoServiceResponse response = CommonUtils.initResponse();
		response = adminBo.getDataEntrySummary(request);
		return response;
	}
	
	@POST
	@Path("/autoCreateExam")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse createExam(EdoServiceRequest request) {
		LoggingUtil.logMessage("Create exam Request :" + request, LoggingUtil.requestLogger);
		EdoServiceResponse response = CommonUtils.initResponse();
		try {
			response = adminBo.automateTest(request);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//LoggingUtil.logMessage("Create exam Response");
		return response;
	}
	
	@POST
	@Path("/resolveDoubt")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse resolveDoubt(EdoServiceRequest request) {
		LoggingUtil.logMessage("Doubt resolve Request :" + request, LoggingUtil.doubtsLogger);
		EdoServiceResponse response = adminBo.addFeedbackResolution(request);
		//LoggingUtil.logMessage("Doubt resolve Response");
		return response;
	}
	
	@POST
	@Path("/getFeedbackData")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse getFeedbackData(EdoServiceRequest request) {
		//LoggingUtil.logMessage("Feedback data Request :" + request);
		EdoServiceResponse response = adminBo.getFeedbackData(request);
		//LoggingUtil.logObject("Feedback data Response", response);
		return response;
	}
	
	@POST
	@Path("/getFeedbackSummary")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse getFeedbackSummary(EdoServiceRequest request) {
		//LoggingUtil.logMessage("Feedback data Request :" + request);
		EdoServiceResponse response = adminBo.getFeedbackSummary(request);
		//LoggingUtil.logObject("Feedback data Response", response);
		return response;
	}
	
	@POST
	@Path("/getQuestionFeedbacks")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse getQuestionFeedbacks(EdoServiceRequest request) {
		//LoggingUtil.logMessage("QuestionFeedback data Request :" + request);
		EdoServiceResponse response = adminBo.getQuestionFeedbacks(request);
		//LoggingUtil.logMessage("QuestionFeedback data Response");
		return response;
	}
	
	@POST
	@Path("/registerStudent")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse registerStudent(EdoServiceRequest request) {
		LoggingUtil.logMessage("Student info Request :" + request, LoggingUtil.requestLogger);
		EdoServiceResponse response = CommonUtils.initResponse();
		response.setStatus(adminBo.registerStudent(request));
		LoggingUtil.logMessage("Student info Response " + response.getStatus().getResponseText(), LoggingUtil.requestLogger);
		return response;
	}
	
	@POST
	@Path("/fixQuestions")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse fixQuestions(EdoServiceRequest request) {
		EdoServiceResponse response = CommonUtils.initResponse();
		adminBo.fixQuestions();
		return response;
	}
	
	@POST
	@Path("/cropQuestionImage")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public EdoServiceResponse cropQuestionImage(@FormDataParam("data") InputStream fileData, @FormDataParam("data") FormDataContentDisposition customerDataDetails,
			@FormDataParam("testId") Integer testId, @FormDataParam("fileName") String fileName) {
		//LoggingUtil.logMessage("Crop image request :" + testId + " for " + fileName);
		EdoServiceResponse response = CommonUtils.initResponse();
		try {
			EdoServiceRequest request = new EdoServiceRequest();
			EdoTest test = new EdoTest();
			test.setId(testId);
			request.setFilePath(fileName);
			request.setTest(test);
			adminBo.cropQuestionImage(request, fileData);
			//LoggingUtil.logMessage("Crop image completed ..");
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
		EdoServiceResponse response = CommonUtils.initResponse();
		response.setStatus(adminBo.backupData(request));
		return response;
	}
	
	@POST
	@Path("/uplink")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse uplink(EdoAdminRequest request) {
		EdoServiceResponse response = CommonUtils.initResponse();
		response.setStatus(adminBo.uplinkData(request));
		return response;
	}
	
	@POST
	@Path("/download")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoAdminRequest downloadData(EdoAdminRequest request) {
		return adminBo.downloadData(request);
	}
	
	@POST
	@Path("/downlink")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse downlink(EdoAdminRequest request) {
		EdoServiceResponse response = CommonUtils.initResponse();
		response.setStatus(adminBo.downlinkData(request));
		return response;
	}
	
	@POST
	@Path("/parsePdf")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public EdoServiceResponse parsePdf(@FormDataParam("data") InputStream fileData, @FormDataParam("data") FormDataContentDisposition fileDetails,
			@FormDataParam("testId") Integer testId, @FormDataParam("buffer") Integer buffer, 
			@FormDataParam("questionSuffix") String questionSuffix, @FormDataParam("questionPrefix") String questionPrefix,
			@FormDataParam("fromQuestion") Integer fromQuestion, @FormDataParam("toQuestion") Integer toQuestion
			) {
		LoggingUtil.logMessage("Parse PDF request :" + testId + " for " + fileDetails.getFileName(), LoggingUtil.requestLogger);
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
			response = adminBo.parsePdf(request, fileData);
			LoggingUtil.logMessage("Parsing PDF for " + testId + " completed ..", LoggingUtil.requestLogger);
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
		EdoServiceResponse response = CommonUtils.initResponse();
		try {
			response =  adminBo.loadParsedQuestions(request);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}
	
	@POST
	@Path("/saveParsedQuestionPaper")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse saveParsedQuestionPaper(EdoServiceRequest request) {
		LoggingUtil.logMessage("Save Parsed paper request :" + request, LoggingUtil.requestLogger);
		EdoServiceResponse response = CommonUtils.initResponse();
		try {
			response.setStatus(adminBo.saveParsedQuestions(request));
		} catch (Exception e) {
			e.printStackTrace();
		}
		//LoggingUtil.logMessage("Save Parsed paper Response");
		return response;
	}
	
	@POST
	@Path("/createAdmin")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse createAdmin(EdoAdminRequest request) {
		LoggingUtil.logMessage("Create admin Request :" + request, LoggingUtil.requestLogger);
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
		//LoggingUtil.logMessage("Export videos :" + request);
		EdoServiceResponse response = CommonUtils.initResponse();
		response.setStatus(adminBo.savePendingVideos(request));
		return response;
	}
	
	@POST
	@Path("/upgradeClient")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse upgradeClient(EdoServiceRequest request) {
		//LoggingUtil.logMessage("Upgrade client :" + request);
		EdoServiceResponse response = CommonUtils.initResponse();
		response.setStatus(adminBo.upgradeClient(request));
		return response;
	}
	
	@POST
	@Path("/updateClientSales")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse updateClientSales(EdoServiceRequest request) {
		//LoggingUtil.logMessage("Update client sales :" + request);
		EdoServiceResponse response = CommonUtils.initResponse();
		response.setStatus(adminBo.updateClientSales(request));
		return response;
	}
	
	@POST
	@Path("/loadQuestionBank")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse loadQuestionBank(EdoServiceRequest request) {
		//LoggingUtil.logMessage("Load question bank :" + request);
		return adminBo.loadQuestionBank(request);
	}
	
	@POST
	@Path("/fixRecordedFile")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse fixRecordedFile(EdoServiceRequest request) {
		//LoggingUtil.logMessage("Load question bank :" + request);
		return adminBo.fixRecordedFile(request);
	}
	
	@POST
	@Path("/fixRecordedSessions")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse fixRecordedSessions(EdoServiceRequest request) {
		LoggingUtil.logMessage("Fix recorded sessions :" + request, LoggingUtil.videoLogger);
		EdoServiceResponse response = new EdoServiceResponse(adminBo.fixRecordedLectures(request));
		return response;
	}
	
	@POST
	@Path("/getLiveAnalysis")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse getLiveAnalysis(EdoServiceRequest request) {
		//LoggingUtil.logMessage("Get Test Analysis Request :" + request);
		EdoServiceResponse response = CommonUtils.initResponse();
		response =  adminBo.getLiveAnalysis(request);
		return response;
	}
	
	@POST
	@Path("/sendNotification")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse sendNotification(EdoServiceRequest request) {
		EdoServiceResponse response = CommonUtils.initResponse();
		response.setStatus(adminBo.sendNotification(request));
		return response;
	}
	

	@POST
	@Path("/savePackage")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse savePackage(EdoServiceRequest request) {
		return adminBo.savePackage(request);
	}
	
	@POST
	@Path("/updateEdofoxTokens")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse updateEdofoxTokens(EdoServiceRequest request) {
		return adminBo.updateEdofoxTokens(request);
	}
	
	@Path("/uploadEvaluation")
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public EdoServiceResponse uploadEvaluation(@FormDataParam("file") FormDataBodyPart bodyParts,
			@FormDataParam("file") FormDataContentDisposition fileDispositions,
			@FormDataParam("answerId") Integer answerId, @FormDataParam("marks") BigDecimal marks, @FormDataParam("evaluator") Integer evaluator) {

		EdoServiceResponse response = new EdoServiceResponse();
		response.setStatus(adminBo.uploadEvaluation(bodyParts, answerId, marks, evaluator, null, null));
		return response;
	}
	
	@POST
	@Path("/updateEvaluation")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse updateEvaluation(EdoServiceRequest request) {
		EdoServiceResponse response = new EdoServiceResponse();
		if(request.getQuestion() != null) {
			Integer answerId = request.getQuestion().getId();
			BigDecimal marks = request.getQuestion().getMarks();
			Integer evaluator = null;
			if(request.getStudent() != null && request.getStudent().getId() != null) {
				evaluator = request.getStudent().getId();
			}
			BigDecimal questionMarks = null; //Question marks to student
			if(request.getQuestion().getWeightage() != null) {
				questionMarks = new BigDecimal(request.getQuestion().getWeightage());
			}
			BigDecimal testMarks = null;
			if(request.getTest() != null) {
				testMarks = request.getTest().getScore();
			}
			
			response.setStatus(adminBo.uploadEvaluation(null, answerId, marks, evaluator, questionMarks, testMarks));
		}
		return response;
	}
	
	
	@POST
	@Path("/updateScore")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse updateScore(EdoServiceRequest request) {
		EdoServiceResponse response = CommonUtils.initResponse();
		response.setStatus(adminBo.updateStudentScore(request));
		return response;
	}
	
	@POST
	@Path("/calculateProctoringScore")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse calculateProctoringScore(EdoServiceRequest request) {
		EdoServiceResponse response = CommonUtils.initResponse();
		response.setStatus(adminBo.calculateProctoringScore(request));
		return response;
	}
	
	@GET
	@Path("/clearGarbage")
	public String clearGarbage() {
		System.gc();
		return "Done";
	}
	
	@POST
	@Path("/getVideoProctoringRecords")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse getVideoProctoringRecords(EdoServiceRequest request) {
		return adminBo.getProctoringVideo(request);
	}
	
	/*
{"signature":{"token":"91b3050780c8ee0528c01ce6cc346055f3d3667cd6ec5ca82f","timestamp":"1623307643",
"signature":"172bfaf08c1f21e41db21fbf23d622467fb47fc4c40d48c326c88dcfc826698f"},
"event-data":{"tags": [], "timestamp": 1623307639.353644, 
"storage": {"url": "https://sw.api.mailgun.net/v3/domains/mg.edofox.com/messages/AgEFx6alaGzzS7dwhLZHdqNCpMxw4aXmZA==", 
"key": "AgEFx6alaGzzS7dwhLZHdqNCpMxw4aXmZA=="}, "recipient-domain": "gmail.com", "event": "delivered", 
"campaigns": [], "user-variables": {"my_message_id": 123}, 

"flags": {"is-routed": false, "is-authenticated": true, "is-system-test": false, "is-test-mode": false}, "log-level": "info", "envelope": {"transport": "smtp", "sender": "postmaster@mg.edofox.com", "sending-ip": "209.61.151.228", "targets": "ajinkyashiva@gmail.com"}, "message": {"headers": {"to": "ajinkyashiva@gmail.com", "message-id": "1448209846.11623307649042.JavaMail.Admin@LAPTOP-3QT1KVRC", "from": "Edofox <postmaster@mg.edofox.com>", "subject": "Today's exam TESTING SMS"}, "attachments": [], "size": 14178}, "recipient": "ajinkyashiva@gmail.com", "id": "2HOVQ7URT3WCokw2xqGuoA", "delivery-status": {"tls": true, "mx-host": "gmail-smtp-in.l.google.com", "attempt-no": 1, "description": "", "session-seconds": 0.5016169548034668, "utf8": true, "code": 250, "message": "OK", "certificate-verified": true}}}

	 */
	
	@POST
	@Path("/emailWebHook")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoApiStatus emailWebHook(String request) {
		
		LoggingUtil.logMessage("Email Web hook called .... " + request, LoggingUtil.emailLogger);
		return adminBo.updateEmailStatus(request, "email");
	}
	
	/*
data: [{"senderId":"mtrsft","requestId":"31666d745341353534373539","report":[{"date":"2021-06-13 20:45:29","number":"919623736773","status":"2","desc":"FAILED"}],"userId":"193344","campaignName":"API"}]
	 * 
	 */
	
	@POST
	@Path("/smsWebHook")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoApiStatus smsWebHook(String request) {
		LoggingUtil.logMessage("SMS Web hook called .... " + request, LoggingUtil.emailLogger);
		return adminBo.updateEmailStatus(request, "sms");
	}
	
	@POST
	@Path("/getSubjectTestAnalysis")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse getSubjectTestAnalysis(EdoServiceRequest request) {
		return adminBo.getSubjectAnalysis(request);
	}
}
