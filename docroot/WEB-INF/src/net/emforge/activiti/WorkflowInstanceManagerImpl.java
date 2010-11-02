package net.emforge.activiti;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.workflow.WorkflowException;
import com.liferay.portal.kernel.workflow.WorkflowInstance;
import com.liferay.portal.kernel.workflow.WorkflowInstanceManager;

@Service(value="workflowInstanceManager")
public class WorkflowInstanceManagerImpl implements WorkflowInstanceManager {

	@Override
	public void deleteWorkflowInstance(long companyId, long workflowInstanceId)
			throws WorkflowException {
		// TODO Auto-generated method stub

	}

	@Override
	public List<String> getNextTransitionNames(long companyId, long userId,
			long workflowInstanceId) throws WorkflowException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WorkflowInstance getWorkflowInstance(long companyId,
			long workflowInstanceId) throws WorkflowException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getWorkflowInstanceCount(long companyId, Long userId,
			String assetClassName, Long assetClassPK, Boolean completed)
			throws WorkflowException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getWorkflowInstanceCount(long companyId,
			String workflowDefinitionName, Integer workflowDefinitionVersion,
			Boolean completed) throws WorkflowException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<WorkflowInstance> getWorkflowInstances(long companyId,
			Long userId, String assetClassName, Long assetClassPK,
			Boolean completed, int start, int end,
			OrderByComparator orderByComparator) throws WorkflowException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<WorkflowInstance> getWorkflowInstances(long companyId,
			String workflowDefinitionName, Integer workflowDefinitionVersion,
			Boolean completed, int start, int end,
			OrderByComparator orderByComparator) throws WorkflowException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WorkflowInstance signalWorkflowInstance(long companyId, long userId,
			long workflowInstanceId, String transitionName,
			Map<String, Serializable> workflowContext) throws WorkflowException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WorkflowInstance startWorkflowInstance(long companyId, long groupId,
			long userId, String workflowDefinitionName,
			Integer workflowDefinitionVersion, String transitionName,
			Map<String, Serializable> workflowContext) throws WorkflowException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WorkflowInstance updateWorkflowContext(long companyId,
			long workflowInstanceId, Map<String, Serializable> workflowContext)
			throws WorkflowException {
		// TODO Auto-generated method stub
		return null;
	}

}
