package net.emforge.activiti.engine.event;

import java.util.Map;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.delegate.event.impl.ActivitiActivityEventImpl;
import org.activiti.engine.impl.bpmn.behavior.CallActivityBehavior;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
import org.activiti.engine.repository.ProcessDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Dmitry Farafonov
 *
 */
public class ActivityEventListener implements ActivitiEventListener {

	protected static final Logger LOGGER = LoggerFactory
			.getLogger(ActivityEventListener.class);

	@Override
	public void onEvent(ActivitiEvent event) {
		ActivitiActivityEventImpl ev = (ActivitiActivityEventImpl) event;

		StringBuilder sb = new StringBuilder();
		
		StringBuilder sbActivity = new StringBuilder();
		sbActivity.append("activityId=" + ev.getActivityId() + ", ");
		sbActivity.append("activityName=" + ev.getActivityName() + ", ");
		sbActivity.append("activityType=" + ev.getActivityType());
		
		StringBuilder sbProcess = new StringBuilder();
		sbProcess.append("executionId=" + ev.getExecutionId() + ", ");
		sbProcess.append("processInstanceId=" + ev.getProcessInstanceId() + ", ");
		sbProcess.append("processDefinitionId=" + ev.getProcessDefinitionId());
		
		sb.append(event.getType() + ": ");
		sb.append("Activity [" + sbActivity + "]");
		sb.append(", ");
		sb.append("Execition [" + sbProcess + "]");
		
		switch (event.getType()) {
		case ACTIVITY_STARTED:
			if ("subProcess".equals(ev.getActivityType()) || "callActivity".equals(ev.getActivityType())) {
				logCallActivityStarted(ev);
			} else if ("startEvent".equals(ev.getActivityType())) {
				logStartEvent(ev);
			} else if ("boundaryError".equals(ev.getActivityType())) {
				// skip
			} else {
				LOGGER.debug(sb.toString());
			}
			break;
		case ACTIVITY_COMPLETED:
			if ("startEvent".equals(ev.getActivityType()) || "boundaryError".equals(ev.getActivityType())) {
				// skip
			} else if ("endEvent".equals(ev.getActivityType())) {
				logEndEvent(ev);
			} else {
				LOGGER.debug(sb.toString());
			}
			break;
		case ACTIVITY_SIGNALED:
			LOGGER.debug(sb.toString());
			break;
		case ACTIVITY_COMPENSATE:
			LOGGER.debug(sb.toString());
			break;
		case ACTIVITY_MESSAGE_RECEIVED:
			LOGGER.debug(sb.toString());
			break;
		case ACTIVITY_ERROR_RECEIVED:
			LOGGER.debug(sb.toString());
			break;
		case ACTIVITY_CANCELLED:
			LOGGER.debug(sb.toString());
			break;
		default:
			LOGGER.debug(sb.toString());
		}
	}

	private void logEndEvent(ActivitiActivityEventImpl ev) {
		ExecutionEntity execution = Context.getExecutionContext().getExecution();
		Map<String, Object> vars = execution.getVariables();
		
		RepositoryService repositoryService = ev.getEngineServices().getRepositoryService();
		ProcessDefinition processDefinition = repositoryService.getProcessDefinition(ev.getProcessDefinitionId());
		
		StringBuilder sb = new StringBuilder();
		sb.append("<<< END: ");
		sb.append(processDefinition.getName());
		sb.append("(" + processDefinition.getKey() + ")"); // TODO: add executionId to log
		sb.append(" VARS: ");
		sb.append(vars);
		LOGGER.info(sb.toString());
	}

	private void logStartEvent(ActivitiActivityEventImpl ev) {
		ExecutionEntity execution = Context.getExecutionContext().getExecution();
		Map<String, Object> vars = execution.getVariables();
		
		RepositoryService repositoryService = ev.getEngineServices().getRepositoryService();
		ProcessDefinition processDefinition = repositoryService.getProcessDefinition(ev.getProcessDefinitionId());
		
		StringBuilder sb = new StringBuilder();
		sb.append(">>> START: ");
		sb.append(processDefinition.getName());
		sb.append("(" + processDefinition.getKey() + ")"); // TODO: add executionId to log
		sb.append(" VARS: ");
		sb.append(vars);
		LOGGER.info(sb.toString());
	}

	private void logCallActivityStarted(ActivitiActivityEventImpl ev) {
		RepositoryService repositoryService = ev.getEngineServices().getRepositoryService();
		
		ExecutionEntity execution = Context.getExecutionContext().getExecution();
		Map<String, Object> vars = execution.getVariables();
		
		StringBuilder sb = new StringBuilder();
		sb.append(">>> CALLACTIVITY: ");
		
		ProcessDefinition processDefinition = repositoryService.getProcessDefinition(ev.getProcessDefinitionId());
		sb.append(processDefinition.getName());
		sb.append(" => ");

		ActivityBehavior activityBehavior = execution.getActivity().getActivityBehavior();
		if (activityBehavior instanceof CallActivityBehavior) {
			CallActivityBehavior callActivityBehavior = (CallActivityBehavior) activityBehavior;
			
			ProcessDefinition processDefinition2 = repositoryService
					.createProcessDefinitionQuery()
					.processDefinitionKey(
							callActivityBehavior.getProcessDefinitonKey())
					.latestVersion().singleResult();

			sb.append(processDefinition2.getName() + "(" + processDefinition2.getKey() + ")");
		}
		
		sb.append(" VARS: ");
		sb.append(vars);
		
		/*Map<String, Object> vars = execution.getVariables();
		if(execution instanceof ExecutionEntity) {
			ExecutionEntity executionEntity = (ExecutionEntity)execution;
			if(executionEntity.getParent() != null) {
				startLogText += executionEntity.getParent().getProcessDefinition().getName() + " => ";
				startLogText += executionEntity.getEventSource().getProperty("name") +": ";
				startLogText += executionEntity.getTransition();
				_log.info(startLogText + ": VARS: " + vars);
			} else {
				startLogText += executionEntity.getProcessDefinition().getName();
				_log.info(startLogText + ": VARS: " + vars);
			}
		} else {
			_log.info(startLogText + execution.getCurrentActivityName() + ": VARS: " + vars);
		}*/
		LOGGER.info(sb.toString());
	}

	@Override
	public boolean isFailOnException() {
		// The logic in the onEvent method of this listener is not critical, exceptions
		// can be ignored if logging fails...
		return false;
	}
}
