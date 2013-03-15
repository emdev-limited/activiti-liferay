package net.emforge.activiti;

import net.emforge.activiti.entity.WorkflowDefinitionExtensionImpl;

import org.activiti.engine.repository.ProcessDefinition;

import com.liferay.portal.kernel.workflow.WorkflowDefinitionManager;

/** Extends workflow definition to add new method for getting activiti process definition
 * 
 * @author akakunin
 *
 */
public interface WorkflowDefinitionManagerExt extends WorkflowDefinitionManager {
	public ProcessDefinition getProcessDefinition(String definitionId);
	public WorkflowDefinitionExtensionImpl getWorkflowDefinitionExt(long companyId, String name, int version);

}
