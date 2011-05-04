package net.emforge.activiti.identity;

import java.util.List;

import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.identity.UserQuery;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.persistence.entity.IdentityInfoEntity;
import org.activiti.engine.impl.persistence.entity.UserEntity;
import org.activiti.engine.impl.persistence.entity.UserManager;
import org.springframework.stereotype.Service;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

@Service("liferayUserManagerSession")
public class LiferayUserManagerSession extends UserManager {
	
	private static Log _log = LogFactoryUtil.getLog(LiferayUserManagerSession.class);

	private LiferayIdentityService liferayIdentityService = new LiferayIdentityService();
	
	@Override
	public User createNewUser(String userId) {
		_log.error("Method is not implemented"); // TODO
		return null;
	}

	@Override
	public void insertUser(User user) {
		_log.error("Method is not implemented"); // TODO
	}

	@Override
	public void updateUser(User updatedUser) {
		_log.error("Method is not implemented"); // TODO
	}
	
	@Override
	public UserEntity findUserById(String userId) {
		return liferayIdentityService.findUserById(userId);
	}
	
	@Override
	public void deleteUser(String userId) {
		_log.error("Method is not implemented"); // TODO
	}

	@Override
	public List<User> findUserByQueryCriteria(Object query, Page page) {
		_log.error("Method is not implemented"); // TODO
		return null;
	}

	@Override
	public long findUserCountByQueryCriteria(Object query) {
		_log.error("Method is not implemented"); // TODO
		return -1;
	}

	@Override
	public List<Group> findGroupsByUser(String userId) {
		return liferayIdentityService.findGroupsByUser(userId);
	}

	@Override
	public UserQuery createNewUserQuery() {
		_log.error("Method is not implemented"); // TODO
		return null;
	}

	@Override
	public IdentityInfoEntity findUserInfoByUserIdAndKey(String userId,
			String key) {
		_log.error("Method is not implemented"); // TODO
		return null;
	}

	@Override
	public List<String> findUserInfoKeysByUserIdAndType(String userId,
			String type) {
		_log.error("Method is not implemented"); // TODO
		return null;
	}

}
