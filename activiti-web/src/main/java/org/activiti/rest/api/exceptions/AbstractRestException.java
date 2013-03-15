package org.activiti.rest.api.exceptions;

import org.activiti.engine.ActivitiException;

public abstract class AbstractRestException extends ActivitiException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public AbstractRestException(String message) {
		super(message);
	}
	
	public AbstractRestException(String message, Throwable cause) {
		super(message, cause);
	}

	public abstract int getCode();
}
