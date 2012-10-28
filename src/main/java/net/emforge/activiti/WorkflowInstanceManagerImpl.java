package net.emforge.activiti;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.emforge.activiti.dao.ProcessInstanceExtensionDao;
import net.emforge.activiti.dao.WorkflowDefinitionExtensionDao;
import net.emforge.activiti.entity.ProcessInstanceExtensionImpl;
import net.emforge.activiti.entity.WorkflowDefinitionExtensionImpl;

import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.ReadOnlyProcessDefinition;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.workflow.DefaultWorkflowInstance;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.kernel.workflow.WorkflowException;
import com.liferay.portal.kernel.workflow.WorkflowInstance;

@Service("workflowInstanceManager")
public class WorkflowInstanceManagerImpl implements WorkflowInstanceManagerExt {
	private static Log _log = LogFactoryUtil.getLog(WorkflowInstanceManagerImpl.class);
	
	@Autowired
	ProcessEngine processEngine;
	
	@Autowired
	RuntimeService runtimeService;
	
	@Autowired
	HistoryService historyService;
	
	@Autowired
	WorkflowDefinitionExtensionDao workflowDefinitionExtensionDao;
	
	@Autowired
	WorkflowDefinitionManagerExt workflowDefinitionManager;
	
	@Autowired
	ProcessInstanceExtensionDao processInstanceExtensionDao;
	
	@Autowired
	IdMappingService idMappingService;
    @Autowired
    RepositoryService repositoryService;

	
    @Transactional	
    @Override
	public void deleteWorkflowInstance(long companyId, long workflowInstanceId) throws WorkflowException {
		String processInstanceId = idMappingService.getActivitiProcessInstanceId(workflowInstanceId);
		_log.info("Deleting process instance " + processInstanceId);
		    
		ProcessInstanceQuery processInstanceQuery = runtimeService.createProcessInstanceQuery();
		processInstanceQuery.processInstanceId(processInstanceId);
		List<ProcessInstance> processInstanceList = processInstanceQuery.list();
		if (processInstanceList != null && !processInstanceList.isEmpty()) {
			runtimeService.deleteProcessInstance(processInstanceId, "cancelled");
		}
	}

	@Override
	public List<String> getNextTransitionNames(long companyId, long userId,
											   long workflowInstanceId) throws WorkflowException {
		_log.error("Method is not implemented"); // TODO
		return null;
	}

	/** Get process instance by ID
	 */
	@Override
	public WorkflowInstance getWorkflowInstance(long companyId, long workflowInstanceId) throws WorkflowException {
		String procId = idMappingService.getActivitiProcessInstanceId(workflowInstanceId);
		if (procId == null) {
			procId = String.valueOf(workflowInstanceId);
		}
		ProcessInstance inst = runtimeService.createProcessInstanceQuery().processInstanceId(procId).singleResult();
		
		if (inst != null) {
			try {
				return getWorkflowInstance(inst, null, null);
			} catch (Exception ex) {
				_log.warn("cannot get workflow instance " + ex);
				_log.debug("cannot get workflow instance", ex);
				return null;
			}
		} else {
			_log.debug("Cannot find process instance with id: " + workflowInstanceId + "(" + procId + "). try to find in history");
			
			HistoricProcessInstance hpi = historyService.createHistoricProcessInstanceQuery().processInstanceId(procId).singleResult();
			
			if (hpi != null) {
				return getHistoryWorkflowInstance(hpi);
			} else {
				_log.error("Cannot find process instance with id: " + workflowInstanceId + "(" + procId + ")");
				return null;
			}
		} 
		
		
	}

	@Override
	public int getWorkflowInstanceCount(long companyId,
			String workflowDefinitionName, Integer workflowDefinitionVersion,
			Boolean completed) throws WorkflowException {
		WorkflowDefinitionExtensionImpl def = workflowDefinitionExtensionDao.find(companyId, workflowDefinitionName, workflowDefinitionVersion);
		
		ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
		query = query.processDefinitionId(def.getProcessDefinitionId());

		/* TODO
		if (completed) {
			query = query.suspended();
		} else {
			query = query.notSuspended();
		}
		*/
	
		return Long.valueOf(query.count()).intValue();
	}

