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
	import="java.net.URLEncoder"
	import= "java.net.*"
	import= "java.io.*"	
	import="java.text.DateFormat"
	import="java.util.Calendar"
	import="java.util.TimeZone"
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
<% // Set the character encoding to use when interpreting request values.
  request.setCharacterEncoding("UTF-8");
%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt_rt" prefix="fmt" %>
<%@ include file="include/logging_params.jsp" %>
<%@ include file="include/i18n.jsp" %>
<fmt:setLocale value="<%=language%>"/>

<%
  String title = request.getParameter("title");
  if (title == null) {
    title = "Arquivo.pt";
  }
  String description = request.getParameter("description");
  if (description == null) {
    description = "Pesquise páginas do passado desde 1996";
  }
  String image = request.getParameter("image");
  if (image == null) {
    image = "//arquivo.pt/img/logoFace.png";
  }else{
    image = URLDecoder.decode(image.replace("+", "%2B"), "UTF-8");
  }

  String url = request.getParameter("url");
  if (url == null) {
    url = "https://arquivo.pt";
  }else{
    url = URLDecoder.decode(url.replace("+", "%2B"), "UTF-8");
  }

%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="pt-PT" lang="pt-PT">
<head>
  <meta property="og:title" content="<%=title%>" />
  <meta property="og:description" content="<%=description%>" />

  <%--
  <meta property="og:image:width" content="<%=imageWidth%>" />
  <meta property="og:image:height" content="<%=imageHeight%>" />
  --%>
  <meta property="og:image" content="<%=image%>" />
  <script type="text/javascript" async="" src="//www.google-analytics.com/ga.js"></script>

<%@include file="include/analytics.jsp" %>
</head>
<script type="text/javascript">window.location = "<%=url%>" ;</script>
<body>
</body>
</html>
