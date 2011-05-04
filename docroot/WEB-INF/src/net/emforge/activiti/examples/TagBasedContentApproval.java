package net.emforge.activiti.examples;

import static net.emforge.activiti.constants.RoleConstants.PURCHASING_CONTENT_REVIEWER;
import static net.emforge.activiti.constants.RoleConstants.SALES_CONTENT_REVIEWER;
import static net.emforge.activiti.constants.TagConstants.TAG_PURCHASING_CONTENT;
import static net.emforge.activiti.constants.TagConstants.TAG_SALES_CONTENT;

import java.util.Collection;
import java.util.List;

import net.emforge.activiti.content.LiferayAssets;
import net.emforge.activiti.identity.LiferayGroups;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

/**
 * Service utility class for example workflow definition TagBasedContentApproval.
 * 
 * @author oteichmann
 */
@Service("tagBasedContentApproval")
public class TagBasedContentApproval {
	
	private static Log _log = LogFactoryUtil.getLog(TagBasedContentApproval.class);

	@Autowired
	LiferayGroups liferayGroups;
	
	@Autowired
	LiferayAssets liferayAssets;
	
	public Collection<String> getCandidateGroupsForAssetTags(DelegateExecution execution) throws Exception {

		// get asset tags
		Collection<String> assetTags = liferayAssets.getAssetTags(execution);
		
		// get approver candidate groups
		Collection<String> cadidateGroupsForTags = getCadidateGroupsForTags(execution, assetTags);
		
		return cadidateGroupsForTags;
	}
	
	public Collection<String> getCadidateGroupsForTags(DelegateExecution execution, Collection<String> assetTags) throws Exception {
		
		List<String> candidateRoleList = liferayGroups.getDefaultApproverGroups();

		if (assetTags.contains(TAG_PURCHASING_CONTENT)) {
			candidateRoleList.add(PURCHASING_CONTENT_REVIEWER);
			_log.debug("Adding role: " + PURCHASING_CONTENT_REVIEWER);
		}
		if (assetTags.contains(TAG_SALES_CONTENT)) {
			candidateRoleList.add(SALES_CONTENT_REVIEWER);
			_log.debug("Adding role: " + SALES_CONTENT_REVIEWER);
		}
		
		return candidateRoleList;
	}
}
