package net.emforge.struts;

import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;

import net.emforge.activiti.util.WebKeys;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.servlet.BrowserSnifferUtil;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.struts.BaseStrutsPortletAction;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HttpUtil;
import com.liferay.portal.kernel.util.JavaConstants;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.model.Layout;
import com.liferay.portal.model.LayoutTypePortlet;
import com.liferay.portal.model.Portlet;
import com.liferay.portal.service.PortletLocalServiceUtil;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PortalUtil;

/**
 * Common methods for custom struts action e.g. sendRedirect
 * 
 * @author Dmitry Farafonov
 *
 */
public abstract class CommonStrutsPortletAction extends BaseStrutsPortletAction {

	protected void setForward(PortletRequest portletRequest, String forward) {
		portletRequest.setAttribute(getForwardKey(portletRequest), forward);
	}

	public static String getForwardKey(PortletRequest portletRequest) {
		String portletId = (String)portletRequest.getAttribute(
			WebKeys.PORTLET_ID);

		String portletNamespace = PortalUtil.getPortletNamespace(portletId);

		return portletNamespace.concat(WebKeys.PORTLET_STRUTS_FORWARD);
	}

	protected void sendRedirect(ActionRequest actionRequest,
			ActionResponse actionResponse) throws IOException, SystemException {

		sendRedirect(actionRequest, actionResponse, null);
	}

	protected void sendRedirect(ActionRequest actionRequest,
			ActionResponse actionResponse, String redirect) throws IOException,
			SystemException {

		sendRedirect(null, actionRequest, actionResponse, redirect, null);
	}

	protected void sendRedirect(PortletConfig portletConfig,
			ActionRequest actionRequest, ActionResponse actionResponse,
			String redirect, String closeRedirect) throws IOException,
			SystemException {

		if (isDisplaySuccessMessage(actionRequest)) {
			addSuccessMessage(actionRequest, actionResponse);
		}

		if (Validator.isNull(redirect)) {
			redirect = (String) actionRequest.getAttribute(WebKeys.REDIRECT);
		}

		if (Validator.isNull(redirect)) {
			redirect = ParamUtil.getString(actionRequest, "redirect");
		}

		if ((portletConfig != null) && Validator.isNotNull(redirect)
				&& Validator.isNotNull(closeRedirect)) {

			redirect = HttpUtil.setParameter(redirect, "closeRedirect",
					closeRedirect);

			SessionMessages.add(actionRequest,
					PortalUtil.getPortletId(actionRequest)
							+ SessionMessages.KEY_SUFFIX_CLOSE_REDIRECT,
					closeRedirect);
		}

		if (Validator.isNull(redirect)) {
			return;
		}

		// LPS-1928

		HttpServletRequest request = PortalUtil
				.getHttpServletRequest(actionRequest);

		if (BrowserSnifferUtil.isIe(request)
				&& (BrowserSnifferUtil.getMajorVersion(request) == 6.0)
				&& redirect.contains(StringPool.POUND)) {

			String redirectToken = "&#";

			if (!redirect.contains(StringPool.QUESTION)) {
				redirectToken = StringPool.QUESTION + redirectToken;
			}

			redirect = StringUtil.replace(redirect, StringPool.POUND,
					redirectToken);
		}

		redirect = PortalUtil.escapeRedirect(redirect);

		if (Validator.isNotNull(redirect)) {
			actionResponse.sendRedirect(redirect);
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

		SessionMessages.add(actionRequest, "requestProcessed", successMessage);
	}

	protected boolean isDisplaySuccessMessage(PortletRequest portletRequest)
			throws SystemException {

		if (!SessionErrors.isEmpty(portletRequest)) {
			return false;
		}

		ThemeDisplay themeDisplay = (ThemeDisplay) portletRequest
				.getAttribute(WebKeys.THEME_DISPLAY);

		Layout layout = themeDisplay.getLayout();

		if (layout.isTypeControlPanel()) {
			return true;
		}

		String portletId = (String) portletRequest
				.getAttribute(WebKeys.PORTLET_ID);

		try {
			LayoutTypePortlet layoutTypePortlet = themeDisplay
					.getLayoutTypePortlet();

			if (layoutTypePortlet.hasPortletId(portletId)) {
				return true;
			}
		} catch (PortalException pe) {
			if (_log.isDebugEnabled()) {
				_log.debug(pe, pe);
			}
		}

		Portlet portlet = PortletLocalServiceUtil.getPortletById(
				themeDisplay.getCompanyId(), portletId);

		if (portlet.isAddDefaultResource()) {
			return true;
		}

		return false;
	}

	private static Log _log = LogFactoryUtil
			.getLog(CommonStrutsPortletAction.class);
}
