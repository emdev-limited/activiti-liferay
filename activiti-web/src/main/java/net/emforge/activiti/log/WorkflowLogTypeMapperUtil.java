package net.emforge.activiti.log;

import com.liferay.portal.kernel.workflow.WorkflowLog;

public class WorkflowLogTypeMapperUtil {
	
	public static int mapToLiferayType(String type) {
		
		if(type.equals(WorkflowLogEntry.TASK_ASSIGN)) {
			return WorkflowLog.TASK_ASSIGN;
		}
		if(type.equals(WorkflowLogEntry.TASK_COMPLETION)) {
			return WorkflowLog.TASK_COMPLETION;
		}
		if(type.equals(WorkflowLogEntry.TASK_UPDATE)) {
			return WorkflowLog.TASK_UPDATE;
		}
		return -1;
	}
	
	public static String mapToActivitiType(int type) {
		
		if(type == WorkflowLog.TASK_ASSIGN) {
			return WorkflowLogEntry.TASK_ASSIGN;
		}
		if(type == WorkflowLog.TASK_COMPLETION) {
			return WorkflowLogEntry.TASK_COMPLETION;
		}
		if(type == WorkflowLog.TASK_UPDATE) {
			return WorkflowLogEntry.TASK_UPDATE;
		}
		return "";
	}

}
