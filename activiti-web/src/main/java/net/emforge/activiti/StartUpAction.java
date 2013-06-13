package net.emforge.activiti;

import static net.emforge.activiti.constants.RoleConstants.APPROVER_ROLE_DESCRIPTION;
import static net.emforge.activiti.constants.RoleConstants.ORGANIZATION_CONTENT_REVIEWER;
import static net.emforge.activiti.constants.RoleConstants.PORTAL_CONTENT_REVIEWER;
import static net.emforge.activiti.constants.RoleConstants.SITE_CONTENT_REVIEWER;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletContext;

import net.emforge.activiti.spring.ContextLoaderListener;

import org.springframework.web.context.ContextLoader;

import com.liferay.portal.dao.db.MySQLDB;
import com.liferay.portal.kernel.dao.db.DBFactoryUtil;
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
	
	private boolean independentFixesRun = false;
	
	private void removeProcessInstanceExtTable() {
		try {
			//Create tmp for PIE
			DBFactoryUtil.getDB().runSQL("CREATE TABLE ACT_PIE_TMP LIKE ACT_PROCESSINSTANCEEXTENSION_LIFERAY");
			DBFactoryUtil.getDB().runSQL("INSERT INTO ACT_PIE_TMP SELECT * FROM ACT_PROCESSINSTANCEEXTENSION_LIFERAY;");
			//Create tmp for WorkflowInstanceLink
			DBFactoryUtil.getDB().runSQL("CREATE TABLE WorkflowInstanceLink_TMP LIKE WorkflowInstanceLink");
			DBFactoryUtil.getDB().runSQL("INSERT INTO WorkflowInstanceLink_TMP SELECT * FROM WorkflowInstanceLink;");
			//update
			DBFactoryUtil.getDB().runSQL("UPDATE WorkflowInstanceLink L INNER JOIN ACT_PROCESSINSTANCEEXTENSION_LIFERAY PIE " +
					"ON PIE.process_instance_extension_id = L.workflowInstanceId SET L.workflowInstanceId = PIE.process_instance_id;");
			//remove PIE
			DBFactoryUtil.getDB().runSQL("DROP TABLE ACT_PROCESSINSTANCEEXTENSION_LIFERAY");
		} catch (SQLException e) {
			log.debug("Seems that PIE table already removed", e);
		}  catch (Exception e) {
			log.error("Failed to remove PIE table", e);
		}
	}
	
	private void removeProcessDefinitionExtTable() {
		try {
			//Create tmp for PDE
			DBFactoryUtil.getDB().runSQL("CREATE TABLE ACT_PDE_TMP LIKE ACT_PROCESSDEFINITIONEXTENSION_LIFERAY");
			DBFactoryUtil.getDB().runSQL("INSERT INTO ACT_PDE_TMP SELECT * FROM ACT_PROCESSDEFINITIONEXTENSION_LIFERAY;");

			//update
            DBFactoryUtil.getDB().runSQL("UPDATE ACT_RE_DEPLOYMENT D INNER JOIN ACT_RE_PROCDEF PDEF on D.ID_ = PDEF.DEPLOYMENT_ID_ " +
                    "SET D.CATEGORY_ = (SELECT distinct(EXT.company_id) FROM ACT_PROCESSDEFINITIONEXTENSION_LIFERAY EXT " +
                    "INNER JOIN ACT_RE_PROCDEF PD on EXT.process_definition_id = PD.ID_ where PD.DEPLOYMENT_ID_ = D.ID_)");
			
			DBFactoryUtil.getDB().runSQL("SET @curByteArrayId:=(SELECT max(ID_*1) FROM ACT_GE_BYTEARRAY);");
			DBFactoryUtil.getDB().runSQL("INSERT INTO ACT_GE_BYTEARRAY (ID_, REV_, NAME_, DEPLOYMENT_ID_, BYTES_, GENERATED_) " +
					"SELECT (@curByteArrayId := @curByteArrayId + 1), 1, concat(PD.ID_, ':', 'title'), PD.DEPLOYMENT_ID_, EXT.title, 0 FROM ACT_PROCESSDEFINITIONEXTENSION_LIFERAY EXT INNER JOIN ACT_RE_PROCDEF PD on EXT.process_definition_id = PD.ID_;");
			//remove PDE
			DBFactoryUtil.getDB().runSQL("DROP TABLE ACT_PROCESSDEFINITIONEXTENSION_LIFERAY");
		} catch (SQLException e) {
			log.debug("Seems that PDE table already removed", e);
		}  catch (Exception e) {
			log.error("Failed to remove PDE table", e);
		}
	}
	
	private void fixTitleAbsence() {
		try {
			log.info("Fixing process definitions titles...");
			DBFactoryUtil.getDB().runSQL("SET @curByteArrayId:=(SELECT max(ID_*1) FROM ACT_GE_BYTEARRAY);");
			DBFactoryUtil.getDB().runSQL("INSERT INTO ACT_GE_BYTEARRAY (ID_, REV_, NAME_, DEPLOYMENT_ID_, BYTES_, GENERATED_) " +
					"SELECT (@curByteArrayId := @curByteArrayId + 1), 1, concat(PD.ID_,':title'), PD.DEPLOYMENT_ID_, PD.NAME_, 0 FROM ACT_RE_PROCDEF PD " +
					"WHERE PD.ID_ NOT IN (SELECT distinct(arp.ID_) " +
					"FROM ACT_RE_PROCDEF arp INNER JOIN ACT_GE_BYTEARRAY agb ON arp.DEPLOYMENT_ID_ = agb.DEPLOYMENT_ID_ " +
					"WHERE agb.NAME_ = concat(arp.ID_,':title'));");
		} catch (Exception e) {
			log.error("Failed to fix titles for process definitions", e);
		}
	}
	
	@Override
	public void run(String[] ids) throws ActionException 
	{
		log.info("Activiti StartUp Action");
		initContext();		
		
		try {
			try {
				if (!independentFixesRun) {
					//we do not need it to be run against each portal instance..
					if (DBFactoryUtil.getDB().getClass().getName().equals("com.liferay.portal.dao.db.MySQLDB")) {
						DBFactoryUtil.getDB().runSQL("SET SQL_SAFE_UPDATES=0;");
						removeProcessInstanceExtTable();
						removeProcessDefinitionExtTable();
						fixTitleAbsence();
						// DBFactoryUtil.getDB().runSQL("SET SQL_SAFE_UPDATES=1;");
						independentFixesRun = true;
					}
				}
			} catch (Exception e) {
				log.warn("Cannot upgrade table to newer version of activiti-web. Please ignore on first deploy: " + e.getMessage());
			}
			
//			for (String companyId : ids) {
//				doRun(GetterUtil.getLong(companyId));
//			}
		} catch (Exception e) {
			log.error("Initialization failed", e);
			throw new ActionException(e);
		}

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
					WorkflowDefinitionManagerUtil.deployWorkflowDefinition(companyId, adminUser.getUserId(), PROCDEF_SINGLE_APPROVER_BY_SCRIPT, resourceAsStream);
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
