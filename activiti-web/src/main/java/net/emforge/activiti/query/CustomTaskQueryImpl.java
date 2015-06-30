package net.emforge.activiti.query;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.TaskQueryImpl;
import org.activiti.engine.impl.TaskServiceImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.variable.VariableTypes;
import org.activiti.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements additional queries we need to Activiti <-> Liferay integration
 * 
 * @author akakunin
 * @author Dmitry Farafonov
 */
public class CustomTaskQueryImpl extends TaskQueryImpl implements CustomTaskQuery, CustomTaskInfoQuery<CustomTaskQuery>{

	private static final long serialVersionUID = 1L;

	static Logger logger = LoggerFactory.getLogger(CustomTaskQueryImpl.class);

	protected String entryClassPK;
	protected List<Long> entryClassPKs;
	protected String entryClassName;
	protected List<String> entryClassNames;
	protected Long groupId;

	protected String variableInName;
	protected List<String> variableInValues;

	protected List<QueryVariableValueIn> queryVariableValuesIn = new ArrayList<QueryVariableValueIn>();

	protected boolean orderByDueDate;
	protected boolean orderByCreateDate;


	public CustomTaskQueryImpl(CommandExecutor commandExecutor,
			String databaseType) {
		super(commandExecutor, databaseType);
	}

	public static CustomTaskQueryImpl create() {
		/*CommandExecutor commandExecutor = ((TaskServiceImpl)taskService).getCommandExecutor(); 
		String databaseType = processEngine.getProcessEngineConfiguration().getDatabaseType();*/

		CommandExecutor commandExecutor = ((TaskServiceImpl)getProcessEngine().getTaskService()).getCommandExecutor(); 
		String databaseType = getProcessEngine().getProcessEngineConfiguration().getDatabaseType();

		return new CustomTaskQueryImpl(commandExecutor, databaseType);
	}

	private static ProcessEngine getProcessEngine() {
		ProcessEngine defaultProcessEngine = ProcessEngines.getDefaultProcessEngine();
		return defaultProcessEngine;
	}

	public CustomTaskQuery taskEntryClassPK(String entryClassPK) {
		this.entryClassPK = entryClassPK;
		return this;
	}

	public CustomTaskQuery taskEntryClassPKs(List<Long> entryClassPKs) {
		this.entryClassPKs = entryClassPKs;
		return this;
	}

	public CustomTaskQuery taskEntryClassName(String entryClassName) {
		this.entryClassName = entryClassName;
		return this;
	}

	public CustomTaskQuery taskEntryClassNames(List<String> entryClassNames) {
		this.entryClassNames = entryClassNames;
		return this;
	}

	public CustomTaskQuery taskGroupId(Long groupId) {
		this.groupId = groupId;
		return this;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public CustomTaskQuery taskVariableValueIn(String name, List value) {
		/**
		 * TODO: implement logic for "localScope=true|false" in CustomTask.xml mapping.
		 */
		boolean localScope = true; 

		queryVariableValuesIn.add(new QueryVariableValueIn(name, value, localScope));
		return (CustomTaskQuery) this;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public CustomTaskQuery processVariableValueIn(String name, List value) {
		boolean localScope = false;

		queryVariableValuesIn.add(new QueryVariableValueIn(name, value, localScope));
		return (CustomTaskQuery) this;
	}

	//results ////////////////////////////////////////////////////////////////

	@Override
	public long executeCount(CommandContext commandContext) {
		ensureVariablesInitialized();
		ensureVariablesInInitialized();
		checkQueryOk();
		return (Long) commandContext.getDbSqlSession().selectOne("customSelectTaskCountByQueryCriteria", this);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Task> executeList(CommandContext commandContext, Page page) {
		ensureVariablesInitialized();
		ensureVariablesInInitialized();
		checkQueryOk();

		if (includeTaskLocalVariables || includeProcessVariables) {
			return commandContext.getDbSqlSession().selectList("customSelectTaskWithVariablesByQueryCriteria", this, page);
		} else {
			return commandContext.getDbSqlSession().selectList("customSelectTaskByQueryCriteria", this, page);
		}
	}

	protected void ensureVariablesInInitialized() {
		VariableTypes types = Context.getProcessEngineConfiguration().getVariableTypes();
		for (QueryVariableValueIn queryVariableValue : queryVariableValuesIn) {
			queryVariableValue.initialize(types);
		}
	}

	public String getEntryClassPK() {
		return entryClassPK;
	}

	public List<Long> getEntryClassPKs() {
		return entryClassPKs;
	}

	public String getEntryClassName() {
		return entryClassName;
	}

	public List<String> getEntryClassNames() {
		return entryClassNames;
	}

	public Long getGroupId() {
		return groupId;
	}

	public boolean isOrderByDueDate() {
		return orderByDueDate;
	}

	public boolean isOrderByCreateDate() {
		return orderByCreateDate;
	}
}