	/** Get process instances
	 * 
	 * TODO support sorting
	 */
	@Override
	public List<WorkflowInstance> getWorkflowInstances(long companyId,
			String workflowDefinitionName, Integer workflowDefinitionVersion,
			Boolean completed, int start, int end,
			OrderByComparator orderByComparator) throws WorkflowException {
		WorkflowDefinitionExtensionImpl def = workflowDefinitionExtensionDao.find(companyId, workflowDefinitionName, workflowDefinitionVersion);
		
		ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
		query = query.processDefinitionId(def.getProcessDefinitionId());

		/* TODO
		if (completed) {
			query = query.suspended();
		} else {
			query = query.notSuspended();
		}
		*/
		
		if ((start != QueryUtil.ALL_POS) && (end != QueryUtil.ALL_POS)) {
			query.listPage(start, end - start);
		}
		
		
		List<ProcessInstance> insts = query.list();
		List<WorkflowInstance> result = new ArrayList<WorkflowInstance>(insts.size());
		
		for (ProcessInstance inst : insts) {
			result.add(getWorkflowInstance(inst, null, null));
		}
		
		return result;
	}

	@Transactional	
	@Override
	public WorkflowInstance signalWorkflowInstance(long companyId, long userId,
												   long workflowInstanceId, String transitionName,
												   Map<String, Serializable> context) throws WorkflowException {
		processEngine.getIdentityService().setAuthenticatedUserId(idMappingService.getUserName(userId));
		
		Map<String, Object> vars = WorkflowInstanceManagerImpl.convertFromContext(context);
		vars.put("outputTransition", transitionName); // Put transition name into outputTransition variable for later use in gateway
		
		//Map<String, Object> vars = convertFromContext(context);
		
		// put outputTransition into context of workflow instance
		runtimeService.setVariable(idMappingService.getActivitiProcessInstanceId(workflowInstanceId), "outputTransition", transitionName);
		
		// TODO support context
		runtimeService.signal(idMappingService.getActivitiProcessInstanceId(workflowInstanceId));
		return null;
	}

	@Transactional	
	@Override
	public WorkflowInstance startWorkflowInstance(long companyId, long groupId, long userId, 
												  String workflowDefinitionName, Integer workflowDefinitionVersion, String transitionName,
												  Map<String, Serializable> workflowContext) throws WorkflowException {
		_log.info("Start workflow instance " + workflowDefinitionName + " : " + workflowDefinitionVersion);
		
		processEngine.getIdentityService().setAuthenticatedUserId(idMappingService.getUserName(userId));
		
		WorkflowDefinitionExtensionImpl def = workflowDefinitionExtensionDao.find(companyId, workflowDefinitionName, workflowDefinitionVersion);
		
		if (def == null) {
			_log.error("Cannot find workflow definition " + workflowDefinitionName + " : " + workflowDefinitionVersion);
			throw new WorkflowException("Cannot find workflow definition " + workflowDefinitionName + " : " + workflowDefinitionVersion);
		}
		
		Map<String, Object> vars = convertFromContext(workflowContext);
		
        ProcessInstance processInstance = runtimeService.startProcessInstanceById(def.getProcessDefinitionId(), vars);
        
        try {
	        DefaultWorkflowInstance inst = getWorkflowInstance(processInstance, userId, workflowContext);
			
	        return inst;
        } catch (Exception ex) {
        	// it may happens we cannot get workflow instance since it is already completed - just ignore this error and return null
        	_log.debug("Cannot get workflow instance: " + ex.getMessage());
        	return null;
        }
	}

	@Override
	public WorkflowInstance updateWorkflowContext(long companyId, long workflowInstanceId, Map<String, Serializable> workflowContext) throws WorkflowException {
		String processInstanceId = idMappingService.getActivitiProcessInstanceId(workflowInstanceId);
		
		for (String key : workflowContext.keySet()) {
			runtimeService.setVariable(processInstanceId, key, workflowContext.get(key));
		}
		
		ProcessInstance inst = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
		
		return getWorkflowInstance(inst, null, workflowContext);
	}

