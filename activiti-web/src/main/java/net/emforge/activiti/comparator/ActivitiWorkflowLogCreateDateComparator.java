package net.emforge.activiti.comparator;

import com.liferay.portal.kernel.workflow.comparator.WorkflowLogCreateDateComparator;


public class ActivitiWorkflowLogCreateDateComparator extends WorkflowLogCreateDateComparator {
	private static final long serialVersionUID = -6768308277581908185L;

	public static String ORDER_BY_ASC = "time_ ASC";
	public static String ORDER_BY_DESC = "time_ DESC";
	public static String[] ORDER_BY_FIELDS = {"time_"};
	
	public ActivitiWorkflowLogCreateDateComparator(boolean ascending) {
		super(ascending, ORDER_BY_ASC, ORDER_BY_DESC, ORDER_BY_FIELDS);
	}
}