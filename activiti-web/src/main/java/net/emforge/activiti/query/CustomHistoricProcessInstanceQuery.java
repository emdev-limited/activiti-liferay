package net.emforge.activiti.query;

import java.util.List;

import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;

/**
 * Allows programmatic querying of {@link HistoricProcessInstance}s.
 * 
 * @author Dmitry Farafonov
 */
public interface CustomHistoricProcessInstanceQuery extends HistoricProcessInstanceQuery {
	
	public HistoricProcessInstanceQuery userId(Long userId);

	public HistoricProcessInstanceQuery entryClassNameId(Long entryClassNameId);

	public HistoricProcessInstanceQuery entryClassNameIds(List<Long> entryClassNameIds);

	public HistoricProcessInstanceQuery entryClassPK(Long entryClassPK);
}