	public static Map<String, Serializable> convertFromVars(Map<String, Object> variables) {
			if (variables == null) {
				return new HashMap<String, Serializable>();
			}

			Map<String, Serializable> workflowContext = new HashMap<String, Serializable>();

			for (Map.Entry<String, Object> entry : variables.entrySet()) {
				workflowContext.put(entry.getKey(), (Serializable)entry.getValue());
			}

			return workflowContext;
		}

	public static Map<String, Object> convertFromContext(Map<String, Serializable> variables) {
		if (variables == null) {
			return new HashMap<String, Object>();
		}

		Map<String, Object> workflowContext = new HashMap<String, Object>();

		for (Map.Entry<String, Serializable> entry : variables.entrySet()) {
			workflowContext.put(entry.getKey(), entry.getValue());
		}

		return workflowContext;
	}

	@Override
	public int getWorkflowInstanceCount(long companyId, Long userId,
			String assetClassName, Long assetClassPK, Boolean completed)
			throws WorkflowException {
		int count = processInstanceExtensionDao.count(companyId, userId, assetClassName, assetClassPK, completed);
		return count;
	}
	
	/**
	 * added by Maxx
	 */
	@Override
	public int getWorkflowInstanceCount(long companyId, Long userId,
			String[] assetClassNames, Boolean completed)
			throws WorkflowException {
		int count = 0;
		// TODO: better solution - modify metod processInstanceExtensionDao.count(...)
		for(String assetClassName : assetClassNames)
			count += processInstanceExtensionDao.count(companyId, userId, assetClassName, null, completed);
		return count;
	}

	@Override
	public List<WorkflowInstance> getWorkflowInstances(long companyId, Long userId, 
													   String assetClassName, Long assetClassPK,
													   Boolean completed, int start, int end,
													   OrderByComparator orderByComparator) throws WorkflowException {
		List<ProcessInstanceExtensionImpl> procInstances = processInstanceExtensionDao.find(companyId, userId, assetClassName, assetClassPK, completed, start, end, orderByComparator);
		List<WorkflowInstance> result = new ArrayList<WorkflowInstance>();
		
		for (ProcessInstanceExtensionImpl processInstance : procInstances) {
			WorkflowInstance workflowInstance = getWorkflowInstance(processInstance);
			
			result.add(workflowInstance);
		}
		
		return result;
	}
	
	/**
	 * added by Maxx
	 */
	@Override
	public List<WorkflowInstance> getWorkflowInstances(long companyId,
			Long userId, String[] assetClassNames, Boolean completed,
			int start, int end, OrderByComparator orderByComparator)
			throws WorkflowException {
		List<WorkflowInstance> result = new ArrayList<WorkflowInstance>();
		// TODO: better solution - modify metod processInstanceExtensionDao.find(...)
		for(String assetClassName : assetClassNames)
			result.addAll(getWorkflowInstances(companyId, userId, assetClassName, null, completed, start, end, orderByComparator));
		return result;
	}

