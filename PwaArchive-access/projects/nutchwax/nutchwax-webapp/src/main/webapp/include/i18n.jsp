<%@ taglib uri="http://jakarta.apache.org/taglibs/i18n" prefix="i18n" %>
<%
	String language = "pt";

	String langParam = request.getParameter("l");
	if (langParam != null) {
		if (langParam.equals("en")) {
			language = langParam;
		} else { /* keep default */ };
	}
	
	pageContext.setAttribute("language", language);
%>

<%--<%@include file="include/i18n.jsp" %> --%>
