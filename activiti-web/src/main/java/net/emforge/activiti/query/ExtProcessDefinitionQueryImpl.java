package net.emforge.activiti.query;

import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.ProcessDefinitionQueryImpl;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.persistence.entity.SuspensionState.SuspensionStateImpl;
import org.activiti.engine.repository.ProcessDefinition;

@SuppressWarnings("serial")
public class ExtProcessDefinitionQueryImpl extends ProcessDefinitionQueryImpl implements ExtProcessDefinitionQuery {
	
	private String companyId;
	private Integer isActiv;
	
	public ExtProcessDefinitionQueryImpl() {
		super();
	}
	
	public ExtProcessDefinitionQueryImpl(CommandContext commandContext) {
		super(commandContext);
	}
	
	public ExtProcessDefinitionQueryImpl(CommandExecutor commandExecutor) {
		super(commandExecutor);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<ProcessDefinition> executeList(CommandContext commandContext, Page page) {
		return commandContext.getDbSqlSession().selectList("selectProcessDefinitionByExtQueryCriteria", this, page);
	}
	
	@Override
	public long executeCount(CommandContext commandContext) {
		return (Long) commandContext.getDbSqlSession().selectOne("selectProcessDefinitionCountByExtQueryCriteria", this);
	}
	
	@Override
	public ExtProcessDefinitionQuery processDefinitionCompanyIdAndName(long companyId, String name, Boolean isActiv) {
		if(companyId <= 0) {
			throw new ActivitiException("Company id less then 0");
		}
		this.name = name;
		this.companyId = String.valueOf(companyId);
		if (isActiv != null) {
			if (isActiv) {
				this.suspensionState = SuspensionStateImpl.ACTIVE;
			} else {
				this.suspensionState = SuspensionStateImpl.SUSPENDED;
			}
		}
		return this;
	}
	
	public ExtProcessDefinitionQuery companyId(String companyId) {
	    if (companyId == null) {
	      throw new ActivitiException("Company id is null");
	    }
	    this.companyId = companyId;
	    return this;
	  }
}