	@Override
	public DefaultWorkflowInstance getWorkflowInstance(Execution processInstance, Long userId, Map<String, Serializable> currentWorkflowContext) throws WorkflowException {
		return getWorkflowInstance(processInstance.getId(), userId, currentWorkflowContext);
	}
	
	
	public DefaultWorkflowInstance getWorkflowInstance(String executionId, Long userId, Map<String, Serializable> currentWorkflowContext) throws WorkflowException {
        HistoricProcessInstance historyPI =  historyService.createHistoricProcessInstanceQuery().processInstanceId(executionId).singleResult();
        ProcessDefinition procDef = workflowDefinitionManager.getProcessDefinition(historyPI.getProcessDefinitionId());
        
        DefaultWorkflowInstance inst = new DefaultWorkflowInstance();
        
        inst.setEndDate(historyPI.getEndTime());
        inst.setStartDate(historyPI.getStartTime());
		inst.setWorkflowDefinitionName(procDef.getName());
		inst.setWorkflowDefinitionVersion(procDef.getVersion());

        // get activities and variables only in case process is active
        boolean activeProcess = true;
        try {
        	Execution execution = runtimeService.createExecutionQuery().executionId(executionId).singleResult();
        	if (execution == null) {
        		activeProcess = false;
        	}
        } catch (Exception ex) {
        	activeProcess = false;
        }
        
        if (activeProcess) {
	        List<String> activities = new ArrayList<String>();
	        try {
	        	activities = runtimeService.getActiveActivityIds(executionId);
	        } catch (Exception ex) {
	        	// in case then process has no user tasks - process may be finished just after it is started
	        	// so - we will not have active activities here.
	        	_log.debug("Error during getting active activities", ex);
	        }
	
	        // activities contains internal ids - need to be converted into names
			List<String> activityNames = new ArrayList<String>(activities.size());
	        ReadOnlyProcessDefinition readOnlyProcessDefinition = ((RepositoryServiceImpl)repositoryService).getDeployedProcessDefinition(procDef.getId());
			
			for (String activiti: activities) {
				PvmActivity findActivity = readOnlyProcessDefinition.findActivity(activiti);
				if (findActivity != null)
					activityNames.add(findActivity.getProperty("name").toString());
			}
			inst.setState(StringUtils.join(activityNames, ","));
        }
        
        Map<String, Serializable> workflowContext = null;
        
        // copy variables
        if (activeProcess) {
	        try {
	        	Map<String, Object> vars = runtimeService.getVariables(executionId);
	        	workflowContext = convertFromVars(vars);
	        } catch (Exception ex) {
	        	// in case then process has no user tasks - process may be finished just after it is started
	        	// so - we will not have active activities here.
	        	_log.debug("Error during getting context vars", ex);
	        	workflowContext = currentWorkflowContext;
	        }
        } else {
        	// process is not active - so, lets use initial currentWorkflowContext
        	workflowContext = currentWorkflowContext;
        }
        
		inst.setWorkflowContext(workflowContext);
    
		Long id = idMappingService.getLiferayProcessInstanceId(executionId);
		if (id == null) {
			// not exists in DB - create new
			if (workflowContext.get(WorkflowConstants.CONTEXT_COMPANY_ID) == null ||
					workflowContext.get(WorkflowConstants.CONTEXT_GROUP_ID) == null ||
					workflowContext.get(WorkflowConstants.CONTEXT_ENTRY_CLASS_NAME) == null ||
					workflowContext.get(WorkflowConstants.CONTEXT_ENTRY_CLASS_PK) == null) {
				// all workflows instances in Liferay should be related to some asset
				throw new WorkflowException("Process Instance has no asset attached");
			}
			ProcessInstanceExtensionImpl procInstImpl = new ProcessInstanceExtensionImpl();
			procInstImpl.setCompanyId(GetterUtil.getLong(workflowContext.get(WorkflowConstants.CONTEXT_COMPANY_ID)));
			procInstImpl.setGroupId(GetterUtil.getLong(workflowContext.get(WorkflowConstants.CONTEXT_GROUP_ID)));
			procInstImpl.setUserId(userId != null ? userId : 0l);
			procInstImpl.setClassName((String)workflowContext.get(WorkflowConstants.CONTEXT_ENTRY_CLASS_NAME));
			procInstImpl.setClassPK(GetterUtil.getLong(workflowContext.get(WorkflowConstants.CONTEXT_ENTRY_CLASS_PK)));
			procInstImpl.setProcessInstanceId(executionId);
			
			id = (Long)processInstanceExtensionDao.save(procInstImpl);
			
			_log.info("Stored new process instance ext " + executionId + " -> " + id);
		}
			
		inst.setWorkflowInstanceId(id);
	
		if (activeProcess) {
			//get children for it if any..
			List<ProcessInstance> children = runtimeService.createProcessInstanceQuery().superProcessInstanceId(executionId).list();
			if (children != null && !children.isEmpty()) {
				List<WorkflowInstance> childrenInst = new ArrayList<WorkflowInstance>();
				for (ProcessInstance procInst : children) {
					childrenInst.add(getWorkflowInstance(procInst, null, null));
				}
				inst.getChildrenWorkflowInstances().addAll(childrenInst);
			}
        }
        
		return inst;
	}
	
