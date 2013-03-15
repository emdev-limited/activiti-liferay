package net.emforge.activiti.hook;

import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.BpmnParser;
import org.activiti.engine.impl.bpmn.parser.LiferayBpmnParse;
import org.activiti.engine.impl.cfg.BpmnParseFactory;
import org.activiti.engine.impl.el.ExpressionManager;

public class LiferayBpmnParser extends BpmnParser {

	public LiferayBpmnParser(ExpressionManager expressionManager, BpmnParseFactory bpmnParseFactory) {
		super(expressionManager, bpmnParseFactory);
	}

	@Override
	public BpmnParse createParse() {
	    return new LiferayBpmnParse(this);
	}
}
