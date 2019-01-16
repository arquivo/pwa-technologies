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
    import="org.apache.hadoop.conf.Configuration"
    import="org.apache.lucene.search.PwaFunctionsWritable"
    import="org.apache.nutch.global.Global"
    import="org.apache.nutch.html.Entities"
    import="org.apache.nutch.metadata.Nutch"
    import="org.apache.nutch.searcher.Hit"
    import="org.apache.nutch.searcher.HitDetails"
    import="org.apache.nutch.searcher.Hits"
    import="org.apache.nutch.searcher.Query"
    import="org.apache.nutch.searcher.Query.Clause"
    import="org.apache.nutch.searcher.NutchBean"
    import="org.apache.nutch.searcher.Summary"
    import="org.apache.nutch.searcher.Summary.Fragment"
    import="org.archive.access.nutch.NutchwaxBean"
    import="org.archive.access.nutch.NutchwaxQuery"
    import="org.archive.access.nutch.NutchwaxConfiguration"
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

<%-- Get the application beans --%>
<%
  Configuration nutchConf = NutchwaxConfiguration.getConfiguration(application);
  NutchBean bean = NutchwaxBean.get(application, nutchConf);
%>


<%---------------------- Start of HTML ---------------------------%>

<%-- TODO: define XML lang --%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="pt-PT" lang="pt-PT">
<head>
    <%@ include file="include/checkMobile.jsp" %>
	<title><fmt:message key='home.meta.title'/></title>
	<meta http-equiv="Content-Type" content="application/xhtml+xml; charset=UTF-8" />
	<%-- TODO: define META lang --%>
	<meta http-equiv="Content-Language" content="pt-PT" />
	<meta name="Keywords" content="<fmt:message key='home.meta.keywords'/>" />
	<meta name="Description" content="<fmt:message key='home.meta.description'/>" />
    <meta property="og:title" content="<fmt:message key='home.meta.title'/>"/>
    <meta property="og:description" content="<fmt:message key='home.meta.description'/>"/>
    <% String arquivoHostName = nutchConf.get("wax.webhost", "arquivo.pt"); %>
    <meta property="og:image" content="//<%=arquivoHostName%>/img/logoFace.png"/>
	<link rel="shortcut icon" href="img/logo-16.png" type="image/x-icon" />
	<link rel="search" type="application/opensearchdescription+xml" title="<fmt:message key='opensearch.title'><fmt:param value='<%=language%>'/></fmt:message>" href="opensearch.jsp?l=<%=language%>" />
	<link rel="stylesheet" title="Estilo principal" type="text/css" href="css/style.css"  media="all" />
    <script src="/js/jquery-latest.min.js"> </script>
