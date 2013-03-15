package org.activiti.rest.api.process;

import net.emforge.activiti.engine.LiferayTaskService;
import net.emforge.activiti.log.WorkflowLogEntry;

import org.activiti.engine.task.Task;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.SecuredResource;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringPool;

public class WorkflowLogResource extends SecuredResource {
	private static Log _log = LogFactoryUtil.getLog(WorkflowLogResource.class);
	
	@Post
	public ObjectNode addLogEntry(Representation entity) {
		if(authenticate() == false) return null;
		ObjectNode resultNode = new ObjectMapper().createObjectNode();
		String taskId = (String) getRequest().getAttributes().get("taskId");
		try {
			String startParams 		= entity.getText();
		    JsonNode queryJSON 		= new ObjectMapper().readTree(startParams);
		    String logComment 		= StringPool.BLANK; 
		    if(queryJSON.path("logComment") != null && queryJSON.path("logComment").getTextValue() != null) {
		    	logComment = queryJSON.path("logComment").getTextValue();
		    }
		    int logType = 0;
		    if(queryJSON.path("logType") != null) {
		    	logType = queryJSON.path("logType").getIntValue();
		    }
		    
		    if (StringUtils.isNotEmpty(logComment)) {
		    	WorkflowLogEntry workflowLogEntry = new WorkflowLogEntry();
				workflowLogEntry.setType(logType);
				workflowLogEntry.setComment(logComment);
				
				Task task = ActivitiUtil.getTaskService().createTaskQuery().taskId(taskId).singleResult();
				LiferayTaskService liferayTaskService = (LiferayTaskService) ActivitiUtil.getTaskService();
				liferayTaskService.addWorkflowLogEntry(taskId, task.getProcessInstanceId(), workflowLogEntry);
		    }
		    
		    resultNode.put("success", true);
		} catch (Exception e) {
			_log.error("Failed to add workflow log entry", e);
			resultNode.put("success", false);
		}
		return resultNode;
	}
	
}
