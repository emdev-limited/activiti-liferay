package net.emforge.activiti;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.model.Company;
import com.liferay.portal.model.Group;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.RoleConstants;
import com.liferay.portal.model.User;
import com.liferay.portal.model.UserGroupRole;
import com.liferay.portal.service.CompanyLocalServiceUtil;
import com.liferay.portal.service.GroupLocalServiceUtil;
import com.liferay.portal.service.RoleLocalServiceUtil;
import com.liferay.portal.service.UserGroupRoleLocalServiceUtil;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portlet.asset.model.AssetCategory;
import com.liferay.portlet.asset.model.AssetTag;
import com.liferay.portlet.asset.model.AssetVocabulary;
import com.liferay.portlet.asset.service.AssetCategoryLocalServiceUtil;
import com.liferay.portlet.asset.service.AssetTagLocalServiceUtil;
import com.liferay.portlet.asset.service.AssetVocabularyLocalServiceUtil;
import com.liferay.portlet.journal.model.JournalArticle;
import com.liferay.portlet.journal.service.JournalArticleLocalServiceUtil;

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
	
	private static String RESERVED_ORG_USER_ROLE 	= "Organization User";
	private static String RESERVED_SITE_MEMBER_ROLE = "Site Member";
	
	public static final String FOLDER_DELIMITER = "/";
	
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

	/** Returns coma-separated list of emails to be sent for specified group
	 * 
	 * @param groupOrCompanyId
	 * @param roleName
	 * @return
	 */
	public String getRoleEmails(Long companyId, Long groupId, String roleName) {
		_log.debug("Get Role Emails for role: " + roleName);
		
		try {
			Role role = RoleLocalServiceUtil.getRole(companyId, roleName);
			List<User> users = new ArrayList<User>();
			
			if (role.getType() == RoleConstants.TYPE_REGULAR) {
				// regular (system wide role)
				users = UserLocalServiceUtil.getRoleUsers(role.getRoleId());
			} else {
				// group specific role
				if (roleName.equals(RESERVED_ORG_USER_ROLE) || roleName.equals(RESERVED_SITE_MEMBER_ROLE)) {
					//add all users of a unit
					Group group = GroupLocalServiceUtil.getGroup(groupId);
					if (roleName.equals(RESERVED_ORG_USER_ROLE)) {
						//get org users
						users = UserLocalServiceUtil.getOrganizationUsers(group.getClassPK());
					} else {
						//get site users
						users = UserLocalServiceUtil.getGroupUsers(groupId);
					}
				} else {
					for (UserGroupRole userGroupRole : 
						UserGroupRoleLocalServiceUtil.getUserGroupRolesByGroupAndRole(groupId, role.getRoleId())) {
					users.add(userGroupRole.getUser());
				}
				}
			}
			
			Set<String> emails = new HashSet<String>();
			for (User user : users) {
				emails.add(user.getEmailAddress());
			}
			
			String result = StringUtils.join(emails, ",");
			
			_log.debug("Group Emails: " + result);
			return result;
		} catch (Exception ex) {
			_log.warn("Cannot get Group Email", ex);
		}
		
		return null;
	}
	
	/**
	 * Check if user has group or global role named {@param roleName}
	 * @param companyId
	 * @param userId
	 * @param groupId
	 * @param roleName
	 * @return
	 */
	public boolean isUserInRole(long companyId, long userId, long groupId, String roleName) {
		try {
			Role role = RoleLocalServiceUtil.getRole(companyId, roleName);
			if (role != null) {
				List<Role> roles = getUserUnitRoles(userId, groupId);
				roles.addAll(getUserGlobalRoles(userId));
				if (roles.contains(role)) {
					return true;
				}
			}
		} catch (Exception e) {
			_log.error(String.format("Failed to retrieve user roles for user id = [%s]", userId),e);
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	protected static List<Role> getUserGlobalRoles(long userId) throws SystemException, PortalException {
		List<Role> roles = new ArrayList<Role>();
		List<Role> allRoles = RoleLocalServiceUtil.getUserRoles(userId);
		for (Role role : allRoles) {
			if (role.getType() == RoleConstants.TYPE_REGULAR) {
				roles.add(role);
			}
		}
		return roles == null ? Collections.EMPTY_LIST : roles;
	}
	
	@SuppressWarnings("unchecked")
	protected static List<Role> getUserUnitRoles(long userId, long groupId) throws SystemException, PortalException {
		List<Role> roles = new ArrayList<Role>();
		List<UserGroupRole> ugrList = UserGroupRoleLocalServiceUtil.getUserGroupRoles(userId, groupId);
		for (UserGroupRole ugr : ugrList) {
			roles.add(ugr.getRole());
		}
		return roles == null ? Collections.EMPTY_LIST : roles;
	}
	
	/**
	 * Check if asset has category named {@param catName}
	 * 
	 * @param groupId
	 * @param classNameId
	 * @param classPK
	 * @param vocabularyName
	 * @param catName
	 * @param locale
	 * @return
	 */
	public boolean hasAssetCategory(long groupId, String className, long classPK, String vocabularyTitle, String catTitle, String languageId) {
		try {
			Locale locale = LocaleUtil.fromLanguageId(languageId);
			
			// check - is groupId - group id or companyId?
			try {
				GroupLocalServiceUtil.getGroup(groupId);
			} catch (Exception ex) {
				// group not found - lets try to get by companyId
				Company company = CompanyLocalServiceUtil.getCompany(groupId);
				// use it's group id
				groupId = company.getGroup().getGroupId();
			}
			
			// special processing for journalArticle - id is passed - but resourcePrimKey should be used
			if (className.equals(JournalArticle.class.getName())) {
				JournalArticle journalArticle = JournalArticleLocalServiceUtil.getArticle(classPK);
				classPK = journalArticle.getResourcePrimKey();
			}
			
			AssetVocabulary vocab = null;
			if (StringUtils.isNotEmpty(vocabularyTitle)) {
				// get vocabulary by title
				List<AssetVocabulary> vocabs = AssetVocabularyLocalServiceUtil.getGroupVocabularies(groupId);
				for (AssetVocabulary curVocab : vocabs) {
					String title = curVocab.getTitle(locale);
					if (StringUtils.equalsIgnoreCase(vocabularyTitle, title)) {
						vocab = curVocab;
						break;
					}
				}
			}
				
			//if vocabulary is null then just get all possible cats for the given className-classPK
			List<AssetCategory> cats = new ArrayList<AssetCategory>();
			cats.addAll(AssetCategoryLocalServiceUtil.getCategories(className, classPK));
			if (vocab != null) {
				//extract all cats from this vocabulary that relate to className-classPK
				List<AssetCategory> vocCats = AssetCategoryLocalServiceUtil.getVocabularyCategories(vocab.getVocabularyId(), 
						QueryUtil.ALL_POS, QueryUtil.ALL_POS, null);
				List<AssetCategory> vocExtractedCats = new ArrayList<AssetCategory>();
				if (vocCats != null) {
					for (AssetCategory vocCat : vocCats) {
						if (cats.contains(vocCat)) {
							vocExtractedCats.add(vocCat);
						}
					}
				}
				
				cats = new ArrayList<AssetCategory>();
				cats.addAll(vocExtractedCats);
			}
			
			if (cats != null && !cats.isEmpty()) {
				for (AssetCategory cat : cats) {
					String title = cat.getTitle(locale);
					if (StringUtils.equalsIgnoreCase(catTitle, title)) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			_log.error(String.format("Failed to check hasAssetCategory"),e);
		}
		return false;
	}
	
	/**
	 * Check if asset has tag named {@param tagName}
	 * 
	 * @param classNameId
	 * @param classPK
	 * @param tagName
	 * @return
	 */
	public boolean hasAssetTag(long classNameId, long classPK, String tagName) {
		try {
			List<AssetTag> tags = AssetTagLocalServiceUtil.getTags(classNameId, classPK);
			if (tags != null && !tags.isEmpty()) {
				for (AssetTag tag : tags) {
					if (tag.getName().equalsIgnoreCase(tagName)) {
						return true;
					}
				}
			}
		} catch (SystemException e) {
			_log.error(String.format("Failed to check tags for classNameId = [%s], " +
					"classPK = [%s] and tagName = [%s]", classNameId, classPK, tagName), e);
		}
		return false;
	}
	
    /** Returns value of Liferay Property (for example specified in portal-ext.properties
     * 
     * @param name
     * @return
     */
    public String getProperty(String name) {
    	return PropsUtil.get(name);
    }
}
