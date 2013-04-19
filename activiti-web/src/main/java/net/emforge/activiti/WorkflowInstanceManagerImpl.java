package net.emforge.activiti;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.liferay.portal.kernel.dao.orm.Disjunction;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQueryFactoryUtil;
import com.liferay.portal.kernel.dao.orm.ProjectionFactoryUtil;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.PortalClassLoaderUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.model.WorkflowInstanceLink;
import com.liferay.portal.service.WorkflowInstanceLinkLocalServiceUtil;
import com.liferay.portal.util.PortalUtil;
import net.emforge.activiti.dao.WorkflowDefinitionExtensionDao;
import net.emforge.activiti.engine.LiferayTaskService;
import net.emforge.activiti.log.WorkflowLogEntry;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiOptimisticLockingException;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.ReadOnlyProcessDefinition;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.workflow.WorkflowException;
import com.liferay.portal.kernel.workflow.WorkflowInstance;
import com.liferay.portal.kernel.workflow.WorkflowInstanceManager;
import com.liferay.portal.kernel.workflow.WorkflowLog;

@Service(value="workflowInstanceManager")
public class WorkflowInstanceManagerImpl implements WorkflowInstanceManager {
	private static Log _log = LogFactoryUtil.getLog(WorkflowInstanceManagerImpl.class);

	@Autowired
	ProcessEngine processEngine;
	@Autowired
	RuntimeService runtimeService;
	@Autowired
	HistoryService historyService;
	@Autowired
	WorkflowDefinitionManagerImpl workflowDefinitionManager;
	@Autowired
	LiferayTaskService liferayTaskService;

	@Autowired
	WorkflowDefinitionExtensionDao workflowDefinitionExtensionDao;
	@Autowired
	IdMappingService idMappingService;
    @Autowired
    RepositoryService repositoryService;

