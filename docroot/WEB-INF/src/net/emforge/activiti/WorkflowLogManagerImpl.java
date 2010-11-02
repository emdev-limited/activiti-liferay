package net.emforge.activiti;

import java.util.List;

import org.springframework.stereotype.Service;

import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.workflow.WorkflowException;
import com.liferay.portal.kernel.workflow.WorkflowLog;
import com.liferay.portal.kernel.workflow.WorkflowLogManager;

@Service(value="workflowLogManager")
public class WorkflowLogManagerImpl implements WorkflowLogManager {

	@Override
	public int getWorkflowLogCountByWorkflowInstance(long companyId,
			long workflowInstanceId, List<Integer> logTypes)
			throws WorkflowException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getWorkflowLogCountByWorkflowTask(long companyId,
			long workflowTaskId, List<Integer> logTypes)
			throws WorkflowException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<WorkflowLog> getWorkflowLogsByWorkflowInstance(long companyId,
			long workflowInstanceId, List<Integer> logTypes, int start,
			int end, OrderByComparator orderByComparator)
			throws WorkflowException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<WorkflowLog> getWorkflowLogsByWorkflowTask(long companyId,
			long workflowTaskId, List<Integer> logTypes, int start, int end,
			OrderByComparator orderByComparator) throws WorkflowException {
		// TODO Auto-generated method stub
		return null;
	}

}
