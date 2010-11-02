package net.emforge.activiti;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.workflow.WorkflowException;
import com.liferay.portal.kernel.workflow.WorkflowTask;
import com.liferay.portal.kernel.workflow.WorkflowTaskManager;

@Service(value="workflowTaskManager")
public class WorkflowTaskManagerImpl implements WorkflowTaskManager {

	@Override
	public WorkflowTask assignWorkflowTaskToRole(long companyId, long userId,
			long workflowTaskId, long roleId, String comment, Date dueDate,
			Map<String, Serializable> workflowContext) throws WorkflowException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WorkflowTask assignWorkflowTaskToUser(long companyId, long userId,
			long workflowTaskId, long assigneeUserId, String comment,
			Date dueDate, Map<String, Serializable> workflowContext)
			throws WorkflowException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WorkflowTask completeWorkflowTask(long companyId, long userId,
			long workflowTaskId, String transitionName, String comment,
			Map<String, Serializable> workflowContext) throws WorkflowException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getNextTransitionNames(long companyId, long userId,
			long workflowTaskId) throws WorkflowException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long[] getPooledActorsIds(long companyId, long workflowTaskId)
			throws WorkflowException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WorkflowTask getWorkflowTask(long companyId, long workflowTaskId)
			throws WorkflowException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getWorkflowTaskCount(long companyId, Boolean completed)
			throws WorkflowException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getWorkflowTaskCountByRole(long companyId, long roleId,
			Boolean completed) throws WorkflowException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getWorkflowTaskCountBySubmittingUser(long companyId,
			long userId, Boolean completed) throws WorkflowException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getWorkflowTaskCountByUser(long companyId, long userId,
			Boolean completed) throws WorkflowException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getWorkflowTaskCountByUserRoles(long companyId, long userId,
			Boolean completed) throws WorkflowException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getWorkflowTaskCountByWorkflowInstance(long companyId,
			Long userId, long workflowInstanceId, Boolean completed)
			throws WorkflowException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<WorkflowTask> getWorkflowTasks(long companyId,
			Boolean completed, int start, int end,
			OrderByComparator orderByComparator) throws WorkflowException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<WorkflowTask> getWorkflowTasksByRole(long companyId,
			long roleId, Boolean completed, int start, int end,
			OrderByComparator orderByComparator) throws WorkflowException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<WorkflowTask> getWorkflowTasksBySubmittingUser(long companyId,
			long userId, Boolean completed, int start, int end,
			OrderByComparator orderByComparator) throws WorkflowException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<WorkflowTask> getWorkflowTasksByUser(long companyId,
			long userId, Boolean completed, int start, int end,
			OrderByComparator orderByComparator) throws WorkflowException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<WorkflowTask> getWorkflowTasksByUserRoles(long companyId,
			long userId, Boolean completed, int start, int end,
			OrderByComparator orderByComparator) throws WorkflowException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<WorkflowTask> getWorkflowTasksByWorkflowInstance(
			long companyId, Long userId, long workflowInstanceId,
			Boolean completed, int start, int end,
			OrderByComparator orderByComparator) throws WorkflowException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<WorkflowTask> search(long companyId, long userId,
			String keywords, Boolean completed, Boolean searchByUserRoles,
			int start, int end, OrderByComparator orderByComparator)
			throws WorkflowException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<WorkflowTask> search(long companyId, long userId,
			String taskName, String assetType, Date dueDateGT, Date dueDateLT,
			Boolean completed, Boolean searchByUserRoles, boolean andOperator,
			int start, int end, OrderByComparator orderByComparator)
			throws WorkflowException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int searchCount(long companyId, long userId, String keywords,
			Boolean completed, Boolean searchByUserRoles)
			throws WorkflowException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int searchCount(long companyId, long userId, String taskName,
			String assetType, Date dueDateGT, Date dueDateLT,
			Boolean completed, Boolean searchByUserRoles, boolean andOperator)
			throws WorkflowException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public WorkflowTask updateDueDate(long companyId, long userId,
			long workflowTaskId, String comment, Date dueDate)
			throws WorkflowException {
		// TODO Auto-generated method stub
		return null;
	}

}
