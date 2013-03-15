package net.emforge.activiti;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;

import net.emforge.activiti.spring.ApplicationContextProvider;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.workflow.DefaultWorkflowInstance;

public class WorkflowInstanceImpl extends DefaultWorkflowInstance {
	private static Log _log = LogFactoryUtil.getLog(WorkflowInstanceImpl.class);
	
	IdMappingService idMappingService = (IdMappingService) ApplicationContextProvider.getApplicationContext().getBean("idMappingService");
	private HistoryService historyService = ProcessEngines.getDefaultProcessEngine().getHistoryService();
	private RuntimeService runtimeService = ProcessEngines.getDefaultProcessEngine().getRuntimeService();

	public Map<String, Serializable> getWorkflowContext() {
		if (super.getWorkflowContext() != null)  {
			return super.getWorkflowContext();
		}
		return ((WorkflowInstanceManagerImpl) ApplicationContextProvider.getApplicationContext().getBean("workflowInstanceManager")).getWorkflowContext(getWorkflowInstanceId());
	}
	
	public Map<String, Serializable> getWorkflowContext(long workflowInstanceId) {
		String procInstanceId = String.valueOf(workflowInstanceId);
		ProcessInstance inst = runtimeService.createProcessInstanceQuery().processInstanceId(procInstanceId).singleResult();
		if (inst != null) {
			return WorkflowUtil.getWorkflowContext(inst.getId(), new HashMap<String, Serializable>());
		} else {
			//try to get it from history
			List<HistoricVariableInstance> hiVars = historyService.createHistoricVariableInstanceQuery().processInstanceId(procInstanceId).list();
			Map<String, Serializable> resMp = new HashMap<String, Serializable>();
    		if (hiVars != null) {
    			for (HistoricVariableInstance hiVar : hiVars) {
    				resMp.put(hiVar.getVariableName(), (Serializable) hiVar.getValue());
    			}
    		}
    		return resMp;
		}
	}

}
