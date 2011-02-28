package net.emforge.activiti;

import java.util.List;

import com.liferay.portal.kernel.events.ActionException;
import com.liferay.portal.kernel.events.SimpleAction;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.RoleConstants;
import com.liferay.portal.model.User;
import com.liferay.portal.service.RoleLocalServiceUtil;
import com.liferay.portal.service.UserLocalServiceUtil;

/** Stratup action called for Activiti Workflow Engine
 * 
 * @author akakunin
 *
 */
public class StartUpAction extends SimpleAction {
	private static final String ROLE_PORTAL_CONTENT_REVIEWER = "Portal Content Reviewer";
	private static final String ROLE_COMMUNITY_CONTENT_REVIEWER = "Community Content Reviewer";
	private static final String ROLE_ORGANIZATION_CONTENT_REVIEWER = "Organization Content Reviewer";
	private static final String ROLE_DESCRIPTION = "User responsible for assets reviewing during publishing";
	
	private static Log log = LogFactoryUtil.getLog(StartUpAction.class);
	
	@Override
	public void run(String[] ids) throws ActionException {
		log.info("Activiti StartUp Action");
		
		try {
			for (String companyId : ids) {
				doRun(GetterUtil.getLong(companyId));
			}
		} catch (Exception e) {
			log.error("Initialization failed", e);
			throw new ActionException(e);
		}

	}

	private void doRun(long companyId) throws Exception {
		Role role = null;
		User admin = getAdminUser(companyId);
		
		// check and create required roles
		// Portal Content Reviewer
		try {
			role = RoleLocalServiceUtil.getRole(companyId, ROLE_PORTAL_CONTENT_REVIEWER);
		} catch (Exception ex) {}
		
		if (role == null) {
			log.info("Create role: " + ROLE_PORTAL_CONTENT_REVIEWER);
			RoleLocalServiceUtil.addRole(admin.getUserId(), companyId, ROLE_PORTAL_CONTENT_REVIEWER, null, ROLE_DESCRIPTION, RoleConstants.TYPE_REGULAR);
		}
		
		// Organization Content Reviewer
		role = null;
		try {
			role = RoleLocalServiceUtil.getRole(companyId, ROLE_ORGANIZATION_CONTENT_REVIEWER);
		} catch (Exception ex) {}
		
		if (role == null) {
			log.info("Create role: " + ROLE_ORGANIZATION_CONTENT_REVIEWER);
			RoleLocalServiceUtil.addRole(admin.getUserId(), companyId, ROLE_ORGANIZATION_CONTENT_REVIEWER, null, ROLE_DESCRIPTION, RoleConstants.TYPE_ORGANIZATION);
		}
		
		// Community Content Reviewer
		role = null;
		try {
			role = RoleLocalServiceUtil.getRole(companyId, ROLE_COMMUNITY_CONTENT_REVIEWER);
		} catch (Exception ex) {}
		
		if (role == null) {
			log.info("Create role: " + ROLE_COMMUNITY_CONTENT_REVIEWER);
			RoleLocalServiceUtil.addRole(admin.getUserId(), companyId, ROLE_COMMUNITY_CONTENT_REVIEWER, null, ROLE_DESCRIPTION, RoleConstants.TYPE_COMMUNITY);
		}
	}

	protected User getAdminUser(long companyId) throws Exception  {
		Role adminRole = RoleLocalServiceUtil.getRole(companyId, RoleConstants.ADMINISTRATOR);
		// org admin is not found - use site admin
		List<User> users = UserLocalServiceUtil.getRoleUsers(adminRole.getRoleId());
		
		if (users.size() > 0) {
			return users.get(0);
		} else {
			return null;
		}
	}
	
}
