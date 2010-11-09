package net.emforge.activiti;

import java.sql.Connection;
import java.util.Map;

import javax.sql.DataSource;

import net.emforge.activiti.identity.LiferayIdentitySessionFactory;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.impl.cfg.IdentitySession;
import org.activiti.engine.impl.cfg.ProcessEngineConfiguration;
import org.activiti.engine.impl.interceptor.SessionFactory;
import org.activiti.spring.ProcessEngineFactoryBean;
import org.apache.commons.lang.StringUtils;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

/** Custom implementation of ProcessEngineFactoryBean to set own IdentitySession
 * and customize other things
 * 
 * @author akakunin
 *
 */
public class LiferayProcessEngineFactoryBean extends ProcessEngineFactoryBean {
	private static Log _log = LogFactoryUtil.getLog(LiferayProcessEngineFactoryBean.class);
	
	@Override
	public ProcessEngine getObject() throws Exception {
		// preconfigure process engine to use our identity session
		Map<Class< ? >, SessionFactory> sessionFactories = processEngineConfiguration.getSessionFactories();
		sessionFactories.put(IdentitySession.class, new LiferayIdentitySessionFactory());
		
		// set history level
		processEngineConfiguration.setHistoryLevel(ProcessEngineConfiguration.HISTORYLEVEL_FULL);
		
		// Add Liferay Script Engine Factory
		processEngineConfiguration.getScriptingEngines().addScriptEngineFactory(new LiferayScriptEngineFactory());
		
		// autodetect db type
		if (StringUtils.isEmpty(processEngineConfiguration.getDatabaseType()) ||
				processEngineConfiguration.getDatabaseType().equals("h2")) { // unfortunatelly Activiti always set it to h2 by default
			autodetectDbType();
		}
		
		return super.getObject();
	}

	private void autodetectDbType() {
		_log.info("database type is not specified - try to autodetect it");
		
		DataSource dataSource = processEngineConfiguration.getDataSource();
		
		try {
			Connection con = dataSource.getConnection();
			try {
				String dbName = con.getMetaData().getDatabaseProductName();
				String databaseType = null;
				
				_log.info("DBName: " + dbName);
				
				if ("MySQL".equalsIgnoreCase(dbName)) {
					databaseType = "mysql";
				} else if ("Oracle".equalsIgnoreCase(dbName)) {
					databaseType = "mysql";
				} else if ("HSQL Database Engine".equalsIgnoreCase(dbName)) { // need to clarify about names for hsql and h2 as well as same type may be used for them
					databaseType = "h2";
				} else if ("PostgreSQL".equalsIgnoreCase(dbName)) {
					databaseType = "postgres";
				} else {
					_log.warn("Unknown DB Type: " + dbName);
				}
				
				if (databaseType != null) {
					_log.info("Set Database Type to " + databaseType);
					processEngineConfiguration.setDatabaseType(databaseType);
				}
			} finally {
				con.close();
			}
		} catch (Exception ex) {
			_log.error("Cannot autodetect connection", ex);
		}
	}
}
