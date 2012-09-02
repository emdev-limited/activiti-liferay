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
package net.emforge.activiti.engine.impl;

import net.emforge.activiti.engine.LiferayTaskService;
import net.emforge.activiti.engine.impl.cmd.AddWorkflowLogEntryCmd;
import net.emforge.activiti.log.WorkflowLogEntry;

/**
 * TODO: Add comment
 * 
 * @author Oliver Teichmann, PRODYNA AG
 * 
 */
public class LiferayTaskServiceImpl extends org.activiti.engine.impl.TaskServiceImpl
		implements LiferayTaskService {

	/* (non-Javadoc)
	 * @see net.emforge.activiti.engine.LiferayTaskService#addWorkflowLogEntry(java.lang.String, java.lang.String, net.emforge.activiti.log.WorkflowLogEntry)
	 */
	public void addWorkflowLogEntry(String taskId, String processInstance, WorkflowLogEntry workflowLogEntry) {
		commandExecutor.execute(new AddWorkflowLogEntryCmd(taskId, processInstance,
				workflowLogEntry));
	}
}
