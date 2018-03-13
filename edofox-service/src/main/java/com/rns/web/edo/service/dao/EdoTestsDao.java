package com.rns.web.edo.service.dao;

import java.util.List;

import com.rns.web.edo.service.domain.EDOPackage;
import com.rns.web.edo.service.domain.EdoPaymentStatus;
import com.rns.web.edo.service.domain.EdoQuestion;
import com.rns.web.edo.service.domain.EdoServiceRequest;
import com.rns.web.edo.service.domain.EdoStudent;
import com.rns.web.edo.service.domain.EdoTest;
import com.rns.web.edo.service.domain.EdoTestQuestionMap;
import com.rns.web.edo.service.domain.EdoTestStudentMap;

public interface EdoTestsDao {

	char[] getstu = null;
	//Fetch queries
	List<EdoQuestion> getTestResult(EdoStudent student);
	List<EdoQuestion> getExamQuestions(Integer value);
	EdoQuestion getQuestion(Integer value);
	List<EdoTestQuestionMap> getExam(Integer value);
	List<EdoTestQuestionMap> getExamResult(EdoServiceRequest request);
	EdoTestStudentMap getTestStatus(EdoTestStudentMap map); 
	
	//Save queries
	Integer saveTestResult(EdoServiceRequest request);
	Integer saveTestStatus(EdoServiceRequest request);
	//public List<EdoQuestion> getTestUnsolved(EdoStudent student);
	
	//Analysis
	EdoTest getExamAnalysis(Integer value);
	List<EdoQuestion> getQuestionAnalysis(Integer value);
	List<EdoStudent> getStudentResults(Integer value);
	
	//Student queries
	List<EDOPackage> getInstituePackages(Integer instituteId);
	Integer saveStudent(EdoStudent student);
	Integer createStudentPackage(EdoStudent student);
	Integer updatePaymentId(EdoStudent student);
	Integer updatePayment(EdoPaymentStatus status);
	List<EdoStudent> getStudentByPhoneNumber(EdoStudent student);
	
	//Admin queries
	Integer saveQuestion(EdoQuestion question);
	Integer saveTestQuestion(EdoTest test);
	
}
