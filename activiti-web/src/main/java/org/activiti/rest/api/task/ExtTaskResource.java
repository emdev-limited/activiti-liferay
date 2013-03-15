package org.activiti.rest.api.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.emforge.activiti.query.ExtTaskQuery;

import org.activiti.engine.impl.TaskQueryProperty;
import org.activiti.engine.query.QueryProperty;
import org.activiti.engine.task.Task;
import org.activiti.rest.api.DataResponse;
import org.activiti.rest.api.SecuredResource;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringPool;

public class ExtTaskResource extends SecuredResource {
	private static Log _log = LogFactoryUtil.getLog(ExtTaskResource.class);
	
	@Post
	public DataResponse getTasks(Representation entity) {
		try {
//			String taskPrefix 	 = getQuery().getValues("taskPrefix") == null ? StringPool.BLANK : getQuery().getValues("taskPrefix");
//			String queueName  	 = getQuery().getValues("queueName") == null ? StringPool.BLANK : getQuery().getValues("queueName");
//			String dueDateString = getQuery().getValues("dueDate") == null ? StringPool.BLANK : getQuery().getValues("dueDate");
			String maxResultsStr = getQuery().getValues("maxResults") == null ? StringPool.BLANK : getQuery().getValues("maxResults");
//			String[] timeZoneIds = getQuery().getValuesArray("timeZoneIds");
			
			String countParams = entity.getText();
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
			
			
			Integer maxResults = 0;
			if(StringUtils.isNotBlank(maxResultsStr)) {
				try {
					maxResults = Integer.valueOf(maxResultsStr);
				} catch (NumberFormatException e) {}
		    }
		    
			ExtTaskQuery taskQueury = ExtTaskCountResource.getTaskQuery(taskPrefix, queueName, dueDateString, timeZoneIds);
			List<Task> tasks = null;
			
			if (maxResults == null || maxResults == 0) {
				maxResults = 0;
				tasks = taskQueury.list();
			} else {
				tasks = taskQueury.listPage(0, maxResults);
			}
			
			List<TaskResponse> taskResponseList = new ArrayList<TaskResponse>();
			for (Task task : tasks) {
				TaskResponse resp = new TaskResponse(task);
				taskResponseList.add(resp);
			}
			
			DataResponse response = new DataResponse();
		    response.setStart(maxResults);
		    response.setSize(taskResponseList.size()); 
		    response.setSort("false");
		    response.setOrder("false");
		    response.setTotal(taskQueury.count());
		    response.setData(taskResponseList);
		    return response;
		} catch (Exception e) {
			_log.error("Failed to count tasks", e);
			return null;
		}
	}
}
