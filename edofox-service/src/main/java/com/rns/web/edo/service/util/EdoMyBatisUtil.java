package com.rns.web.edo.service.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;

import com.rns.web.edo.service.domain.EDOInstitute;
import com.rns.web.edo.service.domain.EdoChapter;
import com.rns.web.edo.service.domain.EdoQuestion;
import com.rns.web.edo.service.domain.EdoTest;
import com.rns.web.edo.service.domain.EdoTestQuestionMap;
import com.rns.web.edo.service.domain.jpa.EdoInstituteEntity;
import com.rns.web.edo.service.domain.jpa.EdoTestEntity;

public class EdoMyBatisUtil {
	
	/** 
	 
	  <resultMap id="testQuestionsMap"
		type="com.rns.web.edo.service.domain.EdoTestQuestionMap">
		<result column="question_id" property="question.qn_id" />
		<result column="question" property="question.question" />
		<result column="option1" property="question.option1" />
		<result column="option2" property="question.option2" />
		<result column="option3" property="question.option3" />
		<result column="option4" property="question.option4" />
		<result column="option5" property="question.option5" />
		<result column="subject" property="question.subject" />
		<result column="question_img_url" property="question.questionImageUrl" />
		<result column="option1_img_url" property="question.option1ImageUrl" />
		<result column="option2_img_url" property="question.option2ImageUrl" />
		<result column="option3_img_url" property="question.option3ImageUrl" />
		<result column="option4_img_url" property="question.option4ImageUrl" />
		<result column="question_type" property="question.type" />
		<result column="meta_data" property="question.metaData" />
		<result column="meta_data_img_url" property="question.metaDataImageUrl" />
		<result column="section" property="question.section" />
		<result column="question_number" property="question.questionNumber"/>
		<result column="test_id" property="test.id" />
		<result column="test_name" property="test.name" />
		<result column="no_of_questions" property="test.noOfQuestions" />
		<result column="total_marks" property="test.totalMarks" />
		<result column="duration" property="test.duration" />
		<result column="test_ui" property="test.testUi"/>
		<result column="start_date" property="test.startDate"/>
		<result column="end_date" property="test.endDate"/>
		<result column="random_questions" property="test.randomQuestions"/>
		<result column="time_constraint" property="test.timeConstraint"/>
		<result column="student_time_constraint" property="test.studentTimeConstraint"/>
		<result column="show_question_paper" property="test.showQuestionPaper"/>
		<result column="pause_timeout_seconds" property="test.pauseTimeout"/>
		<result column="max_allowed_test_starts" property="test.maxStarts"/>
		<result column="offline_conduction" property="test.offlineConduction"/>
		<result column="accept_location" property="test.acceptLocation"/>
		<result column="force_update" property="test.forceUpdate"/>
		<result column="random_pool" property="test.randomPool"/>
		<result column="institute_id" property="test.instituteId"/>
		<result column="custom_instructions" property="test.instructions"/>
		<result column="qStatus" property="question.status"/>
		<result column="weightage" property="question.weightage"/>
		<result column="negative_marks" property="question.negativeMarks"/>
		<result column="correct_answer" property="question.correctAnswer"/>
		<result column="alt_answer" property="question.alternateAnswer"/>
		<result column="partial" property="question.partialCorrection"/>
		<result column="solution" property="question.solution"/>
		<result column="solution_img_url" property="question.solutionImageUrl"/>
		<result column="chapter" property="question.chapter.chapterId" />
		<result column="chapter_name" property="question.chapter.chapterName" />
		<result column="level" property="question.level" />
	</resultMap>
	  
	 * **/
	
