package com.rns.web.edo.service.dao;

import java.util.List;
import java.util.Map;

import com.rns.web.edo.service.domain.EDOInstitute;
import com.rns.web.edo.service.domain.EDOPackage;
import com.rns.web.edo.service.domain.EdoPaymentStatus;
import com.rns.web.edo.service.domain.EdoQuestion;
import com.rns.web.edo.service.domain.EdoServiceRequest;
import com.rns.web.edo.service.domain.EdoStudent;
import com.rns.web.edo.service.domain.EdoTest;
import com.rns.web.edo.service.domain.EdoTestQuestionMap;
import com.rns.web.edo.service.domain.EdoTestStudentMap;

public interface EdoTestsDao {

	//Fetch queries
	List<EdoQuestion> getTestResult(EdoStudent student);
	List<EdoQuestion> getExamQuestions(Integer value);
	EdoQuestion getQuestion(Integer value);
	List<EdoTestQuestionMap> getExam(Integer value);
	List<EdoTestQuestionMap> getExamResult(EdoServiceRequest request);
	EdoTestStudentMap getTestStatus(EdoTestStudentMap map); 
	EdoTestStudentMap getStudentActivePackage(EdoTestStudentMap map);
	EdoTest getTest(Integer id);
	EDOInstitute getInstituteById(Integer id);
	
	//Save queries
	Integer saveTestResult(EdoServiceRequest request);
	Integer saveTestStatus(EdoServiceRequest request);
	Integer updateTestStatus(EdoServiceRequest request);
	Integer updateTestResult(Map<String, Object> requestMap);
	//public List<EdoQuestion> getTestUnsolved(EdoStudent student);
	
	//Analysis
	List<EdoTest> getExamAnalysis(Integer value);
	List<EdoQuestion> getQuestionAnalysis(Integer value);
	List<EdoStudent> getStudentResults(Integer value);
	List<EdoTestStudentMap> getSubjectwiseScore(Integer value);
	
	//Student queries
	List<EDOPackage> getInstituePackages(Integer instituteId);
	Integer saveStudent(EdoStudent student);
	Integer createStudentPackage(EdoStudent student);
	Integer deleteExistingPackages(EdoStudent student);
	Integer updatePaymentId(EdoStudent student);
	Integer updatePayment(EdoPaymentStatus status);
	List<EdoStudent> getStudentByPhoneNumber(EdoStudent student);
	EDOPackage getTestPackage(Integer testId);
	EdoStudent getStudentById(Integer id);
	
	//Admin queries
	Integer saveQuestion(EdoQuestion question);
	Integer saveTestQuestion(EdoTest test);
	Integer updateSolution(EdoQuestion question);
	List<EdoStudent> getStudentByPayment(String paymentId);
	List<EdoStudent> getAllStudents(Integer id);
	
	
	
	
}
