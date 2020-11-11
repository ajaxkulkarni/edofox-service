package com.rns.web.edo.service.domain;

public class EdoQuestionDetails {
	
	private String name_of_publication;//": "dddd", "
	private String page_number;//": "", "
	private String paragraph_number;//": "", "
	private String line_no;//": "", "
	private String status;//": "1", "
	private String short_explanation;//": "ddddd", "topics": "1"
	
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
