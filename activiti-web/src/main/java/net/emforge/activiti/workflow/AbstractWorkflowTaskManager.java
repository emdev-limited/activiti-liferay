package net.emforge.activiti.workflow;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.emforge.activiti.IdMappingService;
import net.emforge.activiti.comparator.WorkflowComparatorUtil;
import net.emforge.activiti.engine.impl.cmd.AddWorkflowLogEntryCmd;
import net.emforge.activiti.identity.LiferayIdentityService;
import net.emforge.activiti.log.WorkflowLogEntry;
import net.emforge.activiti.query.CustomHistoricTaskInstanceQuery;
import net.emforge.activiti.query.CustomHistoricTaskInstanceQueryImpl;
import net.emforge.activiti.query.CustomTaskInfoQuery;
import net.emforge.activiti.query.CustomTaskQuery;
import net.emforge.activiti.query.CustomTaskQueryImpl;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.identity.Group;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.TaskServiceImpl;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskInfo;
import org.activiti.engine.task.TaskInfoQueryWrapper;
import org.activiti.engine.task.TaskQuery;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.workflow.WorkflowException;
import com.liferay.portal.kernel.workflow.WorkflowTask;
import com.liferay.portal.kernel.workflow.WorkflowTaskAssignee;
import com.liferay.portal.kernel.workflow.WorkflowTaskManager;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.User;
import com.liferay.portal.service.RoleLocalServiceUtil;
import com.liferay.portal.util.PortalUtil;

public abstract class AbstractWorkflowTaskManager implements WorkflowTaskManager {
	private static Log _log = LogFactoryUtil.getLog(AbstractWorkflowTaskManager.class);

	@Autowired
	ProcessEngine processEngine;
	@Autowired
	RuntimeService runtimeService;
	@Autowired
	IdentityService identityService;
	@Autowired
	RepositoryService repositoryService;
	@Autowired
	TaskService taskService;
	@Autowired
	HistoryService historyService;
	
	@Autowired
	IdMappingService idMappingService;
	@Autowired
	LiferayIdentityService liferayIdentityService;
	@Autowired
	WorkflowInstanceManagerImpl workflowInstanceManager;

	/** 
	 * Add workflow log entry to process.
	 * 
	 * @param task
	 * @param workflowLogEntry
	 */
	protected void addWorkflowLogEntryToProcess(Task task, WorkflowLogEntry workflowLogEntry) {
		CommandExecutor commandExecutor = ((ProcessEngineImpl) processEngine)
				.getProcessEngineConfiguration().getCommandExecutor();

		commandExecutor.execute(new AddWorkflowLogEntryCmd(task.getId(), task
				.getProcessInstanceId(), workflowLogEntry));
	}

	protected TaskInfoQueryWrapper createQueryWrapperByRole(long companyId,
			long roleId, Boolean completed) throws WorkflowException {
		TaskInfoQueryWrapper taskInfoQueryWrapper = createQueryWrapper(companyId, completed);

		// Get candidate groups by role

		List<Role> roles = new ArrayList<Role>();
		try {
			roles.add(RoleLocalServiceUtil.getRole(roleId));
		} catch (Exception e) {
		}
		
		List<Group> groups = liferayIdentityService.findGroupsByRoles(roles);
		
		String candidateGroup = null;
		if (CollectionUtils.isNotEmpty(groups)) {
			candidateGroup = groups.get(0).getId();
		}
		try {
			taskInfoQueryWrapper.getTaskInfoQuery().taskCandidateGroup(candidateGroup);
			if (taskInfoQueryWrapper.getTaskInfoQuery() instanceof CustomTaskQuery) {
				((CustomTaskQuery)taskInfoQueryWrapper.getTaskInfoQuery()).taskUnassigned();
			}
		} catch (ActivitiIllegalArgumentException e) {
			throw new WorkflowException("Cannot get tasks by Role " + roleId);
		}
		return taskInfoQueryWrapper;
	}
	
