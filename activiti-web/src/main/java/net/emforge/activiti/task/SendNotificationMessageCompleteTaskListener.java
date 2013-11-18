
package net.emforge.activiti.task;

/**
 * @author Konstantin Lysunkin
 */
public class SendNotificationMessageCompleteTaskListener
    extends BaseSendNotificationMessageTaskListener {

    private static final long serialVersionUID = -4357239306752335683L;

    @Override
    public String getDestination() {

        return "liferay/activiti/task/completed";
    }

}
