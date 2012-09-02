package net.emforge.activiti;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.emforge.activiti.engine.impl.LiferayTaskServiceImpl;
import net.emforge.activiti.hook.LiferayBpmnParser;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.bpmn.deployer.BpmnDeployer;
import org.activiti.engine.impl.bpmn.parser.BpmnParser;
import org.activiti.engine.impl.db.IbatisVariableTypeHandler;
import org.activiti.engine.impl.persistence.deploy.Deployer;
import org.activiti.engine.impl.util.IoUtil;
import org.activiti.engine.impl.util.ReflectUtil;
import org.activiti.engine.impl.variable.VariableType;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;
import org.apache.ibatis.type.JdbcType;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

/**
 * Custom {@link SpringProcessEngineConfiguration} implementation to load own classloader and database configurations.
 *
 * @author akakunin
 * @author Oliver Teichmann, PRODYNA AG
 *
 */
public class LiferayProcessEngineConfiguration extends SpringProcessEngineConfiguration {
	private static Log _log = LogFactoryUtil.getLog(LiferayProcessEngineConfiguration.class);
	
	public LiferayProcessEngineConfiguration() {
		//replace taskService with own implementation 
		taskService = new LiferayTaskServiceImpl();
		classLoader = new DatabaseClassloader(this.getClass().getClassLoader());
	}
	
	@Override
	public void initDatabaseType() {
		// add mapping for HSQL Database - map it into H2 (should work)
		databaseTypeMappings.setProperty("HSQL Database Engine","h2");
		super.initDatabaseType();
	}
	
	@Override
	protected void initSqlSessionFactory() {
		if (sqlSessionFactory == null) {
			InputStream inputStream = null;
			try {
				// We load own configuration file
				inputStream = ReflectUtil
						.getResourceAsStream("activiti-liferay.ibatis.mem.conf.xml");
				_log.info("Loaded custom ibatis configuration");
				
				// update the jdbc parameters to the configured ones...
				Environment environment = new Environment("default",
						transactionFactory, dataSource);
				XMLConfigBuilder parser = new XMLConfigBuilder(inputStream);
				Configuration configuration = parser.getConfiguration();
				configuration.setEnvironment(environment);
				configuration.getTypeHandlerRegistry().register(
						VariableType.class, JdbcType.VARCHAR,
						new IbatisVariableTypeHandler());
				configuration = parser.parse();

				sqlSessionFactory = new DefaultSqlSessionFactory(configuration);

			} catch (Exception e) {
				throw new ActivitiException(
						"Error while building ibatis SqlSessionFactory: "
								+ e.getMessage(), e);
			} finally {
				IoUtil.closeSilently(inputStream);
			}
		}
	}
	
	@Override
	protected Collection<? extends Deployer> getDefaultDeployers() {
		List<Deployer> defaultDeployers = new ArrayList<Deployer>();

		BpmnDeployer bpmnDeployer = new BpmnDeployer();
		bpmnDeployer.setExpressionManager(expressionManager);
		bpmnDeployer.setIdGenerator(idGenerator);
		BpmnParser bpmnParser = new LiferayBpmnParser(expressionManager);

		if (preParseListeners != null) {
			bpmnParser.getParseListeners().addAll(preParseListeners);
		}
		bpmnParser.getParseListeners().addAll(getDefaultBPMNParseListeners());
		if (postParseListeners != null) {
			bpmnParser.getParseListeners().addAll(postParseListeners);
		}

		bpmnDeployer.setBpmnParser(bpmnParser);

		defaultDeployers.add(bpmnDeployer);
		return defaultDeployers;
	}

}
