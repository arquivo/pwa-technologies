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
	<title><fmt:message key='highlights.meta.title'/></title>
	<meta http-equiv="Content-Type" content="application/xhtml+xml; charset=UTF-8" />
	<%-- TODO: define META lang --%>
	<meta http-equiv="Content-Language" content="pt-PT" />
	<meta name="Keywords" content="<fmt:message key='highlights.meta.keywords'/>" />
	<meta name="Description" content="<fmt:message key='highlights.meta.description'/>" />
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
				<div id="info-texto">
					<h1><fmt:message key='highlights.title'/></h1>
					<h2><fmt:message key='highlights.subtitle'/></h2>
				</div>
			</div>

			<div id="boxes" class="boxes">
				<fmt:bundle basename='pt.arquivo.i18n.Highlights'>
				<div class="boxes" id="boxes">
                                        <div class="box">
                                                <div class="mascara-img">
                                                        <a href="jose-saramago.html" title="<fmt:message key='highlight.1.link.title'/>"><img src="img/box-josesaramago.png" alt="<fmt:message key='highlight.1.alt'/>" width="82" height="80" /></a>
                                                        <div class="mascara-img-gr"></div>
                                                </div>
                                                <div class="box-info">
                                                        <a href="jose-saramago.html" title="<fmt:message key='highlight.1.link.title'/>"><fmt:message key='highlight.1.title'/></a>
                                                        <p class="box-sub-title"><fmt:message key='highlight.1.description'/></p>
                                                        <p><fmt:message key='highlight.1.description2'/></p>
                                                </div>
                                        </div>
                                        <div class="box">
                                                <div class="mascara-img">
                                                        <a href="jose-saramago.html" title="<fmt:message key='highlight.2.link.title'/>"><img src="img/box-josesaramago.png" alt="<fmt:message key='highlight.2.alt'/>" width="82" height="80" /></a>
                                                        <div class="mascara-img-gr"></div>
                                                </div>
                                                <div class="box-info">
                                                        <a href="jose-saramago.html" title="<fmt:message key='highlight.2.link.title'/>"><fmt:message key='highlight.2.title'/></a>
                                                        <p class="box-sub-title"><fmt:message key='highlight.2.description'/></p>
                                                        <p><fmt:message key='highlight.2.description2'/></p>
                                                </div>
                                        </div>
                                        <div class="box">
                                                <div class="mascara-img">
                                                        <a href="jose-saramago.html" title="<fmt:message key='highlight.3.link.title'/>"><img src="img/box-josesaramago.png" alt="<fmt:message key='highlight.3.alt'/>" width="82" height="80" /></a>
                                                        <div class="mascara-img-gr"></div>
                                                </div>
                                                <div class="box-info">
                                                        <a href="jose-saramago.html" title="<fmt:message key='highlight.3.link.title'/>"><fmt:message key='highlight.3.title'/></a>
                                                        <p class="box-sub-title"><fmt:message key='highlight.3.description'/></p>
                                                        <p><fmt:message key='highlight.3.description2'/></p>
                                                </div>
                                        </div>
                                        <div class="box">
                                                <div class="mascara-img">
                                                        <a href="jose-saramago.html" title="<fmt:message key='highlight.4.link.title'/>"><img src="img/box-josesaramago.png" alt="<fmt:message key='highlight.4.alt'/>" width="82" height="80" /></a>
                                                        <div class="mascara-img-gr"></div>
                                                </div>
                                                <div class="box-info">
                                                        <a href="jose-saramago.html" title="<fmt:message key='highlight.4.link.title'/>"><fmt:message key='highlight.4.title'/></a>
                                                        <p class="box-sub-title"><fmt:message key='highlight.4.description'/></p>
                                                        <p><fmt:message key='highlight.4.description2'/></p>
                                                </div>
                                        </div>
                                        <div class="box">
                                                <div class="mascara-img">
                                                        <a href="jose-saramago.html" title="<fmt:message key='highlight.5.link.title'/>"><img src="img/box-josesaramago.png" alt="<fmt:message key='highlight.5.alt'/>" width="82" height="80" /></a>
                                                        <div class="mascara-img-gr"></div>
                                                </div>
                                                <div class="box-info">
                                                        <a href="jose-saramago.html" title="<fmt:message key='highlight.5.link.title'/>"><fmt:message key='highlight.5.title'/></a>
                                                        <p class="box-sub-title"><fmt:message key='highlight.5.description'/></p>
                                                        <p><fmt:message key='highlight.5.description2'/></p>
                                                </div>
                                        </div>
                                        <div class="box">
                                                <div class="mascara-img">
                                                        <a href="jose-saramago.html" title="<fmt:message key='highlight.6.link.title'/>"><img src="img/box-josesaramago.png" alt="<fmt:message key='highlight.6.alt'/>" width="82" height="80" /></a>
                                                        <div class="mascara-img-gr"></div>
                                                </div>
                                                <div class="box-info">
                                                        <a href="jose-saramago.html" title="<fmt:message key='highlight.6.link.title'/>"><fmt:message key='highlight.6.title'/></a>
                                                        <p class="box-sub-title"><fmt:message key='highlight.6.description'/></p>
                                                        <p><fmt:message key='highlight.6.description2'/></p>
                                                </div>
                                        </div>
                                </div>
				</fmt:bundle>
                        </div>

			<%--
                        <div class="pagination">
                                <div class="previous"><a href="anterior.html" title="Anterior"><img src="img/arrow-left.gif" alt="<fmt:message key='highlight.pager.previous.alt'/>" /><fmt:message key='highlight.pager.previous'/></a></div>
                                <div class="pages">
                                        <ul>
                                                <li>1</li>
                                                <li><a href="2.html">2</a></li>
                                                <li><a href="3.html">3</a></li>
                                                <li><a href="4.html">4</a></li>
                                                <li><a href="5.html">5</a></li>
                                                <li><a href="6.html">6</a></li>
                                                <li><a href="7.html">7</a></li>
                                                <li><a href="8.html">8</a></li>
                                                <li><a href="9.html">9</a></li>
                                                <li><a href="10.html">10</a></li>
                                        </ul>
                                </div>
                                <div class="next"><a href="seguinte.html" title="Seguinte"><fmt:message key⁼'highlight.pager.next'/><img src="img/arrow-right.gif" alt="<fmt:message key='highlight.pager.next.alt'/>" /></a></div>
                        </div>
			--%>
                        <div class="voltar">&lt; <a href="index.jsp" title="<fmt:message key='highlights.return.alt'/>"><fmt:message key='highlights.return'/></a></div>
                </div>

			</div>
		</div>
	</div>
<%@include file="include/footer.jsp" %>
<%@include file="include/analytics.jsp" %>
</body>
</html>

<%@include file="include/logging.jsp" %>
