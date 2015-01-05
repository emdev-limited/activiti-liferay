package net.emforge.activiti.query;

import java.util.List;

/**
 * Common interface for CustomTaskQuery and CustomHistoricTaskInstanceQuery
 * 
 * @author Dmitry Farafonov
 */
public interface CustomTaskInfoQuery<T> {
	T taskEntryClassPK(String entryClassPK);
	T taskEntryClassPKs(List<Long> entryClassPKs);
	T taskEntryClassName(String entryClassName);
	T taskEntryClassNames(List<String> entryClassNames);

	T taskGroupId(Long groupId);

	T taskVariableValueIn(String variableName, List valueList);
	T processVariableValueIn(String variableName, List valueList);
}
