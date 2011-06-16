package net.emforge.activiti;

import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import net.emforge.activiti.dao.ProcessInstanceExtensionDao;
import net.emforge.activiti.engine.LiferayTaskService;
import net.emforge.activiti.identity.LiferayIdentityService;
import net.emforge.activiti.log.WorkflowLogEntry;
import net.emforge.activiti.log.WorkflowLogTypeMapperUtil;
import net.emforge.activiti.query.CustomHistoricTaskInstanceQuery;
import net.emforge.activiti.query.CustomHistoricTaskInstanceQueryImpl;
import net.emforge.activiti.query.CustomTaskQuery;
import net.emforge.activiti.query.CustomTaskQueryImpl;

import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.history.HistoricActivityInstanceQuery;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.TaskServiceImpl;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.workflow.DefaultWorkflowTask;
import com.liferay.portal.kernel.workflow.WorkflowException;
import com.liferay.portal.kernel.workflow.WorkflowHandler;
import com.liferay.portal.kernel.workflow.WorkflowHandlerRegistryUtil;
import com.liferay.portal.kernel.workflow.WorkflowLog;
import com.liferay.portal.kernel.workflow.WorkflowTask;
import com.liferay.portal.kernel.workflow.WorkflowTaskAssignee;
import com.liferay.portal.kernel.workflow.WorkflowTaskManager;
import com.liferay.portal.kernel.workflow.comparator.BaseWorkflowTaskDueDateComparator;
import com.liferay.portal.model.User;

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
	RepositoryService repositoryService;
	
	@Autowired
	IdMappingService idMappingService;
	@Autowired
	LiferayIdentityService liferayIdentityService;
	@Autowired
	ProcessInstanceExtensionDao processInstanceExtensionDao;
	@Autowired
	WorkflowInstanceManagerImpl workflowInstanceManager;
	
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
			return getWorkflowTask(task);
		} catch (WorkflowException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new WorkflowException("Cannot assign task", ex);
		}
	}

	@Override
	public WorkflowTask completeWorkflowTask(long companyId, long userId, long workflowTaskId, 
											 String transitionName, String comment,
											 Map<String, Serializable> context) throws WorkflowException {
		identityService.setAuthenticatedUserId(idMappingService.getUserName(userId));
		
		String taskId = String.valueOf(workflowTaskId);
		Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
		
		// complete task
		Map<String, Object> vars = WorkflowInstanceManagerImpl.convertFromContext(context);
		vars.put("outputTransition", transitionName); // Put transition name into outputTransition variable for later use in gateway
		taskService.complete(taskId, vars);

		// save log
		WorkflowLogEntry workflowLogEntry = new WorkflowLogEntry();
		workflowLogEntry.setType(WorkflowLog.TASK_COMPLETION);
		workflowLogEntry.setComment(comment);
		workflowLogEntry.setState(task.getName());
		
		addWorkflowLogEntryToProcess(task, workflowLogEntry);

		
		
		// TODO - find the next task
		return null;
	}

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
				_log.error("Cannot get next transitions", ex);
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

	@Override
	public int getWorkflowTaskCountByRole(long companyId, long roleId,
			Boolean completed) throws WorkflowException {
		_log.error("Method is not implemented"); // TODO
		return 0;
	}

	@Override
	public int getWorkflowTaskCountByUser(long companyId, long userId,
			Boolean completed) throws WorkflowException {
		_log.error("Method is not implemented"); // TODO
		return 0;
	}

	@Override
	public int getWorkflowTaskCountByUserRoles(long companyId, long userId,
			Boolean completed) throws WorkflowException {
		_log.error("Method is not implemented"); // TODO
		return 0;
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

	@Override
	public List<WorkflowTask> getWorkflowTasksByUser(long companyId,
			long userId, Boolean completed, int start, int end,
			OrderByComparator orderByComparator) throws WorkflowException {
		_log.error("Method is not implemented"); // TODO
		return null;
	}

	@Override
	public List<WorkflowTask> getWorkflowTasksByUserRoles(long companyId,
			long userId, Boolean completed, int start, int end,
			OrderByComparator orderByComparator) throws WorkflowException {
		_log.error("Method is not implemented"); // TODO
		return null;
	}

	@Transactional
	@Override
	public List<WorkflowTask> search(long companyId, long userId,
			String keywords, Boolean completed, Boolean searchByUserRoles,
			int start, int end, OrderByComparator orderByComparator)
			throws WorkflowException {
		if (userId == 0 || StringUtils.isNotEmpty(keywords)) {
			_log.warn("Method is partially implemented"); // TODO
		}
		
        if (searchByUserRoles != null && searchByUserRoles == true) {
        	if (completed == null || !completed) {
        		CustomTaskQuery taskQuery = createCustomTaskQuery().taskCandidateUser(idMappingService.getUserName(userId));

        		// is comparator specified
        		if (orderByComparator != null && orderByComparator instanceof BaseWorkflowTaskDueDateComparator) {
        			if (orderByComparator.isAscending()) {
        				taskQuery.orderByDueDate().asc();
        			} else {
        				taskQuery.orderByDueDate().desc();
        			}
        		}
        		
    			if ((start != QueryUtil.ALL_POS) && (end != QueryUtil.ALL_POS)) {
        			taskQuery.listPage(start, end - start);
        		}
        		
	            List<Task> list = taskQuery.list();
            
	            return getWorkflowTasks(list);
        	} else {
        		_log.warn("Method is partially implemented"); // TODO
        		return new ArrayList<WorkflowTask>();
        	}
        } else {
        	if (completed == null || !completed) {
        		CustomTaskQuery taskQuery = createCustomTaskQuery().taskAssignee(idMappingService.getUserName(userId));

        		// is comparator specified
        		if (orderByComparator != null && orderByComparator instanceof BaseWorkflowTaskDueDateComparator) {
        			if (orderByComparator.isAscending()) {
        				taskQuery.orderByDueDate().asc();
        			} else {
        				taskQuery.orderByDueDate().desc();
        			}
        		}
        		
    			if ((start != QueryUtil.ALL_POS) && (end != QueryUtil.ALL_POS)) {
        			taskQuery.listPage(start, end - start);
        		}
        		
	            List<Task> list = taskQuery.list();
            
	            return getWorkflowTasks(list);
        	} else {
        		// search for completed tasks in history service
        		CustomHistoricTaskInstanceQuery query = createCustomHistoricTaskInstanceQuery().taskAssignee(idMappingService.getUserName(userId));

        		if (orderByComparator != null) {
        			// TODO need to be implemented
        			_log.warn("Method is partially implemented");
	    		}
        		
        		if ((start != QueryUtil.ALL_POS) && (end != QueryUtil.ALL_POS)) {
        			query.listPage(start, end - start);
        		}
        		
        		List<HistoricTaskInstance> list = query.list();
        		
        		return getHistoryWorkflowTasks(list);
        	}
        }
	}

	@Override
	public int searchCount(long companyId, long userId, String keywords,
			Boolean completed, Boolean searchByUserRoles)
			throws WorkflowException {
		if (userId == 0 || StringUtils.isNotEmpty(keywords)) {
			_log.warn("Method is partially implemented"); // TODO
		}
		if (searchByUserRoles != null && searchByUserRoles == true) {
        	if (completed == null || !completed) {
        		CustomTaskQuery taskQuery = createCustomTaskQuery().taskCandidateUser(idMappingService.getUserName(userId));
	        	
        		Long count = taskQuery.count();
	    		return count.intValue();
        	} else {
        		_log.warn("Method is partially implemented"); // TODO
        		return 0;
        	}
        } else {
        	if (completed == null || !completed) {
        		CustomTaskQuery taskQuery = createCustomTaskQuery().taskAssignee(idMappingService.getUserName(userId));
	        	
	    		Long count = taskQuery.count();
	    		return count.intValue();
        	} else {
        		HistoricActivityInstanceQuery query = historyService.createHistoricActivityInstanceQuery().taskAssignee(idMappingService.getUserName(userId)).finished();
        		
        		Long count = query.count();
	    		return count.intValue();
        	}
        }
	}

	@Override
	public WorkflowTask updateDueDate(long companyId, long userId, long workflowTaskId, String comment, Date dueDate)
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

	@Override
	public List<WorkflowTask> search(long companyId, long userId,
			String taskName, String assetType, Date dueDateGT, Date dueDateLT,
			Boolean completed, Boolean searchByUserRoles, boolean andOperator,
			int start, int end, OrderByComparator orderByComparator)
			throws WorkflowException {
		if (dueDateGT != null || dueDateLT != null) {
			_log.warn("Method is partially implemented"); // TODO
		}
		
        if (searchByUserRoles != null && searchByUserRoles == true) {
        	if (completed == null || !completed) {
        		CustomTaskQuery taskQuery = createCustomTaskQuery().taskCandidateUser(idMappingService.getUserName(userId));;
        		// add conditions
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
        		
        		if ((start != QueryUtil.ALL_POS) && (end != QueryUtil.ALL_POS)) {
        			taskQuery.listPage(start, end - start);
        		}
        		
	            List<Task> list = taskQuery.list();
            
	            return getWorkflowTasks(list);
        	} else {
        		_log.warn("Method is partially implemented"); // TODO
        		return new ArrayList<WorkflowTask>();
        	}
        } else {
        	if (completed == null || !completed) {
        		CustomTaskQuery taskQuery = createCustomTaskQuery().taskAssignee(idMappingService.getUserName(userId));
        		// add conditions
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

        		if ((start != QueryUtil.ALL_POS) && (end != QueryUtil.ALL_POS)) {
        			taskQuery.listPage(start, end - start);
        		}
        		
	            List<Task> list = taskQuery.list();
            
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
        		
        		if ((start != QueryUtil.ALL_POS) && (end != QueryUtil.ALL_POS)) {
        			query.listPage(start, end - start);
        		}
        		
        		List<HistoricTaskInstance> list = query.list();
        		
        		return getHistoryWorkflowTasks(list);
        	}
        }
	}

	@Override
	public int searchCount(long companyId, long userId, String taskName,
			String assetType, Date dueDateGT, Date dueDateLT,
			Boolean completed, Boolean searchByUserRoles, boolean andOperator)
			throws WorkflowException {
		if (dueDateGT != null || dueDateLT != null) {
			_log.warn("Method is partially implemented"); // TODO
		}
		
		if (searchByUserRoles != null && searchByUserRoles == true) {
        	if (completed == null || !completed) {
        		CustomTaskQuery taskQuery = createCustomTaskQuery().taskCandidateUser(idMappingService.getUserName(userId));
        		// add conditions
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

	@Override
	public int getWorkflowTaskCountByWorkflowInstance(long companyId, Long userId, long workflowInstanceId, Boolean completed) throws WorkflowException {
		if (completed) {
			_log.warn("Method is partially implemented"); // TODO
			return 0;
		} else {
			TaskService taskService = processEngine.getTaskService();
			TaskQuery taskQuery = taskService.createTaskQuery();
			Long count = taskQuery.taskAssignee(idMappingService.getUserName(userId)).processInstanceId(idMappingService.getActivitiProcessInstanceId(workflowInstanceId)).count();
			
			return count.intValue();
		}
	}

	@Override
	public List<WorkflowTask> getWorkflowTasksByWorkflowInstance(long companyId, Long userId, long workflowInstanceId,
																 Boolean completed, int start, int end,
																 OrderByComparator orderByComparator) throws WorkflowException {
		if (completed) {
			_log.warn("Method is partially implemented"); // TODO
			return null;
		} else {
			TaskService taskService = processEngine.getTaskService();
			TaskQuery taskQuery = taskService.createTaskQuery();
			taskQuery.taskAssignee(idMappingService.getUserName(userId)).processInstanceId(idMappingService.getActivitiProcessInstanceId(workflowInstanceId));
			
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

	/** Convert jBPM Tasks to Liferay WorkflowTask
	 * 
	 * @param list
	 * @return
	 */
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

	/** Convert jBPM Task to Liferay WorkflowTask
	 * 
	 * @param task
	 * @return
	 */
	private WorkflowTask getWorkflowTask(Task task) throws WorkflowException {
		DefaultWorkflowTask workflowTask = new DefaultWorkflowTask();
		
		String processInstanceId = task.getProcessInstanceId();
		ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
		
		ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery();
        processDefinitionQuery.processDefinitionId(processInstance.getProcessDefinitionId());
		ProcessDefinition processDef =  processDefinitionQuery.singleResult();
		
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
		if (liferayProcessInstanceId == null) {
			// subprocess - they do not have liferay process instance - lets try to use original id
			// lets try to create it
			workflowInstanceManager.getWorkflowInstance(processInstance, null, null);
			liferayProcessInstanceId = idMappingService.getLiferayProcessInstanceId(processInstanceId);
			
			//throw new WorkflowException("Cannot get liferay process instance id by activity process instance " + processInstanceId);
		}
		workflowTask.setWorkflowInstanceId(liferayProcessInstanceId);

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
		
		workflowTask.setWorkflowTaskId(Long.valueOf(task.getId()));
		
		return workflowTask;
	}

	private List<WorkflowTask> getHistoryWorkflowTasks(List<HistoricTaskInstance> list) throws WorkflowException {
		List<WorkflowTask> result = new ArrayList<WorkflowTask>(list.size());
		
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
		ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery();
        processDefinitionQuery.processDefinitionId(processDefId);
		ProcessDefinition processDef =  processDefinitionQuery.singleResult();
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