	protected TaskInfoQueryWrapper createQueryWrapperByUserRoles(long companyId,
			long userId, Boolean completed) throws WorkflowException {
		String userName = idMappingService.getUserName(userId);
		
		TaskInfoQueryWrapper taskInfoQueryWrapper = createQueryWrapper(companyId, completed);
		
		// TODO: why candidateUser?
		// Use candidate group.
		// taskInfoQueryWrapper.getTaskInfoQuery().taskCandidateUser(userName);
		
		// Get candidate groups by user
		
		List<Group> groups = liferayIdentityService.findGroupsByUser(userName);
		List<String> candidateGroups = new ArrayList<String>();
		for (Group group : groups) {
			candidateGroups.add(group.getId());
		}
		_log.debug("candidateGroups: " + candidateGroups);
		// add empty group
		if (candidateGroups.isEmpty()) {
			candidateGroups.add("");
		}
		taskInfoQueryWrapper.getTaskInfoQuery().taskCandidateGroupIn(candidateGroups);
		if (taskInfoQueryWrapper.getTaskInfoQuery() instanceof CustomTaskQuery) {
			((CustomTaskQuery)taskInfoQueryWrapper.getTaskInfoQuery()).taskUnassigned();
		}
		
		return taskInfoQueryWrapper;
	}

	protected TaskInfoQueryWrapper createQueryWrapperByWorkflowInstance(
			long companyId, Long userId, long workflowInstanceId,
			Boolean completed) throws WorkflowException {
		TaskInfoQueryWrapper taskInfoQueryWrapper = createQueryWrapper(companyId, completed);

		if (userId != null && userId != 0l) {
			taskInfoQueryWrapper.getTaskInfoQuery().taskAssignee(idMappingService.getUserName(userId));
		}

		List<String> processInsatnceIds = getActiveSubProcessInstanceIds(String.valueOf(workflowInstanceId));
		if (processInsatnceIds.size() > 0) {
			taskInfoQueryWrapper.getTaskInfoQuery().or();
			for (String processInsatnceId : processInsatnceIds) {
				taskInfoQueryWrapper.getTaskInfoQuery().processInstanceId(processInsatnceId);
			}
			taskInfoQueryWrapper.getTaskInfoQuery().endOr();
		}
		return taskInfoQueryWrapper;
	}

	protected TaskInfoQueryWrapper createQueryWrapper(Boolean completed)
			throws WorkflowException {
		return createQueryWrapper(0L, 0L, 0L, completed);
	}
	
	protected TaskInfoQueryWrapper createQueryWrapper(long companyId, Boolean completed)
			throws WorkflowException {
		return createQueryWrapper(companyId, 0L, 0L, completed);
	}

	protected TaskInfoQueryWrapper createQueryWrapperByTaskId(
			long workflowTaskId, Boolean completed
			) throws WorkflowException {
		return createQueryWrapperByTaskId(0L, workflowTaskId, completed);
	}

	protected TaskInfoQueryWrapper createQueryWrapperByTaskId(long companyId,
			long workflowTaskId, Boolean completed
			) throws WorkflowException {
		TaskInfoQueryWrapper taskInfoQueryWrapper = createQueryWrapper(companyId, 0L, 0L, completed);

		taskInfoQueryWrapper.getTaskInfoQuery().taskId(String.valueOf(workflowTaskId));

		return taskInfoQueryWrapper;
	}