	private WorkflowInstance getHistoryWorkflowInstance(HistoricProcessInstance historyPI) {
		ProcessDefinition procDef =  workflowDefinitionManager.getProcessDefinition(historyPI.getProcessDefinitionId());
        
        DefaultWorkflowInstance inst = new DefaultWorkflowInstance();
        
        inst.setEndDate(historyPI.getEndTime());
        inst.setStartDate(historyPI.getStartTime());
        // TODO
        inst.setState("");

        // get basic variables from ext object
        ProcessInstanceExtensionImpl procInstExt = processInstanceExtensionDao.findByProcessInstanceId(historyPI.getId());
        Map<String, Serializable> workflowContext = getWorkflowContext(procInstExt);
		inst.setWorkflowContext(workflowContext);
		
		inst.setWorkflowInstanceId(procInstExt.getId());
		
		inst.setWorkflowDefinitionName(procDef.getName());
		inst.setWorkflowDefinitionVersion(procDef.getVersion());
        
		return inst;
	}

	private WorkflowInstance getWorkflowInstance(ProcessInstanceExtensionImpl processInstance) {
		DefaultWorkflowInstance workflowInstance = new DefaultWorkflowInstance();
		
		HistoricProcessInstance historyPI =  historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstance.getProcessInstanceId()).singleResult();
		
		workflowInstance.setWorkflowInstanceId(processInstance.getId());
		
        ProcessDefinition processDef =  workflowDefinitionManager.getProcessDefinition(historyPI.getProcessDefinitionId());
        
		workflowInstance.setWorkflowDefinitionName(processDef.getName());
		workflowInstance.setWorkflowDefinitionVersion(processDef.getVersion());
		
		workflowInstance.setStartDate(historyPI.getStartTime());
		workflowInstance.setEndDate(historyPI.getEndTime());
		
		if (historyPI.getEndTime() == null) {
			List<String> activities = runtimeService.getActiveActivityIds(processInstance.getProcessInstanceId());
			// activities contains internal ids - need to be converted into names
			List<String> activityNames = new ArrayList<String>(activities.size());
	        ReadOnlyProcessDefinition readOnlyProcessDefinition = ((RepositoryServiceImpl)repositoryService).getDeployedProcessDefinition(processDef.getId());
			
			for (String activiti: activities) {
				PvmActivity findActivity = readOnlyProcessDefinition.findActivity(activiti);
				if (findActivity != null)
					activityNames.add(findActivity.getProperty("name").toString());
			}
			workflowInstance.setState(StringUtils.join(activityNames, ","));
		
			Map<String, Object> vars = 	runtimeService.getVariables(processInstance.getProcessInstanceId());
	        Map<String, Serializable> workflowContext = convertFromVars(vars);
	        workflowInstance.setWorkflowContext(workflowContext);
		} else {
			workflowInstance.setState(historyPI.getEndActivityId());
			
			// for ended process isntance we can restore only limited set of workflow context
			workflowInstance.setWorkflowContext(getWorkflowContext(processInstance));
		}
		
		// Do we need it? private WorkflowInstance _parentWorkflowInstance;
		
		return workflowInstance;
	}
	
	private Map<String, Serializable> getWorkflowContext(ProcessInstanceExtensionImpl procInstExt) {
		Map<String, Serializable> workflowContext = new HashMap<String, Serializable>();
		
        workflowContext.put(WorkflowConstants.CONTEXT_COMPANY_ID, String.valueOf(procInstExt.getCompanyId()));
        workflowContext.put(WorkflowConstants.CONTEXT_GROUP_ID, String.valueOf(procInstExt.getGroupId()));
        workflowContext.put(WorkflowConstants.CONTEXT_ENTRY_CLASS_NAME, procInstExt.getClassName());
        workflowContext.put(WorkflowConstants.CONTEXT_ENTRY_CLASS_PK, String.valueOf(procInstExt.getClassPK()));
		
        return workflowContext;
	}
}
