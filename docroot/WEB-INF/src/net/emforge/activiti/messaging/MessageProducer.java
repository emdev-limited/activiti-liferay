package net.emforge.activiti.messaging;

import java.io.Serializable;

import org.activiti.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Service;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.messaging.Message;
import com.liferay.portal.kernel.messaging.MessageBusUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.kernel.workflow.WorkflowInstanceManagerUtil;
import com.liferay.portal.model.WorkflowInstanceLink;
import com.liferay.portal.service.WorkflowInstanceLinkLocalServiceUtil;

@Service("messageProducer")
public class MessageProducer {
	private static Log _log = LogFactoryUtil.getLog(MessageProducer.class);
	
	public void sendMessage(Serializable classPK, String actionName, String listenerName) throws Exception {
		_log.debug("Sending message for listener [" + listenerName + "], classPK = [" + classPK + "], action name = [" + actionName + "]");
		Message message = new Message();
		message.put("actionName", actionName);
		message.put("classPK", classPK);
		MessageBusUtil.sendMessage(listenerName, message);
	}
	
	public String sendMessage(DelegateExecution execution, Serializable classPK, String actionName,
			String listenerName, String...transitionValues) throws Exception {
		_log.info("Sending message for listener [" + listenerName + "], classPK = [" + classPK + "], action name = [" + actionName + "]");
		Message message = new Message();
		message.put("actionName", actionName);
		message.put("classPK", classPK);
		
		String reply = (String) MessageBusUtil.sendSynchronousMessage(listenerName, message);
		_log.info("Reply fetched [" + reply + "]");
		return reply;
	}

	
}