	protected TaskInfoQueryWrapper createQueryWrapper(
			long companyId, long groupId, long userId, Boolean completed
			) throws WorkflowException {
		/*TaskInfoQueryWrapper taskInfoQueryWrapper = null;

    	if (completed == null || !completed) {
    		CustomTaskQuery taskQuery = createCustomTaskQuery();

    		if (groupId > 0) {
    			taskQuery.taskGroupId(groupId);
    		}

    		taskInfoQueryWrapper = new TaskInfoQueryWrapper(taskQuery);
    	} else {
			// search for completed tasks in history service

    		CustomHistoricTaskInstanceQuery2 taskQuery = createCustomHistoricTaskInstanceQuery();
    		taskQuery.finished();

    		// FIXME: why "createHistoricActivityInstanceQuery"?
    		//HistoricActivityInstanceQuery query = historyService.createHistoricActivityInstanceQuery().taskAssignee(idMappingService.getUserName(userId)).finished();

    		// TODO 
    		//taskQuery.taskGroupId(groupId);

    		taskInfoQueryWrapper = new TaskInfoQueryWrapper(taskQuery);
    	}*/
		TaskInfoQueryWrapper taskInfoQueryWrapper = null;

		if (completed != null && completed == false) {
			taskInfoQueryWrapper = new TaskInfoQueryWrapper(createCustomTaskQuery());
		} else {
			taskInfoQueryWrapper = new TaskInfoQueryWrapper(createCustomHistoricTaskInstanceQuery().finished());

		}

		if (companyId > 0) {
			// Add companyId to search if a lot of company exists only
//			if (WorkflowUtil.getCompaniesCount() > 1) {
				/*taskInfoQueryWrapper.getTaskInfoQuery().processVariableValueEquals(
						WorkflowConstants.CONTEXT_COMPANY_ID, companyId);*/
//				((CustomTaskInfoQuery<?>)taskInfoQueryWrapper.getTaskInfoQuery()).taskCompanyId(companyId);
				taskInfoQueryWrapper.getTaskInfoQuery().taskTenantId(String.valueOf(companyId));
//			}
		}

		if (groupId > 0) {
			/*taskInfoQueryWrapper.getTaskInfoQuery().processVariableValueEquals(
					WorkflowConstants.CONTEXT_GROUP_ID, groupId);*/
			((CustomTaskInfoQuery<?>)taskInfoQueryWrapper.getTaskInfoQuery()).taskGroupId(groupId);
		}

		if (userId > 0) {
			taskInfoQueryWrapper.getTaskInfoQuery().taskAssignee(idMappingService.getUserName(userId));
		}

		return taskInfoQueryWrapper;
	}

	protected TaskInfoQueryWrapper createQueryWrapperSearch(long companyId,
			long userId, String taskName, String[] assetTypes,
			Long[] assetPrimaryKey, Date dueDateGT, Date dueDateLT,
			Boolean completed, Boolean searchByUserRoles) throws WorkflowException {
		TaskInfoQueryWrapper taskInfoQueryWrapper;
		
		if (searchByUserRoles != null && searchByUserRoles == true) {
			taskInfoQueryWrapper = createQueryWrapperByUserRoles(
					companyId, userId, completed);
		} else {
			taskInfoQueryWrapper = createQueryWrapper(
					companyId, 0L, userId, completed);
		}

		// Add conditions

		if (StringUtils.isNotEmpty(taskName)) {
			taskInfoQueryWrapper.getTaskInfoQuery().taskName(taskName);
		}

		if (assetTypes != null) {
			/**
			 * Entry class names may be defined by workflow handler:
			 * 
			 * List<String> entryClassNames = new ArrayList<String>();
			 * for (String assetType : assetTypes) {
			 * 	entryClassNames.add(WorkflowUtil.getAssetClassName(assetType));
			 * }
			 */
			((CustomTaskInfoQuery<?>)taskInfoQueryWrapper.getTaskInfoQuery()).taskEntryClassNames(java.util.Arrays.asList(assetTypes));
		}

		if (taskInfoQueryWrapper.getTaskInfoQuery() instanceof TaskQuery) {
			if (assetPrimaryKey != null/* && assetPrimaryKey.length > 0*/) {
				((CustomTaskInfoQuery<?>)taskInfoQueryWrapper.getTaskInfoQuery()).taskEntryClassPKs(java.util.Arrays.asList(assetPrimaryKey));
			}
		} else {
			// For historic query we don't use taskEntryClassPKs. But why not?
		}

		if (dueDateGT != null) {
			taskInfoQueryWrapper.getTaskInfoQuery().taskDueAfter(dueDateGT);
		}
		if (dueDateLT != null) {
			taskInfoQueryWrapper.getTaskInfoQuery().taskDueBefore(dueDateLT);
		}
		return taskInfoQueryWrapper;
	}

	private CustomTaskQuery createCustomTaskQuery() {
		CustomTaskQueryImpl customTaskQuery = CustomTaskQueryImpl.create();
		/*TaskServiceImpl serviceImpl = (TaskServiceImpl)taskService;
		CustomTaskQueryImpl2 customTaskQuery = new CustomTaskQueryImpl2(
				serviceImpl.getCommandExecutor(), processEngine
						.getProcessEngineConfiguration().getDatabaseType());*/
		return customTaskQuery;
	}
	
