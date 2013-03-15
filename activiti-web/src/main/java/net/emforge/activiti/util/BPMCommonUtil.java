package net.emforge.activiti.util;

import net.emforge.activiti.WorkflowInstanceManagerImpl;

import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.springframework.stereotype.Service;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.kernel.workflow.WorkflowException;
import com.liferay.portal.kernel.workflow.WorkflowStatusManagerUtil;
import com.liferay.portlet.asset.AssetRendererFactoryRegistryUtil;
import com.liferay.portlet.asset.model.AssetRenderer;
import com.liferay.portlet.asset.model.AssetRendererFactory;
import com.liferay.portlet.dynamicdatalists.model.DDLRecord;
import com.liferay.portlet.dynamicdatalists.model.DDLRecordSet;
import com.liferay.portlet.dynamicdatalists.model.DDLRecordVersion;
import com.liferay.portlet.dynamicdatalists.service.DDLRecordLocalServiceUtil;
import com.liferay.portlet.dynamicdatalists.service.DDLRecordSetLocalServiceUtil;
import com.liferay.portlet.dynamicdatalists.service.persistence.DDLRecordVersionUtil;

@Service("bpmCommon")
public class BPMCommonUtil {
	private static Log _log = LogFactoryUtil.getLog(BPMCommonUtil.class);

	public String getAssetSummary() {
		try {
			String summary = null;
			ExecutionEntity execution = Context.getExecutionContext().getExecution();
			String entryClassName = (String) execution.getVariable(WorkflowConstants.CONTEXT_ENTRY_CLASS_NAME);
			String entryClassPKStr = (String) execution.getVariable(WorkflowConstants.CONTEXT_ENTRY_CLASS_PK);
			Long entryClassPK = Long.valueOf(entryClassPKStr);
			AssetRendererFactory arf = AssetRendererFactoryRegistryUtil.getAssetRendererFactoryByClassName(entryClassName);
			if (entryClassName.equals(DDLRecord.class.getName())) {
				//Hack - in this case we are expecting id of DDLRecordVersion and return record set description
				DDLRecordVersion ddlVersion = DDLRecordLocalServiceUtil.getRecordVersion(Long.valueOf(entryClassPK));
				DDLRecordSet set = DDLRecordSetLocalServiceUtil.getRecordSet(ddlVersion.getRecordSetId());
				return set.getDescription(LocaleUtil.getDefault());
			}
			AssetRenderer ar = arf.getAssetRenderer(entryClassPK);
			return ar.getSummary(LocaleUtil.getDefault());
		} catch (Exception e) {
			_log.error(e, e);
			return StringPool.BLANK;
		}
	}
	
	public void updateStatus(String status){
		ExecutionEntity execution = Context.getExecutionContext().getExecution();
		int st = WorkflowConstants.toStatus(status);
		try {
			WorkflowStatusManagerUtil.updateStatus(st, WorkflowInstanceManagerImpl.convertFromVars(execution.getVariables()));
		} catch (WorkflowException e) {
			_log.error("Failed to update status", e);
		}
	}
}
