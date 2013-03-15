package org.activiti.rest.api.exceptions;

import org.restlet.data.Status;

public class NotImplementedException extends AbstractRestException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public NotImplementedException(String message) {
		super(message);
	}

	public int getCode() {
		return Status.SERVER_ERROR_NOT_IMPLEMENTED.getCode();
	}
	
}
