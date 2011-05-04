package net.emforge.activiti.identity;

import org.activiti.engine.impl.interceptor.Session;
import org.activiti.engine.impl.interceptor.SessionFactory;

public class LiferayUserManagerSessionFactory implements SessionFactory {

	@Override
	public Session openSession() {
		return new LiferayUserManagerSession();
	}

	@Override
	public Class<?> getSessionType() {
		return LiferayUserManagerSession.class;
	}

}