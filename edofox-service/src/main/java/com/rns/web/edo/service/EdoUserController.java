package com.rns.web.edo.service;

import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.rns.web.edo.service.bo.api.EdoFile;
import com.rns.web.edo.service.bo.api.EdoUserBo;
import com.rns.web.edo.service.domain.EdoApiStatus;
import com.rns.web.edo.service.domain.EdoServiceRequest;
import com.rns.web.edo.service.domain.EdoServiceResponse;
import com.rns.web.edo.service.domain.EdoTest;
import com.rns.web.edo.service.util.CommonUtils;
import com.rns.web.edo.service.util.EdoConstants;
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
	
	@GET
	@Path("/getImage/{questionId}/{imageType}")
	//@Produces(MediaType.MULTIPART_FORM_DATA)
	@Produces("image/png")
	public Response getImage(@PathParam("questionId") Integer questionId, @PathParam("imageType") String imageType) {
		//LoggingUtil.logObject("Image request:", userId);
		try {
			EdoFile file = userBo.getQuestionImage(questionId, imageType);
			if(file != null) {
				ResponseBuilder response = Response.ok(file.getContent());
				//response.header("Content-Disposition", "filename=" + file.getFileName());
				return response.build();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@POST
	@Path("/getPackages")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse getPackages(EdoServiceRequest request) {
		LoggingUtil.logMessage("Get packages Request :" + request);
		EdoServiceResponse response = userBo.getPackages(request.getInstitute());
		LoggingUtil.logMessage("Get packages Response");
		return response;
	}
	
	@POST
	@Path("/registerStudentPackages")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse registerStudentPackages(EdoServiceRequest request) {
		LoggingUtil.logMessage("Register packages Request :" + request);
		EdoServiceResponse response = new EdoServiceResponse();
		response = userBo.registerStudent(request.getStudent());
		LoggingUtil.logMessage("Register packages Response");
		return response;
	}
	
	@GET
	@Path("/processPayment")
	//@Produces(MediaType.APPLICATION_JSON)
	public Response processPayment(@QueryParam("id") String id, @QueryParam("transaction_id") String transactionId, @QueryParam("payment_id") String paymentId) {
		LoggingUtil.logMessage("Process payment Request :" + id + " : " + transactionId);
		EdoApiStatus response = userBo.processPayment(id, transactionId, paymentId);
		String urlString = EdoConstants.UI_HOST + "success.html";
		if(response == null || response.getStatusCode() != EdoConstants.STATUS_OK) {
			urlString = EdoConstants.UI_HOST + "error.html";
		}
		URI url = null;
		try {
			url = new URI(urlString);
		} catch (URISyntaxException e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
		}
		return Response.temporaryRedirect(url).build();
	}
	
}
