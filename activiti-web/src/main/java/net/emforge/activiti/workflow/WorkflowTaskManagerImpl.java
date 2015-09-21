package net.emforge.activiti.workflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.emforge.activiti.WorkflowConstants;
import net.emforge.activiti.WorkflowUtil;
import net.emforge.activiti.log.WorkflowLogEntry;

import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskInfoQueryWrapper;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.workflow.WorkflowException;
import com.liferay.portal.kernel.workflow.WorkflowLog;
import com.liferay.portal.kernel.workflow.WorkflowTask;
import com.liferay.portal.model.Role;
import com.liferay.portal.service.RoleLocalServiceUtil;

@Service("workflowTaskManager")
public class WorkflowTaskManagerImpl extends AbstractWorkflowTaskManager {
	private static Log _log = LogFactoryUtil
			.getLog(WorkflowTaskManagerImpl.class);

	@Override
	public WorkflowTask assignWorkflowTaskToRole(long companyId, long userId,
			long workflowTaskId, long roleId, String comment, Date dueDate,
			Map<String, Serializable> workflowContext) throws WorkflowException {
		try {
			identityService.setAuthenticatedUserId(idMappingService.getUserName(userId));
			Role arole = RoleLocalServiceUtil.getRole(roleId);

			String taskId = String.valueOf(workflowTaskId);
			Task task = taskService.createTaskQuery().taskId(taskId).singleResult();

			if (task == null) {
				throw new WorkflowException("Task " + workflowTaskId + " not found");
			}

			// update due date
			if (dueDate != null) {
				task.setDueDate(dueDate);
			}
			taskService.saveTask(task);

			// update assignee
			String currentAssignee = task.getAssignee();

			// assign task
			taskService.setAssignee(taskId, null);
			WorkflowUtil.clearCandidateGroups(taskService, taskId);
			taskService.addCandidateGroup(taskId, String.valueOf(companyId) + "/" + arole.getName());

			// save log
			WorkflowLogEntry workflowLogEntry = new WorkflowLogEntry();
			workflowLogEntry.setType(WorkflowLog.TASK_ASSIGN);
			workflowLogEntry.setRoleId(roleId);
			workflowLogEntry.setAssigneeUserId(userId);
			workflowLogEntry.setWorkflowTaskId(workflowTaskId);

			Long prevUserId = null;
			try {
				prevUserId = Long.valueOf(currentAssignee);
			} catch (Exception ex) {}

			if (prevUserId != null) {
				workflowLogEntry.setPreviousUserId(prevUserId);
			}
			workflowLogEntry.setComment(comment);

			addWorkflowLogEntryToProcess(task, workflowLogEntry);

			// get new state of task
			TaskInfoQueryWrapper taskInfoQueryWrapper = createQueryWrapperByTaskId(workflowTaskId, false);

			WorkflowTask workflowTask = getWorkflowTask(taskInfoQueryWrapper);

			return workflowTask;
		} catch (WorkflowException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new WorkflowException("Cannot assign task", ex);
		}	
	}

	@Transactional
	@Override
	public WorkflowTask assignWorkflowTaskToUser(long companyId, long userId,
			long workflowTaskId, long assigneeUserId, String comment,
			Date dueDate, Map<String, Serializable> workflowContext)
					throws WorkflowException {
		try {
			identityService.setAuthenticatedUserId(idMappingService.getUserName(userId));

			String taskId = String.valueOf(workflowTaskId);
			Task task = taskService.createTaskQuery().taskId(taskId).singleResult();

			if (task == null) {
				throw new WorkflowException("Task " + workflowTaskId + " not found");
			}

			// update due date
			if (dueDate != null) {
				task.setDueDate(dueDate);
			}
			taskService.saveTask(task);

			// update assignee
			String currentAssignee = task.getAssignee();

			// assign task
			taskService.setAssignee(taskId, idMappingService.getUserName(assigneeUserId));

			// save log
			WorkflowLogEntry workflowLogEntry = new WorkflowLogEntry();
			workflowLogEntry.setType(WorkflowLog.TASK_ASSIGN);
			workflowLogEntry.setAssigneeUserId(assigneeUserId);
			workflowLogEntry.setWorkflowTaskId(workflowTaskId);

			Long prevUserId = null;
			try {
				prevUserId = Long.valueOf(currentAssignee);
			} catch (Exception ex) {}

			if (prevUserId != null) {
				workflowLogEntry.setPreviousUserId(prevUserId);
			}
			workflowLogEntry.setComment(comment);

			addWorkflowLogEntryToProcess(task, workflowLogEntry);

			// get new state of task
			TaskInfoQueryWrapper taskInfoQueryWrapper = createQueryWrapperByTaskId(workflowTaskId, false);

			WorkflowTask workflowTask = getWorkflowTask(taskInfoQueryWrapper);

			return workflowTask;
		} catch (WorkflowException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new WorkflowException("Cannot assign task", ex);
		}
	}