</head>
<body>
	<%@ include file="include/topbar.jsp" %>
	<div class="wrap">
		<div id="header-home">
                                <div id="logo-home">
                                    <a href="<fmt:message key='home.prizes.href'/>"> 
                                        <img src="img/banner-premiosarquivo-<%=language%>.png?v=premio2019" alt="<fmt:message key='header.logo.alt'/>" style="padding-bottom: 15px;" />
                                    </a>                                        
                                </div>
                                <div id="search-home">
                                        <form id="loginForm" action="search.jsp" name="loginForm" method="get">
						<input type="hidden" name="l" value="<%= language %>" />
                                                <fieldset>
                                                        <label for="txtSearch">&nbsp;</label>
                                                        <input class="search-inputtext" type="text" size="15"  placeholder="<fmt:message key='search.value'/>" onfocus="this.placeholder = ''" onblur="if(this.placeholder == ''){this.placeholder='<fmt:message key='search.value'/>'}"  name="query" id="txtSearch" accesskey="t" />
                                                        <input type="reset" value="&nbsp;" alt="reset" class="search-resetbutton" name="btnReset" id="btnReset" accesskey="r" />
                                                        <input type="submit" value="<fmt:message key='home.submit'/>" class="home-submit" name="btnSubmit" id="btnSubmit" accesskey="e" />
                                                        <a href="advanced.jsp?l=<%=language%>" onclick="ga('send', 'event', 'Homepage', 'pesquisa-avancada', 'Click on link (Pesquisa avancada)');{document.getElementById('pesquisa-avancada').setAttribute('href',document.getElementById('pesquisa-avancada').getAttribute('href')+'&query='+encodeHtmlEntity(document.getElementById('txtSearch').value))}" title="<fmt:message key='home.advanced.link.title'/>" id="pesquisa-avancada"><fmt:message key='home.advanced.link'/></a>
                                                        <script type="text/javascript">
                                                          String.prototype.replaceAll = String.prototype.replaceAll || function(needle, replacement) {
                                                              return this.split(needle).join(replacement);
                                                          };
                                                        </script>
                                                        <script type="text/javascript">
                                                            function encodeHtmlEntity(str) {

                                                                str = str.replaceAll('ç','%26ccedil%3B')
                                                                         .replaceAll('Á','%26Aacute%3B')
                                                                         .replaceAll('á','%26aacute%3B')
                                                                         .replaceAll('À','%26Agrave%3B')
                                                                         .replaceAll('Â','%26Acirc%3B')
                                                                         .replaceAll('à','%26agrave%3B')
                                                                         .replaceAll('â','%26acirc%3B')
                                                                         .replaceAll('Ä','%26Auml%3B')
                                                                         .replaceAll('ä','%26auml%3B')
                                                                         .replaceAll('Ã','%26Atilde%3B')
                                                                         .replaceAll('ã','%26atilde%3B')
                                                                         .replaceAll('Å','%26Aring%3B')
                                                                         .replaceAll('å','%26aring%3B')
                                                                         .replaceAll('Æ','%26Aelig%3B')
                                                                         .replaceAll('æ','%26aelig%3B')
                                                                         .replaceAll('Ç','%26Ccedil%3B')
                                                                         .replaceAll('Ð','%26Eth%3B')
                                                                         .replaceAll('ð','%26eth%3B')
                                                                         .replaceAll('É','%26Eacute%3B')
                                                                         .replaceAll('é','%26eacute%3B')
                                                                         .replaceAll('È','%26Egrave%3B')
                                                                         .replaceAll('è','%26egrave%3B')
                                                                         .replaceAll('Ê','%26Ecirc%3B')
                                                                         .replaceAll('ê','%26ecirc%3B')
                                                                         .replaceAll('Ë','%26Euml%3B')
                                                                         .replaceAll('ë','%26euml%3B')
                                                                         .replaceAll('Í','%26Iacute%3B')
                                                                         .replaceAll('í','%26iacute%3B')
                                                                         .replaceAll('Ì','%26Igrave%3B')
                                                                         .replaceAll('ì','%26igrave%3B')
                                                                         .replaceAll('Î','%26Icirc%3B')
                                                                         .replaceAll('î','%26icirc%3B')
                                                                         .replaceAll('Ï','%26Iuml%3B')
                                                                         .replaceAll('ï','%26iuml%3B')
                                                                         .replaceAll('Ñ','%26Ntilde%3B')
                                                                         .replaceAll('ñ','%26ntilde%3B')
                                                                         .replaceAll('Ó','%26Oacute%3B')
                                                                         .replaceAll('ó','%26oacute%3B')
                                                                         .replaceAll('Ò','%26Ograve%3B')
                                                                         .replaceAll('ò','%26ograve%3B')
                                                                         .replaceAll('Ô','%26Ocirc%3B')
                                                                         .replaceAll('ô','%26ocirc%3B')
                                                                         .replaceAll('Ö','%26Ouml%3B')
                                                                         .replaceAll('ö','%26ouml%3B')
                                                                         .replaceAll('Õ','%26Otilde%3B')
                                                                         .replaceAll('õ','%26otilde%3B')
                                                                         .replaceAll('Ø','%26Oslash%3B')
                                                                         .replaceAll('ø','%26oslash%3B')
                                                                         .replaceAll('ß','%26szlig%3B')
                                                                         .replaceAll('Þ','%26Thorn%3B')
                                                                         .replaceAll('þ','%26thorn%3B')
                                                                         .replaceAll('Ú','%26Uacute%3B')
                                                                         .replaceAll('ú','%26uacute%3B')
                                                                         .replaceAll('Ù','%26Ugrave%3B')
                                                                         .replaceAll('ù','%26ugrave%3B')
                                                                         .replaceAll('Û','%26Ucirc%3B')
                                                                         .replaceAll('û','%26ucirc%3B')
                                                                         .replaceAll('Ü','%26Uuml%3B')
                                                                         .replaceAll('ü','%26uuml%3B')
                                                                         .replaceAll('Ý','%26Yacute%3B')
                                                                         .replaceAll('ý','%26yacute%3B')
                                                                         .replaceAll('ÿ','%26yuml%3B')
                                                                         .replaceAll('©','%26copy%3B')
                                                                         .replaceAll('®','%26reg%3B')
                                                                         .replaceAll('™','%26trade%3B')
                                                                         .replaceAll('&','%26amp%3B')
                                                                         .replaceAll('<','%26lt%3B')
                                                                         .replaceAll('>','%26gt%3B')
                                                                         .replaceAll('€','%26euro%3B')
                                                                         .replaceAll('¢','%26cent%3B')
                                                                         .replaceAll('£','%26pound%3B')
                                                                         .replaceAll('\"','%26quot%3B')
                                                                         .replaceAll('‘','%26lsquo%3B')
                                                                         .replaceAll('’','%26rsquo%3B')
                                                                         .replaceAll('“','%26ldquo%3B')
                                                                         .replaceAll('”','%26rdquo%3B')
                                                                         .replaceAll('«','%26laquo%3B')
                                                                         .replaceAll('»','%26raquo%3B')
                                                                         .replaceAll('—','%26mdash%3B')
                                                                         .replaceAll('–','%26ndash%3B')
                                                                         .replaceAll('°','%26deg%3B')
                                                                         .replaceAll('±','%26plusmn%3B')
                                                                         .replaceAll('¼','%26frac14%3B')
                                                                         .replaceAll('½','%26frac12%3B')
                                                                         .replaceAll('¾','%26frac34%3B')
                                                                         .replaceAll('×','%26times%3B')
                                                                         .replaceAll('÷','%26divide%3B')
                                                                         .replaceAll('α','%26alpha%3B')
                                                                         .replaceAll('β','%26beta%3B')
                                                                         .replaceAll('∞','%26infin%3B')
                                                                         .replaceAll(' ','+');


                                                                return str;
                                                            }
                                                        </script>
                                                </fieldset>
                                        </form>
                                </div>
                                <div id="info">
                                        <div id="info-texto-home">
                                                <h1><fmt:message key='home.title'/></h1>
                                        </div>
                                </div>
		</div>

        <c:choose>
            <c:when test="${language eq 'pt'}">          
                <div id="video-home"><iframe width="480" height="270" src="https://www.youtube.com/embed/2HEudlXPV4o?rel=0&amp;showinfo=0&amp;list=PLKfzD5UuSdETtSCX_TM02nSP7JDmGFGIE" frameborder="0" allowfullscreen></iframe></div>
            </c:when>
            <c:otherwise>
                <div id="video-home"><iframe width="480" height="270" src="https://www.youtube.com/embed/dqG0VILi3gs?rel=0&amp;showinfo=0" frameborder="0" allowfullscreen></iframe></div>
            </c:otherwise>
        </c:choose>

		<div id="main-home">
                                <h3><fmt:message key='home.examples.title'/></h3>
                                <!--<h4><fmt:message key='home.examples.subtitle'/></h4>-->

				<fmt:bundle basename='pt.arquivo.i18n.Highlights'>
                                <div class="boxes-home" id="boxes">
					<%-- Let the box be clickable. Not just that one text --%>
                                        <div class="box">
                                                <div class="mascara-img">
                                                        <img src="img/highlights/saramago.png" alt="<fmt:message key='highlight.saramago.alt'/>" width="80" height="80" />
                                                        <div class="mascara-img-gr"></div>
                                                </div>
                                                <div class="box-info">
                                                        <a href="/wayback/19980205082901/http://www.caleida.pt/saramago/" title="<fmt:message key='highlight.saramago.link.title'/>" onclick="ga('send', 'event', 'Homepage', 'jose-saramago', 'Click on link (Exemplos)');"><fmt:message key='highlight.saramago.title'/></a>
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
							<a href="/wayback/20000823154833/http://www.parquedasnacoes.pt/pt/expo98/" title="<fmt:message key='highlight.expo98.link.title'/>" onclick="ga('send', 'event', 'Homepage', 'expo-98', 'Click on link (Exemplos)');"><fmt:message key='highlight.expo98.title'/></a>
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
                                                        <a href="/wayback/20040525212736/http://euro2004.clix.pt/" title="<fmt:message key='highlight.euro2004.link.title'/>" onclick="ga('send', 'event', 'Homepage', 'euro-2004', 'Click on link (Exemplos)');"><fmt:message key='highlight.euro2004.title'/></a>
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
                                                        <a href="/wayback/20011230091638/http://www.ojogo.pt/17-300/artigo145049.htm" title="<fmt:message key='highlight.figo.link.title'/>" onclick="ga('send', 'event', 'Homepage', 'Luis Figo', 'Click on link (Exemplos)');"><fmt:message key='highlight.figo.title'/></a>
                                                        <p class="box-sub-title"><fmt:message key='highlight.figo.description'/></p>
                                                        <p><fmt:message key='highlight.figo.description2'/></p>
                                                </div>
                                        </div>
					</fmt:bundle>
					
					
<!--Inspect the language for returning the proper highlight page  -->
<c:choose>
  <c:when test="${language == 'pt'}">
   <a href="//sobre.arquivo.pt/pt/exemplos" title="<fmt:message key='home.highlights.link'/>" id="ver-destaques" onclick="ga('send', 'event', 'Homepage', 'ver-mais-pt', 'Click on link (vermais)');"><fmt:message key='home.highlights'/></a>
  </c:when>
  <c:otherwise>
    <a href="//sobre.arquivo.pt/en/examples" title="<fmt:message key='home.highlights.link'/>" id="ver-destaques" onclick="ga('send', 'event', 'Homepage', 'ver-mais-pt', 'Click on link (vermais)');"><fmt:message key='home.highlights'/></a>
  </c:otherwise>
</c:choose>



					
                                </div>
                        </div>

	</div> <!-- end of wrap -->
<%@include file="include/footer.jsp" %>
<%@include file="include/analytics.jsp" %>
</body>
</html>

<%@include file="include/logging.jsp" %>
