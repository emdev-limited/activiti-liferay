package net.emforge.workflowdefinitions.action;

import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.servlet.BrowserSnifferUtil;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.struts.BaseStrutsPortletAction;
import com.liferay.portal.kernel.struts.StrutsPortletAction;
import com.liferay.portal.kernel.upload.UploadPortletRequest;
import com.liferay.portal.kernel.util.Constants;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.JavaConstants;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.LocalizationUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.StreamUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.kernel.workflow.WorkflowDefinition;
import com.liferay.portal.kernel.workflow.WorkflowDefinitionFileException;
import com.liferay.portal.kernel.workflow.WorkflowDefinitionManagerUtil;
import com.liferay.portal.kernel.workflow.WorkflowException;
import com.liferay.portal.model.LayoutTypePortlet;
import com.liferay.portal.model.Portlet;
import com.liferay.portal.service.PortletLocalServiceUtil;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PortalUtil;

import java.io.IOException;
import java.io.InputStream;

import java.util.Locale;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Andrey Filippov
 * 
 * TODO If we have several custom struts actions move common methods e.g. sendRedirect to abstract super class
 * 
 */
public class ExtEditWorkflowDefinitionAction extends BaseStrutsPortletAction {

	@Override
	public void processAction(StrutsPortletAction originalStrutsPortletAction, PortletConfig portletConfig, 
								ActionRequest actionRequest, ActionResponse actionResponse)
											throws Exception {

		String cmd = ParamUtil.getString(actionRequest, Constants.CMD);
		
		try {
			if (cmd.equals(Constants.ADD) || cmd.equals(Constants.UPDATE)) {
				updateWorkflowDefinition(actionRequest);
			}
			else if (cmd.equals(Constants.DEACTIVATE) ||
					 cmd.equals(Constants.DELETE) ||
					 cmd.equals(Constants.RESTORE)) {

				deleteWorkflowDefinition(actionRequest);
			}

			sendRedirect(actionRequest, actionResponse);
		}
		catch (Exception e) {
			if (e instanceof WorkflowDefinitionFileException) {
				SessionErrors.add(actionRequest, e.getClass().getName());
			}
			else if (e instanceof WorkflowException) {
				SessionErrors.add(actionRequest, e.getClass().getName(), e);

				setForward(actionRequest, "portlet.workflow_definitions.error");
			}
			else {
				SessionErrors.add(actionRequest, "unknown-exception", e);
				
				setForward(actionRequest, "portlet.workflow_definitions.error");
			}
		}
	}

	@Override
	public String render(StrutsPortletAction originalStrutsPortletAction, PortletConfig portletConfig, 
							RenderRequest renderRequest, RenderResponse renderResponse) throws Exception {

		return originalStrutsPortletAction.render(portletConfig, renderRequest, renderResponse);
	}

	protected void deleteWorkflowDefinition(ActionRequest actionRequest)
		throws Exception {

		String cmd = ParamUtil.getString(actionRequest, Constants.CMD);

		ThemeDisplay themeDisplay = (ThemeDisplay)actionRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		String name = ParamUtil.getString(actionRequest, "name");
		int version = ParamUtil.getInteger(actionRequest, "version");

		if (cmd.equals(Constants.DEACTIVATE) || cmd.equals(Constants.RESTORE)) {
			boolean active = !cmd.equals(Constants.DEACTIVATE);

			WorkflowDefinitionManagerUtil.updateActive(
				themeDisplay.getCompanyId(), themeDisplay.getUserId(), name,
				version, active);
		}
		else {
			WorkflowDefinitionManagerUtil.undeployWorkflowDefinition(
				themeDisplay.getCompanyId(), themeDisplay.getUserId(), name,
				version);
		}
	}

	protected void updateWorkflowDefinition(ActionRequest actionRequest)
		throws Exception {

		UploadPortletRequest uploadPortletRequest =
			PortalUtil.getUploadPortletRequest(actionRequest);

		ThemeDisplay themeDisplay = (ThemeDisplay)actionRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		Map<Locale, String> titleMap = LocalizationUtil.getLocalizationMap(
			actionRequest, "title");

		InputStream inputStream = null;

		try {
			inputStream = uploadPortletRequest.getFileAsStream("file");

			WorkflowDefinition workflowDefinition = null;

			if (inputStream == null) {
				String name = ParamUtil.getString(actionRequest, "name");
				int version = ParamUtil.getInteger(actionRequest, "version");

				workflowDefinition =
					WorkflowDefinitionManagerUtil.getWorkflowDefinition(
						themeDisplay.getCompanyId(), name, version);

				WorkflowDefinitionManagerUtil.updateTitle(
					themeDisplay.getCompanyId(), themeDisplay.getUserId(), name,
					version, getTitle(titleMap));
			}
			else {
				workflowDefinition =
					WorkflowDefinitionManagerUtil.deployWorkflowDefinition(
						themeDisplay.getCompanyId(), themeDisplay.getUserId(),
						getTitle(titleMap), inputStream);
			}

			actionRequest.setAttribute(
				/*com.liferay.portal.util.WebKeys.WORKFLOW_DEFINITION*/"WORKFLOW_DEFINITION", workflowDefinition);
		}
		finally {
			StreamUtil.cleanUp(inputStream);
		}

	}

