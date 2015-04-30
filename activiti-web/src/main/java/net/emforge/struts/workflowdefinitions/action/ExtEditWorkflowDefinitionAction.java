package net.emforge.struts.workflowdefinitions.action;

import java.io.File;
import java.util.Locale;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import net.emforge.activiti.util.WebKeys;
import net.emforge.struts.CommonStrutsPortletAction;

import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.struts.StrutsPortletAction;
import com.liferay.portal.kernel.upload.UploadPortletRequest;
import com.liferay.portal.kernel.util.Constants;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.LocalizationUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowDefinition;
import com.liferay.portal.kernel.workflow.WorkflowDefinitionFileException;
import com.liferay.portal.kernel.workflow.WorkflowDefinitionManagerUtil;
import com.liferay.portal.kernel.workflow.WorkflowException;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PortalUtil;

/**
 * @author Andrey Filippov
 * @author Dmitry Farafonov
 */
public class ExtEditWorkflowDefinitionAction extends CommonStrutsPortletAction {

	@Override
	public void processAction(StrutsPortletAction originalStrutsPortletAction,
			PortletConfig portletConfig, ActionRequest actionRequest,
			ActionResponse actionResponse) throws Exception {

		String action = ParamUtil.getString(actionRequest, "struts_action");
		if ("/workflow_definitions/edit_workflow_definition_link".equals(action)) {
			// forward to original struts action
			originalStrutsPortletAction.processAction(portletConfig, actionRequest, actionResponse);
			return;
		}
		
		String cmd = ParamUtil.getString(actionRequest, Constants.CMD);

		try {
			if (cmd.equals(Constants.ADD) || cmd.equals(Constants.UPDATE)) {
				updateWorkflowDefinition(actionRequest);
			} else if (cmd.equals(Constants.DEACTIVATE)
					|| cmd.equals(Constants.DELETE)
					|| cmd.equals(Constants.RESTORE)) {

				deleteWorkflowDefinition(actionRequest);
			}

			sendRedirect(actionRequest, actionResponse);
		} catch (Exception e) {
			if (e instanceof WorkflowDefinitionFileException) {
				SessionErrors.add(actionRequest, e.getClass().getName());
			} else if (e instanceof WorkflowException) {
				SessionErrors.add(actionRequest, e.getClass().getName(), e);

				setForward(actionRequest, "portlet.workflow_definitions.error");
			} else {
				SessionErrors.add(actionRequest, "unknown-exception", e);

				setForward(actionRequest, "portlet.workflow_definitions.error");
			}
		}
	}

	@Override
	public String render(StrutsPortletAction originalStrutsPortletAction,
			PortletConfig portletConfig, RenderRequest renderRequest,
			RenderResponse renderResponse) throws Exception {

		return originalStrutsPortletAction.render(portletConfig, renderRequest,
				renderResponse);
	}

	@Override
	public void serveResource(StrutsPortletAction originalStrutsPortletAction,
			PortletConfig portletConfig, ResourceRequest resourceRequest,
			ResourceResponse resourceResponse) throws Exception {

		originalStrutsPortletAction.serveResource(portletConfig,
				resourceRequest, resourceResponse);
	}

	protected void deleteWorkflowDefinition(ActionRequest actionRequest)
			throws Exception {

		String cmd = ParamUtil.getString(actionRequest, Constants.CMD);

		ThemeDisplay themeDisplay = (ThemeDisplay) actionRequest
				.getAttribute(WebKeys.THEME_DISPLAY);

		String name = ParamUtil.getString(actionRequest, "name");
		int version = ParamUtil.getInteger(actionRequest, "version");

		if (cmd.equals(Constants.DEACTIVATE) || cmd.equals(Constants.RESTORE)) {
			boolean active = !cmd.equals(Constants.DEACTIVATE);

			WorkflowDefinitionManagerUtil.updateActive(
					themeDisplay.getCompanyId(), themeDisplay.getUserId(),
					name, version, active);
		} else {
			WorkflowDefinitionManagerUtil.undeployWorkflowDefinition(
					themeDisplay.getCompanyId(), themeDisplay.getUserId(),
					name, version);
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
				value = LocalizationUtil.updateLocalization(value, "Title",
						title, languageId);
			} else {
				value = LocalizationUtil.removeLocalization(value, "Title",
						languageId);
			}
		}

		return value;
	}

	protected void updateWorkflowDefinition(ActionRequest actionRequest)
			throws Exception {

		UploadPortletRequest uploadPortletRequest = PortalUtil
				.getUploadPortletRequest(actionRequest);

		ThemeDisplay themeDisplay = (ThemeDisplay) actionRequest
				.getAttribute(WebKeys.THEME_DISPLAY);

		Map<Locale, String> titleMap = LocalizationUtil.getLocalizationMap(
				actionRequest, "title");

		File file = uploadPortletRequest.getFile("file");

		WorkflowDefinition workflowDefinition = null;

		if (file == null) {
			String name = ParamUtil.getString(actionRequest, "name");
			int version = ParamUtil.getInteger(actionRequest, "version");

			workflowDefinition = WorkflowDefinitionManagerUtil
					.getWorkflowDefinition(themeDisplay.getCompanyId(), name,
							version);

			WorkflowDefinitionManagerUtil.updateTitle(
					themeDisplay.getCompanyId(), themeDisplay.getUserId(),
					name, version, getTitle(titleMap));
		} else {
			workflowDefinition = WorkflowDefinitionManagerUtil
					.deployWorkflowDefinition(themeDisplay.getCompanyId(),
							themeDisplay.getUserId(), getTitle(titleMap),
							FileUtil.getBytes(file));
		}

		actionRequest.setAttribute(WebKeys.WORKFLOW_DEFINITION,
				workflowDefinition);
	}

}
