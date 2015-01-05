package net.emforge.activiti.workflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.emforge.activiti.PortletPropsValues;
import net.emforge.activiti.WorkflowConstants;
import net.emforge.activiti.WorkflowUtil;
import net.emforge.activiti.log.WorkflowLogEntry;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.ExclusiveGateway;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiOptimisticLockingException;
import org.activiti.engine.query.Query;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.workflow.WorkflowException;
import com.liferay.portal.kernel.workflow.WorkflowInstance;
import com.liferay.portal.kernel.workflow.WorkflowInstanceManager;
import com.liferay.portal.kernel.workflow.WorkflowLog;

@Service(value="workflowInstanceManager")
public class WorkflowInstanceManagerImpl extends AbstractWorkflowInstanceManager implements WorkflowInstanceManager {
	private static Log _log = LogFactoryUtil.getLog(WorkflowInstanceManagerImpl.class);
	
	@Override
	public void deleteWorkflowInstance(long companyId, long workflowInstanceId)
			throws WorkflowException {
		String processInstanceId = String.valueOf(workflowInstanceId);
		_log.info("Deleting process instance " + processInstanceId);

		ProcessInstanceQuery processInstanceQuery = runtimeService.createProcessInstanceQuery();
		processInstanceQuery.processInstanceId(processInstanceId);
		List<ProcessInstance> processInstanceList = processInstanceQuery.list();
		if (processInstanceList != null && !processInstanceList.isEmpty()) {
			runtimeService.deleteProcessInstance(processInstanceId, "cancelled");
		}
	}

	/**
	 * Returns outgoing transition names.
	 */
	@Override
	public List<String> getNextTransitionNames(long companyId, long userId,
			long workflowInstanceId) throws WorkflowException {
		String processInstanceId = String.valueOf(workflowInstanceId);
		ProcessInstanceQuery processInstanceQuery = runtimeService
				.createProcessInstanceQuery().processInstanceId(
						processInstanceId);
		ProcessInstance processInstance = processInstanceQuery.singleResult();

		BpmnModel bpmnModel = repositoryService.getBpmnModel(processInstance.getProcessDefinitionId());
		List<String> activeActivityIds = runtimeService.getActiveActivityIds(processInstanceId);
		_log.debug("activeActivityIds for workflowInstance[" + workflowInstanceId + "]: " + activeActivityIds);

		List<String> transitionNames = new ArrayList<String>();
		for (String activityId : activeActivityIds) {
			FlowElement activityFlowElement = bpmnModel.getFlowElement(activityId);
			List<SequenceFlow> outgoingFlows = ((FlowNode) activityFlowElement).getOutgoingFlows();
			if (outgoingFlows.size() == 1) {
				FlowElement targetActivity = bpmnModel.getFlowElement(outgoingFlows.get(0).getTargetRef());
				if (targetActivity instanceof ExclusiveGateway) {
					outgoingFlows = ((FlowNode) targetActivity).getOutgoingFlows();
				}
			}
			for (SequenceFlow sequenceFlow : outgoingFlows) {
				String name = null;
				if (PortletPropsValues.PROCESS_NEXT_TRANSITION_NAMES_STRATEGY
						.equals(WorkflowConstants.PROCESS_NEXT_TRANSITION_NAMES_STRATEGY_NAMES)) {
					if (StringUtils.isNotEmpty(sequenceFlow.getName())) {
						name = sequenceFlow.getName();
					} else {
						_log.warn("Name for SequenceFlow " + sequenceFlow.getId() + " is empty");
					}
				} else {
					name = sequenceFlow.getId();
				}
				if (StringUtils.isNotEmpty(name)) {
					transitionNames.add(name);
				}
			}
		}
		return transitionNames;
	}

