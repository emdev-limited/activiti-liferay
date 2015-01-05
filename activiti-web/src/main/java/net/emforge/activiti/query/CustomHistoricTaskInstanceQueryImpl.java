package net.emforge.activiti.query;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.identity.Group;
import org.activiti.engine.impl.HistoricTaskInstanceQueryImpl;
import org.activiti.engine.impl.HistoryServiceImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.variable.VariableTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements additional queries we need to Activiti <-> Liferay integration
 * 
 * @author akakunin
 * @author Dmitry Farafonov
 *
 */
public class CustomHistoricTaskInstanceQueryImpl extends HistoricTaskInstanceQueryImpl implements CustomHistoricTaskInstanceQuery, CustomTaskInfoQuery<CustomHistoricTaskInstanceQuery> {

	private static final long serialVersionUID = 1L;

	static Logger logger = LoggerFactory.getLogger(CustomHistoricTaskInstanceQueryImpl.class);

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


	public CustomHistoricTaskInstanceQueryImpl(CommandExecutor commandExecutor,
			String databaseType) {
		super(commandExecutor, databaseType);
	}

	public static CustomHistoricTaskInstanceQuery create() {
		/*CommandExecutor commandExecutor = ((TaskServiceImpl)taskService).getCommandExecutor(); 
		String databaseType = processEngine.getProcessEngineConfiguration().getDatabaseType();*/

		CommandExecutor commandExecutor = ((HistoryServiceImpl)getProcessEngine().getHistoryService()).getCommandExecutor(); 
		String databaseType = getProcessEngine().getProcessEngineConfiguration().getDatabaseType();

		return new CustomHistoricTaskInstanceQueryImpl(commandExecutor, databaseType);
	}

	private static ProcessEngine getProcessEngine() {
		ProcessEngine defaultProcessEngine = ProcessEngines.getDefaultProcessEngine();
		return defaultProcessEngine;
	}

	public CustomHistoricTaskInstanceQuery taskEntryClassPK(String entryClassPK) {
		this.entryClassPK = entryClassPK;
		return this;
	}

	public CustomHistoricTaskInstanceQuery taskEntryClassPKs(List<Long> entryClassPKs) {
		this.entryClassPKs = entryClassPKs;
		return this;
	}

	public CustomHistoricTaskInstanceQuery taskEntryClassName(String entryClassName) {
		this.entryClassName = entryClassName;
		return this;
	}

	public CustomHistoricTaskInstanceQuery taskEntryClassNames(List<String> entryClassNames) {
		this.entryClassNames = entryClassNames;
		return this;
	}

	public CustomHistoricTaskInstanceQuery taskGroupId(Long groupId) {
		this.groupId = groupId;
		return this;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public CustomHistoricTaskInstanceQuery taskVariableValueIn(String name, List value) {
		/**
		 * TODO: implement logic for "localScope=true|false" in CustomTask.xml mapping.
		 */
		boolean localScope = true; 

		queryVariableValuesIn.add(new QueryVariableValueIn(name, value, localScope));
		return this;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public CustomHistoricTaskInstanceQuery processVariableValueIn(String name, List value) {
		boolean localScope = false;

		queryVariableValuesIn.add(new QueryVariableValueIn(name, value, localScope));
		return this;
	}

	//results ////////////////////////////////////////////////////////////////

	@Override
	public long executeCount(CommandContext commandContext) {
		ensureVariablesInitialized();
		ensureVariablesInInitialized();
		checkQueryOk();
		return (Long) commandContext.getDbSqlSession().selectOne("customSearchHistoricTaskInstanceCount", this);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<HistoricTaskInstance> executeList(CommandContext commandContext, Page page) {
		ensureVariablesInitialized();
		ensureVariablesInInitialized();
		checkQueryOk();

		if (includeTaskLocalVariables || includeProcessVariables) {
			// TODO
			return commandContext.getDbSqlSession().selectList("customSelectHistoricTaskInstancesWithVariablesByQueryCriteria", this, page);
		} else {
			return commandContext.getDbSqlSession().selectList("customSelectHistoricTaskInstancesByQueryCriteria", this, page);
		}
	}

	protected void ensureVariablesInInitialized() {
		VariableTypes types = Context.getProcessEngineConfiguration().getVariableTypes();
		for (QueryVariableValueIn queryVariableValue : queryVariableValuesIn) {
			queryVariableValue.initialize(types);
		}
	}

	//getters ////////////////////////////////////////////////////////////////

	public List<String> getCandidateGroups() {
		if (candidateUser != null) {
			return getGroupsForCandidateUser(candidateUser);
		} else {
			return null;
		}
	}

	protected List<String> getGroupsForCandidateUser(String candidateUser) {
		List<Group> groups = Context.getCommandContext().getGroupIdentityManager().findGroupsByUser(candidateUser);
		List<String> groupIds = new ArrayList<String>();

		for (Group group : groups) {
			groupIds.add(group.getId());
		}

		return groupIds;
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
