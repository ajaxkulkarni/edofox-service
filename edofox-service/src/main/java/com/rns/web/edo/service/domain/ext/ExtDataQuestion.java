package com.rns.web.edo.service.domain.ext;

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ExtDataQuestion {
	
	private String passage_image;//": "",
    private String question_image;//": "",
    private boolean hint_available;//": false,
    private String passage_footer;//": "",
    private String sequence_no;//": 261,
    private boolean is_bookmarked;//": false,
    //"question_lo_ids": [24344],
    private List<ExtDataQuestionChoice> choices;
    private String hint;//": "",
    private boolean can_ask_doubt;//": true,
    private String question;//
    private String assertion;//": "",
    private String passage;//": "",
    private String question_status;//": "published",
    private double solution_rating;//": 0,
    //"solution_links": [],
    private boolean already_attempted;//": false,
    private String solution_id;//": 1229417,
    private boolean disable_bookmark;//": false,
    private boolean correctly_answered;//": false,
    private String passage_header;//": "",
    private String solution_image;//": "",
    private String reason;//": "",
    private String hint_image;//": "",
    private boolean multiple_correct;//": false,
    private String question_style;//": "single correct",
    private Integer question_level;//": 3,
    private Integer level;//": 3,
    private boolean solution_available;//": true,
    private String solution;//
    private String question_linked_to_id;//": null,
    private boolean question_linked;//": false,
    private String question_id;//": 930991
    private List<ExtDataQuestion> passage_child_questions;
    private List<String> mx_l1;//"
    private List<String> mx_l2;//"
    private String subject_slug;
    
	public String getPassage_image() {
		return passage_image;
	}
	public void setPassage_image(String passage_image) {
		this.passage_image = passage_image;
	}
	public String getQuestion_image() {
		return question_image;
	}
	public void setQuestion_image(String question_image) {
		this.question_image = question_image;
	}
	public boolean isHint_available() {
		return hint_available;
	}
	public void setHint_available(boolean hint_available) {
		this.hint_available = hint_available;
	}
	public String getPassage_footer() {
		return passage_footer;
	}
	public void setPassage_footer(String passage_footer) {
		this.passage_footer = passage_footer;
	}
	public String getSequence_no() {
		return sequence_no;
	}
	public void setSequence_no(String sequence_no) {
		this.sequence_no = sequence_no;
	}
	public boolean isIs_bookmarked() {
		return is_bookmarked;
	}
	public void setIs_bookmarked(boolean is_bookmarked) {
		this.is_bookmarked = is_bookmarked;
	}
	public List<ExtDataQuestionChoice> getChoices() {
		return choices;
	}
	public void setChoices(List<ExtDataQuestionChoice> choices) {
		this.choices = choices;
	}
	public String getHint() {
		return hint;
	}
	public void setHint(String hint) {
		this.hint = hint;
	}
	public boolean isCan_ask_doubt() {
		return can_ask_doubt;
	}
	public void setCan_ask_doubt(boolean can_ask_doubt) {
		this.can_ask_doubt = can_ask_doubt;
	}
	public String getQuestion() {
		return question;
	}
	public void setQuestion(String question) {
		this.question = question;
	}
	public String getAssertion() {
		return assertion;
	}
	public void setAssertion(String assertion) {
		this.assertion = assertion;
	}
	public String getPassage() {
		return passage;
	}
	public void setPassage(String passage) {
		this.passage = passage;
	}
	public String getQuestion_status() {
		return question_status;
	}
	public void setQuestion_status(String question_status) {
		this.question_status = question_status;
	}
	public double getSolution_rating() {
		return solution_rating;
	}
	public void setSolution_rating(double solution_rating) {
		this.solution_rating = solution_rating;
	}
	public boolean isAlready_attempted() {
		return already_attempted;
	}
	public void setAlready_attempted(boolean already_attempted) {
		this.already_attempted = already_attempted;
	}
	public String getSolution_id() {
		return solution_id;
	}
	public void setSolution_id(String solution_id) {
		this.solution_id = solution_id;
	}
	public boolean isDisable_bookmark() {
		return disable_bookmark;
	}
	public void setDisable_bookmark(boolean disable_bookmark) {
		this.disable_bookmark = disable_bookmark;
	}
	public boolean isCorrectly_answered() {
		return correctly_answered;
	}
	public void setCorrectly_answered(boolean correctly_answered) {
		this.correctly_answered = correctly_answered;
	}
	public String getPassage_header() {
		return passage_header;
	}
	public void setPassage_header(String passage_header) {
		this.passage_header = passage_header;
	}
	public String getSolution_image() {
		return solution_image;
	}
	public void setSolution_image(String solution_image) {
		this.solution_image = solution_image;
	}
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
	public String getHint_image() {
		return hint_image;
	}
	public void setHint_image(String hint_image) {
		this.hint_image = hint_image;
	}
	public boolean isMultiple_correct() {
		return multiple_correct;
	}
	public void setMultiple_correct(boolean multiple_correct) {
		this.multiple_correct = multiple_correct;
	}
	public String getQuestion_style() {
		return question_style;
	}
	public void setQuestion_style(String question_style) {
		this.question_style = question_style;
	}
	public Integer getQuestion_level() {
		return question_level;
	}
	public void setQuestion_level(Integer question_level) {
		this.question_level = question_level;
	}
	public Integer getLevel() {
		return level;
	}
	public void setLevel(Integer level) {
		this.level = level;
	}
	public boolean isSolution_available() {
		return solution_available;
	}
	public void setSolution_available(boolean solution_available) {
		this.solution_available = solution_available;
	}
	public String getSolution() {
		return solution;
	}
	public void setSolution(String solution) {
		this.solution = solution;
	}
	public String getQuestion_linked_to_id() {
		return question_linked_to_id;
	}
	public void setQuestion_linked_to_id(String question_linked_to_id) {
		this.question_linked_to_id = question_linked_to_id;
	}
	public boolean isQuestion_linked() {
		return question_linked;
	}
	public void setQuestion_linked(boolean question_linked) {
		this.question_linked = question_linked;
	}
	public String getQuestion_id() {
		return question_id;
	}
	public void setQuestion_id(String question_id) {
		this.question_id = question_id;
	}
    
	@Override
	public String toString() {
		return "Question: " + question + "\nOption List" + choices + "\nQ Image:" + question_image + "\nType:" + question_style 
				+ "\nSolution:" + solution + "\nID:" + question_id;
	}
	public List<ExtDataQuestion> getPassage_child_questions() {
		return passage_child_questions;
	}
	public void setPassage_child_questions(List<ExtDataQuestion> passage_child_questions) {
		this.passage_child_questions = passage_child_questions;
	}
	public List<String> getMx_l1() {
		return mx_l1;
	}
	public void setMx_l1(List<String> mx_l1) {
		this.mx_l1 = mx_l1;
	}
	public List<String> getMx_l2() {
		return mx_l2;
	}
	public void setMx_l2(List<String> mx_l2) {
		this.mx_l2 = mx_l2;
	}
	public String getSubject_slug() {
		return subject_slug;
	}
	public void setSubject_slug(String subject_slug) {
		this.subject_slug = subject_slug;
	}
    

}
