package net.emforge.activiti.comparator;

import com.liferay.portal.kernel.workflow.comparator.WorkflowTaskDueDateComparator;

/** database sorting for task due date is not supported - so, only java sorting used
 * 
 * @author akakunin
 *
 */
public class ActivitiWorkflowTaskDueDateDateComparator extends WorkflowTaskDueDateComparator {
	private static final long serialVersionUID = 728556944092844288L;

	public ActivitiWorkflowTaskDueDateDateComparator(boolean ascending) {
		super(ascending, null, null, null); // TODO - check - probably need to use something else instead of nulls
	}
}
