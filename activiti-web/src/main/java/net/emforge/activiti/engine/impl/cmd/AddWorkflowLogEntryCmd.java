/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.emforge.activiti.engine.impl.cmd;

import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import net.emforge.activiti.log.WorkflowLogEntry;
import net.emforge.activiti.log.WorkflowLogTypeMapperUtil;

import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.CommentEntity;
import org.activiti.engine.impl.util.ClockUtil;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;


/**
 * Add a workflow log entry to comment table.
 *
 * @author Oliver Teichmann, PRODYNA AG
 *
 */
public class AddWorkflowLogEntryCmd implements Command<Object> {
	private static Log _log = LogFactoryUtil.getLog(AddWorkflowLogEntryCmd.class);
	
	protected String taskId;
	protected String processInstanceId;
	protected WorkflowLogEntry workflowLogEntry;
	
	public AddWorkflowLogEntryCmd(String taskId, String processInstanceId, WorkflowLogEntry  workflowLogEntry) {
		this.taskId = taskId;
		this.processInstanceId = processInstanceId;
		this.workflowLogEntry = workflowLogEntry;
	}

	public Object execute(CommandContext commandContext) {
	  
		String action = WorkflowLogTypeMapperUtil.mapToActivitiType(workflowLogEntry.getType());
		String message = workflowLogEntry.getComment();
		
		
		StringWriter fmWriter = new StringWriter();
		try {
			JAXBContext context = JAXBContext.newInstance(workflowLogEntry.getClass());
			Marshaller marshaller = context.createMarshaller();
			marshaller.marshal(workflowLogEntry, fmWriter);
		} catch (Exception e) {
			_log.error("Could not serialize WorkflowLogEntry to XML", e);
		}
		
		String fullMessage = fmWriter.toString();
		  
		String userId = Authentication.getAuthenticatedUserId();
		CommentEntity comment = new CommentEntity();
		comment.setUserId(userId);
		comment.setType(CommentEntity.TYPE_EVENT);
		comment.setTime(ClockUtil.getCurrentTime());
		comment.setTaskId(taskId);
		comment.setProcessInstanceId(processInstanceId);
		comment.setAction(action);
		
		String eventMessage = message.replaceAll("\\s+", " ");
		
		if (eventMessage.length()>163) {
			eventMessage = eventMessage.substring(0, 160)+"...";
		}
		comment.setMessage(eventMessage);
		    
		comment.setFullMessage(fullMessage);
		    
		commandContext.getCommentEntityManager().insert(comment);
		    
		return null;
   	}
}
