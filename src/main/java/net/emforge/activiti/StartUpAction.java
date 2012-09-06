package net.emforge.activiti;

import static net.emforge.activiti.constants.RoleConstants.APPROVER_ROLE_DESCRIPTION;
import static net.emforge.activiti.constants.RoleConstants.SITE_CONTENT_REVIEWER;
import static net.emforge.activiti.constants.RoleConstants.ORGANIZATION_CONTENT_REVIEWER;
import static net.emforge.activiti.constants.RoleConstants.PORTAL_CONTENT_REVIEWER;
import static net.emforge.activiti.constants.RoleConstants.PURCHASING_CONTENT_REVIEWER;
import static net.emforge.activiti.constants.RoleConstants.SALES_CONTENT_REVIEWER;
import static net.emforge.activiti.constants.TagConstants.TAG_PURCHASING_CONTENT;
import static net.emforge.activiti.constants.TagConstants.TAG_SALES_CONTENT;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.liferay.portal.kernel.events.ActionException;
import com.liferay.portal.kernel.events.SimpleAction;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
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
import net.emforge.activiti.spring.ApplicationContextProvider;
import net.emforge.activiti.spring.Initializable;
import net.emforge.activiti.util.SpringUtils;

/** 
 * Startup action called after deployment of Activiti Workflow plugin. Initial settings get configured in Liferay. 
 * 
 * @author akakunin
 * @author Oliver Teichmann, PRODYNA AG
 *
 */
public class StartUpAction extends SimpleAction implements ApplicationContextAware 
{
	ApplicationContext applicationContext;
	
	private static Log log = LogFactoryUtil.getLog(StartUpAction.class);
	
	private static final String PROCDEF_SINGLE_APPROVER_BY_SCRIPT = "SingleApproverByScript";
	private static final String PROCDEF_TAG_BASED_CONTENT_APPROVER = "TagBasedContentApproval";
	
	@Override
	public void run(String[] ids) throws ActionException {
		log.info("Activiti StartUp Action");
		
		applicationContext = ApplicationContextProvider.getApplicationContext();		
		
		try {
			// init and wire lazy beans
			
			initBean("transactionManager");
			initBean("dataSource");
			initBean("sharedTransactionTemplate");
			initBean("sessionFactory");
			initBean("com.liferay.portal.kernel.workflow.comparator.WorkflowComparatorFactoryUtil");
			initBean("processEngineConfiguration");
			initBean("processEngine");
			initBean("repositoryService");
			initBean("runtimeService");
			initBean("taskService");
			initBean("historyService");
			initBean("managementService");
			initBean("identityService");
			initBean("org.springframework.beans.factory.config.PropertyPlaceholderConfigurer");
			
			initBean("workflowDefinitionManager");
			initBean("idMappingService");
			initBean("workflowInstanceManager", WorkflowInstanceManagerImpl.class);
			initBean("workflowEngineManager");
			initBean("workflowTaskManager");
			initBean("workflowLogManager");
			initBean("liferayService");
			initBean("liferayIdentityService");
			initBean("liferayGroupManagerSessionFactory");
			initBean("liferayGroupManagerSession");

			initBean("messageListener.workflow_definition");
			initBean("messageListener.workflow_engine");
			initBean("messageListener.workflow_instance");
			initBean("messageListener.workflow_log");
			initBean("messageListener.workflow_task");
			initBean("messagingConfigurator");
			
			
			for (String companyId : ids) {
				doRun(GetterUtil.getLong(companyId));
			}
		} catch (Exception e) {
			log.error("Initialization failed", e);
			throw new ActionException(e);
		}

	}
	
	private void initBean(String beanName) throws Exception {
		Object aobj = applicationContext.getBean(beanName);
		if (aobj instanceof Initializable)
			((Initializable) aobj).init();
	}	
	
	private void initBean(String beanName, Class cls) throws Exception {
		Object aobj = applicationContext.getBean(beanName);
		Object aobj1 = SpringUtils.getTargetObject(aobj, cls);
		if (aobj instanceof Initializable)
			((Initializable) aobj).init();
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
		
		Map<Locale, String> descriptionMap = new HashMap<Locale, String>();
		descriptionMap.put(LocaleUtil.getDefault(), APPROVER_ROLE_DESCRIPTION);
		
		if (role == null) {
			log.info("Create role: " + PORTAL_CONTENT_REVIEWER);
			RoleLocalServiceUtil.addRole(adminUser.getUserId(), companyId, PORTAL_CONTENT_REVIEWER, null, descriptionMap, RoleConstants.TYPE_REGULAR);
		}
		
		// Organization Content Reviewer
		role = null;
		try {
			role = RoleLocalServiceUtil.getRole(companyId, ORGANIZATION_CONTENT_REVIEWER);
		} catch (Exception ex) {}
		
		if (role == null) {
			log.info("Create role: " + ORGANIZATION_CONTENT_REVIEWER);
			RoleLocalServiceUtil.addRole(adminUser.getUserId(), companyId, ORGANIZATION_CONTENT_REVIEWER, null, descriptionMap, RoleConstants.TYPE_ORGANIZATION);
		}
		
		// Site Content Reviewer
		role = null;
		try {
			role = RoleLocalServiceUtil.getRole(companyId, SITE_CONTENT_REVIEWER);
		} catch (Exception ex) {}
		
		if (role == null) {
			log.info("Create role: " + SITE_CONTENT_REVIEWER);
			RoleLocalServiceUtil.addRole(adminUser.getUserId(), companyId, SITE_CONTENT_REVIEWER, null, descriptionMap, RoleConstants.TYPE_SITE);
		}
		
		try {
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
		} catch (Exception ex) {
			log.warn("Cannot deploy default workflows: " + ex.getMessage());
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

	@Override
	public void setApplicationContext(ApplicationContext ctx)
			throws BeansException {
		applicationContext = ctx;
		
	}
	
}
