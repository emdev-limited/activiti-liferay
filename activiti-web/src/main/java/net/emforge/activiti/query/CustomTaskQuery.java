package net.emforge.activiti.query;

import java.util.List;

import org.activiti.engine.query.Query;
import org.activiti.engine.task.Task;

public interface CustomTaskQuery extends Query<CustomTaskQuery, Task> {
	CustomTaskQuery taskCandidateUser(String userId);
	CustomTaskQuery taskAssignee(String userId);
	
	CustomTaskQuery taskNameLike(String taskName);
	CustomTaskQuery taskEntryClassPK(String entryClassPK);
	CustomTaskQuery taskEntryClassPKs(List<Long> entryClassPKs);
	CustomTaskQuery taskEntryClassName(String entryClassName);
	CustomTaskQuery taskEntryClassNames(List<String> entryClassNames);
	
	CustomTaskQuery taskGroupId(Long groupId);
	CustomTaskQuery taskCompanyId(Long companyId);
	
	CustomTaskQuery orderByDueDate();
}
