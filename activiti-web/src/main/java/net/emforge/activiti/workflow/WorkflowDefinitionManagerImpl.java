package net.emforge.activiti.workflow;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import net.emforge.activiti.WorkflowUtil;
import net.emforge.activiti.util.SignavioFixer;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.LocalizationUtil;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.workflow.WorkflowDefinition;
import com.liferay.portal.kernel.workflow.WorkflowDefinitionFileException;
import com.liferay.portal.kernel.workflow.WorkflowDefinitionManager;
import com.liferay.portal.kernel.workflow.WorkflowException;

/** Own Implementation for Workflow Definition Manager
 * 
 * @author akakunin
 * @author Dmitry Farafonov
 */
@Service(value="workflowDefinitionManager")
public class WorkflowDefinitionManagerImpl extends AbstractWorkflowDefinitionManager implements WorkflowDefinitionManager {
	private static Log _log = LogFactoryUtil.getLog(WorkflowDefinitionManagerImpl.class);

	@Override
	public WorkflowDefinition deployWorkflowDefinition(long companyId,
			long userId, String title, byte[] bytes) throws WorkflowException {
		if (bytes == null) {
			throw new WorkflowDefinitionFileException();
		}

		try {
			boolean isBar = false;
			Deployment deployment = null;
			ActivitiException activitiException = null;
			
			String strTitle = LocalizationUtil.getLocalization(title, "en_US", true);
			String tenantId = String.valueOf(companyId);
			
			// try to read bytes as ZIP file
			
			ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
			ZipInputStream zipInputStream = new ZipInputStream(bais);
			ZipEntry zipEntry = zipInputStream.getNextEntry();
			if (zipEntry != null) {
				_log.info("Deploy bar " + zipEntry.getName());
				try {
					deployment = repositoryService.createDeployment().name(strTitle + ".bar")
							.addZipInputStream(zipInputStream)
							.disableSchemaValidation()
//							.category(String.valueOf(companyId))
							.tenantId(tenantId)
							.deploy();
					isBar = true;
				} catch (ActivitiException ae) {
					//save exception
					activitiException = ae;
				}
			}
			
			// if it's not ZIP then try to deploy as XML file
			
			if (!isBar) {
				_log.info("Deploy process " + strTitle);
				// try to fix xml
				SignavioFixer fixer = new SignavioFixer(strTitle);
				byte[] xmlBytes = fixer.fixSignavioXml(bytes);
	
				// deploy
				if (xmlBytes != null) {
					try {
						bais = new ByteArrayInputStream(xmlBytes);
						deployment = repositoryService.createDeployment().name(strTitle + ".bpmn20.xml")
								.addInputStream(strTitle + ".bpmn20.xml", bais)
								.disableSchemaValidation()
//								.category(String.valueOf(companyId))
								.tenantId(tenantId)
								.deploy();
					} catch (ActivitiException ae) {
						//save exception
						activitiException = ae;
					}
				}
			}

			if (deployment == null) {
				if (activitiException != null) {
					_log.error("Unable to deploy worfklow definition", activitiException);
					throw new WorkflowException("Cannot deploy definition", activitiException);
				} else {
					_log.error("No workflows found");
					throw new WorkflowException("Cannot deploy definition");
				}
			}

			List<ProcessDefinition> processDefs = repositoryService.createProcessDefinitionQuery()
					.deploymentId(deployment.getId())
					.processDefinitionTenantId(tenantId)
					.list();

			StringBuilder sb = new StringBuilder();
			if (isBar) {
				sb.append("Bar ");
			} else {
				sb.append("Process ");
			}
			if (StringUtils.isNotEmpty(strTitle)) {
				sb.append(strTitle + " ");
			}
			if (isBar) {
				sb.append("with " + processDefs.size() + " processes ");
			}
			sb.append("deployed with deployment ID " + deployment.getId() + " in company " + tenantId);
			_log.info(sb.toString());

			if (processDefs.size() == 0) {
				if (activitiException != null) {
					_log.error("Unable to deploy worfklow definition", activitiException);
				} else {
					_log.error("No workflows found");
				}

				throw new WorkflowException("No process definitions found");
			}

			for (ProcessDefinition processDefinition : processDefs) {
				_log.info("Process Definition Id for process " + processDefinition.getName() + " : " + processDefinition.getId());

				//add title as a resource

				saveDefinitionTitle(processDefinition, title);
			}

			// create result (return first process def in case many was deployed
			return new WorkflowDefinitionImpl(processDefs.get(0), title);
		} catch (Exception ex) {
			_log.error("Cannot deploy definition", ex);
			throw new WorkflowException("Cannot deploy definition", ex);
		}
	}

