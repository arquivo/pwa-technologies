<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.Date" %>
<%@ page import="org.archive.wayback.ResultURIConverter" %>
<%@ page import="org.archive.wayback.core.Timestamp" %>
<%@ page import="org.archive.wayback.core.UIResults" %>
<%@ page import="org.archive.wayback.core.WaybackRequest" %>
<%@ page import="org.archive.wayback.query.UIQueryResults" %>
<%@ page import="org.archive.wayback.util.StringFormatter" %>
<%@ page import="org.archive.wayback.webapp.AccessPoint" %>
<%@ page import="org.archive.wayback.archivalurl.ArchivalUrlResultURIConverter" %>
<%@ page import="org.springframework.beans.factory.xml.XmlBeanFactory" %>
<%@ page import="org.springframework.core.io.FileSystemResource" %>
<%@ page import="org.springframework.core.io.Resource" %>

<%!
        /* Get the replay URI prefix during servelet initialization */
        String replayURIPrefix = null;
        
        public void jspInit() {
                Resource res = new FileSystemResource( getServletContext().getRealPath("")+"/WEB-INF/wayback.xml");
                XmlBeanFactory factory = new XmlBeanFactory(res);
                AccessPoint ap = (AccessPoint) factory.getBean("80:wayback");
        
                replayURIPrefix = ((ArchivalUrlResultURIConverter)ap.getUriConverter()).getReplayURIPrefix();
        }
%>

<%
UIQueryResults results = (UIQueryResults) UIResults.getFromRequest(request);
ResultURIConverter uriConverter = results.getURIConverter();
String requestDate = results.getExactRequestedTimestamp().getDateStr();
String contextPath = uriConverter.makeReplayURI(requestDate, "");
String contextRoot = request.getScheme() + "://" + request.getServerName() + ":" 
  + request.getServerPort() + request.getContextPath();

String jsUrl = replayURIPrefix + "replay/client-rewrite.js";
%>
<script type="text/javascript">
  var sWayBackCGI = "<%= contextPath %>";
</script>
<script type="text/javascript" src="<%= jsUrl %>" ></script>
