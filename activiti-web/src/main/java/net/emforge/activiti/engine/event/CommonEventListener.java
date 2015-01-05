package net.emforge.activiti.engine.event;

import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.impl.ActivitiEntityEventImpl;
import org.activiti.engine.impl.persistence.entity.TimerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Dmitry Farafonov
 *
 */
public class CommonEventListener implements
		org.activiti.engine.delegate.event.ActivitiEventListener {
	protected static final Logger LOGGER = LoggerFactory
			.getLogger(CommonEventListener.class);

	@Override
	public void onEvent(ActivitiEvent event) {
		StringBuilder sb = new StringBuilder();
		
		StringBuilder sbProcess = new StringBuilder();
		sbProcess.append("executionId=" + event.getExecutionId() + ", ");
		sbProcess.append("processInstanceId=" + event.getProcessInstanceId() + ", ");
		sbProcess.append("processDefinitionId=" + event.getProcessDefinitionId());
		
		switch (event.getType()) {
		case PROCESS_COMPLETED:
			sb.append("PROCESS_COMPLETED: ");
			sb.append(sbProcess);
			break;
		case TIMER_FIRED:
			TimerEntity timerEntity = (TimerEntity)((ActivitiEntityEventImpl)event).getEntity();
			sb.append("TIMER_FIRED: ");
			sb.append("activityName=" + timerEntity.getJobHandlerConfiguration() + ", ");
			sb.append(sbProcess);
			break;
		default:
			sb.append("ActivitiEventImpl [");
			sb.append("type=" + event.getType() + ", ");
			sb.append(sbProcess);
			sb.append("]");
		}
		LOGGER.info(sb.toString());
	}

	@Override
	public boolean isFailOnException() {
	    // The logic in the onEvent method of this listener is not critical, exceptions
	    // can be ignored if logging fails...
		return false;
	}

}
