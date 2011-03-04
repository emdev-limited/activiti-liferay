package net.emforge.activiti.query;

import org.activiti.engine.query.Query;
import org.activiti.engine.task.Task;

public interface CustomTaskQuery extends Query<CustomTaskQuery, Task> {
	CustomTaskQuery taskCandidateUser(String userId);
	CustomTaskQuery taskAssignee(String userId);
	
	CustomTaskQuery taskNameLike(String taskName);
	CustomTaskQuery taskEntryClassName(String entryClassName);
	
	CustomTaskQuery orderByDueDate();
}
