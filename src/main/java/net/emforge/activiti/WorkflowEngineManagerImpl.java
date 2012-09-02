package net.emforge.activiti;

import java.util.Collections;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.liferay.portal.kernel.workflow.WorkflowEngineManager;

@Service(value="workflowEngineManager")
public class WorkflowEngineManagerImpl implements WorkflowEngineManager {

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
		return Collections.EMPTY_MAP;
	}

	@Override
	public String getVersion() {
		return "5.0.rc1";
	}

}
