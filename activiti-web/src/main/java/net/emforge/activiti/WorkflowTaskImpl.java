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

public class WorkflowTaskImpl extends DefaultWorkflowTask implements WorkflowTaskExt {
	private static Log _log = LogFactoryUtil.getLog(WorkflowTaskImpl.class);
	
	private TaskService taskService = ProcessEngines.getDefaultProcessEngine().getTaskService();
	private HistoryService historyService = ProcessEngines.getDefaultProcessEngine().getHistoryService();
	
	@Override
	public Map<String, Serializable> getOptionalAttributes() {
	    Map<String, Serializable> mpRes = new HashMap<String, Serializable>(3);
	    Map<String, Serializable> mpSuper = super.getOptionalAttributes();
		if (mpSuper != null && ! mpSuper.isEmpty()) {
		    mpRes.putAll(mpSuper);
		}
		
		String taskId = String.valueOf(getWorkflowTaskId());
		Map<String, Serializable> mpCon = getWorkflowContext(taskId);
        if (mpCon != null && ! mpCon.isEmpty()) {
            mpRes.putAll(mpCon);
        }
		
        return mpRes;
	}
	
	public Map<String, Serializable> getWorkflowContext(String taskId) {
		Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
		if (task != null) {		    
		    Map<String, Serializable> mpContext = WorkflowUtil.getWorkflowContext(String.valueOf(task.getExecutionId()), new HashMap<String, Serializable>());
		    WorkflowUtil.putTaskVariables(mpContext,  taskService.getVariablesLocal(task.getId()));
		    return mpContext;
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

    @Override
    public String getOutputTransition() {
        String taskId = String.valueOf(getWorkflowTaskId());
        Map<String, Object> mpVars = taskService.getVariablesLocal(taskId);
        return (String) mpVars.get(WorkflowConstants.NAME_TASK_OUTPUT_TRANSITION);
    }
	
}
