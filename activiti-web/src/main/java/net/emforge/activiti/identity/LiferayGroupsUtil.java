package net.emforge.activiti.identity;

import static net.emforge.activiti.constants.RoleConstants.SITE_CONTENT_REVIEWER;
import static net.emforge.activiti.constants.RoleConstants.ORGANIZATION_CONTENT_REVIEWER;
import static net.emforge.activiti.constants.RoleConstants.PORTAL_CONTENT_REVIEWER;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import net.emforge.activiti.WorkflowUtil;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.runtime.Execution;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.model.Group;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.RoleConstants;
import com.liferay.portal.service.GroupLocalServiceUtil;
import com.liferay.portal.service.RoleLocalServiceUtil;

/** This class is responsible for converting group roles into groups with format groupId/roleName
 * 
 * @author akakunin
 * @author Oliver Teichmann, PRODYNA AG
 *
 */
@Service("liferayGroups")
public class LiferayGroupsUtil {
	private static Log _log = LogFactoryUtil.getLog(LiferayGroupsUtil.class);
	
	public Collection<String> getGroups(DelegateExecution execution, String groups) {
		String[] groupsArray = groups.split(",");

		return getGroupsFromList(execution, Arrays.asList(groupsArray));
	}
	
	/**
	 * Convert a list of group names into correct format.
	 * @param execution The current process execution.
	 * @param groups The list of group names.
	 * @return A list of Liferay roles.
	 */
	public Collection<String> getGroupsFromList(DelegateExecution execution, Collection<String> groups, String groupFriendlyUrl) {
		_log.info("Convert groups : " + groups);
		//check if friendly url is not null then use it to find group 
		//else pick group up from the execution
		//long companyId = GetterUtil.getLong((Serializable)execution.getVariable(WorkflowConstants.CONTEXT_COMPANY_ID));
		ExecutionEntity topExecution = WorkflowUtil.getTopProcessInstance((Execution) execution);
		long companyId = GetterUtil.getLong((Serializable)topExecution.getVariable(WorkflowConstants.CONTEXT_COMPANY_ID));
		
		long groupId = 0;
		if (StringUtils.isNotEmpty(groupFriendlyUrl)) {
			try {
				Group group = GroupLocalServiceUtil.getFriendlyURLGroup(companyId, groupFriendlyUrl);
				groupId = group.getGroupId();
			} catch (Exception e) {
				_log.warn(String.format("Could not get group by friendly url = [%s]. Using from execution instead", groupFriendlyUrl));
				//use common way
				groupId = GetterUtil.getLong((Serializable)topExecution.getVariable(WorkflowConstants.CONTEXT_GROUP_ID));
			}
		} else {
			groupId = GetterUtil.getLong((Serializable)topExecution.getVariable(WorkflowConstants.CONTEXT_GROUP_ID));
		}
		
		List<String> resultGroupList = new ArrayList<String>(groups.size());
		
		for (String group : groups) {
			try {
				group = group.trim();
				if (StringUtils.isNotBlank(group)) {
					Role role = RoleLocalServiceUtil.getRole(companyId, group);
					
					if (RoleConstants.TYPE_REGULAR == role.getType()) {
						group = String.valueOf(companyId) + "/" +  group;
					} else {
						// use groupId as prefix
						group = String.valueOf(groupId) + "/" + group;
					}
					
					resultGroupList.add(group);
					_log.debug("Task assigned to role " + group);
				}
			} catch (Exception ex) {
				_log.warn("Cannot assign task to role " + group, ex);
			}
		}
		
		String result = StringUtils.join(resultGroupList, ",");
		_log.info("Converted to " + result);
		return resultGroupList;
	}
	
	/**
	 * Convert a list of group names into correct format.
	 * @param execution The current process execution.
	 * @param groups The list of group names.
	 * @return A list of Liferay roles.
	 */
	public Collection<String> getGroupsFromList(DelegateExecution execution, Collection<String> groups) {
		return getGroupsFromList(execution, groups, null);
	}
	
	/**
	 * @return A list of the default approver groups.
	 */
	public List<String> getDefaultApproverGroups() {
		
		List<String> candidateRoleList = new ArrayList<String>();
		candidateRoleList.add(com.liferay.portal.model.RoleConstants.ADMINISTRATOR);
		_log.debug("Adding role: " + com.liferay.portal.model.RoleConstants.ADMINISTRATOR);
		candidateRoleList.add(PORTAL_CONTENT_REVIEWER);
		_log.debug("Adding role: " + PORTAL_CONTENT_REVIEWER);
		candidateRoleList.add(ORGANIZATION_CONTENT_REVIEWER);
		_log.debug("Adding role: " + ORGANIZATION_CONTENT_REVIEWER);
		candidateRoleList.add(SITE_CONTENT_REVIEWER);
		_log.debug("Adding role: " + SITE_CONTENT_REVIEWER);
		
		return candidateRoleList;
	}
}
