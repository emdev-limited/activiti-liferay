package net.emforge.activiti.util;

import net.emforge.activiti.WorkflowInstanceManagerImpl;

import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.springframework.stereotype.Service;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.kernel.workflow.WorkflowException;
import com.liferay.portal.kernel.workflow.WorkflowStatusManagerUtil;

@Service("bpmCommon")
public class BPMCommonUtil {
	private static Log _log = LogFactoryUtil.getLog(BPMCommonUtil.class);

	
	public void updateStatus(String status){
		ExecutionEntity execution = Context.getExecutionContext().getExecution();
		int st = WorkflowConstants.toStatus(status);
		try {
			WorkflowStatusManagerUtil.updateStatus(st, WorkflowInstanceManagerImpl.convertFromVars(execution.getVariables()));
		} catch (WorkflowException e) {
			_log.error("Failed to update status", e);
		}
	}
}
