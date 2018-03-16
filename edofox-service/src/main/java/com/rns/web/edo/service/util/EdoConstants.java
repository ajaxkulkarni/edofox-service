package com.rns.web.edo.service.util;

public interface EdoConstants {

	//String HOST_NAME = "http://45.33.7.172:8080/edofox/service/";
	String HOST_NAME = "http://localhost:8080/edo-service/service/"; //DEV
	
	//String UI_HOST = "http://localhost:51050/registration/"; //DEV
	String UI_HOST = "http://www.visionlatur.com/mastertest/registration/";
	
	String RESPONSE_OK = "OK";
	Integer STATUS_OK = 200;
	Integer STATUS_ERROR = -111;
	
	Integer STATUS_TEST_SUBMITTED = -101;
	Integer STATUS_TEST_NOT_ACTIVE = -102;
	Integer STATUS_TEST_NOT_PAID = -103;
	Integer STATUS_TEST_EXPIRED = -104;
	Integer STATUS_TEST_NOT_OPENED = -105;
	
	String ERROR_IN_PROCESSING = "Error in processing!";
	String ERROR_INCOMPLETE_REQUEST = "Insufficient fields!";
	String ERROR_RESULT_NOT_FOUND = "Test result for this test was not found";
	
	String ERROR_TEST_ALREADY_SUBMITTED = "You already submitted this test ..";
	String ERROR_TEST_NOT_ACTIVE = "This test is no longer active";
	String ERROR_TEST_NOT_PAID = "You need to complete the payment in order to attempt this test ..";
	String ERROR_TEST_EXPIRED = "Sorry. This test is already expired ..";
	String ERROR_STUDENT_NOT_FOUND = "Student profile not found!";
	
	
	
	String TEST_STATUS_COMPLETED = "COMPLETED";
	String STATUS_ACTIVE = "Active";
	
	
	String ATTR_QUESTION = "question";
	String ATTR_OPTION4 = "option4";
	String ATTR_OPTION3 = "option3";
	String ATTR_OPTION2 = "option2";
	String ATTR_OPTION1 = "option1";
	
	//DEV
	String TESTS_JSON_PATH = "F:\\Resoneuronance\\Edofox\\Document\\tests";
	
	//Payment variables
	String PAYMENT_STATUS_COMPLETED = "completed";
	String AUTH_ENDPOINT = "https://www.instamojo.com/oauth2/token/";
	String API_ENDPOINT = "https://api.instamojo.com/v2/";
	String CLIENT_SECRET = "Ha4HAOtj2MMOOTqgqwAOjpLz1yulsGE6knDSnk8alulr6V2FkD63A3rBXn68LroZR8QWsLpsAXK6xJXTXXmQmHWu5plenh8FRvJjFt0Rn1wnSIgSWncRxeQy1vEMPaEF";
	String CLIENT_ID = "VnK8OksaNBzM1qq6uBh1FP34bT6KbJtcp2tOGoT9";


}
