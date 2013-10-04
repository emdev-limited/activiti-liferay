package net.emforge.activiti.comparator;

import com.liferay.portal.kernel.workflow.comparator.WorkflowTaskCreateDateComparator;

public class ActivitiWorkflowTaskCreateDateComparator extends WorkflowTaskCreateDateComparator {
	private static final long serialVersionUID = 3400556510629660604L;

	public ActivitiWorkflowTaskCreateDateComparator(boolean ascending) {
		super(ascending, null, null, null); // TODO - check - probably need to use something else instead of nulls
	}
}
