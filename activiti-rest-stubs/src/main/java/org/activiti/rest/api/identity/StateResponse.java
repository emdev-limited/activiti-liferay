package org.activiti.rest.api.identity;

import javax.xml.bind.annotation.XmlRootElement;

import org.activiti.rest.api.BaseResponse;

@XmlRootElement
public class StateResponse extends BaseResponse {

  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
boolean success;

  public boolean isSuccess() {
    return success;
  }

  public StateResponse setSuccess(boolean success) {
    this.success = success;
    return this;
  }
}
