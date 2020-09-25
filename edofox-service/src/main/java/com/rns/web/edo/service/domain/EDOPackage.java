package com.rns.web.edo.service.domain;

import java.math.BigDecimal;
import java.util.Date;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class EDOPackage {
	
	private Integer id;
	private String name;
	private BigDecimal price;
	private BigDecimal offlinePrice;
	private EDOInstitute institute;
	private boolean selected;
	private String status;
	private Date createdDate;
	private String videoUrl;
	private Date fromDate;
	private Date toDate;
	private Long timeLeft;
	
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
	public BigDecimal getPrice() {
		return price;
	}
	public void setPrice(BigDecimal price) {
		this.price = price;
	}
	public EDOInstitute getInstitute() {
		return institute;
	}
	public void setInstitute(EDOInstitute institute) {
		this.institute = institute;
	}
	public boolean isSelected() {
		return selected;
	}
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	public BigDecimal getOfflinePrice() {
		return offlinePrice;
	}
	public void setOfflinePrice(BigDecimal offlinePrice) {
		this.offlinePrice = offlinePrice;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Date getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	public String getVideoUrl() {
		return videoUrl;
	}
	public void setVideoUrl(String videoUrl) {
		this.videoUrl = videoUrl;
	}
	public Date getFromDate() {
		return fromDate;
	}
	public void setFromDate(Date fromDate) {
		this.fromDate = fromDate;
	}
	public Date getToDate() {
		return toDate;
	}
	public void setToDate(Date toDate) {
		this.toDate = toDate;
	}
	public Long getTimeLeft() {
		return timeLeft;
	}
	public void setTimeLeft(Long timeLeft) {
		this.timeLeft = timeLeft;
	}
	
}
