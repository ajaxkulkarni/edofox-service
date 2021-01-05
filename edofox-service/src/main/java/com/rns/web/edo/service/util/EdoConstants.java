package com.rns.web.edo.service.util;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public interface EdoConstants {

	//String HOST_NAME = "http://localhost:8080/edofox/service/";
	//String HOST_NAME = "http://localhost:8080/edo-service/service/"; //DEV
	
	//String UI_HOST = "http://localhost:59370/registration/"; //DEV
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
	String ERROR_INVALID_PROFILE = "Student profile was not valid. Please contact support.";
	String ERROR_NO_STUDENT = "Student profile does not exist. Please ask your administration to create one for you";
	String ERROR_INCOMPLETE_REQUEST = "Insufficient fields!";
	String ERROR_RESULT_NOT_FOUND = "Test result for this test was not found";
	
	String ERROR_TEST_ALREADY_SUBMITTED = "You already submitted this test ..";
	String ERROR_TEST_NOT_ACTIVE = "This test is no longer active";
	String ERROR_TEST_NOT_PAID = "You need to complete the payment in order to attempt this test ..";
	String ERROR_TEST_EXPIRED = "Sorry. This test is already expired ..";
	String ERROR_STUDENT_NOT_FOUND = "Student profile not found!";
	
	String COMMA_SEPARATOR = ", ";
	
	String TEST_STATUS_COMPLETED = "COMPLETED";
	String TEST_STATUS_STARTED = "STARTED";
	
	String STATUS_ACTIVE = "Active";
	String ACCESS_LEVEL_ADMIN = "Admin";
	String ATTR_QUESTION = "question";
	String ATTR_OPTION4 = "option4";
	String ATTR_OPTION3 = "option3";
	String ATTR_OPTION2 = "option2";
	String ATTR_OPTION1 = "option1";
	String ATTR_META_DATA = "meta";
	String ATTR_SOLUTION = "solution";
	String ATTR_VIDEO_QUESTION = "videoQuestion";
	String ATTR_DOUBT_IMAGE = "doubtImage";
	String ATTR_TEMP_QUESTION = "TEMPQ";
	String ATTR_TEMP_SOLUTION = "TEMPS";
	
	String MAIL_TYPE_SUBSCRIPTION = "SubscriptionMail";
	String MAIL_TYPE_ACTIVATED = "PackageActivated";
	String MAIL_TYPE_TEST_RESULT = "TestResult";
	String MAIL_TYPE_TEST_RESULT_RANK = "TestResultRank";
	String MAIL_TYPE_SIGN_UP = "signUpMail";
	String MAIL_TYPE_SIGN_UP_DEMO = "freeTrial";
	String MAIL_TYPE_UPGRADE = "upgrade";
	String MAIL_TYPE_NEW_CLASSWORK = "NewClasswork";
	String MAIL_TYPE_NEW_EXAM = "NewExam";
	String MAIL_TYPE_DOUBT_RESOLVED = "DoubtResolved";
	String MAIL_TYPE_INVITE = "StudentInvite";
	
	Integer JEE_NEW_FORMAT_BEST_OF_VALUE = 5;
	
	String QUESTION_TYPE_MULTIPLE = "MULTIPLE";
	String QUESTION_TYPE_MATCH = "MATCH";
	String QUESTION_TYPE_PASSAGE = "PASSAGE";
	String QUESTION_TYPE_NUMBER = "NUMBER";
	
	String REQUEST_FIREBASE_UPDATE = "firebaseUpdate";
	
	//DEV
	String TESTS_JSON_PATH = "F:\\Resoneuronance\\Edofox\\Document\\tests";
	String Q_BANK_PATH = "/home/service/questionData/";
	String TEMP_QUESTION_PATH = "/home/service/tempQuestions/";
	String TEST_QUESTION_PATH = "/home/service/testQuestionData/";
	String VIDEOS_PATH = "/home/service/videos/";
	String DOUBTS_PATH = "/home/service/doubts/";
	
	//Payment variables
	String PAYMENT_STATUS_COMPLETED = "completed";
	
	//VISION
	//String CLIENT_ID = "KfpmvuHq07V98YAaH3qROzmad6L6VLKtdHhPS430";
	//String CLIENT_SECRET = "lK2O6nmZoT9JozLbhND9qCDKPNMwBOnhrLPkcoylrVBolH0CJEYsBQpyjv2OzeyPvv4rh51AzUiXEh41FwW5tlZM2C7Z3xegib4oJclzR9bTMxYqeOK9pZQ7ypxvSSLb";

	//boolean ABSOLUTE_IMAGE_URLS = false;

	String JSON_PATH =  "/home/service/testData/";
	String[] ADMIN_NUMBERS = {"9423040642", "8830579708", "8668937047", "8669042562"};

	String VIDEO_QUESTION_FILE_PATH = "/home/service/videos/questions/";
	
	static Map<String, Integer> MAX_STUDENTS = Collections.unmodifiableMap(new HashMap<String, Integer>() {
		{
			put("Free", 25);
			put("Basic", 100);
			put("Pro", 250);
			put("Business", 500);
			put("Advanced", 1000);
		}
	});

	static Map<String, Double> MAX_STORAGE = Collections.unmodifiableMap(new HashMap<String, Double>() {
		{
			put("Free", 5d);
			put("Basic", 50d);
			put("Pro", 200d);
			put("Business", 500d);
			put("Advanced", 1024d);
		}
	});
	
	String VIDEO_BASE_URL = "https://streaming.edofox.com:8443/vod/{fileName}.smil/playlist.m3u8";
	
	//EDOFOX
	//String CLIENT_SECRET = "Ha4HAOtj2MMOOTqgqwAOjpLz1yulsGE6knDSnk8alulr6V2FkD63A3rBXn68LroZR8QWsLpsAXK6xJXTXXmQmHWu5plenh8FRvJjFt0Rn1wnSIgSWncRxeQy1vEMPaEF";
	//String CLIENT_ID = "VnK8OksaNBzM1qq6uBh1FP34bT6KbJtcp2tOGoT9";
	
}
