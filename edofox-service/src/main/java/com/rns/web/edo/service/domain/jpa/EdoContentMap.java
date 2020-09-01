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
@Table(name = "dlp_chp_cls_content_map")
@DynamicUpdate
public class EdoContentMap {
	
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true)
	private Integer id;
	@Column(name = "chapter_id", unique = true)
	private Integer chapterId;
	@Column(name = "test_id", unique = true)
	private Integer testId;
	@Column(name = "classroom_id", unique = true)
	private Integer classroomId;
	@Column(name = "content_id", unique = true)
	private Integer contentId;
	@Column(name = "content_order", unique = true)
	private Integer contentOrder;
	@Column(name = "created_date", unique = true)
	private Date createdDate;
	
	
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
	public Integer getChapterId() {
		return chapterId;
	}
	public void setChapterId(Integer chapterId) {
		this.chapterId = chapterId;
	}
	public Integer getTestId() {
		return testId;
	}
	public void setTestId(Integer testId) {
		this.testId = testId;
	}
	public Integer getClassroomId() {
		return classroomId;
	}
	public void setClassroomId(Integer classroomId) {
		this.classroomId = classroomId;
	}
	public Integer getContentId() {
		return contentId;
	}
	public void setContentId(Integer contentId) {
		this.contentId = contentId;
	}
	public Integer getContentOrder() {
		return contentOrder;
	}
	public void setContentOrder(Integer contentOrder) {
		this.contentOrder = contentOrder;
	}
	
}
