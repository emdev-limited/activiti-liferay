/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.rest.api.process;

import javax.xml.bind.annotation.XmlRootElement;

import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.rest.api.BaseResponse;

/**
 * @author Tijs Rademakers
 */
@XmlRootElement
public class StartProcessInstanceResponse extends BaseResponse {
  
  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
String id;
  String businessKey;
  String processInstanceId;
  String processDefinitionId;
  
  public StartProcessInstanceResponse() {}
  
  public StartProcessInstanceResponse(ProcessInstance processInstance) {
    this.setId(processInstance.getId());
    this.setBusinessKey(processInstance.getBusinessKey());
    this.setProcessInstanceId(processInstance.getProcessInstanceId());
    this.setProcessDefinitionId(processInstance.getProcessDefinitionId());
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getBusinessKey() {
    return businessKey;
  }

  public void setBusinessKey(String businessKey) {
    this.businessKey = businessKey;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }
}
