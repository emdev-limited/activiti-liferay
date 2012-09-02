package net.emforge.activiti.messaging;

import java.io.Serializable;

public interface MessageProducerIF {
	public void sendMessage(Serializable classPK, String actionName,String listenerName) throws Exception;
}
