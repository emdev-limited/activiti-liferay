package net.emforge.activiti.engine.event;

import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Dmitry Farafonov
 *
 */
public class EngineEventListener implements ActivitiEventListener {

	protected static final Logger LOGGER = LoggerFactory
			.getLogger(EngineEventListener.class);

	@Override
	public void onEvent(ActivitiEvent event) {
		LOGGER.info("Event: {}", new Object[]{event.getType()});
	}

	@Override
	public boolean isFailOnException() {
		// The logic in the onEvent method of this listener is not critical, exceptions
		// can be ignored if logging fails...
		return false;
	}
}
