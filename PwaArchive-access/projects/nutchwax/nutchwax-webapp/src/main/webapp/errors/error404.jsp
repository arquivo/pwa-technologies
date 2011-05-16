<%@ page 
	session="true"
	contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"
	isErrorPage="true"

	import="java.util.Calendar"
	import="java.util.GregorianCalendar"
	import="java.util.Locale"
%>
<%!
	private static int hitsTotal = -1;
	private static Calendar DATE_START = new GregorianCalendar(1996, 1-1, 1);
%>
<%
	String path = "http://"+ request.getServerName() + request.getContextPath();
	Calendar dateStart = null;
	Calendar dateEnd = null;
%>
<%@ taglib uri="http://jakarta.apache.org/taglibs/i18n" prefix="i18n" %>
<%@ include file="../include/logging_params.jsp" %>
<%@ include file="../include/i18n.jsp" %>
<i18n:bundle baseName="org.nutch.jsp.error" locale="<%= new Locale(language)%>"/>


<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<link rel="shortcut icon" href="<%=path%>/img/logo-16.jpg" type="image/x-icon"/>
	<link href="<%=path%>/css/style.css" type="text/css" rel="stylesheet">
	<title><i18n:message key="windowTitle404"/></title>
</head>
<body>
	<div id="header">
		<%@include file="../header.jsp" %>
		<div id="title">
			<h1><i18n:message key="title404"/></h1>
		</div>
	</div>	
	<div id="main_content">
		<div class="info_bar"></div>
		<div>
			<h2><i18n:message key="sorry"/></h2>
			<i18n:message key="subtitle404"/> <code>(404 Page Not Found).</code>

			<p>	
				<i18n:message key="options"/>
				<ul>
					<li><a href="http://sobre.arquivo.pt/contact-info"><i18n:message key="contactUs"/></a></li>
					<li><a href="./"><i18n:message key="returnHome"/></a></li>
					<li><i18n:message key="seeHow"/> <a href="http://sobre.arquivo.pt/colaboracoes/participacao-individual/?searchterm=contribuir"><i18n:message key="contribute"/></a></li>
				</ul>
			</p>
		</div>
		<hr style="visibility:hidden;margin-top: 10em"/>	
		<%@include file="../footer.jsp" %>
	</div>
<%-- Google Analytics tracking code --%>
<script type="text/javascript">
	var _gaq = _gaq || [];
	_gaq.push(['_setAccount', 'UA-21825027-1']);
	_gaq.push(['_setDomainName', '.arquivo.pt']);
	_gaq.push(['_trackPageview']);
	(function() {
		var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
		ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
		var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
	})();
</script>
</body>
</html>                                 
<%-- logging page access --%>                                                              
<%@include file="../include/logging.jsp" %>
