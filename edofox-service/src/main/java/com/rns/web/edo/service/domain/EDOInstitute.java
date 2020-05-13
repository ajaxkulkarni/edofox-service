package com.rns.web.edo.service.domain;

import java.util.Date;

public class EDOInstitute {
	
	private Integer id;
	private String name;
	private String firebaseId;
	private String contact;
	private String username;
	private String password;
	private Date expiryDate;
	private String expiryDateString;
	private Integer adminId;
	private String purchase;
	private Double storageQuota;
	private Integer maxStudents;
	private String email;
	private Integer currentCount;
	
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
	public String getFirebaseId() {
		return firebaseId;
	}
	public void setFirebaseId(String firebaseId) {
		this.firebaseId = firebaseId;
	}
	public String getContact() {
		return contact;
	}
	public void setContact(String contact) {
		this.contact = contact;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public Date getExpiryDate() {
		return expiryDate;
	}
	public void setExpiryDate(Date expiryDate) {
		this.expiryDate = expiryDate;
	}
	public String getExpiryDateString() {
		return expiryDateString;
	}
	public void setExpiryDateString(String expiryDateString) {
		this.expiryDateString = expiryDateString;
	}
	public Integer getAdminId() {
		return adminId;
	}
	public void setAdminId(Integer adminId) {
		this.adminId = adminId;
	}
	public String getPurchase() {
		return purchase;
	}
	public void setPurchase(String purchase) {
		this.purchase = purchase;
	}
	public Double getStorageQuota() {
		return storageQuota;
	}
	public void setStorageQuota(Double storageQuota) {
		this.storageQuota = storageQuota;
	}
	public Integer getMaxStudents() {
		return maxStudents;
	}
	public void setMaxStudents(Integer maxStudents) {
		this.maxStudents = maxStudents;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public Integer getCurrentCount() {
		return currentCount;
	}
	public void setCurrentCount(Integer currentCount) {
		this.currentCount = currentCount;
	}

}
