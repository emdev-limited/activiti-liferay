package net.emforge.activiti;

import java.io.Serializable;
import java.util.Map;

import org.activiti.engine.runtime.Execution;

import com.liferay.portal.kernel.workflow.DefaultWorkflowInstance;
import com.liferay.portal.kernel.workflow.WorkflowException;
import com.liferay.portal.kernel.workflow.WorkflowInstanceManager;

/** Extends default workflow instance manager
 * 
 * @author akakunin
 *
 */
public interface WorkflowInstanceManagerExt extends WorkflowInstanceManager {
	public DefaultWorkflowInstance getWorkflowInstance(Execution processInstance, Long userId, Map<String, Serializable> currentWorkflowContext) throws WorkflowException;
}
