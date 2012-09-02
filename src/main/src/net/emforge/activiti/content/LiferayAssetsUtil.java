package net.emforge.activiti.content;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.activiti.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Service;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portlet.asset.model.AssetEntry;
import com.liferay.portlet.asset.service.AssetEntryLocalServiceUtil;

/**
 * Service utility class for asset opperations.
 * 
 * @author Oliver Teichmann, PRODYNA AG
 */
@Service("liferayAssets")
public class LiferayAssetsUtil {
	
	private static Log _log = LogFactoryUtil.getLog(LiferayAssetsUtil.class);
	
	/**
	 * Get the tag list for the asset of the current execution.
	 * 
	 * @param execution The current workflow execution.
	 * @return The list of the assets tags.
	 */
	public Collection<String> getAssetTags(DelegateExecution execution) {

		String entryClassName = GetterUtil.getString((Serializable)execution.getVariable(WorkflowConstants.CONTEXT_ENTRY_CLASS_NAME));
		long entryClassPK = GetterUtil.getLong((Serializable)execution.getVariable(WorkflowConstants.CONTEXT_ENTRY_CLASS_PK));
		
		_log.info("entryClassName: " + entryClassName);
		_log.info("entryClassPK: " + Long.toString(entryClassPK));
		
		String[] tagNames = null;
		try {
			AssetEntry assetEntry = AssetEntryLocalServiceUtil.getEntry(entryClassName, entryClassPK);
			tagNames = assetEntry.getTagNames();
		} catch (PortalException e) {
			_log.error("Could not load asset tag names!", e);
		} catch (SystemException e) {
			_log.error("Could not load asset tag names!", e);
		}
		
		List<String> assetTagList = new ArrayList<String>();
		
		if(tagNames != null) {
			_log.info("Asset tags: " + Arrays.toString(tagNames));
			assetTagList.addAll(Arrays.asList(tagNames));
		}
		
		return assetTagList;
	}

}