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
@Table(name = "video_lectures")
@DynamicUpdate
public class EdoVideoLecture {
	
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true)
	private Integer id;
	@Column(name = "video_name")
	private String videoName;
	@Column(name = "created_date")
	private Date createdDate;
	@Column(name = "subject_id")
	private Integer subjectId;
	@Column(name = "institute_id")
	private Integer instituteId;
	@Column(name = "video_url")
	private String video_url;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getVideoName() {
		return videoName;
	}
	public void setVideoName(String videoName) {
		this.videoName = videoName;
	}
	public Date getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	public Integer getSubjectId() {
		return subjectId;
	}
	public void setSubjectId(Integer subjectId) {
		this.subjectId = subjectId;
	}
	public Integer getInstituteId() {
		return instituteId;
	}
	public void setInstituteId(Integer instituteId) {
		this.instituteId = instituteId;
	}
	public String getVideo_url() {
		return video_url;
	}
	public void setVideo_url(String video_url) {
		this.video_url = video_url;
	}

}
