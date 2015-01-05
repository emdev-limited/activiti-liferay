package org.activiti.rest.api.model;

import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

import org.activiti.engine.repository.Model;
import org.activiti.rest.api.BaseResponse;

@XmlRootElement
public class ModelResponse extends BaseResponse {

	private static final long serialVersionUID = 1L;

	protected String id;
	protected String name;
	protected String key;
	protected String category;
	protected Date createTime;
	protected Date lastUpdateTime;
	protected Integer version = 1;
	protected String metaInfo;
	protected String deploymentId;
	protected String editorSourceValueId;
	protected String editorSourceExtraValueId;
	
	public ModelResponse() {}
	
	public ModelResponse(Model model) {
		setId(model.getId());
		setKey(model.getKey());
		setName(model.getName());
		setCategory(model.getCategory());
		setLastUpdateTime(model.getLastUpdateTime());
		setVersion(model.getVersion());
		setMetaInfo(model.getMetaInfo());
		setDeploymentId(model.getDeploymentId());
	}
	
	//----getters & setters
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public Date getLastUpdateTime() {
		return lastUpdateTime;
	}
	public void setLastUpdateTime(Date lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}
	public String getMetaInfo() {
		return metaInfo;
	}
	public void setMetaInfo(String metaInfo) {
		this.metaInfo = metaInfo;
	}
	public String getDeploymentId() {
		return deploymentId;
	}
	public void setDeploymentId(String deploymentId) {
		this.deploymentId = deploymentId;
	}
	public String getEditorSourceValueId() {
		return editorSourceValueId;
	}
	public void setEditorSourceValueId(String editorSourceValueId) {
		this.editorSourceValueId = editorSourceValueId;
	}
	public String getEditorSourceExtraValueId() {
		return editorSourceExtraValueId;
	}
	public void setEditorSourceExtraValueId(String editorSourceExtraValueId) {
		this.editorSourceExtraValueId = editorSourceExtraValueId;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	
}
