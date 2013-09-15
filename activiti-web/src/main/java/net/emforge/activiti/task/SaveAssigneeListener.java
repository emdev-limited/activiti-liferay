package net.emforge.activiti.task;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;

public class SaveAssigneeListener implements TaskListener 
{
	
	public static final String NAME_PREV_ACTOR = "prevUserTaskActor";
	
	@Override
	public void notify(DelegateTask task) {
		String currentAssignee = task.getAssignee();
		task.getExecution().setVariable(NAME_PREV_ACTOR, currentAssignee);
	}

}
