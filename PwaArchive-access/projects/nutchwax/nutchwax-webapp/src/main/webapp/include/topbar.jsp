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
<!-- Main Menu Dependencies -->
<link rel="stylesheet" href="css/swiper.min.css">
<link rel="stylesheet" href="css/MainMenu.css">
<%@ include file="MainMenu.jsp" %>
<script src="https://ajax.googleapis.com/ajax/libs/dojo/1.13.0/dojo/dojo.js"></script>
<script type="text/javascript">MENU.init()</script> 
<script type="text/javascript" src="/js/js.cookie.js"></script>
<script>
	localStorage.setItem("language", "<%=language%>".toUpperCase());
	/*Cookies.set("language", "<%=language%>".toUpperCase());*/
</script>
<div class="main-content">
	<div class="container-fluid">
		 <div class="row text-center logo-main-div">
		                    <a class="pull-left main-menu" id="menuButton"><i class="fa fa-bars line-height"></i></a>
		                    <a href="/?l=<%=language%>"><img src="/img/arquivo-logo-white.svg" id="arquivoLogo" alt="Logo Arquivo.pt" class="text-center logo-main"></a>
		                    <!-- New code: starts Opções button ans Language selection-->
		                    <a href="#" class="menu-opcoes" title="Arquivo.pt">Op&ccedil;&otilde;es</a>
		                    <!--<a href="#" class="select-language" title="Arquivo.pt">EN</a>-->
		                    <!-- ends Opções button -->
		 </div> 	
	</div>
</div>	
<script type="text/javascript">
$('#languageSelection').click( function(e) {
		e.preventDefault();
		window.location = toggleLanguage(); 
		return false; } );
</script>
<!-- NEW - 23.07.19: Call ionic -->
<script src="@ionic/core/dist/ionic.js"></script>
<link rel="stylesheet" href="@ionic/core/css/ionic.bundle.css">