	@Override
	public WorkflowTask completeWorkflowTask(long companyId, long userId,
			long workflowTaskId, String transitionName, String comment,
			Map<String, Serializable> workflowContext) throws WorkflowException {
		identityService.setAuthenticatedUserId(idMappingService.getUserName(userId));

		String taskId = String.valueOf(workflowTaskId);
		Task task = taskService.createTaskQuery().taskId(taskId).singleResult();

		if (task == null) {
			throw new WorkflowException("Task " + workflowTaskId + " not found");
		}

		// FIXME: What is it?
		long groupId = 0;
		try {
			Object objGroupId = taskService.getVariable(task.getId(), "groupId");
			groupId = GetterUtil.getLong(objGroupId, 0l);
		} catch(NullPointerException e) {
			_log.error("taskId = " + taskId + " task is " + task);
			_log.error(e);
			return null;
		}

		// complete task
		Map<String, Object> vars = WorkflowUtil.convertFromContext(workflowContext);
		vars.put("outputTransition", transitionName); // Put transition name into outputTransition variable for later use in gateway
		taskService.complete(taskId, vars);

		// save log
		WorkflowLogEntry workflowLogEntry = new WorkflowLogEntry();
		workflowLogEntry.setType(WorkflowLog.TASK_COMPLETION);
		workflowLogEntry.setComment(comment);
		workflowLogEntry.setWorkflowTaskId(workflowTaskId);
		try {
			workflowLogEntry.setAssigneeUserId(Long.valueOf(task.getAssignee()));
		} catch (NumberFormatException e) {
			_log.warn("Assignee id is not of long value.");
		}
		workflowLogEntry.setState(task.getName());

		addWorkflowLogEntryToProcess(task, workflowLogEntry);

		// Find the next task

		TaskInfoQueryWrapper taskInfoQueryWrapper = new TaskInfoQueryWrapper(taskService.createTaskQuery());
		taskInfoQueryWrapper.getTaskInfoQuery().taskCandidateUser(String.valueOf(userId));

		List<WorkflowTask> workflowTasks = getWorkflowTasks(taskInfoQueryWrapper, 0, 1);
		if (workflowTasks.size() != 0) {
			return workflowTasks.get(0);
		}

		_log.info("There is no next task to user " + userId);
		return null;
	}

	@Override
	public List<String> getNextTransitionNames(long companyId, long userId,
			long workflowTaskId) throws WorkflowException {
		if (workflowTaskId != 0) {
			String taskId = String.valueOf(workflowTaskId);
			try {
				TaskFormData formData = processEngine.getFormService().getTaskFormData(taskId);

				List<FormProperty> properties = formData.getFormProperties();
				for (FormProperty property : properties) {
					if (property.getId().equals("outputTransition")) {
						// get values
						@SuppressWarnings("unchecked")
						Map<String, String> outputTransitions = (Map<String, String>)property.getType().getInformation("values");

						// create list from them
						List<String> result = new ArrayList<String>();
						result.addAll(outputTransitions.keySet());

						return result;
					}
				}
			} catch (Exception ex) {
				_log.warn("Cannot get next transitions: " + ex.getMessage());
				_log.debug("Cannot get next transitions", ex);
			}
		}

		// not found - task has only one output
		List<String> result = new ArrayList<String>(1);
		result.add("Done");

		return result;
	}

