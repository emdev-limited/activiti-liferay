package org.activiti.engine.impl.bpmn.parser;

import java.util.List;

import net.emforge.activiti.hook.LiferayMailActivityBehavior;

import org.activiti.engine.impl.bpmn.helper.ClassDelegate;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.util.xml.Element;

public class LiferayBpmnParse extends BpmnParse {

	public LiferayBpmnParse(BpmnParser parser) { 
		super(parser);
	}
	
	@Override
	protected void parseEmailServiceTask(ActivityImpl activity,
										 Element serviceTaskElement, List<FieldDeclaration> fieldDeclarations) {
	    validateFieldDeclarationsForEmail(serviceTaskElement, fieldDeclarations);
	    activity.setActivityBehavior((LiferayMailActivityBehavior) ClassDelegate.instantiateDelegate(LiferayMailActivityBehavior.class, fieldDeclarations));
	}

}
