package com.rns.web.edo.service.domain.jpa;

import static javax.persistence.GenerationType.IDENTITY;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicUpdate;

@Entity
@Table(name = "uplink_status")
@DynamicUpdate
public class EdoUplinkStatus {
	
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true)
	private Integer id;
	@Column(name = "host_location", unique = true)
	private String hostLocation;
	@Column(name = "created_date", unique = true)
	private Date createdDate;
	@Column(name = "questions_updated", unique = true)
	private Integer questionsUpdated;
	@Column(name = "students_updated", unique = true)
	private Integer studentsUpdated;
	
	public Date getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getHostLocation() {
		return hostLocation;
	}
	public void setHostLocation(String hostLocation) {
		this.hostLocation = hostLocation;
	}
	public Integer getQuestionsUpdated() {
		return questionsUpdated;
	}
	public void setQuestionsUpdated(Integer questionsUpdated) {
		this.questionsUpdated = questionsUpdated;
	}
	public Integer getStudentsUpdated() {
		return studentsUpdated;
	}
	public void setStudentsUpdated(Integer studentsUpdated) {
		this.studentsUpdated = studentsUpdated;
	}
	
}
