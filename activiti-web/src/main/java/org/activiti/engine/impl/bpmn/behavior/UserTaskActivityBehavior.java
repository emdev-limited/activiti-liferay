/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.engine.impl.bpmn.behavior;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import net.emforge.activiti.WorkflowTaskManagerImpl;
import net.emforge.activiti.WorkflowUtil;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.calendar.DueDateBusinessCalendar;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.task.TaskDefinition;
import org.activiti.rest.api.ActivitiUtil;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.notifications.ChannelException;
import com.liferay.portal.kernel.notifications.ChannelHubManagerUtil;
import com.liferay.portal.kernel.notifications.NotificationEvent;
import com.liferay.portal.kernel.notifications.NotificationEventFactoryUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.workflow.WorkflowTaskManagerUtil;

/**
 * activity implementation for the user task.
 * 
 * @author Joram Barrez
 */
public class UserTaskActivityBehavior extends TaskActivityBehavior {
  private static Log _log = LogFactoryUtil.getLog(UserTaskActivityBehavior.class);
	

  protected TaskDefinition taskDefinition;

  public UserTaskActivityBehavior(TaskDefinition taskDefinition) {
    this.taskDefinition = taskDefinition;
  }

  public void execute(ActivityExecution execution) throws Exception {
    TaskEntity task = TaskEntity.createAndInsert(execution);
    task.setExecution(execution);
    task.setTaskDefinition(taskDefinition);

    if (taskDefinition.getNameExpression() != null) {
      String name = (String) taskDefinition.getNameExpression().getValue(execution);
      task.setName(name);
    }

    if (taskDefinition.getDescriptionExpression() != null) {
      String description = (String) taskDefinition.getDescriptionExpression().getValue(execution);
      task.setDescription(description);
    }
    
    if(taskDefinition.getDueDateExpression() != null) {
      Object dueDate = taskDefinition.getDueDateExpression().getValue(execution);
      if(dueDate != null) {
        if (dueDate instanceof Date) {
          task.setDueDate((Date) dueDate);
        } else if (dueDate instanceof String) {
          task.setDueDate(new DueDateBusinessCalendar().resolveDuedate((String) dueDate)); 
        } else {
          throw new ActivitiIllegalArgumentException("Due date expression does not resolve to a Date or Date string: " + 
              taskDefinition.getDueDateExpression().getExpressionText());
        }
      }
    }

    if (taskDefinition.getPriorityExpression() != null) {
      final Object priority = taskDefinition.getPriorityExpression().getValue(execution);
      if (priority != null) {
        if (priority instanceof String) {
          try {
            task.setPriority(Integer.valueOf((String) priority));
          } catch (NumberFormatException e) {
            throw new ActivitiIllegalArgumentException("Priority does not resolve to a number: " + priority, e);
          }
        } else if (priority instanceof Number) {
          task.setPriority(((Number) priority).intValue());
        } else {
          throw new ActivitiIllegalArgumentException("Priority expression does not resolve to a number: " + 
                  taskDefinition.getPriorityExpression().getExpressionText());
        }
      }
    }
    
    handleAssignments(task, execution);
   
    // All properties set, now firing 'create' event
    task.fireEvent(TaskListener.EVENTNAME_CREATE);
  }

