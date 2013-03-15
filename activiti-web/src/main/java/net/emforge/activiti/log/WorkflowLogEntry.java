package net.emforge.activiti.log;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
public class WorkflowLogEntry {
	
	public static final String TASK_ASSIGN = "LiferayTaskAssign";
	public static final String TASK_COMPLETION = "LiferayTaskCompletion";
	public static final String TASK_UPDATE = "LiferayTaskUpdate";
	
	private String comment;
	private Date createDate;
	private long previousRoleId;
	private String previousState;
	private long previousUserId;
    private long assigneeUserId;
	private long roleId;
	private String state;
	private int type;
	
	public String getComment() {
		return comment;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public long getPreviousRoleId() {
		return previousRoleId;
	}
	public String getPreviousState() {
		return previousState;
	}
	public long getPreviousUserId() {
		return previousUserId;
	}
	public long getRoleId() {
		return roleId;
	}
	public String getState() {
		return state;
	}
	public int getType() {
		return type;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	public void setPreviousRoleId(long previousRoleId) {
		this.previousRoleId = previousRoleId;
	}
	public void setPreviousState(String previousState) {
		this.previousState = previousState;
	}
	public void setPreviousUserId(long previousUserId) {
		this.previousUserId = previousUserId;
	}
	public void setRoleId(long roleId) {
		this.roleId = roleId;
	}
	public void setState(String state) {
		this.state = state;
	}
	public void setType(int type) {
		this.type = type;
	}

    public long getAssigneeUserId() {
        return assigneeUserId;
    }

    public void setAssigneeUserId(long assigneeUserId) {
        this.assigneeUserId = assigneeUserId;
    }
}
