package com.rns.web.edo.service.dao;

import java.util.List;
import java.util.Map;

import com.rns.web.edo.service.domain.EDOAdminAnalytics;
import com.rns.web.edo.service.domain.EDOInstitute;
import com.rns.web.edo.service.domain.EDOPackage;
import com.rns.web.edo.service.domain.EdoAdminRequest;
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
import com.rns.web.edo.service.domain.jpa.EdoAnswerFileEntity;
import com.rns.web.edo.service.domain.jpa.EdoVideoLecture;

public interface EdoTestsDao {

	//Fetch queries
	List<EdoQuestion> getTestResult(EdoStudent student);
	List<EdoQuestion> getExamQuestions(Integer value);
	EdoQuestion getQuestion(Integer value);
	List<EdoTestQuestionMap> getExam(Integer value);
	List<EdoTestQuestionMap> getExamResult(EdoServiceRequest request);
	List<EdoTestQuestionMap> getExamSolved(EdoServiceRequest request);
	List<EdoTestStudentMap> getTestStatus(EdoTestStudentMap map); 
	List<EdoTestStudentMap> getStudentActivePackage(EdoTestStudentMap map);
	EdoTest getTest(Integer id);
	EDOInstitute getInstituteById(Integer id);
	List<EdoTestStudentMap> getSubjectwiseScoreStudent(Map<String,Object> value);
	List<EdoVideoLectureMap> getVideoLectures(EdoServiceRequest request);
	List<EdoVideoLectureMap> getVideoLecture(Integer id);
	List<EdoSubject> getVideoSubjects(EdoServiceRequest request);
	
	//Save queries
	Integer saveTestResult(EdoServiceRequest request);
	Integer saveTestStatus(EdoServiceRequest request);
	Integer updateTestStatus(EdoServiceRequest request);
	Integer updateTestStatusEvaluation(EdoServiceRequest request);
	Integer updateTestResult(Map<String, Object> requestMap);
	Integer addTestResult(Map<String, Object> requestMap);
	//public List<EdoQuestion> getTestUnsolved(EdoStudent student);
	
	//Analysis
	List<EdoTest> getExamAnalysis(Integer value);
	List<EdoQuestion> getQuestionAnalysis(Integer value);
	List<EdoStudent> getStudentResults(Integer value);
	List<EdoStudent> getStudentResultsWithAbsent(Integer value);
	List<EdoTestStudentMap> getSubjectwiseScore(Integer value);
	List<EdoSubject> getTestSubjects(Integer testId);
	List<EdoQuestion> getQuestionCorrectness(Integer value);
	List<EdoTestStudentMap> getSubjectwisePerformanceStudent(Integer integer);
	List<EdoTest> getStudentPerformance(EdoStudent value);
	List<EdoTest> getStudentRank(EdoServiceRequest request);
	List<EdoTest> getTopperScore(Integer value);
	List<EdoQuestion> getQuestionwiseCounts(Integer value);
	List<EdoQuestion> getSubjectWiseAverage(EdoServiceRequest request);
	
	//Student queries
	List<EDOPackage> getInstituePackages(/*Integer instituteId*/EdoServiceRequest request);
	List<EDOPackage> getStudentPackages(Integer studentId);
	Integer saveStudent(EdoStudent student);
	Integer createStudentPackage(EdoStudent student);
	Integer deleteExistingPackages(EdoStudent student);
	Integer deleteStudentPackages(EdoStudent student);
	Integer updatePaymentId(EdoStudent student);
	Integer updatePayment(EdoPaymentStatus status);
	List<EdoStudent> getStudentByPhoneNumber(EdoStudent student);
	EDOPackage getTestPackage(Integer testId);
	EdoStudent getStudentById(Integer id);
	int updateStudent(EdoStudent student);
	void updateStudentToken(EdoStudent student);
	void deleteLogin(EdoStudent student);
	List<EDOPackage> getLiveSessions(EDOPackage pkg);
	List<EdoStudent> getStudentByRollNo(EdoStudent student);
	List<EdoStudent> getStudentLogin(EdoStudent student);
	void saveLogin(EdoStudent student);
	List<EdoVideoLectureMap> getTestVideoLectures(Integer testId);
	EdoStudent getDeeperRegistration(String applicationId);
	List<EdoTest> getStudentExams(EdoServiceRequest request);
	List<EdoFeedback> getStudentActivity(EdoServiceRequest request);
	void saveActivitySummary(EdoServiceRequest request);
	void updateActivitySummary(EdoServiceRequest request);
	void updateActivityWatchTime(EdoServiceRequest request);
	List<EdoSubject> getStudentSubjects(EdoServiceRequest request);
	EdoSubject getStudentChapters(EdoServiceRequest request);
	List<EdoVideoLectureMap> getChapterContent(EdoServiceRequest request);	
	List<EdoTest> getChapterExams(EdoServiceRequest request);
	void saveStudentTestActivity(EdoServiceRequest request);
	List<EdoSubject> getDlpContentSubject(Integer value);
	void updateProctorUrl(EdoStudent student);
	List<EdoTestQuestionMap> getStudentTestActivity(EdoServiceRequest request);
	
