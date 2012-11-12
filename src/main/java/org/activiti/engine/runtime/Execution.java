package org.activiti.engine.runtime;




/**
 * Represent a 'path of execution' in a process instance.
 * 
 * Note that a {@link ProcessInstance} also is an execution.
 * 
 * @author Joram Barrez
 */
public interface Execution {
  
  /**
   * The unique identifier of the execution.
   */
  String getId();
  
  /**
   * Indicates if the execution is ended.
   */
  boolean isEnded();
  
  /** Id of the root of the execution tree representing the process instance.
   * It is the same as {@link #getId()} if this execution is the process instance. */ 
  String getProcessInstanceId();
  
  String getLaneName();
}
