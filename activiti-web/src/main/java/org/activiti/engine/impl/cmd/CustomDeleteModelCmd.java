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
import org.activiti.engine.impl.persistence.entity.CustomModelManager;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;


/**
 * @author Joram Barrez
 */
public class CustomDeleteModelCmd implements Command<Void>, Serializable {
	private static Log _log = LogFactoryUtil.getLog(CustomDeleteModelCmd.class);
	
  private static final long serialVersionUID = 1L;
  protected String companyId;
  protected String modelId;
  
  public CustomDeleteModelCmd(String companyId, String modelId) {
    this.companyId = companyId;
    this.modelId = modelId;
  }

  public Void execute(CommandContext commandContext) {
	  // FIXME: use  tenantId as companyId
		if (StringUtils.isEmpty(companyId) || !NumberUtils.isDigits(companyId)
				|| Long.valueOf(companyId) <= 0) {
			throw new ActivitiException("companyId is null");
		}
		
		if (StringUtils.isEmpty(modelId)) {
			throw new ActivitiException("modelId is null");
		}

		((CustomModelManager) commandContext
				.getSession(CustomModelManager.class)).deleteModel(companyId, modelId);
		return null;
  }
  
}
