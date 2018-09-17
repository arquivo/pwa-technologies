<%@ page 
  contentType="text/html; charset=UTF-8"
  pageEncoding="UTF-8"

  import="javax.servlet.*"
  import="javax.servlet.http.*"
  import="java.io.*"
  import="java.util.*"

  import="org.apache.nutch.html.Entities"
  import="org.apache.hadoop.conf.*"
  import="org.apache.nutch.searcher.*"
  import="org.apache.nutch.crawl.*"
  import="org.archive.access.nutch.NutchwaxConfiguration"
%><%
  Configuration nutchConf = NutchwaxConfiguration.getConfiguration(application);
  NutchBean bean = NutchBean.get(application, nutchConf);
  // set the character encoding to use when interpreting request values 
  request.setCharacterEncoding("UTF-8");
  bean.LOG.info("anchors request from " + request.getRemoteAddr());
  Hit hit = new Hit(Integer.parseInt(request.getParameter("idx")),
                    Integer.parseInt(request.getParameter("id")));
  HitDetails details = bean.getDetails(hit);
  String language =
    ResourceBundle.getBundle("org.nutch.jsp.anchors", request.getLocale())
    .getLocale().getLanguage();
  String requestURI = HttpUtils.getRequestURL(request).toString();
  String base = requestURI.substring(0, requestURI.lastIndexOf('/'));
%><!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<%
  // To prevent the character encoding declared with 'contentType' page
  // directive from being overriden by JSTL (apache i18n), we freeze it
  // by flushing the output buffer. 
  // see http://java.sun.com/developer/technicalArticles/Intl/MultilingualJSP/
  out.flush();
%>
<%@ taglib uri="http://jakarta.apache.org/taglibs/i18n" prefix="i18n" %>
<i18n:bundle baseName="org.nutch.jsp.anchors"/>
<html lang="<%= language %>">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<head>
<title>Nutch: <i18n:message key="title"/></title>
<jsp:include page="/include/style.html"/>
</head>

<body>

<jsp:include page="/header.html"/>

<h3>
<i18n:message key="page">
  <i18n:messageArg value="<%=details.getValue("url")%>"/>
</i18n:message>
</h3>

<h3><i18n:message key="anchors"/></h3>

<ul>
<%
  String[] anchors = bean.getAnchors(details);
  if (anchors != null) {
    for (int i = 0; i < anchors.length; i++) {
%><li><%=Entities.encode(anchors[i])%></li>
<%   } %>
<% } %>
</ul>


<h3><i18n:message key="anchors"/> and link</h3>

<ul>
<%
  Inlinks inlinks = bean.getInlinks(details);
  HashMap<String,Inlink> domainToInlink = new HashMap<String,Inlink>();
  if (inlinks != null) {
        Iterator itr=inlinks.iterator();
        while (itr.hasNext()) {
            Inlink inlink = (Inlink)itr.next();
            String url=inlink.getFromUrl();
            String anchor=inlink.getAnchor();

            String domain = null;                       // extract domain name
            try {
                domain = new URL(inlink.getFromUrl()).getHost();
            }
            catch (MalformedURLException e) {}

            if (domainToInlink.get(domain) == null) { // add only one per domain
%>               <li><%=Entities.encode(anchor)%> <%=url%></li>
<%               domainToInlink.put(domain,inlink);
            }
        }
  } %>
</ul>

    
<jsp:include page="/include/footer.html"/>

</body>     
</html>
