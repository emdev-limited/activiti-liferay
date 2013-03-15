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
package org.activiti.engine.impl.cmd;

import java.io.Serializable;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;


/**
 * This class only intended to insignificantly update process definition (e.g. category)
 * For other cases uses classic way of a direct deploy to take into account version etc.
 * 
 * @author Tijs Rademakers
 */
public class UpdateProcessDefinitionCmd implements Command<Void>, Serializable {
  
  private static final long serialVersionUID = 1L;
  protected ProcessDefinitionEntity model;
  
  public UpdateProcessDefinitionCmd(ProcessDefinitionEntity model) {
    this.model = model;
  }
  
  public Void execute(CommandContext commandContext) {
    if(model == null) {
      throw new ActivitiException("model is null");
    }
    commandContext.getDbSqlSession().update(model);
    commandContext.getDbSqlSession().flush();
    return null;
  }

}