	@Override
	public WorkflowInstance getWorkflowInstance(long companyId,
			long workflowInstanceId) throws WorkflowException {
		Query<?, ?> processInstanceQuery = createProcessInstanceQuery(workflowInstanceId, false);

		WorkflowInstance workflowInstance = getWorkflowInstance(processInstanceQuery);

		if (workflowInstance != null) {
			return workflowInstance;
		} else {
			_log.debug("Cannot find active process instance " + workflowInstanceId + " , try to find it in history");

			processInstanceQuery = createProcessInstanceQuery(workflowInstanceId, true);
			workflowInstance = getWorkflowInstance(processInstanceQuery);
			if (workflowInstance != null) {
				return workflowInstance;
			} else {
				_log.warn("Cannot find process instance " + workflowInstanceId);
				return null;
			}
		}
	}

	@Override
	public int getWorkflowInstanceCount(long companyId, Long userId,
			String assetClassName, Long assetClassPK, Boolean completed)
					throws WorkflowException {
		Query<?, ?> processInstanceQuery = null;

		// try to get serialized parameters map from assetClassName
		// NOTES: the better way is to use some marshaller because of Long variables may be converted into Integer variables
		Map<String, Object> parameters = WorkflowUtil.convertValueToParameterValue(assetClassName);

		if (!parameters.equals(MapUtils.EMPTY_MAP)) {
			// if serialized parameters found then do not set assetClassNames
			processInstanceQuery = createProcessInstanceQueryByParametersMap(parameters);
		} else {
			String[] assetClassNames = (assetClassName == null) ? null : new String[]{assetClassName};
			processInstanceQuery = createProcessInstanceQuerySearch(companyId, userId, assetClassNames, assetClassPK, completed);
		}

		Long count = processInstanceQuery.count();
		return count.intValue();
	}

	@Override
	public int getWorkflowInstanceCount(long companyId, Long userId,
			String[] assetClassNames, Boolean completed)
			throws WorkflowException {
		Query<?, ?> processInstanceQuery = createProcessInstanceQuerySearch(companyId, userId, assetClassNames, null, completed);

		Long count = processInstanceQuery.count();
		return count.intValue();
	}

	@Override
	public int getWorkflowInstanceCount(long companyId,
			String workflowDefinitionName, Integer workflowDefinitionVersion,
			Boolean completed) throws WorkflowException {
		Query<?, ?> processInstanceQuery = createProcessInstanceQueryByDefinition(companyId, workflowDefinitionName, workflowDefinitionVersion, completed);

		Long count = processInstanceQuery.count();
		return count.intValue();
	}

	@Override
	public List<WorkflowInstance> getWorkflowInstances(long companyId,
			Long userId, String assetClassName, Long assetClassPK,
			Boolean completed, int start, int end,
			OrderByComparator orderByComparator) throws WorkflowException {
		Query<?, ?> processInstanceQuery = null;

		// try to get serialized parameters map from assetClassName
		Map<String, Object> parameters = WorkflowUtil.convertValueToParameterValue(assetClassName);

		if (!parameters.equals(MapUtils.EMPTY_MAP)) {
			// if serialized parameters found then do not set assetClassNames
			processInstanceQuery = createProcessInstanceQueryByParametersMap(parameters);
		} else {
			String[] assetClassNames = (assetClassName == null) ? null : new String[]{assetClassName};
			processInstanceQuery = createProcessInstanceQuerySearch(companyId, userId, assetClassNames, assetClassPK, completed);
		}

		List<WorkflowInstance> workflowInstances = getWorkflowInstances(processInstanceQuery, start, end, orderByComparator);

		return workflowInstances;
	}

	@Override
	public List<WorkflowInstance> getWorkflowInstances(long companyId,
			Long userId, String[] assetClassNames, Boolean completed,
			int start, int end, OrderByComparator orderByComparator)
			throws WorkflowException {
		Query<?, ?> processInstanceQuery = createProcessInstanceQuerySearch(companyId, userId, assetClassNames, null, completed);

		List<WorkflowInstance> workflowInstances = getWorkflowInstances(processInstanceQuery, start, end, orderByComparator);

		return workflowInstances;
	}

