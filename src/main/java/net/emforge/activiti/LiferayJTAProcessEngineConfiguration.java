package net.emforge.activiti;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.transaction.TransactionManager;

import net.emforge.activiti.LiferayProcessEngineConfiguration;

import org.activiti.engine.impl.cfg.JtaProcessEngineConfiguration;
import org.activiti.engine.impl.interceptor.CommandContextInterceptor;
import org.activiti.engine.impl.interceptor.CommandInterceptor;
import org.activiti.engine.impl.interceptor.JtaTransactionInterceptor;
import org.activiti.engine.impl.interceptor.LogInterceptor;

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
