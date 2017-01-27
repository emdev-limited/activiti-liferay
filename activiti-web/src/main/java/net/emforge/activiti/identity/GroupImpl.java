package net.emforge.activiti.identity;

import org.activiti.engine.impl.persistence.entity.GroupEntity;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.UserGroup;
import com.liferay.portal.model.UserGroupRole;

/** jBPM Group Implementation based on Liferay Role and UserGroupRole objects
 * 
 * @author akakunin
 *
 */
public class GroupImpl extends GroupEntity {
	private static final long serialVersionUID = -4941419640548944436L;
	private static Log _log = LogFactoryUtil.getLog(GroupImpl.class);

	/** Initialize from Liferay User Group
	 * 
	 * @param role
	 */
	public GroupImpl(UserGroup userGroup) {
		id = userGroup.getCompanyId() + "/" +  userGroup.getName();
		name = userGroup.getName();
		type = "regular";
	}
	
	/** Initialize from Liferay Portal Role
	 * 
	 * @param role
	 */
	public GroupImpl(Role role) {
		id = role.getCompanyId() + "/" +  role.getName();
		name = role.getName();
		type = "regular";
	}
	
	/** Initialize from Liferay site Role
	 * 
	 * @param role
	 */
	public GroupImpl(UserGroupRole groupRole) {
		try {
			id = String.valueOf(groupRole.getGroupId()) + "/" + groupRole.getRole().getName();
			name = groupRole.getRole().getName();
			
			if (groupRole.getGroup().isOrganization()) {
				type = "organization";
			} else {
				type = "community";
			}
		} catch (Exception ex) {
			_log.error("Cannot initialize Liferay GroupImpl", ex);
			throw new RuntimeException("Cannot initialize Liferay GroupImpl", ex);
		}
	}
}
