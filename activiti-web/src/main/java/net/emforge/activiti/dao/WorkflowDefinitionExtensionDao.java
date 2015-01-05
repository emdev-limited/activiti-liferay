package net.emforge.activiti.dao;

import java.util.List;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.workflow.WorkflowException;

/** Dao for working with WorkflowDefinitionExtension
 *  
 * @author akakunin
 *
 */
@Service(value="workflowDefinitionExtensionDao")
public class WorkflowDefinitionExtensionDao {
	private static Log _log = LogFactoryUtil.getLog(WorkflowDefinitionExtensionDao.class);

	@Autowired
	RepositoryService repositoryService;

	public ProcessDefinition findLatest(Long companyId, String name) throws WorkflowException {
		return find(companyId, name, 0);
	}

	/** Find ProcessDefinition
	 * 
	 * @param companyId
	 * @param name
	 * @param version
	 * @return
	 * @throws WorkflowException 
	 */
	public ProcessDefinition find(Long companyId, String name, Integer version) throws WorkflowException {
		List<ProcessDefinition> processDefinitions = search(companyId, name, version);

		if (processDefinitions.size() == 0) {
			return null;
		} else if (processDefinitions.size() > 1) {
			_log.warn("More then 1 active workflow definition found for name: " + name);
		}
		return processDefinitions.get(0);
	}

	/**
	 * Find all versions
	 * 
	 * @param companyId
	 * @param name
	 * @return
	 * @throws WorkflowException 
	 */
	public List<ProcessDefinition> search(Long companyId, String name) throws WorkflowException {
		return search(companyId, name, null);
	}

	private List<ProcessDefinition> search(Long companyId, String name, Integer version) throws WorkflowException {
		ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();

		if (companyId != null && companyId > 0) {
			query.processDefinitionTenantId(String.valueOf(companyId));
		}
		if (version != null && version != 0) {
			query.processDefinitionVersion(version);
		} else {
			query.latestVersion();
		}
		return query.list();
	}
}