	private CustomHistoricTaskInstanceQuery createCustomHistoricTaskInstanceQuery() {
		TaskServiceImpl serviceImpl = (TaskServiceImpl)taskService;
		return new CustomHistoricTaskInstanceQueryImpl(
				serviceImpl.getCommandExecutor(), processEngine
				.getProcessEngineConfiguration().getDatabaseType());
	}
	
	/**
	 * <p>
	 * Retrieves workflow task paged list.
	 * <p>
	 * See
	 * {@link net.emforge.activiti.comparator.WorkflowComparatorUtil#applyComparator(TaskInfoQueryWrapper, OrderByComparator)
	 * WorkflowComparatorUtil.applyComparator()} to use
	 * {@link OrderByComparator}.
	 * 
	 * @param taskInfoQueryWrapper
	 * @param start
	 * @param end
	 * @param orderByComparator
	 *            {@link com.liferay.portal.kernel.util.OrderByComparator
	 *            OrderByComparator}
	 * @return
	 */
	protected List<WorkflowTask> getWorkflowTasks(
			TaskInfoQueryWrapper taskInfoQueryWrapper, int start, int end,
			OrderByComparator orderByComparator) {
		
		// apply comparator to query
		WorkflowComparatorUtil.applyComparator(taskInfoQueryWrapper, orderByComparator);
		
		return getWorkflowTasks(taskInfoQueryWrapper, start, end);
	}
	
	protected List<WorkflowTask> getWorkflowTasks(TaskInfoQueryWrapper taskInfoQueryWrapper, int start, int end) {
		List<? extends TaskInfo> taskInfos = null;
		if ((start != QueryUtil.ALL_POS) && (end != QueryUtil.ALL_POS)) {
			taskInfos = taskInfoQueryWrapper.getTaskInfoQuery().listPage(start, end - start);
		} else {
			taskInfos = taskInfoQueryWrapper.getTaskInfoQuery().list();
		}
		
		List<WorkflowTask> result = new ArrayList<WorkflowTask>(taskInfos.size());
		
		Method method = null;
		try {
			if (taskInfoQueryWrapper.getTaskInfoQuery() instanceof HistoricTaskInstanceQuery) {
				method = getClass().getSuperclass().getDeclaredMethod("getHistoryWorkflowTask", TaskInfo.class);
			} else {
				method = getClass().getSuperclass().getDeclaredMethod("getWorkflowTask", TaskInfo.class);
			}
		} catch (NoSuchMethodException e) {
		}
		
		for (TaskInfo taskInfo : taskInfos) {
			try {
				WorkflowTask workflowTask = (WorkflowTask) method.invoke(this, taskInfo);
				result.add(workflowTask);
			} catch (Exception ex) {
				_log.warn("Cannot convert Activiti task " + taskInfo.getId() + " into Liferay: " + ex);
				_log.debug("Cannot convert Activiti task into Liferay", ex);
			}
		}
		
		return result;
	}
	
	protected WorkflowTask getWorkflowTask(TaskInfoQueryWrapper taskInfoQueryWrapper) {
		TaskInfo taskInfo = taskInfoQueryWrapper.getTaskInfoQuery().singleResult();
		
		if (taskInfo == null) {
			return null;
		}
		
		WorkflowTask workflowTask = null;
		try {
			if (taskInfoQueryWrapper.getTaskInfoQuery() instanceof HistoricTaskInstanceQuery) {
				workflowTask = getHistoryWorkflowTask(taskInfo);
			} else {
				workflowTask = getWorkflowTask(taskInfo);
			}
		} catch (Exception ex) {
			_log.warn("Cannot convert Activiti task " + taskInfo.getId() + " into Liferay: " + ex);
			_log.debug("Cannot convert Activiti task into Liferay", ex);
		}
		
		return workflowTask;
	}
	
	
	protected WorkflowTask getHistoryWorkflowTask(TaskInfo taskInfo) throws WorkflowException {
		return getHistoryWorkflowTask((HistoricTaskInstance)taskInfo);
	}

