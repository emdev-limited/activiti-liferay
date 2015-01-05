package net.emforge.activiti.workflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.emforge.activiti.IdMappingService;
import net.emforge.activiti.PortletPropsValues;
import net.emforge.activiti.WorkflowConstants;
import net.emforge.activiti.WorkflowUtil;
import net.emforge.activiti.spring.ApplicationContextProvider;

import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;

import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.workflow.DefaultWorkflowInstance;
import com.liferay.portal.kernel.workflow.WorkflowException;
import com.liferay.portal.kernel.workflow.WorkflowInstance;
import com.liferay.portal.kernel.workflow.WorkflowTask;
import com.liferay.portal.kernel.workflow.WorkflowTaskManagerUtil;

public class WorkflowInstanceImpl extends DefaultWorkflowInstance {
	private static final long serialVersionUID = 1L;

	private static Log _log = LogFactoryUtil.getLog(WorkflowInstanceImpl.class);
	
	IdMappingService idMappingService = (IdMappingService) ApplicationContextProvider.getApplicationContext().getBean("idMappingService");
	private HistoryService historyService = ProcessEngines.getDefaultProcessEngine().getHistoryService();
	private RuntimeService runtimeService = ProcessEngines.getDefaultProcessEngine().getRuntimeService();
//	private WorkflowDefinitionManagerImpl workflowDefinitionManager = (WorkflowDefinitionManagerImpl)WorkflowDefinitionManagerUtil.getWorkflowDefinitionManager();

	private long companyId = 0;
	private String superProcessInstanceId = null;
	private String superExecutionId = null;

	@Override
	public Map<String, Serializable> getWorkflowContext() {
		if (super.getWorkflowContext() != null)  {
			return super.getWorkflowContext();
		}
		return getWorkflowContext(getWorkflowInstanceId());
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

	/**
	 * Actually not all process instance requests needs to know process state
	 */
	@Override
	public String getState() {
		String state = super.getState();
		if (state != null) {
			return state;
		}
		
		state = StringPool.BLANK;
		try {
			state = getInstanceStates(companyId, getWorkflowInstanceId());
			super.setState(state);
		} catch (WorkflowException e) {
			_log.error(e);
		}
		return state;
	}
	
	protected String getInstanceStates(long companyId, Long processInstanceId) throws WorkflowException {
		// use portlet.properties settings to use different strategy
		if (PortletPropsValues.PROCESS_STATE_STRATEGY.equals(WorkflowConstants.PROCESS_STATE_STRATEGY_USERTASKS)) {
			//Find all active wait states for process regardless of user and set comma separated values
			List<WorkflowTask> activeTasks = WorkflowTaskManagerUtil.getWorkflowTasksByWorkflowInstance(companyId, null
					, processInstanceId, false, QueryUtil.ALL_POS, QueryUtil.ALL_POS, null);
			if (activeTasks == null || activeTasks.isEmpty()) {
				return StringPool.BLANK;
			}
			List<String> statesList = new ArrayList<String>();
			if (activeTasks != null && activeTasks.size() > 0) {
				for (WorkflowTask task : activeTasks) {
					statesList.add(task.getName());
				}
			}
			return StringUtil.merge(statesList);
		} else {
			List<String> activities = runtimeService.getActiveActivityIds(String.valueOf(processInstanceId));
			if (CollectionUtils.isEmpty(activities)) {
				return StringPool.BLANK;
			}
			// TODO: use activity names
			return StringUtil.merge(activities);
		}
	}

	public void setCompanyId(long companyId) {
		this.companyId  = companyId;
	}

	@Override
	public int getChildrenWorkflowInstanceCount() {
		String procInstanceId = String.valueOf(getWorkflowInstanceId());
		Long count = 0L;
		if (!isComplete()) {
			count = runtimeService.createProcessInstanceQuery().superProcessInstanceId(procInstanceId).count();
		} else {
			//count = historyService.createHistoricProcessInstanceQuery().superProcessInstanceId(procInstanceId).count();
		}
		return count.intValue();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<WorkflowInstance> getChildrenWorkflowInstances() {
		String procInstanceId = String.valueOf(getWorkflowInstanceId());
		
		// check is process instance completed
		if (!isComplete()) {
			List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().superProcessInstanceId(procInstanceId).list();
			// What if... WorkflowInstanceManager workflowInstanceManager = WorkflowInstanceManagerUtil.getWorkflowInstanceManager();
			WorkflowInstanceManagerImpl workflowInstanceManager = (WorkflowInstanceManagerImpl) ApplicationContextProvider.getApplicationContext().getBean("workflowInstanceManager");
			List<WorkflowInstance> workflowInstances = new ArrayList<WorkflowInstance>();
			for (ProcessInstance processInstance : processInstances) {
				try {
					WorkflowInstance workflowInstance = workflowInstanceManager.getWorkflowInstance(processInstance);
					workflowInstances.add(workflowInstance);
				} catch (WorkflowException e) {
					_log.error(e);
				}
			}
			return workflowInstances;
		} else {
			// TODO: Do we have to return children instances and instance count for completed processes?
			return ListUtils.EMPTY_LIST;
		}
	}

	public void setSuperProcessInstanceId(String superProcessInstanceId) {
		this.superProcessInstanceId = superProcessInstanceId;
	}

	public void setSuperExecutionId(String superExecutionId) {
		this.superExecutionId = superExecutionId;
	}

	@Override
	public String toString() {
		return "WorkflowInstanceImpl ["
				+ "workflowInstanceId=" + getWorkflowInstanceId() + ", "
				+ "superProcessInstanceId=" + superProcessInstanceId + ", "
				+ "superExecutionId=" + superExecutionId + ", "
				+ "workflowDefinitionName=" + getWorkflowDefinitionName() + ", "
				+ "workflowDefinitionVersion=" + getWorkflowDefinitionVersion() + ", "
				+ "startDate=" + getStartDate() + ", "
				+ "isComplete=" + isComplete() + " "
				+ "endDate=" + getEndDate() + ", "
				+ "]";
	}
	
}
