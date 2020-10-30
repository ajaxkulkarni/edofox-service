package com.rns.web.edo.service.domain.ext;

import org.apache.pdfbox.pdmodel.PDPage;

public class EdoPDFCoordinate {
	
	private float x;
	private float y;
	private Integer questionNumber;
	private float width;
	private float height;
	private PDPage page;
	private Float whiteSpaceY;
	private Float lastTextY;
	
	public EdoPDFCoordinate() {

	}
	
	public EdoPDFCoordinate(float x, float y, float height, float width, Integer questionNumber) {
		setX(x);
		setY(y);
		setHeight(height);
		setWidth(width);
		setQuestionNumber(questionNumber);
	}
	
	
	public float getX() {
		return x;
	}
	public void setX(float x) {
		this.x = x;
	}
	public float getY() {
		return y;
	}
	public void setY(float y) {
		this.y = y;
	}
	public Integer getQuestionNumber() {
		return questionNumber;
	}
	public void setQuestionNumber(Integer questionNumber) {
		this.questionNumber = questionNumber;
	}
	public float getWidth() {
		return width;
	}
	public void setWidth(float width) {
		this.width = width;
	}
	public float getHeight() {
		return height;
	}
	public void setHeight(float height) {
		this.height = height;
	}

	public PDPage getPage() {
		return page;
	}

	public void setPage(PDPage page) {
		this.page = page;
	}

	public Float getWhiteSpaceY() {
		return whiteSpaceY;
	}

	public void setWhiteSpaceY(Float whiteSpaceY) {
		this.whiteSpaceY = whiteSpaceY;
	}

	public Float getLastTextY() {
		return lastTextY;
	}

	public void setLastTextY(Float lastTextY) {
		this.lastTextY = lastTextY;
	}
	
	

}
