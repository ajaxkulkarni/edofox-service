package com.rns.web.edo.service.domain;

public class EdoTestStudentMap {

	private EdoTest test;
	private EdoStudent student;
	private String status;
	private Integer mapId;
	
	public EdoTest getTest() {
		return test;
	}
	public void setTest(EdoTest test) {
		this.test = test;
	}
	public EdoStudent getStudent() {
		return student;
	}
	public void setStudent(EdoStudent student) {
		this.student = student;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Integer getMapId() {
		return mapId;
	}
	public void setMapId(Integer mapId) {
		this.mapId = mapId;
	}
	
	
	
}
