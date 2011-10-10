<?xml version="1.0" encoding="UTF-8"?>
<%@ page 
  session="true"
  contentType="application/opensearchdescription+xml; charset=UTF-8"
  pageEncoding="UTF-8"
  import="java.util.Locale"
%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@include file="include/i18n.jsp" %>
<i18n:bundle baseName="org.nutch.jsp.search" locale="<%= new Locale(language) %>"/>
<c:set var="rootUrl" scope="page" value="http://${pageContext.request.serverName}${pageContext.request.contextPath}"/>
<OpenSearchDescription xmlns="http://a9.com/-/spec/opensearch/1.1/">
	<ShortName><i18n:message key="opensearch.title"><i18n:messageArg value="<%=language%>"/></i18n:message></ShortName>
	<LongName><i18n:message key="opensearch.longname"/></LongName>
	<Description><i18n:message key="opensearch.description"/></Description>
	<InputEncoding>UTF-8</InputEncoding>
	<Tags>web archive arquivo Português Portuguese Portugal</Tags>
	<Language><%=language%></Language>
	<Image width="16" height="16" type="image/x-icon"><c:out value="${rootUrl}"/>/img/logo-16.jpg</Image>
	<Url type="text/html" method="get" template="<c:out value="${rootUrl}"/>/search.jsp?query={searchTerms}&amp;l=<%=language%>"/>
	<Url type="rss+html" method="get" template="<c:out value="${rootUrl}"/>/opensearch?query={searchTerms}"/>
        <Url type="application/opensearchdescription+xml" rel="self" template="<c:out value="${rootUrl}"/>/opensearch.jsp" />
</OpenSearchDescription>
