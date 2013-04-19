package org.activiti.engine.impl.cfg;

import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.BpmnParser;
import org.activiti.engine.impl.bpmn.parser.CustomBpmnParse;

public class CustomBpmnParseFactory extends DefaultBpmnParseFactory {
	
	public BpmnParse createBpmnParse(BpmnParser bpmnParser) {
		return new CustomBpmnParse(bpmnParser);
	}
}
