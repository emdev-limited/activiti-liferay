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

package org.activiti.rest.api.task;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.task.Task;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.SecuredResource;
import org.activiti.rest.api.exceptions.NotFoundException;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

/**
 * @author fav
 */
public class CompleteTaskResource extends SecuredResource {
	private static Log _log = LogFactoryUtil.getLog(CompleteTaskResource.class);
  
  @Post
  public ObjectNode completeTask(Representation entity) {
    if(authenticate() == false) return null;
    ObjectNode resultNode = new ObjectMapper().createObjectNode();
    String taskId = (String) getRequest().getAttributes().get("taskId");
    
    Task task = ActivitiUtil.getTaskService().createTaskQuery().taskId(taskId).singleResult();
    
    if(task == null) {
      throw new NotFoundException("Task not found for id " + taskId);
    }
    
    try {
    	Map<String,Object> vars = new HashMap<String, Object>();
      String taskParams = entity.getText();
      if (StringUtils.isNotEmpty(taskParams)) {
    	  JsonNode taskJSON = new ObjectMapper().readTree(taskParams);
    	  //get variables names
    	  String varNames = null;
          if(taskJSON.path("varNames") != null && taskJSON.path("varNames").getTextValue() != null) {
        	  varNames = taskJSON.path("varNames").getTextValue();
          }
          if (StringUtils.isNotEmpty(varNames)) {
        	  if (varNames.contains(",")) {
        		  String[] keys = varNames.split(",");
            	  for (String key : keys) {
            		  if (taskJSON.path(key).getTextValue() != null) {
            			  vars.put(key, taskJSON.path(key).getTextValue());
            		  }
            	  }
        	  } else {
        		  //it possibly means that only one key-value pair
        		  if (taskJSON.path(varNames).getTextValue() != null) {
        			  vars.put(varNames, taskJSON.path(varNames).getTextValue());
        		  }
        	  }
          
          }
      }
      ActivitiUtil.getTaskService().complete(taskId, vars);
      resultNode.put("success", true);
    } catch (Exception e) {
    	_log.error("Failed to update task " + taskId, e);
      resultNode.put("success", false);
    }
    return resultNode;
  }
}
