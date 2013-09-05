package net.emforge.activiti.service;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.transaction.Isolation;
import com.liferay.portal.kernel.transaction.Transactional;
import com.liferay.portal.service.BaseLocalService;
import com.liferay.portal.service.InvokableLocalService;

/**
 * The interface for the activiti local service.
 *
 * <p>
 * This is a local service. Methods of this service will not have security checks based on the propagated JAAS credentials because this service can only be accessed from within the same VM.
 * </p>
 *
 * @author Brian Wing Shun Chan
 * @see ActivitiLocalServiceUtil
 * @see net.emforge.activiti.service.base.ActivitiLocalServiceBaseImpl
 * @see net.emforge.activiti.service.impl.ActivitiLocalServiceImpl
 * @generated
 */
@Transactional(isolation = Isolation.PORTAL, rollbackFor =  {
    PortalException.class, SystemException.class}
)
public interface ActivitiLocalService extends BaseLocalService,
    InvokableLocalService {
    /*
     * NOTE FOR DEVELOPERS:
     *
     * Never modify or reference this interface directly. Always use {@link ActivitiLocalServiceUtil} to access the activiti local service. Add custom service methods to {@link net.emforge.activiti.service.impl.ActivitiLocalServiceImpl} and rerun ServiceBuilder to automatically copy the method declarations to this interface.
     */

    /**
    * Returns the Spring bean ID for this bean.
    *
    * @return the Spring bean ID for this bean
    */
    public java.lang.String getBeanIdentifier();

    /**
    * Sets the Spring bean ID for this bean.
    *
    * @param beanIdentifier the Spring bean ID for this bean
    */
    public void setBeanIdentifier(java.lang.String beanIdentifier);

    public java.lang.Object invokeMethod(java.lang.String name,
        java.lang.String[] parameterTypes, java.lang.Object[] arguments)
        throws java.lang.Throwable;

    public java.lang.String createNewModel(java.lang.String modelName,
        java.lang.String modelDescription)
        throws com.liferay.portal.kernel.exception.PortalException,
            com.liferay.portal.kernel.exception.SystemException;

    public java.lang.String test(java.lang.String s)
        throws com.liferay.portal.kernel.exception.PortalException,
            com.liferay.portal.kernel.exception.SystemException;

    /**
    * Returns all execution ids, including sub-process executions
    *
    * @param instanceIds
    * @return
    * @throws SystemException
    */
    public java.util.List<java.lang.String> findAllExecutions(
        java.util.List instanceIds)
        throws com.liferay.portal.kernel.exception.SystemException;

    /**
    * Returns active UserTask names for selected instances.
    *
    * @param instanceIds
    * @return
    * @throws SystemException
    */
    public java.util.Set<java.lang.String> findUniqueUserTaskNames(
        java.util.List<java.lang.String> executionIds)
        throws com.liferay.portal.kernel.exception.SystemException;

    /**
    * Returns active UserTask assignees for selected instances.
    *
    * @param instanceIds
    * @return
    */
    public java.util.Set findUniqueUserTaskAssignees(
        java.util.List<java.lang.String> executionIds)
        throws com.liferay.portal.kernel.exception.SystemException;
}
