package net.emforge.activiti;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.task.Task;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.workflow.DefaultWorkflowTask;

public class WorkflowTaskImpl extends DefaultWorkflowTask {
	private static Log _log = LogFactoryUtil.getLog(WorkflowTaskImpl.class);
	
	private TaskService taskService = ProcessEngines.getDefaultProcessEngine().getTaskService();
	private HistoryService historyService = ProcessEngines.getDefaultProcessEngine().getHistoryService();
	
	@Override
	public Map<String, Serializable> getOptionalAttributes() {
		if (super.getOptionalAttributes() != null && !super.getOptionalAttributes().isEmpty()) {
			return super.getOptionalAttributes();
		}
		String taskId = String.valueOf(getWorkflowTaskId());
		return getWorkflowContext(taskId);
	}
	
	public Map<String, Serializable> getWorkflowContext(String taskId) {
		Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
		if (task != null) {
			return WorkflowUtil.getWorkflowContext(String.valueOf(task.getExecutionId()), new HashMap<String, Serializable>());
		} else {
			//try to search in history
			HistoricTaskInstance hiTask = historyService.createHistoricTaskInstanceQuery().taskId(taskId).singleResult();
			if (hiTask != null) {
				//try to do it in history
        		List<HistoricVariableInstance> hiVars = historyService.createHistoricVariableInstanceQuery().processInstanceId(hiTask.getProcessInstanceId()).list();
        		Map<String, Serializable> resMp = new HashMap<String, Serializable>();
        		if (hiVars != null) {
        			for (HistoricVariableInstance hiVar : hiVars) {
        				resMp.put(hiVar.getVariableName(), (Serializable) hiVar.getValue());
        			}
        		}
        		return resMp;
			}
		}
		return Collections.EMPTY_MAP;
	}
	
	/**
	 * Use this method instead getWorkflowDefinitionId() to get real value
	 * 
	 * @return
	 */
	public String getProcessDefinitionId() {
		return getWorkflowDefinitionName() + StringPool.COLON + getWorkflowDefinitionVersion() + StringPool.COLON + getWorkflowDefinitionId();
	}
	
}
