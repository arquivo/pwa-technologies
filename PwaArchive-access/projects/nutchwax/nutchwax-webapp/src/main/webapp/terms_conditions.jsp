<%@ page
	session="true"
	contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"	

	import="java.io.File"
	import="java.net.URLDecoder"
	import="java.util.Calendar"
	import="java.util.Date"
	import="java.util.GregorianCalendar"
	import="java.util.Locale"
	import="java.util.ResourceBundle"
	import="java.util.regex.Matcher"
	import="java.util.regex.Pattern"
	import="java.text.DateFormatSymbols"
	import="java.text.SimpleDateFormat"
%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ include file="include/logging_params.jsp" %>
<%@ include file="include/simple_params_processing.jsp" %>
<%@ include file="include/i18n.jsp" %>
<i18n:bundle baseName="org.nutch.jsp.terms_conditions" locale="<%= new Locale(language) %>"/>

<%!	//To please the compiler since logging need those -- check [search.jsp]
	private static int hitsTotal = -10;		// the value -10 will be used to mark as being "advanced search"
	private static Calendar DATE_START = new GregorianCalendar(1996, 1-1, 1);
	private static Calendar dateStart = new GregorianCalendar();
	private static Calendar dateEnd = new GregorianCalendar();
%>

<%---------------------- Start of HTML ---------------------------%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="<%=language%>" xml:lang="<%=language%>">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	<%@ include file="include/metadata.jsp" %>
	<title><i18n:message key="title"/></title>
	<link rel="shortcut icon" href="img/logo-16.jpg" type="image/x-icon" />
	<link type="text/css" href="css/jquery-ui-1.7.2.custom.css" rel="stylesheet"/>
	<link rel="stylesheet" type="text/css" href="css/advanced.css" />
</head>
<body class="oneColElsCtrHdr">

<div id="header">
	<%@include file="header.jsp" %>
        <div id="title">
                <h1><i18n:message key="header"/></h1>
        </div>
</div>
<div class="info_bar"></div>
<div id="utilities">
	<a href="<c:url value="">
			<c:if test="${ (empty param.l) || (param.l eq 'pt') }">
				<c:param name='l' value='en'/>
			</c:if>
			<c:if test="${ param.l eq 'en' }">
				<c:param name='l' value='pt'/>
			</c:if>
		</c:url>"><i18n:message key="otherLang"/></a>
	|
        <c:choose>
                <c:when test="${ param.l eq 'en' }">
                        <a href="http://sobre.arquivo.pt/faq/advanced-search"><i18n:message key="help"/></a>
                </c:when>
                <c:otherwise>
                        <a href="http://sobre.arquivo.pt/perguntas-frequentes/pesquisa-avancada"><i18n:message key="help"/></a>
                </c:otherwise>
        </c:choose>
        |
        <a href="<c:url value='http://sobre.arquivo.pt/'><c:param name='set_language' value='${language}'/></c:url>"><i18n:message key="about"/></a>
</div>
<%--<div id="main_content">--%>

<div id="container">

  <div id="legalNotice" class="spacing">
	<h2 style="margin-bottom:0"><i18n:message key="conditionsTitle"/></h2>
	<hr />
	<ol>
		<li>
			<i18n:message key="condition1Title"/>
			<i18n:message key="condition1Text"/>
		</li>
		<li>
			<i18n:message key="condition2Title"/>
			<i18n:message key="condition2Text"/>
		</li>
		<li>
			<i18n:message key="condition3Title"/>
			<i18n:message key="condition3Text"/>
		</li>
		<li>
			<i18n:message key="condition4Title"/>
			<i18n:message key="condition4Text"/>
		</li>
		<li>
			<i18n:message key="condition5Title"/>
			<i18n:message key="condition5Text"/>
		</li>
		<li>
			<i18n:message key="condition6Title"/>
			<i18n:message key="condition6Text"/>
		</li>
		<li>
			<i18n:message key="condition7Title"/>
			<i18n:message key="condition7Text"/>
		</li>
		<li>
			<i18n:message key="condition8Title"/>
			<i18n:message key="condition8Text"/>
		</li>
		<li>
			<i18n:message key="condition9Title"/>
			<i18n:message key="condition9Text"/>
		</li>
		<li>
			<i18n:message key="condition10Title"/>
			<i18n:message key="condition10Text"/>
		</li>
	</ol>
	<%
	String jspPath = getServletContext().getRealPath(request.getServletPath());
	File jspFile = new File(jspPath);
	Date lastModified = new Date(jspFile.lastModified()); 
	%>
	<div id="last_modified">
		<i18n:message key="lastModified"><i18n:messageArg value="<%= lastModified%>"/></i18n:message>
	</div>
  </div><!-- end #mainContent -->
    <hr class="spacing" />
    <%@include file="/footer.jsp" %>
<!-- end #container --></div>
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

<%@include file="include/logging.jsp" %>
