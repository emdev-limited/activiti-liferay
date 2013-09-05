package net.emforge.activiti.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import net.emforge.activiti.service.base.ActivitiLocalServiceBaseImpl;
import net.emforge.activiti.service.persistence.ActivitiFinderUtil;
import net.emforge.activiti.service.transaction.ActivitiTransactionHelperIF;
import net.emforge.activiti.spring.ApplicationContextProvider;

/**
 * The implementation of the activiti local service.
 *
 * <p>
 * All custom service methods should be put in this class. Whenever methods are added, rerun ServiceBuilder to copy their definitions into the {@link net.emforge.activiti.service.ActivitiLocalService} interface.
 *
 * <p>
 * This is a local service. Methods of this service will not have security checks based on the propagated JAAS credentials because this service can only be accessed from within the same VM.
 * </p>
 *
 * @author Brian Wing Shun Chan
 * @see net.emforge.activiti.service.base.ActivitiLocalServiceBaseImpl
 * @see net.emforge.activiti.service.ActivitiLocalServiceUtil
 */
public class ActivitiLocalServiceImpl extends ActivitiLocalServiceBaseImpl {
	
	@Autowired
	TaskService taskService;
	
	@Autowired
	RuntimeService runtimeService;	
	
    /*
     * NOTE FOR DEVELOPERS:
     *
     * Never reference this interface directly. Always use {@link net.emforge.activiti.service.ActivitiLocalServiceUtil} to access the activiti local service.
     */
	private static Log _log = LogFactoryUtil.getLog(ActivitiLocalServiceImpl.class.getName());
	
	@Override
	public String createNewModel(String modelName, String modelDescription) throws SystemException, PortalException {
		try {
			ApplicationContext context = ApplicationContextProvider.getApplicationContext();
			ActivitiTransactionHelperIF helper = (ActivitiTransactionHelperIF) context.getBean("activitiTransactionHelper");
	        return helper.createNewModel(modelName, modelDescription);  
	    } catch(Exception e) {
	      _log.error(e,e);
	      throw new RuntimeException(e.getMessage());
	    }
	}
	
	@Override
	public String test(String s) throws SystemException, PortalException {
		
		try {
			ApplicationContext context = ApplicationContextProvider.getApplicationContext();
			ActivitiTransactionHelperIF helper = (ActivitiTransactionHelperIF) context.getBean("activitiTransactionHelper");
			return helper.test(s);
		} catch (Exception e) {
			_log.error(e,e);
			throw new RuntimeException(e.getMessage());
		}
	}
	
	/**
	 * Returns all execution ids, including sub-process executions
	 * @param instanceIds
	 * @return
	 * @throws SystemException
	 */
	public List<String> findAllExecutions(List instanceIds) throws SystemException {
		// convert List<Long> to List<String>
		ArrayList<String> lstInstIds = new ArrayList<String>(instanceIds.size()); 
		for (Object instanceId: instanceIds)
			lstInstIds.add(instanceId.toString());
		
		// find all executions, including sub-executions
		List<String> topExecutions = ActivitiFinderUtil.findTopExecutions(lstInstIds);
		ArrayList<String> allExecutions = new ArrayList<String>(topExecutions.size()*2);
		allExecutions.addAll(topExecutions);
		
		while (true) {
			List<String> subExecutions = ActivitiFinderUtil.findSubExecutions(topExecutions);
			if (subExecutions == null || subExecutions.size() == 0)
				break;
			
			allExecutions.addAll(subExecutions);
			topExecutions = subExecutions;
		}
		return allExecutions;
	}
	
	/**
	 * Returns active UserTask names for selected instances.
	 * @param instanceIds
	 * @return
	 * @throws SystemException 
	 */
	public Set<String> findUniqueUserTaskNames(List<String> executionIds) throws SystemException {
		// find uniqie task names
		List<String> lstTasks = ActivitiFinderUtil.findUniqueUserTaskNames(executionIds);
		HashSet<String> res = new HashSet<String>(lstTasks.size());
		res.addAll(lstTasks);

		return res;
	}

	/**
	 * Returns active UserTask assignees for selected instances.
	 * @param instanceIds
	 * @return
	 */	
	public Set findUniqueUserTaskAssignees(List<String> executionIds) throws SystemException {
		// find uniqie assignees
		List lstAsg = ActivitiFinderUtil.findUniqueUserTaskAssignees(executionIds);
		HashSet res = new HashSet(lstAsg.size());
		res.addAll(lstAsg);

		return res;		
	}	
}
