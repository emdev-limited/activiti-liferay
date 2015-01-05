package net.emforge.activiti;

import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

/** Special Script Engine to forward all script-tasks to Liferay
 * 
 * @author akakunin
 *
 */
public class LiferayScriptEngineFactory implements ScriptEngineFactory {
	private static Log _log = LogFactoryUtil.getLog(LiferayScriptEngineFactory.class);
	
	private String activitiScriptName;
	private String liferayScriptName;
	
	public LiferayScriptEngineFactory(String activitiScriptName, String liferayScriptName) {
		this.activitiScriptName = activitiScriptName;
		this.liferayScriptName = liferayScriptName;
	}
	
	@Override
	public String getEngineName() {
		return activitiScriptName;
	}

	@Override
	public String getEngineVersion() {
		return "1.0";
	}

	@Override
	public List<String> getExtensions() {
		return null;
	}

	@Override
	public String getLanguageName() {
		return activitiScriptName;
	}

	@Override
	public String getLanguageVersion() {
		return "1.0";
	}

	@Override
	public String getMethodCallSyntax(String arg0, String arg1, String... arg2) {
		_log.error("Method is not implemented"); // TODO
		return null;
	}

	@Override
	public List<String> getMimeTypes() {
		_log.error("Method is not implemented"); // TODO
		return null;
	}

	@Override
	public List<String> getNames() {
		_log.error("Method is not implemented"); // TODO
		return null;
	}

	@Override
	public String getOutputStatement(String arg0) {
		_log.error("Method is not implemented"); // TODO
		return null;
	}

	@Override
	public Object getParameter(String arg0) {
		_log.error("Method is not implemented"); // TODO
		return null;
	}

	@Override
	public String getProgram(String... arg0) {
		_log.error("Method is not implemented"); // TODO
		return null;
	}

	@Override
	public ScriptEngine getScriptEngine() {
		return new LiferayScriptEngine(this, liferayScriptName);
	}

}
