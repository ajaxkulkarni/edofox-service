package com.rns.web.edo.service.bo.domain;

import java.util.ArrayList;
import java.util.List;

import com.rns.web.edo.service.dao.domain.EdoQuestion;

public class EdoTest {

	private String id;
	private EdoQuestion currentQuestion;
	private List<EdoQuestion> test;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<EdoQuestion> getTest() {
		if(test == null) {
			test = new ArrayList<EdoQuestion>();
		}
		return test;
	}

	public void setTest(List<EdoQuestion> questions) {
		this.test = questions;
	}

	public EdoQuestion getCurrentQuestion() {
		return currentQuestion;
	}

	public void setCurrentQuestion(EdoQuestion currentQuestion) {
		this.currentQuestion = currentQuestion;
	}
	
}
