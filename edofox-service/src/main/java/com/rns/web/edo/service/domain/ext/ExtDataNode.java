package com.rns.web.edo.service.domain.ext;

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ExtDataNode {

	private List<ExtDataQuestion> questions;

	public List<ExtDataQuestion> getQuestions() {
		return questions;
	}

	public void setQuestions(List<ExtDataQuestion> questions) {
		this.questions = questions;
	}
}
