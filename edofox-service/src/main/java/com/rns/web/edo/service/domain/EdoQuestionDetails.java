package com.rns.web.edo.service.domain;

public class EdoQuestionDetails {
	
	private String name_of_publication;//": "dddd", "
	private String page_number;//": "", "
	private String paragraph_number;//": "", "
	private String line_no;//": "", "
	private String status;//": "1", "
	private String short_explanation;//": "ddddd", "
	private String topics;//": "1"
	private Integer self_created;
	private Integer neet_no_of_ques;
	private Integer jee_no_of_ques;
	private Integer setter_id;
	private Integer board_id;

	
	public String getTopics() {
		return topics;
	}
	public void setTopics(String topics) {
		this.topics = topics;
	}
	public Integer getSelf_created() {
		return self_created;
	}
	public void setSelf_created(Integer self_created) {
		this.self_created = self_created;
	}
	public Integer getNeet_no_of_ques() {
		return neet_no_of_ques;
	}
	public void setNeet_no_of_ques(Integer neet_no_of_ques) {
		this.neet_no_of_ques = neet_no_of_ques;
	}
	public Integer getJee_no_of_ques() {
		return jee_no_of_ques;
	}
	public void setJee_no_of_ques(Integer jee_no_of_ques) {
		this.jee_no_of_ques = jee_no_of_ques;
	}
	public Integer getSetter_id() {
		return setter_id;
	}
	public void setSetter_id(Integer setter_id) {
		this.setter_id = setter_id;
	}
	public Integer getBoard_id() {
		return board_id;
	}
	public void setBoard_id(Integer board_id) {
		this.board_id = board_id;
	}
	public String getName_of_publication() {
		return name_of_publication;
	}
	public void setName_of_publication(String name_of_publication) {
		this.name_of_publication = name_of_publication;
	}
	public String getPage_number() {
		return page_number;
	}
	public void setPage_number(String page_number) {
		this.page_number = page_number;
	}
	public String getParagraph_number() {
		return paragraph_number;
	}
	public void setParagraph_number(String paragraph_number) {
		this.paragraph_number = paragraph_number;
	}
	public String getLine_no() {
		return line_no;
	}
	public void setLine_no(String line_no) {
		this.line_no = line_no;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getShort_explanation() {
		return short_explanation;
	}
	public void setShort_explanation(String short_explanation) {
		this.short_explanation = short_explanation;
	}
	
	

}