package net.emforge.activiti;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import net.emforge.activiti.log.WorkflowLogEntry;
import net.emforge.activiti.spring.Initializable;

import org.activiti.engine.TaskService;
import org.activiti.engine.impl.persistence.entity.CommentEntity;
import org.activiti.engine.task.Comment;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.workflow.DefaultWorkflowLog;
import com.liferay.portal.kernel.workflow.WorkflowException;
import com.liferay.portal.kernel.workflow.WorkflowLog;
import com.liferay.portal.kernel.workflow.WorkflowLogManager;

@Service("workflowLogManager")
public class WorkflowLogManagerImpl implements WorkflowLogManager, ApplicationContextAware, Initializable  
{
	private static Log _log = LogFactoryUtil.getLog(WorkflowLogManagerImpl.class);

	ApplicationContext applicationContext;
	
	TaskService taskService;

	IdMappingService idMappingService;

	public void init() {
		taskService = applicationContext.getBean("taskService", TaskService.class);
		idMappingService = applicationContext.getBean("idMappingService", IdMappingService.class);
	}	
	
	@Override
	public int getWorkflowLogCountByWorkflowInstance(long companyId, long workflowInstanceId, 
													 List<Integer> logTypes) throws WorkflowException {
		_log.error("Method is not implemented"); // TODO
		return 0;
	}

	@Override
	public int getWorkflowLogCountByWorkflowTask(long companyId,
			long workflowTaskId, List<Integer> logTypes)
			throws WorkflowException {
		_log.error("Method is not implemented"); // TODO
		return 0;
	}

	@Override
	public List<WorkflowLog> getWorkflowLogsByWorkflowInstance(long companyId, long workflowInstanceId, 
															   List<Integer> logTypes, 
															   int start, int end, OrderByComparator orderByComparator) throws WorkflowException {
		
		List<Comment> processInstanceComments = taskService.getProcessInstanceComments(idMappingService.getActivitiProcessInstanceId(workflowInstanceId));
		List<WorkflowLog> workflowLogs = getWorkflowLogsFromComments(processInstanceComments);
		
		return workflowLogs;
	}

	private List<WorkflowLog> getWorkflowLogsFromComments(
			List<Comment> processInstanceComments) {
		List<WorkflowLog> logs = new ArrayList<WorkflowLog>(processInstanceComments.size());
		
		for (Comment comment : processInstanceComments) {
			WorkflowLog log = getWorkflowLogFromComment(comment);
			if(log != null) {
				logs.add(log);
			}
		}
		
		return logs;
	}
	
	private WorkflowLog getWorkflowLogFromComment(Comment comment) {
		
		if (comment instanceof CommentEntity) {
			CommentEntity commentEntity = (CommentEntity) comment;
			
			if (commentEntity.getType().equals(CommentEntity.TYPE_EVENT)
					&& (commentEntity.getAction().equals(WorkflowLogEntry.TASK_ASSIGN)
						|| commentEntity.getAction().equals(WorkflowLogEntry.TASK_COMPLETION) 
						|| commentEntity.getAction().equals(WorkflowLogEntry.TASK_UPDATE))) {
				
				WorkflowLogEntry workflowLogEntry = null;
				try {
					JAXBContext context = JAXBContext.newInstance(WorkflowLogEntry.class);
					Unmarshaller unmarshaller = context.createUnmarshaller();
					//note: setting schema to null will turn validator off
					unmarshaller.setSchema(null);
					
					StringReader stringReader = new StringReader(commentEntity.getFullMessage());
					workflowLogEntry = (WorkflowLogEntry) unmarshaller.unmarshal(stringReader);
				} catch (Exception e) {
					_log.error("Could not deserialize WorkflowLogEntry from XML", e);
				}
				
				DefaultWorkflowLog log = new DefaultWorkflowLog();
				
				log.setUserId(Long.valueOf(commentEntity.getUserId()));
				log.setCreateDate(commentEntity.getTime());
				log.setWorkflowLogId(Long.valueOf(commentEntity.getId()));
				
				if (workflowLogEntry != null) {
					log.setComment(workflowLogEntry.getComment());
					log.setPreviousState(workflowLogEntry.getPreviousState());
					log.setPreviousUserId(workflowLogEntry.getPreviousUserId());
					log.setPreviousRoleId(workflowLogEntry.getPreviousRoleId());
					log.setState(workflowLogEntry.getState());
					log.setType(workflowLogEntry.getType());
					log.setRoleId(workflowLogEntry.getRoleId());
				}
				
				return log;
			}
		}
		
		return null;		
	}

	@Override
	public List<WorkflowLog> getWorkflowLogsByWorkflowTask(long companyId,
			long workflowTaskId, List<Integer> logTypes, int start, int end,
			OrderByComparator orderByComparator) throws WorkflowException {
		_log.error("Method is not implemented"); // TODO
		return null;
	}

	@Override
	public void setApplicationContext(ApplicationContext ctx)
			throws BeansException 
	{
		applicationContext = ctx;
	
	}
}
