package org.activiti.rest.api.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.emforge.activiti.query.ExtTaskQuery;
import net.emforge.activiti.query.ExtTaskQueryImpl;

import org.activiti.engine.impl.TaskServiceImpl;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.RequestUtil;
import org.activiti.rest.api.SecuredResource;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.workflow.WorkflowConstants;

public class ExtTaskCountResource extends SecuredResource {
	private static Log _log = LogFactoryUtil.getLog(ExtTaskCountResource.class);
	
	public static final String ORGANIZATIN_ID_VAR = "organizationId";
	public static final String STATUS_VAR = "status";
	
	@Post
	public ObjectNode getTasksCount(Representation entity) {
		ObjectNode resultNode = new ObjectMapper().createObjectNode();
		try {
			
			String countParams = entity.getText();
			_log.info(String.format(">>>>>>>>>>Form params are: %s", countParams));
		    JsonNode countJSON = new ObjectMapper().readTree(countParams);
		    
		    String taskPrefix = null;
		    if(countJSON.path("taskPrefix") != null && countJSON.path("taskPrefix").getTextValue() != null) {
		    	taskPrefix = countJSON.path("taskPrefix").getTextValue();
		    }
		    
		    String queueName = null;
		    if(countJSON.path("queueName") != null && countJSON.path("queueName").getTextValue() != null) {
		    	queueName = countJSON.path("queueName").getTextValue();
		    }
		    
		    String dueDateString = null;
		    if(countJSON.path("dueDate") != null && countJSON.path("dueDate").getTextValue() != null) {
		    	dueDateString = countJSON.path("dueDate").getTextValue();
		    }
		    
		    String timeZoneIdsStr = StringPool.BLANK;
		    if(countJSON.path("timeZoneIds") != null && countJSON.path("timeZoneIds").getTextValue() != null) {
		    	timeZoneIdsStr = countJSON.path("timeZoneIds").getTextValue();
		    }
			
			String[] timeZoneIds = timeZoneIdsStr.split(StringPool.COMMA);
			
			_log.info(String.format(">>>>>>>>>>>>>>>Trying to count tasks for prefix = [%s], queue = [%s], due date = [%s] and time zones = [%s]"
					, taskPrefix, queueName, dueDateString, timeZoneIds));
			
			Long result = getTaskQuery(taskPrefix, queueName, dueDateString, timeZoneIds).count();
			resultNode.put("result", result);
			_log.info(String.format(">>>>>>>>>>>>>>>Ext tasks REST count result = [%s]", resultNode));
		    return resultNode;
		} catch (Exception e) {
			_log.error("Failed to count tasks", e);
			resultNode.put("result", 0l);
		}
		
		return resultNode;
	}
	
	public static ExtTaskQuery getTaskQuery(String taskPrefix, String queueName, String dueDateString, String[] timeZoneIds) throws Exception {
		Date dueDate = null;
	    if (StringUtils.isNotEmpty(dueDateString)) {
	    	try {
				dueDate = RequestUtil.parseToDate(dueDateString);
			} catch (Exception e) {
				_log.warn("Failed to parse due date for incoming string = " + dueDateString);
			}
	    }
	    
	    List<String> valueList = new ArrayList<String>();
		if (timeZoneIds != null) {
			for (String timeZoneId : timeZoneIds) {
				valueList.add(timeZoneId);
			}
		}
		// timeZoneId can be undefined, so call them whenever we want
		valueList.add("");
		valueList.add(null);
	    
	    TaskServiceImpl serviceImpl = (TaskServiceImpl)ActivitiUtil.getTaskService();
	    ExtTaskQuery query = new ExtTaskQueryImpl(serviceImpl.getCommandExecutor());
	    
	    query = (ExtTaskQuery) query.taskUnassigned()
	    		//taskDescriptionLike(taskPrefix + "%:%")
				.processVariableValueEquals(ORGANIZATIN_ID_VAR, GetterUtil.getLong(queueName))
				.processVariableValueEquals("outboundCallMode", "PredictiveCall");
	    
	    String variableName = "timeZoneId";
	    query = query.taskVariableValueIn(variableName, valueList);

		if(dueDate != null) {
			query = (ExtTaskQuery) query.dueBefore(dueDate);
		} else {
			query = query.dueDateIsNull();
		}
	    
		return (ExtTaskQuery) query.orderByTaskPriority().desc();
	}
}
