/**
 * Copyright (c) 2000-2010 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package net.emforge.activiti.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.activiti.engine.repository.ProcessDefinition;


/**
 * <a href="WorkflowDefinitionExtensionImpl.java.html"><b><i>View Source</i></b>
 * </a>
 *
 * @author Marcellus Tavares
 * @author Alexey Kakunin
 */
@Entity
@Table(name = "ACT_PROCESSDEFINITIONEXTENSION_LIFERAY")
public class WorkflowDefinitionExtensionImpl {
	public WorkflowDefinitionExtensionImpl() {
	}

	public WorkflowDefinitionExtensionImpl(ProcessDefinition processDefinition,
										   long companyId,
										   String title,
										   String name,
										   boolean active,
										   int version) {

		this.processDefinitionId = processDefinition.getId();
		this.title = title;
		this.name = name;
		this.active = active;
		this.companyId = companyId;
		this.version = version;
	}

	// TODO understand why unique produced bug during schemaUpdate on MySQL
    @Column(name = "process_definition_id", nullable=false, length=256)
	public String getProcessDefinitionId() {
		return this.processDefinitionId;
	}

    @Column(name = "title", nullable=false, length=4000)
	public String getTitle() {
		return this.title;
	}

    @Id
    @Column(name = "workflow_definition_extension_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
	public long getWorkflowDefinitionExtensionId() {
		return this.workflowDefinitionExtensionId;
	}

	@Column(name = "name", nullable=false, length=1024)
	public String getName() {
		return name;
	}

    @Column(name = "version", nullable=false)
	public int getVersion() {
		return version;
	}

    @Column(name = "active")
	public boolean isActive() {
		return this.active;
	}

    @Column(name = "company_id", nullable=false)
	public long getCompanyId() {
		return this.companyId;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public void setProcessDefinitionId(String processDefinitionId) {
		this.processDefinitionId = processDefinitionId;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}

	public void setWorkflowDefinitionExtensionId(long workflowDefinitionExtensionId) {
		this.workflowDefinitionExtensionId = workflowDefinitionExtensionId;
	}

	public void setCompanyId(long companyId) {
		this.companyId = companyId;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setVersion(int version) {
		this.version = version;
	}

    private boolean active;
	private String title;
	private String processDefinitionId;
	private long companyId;
    private String name;
    private int version;
    
    
	private long workflowDefinitionExtensionId;
}