package net.emforge.activiti.samples;

import static net.emforge.activiti.constants.RoleConstants.PURCHASING_CONTENT_REVIEWER;
import static net.emforge.activiti.constants.RoleConstants.SALES_CONTENT_REVIEWER;
import static net.emforge.activiti.constants.TagConstants.TAG_PURCHASING_CONTENT;
import static net.emforge.activiti.constants.TagConstants.TAG_SALES_CONTENT;

import java.util.Collection;
import java.util.List;

import net.emforge.activiti.content.LiferayAssetsUtil;
import net.emforge.activiti.identity.LiferayGroupsUtil;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

/**
 * Service utility class for example workflow definition GetCandidateGroupsForAssetTagsDelegate.
 * 
 * @author Oliver Teichmann, PRODYNA AG
 */
public class GetCandidateGroupsForAssetTagsDelegate implements JavaDelegate {
	
	private static Log _log = LogFactoryUtil.getLog(GetCandidateGroupsForAssetTagsDelegate.class);

	private LiferayGroupsUtil liferayGroupsUtil;
	private LiferayAssetsUtil liferayAssetsUtil;
	
	public GetCandidateGroupsForAssetTagsDelegate() {
		liferayGroupsUtil = new LiferayGroupsUtil();
		liferayAssetsUtil = new LiferayAssetsUtil();
	}
	
	@Override
	public void execute(DelegateExecution execution) throws Exception {
		
		// get asset tags
		Collection<String> assetTags = liferayAssetsUtil.getAssetTags(execution);
		
		// get approver candidate groups
		Collection<String> cadidateGroupsForTags = getCadidateGroupsForTags(execution, assetTags);

		// set process variable
		execution.setVariable("candidateGroupList", cadidateGroupsForTags);
	}
	
	private Collection<String> getCadidateGroupsForTags(DelegateExecution execution, Collection<String> assetTags) throws Exception {
		
		List<String> candidateRoleList = liferayGroupsUtil.getDefaultApproverGroups();

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
