package net.emforge.activiti.spring;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;

import com.liferay.portal.kernel.bean.PortletBeanLocatorUtil;

public class ApplicationContextWrapper implements ApplicationContext, ApplicationContextAware {
	ApplicationContext applicationContext;
	
	public ApplicationContextWrapper() {
		
	}
	
	public ApplicationContextWrapper(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	
	@Override
	public boolean containsBean(String beanName) {
		if (beanName.contains("_")) {
			// try to get bean via context-path and beanName
			String[] beanDefs = beanName.split("_");
			try {
				Object bean = PortletBeanLocatorUtil.locate(beanDefs[0] + "-portlet", beanDefs[1]);
				return bean != null;
			} catch (Exception ex) {
				return false;
			}
		} else {
			return applicationContext.containsBean(beanName);
		}
	}
	
	@Override
	public Object getBean(String beanName) throws BeansException {
		if (beanName.contains("_")) {
			// try to get bean via context-path and beanName
			String[] beanDefs = beanName.split("_");
			return PortletBeanLocatorUtil.locate(beanDefs[0] + "-portlet", beanDefs[1]);
		} else {
			return applicationContext.getBean(beanName);
		}
	}


	
	
	
	@Override
	public AutowireCapableBeanFactory getAutowireCapableBeanFactory()
			throws IllegalStateException {
		return applicationContext.getAutowireCapableBeanFactory();
	}

	@Override
	public String getDisplayName() {
		return applicationContext.getDisplayName();
	}

	@Override
	public String getId() {
		return applicationContext.getId();
	}

	@Override
	public ApplicationContext getParent() {
		return applicationContext.getParent();
	}

	@Override
	public long getStartupDate() {
		return applicationContext.getStartupDate();
	}

	@Override
	public boolean containsBeanDefinition(String arg0) {
		return applicationContext.containsBeanDefinition(arg0);
	}

	@Override
	public <A extends Annotation> A findAnnotationOnBean(String arg0,
			Class<A> arg1) {
		return applicationContext.findAnnotationOnBean(arg0, arg1);
	}

	@Override
	public int getBeanDefinitionCount() {
		return applicationContext.getBeanDefinitionCount();
	}

	@Override
	public String[] getBeanDefinitionNames() {
		return applicationContext.getBeanDefinitionNames();
	}

	@Override
	public <T> Map<String, T> getBeansOfType(Class<T> arg0) throws BeansException {
		return applicationContext.getBeansOfType(arg0);
	}

	@Override
	public <T> Map<String, T> getBeansOfType(Class<T> arg0, boolean arg1,
			boolean arg2) throws BeansException {
		return applicationContext.getBeansOfType(arg0, arg1, arg2);
	}

	@Override
	public Map<String, Object> getBeansWithAnnotation(
			Class<? extends Annotation> arg0) throws BeansException {
		return applicationContext.getBeansWithAnnotation(arg0);
	}



	@Override
	public String[] getAliases(String arg0) {
		return applicationContext.getAliases(arg0);
	}

	@Override
	public <T> T getBean(Class<T> arg0) throws BeansException {
		return applicationContext.getBean(arg0);
	}

	@Override
	public <T> T getBean(String arg0, Class<T> arg1) throws BeansException {
		return applicationContext.getBean(arg0, arg1);
	}

	@Override
	public Object getBean(String arg0, Object... arg1) throws BeansException {
		return applicationContext.getBean(arg0, arg1);
	}

	@Override
	public Class<?> getType(String arg0) throws NoSuchBeanDefinitionException {
		return applicationContext.getType(arg0);
	}

	@Override
	public boolean isPrototype(String arg0)
			throws NoSuchBeanDefinitionException {
		return applicationContext.isPrototype(arg0);
	}

	@Override
	public boolean isSingleton(String arg0)
			throws NoSuchBeanDefinitionException {
		return applicationContext.isSingleton(arg0);
	}

	@Override
	public boolean containsLocalBean(String arg0) {
		return applicationContext.containsLocalBean(arg0);
	}

	@Override
	public BeanFactory getParentBeanFactory() {
		return applicationContext.getParentBeanFactory();
	}

	@Override
	public String getMessage(MessageSourceResolvable arg0, Locale arg1)
			throws NoSuchMessageException {
		return applicationContext.getMessage(arg0, arg1);
	}

	@Override
	public String getMessage(String arg0, Object[] arg1, Locale arg2)
			throws NoSuchMessageException {
		return applicationContext.getMessage(arg0, arg1, arg2);
	}

	@Override
	public String getMessage(String arg0, Object[] arg1, String arg2,
			Locale arg3) {
		return applicationContext.getMessage(arg0, arg1, arg2, arg3);
	}

	@Override
	public void publishEvent(ApplicationEvent arg0) {
		applicationContext.publishEvent(arg0);
	}

	@Override
	public Resource[] getResources(String arg0) throws IOException {
		return applicationContext.getResources(arg0);
	}

	@Override
	public ClassLoader getClassLoader() {
		return applicationContext.getClassLoader();
	}

	@Override
	public Resource getResource(String arg0) {
		return applicationContext.getResource(arg0);
	}

	@Override
	public Environment getEnvironment() {
		return applicationContext.getEnvironment();
	}

	@Override
	public String[] getBeanNamesForType(Class<?> arg0) {
		return applicationContext.getBeanNamesForType(arg0);
	}

	@Override
	public String[] getBeanNamesForType(Class<?> arg0, boolean arg1, boolean arg2) {
		return applicationContext.getBeanNamesForType(arg0, arg1, arg2);
	}

	@Override
	public boolean isTypeMatch(String arg0, Class<?> arg1) throws NoSuchBeanDefinitionException {
		return applicationContext.isTypeMatch(arg0, arg1);
	}

	@Override
	public String[] getBeanNamesForAnnotation(Class<? extends Annotation> arg0) {
		return applicationContext.getBeanNamesForAnnotation(arg0);
	}

	@Override
	public String getApplicationName() {
		return applicationContext.getApplicationName();
	}

	@Override
	public <T> T getBean(Class<T> arg0, Object... arg1) throws BeansException {
		// TODO Auto-generated method stub
		return null;
	}


}