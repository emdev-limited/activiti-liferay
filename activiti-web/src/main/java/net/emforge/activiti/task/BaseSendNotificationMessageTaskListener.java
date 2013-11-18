
package net.emforge.activiti.task;

import java.util.Map;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.task.IdentityLink;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.messaging.Message;
import com.liferay.portal.kernel.messaging.MessageBusUtil;

/**
 * @author Konstantin Lysunkin
 */
public abstract class BaseSendNotificationMessageTaskListener implements TaskListener {

    @Override
    public void notify(DelegateTask task) {

        Map<String, Object> workflowContext = task.getVariables();
        String currentUserId = Authentication.getAuthenticatedUserId();

        Message message = new Message();
        message.setValues(workflowContext);
        message.put("workflowTaskId", task.getId());
        message.put("authenticatedUserId", currentUserId);
        message.put("createDate", task.getCreateTime());
        message.put("name", task.getName());
        message.put("description", task.getDescription());
        message.put("assignee", task.getAssignee());

        JSONArray candidates = JSONFactoryUtil.createJSONArray();
        for (IdentityLink link : task.getCandidates()) {
            JSONObject object = JSONFactoryUtil.createJSONObject();
            object.put("userId", link.getUserId());
            object.put("groupId", link.getGroupId());
            candidates.put(object);
        }

        message.put("candidates", candidates);

        MessageBusUtil.sendMessage(getDestination(), message);
    }
    
    public abstract String getDestination();
}
