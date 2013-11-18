
package net.emforge.activiti.task;

/**
 * @author Konstantin Lysunkin
 */
public class SendNotificationMessageAssignmentTaskListener
    extends BaseSendNotificationMessageTaskListener {

    private static final long serialVersionUID = 7369079469717789857L;

    @Override
    public String getDestination() {

        return "liferay/activiti/task/assigned";
    }

}
