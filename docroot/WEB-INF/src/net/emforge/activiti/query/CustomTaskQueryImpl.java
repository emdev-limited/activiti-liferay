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

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

/** Implements additional queries we need to Activiti <-> Liferay integration
 * 
 * @author akakunin
 *
 */
public class CustomTaskQueryImpl extends AbstractQuery<CustomTaskQuery, Task> implements CustomTaskQuery {
	
	private static Log _log = LogFactoryUtil.getLog(CustomTaskQueryImpl.class);

	protected String candidateUser;
	protected String assignee;
	protected String nameLike;
	protected String entryClassName;
	
	protected boolean orderByDueDate;
	
	public CustomTaskQueryImpl(CommandContext commandContext) {
		super(commandContext);
	}

	public CustomTaskQueryImpl(CommandExecutor commandExecutor) {
		super(commandExecutor);
	}
	
	public CustomTaskQuery taskCandidateUser(String userId) {
		this.candidateUser = userId;
		return this;
	}
	
	public CustomTaskQuery taskNameLike(String taskName) {
		this.nameLike = taskName; 
		return this;
	}
	
	public CustomTaskQuery taskEntryClassName(String entryClassName) {
		this.entryClassName = entryClassName;
		return this;
	}
	
	public CustomTaskQuery taskAssignee(String assignee) {
		this.assignee = assignee;
		return this;
	}
	
	public CustomTaskQuery orderByDueDate() {
		orderByDueDate = true;
		return orderBy(new TaskQueryProperty("T.DUE_DATE_"));
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
	    List<Group> groups = Context.getCommandContext().getGroupManager().findGroupsByUser(candidateUser);
	    List<String> groupIds = new ArrayList<String>();
	    
	    for (Group group : groups) {
	    	groupIds.add(group.getId());
	    	_log.debug("Candidate role added: " + group.getId());
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
	
	public String getEntryClassName() {
		return entryClassName;
	}
	
	public boolean isOrderByDueDate() {
		return orderByDueDate;
	}
}
