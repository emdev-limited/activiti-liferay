package org.activiti.rest.api;

import java.io.Serializable;

public class BaseResponse implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String responseMessage;
	private int responseCode = 200; //default code
	private boolean responseSuccess = true; //default value
	
	public String getResponseMessage() {
		return responseMessage;
	}
	public void setResponseMessage(String responseMessage) {
		this.responseMessage = responseMessage;
	}
	public int getResponseCode() {
		return responseCode;
	}
	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}
	public boolean isResponseSuccess() {
		return responseSuccess;
	}
	public void setResponseSuccess(boolean responseSuccess) {
		this.responseSuccess = responseSuccess;
	}
}
