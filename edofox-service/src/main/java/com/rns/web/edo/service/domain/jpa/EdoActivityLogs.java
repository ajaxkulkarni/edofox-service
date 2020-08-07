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
@Table(name = "activity_logs")
@DynamicUpdate
public class EdoActivityLogs {
	
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true)
	private Integer id;
	@Column(name = "user_id")
	private Integer loginId;
	@Column(name = "log_time")
	private Date logTime;
	@Column(name = "module")
	private String module;
	@Column(name = "comment")
	private String comment;
	@Column(name = "institute_id")
	private Integer instituteId;
	
	
	public EdoActivityLogs() {
		//setUserType(0);
	}
	
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getLoginId() {
		return loginId;
	}
	public void setLoginId(Integer loginId) {
		this.loginId = loginId;
	}
	public Date getLogTime() {
		return logTime;
	}
	public void setLogTime(Date logTime) {
		this.logTime = logTime;
	}
	public String getModule() {
		return module;
	}
	public void setModule(String module) {
		this.module = module;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public void setInstituteId(Integer instituteId) {
		this.instituteId = instituteId;
	}
	public Integer getInstituteId() {
		return instituteId;
	}
}
