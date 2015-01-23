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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.activiti.bpmn.model.ExclusiveGateway;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FormProperty;
import org.activiti.bpmn.model.FormValue;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.bpmn.behavior.ExclusiveGatewayActivityBehavior;
import org.activiti.engine.impl.bpmn.parser.handler.UserTaskParseHandler;
import org.activiti.engine.impl.form.DefaultTaskFormHandler;
import org.activiti.engine.impl.form.TaskFormHandler;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.impl.task.TaskDefinition;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Specific parsing of one BPMN 2.0 XML file, created by the {@link BpmnParser}.
 * 
 * @author Tijs Rademakers
 * @author Joram Barrez
 * @author Dmitry Farafonov
 */
public class CustomBpmnParse extends BpmnParse {

	protected static final Logger LOGGER = LoggerFactory.getLogger(CustomBpmnParse.class);

	private static final String OUTPUT_TRANSITION = "outputTransition";
	private static final String DEFAULT_OUTPUT_TRANSITION_NAME = "default";

	private Collection<FlowElement> flowElements = null;

	public CustomBpmnParse(BpmnParser parser) {
		super(parser);
	}
	@Override
	public void processFlowElements(Collection<FlowElement> _flowElements) {
		flowElements = _flowElements;
		super.processFlowElements(flowElements);

		for (FlowElement flowElement : flowElements) {
			if (flowElement instanceof UserTask) {
				handleUserTaskForms((UserTask)flowElement);
			}
		}
	}

	public void handleUserTaskForms(UserTask userTask) {
		try {
			List<SequenceFlow> outgoingFlows = userTask.getOutgoingFlows();
			String defaultSequenceFlow = null;

			List<String> outputTransitionNames = new ArrayList<String>();
			
			// Skip if user task has no outgoing flows
			if (outgoingFlows.size() == 0) {
				return;
			} else if (outgoingFlows.size() == 1) {
				String targetRef = outgoingFlows.get(0).getTargetRef();
				ActivityImpl outgoingActivity = getCurrentScope().findActivity(targetRef);
				if (!(outgoingActivity.getActivityBehavior() instanceof ExclusiveGatewayActivityBehavior)) {
					// we have only one outgoing flow from the user task and it is not finished by Exlusive Gateway.
					// Lets use name of this flow as output name
					SequenceFlow sequenceFlow = outgoingFlows.get(0);

					if (StringUtils.isNotEmpty(sequenceFlow.getName())) {
						outputTransitionNames.add(sequenceFlow.getName());
					} else {
						outputTransitionNames.add(DEFAULT_OUTPUT_TRANSITION_NAME);
					}
					LOGGER.debug("* id: {}, name: {}", sequenceFlow.getId(), sequenceFlow.getName());
					
					addOutputTransitionToTaskForm(userTask, outputTransitionNames);

					// finish to process task without next exclusive gateway
					return;
				}
				
				
				FlowElement outgoingFlowElement = findFlowElement(flowElements, targetRef);

				outgoingFlows = ((ExclusiveGateway)outgoingFlowElement).getOutgoingFlows();
				// Skip if exclusive gateway has only one outgoing flow
				if (outgoingFlows.size() <= 1) {
					return;
				}
				defaultSequenceFlow = (String) outgoingActivity.getProperty("default");
			}

			

			for (SequenceFlow sequenceFlow : outgoingFlows) {
				boolean isDefaultFlow = defaultSequenceFlow != null && sequenceFlow.getId() != null && sequenceFlow.getId().equals(defaultSequenceFlow);
				TransitionImpl transition = getSequenceFlows().get(sequenceFlow.getId());
				Boolean automaticGeneratedCondition = (Boolean) transition.getProperty("automaticGeneratedCondition");

				if (Boolean.TRUE.equals(automaticGeneratedCondition)) {
					// TODO: Do we need to collect transition names just for automatic generated conditions?
					outputTransitionNames.add(sequenceFlow.getName());
				}
				if (isDefaultFlow) {
					// TODO: What about default transition?
					if (StringUtils.isNotEmpty(sequenceFlow.getName())) {
						outputTransitionNames.add(sequenceFlow.getName());
					} else {
						outputTransitionNames.add(DEFAULT_OUTPUT_TRANSITION_NAME);
					}
				}
				LOGGER.debug("*{} id: {}, name: {}, expresion: {}{}", ((Boolean.TRUE.equals(automaticGeneratedCondition))? "!":" "), sequenceFlow.getId(), sequenceFlow.getName(), sequenceFlow.getConditionExpression(), ((isDefaultFlow)?", isDefault":""));
			}

			if (!outputTransitionNames.isEmpty()) {
				addOutputTransitionToTaskForm(userTask, outputTransitionNames);
			}
		} catch (Exception e) {
			LOGGER.error("Failed to improve user tasks form", e);
		}
	}

	private FlowElement findFlowElement(Collection<FlowElement> flowElements, String elementId) {
		for (FlowElement flowElement : flowElements) {
			if (flowElement.getId().equals(elementId)) {
				return flowElement;
			}
		}
		return null;
	}

	protected void addOutputTransitionToTaskForm(UserTask userTask, List<String> outpuTransitionNames) {
		ActivityImpl userTaskActivity = getCurrentScope().findActivity(userTask.getId());
		TaskDefinition taskDefinition = (TaskDefinition) userTaskActivity.getProperty(UserTaskParseHandler.PROPERTY_TASK_DEFINITION);

		// TODO: what about original handler?
		// DefaultTaskFormHandler originalTaskFormHandler = (DefaultTaskFormHandler) taskDefinition.getTaskFormHandler();

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

	public BpmnParse execute() {
		// validateSchema = false;
		super.execute();

		if (bpmnModel.getErrors().size() > 0) {
			StringBuilder problemBuilder = new StringBuilder();
			Map<String, String> errors = bpmnModel.getErrors();
			for (String errorKey : errors.keySet()) {
				problemBuilder.append(errorKey);
				problemBuilder.append(": ");
				problemBuilder.append(errors.get(errorKey));
				problemBuilder.append("\n");
			}
			throw new ActivitiException("Errors while parsing:\n"
					+ problemBuilder.toString());
		}

		return this;
	}

}
