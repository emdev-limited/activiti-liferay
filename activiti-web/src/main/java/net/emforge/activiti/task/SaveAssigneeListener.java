package net.emforge.activiti.task;

import org.activiti.engine.delegate.DelegateTask;

public class SaveAssigneeListener {

	public static final String NAME_PREV_ACTOR = "prevUserTaskActor";

	public void notify(DelegateTask task) {
		String currentAssignee = task.getAssignee();
		task.getExecution().setVariable(NAME_PREV_ACTOR, currentAssignee);
	}

}
