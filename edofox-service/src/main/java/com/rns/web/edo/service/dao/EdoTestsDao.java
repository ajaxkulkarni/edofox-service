package com.rns.web.edo.service.dao;

import java.util.List;

import com.rns.web.edo.service.bo.domain.EdoStudent;
import com.rns.web.edo.service.dao.domain.EdoQuestion;

public interface EdoTestsDao {

	public List<EdoQuestion> getTestResult(EdoStudent student);
	public EdoQuestion getQuestion(Integer value);
	//public List<EdoQuestion> getTestUnsolved(EdoStudent student);
	
}
