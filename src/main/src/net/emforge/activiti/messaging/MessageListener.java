package net.emforge.activiti.messaging;

import com.liferay.portal.kernel.messaging.Message;
import com.liferay.portal.kernel.messaging.proxy.ProxyMessageListener;

/** Fixed version of ProxyMessageListener to set current manager classLoader as thread classLoader
 * It is required since Activiti used currentThread's classLoader to load classes
 * and it may happens what thread will contain incorrect classLoader (from other portlet)
 * 
 * @author akakunin
 *
 */
public class MessageListener extends ProxyMessageListener {
	@Override
	public void receive(Message message) {
		Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
		
		super.receive(message);
	}
}
