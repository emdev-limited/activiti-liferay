package org.activiti.rest.api.exceptions;

import org.restlet.data.Status;

public class ConflictException extends AbstractRestException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ConflictException(String message) {
		super(message);
	}

	public int getCode() {
		return Status.CLIENT_ERROR_CONFLICT.getCode();
	}
	
}
