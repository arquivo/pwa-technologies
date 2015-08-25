<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.Date" %>
<%@ page import="org.archive.wayback.WaybackConstants" %>
<%@ page import="org.archive.wayback.core.Timestamp" %>
<%@ page import="org.archive.wayback.core.SearchResult" %>
<%@ page import="org.archive.wayback.core.UIResults" %>
<%@ page import="org.archive.wayback.core.WaybackRequest" %>
<%@ page import="org.archive.wayback.query.UIQueryResults" %>
<%@ page import="org.archive.wayback.util.StringFormatter" %>
<%@ page import="org.archive.wayback.webapp.AccessPoint" %>
<%@ page import="org.archive.wayback.archivalurl.ArchivalUrlResultURIConverter" %>
<%@ page import="org.springframework.beans.factory.xml.XmlBeanFactory" %>
<%@ page import="org.springframework.core.io.FileSystemResource" %>
<%@ page import="org.springframework.core.io.Resource" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

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
        String language = "pt";

        String langParam = request.getParameter("l");
        if (langParam != null) {
                if (langParam.equals("en")) {
                        language = langParam;
                } else { /* keep default */ };
        }
%>
<fmt:setLocale value="<%= language %>"/>
<%
UIQueryResults results = (UIQueryResults) UIResults.getFromRequest(request);

StringFormatter fmt = results.getFormatter();
SearchResult result = results.getResult();
String dupeMsg = "";
if(result != null) {
        String dupeType = result.get(WaybackConstants.RESULT_DUPLICATE_ANNOTATION);
        if(dupeType != null) {
                String dupeDate = result.get(WaybackConstants.RESULT_DUPLICATE_STORED_DATE);
                String prettyDate = "";
                if(dupeDate != null) {
                	  Timestamp dupeTS = Timestamp.parseBefore(dupeDate);
                    prettyDate = "(" + 
                    		fmt.format("MetaReplay.captureDateDisplay",
                    				dupeTS.getDate()) + ")";
                }
                dupeMsg = " Note that this document was downloaded, and not saved because it was a duplicate of a previously captured version " + 
                          prettyDate + ". HTTP headers presented here are from the original capture.";
        }
}

Date requestDate = results.getExactRequestedTimestamp().getDate();
String requestUrl = results.getSearchUrl();

String contextRoot = request.getScheme() + "://" + request.getServerName() + ":"
+ request.getServerPort() + request.getContextPath();
String jsUrl = replayURIPrefix +"replay/disclaim.js";
%>
<script type="text/javascript">
String.prototype.getHostname = function() {
	var re = new RegExp('^(?:f|ht)tp(?:s)?\://([^/]+)', 'im');
	return this.match(re)[1].toString();
}

function goBack() {
	var prev = top.window.location.protocol +'//'+ top.window.location.host;

	if ( top.document.referrer != undefined && top.document.referrer != '') {
		prev = top.document.referrer;
	}

	if (top.window.location.host !== prev.getHostname() ) {
		prev = top.window.location.protocol +'//'+ top.window.location.host;
	}

	top.window.location = prev;
	return false;	/* Prevent the anchor default behavior if permormed as 'onclick' */
}

function goHomepage() {
	top.window.location = top.window.location.protocol +'//'+ top.window.location.host;
}

  var wmNotice = '<fmt:message key="ReplayView.banner"><fmt:param value="javascript:goHomepage()"/><fmt:param value="<%= requestUrl %>"/><fmt:param value="<%= requestDate %>"/></fmt:message><%= dupeMsg %>';
  var wmHideNotice = '<fmt:message key="ReplayView.bannerHideLink"/>';
  var logoPath = '<%= replayURIPrefix %>images/awp-small_logo.png';
  var logoAltText = '<fmt:message key="ReplayView.logoAltText"/>';
</script>
<script type="text/javascript" src="<%= jsUrl %>"></script>
