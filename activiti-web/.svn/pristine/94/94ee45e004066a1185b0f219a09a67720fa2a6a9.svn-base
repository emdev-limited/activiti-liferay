package org.activiti.engine.impl.variable;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

public class CustomObjectInputStream extends ObjectInputStream {

	public CustomObjectInputStream(InputStream in) throws IOException {
		super(in);
	}

	@Override
	public Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
		ClassLoader currentTccl = null;
		try {			
			currentTccl = new CustomClassLoader();
			return currentTccl.loadClass(desc.getName());
		} catch (Exception e) {
		}
		return super.resolveClass(desc);

	}

}
