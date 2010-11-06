package net.emforge.activiti;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.history.HistoricDetail;
import org.activiti.engine.history.HistoricDetailQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.workflow.DefaultWorkflowLog;
import com.liferay.portal.kernel.workflow.WorkflowException;
import com.liferay.portal.kernel.workflow.WorkflowLog;
import com.liferay.portal.kernel.workflow.WorkflowLogManager;

@Service("workflowLogManager")
public class WorkflowLogManagerImpl implements WorkflowLogManager {
	private static Log _log = LogFactoryUtil.getLog(WorkflowLogManagerImpl.class);

	@Autowired
	ProcessEngine processEngine;
	@Autowired
	IdMappingService idMappingService;
	@Autowired
	HistoryService historyService;
	
	
	@Override
	public int getWorkflowLogCountByWorkflowInstance(long companyId, long workflowInstanceId, 
													 List<Integer> logTypes) throws WorkflowException {
		_log.error("Method is not implemented"); // TODO
		return 0;
	}

	@Override
	public int getWorkflowLogCountByWorkflowTask(long companyId,
			long workflowTaskId, List<Integer> logTypes)
			throws WorkflowException {
		_log.error("Method is not implemented"); // TODO
		return 0;
	}

	@Override
	public List<WorkflowLog> getWorkflowLogsByWorkflowInstance(long companyId, long workflowInstanceId, 
															   List<Integer> logTypes, 
															   int start, int end, OrderByComparator orderByComparator) throws WorkflowException {
		if (logTypes != null) {
			_log.warn("Method is partially implemented");
		}
		
		String processInstanceId = idMappingService.getJbpmProcessInstanceId(workflowInstanceId);
		
		//HistoryProcessInstanceImpl historyPI = ()historyService.createHistoryProcessInstanceQuery().processInstanceId(processInstanceId).uniqueResult();
		HistoricDetailQuery query = historyService.createHistoricDetailQuery().processInstanceId(processInstanceId);
		
		if ((start != QueryUtil.ALL_POS) && (end != QueryUtil.ALL_POS)) {
			query.listPage(start, end - start);
		}
		
		// TODO actually only one order is supported
		if (orderByComparator != null) {
			if (orderByComparator.getOrderByFields().length > 1) {
				_log.warn("Method is partially implemented");
			} else {
				if (orderByComparator.isAscending()) {
					query.orderByTime().asc();
				} else {
					query.orderByTime().desc();
				}
			}
		}
			
		List<HistoricDetail> historyDetails = query.list();
		
		
		return getWorkflowLogs(historyDetails);
	}

	@Override
	public List<WorkflowLog> getWorkflowLogsByWorkflowTask(long companyId,
			long workflowTaskId, List<Integer> logTypes, int start, int end,
			OrderByComparator orderByComparator) throws WorkflowException {
		_log.error("Method is not implemented"); // TODO
		return null;
	}

	/** Converts jBPM4 history details into Liferay's WorkflowLog
	 * 
	 * @param historyDetails
	 * @return
	 */
	private List<WorkflowLog> getWorkflowLogs(List<HistoricDetail> historyDetails) {
		List<WorkflowLog> logs = new ArrayList<WorkflowLog>(historyDetails.size());
		
		for (HistoricDetail historyDetail : historyDetails) {
			logs.add(getWorkflowLog(historyDetail));
		}
		
		return logs;
	}

	/** Convert History Detail to WorkflowLog
	 * 
	 * @param historyDetail
	 * @return
	 */
	private WorkflowLog getWorkflowLog(HistoricDetail historyDetail) {
		DefaultWorkflowLog log = new DefaultWorkflowLog();
		
		log.setCreateDate(historyDetail.getTime());
		// TODO log.setPreviousUserId(GetterUtil.getLong(oldActorId));
		//log.setWorkflowTaskId();
		
		// TODO
		log.setType(WorkflowLog.TASK_UPDATE);
		log.setComment("");

		/*
		if (historyDetail instanceof HistoryComment) {
			HistoryComment historyComment = (HistoryComment) historyDetail;
			log.setComment(historyComment.getMessage());
			log.setState("");
			log.setType(WorkflowLog.TASK_COMPLETION);
			
		} else if (historyDetail instanceof HistoryTaskAssignmentImpl) {
			HistoryTaskAssignmentImpl taskAssignment = (HistoryTaskAssignmentImpl)historyDetail;
			log.setType(WorkflowLog.TASK_ASSIGN);
			
		} else if (historyDetail instanceof HistoryTaskDuedateUpdateImpl) {
			HistoryTaskDuedateUpdateImpl historyDetailImpl = (HistoryTaskDuedateUpdateImpl)historyDetail;
			log.setState("");
			
		}
		*/
		
		return log;
	}
	


}
