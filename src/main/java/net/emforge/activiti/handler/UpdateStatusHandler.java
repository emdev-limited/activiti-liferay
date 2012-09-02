package net.emforge.activiti.handler;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Service;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.kernel.workflow.WorkflowStatusManagerUtil;

/** Update status handler
 * 
 * @author akakunin
 *
 */
@Service("updateStatusHandler")
public class UpdateStatusHandler {
	private static Log _log = LogFactoryUtil.getLog(UpdateStatusHandler.class);
	
	public void updateStatus(DelegateExecution execution, String status) throws Exception {
		Map<String, Serializable> workflowContext = new HashMap<String, Serializable>();

		for (String key : execution.getVariables().keySet()) {
			workflowContext.put(key, (Serializable)execution.getVariable(key));
		}
		
		WorkflowStatusManagerUtil.updateStatus(WorkflowConstants.toStatus(status), workflowContext);

		_log.info("set asset status to " + status);		
	}

}
