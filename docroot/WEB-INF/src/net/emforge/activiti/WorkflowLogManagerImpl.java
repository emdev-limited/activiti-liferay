package net.emforge.activiti;

import java.util.ArrayList;
import java.util.List;

import net.emforge.activiti.dao.ProcessInstanceHistoryDao;
import net.emforge.activiti.entity.ProcessInstanceHistory;

import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
	
	@Autowired
	ProcessInstanceHistoryDao processInstanceHistoryDao;
	
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
		
		List<ProcessInstanceHistory> historyDetails = processInstanceHistoryDao.searchByWorkflowInstance(workflowInstanceId, logTypes, start, end, orderByComparator);

		return getWorkflowLogs(historyDetails);
	}

	@Override
	public List<WorkflowLog> getWorkflowLogsByWorkflowTask(long companyId,
			long workflowTaskId, List<Integer> logTypes, int start, int end,
			OrderByComparator orderByComparator) throws WorkflowException {
		_log.error("Method is not implemented"); // TODO
		return null;
	}

	private List<WorkflowLog> getWorkflowLogs(List<ProcessInstanceHistory> historyDetails) {
		List<WorkflowLog> logs = new ArrayList<WorkflowLog>(historyDetails.size());
		
		for (ProcessInstanceHistory historyDetail : historyDetails) {
			WorkflowLog log = getWorkflowLog(historyDetail);
			if (log != null) {
				logs.add(log);
			}
		}
		
		return logs;
	}

	private WorkflowLog getWorkflowLog(ProcessInstanceHistory historyDetail) {
		DefaultWorkflowLog log = new DefaultWorkflowLog();
		
		log.setComment(historyDetail.getComment());
		log.setCreateDate(historyDetail.getCreateDate());
		log.setPreviousState(historyDetail.getPreviousState());
		log.setPreviousUserId(historyDetail.getPreviousUserId());
		log.setPreviousRoleId(historyDetail.getPreviousRoleId());
		log.setState(historyDetail.getState());
		log.setType(historyDetail.getType());
		log.setRoleId(historyDetail.getRoleId());
		log.setUserId(historyDetail.getUserId());
		log.setWorkflowLogId(historyDetail.getProcessInstanceHistoryId());
		
		return log;
	}

}
