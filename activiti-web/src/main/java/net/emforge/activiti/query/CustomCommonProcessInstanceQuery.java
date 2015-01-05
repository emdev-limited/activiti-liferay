package net.emforge.activiti.query;

import java.util.List;

import org.activiti.engine.query.Query;

/**
 * Common interface for CustomProcessInstanceQuery and CustomHistoricProcessInstanceQuery
 * 
 * @author Dmitry Farafonov
 */
public interface CustomCommonProcessInstanceQuery<T extends Query<?, ?>> {
	
	T processInstanceTenantId(String tenantId);
	
	T userId(Long userId);

	T entryClassNameId(Long entryClassNameId);

	T entryClassNameIds(List<Long> entryClassNameIds);

	T entryClassPK(Long entryClassPK);
	
	T variableValueEquals(String name, Object value);
}
