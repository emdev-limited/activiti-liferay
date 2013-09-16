package net.emforge.activiti.comparator;

import com.liferay.portal.kernel.workflow.comparator.BaseWorkflowTaskCreateDateComparator;

public class WorkflowTaskCreateDateComparator extends BaseWorkflowTaskCreateDateComparator {
	private static final long serialVersionUID = 3400556510629660604L;

	public WorkflowTaskCreateDateComparator() {
		super();
	}

	public WorkflowTaskCreateDateComparator(boolean ascending) {
		super(ascending);
	}
}
