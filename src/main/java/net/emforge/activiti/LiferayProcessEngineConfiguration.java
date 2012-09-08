package net.emforge.activiti;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.emforge.activiti.engine.impl.LiferayTaskServiceImpl;
import net.emforge.activiti.hook.LiferayBpmnParser;

import org.activiti.engine.impl.bpmn.deployer.BpmnDeployer;
import org.activiti.engine.impl.bpmn.parser.BpmnParser;
import org.activiti.engine.impl.persistence.deploy.Deployer;
import org.activiti.engine.impl.util.ReflectUtil;
import org.activiti.spring.SpringProcessEngineConfiguration;

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
	public static final String DEFAULT_MYBATIS_MAPPING_FILE = "activiti-liferay.ibatis.mem.conf.xml";
     
	
	public LiferayProcessEngineConfiguration() {
		//replace taskService with own implementation 
		taskService = new LiferayTaskServiceImpl();
		classLoader = new DatabaseClassloader(this.getClass().getClassLoader());
	}
	
	@Override
	protected InputStream getMyBatisXmlConfigurationSteam() {
		return ReflectUtil.getResourceAsStream(DEFAULT_MYBATIS_MAPPING_FILE);
	}
	
	@Override
	public void initDatabaseType() {
		// add mapping for HSQL Database - map it into H2 (should work)
		databaseTypeMappings.setProperty("HSQL Database Engine","h2");
		super.initDatabaseType();
	}
	
	@Override
	protected Collection<? extends Deployer> getDefaultDeployers() {
		List<Deployer> defaultDeployers = new ArrayList<Deployer>();

		BpmnDeployer bpmnDeployer = new BpmnDeployer();
		bpmnDeployer.setExpressionManager(expressionManager);
		bpmnDeployer.setIdGenerator(idGenerator);
		BpmnParser bpmnParser = new LiferayBpmnParser(expressionManager);
		_log.debug("Used own Liferay BPMN Parser");
		
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
