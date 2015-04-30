package net.emforge.activiti.messaging;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.messaging.BaseMessageListener;
import com.liferay.portal.kernel.messaging.Message;

public class ActivitiAssetDeploymentMessageListener extends BaseMessageListener {
	private static Log _log = LogFactoryUtil.getLog(ActivitiAssetDeploymentMessageListener.class);

	@Override
	protected void doReceive(Message message) throws Exception {
		_log.warn("Method not implemented");
	}
}
