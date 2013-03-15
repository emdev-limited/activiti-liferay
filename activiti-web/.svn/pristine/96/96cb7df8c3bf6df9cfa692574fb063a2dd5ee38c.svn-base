package net.emforge.activiti.identity;

import org.activiti.engine.impl.persistence.entity.UserEntity;



/** Liferay based implementation of jBPM4 user interface
 * 
 * @author akakunin
 *
 */
public class UserImpl extends UserEntity {
	private static final long serialVersionUID = -5809624687782521587L;
	
	public UserImpl(com.liferay.portal.model.User liferayUser) {
		id = String.valueOf(liferayUser.getUserId());
		firstName = liferayUser.getFirstName();
		lastName = liferayUser.getLastName();
		email = liferayUser.getEmailAddress();
	}
}
