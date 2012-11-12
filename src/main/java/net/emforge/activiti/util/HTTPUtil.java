package net.emforge.activiti.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Map.Entry;

import org.activiti.engine.delegate.DelegateExecution;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.springframework.stereotype.Service;

import com.liferay.portal.kernel.io.unsync.UnsyncStringWriter;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.velocity.VelocityContext;
import com.liferay.portal.kernel.velocity.VelocityEngine;
import com.liferay.portal.kernel.velocity.VelocityEngineUtil;

@Service("httpUtil")
public class HTTPUtil {
	private static Log _log = LogFactoryUtil.getLog("BPMSHTTPUtil");
	
	public static void get(DelegateExecution execution){
		String url = "http://46.61.160.26:3098/sms?id=${DDLRecordFields.loanId}&to=${phone.number}&t=${msgType}&n=${n}&c=0&ds=${DDLRecordFields.paymentAmount}&dd=${DDLRecordFields.dueAmount}";
		sendGet(execution, url);
	}
	
	public static void sendGet(DelegateExecution execution, String url){
		//String evaluatedUrl = parseTemplate(execution.getId(), url, execution.getVariables());
		_log.info("sendGet evaluatedUrl: " + url);
		String response = sendHttpGet(url);
		_log.info("sendGet response: " + response);
	}
	
	public static String parseTemplate(String executionId, String velocityTemplateContent, Map<String,Object> context){
		VelocityEngine veloEngine = VelocityEngineUtil.getVelocityEngine();
		UnsyncStringWriter unsyncStringWriter = new UnsyncStringWriter();
		try {
			VelocityEngineUtil.init();
		} catch (Exception e1) {
			e1.printStackTrace();
			return velocityTemplateContent;
		}
		
		VelocityContext velocityContext = veloEngine.getWrappedStandardToolsContext();
		if (velocityContext == null) {
			velocityContext = veloEngine.getStandardToolsContext();
		}
		
		for (Entry<String, Object> o : context.entrySet()) {
			velocityContext.put(o.getKey(), o.getValue());
		}
		
		String velocityTemplateId = getTemplatePath(executionId);
		
		try {
			veloEngine.flushTemplate(velocityTemplateId);
			veloEngine.mergeTemplate(velocityTemplateId, velocityTemplateContent, velocityContext, unsyncStringWriter);
		} catch (Exception e) {
			_log.warn(e.getMessage() + " " + e.getCause());
		}
		
		return unsyncStringWriter.toString();
	}
	
	// TODO: add random, because cache
	public static String getTemplatePath(String executionId) {
		return "com.fsphere.activiti.velocity.execution." + executionId;
	}

	public final static String sendHttpGet(String url) {
		return sendHttpGet(url, 7000);
	}
	
	public final static String sendHttpGet(String url, int timeOut) {

		String responseBody = "";
		
		// set the connection timeout value to 7 seconds (7000 milliseconds)
	    final HttpParams httpParams = new BasicHttpParams();
	    HttpConnectionParams.setConnectionTimeout(httpParams, timeOut);
		
        DefaultHttpClient httpclient = new DefaultHttpClient(httpParams);
        try {
        	HttpGet httpget = new HttpGet(url);
            
        	_log.info("sendHttpGet url: " + url);
            System.out.println("executing request " + httpget.getURI());

            // Create a response handler
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            responseBody = httpclient.execute(httpget, responseHandler);
            System.out.println("----------------------------------------");
            System.out.println(responseBody);
            System.out.println("----------------------------------------");
        } catch (ConnectTimeoutException e) {
        	_log.error(e.getMessage());
        } catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IllegalStateException e){
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
            // When HttpClient instance is no longer needed,
            // shut down the connection manager to ensure
            // immediate deallocation of all system resources
            httpclient.getConnectionManager().shutdown();
        }
        
        return responseBody;
    }

	public String encode(DelegateExecution execution, String template) {
		// evaluate variables
		String result = parseTemplate(execution.getId(), template, execution.getVariables());
		try {
			result = URLEncoder.encode(result, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result;
	}
}
