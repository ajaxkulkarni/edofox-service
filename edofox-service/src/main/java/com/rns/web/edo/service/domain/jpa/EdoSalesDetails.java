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
@Table(name = "sales_details")
@DynamicUpdate
public class EdoSalesDetails {
	
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true)
	private Integer id;
	@Column(name = "institute_id")
	private Integer instituteId;
	@Column(name = "last_updated")
	private Date lastUpdated;
	@Column(name = "status")
	private String status;
	@Column(name = "comments")
	private String comments;
	
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getInstituteId() {
		return instituteId;
	}
	public void setInstituteId(Integer instituteId) {
		this.instituteId = instituteId;
	}
	public Date getLastUpdated() {
		return lastUpdated;
	}
	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getComments() {
		return comments;
	}
	public void setComments(String comments) {
		this.comments = comments;
	}
	
}
