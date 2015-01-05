package net.emforge.activiti.workflow;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.emforge.activiti.WorkflowConstants;
import net.emforge.activiti.WorkflowTaskExt;
import net.emforge.activiti.WorkflowUtil;

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
	private static final long serialVersionUID = 1L;

	private static Log _log = LogFactoryUtil.getLog(WorkflowTaskImpl.class);

	private TaskService taskService = ProcessEngines.getDefaultProcessEngine().getTaskService();
	private HistoryService historyService = ProcessEngines.getDefaultProcessEngine().getHistoryService();
	private String deleteReason;

	/**
	 * <p>
	 * Returns map {@code optionalAttributes} if is not empty. Otherwise
	 * retrieves task variables and execution variables and stores retrieved
	 * variables into {@code optionalAttributes} map.
	 * 
	 * <p>
	 * Changed by: Dmitry Farafonov
	 */
	@Override
	public Map<String, Serializable> getOptionalAttributes() {
		Map<String, Serializable> mpRes = new HashMap<String, Serializable>(3);
		Map<String, Serializable> mpSuper = super.getOptionalAttributes();
		if (mpSuper != null && ! mpSuper.isEmpty()) {
			mpRes.putAll(mpSuper);
		} else {
			String taskId = String.valueOf(getWorkflowTaskId());
			Map<String, Serializable> mpCon = getWorkflowContext(taskId);
			if (mpCon != null && ! mpCon.isEmpty()) {
				// Store deleteReason in optionalAttributes if is set
				if (deleteReason != null) {
					mpCon.put("deleteReason", deleteReason);
				}
				super.setOptionalAttributes(mpCon);
				mpRes.putAll(mpCon);
			}
		}

		return mpRes;
	}

	private Map<String, Serializable> getWorkflowContext(String taskId) {
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

	/**
	 * Notes: {@link net.emforge.activiti.task.SaveTransitionListener SaveTransitionListener} sets {@code taskOutputTransition}
	 */
	@Override
	public String getOutputTransition() {
		String taskId = String.valueOf(getWorkflowTaskId());
		Map<String, Object> mpVars = taskService.getVariablesLocal(taskId);
		return (String) mpVars.get(WorkflowConstants.NAME_TASK_OUTPUT_TRANSITION);
	}

	/** The reason why this task was deleted {'completed' | 'deleted' | any other user defined string }. */
	public void setDeleteReason(String deleteReason) {
		this.deleteReason = deleteReason;
	}

	public String getDeleteReason() {
		return deleteReason;
	}

	@Override
	public String toString() {
		return "WorkflowTaskImpl [" 
				+ "taskId=" + getWorkflowTaskId()
				+ ", name=" + getName()
				+ ", createDate=" + getCreateDate()
				+ ((isCompleted())? ", completionDate=" + getCompletionDate() + "(" + getDeleteReason() + ")" : "")
				+ ((getDueDate() != null)? ", dueDate=" + getDueDate()  : "")
				+ ", workflowDefinition=" + getWorkflowDefinitionName() + ":" + getWorkflowDefinitionVersion() + ":" + getWorkflowDefinitionId() 
				+ ", workflowInstanceId=" + getWorkflowInstanceId() 
				+ ((getAssigneeUserId() != -1)? ", assigneeUserId=" + getAssigneeUserId() : "")
				//+ ((!getWorkflowTaskAssignees().isEmpty())? ", workflowTaskAssignees=" + getWorkflowTaskAssignees() : "")
				+ "]";
	}
}
