package com.rns.web.edo.service.bo.api;

import java.io.InputStream;

public class EdoFile {
	
	private String fileName;
	private InputStream content;
	
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public InputStream getContent() {
		return content;
	}
	public void setContent(InputStream content) {
		this.content = content;
	}

}
