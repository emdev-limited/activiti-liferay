package net.emforge.activiti.query;

import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.query.Query;

public interface CustomHistoricActivityInstanceQuery extends Query<CustomHistoricActivityInstanceQuery, HistoricActivityInstance> {
	CustomHistoricActivityInstanceQuery taskAssignee(String userId);
	
	CustomHistoricActivityInstanceQuery taskNameLike(String taskName);
	CustomHistoricActivityInstanceQuery taskEntryClassName(String entryClassName);
}
