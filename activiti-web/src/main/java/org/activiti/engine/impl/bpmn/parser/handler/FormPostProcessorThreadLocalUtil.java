package org.activiti.engine.impl.bpmn.parser.handler;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.apache.commons.lang.StringUtils;

public class FormPostProcessorThreadLocalUtil {

	private static ThreadLocal<List<FormPostProcessorWrapper>> LOCALS = new ThreadLocal<List<FormPostProcessorWrapper>>();
	
	public static List<FormPostProcessorWrapper> getFromThreadLocal() {
		return LOCALS.get();
	}
	
	public static FormPostProcessorWrapper getFromThreadLocalBySourceAndDest(ActivityImpl sourceActivity, ActivityImpl destinationActivity) {
		if (sourceActivity == null || destinationActivity == null) return null;
		List<FormPostProcessorWrapper> objectList = LOCALS.get();
		if (objectList != null) {
			for (FormPostProcessorWrapper obj : objectList) {
				if (obj.getSourceActivity().equals(sourceActivity) && obj.getDestinationActivity().equals(destinationActivity)) {
					return obj;
				}
			}
		}
		return null;
	}
	
	public static FormPostProcessorWrapper getFromThreadLocalBySource(ActivityImpl sourceActivity) {
		if (sourceActivity == null) return null;
		List<FormPostProcessorWrapper> objectList = LOCALS.get();
		if (objectList != null) {
			for (FormPostProcessorWrapper obj : objectList) {
				if (obj.getSourceActivity().equals(sourceActivity)) {
					return obj;
				}
			}
		}
		return null;
	}
	
	public static FormPostProcessorWrapper getFromThreadLocalByDestination(ActivityImpl destinationActivity) {
		if (destinationActivity == null) return null;
		List<FormPostProcessorWrapper> objectList = LOCALS.get();
		if (objectList != null) {
			for (FormPostProcessorWrapper obj : objectList) {
				if (obj.getDestinationActivity().equals(destinationActivity)) {
					return obj;
				}
			}
		}
		return null;
	}
	
	public static void putToThreadLocal(ActivityImpl sourceActivity, ActivityImpl destinationActivity, String name) {
		if (destinationActivity == null) return;
		//check if it already exists
		FormPostProcessorWrapper existing = getFromThreadLocalByDestination(destinationActivity);
		if (existing != null) {
			if (!StringUtils.isEmpty(name)) {
				//put sequence flow info
				existing.getOutputTransitionNames().add(name);
			}
			if (sourceActivity != null) {
				existing.setSourceActivity(sourceActivity);
			}
		} else {
			FormPostProcessorWrapper obj = new FormPostProcessorWrapper(sourceActivity, destinationActivity);
			if (!StringUtils.isEmpty(name)) {
				//put sequence flow info
				obj.getOutputTransitionNames().add(name);
			}
			if (LOCALS.get() == null) {
				LOCALS.set(new ArrayList<FormPostProcessorWrapper>());
			}
			LOCALS.get().add(obj);
		}
	}
	
	public static void cleanUp() {
		LOCALS.set(new ArrayList<FormPostProcessorWrapper>());
	}
}
