package net.emforge.activiti;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQueryFactoryUtil;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.util.PortalClassLoaderUtil;
import com.liferay.portal.model.WorkflowInstanceLink;
import com.liferay.portal.service.WorkflowInstanceLinkLocalServiceUtil;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

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

	private static Map<String, Serializable> convertFromVars(Map<String, Object> variables) {
		if (variables == null) {
			return new HashMap<String, Serializable>();
		}

		Map<String, Serializable> workflowContext = new HashMap<String, Serializable>();

		for (Map.Entry<String, Object> entry : variables.entrySet()) {
			workflowContext.put(entry.getKey(), (Serializable)entry.getValue());
		}

		return workflowContext;
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
}
