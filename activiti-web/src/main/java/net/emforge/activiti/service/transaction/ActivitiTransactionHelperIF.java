package net.emforge.activiti.service.transaction;

import org.activiti.engine.ActivitiException;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;

public interface ActivitiTransactionHelperIF {
	public String test(String s)  throws SystemException, PortalException, ActivitiException ;
	public String createNewModel(String modelName, String modelDescription) throws SystemException, PortalException;
}
