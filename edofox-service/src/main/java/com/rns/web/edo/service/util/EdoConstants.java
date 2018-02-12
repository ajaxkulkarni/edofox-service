package com.rns.web.edo.service.util;

public interface EdoConstants {

	String HOST_NAME = "http://172.104.47.180:8080/edofox/service/";
	
	String RESPONSE_OK = "OK";
	Integer STATUS_OK = 200;
	Integer STATUS_ERROR = -111;
	
	String ERROR_IN_PROCESSING = "Error in processing!";
	String ERROR_INCOMPLETE_REQUEST = "Insufficient fields!";
	String ERROR_TEST_ALREADY_SUBMITTED = "You already submitted this test ..";
	String ERROR_RESULT_NOT_FOUND = "Test result for this test was not found";
	
	String TEST_STATUS_COMPLETED = "COMPLETED";
	
	String ATTR_QUESTION = "question";
	String ATTR_OPTION4 = "option4";
	String ATTR_OPTION3 = "option3";
	String ATTR_OPTION2 = "option2";
	String ATTR_OPTION1 = "option1";
	
	//DEV
	String TESTS_JSON_PATH = "F:\\Resoneuronance\\Edofox\\Document\\tests";

}
