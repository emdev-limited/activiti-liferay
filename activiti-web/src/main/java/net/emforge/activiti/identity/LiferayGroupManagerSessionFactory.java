package net.emforge.activiti.identity;

import org.activiti.engine.impl.interceptor.Session;
import org.activiti.engine.impl.interceptor.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("liferayGroupManagerSessionFactory")
public class LiferayGroupManagerSessionFactory implements SessionFactory {
	@Autowired
	LiferayGroupManagerSession liferayGroupManagerSession;

	@Override
	public Session openSession() {
		return liferayGroupManagerSession;
	}

	@Override
	public Class<?> getSessionType() {
		return LiferayGroupManagerSession.class;
	}

}