	@Override
	public List<WorkflowInstance> getWorkflowInstances(long companyId,
			String workflowDefinitionName, Integer workflowDefinitionVersion,
			Boolean completed, int start, int end,
			OrderByComparator orderByComparator) throws WorkflowException {
		Query<?, ?> processInstanceQuery = createProcessInstanceQueryByDefinition(companyId, workflowDefinitionName, workflowDefinitionVersion, completed);
		
		List<WorkflowInstance> workflowInstances = getWorkflowInstances(processInstanceQuery, start, end, orderByComparator);
		
		return workflowInstances;
	}

	@Override
	public WorkflowInstance signalWorkflowInstance(long companyId, long userId,
			long workflowInstanceId, String transitionName,
			Map<String, Serializable> workflowContext) throws WorkflowException {
		processEngine.getIdentityService().setAuthenticatedUserId(idMappingService.getUserName(userId));
		String processInstanceId = String.valueOf(workflowInstanceId);

		Map<String, Object> vars = WorkflowUtil.convertFromContext(workflowContext);

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
			
			WorkflowLogEntry workflowLogEntry = new WorkflowLogEntry();
			workflowLogEntry.setType(type);
			workflowLogEntry.setComment(comment);
			workflowLogEntry.setPreviousUserId(userId);
			addWorkflowLogEntryToProcess(processInstanceId, workflowLogEntry);
		
		} catch(Exception e) { _log.error("add comment failed: " + e.getMessage()); }

		return null;
	}

	@Override
	public WorkflowInstance startWorkflowInstance(long companyId, long groupId,
			long userId, String workflowDefinitionName,
			Integer workflowDefinitionVersion, String transitionName,
			Map<String, Serializable> workflowContext) throws WorkflowException {
		_log.info("Start workflow instance " + workflowDefinitionName + " : " + workflowDefinitionVersion);

		processEngine.getIdentityService().setAuthenticatedUserId(idMappingService.getUserName(userId));

		ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
				.processDefinitionTenantId(String.valueOf(companyId))
				.processDefinitionName(workflowDefinitionName)
				.processDefinitionVersion(workflowDefinitionVersion)
				.singleResult();

		if (processDefinition == null) {
			_log.error("Cannot find workflow definition " + workflowDefinitionName + " : " + workflowDefinitionVersion);
			throw new WorkflowException("Cannot find workflow definition " + workflowDefinitionName + " : " + workflowDefinitionVersion);
		}

		// update workflowContext to use Long types instead of String added in WorkflowInstanceLinkLocalServiceImpl
		if (PortletPropsValues.WORKFLOWCONTEXT_LIFERAY_USE_LONG){
			Serializable cId = workflowContext.get(WorkflowConstants.CONTEXT_COMPANY_ID);
			Serializable gId = workflowContext.get(WorkflowConstants.CONTEXT_GROUP_ID);
			Serializable ecPK = workflowContext.get(WorkflowConstants.CONTEXT_ENTRY_CLASS_PK);
			if (cId != null) {
				workflowContext.put(
						WorkflowConstants.CONTEXT_COMPANY_ID, GetterUtil.getLong(cId));
			}
			if (gId != null) {
				workflowContext.put(
						WorkflowConstants.CONTEXT_GROUP_ID, GetterUtil.getLong(gId));
			}
			if (ecPK != null) {
				workflowContext.put(
						WorkflowConstants.CONTEXT_ENTRY_CLASS_PK, GetterUtil.getLong(ecPK));
			}
		}

		Map<String, Object> vars = WorkflowUtil.convertFromContext(workflowContext);

		ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId(), vars);

		WorkflowInstance inst = getWorkflowInstance(processInstance);

		return inst;
	}

	@Override
	public WorkflowInstance updateWorkflowContext(long companyId,
			long workflowInstanceId, Map<String, Serializable> workflowContext)
					throws WorkflowException {
		String processInstanceId = String.valueOf(workflowInstanceId);

		for (String key : workflowContext.keySet()) {
			runtimeService.setVariable(processInstanceId, key, workflowContext.get(key));
		}

		ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();

		return getWorkflowInstance(processInstance);
	}

}
