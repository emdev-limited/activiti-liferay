package net.emforge.activiti;

import com.liferay.portal.kernel.workflow.WorkflowTask;

public interface WorkflowTaskExt extends WorkflowTask {
    public String getOutputTransition();
}
