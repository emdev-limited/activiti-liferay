package net.emforge.activiti.log;

/** Added custom log types
 * 
 * @author akakunin
 *
 */
public interface WorkflowLogConstants extends com.liferay.portal.kernel.workflow.WorkflowLog {
	
	/** Stop of the instance
	 * 
	 */
	public static final int INSTANCE_STOP = 10;
	
	/**
	 * Service
	 */
	public static final int SERVICE = 11;
	
	/**
	 * Comment
	 */
	public static final int COMMENT = 12;
}
