package com.rns.web.edo.service.domain;

public class EdoStudent {
	
	private Integer id;
	
	
	public EdoStudent() {
		
	}
	
	public EdoStudent(Integer studenId) {
		setId(studenId);
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}

}
