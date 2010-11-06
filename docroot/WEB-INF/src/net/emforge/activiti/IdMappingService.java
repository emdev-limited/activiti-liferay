package net.emforge.activiti;

import java.util.List;

import net.emforge.activiti.dao.ProcessInstanceExtensionDao;
import net.emforge.activiti.dao.WorkflowDefinitionExtensionDao;
import net.emforge.activiti.entity.ProcessInstanceExtensionImpl;
import net.emforge.activiti.entity.WorkflowDefinitionExtensionImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

/** Target of this service is mapping of jbpm4 ids for different objects (String) to Liferay ids (longs)
 * 
 * @author akakunin
 *
 */
@Service("idMappingService")
public class IdMappingService {
	private static Log _log = LogFactoryUtil.getLog(IdMappingService.class);

	@Autowired
	ProcessInstanceExtensionDao processInstanceExtensionDao;
	@Autowired
	WorkflowDefinitionExtensionDao workflowDefinitionExtensionDao;
	
	/** Get Long id for workflow definition id
	 * Format of workflow instance id is 'to_do-1'
	 * 
	 * @param id
	 * @return
	 */
	public Long getLiferayWorkflowDefinitionId(String id) {
		WorkflowDefinitionExtensionImpl workflowInst = workflowDefinitionExtensionDao.findByProcessDefinitionId(id);
		
		if (workflowInst == null) {
			return null;
		} else {
			return workflowInst.getWorkflowDefinitionExtensionId();
		}
	}
	
	public String getJbpmWorkflowDefinitionId(Long id) {
		WorkflowDefinitionExtensionImpl workflowInst = workflowDefinitionExtensionDao.get(WorkflowDefinitionExtensionImpl.class, id);
		
		if (workflowInst == null) {
			return null;
		} else {
			return workflowInst.getProcessDefinitionId();
		}		
	}

	/** Get Long id for process instance id
	 * Format of workflow instance id is 'workflowName.1'
	 */ 
	public Long getLiferayProcessInstanceId(String id) {
		ProcessInstanceExtensionImpl procInst = processInstanceExtensionDao.findByProcessInstanceId(id);
		if (procInst == null) {
			return null;
		} else {
			return procInst.getId();
		}
	}
	
	public String getJbpmProcessInstanceId(Long id) {
		ProcessInstanceExtensionImpl procInst = processInstanceExtensionDao.get(ProcessInstanceExtensionImpl.class, id);
		if (procInst == null) {
			return null;
		} else {
			return procInst.getProcessInstanceId();
		}
	}	
}
