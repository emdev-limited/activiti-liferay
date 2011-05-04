package net.emforge.activiti.identity;

import static net.emforge.activiti.constants.RoleConstants.COMMUNITY_CONTENT_REVIEWER;
import static net.emforge.activiti.constants.RoleConstants.ORGANIZATION_CONTENT_REVIEWER;
import static net.emforge.activiti.constants.RoleConstants.PORTAL_CONTENT_REVIEWER;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.activiti.engine.delegate.DelegateExecution;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.RoleConstants;
import com.liferay.portal.service.RoleLocalServiceUtil;

/** This class is responsible for converting group roles into groups with format groupId/roleName
 * 
 * @author akakunin
 * @author oteichmann
 *
 */
@Service("liferayGroups")
public class LiferayGroups {
	private static Log _log = LogFactoryUtil.getLog(LiferayGroups.class);
	
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
	public Collection<String> getGroupsFromList(DelegateExecution execution, Collection<String> groups) {
		_log.info("Convert groups : " + groups);
		
		long companyId = GetterUtil.getLong((Serializable)execution.getVariable(WorkflowConstants.CONTEXT_COMPANY_ID));
		long groupId = GetterUtil.getLong((Serializable)execution.getVariable(WorkflowConstants.CONTEXT_GROUP_ID));
		
		List<String> resultGroupList = new ArrayList<String>(groups.size());
		
		for (String group : groups) {
			try {
				group = group.trim();
				if (StringUtils.isNotBlank(group)) {
					Role role = RoleLocalServiceUtil.getRole(companyId, group);
					
					if (RoleConstants.TYPE_REGULAR != role.getType()) {
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
		candidateRoleList.add(COMMUNITY_CONTENT_REVIEWER);
		_log.debug("Adding role: " + COMMUNITY_CONTENT_REVIEWER);
		
		return candidateRoleList;
	}
}
