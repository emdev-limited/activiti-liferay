package net.emforge.activiti.service;

import com.liferay.portal.service.ServiceWrapper;

/**
 * Provides a wrapper for {@link ActivitiLocalService}.
 *
 * @author Brian Wing Shun Chan
 * @see ActivitiLocalService
 * @generated
 */
public class ActivitiLocalServiceWrapper implements ActivitiLocalService,
    ServiceWrapper<ActivitiLocalService> {
    private ActivitiLocalService _activitiLocalService;

    public ActivitiLocalServiceWrapper(
        ActivitiLocalService activitiLocalService) {
        _activitiLocalService = activitiLocalService;
    }

    /**
    * Returns the Spring bean ID for this bean.
    *
    * @return the Spring bean ID for this bean
    */
    @Override
    public java.lang.String getBeanIdentifier() {
        return _activitiLocalService.getBeanIdentifier();
    }

    /**
    * Sets the Spring bean ID for this bean.
    *
    * @param beanIdentifier the Spring bean ID for this bean
    */
    @Override
    public void setBeanIdentifier(java.lang.String beanIdentifier) {
        _activitiLocalService.setBeanIdentifier(beanIdentifier);
    }

    @Override
    public java.lang.Object invokeMethod(java.lang.String name,
        java.lang.String[] parameterTypes, java.lang.Object[] arguments)
        throws java.lang.Throwable {
        return _activitiLocalService.invokeMethod(name, parameterTypes,
            arguments);
    }

    @Override
    public java.lang.String createNewModel(java.lang.String modelName,
        java.lang.String modelDescription)
        throws com.liferay.portal.kernel.exception.PortalException,
            com.liferay.portal.kernel.exception.SystemException {
        return _activitiLocalService.createNewModel(modelName, modelDescription);
    }

    @Override
    public java.lang.String test(java.lang.String s)
        throws com.liferay.portal.kernel.exception.PortalException,
            com.liferay.portal.kernel.exception.SystemException {
        return _activitiLocalService.test(s);
    }

    /**
    * Returns all execution ids, including sub-process executions
    *
    * @param instanceIds
    * @return
    * @throws SystemException
    */
    @Override
    public java.util.List<java.lang.String> findAllExecutions(
        java.util.List instanceIds)
        throws com.liferay.portal.kernel.exception.SystemException {
        return _activitiLocalService.findAllExecutions(instanceIds);
    }

    /**
    * Returns active UserTask names for selected instances.
    *
    * @param instanceIds
    * @return
    * @throws SystemException
    */
    @Override
    public java.util.Set<java.lang.String> findUniqueUserTaskNames(
        java.util.List<java.lang.String> executionIds)
        throws com.liferay.portal.kernel.exception.SystemException {
        return _activitiLocalService.findUniqueUserTaskNames(executionIds);
    }

    /**
    * Returns active UserTask assignees for selected instances.
    *
    * @param instanceIds
    * @return
    */
    @Override
    public java.util.Set findUniqueUserTaskAssignees(
        java.util.List<java.lang.String> executionIds)
        throws com.liferay.portal.kernel.exception.SystemException {
        return _activitiLocalService.findUniqueUserTaskAssignees(executionIds);
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
    @Override
    public java.util.List<java.lang.String> findTopLevelProcessInstances(
        java.lang.String taskName, java.lang.String assigneeUser,
        java.lang.String candidateRole)
        throws com.liferay.portal.kernel.exception.SystemException {
        return _activitiLocalService.findTopLevelProcessInstances(taskName,
            assigneeUser, candidateRole);
    }

    @Override
    public java.lang.String findTopLevelProcess(java.lang.String taskId)
        throws com.liferay.portal.kernel.exception.SystemException {
        return _activitiLocalService.findTopLevelProcess(taskId);
    }

    /**
    * Suspend workflow instance
    */
    @Override
    public boolean suspendWorkflowInstance(long companyId,
        long workflowInstanceId)
        throws com.liferay.portal.kernel.workflow.WorkflowException {
        return _activitiLocalService.suspendWorkflowInstance(companyId,
            workflowInstanceId);
    }

    /**
    * Resume workflow instance
    */
    @Override
    public boolean resumeWorkflowInstance(long companyId,
        long workflowInstanceId)
        throws com.liferay.portal.kernel.workflow.WorkflowException {
        return _activitiLocalService.resumeWorkflowInstance(companyId,
            workflowInstanceId);
    }

    @Override
    public boolean stopWorkflowInstance(long companyId, long userId,
        long workflowInstanceId, java.lang.String comment)
        throws com.liferay.portal.kernel.workflow.WorkflowException {
        return _activitiLocalService.stopWorkflowInstance(companyId, userId,
            workflowInstanceId, comment);
    }

    @Override
    public void addWorkflowInstanceComment(long companyId, long userId,
        long workflowInstanceId, long workflowTaskId, int logType,
        java.lang.String comment)
        throws com.liferay.portal.kernel.workflow.WorkflowException {
        _activitiLocalService.addWorkflowInstanceComment(companyId, userId,
            workflowInstanceId, workflowTaskId, logType, comment);
    }

    @Override
    public java.util.List<java.lang.String> findHistoricActivityByName(
        java.lang.String topProcessInstanceId, java.lang.String activityName)
        throws com.liferay.portal.kernel.exception.SystemException {
        return _activitiLocalService.findHistoricActivityByName(topProcessInstanceId,
            activityName);
    }

    /**
     * @deprecated As of 6.1.0, replaced by {@link #getWrappedService}
     */
    public ActivitiLocalService getWrappedActivitiLocalService() {
        return _activitiLocalService;
    }

    /**
     * @deprecated As of 6.1.0, replaced by {@link #setWrappedService}
     */
    public void setWrappedActivitiLocalService(
        ActivitiLocalService activitiLocalService) {
        _activitiLocalService = activitiLocalService;
    }

    @Override
    public ActivitiLocalService getWrappedService() {
        return _activitiLocalService;
    }

    @Override
    public void setWrappedService(ActivitiLocalService activitiLocalService) {
        _activitiLocalService = activitiLocalService;
    }
}
