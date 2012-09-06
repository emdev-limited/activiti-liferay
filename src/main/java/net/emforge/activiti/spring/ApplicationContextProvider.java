package net.emforge.activiti.spring;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * ApplicationContextProvider provides access to spring application context from 
 * non-spring classes. If your class is not described as spring bean, spring component 
 * or spring service, you can retrieve spring context:
 * 
 *   ApplicationContext context = ApplicationContextProvider.getApplicationContext(); 
 * 
 * @author akakunin
 *
 */
public class ApplicationContextProvider implements ApplicationContextAware {
	private static ApplicationContext ctx = null;
	public static ApplicationContext getApplicationContext() {
		return ctx;
	}
	
	public void setApplicationContext(ApplicationContext ctx) throws BeansException {
		this.ctx = ctx;
	}
}
