package net.emforge.activiti.query;

import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.impl.HistoricProcessInstanceQueryImpl;
import org.activiti.engine.impl.HistoryServiceImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liferay.portal.kernel.workflow.WorkflowException;

/**
 * Implements additional queries to integrate Activiti with Liferay
 * 
 * @author Dmitry Farafonov
 */
public class CustomHistoricProcessInstanceQueryImpl extends HistoricProcessInstanceQueryImpl implements CustomHistoricProcessInstanceQuery, CustomCommonProcessInstanceQuery<HistoricProcessInstanceQuery> {
	private static final long serialVersionUID = 1L;

	static Logger logger = LoggerFactory.getLogger(CustomHistoricProcessInstanceQueryImpl.class);

	protected Long userId;
	protected Long entryClassNameId;
	protected List<Long> entryClassNameIds;
	protected Long entryClassPK;

	public CustomHistoricProcessInstanceQueryImpl() {
	}

	public CustomHistoricProcessInstanceQueryImpl(CommandExecutor commandExecutor) {
		super(commandExecutor);
	}

	public static CustomHistoricProcessInstanceQuery create() throws WorkflowException {
		ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
		if (processEngine == null) {
			throw new WorkflowException("Process Engine is undefined");
		}
		CommandExecutor commandExecutor = ((HistoryServiceImpl) processEngine.getHistoryService()).getCommandExecutor();
		//		String databaseType = processEngine.getProcessEngineConfiguration().getDatabaseType();

		return new CustomHistoricProcessInstanceQueryImpl(commandExecutor);
	}

	@Override
	public HistoricProcessInstanceQuery userId(Long userId) {
		this.userId = userId;
		return this;
	}

	@Override
	public HistoricProcessInstanceQuery entryClassNameId(Long entryClassNameId) {
		if (entryClassNameId == null) {
			throw new ActivitiIllegalArgumentException("Entry class name id is null");
		}
		if (this.entryClassNameIds != null) {
			throw new ActivitiException("Single entry class name id is not allowed when set of entry class name ids is already defined");
		}
		this.entryClassNameId = entryClassNameId;
		return this;
	}

	@Override
	public HistoricProcessInstanceQuery entryClassNameIds(List<Long> entryClassNameIds) {
		if (entryClassNameIds == null) {
			throw new ActivitiIllegalArgumentException("Set of entry class name ids is null");
		}
		if (this.entryClassNameId != null) {
			throw new ActivitiException("Set of entry class name ids is not allowed when single entry class name id is already defined");
		}
		this.entryClassNameIds = entryClassNameIds;
		return this;
	}

	@Override
	public HistoricProcessInstanceQuery entryClassPK(Long entryClassPK) {
		this.entryClassPK = entryClassPK;
		return this;
	}

	@Override
	public HistoricProcessInstanceQuery or() {
		if (inOrStatement) {
			throw new ActivitiException("the query is already in an or statement");
		}

		inOrStatement = true;
		currentOrQueryObject = new CustomHistoricProcessInstanceQueryImpl();
		orQueryObjects.add(currentOrQueryObject);
		return this;
	}

	//results ////////////////////////////////////////////////////////////////

	public long executeCount(CommandContext commandContext) {
		checkQueryOk();
		ensureVariablesInitialized();
		return (Long) commandContext.getDbSqlSession().selectOne("customSelectHistoricProcessInstanceCountByQueryCriteria", this);
	}

	@SuppressWarnings("unchecked")
	public List<HistoricProcessInstance> executeList(CommandContext commandContext, Page page) {
		checkQueryOk();
		ensureVariablesInitialized();
		if (includeProcessVariables) {
			return commandContext.getDbSqlSession().selectList("customSelectHistoricProcessInstancesWithVariablesByQueryCriteria", this, page);	
		} else {
			return commandContext.getDbSqlSession().selectList("customSelectHistoricProcessInstancesByQueryCriteria", this, page);
		}
	}

	//getters ////////////////////////////////////////////////////////////////

	public Long getUserId() {
		return userId;
	}

	public Long getEntryClassNameId() {
		return entryClassNameId;
	}

	public List<Long> getEntryClassNameIds() {
		return entryClassNameIds;
	}

	public Long getEntryClassPK() {
		return entryClassPK;
	}
}
