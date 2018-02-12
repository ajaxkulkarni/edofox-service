package com.rns.web.edo.service.dao;

import java.util.List;

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
	
	//Save queries
	Integer saveTestResult(EdoServiceRequest request);
	Integer saveTestStatus(EdoServiceRequest request);
	//public List<EdoQuestion> getTestUnsolved(EdoStudent student);
	
	//Analysis
	EdoTest getExamAnalysis(Integer value);
	List<EdoQuestion> getQuestionAnalysis(Integer value);
	
}
