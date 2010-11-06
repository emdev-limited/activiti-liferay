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
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
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
import com.liferay.portal.kernel.workflow.DefaultWorkflowInstance;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.kernel.workflow.WorkflowException;
import com.liferay.portal.kernel.workflow.WorkflowInstance;
import com.liferay.portal.kernel.workflow.WorkflowInstanceManager;

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
	RepositoryService repositoryService;
	
	@Autowired
	WorkflowDefinitionExtensionDao workflowDefinitionExtensionDao;
	@Autowired
	ProcessInstanceExtensionDao processInstanceExtensionDao;
	@Autowired
	IdMappingService idMappingService;
	
	@Override
	public void deleteWorkflowInstance(long companyId, long workflowInstanceId) throws WorkflowException {
		runtimeService.deleteProcessInstance(idMappingService.getJbpmProcessInstanceId(workflowInstanceId), "cancelled");
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
		String procId = idMappingService.getJbpmProcessInstanceId(workflowInstanceId);
		ProcessInstance inst = runtimeService.createProcessInstanceQuery().processInstanceId(procId).singleResult();
		
		if (inst != null) {
			return getWorkflowInstance(inst, null);
		} else {
			_log.info("Cannot find process instance with id: " + workflowInstanceId + "(" + procId + "). try to find in history");
			
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
			result.add(getWorkflowInstance(inst, null));
		}
		
		return result;
	}

	@Override
	public WorkflowInstance signalWorkflowInstance(long companyId, long userId,
												   long workflowInstanceId, String transitionName,
												   Map<String, Serializable> context) throws WorkflowException {
		processEngine.getIdentityService().setAuthenticatedUserId(String.valueOf(userId));
		
		Map<String, Object> vars = convertFromContext(context);
		
		// TODO support transition and context
		runtimeService.signal(idMappingService.getJbpmProcessInstanceId(workflowInstanceId));
		return null;
	}

	@Override
	public WorkflowInstance startWorkflowInstance(long companyId, long groupId, long userId, 
												  String workflowDefinitionName, Integer workflowDefinitionVersion, String transitionName,
												  Map<String, Serializable> workflowContext) throws WorkflowException {
		processEngine.getIdentityService().setAuthenticatedUserId(String.valueOf(userId));
		
		WorkflowDefinitionExtensionImpl def = workflowDefinitionExtensionDao.find(companyId, workflowDefinitionName, workflowDefinitionVersion);
		
		Map<String, Object> vars = convertFromContext(workflowContext);
		
        ProcessInstance processInstance = runtimeService.startProcessInstanceById(def.getProcessDefinitionId(), vars);
        
        DefaultWorkflowInstance inst = getWorkflowInstance(processInstance, userId);
		
        return inst;
	}


	@Override
	public WorkflowInstance updateWorkflowContext(long companyId, long workflowInstanceId, Map<String, Serializable> workflowContext) throws WorkflowException {
		String processInstanceId = idMappingService.getJbpmProcessInstanceId(workflowInstanceId);
		
		for (String key : workflowContext.keySet()) {
			runtimeService.setVariable(processInstanceId, key, workflowContext.get(key));
		}
		
		ProcessInstance inst = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
		
		return getWorkflowInstance(inst, null);
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

	private DefaultWorkflowInstance getWorkflowInstance(Execution processInstance, Long userId) {
		RepositoryService repositoryService = this.processEngine.getRepositoryService();
		
        HistoricProcessInstance historyPI =  historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult();
        ProcessDefinition procDef = repositoryService.createProcessDefinitionQuery().processDefinitionId(historyPI.getProcessDefinitionId()).singleResult();
        
        DefaultWorkflowInstance inst = new DefaultWorkflowInstance();
        
        inst.setEndDate(historyPI.getEndTime());
        inst.setStartDate(historyPI.getStartTime());
        // TODO
        inst.setState("");

        // copy variables
        Map<String, Object> vars = 	runtimeService.getVariables(processInstance.getId());
        Map<String, Serializable> workflowContext = convertFromVars(vars);
		inst.setWorkflowContext(workflowContext);

		inst.setWorkflowDefinitionName(procDef.getName());
		inst.setWorkflowDefinitionVersion(procDef.getVersion());
		
		Long id = idMappingService.getLiferayProcessInstanceId(processInstance.getId());
		if (id == null) {
			// not exists in DB - create new
			ProcessInstanceExtensionImpl procInstImpl = new ProcessInstanceExtensionImpl();
			procInstImpl.setCompanyId(GetterUtil.getLong((Serializable)vars.get(WorkflowConstants.CONTEXT_COMPANY_ID)));
			procInstImpl.setGroupId(GetterUtil.getLong((Serializable)vars.get(WorkflowConstants.CONTEXT_GROUP_ID)));
			procInstImpl.setUserId(userId);
			procInstImpl.setClassName((String)vars.get(WorkflowConstants.CONTEXT_ENTRY_CLASS_NAME));
			procInstImpl.setClassPK(GetterUtil.getLong((Serializable)vars.get(WorkflowConstants.CONTEXT_ENTRY_CLASS_PK)));
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
        /* TODO
        ProcessInstanceExtensionImpl procInstExt = processInstanceExtensionDao.findByProcessInstanceId(historyPI.get);
        Map<String, Serializable> workflowContext = getWorkflowContext(procInstExt);
		inst.setWorkflowContext(workflowContext);
		
		inst.setWorkflowInstanceId(procInstExt.getId());
		*/
		
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
		
		if (historyPI.getEndTime() != null) {
			List<String> activities = runtimeService.getActiveActivityIds(processInstance.getProcessInstanceId());
			workflowInstance.setState(StringUtils.join(activities, ","));
		
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
