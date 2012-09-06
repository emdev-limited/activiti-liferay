package net.emforge.activiti.spring;

/**
 * Runtime initialization and wiring for spring beans, described as 
 * lazy-init="true" and autowire="no".
 */
public interface Initializable {
	public void init() throws Exception;
}
