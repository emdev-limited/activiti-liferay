package org.activiti.engine.impl.bpmn.parser.handler;

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

public class CustomSequenceFlowParseHandler extends SequenceFlowParseHandler {

	@Override
	protected void executeParse(BpmnParse bpmnParse, SequenceFlow sequenceFlow) {
		super.executeParse(bpmnParse, sequenceFlow);
		
		ScopeImpl scope = bpmnParse.getCurrentScope();
		ActivityImpl sourceActivity = scope.findActivity(sequenceFlow.getSourceRef());
	    ActivityImpl destinationActivity = scope.findActivity(sequenceFlow.getTargetRef());
	    
	    if (sourceActivity != null && sourceActivity.getActivityBehavior() instanceof ExclusiveGatewayActivityBehavior) {
	    	if (StringUtils.isEmpty(sequenceFlow.getConditionExpression())) {
	    		//Turn flow name into expression
	    		TransitionImpl transition = bpmnParse.getSequenceFlows().get(sequenceFlow.getId());
	    		String expression = "${outputTransition == \"" + sequenceFlow.getName() + "\"}";
	    		sequenceFlow.setConditionExpression(expression);
	    		Condition expressionCondition = new UelExpressionCondition(bpmnParse.getExpressionManager().createExpression(expression));
	            transition.setProperty(PROPERTYNAME_CONDITION_TEXT, expression);
	            transition.setProperty(PROPERTYNAME_CONDITION, expressionCondition);
	            createExecutionListenersOnTransition(bpmnParse, sequenceFlow.getExecutionListeners(), transition);
	            FormPostProcessorThreadLocalUtil.putToThreadLocal(null, sourceActivity, sequenceFlow.getName());
	    	}	
	    }
	    
	    if (destinationActivity != null && destinationActivity.getActivityBehavior() instanceof ExclusiveGatewayActivityBehavior
	    		&& sourceActivity != null && sourceActivity.getActivityBehavior() instanceof UserTaskActivityBehavior) {
	    	if (StringUtils.isEmpty(sequenceFlow.getConditionExpression())) {
	    		//put it into thread local for post-processing. Use {@link FormPostProcessorWrapper}
		    	FormPostProcessorThreadLocalUtil.putToThreadLocal(sourceActivity, destinationActivity, null);
	    	}
	    }
	    
	}
}
