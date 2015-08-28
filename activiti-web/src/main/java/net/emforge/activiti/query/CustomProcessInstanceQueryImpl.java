package net.emforge.activiti.query;

import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.ProcessInstanceQueryImpl;
import org.activiti.engine.impl.RuntimeServiceImpl;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liferay.portal.kernel.workflow.WorkflowException;

/**
 * Implements additional queries to integrate Activiti with Liferay
 * 
 * @author Dmitry Farafonov
 */
public class CustomProcessInstanceQueryImpl extends ProcessInstanceQueryImpl implements CustomProcessInstanceQuery, CustomCommonProcessInstanceQuery<ProcessInstanceQuery>{
	private static final long serialVersionUID = 1L;

	static Logger logger = LoggerFactory.getLogger(CustomProcessInstanceQueryImpl.class);
	
	protected Integer processDefinitionVersion;
	protected boolean processDefinitionLatestVersion = false;
	protected Long userId;
	protected Long entryClassNameId;
	protected List<Long> entryClassNameIds;
	protected Long entryClassPK;

	public CustomProcessInstanceQueryImpl() {
	}
	
	public CustomProcessInstanceQueryImpl(CommandExecutor commandExecutor) {
		super(commandExecutor);
	}

	public static CustomProcessInstanceQuery create() throws WorkflowException {
		ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
		if (processEngine == null) {
			throw new WorkflowException("Process Engine is undefined");
		}
		CommandExecutor commandExecutor = ((RuntimeServiceImpl) processEngine.getRuntimeService()).getCommandExecutor();
//		String databaseType = processEngine.getProcessEngineConfiguration().getDatabaseType();

		return new CustomProcessInstanceQueryImpl(commandExecutor);
	}
	
	@Override
	public ProcessInstanceQuery processDefinitionVersion(Integer processDefinitionVersion) {
		if (processDefinitionName == null) {
			return this;
		}

		if (inOrStatement) {
			throw new ActivitiIllegalArgumentException("This method is not supported in an OR statement");
		} else {
			this.processDefinitionVersion = processDefinitionVersion;
		}
		return this;
	}

	@Override
	public ProcessInstanceQuery processDefinitionLatestVersion() {
		this.processDefinitionLatestVersion = true;
		return this;
	}

	@Override
	public ProcessInstanceQuery userId(Long userId) {
		this.userId = userId;
		return this;
	}
	
	@Override
	public ProcessInstanceQuery entryClassNameId(Long entryClassNameId) {
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
	public ProcessInstanceQuery entryClassNameIds(List<Long> entryClassNameIds) {
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
	public ProcessInstanceQuery entryClassPK(Long entryClassPK) {
		this.entryClassPK = entryClassPK;
		return this;
	}

	public ProcessInstanceQuery or() {
		if (inOrStatement) {
			throw new ActivitiException("the query is already in an or statement");
		}

		inOrStatement = true;
		currentOrQueryObject = new CustomProcessInstanceQueryImpl();
		orQueryObjects.add(currentOrQueryObject);
		return this;
	}

	//results ////////////////////////////////////////////////////////////////

	public long executeCount(CommandContext commandContext) {
		checkQueryOk();
		ensureVariablesInitialized();
		return (Long) commandContext.getDbSqlSession().selectOne("customSelectProcessInstanceCountByQueryCriteria", this);
	}

	@SuppressWarnings("unchecked")
	public List<ProcessInstance> executeList(CommandContext commandContext, Page page) {
		checkQueryOk();
		ensureVariablesInitialized();
		if (includeProcessVariables) {
			return commandContext.getDbSqlSession().selectList("customSelectProcessInstanceWithVariablesByQueryCriteria", this, page);	
		} else {
			return commandContext.getDbSqlSession().selectList("customSelectProcessInstanceByQueryCriteria", this, page);
		}
	}

	//getters ////////////////////////////////////////////////////////////////

	public Integer getProcessDefinitionVersion() {
		return processDefinitionVersion;
	}

	public boolean isProcessDefinitionLatestVersion() {
		return processDefinitionLatestVersion;
	}

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
