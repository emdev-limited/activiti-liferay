package net.emforge.activiti.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.emforge.activiti.engine.impl.cmd.AddWorkflowLogEntryCmd;
import net.emforge.activiti.log.WorkflowLogConstants;
import net.emforge.activiti.log.WorkflowLogEntry;
import net.emforge.activiti.service.base.ActivitiLocalServiceBaseImpl;
import net.emforge.activiti.service.persistence.ActivitiFinderUtil;
import net.emforge.activiti.service.transaction.ActivitiTransactionHelperIF;
import net.emforge.activiti.spring.ApplicationContextProvider;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.workflow.WorkflowException;

public class ActivitiLocalServiceImpl extends ActivitiLocalServiceBaseImpl {
    private static Log _log = LogFactoryUtil.getLog(ActivitiLocalServiceImpl.class.getName());

    @Autowired
    ProcessEngine processEngine;    
    @Autowired
    RuntimeService runtimeService;    

    @Override
    public String createNewModel(String modelName, String modelDescription)
            throws SystemException, PortalException {
        try {
            ApplicationContext context = ApplicationContextProvider
                    .getApplicationContext();
            ActivitiTransactionHelperIF helper = (ActivitiTransactionHelperIF) context
                    .getBean("activitiTransactionHelper");
            return helper.createNewModel(modelName, modelDescription);
        } catch (Exception e) {
            _log.error(e, e);
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public String test(String s) throws SystemException, PortalException {

        try {
            ApplicationContext context = ApplicationContextProvider
                    .getApplicationContext();
            ActivitiTransactionHelperIF helper = (ActivitiTransactionHelperIF) context
                    .getBean("activitiTransactionHelper");
            return helper.test(s);
        } catch (Exception e) {
            _log.error(e, e);
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Returns all execution ids, including sub-process executions
     * 
     * @param instanceIds
     * @return
     * @throws SystemException
     */
    @Override
    public List<String> findAllExecutions(List instanceIds)
            throws SystemException {
        // convert List<Long> to List<String>
        ArrayList<String> lstInstIds = new ArrayList<String>(instanceIds.size());
        for (Object instanceId : instanceIds) {
            lstInstIds.add(instanceId.toString());
        }
        
        // find all executions, including sub-executions
        List<String> topExecutions = ActivitiFinderUtil
                .findTopExecutions(lstInstIds);
        ArrayList<String> allExecutions = new ArrayList<String>(
                topExecutions.size() * 2);
        allExecutions.addAll(topExecutions);

        allExecutions.addAll(getSubExecutions(topExecutions));
        
        return allExecutions;
    }

    
    private List<String> getSubExecutions(List<String> topExecutions) throws SystemException {
    	if (topExecutions.isEmpty()) {
    		return new ArrayList<String>();
    	}
    	
    	// not empty
    	List<String> allSubExecutions = new ArrayList<String>(topExecutions.size() * 2);
    	List<String> subExecutions = ActivitiFinderUtil.findSubExecutions(topExecutions);
    	
    	allSubExecutions.addAll(subExecutions);
    	
    	// iterate
    	allSubExecutions.addAll(getSubExecutions(subExecutions));
    	
    	return allSubExecutions;
    }
    
    /**
     * Returns active UserTask names for selected instances.
     * 
     * @param instanceIds
     * @return
     * @throws SystemException
     */
    @Override
    public Set<String> findUniqueUserTaskNames(List<String> executionIds)
            throws SystemException {
        // find uniqie task names
        List<String> lstTasks = ActivitiFinderUtil
                .findUniqueUserTaskNames(executionIds);
        HashSet<String> res = new HashSet<String>(lstTasks.size());
        res.addAll(lstTasks);

        return res;
    }

    /**
     * Returns active UserTask assignees for selected instances.
     * 
     * @param instanceIds
     * @return
     */
    @Override
    public Set findUniqueUserTaskAssignees(List<String> executionIds)
            throws SystemException {
        // find uniqie assignees
        List lstAsg = ActivitiFinderUtil
                .findUniqueUserTaskAssignees(executionIds);
        HashSet res = new HashSet(lstAsg.size());
        res.addAll(lstAsg);

        return res;
    }

    /**
     * Returns top level process instances, filtered by active user task.
     * 
     * @param taskName
     *            - user task name
     * @param assigneeUser
     *            - task assignee
     * @param candidateRole
     *            - candidate role for task
     * @return
     * @throws SystemException
     */
    @Override
    public List<String> findTopLevelProcessInstances(String taskName,
            String assigneeUser, String candidateRole) throws SystemException {

        List<Object[]> lstExec = ActivitiFinderUtil.findUserTasks(taskName,
                assigneeUser, candidateRole);
        if (lstExec.size() == 0)
            return new ArrayList<String>(0);

        ArrayList<String> lstInstances = new ArrayList<String>(
                lstExec.size() * 2);
        extractColumn(lstExec, 1, lstInstances);

        ArrayList<String> lstSuperExec = new ArrayList<String>(
                lstInstances.size());
        extractColumn(lstExec, 2, lstSuperExec);
        extractColumn(lstExec, 3, lstSuperExec);
        
        while (lstSuperExec.size() > 0) {
            lstExec = ActivitiFinderUtil.findSuperExecutions(lstSuperExec);
            extractColumn(lstExec, 1, lstInstances);

            lstSuperExec.clear();
            extractColumn(lstExec, 2, lstSuperExec);
        }

        return lstInstances;
    }

    private void extractColumn(List<Object[]> source, int ncol, List dest) {
        for (Object[] row : source) {
            if (row[ncol] != null) {
                dest.add(row[ncol]);
            }
        }
    }
    
    private void checkServices() {
        if (runtimeService == null) {
            try {
                ApplicationContext context = ApplicationContextProvider.getApplicationContext();
                runtimeService = context.getBean(RuntimeService.class);  
                processEngine = context.getBean(ProcessEngine.class);  
            } catch (Exception e) {
                _log.error(e, e);
                throw new RuntimeException(e.getMessage());
            }        
        }
    }

    /** Suspend workflow instance
     * 
     */
    @Override
    public boolean suspendWorkflowInstance(long companyId,
            long workflowInstanceId) throws WorkflowException {
        
        checkServices();
        
        String procInstanceId = String.valueOf(workflowInstanceId);
        ProcessInstanceQuery qry = runtimeService.createProcessInstanceQuery().processInstanceId(procInstanceId);
        ProcessInstance inst = qry.singleResult();
        if (inst == null) {
            return false;
        } else {
            if (inst.isSuspended()) {
                return true;
            }
            
            runtimeService.suspendProcessInstanceById(procInstanceId);
            inst = qry.singleResult();
            return inst.isSuspended();
        }
    }

    /** Resume workflow instance
     * 
     */
    @Override
    public boolean resumeWorkflowInstance(long companyId,
            long workflowInstanceId) throws WorkflowException {

        checkServices();
        
        String procInstanceId = String.valueOf(workflowInstanceId);
        ProcessInstanceQuery qry = runtimeService.createProcessInstanceQuery().processInstanceId(procInstanceId);
        ProcessInstance inst = qry.singleResult();
        if (inst == null) {
            return false;
        } else {
            if (! inst.isSuspended()) {
                return true;
            }
            
            runtimeService.activateProcessInstanceById(procInstanceId);
            inst = qry.singleResult();
            return (!inst.isSuspended());
        }
        
    }
    
    @Override
    public boolean stopWorkflowInstance(long companyId, long userId, long workflowInstanceId, String comment) throws WorkflowException {
        _log.info("User " + userId + " stops workflow instance " + workflowInstanceId + " with reason: " + comment);
        
        checkServices();
        
        Authentication.setAuthenticatedUserId(String.valueOf(userId));
        
        String procInstanceId = String.valueOf(workflowInstanceId);
        ProcessInstanceQuery qry = runtimeService.createProcessInstanceQuery().processInstanceId(procInstanceId);
        ProcessInstance inst = qry.singleResult();
        if (inst == null) {
            return false;
        } else {
    		WorkflowLogEntry workflowLogEntry = new WorkflowLogEntry();
    		workflowLogEntry.setType(WorkflowLogConstants.INSTANCE_STOP);
    		workflowLogEntry.setComment(comment);
    		workflowLogEntry.setAssigneeUserId(userId);
    		workflowLogEntry.setState(null);
    		
    		CommandExecutor commandExecutor = ((ProcessEngineImpl) processEngine)
    				.getProcessEngineConfiguration().getCommandExecutor();

    		commandExecutor.execute(new AddWorkflowLogEntryCmd(null, procInstanceId, workflowLogEntry));
        	
            runtimeService.deleteProcessInstance(procInstanceId, comment);
            return true;
        }
    }
}
