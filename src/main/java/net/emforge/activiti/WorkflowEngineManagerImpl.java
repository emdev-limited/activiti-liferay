package net.emforge.activiti;

import java.util.HashMap;
import java.util.Map;

import net.emforge.activiti.spring.Initializable;

import org.activiti.engine.ProcessEngine;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import com.liferay.portal.kernel.workflow.WorkflowEngineManager;

@Service(value="workflowEngineManager")
public class WorkflowEngineManagerImpl implements WorkflowEngineManager, ApplicationContextAware, Initializable 
{
//	@Autowired
	ProcessEngine processEngine;
	
	ApplicationContext applicationContext;
	
	public void init() {
		processEngine = applicationContext.getBean("processEngine", ProcessEngine.class);
	}	

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
		return "5.9";
	}
	
	@Override
	public void setApplicationContext(ApplicationContext ctx)
			throws BeansException {
		applicationContext = ctx;
		
	}	

}
