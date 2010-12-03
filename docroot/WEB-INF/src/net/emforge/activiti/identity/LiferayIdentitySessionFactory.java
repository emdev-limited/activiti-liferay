package net.emforge.activiti.identity;

import org.activiti.engine.impl.interceptor.Session;
import org.activiti.engine.impl.interceptor.SessionFactory;

public class LiferayIdentitySessionFactory implements SessionFactory {

	@Override
	public Session openSession() {
		return new LiferayIdentitySessionImpl();
	}

	@Override
	public Class<?> getSessionType() {
		return LiferayIdentitySessionImpl.class;
	}

}
