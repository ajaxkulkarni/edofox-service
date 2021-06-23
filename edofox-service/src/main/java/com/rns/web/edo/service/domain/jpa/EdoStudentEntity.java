package com.rns.web.edo.service.domain.jpa;

import static javax.persistence.GenerationType.IDENTITY;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicUpdate;

@Entity
@Table(name = "student")
@DynamicUpdate
public class EdoStudentEntity {
	
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true)
	private Integer id;
	@Column(name = "name")
	private String name;
	@Column(name = "proctor_img")
	private String proctorImageRef;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getProctorImageRef() {
		return proctorImageRef;
	}
	public void setProctorImageRef(String proctorImageRef) {
		this.proctorImageRef = proctorImageRef;
	}

}
