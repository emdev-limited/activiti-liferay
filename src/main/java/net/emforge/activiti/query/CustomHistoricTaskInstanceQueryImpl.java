package net.emforge.activiti.query;

import java.util.List;

import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.AbstractQuery;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;

public class CustomHistoricTaskInstanceQueryImpl extends AbstractQuery<CustomHistoricTaskInstanceQuery, HistoricTaskInstance> implements CustomHistoricTaskInstanceQuery {
	protected String assignee;
	protected String nameLike;
	protected String entryClassName;
	protected Long companyId;

	public CustomHistoricTaskInstanceQueryImpl(CommandContext commandContext) {
		super(commandContext);
	}

	public CustomHistoricTaskInstanceQueryImpl(CommandExecutor commandExecutor) {
		super(commandExecutor);
	}
	
	public CustomHistoricTaskInstanceQueryImpl taskNameLike(String taskName) {
		this.nameLike = taskName; 
		return this;
	}
	
	public CustomHistoricTaskInstanceQuery taskEntryClassName(String entryClassName) {
		this.entryClassName = entryClassName;
		return this;
	}
	
	public CustomHistoricTaskInstanceQueryImpl taskAssignee(String assignee) {
		this.assignee = assignee;
		return this;
	}
	
	public CustomHistoricTaskInstanceQueryImpl taskCompanyId(Long companyId) {
		this.companyId = companyId;
		return this;
	}
	
	@Override
	public long executeCount(CommandContext commandContext) {
		return (Long) commandContext.getDbSqlSession().selectOne("customSearchHistoricTaskInstanceCount", this);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<HistoricTaskInstance> executeList(CommandContext commandContext, Page page) {
		String statement = "customSearchHistoricTaskInstance";
		
		return commandContext.getDbSqlSession().selectList(statement, this, page);
	}

	public String getAssignee() {
		return assignee;
	}
	
	public String getNameLike() {
		return nameLike;
	}
	
	public String getEntryClassName() {
		return entryClassName;
	}
	
	public Long getCompanyId() {
		return companyId;
	}
	
}
