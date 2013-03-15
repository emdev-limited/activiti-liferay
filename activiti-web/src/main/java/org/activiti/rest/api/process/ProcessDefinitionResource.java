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

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.SecuredResource;
import org.activiti.rest.api.exceptions.BadRequestException;
import org.activiti.rest.api.exceptions.NotFoundException;
import org.restlet.resource.Get;

/**
 * @author Tijs Rademakers
 */
public class ProcessDefinitionResource extends SecuredResource {
  
  private RepositoryServiceImpl repositoryService = (RepositoryServiceImpl) ActivitiUtil.getRepositoryService();
  private ProcessDefinitionEntity processDefinition;
  
  @Get
  public ProcessDefinitionResponse getProcessDefinition() {
    if(authenticate() == false) return null;

    String processDefinitionId = (String) getRequest().getAttributes().get("processDefinitionId");
    
    if (processDefinitionId == null) {
		throw new BadRequestException("No process definition id provided");
	}
    
    if (!processDefinitionId.contains(":")) {
    	String processDefinitionKey = processDefinitionId;
    	
    	processDefinition = (ProcessDefinitionEntity) repositoryService.createProcessDefinitionQuery()
		  		.processDefinitionKey(processDefinitionKey)
		  		.latestVersion().singleResult();
    } else {    
    	processDefinition = (ProcessDefinitionEntity) repositoryService
        	.getDeployedProcessDefinition(processDefinitionId);
    }
    
    if (processDefinition == null) {
		throw new NotFoundException("Process definition " + processDefinitionId + " could not be found");
	}
    
    // Process definition response
	
 	ProcessDefinitionResponse processDefinitionResponse = new ProcessDefinitionResponse(processDefinition);
 	
 	processDefinitionResponse.setGraphicNotationDefined(isGraphicNotationDefined(processDefinition));
    
    return processDefinitionResponse;
  }
  
  private boolean isGraphicNotationDefined(ProcessDefinitionEntity processDefinition) {
    return ((ProcessDefinitionEntity) repositoryService.getDeployedProcessDefinition(processDefinition.getId())).isGraphicalNotationDefined();
  }
}
