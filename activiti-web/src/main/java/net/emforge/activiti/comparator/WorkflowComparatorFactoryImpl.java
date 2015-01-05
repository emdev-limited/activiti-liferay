package net.emforge.activiti.comparator;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.workflow.comparator.WorkflowComparatorFactory;
import com.liferay.portal.kernel.workflow.comparator.WorkflowDefinitionNameComparator;
import com.liferay.portal.kernel.workflow.comparator.WorkflowTaskCompletionDateComparator;
import com.liferay.portal.kernel.workflow.comparator.WorkflowTaskCreateDateComparator;
import com.liferay.portal.kernel.workflow.comparator.WorkflowTaskDueDateComparator;
import com.liferay.portal.kernel.workflow.comparator.WorkflowTaskNameComparator;
import com.liferay.portal.kernel.workflow.comparator.WorkflowTaskUserIdComparator;

/** Implementation of workflowComparatorFactory interface
 * 
 * @author akakunin
 * @author Dmitry Farafonov
 *
 */
public class WorkflowComparatorFactoryImpl implements WorkflowComparatorFactory {
	private static Log _log = LogFactoryUtil.getLog(WorkflowComparatorFactoryImpl.class);

	private static final String _ORDER_BY_ASC = " ASC";
	private static final String _ORDER_BY_DESC = " DESC";
	
	@Override
	public OrderByComparator getDefinitionNameComparator(boolean ascending) {
		String fieldName = "ProcessDefinitionName";
		String orderByAsc = fieldName  + _ORDER_BY_ASC;
		String orderByDesc = fieldName + _ORDER_BY_DESC;
		String[] orderByFields = {fieldName};
		return new WorkflowDefinitionNameComparator(ascending, orderByAsc, orderByDesc, orderByFields);
	}

	@Override
	public OrderByComparator getInstanceEndDateComparator(boolean ascending) {
		_log.error("Method is not implemented"); // TODO
		return null;
	}

	@Override
	public OrderByComparator getInstanceStartDateComparator(boolean ascending) {
		// NOTES: to order by any column this column must be present in select query statement
		/*String fieldName = "CreateDate";
		String orderByAsc = fieldName  + _ORDER_BY_ASC;
		String orderByDesc = fieldName + _ORDER_BY_DESC;
		String[] orderByFields = {fieldName};
		return new WorkflowInstanceStartDateComparator(ascending, orderByAsc, orderByDesc, orderByFields);*/
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
		return new ActivitiWorkflowLogCreateDateComparator(ascending);
	}

	@Override
	public OrderByComparator getLogUserIdComparator(boolean ascending) {
		_log.error("Method is not implemented"); // TODO
		return null;
	}

	@Override
	public OrderByComparator getTaskCompletionDateComparator(boolean ascending) {
		String fieldName = "HistoricTaskInstanceEndTime";
		String orderByAsc = fieldName  + _ORDER_BY_ASC;
		String orderByDesc = fieldName + _ORDER_BY_DESC;
		String[] orderByFields = {fieldName};
		return new WorkflowTaskCompletionDateComparator(ascending, orderByAsc, orderByDesc, orderByFields);
	}

	@Override
	public OrderByComparator getTaskCreateDateComparator(boolean ascending) {
		String fieldName = "CreateDate";
		String orderByAsc = fieldName  + _ORDER_BY_ASC;
		String orderByDesc = fieldName + _ORDER_BY_DESC;
		String[] orderByFields = {fieldName};
		return new WorkflowTaskCreateDateComparator(ascending, orderByAsc, orderByDesc, orderByFields);
	}

	@Override
	public OrderByComparator getTaskDueDateComparator(boolean ascending) {
		// FIXME: OBC - due date nulls first or last?
		String fieldDueDate = "DueDateNullsFirst";
		String fieldTaskId = "TaskId";
		String orderByAsc = fieldDueDate  + _ORDER_BY_ASC + ", " + fieldTaskId + _ORDER_BY_ASC;
		String orderByDesc = fieldDueDate + _ORDER_BY_DESC + ", " + fieldTaskId + _ORDER_BY_DESC;
		String[] orderByFields = {fieldDueDate, fieldTaskId};
		return new WorkflowTaskDueDateComparator(ascending, orderByAsc, orderByDesc, orderByFields);
	}

	@Override
	public OrderByComparator getTaskNameComparator(boolean ascending) {
		String fieldName = "TaskName";
		String orderByAsc = fieldName  + _ORDER_BY_ASC;
		String orderByDesc = fieldName + _ORDER_BY_DESC;
		String[] orderByFields = {fieldName};
		return new WorkflowTaskNameComparator(ascending, orderByAsc, orderByDesc, orderByFields);
	}

	@Override
	public OrderByComparator getTaskUserIdComparator(boolean ascending) {
		String fieldName = "TaskAssignee";
		String orderByAsc = fieldName  + _ORDER_BY_ASC;
		String orderByDesc = fieldName + _ORDER_BY_DESC;
		String[] orderByFields = {fieldName};
		return new WorkflowTaskUserIdComparator(ascending, orderByAsc, orderByDesc, orderByFields);
	}

}
