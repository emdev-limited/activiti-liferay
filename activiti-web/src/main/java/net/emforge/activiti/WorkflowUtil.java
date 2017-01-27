package net.emforge.activiti;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.IdentityLinkType;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQueryFactoryUtil;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.PortalClassLoaderUtil;
import com.liferay.portal.kernel.workflow.WorkflowHandler;
import com.liferay.portal.kernel.workflow.WorkflowHandlerRegistryUtil;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.UserGroup;
import com.liferay.portal.model.UserGroupRole;
import com.liferay.portal.model.WorkflowInstanceLink;
import com.liferay.portal.service.CompanyLocalServiceUtil;
import com.liferay.portal.service.RoleLocalServiceUtil;
import com.liferay.portal.service.UserGroupLocalServiceUtil;
import com.liferay.portal.service.UserGroupRoleLocalServiceUtil;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.service.WorkflowInstanceLinkLocalServiceUtil;

import net.emforge.activiti.identity.UserImpl;

public class WorkflowUtil {
	private static Log _log = LogFactoryUtil.getLog(WorkflowUtil.class);
	
	private static RuntimeService runtimeService = ProcessEngines.getDefaultProcessEngine().getRuntimeService();
	
	public static Map<String, Serializable> getWorkflowContext(String executionId, Map<String, Serializable> currentWorkflowContext) {
        Map<String, Serializable> workflowContext = null;
        try {
        	Execution e = runtimeService.createExecutionQuery().executionId(executionId).singleResult();
        	if(e == null) {
        		workflowContext = currentWorkflowContext;
        	} else {
        		Map<String, Object> vars = runtimeService.getVariables(executionId);
            	workflowContext = convertFromVars(vars);
                currentWorkflowContext = workflowContext;
                
                workflowContext.put("allVariables", (Serializable)convertFromVars(getAllProcessInstanceVars(executionId)));
        	}
        } catch (Exception ex) {
        	// in case then process has no user tasks - process may be finished just after it is started
        	// so - we will not have active activities here.
        	_log.debug("Error during getting context vars", ex);
        	workflowContext = currentWorkflowContext;
        }
        return workflowContext;
    }

	public static Map<String, Serializable> convertFromVars(Map<String, Object> variables) {
		if (variables == null) {
			return new HashMap<String, Serializable>();
		}

		Map<String, Serializable> workflowContext = new HashMap<String, Serializable>();

		for (Map.Entry<String, Object> entry : variables.entrySet()) {
			workflowContext.put(entry.getKey(), (Serializable)entry.getValue());
		}

		return workflowContext;
	}
	
	public static Map<String, Object> convertFromContext(Map<String, Serializable> workflowContext) {
		Map<String, Object> variables = new HashMap<String, Object>();
		if (workflowContext != null) {
			variables.putAll(workflowContext);
		}		
		return variables;
	}
	
	/**
	 * <p>
	 * Deserializes keywords string into a map.
	 * <p>
	 * Keywords should be serialized like this:
	 * <blockquote><pre>
	 *     Map<String, Object> map = new HashMap<String, Object>();
	 *     map.put("groupId", 100);
	 *     String keywords = JSONFactoryUtil.serialize(map);
	 * </pre></blockquote>
	 * <p>
	 * May be the beter way is use some marshaller because of Long variables may be converted into Integer variables
	 * 
	 * @param keywords
	 * 			{@code String} serialized by {@code JSONFactoryUtil}
	 * @return {@code Map<String, Object>} if it can be converted to or {@code null} otherwise.
	 * 
	 * @author Dmitry Farafonov
	 */
	public static Map<String, Object> convertValueToParameterValue(String keywords) {
		if (keywords == null) {
			return MapUtils.EMPTY_MAP;
		}
		try{
			Object obj = JSONFactoryUtil.deserialize(keywords);
			if (obj instanceof Map) {
				@SuppressWarnings("unchecked")
				Map<String, Object> res = (Map<String, Object>) obj;
				return res;
			}
		} catch (Exception e) {
			// skip
		}
		return MapUtils.EMPTY_MAP;
	}
	