	public static List<EdoTestQuestionMap> convertExamMap(List<Map> records) {
		if(CollectionUtils.isEmpty(records)) {
			return null;
		}
		List<EdoTestQuestionMap> list = new ArrayList<EdoTestQuestionMap>();
		EdoTest test = null;
		for(Map record: records) {
			if(CollectionUtils.isNotEmpty(record.keySet())) {
				EdoTestQuestionMap map = new EdoTestQuestionMap();
				EdoQuestion question = new EdoQuestion();
				question.setQn_id(getInteger(record, "question_id"));
				question.setQuestion(getString(record, "question"));
				question.setOption1(getString(record, "option1"));
				question.setOption2(getString(record, "option2"));
				question.setOption3(getString(record, "option3"));
				question.setOption4(getString(record, "option4"));
				question.setSubject(getString(record, "subject"));
				question.setQuestionImageUrl(getString(record, "question_img_url"));
				question.setOption1ImageUrl(getString(record, "option1_img_url"));
				question.setOption2ImageUrl(getString(record, "option2_img_url"));
				question.setOption3ImageUrl(getString(record, "option3_img_url"));
				question.setOption4ImageUrl(getString(record, "option4_img_url"));
				question.setMetaDataImageUrl(getString(record, "meta_data_img_url"));
				question.setMetaData(getString(record, "meta_data"));
				question.setType(getString(record, "question_type"));
				question.setSection(getString(record, "section"));
				question.setQuestionNumber(getInteger(record, "question_number"));
				question.setStatus(getString(record, "qStatus"));
				question.setWeightage(getFloat(record, "weightage"));
				question.setNegativeMarks(getFloat(record, "negative_marks"));
				question.setCorrectAnswer(getString(record, "correct_answer"));
				question.setAlternateAnswer(getString(record, "alt_answer"));
				question.setPartialCorrection(getString(record, "partial"));
				question.setSolution(getString(record, "solution"));
				question.setSolutionImageUrl(getString(record, "solution_img_url"));
				question.setLevel(getInteger(record, "level"));
				EdoChapter chapter = new EdoChapter();
				chapter.setChapterId(getInteger(record, "chapter"));
				chapter.setChapterName(getString(record, "chapter_name"));
				question.setChapter(chapter);
				map.setQuestion(question);
				
				if(test == null) {
					test = new EdoTest();
				} else {
					map.setTest(test);
				}
				
				test.setId(getInteger(record, "test_id"));
				test.setName(getString(record, "test_name"));
				test.setNoOfQuestions(getInteger(record, "no_of_questions"));
				test.setTotalMarks(getInteger(record, "total_marks"));
				test.setDuration(getInteger(record, "duration"));
				test.setTestUi(getString(record, "test_ui"));
				test.setStartDate(getDate(record, "start_date", "yyyy-MM-dd HH:mm:ss"));
				test.setEndDate(getDate(record, "end_date", "yyyy-MM-dd HH:mm:ss"));
				test.setRandomQuestions(getString(record, "random_questions"));
				test.setTimeConstraint(getString(record, "time_constraint"));
				test.setStudentTimeConstraint(getString(record, "student_time_constraint"));
				test.setShowQuestionPaper(getString(record, "show_question_paper"));
				test.setMaxStarts(getInteger(record, "max_allowed_test_starts"));
				test.setOfflineConduction(getBoolean(record, "offline_conduction"));
				test.setAcceptLocation(getBoolean(record, "accept_location"));
				test.setForceUpdate(getBoolean(record, "force_update"));
				test.setRandomPool(getBoolean(record, "random_pool"));
				test.setInstituteId(getInteger(record, "institute_id"));
				test.setInstructions(getString(record, "custom_instructions"));
				map.setTest(test);
				
				list.add(map);
				
			}
		}
		return list;
	}
	
	/*
	 * test_questions_map.question_id,question,option1,option2,option3,option4,option5,test_subjects.subject, " +
					"test_questions.status as qStatus,question_img_url,option1_img_url,option2_img_url,option3_img_url,option4_img_url, " +
					"test_questions.question_type,meta_data,meta_data_img_url,test_questions_map.section,test_questions_map.question_number, " +
					"test_questions_map.weightage,test_questions_map.negative_marks,test_questions.correct_answer,alt_answer,partial,solution, " +
					"solution_img_url,test_questions.chapter,chapters.chapter_name,test_questions.level
	 * 
	 * */
	
