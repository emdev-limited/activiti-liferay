package net.emforge.activiti.query;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.identity.Group;
import org.activiti.engine.impl.AbstractQuery;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.TaskQueryProperty;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.task.Task;

/** Implements additional queries we need to Activiti <-> Liferay integration
 * 
 * @author akakunin
 *
 */
public class CustomTaskQueryImpl extends AbstractQuery<CustomTaskQuery, Task> implements CustomTaskQuery {

	protected String candidateUser;
	protected String assignee;
	protected String nameLike;
	protected String entryClassPK;
	protected List<Long> entryClassPKs;
	protected String entryClassName;
	protected List<String> entryClassNames;
	protected Long groupId;
	protected Long companyId;
	
	protected boolean orderByDueDate;
	
	public CustomTaskQueryImpl(CommandContext commandContext) {
		super(commandContext);
	}

	public CustomTaskQueryImpl(CommandExecutor commandExecutor) {
		super(commandExecutor);
	}
	
	public CustomTaskQuery taskCandidateUser(String userId) {
		try {
			if (Long.parseLong(userId) <= 0) 
				return this;
		} catch (Exception e) {
			return this;
		}
		this.candidateUser = userId;
		return this;
	}
	
	public CustomTaskQuery taskNameLike(String taskName) {
		this.nameLike = taskName; 
		return this;
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
	
	public CustomTaskQuery taskAssignee(String assignee) {
		try {
			if (Long.parseLong(assignee) <= 0) 
				return this;
		} catch (Exception e) {
			return this;
		}
		this.assignee = assignee;
		return this;
	}
	
	public CustomTaskQuery taskGroupId(Long groupId) {
		this.groupId = groupId;
		return this;
	}
	
	public CustomTaskQuery taskCompanyId(Long companyId) {
		this.companyId = companyId;
		return this;
	}
	
	public CustomTaskQuery orderByDueDate() {
		orderByDueDate = true;
		return orderBy(new TaskQueryProperty("RES.DUE_DATE_"));
	}
	
	@Override
	public long executeCount(CommandContext commandContext) {
		return (Long) commandContext.getDbSqlSession().selectOne("customSearchTasksCount", this);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Task> executeList(CommandContext commandContext, Page page) {
		// TODO Auto-generated method stub
		
		String statement = "customSearchTasks";
		
		return commandContext.getDbSqlSession().selectList(statement, this, page);
	}

	public List<String> getCandidateGroups() {
		if (candidateUser != null) {
			return getGroupsForCandidateUser(candidateUser);
		} else {
			return null;
		}
	}
	
	protected List<String> getGroupsForCandidateUser(String candidateUser) {
	    List<Group> groups = Context.getCommandContext().getGroupEntityManager().findGroupsByUser(candidateUser);
	    List<String> groupIds = new ArrayList<String>();
	    
	    for (Group group : groups) {
	    	groupIds.add(group.getId());
	    }
	    
	    return groupIds;
	}
	
	public String getAssignee() {
		return assignee;
	}
	
	public String getCandidateUser() {
		return candidateUser;
	}
	
	public String getNameLike() {
		return nameLike;
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
	
	public Long getCompanyId() {
		return companyId;
	}
	
	public boolean isOrderByDueDate() {
		return orderByDueDate;
	}
}