	protected static Map<String, Object> getAllProcessInstanceVars(String executionId) {
        Map<String, Object> result = new HashMap<String, Object>();

        Execution execution = runtimeService.createExecutionQuery().executionId(executionId).singleResult();
        ExecutionEntity executionEntity;
        if (execution instanceof ExecutionEntity) {
            executionEntity = (ExecutionEntity) execution;
        } else {
            return runtimeService.getVariables(executionId);
        }

        String lastSuperExecutionId = executionEntity.getProcessInstanceId();
        do {
            final Map<String, Object> variables = runtimeService.getVariables(lastSuperExecutionId);
            for (String key : variables.keySet()) {
                if (!result.containsKey(key)) {
                    result.put(key, variables.get(key));
                }
            }
            ProcessInstance pInst = runtimeService.createProcessInstanceQuery().processInstanceId(lastSuperExecutionId).singleResult();
            if (pInst instanceof ExecutionEntity) {
                executionEntity = (ExecutionEntity)pInst;
            } else {
                return result;
            }
            String superExecutionId = executionEntity.getSuperExecutionId();
            if (superExecutionId == null) break;
            executionEntity = (ExecutionEntity)runtimeService.createExecutionQuery().executionId(superExecutionId).singleResult();
            lastSuperExecutionId = executionEntity.getProcessInstanceId();
        } while (true);

        return result;
    }
	
	public static ExecutionEntity getTopProcessInstance(Execution execution){
		do {
			//String superExecutionId = ((ExecutionEntity)execution).getSuperExecutionId();
			//ExecutionEntity superExecution = (ExecutionEntity)runtimeService.createExecutionQuery().executionId(superExecutionId).singleResult();
//			ExecutionEntity superExecution = ((ExecutionEntity)execution).getSuperExecution();
			ExecutionEntity superExecution = ((ExecutionEntity)execution).getProcessInstance().getSuperExecution();
			if (superExecution == null) {
				return (ExecutionEntity) execution;
			}
			execution = superExecution;
		} while (true);
	}
	
	public static ExecutionEntity getTopProcessInstance(String executionId){
		do {
			//ExecutionEntity execution = (ExecutionEntity)runtimeService.createExecutionQuery().executionId(executionId).singleResult();
			ExecutionEntity execution = (ExecutionEntity)runtimeService.createProcessInstanceQuery().processInstanceId(executionId).singleResult();
			String superExecutionId = execution.getSuperExecutionId();
			if (superExecutionId == null) {
				return execution;
			}
			try {
				ExecutionEntity superExecution = (ExecutionEntity)runtimeService.createExecutionQuery().executionId(superExecutionId).singleResult();
				executionId = superExecution.getProcessInstanceId();
			} catch(Exception e) {
				return execution;
			}
		} while (true);
	}
	
	public static Map<String, Object> getTopWorkflowContext(String executionId) {
        ExecutionEntity executionEntity = (ExecutionEntity)runtimeService.createExecutionQuery().executionId(executionId).singleResult();
        ExecutionEntity topexecution = getTopProcessInstance(executionEntity);
        Map<String, Object> topWorkflowContext = topexecution.getVariables();
        return topWorkflowContext;
	}

    public static WorkflowInstanceLink findByProcessInstanceId(String processInstanceId) {
        try {
            _log.debug("Trying to fetch workflow instance link with id = " + processInstanceId);
            DynamicQuery wfLinkQuery = DynamicQueryFactoryUtil.forClass(WorkflowInstanceLink.class, PortalClassLoaderUtil.getClassLoader());
            wfLinkQuery.add(PropertyFactoryUtil.forName("workflowInstanceId").eq(Long.valueOf(processInstanceId)));
            List resultList = WorkflowInstanceLinkLocalServiceUtil.dynamicQuery(wfLinkQuery);
            if (resultList.size() > 0) {
                return (WorkflowInstanceLink) resultList.get(0);
            } else {
                return null;
            }
        } catch (Exception e) {
            _log.error(e,e);
            return null;
        }
    }
    
