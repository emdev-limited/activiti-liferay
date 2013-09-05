package net.emforge.activiti.service.persistence;

public interface ActivitiFinder {
    public java.util.List<java.lang.String> findTopExecutions(
        java.util.List<java.lang.String> processInstanceIds)
        throws com.liferay.portal.kernel.exception.SystemException;

    public java.util.List<java.lang.String> findSubExecutions(
        java.util.List<java.lang.String> execIds)
        throws com.liferay.portal.kernel.exception.SystemException;

    public java.util.List<java.lang.String> findUniqueUserTaskNames(
        java.util.List<java.lang.String> execIds)
        throws com.liferay.portal.kernel.exception.SystemException;

    public java.util.List findUniqueUserTaskAssignees(
        java.util.List<java.lang.String> execIds)
        throws com.liferay.portal.kernel.exception.SystemException;

    public java.util.List<java.lang.Object[]> findUserTasks(
        java.lang.String taskName, java.lang.String assigneeUser,
        java.lang.String candidateRole)
        throws com.liferay.portal.kernel.exception.SystemException;

    public java.util.List<java.lang.Object[]> findSuperExecutions(
        java.util.List<java.lang.String> execIds)
        throws com.liferay.portal.kernel.exception.SystemException;
}
