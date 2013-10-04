package net.emforge.activiti;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.ProcessEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.liferay.portal.kernel.workflow.WorkflowEngineManager;

@Service(value="workflowEngineManager")
public class WorkflowEngineManagerImpl implements WorkflowEngineManager {
	@Autowired
	ProcessEngine processEngine;

	@Override
	public String getKey() {
		return "activiti";
	}

	@Override
	public String getName() {
		return "Activiti";
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> getOptionalAttributes() {
		HashMap<String, Object> mp = new HashMap<String, Object>(1);
		mp.put("processEngine", processEngine);
		return mp;
	}

	@Override
	public String getVersion() {
		return "5.13";
	}
	
	@Override
	public boolean isDeployed() {
		return true;
	}
	
}
