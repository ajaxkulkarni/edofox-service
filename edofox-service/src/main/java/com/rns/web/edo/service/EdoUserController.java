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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.rns.web.edo.service.bo.api.EdoFile;
import com.rns.web.edo.service.bo.api.EdoUserBo;
import com.rns.web.edo.service.domain.EDOInstitute;
import com.rns.web.edo.service.domain.EDOPackage;
import com.rns.web.edo.service.domain.EdoApiStatus;
import com.rns.web.edo.service.domain.EdoPaymentStatus;
import com.rns.web.edo.service.domain.EdoServiceRequest;
import com.rns.web.edo.service.domain.EdoServiceResponse;
import com.rns.web.edo.service.domain.EdoTest;
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
		//LoggingUtil.logMessage("Get Test result Request :" + request);
		EdoServiceResponse response = CommonUtils.initResponse();
		try {
			response =  userBo.getTestResult(request);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//LoggingUtil.logMessage("Get Test result Response");
		return response;
	}
	
	@POST
	@Path("/getTest")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse getTest(EdoServiceRequest request) {
		LoggingUtil.logMessage("Get Test Request :" + request, LoggingUtil.requestLogger);
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
		//LoggingUtil.logMessage("Get Test Response");
		return response;
	}
	
	@POST
	@Path("/saveTest")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse saveTest(EdoServiceRequest request) {
		LoggingUtil.logMessage("Save Test result Request :" + request, LoggingUtil.saveTestLogger);
		EdoServiceResponse response = CommonUtils.initResponse();
		try {
			response.setStatus(userBo.saveTest(request));
		} catch (Exception e) {
			e.printStackTrace();
		}
		//LoggingUtil.logMessage("Save Test result Response");
		return response;
	}
	
	@POST
	@Path("/saveAnswer")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse saveAnswer(EdoServiceRequest request) {
		LoggingUtil.logMessage("Save Answer Request :" + request, LoggingUtil.saveAnswerLogger);
		EdoServiceResponse response = CommonUtils.initResponse();
		try {
			response.setStatus(userBo.saveAnswer(request));
		} catch (Exception e) {
			e.printStackTrace();
		}
		//LoggingUtil.logMessage("Save Answer Response");
		return response;
	}
	
	@POST
	@Path("/getSolved")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse getSolved(EdoServiceRequest request) {
		//LoggingUtil.logMessage("Get solved Request :" + request);
		EdoServiceResponse response = CommonUtils.initResponse();
		try {
			response = userBo.getSolved(request);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//LoggingUtil.logMessage("Get solved Response");
		return response;
	}
	
	@GET
	@Path("/getImage/{questionId}/{imageType}")
	//@Produces(MediaType.MULTIPART_FORM_DATA)
	@Produces("image/png")
	public Response getImage(@PathParam("questionId") Integer questionId, @PathParam("imageType") String imageType) {
		//LoggingUtil.logObject("Image request:", userId);
		try {
			EdoFile file = userBo.getQuestionImage(questionId, imageType, null);
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
	
	@GET
	@Path("/getVideo/{docId}")
	//@Produces(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public EdoFile getVideo(@PathParam("docId") Integer videoId) {
		//LoggingUtil.logMessage("Video request:" + videoId);
		try {
			return userBo.getVideo(videoId);
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
		}
		return new EdoFile();
	}
	
	@POST
	@Path("/getPackages")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse getPackages(EdoServiceRequest request) {
		//LoggingUtil.logMessage("Get packages Request :" + request);
		EdoServiceResponse response = userBo.getPackages(request.getInstitute());
		//LoggingUtil.logMessage("Get packages Response");
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
		LoggingUtil.logMessage("Register student Request :" + request, LoggingUtil.paymentLogger);
		EdoServiceResponse response = new EdoServiceResponse();
		response = userBo.registerStudent(request.getStudent());
		//LoggingUtil.logMessage("Register packages Response");
		return response;
	}
	
	@GET
	@Path("/processPayment")
	//@Produces(MediaType.APPLICATION_JSON)
	public Response processPayment(@QueryParam("id") String id, @QueryParam("transaction_id") String transactionId, @QueryParam("payment_id") String paymentId) {
		LoggingUtil.logMessage("Process payment Request :" + id + " : " + transactionId, LoggingUtil.paymentLogger);
		EdoPaymentStatus response = userBo.processPayment(id, transactionId, paymentId, null);
		String urlString = EdoPropertyUtil.getProperty(EdoPropertyUtil.HOST_NAME) + "payment.php?payment_id=" + id + "&status=";
		if(response == null || response.getStatusCode() != EdoConstants.STATUS_OK) {
			urlString = urlString + "Failed";
		} else {
			urlString = urlString + "Success";
		}
		urlString = urlString + "&amount=" + response.getAmount();
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
		LoggingUtil.logMessage("Webhook payment Request :" + formParams, LoggingUtil.paymentLogger);
		/*EdoApiStatus response = */userBo.processPayment(formParams.getFirst("id"), formParams.getFirst("transaction_id"), null, null);
		return Response.ok().build();
	}
	
	@POST
	@Path("/completePayment")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse completePayment(EdoServiceRequest request) {
		LoggingUtil.logMessage("Complete payment Request :" + request, LoggingUtil.paymentLogger);
		EdoServiceResponse response = new EdoServiceResponse();
		response.setPaymentStatus(userBo.completePayment(request.getTest(), request.getStudent()));
		//LoggingUtil.logMessage("Complete payment Response");
		return response;
	}
	
	@POST
	@Path("/getAllSubjects")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse getAllSubjects(EdoServiceRequest request) {
		EdoServiceResponse response = userBo.getAllSubjects(request);
		return response;
	}
	
	@POST
	@Path("/getNextQuestion")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse getNextQuestion(EdoServiceRequest request) {
		//LoggingUtil.logMessage("Get next question Request :" + request);
		EdoServiceResponse response = userBo.getNextQuestion(request);
		return response;
	}
	
	@POST
	@Path("/submitAnswer")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse submitAnswer(EdoServiceRequest request) {
		//LoggingUtil.logMessage("Submit question answer Request :" + request);
		EdoServiceResponse response = userBo.submitAnswer(request);
		return response;
	}
	
	@POST
	@Path("/raiseDoubt")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse raiseDoubt(EdoServiceRequest request) {
		LoggingUtil.logMessage("Doubt Request :" + request, LoggingUtil.doubtsLogger);
		EdoServiceResponse response = userBo.raiseDoubt(request);
		//LoggingUtil.logMessage("Doubt Response");
		return response;
	}
	
	@GET
	@Path("/ping")
	@Produces(MediaType.APPLICATION_JSON)
	public EdoServiceResponse ping() {
		return new EdoServiceResponse();
	}
	
	@POST
	@Path("/systemTime")
	@Produces(MediaType.APPLICATION_JSON)
	public EdoServiceResponse systemTime() {
		return new EdoServiceResponse();
	}
	
	@POST
	@Path("/getVideoToken")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse getToken(EdoServiceRequest request) {
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
		EdoServiceResponse response = CommonUtils.initResponse();
		try {
			return userBo.uploadRecording(sessionId, recordingData, channelId);
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
		}
		return response;
	}
	
	@POST
	@Path("/startLiveSession")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse startLiveSession(EdoServiceRequest request) {
		//LoggingUtil.logMessage("Start live session request :" + request, LoggingUtil.videoLogger);
		EdoServiceResponse response = userBo.startLiveSession(request);
		return response;
	}
	
	@POST
	@Path("/joinSession")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse joinSession(EdoServiceRequest request) {
		//LoggingUtil.logMessage("Start live session request :" + request, LoggingUtil.videoLogger);
		EdoServiceResponse response = userBo.joinSession(request);
		return response;
	}
	
	@POST
	@Path("/getLiveSessions")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse getLiveSessions(EdoServiceRequest request) {
		//LoggingUtil.logMessage("Get live sessions request :" + request);
		EdoServiceResponse response = userBo.getLiveSessions(request);
		//LoggingUtil.logObject("Get live session response ", response);
		return response;
	}
	
	@POST
	@Path("/finishRecording")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse finishRecording(EdoServiceRequest request) {
		LoggingUtil.logMessage("Finish recording request request :" + request, LoggingUtil.requestLogger);
		EdoServiceResponse response = userBo.finishRecording(request);
		//LoggingUtil.logObject("Finish recording response ", response);
		return response;
	}
	
	@POST
	@Path("/getSession")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse getSession(EdoServiceRequest request) {
		//LoggingUtil.logMessage("Get session request :" + request);
		EdoServiceResponse response = userBo.getSession(request);
		return response;
	}
	
	@POST
	@Path("/uploadVideoLecture")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public EdoServiceResponse uploadVideoLecture(@FormDataParam("data") InputStream videoData, @FormDataParam("data") FormDataContentDisposition videoDetails,
			@FormDataParam("subjectId") Integer subjectId, @FormDataParam("instituteId") Integer instituteId, 
			@FormDataParam("title") String title, @FormDataParam("packageId") Integer packageId, @FormDataParam("topicId") Integer topicId, 
			@FormDataParam("keywords") String keywords, @FormDataParam("questionFile") InputStream questionFile, 
			@FormDataParam("questionFile") FormDataContentDisposition questionFileDetails, @FormDataParam("classrooms") String classrooms, @FormDataParam("type") String type) {
		LoggingUtil.logMessage("Upload video :" + title, LoggingUtil.videoLogger);
		EdoServiceResponse response = CommonUtils.initResponse();
		try {
			String questionFileName = "";
			if(questionFileDetails != null) {
				questionFileName = questionFileDetails.getFileName();
			}
			return userBo.uploadVideo(videoData, title, instituteId, subjectId, packageId, topicId, keywords, questionFile, questionFileName, classrooms, type);
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			response.setStatus(new EdoApiStatus(-111, EdoConstants.ERROR_IN_PROCESSING));
		}
		//LoggingUtil.logMessage("Upload video Response", LoggingUtil.videoLogger);
		return response;
	}
	
	@POST
	@Path("/getVideoLectures")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse getVideoLectures(EdoServiceRequest request) {
		//LoggingUtil.logMessage("Get video lectures request :" + request);
		EdoServiceResponse response = userBo.getVideoLectures(request);
		//LoggingUtil.logObject("Get video lectures response ", response);
		return response;
	}
	
	@POST
	@Path("/login")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse login(EdoServiceRequest request) {
		LoggingUtil.logMessage("Login request :" + request, LoggingUtil.requestLogger);
		EdoServiceResponse response = userBo.login(request);
		//LoggingUtil.logObject("Login response ", response);
		return response;
	}
	
	@POST
	@Path("/saveVideoActivity")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse saveVideoActivity(EdoServiceRequest request) {
		LoggingUtil.logMessage("Save video activity request :" + request, LoggingUtil.activityLogger);
		EdoServiceResponse response = new EdoServiceResponse();
		response.setStatus(userBo.updateStudentActivity(request));
		//LoggingUtil.logObject("Save video activity response ", response);
		return response;
	}
	
	@POST
	@Path("/saveTestActivity")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse saveTestActivity(EdoServiceRequest request) {
		//LoggingUtil.logMessage("Save Test activity request :" + request, LoggingUtil.activityLogger);
		EdoServiceResponse response = new EdoServiceResponse();
		response.setStatus(userBo.updateStudentTestActivity(request));
		//LoggingUtil.logObject("Save video activity response ", response);
		return response;
	}
	
	@POST
	@Path("/updateVideoLecture")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse updateVideoLecture(EdoServiceRequest request) {
		//LoggingUtil.logMessage("Update video request :" + request);
		EdoServiceResponse response = new EdoServiceResponse();
		response.setStatus(userBo.updateVideoLecture(request));
		//LoggingUtil.logObject("Update video response ", response);
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
	@Path("/searchForDeeperRegistration")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse searchForDeeperRegistration(EdoServiceRequest request) {
		LoggingUtil.logMessage("Deeper search request :" + request, LoggingUtil.requestLogger);
		return userBo.getDeeperRegistration(request.getStudent().getRollNo());
	}
	
	@POST
	@Path("/getStudentExams")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse getStudentExams(EdoServiceRequest request) {
		LoggingUtil.logMessage("Exams request :" + request, LoggingUtil.requestLogger);
		return userBo.getStudentExams(request);
	}
	
	@POST
	@Path("/getQuestionAnalysis")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse getQuestionAnalysis(EdoServiceRequest request) {
		return userBo.getQuestionAnalysis(request);
	}
	
	//{country=[], udf10=[], discount=[0.00], mode=[CC], 
	//cardhash=[This field is no longer supported in postback params.], 
	//error_Message=[No Error], state=[], bankcode=[MAST], txnid=[T27676], 
	//net_amount_debit=[1010.1], lastname=[], zipcode=[], additionalCharges=[10.1], 
	//phone=[4004004031], productinfo=[Deeper exams], 
	//hash=[863d2d7c43784034d981f0f5d7a6021d57184198981cf960403e0eb04bd0222eb10e81ced9cef95104be1c2a85109d71044dc16a8dc54a3df400dcd900b75264], 
	//status=[success], firstname=[user  demo], city=[], isConsentPayment=[0], error=[E000], 
	//addedon=[2020-08-24 23:34:20], udf9=[], udf7=[], udf8=[], 
	//encryptedPaymentId=[8943EF9206AEBFCD5DF1B0B315B4D7BE], bank_ref_num=[274790348051535], key=[005QTvo8], 
	//email=[demouser@Mail.com], amount=[1000.00], unmappedstatus=[captured], address2=[], payuMoneyId=[250461076], 
	//address1=[], udf5=[], mihpayid=[9083979633], udf6=[], udf3=[], udf4=[], udf1=[], udf2=[], giftCardIssued=[true], 
	//field1=[833640347203], cardnum=[512345XXXXXX2346], field7=[AUTHPOSITIVE], field6=[], field9=[], field8=[], amount_split=[{"PAYU":"1010.10"}], field3=[274790348051535], field2=[816063], field5=[02], PG_TYPE=[HDFCPG], field4=[VDZ0d1ZKV1ZaMlpXck5xdWl1WUk=], name_on_card=[Test]}

	
	@POST
	@Path("/payUSuccess")
	// @Produces(MediaType.APPLICATION_JSON)
	public Response payUSuccess(MultivaluedMap<String, String> formParams) {
		System.out.println(formParams);
		LoggingUtil.logMessage("PayU success payment :" + formParams, LoggingUtil.paymentLogger);
		EdoPaymentStatus response = userBo.processPayment(formParams.getFirst("txnid"), formParams.getFirst("txnid"), null, formParams.getFirst("status"));
		String urlString = EdoPropertyUtil.getProperty(EdoPropertyUtil.HOST_NAME) + "payment.php?payment_id=" + formParams.getFirst("txnid") + "&status=";
		if(response == null || response.getStatusCode() != EdoConstants.STATUS_OK) {
			urlString = urlString + "Failed";
		} else {
			urlString = urlString + "Success";
		}
		urlString = urlString + "&amount=" + formParams.getFirst("amount");
		URI url = null;
		try {
			url = new URI(urlString);
		} catch (URISyntaxException e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
		}
		return Response.temporaryRedirect(url).build();
	}
	
	@POST
	@Path("/payUFailure")
	// @Produces(MediaType.APPLICATION_JSON)
	public Response payUFailure(MultivaluedMap<String, String> formParams) {
		System.out.println(formParams);
		LoggingUtil.logMessage("PayU failed payment :" + formParams, LoggingUtil.paymentLogger);
		String urlString = EdoPropertyUtil.getProperty(EdoPropertyUtil.HOST_NAME) + "payment.php?payment_id=" + formParams.getFirst("txnid") + "&status=Failed";
		urlString = urlString + "&amount=" + formParams.getFirst("amount");
		URI url = null;
		try {
			url = new URI(urlString);
		} catch (URISyntaxException e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
		}
		return Response.temporaryRedirect(url).build();
	}
	
	@POST
	@Path("/updateDeviceId")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse updateDeviceId(EdoServiceRequest request) {
		//LoggingUtil.logMessage("Update device id request :" + request);
		EdoServiceResponse response = new EdoServiceResponse();
		response.setStatus(userBo.addDeviceId(request));
		//LoggingUtil.logObject("Update device Id response ", response);
		return response;
	}
	
	@POST
	@Path("/getStudentSubjects")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse getStudentSubjects(EdoServiceRequest request) {
		EdoServiceResponse response = new EdoServiceResponse();
		return userBo.getStudentSubjects(request);
	}
	
	@POST
	@Path("/getStudentChapters")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse getStudentChapters(EdoServiceRequest request) {
		//LoggingUtil.logMessage("Update device id request :" + request);
		return userBo.getStudentChapters(request);
	}
	

	@POST
	@Path("/getChapterContent")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse getChapterContent(EdoServiceRequest request) {
		//LoggingUtil.logMessage("Update device id request :" + request);
		return userBo.getChapterContent(request);
	}
	
	@POST
	@Path("/getChapterExams")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse getChapterExams(EdoServiceRequest request) {
		//LoggingUtil.logMessage("Update device id request :" + request);
		return userBo.getChapterExams(request);
	}
	
	@POST
	@Path("/getStudentPerformance")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse getStudentPerformance(EdoServiceRequest request) {
		//LoggingUtil.logMessage("Get Test result Request :" + request);
		EdoServiceResponse response = CommonUtils.initResponse();
		response =  userBo.getStudentPerformance(request);
		return response;
	}
	
	@POST
	@Path("/updateVideoProgress")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public EdoServiceResponse updateVideoProgress(EdoServiceRequest request) {
		LoggingUtil.logMessage("Update video progress :" + request, LoggingUtil.videoLogger);
		EdoServiceResponse response = CommonUtils.initResponse();
		//response =  userBo.getStudentPerformance(request);
		return response;
	}
}