	public static List<EdoTestQuestionMap> convertHibernateExamMap(List<Object[]> records) {
		if(CollectionUtils.isEmpty(records)) {
			return null;
		}
		List<EdoTestQuestionMap> list = new ArrayList<EdoTestQuestionMap>();
		//EdoTest test = null;
		for(Object[] row: records) {
			if(ArrayUtils.isEmpty(row)) {
				continue;
			}
			EdoTestQuestionMap map = new EdoTestQuestionMap();
			EdoQuestion question = new EdoQuestion();
			question.setQn_id(getInteger(row, 0));
			question.setQuestion(getString(row, 1));
			question.setOption1(getString(row, 2));
			question.setOption2(getString(row, 3));
			question.setOption3(getString(row, 4));
			question.setOption4(getString(row, 5));
			question.setOption5(getString(row, 6));
			question.setSubject(getString(row, 7));
			question.setStatus(getString(row, 8));
			question.setQuestionImageUrl(getString(row, 9));
			question.setOption1ImageUrl(getString(row, 10));
			question.setOption2ImageUrl(getString(row, 11));
			question.setOption3ImageUrl(getString(row, 12));
			question.setOption4ImageUrl(getString(row, 13));
			question.setType(getString(row, 14));
			question.setMetaData(getString(row, 15));
			question.setMetaDataImageUrl(getString(row, 16));
			question.setSection(getString(row, 17));
			question.setQuestionNumber(getInteger(row, 18));
			question.setWeightage(getFloat(row, 19));
			question.setNegativeMarks(getFloat(row, 20));
			question.setCorrectAnswer(getString(row, 21));
			question.setAlternateAnswer(getString(row, 22));
			question.setPartialCorrection(getString(row, 23));
			question.setSolution(getString(row, 24));
			question.setSolutionImageUrl(getString(row, 25));
			EdoChapter chapter = new EdoChapter();
			chapter.setChapterId(getInteger(row, 26));
			chapter.setChapterName(getString(row, 27));
			question.setChapter(chapter);
			question.setLevel(getInteger(row, 28));
			map.setQuestion(question);
			list.add(map);
		}
		return list;
	}

	private static Integer getInteger(Object[] row, int i) {
		if(i >= 0 && i < row.length) {
			return row[i] != null ? Integer.parseInt(row[i].toString()) : null;
		}
		return null;
	}
	
	private static String getString(Object[] row, int i) {
		if(i >= 0 && i < row.length) {
			return row[i] != null ? row[i].toString() : null;
		}
		return null;
	}
	
	private static Float getFloat(Object[] row, int i) {
		if(i >= 0 && i < row.length) {
			return row[i] != null ? Float.parseFloat(row[i].toString()) : null;
		}
		return null;
	}

	private static Integer getBoolean(Map record, String key) {
		if(key != null && record != null && record.containsKey(key)) {
			return record.get(key).toString().equals("true") ? 1 : 0;
		}
		return null;
	}

	private static Date getDate(Map record, String key, String format) {
		if(key != null && record != null && record.containsKey(key)) {
			return CommonUtils.parseDate(format, record.get(key).toString());
		}
		return null;
	}

	private static Integer getInteger(Map record, String key) {
		if(key != null && record != null && record.containsKey(key)) {
			return Integer.parseInt(record.get(key).toString());
		}
		return null;
	}
	
	private static String getString(Map record, String key) {
		if(key != null && record != null && record.containsKey(key)) {
			return record.get(key).toString();
		}
		return null;
	}
	
	private static Float getFloat(Map record, String key) {
		if(key != null && record != null && record.containsKey(key)) {
			return Float.parseFloat(record.get(key).toString());
		}
		return null;
	}

	public static EdoTest convertToTest(EdoTestEntity entity) {
		if(entity == null) {
			return null;
		}
		EdoTest test = new EdoTest();
		test.setId(entity.getId());
		test.setName(entity.getName());
		test.setAcceptLocation(entity.getAcceptLocation());
		test.setCreatedDate(entity.getCreatedDate());
		test.setDuration(entity.getDuration());
		test.setEndDate(entity.getEndDate());
		test.setForceUpdate(entity.getForceUpdate());
		test.setInstituteId(entity.getInstituteId());
		test.setInstructions(entity.getInstructions());
		test.setMaxStarts(entity.getMaxStarts());
		test.setNoOfQuestions(entity.getNoOfQuestions());
		test.setOfflineConduction(entity.getOfflineConduction());
		test.setPauseTimeout(entity.getPauseTimeout());
		test.setRandomPool(entity.getRandomPool());
		test.setRandomQuestions(entity.getRandomQuestions());
		test.setShowQuestionPaper(entity.getShowQuestionPaper());
		test.setShowResult(entity.getShowResult());
		test.setStartDate(entity.getStartDate());
		test.setTestUi(entity.getTestUi());
		test.setTimeConstraint(entity.getTimeConstraint());
		test.setStudentTimeConstraint(entity.getStudentTimeConstraint());
		test.setTotalMarks(entity.getTotalMarks());
		return test;
	}
	
	public static EDOInstitute convertToInstitute(EdoInstituteEntity entity) {
		if(entity == null) {
			return null;
		}
		EDOInstitute institute = new EDOInstitute();
		institute.setId(entity.getId());
		institute.setName(entity.getName());
		institute.setAppVersion(entity.getAppVersion());
		institute.setAppUrl(entity.getAppUrl());
		return institute;
	}
}