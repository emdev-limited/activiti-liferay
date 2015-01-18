package net.emforge.activiti.workflow;

import groovy.transform.Synchronized;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import net.emforge.activiti.comparator.WorkflowComparatorUtil;
import net.emforge.activiti.dao.WorkflowDefinitionExtensionDao;
import net.emforge.activiti.engine.impl.cmd.GetDefinitionTitleCmd;
import net.emforge.activiti.engine.impl.cmd.SaveDefinitionTitleCmd;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.persistence.deploy.DefaultDeploymentCache;
import org.activiti.engine.impl.persistence.deploy.DeploymentCache;
import org.activiti.engine.query.Query;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.validation.ValidationError;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.workflow.WorkflowDefinition;
import com.liferay.portal.kernel.workflow.WorkflowException;

/**
 * Notes: companyId stores as tenantId for processDefinition.
 * <br />See Multitenancy in User Guide:
 * <br />http://www.activiti.org/userguide/#advanced.tenancy
 * 
 * @author Dmitry Farafonov
 */
public class AbstractWorkflowDefinitionManager {
	private static Log _log = LogFactoryUtil.getLog(AbstractWorkflowDefinitionManager.class);
	
	@Autowired
	RepositoryService repositoryService;

	@Autowired
	WorkflowDefinitionExtensionDao workflowDefinitionExtensionDao;

	/** Cache for ProcessDefinitions
	 * TODO - think - probably to use some of Spring Caching features
	 */
	private Map<String, ProcessDefinition> processDefMap = new HashMap<String, ProcessDefinition>();
	protected DeploymentCache<WorkflowDefinition> workflowDefinitionCache = new DefaultDeploymentCache<WorkflowDefinition>();

	protected void saveDefinitionTitle(ProcessDefinition processDefinition, String title) {
		((RepositoryServiceImpl) repositoryService).getCommandExecutor()
		.execute(
				new SaveDefinitionTitleCmd(processDefinition.getDeploymentId(),
						processDefinition.getId(), title));
	}

	protected String getProcessDefinitionTitle(ProcessDefinition processDefinition) {
		String title = StringPool.BLANK;
		try {
			title = ((RepositoryServiceImpl) repositoryService)
					.getCommandExecutor().execute(
							new GetDefinitionTitleCmd(processDefinition.getDeploymentId(),
									processDefinition.getId()));
		} catch (ActivitiObjectNotFoundException e) {
			if (e.getObjectClass() == Deployment.class) {
				_log.error(e);
			} else {
				_log.debug(e.getMessage());
			}
		}
		return title;
	}

    /**
	 * <p>Get process definition with using cache
	 * <p>Deprecated. Repository service works with own cache
	 * {@link org.activiti.engine.impl.persistence.deploy.DefaultDeploymentCache}
	 * <p>Use this one:
	 * <pre>
	 * repositoryService.getProcessDefinition(processDefinitionId)
	 * </pre>
	 * 
	 * @param definitionId
	 * @return
	 */
	@Deprecated
    @Synchronized
    public ProcessDefinition getProcessDefinition(String definitionId) {
    	// check in cache
    	ProcessDefinition processDef = processDefMap.get(definitionId);

    	if (processDef == null) {
    		// not found
    		ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery();
    		processDefinitionQuery.processDefinitionId(definitionId);
    		processDef =  processDefinitionQuery.singleResult();

    		// put into cache
    		processDefMap.put(definitionId, processDef);
    	}

    	return processDef;
    }

	protected ProcessDefinition getProcessDefinition(long companyId, String name, int version) throws WorkflowException {
		ProcessDefinitionQuery query = createProcessDefinitionQueryByVersion(companyId, name, version);
		ProcessDefinition processDefinition = query.singleResult();
		return processDefinition;
	}

	
	/** Return latest version of Workflow Definition
	 * 
	 * @param companyId
	 * @param name
	 * @return
	 * @throws WorkflowException
	 */
	protected ProcessDefinition getProcessDefinition(long companyId, String name) throws WorkflowException {
		ProcessDefinitionQuery query = createProcessDefinitionQuery(companyId, name, true);
		List<ProcessDefinition> processDefinitions = query.orderByProcessDefinitionVersion().desc().list();
		if (processDefinitions.size() == 0) {
			return null;
		}
		
		ProcessDefinition latestProcessDefinition = processDefinitions.get(0);
		
		return latestProcessDefinition;
	}
	
	protected ProcessDefinitionQuery createProcessDefinitionQuery() throws WorkflowException {
//		return CustomProcessDefinitionQueryImpl.create();
		return repositoryService.createProcessDefinitionQuery();
	}

	protected ProcessDefinitionQuery createProcessDefinitionQuery(long companyId, Boolean active) throws WorkflowException {
		return createProcessDefinitionQuery(companyId, null, active);
	}

	protected ProcessDefinitionQuery createProcessDefinitionQuery(long companyId, String name, Boolean active) throws WorkflowException {
		ProcessDefinitionQuery query = createProcessDefinitionQuery();

		if (companyId > 0) {
			// TODO: check if more than one company exist
			// if (WorkflowUtil.getCompaniesCount() > 1) {
			// }
			query.processDefinitionTenantId(String.valueOf(companyId));
		}

		if (active != null) {
			if (active) {
				query.active();
			} else {
				query.suspended();
			}
		}

		if (name != null) {
			query.processDefinitionName(name);
		}

		return query;
	}

	protected ProcessDefinitionQuery createProcessDefinitionQueryLatest(long companyId, String name) throws WorkflowException {
		ProcessDefinitionQuery query = createProcessDefinitionQuery(companyId, name, null);
//		((CustomProcessDefinitionQuery)query).latestWithinCompany(companyId);
		query.latestVersion();
		return query;
	}

