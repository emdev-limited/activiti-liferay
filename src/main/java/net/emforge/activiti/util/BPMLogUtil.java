package net.emforge.activiti.util;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.DelegateTask;
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
	
	public static void echo(String msg){
		_log.info("[" + _up(msg) + "]");
	}

	private static String _up(String msg) {
		if(msg == null || msg.length() == 0)
			return "<NULL>";
		else
			return StringUtil.upperCase(msg);
	}
	
	private static String _getInfo(DelegateExecution execution) {
		return "PROCESS EXECUTION[id:" + execution.getId() + ", piId:" + execution.getProcessInstanceId() + "]:{" +
				"event: " + _up(execution.getEventName()) + ", " + 
				"key: " + _up(execution.getProcessBusinessKey()) +
			"}";
	}
	
	private static String _getTrace(DelegateExecution execution) {
		return "PROCESS EXECUTION[id:" + execution.getId() + ", piId:" + execution.getProcessInstanceId() + "]:{" +
				"event:" + _up(execution.getEventName()) + ", " + 
				"key:" + _up(execution.getProcessBusinessKey()) + ", " + 
				"VARS:[" + execution.getVariables() + "]" +
			"}";
	}
	
	private static String _getInfo(DelegateTask task) {
		DelegateExecution execution = task.getExecution();
		return "PROCESS TASK[" + _up(task.getName()) + "]:" + 
					"[id:" + task.getId() + ", eId:" + execution.getId() + ", piId:" + execution.getProcessInstanceId() + "]:{" +
				"event:" + _up(task.getEventName()) + ", " + 
				"assignee:" + _up(task.getAssignee()) + ", " +
				"pdId:" + task.getProcessDefinitionId() + ", " +
				"tdKey:" + _up(task.getTaskDefinitionKey()) 
			+ "}";
	}
	
	private static String _getTrace(DelegateTask task) {
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
	
	public static void trace(DelegateExecution execution){
		_log.info(_getTrace(execution));
	}
	
	public static void trace(DelegateTask task) {
		_log.info(_getTrace(task));
	}
	
	public static void info(DelegateExecution execution){
		_log.info(_getInfo(execution));
	}
	
	public static void info(DelegateTask task) {
		_log.info(_getInfo(task));
	}
	
	public static void warn(DelegateExecution execution){
		_log.warn(_getInfo(execution));
	}
	
	public static void warn(DelegateTask task) {
		_log.warn(_getInfo(task));
	}
	
	public static void error(DelegateExecution execution){
		_log.error(_getTrace(execution));
	}
	
	public static void error(DelegateTask task) {
		_log.error(_getTrace(task));
	}

	public static void warn(String msg){
		_log.warn(_up(msg));
	}

	public static void error(String msg){
		_log.error(_up(msg));
	}

	public static void info(String msg, DelegateExecution execution){
		_log.info(_up(msg) + ": ==> " + _getInfo(execution));
	}

	public static void warn(String msg, DelegateExecution execution){
		_log.warn(_up(msg) + ": ==> " + _getInfo(execution));
	}

	public static void error(String msg, DelegateExecution execution){
		_log.error(_up(msg) + ": ==> " + _getTrace(execution));
	}
	
	public static void info(String msg, DelegateTask task){
		_log.info(_up(msg) + ": ==> " + _getInfo(task));
	}

	public static void warn(String msg, DelegateTask task){
		_log.warn(_up(msg) + ": ==> " + _getInfo(task));
	}

	public static void error(String msg, DelegateTask task){
		_log.error(_up(msg) + ": ==> " + _getTrace(task));
	}
}
