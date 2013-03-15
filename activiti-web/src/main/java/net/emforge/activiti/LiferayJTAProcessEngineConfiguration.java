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
import org.activiti.engine.impl.cfg.jta.JtaTransactionContextFactory;
import org.activiti.engine.impl.interceptor.CommandContextInterceptor;
import org.activiti.engine.impl.interceptor.CommandInterceptor;
import org.activiti.engine.impl.interceptor.JtaTransactionInterceptor;
import org.activiti.engine.impl.interceptor.LogInterceptor;
import org.activiti.engine.impl.persistence.deploy.Deployer;
import org.activiti.engine.impl.util.ReflectUtil;
import org.springframework.transaction.PlatformTransactionManager;

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

	protected TransactionManager jtaTransactionManager;
	
	public LiferayJTAProcessEngineConfiguration() {
		super();
	}
	
	  @Override
	  protected Collection< ? extends CommandInterceptor> getDefaultCommandInterceptorsTxRequired() {
	    List<CommandInterceptor> defaultCommandInterceptorsTxRequired = new ArrayList<CommandInterceptor>();
	    defaultCommandInterceptorsTxRequired.add(new LogInterceptor());
	    defaultCommandInterceptorsTxRequired.add(new JtaTransactionInterceptor(getJtaTransactionManager(), false));
	    defaultCommandInterceptorsTxRequired.add(new CommandContextInterceptor(commandContextFactory, this));
	    return defaultCommandInterceptorsTxRequired;
	  }

	  @Override
	  protected Collection< ? extends CommandInterceptor> getDefaultCommandInterceptorsTxRequiresNew() {
	    List<CommandInterceptor> defaultCommandInterceptorsTxRequiresNew = new ArrayList<CommandInterceptor>();
	    defaultCommandInterceptorsTxRequiresNew.add(new LogInterceptor());
	    defaultCommandInterceptorsTxRequiresNew.add(new JtaTransactionInterceptor(getJtaTransactionManager(), true));
	    defaultCommandInterceptorsTxRequiresNew.add(new CommandContextInterceptor(commandContextFactory, this));
	    return defaultCommandInterceptorsTxRequiresNew;
	  }
	  
	  @Override
	  protected void initTransactionContextFactory() {
	    if(transactionContextFactory == null) {
	      transactionContextFactory = new JtaTransactionContextFactory(getJtaTransactionManager());
	    }
	  }

	  protected TransactionManager getJtaTransactionManager() 
	  {
		  if (jtaTransactionManager == null) {
			  if (transactionManager instanceof TransactionManager)
				  return (TransactionManager) transactionManager;
			  else
				  return null;
		  } else
			  return jtaTransactionManager;
	  }
	  
	  public void setJtaTransactionManager(TransactionManager tm) 
	  {
		  jtaTransactionManager = tm;
		  if (tm instanceof PlatformTransactionManager)
			  transactionManager = (PlatformTransactionManager) tm;
		  else
			  transactionManager = null;
	  }

	@Override
	public PlatformTransactionManager getTransactionManager() 
	{
		if (transactionManager == null) {
			if (jtaTransactionManager != null && jtaTransactionManager instanceof PlatformTransactionManager)
				return (PlatformTransactionManager) jtaTransactionManager;
			else
				return null;
		} else
			return super.getTransactionManager();
	}

	@Override
	public void setTransactionManager(	PlatformTransactionManager tm) 
	{
		super.setTransactionManager(tm);
		if (tm instanceof TransactionManager)
			jtaTransactionManager = (TransactionManager) tm;
	}
	  
}