	protected ProcessDefinitionQuery createProcessDefinitionQueryByVersion(long companyId, String name, int version) throws WorkflowException {
		ProcessDefinitionQuery query = createProcessDefinitionQuery(companyId, name, null);
		query.processDefinitionVersion(version);
		return query;
	}

	protected ProcessDefinitionQuery createProcessDefinitionQueryByParameters(
			long companyId, Map<String, Object> parameters) throws WorkflowException {
		return createProcessDefinitionQueryByParameters(companyId, parameters, null);
	}

	protected ProcessDefinitionQuery createProcessDefinitionQueryByParameters(
			long companyId, Map<String, Object> parameters, Boolean active) throws WorkflowException {
		ProcessDefinitionQuery query = createProcessDefinitionQuery(companyId, active);

		String name = GetterUtil.getString(parameters.get("name"));
		if (StringUtils.isNotEmpty(name)) {
			query.processDefinitionName(name);
		}

		String key = GetterUtil.getString(parameters.get("key"));
		if (StringUtils.isNotEmpty(key)) {
			query.processDefinitionKey(key);
		}

		Integer version = GetterUtil.getInteger(parameters.get("version"), 0);
		boolean latest = GetterUtil.getBoolean(parameters.get("latest"), false);
		if (!latest && version > 0) {
			query.processDefinitionVersion(version);
		} else if (latest) {
			query.latestVersion();
		}

		return query;
	}

	/**
	 * <p>
	 * Retrieves workflow instance paged list.
	 * <p>
	 * See
	 * {@link net.emforge.activiti.comparator.WorkflowComparatorUtil#applyComparator(Query, OrderByComparator)
	 * WorkflowComparatorUtil.applyComparator()} to use
	 * {@link OrderByComparator}.
	 * 
	 * @param query
	 * @param start
	 * @param end
	 * @param orderByComparator
	 *            {@link com.liferay.portal.kernel.util.OrderByComparator
	 *            OrderByComparator}
	 * @return
	 */
	protected List<WorkflowDefinition> getWorkflowDefinitions(
			ProcessDefinitionQuery query, int start, int end,
			OrderByComparator orderByComparator) {
		
		// apply comparator to query
		WorkflowComparatorUtil.applyComparator(query, orderByComparator);
		
		return getWorkflowDefinitions(query, start, end);
	}

	protected List<WorkflowDefinition> getWorkflowDefinitions(ProcessDefinitionQuery query, int start, int end) {
		List<ProcessDefinition> definitionList = null;
		if ((start != QueryUtil.ALL_POS) && (end != QueryUtil.ALL_POS)) {
			definitionList = query.listPage(start, end - start);
		} else {
			definitionList = query.list();
		}

		List<WorkflowDefinition> result = new ArrayList<WorkflowDefinition>(definitionList.size());

		for (ProcessDefinition processDefinition : definitionList) {
			try {
				WorkflowDefinition workflowDefinition = getWorkflowDefinition(processDefinition);
				result.add(workflowDefinition);
			} catch (Exception ex) {
				_log.warn("Cannot convert Activiti process definition " + processDefinition.getId() + " into Liferay: " + ex);
				_log.debug("Cannot convert Activiti process into Liferay", ex);
			}
		}

		return result;
	}

	protected WorkflowDefinition getWorkflowDefinition(ProcessDefinitionQuery query) {
		ProcessDefinition processDefinition = query.singleResult();

		WorkflowDefinition workflowDefinition = getWorkflowDefinition(processDefinition);

		return workflowDefinition;
	}

	protected WorkflowDefinition getWorkflowDefinition(ProcessDefinition processDefinition) {
		String title = getProcessDefinitionTitle(processDefinition);
		return new WorkflowDefinitionImpl(processDefinition, title);
	}

	protected BpmnModel readXMLFile(InputStream xmlStream) throws Exception {
		XMLInputFactory xif = XMLInputFactory.newInstance();
		InputStreamReader in = new InputStreamReader(xmlStream, "UTF-8");
		XMLStreamReader xtr = xif.createXMLStreamReader(in);
		return new BpmnXMLConverter().convertToBpmnModel(xtr);
	}

	protected BpmnModel readXMLFile(byte[] xml) throws Exception {
		System.out.println("xml " + new String(xml, "UTF-8"));
		XMLInputFactory xif = XMLInputFactory.newInstance();
		InputStreamReader in = new InputStreamReader(new ByteArrayInputStream(xml), "UTF-8");
		XMLStreamReader xtr = xif.createXMLStreamReader(in);
		return new BpmnXMLConverter().convertToBpmnModel(xtr);
	}

	protected void validate(BpmnModel bpmnModel) throws WorkflowException {
		List<ValidationError> validationErrors = repositoryService.validateProcess(bpmnModel);
		if (CollectionUtils.isNotEmpty(validationErrors)) {
			int errorsCount = 0;
			StringBuilder sb = new StringBuilder();
			sb.append("Workflow definition validation error:");
			sb.append(StringPool.NEW_LINE);
			for (ValidationError error : validationErrors) {
				//String error = bpmnModel.getErrors().get(errorRef);
				if (!error.isWarning()) {
					sb.append(StringPool.TAB + error.getActivityId() + ": " + error.getProblem());
					sb.append(StringPool.NEW_LINE);
					errorsCount++;
				}
			}
			if (errorsCount > 0) {
				_log.error(sb);
				throw new WorkflowException(sb.toString());
			}
		}
	}
}
