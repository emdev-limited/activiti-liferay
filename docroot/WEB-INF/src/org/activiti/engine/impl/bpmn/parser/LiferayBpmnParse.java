package org.activiti.engine.impl.bpmn.parser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.emforge.activiti.hook.LiferayMailActivityBehavior;

import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.impl.Condition;
import org.activiti.engine.impl.bpmn.behavior.EventBasedGatewayActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.IntermediateCatchEventActivitiBehaviour;
import org.activiti.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.activiti.engine.impl.bpmn.helper.ClassDelegate;
import org.activiti.engine.impl.bpmn.listener.ExpressionExecutionListener;
import org.activiti.engine.impl.el.UelExpressionCondition;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ScopeImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.impl.task.TaskDefinition;
import org.activiti.engine.impl.util.xml.Element;
import org.apache.commons.lang.StringUtils;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

public class LiferayBpmnParse extends BpmnParse {
	private static Log _log = LogFactoryUtil.getLog(LiferayBpmnParse.class);
	
	protected Map<String,Element> userTaskElements = new HashMap<String,Element>();
	protected Map<String,Element> exclusiveGatewayElements = new HashMap<String,Element>();

	public LiferayBpmnParse(BpmnParser parser) { 
		super(parser);
	}
	
	@Override
	protected void parseEmailServiceTask(ActivityImpl activity,
										 Element serviceTaskElement, List<FieldDeclaration> fieldDeclarations) {
	    validateFieldDeclarationsForEmail(serviceTaskElement, fieldDeclarations);
	    activity.setActivityBehavior((LiferayMailActivityBehavior) ClassDelegate.instantiateDelegate(LiferayMailActivityBehavior.class, fieldDeclarations));
	}
	
	/**
	* Parses a userTask declaration.
	*/
	public ActivityImpl parseUserTask(Element userTaskElement, ScopeImpl scope) {
		String id = userTaskElement.attribute("id");
		if (id == null) {
	        addError("Invalid property usage on line " + userTaskElement.getLine() + ": no id or name specified.", userTaskElement);
	    }
		userTaskElements.put(id, userTaskElement);
		
		return super.parseUserTask(userTaskElement, scope);
	}
	
	/**
	   * Parses an exclusive gateway declaration.
	   */
	  public ActivityImpl parseExclusiveGateway(Element exclusiveGwElement, ScopeImpl scope) {
		String id = exclusiveGwElement.attribute("id");
		if (id == null) {
		     addError("Invalid property usage on line " + exclusiveGwElement.getLine() + ": no id or name specified.", exclusiveGwElement);
		}
		exclusiveGatewayElements.put(id, exclusiveGwElement);
		return super.parseExclusiveGateway(exclusiveGwElement, scope);
	  }
	
	/**
	   * Parses all sequence flow of a scope.
	   * 
	   * @param processElement
	   *          The 'process' element wherein the sequence flow are defined.
	   * @param scope
	   *          The scope to which the sequence flow must be added.
	   */
	public void parseSequenceFlow(Element processElement, ScopeImpl scope) {
	    for (Element sequenceFlowElement : processElement.elements("sequenceFlow")) {

	      String id = sequenceFlowElement.attribute("id");
	      String sourceRef = sequenceFlowElement.attribute("sourceRef");
	      String destinationRef = sequenceFlowElement.attribute("targetRef");
	      
	      // Implicit check: sequence flow cannot cross (sub) process boundaries: we
	      // don't do a processDefinition.findActivity here
	      ActivityImpl sourceActivity = scope.findActivity(sourceRef);
	      ActivityImpl destinationActivity = scope.findActivity(destinationRef);
	      
	      if (sourceActivity == null) {
	        addError("Invalid source '" + sourceRef + "' of sequence flow '" + id + "'", sequenceFlowElement);
	      } else if (destinationActivity == null) {
	        addError("Invalid destination '" + destinationRef + "' of sequence flow '" + id + "'", sequenceFlowElement);
	      } else if(sourceActivity.getActivityBehavior() instanceof EventBasedGatewayActivityBehavior) {     
	        // ignore
	      } else if(destinationActivity.getActivityBehavior() instanceof IntermediateCatchEventActivitiBehaviour
	              && (destinationActivity.getParentActivity() != null)
	              && (destinationActivity.getParentActivity().getActivityBehavior() instanceof EventBasedGatewayActivityBehavior)) {
	        addError("Invalid incoming sequenceflow for intermediateCatchEvent with id '"+destinationActivity.getId()+"' connected to an event-based gateway.", sequenceFlowElement);        
	      } else {                
	        TransitionImpl transition = sourceActivity.createOutgoingTransition(id);
	        sequenceFlows.put(id, transition);
	        transition.setProperty("name", sequenceFlowElement.attribute("name"));
	        transition.setProperty("documentation", parseDocumentation(sequenceFlowElement));
	        transition.setDestination(destinationActivity);
	        parseSequenceFlowConditionExpression(sequenceFlowElement, transition);
	        parseExecutionListenersOnTransition(sequenceFlowElement, transition);

	        for (BpmnParseListener parseListener : parseListeners) {
	          parseListener.parseSequenceFlow(sequenceFlowElement, scope, transition);
	        }
	        
	        if (userTaskElements.containsKey(destinationRef)) {
		    	  _log.debug("Found appropriate target user task");
		    	  ExecutionListener executionListener = new ExpressionExecutionListener(expressionManager.createExpression("#{execution.removeVariable(\"outputTransition\")}"));
		    	  if (executionListener != null) {
		              // Since a transition only fires event 'take', we don't parse the
		              // eventName, it is ignored
		    		  transition.addExecutionListener(executionListener);
		            }
		    }
	      } 
	    }
	  }
	
	/**
	   * Parses a condition expression on a sequence flow.
	   * 
	   * @param seqFlowElement
	   *          The 'sequenceFlow' element that can contain a condition.
	   * @param seqFlow
	   *          The sequenceFlow object representation to which the condition must
	   *          be added.
	   */
	  public void parseSequenceFlowConditionExpression(Element seqFlowElement, TransitionImpl seqFlow) {
	    Element conditionExprElement = seqFlowElement.element("conditionExpression");
	    if (conditionExprElement != null) {
	      String expression = conditionExprElement.getText().trim();
	      String type = conditionExprElement.attributeNS(BpmnParser.XSI_NS, "type");
	      if (type != null && !type.equals("tFormalExpression")) {
	        addError("Invalid type, only tFormalExpression is currently supported", conditionExprElement);
	      }

	      Condition expressionCondition = new UelExpressionCondition(expressionManager.createExpression(expression));
	      seqFlow.setProperty(PROPERTYNAME_CONDITION_TEXT, expression);
	      seqFlow.setProperty(PROPERTYNAME_CONDITION, expressionCondition);
	    } else {
	    	_log.debug("Found appropriate source Exclusive gateway");
	    	//Get label from sequence flow and use it in condition expression
	    	String id = seqFlowElement.attribute("id");
	    	String sourceRef = seqFlowElement.attribute("sourceRef");
		    if (exclusiveGatewayElements.containsKey(sourceRef)) {
		    	String name = seqFlowElement.attribute("name");
		    	String expression = "${outputTransition == \"" + name + "\"}";
		    	Condition expressionCondition = new UelExpressionCondition(expressionManager.createExpression(expression));
			    seqFlow.setProperty(PROPERTYNAME_CONDITION_TEXT, expression);
			    seqFlow.setProperty(PROPERTYNAME_CONDITION, expressionCondition);
		    }
	    }
	  }

}
