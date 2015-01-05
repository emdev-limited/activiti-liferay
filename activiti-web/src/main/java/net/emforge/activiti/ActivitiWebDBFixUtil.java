package net.emforge.activiti;

import java.sql.SQLException;
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.cmd.GetDeploymentCmd;
import org.activiti.engine.impl.db.DbSqlSessionFactory;
import org.activiti.engine.impl.db.HasRevision;
import org.activiti.engine.impl.db.PersistentObject;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.persistence.entity.DeploymentEntity;
import org.activiti.engine.impl.persistence.entity.PropertyEntity;
import org.activiti.engine.repository.ProcessDefinition;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSession;

import com.liferay.portal.kernel.dao.db.DB;
import com.liferay.portal.kernel.dao.db.DBFactoryUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.util.PortalUtil;

public class ActivitiWebDBFixUtil {
	private static DbSqlSessionFactory dbSqlSessionFactory;
	private static SqlSession sqlSession;
	
	private static Log log = LogFactoryUtil.getLog(ActivitiWebDBFixUtil.class);

	public static void fix() {
		try {
			ProcessEngine pe = ProcessEngines.getDefaultProcessEngine();
		    ProcessEngineImpl peImpl = (ProcessEngineImpl)pe;
			dbSqlSessionFactory = peImpl.getProcessEngineConfiguration().getDbSqlSessionFactory();
		    sqlSession = dbSqlSessionFactory
		    	      .getSqlSessionFactory()
		    	      .openSession();
		    
			if (!isDBFixed("liferay.fixed")) {
				log.info("Tables need to be fixed");
				
				if (DBFactoryUtil.getDB().getType().equals(DB.TYPE_MYSQL))
					DBFactoryUtil.getDB().runSQL("SET SQL_SAFE_UPDATES=0;");
				removeProcessInstanceExtTable();
				removeProcessDefinitionExtTable();
				fixTitleAbsence();
				
				PropertyEntity dbFixedProperty = new PropertyEntity("liferay.fixed", "true");
		        insert(dbFixedProperty);
		        
		        log.info("Fixing complete");
			}
		} catch (ActivitiException e){
			log.error(e.getMessage(), e);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * Reads deployment category (as companyId) and adds as tenantId.
	 * <p>When it is fixed then adds property into DB.
	 */
	public static void tenantFix() {		
		if (!isDBFixed("liferay.company.tenant.fixed")) {
			log.info("Add tenant as companyId to process definitions");
			ProcessEngine pe = ProcessEngines.getDefaultProcessEngine();
			ProcessEngineConfigurationImpl processEngineConfiguration = (ProcessEngineConfigurationImpl) pe
					.getProcessEngineConfiguration();
			CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutor();
			RepositoryService repositoryService = pe.getRepositoryService();

			List<ProcessDefinition> processDefinitions = repositoryService
					.createProcessDefinitionQuery()
					.processDefinitionWithoutTenantId()
					.list();

			String defaultCompanyId = String.valueOf(PortalUtil.getDefaultCompanyId());

			for (ProcessDefinition processDefinition : processDefinitions) {
				DeploymentEntity deploymentEntity = commandExecutor.execute(new GetDeploymentCmd(processDefinition.getDeploymentId()));

				// Get deployment category as companyId (old strategy)
				String companyId = deploymentEntity.getCategory();
				if (StringUtils.isEmpty(companyId)) {
					// use default company
					log.info("Add default companyId to tenant for process " + processDefinition.getId());
					companyId = defaultCompanyId;
				}
				try {
					repositoryService.changeDeploymentTenantId(processDefinition.getDeploymentId(), companyId);
				} catch (Exception e) {
					log.info("Unable to change tenantId " + companyId + " for process " + processDefinition.getId() + ": " + e.getMessage());
				}
			}

			PropertyEntity dbFixedProperty = new PropertyEntity("liferay.company.tenant.fixed", "true");
			insert(dbFixedProperty);

			log.info("Tenants added");
		}
	}
	
	private static boolean isDBFixed(String propertyName) {
		PropertyEntity dbFixedProperty = selectById(PropertyEntity.class, propertyName);
		boolean dbFixed = false;
		if (dbFixedProperty != null) {
			dbFixed = GetterUtil.getBoolean(dbFixedProperty.getValue());
		}
		
		log.debug("dbFixed: " + dbFixed);
	    return dbFixed;
	}
	
	@SuppressWarnings("unchecked")
	  public static <T extends PersistentObject> T selectById(Class<T> entityClass, String id) {
	    /*T persistentObject = cacheGet(entityClass, id);
	    if (persistentObject!=null) {
	      return persistentObject;
	    }*/
	    String selectStatement = dbSqlSessionFactory.getSelectStatement(entityClass);
	    selectStatement = dbSqlSessionFactory.mapStatement(selectStatement);
	    T persistentObject = (T) sqlSession.selectOne(selectStatement, id);
	    if (persistentObject==null) {
	      return null;
	    }
	    /*cachePut(persistentObject, true);*/
	    return persistentObject;
	  }
	
	public static void insert(PersistentObject persistentObject) {
	  if (persistentObject.getId()==null) {
	    String id = dbSqlSessionFactory.getIdGenerator().getNextId();  
	    persistentObject.setId(id);
	  }

      String insertStatement = dbSqlSessionFactory.getInsertStatement(persistentObject);
	  insertStatement = dbSqlSessionFactory.mapStatement(insertStatement);
	
	  if (insertStatement==null) {
	    throw new ActivitiException("no insert statement for "+persistentObject.getClass()+" in the ibatis mapping files");
	  }
	  
	  log.debug("inserting: " + persistentObject);
	  sqlSession.insert(insertStatement, persistentObject);
	  
	  // See http://jira.codehaus.org/browse/ACT-1290
	  if (persistentObject instanceof HasRevision) {
	    ((HasRevision) persistentObject).setRevision(((HasRevision) persistentObject).getRevisionNext());
	  }
	}
	
	private static void removeProcessInstanceExtTable() {
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
	
	private static void removeProcessDefinitionExtTable() {
		try {
			if (DBFactoryUtil.getDB().getType().equals(DB.TYPE_MYSQL)) {
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
			} else {
				log.warn("There aren't any update SQLs for other database types. Sorry.");
			}
		} catch (SQLException e) {
			log.debug("Seems that PDE table already removed", e);
		}  catch (Exception e) {
			log.error("Failed to remove PDE table", e);
		}
	}
	
	private static void fixTitleAbsence() {
		try {
			log.info("Fixing process definitions titles...");
			if (DBFactoryUtil.getDB().getType().equals(DB.TYPE_MYSQL)) {
				DBFactoryUtil.getDB().runSQL("SET @curByteArrayId:=(SELECT max(ID_*1) FROM ACT_GE_BYTEARRAY);");
				DBFactoryUtil.getDB().runSQL("INSERT INTO ACT_GE_BYTEARRAY (ID_, REV_, NAME_, DEPLOYMENT_ID_, BYTES_, GENERATED_) " +
						"SELECT (@curByteArrayId := @curByteArrayId + 1), 1, concat(PD.ID_,':title'), PD.DEPLOYMENT_ID_, PD.NAME_, 0 FROM ACT_RE_PROCDEF PD " +
						"WHERE PD.ID_ NOT IN (SELECT distinct(arp.ID_) " +
						"FROM ACT_RE_PROCDEF arp INNER JOIN ACT_GE_BYTEARRAY agb ON arp.DEPLOYMENT_ID_ = agb.DEPLOYMENT_ID_ " +
						"WHERE agb.NAME_ = concat(arp.ID_,':title'));");
			} else {
				log.warn("There aren't any update SQLs for other database types. Sorry.");
			}
		} catch (Exception e) {
			log.error("Failed to fix titles for process definitions", e);
		}
	}
}
