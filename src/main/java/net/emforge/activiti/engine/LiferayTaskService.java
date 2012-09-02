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
package net.emforge.activiti.engine;

import net.emforge.activiti.log.WorkflowLogEntry;

/** Extended interface for {@link org.activiti.engine.TaskService}.
 * 
 * @author Oliver Teichmann, PRODYNA AG
 */
public interface LiferayTaskService extends org.activiti.engine.TaskService {

	  /** Add a workflow log entry to a task and/or process instance. */
	  void addWorkflowLogEntry(String taskId, String processInstanceId, WorkflowLogEntry workflowLogEntry);
}
