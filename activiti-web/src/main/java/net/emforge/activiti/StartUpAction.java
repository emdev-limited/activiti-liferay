package net.emforge.activiti;

import java.io.InputStream;
import java.util.List;

import javax.servlet.ServletContext;

import net.emforge.activiti.spring.ContextLoaderListener;

import org.apache.commons.io.IOUtils;
import org.springframework.web.context.ContextLoader;

import com.liferay.portal.kernel.events.ActionException;
import com.liferay.portal.kernel.events.SimpleAction;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.workflow.WorkflowDefinitionManagerUtil;
import com.liferay.portal.kernel.workflow.WorkflowEngineManagerUtil;
import com.liferay.portal.model.Group;
import com.liferay.portal.model.GroupConstants;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.RoleConstants;
import com.liferay.portal.model.User;
import com.liferay.portal.service.GroupLocalServiceUtil;
import com.liferay.portal.service.RoleLocalServiceUtil;
import com.liferay.portal.service.UserLocalServiceUtil;

/** 
 * Startup action called after deployment of Activiti Workflow plugin. Initial settings get configured in Liferay. 
 * 
 * @author akakunin
 * @author Oliver Teichmann, PRODYNA AG
 * @author Dmitry Farafonov
 */
public class StartUpAction extends SimpleAction {
	private static Log log = LogFactoryUtil.getLog(StartUpAction.class);
	
	private static final String PROCDEF_SINGLE_APPROVER_BY_SCRIPT = "SingleApproverByScript";
	private static final String PROCDEF_TAG_BASED_CONTENT_APPROVER = "TagBasedContentApproval";
	
	private boolean independentFixesRun = false;
	
	@Override
	public void run(String[] ids) throws ActionException 
	{
		log.info("Activiti StartUp Action");
		initContext();		
		
		try {
			try {
				if (!independentFixesRun) {
					// we do not need it to be run against each portal instance..
					ActivitiWebDBFixUtil.fix();
					ActivitiWebDBFixUtil.tenantFix();
				}
			} catch (Exception e) {
				log.warn("Cannot upgrade table to newer version of activiti-web. Please ignore on first deploy: " + e.getMessage());
			}
			
		} catch (Exception e) {
			log.error("Initialization failed", e);
			throw new ActionException(e);
		}
		log.info("Activiti engine " + WorkflowEngineManagerUtil.getVersion() + " installed");
	}
	
	/**
	 * Plugin starts in different ways:
	 * 1. We deploying plugin to running JBoss instance. In this case StartupAction runs at first, and then web application context starts.
	 * 2. We starts JBoss with deployed plugin. In this case web application context runs at first, and then StartupAction runs.   
	 * 
	 * However, in both cases spring context must be initialized from StartupAction 
	 */
	private void initContext() {
		ServletContext sctx = null;
		ContextLoader ctxLoader = new ContextLoader();
		
		sctx = ContextLoaderListener.getInitServletContext();
		
		if (sctx == null) {
			ContextLoaderListener.setIsInitialized(false);
		} else if (!ContextLoaderListener.isInitialized()) {
			ContextLoaderListener.setIsInitialized(true);
			ctxLoader.initWebApplicationContext(sctx);
		}		
	}

	private void doRun(long companyId) throws Exception {
		log.info("Updating company " + companyId);
		User adminUser = getAdminUser(companyId);
		
		try {
			// Deploy workflow definitions
			int workflowDefinitionCount = WorkflowDefinitionManagerUtil.getWorkflowDefinitionCount(companyId, PROCDEF_SINGLE_APPROVER_BY_SCRIPT);
			if(workflowDefinitionCount < 1) { 
				InputStream resourceAsStream = StartUpAction.class.getClassLoader().getResourceAsStream("/META-INF/resources/" + PROCDEF_SINGLE_APPROVER_BY_SCRIPT + ".bar");
				if(resourceAsStream != null) {
					byte bytes[] = IOUtils.toByteArray(resourceAsStream);
					WorkflowDefinitionManagerUtil.deployWorkflowDefinition(companyId, adminUser.getUserId(), PROCDEF_SINGLE_APPROVER_BY_SCRIPT, bytes);
				}
			}
			
//			workflowDefinitionCount = WorkflowDefinitionManagerUtil.getWorkflowDefinitionCount(companyId, PROCDEF_TAG_BASED_CONTENT_APPROVER);
//			if(workflowDefinitionCount < 1) { 
//				InputStream resourceAsStream = StartUpAction.class.getClassLoader().getResourceAsStream("/META-INF/resources/" + PROCDEF_TAG_BASED_CONTENT_APPROVER + ".bar");
//				if(resourceAsStream != null) {
//					WorkflowDefinitionManagerUtil.deployWorkflowDefinition(companyId, adminUser.getUserId(), PROCDEF_TAG_BASED_CONTENT_APPROVER, resourceAsStream);
//				}
//			}
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
}
