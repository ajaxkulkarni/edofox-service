package com.rns.web.edo.service.bo.impl;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.rns.web.edo.service.bo.api.EdoUserBo;
import com.rns.web.edo.service.bo.domain.EdoStudent;
import com.rns.web.edo.service.bo.domain.EdoTest;
import com.rns.web.edo.service.dao.EdoTestsDao;
import com.rns.web.edo.service.dao.domain.EdoQuestion;
import com.rns.web.edo.service.util.EdoConstants;

public class EdoUserBoImpl implements EdoUserBo, EdoConstants {

	private ThreadPoolTaskExecutor executor;
	private EdoTestsDao testsDao;
	private String filePath;

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public ThreadPoolTaskExecutor getExecutor() {
		return executor;
	}

	public void setExecutor(ThreadPoolTaskExecutor executor) {
		this.executor = executor;
	}

	public EdoTestsDao getTestsDao() {
		return testsDao;
	}

	public void setTestsDao(EdoTestsDao testsDao) {
		this.testsDao = testsDao;
	}

	public EdoStudent getTestResult(EdoStudent student) {
		if (student == null || student.getId() == null || student.getTest() == null || student.getTest().getId() == null) {
			return null;
		}
		// create ObjectMapper instance
		ObjectMapper mapper = new ObjectMapper();
		EdoTest result = null;
		// convert json string to object
		try {
			String fileName = "test_" + student.getTest().getId() + ".json";
			result = mapper.readValue(new File(filePath + "\\" + fileName), EdoTest.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (result == null || CollectionUtils.isEmpty(result.getTest())) {
			return student;
		}
		student.getTest().setTest(result.getTest());
		// Get solved questions
		// List<EdoQuestion> unsolved = testsDao.getTestUnsolved(student);
		for (EdoQuestion question : result.getTest()) {
			EdoQuestion currentQuestion = testsDao.getQuestion(question.getQn_id());
			if (currentQuestion != null) {
				question.setAnswer(currentQuestion.getAnswer());
				question.setChapter(currentQuestion.getChapter());
				student.getTest().setCurrentQuestion(currentQuestion);
				List<EdoQuestion> solved = testsDao.getTestResult(student);
				if (CollectionUtils.isNotEmpty(solved)) {
					question.setResponse(solved.get(0).getResponse());
					//System.out.println("Response:" + question.getResponse());
					question.setResult(solved.get(0).getResult());
					if(StringUtils.equals(StringUtils.trim(question.getResponse()), StringUtils.trim(question.getAnswer()))) {
						question.setCorrect(true);
					}
				}
			}
		}
		return student;
	}

}
