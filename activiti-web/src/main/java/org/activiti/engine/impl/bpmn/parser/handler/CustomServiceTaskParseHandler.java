package org.activiti.engine.impl.bpmn.parser.handler;

import java.util.List;

import net.emforge.activiti.hook.LiferayMailActivityBehavior;

import org.activiti.bpmn.model.ServiceTask;
import org.activiti.engine.impl.bpmn.helper.ClassDelegate;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.FieldDeclaration;
import org.activiti.engine.impl.bpmn.parser.factory.AbstractBehaviorFactory;
import org.activiti.engine.impl.bpmn.parser.factory.ActivityBehaviorFactory;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.apache.commons.lang.StringUtils;

public class CustomServiceTaskParseHandler extends ServiceTaskParseHandler {

	@Override
	protected void executeParse(BpmnParse bpmnParse, ServiceTask serviceTask) {
		super.executeParse(bpmnParse, serviceTask);
		ActivityImpl activity = bpmnParse.getCurrentActivity();
		// Email service tasks
	      if (StringUtils.isNotEmpty(serviceTask.getType())) {
	    	  if (serviceTask.getType().equalsIgnoreCase("mail")) {
	    		  ActivityBehaviorFactory abf = bpmnParse.getActivityBehaviorFactory();
	    		  if (abf instanceof AbstractBehaviorFactory) {
	    			  //createFieldDeclarations method available there
	    			  //otherwise use standard Mail behavior
	    			  List<FieldDeclaration> fieldDeclarations = ((AbstractBehaviorFactory) abf).createFieldDeclarations(serviceTask.getFieldExtensions());
		    		  activity.setActivityBehavior((LiferayMailActivityBehavior) ClassDelegate.instantiateDelegate(LiferayMailActivityBehavior.class, fieldDeclarations));
	    		  }
	    	  }
	      }
	}
}
