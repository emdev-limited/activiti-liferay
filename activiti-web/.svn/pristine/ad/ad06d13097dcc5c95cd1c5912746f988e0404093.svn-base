package net.emforge.activiti;

import net.emforge.activiti.dao.ProcessInstanceExtensionDao;
import net.emforge.activiti.dao.WorkflowDefinitionExtensionDao;
import net.emforge.activiti.entity.ProcessInstanceExtensionImpl;
import net.emforge.activiti.entity.WorkflowDefinitionExtensionImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.workflow.WorkflowException;
import com.liferay.portal.model.Company;
import com.liferay.portal.model.User;
import com.liferay.portal.service.CompanyLocalServiceUtil;
import com.liferay.portal.service.UserLocalServiceUtil;

/** Target of this service is mapping of jbpm4 ids for different objects (String) to Liferay ids (longs)
 * 
 * @author akakunin
 *
 */
@Service("idMappingService")
public class IdMappingService {
	private static Log _log = LogFactoryUtil.getLog(IdMappingService.class);

	private static final String ACTIVITI_USER_NAME_STRATEGY="activiti.userName.strategy";
	private static final String ACTIVITI_USER_NAME_SCREENNAME="screenName";
	
	@Value("${activiti.userName.strategy}")
	private String userNameStrategy;
	
	private Boolean useScreenName = null;
	
	public boolean isUseScreenName() {
		if (useScreenName == null) {
			//String userNameStrategy = PropsUtil.get(ACTIVITI_USER_NAME_STRATEGY);
			_log.info("Activiti user name strategy: " + userNameStrategy);
			
			if (ACTIVITI_USER_NAME_SCREENNAME.equals(userNameStrategy)) {
				useScreenName = true;
			} else {
				useScreenName = false;
			}
		}
		
		return useScreenName;
	}
	
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
	
	public String getActivitiProcessInstanceId(Long id) {
		ProcessInstanceExtensionImpl procInst = processInstanceExtensionDao.get(ProcessInstanceExtensionImpl.class, id);
		if (procInst == null) {
			return null;
		} else {
			return procInst.getProcessInstanceId();
		}
	}
	
	public String getUserName(long userId) throws WorkflowException {
		if (isUseScreenName()) {
			try {
				User user = UserLocalServiceUtil.getUser(userId);
				
				return user.getScreenName();
			} catch (Exception ex) {
				throw new WorkflowException("Cannot get user", ex);
			}
		} else {
			return String.valueOf(userId);
		}
	}
	
	/** Convert from userName, stored in DB to userId, used in Liferay Workflow API
	 * 
	 * @param screenName
	 * @return
	 * @throws WorkflowException
	 */
	public long getUserId(String screenName) throws WorkflowException {
		if (isUseScreenName()) {
			try {
				// TODO - for now always use default company - need to be changed
				Company defaultCompany =  CompanyLocalServiceUtil.getCompanyByWebId(PropsUtil.get(PropsKeys.COMPANY_DEFAULT_WEB_ID));
				
				User user = UserLocalServiceUtil.getUserByScreenName(defaultCompany.getCompanyId(), screenName);
				
				return user.getUserId();
			} catch (Exception ex) {
				throw new WorkflowException("Cannot get user", ex);
			}
		} else {
			return Long.valueOf(screenName);
		}
	}
}
