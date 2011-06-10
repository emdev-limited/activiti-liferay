package net.emforge.activiti;

import static net.emforge.activiti.constants.RoleConstants.APPROVER_ROLE_DESCRIPTION;
import static net.emforge.activiti.constants.RoleConstants.COMMUNITY_CONTENT_REVIEWER;
import static net.emforge.activiti.constants.RoleConstants.ORGANIZATION_CONTENT_REVIEWER;
import static net.emforge.activiti.constants.RoleConstants.PORTAL_CONTENT_REVIEWER;
import static net.emforge.activiti.constants.RoleConstants.PURCHASING_CONTENT_REVIEWER;
import static net.emforge.activiti.constants.RoleConstants.SALES_CONTENT_REVIEWER;
import static net.emforge.activiti.constants.TagConstants.TAG_PURCHASING_CONTENT;
import static net.emforge.activiti.constants.TagConstants.TAG_SALES_CONTENT;

import java.io.InputStream;
import java.util.List;

import com.liferay.portal.kernel.events.ActionException;
import com.liferay.portal.kernel.events.SimpleAction;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.workflow.WorkflowDefinitionManagerUtil;
import com.liferay.portal.model.Group;
import com.liferay.portal.model.GroupConstants;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.RoleConstants;
import com.liferay.portal.model.User;
import com.liferay.portal.service.GroupLocalServiceUtil;
import com.liferay.portal.service.RoleLocalServiceUtil;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portlet.asset.model.AssetTag;
import com.liferay.portlet.asset.service.AssetTagLocalServiceUtil;
import net.emforge.activiti.StartUpAction;

/** 
 * Startup action called after deployment of Activiti Workflow plugin. Initial settings get configured in Liferay. 
 * 
 * @author akakunin
 * @author Oliver Teichmann, PRODYNA AG
 *
 */
public class StartUpAction extends SimpleAction {
	
	private static Log log = LogFactoryUtil.getLog(StartUpAction.class);
	
	private static final String PROCDEF_SINGLE_APPROVER_BY_SCRIPT = "SingleApproverByScript";
	private static final String PROCDEF_TAG_BASED_CONTENT_APPROVER = "TagBasedContentApproval";
	
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
		log.info("Updating company " + companyId);
		User adminUser = getAdminUser(companyId);
		Group guestGroup = getGuestGroup(companyId);
		
		// create service context
		ServiceContext serviceContext = new ServiceContext();
		serviceContext.setCompanyId(companyId);
		serviceContext.setUserId(adminUser.getUserId());
		serviceContext.setAddGuestPermissions(true);
		
		// check and create required roles
		
		// Portal Content Reviewer
		Role role = null;
		try {
			role = RoleLocalServiceUtil.getRole(companyId, PORTAL_CONTENT_REVIEWER);
		} catch (Exception ex) {}
		
		if (role == null) {
			log.info("Create role: " + PORTAL_CONTENT_REVIEWER);
			RoleLocalServiceUtil.addRole(adminUser.getUserId(), companyId, PORTAL_CONTENT_REVIEWER, null, APPROVER_ROLE_DESCRIPTION, RoleConstants.TYPE_REGULAR);
		}
		
		// Organization Content Reviewer
		role = null;
		try {
			role = RoleLocalServiceUtil.getRole(companyId, ORGANIZATION_CONTENT_REVIEWER);
		} catch (Exception ex) {}
		
		if (role == null) {
			log.info("Create role: " + ORGANIZATION_CONTENT_REVIEWER);
			RoleLocalServiceUtil.addRole(adminUser.getUserId(), companyId, ORGANIZATION_CONTENT_REVIEWER, null, APPROVER_ROLE_DESCRIPTION, RoleConstants.TYPE_ORGANIZATION);
		}
		
		// Community Content Reviewer
		role = null;
		try {
			role = RoleLocalServiceUtil.getRole(companyId, COMMUNITY_CONTENT_REVIEWER);
		} catch (Exception ex) {}
		
		if (role == null) {
			log.info("Create role: " + COMMUNITY_CONTENT_REVIEWER);
			RoleLocalServiceUtil.addRole(adminUser.getUserId(), companyId, COMMUNITY_CONTENT_REVIEWER, null, APPROVER_ROLE_DESCRIPTION, RoleConstants.TYPE_COMMUNITY);
		}
		
