package net.emforge.activiti.workflow;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.emforge.activiti.IdMappingService;
import net.emforge.activiti.comparator.WorkflowComparatorUtil;
import net.emforge.activiti.engine.impl.cmd.AddWorkflowLogEntryCmd;
import net.emforge.activiti.log.WorkflowLogEntry;
import net.emforge.activiti.query.CustomCommonProcessInstanceQuery;
import net.emforge.activiti.query.CustomHistoricProcessInstanceQuery;
import net.emforge.activiti.query.CustomHistoricProcessInstanceQueryImpl;
import net.emforge.activiti.query.CustomProcessInstanceQuery;
import net.emforge.activiti.query.CustomProcessInstanceQueryImpl;

import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.query.Query;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.workflow.WorkflowException;
import com.liferay.portal.kernel.workflow.WorkflowInstance;
import com.liferay.portal.kernel.workflow.WorkflowInstanceManager;
import com.liferay.portal.util.PortalUtil;

public abstract class AbstractWorkflowInstanceManager implements WorkflowInstanceManager {
	private static Log _log = LogFactoryUtil.getLog(AbstractWorkflowInstanceManager.class);

	@Autowired
	ProcessEngine processEngine;
	@Autowired
	RuntimeService runtimeService;
	@Autowired
	IdentityService identityService;
	@Autowired
	HistoryService historyService;
	@Autowired
	RepositoryService repositoryService;

	@Autowired
	IdMappingService idMappingService;

	/** 
	 * Add workflow log entry to process.
	 * 
	 * @param processInstance
	 * @param workflowLogEntry
	 */
	protected void addWorkflowLogEntryToProcess(String processInstanceId, WorkflowLogEntry workflowLogEntry) {
		CommandExecutor commandExecutor = ((ProcessEngineImpl) processEngine)
				.getProcessEngineConfiguration().getCommandExecutor();

		commandExecutor.execute(new AddWorkflowLogEntryCmd("", processInstanceId, workflowLogEntry));
	}

	private CustomProcessInstanceQuery createProcessInstanceQuery() throws WorkflowException {
		return CustomProcessInstanceQueryImpl.create();
	}

	private CustomHistoricProcessInstanceQuery createHistoricProcessInstanceQuery() throws WorkflowException {
		return CustomHistoricProcessInstanceQueryImpl.create();
	}

	protected Query<?, ?> createProcessInstanceQuery(long workflowInstanceId, boolean completed) throws WorkflowException {
		String processInstanceId = String.valueOf(workflowInstanceId);
		if (completed) {
			return createProcessInstanceQuery().processInstanceId(processInstanceId);
		} else {
			return createHistoricProcessInstanceQuery().processInstanceId(processInstanceId);
		}
	}

	/*protected Query<?, ?> createProcessInstanceQueryByDefinition(long companyId,
			String processDefinitionId,
			Boolean completed) {
		
		if (completed != null && completed == false) {
			// search in runtime
			ProcessInstanceQuery query = createProcessInstanceQuery();
			
			if (processDefinitionId != null) {
				query.processDefinitionId(processDefinitionId);
			}
			return query;
		} else {
			// search in history
			HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();
			query.finished();
			
			if (processDefinitionId != null) {
				query.processDefinitionId(processDefinitionId);
			}
			return query;
		}
	}*/

	protected Query<?, ?> createProcessInstanceQueryByDefinition(
			long companyId, String workflowDefinitionName,
			Integer workflowDefinitionVersion, Boolean completed) throws WorkflowException {

		if (completed != null && completed == false) {
			// search in runtime
			CustomProcessInstanceQuery query = createProcessInstanceQuery();

			if (workflowDefinitionName != null) {
				query.processDefinitionName(workflowDefinitionName);
				if (workflowDefinitionVersion != null && workflowDefinitionVersion == 0) {
					query.processDefinitionLatestVersion();
				} else {
					query.processDefinitionVersion(workflowDefinitionVersion);
				}
			}

			return query;
		} else {
			// search in history
			HistoricProcessInstanceQuery query = createHistoricProcessInstanceQuery();
			//query.excludeSubprocesses(true);
			query.finished();

			// TODO: may be definition key have to be used there like this one
			/**
			 * query.processDefinitionKey(workflowDefinitionName);
			 */
			if (workflowDefinitionName != null) {
				// instead of searching latest to define definition key:
				ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
						.processDefinitionName(workflowDefinitionName)
						.processDefinitionTenantId(String.valueOf(companyId))
						.latestVersion()
						.singleResult();
				if (processDefinition != null) {
					query.processDefinitionKey(processDefinition.getKey());
				}
			}
			return query;
		}
	}

