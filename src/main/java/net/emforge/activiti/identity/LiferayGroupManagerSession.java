package net.emforge.activiti.identity;

import java.util.List;

import net.emforge.activiti.IdMappingService;
import net.emforge.activiti.spring.Initializable;

import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.GroupQuery;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.persistence.entity.GroupEntity;
import org.activiti.engine.impl.persistence.entity.GroupManager;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

@Service("liferayGroupManagerSession")
public class LiferayGroupManagerSession extends GroupManager
	implements ApplicationContextAware, Initializable
{
	private static Log _log = LogFactoryUtil.getLog(LiferayGroupManagerSession.class);

	ApplicationContext applicationContext;
	
	private LiferayIdentityService liferayIdentityService;
	
	public void init() {
		liferayIdentityService = applicationContext.getBean("liferayIdentityService", LiferayIdentityService.class);
	}	
	
	@Override
	public Group createNewGroup(String groupId) {
		_log.error("Method is not implemented"); // TODO
		return null;
	}
	
	@Override
	public void insertGroup(Group group) {
		_log.error("Method is not implemented"); // TODO
		
	}
	
	@Override
	public void updateGroup(Group updatedGroup) {
		_log.error("Method is not implemented"); // TODO
		
	}

	@Override
	public void deleteGroup(String groupId) {
		_log.error("Method is not implemented"); // TODO
	}
	
	@Override
	public GroupQuery createNewGroupQuery() {
		_log.error("Method is not implemented"); // TODO
		return null;
	}
	
	@Override
	public List<Group> findGroupByQueryCriteria(Object query, Page page) {
		_log.error("Method is not implemented"); // TODO
		return null;
	}

	@Override
	public long findGroupCountByQueryCriteria(Object query) {
		_log.error("Method is not implemented"); // TODO
		return -1;
	}
	
	@Override
	public GroupEntity findGroupById(String groupId) {
		_log.error("Method is not implemented"); // TODO
		return null;
	}
	
	@Override
	public List<Group> findGroupsByUser(String userId) {
		return liferayIdentityService.findGroupsByUser(userId);
	}
	
	@Override
	public void setApplicationContext(ApplicationContext ctx)
			throws BeansException {
		applicationContext = ctx;
		
	}		

}
