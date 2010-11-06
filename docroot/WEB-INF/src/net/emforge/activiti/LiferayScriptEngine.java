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

import org.activiti.engine.runtime.Execution;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.scripting.ScriptingUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.model.User;
import com.liferay.portal.service.UserLocalServiceUtil;

public class LiferayScriptEngine implements ScriptEngine {
	private static Log _log = LogFactoryUtil.getLog(LiferayScriptEngine.class);

	@Override
	public void setBindings(Bindings arg0, int arg1) {
		// we just don't need it in activiti

	}
	
	@Override
	public Object eval(String script, Bindings bindings) throws ScriptException {
		_log.info("Perform Script: " + script);
		
		try {
			Map<String, Serializable> workflowContext = toWorkflowContext(bindings);
				
			Map<String, Object> inputObjects = new HashMap<String, Object>(workflowContext);
			inputObjects.put("workflowContext", workflowContext);
			
			Long companyId = GetterUtil.getLong((String)workflowContext.get(WorkflowConstants.CONTEXT_COMPANY_ID));
			if (companyId != null) {
				User user = UserLocalServiceUtil.getDefaultUser(companyId);
	
				inputObjects.put(WorkflowConstants.CONTEXT_USER_ID, String.valueOf(user.getUserId()));
				
				ScriptingUtil.exec(null, inputObjects, "javascript", script);
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
		
		for (String varName : bindings.keySet()) {
			workflowContext.put(varName, (Serializable)bindings.get(varName));
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
		_log.error("Method is not implemented"); // TODO
		return null;
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
