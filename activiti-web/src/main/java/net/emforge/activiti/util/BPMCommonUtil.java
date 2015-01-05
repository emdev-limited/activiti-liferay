package net.emforge.activiti.util;

import java.util.Map;

import net.emforge.activiti.WorkflowUtil;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.springframework.stereotype.Service;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.kernel.workflow.WorkflowException;
import com.liferay.portal.kernel.workflow.WorkflowHandler;
import com.liferay.portal.kernel.workflow.WorkflowHandlerRegistryUtil;
import com.liferay.portal.kernel.workflow.WorkflowStatusManagerUtil;
import com.liferay.portlet.asset.AssetRendererFactoryRegistryUtil;
import com.liferay.portlet.asset.model.AssetRenderer;
import com.liferay.portlet.asset.model.AssetRendererFactory;
import com.liferay.portlet.dynamicdatalists.model.DDLRecord;
import com.liferay.portlet.dynamicdatalists.model.DDLRecordSet;
import com.liferay.portlet.dynamicdatalists.model.DDLRecordVersion;
import com.liferay.portlet.dynamicdatalists.service.DDLRecordLocalServiceUtil;
import com.liferay.portlet.dynamicdatalists.service.DDLRecordSetLocalServiceUtil;

@Service("bpmCommon")
public class BPMCommonUtil {
	private static Log _log = LogFactoryUtil.getLog(BPMCommonUtil.class);

	public String getAssetSummary() {
		try {
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
	
	/**
	 * Tries to update synchronously first, then asynchronously
	 * 
	 * @param status
	 */
	public void updateStatus(String status){
		try {
			updateStatusSynchronously(status);
		} catch (Exception e) {
			_log.error("Failed to update status", e);
			try {
				//something goes wrong - try Liferay classic way:
				updateStatusAsynchronously(status);
			} catch (Exception e2) {
				_log.error("Failed to update status Asynchronously", e2);
			}
		}
	}
	
	/**
	 * This method updates asset status in a asynchronous way - standard for Liferay
	 * 
	 * @param status
	 */
	public void updateStatusAsynchronously(String status){
		try {
			ExecutionEntity execution = Context.getExecutionContext().getExecution();
			int st = WorkflowConstants.getLabelStatus(status);
			
			WorkflowStatusManagerUtil.updateStatus(st, WorkflowUtil.convertFromVars(execution.getVariables()));
		} catch (WorkflowException e) {
			_log.error("Failed to update status", e);
		}
	}
	
	/**
	 * This method updates asset status in a synchronous way.
	 * Standard Liferay implementation uses asynchronous.
	 * 
	 * @param status
	 */
	public void updateStatusSynchronously(String status){
		
		try {
			ExecutionEntity execution = Context.getExecutionContext().getExecution();
			
			if (!(execution instanceof DelegateExecution)) {
				throw new Exception();
			}
			Map<String, Object> vars = execution.getVariables();
			String className = (String)vars.get(
					WorkflowConstants.CONTEXT_ENTRY_CLASS_NAME);

			WorkflowHandler workflowHandler = WorkflowHandlerRegistryUtil.getWorkflowHandler(className);

			if (workflowHandler != null) {
				workflowHandler.updateStatus(WorkflowConstants.getLabelStatus(status), WorkflowUtil.convertFromVars(vars));
			} else {
				_log.warn("Could not update status cause could not find appropriate workflowHandler");
			}
		} catch (Exception e) {
			_log.error("Failed to update status synchronously", e);
		}
	}
	
}
