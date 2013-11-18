package net.emforge.activiti.task;

/**
 * @author Konstantin Lysunkin
 */
public class SendNotificationMessageCreateTaskListener extends BaseSendNotificationMessageTaskListener {

    private static final long serialVersionUID = 1992710608447945023L;

    @Override
    public String getDestination() {

        return "liferay/activiti/task/created";

    }
}