	@Override
	public long[] getPooledActorsIds(long companyId, long workflowTaskId)
			throws WorkflowException {
		String taskId = String.valueOf(workflowTaskId);

		List<IdentityLink> participations = new ArrayList<IdentityLink>();

		if (workflowTaskId != 0 && taskService.createTaskQuery().taskId(taskId).singleResult() != null) {
			participations = taskService.getIdentityLinksForTask(taskId);
		}

		Set<Long> userIds = new HashSet<Long>();

		for (IdentityLink participation : participations) {
			if(participation.getGroupId() != null) {
				List<org.activiti.engine.identity.User> users = liferayIdentityService.findUsersByGroup(companyId, participation.getGroupId());
				for (org.activiti.engine.identity.User user : users) {
					userIds.add(Long.valueOf(user.getId()));
				}
			} else if(participation.getUserId() != null){
				userIds.add(Long.valueOf(participation.getUserId()));
			}
		}

		long[] result = new long[userIds.size()];
		int i = 0;
		for (Long userId : userIds) {
			result[i++] = userId;
		}

		return result;
	}

	@Transactional
	@Override
	public WorkflowTask getWorkflowTask(long companyId, long workflowTaskId)
			throws WorkflowException {
		// TODO: do we have to use companyId to search task by id?
		TaskInfoQueryWrapper taskInfoQueryWrapper = createQueryWrapperByTaskId(workflowTaskId, false);

		WorkflowTask workflowTask = getWorkflowTask(taskInfoQueryWrapper);

		if (workflowTask != null) {
			return workflowTask;
		} else {
			_log.debug("Cannot find active task " + workflowTaskId + " , try to find in history");

			taskInfoQueryWrapper = createQueryWrapperByTaskId(workflowTaskId, true);
			workflowTask = getWorkflowTask(taskInfoQueryWrapper);
			if (workflowTask != null) {
				return workflowTask;
			} else {
				_log.warn("Cannot find task " + workflowTaskId);
				return null;
			}
		}
	}

	@Override
	public int getWorkflowTaskCount(long companyId, Boolean completed)
			throws WorkflowException {
		TaskInfoQueryWrapper taskInfoQueryWrapper = createQueryWrapper(companyId, completed);

		Long cnt = taskInfoQueryWrapper.getTaskInfoQuery().count();
		return cnt.intValue();
	}

	@Override
	public int getWorkflowTaskCountByRole(long companyId, long roleId,
			Boolean completed) throws WorkflowException {
		TaskInfoQueryWrapper taskInfoQueryWrapper = createQueryWrapperByRole(
				companyId, roleId, completed);

		Long count = taskInfoQueryWrapper.getTaskInfoQuery().count();
		return count.intValue();
	}

	@Deprecated
	@Override
	public int getWorkflowTaskCountBySubmittingUser(long companyId,
			long userId, Boolean completed) throws WorkflowException {
		_log.error("Method is not implemented"); // TODO
		// Who is submitting user?
		throw new UnsupportedOperationException();
	}

	@Override
	public int getWorkflowTaskCountByUser(long companyId, long userId,
			Boolean completed) throws WorkflowException {
		TaskInfoQueryWrapper taskInfoQueryWrapper = createQueryWrapper(
				companyId, 0L, userId, completed);

		Long count = taskInfoQueryWrapper.getTaskInfoQuery().count();
		return count.intValue();
	}

	@Override
	public int getWorkflowTaskCountByUserRoles(long companyId, long userId,
			Boolean completed) throws WorkflowException {
		TaskInfoQueryWrapper taskInfoQueryWrapper = createQueryWrapperByUserRoles(
				companyId, userId, completed);

		Long count = taskInfoQueryWrapper.getTaskInfoQuery().count();
		return count.intValue();
	}

	@Override
	public int getWorkflowTaskCountByWorkflowInstance(long companyId,
			Long userId, long workflowInstanceId, Boolean completed)
					throws WorkflowException {
		TaskInfoQueryWrapper taskInfoQueryWrapper = createQueryWrapperByWorkflowInstance(
				companyId, userId, workflowInstanceId, completed);

		Long count = taskInfoQueryWrapper.getTaskInfoQuery().count();
		return count.intValue();
	}

	@Override
	public List<WorkflowTask> getWorkflowTasks(long companyId,
			Boolean completed, int start, int end,
			OrderByComparator orderByComparator) throws WorkflowException {
		TaskInfoQueryWrapper taskInfoQueryWrapper = createQueryWrapper(companyId, completed);

		List<WorkflowTask> workflowTasks = getWorkflowTasks(taskInfoQueryWrapper, start, end, orderByComparator);

		return workflowTasks;
	}

