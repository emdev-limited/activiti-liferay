package net.emforge.activiti.service.persistence;

import com.liferay.portal.kernel.bean.PortletBeanLocatorUtil;
import com.liferay.portal.kernel.util.ReferenceRegistry;


public class ActivitiFinderUtil {
    private static ActivitiFinder _finder;

    public static java.util.List<java.lang.String> findTopExecutions(
        java.util.List<java.lang.String> processInstanceIds)
        throws com.liferay.portal.kernel.exception.SystemException {
        return getFinder().findTopExecutions(processInstanceIds);
    }

    public static java.util.List<java.lang.String> findSubExecutions(
        java.util.List<java.lang.String> execIds)
        throws com.liferay.portal.kernel.exception.SystemException {
        return getFinder().findSubExecutions(execIds);
    }

    public static java.util.List<java.lang.String> findUniqueUserTaskNames(
        java.util.List<java.lang.String> execIds)
        throws com.liferay.portal.kernel.exception.SystemException {
        return getFinder().findUniqueUserTaskNames(execIds);
    }

    public static java.util.List findUniqueUserTaskAssignees(
        java.util.List<java.lang.String> execIds)
        throws com.liferay.portal.kernel.exception.SystemException {
        return getFinder().findUniqueUserTaskAssignees(execIds);
    }

    public static ActivitiFinder getFinder() {
        if (_finder == null) {
            _finder = (ActivitiFinder) PortletBeanLocatorUtil.locate(net.emforge.activiti.service.ClpSerializer.getServletContextName(),
                    ActivitiFinder.class.getName());

            ReferenceRegistry.registerReference(ActivitiFinderUtil.class,
                "_finder");
        }

        return _finder;
    }

    public void setFinder(ActivitiFinder finder) {
        _finder = finder;

        ReferenceRegistry.registerReference(ActivitiFinderUtil.class, "_finder");
    }
}
