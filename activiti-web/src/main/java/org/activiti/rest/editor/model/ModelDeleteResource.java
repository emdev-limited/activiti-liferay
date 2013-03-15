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
package org.activiti.rest.editor.model;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.cmd.CustomDeleteModelCmd;
import org.activiti.engine.impl.cmd.GetModelsCmd;
import org.activiti.rest.api.ActivitiUtil;
import org.apache.commons.lang.StringUtils;
import org.restlet.data.Status;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;

/**
 * @author Tijs Rademakers
 */
public class ModelDeleteResource extends ServerResource implements ModelDataJsonConstants {
  
  protected static final Logger LOGGER = Logger.getLogger(ModelDeleteResource.class.getName());

  @Put
  public void deleteModel() {
	  String modelId = (String) getRequest().getAttributes().get("modelId");
	  String companyId = (String) getRequest().getAttributes().get("companyId");
	  try {
    	if (StringUtils.isEmpty(modelId)) {
    		throw new Exception("No model id provided");
    	}
    	RepositoryServiceImpl serviceImpl = (RepositoryServiceImpl) ActivitiUtil.getRepositoryService();
    	serviceImpl.getCommandExecutor().execute(new CustomDeleteModelCmd(companyId, modelId));
    } catch(Exception e) {
      LOGGER.log(Level.SEVERE, "Error deleting model", e);
      setStatus(Status.SERVER_ERROR_INTERNAL);
    }
  }
}
