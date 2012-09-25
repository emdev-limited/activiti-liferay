package net.emforge.activiti;

import groovy.transform.Synchronized;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

import net.emforge.activiti.dao.WorkflowDefinitionExtensionDao;
import net.emforge.activiti.entity.WorkflowDefinitionExtensionImpl;
import net.emforge.activiti.util.SignavioFixer;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.liferay.portal.kernel.dao.orm.QueryUtil;
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
    
    /** Cache for ProcessDefinitions
     * TODO - think - probably to use some of Spring Caching features
     */
    private Map<String, ProcessDefinition> processDefMap = new HashMap<String, ProcessDefinition>();
    
    /** Deploy new workflow
     * 
     * TODO Currently we supporting only deployment of jpdl.xml - need add support for whole par deployment
     */
    @Override
    public WorkflowDefinition deployWorkflowDefinition(long companyId,
                                                       long userId, 
                                                       String title, 
                                                       InputStream inputStream) throws WorkflowException {
        try {
            String strTitle = LocalizationUtil.getLocalization(title, "en_US", true);
            _log.info("Try to deploy process " + strTitle);
    
            // since we may need to reuse this input stream - lets copy it into bytes and user ByteInputStream
            byte[] bytes = IOUtils.toByteArray(inputStream);
            
            // try to fix xml
            SignavioFixer fixer = new SignavioFixer(strTitle);
            byte[] xmlBytes = fixer.fixSignavioXml(bytes);
            
            // deploy
            Deployment deployment = null;
            ActivitiException activitiException = null;			
            if (xmlBytes != null) {
                try {
                    ByteArrayInputStream bais = new ByteArrayInputStream(xmlBytes);
                    deployment = repositoryService.createDeployment().addInputStream(strTitle + ".bpmn20.xml", bais).deploy();
                } catch (ActivitiException ae) {
                    //save exception
                    activitiException = ae;
                }
            }
            
            if (deployment == null) {
                _log.info("Cannot deploy process as xml - lets try as bar");

                ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                ZipInputStream zipInputStream = new ZipInputStream(bais);
                try {
                deployment = repositoryService.createDeployment().name(strTitle + ".bar").addZipInputStream(zipInputStream).deploy();
                } catch (ActivitiException ae) {
                    //save exception
                    activitiException = ae;
                }
            }
            
            if (deployment == null) {
                if (activitiException != null) {
                    _log.error("Unable to deploy worfklow definition", activitiException);
                    throw new WorkflowException("Cannot deploy definition: " + activitiException.getMessage());
                } else {
                    _log.error("No workflows found");
                    throw new WorkflowException("Cannot deploy definition");
                }
                
                
            }

            ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery();
            processDefinitionQuery.deploymentId(deployment.getId());
            List<ProcessDefinition> processDefs = processDefinitionQuery.list();
    
            _log.info("Process " + strTitle + " deployed with deployment ID " + deployment.getId());
            
            _log.info(processDefs.size() + " process definitions deployed");
            
            if (processDefs.size() == 0) {
                if (activitiException != null) {
                    _log.error("Unable to deploy worfklow definition", activitiException);
                    throw new WorkflowException("No process definitions found:"  + activitiException.getMessage());
                } else {
                    _log.error("No workflows found");
                    throw new WorkflowException("No process definitions found");
                }

                
            }
            
            for (ProcessDefinition processDef : processDefs) {
                _log.info("Process Definition Id for process " + processDef.getName() + " : " + processDef.getId());
                
                WorkflowDefinitionExtensionImpl workflowDefinitionExtension =
                    new WorkflowDefinitionExtensionImpl(processDef, companyId, title, processDef.getName(), true, processDef.getVersion());
        
                workflowDefinitionExtensionDao.saveOrUpdate(workflowDefinitionExtension);
            }
            
            // create result (return first process def in case many was deployed
            if (processDefs.size() > 0) {
            	return new WorkflowDefinitionImpl(processDefs.get(0), title, true);
            } else {
            	return null;
            }
        } catch (WorkflowException ex) {
            throw ex;
        } catch (Exception ex) {
            _log.error("Cannot deploy definition", ex);
            throw new WorkflowException("Cannot deploy definition", ex);
        }
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

    /** 
	 * added by Maxx
	 */
	@Override
	public WorkflowDefinition getLatestKaleoDefinition(long companyId, String name) throws WorkflowException {
		 
		//return last (active) workflow definition
		return getWorkflowDefinition(companyId, name, 0);
	}
	
    @Override
    public WorkflowDefinition getWorkflowDefinition(long companyId, String name, int version) throws WorkflowException {
        _log.info("try to get workflow definition, name: " + name + " , version " + version);
        
        WorkflowDefinitionExtensionImpl def = getWorkflowDefinitionExt(companyId, name, version);
        if (def == null) {
        	return null;
        } else {
        	return new WorkflowDefinitionImpl(def);
        }
    }

    /** This method not implemnted basic interface but still public since used from servlet
     * 
     * @param companyId
     * @param name
     * @param version
     * @return
     */
    public WorkflowDefinitionExtensionImpl getWorkflowDefinitionExt(long companyId, String name, int version) {
        if (version != 0) {
            WorkflowDefinitionExtensionImpl def = workflowDefinitionExtensionDao.find(companyId, name, version);
            return def;
        } else {
            // return last (active) workflow definition
            List<WorkflowDefinitionExtensionImpl> defs = workflowDefinitionExtensionDao.find(companyId, name, true, QueryUtil.ALL_POS, QueryUtil.ALL_POS);
            if (defs.size() == 0) {
                return null;
            }
            
            if (defs.size() > 1) {
                _log.warn("More then 1 active workflow definition found for name: " + name);
            }
            
            return defs.get(0);
        }
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
    
    /** 
	 * added by Maxx
	 *  
	 * TODO I don't know what we must to do here =)
	 */
	public void validateWorkflowDefinition(InputStream inputStream)
			throws WorkflowException {
		_log.info("Has been called validateWorkflowDefinition(...)");
	}
	
	/** Get process definition with using cache
	 * 
	 * @param definitionId
	 * @return
	 */
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
}