  public void signal(ActivityExecution execution, String signalName, Object signalData) throws Exception {
    leave(execution);
  }

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void handleAssignments(TaskEntity task,
			ActivityExecution execution) {

		// to send SO notification we need to extract workflow context, get
		// users to send to..
		Map<String,Object> workflowContext = ActivitiUtil.getRuntimeService().getVariables(execution.getId());
		_log.debug("User task for companyId = " + (String) workflowContext.get("companyId"));
		long companyId = Long.valueOf((String) workflowContext.get("companyId"));

		if (taskDefinition.getAssigneeExpression() != null) {
			String userId = (String) taskDefinition.getAssigneeExpression()
					.getValue(execution);
			task.setAssignee(userId);
			try {
				List<Long> receiverUserIds = new ArrayList<Long>();
				receiverUserIds.add(Long.valueOf(userId));
				sendPortalNotification(task, receiverUserIds, workflowContext, false);
			} catch (ChannelException e) {
				_log.error("Could not send portal notification to user", e);
			}
		}

		if (!taskDefinition.getCandidateGroupIdExpressions().isEmpty()) {
			List<User> users = new ArrayList<User>();
			for (Expression groupIdExpr : taskDefinition
					.getCandidateGroupIdExpressions()) {
				Object value = groupIdExpr.getValue(execution);
				if (value instanceof String) {
					List<String> candiates = extractCandidates((String) value);
					task.addCandidateGroups(candiates);
					users.addAll(resolveUsersForGroups(companyId, candiates));
				} else if (value instanceof Collection) {
					task.addCandidateGroups((Collection) value);
					users.addAll(resolveUsersForGroups(companyId, (Collection) value));
				} else {
					throw new ActivitiIllegalArgumentException(
							"Expression did not resolve to a string or collection of strings");
				}
			}
			try {
				long[] pooledActorsIds = WorkflowTaskManagerUtil.getPooledActorsIds(companyId, Long.valueOf(task.getId()));
				List<Long> receiverUserIds = null;
				if (pooledActorsIds == null || pooledActorsIds.length == 0) {
					//try to use users list
					receiverUserIds = new ArrayList<Long>(users.size());
					for (User user : users) {
						receiverUserIds.add(Long.valueOf(user.getId()));
					}
				} else {
					receiverUserIds = new ArrayList<Long>(Arrays.asList(ArrayUtils.toObject(pooledActorsIds)));
				}
				try {
					sendPortalNotification(task, receiverUserIds, workflowContext, true);
				} catch (ChannelException e) {
					_log.error("Could not send portal notification to group", e);
				}
			} catch (Exception e) {
				_log.error("Could not send portal notification to group", e);
			}
			
		}

		if (!taskDefinition.getCandidateUserIdExpressions().isEmpty()) {
			for (Expression userIdExpr : taskDefinition
					.getCandidateUserIdExpressions()) {
				Object value = userIdExpr.getValue(execution);
				if (value instanceof String) {
					List<String> candiates = extractCandidates((String) value);
					task.addCandidateUsers(candiates);
				} else if (value instanceof Collection) {
					task.addCandidateUsers((Collection) value);
				} else {
					throw new ActivitiException(
							"Expression did not resolve to a string or collection of strings");
				}
			}
		}
	}
	
	private List<User> resolveUsersForGroups(long companyId, Collection groupNames) {
		List<User> users = new ArrayList<User>();
		for (Object name : groupNames) {
			String groupName = (String) name;
			users.addAll(WorkflowUtil.findUsersByGroup(companyId, groupName));
		}
		return users;
		
	}
	
	protected void sendPortalNotification(TaskEntity task, List<Long> receiverUserIds, Map<String,Object> workflowContext, boolean isGroup) throws ChannelException {
		JSONObject notificationEventJSONObject = JSONFactoryUtil.createJSONObject();
		
		long companyId = Long.valueOf((String) workflowContext.get("companyId"));

		notificationEventJSONObject.put("body", task.getName());
		notificationEventJSONObject.put("entryId", (String) workflowContext.get("entryClassPK"));
		// workflow tasks portlet id
		notificationEventJSONObject.put("portletId", 150);
		notificationEventJSONObject.put("userId", (String) workflowContext.get("userId"));
		
		String title = StringPool.BLANK;
		if (isGroup) {
			title = "New workflow task " + task.getName() + " has been assigned to your role";
		} else {
			title = "New workflow task " + task.getName() + " has been assigned to you";
		}
		// FIXME localize notifications
		for (long receiverUserId : receiverUserIds) {
			_log.debug("Before sending notification receiverUserId = " + receiverUserId);
			notificationEventJSONObject.put("title", title);
			NotificationEvent notificationEvent = NotificationEventFactoryUtil.createNotificationEvent(
					System.currentTimeMillis(), "6_WAR_soportlet", notificationEventJSONObject);
			notificationEvent.setDeliveryRequired(0);
			ChannelHubManagerUtil.sendNotificationEvent(companyId, receiverUserId, notificationEvent);
			_log.debug("Notification for receiverUserId = " + receiverUserId + " sent");
		}
	}

  /**
   * Extract a candidate list from a string. 
   * 
   * @param str
   * @return 
   */
  protected List<String> extractCandidates(String str) {
    return Arrays.asList(str.split("[\\s]*,[\\s]*"));
  }
  
  // getters and setters //////////////////////////////////////////////////////
  
  public TaskDefinition getTaskDefinition() {
    return taskDefinition;
  }
  
}
