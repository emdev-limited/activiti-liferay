package net.emforge.activiti.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "ACT_PROCESSINSTANCEEXTENSION_LIFERAY")
public class ProcessInstanceExtensionImpl {
	public ProcessInstanceExtensionImpl() {
	}
	
    @Id
    @Column(name = "process_instance_extension_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@Column(name = "company_id", nullable=false)
	public long getCompanyId() {
		return companyId;
	}

	public void setCompanyId(long companyId) {
		this.companyId = companyId;
	}

	@Column(name = "group_id", nullable=false)
	public long getGroupId() {
		return groupId;
	}

	public void setGroupId(long groupId) {
		this.groupId = groupId;
	}

	@Column(name = "user_id", nullable=false)
	public long getUserId() {
		return userId;
	}
	
	public void setUserId(long userId) {
		this.userId = userId;
	}
	
	@Column(name = "class_name", nullable=false, length=1024)
	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	@Column(name = "class_pk", nullable=false)
	public long getClassPK() {
		return classPK;
	}

	public void setClassPK(long classPK) {
		this.classPK = classPK;
	}

	@Column(name = "process_instance_id", nullable=false)
	public String getProcessInstanceId() {
		return processInstanceId;
	}
	
	public void setProcessInstanceId(String processInstanceId) {
		this.processInstanceId = processInstanceId;
	}
	
	private long id;
	private long companyId;
	private long groupId;
	private long userId;
	private String className;
	private long classPK;
	
	private String processInstanceId;
}