	//Admin queries
	Integer saveQuestion(EdoQuestion question);
	Integer saveTestQuestion(EdoTest test);
	Integer updateSolution(EdoQuestion question);
	List<EdoStudent> getStudentByPayment(String paymentId);
	List<EdoStudent> getAllStudents(Integer id);
	List<EdoStudent> getAllPackageStudents(EdoStudent student);
	Integer getLastQuestionNumber(Integer testId);
	Integer saveInstitute(EDOInstitute institute);
	void createAdminLogin(EDOInstitute institute);
	Integer createPackage(EDOPackage pkg);
	void updatePackage(EDOPackage pkg);
	Integer addTest(EdoTestStudentMap map);
	Integer isAdminLogin(EDOInstitute institute);
	void deductQuota(EDOInstitute institute);
	void addQuota(EDOInstitute institute);
	EDOPackage getPackage(Integer packageId);
	EDOInstitute getStudentStats(Integer instituteId);
	void upgradeInstitute(EDOInstitute institute);
	List<EdoStudent> getStudentDevicesForPackage(Integer packageId);
	List<EdoStudent> getStudentDevicesForVideo(EdoVideoLecture lec);
	List<EdoStudent> getStudentDevicesForExam(EdoTest exam);
	List<EdoStudent> getStudentDevicesForDoubt(EdoFeedback feedback);
	List<EdoStudent> getStudentContactsForDoubt(EdoFeedback feedback);
	List<EdoStudent> getStudentContactsForExam(EdoTest exam);
	List<EdoStudent> getStudentContactsForPackage(Integer packageId);
	List<EdoStudent> getStudentContactsForVideo(EdoVideoLecture classworkInfo);
	List<EdoTest> getExamsForDate(String value);
	List<EdoVideoLectureMap> getVideosForDate(EdoAdminRequest request);
	List<EdoStudent> getStudentsForPackages(String packageList);
	
	//Question queries
	List<EdoSubject> getAllSubjects(Integer instituteId);
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
	void updateDoubtFile(EdoFeedback feedback);
	void addResolution(EdoFeedback feedback);
	EdoFeedback getFeedback(Integer id);
	EdoQuestion getFeedbackDetails(EdoServiceRequest feedback);
	List<EdoQuestion> getFeedbackData(EdoServiceRequest request);
	List<EdoQuestion> getVideoFeedback(EdoServiceRequest request);
	List<EdoQuestion> getGeneralFeedback(EdoServiceRequest request);
	EdoQuestion getFeedbackSummary(EdoServiceRequest request);
	List<EdoSubject> getDoubtSubjects(EdoServiceRequest request);
	List<EdoTestStudentMap> getQuestionFeedbacks(EdoFeedback feedback);
	EDOPackage getLiveSession(Integer id);
	void saveVideoActiviy(EdoServiceRequest request);
	List<EdoQuestion> getQuestionBank(EdoQuestion request);
	List<EdoAnswerFileEntity> getAnswerFiles(EdoServiceRequest request);
	
	
	//Super admin
	List<EDOAdminAnalytics> getInstituteExamReport(EdoAdminRequest request);
	List<EDOAdminAnalytics> getInstituteDoubtsReport(EdoAdminRequest request);
	
}