		// Purchasing Content Reviewer
		role = null;
		try {
			role = RoleLocalServiceUtil.getRole(companyId, PURCHASING_CONTENT_REVIEWER);
		} catch (Exception ex) {}
		
		if (role == null) {
			log.info("Create role: " + PURCHASING_CONTENT_REVIEWER);
			RoleLocalServiceUtil.addRole(adminUser.getUserId(), companyId, PURCHASING_CONTENT_REVIEWER, null, APPROVER_ROLE_DESCRIPTION, RoleConstants.TYPE_COMMUNITY);
		}
		
		// Sales Content Reviewer
		role = null;
		try {
			role = RoleLocalServiceUtil.getRole(companyId, SALES_CONTENT_REVIEWER);
		} catch (Exception ex) {}
		
		if (role == null) {
			log.info("Create role: " + SALES_CONTENT_REVIEWER);
			RoleLocalServiceUtil.addRole(adminUser.getUserId(), companyId, SALES_CONTENT_REVIEWER, null, APPROVER_ROLE_DESCRIPTION, RoleConstants.TYPE_COMMUNITY);
		}
		
		AssetTag purchasingContentTag = null;
		try  {
			purchasingContentTag = AssetTagLocalServiceUtil.getTag(guestGroup.getGroupId(), TAG_PURCHASING_CONTENT);
		} catch (Exception e) {}
		
		if (purchasingContentTag == null) {
			purchasingContentTag = AssetTagLocalServiceUtil.addTag(adminUser.getUserId(), TAG_PURCHASING_CONTENT, new String [] {}, serviceContext);
			purchasingContentTag.setGroupId(guestGroup.getGroupId());
			AssetTagLocalServiceUtil.updateAssetTag(purchasingContentTag);
		}
		
		AssetTag salesContentTag = null;
		try  {
			salesContentTag = AssetTagLocalServiceUtil.getTag(guestGroup.getGroupId(), TAG_SALES_CONTENT);
		} catch (Exception e) {}
		
		if (salesContentTag == null) {
			salesContentTag = AssetTagLocalServiceUtil.addTag(adminUser.getUserId(), TAG_SALES_CONTENT, new String [] {}, serviceContext);
			salesContentTag.setGroupId(guestGroup.getGroupId());
			AssetTagLocalServiceUtil.updateAssetTag(salesContentTag);
		}
		
		// Deploy workflow definitions
		int workflowDefinitionCount = WorkflowDefinitionManagerUtil.getWorkflowDefinitionCount(companyId, PROCDEF_SINGLE_APPROVER_BY_SCRIPT);
		if(workflowDefinitionCount < 1) { 
			InputStream resourceAsStream = StartUpAction.class.getClassLoader().getResourceAsStream("/META-INF/resources/" + PROCDEF_SINGLE_APPROVER_BY_SCRIPT + ".bar");
			if(resourceAsStream != null) {
				WorkflowDefinitionManagerUtil.deployWorkflowDefinition(companyId, adminUser.getUserId(), PROCDEF_SINGLE_APPROVER_BY_SCRIPT, resourceAsStream);
			}
		}
		
		workflowDefinitionCount = WorkflowDefinitionManagerUtil.getWorkflowDefinitionCount(companyId, PROCDEF_TAG_BASED_CONTENT_APPROVER);
		if(workflowDefinitionCount < 1) { 
			InputStream resourceAsStream = StartUpAction.class.getClassLoader().getResourceAsStream("/META-INF/resources/" + PROCDEF_TAG_BASED_CONTENT_APPROVER + ".bar");
			if(resourceAsStream != null) {
				WorkflowDefinitionManagerUtil.deployWorkflowDefinition(companyId, adminUser.getUserId(), PROCDEF_TAG_BASED_CONTENT_APPROVER, resourceAsStream);
			}
		}
	}
	
	protected Group getGuestGroup(long companyId) throws Exception {
		return GroupLocalServiceUtil.getGroup(companyId, GroupConstants.GUEST);
	}

	protected User getAdminUser(long companyId) throws Exception {
		Role adminRole = RoleLocalServiceUtil.getRole(companyId,
				RoleConstants.ADMINISTRATOR);
		// org admin is not found - use site admin
		List<User> users = UserLocalServiceUtil.getRoleUsers(adminRole
				.getRoleId());

		if (users.size() > 0) {
			return users.get(0);
		} else {
			return null;
		}
	}
	
}
