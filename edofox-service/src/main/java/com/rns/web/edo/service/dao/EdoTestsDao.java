package com.rns.web.edo.service.dao;

import java.util.List;
import java.util.Map;

import com.rns.web.edo.service.domain.EDOInstitute;
import com.rns.web.edo.service.domain.EDOPackage;
import com.rns.web.edo.service.domain.EdoFeedback;
import com.rns.web.edo.service.domain.EdoPaymentStatus;
import com.rns.web.edo.service.domain.EdoQuestion;
import com.rns.web.edo.service.domain.EdoServiceRequest;
import com.rns.web.edo.service.domain.EdoStudent;
import com.rns.web.edo.service.domain.EdoSubject;
import com.rns.web.edo.service.domain.EdoTest;
import com.rns.web.edo.service.domain.EdoTestQuestionMap;
import com.rns.web.edo.service.domain.EdoTestStudentMap;
import com.rns.web.edo.service.domain.EdoVideoLectureMap;

public interface EdoTestsDao {

	//Fetch queries
	List<EdoQuestion> getTestResult(EdoStudent student);
	List<EdoQuestion> getExamQuestions(Integer value);
	EdoQuestion getQuestion(Integer value);
	List<EdoTestQuestionMap> getExam(Integer value);
	List<EdoTestQuestionMap> getExamResult(EdoServiceRequest request);
	List<EdoTestStudentMap> getTestStatus(EdoTestStudentMap map); 
	List<EdoTestStudentMap> getStudentActivePackage(EdoTestStudentMap map);
	EdoTest getTest(Integer id);
	EDOInstitute getInstituteById(Integer id);
	List<EdoTestStudentMap> getSubjectwiseScoreStudent(Map<String,Object> value);
	List<EdoVideoLectureMap> getVideoLectures(EdoServiceRequest request);
	
	//Save queries
	Integer saveTestResult(EdoServiceRequest request);
	Integer saveTestStatus(EdoServiceRequest request);
	Integer updateTestStatus(EdoServiceRequest request);
	Integer updateTestResult(Map<String, Object> requestMap);
	Integer addTestResult(Map<String, Object> requestMap);
	//public List<EdoQuestion> getTestUnsolved(EdoStudent student);
	
	//Analysis
	List<EdoTest> getExamAnalysis(Integer value);
	List<EdoQuestion> getQuestionAnalysis(Integer value);
	List<EdoStudent> getStudentResults(Integer value);
	List<EdoTestStudentMap> getSubjectwiseScore(Integer value);
	List<EdoSubject> getTestSubjects(Integer testId);
	List<EdoQuestion> getQuestionCorrectness(Integer value);
	
	//Student queries
	List<EDOPackage> getInstituePackages(Integer instituteId);
	List<EDOPackage> getStudentPackages(Integer studentId);
	Integer saveStudent(EdoStudent student);
	Integer createStudentPackage(EdoStudent student);
	Integer deleteExistingPackages(EdoStudent student);
	Integer updatePaymentId(EdoStudent student);
	Integer updatePayment(EdoPaymentStatus status);
	List<EdoStudent> getStudentByPhoneNumber(EdoStudent student);
	EDOPackage getTestPackage(Integer testId);
	EdoStudent getStudentById(Integer id);
	int updateStudent(EdoStudent student);
	List<EDOPackage> getLiveSessions(EDOPackage pkg);
	List<EdoStudent> getStudentByRollNo(EdoStudent student);
	List<EdoStudent> getStudentLogin(EdoStudent student);
	void saveLogin(EdoStudent student);
	List<EdoVideoLectureMap> getTestVideoLectures(Integer testId);
	
	//Admin queries
	Integer saveQuestion(EdoQuestion question);
	Integer saveTestQuestion(EdoTest test);
	Integer updateSolution(EdoQuestion question);
	List<EdoStudent> getStudentByPayment(String paymentId);
	List<EdoStudent> getAllStudents(Integer id);
	Integer getLastQuestionNumber(Integer testId);
	Integer saveInstitute(EDOInstitute institute);
	void createAdminLogin(EDOInstitute institute);
	Integer createPackage(EDOPackage pkg);
	Integer addTest(EdoTestStudentMap map);
	Integer isAdminLogin(EDOInstitute institute);
	void deductQuota(EDOInstitute institute);
	void addQuota(EDOInstitute institute);
	EDOPackage getPackage(Integer packageId);
	EDOInstitute getStudentStats(Integer instituteId);
	void upgradeInstitute(EDOInstitute institute);
	
	//Question queries
	List<EdoSubject> getAllSubjects();
	int addQuestion(EdoQuestion question);
	int getNoOfQuestionsByChapter(Integer chapterId);
	int getNoOfQuestionsByDate(String date);
	List<EdoQuestion> getQuestionsByRefId(String refId);
	int updateQuestion(EdoQuestion question);
	void addQuestionsBatch(List<EdoQuestion> questions);
	List<EdoQuestion> getNextQuestion(EdoQuestion request);
	List<EdoQuestion> getFixableQuestions(Map<String,Object> map);
	int fixQuestions(List<EdoQuestion> questions);
	int fixQuestion(EdoQuestion question);
	void createExam(EdoServiceRequest request);
	List<EdoQuestion> getQuestionsByExam(EdoQuestion question);
	void addQuestionQuery(EdoTestStudentMap map);
	void addResolution(EdoFeedback feedback);
	List<EdoQuestion> getFeedbackData(EdoServiceRequest request);
	List<EdoQuestion> getVideoFeedback(EdoServiceRequest request);
	List<EdoTestStudentMap> getQuestionFeedbacks(EdoFeedback feedback);
	EDOPackage getLiveSession(Integer id);
	void saveVideoActiviy(EdoServiceRequest request);
	
	//ERP
	List<EdoStudent> getStudentDevices(Map<String,Object> value);
	
	
}