	@Override
	public List<WorkflowTask> getWorkflowTasksByRole(long companyId,
			long roleId, Boolean completed, int start, int end,
			OrderByComparator orderByComparator) throws WorkflowException {
		TaskInfoQueryWrapper taskInfoQueryWrapper = createQueryWrapperByRole(
				companyId, roleId, completed);

		List<WorkflowTask> workflowTasks = getWorkflowTasks(taskInfoQueryWrapper, start, end, orderByComparator);

		return workflowTasks;
	}

	@Deprecated
	@Override
	public List<WorkflowTask> getWorkflowTasksBySubmittingUser(long companyId,
			long userId, Boolean completed, int start, int end,
			OrderByComparator orderByComparator) throws WorkflowException {
		_log.error("Method is not implemented"); // TODO
		// Who is submitting user?
		throw new UnsupportedOperationException();
	}

	@Override
	public List<WorkflowTask> getWorkflowTasksByUser(long companyId,
			long userId, Boolean completed, int start, int end,
			OrderByComparator orderByComparator) throws WorkflowException {

		TaskInfoQueryWrapper taskInfoQueryWrapper = createQueryWrapper(
				companyId, 0L, userId, completed);

		List<WorkflowTask> workflowTasks = getWorkflowTasks(taskInfoQueryWrapper, start, end, orderByComparator);

		return workflowTasks;
	}

	@Override
	public List<WorkflowTask> getWorkflowTasksByUserRoles(long companyId,
			long userId, Boolean completed, int start, int end,
			OrderByComparator orderByComparator) throws WorkflowException {

		TaskInfoQueryWrapper taskInfoQueryWrapper = createQueryWrapperByUserRoles(companyId, userId, completed);

		List<WorkflowTask> workflowTasks = getWorkflowTasks(taskInfoQueryWrapper, start, end, orderByComparator);

		return workflowTasks;
	}

	@Override
	public List<WorkflowTask> getWorkflowTasksByWorkflowInstance(
			long companyId, Long userId, long workflowInstanceId,
			Boolean completed, int start, int end,
			OrderByComparator orderByComparator) throws WorkflowException {
		TaskInfoQueryWrapper taskInfoQueryWrapper = createQueryWrapperByWorkflowInstance(
				companyId, userId, workflowInstanceId, completed);

		List<WorkflowTask> workflowTasks = getWorkflowTasks(taskInfoQueryWrapper, start, end, orderByComparator);

		return workflowTasks;
	}

	@Transactional
	@Override
	public List<WorkflowTask> search(long companyId, long userId,
			String keywords, Boolean completed, Boolean searchByUserRoles,
			int start, int end, OrderByComparator orderByComparator)
					throws WorkflowException {
		TaskInfoQueryWrapper taskInfoQueryWrapper;

		if (searchByUserRoles != null && searchByUserRoles == true) {
			taskInfoQueryWrapper = createQueryWrapperByUserRoles(
					companyId, userId, completed);
		} else {
			taskInfoQueryWrapper = createQueryWrapper(
					companyId, 0L, userId, completed);
		}

		Long groupId = null;

		// get serialized parameters map from keywords
		Map<String, Object> parameters = WorkflowUtil.convertValueToParameterValue(keywords);
		if (!parameters.equals(MapUtils.EMPTY_MAP)) {
			groupId = GetterUtil.getLong(parameters.get("groupId"));

			taskInfoQueryWrapper.getTaskInfoQuery().processVariableValueEquals(
					WorkflowConstants.CONTEXT_GROUP_ID, groupId);
		}

		List<WorkflowTask> workflowTasks = getWorkflowTasks(taskInfoQueryWrapper, start, end, orderByComparator);

		return workflowTasks;
	}

	@Override
	public List<WorkflowTask> search(long companyId, long userId,
			String taskName, String assetType, Long[] assetPrimaryKey,
			Date dueDateGT, Date dueDateLT, Boolean completed,
			Boolean searchByUserRoles, boolean andOperator, int start, int end,
			OrderByComparator orderByComparator) throws WorkflowException {
		String[] assetTypes =  new String[]{};
		if (StringUtils.isNotEmpty(assetType)) {
			assetTypes = new String[]{assetType};
		}
		
		TaskInfoQueryWrapper taskInfoQueryWrapper = createQueryWrapperSearch(
				companyId, userId, taskName, assetTypes,
				assetPrimaryKey, dueDateGT, dueDateLT, completed,
				searchByUserRoles);
		
		List<WorkflowTask> workflowTasks = getWorkflowTasks(taskInfoQueryWrapper, start, end, orderByComparator);

		return workflowTasks;
	}

