package org.activiti.rest.api.exceptions;

import org.restlet.data.Status;

public class NotFoundException extends AbstractRestException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public NotFoundException(String message) {
		super(message);
	}

	public int getCode() {
		return Status.CLIENT_ERROR_NOT_FOUND.getCode();
	}
	
}
