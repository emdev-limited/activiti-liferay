package net.emforge.activiti.comparator;

import com.liferay.portal.kernel.workflow.comparator.BaseWorkflowTaskDueDateComparator;

/** database sorting for task due date is not supported - so, only java sorting used
 * 
 * @author akakunin
 *
 */
public class WorkflowTaskDueDateDateComparator extends BaseWorkflowTaskDueDateComparator {
	private static final long serialVersionUID = 728556944092844288L;

	public WorkflowTaskDueDateDateComparator() {
		super();
	}

	public WorkflowTaskDueDateDateComparator(boolean ascending) {
		super(ascending);
	}
}
