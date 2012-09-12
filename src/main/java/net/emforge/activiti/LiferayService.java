package net.emforge.activiti;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.kernel.util.MimeTypesUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.model.CompanyConstants;
import com.liferay.portal.model.Group;
import com.liferay.portal.model.User;
import com.liferay.portal.service.GroupLocalServiceUtil;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portlet.documentlibrary.NoSuchFileEntryException;
import com.liferay.portlet.documentlibrary.NoSuchFolderException;
import com.liferay.portlet.documentlibrary.model.DLFileEntry;
import com.liferay.portlet.documentlibrary.model.DLFileVersion;
import com.liferay.portlet.documentlibrary.model.DLFolder;
import com.liferay.portlet.documentlibrary.model.DLFolderConstants;
import com.liferay.portlet.documentlibrary.service.DLAppLocalServiceUtil;
import com.liferay.portlet.documentlibrary.service.DLFileEntryLocalServiceUtil;
import com.liferay.portlet.documentlibrary.service.DLFileVersionLocalServiceUtil;
import com.liferay.portlet.documentlibrary.service.DLFolderLocalServiceUtil;

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
			
			File file = FileUtil.createTempFile(bytes);
			String contentType = MimeTypesUtil.getContentType(toCopy.getName());
			FileEntry fEntry = DLAppLocalServiceUtil.addFileEntry(destOwnerId, 
																  targetFolder.getRepositoryId(), 
															  	  targetFolder.getFolderId(),
															  	  toCopy.getTitle(), 
																  contentType, 
																  toCopy.getTitle(),//use initial fileName
																  toCopy.getDescription(), 
																  "", 				//? changeLog
																  file, serviceContext);
			
			_log.info("File " + file.getName() + " copied");
		} catch (Exception e) {
			_log.error(String.format("Error while copying file from group [%s] and path [%s] to [%s]", 
					toCopy != null ? toCopy.getGroupId() : "null file", 
							targetOrgFriendlyUrl,targetGroup != null? targetGroup.getGroupId() : "null target group"));
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
