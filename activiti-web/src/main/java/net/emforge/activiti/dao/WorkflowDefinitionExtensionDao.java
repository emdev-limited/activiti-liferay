package net.emforge.activiti.dao;

import java.util.List;

import net.emforge.activiti.query.ExtProcessDefinitionQuery;
import net.emforge.activiti.query.ExtProcessDefinitionQueryImpl;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.repository.ProcessDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

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
	
    /** Find ProcessDefinition
     * 
     * @param companyId
     * @param name
     * @param version
     * @return
     */
	public ProcessDefinition find(Long companyId, String name, Integer version) {
		if (version != 0) {
        	RepositoryServiceImpl serviceImpl = (RepositoryServiceImpl) repositoryService;
        	ExtProcessDefinitionQuery query = new ExtProcessDefinitionQueryImpl(serviceImpl.getCommandExecutor());
        	query = (ExtProcessDefinitionQuery) query.processDefinitionCompanyIdAndName(companyId, name, null).processDefinitionVersion(new Integer(version));
        	ProcessDefinition procDef = query.singleResult();
            return procDef;
        } else {
            // return last (active) workflow definition
        	RepositoryServiceImpl serviceImpl = (RepositoryServiceImpl) repositoryService;
        	ExtProcessDefinitionQuery query = new ExtProcessDefinitionQueryImpl(serviceImpl.getCommandExecutor());
        	query = (ExtProcessDefinitionQuery) query.processDefinitionCompanyIdAndName(companyId, name, null).orderByProcessDefinitionId().desc();
        	List<ProcessDefinition> procDefs = query.list();
        	
            if (procDefs.size() == 0) {
                return null;
            }
            
            if (procDefs.size() > 1) {
            	_log.warn("More then 1 active workflow definition found for name: " + name);
            }
            
            return procDefs.get(0);
        }
		
	}
	
	/**
	 * Find all versions
	 * 
	 * @param companyId
	 * @param name
	 * @return
	 */
	public List<ProcessDefinition> find(Long companyId, String name) {
		RepositoryServiceImpl serviceImpl = (RepositoryServiceImpl) repositoryService;
    	ExtProcessDefinitionQuery query = new ExtProcessDefinitionQueryImpl(serviceImpl.getCommandExecutor());
    	query = query.processDefinitionCompanyIdAndName(companyId, name, null);
    	return query.list();
	}
}
