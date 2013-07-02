package net.emforge.activiti.hook;

import org.activiti.engine.impl.bpmn.parser.CustomBpmnParse;
import org.activiti.engine.impl.bpmn.parser.BpmnParser;

public class LiferayBpmnParser extends BpmnParser {

	public LiferayBpmnParser() {
		super();
	}

	@Override
	public CustomBpmnParse createParse() {
	    return new CustomBpmnParse(this);
	}
}
