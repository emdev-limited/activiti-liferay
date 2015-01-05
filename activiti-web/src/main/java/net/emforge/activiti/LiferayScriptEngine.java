package net.emforge.activiti;

import java.io.Reader;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.scripting.ScriptingUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.model.User;
import com.liferay.portal.service.UserLocalServiceUtil;

public class LiferayScriptEngine implements ScriptEngine {
	private static Log _log = LogFactoryUtil.getLog(LiferayScriptEngine.class);

	private String liferayScriptName;
	private ScriptEngineFactory factory;
	
	public LiferayScriptEngine(ScriptEngineFactory factory, String liferayScriptName) {
		this.factory = factory;
		this.liferayScriptName = liferayScriptName;
	}
	
	@Override
	public void setBindings(Bindings arg0, int arg1) {
		// we just don't need it in activiti

	}
	
	@Override
	public Object eval(String script, Bindings bindings) throws ScriptException {
		_log.info("Perform Script (" + liferayScriptName + "): " + script);
		
		try {
			Map<String, Serializable> workflowContext = toWorkflowContext(bindings);
				
			Map<String, Object> inputObjects = new HashMap<String, Object>(workflowContext);
			inputObjects.put("workflowContext", workflowContext);
			
			Long companyId = GetterUtil.getLong((String)workflowContext.get(WorkflowConstants.CONTEXT_COMPANY_ID));
			if (companyId != null) {
				User user = UserLocalServiceUtil.getDefaultUser(companyId);
	
				inputObjects.put(WorkflowConstants.CONTEXT_USER_ID, String.valueOf(user.getUserId()));
				//This commented part related to attempts to resolve this issue: http://issues.liferay.com/browse/LPS-27713
//				Long userId = (Long) workflowContext.get("latestActivitiUserId");
//				if (userId != null && userId > 0) {
//					try {
//						inputObjects.put(WorkflowConstants.CONTEXT_USER_ID, String.valueOf(userId));
//						//Set permissions for this thread
//						PrincipalThreadLocal.setName(userId);
//						User usr = UserLocalServiceUtil.getUserById(userId);
//						PermissionChecker permissionChecker =
//							PermissionCheckerFactoryUtil.create(usr, false);
//						PermissionThreadLocal.setPermissionChecker(permissionChecker);
//					} catch (Exception e) {
//						_log.warn("Could not set PermissionChecker");
//					}
//				}
				
				//ScriptingUtil.exec(null, inputObjects, liferayScriptName, script);
				ScriptingUtil.exec(null, inputObjects, liferayScriptName, script, StringPool.EMPTY_ARRAY);
			}
		} catch (Exception ex) {
			_log.error("Cannot execute Script", ex);
			throw new ScriptException(ex);
		}
		return null;
	}

	/** Convert Binding to Workflow Context
	 * 
	 * @param bindings
	 * @return
	 */
	private Map<String, Serializable> toWorkflowContext(Bindings bindings) {
		Map<String, Serializable> workflowContext = new HashMap<String, Serializable>();
		
		for (String key : bindings.keySet()) {
			workflowContext.put(key, (Serializable)bindings.get(key));
		}
			
		return workflowContext;
	}

	@Override
	public Bindings createBindings() {
		_log.error("Method is not implemented"); // TODO
		return null;
	}

	@Override
	public Object eval(String arg0) throws ScriptException {
		_log.error("Method is not implemented"); // TODO
		return null;
	}

	@Override
	public Object eval(Reader arg0) throws ScriptException {
		_log.error("Method is not implemented"); // TODO
		return null;
	}

	@Override
	public Object eval(String arg0, ScriptContext arg1) throws ScriptException {
		_log.error("Method is not implemented"); // TODO
		return null;
	}

	@Override
	public Object eval(Reader arg0, ScriptContext arg1) throws ScriptException {
		_log.error("Method is not implemented"); // TODO
		return null;
	}


	@Override
	public Object eval(Reader arg0, Bindings arg1) throws ScriptException {
		_log.error("Method is not implemented"); // TODO
		return null;
	}

	@Override
	public Object get(String arg0) {
		_log.error("Method is not implemented"); // TODO
		return null;
	}

	@Override
	public Bindings getBindings(int arg0) {
		_log.error("Method is not implemented"); // TODO
		return null;
	}

	@Override
	public ScriptContext getContext() {
		_log.error("Method is not implemented"); // TODO
		return null;
	}

	@Override
	public ScriptEngineFactory getFactory() {
		return factory;
	}

	@Override
	public void put(String arg0, Object arg1) {
		_log.error("Method is not implemented"); // TODO

	}

	@Override
	public void setContext(ScriptContext arg0) {
		_log.error("Method is not implemented"); // TODO

	}

}
