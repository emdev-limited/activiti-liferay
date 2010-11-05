package net.emforge.activiti;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipInputStream;

import net.emforge.activiti.dao.WorkflowDefinitionExtensionDao;
import net.emforge.activiti.entity.WorkflowDefinitionExtensionImpl;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.LocalizationUtil;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.workflow.WorkflowDefinition;
import com.liferay.portal.kernel.workflow.WorkflowDefinitionManager;
import com.liferay.portal.kernel.workflow.WorkflowException;

/** Own Implementation for Workflow Definition Manager
 * 
 * @author akakunin
 *
 */
@Service(value="workflowDefinitionManager")
public class WorkflowDefinitionManagerImpl implements WorkflowDefinitionManager {
	private static Log _log = LogFactoryUtil.getLog(WorkflowDefinitionManagerImpl.class);
	
	@Autowired
	RepositoryService repositoryService;
	@Autowired
	WorkflowDefinitionExtensionDao workflowDefinitionExtensionDao;
	
	
	/** Deploy new workflow
	 * 
	 * TODO Currently we supporting only deployment of jpdl.xml - need add support for whole par deployment
	 */
	@Override
	public WorkflowDefinition deployWorkflowDefinition(long companyId,
													   long userId, 
													   String title, 
													   InputStream inputStream) throws WorkflowException {
		String strTitle = LocalizationUtil.getLocalization(title, "en_US", true);
		
		_log.info("Try to deploy process " + strTitle);

		ZipInputStream zipInputStream = new ZipInputStream(inputStream);
		Deployment deployment = repositoryService.createDeployment().name(strTitle + ".bar").addZipInputStream(zipInputStream).deploy();
        
		_log.info("Process " + strTitle + " deployed with deployment ID " + deployment.getId());
		
        // get process definition
        ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery();
        processDefinitionQuery.deploymentId(deployment.getId());
        ProcessDefinition processDef = processDefinitionQuery.singleResult();
        
        _log.info("Process Definition Id for process " + strTitle + " : " + processDef.getId());
        
        // save our extension
		WorkflowDefinitionExtensionImpl workflowDefinitionExtension =
			new WorkflowDefinitionExtensionImpl(processDef, companyId, title, processDef.getName(), true, processDef.getVersion());

		workflowDefinitionExtensionDao.saveOrUpdate(workflowDefinitionExtension);
		
        // create result
		return new WorkflowDefinitionImpl(processDef, title, true);
	}

	/** Return count of active workflow definitions
	 */
	@Override
	public int getActiveWorkflowDefinitionCount(long companyId) throws WorkflowException {
		return workflowDefinitionExtensionDao.count(companyId, null, true).intValue();
	}

	@Override
	public int getActiveWorkflowDefinitionCount(long companyId, String name) throws WorkflowException {
		return workflowDefinitionExtensionDao.count(companyId, name, true).intValue();
	}

	/** return list of process definitions
	 * 
	 * TODO Take into account requested sorting
	 */
	@Override
	public List<WorkflowDefinition> getActiveWorkflowDefinitions(long companyId, int start, int end,
																 OrderByComparator orderByComparator) throws WorkflowException {
		List<WorkflowDefinitionExtensionImpl> defs = workflowDefinitionExtensionDao.find(companyId, null, true, start, end);
		
		return getWorkflowDefinitions(defs);
	}

	@Override
	public List<WorkflowDefinition> getActiveWorkflowDefinitions(long companyId, String name, 
																 int start, int end,
																 OrderByComparator orderByComparator) throws WorkflowException {
		List<WorkflowDefinitionExtensionImpl> defs = workflowDefinitionExtensionDao.find(companyId, name, true, start, end);
		
		return getWorkflowDefinitions(defs);
	}

	@Override
	public WorkflowDefinition getWorkflowDefinition(long companyId, String name, int version) throws WorkflowException {
		WorkflowDefinitionExtensionImpl def = workflowDefinitionExtensionDao.find(companyId, name, version);
		return new WorkflowDefinitionImpl(def);
	}

	@Override
	public int getWorkflowDefinitionCount(long companyId) throws WorkflowException {
		return workflowDefinitionExtensionDao.count(companyId, null, null).intValue();
	}

	@Override
	public int getWorkflowDefinitionCount(long companyId, String name) throws WorkflowException {
		return workflowDefinitionExtensionDao.count(companyId, name, null).intValue();
	}

	/** Get workflow definitions
	 * 
	 * TODO Take into account requested sorting
	 */
	@Override
	public List<WorkflowDefinition> getWorkflowDefinitions(long companyId,
														   int start, 
														   int end, 
														   OrderByComparator orderByComparator) throws WorkflowException {
		List<WorkflowDefinitionExtensionImpl> defs = workflowDefinitionExtensionDao.find(companyId, null, null, start, end);
		
		return getWorkflowDefinitions(defs);
	}

	/** Get workflow definitions by name
	 * 
	 * TODO Take into account requested sorting
	 */
	@Override
	public List<WorkflowDefinition> getWorkflowDefinitions(long companyId, String name, 
														   int start, int end, OrderByComparator orderByComparator)
			throws WorkflowException {
		List<WorkflowDefinitionExtensionImpl> defs = workflowDefinitionExtensionDao.find(companyId, name, null, start, end);
		
		return getWorkflowDefinitions(defs);
	}

	@Override
	public void undeployWorkflowDefinition(long companyId, long userId, String name, int version) throws WorkflowException {
		// TODO - Not sure it is supported
		// for now we will simple set process inactive
		updateActive(companyId, userId, name, version, false);

	}

	@Override
	public WorkflowDefinition updateActive(long companyId, long userId, String name, 
										   int version, boolean active) throws WorkflowException {
		WorkflowDefinitionExtensionImpl def = workflowDefinitionExtensionDao.find(companyId, name, version);
		
		def.setActive(active);
		workflowDefinitionExtensionDao.saveOrUpdate(def);
		
		return new WorkflowDefinitionImpl(def);
	}

	@Override
	public WorkflowDefinition updateTitle(long companyId, long userId, String name, int version, 
										  String title) throws WorkflowException {
		WorkflowDefinitionExtensionImpl def = workflowDefinitionExtensionDao.find(companyId, name, version);
		
		def.setTitle(title);
		workflowDefinitionExtensionDao.saveOrUpdate(def);
		
		return new WorkflowDefinitionImpl(def);
	}

	/** Convert internal workflow definitions objects to interface objects
	 * 
	 * @param defs
	 * @return
	 */
	private List<WorkflowDefinition> getWorkflowDefinitions(List<WorkflowDefinitionExtensionImpl> defs) {
		List<WorkflowDefinition> result = new ArrayList<WorkflowDefinition>(defs.size());
		
		for (WorkflowDefinitionExtensionImpl defImpl : defs) {
            result.add(new WorkflowDefinitionImpl(defImpl));
        }
        
		return result;
	}

}
