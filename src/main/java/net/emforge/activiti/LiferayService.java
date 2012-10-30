package net.emforge.activiti;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.MimeTypesUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.kernel.workflow.WorkflowHandlerRegistryUtil;
import com.liferay.portal.model.Company;
import com.liferay.portal.model.Group;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.RoleConstants;
import com.liferay.portal.model.User;
import com.liferay.portal.model.UserGroupRole;
import com.liferay.portal.service.CompanyLocalServiceUtil;
import com.liferay.portal.service.GroupLocalServiceUtil;
import com.liferay.portal.service.RoleLocalServiceUtil;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.service.UserGroupRoleLocalServiceUtil;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portlet.asset.model.AssetCategory;
import com.liferay.portlet.asset.model.AssetTag;
import com.liferay.portlet.asset.model.AssetVocabulary;
import com.liferay.portlet.asset.service.AssetCategoryLocalServiceUtil;
import com.liferay.portlet.asset.service.AssetEntryLocalServiceUtil;
import com.liferay.portlet.asset.service.AssetTagLocalServiceUtil;
import com.liferay.portlet.asset.service.AssetVocabularyLocalServiceUtil;
import com.liferay.portlet.documentlibrary.NoSuchFileEntryException;
import com.liferay.portlet.documentlibrary.NoSuchFolderException;
import com.liferay.portlet.documentlibrary.model.DLFileEntry;
import com.liferay.portlet.documentlibrary.model.DLFileEntryConstants;
import com.liferay.portlet.documentlibrary.model.DLFileVersion;
import com.liferay.portlet.documentlibrary.model.DLFolder;
import com.liferay.portlet.documentlibrary.model.DLFolderConstants;
import com.liferay.portlet.documentlibrary.model.DLSyncConstants;
import com.liferay.portlet.documentlibrary.service.DLFileEntryLocalServiceUtil;
import com.liferay.portlet.documentlibrary.service.DLFileVersionLocalServiceUtil;
import com.liferay.portlet.documentlibrary.service.DLFolderLocalServiceUtil;
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
				for (UserGroupRole userGroupRole : 
						UserGroupRoleLocalServiceUtil.getUserGroupRolesByGroupAndRole(groupId, role.getRoleId())) {
					users.add(userGroupRole.getUser());
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
	
	/** Copy file from one folder to another. This method is temporary here - we need it for making some demos. Later we will move it into proper location
	 * 
	 * TODO - Move it into correct place.
	 * 
	 * @param classPK
	 * @param ownerId
	 * @param destOwnerId
	 * @param targetOrgFriendlyUrl
	 * @param destinationFolder
	 * @param remove
	 */
	public void copyFileToTargetGroup(long classPK, long ownerId, long destOwnerId, String targetOrgFriendlyUrl, String destinationFolder, boolean remove) {
		_log.info("Copy file " + classPK + " to group " + targetOrgFriendlyUrl + " and destination folder " + destinationFolder);
		
		DLFileEntry toCopy = null;
		DLFileVersion version = null;
		InputStream is = null;
		try {
			version = DLFileVersionLocalServiceUtil.getFileVersion(classPK);
			toCopy =  DLFileEntryLocalServiceUtil.getDLFileEntry(version.getFileEntryId());
		} catch (Exception e) {
			_log.warn(String.format("No file exists with pk = [%s]", classPK));
			return;
		}
		if (toCopy == null) {
			_log.warn(String.format("No file exists with pk = [%s]", classPK));
			return;
		}
		
		Group targetGroup = null;
		try {
			targetGroup = GroupLocalServiceUtil.getFriendlyURLGroup(toCopy.getCompanyId(), targetOrgFriendlyUrl);
		} catch (Exception e) {
			_log.warn(String.format("No group exists with friendly url = [%s]", targetOrgFriendlyUrl));
			return;
		}
		if (targetGroup == null) {
			_log.warn(String.format("No group exists with friendly url = [%s]", targetOrgFriendlyUrl));
			return;
		}
		
		try {
			//get initial folder
			DLFolder initialFolder = DLFolderLocalServiceUtil.getFolder(toCopy.getFolderId());
			String[] folderPath = null;
			if (StringUtils.isNotEmpty(destinationFolder)) {
				if (destinationFolder.startsWith(FOLDER_DELIMITER)) {
					destinationFolder = destinationFolder.substring(1);
				}
				if (destinationFolder.endsWith(FOLDER_DELIMITER)) {
					destinationFolder = destinationFolder.substring(0, destinationFolder.length() - 1);
				}
				folderPath = destinationFolder.split(FOLDER_DELIMITER);
			} else {
				folderPath = initialFolder.getPathArray();
			}
			ServiceContext serviceContext = new ServiceContext();
			serviceContext.setAddGuestPermissions(false);
			serviceContext.setAddGroupPermissions(true);
			serviceContext.setUserId(destOwnerId);
			DLFolder targetFolder = createFolderStructureByPath(targetGroup.getGroupId(), destOwnerId, folderPath, serviceContext);
			
			is = DLFileEntryLocalServiceUtil.getFileAsStream(ownerId, version.getFileEntryId(), null);
			byte[] bytes = IOUtils.toByteArray(is);
			
			
			if (remove) {
				DLFileEntryLocalServiceUtil.deleteDLFileEntry(toCopy);
			}
			
			serviceContext.setWorkflowAction(WorkflowConstants.ACTION_PUBLISH);
			
			File file = FileUtil.createTempFile(bytes);
			String contentType = MimeTypesUtil.getContentType(toCopy.getName());
			DLFileEntry dlFileEntry = DLFileEntryLocalServiceUtil.addFileEntry(destOwnerId, 
													 targetFolder.getGroupId(), 
													 targetFolder.getRepositoryId(), 
												  	  targetFolder.getFolderId(),
												  	  toCopy.getTitle(), 
													  contentType, 
													  toCopy.getTitle(),
													  toCopy.getDescription(), 
													  "", // changelog
													  toCopy.getFileEntryTypeId(), toCopy.getFieldsMap(toCopy.getFileVersion().getFileVersionId()), 
													  file, is, toCopy.getSize(), serviceContext);
			
			DLFileEntryLocalServiceUtil.addFileEntryResources(
					dlFileEntry, true, true);
			
			DLFileVersion dlFileVersion = dlFileEntry.getFileVersion();
			AssetEntryLocalServiceUtil.updateEntry(
					destOwnerId, dlFileEntry.getGroupId(),
					DLFileEntryConstants.getClassName(),
					dlFileEntry.getFileEntryId(), dlFileEntry.getUuid(),
					dlFileEntry.getFileEntryTypeId(), new long[] {}, new String[] {}, false, null,
					null, null, null, dlFileEntry.getMimeType(), dlFileEntry.getTitle(),
					dlFileEntry.getDescription(), null, null, null, 0, 0, null,
					false);

			Map<String, Serializable> workflowContext =
					new HashMap<String, Serializable>();

			workflowContext.put("event", DLSyncConstants.EVENT_ADD);

			WorkflowHandlerRegistryUtil.startWorkflowInstance(
				dlFileVersion.getCompanyId(), dlFileVersion.getGroupId(),
				destOwnerId, DLFileEntryConstants.getClassName(),
				dlFileVersion.getFileVersionId(), dlFileVersion, serviceContext,
				workflowContext);

			
			_log.info("File " + toCopy.getTitle() + " copied");
		} catch (Exception e) {
			_log.error(String.format("Error while copying file from group [%s] and path [%s] to [%s]", 
					toCopy != null ? toCopy.getGroupId() : "null file", 
							targetOrgFriendlyUrl,targetGroup != null? targetGroup.getGroupId() : "null target group"), e);
		} finally {
			if (is != null) {
				try {
					is.close();
					is = null;
				} catch (IOException e2) {}
			}
		}
		
	}
	
	protected DLFolder createFolderStructureByPath(long groupId, long userId, String[] path, ServiceContext serviceContext) throws PortalException, SystemException {
		DLFolder folder = null;
		long parentFolderId = DLFolderConstants.DEFAULT_PARENT_FOLDER_ID;
		for (String fName : path) {
			try {
				folder = DLFolderLocalServiceUtil.getFolder(groupId, parentFolderId, fName);
				parentFolderId = folder.getFolderId();
			} catch (NoSuchFolderException e) {
				_log.debug("Adding new folder with name = " + fName);
				folder = DLFolderLocalServiceUtil.addFolder(userId, groupId, groupId, false, parentFolderId, fName, fName, serviceContext);
				parentFolderId = folder.getFolderId();
			}
		}
		return folder;
	}
	
	/**
     * Construct unique file name to avoid DuplicateFileException if file with same name already exists
     */
    protected String constructUniqueFileName(long groupId, long folderId, String origFileName) throws SystemException, PortalException {
        String name = FileUtil.stripExtension(origFileName);
        String extension = FileUtil.getExtension(origFileName);

        int feCounter = 0;
        boolean feFound = true;
        String checkFileName = origFileName;
        while(feFound) {
            //for some reason name is stored in title
            try {
                DLFileEntryLocalServiceUtil.getFileEntryByName(groupId, folderId, checkFileName);
                feCounter++;
                checkFileName = name + StringPool.OPEN_PARENTHESIS + feCounter + StringPool.CLOSE_PARENTHESIS
                        + StringPool.PERIOD + extension;
            } catch (NoSuchFileEntryException e) {
                feFound = false;
            }
        }
        return checkFileName;
    }
}
