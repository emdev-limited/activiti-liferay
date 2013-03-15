package net.emforge.activiti.query;

import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.query.Query;

public interface CustomHistoricTaskInstanceQuery  extends Query<CustomHistoricTaskInstanceQuery, HistoricTaskInstance> {
	CustomHistoricTaskInstanceQuery taskAssignee(String userId);
	
	CustomHistoricTaskInstanceQuery taskNameLike(String taskName);
	CustomHistoricTaskInstanceQuery taskEntryClassName(String entryClassName);
	
	CustomHistoricTaskInstanceQuery taskCompanyId(Long companyId);
}
