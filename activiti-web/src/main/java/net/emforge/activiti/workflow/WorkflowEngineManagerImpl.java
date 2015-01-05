package net.emforge.activiti.workflow;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.cmd.GetPropertiesCmd;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.liferay.portal.kernel.workflow.WorkflowEngineManager;

@Service("workflowEngineManager")
public class WorkflowEngineManagerImpl implements WorkflowEngineManager {
	@Autowired
	ProcessEngine processEngine;

	@Autowired
	RepositoryService repositoryService;

	@Override
	public String getKey() {
		return "activiti";
	}

	@Override
	public String getName() {
		return "Activiti";
	}

	@Override
	public Map<String, Object> getOptionalAttributes() {
		HashMap<String, Object> mp = new HashMap<String, Object>(2);

		mp.put("processEngine", processEngine);

		Map<String, String> properties = ((RepositoryServiceImpl) repositoryService)
				.getCommandExecutor().execute(new GetPropertiesCmd());
		mp.put("properties", properties);

		return mp;
	}

	@Override
	public String getVersion() {
		return ProcessEngine.VERSION;
	}

	@Override
	public boolean isDeployed() {
		return true;
	}

}