	/**
	 * Creates process instance query by parameters.
	 * Some additional logic with parameters may be implemented there.
	 * 
	 * @param parameters Map with parameters
	 * @return Query
	 * @throws WorkflowException
	 */
	protected Query<?, ?> createProcessInstanceQueryByParametersMap(Map<String, Object> parameters) throws WorkflowException {
		Query<?, ?> query = null;

		Boolean completed = (Boolean) parameters.get("completed");

		// (*)
		if (completed != null && completed == false) {
			CustomProcessInstanceQuery runtimeQuery = createProcessInstanceQuery();
			query = runtimeQuery;
		} else {
			CustomHistoricProcessInstanceQuery historicQuery = createHistoricProcessInstanceQuery();
			if (completed != null) {
				if (completed) {
					historicQuery.finished();
				} else {
					// to search active instances in history first IF (*) should be removed
					historicQuery.unfinished();
				}
			}
			query = historicQuery;
		}

		// search by variables
		try {
			@SuppressWarnings("unchecked")
			Map<String, Object> variables = (Map<String, Object>) parameters.get("variables");
			if (variables != null) {
				for (String name : variables.keySet()) {
					((CustomCommonProcessInstanceQuery<?>)query).variableValueEquals(name, variables.get(name));
				}
			}
		} catch (Exception e) {
			_log.error(e);
		}

		// some additional parameters and logic can be added there

		return query;
	}

	protected Query<?, ?> createProcessInstanceQuerySearch(long companyId,
			Long userId, String[] assetClassNames, Long assetClassPK,
			Boolean completed) throws WorkflowException {
		Query<?, ?> query = null;

		// with common interface for active and historic instance queries is able to add common filters

		// (*)
		if (completed != null && completed == false) {
			// search in runtime: CustomProcessInstanceQuery
			query = createProcessInstanceQuery();
			// NOTES: but it is possible to do not search in runtime and search active instances in history too
		} else {
			// search in history: CustomHistoricProcessInstanceQuery
			query = createHistoricProcessInstanceQuery();
			if (completed != null) {
				if (completed) {
					((CustomHistoricProcessInstanceQuery)query).finished();
				} else {
					// to search active instances in history first IF (*) should be removed
					((CustomHistoricProcessInstanceQuery)query).unfinished();
				}
			}
		}

		if (companyId > 0) {
			// TODO: check if more than one company exist
			// if (WorkflowUtil.getCompaniesCount() > 1) {
			// }
			((CustomCommonProcessInstanceQuery<?>)query).processInstanceTenantId(String.valueOf(companyId));
		}

		if (userId != null) {
			((CustomCommonProcessInstanceQuery<?>)query).userId(userId);
		}

		if (assetClassNames != null) {
			/**
			 * Entry class names may be defined by workflow handler:
			 * 
			 * List<String> entryClassNames = new ArrayList<String>();
			 * for (String assetType : assetTypes) {
			 * 	entryClassNames.add(WorkflowUtil.getAssetClassName(assetType));
			 * }
			 */
			List<Long> entryClassNameIds = new ArrayList<Long>();
			for (String assetClassName : assetClassNames) {
				entryClassNameIds.add(PortalUtil.getClassNameId(assetClassName));
			}
			if (entryClassNameIds.size() == 1) {
				((CustomCommonProcessInstanceQuery<?>)query).entryClassNameId(entryClassNameIds.get(0));
			} else {
				((CustomCommonProcessInstanceQuery<?>)query).entryClassNameIds(entryClassNameIds);
			}
		}

		if (assetClassPK != null) {
			((CustomCommonProcessInstanceQuery<?>)query).entryClassPK(assetClassPK);
		}

		// Notes: do we have to exclude subprocesses when join WorkflowInstanceLink?
		// query.excludeSubprocesses(true);

		return query;
	}

	/**
	 * <p>
	 * Retrieves workflow instance paged list.
	 * <p>
	 * See
	 * {@link net.emforge.activiti.comparator.WorkflowComparatorUtil#applyComparator(Query, OrderByComparator)
	 * WorkflowComparatorUtil.applyComparator()} to use
	 * {@link OrderByComparator}.
	 * 
	 * @param query
	 * @param start
	 * @param end
	 * @param orderByComparator
	 *            {@link com.liferay.portal.kernel.util.OrderByComparator
	 *            OrderByComparator}
	 * @return
	 */
	protected List<WorkflowInstance> getWorkflowInstances(
			Query<?, ?> query, int start, int end,
			OrderByComparator orderByComparator) {
		
		// apply comparator to query
		WorkflowComparatorUtil.applyComparator(query, orderByComparator);
		
		return getWorkflowInstances(query, start, end);
	}
	
