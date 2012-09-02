package net.emforge.activiti;

import org.springframework.stereotype.Service;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.model.User;
import com.liferay.portal.service.UserLocalServiceUtil;

/** It is temporary solution to call some required login from activiti process.
 * The thing is - then I've tried to use liferayIdentityService from activiti process I've got NPE
 * Because idMappingService is not initialized.
 * For me - it looks like bean in this case is not initialized properly - in fact another instance of service is created
 * (not same as used in plugin itself) - and neiver AfterPropertiesSet - nor any Autowired is called for it
 * 
 * So, I just created simple service to call required functions
 * 
 * @author akakunin
 *
 */
@Service("liferayService")
public class LiferayService {
	private static Log _log = LogFactoryUtil.getLog(LiferayService.class);
	
	public String getUserEmail(String userId) {
		try {
			User user = UserLocalServiceUtil.getUser(Long.valueOf(userId));
			if (user != null) {
				return user.getEmailAddress();
			}
		} catch (Exception ex) {
			_log.warn("Cannot get User Email", ex);
		}
		
		return null;
	}
}
