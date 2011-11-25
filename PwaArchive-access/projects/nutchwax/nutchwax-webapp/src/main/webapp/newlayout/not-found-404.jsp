<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd" >
<%@ page
	session="true"
	contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"	

	import="java.io.File"
	import="java.util.Calendar"
	import="java.util.Date"
	import="java.util.GregorianCalendar"
%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt_rt" prefix="fmt" %>
<%@ include file="include/logging_params.jsp" %>
<%@ include file="include/i18n.jsp" %>
<fmt:setLocale value="<%=language%>"/>

<%!	//To please the compiler since logging need those -- check [search.jsp]
	private static int hitsTotal = -10;		// the value -10 will be used to mark as being "advanced search"
	private static Calendar DATE_START = new GregorianCalendar(1996, 1-1, 1);
	private static Calendar dateStart = new GregorianCalendar();
	private static Calendar dateEnd = new GregorianCalendar();
%>

<%---------------------- Start of HTML ---------------------------%>

<%-- TODO: define XML lang --%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="pt-PT" lang="pt-PT">
<head>
	<title><fmt:message key='404.meta.title'/></title>
	<meta http-equiv="Content-Type" content="application/xhtml+xml; charset=UTF-8" />
	<%-- TODO: define META lang --%>
	<meta http-equiv="Content-Language" content="pt-PT" />
	<meta name="Keywords" content="<fmt:message key='404.meta.keywords'/>" />
	<meta name="Description" content="<fmt:message key='404.meta.description'/>" />
	<link rel="shortcut icon" href="img/logo-16.jpg" type="image/x-icon" />
	<link rel="stylesheet" title="Estilo principal" type="text/css" href="css/style.css"  media="all" />
</head>
<body>
	<%@ include file="include/topbar.jsp" %>
	<div class="wrap">
		<div id="main">
			<div id="header">
				<div id="logo">
					<a href="index.jsp" title="<fmt:message key='header.logo.link'/>">
						<img src="img/logo-2.gif" alt="<fmt:message key='header.logo.alt'/>" width="122" height="90" />
					</a>
				</div>
				<div id="info-texto-termos">
					<h1><fmt:message key='404.title'/></h1>
					<h2><fmt:message key='404.subtitle'/></h2>
				</div>
			</div>

			<div id="conteudo-erro">
				<p><fmt:message key='404.suggestions.title'/>
				<ul>
					<li><a href="http://sobre.arquivo.pt/contact-info?set_language=<%=language%>"><fmt:message key='404.suggestions.feedback'/></a></li>
                                        <li><a href="./"><fmt:message key='404.suggestions.go-home'/></a></li>
                                        <%	String contributeUrl = null;
						if (language.equals("en"))
							contributeUrl = "http://sobre.arquivo.pt/colaboracoes/outras-formas-colaboracao";
						else
							contributeUrl = "http://sobre.arquivo.pt/how-to-participate/individual-participation";
					%>
					<li><fmt:message key='404.suggestions.contribute'><fmt:param value='<%=contributeUrl%>'/></fmt:message></li>
				</ul>
				</p>
				<img src="img/not-found.png" alt="" />
			</div>
		</div>
	</div>
<%@include file="include/footer.jsp" %>
<%@include file="include/analytics.jsp" %>
</body>
</html>

<%@include file="include/logging.jsp" %>
