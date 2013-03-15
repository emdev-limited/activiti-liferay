package org.activiti.engine.impl.variable;

import java.io.InputStream;
import java.net.URL;

import com.liferay.portal.kernel.util.PortalClassLoaderUtil;

public class CustomClassLoader extends ClassLoader {
   
	ClassLoader portalContextClassLoader;
	
	public CustomClassLoader(){
        super(CustomClassLoader.class.getClassLoader());
        portalContextClassLoader = PortalClassLoaderUtil.getClassLoader();
    }
	
	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		Class<?> clazz = null;
		try {
			clazz = super.loadClass(name);
		} catch (ClassNotFoundException e) {
			clazz = portalContextClassLoader.loadClass(name);
		}
		return clazz;
	}
	
	@Override
	protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		Class<?> clazz = null;
		try {
			clazz = super.loadClass(name, resolve);
		} catch (ClassNotFoundException e) {
			clazz = portalContextClassLoader.loadClass(name);
		}
		return clazz;
	}
	
	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		Class<?> clazz = null;
		try {
			clazz = super.findClass(name);
		} catch (ClassNotFoundException e) {
			clazz = portalContextClassLoader.loadClass(name);
		}
		return clazz;
	}
	
	@Override
	protected URL findResource(String name) {
		return super.findResource(name);
	}
	
	@Override
	public InputStream getResourceAsStream(String name) {
		return super.getResourceAsStream(name);
	}
}