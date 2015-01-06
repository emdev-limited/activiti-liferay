package net.emforge.activiti.log;

import net.emforge.activiti.log.WorkflowLogConstants;

public class WorkflowLogTypeMapperUtil {
	
	public static int mapToLiferayType(String type) {
		
		if (type.equals(WorkflowLogEntry.TASK_ASSIGN)) {
			return WorkflowLogConstants.TASK_ASSIGN;
		}
		if (type.equals(WorkflowLogEntry.TASK_COMPLETION)) {
			return WorkflowLogConstants.TASK_COMPLETION;
		}
		if (type.equals(WorkflowLogEntry.TASK_UPDATE)) {
			return WorkflowLogConstants.TASK_UPDATE;
		}
		if (type.equals(WorkflowLogEntry.INSTANCE_STOP)) {
			return WorkflowLogConstants.INSTANCE_STOP;
		}
		if (type.equals(WorkflowLogEntry.SERVICE)) {
			return WorkflowLogConstants.SERVICE;
		}
		if (type.equals(WorkflowLogEntry.COMMENT)) {
			return WorkflowLogConstants.COMMENT;
		}
		return -1;
	}
	
	public static String mapToActivitiType(int type) {
		
		if (type == WorkflowLogConstants.TASK_ASSIGN) {
			return WorkflowLogEntry.TASK_ASSIGN;
		}
		if (type == WorkflowLogConstants.TASK_COMPLETION) {
			return WorkflowLogEntry.TASK_COMPLETION;
		}
		if (type == WorkflowLogConstants.TASK_UPDATE) {
			return WorkflowLogEntry.TASK_UPDATE;
		}
		if (type == WorkflowLogConstants.INSTANCE_STOP) {
			return WorkflowLogEntry.INSTANCE_STOP;
		}
		if (type == WorkflowLogConstants.SERVICE) {
			return WorkflowLogEntry.SERVICE;
		}
		if (type == WorkflowLogConstants.COMMENT) {
			return WorkflowLogEntry.COMMENT;
		}
		return "";
	}

}
