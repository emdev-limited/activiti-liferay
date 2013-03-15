package net.emforge.activiti;

import org.activiti.engine.repository.ProcessDefinition;

import com.liferay.portal.kernel.util.LocalizationUtil;
import com.liferay.portal.kernel.workflow.DefaultWorkflowDefinition;

public class WorkflowDefinitionImpl extends DefaultWorkflowDefinition {
	private static final long serialVersionUID = -6169554905485668421L;

	public WorkflowDefinitionImpl(ProcessDefinition processDef, String title, boolean active) {
        setName(processDef.getName());
        setTitle(title);
        setActive(active);
        setVersion(processDef.getVersion());
	}

	public String getTitle(String languageId) {
		return LocalizationUtil.getLocalization(getTitle(), languageId);
	}
}
