package net.emforge.activiti.identity;

import java.util.ArrayList;
import java.util.List;

import net.emforge.activiti.IdMappingService;
import net.emforge.activiti.WorkflowUtil;

import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.persistence.entity.UserEntity;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.UserGroupRole;
import com.liferay.portal.service.RoleLocalServiceUtil;
import com.liferay.portal.service.UserGroupRoleLocalServiceUtil;
import com.liferay.portal.service.UserLocalServiceUtil;

@Service("liferayIdentityService")
public class LiferayIdentityService {
	private static Log _log = LogFactoryUtil.getLog(LiferayIdentityService.class);

	@Autowired
	IdMappingService idMappingService;

	// Groups
	
	public List<Group> findGroupsByUser(String userName) {
		try {
			// get regular roles
			List<Role> roles = RoleLocalServiceUtil.getUserRoles(idMappingService.getUserId(userName));

			// conert from site roles to the groups
			List<Group> groups = new ArrayList<Group>();
			for (Role role : roles) {
				GroupImpl groupImpl = new GroupImpl(role);
				groups.add(groupImpl);
			}
			
			// get group roles for specified user
			List<UserGroupRole> groupRoles = UserGroupRoleLocalServiceUtil.getUserGroupRoles(idMappingService.getUserId(userName));
			for (UserGroupRole groupRole : groupRoles) {
				GroupImpl groupImpl = new GroupImpl(groupRole);
				groups.add(groupImpl);
			}
			
			return groups;
		} catch (Exception e) {
			_log.error("Cannot get list of roles for user: " + userName, e);
			return new ArrayList<Group>();
		}
	}

	public List<User> findUsersByGroup(long companyId, String groupName) {
		return WorkflowUtil.findUsersByGroup(companyId, groupName);
	}
	
	public Role findRole(long companyId, String groupName) {
		// first - try to parse group to identify - it is regular group or org/community group
		String[] parsedName = groupName.split("/");
		List<com.liferay.portal.model.User> users = null;
		List<User> result = new ArrayList<User>();
		
		try {
			if (parsedName.length == 1) {
				// regilar group
				Role role = RoleLocalServiceUtil.getRole(companyId, groupName);
				
				return role;
			} else {
				long groupId = Long.valueOf(parsedName[0]);
				groupName = parsedName[1];
				
				if (parsedName.length > 2) {
					groupName = StringUtils.join(ArrayUtils.subarray(parsedName, 1, parsedName.length), "/");
				}
				
				Role role = RoleLocalServiceUtil.getRole(companyId, groupName);
				
				return role;
			}
		} catch (Exception ex) {
			_log.warn("Cannot get group users", ex);
			return null;
		}
	}

	// Users
	
	public UserEntity findUserById(String userName) {
		try {
			com.liferay.portal.model.User liferayUser = UserLocalServiceUtil.getUser(idMappingService.getUserId(userName));
			return new UserImpl(liferayUser);
		} catch (Exception ex) {
			_log.error("Cannot find user " + userName + " : " + ex.getMessage());
			return null;
		}
	}

}
