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
	<title><fmt:message key='home.meta.title'/></title>
	<meta http-equiv="Content-Type" content="application/xhtml+xml; charset=UTF-8" />
	<%-- TODO: define META lang --%>
	<meta http-equiv="Content-Language" content="pt-PT" />
	<meta name="Keywords" content="<fmt:message key='home.meta.keywords'/>" />
	<meta name="Description" content="<fmt:message key='home.meta.description'/>" />
	<link rel="shortcut icon" href="img/logo-16.jpg" type="image/x-icon" />
	<link rel="search" type="application/opensearchdescription+xml" title="<fmt:message key='opensearch.title'><fmt:param value='<%=language%>'/></fmt:message>" href="opensearch.jsp?l=<%=language%>" />
	<link rel="stylesheet" title="Estilo principal" type="text/css" href="css/style.css"  media="all" />
</head>
<body>
	<%@ include file="include/topbar.jsp" %>
	<div class="wrap">
		<div id="header-home">
                                <div id="logo-home">
                                        <img src="img/logo-home-<%=language%>.png" alt="<fmt:message key='header.logo.alt'/>" width="400" height="105" />
                                </div>
                                <div id="search-home">
                                        <form id="loginForm" action="search.jsp" name="loginForm" method="get">
						<input type="hidden" name="l" value="<%= language %>" />
                                                <fieldset>
                                                        <label for="txtSearch">&nbsp;</label>
                                                        <input class="search-inputtext" type="text" size="15" value="<fmt:message key='search.value'/>" onfocus="if(this.value=='<fmt:message key='search.value'/>') this.value=''; " onblur=" if(this.value=='')this.value='<fmt:message key='search.value'/>' "  name="query" id="txtSearch" accesskey="t" />
                                                        <input type="reset" value="&nbsp;" alt="reset" class="search-resetbutton" name="btnReset" id="btnReset" accesskey="r" />
                                                        <input type="submit" value="<fmt:message key='home.submit'/>" class="home-submit" name="btnSubmit" id="btnSubmit" accesskey="e" />
                                                        <a href="advanced.jsp?l=<%=language%>" title="<fmt:message key='home.advanced.link.title'/>" id="pesquisa-avancada"><fmt:message key='home.advanced.link'/></a>
                                                </fieldset>
                                        </form>
                                </div>
                                <div id="info">
                                      <!--  <img src="img/search-icon.gif" width="62" height="71" alt="" /> -->
                                        <div id="info-texto-home">
						<%-- TODO: wrong H1. The title should be AWP. Merge H1 with logo for the intro page --%>
                                                <h1><fmt:message key='home.title'/></h1>
                                               <!-- <h2><fmt:message key='home.subtitle'/></h2> -->
                                                <p class="green"><fmt:message key='home.description'/></p>
                                                <a href="http://sobre.arquivo.pt?set_language=<%=language%>" title="<fmt:message key='home.know-more.link'/>"><fmt:message key='home.know-more'/></a>
                                        </div>
                                </div>
		</div>
		<div id="main-home">
                                <h3><fmt:message key='home.examples.title'/></h3>
                                <h4><fmt:message key='home.examples.subtitle'/></h4>

				<fmt:bundle basename='pt.arquivo.i18n.Highlights'>
                                <div class="boxes-home" id="boxes">
					<%-- Let the box be clickable. Not just that one text --%>
                                        <div class="box">
                                                <div class="mascara-img">
                                                        <img src="img/highlights/saramago.png" alt="<fmt:message key='highlight.saramago.alt'/>" width="80" height="80" />
                                                        <div class="mascara-img-gr"></div>
                                                </div>
                                                <div class="box-info">
                                                        <a href="/wayback/wayback/19980205082901/http://www.caleida.pt/saramago/" title="<fmt:message key='highlight.saramago.link.title'/>"><fmt:message key='highlight.saramago.title'/></a>
                                                        <p class="box-sub-title"><fmt:message key='highlight.saramago.description'/></p>
                                                        <p><fmt:message key='highlight.saramago.description2'/></p>
                                                </div>
                                        </div>

                                        <div class="box">
                                                <div class="mascara-img">
                                                        <img src="img/highlights/expo98.gif" alt="<fmt:message key='highlight.expo98.alt'/>" width="80" height="80" />
                                                        <div class="mascara-img-gr"></div>
                                                </div>
                                                <div class="box-info">
							<a href="/wayback/wayback/20000823154833/http://www.parquedasnacoes.pt/pt/expo98/" title="<fmt:message key='highlight.expo98.link.title'/>"><fmt:message key='highlight.expo98.title'/></a>
                                                        <p class="box-sub-title"><fmt:message key='highlight.expo98.description'/></p>
                                                        <p><fmt:message key='highlight.expo98.description2'/></p>
                                                </div>
                                        </div>

                                        <div class="box">
                                                <div class="mascara-img">
                                                        <img src="img/highlights/euro2004.png" alt="<fmt:message key='highlight.euro2004.alt'/>" width="80" height="80" />
                                                        <div class="mascara-img-gr"></div>
                                                </div>
                                                <div class="box-info">
                                                        <a href="/wayback/wayback/20040525212736/http://euro2004.clix.pt/" title="<fmt:message key='highlight.euro2004.link.title'/>"><fmt:message key='highlight.euro2004.title'/></a>
                                                        <p class="box-sub-title"><fmt:message key='highlight.euro2004.description'/></p>
                                                        <p><fmt:message key='highlight.euro2004.description2'/></p>
                                                </div>
                                        </div>

                                        <div class="box">
                                                <div class="mascara-img">
                                                        <img src="img/highlights/figo.png" alt="<fmt:message key='highlight.figo.alt'/>" width="80" height="80" />
                                                        <div class="mascara-img-gr"></div>
                                                </div>
                                                <div class="box-info">
                                                        <a href="/wayback/wayback/20011230091638/http://www.ojogo.pt/17-300/artigo145049.htm" title="<fmt:message key='highlight.figo.link.title'/>"><fmt:message key='highlight.figo.title'/></a>
                                                        <p class="box-sub-title"><fmt:message key='highlight.figo.description'/></p>
                                                        <p><fmt:message key='highlight.figo.description2'/></p>
                                                </div>
                                        </div>
					</fmt:bundle>

					<a href="highlights.jsp?l=<%=language%>" title="<fmt:message key='home.highlights.link'/>" id="ver-destaques"><fmt:message key='home.highlights'/></a>
                                </div>
                        </div>

	</div> <!-- end of wrap -->
<%@include file="include/footer.jsp" %>
<%@include file="include/analytics.jsp" %>
</body>
</html>

<%@include file="include/logging.jsp" %>
