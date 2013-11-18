package net.emforge.activiti.task;

import net.emforge.activiti.WorkflowConstants;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;

/**
 * Save task output transition on complete
 * @author irina
 *
 */
public class SaveTransitionListener implements TaskListener {
    
    @Override
    public void notify(DelegateTask task) {
        Object outputTransition = task.getExecution().getVariable(WorkflowConstants.NAME_OUTPUT_TRANSITION);
        if (outputTransition != null) {
            task.setVariableLocal(WorkflowConstants.NAME_TASK_OUTPUT_TRANSITION, outputTransition);
        }
        
    }

}
