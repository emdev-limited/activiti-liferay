package net.emforge.activiti;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.emforge.activiti.dao.ProcessInstanceExtensionDao;
import net.emforge.activiti.engine.LiferayTaskService;
import net.emforge.activiti.identity.LiferayIdentityService;
import net.emforge.activiti.log.WorkflowLogEntry;
import net.emforge.activiti.query.CustomHistoricTaskInstanceQuery;
import net.emforge.activiti.query.CustomHistoricTaskInstanceQueryImpl;
import net.emforge.activiti.query.CustomTaskQuery;
import net.emforge.activiti.query.CustomTaskQueryImpl;

import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.history.HistoricActivityInstanceQuery;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.TaskServiceImpl;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.emforge.activiti.social.WorkflowTaskActivityKeys;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.workflow.DefaultWorkflowTask;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.kernel.workflow.WorkflowException;
import com.liferay.portal.kernel.workflow.WorkflowHandler;
import com.liferay.portal.kernel.workflow.WorkflowHandlerRegistryUtil;
import com.liferay.portal.kernel.workflow.WorkflowLog;
import com.liferay.portal.kernel.workflow.WorkflowTask;
import com.liferay.portal.kernel.workflow.WorkflowTaskAssignee;
import com.liferay.portal.kernel.workflow.WorkflowTaskManager;
import com.liferay.portal.kernel.workflow.comparator.BaseWorkflowTaskDueDateComparator;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.User;
import com.liferay.portlet.social.service.SocialActivityLocalServiceUtil;

@Service("workflowTaskManager")
public class WorkflowTaskManagerImpl implements WorkflowTaskManager {
	private static Log _log = LogFactoryUtil.getLog(WorkflowTaskManagerImpl.class);

	@Autowired
	ProcessEngine processEngine;
	@Autowired
	RuntimeService runtimeService;
	@Autowired
	IdentityService identityService;
	@Autowired
	TaskService taskService;
	@Autowired
	HistoryService historyService;
	
	@Autowired
	IdMappingService idMappingService;
	@Autowired
	LiferayIdentityService liferayIdentityService;
	@Autowired
	ProcessInstanceExtensionDao processInstanceExtensionDao;
	@Autowired
	WorkflowInstanceManagerExt workflowInstanceManager;
	@Autowired
	WorkflowDefinitionManagerExt workflowDefinitionManager;
	
	@Override
	public WorkflowTask assignWorkflowTaskToRole(long companyId, long userId,
			long workflowTaskId, long roleId, String comment, Date dueDate,
			Map<String, Serializable> context) throws WorkflowException {
		_log.error("Method is not implemented"); // TODO
		return null;
	}

	@Transactional
	@Override
	public WorkflowTask assignWorkflowTaskToUser(long companyId, long userId,
			long workflowTaskId, long assigneeUserId, String comment,
			Date dueDate, Map<String, Serializable> context)
			throws WorkflowException {
		try {
			identityService.setAuthenticatedUserId(idMappingService.getUserName(userId));
			
			String taskId = String.valueOf(workflowTaskId);
			Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
			
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
			task = taskService.createTaskQuery().taskId(taskId).singleResult();
			WorkflowTask workflowTask = getWorkflowTask(task);
			try {
				long groupId = GetterUtil.getLong((String)taskService.getVariable(task.getId(), "groupId"));
				
				// Add activity
				SocialActivityLocalServiceUtil.addActivity(
						userId, groupId, WorkflowTask.class.getName(),
						workflowTask.getWorkflowTaskId(), WorkflowTaskActivityKeys.ASSIGNE_WORKFLOW_TASK_TO_USER,
						comment, 0);				
			} catch(Exception e) {
				_log.error("SocialActivity was not created", e);
			}
			
			//return getWorkflowTask(task);
			return workflowTask;
		} catch (WorkflowException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new WorkflowException("Cannot assign task", ex);
		}
	}

	@Transactional
	@Override
	public WorkflowTask completeWorkflowTask(long companyId, long userId, long workflowTaskId, 
											 String transitionName, String comment,
											 Map<String, Serializable> context) throws WorkflowException {
		identityService.setAuthenticatedUserId(idMappingService.getUserName(userId));
		
		String taskId = String.valueOf(workflowTaskId);
		Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
		long groupId = GetterUtil.getLong((String)taskService.getVariable(task.getId(), "groupId"));
		// complete task
		Map<String, Object> vars = WorkflowInstanceManagerImpl.convertFromContext(context);
		vars.put("outputTransition", transitionName); // Put transition name into outputTransition variable for later use in gateway
		taskService.complete(taskId, vars);
		
		try {
			// Add activity
			SocialActivityLocalServiceUtil.addActivity(
					userId, groupId, WorkflowTask.class.getName(),
					workflowTaskId, WorkflowTaskActivityKeys.COMPLETE_WORKFLOW_TASK,
					comment, 0);				
		} catch(Exception e) {
			_log.error("SocialActivity was not created", e);
		}

		// save log
		WorkflowLogEntry workflowLogEntry = new WorkflowLogEntry();
		workflowLogEntry.setType(WorkflowLog.TASK_COMPLETION);
		workflowLogEntry.setComment(comment);
		workflowLogEntry.setState(task.getName());
		
		addWorkflowLogEntryToProcess(task, workflowLogEntry);
		
		// TODO - find the next task
		try {
			Task nextTask = taskService.createTaskQuery()
				.taskCandidateUser(String.valueOf(userId)).listPage(0, 1).get(0);
			return getWorkflowTask(nextTask);
		} catch(Exception e) {
			_log.info("There is no next task to user " + userId + ". " + e.getMessage());
		}
		return null;
	}

