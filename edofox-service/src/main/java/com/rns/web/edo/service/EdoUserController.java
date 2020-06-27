package com.rns.web.edo.service;

import java.io.InputStream;
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
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.rns.web.edo.service.bo.api.EdoFile;
import com.rns.web.edo.service.bo.api.EdoUserBo;
import com.rns.web.edo.service.domain.EDOInstitute;
import com.rns.web.edo.service.domain.EDOPackage;
import com.rns.web.edo.service.domain.EdoApiStatus;
import com.rns.web.edo.service.domain.EdoServiceRequest;
import com.rns.web.edo.service.domain.EdoServiceResponse;
import com.rns.web.edo.service.domain.EdoTest;
import com.rns.web.edo.service.domain.jpa.EdoClasswork;
import com.rns.web.edo.service.domain.jpa.EdoVideoLecture;
import com.rns.web.edo.service.util.CommonUtils;
import com.rns.web.edo.service.util.EdoConstants;
import com.rns.web.edo.service.util.EdoExcelUtil;
import com.rns.web.edo.service.util.EdoPropertyUtil;
import com.rns.web.edo.service.util.LoggingUtil;
import com.rns.web.edo.service.util.VideoTokenGenerator;
import com.rns.web.edo.service.util.VideoUtil;
import com.rns.web.edo.service.util.RtcTokenBuilder.Role;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

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
	
	@POST
	@Path("/getTest")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse getTest(EdoServiceRequest request) {
		LoggingUtil.logMessage("Get Test Request :" + request);
		EdoServiceResponse response = CommonUtils.initResponse();
		try {
			Integer studentId = null;
			if(request.getStudent() != null) {
				studentId = request.getStudent().getId();
			}
			response =  userBo.getTest(request.getTest().getId(), studentId);
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
	
	@POST
	@Path("/saveAnswer")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse saveAnswer(EdoServiceRequest request) {
		LoggingUtil.logMessage("Save Answer Request :" + request);
		EdoServiceResponse response = CommonUtils.initResponse();
		try {
			response.setStatus(userBo.saveAnswer(request));
		} catch (Exception e) {
			e.printStackTrace();
		}
		LoggingUtil.logMessage("Save Answer Response");
		return response;
	}
	
	@POST
	@Path("/getSolved")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse getSolved(EdoServiceRequest request) {
		LoggingUtil.logMessage("Get solved Request :" + request);
		EdoServiceResponse response = CommonUtils.initResponse();
		try {
			response = userBo.getSolved(request);
		} catch (Exception e) {
			e.printStackTrace();
		}
		LoggingUtil.logMessage("Get solved Response");
		return response;
	}
	
	@GET
	@Path("/document/{docId}")
	@Produces(MediaType.MULTIPART_FORM_DATA)
	//@Produces("image/png")
	public Response getDocument(@PathParam("docId") Integer documentId) {
		LoggingUtil.logMessage("Image request:" + documentId);
		try {
			EdoFile file = userBo.getDocument(documentId);
			if(file != null) {
				ResponseBuilder response = Response.ok(file.getContent());
				response.header("Content-Disposition", "filename=" + file.getFileName());
				if(file.getContentType() != null) {
					response.header("Content-Type", file.getContentType());
				}
				return response.build();
			}

		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
		}
		return null;
	}
	
	@GET
	@Path("/getVideo/{docId}")
	//@Produces(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public EdoFile getVideo(@PathParam("docId") Integer videoId) {
		LoggingUtil.logMessage("Video request:" + videoId);
		try {
			return userBo.getVideo(videoId);
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
		}
		return new EdoFile();
	}
	
	@GET
	@Path("/getTempImage/{testId}/{questionId}")
	//@Produces(MediaType.MULTIPART_FORM_DATA)
	@Produces("image/png")
	public Response geTempImage(@PathParam("questionId") Integer questionId, @PathParam("testId") Integer testId) {
		//LoggingUtil.logObject("Image request:", userId);
		try {
			EdoFile file = userBo.getQuestionImage(questionId, "TEMP", testId);
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
	
	@GET
	@Path("/getImage/{studentId}")
	//@Produces(MediaType.MULTIPART_FORM_DATA)
	@Produces("image/png")
	public Response getImage(@PathParam("studentId") Integer studentId) {
		//LoggingUtil.logObject("Image request:", userId);
		try {
			EdoFile file = userBo.getStudentImage(studentId);
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
	@Path("/getStudentPackages")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse getStudentPackages(EdoServiceRequest request) {
		LoggingUtil.logMessage("Get studentpackages Request :" + request);
		EdoServiceResponse response = userBo.getPackages(request.getStudent());
		LoggingUtil.logMessage("Get student packages Response");
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
		LoggingUtil.logMessage("Process payment Request :" + id + " : " + transactionId, LoggingUtil.paymentLogger);
		EdoApiStatus response = userBo.processPayment(id, transactionId, paymentId);
		String urlString = EdoPropertyUtil.getProperty(EdoPropertyUtil.HOST_NAME) + "payment.php?payment_id=" + id + "&status=";
		if(response == null || response.getStatusCode() != EdoConstants.STATUS_OK) {
			urlString = urlString + "Failed";
		} else {
			urlString = urlString + "Success";
		}
		URI url = null;
		try {
			url = new URI(urlString);
		} catch (URISyntaxException e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
		}
		return Response.temporaryRedirect(url).build();
	}
	//{amount=[200.00], fees=[3.80], purpose=[Edofox payment], shorturl=[], buyer_phone=[+919423040642], buyer_name=[ajinkyA chandrashekhaR kulkarnI], payment_request_id=[e919c33d3d0241a98f25b91e733406b4], mac=[c10296185b86a2180d4dc85e9b59ae78a01893c9], buyer=[ajinkyashiva@gmail.coM], payment_id=[MOJO549240353296233], currency=[INR], longurl=[https://test.instamojo.com/@contact_9994e/e919c33d3d0241a98f25b91e733406b4], status=[Credit]}
	@POST
	@Path("/paymentWebhook")
	// @Produces(MediaType.APPLICATION_JSON)
	public Response paymentWebhook(MultivaluedMap<String, String> formParams) {
		System.out.println(formParams);
		LoggingUtil.logMessage("Webhook payment Request :" + formParams);
		/*EdoApiStatus response = */userBo.processPayment(formParams.getFirst("id"), formParams.getFirst("transaction_id"), null);
		return Response.ok().build();
	}
	
	@POST
	@Path("/completePayment")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse completePayment(EdoServiceRequest request) {
		LoggingUtil.logMessage("Complete payment Request :" + request);
		EdoServiceResponse response = new EdoServiceResponse();
		response.setPaymentStatus(userBo.completePayment(request.getTest(), request.getStudent()));
		LoggingUtil.logMessage("Complete payment Response");
		return response;
	}
	
	@POST
	@Path("/getAllSubjects")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse getAllSubjects(EdoServiceRequest request) {
		LoggingUtil.logMessage("Get subjects Request :" + request);
		EdoServiceResponse response = userBo.getAllSubjects();
		LoggingUtil.logMessage("Get subjects Response");
		return response;
	}
	
	@POST
	@Path("/getNextQuestion")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse getNextQuestion(EdoServiceRequest request) {
		LoggingUtil.logMessage("Get next question Request :" + request);
		EdoServiceResponse response = userBo.getNextQuestion(request);
		LoggingUtil.logMessage("Get next question Response");
		return response;
	}
	
	@POST
	@Path("/submitAnswer")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse submitAnswer(EdoServiceRequest request) {
		LoggingUtil.logMessage("Submit question answer Request :" + request);
		EdoServiceResponse response = userBo.submitAnswer(request);
		LoggingUtil.logMessage("Submit question answer Response");
		return response;
	}
	
	@POST
	@Path("/raiseDoubt")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse raiseDoubt(EdoServiceRequest request) {
		LoggingUtil.logMessage("Doubt Request :" + request);
		EdoServiceResponse response = userBo.raiseDoubt(request);
		LoggingUtil.logMessage("Doubt Response");
		return response;
	}
	
	@GET
	@Path("/ping")
	@Produces(MediaType.APPLICATION_JSON)
	public EdoServiceResponse ping() {
		return new EdoServiceResponse();
	}
	
	@POST
	@Path("/getVideoToken")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse getToken(EdoServiceRequest request) {
		LoggingUtil.logMessage("Token Request :" + request);
		Role role = Role.Role_Attendee;
		if(StringUtils.equals(request.getRequestType(), "host")) {
			role = Role.Role_Publisher;
		} 
		String token = VideoTokenGenerator.generateToken(request.getStudent().getCurrentPackage().getName(), request.getStudent().getName(), role);
		LoggingUtil.logMessage("Token Response == " + token);
		EdoServiceResponse response = new EdoServiceResponse();
		response.setToken(token);
		return response;
	}
	
	@POST
	@Path("/getVideoTokenUid")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse getVideoTokenUid(EdoServiceRequest request) {
		LoggingUtil.logMessage("Token Request with Id :" + request);
		Role role = Role.Role_Attendee;
		if(StringUtils.equals(request.getRequestType(), "host")) {
			role = Role.Role_Publisher;
		} 
		String token = VideoTokenGenerator.generateToken(request.getStudent().getCurrentPackage().getName(), request.getStudent().getId(), role);
		LoggingUtil.logMessage("Token Response with id == " + token);
		EdoServiceResponse response = new EdoServiceResponse();
		response.setToken(token);
		return response;
	}
	
	@POST
	@Path("/uploadRecording")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public EdoServiceResponse uploadRecording(@FormDataParam("data") InputStream recordingData, @FormDataParam("data") FormDataContentDisposition recordingDataDetails,
			@FormDataParam("sessionId") Integer sessionId, @FormDataParam("channelId") Integer channelId) {
		LoggingUtil.logMessage("Upload recording :" + sessionId, LoggingUtil.videoLogger);
		EdoServiceResponse response = CommonUtils.initResponse();
		try {
			return userBo.uploadRecording(sessionId, recordingData, channelId);
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
		}
		LoggingUtil.logMessage("Upload recording Response", LoggingUtil.videoLogger);
		return response;
	}
	
	@POST
	@Path("/startLiveSession")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse startLiveSession(EdoServiceRequest request) {
		LoggingUtil.logMessage("Start live session request :" + request);
		EdoServiceResponse response = userBo.startLiveSession(request);
		LoggingUtil.logObject("Start live session response ", response);
		return response;
	}
	
	@POST
	@Path("/getLiveSessions")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse getLiveSessions(EdoServiceRequest request) {
		LoggingUtil.logMessage("Get live sessions request :" + request);
		EdoServiceResponse response = userBo.getLiveSessions(request);
		LoggingUtil.logObject("Get live session response ", response);
		return response;
	}
	
	@POST
	@Path("/finishRecording")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse finishRecording(EdoServiceRequest request) {
		LoggingUtil.logMessage("Finish recording request request :" + request);
		EdoServiceResponse response = userBo.finishRecording(request);
		LoggingUtil.logObject("Finish recording response ", response);
		return response;
	}
	
	@POST
	@Path("/getSession")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse getSession(EdoServiceRequest request) {
		LoggingUtil.logMessage("Get session request :" + request);
		EdoServiceResponse response = userBo.getSession(request);
		LoggingUtil.logObject("Get session response ", response);
		return response;
	}
	
	@POST
	@Path("/uploadContent")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public EdoServiceResponse uploadVideoLecture(@FormDataParam("data") InputStream videoData, @FormDataParam("data") FormDataContentDisposition videoDetails,
			@FormDataParam("info") String info, @FormDataParam("maps") String maps) {
		LoggingUtil.logMessage("Upload content request :" + info, LoggingUtil.videoLogger);
		EdoServiceResponse response = CommonUtils.initResponse();
		try {
			EdoClasswork classwork = new ObjectMapper().readValue(info, EdoClasswork.class);
			return userBo.uploadVideo(videoData, classwork, maps, videoDetails.getFileName());
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			response.setStatus(new EdoApiStatus(-111, EdoConstants.ERROR_IN_PROCESSING));
		}
		LoggingUtil.logMessage("Upload content Response", LoggingUtil.videoLogger);
		return response;
	}
	
	@POST
	@Path("/getVideoLectures")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse getVideoLectures(EdoServiceRequest request) {
		LoggingUtil.logMessage("Get video lectures request :" + request);
		EdoServiceResponse response = userBo.getVideoLectures(request);
		LoggingUtil.logObject("Get video lectures response ", response);
		return response;
	}
	
	@POST
	@Path("/login")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse login(EdoServiceRequest request) {
		LoggingUtil.logMessage("Login request :" + request);
		EdoServiceResponse response = userBo.login(request);
		LoggingUtil.logObject("Login response ", response);
		return response;
	}
	
	@POST
	@Path("/saveVideoActivity")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse saveVideoActivity(EdoServiceRequest request) {
		LoggingUtil.logMessage("Save video activity request :" + request);
		EdoServiceResponse response = new EdoServiceResponse();
		response.setStatus(userBo.updateStudentActivity(request));
		LoggingUtil.logObject("Save video activity response ", response);
		return response;
	}
	
	@POST
	@Path("/updateVideoLecture")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse updateVideoLecture(EdoServiceRequest request) {
		LoggingUtil.logMessage("Update video request :" + request);
		EdoServiceResponse response = new EdoServiceResponse();
		response.setStatus(userBo.updateVideoLecture(request));
		LoggingUtil.logObject("Update video response ", response);
		return response;
	}
	
	@GET
	@Path("/getTags/{instituteId}")
	//@Produces(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public EdoServiceResponse getTags(@PathParam("instituteId") Integer instituteId, @QueryParam("query") String query) {
		//LoggingUtil.logMessage("Tags request:" + instituteId  + " and query " + query);
		try {
			return userBo.getTags(instituteId, query);
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
		}
		return null;
	}
	
	@POST
	@Path("/sendMails")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse sendMails(EdoServiceRequest request) {
		LoggingUtil.logMessage("Send mail request :" + request);
		EdoServiceResponse response = new EdoServiceResponse();
		response.setStatus(userBo.sendEmail(request));
		LoggingUtil.logObject("Send mail response ", response);
		return response;
	}
	
	@POST
	@Path("/updateDeviceId")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse updateDeviceId(EdoServiceRequest request) {
		LoggingUtil.logMessage("Update device id request :" + request);
		EdoServiceResponse response = new EdoServiceResponse();
		response.setStatus(userBo.addDeviceId(request));
		LoggingUtil.logObject("Update device Id response ", response);
		return response;
	}
	
}
