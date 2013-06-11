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
package org.activiti.engine.impl.bpmn.parser;

import java.util.Collection;
import java.util.List;

import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FormProperty;
import org.activiti.bpmn.model.FormValue;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.impl.bpmn.parser.handler.FormPostProcessorThreadLocalUtil;
import org.activiti.engine.impl.bpmn.parser.handler.FormPostProcessorWrapper;
import org.activiti.engine.impl.bpmn.parser.handler.UserTaskParseHandler;
import org.activiti.engine.impl.form.DefaultTaskFormHandler;
import org.activiti.engine.impl.form.TaskFormHandler;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.task.TaskDefinition;

/**
 * Specific parsing of one BPMN 2.0 XML file, created by the {@link BpmnParser}.
 * 
 * @author Tijs Rademakers
 * @author Joram Barrez
 */
public class CustomBpmnParse extends BpmnParse {
	
	private static final String OUTPUT_TRANSITION = "outputTransition";

	public CustomBpmnParse(BpmnParser parser) {
		super(parser);
	}

	@Override
	public void processFlowElements(Collection<FlowElement> flowElements) {
		super.processFlowElements(flowElements);
		
		handleUserTaskForms(flowElements);
	}
  
	public void handleUserTaskForms(Collection<FlowElement> flowElements) {
		try {
			List<FormPostProcessorWrapper> forms = FormPostProcessorThreadLocalUtil.getFromThreadLocal();
			if (forms != null && !forms.isEmpty()) {
				for (FormPostProcessorWrapper form : forms) {
					List<String> outpuTransitionNames = form.getOutputTransitionNames();
					if (form.getSourceActivity() != null && outpuTransitionNames != null 
							&& !outpuTransitionNames.isEmpty()) {
						try {
							ActivityImpl userTaskActivity = form.getSourceActivity();
							//get user task
							UserTask userTask = null;
							for (FlowElement flowElement : flowElements) {
								if (flowElement instanceof UserTask && flowElement.getId().equals(userTaskActivity.getId())) {
									userTask = (UserTask) flowElement;
									break;
								}
							}
							if (userTask != null) {
								addOutputTransitionToTaskForm(userTask, userTaskActivity, outpuTransitionNames);
							}
						} catch (Exception e) {
							LOGGER.error("Failed to improve user task form", e);
						}
					}
				}
				//clean up thread local
				try {
					FormPostProcessorThreadLocalUtil.cleanUp();
				} catch (Exception e) {
					LOGGER.error("Failed to clean up FormPostProcessorThreadLocal ", e);
				}
			}
		} catch (Exception e) {
			LOGGER.error("Failed to improve user tasks form", e);
		}
	}
	
	protected void addOutputTransitionToTaskForm(UserTask userTask, ActivityImpl userTaskActivity, List<String> outpuTransitionNames) {
		
		TaskDefinition taskDefinition = (TaskDefinition) userTaskActivity.getProperty(UserTaskParseHandler.PROPERTY_TASK_DEFINITION);
		
		TaskFormHandler taskFormHandler = new DefaultTaskFormHandler();
		List<FormProperty> formProperties = userTask.getFormProperties();
		for (FormProperty formProperty : formProperties) {
			if (formProperty.getId().equals(OUTPUT_TRANSITION)) {
				return;
			}
		}
		FormProperty formProperty = new FormProperty();
		formProperty.setId(OUTPUT_TRANSITION);
		formProperty.setName(OUTPUT_TRANSITION);
		formProperty.setType("enum");
		for (String outpuTransitionName : outpuTransitionNames) {
			FormValue formValue = new FormValue();
			formValue.setId(outpuTransitionName);
			formValue.setName(outpuTransitionName);
			formProperty.getFormValues().add(formValue);
		}
		formProperty.setRequired(true);
		formProperty.setReadable(true);
		formProperty.setWriteable(true);
		formProperty.setVariable(null);
		userTask.getFormProperties().add(formProperty);
		
	    taskFormHandler.parseConfiguration(userTask.getFormProperties(), userTask.getFormKey(), this.getDeployment()
	    		, (ProcessDefinitionEntity) this.getCurrentScope().getProcessDefinition());
	    
	    taskDefinition.setTaskFormHandler(taskFormHandler);
	}
}
