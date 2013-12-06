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

import javax.mail.internet.InternetAddress;

import net.emforge.activiti.WorkflowUtil;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.calendar.DueDateBusinessCalendar;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.task.TaskDefinition;
import org.activiti.rest.api.ActivitiUtil;
import org.apache.commons.lang.ArrayUtils;

import com.liferay.mail.service.MailServiceUtil;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.mail.MailMessage;
import com.liferay.portal.kernel.notifications.NotificationEvent;
import com.liferay.portal.kernel.notifications.NotificationEventFactoryUtil;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.workflow.WorkflowTaskManagerUtil;
import com.liferay.portal.service.UserNotificationEventLocalServiceUtil;
import com.liferay.portal.util.PortletKeys;

/**
 * activity implementation for the user task.
 * 
 * @author Joram Barrez
 */
public class UserTaskActivityBehavior extends TaskActivityBehavior {
  private static Log _log = LogFactoryUtil.getLog(UserTaskActivityBehavior.class);
  
  private static final String NOTIFICATIONS_PORTLET_ID = PortletKeys.MY_WORKFLOW_TASKS;
	

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
			List<Long> receiverUserIds = new ArrayList<Long>();
			receiverUserIds.add(Long.valueOf(userId));
			sendPortalNotification(task, receiverUserIds, workflowContext, false);
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
				sendPortalNotification(task, receiverUserIds, workflowContext, true);
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
	
	protected void sendPortalNotification(TaskEntity task, List<Long> receiverUserIds, Map<String,Object> workflowContext, boolean isGroup) {
		String currentUserId = Authentication.getAuthenticatedUserId();
		JSONObject notificationEventJSONObject = JSONFactoryUtil.createJSONObject();
		
		long companyId = Long.valueOf((String) workflowContext.get("companyId"));

		// FIXME localize notifications
		String title = StringPool.BLANK;
		if (isGroup) {
			title = "New workflow task \"" + task.getName() + "\" has been assigned to your role";
		} else {
			title = "New workflow task \"" + task.getName() + "\" has been assigned to you";
		}
		notificationEventJSONObject.put("notificationMessage", title);
		notificationEventJSONObject.put("workflowTaskId", task.getId());
		
		for (Long receiverUserId : receiverUserIds) {
			try {
				if (receiverUserId.toString().equals(currentUserId)) {
					// do not send notification in case action was performed by same user
					_log.debug("User " + receiverUserId + " skipped from sending notification since it is current user");
					continue;
				}
				
				_log.debug("Before sending notification receiverUserId = " + receiverUserId);
				
				
				// send notification to the user
				NotificationEvent notificationEvent =
						NotificationEventFactoryUtil.createNotificationEvent(
							System.currentTimeMillis(), NOTIFICATIONS_PORTLET_ID,
							notificationEventJSONObject);

				notificationEvent.setDeliveryRequired(0);

				UserNotificationEventLocalServiceUtil.addUserNotificationEvent(
					receiverUserId, notificationEvent);				
				
				_log.debug("Notification for receiverUserId = " + receiverUserId + " sent");
				
				/*
				// check email notification
				if (UserNotificationManagerUtil.isDeliver(
						receiverUserId, NOTIFICATIONS_PORTLET_ID, 0,
						0,
						UserNotificationDeliveryConstants.TYPE_EMAIL)) {
				*/	
					// TODO - Make it better
					/*
					com.liferay.portal.model.User receiverUser = UserLocalServiceUtil.getUser(receiverUserId);
					String body = "New Task is assigned to you. You can access task by using followed link: ";
					
					// get link to the task
					Company company = CompanyLocalServiceUtil.getCompany(companyId);
					String taskUrl = "http://" + company.getVirtualHostname() + "/group/control_panel/manage/-/my_workflow_tasks/view/" + task.getId() + "?_153_struts_action=%2Fmy_workflow_tasks%2Fedit_workflow_task";
					body += taskUrl;
					
					sendEmail(null, new InternetAddress(receiverUser.getEmailAddress(), receiverUser.getFullName()), title, body, false);
					*/
				//}
				
			} catch (Exception ex) {
				_log.warn("Cannot send notifications to the user " + receiverUserId + ":" + ex.getMessage());
				_log.debug("Cannot send notifications to the user " + receiverUserId, ex);
				
			}
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
  
	protected void sendEmail(InternetAddress internetAddressFrom,
			InternetAddress internetAddressesTo, String subject,
			String body, boolean isHtml) {

		if (internetAddressFrom == null) {
			String fromAddr = PropsUtil.get(PropsKeys.ADMIN_EMAIL_FROM_ADDRESS);
			String fromName = PropsUtil.get(PropsKeys.ADMIN_EMAIL_FROM_NAME);

			try {
				internetAddressFrom = new InternetAddress(fromAddr, fromName);
			} catch (Exception e) {
				_log.error(String
						.format("Error occured, while trying to create internet address using [%s]: %s",
								fromAddr, e.getMessage()));
				return;
			}
		}

		// always send mail one-by-one
		InternetAddress ia = internetAddressesTo;
		
		MailMessage mailMessage = new MailMessage();

		mailMessage.setFrom(internetAddressFrom);
		mailMessage.setBody(body);
		mailMessage.setSubject(subject);
		mailMessage.setHTMLFormat(isHtml);

		InternetAddress[] iAddresses = new InternetAddress[1];
		iAddresses[0] = ia;
		mailMessage.setTo(iAddresses);

		MailServiceUtil.sendEmail(mailMessage);

		_log.info("Notification e-mail to addresses "
				+ ArrayUtils.toString(internetAddressesTo)
				+ " has been sent successfully");
	}
  
}
