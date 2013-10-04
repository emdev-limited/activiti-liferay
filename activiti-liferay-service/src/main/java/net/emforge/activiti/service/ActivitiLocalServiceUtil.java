package net.emforge.activiti.service;

import com.liferay.portal.kernel.bean.PortletBeanLocatorUtil;
import com.liferay.portal.kernel.util.ReferenceRegistry;
import com.liferay.portal.service.InvokableLocalService;

/**
 * The utility for the activiti local service. This utility wraps {@link net.emforge.activiti.service.impl.ActivitiLocalServiceImpl} and is the primary access point for service operations in application layer code running on the local server.
 *
 * <p>
 * This is a local service. Methods of this service will not have security checks based on the propagated JAAS credentials because this service can only be accessed from within the same VM.
 * </p>
 *
 * @author Brian Wing Shun Chan
 * @see ActivitiLocalService
 * @see net.emforge.activiti.service.base.ActivitiLocalServiceBaseImpl
 * @see net.emforge.activiti.service.impl.ActivitiLocalServiceImpl
 * @generated
 */
public class ActivitiLocalServiceUtil {
    private static ActivitiLocalService _service;

    /*
     * NOTE FOR DEVELOPERS:
     *
     * Never modify this class directly. Add custom service methods to {@link net.emforge.activiti.service.impl.ActivitiLocalServiceImpl} and rerun ServiceBuilder to regenerate this class.
     */

    /**
    * Returns the Spring bean ID for this bean.
    *
    * @return the Spring bean ID for this bean
    */
    public static java.lang.String getBeanIdentifier() {
        return getService().getBeanIdentifier();
    }

    /**
    * Sets the Spring bean ID for this bean.
    *
    * @param beanIdentifier the Spring bean ID for this bean
    */
    public static void setBeanIdentifier(java.lang.String beanIdentifier) {
        getService().setBeanIdentifier(beanIdentifier);
    }

    public static java.lang.Object invokeMethod(java.lang.String name,
        java.lang.String[] parameterTypes, java.lang.Object[] arguments)
        throws java.lang.Throwable {
        return getService().invokeMethod(name, parameterTypes, arguments);
    }

    public static java.lang.String createNewModel(java.lang.String modelName,
        java.lang.String modelDescription)
        throws com.liferay.portal.kernel.exception.PortalException,
            com.liferay.portal.kernel.exception.SystemException {
        return getService().createNewModel(modelName, modelDescription);
    }

    public static java.lang.String test(java.lang.String s)
        throws com.liferay.portal.kernel.exception.PortalException,
            com.liferay.portal.kernel.exception.SystemException {
        return getService().test(s);
    }

    /**
    * Returns all execution ids, including sub-process executions
    *
    * @param instanceIds
    * @return
    * @throws SystemException
    */
    public static java.util.List<java.lang.String> findAllExecutions(
        java.util.List instanceIds)
        throws com.liferay.portal.kernel.exception.SystemException {
        return getService().findAllExecutions(instanceIds);
    }

    /**
    * Returns active UserTask names for selected instances.
    *
    * @param instanceIds
    * @return
    * @throws SystemException
    */
    public static java.util.Set<java.lang.String> findUniqueUserTaskNames(
        java.util.List<java.lang.String> executionIds)
        throws com.liferay.portal.kernel.exception.SystemException {
        return getService().findUniqueUserTaskNames(executionIds);
    }

    /**
    * Returns active UserTask assignees for selected instances.
    *
    * @param instanceIds
    * @return
    */
    public static java.util.Set findUniqueUserTaskAssignees(
        java.util.List<java.lang.String> executionIds)
        throws com.liferay.portal.kernel.exception.SystemException {
        return getService().findUniqueUserTaskAssignees(executionIds);
    }

    /**
    * Returns top level process instances, filtered by active user task.
    *
    * @param taskName
    - user task name
    * @param assigneeUser
    - task assignee
    * @param candidateRole
    - candidate role for task
    * @return
    * @throws SystemException
    */
    public static java.util.List<java.lang.String> findTopLevelProcessInstances(
        java.lang.String taskName, java.lang.String assigneeUser,
        java.lang.String candidateRole)
        throws com.liferay.portal.kernel.exception.SystemException {
        return getService()
                   .findTopLevelProcessInstances(taskName, assigneeUser,
            candidateRole);
    }

    /**
    * Suspend workflow instance
    */
    public static boolean suspendWorkflowInstance(long companyId,
        long workflowInstanceId)
        throws com.liferay.portal.kernel.workflow.WorkflowException {
        return getService()
                   .suspendWorkflowInstance(companyId, workflowInstanceId);
    }

    /**
    * Resume workflow instance
    */
    public static boolean resumeWorkflowInstance(long companyId,
        long workflowInstanceId)
        throws com.liferay.portal.kernel.workflow.WorkflowException {
        return getService().resumeWorkflowInstance(companyId, workflowInstanceId);
    }

    public static boolean stopWorkflowInstance(long companyId, long userId,
        long workflowInstanceId, java.lang.String comment)
        throws com.liferay.portal.kernel.workflow.WorkflowException {
        return getService()
                   .stopWorkflowInstance(companyId, userId, workflowInstanceId,
            comment);
    }

    public static void clearService() {
        _service = null;
    }

    public static ActivitiLocalService getService() {
        if (_service == null) {
            InvokableLocalService invokableLocalService = (InvokableLocalService) PortletBeanLocatorUtil.locate(ClpSerializer.getServletContextName(),
                    ActivitiLocalService.class.getName());

            if (invokableLocalService instanceof ActivitiLocalService) {
                _service = (ActivitiLocalService) invokableLocalService;
            } else {
                _service = new ActivitiLocalServiceClp(invokableLocalService);
            }

            ReferenceRegistry.registerReference(ActivitiLocalServiceUtil.class,
                "_service");
        }

        return _service;
    }

    /**
     * @deprecated
     */
    public void setService(ActivitiLocalService service) {
    }
}
