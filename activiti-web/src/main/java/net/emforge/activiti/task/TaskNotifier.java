package net.emforge.activiti.task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.mail.internet.InternetAddress;

import net.emforge.activiti.WorkflowConstants;
import net.emforge.activiti.WorkflowUtil;
import net.emforge.activiti.identity.UserImpl;

import org.activiti.engine.HistoryService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.task.IdentityLink;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liferay.mail.service.MailServiceUtil;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.mail.MailMessage;
import com.liferay.portal.kernel.messaging.Message;
import com.liferay.portal.kernel.messaging.MessageBusUtil;
import com.liferay.portal.kernel.notifications.NotificationEvent;
import com.liferay.portal.kernel.notifications.NotificationEventFactoryUtil;
import com.liferay.portal.kernel.notifications.UserNotificationManagerUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.workflow.WorkflowTaskManagerUtil;
import com.liferay.portal.model.Company;
import com.liferay.portal.model.UserNotificationDeliveryConstants;
import com.liferay.portal.service.CompanyLocalServiceUtil;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.service.UserNotificationEventLocalServiceUtil;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portal.util.PortletKeys;
import com.liferay.portlet.announcements.model.AnnouncementsDelivery;
import com.liferay.portlet.announcements.service.AnnouncementsDeliveryLocalServiceUtil;

/**
 * @author Dmitry Farafonov
 */
public class TaskNotifier {
	protected static final Logger LOGGER = LoggerFactory
			.getLogger(TaskNotifier.class);

	private static final String NOTIFICATIONS_PORTLET_ID = PortletKeys.MY_WORKFLOW_TASKS;

	public static void sendMBNotification(String destination, TaskEntity task) {

		if (!MessageBusUtil.hasMessageListener(destination)) {
			return;
		}

		Map<String, Object> workflowContext = task.getVariables();
		String currentUserId = Authentication.getAuthenticatedUserId();

		Message message = new Message();
		message.setValues(workflowContext);
		message.put("workflowTaskId", task.getId());
		message.put("authenticatedUserId", currentUserId);
		message.put("createDate", task.getCreateTime());
		message.put("name", task.getName());
		message.put("description", task.getDescription());
		message.put("assignee", task.getAssignee());

		JSONArray candidates = JSONFactoryUtil.createJSONArray();
		for (IdentityLink link : task.getCandidates()) {
			JSONObject object = JSONFactoryUtil.createJSONObject();
			object.put("userId", link.getUserId());
			object.put("groupId", link.getGroupId());
			candidates.put(object);
		}

		message.put("candidates", candidates);

		if (task.isDeleted()) {
			HistoryService historyService = task.getExecution()
					.getEngineServices().getHistoryService();
			HistoricTaskInstance historyTask = historyService
					.createHistoricTaskInstanceQuery().taskId(task.getId())
					.singleResult();
			String deleteReason = historyTask.getDeleteReason();
			message.put("deleteReason", deleteReason);
			message.put("endTime", historyTask.getEndTime());
		}

		MessageBusUtil.sendMessage(destination, message);
	}