	@Override
	public int getActiveWorkflowDefinitionCount(long companyId)
			throws WorkflowException {
		ProcessDefinitionQuery query = createProcessDefinitionQuery(companyId, true);
		
		Long count = query.count();
		return count.intValue();
	}

	@Override
	public int getActiveWorkflowDefinitionCount(long companyId, String name)
			throws WorkflowException {
		ProcessDefinitionQuery query = null;

		// try to get serialized parameters map from name
		Map<String, Object> parameters = WorkflowUtil.convertValueToParameterValue(name);

		if (!parameters.equals(MapUtils.EMPTY_MAP)) {
			query  = createProcessDefinitionQueryByParameters(companyId, parameters, true);
		} else {
			query = createProcessDefinitionQuery(companyId, name, null);
		}

		Long count = query.count();
		return count.intValue();
	}

	@Override
	public List<WorkflowDefinition> getActiveWorkflowDefinitions(
			long companyId, int start, int end,
			OrderByComparator orderByComparator) throws WorkflowException {
		ProcessDefinitionQuery query = createProcessDefinitionQuery(companyId, true);

		List<WorkflowDefinition> workflowDefinitions = getWorkflowDefinitions(query, start, end, orderByComparator);

		return workflowDefinitions;
	}

	@Override
	public List<WorkflowDefinition> getActiveWorkflowDefinitions(
			long companyId, String name, int start, int end,
			OrderByComparator orderByComparator) throws WorkflowException {
		ProcessDefinitionQuery query = null;

		// try to get serialized parameters map from name
		Map<String, Object> parameters = WorkflowUtil.convertValueToParameterValue(name);

		if (!parameters.equals(MapUtils.EMPTY_MAP)) {
			query = createProcessDefinitionQueryByParameters(companyId, parameters, true);
		} else {
			query = createProcessDefinitionQuery(companyId, name, null);
		}

		List<WorkflowDefinition> workflowDefinitions = getWorkflowDefinitions(query, start, end, orderByComparator);

		return workflowDefinitions;
	}

	@Override
	public WorkflowDefinition getLatestKaleoDefinition(long companyId,
			String name) throws WorkflowException {
		_log.warn("Method is partialy implemented but it doesn't work correct :)");
		// TODO: we have to use keys instead of names. Option "latest" can be used only with key.
		// ProcessDefinitionQuery query = createProcessDefinitionQueryLatest(companyId, name);
		// WorkflowDefinition workflowDefinition = getWorkflowDefinition(query);
		// return workflowDefinition;

		// temporary hook
		ProcessDefinition processDefinition = getProcessDefinition(companyId, name);

		if (processDefinition == null) {
    		return null;
    	} else {
    		return getWorkflowDefinition(processDefinition);
    	}
	}

	@Override
	public WorkflowDefinition getWorkflowDefinition(long companyId,
			String name, int version) throws WorkflowException {
		ProcessDefinition processDefinition = null;
		
		// check version
		if (version > 0) {
			processDefinition = getProcessDefinition(companyId, name, version);
		} else {
			// if 0 - return latest workflow definition
			processDefinition = getProcessDefinition(companyId, name);
		}
		

		if (processDefinition == null) {
    		return null;
    	} else {
    		return getWorkflowDefinition(processDefinition);
    	}
	}

	@Override
	public int getWorkflowDefinitionCount(long companyId)
			throws WorkflowException {
		ProcessDefinitionQuery query = createProcessDefinitionQuery(companyId, null);
		
		Long count = query.count();
		return count.intValue();
	}

