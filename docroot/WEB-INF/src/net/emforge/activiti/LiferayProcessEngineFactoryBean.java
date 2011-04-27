package net.emforge.activiti;

import java.sql.Connection;
import java.util.Map;

import javax.sql.DataSource;

import net.emforge.activiti.identity.LiferayIdentitySessionFactory;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.impl.cfg.IdentitySession;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.interceptor.SessionFactory;
import org.activiti.spring.ProcessEngineFactoryBean;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.mapping.MappedStatement;

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
		// set history level
		processEngineConfiguration.setHistoryLevel(ProcessEngineConfigurationImpl.HISTORYLEVEL_FULL);
		
		ProcessEngine processEngine = super.getObject();
		
		// preconfigure process engine to use our identity session
		Map<Class< ? >, SessionFactory> sessionFactories = processEngineConfiguration.getSessionFactories();
		sessionFactories.put(IdentitySession.class, new LiferayIdentitySessionFactory());
		
		// Add Liferay Script Engine Factory
		processEngineConfiguration.getScriptingEngines().addScriptEngineFactory(new LiferayScriptEngineFactory());
		// Add Groovy Script Engine Factory
		processEngineConfiguration.getScriptingEngines().addScriptEngineFactory(new GroovyScriptEngineFactory());
		
		return processEngine;
	}
}