	public static void sendSONotification(TaskEntity task) {
		// RuntimeService runtimeService = task.getExecution().getEngineServices().getRuntimeService();

		long companyId = 0;
		Object taskCompanyId = task.getVariable(WorkflowConstants.CONTEXT_COMPANY_ID);
		if (taskCompanyId != null) {
			companyId = GetterUtil.getLong(taskCompanyId);
		} else {
			companyId = PortalUtil.getDefaultCompanyId();
		}

		LOGGER.debug("User task for companyId = " + companyId);

		//Map<String,Object> workflowContext = runtimeService.getVariables(execution.getId());
		Map<String,Object> workflowContext = task.getExecution().getVariables();


		String userId = task.getAssignee();
		if (userId != null) {
			List<Long> receiverUserIds = new ArrayList<Long>();
			receiverUserIds.add(Long.valueOf(userId));
			sendPortalNotification(task, receiverUserIds, workflowContext, false);
		}

		List<User> users = new ArrayList<User>();
		Set<IdentityLink> candidates = task.getCandidates();
		users.addAll(resolveUsersForGroups(companyId, candidates));


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
			LOGGER.error("Could not send portal notification to group", e);
		}
	}

	private static List<User> resolveUsersForGroups(long companyId, Set<IdentityLink> candidates) {
		List<User> users = new ArrayList<User>();
		for (IdentityLink identityLink : candidates) {
			String groupName = identityLink.getGroupId();
			String userId = identityLink.getUserId();
			if (groupName != null) {
				users.addAll(WorkflowUtil.findUsersByGroup(companyId, groupName));
			} else if (userId != null){
				try {
					com.liferay.portal.model.User user = UserLocalServiceUtil.getUser(Long.valueOf(userId));
					users.add(new UserImpl(user));
				} catch (Exception e) {
					LOGGER.warn("No user found with id=" + userId);
				} 
			}
		}
		return users;		
	}

	protected static void sendPortalNotification(TaskEntity task,
			List<Long> receiverUserIds, Map<String, Object> workflowContext,
			boolean isGroup) {
		String currentUserId = Authentication.getAuthenticatedUserId();
		JSONObject notificationEventJSONObject = JSONFactoryUtil.createJSONObject();

		// ExecutionEntity topExecution = WorkflowUtil.getTopProcessInstance(task.getExecution());
		// Map<String, Object> topWorkflowContext = topExecution.getVariables();
		// long companyId = GetterUtil.getLong(topWorkflowContext.get("companyId"));

		// FIXME localize notifications
		String titlePattern = StringPool.BLANK;
		if (isGroup) {
			titlePattern = "workflow.notification.website.assigned-to-role";
		} else {
			titlePattern = "workflow.notification.website.assigned-to-you";
		}
		
		notificationEventJSONObject.put("workflowTaskId", task.getId());

		for (Long receiverUserId : receiverUserIds) {
			try {
				if (receiverUserId.toString().equals(currentUserId)) {
					// do not send notification in case action was performed by same user
					LOGGER.debug("User " + receiverUserId + " skipped from sending notification since it is current user");
					continue;
				}

				LOGGER.debug("Before sending notification receiverUserId = " + receiverUserId);

				com.liferay.portal.model.User receiverUser = UserLocalServiceUtil.getUser(receiverUserId);
				Locale locale = receiverUser.getLocale();
				
				String title = LanguageUtil.format(locale, titlePattern, task.getName());
				notificationEventJSONObject.put("notificationMessage", title);
				// send notification to the user
				NotificationEvent notificationEvent =
						NotificationEventFactoryUtil.createNotificationEvent(
								System.currentTimeMillis(), NOTIFICATIONS_PORTLET_ID,
								notificationEventJSONObject);

				notificationEvent.setDeliveryRequired(0);

				UserNotificationEventLocalServiceUtil.addUserNotificationEvent(
						receiverUserId, notificationEvent);				

				LOGGER.debug("Notification for receiverUserId = " + receiverUserId + " sent");

				
				// check email notification
				boolean isEmailDelivery = AnnouncementsDeliveryLocalServiceUtil.getUserDelivery(receiverUserId, "workflow").isEmail();
				if (isEmailDelivery) {

					// get link to the task
					Company company = CompanyLocalServiceUtil.getCompany(receiverUser.getCompanyId());
					String taskUrl = "http://" + company.getVirtualHostname() + "/group/control_panel/manage/-/my_workflow_tasks/view/" + task.getId() + "?_153_struts_action=%2Fmy_workflow_tasks%2Fedit_workflow_task";
					
					String body = LanguageUtil.format(locale, "workflow.notification.email.assigned-to-you", new String[] {task.getName(),taskUrl});

					sendEmail(null, new InternetAddress(receiverUser.getEmailAddress(), receiverUser.getFullName()), title, body, false);
				 
				}

			} catch (Exception ex) {
				LOGGER.warn("Cannot send notifications to the user " + receiverUserId + ":" + ex.getMessage());
				LOGGER.debug("Cannot send notifications to the user " + receiverUserId, ex);

			}
		}
	}

	protected static void sendEmail(InternetAddress internetAddressFrom,
			InternetAddress internetAddressesTo, String subject,
			String body, boolean isHtml) {

		if (internetAddressFrom == null) {
			String fromAddr = PropsUtil.get(PropsKeys.ADMIN_EMAIL_FROM_ADDRESS);
			String fromName = PropsUtil.get(PropsKeys.ADMIN_EMAIL_FROM_NAME);

			try {
				internetAddressFrom = new InternetAddress(fromAddr, fromName);
			} catch (Exception e) {
				LOGGER.error(String
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

		LOGGER.info("Notification e-mail to addresses "
				+ ArrayUtils.toString(internetAddressesTo)
				+ " has been sent successfully");
	}
}
