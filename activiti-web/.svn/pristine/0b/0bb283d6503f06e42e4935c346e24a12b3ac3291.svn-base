/**
 *
 */
package net.emforge.activiti.util;

import java.util.Map;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.pvm.PvmProcessElement;
import org.springframework.stereotype.Service;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringUtil;


/**
 * @author Khalutin Vladimir
 *
 */
@Service("bpmLog")
public class BPMLogUtil {
	private static Log _log = LogFactoryUtil.getLog("BPMS"/*BPMLogUtil.class*/);
	
	public void start() {
		ExecutionEntity execution = Context.getExecutionContext().getExecution();
		start(execution);
	}
	
	public void start(DelegateExecution execution) {
		String startLogText = ">>> START: ";
		Map<String, Object> vars = execution.getVariables();
		if(execution instanceof ExecutionEntity) {
			ExecutionEntity executionEntity = (ExecutionEntity)execution;
			if(executionEntity.getParent() != null) {
				startLogText += executionEntity.getParent().getProcessDefinition().getName() + " => ";
				startLogText += executionEntity.getEventSource().getProperty("name") +": ";
				startLogText += executionEntity.getTransition();
				_log.info(startLogText + ": VARS: " + vars);
			} else {
				startLogText += executionEntity.getProcessDefinition().getName();
				_log.info(startLogText + ": VARS: " + vars);
			}
		} else {
			_log.info(startLogText + execution.getCurrentActivityName() + ": VARS: " + vars);
		}
	}
	
	public void end() {
		ExecutionEntity execution = Context.getExecutionContext().getExecution();
		end(execution);
	}
	
	public void end(DelegateExecution execution) {
		String startLogText = "<<< END: ";
		Map<String, Object> vars = execution.getVariables();
		if(execution instanceof ExecutionEntity) {
			ExecutionEntity executionEntity = (ExecutionEntity)execution;
			if(executionEntity.getParent() != null) {
				startLogText += executionEntity.getParent().getProcessDefinition().getName() + " <= ";
				PvmProcessElement eventSource = executionEntity.getEventSource();
				if (eventSource != null)
					startLogText += eventSource.getProperty("name") +": ";
				startLogText += executionEntity.getTransition();
				_log.info(startLogText + ": VARS: " + vars);
			} else {
				startLogText += executionEntity.getProcessDefinition().getName() +": ";
				startLogText += executionEntity.getActivity();
				_log.info(startLogText + ": VARS: " + vars);
			}
		} else {
			_log.info(startLogText + execution.getCurrentActivityName() + ": VARS: " + vars);
		}
	}
	
	public void echo(String msg){
		_log.info("[" + _up(msg) + "]");
	}

	private String _up(String msg) {
		if(msg == null || msg.length() == 0)
			return "<NULL>";
		else
			return StringUtil.upperCase(msg);
	}
	
	private String _getInfo(DelegateTask task) {
		DelegateExecution execution = task.getExecution();
		return "PROCESS TASK[" + _up(task.getName()) + "]:" + 
					"[id:" + task.getId() + ", eId:" + execution.getId() + ", piId:" + execution.getProcessInstanceId() + "]:{" +
				"event:" + _up(task.getEventName()) + ", " + 
				"assignee:" + _up(task.getAssignee()) + ", " +
				"pdId:" + task.getProcessDefinitionId() + ", " +
				"tdKey:" + _up(task.getTaskDefinitionKey()) 
			+ "}";
	}
	
	private String _getTrace(DelegateExecution execution) {
		Map<String, Object> vars = execution.getVariables();
		vars.remove("bpmLog");
		vars.remove("bpmTimer");
		return "PROCESS EXECUTION[" + execution.getProcessDefinitionId() + "] " + 
				"[id:" + execution.getId() + ", piId:" + execution.getProcessInstanceId() + "], " +
				( (execution.getEventName() == null)? "" : "event:" + _up(execution.getEventName()) + ", " ) + 
				"activity: " + execution.getCurrentActivityId() + ", (" + 
				execution.getCurrentActivityName() + "), " + 
				"VARS:[" + vars + "]";
	}
	
	private String _getInfo(DelegateExecution execution) {
		return "PROCESS EXECUTION [" + execution.getProcessDefinitionId() + "] " + 
				"[id:" + execution.getId() + ", piId:" + execution.getProcessInstanceId() + "], " +
				( (execution.getEventName() == null)? "" : "event:" + _up(execution.getEventName()) + ", " ) +
				"activity: " + execution.getCurrentActivityId() + "(" + 
				execution.getCurrentActivityName() + ")";
	}
	
