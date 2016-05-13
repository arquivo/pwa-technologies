<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd" >
<%@page import="java.net.URL"%>

<%@ page
	session="true"
	contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"	

	import="java.io.File"
	import="java.io.IOException"
	import="java.net.URLEncoder"
	import="java.text.DateFormat"
	import="java.util.Calendar"
	import="java.util.Date"
	import="java.util.regex.Matcher"
	import="java.util.regex.Pattern"
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

<%
  Configuration nutchConfAlt = NutchwaxConfiguration.getConfiguration(application);
  NutchBean bean2 = NutchwaxBean.get(application, nutchConfAlt);
%>
<%String arquivoHost = nutchConfAlt.get("wax.webhost", "arquivo.pt"); %>
<script type="text/javascript" src="/js/js.cookie.js"></script>
<div id="language">
<!--	<img src="img/experimental.png" alt="<fmt:message key='topbar.experimental.alt'/>" width="123" height="124" /> -->
	<div class="wrap">
		<ul>
			<li><a href="<c:url value='http://sobre.arquivo.pt/'><c:param name='set_language' value='${language}'/></c:url>" title="<fmt:message key='topbar.help'/>" class="ajuda"><fmt:message key='topbar.help'/></a></li>
		</ul>
		<ul class="langs">
		<c:choose>
			<c:when test="${language eq 'pt'}">
			<script type="text/javascript">
				ptHref = window.location.toString();

				if(ptHref.indexOf("l=pt") > -1){ // if found l=en parameter change to l=pt
					enHref = ptHref.replace('?l=pt', '?l=en');
				}
				else{
					enHref = ptHref +"?l=en";
				}
				document.write('<li><a href="'+ ptHref +'" title="<fmt:message key="topbar.portuguese"/>" class="activo"><fmt:message key="topbar.portuguese"/></a></li>');
				document.write('<li><a href="'+ enHref +'" title="<fmt:message key="topbar.english"/>"><fmt:message key="topbar.english"/></a></li>');
		        var arquivoHostName = "<%=arquivoHost%>";
		        Cookies.set('language', 'PT', { expires: 30, path: '/', domain: arquivoHostName  });
			</script>
			
			</c:when>
			<c:otherwise>
			<script type="text/javascript">
				enHref = window.location.toString();

				if(enHref.indexOf("l=en") > -1){ // if found l=en parameter change to l=pt
					ptHref = enHref.replace('?l=en', '?l=pt');
				}
				else{
					ptHref = enHref + "?l=pt";
				}
				document.write('<li><a href="'+ ptHref +'" title="<fmt:message key="topbar.portuguese"/>" ><fmt:message key="topbar.portuguese"/></a></li>');
				document.write('<li><a href="'+ enHref +'" title="<fmt:message key="topbar.english"/>" class="activo"><fmt:message key="topbar.english"/></a></li>');
				var arquivoHostName = "<%=arquivoHost%>";
		        Cookies.set('language', 'EN', { expires: 30, path: '/', domain: arquivoHostName  });
			</script>

			</c:otherwise>
		</c:choose>
		</ul>
	</div>
</div>
