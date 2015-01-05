package org.activiti.engine.impl.bpmn.parser.handler;

import java.util.Collections;
import java.util.List;

import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.engine.impl.Condition;
import org.activiti.engine.impl.bpmn.behavior.ExclusiveGatewayActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.el.UelExpressionCondition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ScopeImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.apache.commons.lang.StringUtils;

/**
 * 
 * @author Emdev
 * @author Dmitry Farafonov
 *
 */
public class CustomSequenceFlowParseHandler extends SequenceFlowParseHandler {

	@Override
	protected void executeParse(BpmnParse bpmnParse, SequenceFlow sequenceFlow) {
		super.executeParse(bpmnParse, sequenceFlow);
		
		ScopeImpl scope = bpmnParse.getCurrentScope();
		Process process = getCurrentProcess(bpmnParse);
		ActivityImpl sourceActivity = scope.findActivity(sequenceFlow.getSourceRef());
//	    ActivityImpl destinationActivity = scope.findActivity(sequenceFlow.getTargetRef());
	    
		if (sourceActivity != null
				&& (sourceActivity.getActivityBehavior() instanceof ExclusiveGatewayActivityBehavior || 
						sourceActivity.getActivityBehavior() instanceof UserTaskActivityBehavior)) {
	    	// Outgoing from Exclusive Gateway flows
	    	List<SequenceFlow> outgoingFlows = getOutgoingFlows(process, sourceActivity.getId());
	    	
	    	boolean hasAntecedentUserTask = false;
	    	
	    	if (sourceActivity.getActivityBehavior() instanceof UserTaskActivityBehavior){
	    		hasAntecedentUserTask = true;
	    	} else {
	    		// Find antecedent user task
	    		
		    	// Incoming to Exclusive Gateway flows
		    	List<SequenceFlow> incomingFlows = getIncomingFlows(process, sourceActivity.getId());
		    	
				// Process may have no user task or this Exclusive Gateway flows
				// after any activity elements are different of UserTask.
				// In this case we have no to change condition expression.
				hasAntecedentUserTask = false;
		    	for (SequenceFlow incomingFlow : incomingFlows) {
		    		ActivityImpl activity = scope.findActivity(incomingFlow.getSourceRef());
		    		if (activity.getActivityBehavior() instanceof UserTaskActivityBehavior) {
		    			hasAntecedentUserTask = true;
		    			break;
		    		}
				}
	    	}
	    	// Don't change sequence flow when activity has only one sequence flow or has no antecedent user task
	    	if (outgoingFlows.size() > 1 && hasAntecedentUserTask) {
	    	
		    	// PS: Exclusive Gateway may have more than one previous activities
		    	
		    	String defaultSequenceFlow = (String) sourceActivity.getProperty("default");
		    	boolean isDefaultFlow = defaultSequenceFlow != null && sequenceFlow.getId() != null && sequenceFlow.getId().equals(defaultSequenceFlow);
		    	
		    	// Change Expression condition only when condition expression is empty and is not default sequence flow
		    	if (StringUtils.isEmpty(sequenceFlow.getConditionExpression()) && StringUtils.isNotEmpty(sequenceFlow.getName())) {
		    		//Turn flow name into expression
	    			TransitionImpl transition = bpmnParse.getSequenceFlows().get(sequenceFlow.getId());
	    			if (!isDefaultFlow) {
		    			String expression = "${outputTransition == \"" + sequenceFlow.getName() + "\"}";
			    		sequenceFlow.setConditionExpression(expression);
			    		transition.setProperty("automaticGeneratedCondition", true);
			    		Condition expressionCondition = new UelExpressionCondition(bpmnParse.getExpressionManager().createExpression(expression));
			            transition.setProperty(PROPERTYNAME_CONDITION_TEXT, expression);
			            transition.setProperty(PROPERTYNAME_CONDITION, expressionCondition);
			            createExecutionListenersOnTransition(bpmnParse, sequenceFlow.getExecutionListeners(), transition);
	    			}
		    	}
	    	}
	    }
	}
	
	private List<SequenceFlow> getOutgoingFlows(Process process, String sourceRef) {
		FlowElement sourceFlowElement = process.getFlowElement(sourceRef);
		if (sourceFlowElement instanceof FlowNode) {
			return ((FlowNode)sourceFlowElement).getOutgoingFlows();
		}
		return Collections.EMPTY_LIST;
	}
	
	private List<SequenceFlow> getIncomingFlows(Process process, String destinationRef) {
		FlowElement destinationFlowElement = process.getFlowElement(destinationRef);
		if (destinationFlowElement instanceof FlowNode) {
			return ((FlowNode)destinationFlowElement).getIncomingFlows();
		}
		return Collections.EMPTY_LIST;
	}
	
	private Process getCurrentProcess(BpmnParse bpmnParse) {
		ScopeImpl scope = bpmnParse.getCurrentScope();
		List<Process> processes = bpmnParse.getBpmnModel().getProcesses();
		for (Process process : processes) {
			if (process.getId().equals(scope.getProcessDefinition().getKey())) {
				return process;
			}
		}
		return null;
	}
}
