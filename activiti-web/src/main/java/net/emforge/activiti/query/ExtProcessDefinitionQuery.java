package net.emforge.activiti.query;

import org.activiti.engine.repository.ProcessDefinitionQuery;

public interface ExtProcessDefinitionQuery extends ProcessDefinitionQuery {

	public ExtProcessDefinitionQuery processDefinitionCompanyIdAndName(long companyId, String name, Boolean isActiv);
}
