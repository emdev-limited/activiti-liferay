package net.emforge.activiti.util;

import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;

public class SpringUtils {
	@SuppressWarnings({"unchecked"})
	public static <T> T getTargetObject(Object proxy, Class<T> targetClass) throws Exception {
	  if (AopUtils.isJdkDynamicProxy(proxy)) {
	    return (T) ((Advised)proxy).getTargetSource().getTarget();
	  } else {
	    return (T) proxy; // expected to be cglib proxy then, which is simply a specialized class
	  }
	}
}
