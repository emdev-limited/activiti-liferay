package net.emforge.activiti;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.transaction.TransactionManager;

import net.emforge.activiti.engine.impl.LiferayTaskServiceImpl;
import net.emforge.activiti.hook.LiferayBpmnParser;

import org.activiti.engine.impl.bpmn.deployer.BpmnDeployer;
import org.activiti.engine.impl.bpmn.parser.BpmnParser;
import org.activiti.engine.impl.cfg.JtaProcessEngineConfiguration;
import org.activiti.engine.impl.interceptor.CommandContextInterceptor;
import org.activiti.engine.impl.interceptor.CommandInterceptor;
import org.activiti.engine.impl.interceptor.JtaTransactionInterceptor;
import org.activiti.engine.impl.interceptor.LogInterceptor;
import org.activiti.engine.impl.persistence.deploy.Deployer;
import org.activiti.engine.impl.util.ReflectUtil;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

/**
 * Custom {@link JtaProcessEngineConfiguration} implementation to load own classloader and database configurations.
 *
 * @author akakunin
 * @author Oliver Teichmann, PRODYNA AG
 *
 */
public class LiferayJTAProcessEngineConfiguration extends LiferayProcessEngineConfiguration {
	private static Log _log = LogFactoryUtil.getLog(LiferayJTAProcessEngineConfiguration.class);

	TransactionManager tm;

	public static final String DEFAULT_MYBATIS_MAPPING_FILE = "activiti-liferay.ibatis.mem.conf.xml";
	
	public LiferayJTAProcessEngineConfiguration() {
		super();
	}
	
	protected InputStream getMyBatisXmlConfigurationSteam() {
		return ReflectUtil.getResourceAsStream(DEFAULT_MYBATIS_MAPPING_FILE);
	}
	
	@Override
	protected Collection<? extends CommandInterceptor> getDefaultCommandInterceptorsTxRequired() {
		List<CommandInterceptor> defaultCommandInterceptorsTxRequired = new ArrayList<CommandInterceptor>();
		defaultCommandInterceptorsTxRequired.add(new LogInterceptor());
		defaultCommandInterceptorsTxRequired.add(new JtaTransactionInterceptor(tm, false));
		defaultCommandInterceptorsTxRequired.add(new CommandContextInterceptor(commandContextFactory, this));
		return defaultCommandInterceptorsTxRequired;
	}

	@Override
	protected Collection<? extends CommandInterceptor> getDefaultCommandInterceptorsTxRequiresNew() {
		List<CommandInterceptor> defaultCommandInterceptorsTxRequiresNew = new ArrayList<CommandInterceptor>();
		defaultCommandInterceptorsTxRequiresNew.add(new LogInterceptor());
		defaultCommandInterceptorsTxRequiresNew.add(new JtaTransactionInterceptor(tm, true));
		defaultCommandInterceptorsTxRequiresNew.add(new CommandContextInterceptor(commandContextFactory, this));
		return defaultCommandInterceptorsTxRequiresNew;
	}
	
	public TransactionManager getJtaTransactionManager() {
		return tm;
	}
	
	public void setJtaTransactionManager(TransactionManager tm) {
		this.tm = tm;
	}

}