	protected List<WorkflowInstance> getWorkflowInstances(Query<?, ?> query, int start, int end) {
		List<?> instanceList = null;
		if ((start != QueryUtil.ALL_POS) && (end != QueryUtil.ALL_POS)) {
			instanceList = query.listPage(start, end - start);
		} else {
			instanceList = query.list();
		}
		
		List<WorkflowInstance> result = new ArrayList<WorkflowInstance>(instanceList.size());
		
		boolean isHistoric = query instanceof HistoricProcessInstanceQuery;
		Method method = null;
		try {
			if (isHistoric) {
				method = getClass().getSuperclass().getDeclaredMethod("getHistoryWorkflowInstance", HistoricProcessInstance.class);
			} else {
				method = getClass().getSuperclass().getDeclaredMethod("getWorkflowInstance", ProcessInstance.class);
			}
		} catch (NoSuchMethodException e) {
		}
		
		for (Object instance : instanceList) {
			try {
				WorkflowInstance workflowInstance = (WorkflowInstance) method.invoke(this, instance);
				result.add(workflowInstance);
			} catch (Exception ex) {
				String id = (isHistoric) ? ((HistoricProcessInstance)instance).getId() : ((ProcessInstance)instance).getId();
				_log.warn("Cannot convert Activiti " + ((isHistoric)? "historic" : "runtime") + " process " + id + " into Liferay: " + ex);
				_log.debug("Cannot convert Activiti process into Liferay", ex);
			}
		}
		
		return result;
	}
	
	protected WorkflowInstance getWorkflowInstance(Query<?, ?> query) {
		Object instance = query.singleResult();
		
		if (instance == null) {
			return null;
		}
		
		boolean isHistoric = query instanceof HistoricProcessInstanceQuery;
		Method method = null;
		try {
			if (isHistoric) {
				method = getClass().getSuperclass().getDeclaredMethod("getHistoryWorkflowInstance", HistoricProcessInstance.class);
			} else {
				method = getClass().getSuperclass().getDeclaredMethod("getWorkflowInstance", ProcessInstance.class);
			}
		} catch (NoSuchMethodException e) {
		}
		
		try {
			WorkflowInstance workflowInstance = (WorkflowInstance) method.invoke(this, instance);
			return workflowInstance;
		} catch (Exception ex) {
			String id = (isHistoric) ? ((HistoricProcessInstance)instance).getId() : ((ProcessInstance)instance).getId();
			_log.warn("Cannot convert Activiti " + ((isHistoric)? "historic" : "runtime") + " process " + id + " into Liferay: " + ex);
			_log.debug("Cannot convert Activiti task into Liferay", ex);
		}
		
		return null;
	}
	
	/** Convert Activiti historic process instance to Liferay WorkflowInstance
	 * 
	 * @param processInstance
	 * @return
	 */
	protected WorkflowInstance getHistoryWorkflowInstance(HistoricProcessInstance processInstance) throws WorkflowException {
		ProcessDefinition processDefinition = repositoryService.getProcessDefinition(processInstance.getProcessDefinitionId());

		WorkflowInstanceImpl workflowInstance = new WorkflowInstanceImpl();

		// we could use long - see DbIdGenerator
		workflowInstance.setWorkflowInstanceId(Long.valueOf(processInstance.getId()));
		workflowInstance.setSuperProcessInstanceId(processInstance.getSuperProcessInstanceId());

		workflowInstance.setEndDate(processInstance.getEndTime());
        workflowInstance.setStartDate(processInstance.getStartTime());
		
		workflowInstance.setWorkflowDefinitionName(processDefinition.getName());
        workflowInstance.setWorkflowDefinitionVersion(processDefinition.getVersion());
        
//        long companyId = getProcessCompanyId(processInstance.getId(), true);
        String tenantId = processInstance.getTenantId();
        if (StringUtils.isNotEmpty(tenantId)) {
			workflowInstance.setCompanyId(Long.valueOf(tenantId));
        }		
		workflowInstance.setState("completed");
		
		return workflowInstance;
	}

	/*@SuppressWarnings("unused")
	private WorkflowTask getWorkflowTask(TaskInfo taskInfo) throws WorkflowException {
		return getWorkflowTask((Task)taskInfo);
	}*/

