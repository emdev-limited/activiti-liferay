package net.emforge.activiti;

import java.util.Map;

import net.emforge.activiti.identity.LiferayGroupManagerSessionFactory;
import net.emforge.activiti.identity.LiferayUserManagerSessionFactory;
import net.emforge.activiti.spring.ApplicationContextWrapper;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.interceptor.SessionFactory;
import org.activiti.engine.impl.persistence.entity.GroupEntityManager;
import org.activiti.engine.impl.persistence.entity.GroupIdentityManager;
import org.activiti.engine.impl.persistence.entity.UserEntityManager;
import org.activiti.engine.impl.persistence.entity.UserIdentityManager;
import org.activiti.spring.ProcessEngineFactoryBean;
import org.activiti.spring.SpringExpressionManager;
import org.codehaus.groovy.jsr223.GroovyScriptEngineFactory;
import org.springframework.beans.factory.annotation.Autowired;

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
	
	@Autowired
	LiferayGroupManagerSessionFactory liferayGroupManagerSessionFactory;
	
	@Override
	public ProcessEngine getObject() throws Exception {
		// set history level
		processEngineConfiguration.setHistoryLevel(HistoryLevel.FULL);
		
		ProcessEngine processEngine = super.getObject();
		
		// preconfigure process engine to use our identity session
		Map<Class<?>, SessionFactory> sessionFactories = processEngineConfiguration.getSessionFactories();
		sessionFactories.put(GroupIdentityManager.class, liferayGroupManagerSessionFactory);
		sessionFactories.put(UserIdentityManager.class, new LiferayUserManagerSessionFactory());
		
		// Add Liferay Script Engine Factory
		processEngineConfiguration.getScriptingEngines().addScriptEngineFactory(new LiferayScriptEngineFactory("LiferayJavaScript", "javascript"));
		processEngineConfiguration.getScriptingEngines().addScriptEngineFactory(new LiferayScriptEngineFactory("LiferayGroovy", "groovy"));
		
		// Add Groovy Script Engine Factory
		processEngineConfiguration.getScriptingEngines().addScriptEngineFactory(new GroovyScriptEngineFactory());
		
		return processEngine;
	}
	
	@Override
	protected void configureExpressionManager() {
		if (applicationContext != null) {
			processEngineConfiguration
					.setExpressionManager(new SpringExpressionManager(
							new ApplicationContextWrapper(applicationContext), processEngineConfiguration
									.getBeans()));
		}
		//super.configureExpressionManager();
	}
}
