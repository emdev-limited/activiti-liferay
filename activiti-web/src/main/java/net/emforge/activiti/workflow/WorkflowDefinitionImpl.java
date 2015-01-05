package net.emforge.activiti.workflow;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.repository.ProcessDefinition;

import com.liferay.portal.kernel.util.LocalizationUtil;
import com.liferay.portal.kernel.workflow.DefaultWorkflowDefinition;

public class WorkflowDefinitionImpl extends DefaultWorkflowDefinition {
	private static final long serialVersionUID = -6169554905485668421L;

	@Deprecated
	public WorkflowDefinitionImpl(ProcessDefinition processDef, String title, boolean active) {
		this(processDef, title);
		setActive(active);
		setOptionalAttributes(new HashMap<String, Object>());
	}

	public WorkflowDefinitionImpl(ProcessDefinition processDef, String title) {
		setName(processDef.getName());
		setTitle(title);
		setActive(!processDef.isSuspended());
		setVersion(processDef.getVersion());

		Map<String, Object> optionalAttributes = new HashMap<String, Object>();
		optionalAttributes.put("id", processDef.getId());
		optionalAttributes.put("deploymentId", processDef.getDeploymentId());
		optionalAttributes.put("key", processDef.getKey());
		optionalAttributes.put("category", processDef.getCategory());
		optionalAttributes.put("isSuspended", processDef.isSuspended());

		optionalAttributes.put("description", processDef.getDescription());
		optionalAttributes.put("deploymentId", processDef.getDeploymentId());
		optionalAttributes.put("diagramResourceName", processDef.getDiagramResourceName());
		optionalAttributes.put("tenantId", processDef.getTenantId());

		setOptionalAttributes(optionalAttributes);
	}

	@Override
	public String getTitle(String languageId) {
		return LocalizationUtil.getLocalization(getTitle(), languageId);
	}

	@Override
	public String toString() {
		return "WorkflowDefinitionImpl ["
				+ "id=" + getOptionalAttributes().get("id") + ", "
				+ "name=" + getName() + ", "
				+ "title langs=" + Arrays.toString(LocalizationUtil.getAvailableLanguageIds(getTitle())) + ", "
				+ "version=" + getVersion() + ", "
				+ "isActive=" + isActive() + ", "
				+ "key=" + getOptionalAttributes().get("key") + ", "
				+ "deploymentId=" + getOptionalAttributes().get("deploymentId")
				+ "]";
	}

	
}
