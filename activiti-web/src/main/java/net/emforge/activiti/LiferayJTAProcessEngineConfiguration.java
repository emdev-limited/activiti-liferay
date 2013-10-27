package net.emforge.activiti;

import javax.transaction.TransactionManager;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.cfg.JtaProcessEngineConfiguration;
import org.activiti.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.activiti.engine.impl.cfg.jta.JtaTransactionContextFactory;
import org.activiti.engine.impl.interceptor.CommandInterceptor;
import org.activiti.engine.impl.interceptor.JtaTransactionInterceptor;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Custom {@link JtaProcessEngineConfiguration} implementation to load own
 * classloader and database configurations.
 * 
 * @author akakunin
 * @author Oliver Teichmann, PRODYNA AG
 * 
 */
public class LiferayJTAProcessEngineConfiguration extends LiferayProcessEngineConfiguration {
	protected TransactionManager jtaTransactionManager;

	public LiferayJTAProcessEngineConfiguration() {
		super();
	}

	@Override
	protected CommandInterceptor createTransactionInterceptor() {
		if (transactionManager == null) {
			throw new ActivitiException(
					"transactionManager is required property for JtaProcessEngineConfiguration, use "
							+ StandaloneProcessEngineConfiguration.class
									.getName() + " otherwise");
		}

		return new JtaTransactionInterceptor(jtaTransactionManager);
	}
	
	@Override
	protected void initTransactionContextFactory() {
		if (transactionContextFactory == null) {
			transactionContextFactory = new JtaTransactionContextFactory(
					getJtaTransactionManager());
		}
	}

	protected TransactionManager getJtaTransactionManager() {
		if (jtaTransactionManager == null) {
			if (transactionManager instanceof TransactionManager)
				return (TransactionManager) transactionManager;
			else
				return null;
		} else
			return jtaTransactionManager;
	}

	public void setJtaTransactionManager(TransactionManager tm) {
		jtaTransactionManager = tm;
		if (tm instanceof PlatformTransactionManager)
			transactionManager = (PlatformTransactionManager) tm;
		else
			transactionManager = null;
	}

	@Override
	public PlatformTransactionManager getTransactionManager() {
		if (transactionManager == null) {
			if (jtaTransactionManager != null
					&& jtaTransactionManager instanceof PlatformTransactionManager)
				return (PlatformTransactionManager) jtaTransactionManager;
			else
				return null;
		} else
			return super.getTransactionManager();
	}

	@Override
	public void setTransactionManager(PlatformTransactionManager tm) {
		super.setTransactionManager(tm);
		if (tm instanceof TransactionManager)
			jtaTransactionManager = (TransactionManager) tm;
	}

}
