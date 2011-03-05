package net.emforge.activiti.query;

import java.util.List;

import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.impl.AbstractQuery;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;

public class CustomHistoricActivityInstanceQueryImpl extends AbstractQuery<CustomHistoricActivityInstanceQuery, HistoricActivityInstance> implements CustomHistoricActivityInstanceQuery {
	protected String assignee;
	protected String nameLike;
	protected String entryClassName;

	public CustomHistoricActivityInstanceQueryImpl(CommandContext commandContext) {
		super(commandContext);
	}

	public CustomHistoricActivityInstanceQueryImpl(CommandExecutor commandExecutor) {
		super(commandExecutor);
	}
	
	public CustomHistoricActivityInstanceQuery taskNameLike(String taskName) {
		this.nameLike = taskName; 
		return this;
	}
	
	public CustomHistoricActivityInstanceQuery taskEntryClassName(String entryClassName) {
		this.entryClassName = entryClassName;
		return this;
	}
	
	public CustomHistoricActivityInstanceQuery taskAssignee(String assignee) {
		this.assignee = assignee;
		return this;
	}
	
	@Override
	public long executeCount(CommandContext commandContext) {
		return (Long) commandContext.getDbSqlSession().selectOne("customSearchHistoricActivityInstanceCount", this);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<HistoricActivityInstance> executeList(CommandContext commandContext, Page page) {
		String statement = "customSearchHistoricActivityInstance";
		
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
	
}
