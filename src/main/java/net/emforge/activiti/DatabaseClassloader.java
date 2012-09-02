package net.emforge.activiti;

import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.ResourceEntity;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

/**
 * Custom {@link ClassLoader} implementation to load classes from Activiti database. 
 *
 * @author Oliver Teichmann, PRODYNA AG
 *
 */
public class DatabaseClassloader extends ClassLoader {
	private static Log _log = LogFactoryUtil.getLog(DatabaseClassloader.class);
	
	public DatabaseClassloader() {
		super();
	}
	
	public DatabaseClassloader(ClassLoader parent) {
		super(parent);
	}

	@Override
	public Class<?> loadClass(String className) throws ClassNotFoundException {
		
		_log.debug("Try to load class " + className);

		// Try to load class form deployment resources
		String fileName = "classes/" + className.replace('.', '/') + ".class";
		
		ResourceEntity resource = Context.getExecutionContext().getDeployment()
				.getResource(fileName);

		if(resource != null) {
			byte[] classBytes = resource.getBytes();
	
			if (classBytes != null) {
				ProcessDefinitionEntity processDefinition = Context
				.getExecutionContext().getProcessDefinition();
	
				int packageIndex = className.lastIndexOf('.');
	
				if (packageIndex != -1) {
					String packageName = className.substring(0, packageIndex);
	
					if (getPackage(packageName) == null) {
						definePackage(packageName, null, null, null,
								processDefinition.getName(),
								Integer.toString(processDefinition.getVersion()),
								null, null);
					}
				}
				return defineClass(className, classBytes, 0, classBytes.length);
			}
		}
		
		// Try to load class from parent class loader
		Class<?> clazz = super.loadClass(className);
		if(clazz != null) {
			return clazz;
		}

		throw new ClassNotFoundException(className);
	}

}
