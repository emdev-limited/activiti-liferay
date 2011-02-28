package net.emforge.activiti.comparator;

import com.liferay.portal.kernel.workflow.comparator.BaseWorkflowTaskDueDateComparator;

public class WorkflowTaskDueDateDateComparator extends BaseWorkflowTaskDueDateComparator {
	private static final long serialVersionUID = 728556944092844288L;

	public static String ORDER_BY_ASC = "dueDate_ ASC";
	public static String ORDER_BY_DESC = "dueDate_ DESC";
	public static String[] ORDER_BY_FIELDS = {"dueDate_"};
	
	public WorkflowTaskDueDateDateComparator() {
		super();
	}

	public WorkflowTaskDueDateDateComparator(boolean ascending) {
		super(ascending);
	}

	public String getOrderBy() {
		if (isAscending()) {
			return ORDER_BY_ASC;
		} else {
			return ORDER_BY_DESC;
		}
	}

	public String[] getOrderByFields() {
		return ORDER_BY_FIELDS;
	}

	
}