	protected WorkflowTask getHistoryWorkflowTask(HistoricTaskInstance task) throws WorkflowException {
		WorkflowTaskImpl workflowTask = new WorkflowTaskImpl();

		// TODO setAsynchronous(!task.isBlocking());
		workflowTask.setCreateDate(task.getStartTime());
		workflowTask.setCompletionDate(task.getEndTime());
		workflowTask.setDescription(task.getDescription());
		workflowTask.setName(task.getName());

		if (task.getEndTime() != null) {
			workflowTask.setDueDate(task.getDueDate());
			workflowTask.setDeleteReason(task.getDeleteReason());
		} else {
//			Map<String, Serializable> vars = workflowInstanceManager.getWorkflowContext(task.getExecutionId(), null);

			//AF: commented because not clear if it is neccressary
//			workflowTask.setDueDate(task.getDueDate() != null ? task.getDueDate() : (Date)vars.get("dueDate")); // keep getting dueDate from vars for backward compatibility
			workflowTask.setDueDate(task.getDueDate());
//			workflowTask.setOptionalAttributes(vars);
		}

		// get process def from activity
		String processDefId = task.getProcessDefinitionId();

		ProcessDefinition processDef = repositoryService.getProcessDefinition(task.getProcessDefinitionId());

		workflowTask.setWorkflowDefinitionId(idMappingService.getLiferayWorkflowDefinitionId(processDefId));
		if (processDef != null) {
			workflowTask.setWorkflowDefinitionName(processDef.getName());
			workflowTask.setWorkflowDefinitionVersion(processDef.getVersion());
		} else {
			workflowTask.setWorkflowDefinitionName(idMappingService.getLiferayWorkflowDefinitionName(processDefId));
			workflowTask.setWorkflowDefinitionVersion(idMappingService.getLiferayWorkflowDefinitionVersion(processDefId));
		}

		/* Not applicable for history task
		Set<String> variableNames = taskService.getVariableNames(task.getId());
		Map<String, Object> vars = taskService.getVariables(task.getId(), variableNames);
		workflowTask.setOptionalAttributes(WorkflowInstanceManagerImpl.convertFromVars(vars));
		*/
		workflowTask.setWorkflowInstanceId(Long.valueOf(task.getProcessInstanceId()));

		/*
		long companyId = GetterUtil.getLong((String)taskService.getVariable(task.getId(), "companyId"));
		long groupId = GetterUtil.getLong((String)taskService.getVariable(task.getId(), "groupId"));
		*/

		String assignee = task.getAssignee();
		if (assignee != null && !"null".equals(assignee)) { // TODO check why we have this "null" string
			List<WorkflowTaskAssignee> workflowTaskAssignees = new ArrayList<WorkflowTaskAssignee>(1);

			WorkflowTaskAssignee workflowTaskAssignee = new WorkflowTaskAssignee(
					User.class.getName(), idMappingService.getUserId(assignee));
			workflowTaskAssignees.add(workflowTaskAssignee);

			workflowTask.setWorkflowTaskAssignees(workflowTaskAssignees);
		}

		workflowTask.setWorkflowTaskId(Long.valueOf(task.getId())); // TODO

		return workflowTask;
	}

	
	protected WorkflowTask getWorkflowTask(TaskInfo taskInfo) throws WorkflowException {
		return getWorkflowTask((Task)taskInfo);
	}

