package net.emforge.activiti.service;

import com.liferay.portal.service.ServiceWrapper;

/**
 * <p>
 * This class is a wrapper for {@link ActivitiLocalService}.
 * </p>
 *
 * @author    Brian Wing Shun Chan
 * @see       ActivitiLocalService
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
    public java.lang.String getBeanIdentifier() {
        return _activitiLocalService.getBeanIdentifier();
    }

    /**
    * Sets the Spring bean ID for this bean.
    *
    * @param beanIdentifier the Spring bean ID for this bean
    */
    public void setBeanIdentifier(java.lang.String beanIdentifier) {
        _activitiLocalService.setBeanIdentifier(beanIdentifier);
    }

    public java.lang.Object invokeMethod(java.lang.String name,
        java.lang.String[] parameterTypes, java.lang.Object[] arguments)
        throws java.lang.Throwable {
        return _activitiLocalService.invokeMethod(name, parameterTypes,
            arguments);
    }

    public java.lang.String createNewModel(java.lang.String modelName,
        java.lang.String modelDescription)
        throws com.liferay.portal.kernel.exception.PortalException,
            com.liferay.portal.kernel.exception.SystemException {
        return _activitiLocalService.createNewModel(modelName, modelDescription);
    }

    public java.lang.String test(java.lang.String s)
        throws com.liferay.portal.kernel.exception.PortalException,
            com.liferay.portal.kernel.exception.SystemException {
        return _activitiLocalService.test(s);
    }

    /**
     * @deprecated Renamed to {@link #getWrappedService}
     */
    public ActivitiLocalService getWrappedActivitiLocalService() {
        return _activitiLocalService;
    }

    /**
     * @deprecated Renamed to {@link #setWrappedService}
     */
    public void setWrappedActivitiLocalService(
        ActivitiLocalService activitiLocalService) {
        _activitiLocalService = activitiLocalService;
    }

    public ActivitiLocalService getWrappedService() {
        return _activitiLocalService;
    }

    public void setWrappedService(ActivitiLocalService activitiLocalService) {
        _activitiLocalService = activitiLocalService;
    }
}
