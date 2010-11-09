package net.emforge.activiti.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "ACT_WORKFLOW_LOG_LIFERAY")
public class ProcessInstanceHistory {
	
	public long getProcessInstanceHistoryId() {
		return processInstanceHistoryId;
	}
	public void setProcessInstanceHistoryId(long processInstanceHistoryId) {
		this.processInstanceHistoryId = processInstanceHistoryId;
	}
	
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	public long getPreviousRoleId() {
		return previousRoleId;
	}
	public void setPreviousRoleId(long previousRoleId) {
		this.previousRoleId = previousRoleId;
	}
	public String getPreviousState() {
		return previousState;
	}
	public void setPreviousState(String previousState) {
		this.previousState = previousState;
	}
	public long getPreviousUserId() {
		return previousUserId;
	}
	public void setPreviousUserId(long previousUserId) {
		this.previousUserId = previousUserId;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	public long getRoleId() {
		return roleId;
	}
	public void setRoleId(long roleId) {
		this.roleId = roleId;
	}
	public long getWorkflowInstanceId() {
		return workflowInstanceId;
	}
	public void setWorkflowInstanceId(long workflowInstanceId) {
		this.workflowInstanceId = workflowInstanceId;
	}
	
	@Id
    @Column
    @GeneratedValue(strategy = GenerationType.AUTO)
	private long processInstanceHistoryId;
	
	@Column(length = 1024)
	private String comment;
	@Column(nullable=false)
	private Date createDate;
	@Column
	private long previousRoleId;
	@Column
	private String previousState;
	@Column
	private long previousUserId;
	@Column
	private String state;
	@Column
	private int type;
	@Column
	private long userId;
	@Column
	private long roleId;
	@Column
	private long workflowInstanceId;
	
}
