package net.emforge.activiti.service.transaction;

import net.emforge.activiti.spring.ApplicationContextProvider;

import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

@Order(2)
public class ActivitiTransactionAdvice {
	private static Log _log = LogFactoryUtil.getLog(ActivitiTransactionAdvice.class.getName());
	
	private ApplicationContext context;
	private DataSourceTransactionManager txManager;
	private ThreadLocal<TransactionStatus> threadStatusLocal = new ThreadLocal<TransactionStatus>();
	
	public ActivitiTransactionAdvice() {
		context = ApplicationContextProvider.getApplicationContext();
	}
	
	public void before() {
		if (txManager == null) {
			txManager = (DataSourceTransactionManager) context.getBean("transactionManager");
		}
		//Put transaction status into local thread
		TransactionDefinition def = new DefaultTransactionDefinition();
	    TransactionStatus status = txManager.getTransaction(def);
		threadStatusLocal.set(status);
	}

	public void afterFault(Exception ex) {
		_log.info("Activiti transaction rollback invoked");
		try {
			TransactionStatus status = threadStatusLocal.get();
		    txManager.rollback( status );
		} finally {
			threadStatusLocal.remove();
		}
	}
	
	public void afterSuccess(Object retVal) {
		_log.info("Activiti transaction commit invoked");
		try {
			TransactionStatus status = threadStatusLocal.get();
		    txManager.commit( status );
		} finally {
			threadStatusLocal.remove();
		}
	}
}
