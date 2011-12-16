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
	<title><fmt:message key='terms-conditions.meta.title'/></title>
	<meta http-equiv="Content-Type" content="application/xhtml+xml; charset=UTF-8" />
	<%-- TODO: define META lang --%>
	<meta http-equiv="Content-Language" content="pt-PT" />
	<meta name="Keywords" content="<fmt:message key='terms-conditions.meta.keywords'/>" />
	<meta name="Description" content="<fmt:message key='terms-conditions.meta.description'/>" />
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
						<img src="img/logo-<%=language%>.png" alt="<fmt:message key='header.logo.alt'/>" width="125" height="90" />
					</a>
				</div>
				<div id="info-texto-termos">
					<h1><fmt:message key='terms-conditions.title'/></h1>
					<h2><fmt:message key='terms-conditions.subtitle'/></h2>
				</div>
			</div>

			<div id="conteudo-termos">
			<fmt:bundle basename='pt.arquivo.i18n.terms-conditions'>
				<h3><fmt:message key='objectives.title'/></h3>
				<fmt:message key='objectives.text'/>

				<h3><fmt:message key='definitions.title'/></h3>
				<fmt:message key='definitions.text'/>

				<h3><fmt:message key='access.title'/></h3>
				<fmt:message key='access.text'/>

				<h3><fmt:message key='conditions.title'/></h3>
				<fmt:message key='conditions.text'/>

				<h3><fmt:message key='liability.title'/></h3>
				<fmt:message key='liability.text'/>

				<h3><fmt:message key='usage.title'/></h3>
				<fmt:message key='usage.text'/>

				<h3><fmt:message key='cancellation.title'/></h3>
				<fmt:message key='cancellation.text'/>

				<h3><fmt:message key='litigation.title'/></h3>
				<fmt:message key='litigation.text'/>

				<h3><fmt:message key='alterations.title'/></h3>
				<fmt:message key='alterations.text'/>

				<h3><fmt:message key='questions.title'/></h3>
				<fmt:message key='questions.text'/>
			</fmt:bundle>
			</div>
			
			<div class="ultima-modificacao">
				<hr />
				<%
				String jspPath = getServletContext().getRealPath(request.getServletPath());
				File jspFile = new File(jspPath);
				Date lastModified = new Date(jspFile.lastModified()); 
				%>
				<p><fmt:message key='sample.last-modification'><fmt:param value='<%=lastModified%>'/></fmt:message></p>
			</div>

			<%--
			<div id="conteudos-relacionados">
				<h5>Conteúdos relacionados:</h5>
				<ul>
					<li>
						<a href="#" title="Quisque nulla lacus, sollicitudin vitae">Quisque nulla lacus, sollicitudin vitae</a>
					</li>
					<li>
						<a href="#" title="Pellentesque at lacus">Pellentesque at lacus</a>
					</li>
					<li class="excel">
						<a href="#" title="Mauris venenatis semper leo">Mauris venenatis semper leo</a><span class="tipo">(XLS, 210kb)</span>
					</li>
					<li class="powerpoint">
						<a href="#" title="Donec congue aliquam">Donec congue aliquam</a>  <span class="tipo">(PPT, 157kb)</span>
					</li>
					<li class="word">
						<a href="#" title="Aenean blandit suscipit">Aenean blandit suscipit</a> <span class="tipo">(DOC, 83kb)</span>
					</li>
					<li class="pdf">
						<a href="#" title="Cras ligula libero">Cras ligula libero</a>  <span class="tipo">(PDF, 171kb)</span>
					</li>
					<li class="download">
						<a href="#" title="Sed lobortis libero dapibus quam">Sed lobortis libero dapibus quam</a>  <span class="tipo">(JPG, 171kb)</span>
					</li>
				</ul>
			</div>
			--%>
		</div>
	</div>
<%@include file="include/footer.jsp" %>
<%@include file="include/analytics.jsp" %>
</body>
</html>

<%@include file="include/logging.jsp" %>
