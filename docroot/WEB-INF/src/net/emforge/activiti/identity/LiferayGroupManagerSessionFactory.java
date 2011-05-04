package net.emforge.activiti.identity;

import org.activiti.engine.impl.interceptor.Session;
import org.activiti.engine.impl.interceptor.SessionFactory;

public class LiferayGroupManagerSessionFactory implements SessionFactory {

	@Override
	public Session openSession() {
		return new LiferayGroupManagerSession();
	}

	@Override
	public Class<?> getSessionType() {
		return LiferayGroupManagerSession.class;
	}

}