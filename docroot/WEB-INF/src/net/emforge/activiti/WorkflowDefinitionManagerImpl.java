package net.emforge.activiti;

import java.io.InputStream;
import java.util.List;

import org.springframework.stereotype.Service;

import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.workflow.WorkflowDefinition;
import com.liferay.portal.kernel.workflow.WorkflowDefinitionManager;
import com.liferay.portal.kernel.workflow.WorkflowException;

@Service(value="workflowDefinitionManager")
public class WorkflowDefinitionManagerImpl implements WorkflowDefinitionManager {

	@Override
	public WorkflowDefinition deployWorkflowDefinition(long companyId,
			long userId, String title, InputStream inputStream)
			throws WorkflowException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getActiveWorkflowDefinitionCount(long companyId)
			throws WorkflowException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getActiveWorkflowDefinitionCount(long companyId, String name)
			throws WorkflowException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<WorkflowDefinition> getActiveWorkflowDefinitions(
			long companyId, int start, int end,
			OrderByComparator orderByComparator) throws WorkflowException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<WorkflowDefinition> getActiveWorkflowDefinitions(
			long companyId, String name, int start, int end,
			OrderByComparator orderByComparator) throws WorkflowException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WorkflowDefinition getWorkflowDefinition(long companyId,
			String name, int version) throws WorkflowException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getWorkflowDefinitionCount(long companyId)
			throws WorkflowException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getWorkflowDefinitionCount(long companyId, String name)
			throws WorkflowException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<WorkflowDefinition> getWorkflowDefinitions(long companyId,
			int start, int end, OrderByComparator orderByComparator)
			throws WorkflowException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<WorkflowDefinition> getWorkflowDefinitions(long companyId,
			String name, int start, int end, OrderByComparator orderByComparator)
			throws WorkflowException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void undeployWorkflowDefinition(long companyId, long userId,
			String name, int version) throws WorkflowException {
		// TODO Auto-generated method stub

	}

	@Override
	public WorkflowDefinition updateActive(long companyId, long userId,
			String name, int version, boolean active) throws WorkflowException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WorkflowDefinition updateTitle(long companyId, long userId,
			String name, int version, String title) throws WorkflowException {
		// TODO Auto-generated method stub
		return null;
	}

}
