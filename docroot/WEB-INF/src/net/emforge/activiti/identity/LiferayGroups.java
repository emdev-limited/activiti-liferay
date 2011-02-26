package net.emforge.activiti.identity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.impl.runtime.ExecutionEntity;
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
 *
 */
@Service("liferayGroups")
public class LiferayGroups {
	private static Log _log = LogFactoryUtil.getLog(LiferayGroups.class);
	
	public String getGroups(ExecutionEntity execution, String groups) {
		_log.info("Convet groups : " + groups);
		
		long companyId = GetterUtil.getLong((Serializable)execution.getVariable(WorkflowConstants.CONTEXT_COMPANY_ID));
		long groupId = GetterUtil.getLong((Serializable)execution.getVariable(WorkflowConstants.CONTEXT_GROUP_ID));
		
		String[] groupsArray = groups.split(",");
		List<String> resultGroups = new ArrayList<String>(groupsArray.length);
		
		for (String group : groupsArray) {
			try {
				group = group.trim();
				if (StringUtils.isNotBlank(group)) {
					Role role = RoleLocalServiceUtil.getRole(companyId, group);
					
					if (RoleConstants.TYPE_REGULAR != role.getType()) {
						// use groupId as prefix
						group = String.valueOf(groupId) + "/" + group;
					}
					
					resultGroups.add(group);
					_log.debug("Task assigned to role " + group);
				}
			} catch (Exception ex) {
				_log.warn("Cannot assign task to role " + group, ex);
			}
		}
		
		String result = StringUtils.join(resultGroups, ",");
		_log.info("Converted to " + result);
		return result;
	}
}
