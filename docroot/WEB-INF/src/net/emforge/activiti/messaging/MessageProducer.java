package net.emforge.activiti.messaging;

import java.io.Serializable;

import org.springframework.stereotype.Service;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.messaging.Message;
import com.liferay.portal.kernel.messaging.MessageBusUtil;

@Service("messageProducer")
public class MessageProducer implements MessageProducerIF {
	private static Log _log = LogFactoryUtil.getLog(MessageProducer.class);
	
	@Override
	public void sendMessage(Serializable classPK, String actionName,
			String listenerName) throws Exception {
		_log.debug("Sending message for listener [" + listenerName + "], classPK = [" + classPK + "], action name = [" + actionName + "]");
		Message message = new Message();
		message.put("actionName", actionName);
		message.put("classPK", classPK);
		MessageBusUtil.sendMessage(listenerName, message);
	}

	
}