	/** Convert active Activiti Task to Liferay WorkflowTask
	 * 
	 * @param task
	 * @return
	 */
	private WorkflowTask getWorkflowTask(Task task) throws WorkflowException {

		// TODO: Implement WorkflowTaskImpl that is optimized to invoke getWorkflowContext
		WorkflowTaskImpl workflowTask = new WorkflowTaskImpl();

		String processInstanceId = task.getProcessInstanceId();

		ProcessDefinition processDef = repositoryService.getProcessDefinition(task.getProcessDefinitionId());

		// TODO setAsynchronous(!task.isBlocking());
		workflowTask.setCreateDate(task.getCreateTime());
		workflowTask.setDescription(task.getDescription());
		workflowTask.setName(task.getName());
		workflowTask.setDueDate(task.getDueDate());

		// optional attributes may be retrieved from WorkflowTask object when it is necessary
		// workflowTask.setOptionalAttributes(vars);

		workflowTask.setWorkflowDefinitionId(idMappingService.getLiferayWorkflowDefinitionId(processDef.getId()));

		workflowTask.setWorkflowDefinitionName(processDef.getName());
		workflowTask.setWorkflowDefinitionVersion(processDef.getVersion());

		/*
		Long liferayProcessInstanceId = idMappingService.getLiferayProcessInstanceId(processInstanceId);
		
		_log.debug("liferayProcessInstanceId " + liferayProcessInstanceId);
		
		if (liferayProcessInstanceId == null) {
			// subprocess - they do not have liferay process instance - lets try to use original id
			// lets try to create it
			ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
			workflowInstanceManager.getWorkflowInstance(processInstance, null, null);
			liferayProcessInstanceId = idMappingService.getLiferayProcessInstanceId(processInstanceId);
			//throw new WorkflowException("Cannot get liferay process instance id by activity process instance " + processInstanceId);
		}
		*/
		workflowTask.setWorkflowInstanceId(Long.valueOf(processInstanceId));


		String assignee = task.getAssignee();
		if (assignee != null && !"null".equals(assignee)) { // TODO check why we have this "null" string
			List<WorkflowTaskAssignee> workflowTaskAssignees = new ArrayList<WorkflowTaskAssignee>(1);

			WorkflowTaskAssignee workflowTaskAssignee = new WorkflowTaskAssignee(
					User.class.getName(), idMappingService.getUserId(assignee));
			workflowTaskAssignees.add(workflowTaskAssignee);

			workflowTask.setWorkflowTaskAssignees(workflowTaskAssignees);
		} else {
			// return group (if exists)
			List<IdentityLink> participations = new ArrayList<IdentityLink>();

			List<WorkflowTaskAssignee> workflowTaskAssignees = new ArrayList<WorkflowTaskAssignee>(1);
			try {					
				participations = taskService.getIdentityLinksForTask(task.getId());
			} catch (Exception ex) {
				// for completed tasks it will simple produce exception - ignore it
			}

			// Retrieve company id

			/**
			 * This is example how to retrieve companyId from the top process instance:
			 * 
			 * ExecutionEntity executionEntity = (ExecutionEntity)runtimeService.createExecutionQuery().executionId(task.getExecutionId()).singleResult();
	         * ExecutionEntity topExecution = WorkflowUtil.getTopProcessInstance(executionEntity.getProcessInstanceId());
			 * long companyId = GetterUtil.getLong(runtimeService.getVariable(topExecution.getId(), WorkflowConstants.CONTEXT_COMPANY_ID));
			 * 
			 * @author Dmitry Farafonov
			 */

			// it can be default company if companyId is not present in task variables
			String tenantId = task.getTenantId();
			long companyId = 0;
//			Object taskCompanyId = taskService.getVariable(task.getId(), WorkflowConstants.CONTEXT_COMPANY_ID);
			if (StringUtils.isNotEmpty(tenantId)) {
				companyId = GetterUtil.getLong(tenantId);
			} else {
				companyId = PortalUtil.getDefaultCompanyId();
			}

			_log.debug("participations size " + participations.size());
			for (IdentityLink participation : participations) {
				if (StringUtils.isNotEmpty(participation.getGroupId())) {
					Role role = liferayIdentityService.findRole(companyId, participation.getGroupId());
					WorkflowTaskAssignee workflowTaskAssignee = new WorkflowTaskAssignee(
							Role.class.getName(), role.getRoleId());
					workflowTaskAssignees.add(workflowTaskAssignee);
				}
			}
			_log.debug("participations end ");
			workflowTask.setWorkflowTaskAssignees(workflowTaskAssignees);
		}

		workflowTask.setWorkflowTaskId(Long.valueOf(task.getId()));
		_log.debug("END-----> convert task : " + task.getId());
		return workflowTask;
	}

	protected List<String> getActiveSubProcessInstanceIds(String processInstanceId) {
		// go through subprocesses
		ProcessInstanceQuery processQuery = runtimeService.createProcessInstanceQuery();
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
}