	@Override
	public void deleteWorkflowInstance(long companyId, long workflowInstanceId) throws WorkflowException {
		String processInstanceId = String.valueOf(workflowInstanceId);
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
		String procId = String.valueOf(workflowInstanceId);
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
		ProcessDefinition def = workflowDefinitionExtensionDao.find(companyId, workflowDefinitionName, workflowDefinitionVersion);

		ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
		query = query.processDefinitionId(def.getId());

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
		_log.info("Get process instances 3");
		ProcessDefinition def = workflowDefinitionExtensionDao.find(companyId, workflowDefinitionName, workflowDefinitionVersion);

		ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
		query = query.processDefinitionId(def.getId());

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

	@Override
	public WorkflowInstance signalWorkflowInstance(long companyId, long userId,
												   long workflowInstanceId, String transitionName,
												   Map<String, Serializable> context) throws WorkflowException {
		processEngine.getIdentityService().setAuthenticatedUserId(idMappingService.getUserName(userId));
		String processInstanceId = String.valueOf(workflowInstanceId);

		Map<String, Object> vars = convertFromContext(context);

		if (vars.containsKey("messageEventReceived")) {
			vars.remove("messageEventReceived");
			List<Execution> executions = runtimeService.createExecutionQuery()
				      .processInstanceId(processInstanceId)
				      .messageEventSubscriptionName(transitionName)
				      .list();
			for (Execution execution : executions) {
				_log.info("Message event received: " + transitionName + ", executionId: " + execution.getId());
				runtimeService.messageEventReceived(transitionName, execution.getId(), vars);
			}
		} else if (vars.containsKey("startProcessByMessage")) {
			vars.remove("startProcessByMessage");
			List<Execution> executions = runtimeService.createExecutionQuery()
				      .processInstanceId(processInstanceId)
				      .list();
			for (Execution execution : executions) {
				_log.info("Signal startProcessByMessage received: " + transitionName + ", executionId: " + execution.getId());
				runtimeService.startProcessInstanceByMessage(transitionName, vars);
			}
		} else {
			_log.info("Prior to signal event received: " + transitionName);
			//List<Execution> executions = runtimeService.createExecutionQuery().signalEventSubscriptionName(transitionName).processInstanceId(processInstanceId).list();
			List<Execution> executions = getTreeExecutions(processInstanceId, transitionName);
			for (Execution execution : executions) {
				try {
					_log.info("Signal event received: " + transitionName + ", executionId: " + execution.getId());
					try {
						runtimeService.signalEventReceived(transitionName, execution.getId(), vars);
					} catch(ActivitiException ae) {
						_log.debug("executionId = " + execution.getId() + " not exist");
					}
				} catch (ActivitiOptimisticLockingException ae) {
					_log.warn(ae.getMessage() + ": " + ae.getCause());
				}
			}
		}
		
		try{
			int type = WorkflowLog.TASK_UPDATE;
			String comment = GetterUtil.getString(vars.get("comment"));
			if(comment == null || comment.isEmpty())
				comment = "signal-workflow-instance$" + transitionName;
			
//			LiferayTaskService liferayTaskService = (LiferayTaskService)processEngine.getTaskService();
			WorkflowLogEntry workflowLogEntry = new WorkflowLogEntry();
			workflowLogEntry.setType(type);
			workflowLogEntry.setComment(comment);
			workflowLogEntry.setPreviousUserId(userId);
			//liferayTaskService.addWorkflowLogEntry(task.getId(), task.getProcessInstanceId(), workflowLogEntry);
			//liferayTaskService.addWorkflowLogEntry(processInstanceId, processInstanceId, workflowLogEntry);
			liferayTaskService.addWorkflowLogEntry("", processInstanceId, workflowLogEntry);
		
		} catch(Exception e) { _log.error("add comment failed: " + e.getMessage()); }

		return null;
	}
	
	private List<Execution> getTreeExecutions(String processInstanceId, String transitionName) {		
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

	@Override
	public WorkflowInstance startWorkflowInstance(long companyId, long groupId, long userId,
												  String workflowDefinitionName, Integer workflowDefinitionVersion, String transitionName,
												  Map<String, Serializable> workflowContext) throws WorkflowException {
		_log.info("Start workflow instance " + workflowDefinitionName + " : " + workflowDefinitionVersion);

        processEngine.getIdentityService().setAuthenticatedUserId(idMappingService.getUserName(userId));

		ProcessDefinition def = workflowDefinitionExtensionDao.find(companyId, workflowDefinitionName, workflowDefinitionVersion);

		if (def == null) {
			_log.error("Cannot find workflow definition " + workflowDefinitionName + " : " + workflowDefinitionVersion);
			throw new WorkflowException("Cannot find workflow definition " + workflowDefinitionName + " : " + workflowDefinitionVersion);
		}

		Map<String, Object> vars = convertFromContext(workflowContext);

        ProcessInstance processInstance = runtimeService.startProcessInstanceById(def.getId(), vars);

        WorkflowInstanceImpl inst = getWorkflowInstance(processInstance, userId, workflowContext);

        return inst;
	}


	@Override
	public WorkflowInstance updateWorkflowContext(long companyId, long workflowInstanceId, Map<String, Serializable> workflowContext) throws WorkflowException {
		String processInstanceId = String.valueOf(workflowInstanceId);

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
	public int getWorkflowInstanceCount(long companyId, Long userId, String assetClassName, Long assetClassPK, Boolean completed)
			throws WorkflowException {

		return getWorkflowInstanceCount(companyId, userId, new String[]{assetClassName}, assetClassPK, completed);
	}

    /**
     * added by Maxx
     */
    @Override
    public int getWorkflowInstanceCount(long companyId, Long userId, String[] assetClassNames, Boolean completed)
            throws WorkflowException {
        
        return getWorkflowInstanceCount(companyId, userId, assetClassNames, null, completed);
    }

    private int getWorkflowInstanceCount(long companyId, Long userId, String[] assetClassNames, Long assetClassPK, Boolean completed)
            throws WorkflowException {
        DynamicQuery dynamicQuery = getDynamicQuery(companyId, userId, assetClassNames, assetClassPK);

        final List<Long> workflowInstanceIds;
        try {
            if (Validator.isNull(completed)) {
                return (int) WorkflowInstanceLinkLocalServiceUtil.dynamicQueryCount(dynamicQuery);
            } else {
                workflowInstanceIds = WorkflowInstanceLinkLocalServiceUtil.dynamicQuery(dynamicQuery);
            }
        } catch (SystemException e) {
            _log.error(String.format("Can't get workflow instance count for companyId=%d userId=%d completed=%s classNames=%s classPK=%d", companyId, userId, completed, Arrays.toString(assetClassNames), assetClassPK));
            return 0;
        }
        
        if (workflowInstanceIds.size() == 0) {
            return 0;
        }
        
        final HashSet<String> ids = new HashSet<String>(workflowInstanceIds.size());
        for (Long id : workflowInstanceIds) {
            ids.add(String.valueOf(id));
        }
        
        final HistoricProcessInstanceQuery historicProcessInstanceQuery = historyService.createHistoricProcessInstanceQuery();
        historicProcessInstanceQuery.processInstanceIds(ids);
        if (completed) {
            historicProcessInstanceQuery.finished();
        } else {
            historicProcessInstanceQuery.unfinished();
        }

        return (int) historicProcessInstanceQuery.count();
    }


    private DynamicQuery getDynamicQuery(long companyId, Long userId, String[] assetClassNames, Long classPK) {
    	_log.debug(String.format(">>> Fetching WorkflowInstanceLinks for " +
    			"companyId = [%s], userId = [%s], asset class names = [%s] and classPK = [%s] ", companyId, userId, assetClassNames, classPK));
        DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(WorkflowInstanceLink.class, PortalClassLoaderUtil.getClassLoader());
        if (companyId > 0) {
            dynamicQuery.add(PropertyFactoryUtil.forName("companyId").eq(companyId));
        }
        if (Validator.isNotNull(userId) && userId > 0) {
            dynamicQuery.add(PropertyFactoryUtil.forName("userId").eq(userId));
        }
        if (assetClassNames != null && assetClassNames.length > 0) {
            Disjunction disjunction = RestrictionsFactoryUtil.disjunction();

            for (String assetClassName : assetClassNames) {
                disjunction.add(PropertyFactoryUtil.forName("classNameId").eq(PortalUtil.getClassNameId(assetClassName)));
            }
            dynamicQuery.add(disjunction);
        }
        if (Validator.isNotNull(classPK) && classPK > 0) {
            dynamicQuery.add(PropertyFactoryUtil.forName("classPK").eq(classPK));
        }
        dynamicQuery.setProjection(ProjectionFactoryUtil.property("workflowInstanceId"));
        return dynamicQuery;
    }

    @Override
	public List<WorkflowInstance> getWorkflowInstances(long companyId, Long userId,
													   String assetClassName, Long assetClassPK,
													   Boolean completed, int start, int end,
													   OrderByComparator orderByComparator) throws WorkflowException {
        
        return getWorkflowInstances(companyId, userId, new String[]{assetClassName}, assetClassPK, completed, start, end);
	}

    /**
     * added by Maxx
     * modified by Alex Z
     */
    @Override
    public List<WorkflowInstance> getWorkflowInstances(long companyId,
                                                       Long userId, String[] assetClassNames, Boolean completed,
                                                       int start, int end, OrderByComparator orderByComparator)
            throws WorkflowException {

        return getWorkflowInstances(companyId, userId, assetClassNames, null, completed, start, end);
    }

    private List<WorkflowInstance> getWorkflowInstances(long companyId, Long userId, String[] assetClassNames, Long assetClassPK, Boolean completed, int start, int end) {
        if (_log.isDebugEnabled()) {
            _log.debug(String.format("getWorkflowInstances: Parameters are: companyId = [%s], userId = [%s], assetClassNames = [%s], assetClassPK = [%s], completed = [%s], start = [%s], end = [%s]", companyId, userId, Arrays.toString(assetClassNames), assetClassPK, completed, start, end));
        }

        DynamicQuery dynamicQuery = getDynamicQuery(companyId, userId, assetClassNames, assetClassPK);

        final List<Long> workflowInstanceIds;
        try {
            if (Validator.isNull(completed)) {
                workflowInstanceIds = WorkflowInstanceLinkLocalServiceUtil.dynamicQuery(dynamicQuery, start, end);
            } else {
                workflowInstanceIds = WorkflowInstanceLinkLocalServiceUtil.dynamicQuery(dynamicQuery);
            }
        } catch (SystemException e) {
            _log.error(String.format("Can't get workflow instances for companyId=%d userId=%d completed=%s classNames=%s classPK=%d", companyId, userId, completed, Arrays.toString(assetClassNames), assetClassPK));
            return Collections.emptyList();
        }
        
        if (workflowInstanceIds.size() == 0) {
            return Collections.emptyList();
        }
        
        final HashSet<String> ids = new HashSet<String>(workflowInstanceIds.size());
        for (Long id : workflowInstanceIds) {
            ids.add(String.valueOf(id));
        }
        final HistoricProcessInstanceQuery historicProcessInstanceQuery = historyService.createHistoricProcessInstanceQuery();
        historicProcessInstanceQuery.processInstanceIds(ids);
        if (completed) {
            historicProcessInstanceQuery.finished();
        } else {
            historicProcessInstanceQuery.unfinished();
        }

        List<HistoricProcessInstance> historicProcessInstances;
        if ((start != QueryUtil.ALL_POS) && (end != QueryUtil.ALL_POS)) {
            historicProcessInstances = historicProcessInstanceQuery.listPage(start, end);
        } else {
            historicProcessInstances = historicProcessInstanceQuery.list();
        }

        if (_log.isDebugEnabled()) {
            _log.debug(String.format("Found results: [%s]", historicProcessInstances == null || historicProcessInstances.isEmpty() ? 0 : historicProcessInstances.size()));
        }

        if (historicProcessInstances != null) {
            List<WorkflowInstance> result = new ArrayList<WorkflowInstance>(historicProcessInstances.size());
            for (HistoricProcessInstance historicProcessInstance : historicProcessInstances) {
                result.add(getWorkflowInstance(historicProcessInstance));
            }
            return result;
        } else {
            return Collections.emptyList();
        }
    }

	public WorkflowInstanceImpl getWorkflowInstance(ProcessInstance processInstance, Long userId, Map<String, Serializable> currentWorkflowContext) throws WorkflowException {
		try{
	        HistoricProcessInstance historyPI =  historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult();
	        ProcessDefinition procDef = workflowDefinitionManager.getProcessDefinition(historyPI.getProcessDefinitionId());
	
	        WorkflowInstanceImpl inst = new WorkflowInstanceImpl();
	
	        inst.setEndDate(historyPI.getEndTime());
	        inst.setStartDate(historyPI.getStartTime());
	
	        List<String> activities = new ArrayList<String>();
	        try {
	        	activities = runtimeService.getActiveActivityIds(processInstance.getProcessInstanceId());
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
	
	        // copy variables
	
	
	        //Postpone this action up to inst.getWorkflowContext invocation
//			inst.setWorkflowContext(workflowContext);
	
			inst.setWorkflowDefinitionName(procDef.getName());
			inst.setWorkflowDefinitionVersion(procDef.getVersion());
	
			/*Long id = idMappingService.getLiferayProcessInstanceId(processInstance.getId());
			if (id == null) {
				// not exists in DB - create new
				Map<String, Serializable> workflowContext = getWorkflowContext(processInstance.getId(), currentWorkflowContext);
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
				procInstImpl.setProcessInstanceId(processInstance.getId());
	
				id = (Long)processInstanceExtensionDao.save(procInstImpl);
	
				_log.info("Stored new process instance ext " + processInstance.getId() + " -> " + id);
			}*/
	
			//could do it safety - see DbIdGenerator
			inst.setWorkflowInstanceId(Long.valueOf(processInstance.getId()));
	
			return inst;
		} catch(Exception e) {
			_log.error("getProcessInstance FAILED: " + processInstance, e);
			return null;
		}
	}
	
	public Map<String, Serializable> getWorkflowContext(long workflowInstanceId) {
		String procInstanceId = String.valueOf(workflowInstanceId);
		ProcessInstance inst = runtimeService.createProcessInstanceQuery().processInstanceId(procInstanceId).singleResult();
		if (inst != null) {
			return getWorkflowContext(inst.getId(), new HashMap<String, Serializable>());
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

    public Map<String, Serializable> getWorkflowContext(String executionId, Map<String, Serializable> currentWorkflowContext) {
        Map<String, Serializable> workflowContext = null;
        try {
        	Execution e = runtimeService.createExecutionQuery().executionId(executionId).singleResult();
        	if(e == null) {
        		workflowContext = currentWorkflowContext;
        	} else {
        		_log.debug(String.format(">>>Fetching vars for execution id = [%s]", executionId));
        		Map<String, Object> vars = runtimeService.getVariables(executionId);
            	workflowContext = convertFromVars(vars);
                currentWorkflowContext = workflowContext;
                
                workflowContext.put("allVariables", (Serializable)convertFromVars(getAllProcessInstanceVars(executionId)));
        	}
        } catch (Exception ex) {
        	// in case then process has no user tasks - process may be finished just after it is started
        	// so - we will not have active activities here.
        	_log.debug("Error during getting context vars", ex);
        	workflowContext = currentWorkflowContext;
        }
        return workflowContext;
    }

    private WorkflowInstance getHistoryWorkflowInstance(HistoricProcessInstance historyPI) {
		ProcessDefinition procDef =  workflowDefinitionManager.getProcessDefinition(historyPI.getProcessDefinitionId());

        WorkflowInstanceImpl inst = new WorkflowInstanceImpl();

        inst.setEndDate(historyPI.getEndTime());
        inst.setStartDate(historyPI.getStartTime());
        // TODO
        inst.setState("");

        // get basic variables from ext object
//        ProcessInstanceExtensionImpl procInstExt = processInstanceExtensionDao.findByProcessInstanceId(historyPI.getId());
//        Map<String, Serializable> workflowContext = getWorkflowContext(procInstExt);
//		inst.setWorkflowContext(workflowContext);

		inst.setWorkflowInstanceId(Long.valueOf(historyPI.getId()));

		inst.setWorkflowDefinitionName(procDef.getName());
		inst.setWorkflowDefinitionVersion(procDef.getVersion());

		return inst;
	}

	private WorkflowInstance getWorkflowInstance(HistoricProcessInstance processInstance) {
		
		_log.debug(String.format(">>>Fetching HistoricProcessInstance with id = [%s]", processInstance.getId()));
		
		WorkflowInstanceImpl workflowInstance = new WorkflowInstanceImpl();

		//we could use long - see DbIdGenerator
		workflowInstance.setWorkflowInstanceId(Long.valueOf(processInstance.getId()));

		ProcessDefinition processDef =  workflowDefinitionManager.getProcessDefinition(processInstance.getProcessDefinitionId());

		workflowInstance.setWorkflowDefinitionName(processDef.getName());
		workflowInstance.setWorkflowDefinitionVersion(processDef.getVersion());

		workflowInstance.setStartDate(processInstance.getStartTime());
		workflowInstance.setEndDate(processInstance.getEndTime());

		if (processInstance.getEndTime() == null) {
			List<String> activities = runtimeService.getActiveActivityIds(processInstance.getId());
			// activities contains internal ids - need to be converted into names
			List<String> activityNames = new ArrayList<String>(activities.size());
	        ReadOnlyProcessDefinition readOnlyProcessDefinition = ((RepositoryServiceImpl)repositoryService).getDeployedProcessDefinition(processDef.getId());

			for (String activiti: activities) {
				PvmActivity findActivity = readOnlyProcessDefinition.findActivity(activiti);
				if (findActivity != null)
					activityNames.add(String.valueOf(findActivity.getProperty("name")));
			}
			workflowInstance.setState(StringUtils.join(activityNames, ","));

//            Map<String, Serializable> workflowContext = getWorkflowContext(processInstance.getProcessInstanceId(), null);
//	        workflowInstance.setWorkflowContext(workflowContext);
		} else {
			workflowInstance.setState(processInstance.getEndActivityId());

			// for ended process instance we can restore only limited set of workflow context
//			workflowInstance.setWorkflowContext(getWorkflowContext(processInstance));
		}

		return workflowInstance;
	}

	/*
	private Map<String, Serializable> getWorkflowContext(ProcessInstanceExtensionImpl procInstExt) {
		Map<String, Serializable> workflowContext = new HashMap<String, Serializable>();

        workflowContext.put(WorkflowConstants.CONTEXT_COMPANY_ID, String.valueOf(procInstExt.getCompanyId()));
        workflowContext.put(WorkflowConstants.CONTEXT_GROUP_ID, String.valueOf(procInstExt.getGroupId()));
        workflowContext.put(WorkflowConstants.CONTEXT_ENTRY_CLASS_NAME, procInstExt.getClassName());
        workflowContext.put(WorkflowConstants.CONTEXT_ENTRY_CLASS_PK, String.valueOf(procInstExt.getClassPK()));

        return workflowContext;
	}
	*/

    protected Map<String, Object> getAllProcessInstanceVars(String executionId) {
        Map<String, Object> result = new HashMap<String, Object>();

        Execution execution = runtimeService.createExecutionQuery().executionId(executionId).singleResult();
        ExecutionEntity executionEntity;
        if (execution instanceof ExecutionEntity) {
            executionEntity = (ExecutionEntity) execution;
        } else {
            return runtimeService.getVariables(executionId);
        }

        String lastSuperExecutionId = executionEntity.getProcessInstanceId();
        do {
            final Map<String, Object> variables = runtimeService.getVariables(lastSuperExecutionId);
            for (String key : variables.keySet()) {
                if (!result.containsKey(key)) {
                    result.put(key, variables.get(key));
                }
            }
            ProcessInstance pInst = runtimeService.createProcessInstanceQuery().processInstanceId(lastSuperExecutionId).singleResult();
            if (pInst instanceof ExecutionEntity) {
                executionEntity = (ExecutionEntity)pInst;
            } else {
                return result;
            }
            String superExecutionId = executionEntity.getSuperExecutionId();
            if (superExecutionId == null) break;
            executionEntity = (ExecutionEntity)runtimeService.createExecutionQuery().executionId(superExecutionId).singleResult();
            lastSuperExecutionId = executionEntity.getProcessInstanceId();
        } while (true);

        return result;
    }
}
