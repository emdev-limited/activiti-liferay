package net.emforge.activiti;

import groovy.transform.Synchronized;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import net.emforge.activiti.dao.WorkflowDefinitionExtensionDao;
import net.emforge.activiti.query.ExtProcessDefinitionQuery;
import net.emforge.activiti.query.ExtProcessDefinitionQueryImpl;
import net.emforge.activiti.util.SignavioFixer;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.cmd.GetResourceCmd;
import org.activiti.engine.impl.cmd.SaveResourceCmd;
import org.activiti.engine.impl.persistence.entity.ResourceEntity;
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
import com.liferay.portal.kernel.util.StringPool;
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
        	boolean isBar = false;
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
                    deployment = repositoryService.createDeployment().name(strTitle + ".bpmn20.xml")
                    		.addInputStream(strTitle + ".bpmn20.xml", bais)
                    		.category(String.valueOf(companyId)).deploy();
                } catch (ActivitiException ae) {
                    //save exception
                    activitiException = ae;
                }
            }
            
            if (deployment == null) {
                _log.info("Cannot deploy process as xml - lets try as bar");

                ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                ZipInputStream zipInputStream = new ZipInputStream(bais);
                ZipEntry zipEntry = zipInputStream.getNextEntry();
                if (zipEntry != null) {
	                try {
	                	deployment = repositoryService.createDeployment().name(strTitle + ".bar")
	                			.addZipInputStream(zipInputStream)
	                    		.category(String.valueOf(companyId)).deploy();
	                	isBar = true;
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

            ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery();
            processDefinitionQuery.deploymentId(deployment.getId());
            List<ProcessDefinition> processDefs = processDefinitionQuery.list();
    
            _log.info("Process " + strTitle + " deployed with deployment ID " + deployment.getId());
            
            _log.info(processDefs.size() + " process definitions deployed");
            
            if (processDefs.size() == 0) {
                if (activitiException != null) {
                    _log.error("Unable to deploy worfklow definition", activitiException);
                } else {
                    _log.error("No workflows found");
                }

                throw new WorkflowException("No process definitions found");
            }
            
//            CommandContext commandContext = Context.getCommandContext();
//            DbSqlSession dbSqlSession = commandContext.getSession(DbSqlSession.class);
            for (ProcessDefinition processDef : processDefs) {
                _log.info("Process Definition Id for process " + processDef.getName() + " : " + processDef.getId());
                
                //add title as a resource
                ResourceEntity resource = new ResourceEntity();
                resource.setDeploymentId(processDef.getDeploymentId());
                resource.setGenerated(false);
                resource.setName(processDef.getId() + StringPool.COLON + "title");
                resource.setBytes(title.getBytes("UTF-8"));
                RepositoryServiceImpl serviceImpl = (RepositoryServiceImpl) repositoryService;
                serviceImpl.getCommandExecutor().execute(new SaveResourceCmd(resource));
                
//                ProcessDefinitionEntity processDefEntity = (ProcessDefinitionEntity) processDef;
//                String cat = processDefEntity.getCategory() == null ? StringPool.BLANK : processDefEntity.getCategory();
//                if (!cat.endsWith(StringPool.SLASH)) {
//                	cat += StringPool.SLASH;
//                }
//                processDefEntity.setCategory(cat + companyId);
//                serviceImpl.getCommandExecutor().execute(new UpdateProcessDefinitionCmd(processDefEntity));
            }
            
            // create result (return first process def in case many was deployed
            if (processDefs.size() > 0) {
            	return new WorkflowDefinitionImpl(processDefs.get(0), title, true);
            } else {
            	return null;
            }
        } catch (Exception ex) {
            _log.error("Cannot deploy definition", ex);
            throw new WorkflowException("Cannot deploy definition", ex);
        }
    }

    /** Return count of active workflow definitions
     */
    @Override
    public int getActiveWorkflowDefinitionCount(long companyId) throws WorkflowException {
    	//Get all deployments with category = companyId
    	//then get all procdefs by deploymentIds
    	RepositoryServiceImpl serviceImpl = (RepositoryServiceImpl) repositoryService;
    	ExtProcessDefinitionQuery query = new ExtProcessDefinitionQueryImpl(serviceImpl.getCommandExecutor());
    	query = query.processDefinitionCompanyIdAndName(companyId, null, true);
    	return (new Long(query.count())).intValue();
    }

    @Override
    public int getActiveWorkflowDefinitionCount(long companyId, String name) throws WorkflowException {
    	//Get all deployments with category = companyId
    	//then get all procdefs by deploymentIds and name
    	RepositoryServiceImpl serviceImpl = (RepositoryServiceImpl) repositoryService;
    	ExtProcessDefinitionQuery query = new ExtProcessDefinitionQueryImpl(serviceImpl.getCommandExecutor());
    	query = query.processDefinitionCompanyIdAndName(companyId, name, true);
    	return (new Long(query.count())).intValue();
    }

    /** return list of process definitions
     * 
     * TODO Take into account requested sorting
     */
    @Override
    public List<WorkflowDefinition> getActiveWorkflowDefinitions(long companyId, int start, int end,
                                                                 OrderByComparator orderByComparator) throws WorkflowException {
    	RepositoryServiceImpl serviceImpl = (RepositoryServiceImpl) repositoryService;
    	ExtProcessDefinitionQuery query = new ExtProcessDefinitionQueryImpl(serviceImpl.getCommandExecutor());
    	query = query.processDefinitionCompanyIdAndName(companyId, null, true);
    	List<ProcessDefinition> procDefs = query.listPage(prepareSearchLimits(start, true), prepareSearchLimits(end, false));
        return getWorkflowDefinitionsFromProcessDefinitions(procDefs);
    }

    @Override
    public List<WorkflowDefinition> getActiveWorkflowDefinitions(long companyId, String name, 
                                                                 int start, int end,
                                                                 OrderByComparator orderByComparator) throws WorkflowException {
    	
    	RepositoryServiceImpl serviceImpl = (RepositoryServiceImpl) repositoryService;
    	ExtProcessDefinitionQuery query = new ExtProcessDefinitionQueryImpl(serviceImpl.getCommandExecutor());
    	query = query.processDefinitionCompanyIdAndName(companyId, name, true);
    	List<ProcessDefinition> procDefs = query.listPage(prepareSearchLimits(start, true), prepareSearchLimits(end, false));
    	
        return getWorkflowDefinitionsFromProcessDefinitions(procDefs);
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
        
        ProcessDefinition def = workflowDefinitionExtensionDao.find(companyId, name, version);
        if (def == null) {
        	return null;
        } else {
        	return new WorkflowDefinitionImpl(def, getProcessDefinitionTitle(def), true);
        }
    }
    
    @Override
    public int getWorkflowDefinitionCount(long companyId) throws WorkflowException {
    	RepositoryServiceImpl serviceImpl = (RepositoryServiceImpl) repositoryService;
    	ExtProcessDefinitionQuery query = new ExtProcessDefinitionQueryImpl(serviceImpl.getCommandExecutor());
    	query = query.processDefinitionCompanyIdAndName(companyId, null, null);
    	return (new Long(query.count())).intValue();
    }

    @Override
    public int getWorkflowDefinitionCount(long companyId, String name) throws WorkflowException {
    	RepositoryServiceImpl serviceImpl = (RepositoryServiceImpl) repositoryService;
    	ExtProcessDefinitionQuery query = new ExtProcessDefinitionQueryImpl(serviceImpl.getCommandExecutor());
    	query = query.processDefinitionCompanyIdAndName(companyId, name, null);
    	return (new Long(query.count())).intValue();
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
    	RepositoryServiceImpl serviceImpl = (RepositoryServiceImpl) repositoryService;
    	ExtProcessDefinitionQuery query = new ExtProcessDefinitionQueryImpl(serviceImpl.getCommandExecutor());
    	query = query.processDefinitionCompanyIdAndName(companyId, null, null);
    	List<ProcessDefinition> procDefs = query.listPage(prepareSearchLimits(start, true), prepareSearchLimits(end, false));
    	return getWorkflowDefinitionsFromProcessDefinitions(procDefs);
    }

    /** Get workflow definitions by name
     * 
     * TODO Take into account requested sorting
     */
    @Override
    public List<WorkflowDefinition> getWorkflowDefinitions(long companyId, String name, 
                                                           int start, int end, OrderByComparator orderByComparator)
            throws WorkflowException {
    	RepositoryServiceImpl serviceImpl = (RepositoryServiceImpl) repositoryService;
    	ExtProcessDefinitionQuery query = new ExtProcessDefinitionQueryImpl(serviceImpl.getCommandExecutor());
    	query = query.processDefinitionCompanyIdAndName(companyId, name, null);
    	List<ProcessDefinition> procDefs = query.listPage(prepareSearchLimits(start, true), prepareSearchLimits(end, false));
    	return getWorkflowDefinitionsFromProcessDefinitions(procDefs);
    }

    @Override
    public void undeployWorkflowDefinition(long companyId, long userId, String name, int version) throws WorkflowException {
        // TODO - Not sure it is supported
        // for now we will simple set process inactive
    	ProcessDefinition def = workflowDefinitionExtensionDao.find(companyId, name, version);
    	try {
    		repositoryService.deleteDeployment(def.getDeploymentId());
		} catch (RuntimeException e) {
			_log.info("Could not remove deployment. There are still some tasks or jobs exist for this deployment id = " + def == null ? "" : def.getDeploymentId());
		}
//        updateActive(companyId, userId, name, version, false);

    }

    @Override
    public WorkflowDefinition updateActive(long companyId, long userId, String name, 
                                           int version, boolean active) throws WorkflowException {
    	ProcessDefinition def = workflowDefinitionExtensionDao.find(companyId, name, version);
    	if (active && def.isSuspended()) {
    		repositoryService.activateProcessDefinitionById(def.getId());
    	} else if (!active && !def.isSuspended()) {
    		repositoryService.suspendProcessDefinitionById(def.getId());
    	}
        
        return new WorkflowDefinitionImpl(def, getProcessDefinitionTitle(def), active);
    }

    @Override
    public WorkflowDefinition updateTitle(long companyId, long userId, String name, int version, 
                                          String title) throws WorkflowException {
    	try {
    		ProcessDefinition def = workflowDefinitionExtensionDao.find(companyId, name, version);
        	RepositoryServiceImpl serviceImpl = (RepositoryServiceImpl) repositoryService;
        	ResourceEntity resource = serviceImpl.getCommandExecutor().execute(new GetResourceCmd(def.getDeploymentId(), def.getId() + StringPool.COLON + "title"));
        	if (resource == null) {
        		resource = new ResourceEntity();
                resource.setDeploymentId(def.getDeploymentId());
                resource.setGenerated(false);
                resource.setName(def.getId() + StringPool.COLON + "title");
        	}
        	resource.setBytes(title.getBytes("UTF-8"));
    		serviceImpl.getCommandExecutor().execute(new SaveResourceCmd(resource));
            
            return new WorkflowDefinitionImpl(def, title, def.isSuspended());
		} catch (Exception e) {
			_log.error("Failed to update title", e);
			throw new WorkflowException(e);
		}
    }
    
    /** Convert internal workflow definitions objects to interface objects
     * 
     * @param defs
     * @return
     */
    private List<WorkflowDefinition> getWorkflowDefinitionsFromProcessDefinitions(List<ProcessDefinition> defs) {
        List<WorkflowDefinition> result = new ArrayList<WorkflowDefinition>(defs.size());
        
        for (ProcessDefinition defImpl : defs) {
        	String title = getProcessDefinitionTitle(defImpl);
            result.add(new WorkflowDefinitionImpl(defImpl, title, !defImpl.isSuspended()));
        }
        
        return result;
    }
    
    private String getProcessDefinitionTitle(ProcessDefinition def) {
    	String title = StringPool.BLANK;
    	try {
			//try to get title initially
    		title = getResourceAsString(def.getId() + StringPool.COLON + "title", def.getDeploymentId());
		} catch (ActivitiException e) {
			String defId = def == null ? StringPool.BLANK : def.getId();
			_log.debug("No localized title defined for definition = " + defId);
		}
    	return title;
    }
    
    private String getResourceAsString(String resourceName, String deploymentId) throws ActivitiException {
    	String result = null;
    	InputStream is = null;
    	try {
    		is = repositoryService.getResourceAsStream(deploymentId, resourceName);
    		StringWriter writer = new StringWriter();
    		IOUtils.copy(is, writer, "UTF-8");
    		result = writer.toString();
		} catch (ActivitiException e) {
			_log.debug(String.format("No [%s] resource defined for this deployment id [%s]", resourceName, deploymentId));
			throw e;
		} catch (IOException ioe) {
			_log.error("IO error", ioe);
		} finally {
			try {
				if (is != null) {
					is.close();
					is = null;
				}
			} catch (IOException e2) {
				//no-op
			}
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
	
	private int prepareSearchLimits(int limit, boolean isMin) {
		//if we have -1 as limits - Activiti will return an empty list
		if (limit == QueryUtil.ALL_POS) {
			if (isMin) {
				return 0;
			}
			return Integer.MAX_VALUE;
		}
		return limit;
	}
}