	@Override
	public List<WorkflowTask> search(long companyId, long userId,
			String keywords, String[] assetTypes, Boolean completed,
			Boolean searchByUserRoles, int start, int end,
			OrderByComparator orderByComparator) throws WorkflowException {
		TaskInfoQueryWrapper taskInfoQueryWrapper = createQueryWrapperSearch(
				companyId, userId, null, assetTypes, null, null, null,
				completed, searchByUserRoles);

		List<WorkflowTask> workflowTasks = getWorkflowTasks(taskInfoQueryWrapper, start, end, orderByComparator);

		return workflowTasks;
	}

	@Override
	public int searchCount(long companyId, long userId, String keywords,
			Boolean completed, Boolean searchByUserRoles)
					throws WorkflowException {
		TaskInfoQueryWrapper taskInfoQueryWrapper;

		if (searchByUserRoles != null && searchByUserRoles == true) {
			taskInfoQueryWrapper = createQueryWrapperByUserRoles(
					companyId, userId, completed);
		} else {
			taskInfoQueryWrapper = createQueryWrapper(
					companyId, 0L, userId, completed);
		}

		Long groupId = null;

		// get serialized parameters map from keywords
		Map<String, Object> parameters = WorkflowUtil.convertValueToParameterValue(keywords);
		if (!parameters.equals(MapUtils.EMPTY_MAP)) {
			groupId = GetterUtil.getLong(parameters.get("groupId"));

			taskInfoQueryWrapper.getTaskInfoQuery().processVariableValueEquals(
					WorkflowConstants.CONTEXT_GROUP_ID, groupId);
		}

		Long count = taskInfoQueryWrapper.getTaskInfoQuery().count();
		return count.intValue();
	}

	@Override
	public int searchCount(long companyId, long userId, String taskName,
			String assetType, Long[] assetPrimaryKey, Date dueDateGT,
			Date dueDateLT, Boolean completed, Boolean searchByUserRoles,
			boolean andOperator) throws WorkflowException {
		String[] assetTypes =  new String[]{};
		if (StringUtils.isNotEmpty(assetType)) {
			assetTypes = new String[]{assetType};
		}

		TaskInfoQueryWrapper taskInfoQueryWrapper = createQueryWrapperSearch(
				companyId, userId, taskName, assetTypes,
				assetPrimaryKey, dueDateGT, dueDateLT, completed,
				searchByUserRoles);

		Long count = taskInfoQueryWrapper.getTaskInfoQuery().count();
		return count.intValue();
	}

	@Override
	public int searchCount(long companyId, long userId, String keywords,
			String[] assetTypes, Boolean completed, Boolean searchByUserRoles)
					throws WorkflowException {
		TaskInfoQueryWrapper taskInfoQueryWrapper = createQueryWrapperSearch(
				companyId, userId, null, assetTypes, null, null, null,
				completed, searchByUserRoles);

		Long count = taskInfoQueryWrapper.getTaskInfoQuery().count();
		return count.intValue();
	}

	@Override
	public WorkflowTask updateDueDate(long companyId, long userId,
			long workflowTaskId, String comment, Date dueDate)
					throws WorkflowException {
		identityService.setAuthenticatedUserId(idMappingService.getUserName(userId));

		String taskId = String.valueOf(workflowTaskId);
		Task task = taskService.createTaskQuery().taskId(taskId).singleResult();

		task.setDueDate(dueDate);
		taskService.saveTask(task);

		// save log
		WorkflowLogEntry workflowLogEntry = new WorkflowLogEntry();
		workflowLogEntry.setType(WorkflowLog.TASK_UPDATE);
		workflowLogEntry.setComment(comment);
		workflowLogEntry.setWorkflowTaskId(workflowTaskId);

		addWorkflowLogEntryToProcess(task, workflowLogEntry);

		// get new state of task
		TaskInfoQueryWrapper taskInfoQueryWrapper = createQueryWrapperByTaskId(workflowTaskId, false);

		WorkflowTask workflowTask = getWorkflowTask(taskInfoQueryWrapper);
		return workflowTask;
	}

}