	/** Convert Activiti process instance to Liferay WorkflowInstance
	 * 
	 * @param processInstance
	 * @return
	 */
	public WorkflowInstance getWorkflowInstance(ProcessInstance processInstance) throws WorkflowException {
		try{
			HistoricProcessInstance historicProcessInstance =  historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult();
			ProcessDefinition processDefinition = repositoryService.getProcessDefinition(processInstance.getProcessDefinitionId());

			WorkflowInstanceImpl workflowInstance = new WorkflowInstanceImpl();

			// could do it safety - see DbIdGenerator
			workflowInstance.setWorkflowInstanceId(Long.valueOf(processInstance.getProcessInstanceId()));
			workflowInstance.setSuperProcessInstanceId(historicProcessInstance.getSuperProcessInstanceId());
			workflowInstance.setSuperExecutionId(((ExecutionEntity)processInstance).getSuperExecutionId());

			workflowInstance.setEndDate(historicProcessInstance.getEndTime());
			workflowInstance.setStartDate(historicProcessInstance.getStartTime());

			workflowInstance.setWorkflowDefinitionName(processDefinition.getName());
			workflowInstance.setWorkflowDefinitionVersion(processDefinition.getVersion());

//			long companyId = getProcessCompanyId(processInstance.getProcessInstanceId(), false);
			String tenantId = processInstance.getTenantId();
			if (StringUtils.isNotEmpty(tenantId)) {
				workflowInstance.setCompanyId(Long.valueOf(tenantId));
			}
			return workflowInstance;
		} catch(Exception e) {
			_log.error("getWorkflowInstance FAILED: " + processInstance, e);
			return null;
		}
	}

	protected List<String> getActiveSubProcessInstanceIds(String processInstanceId) throws WorkflowException {
		// go through subprocesses
		ProcessInstanceQuery processQuery = createProcessInstanceQuery();
		processQuery.active().superProcessInstanceId(processInstanceId);

		List<String> processIds = new ArrayList<String>();
		processIds.add(processInstanceId);

		for (ProcessInstance subProcess : processQuery.list()) {
			List<String> subProcessIds = getActiveSubProcessInstanceIds(subProcess.getProcessInstanceId());
			processIds.addAll(subProcessIds);
		}

		return processIds;
		//historyService.createHistoricProcessInstanceQuery().superProcessInstanceId(superProcessInstanceId)
	}

	protected List<Execution> getTreeExecutions(String processInstanceId, String transitionName) {		
		List<Execution> executions = runtimeService.createExecutionQuery().signalEventSubscriptionName(transitionName).processInstanceId(processInstanceId).list();
		List<ProcessInstance> subProcessInstances = getSubProcessInstances(processInstanceId);
		for (ProcessInstance subProcessInstance : subProcessInstances) {
			executions.addAll(runtimeService.createExecutionQuery().signalEventSubscriptionName(transitionName).processInstanceId(subProcessInstance.getProcessInstanceId()).list());
		}
		return executions;	
	}

	private List<ProcessInstance> getSubProcessInstances(String processInstanceId) {
		List<ProcessInstance> subProcessInstances = runtimeService.createProcessInstanceQuery().superProcessInstanceId(processInstanceId).list();
		int size = subProcessInstances.size();
		for (int i = 0; i < size; ++i) {
			ProcessInstance subProcessInstance = subProcessInstances.get(i);
			subProcessInstances.addAll(getSubProcessInstances(subProcessInstance.getId()));			
		}
		return subProcessInstances;
	}
	
	// TODO: test it with multiinstance and subprocesses
	// use tenantId as companyId
	@Deprecated
	private long getProcessCompanyId(String processInstanceId, boolean completed) {
		long companyId = 0l;
		Map<String, Object> processVariables = new HashMap<String, Object>();
		try {
			if (completed) {
				processVariables = createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).includeProcessVariables().singleResult().getProcessVariables();
			} else {
				processVariables = createProcessInstanceQuery().processInstanceId(processInstanceId).includeProcessVariables().singleResult().getProcessVariables();
			}
			companyId = GetterUtil.getLong(processVariables.get("companyId"), 0);
		} catch(Exception e) {
		}
		return companyId;
		/*List<Execution> executions = runtimeService.createExecutionQuery().processInstanceId(processInstanceId).list();
		Execution execution = null;
		if (executions.size() == 1) {
			execution = executions.get(0);
		} else {
			//find execution where parent link is null - this is the main
			for (Execution ex : executions) {
				ExecutionEntity exEntity = (ExecutionEntity) ex;
				if (exEntity.getParentId() == null) {
					execution = ex;
					break;
				}
			}
		}
		if (execution == null) {
			return null;
		}
		return execution.getId();*/
	}
	
	/*private long getProcessCompanyId(String processInstanceId, boolean completed) {
		long companyId = 0l;
		try {
			Map currentWorkflowContext = null;
			if (currentWorkflowContext == null) {
				Map<String,Object> workflowContext = new HashMap<String,Object>();
				String executionId = getProcessExecutionId(processInstanceId, completed);
				workflowContext = runtimeService.getVariablesLocal(executionId);
				companyId = GetterUtil.getLong(workflowContext.get("companyId"), 0);
			} else {
				companyId = GetterUtil.getLong(currentWorkflowContext.get("companyId"), 0);
			}
		} catch (Exception e) {
			_log.error(e);
		}
		return companyId;
	}*/
}
