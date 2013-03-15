package net.emforge.activiti.spring;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

public class ContextLoaderListener extends org.springframework.web.context.ContextLoaderListener {
	private static ServletContext initSc;
	private static boolean isInitialized = true;
	
	@Override
	public void contextInitialized(ServletContextEvent event) {
		initSc = event.getServletContext();
		if (!isInitialized)
			super.contextInitialized(event);
	}

	public static ServletContext getInitServletContext() {
		return initSc;
	}
	
	public static void setIsInitialized(boolean bInitz) {
		isInitialized = bInitz;
	}

	public static boolean isInitialized() {
		return isInitialized;
	}
}
