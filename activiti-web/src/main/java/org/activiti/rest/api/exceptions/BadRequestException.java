package org.activiti.rest.api.exceptions;

import org.restlet.data.Status;

public class BadRequestException extends AbstractRestException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public BadRequestException(String message) {
		super(message);
	}
	
	public BadRequestException(String message, Throwable cause) {
		super(message, cause);
	}

	public int getCode() {
		return Status.CLIENT_ERROR_BAD_REQUEST.getCode();
	}
	
}