	private String _getTrace(DelegateTask task) {
		DelegateExecution execution = task.getExecution();
		return "PROCESS TASK[" + _up(task.getName()) + "]:" + 
					"[id:" + task.getId() + ", eId:" + execution.getId() + ", piId:" + execution.getProcessInstanceId() + "]:{" +
				"event:" + _up(task.getEventName()) + ", " + 
				"assignee:" + _up(task.getAssignee()) + ", " +
				"createDate:" + task.getCreateTime() + ", " +
				"dueDate:" + task.getDueDate() + ", " +
				"owner:" + _up(task.getOwner()) + ", " +
				"priority:" + task.getPriority() + ", " +
				"pdId:" + task.getProcessDefinitionId() + ", " +
				"piId:" + task.getProcessInstanceId() + ", " +
				"tdKey:" + _up(task.getTaskDefinitionKey()) + ", " +
				"VARS:[" + task.getVariables() + "]}";
	}
	
	public void complete(){
		ExecutionEntity execution = Context.getExecutionContext().getExecution();
		complete((DelegateTask)execution);
	}
	
	public void complete(DelegateTask task){
		_log.info(">>> COMPLETE: " + task + ": " + task.getVariables());
	}
	
	public void info(Object msg) {
		if (msg instanceof String) {
			info((String) msg);
		} else if (msg instanceof DelegateExecution) {
			info((DelegateExecution) msg);
		} else if (msg instanceof DelegateTask) {
			info((DelegateTask) msg);
		}
	}
	
	private void info(String msg){
		_log.info(_up(msg));
	}
	
	private void info(DelegateExecution execution){
		_log.info(_getInfo(execution));
	}
	
	private void info(DelegateTask task) {
		_log.info(_getInfo(task));
	}
	
	public void trace(){
		ExecutionEntity execution = Context.getExecutionContext().getExecution();
	
		if (execution instanceof DelegateExecution)
			trace((DelegateExecution)execution);
		else if (execution instanceof DelegateTask)
			trace((DelegateTask)execution);
	}

	public void trace(DelegateExecution execution){
		_log.info(_getTrace(execution));
	}
	
	public void trace(DelegateTask task) {
		_log.info(_getTrace(task));
	}
	
	public void warn(DelegateExecution execution){
		_log.warn(_getInfo(execution));
	}
	
	public void warn(DelegateTask task) {
		_log.warn(_getInfo(task));
	}
	
	public void error(DelegateExecution execution){
		_log.error(_getTrace(execution));
	}
	
	public void error(DelegateTask task) {
		_log.error(_getTrace(task));
	}

	public void warn(String msg){
		_log.warn(_up(msg));
	}

	public void error(String msg){
		_log.error(_up(msg));
	}

	

	public void warn(String msg, DelegateExecution execution){
		_log.warn(_up(msg) + ": ==> " + _getInfo(execution));
	}

	public void error(String msg, DelegateExecution execution){
		_log.error(_up(msg) + ": ==> " + _getTrace(execution));
	}
	
	public void info(String msg, Object delegate){
		if(delegate instanceof DelegateExecution) {
			_log.info(_up(msg) + ": ==> " + _getInfo((DelegateExecution)delegate));
		} else if(delegate instanceof DelegateTask) {
			_log.info(_up(msg) + ": ==> " + _getInfo((DelegateTask)delegate));
		}
		
	}

	public void warn(String msg, DelegateTask task){
		_log.warn(_up(msg) + ": ==> " + _getInfo(task));
	}

	public void error(String msg, DelegateTask task){
		_log.error(_up(msg) + ": ==> " + _getTrace(task));
	}
	
	/*
	 public void info(Object...objects){
		ExecutionContext executionContext = Context.getExecutionContext();
		ExecutionEntity execution = executionContext.getExecution();
		
		if (objects.length == 0) {
			if (execution instanceof DelegateExecution)
				_log.info(_getInfo((DelegateExecution)execution));
			else if (execution instanceof DelegateTask)
				_log.info(_getInfo((DelegateTask)execution));
			
		} else if (objects.length == 1) {
			Object object = objects[0];
			if (object instanceof String)
				_log.info(_up((String)object));
			else if (execution instanceof DelegateExecution)
				_log.info(_getInfo((DelegateExecution)execution));
			else if (execution instanceof DelegateTask)
				_log.info(_getInfo((DelegateTask)execution));
			
		} else if (objects.length == 2) {
			String msg = (String)objects[0];
			Object object = objects[1];
			if (object instanceof DelegateExecution)
				_log.info(_up(msg) + ": ==> " + _getInfo((DelegateExecution)object));
			else if (object instanceof DelegateTask)
				_log.info(_up(msg) + ": ==> " + _getInfo((DelegateTask)object));
			
		} else {
			List<Object> list = new ArrayList<Object>();
			for (Object object: objects){
				list.add(object.getClass().getSimpleName() + ": " + object);
			}
			_log.info(list);
		}
	}
	
	public void trace(Object...objects){
		ExecutionContext executionContext = Context.getExecutionContext();
		ExecutionEntity execution = executionContext.getExecution();
		
		if (execution instanceof DelegateExecution)
			_log.info(_getTrace((DelegateExecution)execution));
		else if (execution instanceof DelegateTask)
			_log.info(_getTrace((DelegateTask)execution));
	}
	 */
}
