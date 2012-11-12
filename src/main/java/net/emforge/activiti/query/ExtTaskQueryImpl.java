package net.emforge.activiti.query;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.TaskQueryImpl;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.task.Task;

@SuppressWarnings("serial")
public class ExtTaskQueryImpl extends TaskQueryImpl implements ExtTaskQuery {
	
	//private Map<String, List<String>> variablesIn2;
	private String variableInName;
	private List<String> variableInValues;
	private boolean dueDateIsNull = false;
	private boolean dueDateIsNotNull = false;
	
	public ExtTaskQueryImpl(CommandContext commandContext) {
		super(commandContext);
	}
	
	public ExtTaskQueryImpl(CommandExecutor commandExecutor) {
		super(commandExecutor);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Task> executeList(CommandContext commandContext, Page page) {
		ensureVariablesInitialized();
		return commandContext.getDbSqlSession().selectList("selectTaskByExtQueryCriteria", this, page);
	}
	
	@Override
	public long executeCount(CommandContext commandContext) {
		ensureVariablesInitialized();
		return (Long) commandContext.getDbSqlSession().selectOne("selectTaskCountByExtQueryCriteria", this);
	}
	
	@Override
	public ExtTaskQuery taskVariableValueIn(String variableName, List<String> valueList) {
		
		if(valueList == null) {
			//throw new ActivitiException("Variable value list is null");
			return this;
		}
		
		List<String> valueListCopy = new ArrayList<String>();
		valueListCopy.addAll(valueList);
		
		if(valueList.size()== 0) {
			valueList.add("");
			//throw new ActivitiException("Variable value list is empty");
		}
		    
		this.variableInName = variableName;
		this.variableInValues = valueList;
		
		return this;
	}
	
	@Override
	public ExtTaskQuery dueDateIsNull() {
		this.dueDateIsNull = true;
		this.dueDateIsNotNull = false;
		return this;
	}
	
	@Override
	public ExtTaskQuery dueDateIsNotNull() {
		this.dueDateIsNotNull = true;
		this.dueDateIsNull = false;
		return this;
	}
	
}
