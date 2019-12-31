package com.rns.web.edo.service.domain;

import java.math.BigDecimal;

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
	
	

}
