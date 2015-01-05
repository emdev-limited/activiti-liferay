package net.emforge.activiti.query;

import java.util.List;

import org.activiti.engine.runtime.ProcessInstanceQuery;

/**
 * 
 * @author Dmitry Farafonov
 */
public interface CustomProcessInstanceQuery extends ProcessInstanceQuery {
	ProcessInstanceQuery processDefinitionVersion(Integer processDefinitionVersion);

	ProcessInstanceQuery processDefinitionLatestVersion();
	
	ProcessInstanceQuery userId(Long userId);

	ProcessInstanceQuery entryClassNameId(Long entryClassNameIds);

	ProcessInstanceQuery entryClassNameIds(List<Long> entryClassNameIds);

	ProcessInstanceQuery entryClassPK(Long entryClassPK);
}