    public static List<User> findUsersByGroup(long companyId, String groupName) {
		// first - try to parse group to identify - it is regular group or org/community group
		String[] parsedName = groupName.split("/");
		List<com.liferay.portal.model.User> users = null;
		List<User> result = new ArrayList<User>();
		
		try {
			if (parsedName.length == 1 || Long.valueOf(parsedName[0]) == companyId) {
				if (parsedName.length > 1) {
					groupName = parsedName[1];
					if (parsedName.length > 2) {
						groupName = StringUtils.join(ArrayUtils.subarray(parsedName, 1, parsedName.length), "/");
					}
				}
				// try to get user group
				try {
					UserGroup userGroup = UserGroupLocalServiceUtil.getUserGroup(companyId, groupName);
					users = UserLocalServiceUtil.getUserGroupUsers(userGroup.getUserGroupId());
					
					for (com.liferay.portal.model.User user : users) {
						result.add(new UserImpl(user));
					}
				} catch (Exception ex) {
					// user group not found - use regular group
					Role role = RoleLocalServiceUtil.getRole(companyId, groupName);
					users = UserLocalServiceUtil.getRoleUsers(role.getRoleId());
					
					for (com.liferay.portal.model.User user : users) {
						result.add(new UserImpl(user));
					}
				}
			} else {
				long groupId = Long.valueOf(parsedName[0]);
				groupName = parsedName[1];
				
				if (parsedName.length > 2) {
					groupName = StringUtils.join(ArrayUtils.subarray(parsedName, 1, parsedName.length), "/");
				}
				
				Role role = RoleLocalServiceUtil.getRole(companyId, groupName);
				List<UserGroupRole> userRoles = UserGroupRoleLocalServiceUtil.getUserGroupRolesByGroupAndRole(groupId, role.getRoleId());
				
				for (UserGroupRole userRole : userRoles) {
					result.add(new UserImpl(userRole.getUser()));
				}
			}
		} catch (Exception ex) {
			_log.warn("Cannot get group users", ex);
		}
		
		return result;
	}

    /**
     * Put task local variables into current workflow context
     * @param mpContext
     * @param variablesLocal
     */
    public static void putTaskVariables(Map<String, Serializable> mpContext,
            Map<String, Object> variablesLocal) 
    {
        mpContext.put("taskLocalVariables", (Serializable)convertFromVars(variablesLocal));
    }
    
    public static void clearCandidateGroups(TaskService taskService, String taskId) {
        List<IdentityLink> lstLinks = taskService.getIdentityLinksForTask(taskId);
        for (IdentityLink link: lstLinks) {
            if (IdentityLinkType.CANDIDATE.equalsIgnoreCase(link.getType()) && (! StringUtils.isEmpty(link.getGroupId())) )
                taskService.deleteCandidateGroup(taskId, link.getGroupId());
        }
        
    }

    /**
     * This method could be used to define asset class name by asset type via workflow handler
     * 
     * @param assetType
     * @return
     */
    public static String getAssetClassName(String assetType) {
		String type = assetType.substring(1, assetType.length() - 1);

		// find it in workflow handlers
		List<WorkflowHandler> workflowHhandlers = WorkflowHandlerRegistryUtil.getWorkflowHandlers();

		for (WorkflowHandler workflowHandler : workflowHhandlers) {
			String workflowHandlerType = workflowHandler.getType(LocaleUtil.getDefault());

			// compare by handler type
			if (workflowHandlerType.equalsIgnoreCase(type)) {
				return workflowHandler.getClassName();
			}
		}

		return assetType;
	}

	public static int getCompaniesCount() {
		try {
			return CompanyLocalServiceUtil.getCompaniesCount();
		} catch (SystemException e) {
			_log.error(e);
		}
		return 1;
	}
}