	@Override
	public int getWorkflowDefinitionCount(long companyId, String name)
			throws WorkflowException {
		ProcessDefinitionQuery query = null;

		// try to get serialized parameters map from name
		Map<String, Object> parameters = WorkflowUtil.convertValueToParameterValue(name);

		if (!parameters.equals(MapUtils.EMPTY_MAP)) {
			query = createProcessDefinitionQueryByParameters(companyId, parameters);
		} else {
			query = createProcessDefinitionQuery(companyId, name, null);
		}

		Long count = query.count();
		return count.intValue();
	}

	@Override
	public List<WorkflowDefinition> getWorkflowDefinitions(long companyId,
			int start, int end, OrderByComparator orderByComparator)
			throws WorkflowException {
		ProcessDefinitionQuery query = createProcessDefinitionQuery(companyId, null);

		List<WorkflowDefinition> workflowDefinitions = getWorkflowDefinitions(query, start, end, orderByComparator);

		return workflowDefinitions;
	}

	@Override
	public List<WorkflowDefinition> getWorkflowDefinitions(long companyId,
			String name, int start, int end, OrderByComparator orderByComparator)
			throws WorkflowException {
		ProcessDefinitionQuery query = null;

		// try to get serialized parameters map from name
		Map<String, Object> parameters = WorkflowUtil.convertValueToParameterValue(name);

		if (!parameters.equals(MapUtils.EMPTY_MAP)) {
			query = createProcessDefinitionQueryByParameters(companyId, parameters);
		} else {
			query = createProcessDefinitionQuery(companyId, name, null);
		}

		List<WorkflowDefinition> workflowDefinitions = getWorkflowDefinitions(query, start, end, orderByComparator);

		return workflowDefinitions;
	}

	@Override
	public void undeployWorkflowDefinition(long companyId, long userId,
			String name, int version) throws WorkflowException {
		// TODO - Not sure it is supported
    	// for now we will simple set process inactive
		ProcessDefinition processDefinition = getProcessDefinition(companyId, name, version);
		try {
			repositoryService.deleteDeployment(processDefinition.getDeploymentId());
		} catch (RuntimeException e) {
			_log.info("Could not remove deployment. There are still some tasks or jobs exist for this deployment id = " + processDefinition == null ? "" : processDefinition.getDeploymentId());
		}
		// updateActive(companyId, userId, name, version, false);
	}

	@Override
	public WorkflowDefinition updateActive(long companyId, long userId,
			String name, int version, boolean active) throws WorkflowException {
		ProcessDefinition processDefinition = getProcessDefinition(companyId, name, version);
		try {
			if (active && processDefinition.isSuspended()) {
				repositoryService.activateProcessDefinitionById(processDefinition.getId());
			} else if (!active && !processDefinition.isSuspended()) {
				repositoryService.suspendProcessDefinitionById(processDefinition.getId());
			}

			// retrieve updated
			processDefinition = repositoryService.getProcessDefinition(processDefinition.getId());

			WorkflowDefinition workflowDefinition = getWorkflowDefinition(processDefinition);

			return workflowDefinition;
		} catch (Exception e) {
			if (active) {
				_log.error("Failed to activate process " + name + " with version " + version, e);
			} else {
				_log.error("Failed to suspend process " + name + " with version " + version, e);
			}
			throw new WorkflowException(e);
		}
	}

	@Override
	public WorkflowDefinition updateTitle(long companyId, long userId,
			String name, int version, String title) throws WorkflowException {
		ProcessDefinition processDefinition = getProcessDefinition(companyId, name, version);
		try {
			saveDefinitionTitle(processDefinition, title);
			
			WorkflowDefinition workflowDefinition = getWorkflowDefinition(processDefinition);
			
			return workflowDefinition;
		} catch (Exception e) {
			_log.error("Failed to update title", e);
			throw new WorkflowException(e);
		}
	}

	@Override
	public void validateWorkflowDefinition(byte[] bytes)
			throws WorkflowException {
		try {
    		BpmnModel bpmnModel = readXMLFile(bytes);
    		validate(bpmnModel);
    	} catch (WorkflowException e) {
    		throw e;
    	} catch (Exception e) {
    		throw new WorkflowException("Could not validate XML", e);
    	}
	}

}
