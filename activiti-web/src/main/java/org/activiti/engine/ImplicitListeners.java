package org.activiti.engine;

import java.util.List;

import org.activiti.engine.delegate.TaskListener;

/**
 * Added by emdev.
 * Implicit listeners fires on every user task after explicit listeners fired. 
 * In order to use this feature you must implement "ImplicitListeners" interface in your ProcessEngineConfiguration.  
 * @author irina
 *
 */
public interface ImplicitListeners {
	public List<TaskListener> getImplicitUserTaskListenersFor(String taskEventName);
}
