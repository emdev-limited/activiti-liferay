package net.emforge.activiti.identity;

import net.emforge.activiti.spring.Initializable;

import org.activiti.engine.impl.interceptor.Session;
import org.activiti.engine.impl.interceptor.SessionFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

@Service("liferayGroupManagerSessionFactory")
public class LiferayGroupManagerSessionFactory implements SessionFactory, ApplicationContextAware, Initializable  
{
	ApplicationContext applicationContext;
	
	LiferayGroupManagerSession liferayGroupManagerSession;

	public void init() {
		liferayGroupManagerSession = applicationContext.getBean("liferayGroupManagerSession", LiferayGroupManagerSession.class);
	}		
	
	@Override
	public Session openSession() {
		return liferayGroupManagerSession;
	}

	@Override
	public Class<?> getSessionType() {
		return LiferayGroupManagerSession.class;
	}
	
	@Override
	public void setApplicationContext(ApplicationContext ctx)
			throws BeansException 
	{
		applicationContext = ctx;
		
	}	

}