package org.activiti.engine.impl.bpmn.parser.handler;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.impl.pvm.process.ActivityImpl;

public class FormPostProcessorWrapper {
	
	public FormPostProcessorWrapper(ActivityImpl sourceActivity, ActivityImpl destinationActivity) {
		this.sourceActivity = sourceActivity;
		this.destinationActivity = destinationActivity;
	}

	ActivityImpl sourceActivity;
	ActivityImpl destinationActivity;
	List<String> outputTransitionNames = new ArrayList<String>();
	
	public ActivityImpl getSourceActivity() {
		return sourceActivity;
	}
	public void setSourceActivity(ActivityImpl sourceActivity) {
		this.sourceActivity = sourceActivity;
	}
	public ActivityImpl getDestinationActivity() {
		return destinationActivity;
	}
	public void setDestinationActivity(ActivityImpl destinationActivity) {
		this.destinationActivity = destinationActivity;
	}
	public List<String> getOutputTransitionNames() {
		return outputTransitionNames;
	}
	public void setOutputTransitionNames(List<String> outputTransitionNames) {
		this.outputTransitionNames = outputTransitionNames;
	}
	
}
