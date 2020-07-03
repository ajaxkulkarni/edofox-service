package com.rns.web.edo.service.domain.jpa;

import static javax.persistence.GenerationType.IDENTITY;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicUpdate;

@Entity
@Table(name = "notices_map")
@DynamicUpdate
public class EdoNoticeMap {
	
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true)
	private Integer id;
	@Column(name = "notice")
	private Integer notice;
	@Column(name = "course")
	private Integer course;
	@Column(name = "division")
	private Integer division;

	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getNotice() {
		return notice;
	}
	public void setNotice(Integer notice) {
		this.notice = notice;
	}
	public Integer getCourse() {
		return course;
	}
	public void setCourse(Integer course) {
		this.course = course;
	}
	public Integer getDivision() {
		return division;
	}
	public void setDivision(Integer division) {
		this.division = division;
	}
	
}
