<!DOCTYPE html>
<%@ page
	session="true"
	contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"	

	import="java.io.File"
	import="java.util.Calendar"
	import="java.util.Date"
	import="java.util.GregorianCalendar"

    import="org.apache.hadoop.conf.Configuration"
    import="org.apache.nutch.searcher.Hit"
    import="org.apache.nutch.searcher.HitDetails"
    import="org.apache.nutch.searcher.NutchBean"

    import="org.archive.access.nutch.NutchwaxConfiguration"
%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt_rt" prefix="fmt" %>
<%@ include file="../include/logging_params.jsp" %>
<%@ include file="../include/i18n.jsp" %>
<fmt:setLocale value="<%=language%>"/>

<%!	//To please the compiler since logging need those -- check [search.jsp]
	private static int hitsTotal = -10;		// the value -10 will be used to mark as being "advanced search"
	private static Calendar DATE_START = new GregorianCalendar(1996, 1-1, 1);
	private static Calendar dateStart = new GregorianCalendar();
	private static Calendar dateEnd = new GregorianCalendar();

    private static final int ERROR = -1;
    private static final int MISSING_PARAMETER = 0;
    private static final int RESULT_FOUND = 1;
%>

<%
    request.setCharacterEncoding("UTF-8");

    Configuration nutchConf = NutchwaxConfiguration.getConfiguration(application);
    NutchBean bean = NutchBean.get(application, nutchConf);

    String docIndex = request.getParameter("idx");
    String docId = request.getParameter("id");

    HitDetails details = null;

    int inspectStatus = 0;

    try {
        if (docIndex != null && docId != null) {
            Hit hit = new Hit(Integer.parseInt(docIndex), Integer.parseInt(docId));
            details = bean.getDetails(hit);

            inspectStatus = RESULT_FOUND;
        } else {
            inspectStatus = MISSING_PARAMETER;
            docIndex = "";
            docId = "";
        }
    } catch(Exception error) {
        inspectStatus = ERROR;
        bean.LOG.error("ERROR while inspecting for document in IDX: "+ docIndex + " and ID: "+ docId);
        bean.LOG.error(error.toString());
    }

    SimpleDateFormat timestampDateFormatter = new SimpleDateFormat("yyyyMMddhhmmssSSS");
    SimpleDateFormat outputDateFormatter = new SimpleDateFormat("dd/MM/yyyy - hh:mm:ss");
%>

<%---------------------- Start of HTML ---------------------------%>

<fmt:bundle basename='pt.arquivo.i18n.Utilities'>
<html xmlns="http://www.w3.org/1999/xhtml" lang="<%=language%>">
<head>
	<title><fmt:message key='inspector.meta.title'/></title>
	<meta charset="UTF-8" />
	<meta name="Keywords" content="<fmt:message key='inspector.meta.keywords'/>" />
	<meta name="Description" content="<fmt:message key='inspector.meta.description'/>" />
	<link rel="shortcut icon" href="<c:out value="${pageContext.servletContext.contextPath}" />/img/logo-16.jpg" type="image/x-icon" />
	<link rel="stylesheet" type="text/css" href="../css/style.css"  media="all" />
	<link rel="stylesheet" type="text/css" href="inspector.css" media="all" />
</fmt:bundle>
</head>
<body>
	<%@ include file="../include/topbar.jsp" %>
	<div class="wrap">
		<div id="main">
			<div id="header">
				<%@ include file="../include/logo.jsp" %>
				<div id="info-texto-termos">
                <fmt:bundle basename='pt.arquivo.i18n.Utilities'>
					<h1><fmt:message key='inspector.title' /></h1>
					<h2><fmt:message key='inspector.subtitle' /></h2>
                </fmt:bundle>
				</div>
			</div>

    <fmt:bundle basename='pt.arquivo.i18n.Utilities'>
			<div id="conteudo-termos">
                <div class="inspector-form">
                <form action="" method="GET">
                        <label>
                            <fmt:message key='inspector.form.index'/>
                            <input class="input-idx" type="text" name="idx" value="<%=docIndex%>" />
                        </label>
                        <label>
                            <fmt:message key='inspector.form.id'/>
                            <input class="input-id" type="text" name="id" value="<%=docId%>" />
                        </label>
                        <input class="submit" type="submit" value="<fmt:message key='inspector.form.submit'/>" />
                </form>
                </div>

                <%
                /** Did we get an error search for the document info? */
                if(inspectStatus == ERROR) {
                %>
                <div class="message-pane">
                    <div class="error">
                        <h2><fmt:message key='inspector.error.title'/></h2>
                        <p><fmt:message key='inspector.error.subtitle'/></p>
                        <ul>
                            <li><fmt:message key='inspector.error.cause1'/></li>
                            <li><fmt:message key='inspector.error.cause2'/></li>
                            <li><fmt:message key='inspector.error.cause3'/></li>
                        </ul>
                    </div>
                </div>

                <%
                }
                /** Where one or more of the parameters missing? */
                if(inspectStatus == MISSING_PARAMETER) {
                %>
                <div class="message-pane">
                    <div class="no-result">
                        <h2><fmt:message key='inspector.no-result.title'/></h2>
                    </div>
                </div>

                <%
                }
                if(inspectStatus == RESULT_FOUND) {
                %>
                <div class="document-info">
                
                    <h2>Details for the document with ID: <em><%=docId%></em> in collection <em><%=details.getValue("collection")%></em> (Index: <%=docIndex%>)</h2>

                    <h3><fmt:message key='inspector.result.original'/></h3>
                    <dl class="inline clearfix">
                        <dt><fmt:message key='inspector.result.original.url'/></dt>
                            <dd><%=details.getValue("url")%></dd>
                        <dt><fmt:message key='inspector.result.original.date'/></dt>
                            <% Date timestamp = timestampDateFormatter.parse(details.getValue("tstamp")); %>
                            <dd><%=outputDateFormatter.format(timestamp)%></dd>
                        <dt><fmt:message key='inspector.result.original.title'/></dt>
                            <dd><%=details.getValue("title")%></dd>
                        <dt><fmt:message key='inspector.result.original.content-type'/></dt>
                            <dd><%=details.getValue("primaryType")%>/<%=details.getValue("subType")%></dd>
                        <dt><fmt:message key='inspector.result.original.encoding'/></dt>
                            <dd><%=details.getValue("encoding")%></dd>
                   </dl>

                    <h3><fmt:message key='inspector.result.arc'/></h3>
                    <dl class="inline clearfix">
                        <dt><fmt:message key='inspector.result.arc.collection'/></dt>
                            <dd><%=details.getValue("collection")%></dd>
                        <dt><fmt:message key='inspector.result.arc.arcname'/></dt>
                            <dd><%=details.getValue("arcname")%></dd>
                        <dt><fmt:message key='inspector.result.arc.offset'/></dt>
                            <dd><%=details.getValue("arcoffset")%></dd>
                        <dt><fmt:message key='inspector.result.arc.length'/></dt>
                            <dd><%=details.getValue("contentLength")%> bytes</dd>
                    </dl>
                </div>

            <%
            }
            %>

			</div>
		</div>
	</div>
    </fmt:bundle>
<%@include file="../include/footer.jsp" %>
<%@include file="../include/analytics.jsp" %>
</body>
</html>

<%@include file="../include/logging.jsp" %>