	protected String getTitle(Map<Locale, String> titleMap) {
		if (titleMap == null) {
			return null;
		}

		String value = StringPool.BLANK;

		Locale[] locales = LanguageUtil.getAvailableLocales();

		for (Locale locale : locales) {
			String languageId = LocaleUtil.toLanguageId(locale);
			String title = titleMap.get(locale);

			if (Validator.isNotNull(title)) {
				value = LocalizationUtil.updateLocalization(
					value, "Title", title, languageId);
			}
			else {
				value = LocalizationUtil.removeLocalization(
					value, "Title", languageId);
			}
		}

		return value;
	}
	
	
	//--------------------------------------------------------------------
	//---Common methods. Candidates to be moved to abstract super class---
	//--------------------------------------------------------------------
	
	protected void setForward(PortletRequest portletRequest, String forward) {
		portletRequest.setAttribute(getForwardKey(portletRequest), forward);
	}
	
	public static String getForwardKey(PortletRequest portletRequest) {
		String portletId = (String)portletRequest.getAttribute(
			WebKeys.PORTLET_ID);

		String portletNamespace = PortalUtil.getPortletNamespace(portletId);

		return portletNamespace.concat(/*com.liferay.portal.util.WebKeys.PORTLET_STRUTS_FORWARD*/"PORTLET_STRUTS_FORWARD");
	}
	
	protected void sendRedirect(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws IOException {

		sendRedirect(actionRequest, actionResponse, null);
	}

	protected void sendRedirect(
			ActionRequest actionRequest, ActionResponse actionResponse,
			String redirect)
		throws IOException {

		if (SessionErrors.isEmpty(actionRequest)) {
			ThemeDisplay themeDisplay =
				(ThemeDisplay)actionRequest.getAttribute(WebKeys.THEME_DISPLAY);

			LayoutTypePortlet layoutTypePortlet =
				themeDisplay.getLayoutTypePortlet();

			boolean hasPortletId = false;

			String portletId = (String)actionRequest.getAttribute(
				WebKeys.PORTLET_ID);

			try {
				hasPortletId = layoutTypePortlet.hasPortletId(portletId);
			}
			catch (Exception e) {
			}

			Portlet portlet = PortletLocalServiceUtil.getPortletById(portletId);

			if (hasPortletId || portlet.isAddDefaultResource()) {
				addSuccessMessage(actionRequest, actionResponse);
			}
		}

		if (Validator.isNull(redirect)) {
			redirect = (String)actionRequest.getAttribute(WebKeys.REDIRECT);
		}

		if (Validator.isNull(redirect)) {
			redirect = ParamUtil.getString(actionRequest, "redirect");
		}

		if (Validator.isNotNull(redirect)) {

			// LPS-1928

			HttpServletRequest request = PortalUtil.getHttpServletRequest(
				actionRequest);

			if ((BrowserSnifferUtil.isIe(request)) &&
				(BrowserSnifferUtil.getMajorVersion(request) == 6.0) &&
				(redirect.contains(StringPool.POUND))) {

				String redirectToken = "&#";

				if (!redirect.contains(StringPool.QUESTION)) {
					redirectToken = StringPool.QUESTION + redirectToken;
				}

				redirect = StringUtil.replace(
					redirect, StringPool.POUND, redirectToken);
			}

			redirect = PortalUtil.escapeRedirect(redirect);

			if (Validator.isNotNull(redirect)) {
				actionResponse.sendRedirect(redirect);
			}
		}
	}
	
	protected void addSuccessMessage(ActionRequest actionRequest,
			ActionResponse actionResponse) {

		PortletConfig portletConfig = (PortletConfig) actionRequest
				.getAttribute(JavaConstants.JAVAX_PORTLET_CONFIG);

		boolean addProcessActionSuccessMessage = GetterUtil.getBoolean(
				portletConfig
						.getInitParameter("add-process-action-success-action"),
				true);

		if (!addProcessActionSuccessMessage) {
			return;
		}

		String successMessage = ParamUtil.getString(actionRequest,
				"successMessage");

		SessionMessages.add(actionRequest, "request_processed", successMessage);
	}

}
