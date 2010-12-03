package net.emforge.activiti.identity;

import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.GroupQuery;
import org.activiti.engine.identity.User;
import org.activiti.engine.identity.UserQuery;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

public class LiferayIdentityServiceImpl implements IdentityService {
	private static Log _log = LogFactoryUtil.getLog(LiferayIdentityServiceImpl.class);

	@Override
	public User newUser(String userId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void saveUser(User user) {
		// TODO Auto-generated method stub

	}

	@Override
	public UserQuery createUserQuery() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteUser(String userId) {
		// TODO Auto-generated method stub

	}

	@Override
	public Group newGroup(String groupId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GroupQuery createGroupQuery() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void saveGroup(Group group) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteGroup(String groupId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void createMembership(String userId, String groupId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteMembership(String userId, String groupId) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean checkPassword(String userId, String password) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setAuthenticatedUserId(String authenticatedUserId) {
		// TODO Auto-generated method stub

	}

}
