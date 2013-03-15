package org.activiti.rest.api.task;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.SecuredResource;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;

import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONSerializer;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringPool;

public class TaskVariableResource extends SecuredResource {
	private static Log _log = LogFactoryUtil.getLog(TaskVariableResource.class);
	
	public ObjectNode getVariable(String taskId, String varName) {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode resultNode = mapper.createObjectNode();
		if (StringUtils.isEmpty(varName) || StringUtils.isEmpty(taskId)) {
			return resultNode;
		}
	    Object val = ActivitiUtil.getTaskService().getVariable(taskId, varName);
	    if (val instanceof Integer) {
			resultNode.put("result", (Integer) val);
		} else if (val instanceof Long) {
			resultNode.put("result", (Long) val);
		} else if (val instanceof BigDecimal) {
			resultNode.put("result", (BigDecimal) val);
		} else if (val instanceof Boolean) {
			resultNode.put("result", (Boolean) val);
		} else if (val instanceof byte[]) {
			resultNode.put("result", (byte[]) val);
		} else if (val instanceof Double) {
			resultNode.put("result", (Double) val);
		} else if (val instanceof Float) {
			resultNode.put("result", (Float) val);
		} else if (val instanceof String) {
			resultNode.put("result", (String) val);
		} else {
			try {
				JSONSerializer jsonSerializer = JSONFactoryUtil.createJSONSerializer();
				jsonSerializer.exclude("expandoBridge");
				String valStr = jsonSerializer.serialize(val);
				resultNode.put("result", valStr);
			} catch (Exception e) {
				_log.error(e,e);
				throw new ActivitiException("Failed to get variable for task", e);
//				resultNode.put("result", (String) val);
			}
		}
		return resultNode;
	}
	
	@Post
	public ObjectNode processVariablesAction(Representation entity) {
		if(authenticate() == false) return null;
		ObjectNode resultNode = new ObjectMapper().createObjectNode();
		try {
			String taskId = (String) getRequest().getAttributes().get("taskId");
		    String actionName = (String) getRequest().getAttributes().get("action");
		    
		    String startParams 		= entity.getText();
		    JsonNode queryJSON 		= new ObjectMapper().readTree(startParams);
		    
		    String varName = StringPool.BLANK;
	    	if(queryJSON.path("varName") != null && queryJSON.path("varName").getTextValue() != null) {
	    		varName = queryJSON.path("varName").getTextValue();
	    	}
		    
		    if ("post".equals(actionName)) {
				try {
					if (StringUtils.isEmpty(varName)) {
						throw new Exception();
					}
					
					if (queryJSON.path("varValue") != null) {
						JsonNode nodeValue = queryJSON.path("varValue");
						ObjectMapper m = new ObjectMapper();
						if (nodeValue.isArray()) {
							//handle List
//							List listValue =  nodeValue.getElements();
							_log.debug("List value recieved");
						}
						if (nodeValue.isObject() || nodeValue.isPojo()) {
							if (queryJSON.path("pojoClass") != null && queryJSON.path("pojoClass").getTextValue() != null) {
								String pojoClass = queryJSON.path("pojoClass").getTextValue();
								Class<?> pojoClazz = Class.forName(pojoClass);
								Object toStore = new ObjectMapper().readValue(queryJSON.path("varValue").toString(), pojoClazz);
								ActivitiUtil.getTaskService().setVariable(taskId, varName, toStore);
							} else {
								throw new ActivitiException("Could not get class name to deserialize pojo");
							}
							//TODO
						} else {
							ActivitiUtil.getTaskService().setVariable(taskId, varName, queryJSON.path("varValue").getTextValue());
						}
					}
					
					resultNode.put("success", true);
				} catch (Exception e) {
					_log.error("Failed to set variable for task", e);
					throw new ActivitiException("Failed to set variable for task", e);
				}
				return resultNode;
		    } else {
		    	return getVariable(taskId, varName);
		    }
		} catch (Exception e) {
			resultNode.put("success", false);
		}
	    
		return resultNode;
	}
	
}
