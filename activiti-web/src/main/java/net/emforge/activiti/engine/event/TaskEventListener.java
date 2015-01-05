package net.emforge.activiti.engine.event;

import net.emforge.activiti.task.SaveAssigneeListener;
import net.emforge.activiti.task.SaveInitialRoleListener;
import net.emforge.activiti.task.SaveTransitionListener;
import net.emforge.activiti.task.TaskNotifier;

import org.activiti.engine.HistoryService;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.delegate.event.impl.ActivitiEntityEventImpl;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author Dmitry Farafonov
 *
 */
public class TaskEventListener implements ActivitiEventListener {

	protected static final Logger LOGGER = LoggerFactory
			.getLogger(TaskEventListener.class);

	@Autowired
	SaveTransitionListener saveTransitionListener;

	@Autowired
	SaveAssigneeListener saveAssigneeListener;

	@Autowired
	SaveInitialRoleListener saveInitialRoleListener;

	@Override
	public void onEvent(ActivitiEvent event) {
		ActivitiEntityEventImpl ev = (ActivitiEntityEventImpl) event;
		if (!(ev.getEntity() instanceof TaskEntity)) {
			return;
		}

		TaskEntity task = (TaskEntity) ev.getEntity();
		LOGGER.info("EventType: {}, Task: {}", event.getType(), task);

		switch (event.getType()) {
		case TASK_CREATED:
			LOGGER.debug("TASK CREATED: " + task.getId());

			TaskNotifier.sendSONotification(task);
			TaskNotifier.sendMBNotification("liferay/activiti/task/created", task);
			break;
		case TASK_ASSIGNED:
			LOGGER.debug("TASK " + task.getId() + " ASSIGNED: " + task.getAssignee());

			saveAssigneeListener.notify(task);
			saveInitialRoleListener.notify(task);

			TaskNotifier.sendMBNotification("liferay/activiti/task/assigned", task);
			break;
		case TASK_COMPLETED:
			LOGGER.debug("TASK " + task.getId() + " COMPLETED: " + task.getVariables());
			
			saveTransitionListener.notify(task);
			
			TaskNotifier.sendMBNotification("liferay/activiti/task/completed", task);
			break;
		case ENTITY_DELETED:
			LOGGER.debug("TASK " + task.getId() + " DELETED: " + task.getVariables());
			
			HistoryService historyService = event.getEngineServices().getHistoryService();
			HistoricTaskInstance historyTask = historyService.createHistoricTaskInstanceQuery()
					.taskId(task.getId()).singleResult();
			String deleteReason = historyTask.getDeleteReason();
			LOGGER.debug("Task {} deleted with deleteReason '{}'", task.getId(), deleteReason);
			
			TaskNotifier.sendMBNotification("liferay/activiti/task/completed", task);
			break;
		default:
		}
	}

	@Override
	public boolean isFailOnException() {
		// The logic in the onEvent method of this listener is not critical, exceptions
		// can be ignored if logging fails...
		return false;
	}
}
