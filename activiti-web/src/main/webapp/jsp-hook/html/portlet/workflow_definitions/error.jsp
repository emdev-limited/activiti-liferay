<%--
/**
 * Copyright (c) 2000-2012 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
--%>

<%@page import="org.apache.commons.lang.exception.ExceptionUtils"%>
<%@page import="com.liferay.portal.kernel.language.LanguageUtil"%>
<%@ include file="/html/portlet/workflow_definitions/init.jsp" %>

<liferay-ui:header
	backURL="javascript:history.go(-1);"
	title="error"
/>

<liferay-ui:error exception="<%= RequiredWorkflowDefinitionException.class %>" message="you-cannot-deactivate-or-delete-this-definition" />
<liferay-ui:error exception="<%= PrincipalException.class %>"  message="you-do-not-have-the-required-permissions" />
<liferay-ui:error exception="<%= WorkflowException.class %>" >
	<%= LanguageUtil.get(pageContext, "an-error-occurred-in-the-workflow-engine") %>
	<br/>
	<%= ((WorkflowException) errorException).getMessage() %>
	<br/>
	<br/>
	<%= ExceptionUtils.getStackTrace((WorkflowException) errorException)  %>
</liferay-ui:error>

<liferay-ui:error key="unknown-exception" >
	<%= LanguageUtil.get(pageContext, "an-error-occurred-in-the-workflow-engine") %>
	<br/>
	<%= ((Exception) errorException).getMessage() %>
	<br/>
	<br/>
	<%= ExceptionUtils.getStackTrace((Exception)errorException)  %>
</liferay-ui:error>