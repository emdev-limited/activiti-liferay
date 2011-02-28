package net.emforge.activiti.comparator;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.workflow.comparator.WorkflowComparatorFactory;

/** Implementation of workflowComparatorFactory interface
 * 
 * @author akakunin
 *
 */
public class WorkflowComparatorFactoryImpl implements WorkflowComparatorFactory {
	private static Log _log = LogFactoryUtil.getLog(WorkflowComparatorFactoryImpl.class);

	@Override
	public OrderByComparator getDefinitionNameComparator(boolean ascending) {
		_log.error("Method is not implemented"); // TODO
		return null;
	}

	@Override
	public OrderByComparator getInstanceEndDateComparator(boolean ascending) {
		_log.error("Method is not implemented"); // TODO
		return null;
	}

	@Override
	public OrderByComparator getInstanceStartDateComparator(boolean ascending) {
		_log.error("Method is not implemented"); // TODO
		return null;
	}

	@Override
	public OrderByComparator getInstanceStateComparator(boolean ascending) {
		_log.error("Method is not implemented"); // TODO
		return null;
	}

	@Override
	public OrderByComparator getLogCreateDateComparator(boolean ascending) {
		return new WorkflowLogCreateDateComparator(ascending);
	}

	@Override
	public OrderByComparator getLogUserIdComparator(boolean ascending) {
		_log.error("Method is not implemented"); // TODO
		return null;
	}

	@Override
	public OrderByComparator getTaskCompletionDateComparator(boolean ascending) {
		_log.error("Method is not implemented"); // TODO
		return null;
	}

	@Override
	public OrderByComparator getTaskCreateDateComparator(boolean ascending) {
		_log.error("Method is not implemented"); // TODO
		return null;
	}

	@Override
	public OrderByComparator getTaskDueDateComparator(boolean ascending) {
		return new WorkflowTaskDueDateDateComparator(ascending);
	}

	@Override
	public OrderByComparator getTaskNameComparator(boolean ascending) {
		_log.error("Method is not implemented"); // TODO
		return null;
	}

	@Override
	public OrderByComparator getTaskUserIdComparator(boolean ascending) {
		_log.error("Method is not implemented"); // TODO
		return null;
	}

}
