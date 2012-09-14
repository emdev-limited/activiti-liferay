package net.emforge.activiti.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.emforge.activiti.WorkflowDefinitionManagerImpl;
import net.emforge.activiti.entity.WorkflowDefinitionExtensionImpl;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.apache.commons.io.IOUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.servlet.BrowserSnifferUtil;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HttpUtil;
import com.liferay.portal.kernel.util.MimeTypesUtil;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

public class ImageServlet extends HttpServlet {
	private static final long serialVersionUID = 532117496619322018L;
    private static Log _log = LogFactoryUtil.getLog(ImageServlet.class);

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//get params
		Long companyId = GetterUtil.getLong(request.getParameter("companyId"), 0l);
		String workflowName = request.getParameter("workflow");
		Integer workflowVersion = GetterUtil.getInteger(request.getParameter("version"), 0);
		Long workflowTaskId = GetterUtil.getLong(request.getParameter("taskId"), 0l);

		// get application context
		ApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());

		// get beans
		WorkflowDefinitionManagerImpl workflowManager = (WorkflowDefinitionManagerImpl)applicationContext.getBean("workflowDefinitionManager");
		RepositoryService repositoryService = (RepositoryService)applicationContext.getBean(RepositoryService.class);
		TaskService taskService = (TaskService)applicationContext.getBean(TaskService.class);
		_log.debug("Get app context and beans");

		String defId = null;
		if (workflowTaskId > 0) {
			_log.info("Generate image for task: " + workflowTaskId);
			String taskId = String.valueOf(workflowTaskId);
			Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
			
			defId = task.getProcessDefinitionId();
		} else {
			_log.debug("Generate image for workflow " + workflowName + " varsion " + workflowVersion);
		
			// get workflow definition
			WorkflowDefinitionExtensionImpl defExt = workflowManager.getWorkflowDefinitionExt(companyId, workflowName, workflowVersion);
			
			_log.debug("WorkflowDefExt: " + defExt);
		
			if (defExt != null) {
				defId = defExt.getProcessDefinitionId();
			}
		}
		
		_log.info("Definition Id: " + defId);
		
		if (defId != null) {
			// get Activiti Workflow
			InputStream is = repositoryService.getProcessDiagram(defId);
			byte[] binaryContent = IOUtils.toByteArray(is);
			
			_log.debug("Image Size: " + binaryContent.length);
			
			try {
				sendFile(request, response, "process.png", binaryContent, MimeTypesUtil.getContentType("process.png"));
			} catch (Exception ex) {
				_log.error("Cannot send response");
			}
		} else {
			super.doGet(request, response);
		}
	}
	
	public void sendFile(
			HttpServletRequest portletRequest, HttpServletResponse mimeResponse,
			String fileName, byte[] bytes,
			String contentType)
		throws IOException {

		setHeaders(portletRequest, mimeResponse, fileName, contentType);

		write(mimeResponse, bytes, 0, 0);
	}

	protected void setHeaders(HttpServletRequest request, HttpServletResponse response,
			String fileName, String contentType) {

			if (_log.isDebugEnabled()) {
				_log.debug("Sending file of type " + contentType);
			}

			// LEP-2201

			if (Validator.isNotNull(contentType)) {
				response.setContentType(contentType);
			}

			/*
			response.setProperty(
				HttpHeaders.CACHE_CONTROL, HttpHeaders.CACHE_CONTROL_PRIVATE_VALUE);
			response.setProperty(
				HttpHeaders.PRAGMA, HttpHeaders.PRAGMA_NO_CACHE_VALUE);
			*/
			
			if (Validator.isNotNull(fileName)) {
				String contentDisposition =
					"attachment; filename=\"" + fileName + "\"";

				// If necessary for non-ASCII characters, encode based on RFC 2184.
				// However, not all browsers support RFC 2184. See LEP-3127.

				boolean ascii = true;

				for (int i = 0; i < fileName.length(); i++) {
					if (!Validator.isAscii(fileName.charAt(i))) {
						ascii = false;

						break;
					}
				}

				try {
					if (!ascii) {
						String encodedFileName = HttpUtil.encodeURL(fileName, true);

						if (BrowserSnifferUtil.isIe(request)) {
							contentDisposition =
								"attachment; filename=\"" + encodedFileName + "\"";
						}
						else {
							contentDisposition =
								"attachment; filename*=UTF-8''" + encodedFileName;
						}
					}
				}
				catch (Exception e) {
					if (_log.isWarnEnabled()) {
						_log.warn(e);
					}
				}

				String extension = GetterUtil.getString(
					FileUtil.getExtension(fileName)).toLowerCase();

				String[] mimeTypesContentDispositionInline = null;

				try {
					mimeTypesContentDispositionInline = PropsUtil.getArray(
						"mime.types.content.disposition.inline");
				}
				catch (Exception e) {
					mimeTypesContentDispositionInline = new String[0];
				}

				if (ArrayUtil.contains(
						mimeTypesContentDispositionInline, extension)) {

					contentDisposition = StringUtil.replace(
						contentDisposition, "attachment; ", "inline; ");
				}

				response.setHeader("Content-Disposition","inline; filename=" + fileName);
			}
		}

	public static void write(
			HttpServletResponse response, byte[] bytes, int offset,
			int contentLength)
		throws IOException {

		if (contentLength == 0) {
			contentLength = bytes.length;
		}


		response.setContentLength(contentLength);
		OutputStream outputStream = response.getOutputStream();

		outputStream.write(bytes, offset, contentLength);
	}

}
