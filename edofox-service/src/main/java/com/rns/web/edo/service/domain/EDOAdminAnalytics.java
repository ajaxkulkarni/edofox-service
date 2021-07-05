package com.rns.web.edo.service.domain;

import java.math.BigDecimal;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class EDOAdminAnalytics {
	

	private Integer studentsAppeared;
	private Integer expectedCount;
	private BigDecimal presenty;
	private BigDecimal absenty;
	private Integer activeTests;
	private Integer testSubmits;
	private Integer doubtsRaised;
	private Integer doubtsResolved;
	private Integer doubtsPending;
	
	private EDOInstitute institute;


	public Integer getExpectedCount() {
		return expectedCount;
	}

	public void setExpectedCount(Integer expectedCount) {
		this.expectedCount = expectedCount;
	}

	public BigDecimal getPresenty() {
		return presenty;
	}

	public void setPresenty(BigDecimal presenty) {
		this.presenty = presenty;
	}

	public BigDecimal getAbsenty() {
		return absenty;
	}

	public void setAbsenty(BigDecimal absenty) {
		this.absenty = absenty;
	}

	public Integer getActiveTests() {
		return activeTests;
	}

	public void setActiveTests(Integer activeTests) {
		this.activeTests = activeTests;
	}

	public Integer getTestSubmits() {
		return testSubmits;
	}

	public void setTestSubmits(Integer testSubmits) {
		this.testSubmits = testSubmits;
	}

	public EDOInstitute getInstitute() {
		return institute;
	}

	public void setInstitute(EDOInstitute institute) {
		this.institute = institute;
	}

	public Integer getStudentsAppeared() {
		return studentsAppeared;
	}

	public void setStudentsAppeared(Integer studentsAppeared) {
		this.studentsAppeared = studentsAppeared;
	}

	public Integer getDoubtsResolved() {
		return doubtsResolved;
	}

	public void setDoubtsResolved(Integer doubtsResolved) {
		this.doubtsResolved = doubtsResolved;
	}

	public Integer getDoubtsRaised() {
		return doubtsRaised;
	}

	public void setDoubtsRaised(Integer doubtsRaised) {
		this.doubtsRaised = doubtsRaised;
	}

	public Integer getDoubtsPending() {
		return doubtsPending;
	}

	public void setDoubtsPending(Integer duobtsPending) {
		this.doubtsPending = duobtsPending;
	}
	

}
