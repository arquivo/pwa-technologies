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
				<%@ include file="include/logo.jsp" %>
				<div id="info-texto">
					<h1><fmt:message key='highlights.title'/></h1>
					<h2><fmt:message key='highlights.subtitle'/></h2>
				</div>
			</div>

			<fmt:bundle basename='pt.arquivo.i18n.Highlights'>
			<div id="boxes" class="boxes">
				<div class="box">
					<div class="mascara-img">
						<a href="/wayback/wayback/id1101166index0?l=<%=language%>" title="<fmt:message key='highlight.saramago.link.title'/>"><img src="img/highlights/saramago.png" alt="<fmt:message key='highlight.saramago.alt'/>" width="80" height="80" /></a>
						<div class="mascara-img-gr"></div>
					</div>
					<div class="box-info">
						<a href="/wayback/wayback/id1101166index0?l=<%=language%>" title="<fmt:message key='highlight.saramago.link.title'/>"><fmt:message key='highlight.saramago.title'/></a>
						<p class="box-sub-title"><fmt:message key='highlight.saramago.description'/></p>
						<p><fmt:message key='highlight.saramago.description2'/></p>
					</div>
				</div>
				<div class="box">
					<div class="mascara-img">
						<a href="/wayback/wayback/id4705954index0?l=<%=language%>" title="<fmt:message key='highlight.expo98.link.title'/>"><img src="img/highlights/expo98.gif" alt="<fmt:message key='highlight.expo98.alt'/>" width="80" height="80" /></a>
						<div class="mascara-img-gr"></div>
					</div>
					<div class="box-info">
						<a href="/wayback/wayback/id4705954index0?l=<%=language%>" title="<fmt:message key='highlight.expo98.link.title'/>"><fmt:message key='highlight.expo98.title'/></a>
						<p class="box-sub-title"><fmt:message key='highlight.expo98.description'/></p>
						<p><fmt:message key='highlight.expo98.description2'/></p>
					</div>
				</div>
				<div class="box">
					<div class="mascara-img">
						<a href="/wayback/wayback/id4390263index3?l=<%=language%>" title="<fmt:message key='highlight.euro2004.link.title'/>"><img src="img/highlights/euro2004.png" alt="<fmt:message key='highlight.euro2004.alt'/>" width="80" height="80" /></a>
						<div class="mascara-img-gr"></div>
					</div>
					<div class="box-info">
						<a href="/wayback/wayback/id4390263index3?l=<%=language%>" title="<fmt:message key='highlight.euro2004.link.title'/>"><fmt:message key='highlight.euro2004.title'/></a>
						<p class="box-sub-title"><fmt:message key='highlight.euro2004.description'/></p>
						<p><fmt:message key='highlight.euro2004.description2'/></p>
					</div>
				</div>
				<div class="box">
					<div class="mascara-img">
						<a href="/wayback/wayback/id87775634index0?l=<%=language%>" title="<fmt:message key='highlight.figo.link.title'/>"><img src="img/highlights/figo.png" alt="<fmt:message key='highlight.figo.alt'/>" width="80" height="80" /></a>
						<div class="mascara-img-gr"></div>
					</div>
					<div class="box-info">
						<a href="/wayback/wayback/id87775634index0?l=<%=language%>" title="<fmt:message key='highlight.figo.link.title'/>"><fmt:message key='highlight.figo.title'/></a>
						<p class="box-sub-title"><fmt:message key='highlight.figo.description'/></p>
						<p><fmt:message key='highlight.figo.description2'/></p>
					</div>
				</div>
				<div class="box">
					<div class="mascara-img">
						<a href="/wayback/wayback/id94index1?l=<%=language%>" title="<fmt:message key='highlight.publico.link.title'/>"><img src="img/highlights/publico.png" alt="<fmt:message key='highlight.publico.alt'/>" width="80" height="80" /></a>
						<div class="mascara-img-gr"></div>
					</div>
					<div class="box-info">
						<a href="/wayback/wayback/id94index1?l=<%=language%>" title="<fmt:message key='highlight.publico.link.title'/>"><fmt:message key='highlight.publico.title'/></a>
						<p class="box-sub-title"><fmt:message key='highlight.publico.description'/></p>
						<p><fmt:message key='highlight.publico.description2'/></p>
					</div>
				</div>
				<div class="box">
					<div class="mascara-img">
						<a href="/wayback/wayback/id39index1?l=<%=language%>" title="<fmt:message key='highlight.sapo.link.title'/>"><img src="img/highlights/sapo.png" alt="<fmt:message key='highlight.sapo.alt'/>" width="80" height="80" /></a>
						<div class="mascara-img-gr"></div>
					</div>
					<div class="box-info">
						<a href="/wayback/wayback/id39index1?l=<%=language%>" title="<fmt:message key='highlight.sapo.link.title'/>"><fmt:message key='highlight.sapo.title'/></a>
						<p class="box-sub-title"><fmt:message key='highlight.sapo.description'/></p>
						<p><fmt:message key='highlight.sapo.description2'/></p>
					</div>
				</div>
				<div class="box">
					<div class="mascara-img">
						<a href="/wayback/wayback/id4447index1?l=<%=language%>" title="<fmt:message key='highlight.tim-berners-lee.link.title'/>"><img src="img/highlights/tim-berners-lee.png" alt="<fmt:message key='highlight.tim-berners-lee.alt'/>" width="80" height="80" /></a>
						<div class="mascara-img-gr"></div>
					</div>
					<div class="box-info">
						<a href="/wayback/wayback/id4447index1?l=<%=language%>" title="<fmt:message key='highlight.tim-berners-lee.link.title'/>"><fmt:message key='highlight.tim-berners-lee.title'/></a>
						<p class="box-sub-title"><fmt:message key='highlight.tim-berners-lee.description'/></p>
						<p><fmt:message key='highlight.tim-berners-lee.description2'/></p>
					</div>
				</div>
				<div class="box">
					<div class="mascara-img">
						<a href="/wayback/wayback/id23306629index0?l=<%=language%>" title="<fmt:message key='highlight.presidential2001.link.title'/>"><img src="img/highlights/presidential2001.gif" alt="<fmt:message key='highlight.presidential2001.alt'/>" width="80" height="80" /></a>
						<div class="mascara-img-gr"></div>
					</div>
					<div class="box-info">
						<a href="/wayback/wayback/id23306629index0?l=<%=language%>" title="<fmt:message key='highlight.presidential2001.link.title'/>"><fmt:message key='highlight.presidential2001.title'/></a>
						<p class="box-sub-title"><fmt:message key='highlight.presidential2001.description'/></p>
						<p><fmt:message key='highlight.presidential2001.description2'/></p>
					</div>
				</div>
			</div>
			</fmt:bundle>

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
                        <div class="voltar">&lt; <a href="index.jsp?l=<%=language%>" title="<fmt:message key='highlights.return.alt'/>"><fmt:message key='highlights.return'/></a></div>
                </div>
	</div>
<%@include file="include/footer.jsp" %>
<%@include file="include/analytics.jsp" %>
</body>
</html>

<%@include file="include/logging.jsp" %>