	@Transactional
	@SuppressWarnings("unchecked")
	@Override
	public List<String> getNextTransitionNames(long companyId, long userId, long workflowTaskId) throws WorkflowException {
		if (workflowTaskId != 0) {
			String taskId = String.valueOf(workflowTaskId);
			try {
				TaskFormData formData = processEngine.getFormService().getTaskFormData(taskId);
				
				List<FormProperty> properties = formData.getFormProperties();
				for (FormProperty property : properties) {
					if (property.getId().equals("outputTransition")) {
						// get values
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
	public long[] getPooledActorsIds(long companyId, long workflowTaskId) throws WorkflowException {
		String taskId = String.valueOf(workflowTaskId);
		
		List<IdentityLink> participations = new ArrayList<IdentityLink>();
		
		try {
			if (workflowTaskId != 0) {
				participations = taskService.getIdentityLinksForTask(taskId);
			}
		} catch (Exception ex) {
			// for completed tasks it will simple produce exception - ignore it
		}
		
		Set<Long> userIds = new HashSet<Long>();
		
		for (IdentityLink participation : participations) {
			if (StringUtils.isNotEmpty(participation.getGroupId())) {
				List<org.activiti.engine.identity.User> users = liferayIdentityService.findUsersByGroup(companyId, participation.getGroupId());
				for (org.activiti.engine.identity.User user : users) {
					userIds.add(Long.valueOf(user.getId()));
				}
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
	public WorkflowTask getWorkflowTask(long companyId, long workflowTaskId) throws WorkflowException {
		String taskId = String.valueOf(workflowTaskId);
		Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
		
		if (task != null) {
			return getWorkflowTask(task);
		} else {
			_log.debug("Cannot find active task " + workflowTaskId + " , try to find in history");
			
			HistoricTaskInstance historyTask = historyService.createHistoricTaskInstanceQuery().taskId(String.valueOf(workflowTaskId)).singleResult();
			if (historyTask != null) {
				return getHistoryWorkflowTask(historyTask);
			} else {
				_log.info("Cannot find task " + workflowTaskId);
				return null;
			}
			
		}
		
	}

	@Override
	public int getWorkflowTaskCount(long companyId, Boolean completed)
			throws WorkflowException {
		_log.error("Method is not implemented"); // TODO
		return 0;
	}

	@Transactional
	@Override
	public int getWorkflowTaskCountByRole(long companyId, long roleId,
			Boolean completed) throws WorkflowException {
		_log.error("Method is not implemented"); // TODO
		return 0;
	}

	@Transactional
	@Override
	public int getWorkflowTaskCountByUser(long companyId, long userId,
			Boolean completed) throws WorkflowException {
		return searchCount(companyId, userId, null, completed, false);
	}

	@Transactional	
	@Override
	public int getWorkflowTaskCountByUserRoles(long companyId, long userId,
			Boolean completed) throws WorkflowException {
		return searchCount(companyId, userId, null, completed, true);
	}

	@Override
	public List<WorkflowTask> getWorkflowTasks(long companyId,
			Boolean completed, int start, int end,
			OrderByComparator orderByComparator) throws WorkflowException {
		_log.error("Method is not implemented"); // TODO
		return null;
	}

	@Override
	public List<WorkflowTask> getWorkflowTasksByRole(long companyId,
			long roleId, Boolean completed, int start, int end,
			OrderByComparator orderByComparator) throws WorkflowException {
		_log.error("Method is not implemented"); // TODO
		return null;
	}

	@Transactional
	@Override
	public List<WorkflowTask> getWorkflowTasksByUser(long companyId, long userId, Boolean completed, int start, int end, OrderByComparator orderByComparator) throws WorkflowException {
		return search(companyId, userId, null, completed, false,
				start, end, orderByComparator);
	}

	@Transactional
	@Override
	public List<WorkflowTask> getWorkflowTasksByUserRoles(long companyId, long userId, Boolean completed, int start, int end,
			OrderByComparator orderByComparator) throws WorkflowException {
		return search(companyId, userId, null, completed, true,
				start, end, orderByComparator);
	}

	@Transactional
	@Override
	public List<WorkflowTask> search(long companyId, long userId,
			String keywords, Boolean completed, Boolean searchByUserRoles,
			int start, int end, OrderByComparator orderByComparator)
			throws WorkflowException {
		_log.debug("-----> Search Start1 " + start + " end " + end);
		Long groupId = null;
		if (StringUtils.isNotEmpty(keywords)) {
			try {
				groupId = new Long(keywords);
			} catch (Exception e) {
				
			}
		}
		
        if (searchByUserRoles != null && searchByUserRoles == true) {
        	if (completed == null || !completed) {
        		CustomTaskQuery taskQuery = createCustomTaskQuery().taskCandidateUser(idMappingService.getUserName(userId));

        		taskQuery.taskGroupId(groupId);
        		// is comparator specified
        		if (orderByComparator != null && orderByComparator instanceof BaseWorkflowTaskDueDateComparator) {
        			if (orderByComparator.isAscending()) {
        				taskQuery = taskQuery.orderByDueDate().asc();
        			} else {
        				taskQuery = taskQuery.orderByDueDate().desc();
        			}
        		}
        		List<Task> list = null;
    			if ((start != QueryUtil.ALL_POS) && (end != QueryUtil.ALL_POS)) {
    				list = taskQuery.listPage(start, end - start);
        		} else {
        			list = taskQuery.list();
        		}           
            
	            return getWorkflowTasks(list);
        	} else {
        		_log.warn("Method is partially implemented"); // TODO
        		return new ArrayList<WorkflowTask>();
        	}
        } else {
        	if (completed == null || !completed) {
        		CustomTaskQuery taskQuery = createCustomTaskQuery().taskAssignee(idMappingService.getUserName(userId));

        		taskQuery.taskGroupId(groupId);
        		// is comparator specified
        		if (orderByComparator != null && orderByComparator instanceof BaseWorkflowTaskDueDateComparator) {
        			if (orderByComparator.isAscending()) {
        				taskQuery = taskQuery.orderByDueDate().asc();
        			} else {
        				taskQuery = taskQuery.orderByDueDate().desc();
        			}
        		}
        		List<Task> list = null;
    			if ((start != QueryUtil.ALL_POS) && (end != QueryUtil.ALL_POS)) {
    				list = taskQuery.listPage(start, end - start);
        		} else {
        			list = taskQuery.list();
        		}

	            return getWorkflowTasks(list);
        	} else {
        		// search for completed tasks in history service
        		CustomHistoricTaskInstanceQuery query = createCustomHistoricTaskInstanceQuery().taskAssignee(idMappingService.getUserName(userId));

        		// TODO taskQuery.taskGroupId(groupId);
        		if (orderByComparator != null) {
        			// TODO need to be implemented
        			_log.warn("Method is partially implemented");
	    		}
        		List<HistoricTaskInstance> list = null;
        		if ((start != QueryUtil.ALL_POS) && (end != QueryUtil.ALL_POS)) {
        			list = query.listPage(start, end - start);
        		} else {
        			list = query.list();
        		}
        		

        		
        		return getHistoryWorkflowTasks(list);
        	}
        }
	}

	@Transactional
	@Override
	public int searchCount(long companyId, long userId, String keywords,
			Boolean completed, Boolean searchByUserRoles)
			throws WorkflowException {
		Long groupId = null;
		if (StringUtils.isNotEmpty(keywords)) {
			try {
				groupId = new Long(keywords);
			} catch (Exception e) {
				
			}
		}
		
		if (searchByUserRoles != null && searchByUserRoles == true) {
        	if (completed == null || !completed) {
        		CustomTaskQuery taskQuery = createCustomTaskQuery().taskCandidateUser(idMappingService.getUserName(userId));
        		taskQuery.taskGroupId(groupId);
        		
        		Long count = taskQuery.count();
	    		return count.intValue();
        	} else {
        		_log.warn("Method is partially implemented"); // TODO
        		return 0;
        	}
        } else {
        	if (completed == null || !completed) {
        		CustomTaskQuery taskQuery = createCustomTaskQuery().taskAssignee(idMappingService.getUserName(userId));
        		taskQuery.taskGroupId(groupId);
        		
	    		Long count = taskQuery.count();
	    		return count.intValue();
        	} else {
        		HistoricActivityInstanceQuery query = historyService.createHistoricActivityInstanceQuery().taskAssignee(idMappingService.getUserName(userId)).finished();
        		// TODO 
        		//taskQuery.taskGroupId(groupId);
        		
        		Long count = query.count();
	    		return count.intValue();
        	}
        }
	}

	@Transactional
	@Override
	public WorkflowTask updateDueDate(long companyId, long userId, long workflowTaskId, String comment, Date dueDate)
			throws WorkflowException {
		identityService.setAuthenticatedUserId(idMappingService.getUserName(userId));
		
		String taskId = String.valueOf(workflowTaskId);
		Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
		
		task.setDueDate(dueDate);
		taskService.saveTask(task);
		
		try {
			long groupId = GetterUtil.getLong((String)taskService.getVariable(task.getId(), "groupId"));
			
			// Add activity
			SocialActivityLocalServiceUtil.addActivity(
					userId, groupId, WorkflowTask.class.getName(),
					workflowTaskId, WorkflowTaskActivityKeys.UPDATE_DUE_DATE,
					comment, 0);				
		} catch(Exception e) {
			_log.error("SocialActivity does not create", e);
		}
		
		// save log
		WorkflowLogEntry workflowLogEntry = new WorkflowLogEntry();
		workflowLogEntry.setType(WorkflowLog.TASK_UPDATE);
		workflowLogEntry.setComment(comment);
		
		addWorkflowLogEntryToProcess(task, workflowLogEntry);
		
		task = taskService.createTaskQuery().taskId(taskId).singleResult();
		return getWorkflowTask(task);
	}

	@Override
	public int getWorkflowTaskCountBySubmittingUser(long companyId,
			long userId, Boolean completed) throws WorkflowException {
		_log.error("Method is not implemented"); // TODO
		return 0;
	}

	@Override
	public List<WorkflowTask> getWorkflowTasksBySubmittingUser(long companyId,
			long userId, Boolean completed, int start, int end,
			OrderByComparator orderByComparator) throws WorkflowException {
		_log.error("Method is not implemented"); // TODO
		return null;
	}

	/**
	 * parameter Long[] assetPrimaryKey added by Maxx
	 */
	@Override
	public List<WorkflowTask> search(long companyId, long userId,
			String taskName, String assetType, Long[] assetPrimaryKey, Date dueDateGT, Date dueDateLT,
			Boolean completed, Boolean searchByUserRoles, boolean andOperator,
			int start, int end, OrderByComparator orderByComparator)
			throws WorkflowException {
		_log.debug("-----> Search Start2 " + start + " end " + end);
		if (dueDateGT != null || dueDateLT != null || assetPrimaryKey != null) {
			_log.warn("Method is partially implemented"); // TODO
		}		
		
        if (searchByUserRoles != null && searchByUserRoles == true) {
        	if (completed == null || !completed) {
        		CustomTaskQuery taskQuery = createCustomTaskQuery().taskCandidateUser(idMappingService.getUserName(userId));;
        		// add conditions
        		if (companyId > 0) {
        			taskQuery.taskCompanyId(companyId);
        		}
        		if (StringUtils.isNotEmpty(taskName)) {
        			taskQuery.taskNameLike(taskName);
        		}
        		if (StringUtils.isNotEmpty(assetType)) {
        			taskQuery.taskEntryClassName(getAssetClassName(assetType));
        		}

        		if (orderByComparator != null && orderByComparator instanceof BaseWorkflowTaskDueDateComparator) {
        			if (orderByComparator.isAscending()) {
        				taskQuery.orderByDueDate().asc();
        			} else {
        				taskQuery.orderByDueDate().desc();
        			}
        		}
        		List<Task> list = null;
        		if ((start != QueryUtil.ALL_POS) && (end != QueryUtil.ALL_POS)) {
        			list = taskQuery.listPage(start, end - start);
        		} else {
        			list = taskQuery.list();
        		}	            
            
	            return getWorkflowTasks(list);
        	} else {
        		_log.warn("Method is partially implemented"); // TODO
        		return new ArrayList<WorkflowTask>();
        	}
        } else {
        	if (completed == null || !completed) {
        		CustomTaskQuery taskQuery = createCustomTaskQuery().taskAssignee(idMappingService.getUserName(userId));
        		// add conditions
        		if (companyId > 0) {
        			taskQuery.taskCompanyId(companyId);
        		}
        		if (StringUtils.isNotEmpty(taskName)) {
        			taskQuery.taskNameLike(taskName);
        		}
        		if (StringUtils.isNotEmpty(assetType)) {
        			taskQuery.taskEntryClassName(getAssetClassName(assetType));
        		}

        		if (orderByComparator != null && orderByComparator instanceof BaseWorkflowTaskDueDateComparator) {
        			if (orderByComparator.isAscending()) {
        				taskQuery.orderByDueDate().asc();
        			} else {
        				taskQuery.orderByDueDate().desc();
        			}
        		}
        		List<Task> list = null;
        		if ((start != QueryUtil.ALL_POS) && (end != QueryUtil.ALL_POS)) {
        			list = taskQuery.listPage(start, end - start);
        		} else {
        			list = taskQuery.list();
        		}	  
            
	            return getWorkflowTasks(list);
        	} else {
        		// search for completed tasks in history service
        		CustomHistoricTaskInstanceQuery query = createCustomHistoricTaskInstanceQuery().taskAssignee(idMappingService.getUserName(userId));
        		// add conditions
        		if (StringUtils.isNotEmpty(taskName)) {
        			query.taskNameLike(taskName);
        		}
        		if (StringUtils.isNotEmpty(assetType)) {
        			query.taskEntryClassName(getAssetClassName(assetType));
        		}
        		
        		if (orderByComparator != null) {
        			_log.warn("Method is partially implemented"); // TODO
	    		}
        		List<HistoricTaskInstance> list = null;
        		if ((start != QueryUtil.ALL_POS) && (end != QueryUtil.ALL_POS)) {
        			list = query.listPage(start, end - start);
        		} else {
        			list = query.list();
        		}
        		
        		return getHistoryWorkflowTasks(list);
        	}
        }
	}
	
	/**
	 * added by Alex Zhdanov
	 */
	@Transactional
	@Override
	public List<WorkflowTask> search(
			long companyId, long userId, String keywords, String[] assetTypes,
			Boolean completed, Boolean searchByUserRoles, int start, int end,
			OrderByComparator orderByComparator)
		throws WorkflowException {
		_log.debug("-----> Search Start3 " + start + " end " + end);
		
		List<String> listAssetTypes = new ArrayList<String>();
		if (assetTypes != null) {
			for (int i = 0; i < assetTypes.length; i++) {
				listAssetTypes.add(getAssetClassName(assetTypes[i]));
			}
		}
		
        if (searchByUserRoles != null && searchByUserRoles == true) {
        	if (completed == null || !completed) {
        		CustomTaskQuery taskQuery = createCustomTaskQuery().taskCandidateUser(idMappingService.getUserName(userId));;
        		if (companyId > 0) {
        			taskQuery.taskCompanyId(companyId);
        		}
        		if (listAssetTypes.size() > 0) {
        			taskQuery.taskEntryClassNames(listAssetTypes);
        		}

        		if (orderByComparator != null && orderByComparator instanceof BaseWorkflowTaskDueDateComparator) {
        			if (orderByComparator.isAscending()) {
        				taskQuery.orderByDueDate().asc();
        			} else {
        				taskQuery.orderByDueDate().desc();
        			}
        		}
        		List<Task> list = null;
        		if ((start != QueryUtil.ALL_POS) && (end != QueryUtil.ALL_POS)) {
        			_log.debug("paged query 1");
        			list = taskQuery.listPage(start, end - start);
        		} else {
        			list = taskQuery.list();
        		}	            
            
	            return getWorkflowTasks(list);
        	} else {
        		_log.warn("Method is partially implemented"); // TODO
        		return new ArrayList<WorkflowTask>();
        	}
        } else {
        	if (completed == null || !completed) {
        		CustomTaskQuery taskQuery = createCustomTaskQuery().taskAssignee(idMappingService.getUserName(userId));
        		if (companyId > 0) {
        			taskQuery.taskCompanyId(companyId);
        		}
        		if (listAssetTypes.size() > 0) {
        			taskQuery.taskEntryClassNames(listAssetTypes);
        		}

        		if (orderByComparator != null && orderByComparator instanceof BaseWorkflowTaskDueDateComparator) {
        			if (orderByComparator.isAscending()) {
        				taskQuery.orderByDueDate().asc();
        			} else {
        				taskQuery.orderByDueDate().desc();
        			}
        		}
        		List<Task> list = null;
        		if ((start != QueryUtil.ALL_POS) && (end != QueryUtil.ALL_POS)) {
        			_log.debug("paged query 2");
        			list = taskQuery.listPage(start, end - start);
        		} else {
        			list = taskQuery.list();
        		}	  
            
	            return getWorkflowTasks(list);
        	} else {
        		// search for completed tasks in history service
        		CustomHistoricTaskInstanceQuery query = createCustomHistoricTaskInstanceQuery().taskAssignee(idMappingService.getUserName(userId));
        		
        		 //TODO
        		/*if (listAssetTypes.size() > 0) {
        			query.taskEntryClassNames(listAssetTypes);
        		}*/
        		
        		if (orderByComparator != null) {
        			_log.warn("Method is partially implemented"); // TODO
	    		}
        		List<HistoricTaskInstance> list = null;
        		if ((start != QueryUtil.ALL_POS) && (end != QueryUtil.ALL_POS)) {
        			_log.debug("paged query 3");
        			list = query.listPage(start, end - start);
        		} else {
        			list = query.list();
        		}
        		
        		return getHistoryWorkflowTasks(list);
        	}
        }
	}

	/**
	 * parameter Long[] assetPrimaryKey added by Maxx
	 */
	@Transactional
	@Override
	public int searchCount(long companyId, long userId, String taskName,
			String assetType, Long[] assetPrimaryKey, Date dueDateGT, Date dueDateLT,
			Boolean completed, Boolean searchByUserRoles, boolean andOperator)
			throws WorkflowException {
		if (dueDateGT != null || dueDateLT != null || assetPrimaryKey != null) {
			_log.warn("Method is partially implemented"); // TODO
		}
		
		if (searchByUserRoles != null && searchByUserRoles == true) {
        	if (completed == null || !completed) {
        		CustomTaskQuery taskQuery = createCustomTaskQuery().taskCandidateUser(idMappingService.getUserName(userId));
        		// add conditions
        		if (companyId > 0) {
        			taskQuery.taskCompanyId(companyId);
        		}
        		if (StringUtils.isNotEmpty(taskName)) {
        			taskQuery.taskNameLike(taskName);
        		}
        		if (StringUtils.isNotEmpty(assetType)) {
        			taskQuery.taskEntryClassName(getAssetClassName(assetType));
        		}

        		Long count = taskQuery.count();
	    		return count.intValue();
        	} else {
        		_log.warn("Method is partially implemented"); // TODO
        		return 0;
        	}
        } else {
        	if (completed == null || !completed) {
        		CustomTaskQuery taskQuery = createCustomTaskQuery().taskAssignee(idMappingService.getUserName(userId));
        		// add conditions
        		if (companyId > 0) {
        			taskQuery.taskCompanyId(companyId);
        		}
        		if (StringUtils.isNotEmpty(taskName)) {
        			taskQuery.taskNameLike(taskName);
        		}
        		if (StringUtils.isNotEmpty(assetType)) {
        			taskQuery.taskEntryClassName(getAssetClassName(assetType));
        		}
        			        	
	    		Long count = taskQuery.count();
	    		return count.intValue();
        	} else {
        		// search for completed tasks in history service
        		CustomHistoricTaskInstanceQuery query = createCustomHistoricTaskInstanceQuery().taskAssignee(idMappingService.getUserName(userId));
        		// add conditions
        		if (StringUtils.isNotEmpty(taskName)) {
        			query.taskNameLike(taskName);
        		}
        		if (StringUtils.isNotEmpty(assetType)) {
        			query.taskEntryClassName(getAssetClassName(assetType));
        		}
        		
        		Long count = query.count();
	    		return count.intValue();
        	}
        }
	}
	
	/**
	 * added by Maxx
	 */
	@Transactional
	@Override
	public int searchCount(
			long companyId, long userId, String keywords, String[] assetTypes,
			Boolean completed, Boolean searchByUserRoles)
		throws WorkflowException {
		
		List<String> listAssetTypes = new ArrayList<String>();
		if (assetTypes != null) {
			for (int i = 0; i < assetTypes.length; i++) {
				listAssetTypes.add(getAssetClassName(assetTypes[i]));
			}
		}
		
		if (searchByUserRoles != null && searchByUserRoles == true) {
        	if (completed == null || !completed) {
        		
        		CustomTaskQuery taskQuery = createCustomTaskQuery().taskCandidateUser(idMappingService.getUserName(userId));
        		if (companyId > 0) {
        			taskQuery.taskCompanyId(companyId);
        		}
        		if (listAssetTypes.size() > 0) {
        			taskQuery.taskEntryClassNames(listAssetTypes);
        		}

        		Long count = taskQuery.count();
	    		return count.intValue();
        	} else {
        		_log.warn("Method is partially implemented"); // TODO
        		return 0;
        	}
        } else {
        	if (completed == null || !completed) {
        		CustomTaskQuery taskQuery = createCustomTaskQuery().taskAssignee(idMappingService.getUserName(userId));
        		if (companyId > 0) {
        			taskQuery.taskCompanyId(companyId);
        		}
        		if (listAssetTypes.size() > 0) {
        			taskQuery.taskEntryClassNames(listAssetTypes);
        		}
        			        	
	    		Long count = taskQuery.count();
	    		return count.intValue();
        	} else {
        		// search for completed tasks in history service
        		CustomHistoricTaskInstanceQuery query = createCustomHistoricTaskInstanceQuery().taskAssignee(idMappingService.getUserName(userId));
        		
        		// TODO
        		/*if (listAssetTypes.size() > 0) {
        			taskQuery.taskEntryClassNames(listAssetTypes);
        		}*/
        		
        		Long count = query.count();
	    		return count.intValue();
        	}
        }
	}

	@Transactional
	@Override
	public int getWorkflowTaskCountByWorkflowInstance(long companyId, Long userId, long workflowInstanceId, Boolean completed) throws WorkflowException {
		if (completed == null || completed) {
			Long count = historyService.createHistoricTaskInstanceQuery()
				.processInstanceId(idMappingService.getActivitiProcessInstanceId(workflowInstanceId)).count();
			return count.intValue();
			
		} else {
			TaskService taskService = processEngine.getTaskService();
			TaskQuery taskQuery = taskService.createTaskQuery();
			Long count = taskQuery.taskAssignee(idMappingService.getUserName(userId)).processInstanceId(idMappingService.getActivitiProcessInstanceId(workflowInstanceId)).count();
			
			return count.intValue();
		}
	}

	@Transactional
	@Override
	public List<WorkflowTask> getWorkflowTasksByWorkflowInstance(long companyId, Long userId, long workflowInstanceId,
																 Boolean completed, int start, int end,
																 OrderByComparator orderByComparator) throws WorkflowException {
		if (completed == null || completed) {
			List<HistoricTaskInstance> hiList = historyService.createHistoricTaskInstanceQuery().processInstanceId(idMappingService.getActivitiProcessInstanceId(workflowInstanceId)).list();
			List<WorkflowTask> result = new ArrayList<WorkflowTask>(hiList.size());
			
			for (HistoricTaskInstance hiTask : hiList) {
				try {
					result.add(getWorkflowTask(companyId, hiTask));
				} catch (Exception ex) {
					_log.warn("Cannot convert Activiti task " + hiTask.getId() + " into Liferay: " + ex);
					_log.debug("Cannot convert Activiti task into Liferay", ex);
				}
			}
			
			return result;
			// return null;
		} else {
			TaskService taskService = processEngine.getTaskService();
			TaskQuery taskQuery = taskService.createTaskQuery();
			
			if (userId != 0l) {
				taskQuery.taskAssignee(idMappingService.getUserName(userId));
			}
			
			taskQuery.processInstanceId(idMappingService.getActivitiProcessInstanceId(workflowInstanceId));
			
			/* TODO Ordering is not supported
			if (orderByComparator != null) {
    			if (orderByComparator.getOrderByFields().length > 1) {
    				_log.warn("Method is partially implemented");
    			} else {
    				if (orderByComparator.isAscending()) {
    					taskQuery.orderAsc(orderByComparator.getOrderByFields()[0]);
    				} else {
    					taskQuery.orderDesc(orderByComparator.getOrderByFields()[0]);
    				}
    			}
    		}
    		*/
			
    		if ((start != QueryUtil.ALL_POS) && (end != QueryUtil.ALL_POS)) {
    			taskQuery.listPage(start, end - start);
    		}
            
            List<Task> list = taskQuery.list();
            
    		return getWorkflowTasks(list);
		}
	}

	@Transactional
	private WorkflowTask getWorkflowTask(long companyId, HistoricTaskInstance task) throws WorkflowException {
		DefaultWorkflowTask workflowTask = new DefaultWorkflowTask();
		
		String processInstanceId = task.getProcessInstanceId();
		ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
		
		ProcessDefinition processDef = workflowDefinitionManager.getProcessDefinition(task.getProcessDefinitionId());
		
		// TODO setAsynchronous(!task.isBlocking());
		// TODO setCompletionDate(taskInstance.getEnd());
		workflowTask.setCreateDate(task.getStartTime());
		workflowTask.setDescription(task.getDescription());
		workflowTask.setName(task.getName());
		
		if ("completed".equals(task.getDeleteReason())) {
			Map<String, Serializable> optionalAttributes = new HashMap<String, Serializable>();
			optionalAttributes.put("deleteReason", task.getDeleteReason());
			workflowTask.setOptionalAttributes(optionalAttributes);
			workflowTask.setCompletionDate(task.getEndTime());
		}
		
		Map<String, Object> vars = runtimeService.getVariables(processInstanceId);
		
		workflowTask.setDueDate(task.getDueDate() != null ? task.getDueDate() : (Date)vars.get("dueDate")); // keep getting dueDate from vars for backward compatibility
		
		workflowTask.setOptionalAttributes(WorkflowInstanceManagerImpl.convertFromVars(vars));
		
		workflowTask.setWorkflowDefinitionId(idMappingService.getLiferayWorkflowDefinitionId(processDef.getId()));
		
		workflowTask.setWorkflowDefinitionName(processDef.getName());
		workflowTask.setWorkflowDefinitionVersion(processDef.getVersion());
		Long liferayProcessInstanceId = idMappingService.getLiferayProcessInstanceId(processInstanceId);
		if (liferayProcessInstanceId == null) {
			// subprocess - they do not have liferay process instance - lets try to use original id
			// lets try to create it
			workflowInstanceManager.getWorkflowInstance(processInstance, null, null);
			liferayProcessInstanceId = idMappingService.getLiferayProcessInstanceId(processInstanceId);
			
			//throw new WorkflowException("Cannot get liferay process instance id by activity process instance " + processInstanceId);
		}
		workflowTask.setWorkflowInstanceId(liferayProcessInstanceId);
		
		/*
		long groupId = GetterUtil.getLong((String)taskService.getVariable(task.getId(), "groupId"));
		*/
		
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
			
			for (IdentityLink participation : participations) {
				if (StringUtils.isNotEmpty(participation.getGroupId())) {
					Role role = liferayIdentityService.findRole(companyId, participation.getGroupId());
					WorkflowTaskAssignee workflowTaskAssignee = new WorkflowTaskAssignee(
							Role.class.getName(), role.getRoleId());
					workflowTaskAssignees.add(workflowTaskAssignee);
				}
			}
			workflowTask.setWorkflowTaskAssignees(workflowTaskAssignees);
		}
		
		workflowTask.setWorkflowTaskId(Long.valueOf(task.getId()));
		
		return workflowTask;
	}

	private WorkflowTask getWorkflowTask(HistoricTaskInstance task) throws WorkflowException {
		DefaultWorkflowTask workflowTask = new DefaultWorkflowTask();
		
		String processInstanceId = task.getProcessInstanceId();
		ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
		
		ProcessDefinition processDef = workflowDefinitionManager.getProcessDefinition(task.getProcessDefinitionId());
		
		// TODO setAsynchronous(!task.isBlocking());
		// TODO setCompletionDate(taskInstance.getEnd());
		workflowTask.setCreateDate(task.getStartTime());
		workflowTask.setDescription(task.getDescription());
		workflowTask.setName(task.getName());
		
		Map<String, Object> vars = runtimeService.getVariables(processInstanceId);
		
		workflowTask.setDueDate(task.getDueDate() != null ? task.getDueDate() : (Date)vars.get("dueDate")); // keep getting dueDate from vars for backward compatibility
		
		workflowTask.setOptionalAttributes(WorkflowInstanceManagerImpl.convertFromVars(vars));
		
		workflowTask.setWorkflowDefinitionId(idMappingService.getLiferayWorkflowDefinitionId(processDef.getId()));
		
		workflowTask.setWorkflowDefinitionName(processDef.getName());
		workflowTask.setWorkflowDefinitionVersion(processDef.getVersion());
		Long liferayProcessInstanceId = idMappingService.getLiferayProcessInstanceId(processInstanceId);
		if (liferayProcessInstanceId == null) {
			// subprocess - they do not have liferay process instance - lets try to use original id
			// lets try to create it
			workflowInstanceManager.getWorkflowInstance(processInstance, null, null);
			liferayProcessInstanceId = idMappingService.getLiferayProcessInstanceId(processInstanceId);
			
			//throw new WorkflowException("Cannot get liferay process instance id by activity process instance " + processInstanceId);
		}
		workflowTask.setWorkflowInstanceId(liferayProcessInstanceId);
		
		long companyId = GetterUtil.getLong((String)taskService.getVariable(task.getId(), "companyId"));
		/*
		long groupId = GetterUtil.getLong((String)taskService.getVariable(task.getId(), "groupId"));
		*/
		
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
			
			for (IdentityLink participation : participations) {
				if (StringUtils.isNotEmpty(participation.getGroupId())) {
					Role role = liferayIdentityService.findRole(companyId, participation.getGroupId());
					WorkflowTaskAssignee workflowTaskAssignee = new WorkflowTaskAssignee(
							Role.class.getName(), role.getRoleId());
					workflowTaskAssignees.add(workflowTaskAssignee);
				}
			}
			workflowTask.setWorkflowTaskAssignees(workflowTaskAssignees);
		}
		
		workflowTask.setWorkflowTaskId(Long.valueOf(task.getId()));
		
		return workflowTask;
	}

	/** Convert jBPM Tasks to Liferay WorkflowTask
	 * 
	 * @param list
	 * @return
	 */
	@Transactional
	private List<WorkflowTask> getWorkflowTasks(List<Task> list) {
		List<WorkflowTask> result = new ArrayList<WorkflowTask>(list.size());
		
		for (Task task : list) {
			try {
				result.add(getWorkflowTask(task));
			} catch (Exception ex) {
				_log.warn("Cannot convert Activiti task " + task.getId() + " into Liferay: " + ex);
				_log.debug("Cannot convert Activiti task into Liferay", ex);
			}
		}
		
		return result;
	}

	/** Convert Activiti Task to Liferay WorkflowTask
	 * 
	 * @param task
	 * @return
	 */
	@Transactional
	private WorkflowTask getWorkflowTask(Task task) throws WorkflowException {

		DefaultWorkflowTask workflowTask = new DefaultWorkflowTask();
		
		String processInstanceId = task.getProcessInstanceId();
		
		ProcessDefinition processDef = workflowDefinitionManager.getProcessDefinition(task.getProcessDefinitionId());

		// TODO setAsynchronous(!task.isBlocking());
		// TODO setCompletionDate(taskInstance.getEnd());
		workflowTask.setCreateDate(task.getCreateTime());
		workflowTask.setDescription(task.getDescription());
		workflowTask.setName(task.getName());
		
		Map<String, Object> vars = runtimeService.getVariables(processInstanceId);
		workflowTask.setDueDate(task.getDueDate() != null ? task.getDueDate() : (Date)vars.get("dueDate")); // keep getting dueDate from vars for backward compatibility
		
		workflowTask.setOptionalAttributes(WorkflowInstanceManagerImpl.convertFromVars(vars));
		
		
		workflowTask.setWorkflowDefinitionId(idMappingService.getLiferayWorkflowDefinitionId(processDef.getId()));
		
		workflowTask.setWorkflowDefinitionName(processDef.getName());
		workflowTask.setWorkflowDefinitionVersion(processDef.getVersion());
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
		workflowTask.setWorkflowInstanceId(liferayProcessInstanceId);

		
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
			
			long companyId = GetterUtil.getLong((String)vars.get(WorkflowConstants.CONTEXT_COMPANY_ID));

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

	private List<WorkflowTask> getHistoryWorkflowTasks(List<HistoricTaskInstance> list) throws WorkflowException {
		List<WorkflowTask> result = new ArrayList<WorkflowTask>(list.size());
		_log.debug("----->  size " + list.size());
		for (HistoricTaskInstance task : list) {
			WorkflowTask workflowTask = getHistoryWorkflowTask(task);
			result.add(workflowTask);
		}
		
		return result;
	}
	
	private WorkflowTask getHistoryWorkflowTask(HistoricTaskInstance task) throws WorkflowException {
		DefaultWorkflowTask workflowTask = new DefaultWorkflowTask();
		
		// TODO setAsynchronous(!task.isBlocking());
		workflowTask.setCreateDate(task.getStartTime());
		workflowTask.setCompletionDate(task.getEndTime());
		
		// TODO workflowTask.setDescription(task.getDescription());
		// TODO workflowTask.setDueDate(task.getDuedate());
		
		workflowTask.setName(task.getName());
					
		// get process def from activity
		String processDefId = task.getProcessDefinitionId();
		
		ProcessDefinition processDef = workflowDefinitionManager.getProcessDefinition(task.getProcessDefinitionId());
		
		workflowTask.setWorkflowDefinitionId(idMappingService.getLiferayWorkflowDefinitionId(processDefId));
		workflowTask.setWorkflowDefinitionName(processDef.getName());
		workflowTask.setWorkflowDefinitionVersion(processDef.getVersion());
		
		/* Not applicable for history task
		Set<String> variableNames = taskService.getVariableNames(task.getId());
		Map<String, Object> vars = taskService.getVariables(task.getId(), variableNames);
		workflowTask.setOptionalAttributes(WorkflowInstanceManagerImpl.convertFromVars(vars));
		*/
		workflowTask.setWorkflowInstanceId(idMappingService.getLiferayProcessInstanceId(task.getProcessInstanceId()));

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
	
	/** 
	 * Add workflow log entry to process.
	 * 
	 * @param taskId
	 * @param comment
	 */
	private void addWorkflowLogEntryToProcess(Task task, WorkflowLogEntry workflowLogEntry) {
		
		if (taskService instanceof LiferayTaskService) {
			LiferayTaskService liferayTaskService = (LiferayTaskService) taskService;
			liferayTaskService.addWorkflowLogEntry(task.getId(), task.getProcessInstanceId(), workflowLogEntry);
		}
	}
	
//	/** 
//	 * Add workflow log entry to task.
//	 * 
//	 * @param taskId
//	 * @param comment
//	 */
//	private void addWorkflowLogEntryToTask(Task task, WorkflowLogEntry workflowLogEntry) {
//		
//		if (taskService instanceof LiferayTaskService) {
//			LiferayTaskService liferayTaskService = (LiferayTaskService) taskService;
//			liferayTaskService.addWorkflowLogEntry(task.getId(), null, workflowLogEntry);
//		}
//	}

	
	private String getAssetClassName(String assetType) {
		String type = assetType.substring(1, assetType.length() - 1);
		
		// find it in workflow handlers
		List<WorkflowHandler> workflowHhandlers = WorkflowHandlerRegistryUtil.getWorkflowHandlers();

		for (WorkflowHandler workflowHandler : workflowHhandlers) {
			String workflowHandlerType = workflowHandler.getType(LocaleUtil.getDefault());
			
			// compare by handler type
			if (workflowHandlerType.equalsIgnoreCase(type)) {
				return workflowHandler.getClassName();
			}
		}
		
		return assetType;
	}

	protected CustomTaskQuery createCustomTaskQuery() {
		TaskServiceImpl serviceImpl = (TaskServiceImpl)taskService;
	    return new CustomTaskQueryImpl(serviceImpl.getCommandExecutor());
	}

	protected CustomHistoricTaskInstanceQuery createCustomHistoricTaskInstanceQuery() {
		TaskServiceImpl serviceImpl = (TaskServiceImpl)taskService;
	    return new CustomHistoricTaskInstanceQueryImpl(serviceImpl.getCommandExecutor());
	}
}
