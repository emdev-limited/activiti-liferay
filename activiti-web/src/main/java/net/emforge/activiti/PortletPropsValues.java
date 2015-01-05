package net.emforge.activiti;

import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.util.portlet.PortletProps;

public class PortletPropsValues {
	public static final boolean WORKFLOWCONTEXT_LIFERAY_USE_LONG = GetterUtil.getBoolean(
			PortletProps.get(PortletPropsKeys.WORKFLOWCONTEXT_LIFERAY_USE_LONG), false);

	public static final String PROCESS_STATE_STRATEGY = StringUtil.lowerCase(GetterUtil.getString(
			PortletProps.get(PortletPropsKeys.PROCESS_STATE_STRATEGY),
			WorkflowConstants.PROCESS_STATE_STRATEGY_USERTASKS));
	
	public static final String PROCESS_NEXT_TRANSITION_NAMES_STRATEGY = StringUtil.lowerCase(GetterUtil.getString(
			PortletProps.get(PortletPropsKeys.PROCESS_NEXT_TRANSITION_NAMES_STRATEGY),
			WorkflowConstants.PROCESS_NEXT_TRANSITION_NAMES_STRATEGY_NAMES));

}
