package com.rns.web.edo.service.domain.ext;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ExtDataRoot {

	private String status;//": "success",
    private Integer status_code;//": 200,
    private String message;//": "",
    public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Integer getStatus_code() {
		return status_code;
	}

	public void setStatus_code(Integer status_code) {
		this.status_code = status_code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	//"meta": {},
	private ExtDataNode data;

	public ExtDataNode getData() {
		return data;
	}

	public void setData(ExtDataNode data) {
		this.data = data;
	}
	
	
}
