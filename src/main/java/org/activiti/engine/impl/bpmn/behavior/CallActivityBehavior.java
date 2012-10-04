/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.engine.impl.bpmn.behavior;

import java.util.ArrayList;
import java.util.List;

import net.emforge.activiti.WorkflowInstanceManagerImpl;
import net.emforge.activiti.dao.ProcessInstanceExtensionDao;
import net.emforge.activiti.entity.ProcessInstanceExtensionImpl;
import net.emforge.activiti.spring.ApplicationContextProvider;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.bpmn.data.AbstractDataAssociation;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.pvm.PvmProcessInstance;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.pvm.delegate.SubProcessActivityBehavior;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.runtime.Execution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;


/**
 * Implementation of the BPMN 2.0 call activity
 * (limited currently to calling a subprocess and not (yet) a global task).
 * 
 * @author Joram Barrez
 */
public class CallActivityBehavior extends AbstractBpmnActivityBehavior implements SubProcessActivityBehavior {
	private static Log _log = LogFactoryUtil.getLog(CallActivityBehavior.class);
  
  protected String processDefinitonKey;
  private List<AbstractDataAssociation> dataInputAssociations = new ArrayList<AbstractDataAssociation>();
  private List<AbstractDataAssociation> dataOutputAssociations = new ArrayList<AbstractDataAssociation>();
  private Expression processDefinitionExpression;
  
//  @Autowired
//  protected ProcessInstanceExtensionDao processInstanceExtensionDao;

  public CallActivityBehavior(String processDefinitionKey) {
    this.processDefinitonKey = processDefinitionKey;
  }
  
  public CallActivityBehavior(Expression processDefinitionExpression) {
    super();
    this.processDefinitionExpression = processDefinitionExpression;
  }

  public void addDataInputAssociation(AbstractDataAssociation dataInputAssociation) {
    this.dataInputAssociations.add(dataInputAssociation);
  }

  public void addDataOutputAssociation(AbstractDataAssociation dataOutputAssociation) {
    this.dataOutputAssociations.add(dataOutputAssociation);
  }

  public void execute(ActivityExecution execution) throws Exception {
    
    if (processDefinitionExpression != null) {
      processDefinitonKey = (String) processDefinitionExpression.getValue(execution);
    }
    
    ProcessDefinitionImpl processDefinition = Context
      .getProcessEngineConfiguration()
      .getDeploymentCache()
      .findDeployedLatestProcessDefinitionByKey(processDefinitonKey);
    
    PvmProcessInstance subProcessInstance = execution.createSubProcessInstance(processDefinition);
    
    // copy ALL context variables
    ProcessInstanceExtensionImpl procInstImpl = new ProcessInstanceExtensionImpl();
    for (String varName : execution.getVariableNames()) {
    	Object value = execution.getVariable(varName);
    	subProcessInstance.setVariable(varName, value);
    	
    	//Set Variables to procInstImpl
    	if (varName.equals(WorkflowConstants.CONTEXT_COMPANY_ID)) {
    		procInstImpl.setCompanyId(Long.valueOf((String)value));
    	}
    	if (varName.equals(WorkflowConstants.CONTEXT_USER_ID)) {
    		procInstImpl.setUserId(Long.valueOf((String)value));
    	}
    	if (varName.equals(WorkflowConstants.CONTEXT_GROUP_ID)) {
    		procInstImpl.setGroupId(Long.valueOf((String)value));
    	}
    	if (varName.equals(WorkflowConstants.CONTEXT_ENTRY_CLASS_NAME)) {
    		procInstImpl.setClassName((String)value);
    	}
    	if (varName.equals(WorkflowConstants.CONTEXT_ENTRY_CLASS_PK)) {
    		procInstImpl.setClassPK(Long.valueOf((String)value));
    	}
    }
    
    // copy process variables
    for (AbstractDataAssociation dataInputAssociation : dataInputAssociations) {
      Object value = null;
      if (dataInputAssociation.getSourceExpression()!=null) {
        value = dataInputAssociation.getSourceExpression().getValue(execution);
      }
      else {
        value = execution.getVariable(dataInputAssociation.getSource());
      }
      subProcessInstance.setVariable(dataInputAssociation.getTarget(), value);
    }
    
    subProcessInstance.start();
    
    if (subProcessInstance instanceof Execution) {
    	Execution ex = (Execution) subProcessInstance;
        // copy ALL context variables
    	procInstImpl.setProcessInstanceId(ex.getId());
        
        ApplicationContext ctx = ApplicationContextProvider.getApplicationContext();
        ProcessInstanceExtensionDao processInstanceExtensionDao = ctx.getBean(ProcessInstanceExtensionDao.class);
        Long id = (Long)processInstanceExtensionDao.save(procInstImpl);
    	_log.info("Stored new process instance ext " + procInstImpl.getId() + " -> " + id);
    }
    
  }
  
  public void completing(DelegateExecution execution, DelegateExecution subProcessInstance) throws Exception {
    // only data.  no control flow available on this execution.

	// copy ALL varibles from subprocess
    for (String varName : subProcessInstance.getVariableNames()) {
    	Object value = subProcessInstance.getVariable(varName);
    	execution.setVariable(varName, value);
    }
	    
    // copy process variables
    for (AbstractDataAssociation dataOutputAssociation : dataOutputAssociations) {
      Object value = null;
      if (dataOutputAssociation.getSourceExpression()!=null) {
        value = dataOutputAssociation.getSourceExpression().getValue(subProcessInstance);
      }
      else {
        value = subProcessInstance.getVariable(dataOutputAssociation.getSource());
      }
      
      execution.setVariable(dataOutputAssociation.getTarget(), value);
    }
  }

  public void completed(ActivityExecution execution) throws Exception {
    // only control flow.  no sub process instance data available
    leave(execution);
  }

}
