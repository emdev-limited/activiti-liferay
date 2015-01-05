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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.emforge.activiti.query.ResourceByCompanyQuery;
import net.emforge.activiti.query.ResourceByCompanyQueryImpl;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.persistence.entity.CustomModelManager;
import org.activiti.engine.repository.Model;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringPool;


/**
 * @author Joram Barrez
 */
public class GetModelsCmd implements Command<List<Model>>, Serializable {
	private static Log _log = LogFactoryUtil.getLog(GetModelsCmd.class);
	
  private static final long serialVersionUID = 1L;
  protected String companyId;
  protected int start;
  protected int end;
  
  public GetModelsCmd(String companyId, int start ,int end) {
    this.companyId = companyId;
    this.start = start;
    this.end = end;
  }

  public List<Model> execute(CommandContext commandContext) {
	// FIXME: use  tenantId as companyId
		if (StringUtils.isEmpty(companyId) || !NumberUtils.isDigits(companyId)
				|| Long.valueOf(companyId) <= 0) {
			throw new ActivitiException("companyId is null");
		}

		CommandExecutor commandExecutor = Context
		        .getProcessEngineConfiguration()
		        .getCommandExecutor();
		ResourceByCompanyQuery rbc = new ResourceByCompanyQueryImpl(commandExecutor);
		rbc.companyAndNameLike(companyId, "model:%:company");
		List<String> names = rbc.list();

		if (names == null || names.isEmpty()) {
			_log.debug("No models exist");
			return Collections.EMPTY_LIST;
		} else {
			_log.debug(">>>>>Names[0] = " + names.get(0) + ", names size = " + names.size());
		}
		List<String> ids = extractModelIds(names);

		List<Model> models = ((CustomModelManager) commandContext
				.getSession(CustomModelManager.class)).findModelsByIds(ids, start, end);
		return models;
  }
  
  private List<String> extractModelIds(List<String> names) {
		List<String> ids = new ArrayList<String>();

		for (String name : names) {
			String id = name.substring(name.indexOf(StringPool.COLON) + 1);
			id = id.substring(0, id.indexOf(StringPool.COLON));
			
			_log.debug("Id to convert: " + id);
			// check
			if (NumberUtils.isNumber(id)) {
				ids.add(id);
			} else {
				_log.error("Failed to convert model id from resource name = "
						+ name);
			}
		}

		return ids;
  }
}
