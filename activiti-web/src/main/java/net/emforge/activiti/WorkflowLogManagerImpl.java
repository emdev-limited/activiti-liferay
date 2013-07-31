package net.emforge.activiti;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import com.liferay.portal.kernel.workflow.*;

import net.emforge.activiti.log.WorkflowLogEntry;

import org.activiti.engine.HistoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.persistence.entity.CommentEntity;
import org.activiti.engine.task.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.StringPool;

@Service("workflowLogManager")
public class WorkflowLogManagerImpl implements WorkflowLogManager {
	private static Log _log = LogFactoryUtil.getLog(WorkflowLogManagerImpl.class);

	@Autowired
	TaskService taskService;
	@Autowired
	HistoryService historyService;

	@Autowired
	IdMappingService idMappingService;

	
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
	
	private List<Comment> getCommentsRecursively(long workflowInstanceId) throws WorkflowException {
		HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(String.valueOf(workflowInstanceId)).singleResult();
		String sProcessInstanceId = processInstance.getSuperProcessInstanceId();
		if (sProcessInstanceId == null) {
			return getCommentsRecursivelyDown(processInstance.getId());
		}
		String topProcessInstanceId = StringPool.BLANK;
		while (sProcessInstanceId != null) {
			topProcessInstanceId = sProcessInstanceId;
			processInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(sProcessInstanceId).singleResult();
			sProcessInstanceId = processInstance.getSuperProcessInstanceId();
		}
		List<Comment> comments = new ArrayList<Comment>();
		comments.addAll(getCommentsRecursivelyDown(topProcessInstanceId));
		return comments;
	}
	
	private List<Comment> getCommentsRecursivelyDown(String workflowInstanceId) throws WorkflowException {
		List<Comment> comments = new ArrayList<Comment>();
		comments.addAll(taskService.getProcessInstanceComments(workflowInstanceId));
		List<HistoricProcessInstance> subProcessInstances = historyService.createHistoricProcessInstanceQuery().superProcessInstanceId(workflowInstanceId).list();
		if (subProcessInstances == null || subProcessInstances.isEmpty()) {
			return comments;
		}
		for (HistoricProcessInstance hpi : subProcessInstances) {
			comments.addAll(getCommentsRecursivelyDown(hpi.getId()));
		}
		
		return comments;
	}
	
	@Override
	public List<WorkflowLog> getWorkflowLogsByWorkflowInstance(long companyId, long workflowInstanceId, 
															   List<Integer> logTypes, 
															   int start, int end, OrderByComparator orderByComparator) throws WorkflowException {
		
		List<Comment> processInstanceComments = getCommentsRecursively(workflowInstanceId);
		if (processInstanceComments != null && !processInstanceComments.isEmpty()) {
			//order by date
			Collections.sort(processInstanceComments, new Comparator<Comment>(){
				  public int compare(Comment c1, Comment c2) {
				    return c1.getTime().compareTo(c2.getTime())*(-1);
				  }
			});
		}
		
		List<WorkflowLog> workflowLogs = getWorkflowLogsFromComments(processInstanceComments);
		
		return workflowLogs;
	}

	private List<WorkflowLog> getWorkflowLogsFromComments(
			List<Comment> processInstanceComments) {
		List<WorkflowLog> logs = new ArrayList<WorkflowLog>(processInstanceComments.size());
		
		// TODO: fix bug with retrieving log
		int counter = 0;
		for (Comment comment : processInstanceComments) {
			counter++;
			if(counter >= 20) {
				break;
			}
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
				//log.setUserId(Long.valueOf(commentEntity.getUserId()));
				//log.setUserId(GetterUtil.getLong(commentEntity.getUserId()));

                // User, who created a comment, is a changes initiator, i.e. audit user.
                log.setAuditUserId(GetterUtil.getLong(commentEntity.getUserId()));

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
                    log.setUserId(workflowLogEntry.getAssigneeUserId());
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


}
