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
import net.emforge.activiti.spring.Initializable;

import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
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
import com.liferay.portal.kernel.workflow.WorkflowInstanceManager;

@Service(value="workflowInstanceManager")
public class WorkflowInstanceManagerImpl implements WorkflowInstanceManager, ApplicationContextAware, Initializable  
{
	private static Log _log = LogFactoryUtil.getLog(WorkflowInstanceManagerImpl.class);
	
	ApplicationContext applicationContext;
	
	ProcessEngine processEngine;

	RuntimeService runtimeService;

	HistoryService historyService;

	RepositoryService repositoryService;
	

	WorkflowDefinitionExtensionDao workflowDefinitionExtensionDao;

	ProcessInstanceExtensionDao processInstanceExtensionDao;

	IdMappingService idMappingService;
	
	public void init() {
		processEngine = applicationContext.getBean("processEngine", ProcessEngine.class);
		runtimeService = applicationContext.getBean("runtimeService", RuntimeService.class);
		historyService = applicationContext.getBean("historyService", HistoryService.class);
		repositoryService = applicationContext.getBean("repositoryService", RepositoryService.class);
		
		workflowDefinitionExtensionDao = applicationContext.getBean(WorkflowDefinitionExtensionDao.class);
		workflowDefinitionExtensionDao.init();
		processInstanceExtensionDao = applicationContext.getBean(ProcessInstanceExtensionDao.class);
		processInstanceExtensionDao.init();
		idMappingService = applicationContext.getBean("idMappingService", IdMappingService.class);
	}	
	
	@Override
	@Transactional
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

	@Override
	@Transactional
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

	@Override
	@Transactional
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
        
        DefaultWorkflowInstance inst = getWorkflowInstance(processInstance, userId, workflowContext);
		
        return inst;
	}


	@Override
	@Transactional
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

	public DefaultWorkflowInstance getWorkflowInstance(Execution processInstance, Long userId, Map<String, Serializable> currentWorkflowContext) throws WorkflowException {
		RepositoryService repositoryService = this.processEngine.getRepositoryService();
		
        HistoricProcessInstance historyPI =  historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult();
        ProcessDefinition procDef = repositoryService.createProcessDefinitionQuery().processDefinitionId(historyPI.getProcessDefinitionId()).singleResult();
        
        DefaultWorkflowInstance inst = new DefaultWorkflowInstance();
        
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
		
		for (String activiti: activities) {
			List<HistoricActivityInstance> histActs = historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstance.getProcessInstanceId()).activityId(activiti).list();
			if (histActs.size() > 0) {
				activityNames.add(histActs.get(0).getActivityName());
			}
		}
		inst.setState(StringUtils.join(activityNames, ","));

        // copy variables
        
        Map<String, Serializable> workflowContext = null;
        try {
        	Map<String, Object> vars = runtimeService.getVariables(processInstance.getId());
        	workflowContext = convertFromVars(vars);
        } catch (Exception ex) {
        	// in case then process has no user tasks - process may be finished just after it is started
        	// so - we will not have active activities here.
        	_log.debug("Error during getting context vars", ex);
        	workflowContext = currentWorkflowContext;
        }
        
		inst.setWorkflowContext(workflowContext);

		inst.setWorkflowDefinitionName(procDef.getName());
		inst.setWorkflowDefinitionVersion(procDef.getVersion());
		
		Long id = idMappingService.getLiferayProcessInstanceId(processInstance.getId());
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
			procInstImpl.setProcessInstanceId(processInstance.getId());
			
			id = (Long)processInstanceExtensionDao.save(procInstImpl);
			
			_log.info("Stored new process instance ext " + processInstance.getId() + " -> " + id);
		}
		
		inst.setWorkflowInstanceId(id);
		
		return inst;
	}

	private WorkflowInstance getHistoryWorkflowInstance(HistoricProcessInstance historyPI) {
		ProcessDefinition procDef = repositoryService.createProcessDefinitionQuery().processDefinitionId(historyPI.getProcessDefinitionId()).singleResult();
        
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
		
		ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery();
        processDefinitionQuery.processDefinitionId(historyPI.getProcessDefinitionId());
        ProcessDefinition processDef = processDefinitionQuery.singleResult();
        
		workflowInstance.setWorkflowDefinitionName(processDef.getName());
		workflowInstance.setWorkflowDefinitionVersion(processDef.getVersion());
		
		workflowInstance.setStartDate(historyPI.getStartTime());
		workflowInstance.setEndDate(historyPI.getEndTime());
		
		if (historyPI.getEndTime() == null) {
			List<String> activities = runtimeService.getActiveActivityIds(processInstance.getProcessInstanceId());
			// activities contains internal ids - need to be converted into names
			List<String> activityNames = new ArrayList<String>(activities.size());
			
			for (String activiti: activities) {
				List<HistoricActivityInstance> histActs = historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstance.getProcessInstanceId()).activityId(activiti).list();
				if (histActs.size() > 0) {
					activityNames.add(histActs.get(0).getActivityName());
				}
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
	
	@Override
	public void setApplicationContext(ApplicationContext ctx)
			throws BeansException 
	{
		applicationContext = ctx;
	
	}	
}
