<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd" >
<%@page import="java.net.URL"%>
<%@ page
  session="true"
  contentType="text/html; charset=UTF-8"
  pageEncoding="UTF-8"  

  import="java.io.File"
  import="java.io.IOException"
  import="java.util.Calendar"
  import="java.util.Date"
  import="java.util.GregorianCalendar"
  import="java.net.URLEncoder"
  import= "java.net.*"
  import= "java.io.*"
  import="java.text.DateFormat"
  import="java.text.SimpleDateFormat"
  import="java.util.TimeZone"
  import="java.util.regex.Matcher"
  import="java.util.regex.Pattern"
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
  import="org.apache.commons.lang.StringEscapeUtils"
  import="java.util.Properties"
%>
<% // Set the character encoding to use when interpreting request values.
  request.setCharacterEncoding("UTF-8");
%>
<%
response.setHeader("Cache-Control","public, max-age=600");
%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt_rt" prefix="fmt" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ include file="include/logging_params.jsp" %>
<%@ include file="include/i18n.jsp" %>
<fmt:setLocale value="<%=language%>"/>

<%! //To please the compiler since logging need those 
  private static Calendar DATE_START = new GregorianCalendar(1996, 1-1, 1);
  private static final DateFormat FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");
  //TODO: remove dateStart & dateEnd ???
  //private static Calendar dateStart = new GregorianCalendar();
  //private static Calendar dateEnd = new GregorianCalendar();
  private static final DateFormat OFFSET_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
  private static final Pattern OFFSET_PARAMETER = Pattern.compile("(\\d{4})-(\\d{2})-(\\d{2})");
  
%>

<%
  Properties prop = new Properties();
  prop.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("validTLDs/valid.properties"));
  String tldsLine = prop.getProperty("valid.tld");
  String tlds[] = tldsLine.split("\t");
%>

<%-- Get the application beans --%>
<%
  Configuration nutchConf = NutchwaxConfiguration.getConfiguration(application);
  NutchBean bean = NutchwaxBean.get(application, nutchConf);
%>
<%-- Define the default end date --%>
<%
String imgSrc = "";
String imgHeight = "";
String imgWidth = "";
String imgTstamp = "";
String pageURL = "";
String pageTstamp = "";
String pageTitle = "";
String imgTitle = "";
String imgAlt = "";
String imgMimeType = "";
String backURL = "";
String safe ="";
String collection ="";

if( request.getParameter("imgSrc") != null){
  imgSrc = request.getParameter("imgSrc");
}
if( request.getParameter("imgHeight") != null){
  imgHeight = request.getParameter("imgHeight");
}
if( request.getParameter("imgWidth") != null){
  imgWidth = request.getParameter("imgWidth");
}
if( request.getParameter("imgTstamp") != null){
  imgTstamp = request.getParameter("imgTstamp");
}
if( request.getParameter("pageURL") != null){
  pageURL = request.getParameter("pageURL");
}
if( request.getParameter("pageTitle") != null){
  pageTitle = request.getParameter("pageTitle");
}

if( request.getParameter("pageTstamp") != null){
  pageTstamp = request.getParameter("pageTstamp");
}

if( request.getParameter("imgTitle") != null){
  imgTitle = request.getParameter("imgTitle");
}
if( request.getParameter("imgAlt") != null){
  imgAlt = request.getParameter("imgAlt");
}
if( request.getParameter("imgMimeType") != null){
  imgMimeType = request.getParameter("imgMimeType");
}
if( request.getParameter("backURL") != null){
  backURL = request.getParameter("backURL");
}
if( request.getParameter("safe") != null){
  safe = request.getParameter("safe");
}
if( request.getParameter("collection") != null){
  collection = request.getParameter("collection");
}

String shareTitle= "Arquivo.pt";
String shareDescription = pageTitle + "- Arquivo.pt";
String shareImage = "https://arquivo.pt/wayback/"+imgTstamp+"/"+imgSrc;

%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="pt-PT" lang="pt-PT"><head>
  <title><fmt:message key='images.imageTitle'/>:&nbsp; <c:out value = "${htmlQueryString}"/> &nbsp;  &mdash; Arquivo.pt</title>
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <meta http-equiv="Content-Type" content="application/xhtml+xml; charset=UTF-8"/>
  <meta http-equiv="Content-Language" content="pt-PT"/>
  <meta name="Keywords" content="resultado, pesquisa, buscar, arquivo, Web, português, portuguesa, Portugal"/>
  <meta name="Description" content="Página de resultados de uma pesquisa de imagens feita no Arquivo.pt."/>
  <meta name="theme-color" content="#252525">
  <!-- Windows Phone -->
  <meta name="msapplication-navbutton-color" content="#252525">
  <!-- iOS Safari -->
  <meta name="apple-mobile-web-app-status-bar-style" content="#252525">  
  <meta property="og:title" content="<%=shareTitle%>" />
  <meta property="og:description" content="<%=shareDescription%>" />
  <meta property="og:image" content="<%=shareImage%>" />  

  <link rel="shortcut icon" href="img/logo-16.png" type="image/x-icon"/>
  <link href="css/csspin.css" rel="stylesheet" type="text/css"/>

  <link rel="stylesheet" title="Estilo principal" type="text/css" href="css/newStyle.css?build=<c:out value='${initParam.buildTimeStamp}'/>"  media="all" />
    <!-- font awesome -->
    <link rel="stylesheet" href="css/font-awesome.min.css">
    <!-- bootstrap -->
    <link rel="stylesheet" href="/css/bootstrap.min.css">
    <script src="/js/jquery-latest.min.js"></script>
    <script src="/js/bootstrap.min.js"></script>
    <!-- dual slider dependencies -->
    <script type="text/javascript" src="/js/nouislider.min.js"></script>
    <link rel="stylesheet" href="/css/nouislider.min.css">
    <script type="text/javascript" src="/js/wNumb.js"></script>
    <!-- CSS loading spiner -->
    <link href="css/csspin.css" rel="stylesheet" type="text/css">
    <script type="text/javascript" src="//s7.addthis.com/js/300/addthis_widget.js#pubid=ra-5645cdb2e22ca317"></script> 
    <!-- end addthis for sharing on social media --> 
    <script type="text/javascript" src="js/configs.js"></script>



  <link rel="stylesheet" type="text/css" href="css/jquery-ui-1.7.2.custom.css"/>

  <script src="https://apis.google.com/js/client.js" type="text/javascript"> </script>
  <script type="text/javascript" src="js/ui.datepicker.js"></script>
  <script type="text/javascript" src="js/ui.datepicker-pt-BR.js"></script>
  <!--<script type="text/javascript" src="js/imageConfigs.js"></script>-->


  <script type="text/javascript">
    calendarBegin = '<fmt:message key="calendar.begin" />'.replace("calendario", "calendário");
    calendarEnd = '<fmt:message key="calendar.end" />'.replace("calendario", "calendário");
    /*Object with required properties to display in the view details modal*/
    details = {
      details : '<fmt:message key="images.details.details"/>',
      page  : '<fmt:message key="images.details.page"/>',
      title     : '<fmt:message key="images.details.title"/>',
      image  : '<fmt:message key="images.details.image"/>',
      resolution: '<fmt:message key="images.details.resolution"/>',
      safesearch: '<fmt:message key="images.details.safesearch"/>',
      collection: '<fmt:message key="images.details.collection"/>',
      name: '<fmt:message key="images.details.name"/>',
      visit: '<fmt:message key="images.viewer.visit"/>'
    };
    imageObj = {
      currentImageURL: decodeURIComponent('<%=imgSrc%>'),
      expandedWidth: '<%=imgWidth%>',
      expandedHeight: '<%=imgHeight%>',
      pageTstamp: '<%=pageTstamp%>',
      pageURL: decodeURIComponent('<%=pageURL%>'),
      pageTitleFull: decodeURIComponent('<%=pageTitle%>'),
      titleFull : '<%=imgTitle%>',
      imgAltFull : '<%=imgAlt%>',
      imgMimeType: '<%=imgMimeType%>',
      imgSrc: '<%=imgSrc%>',
      timestamp: '<%=imgTstamp%>',
      backURL: decodeURIComponent('<%=backURL%>'),
      safe: '<%=safe%>',
      collection: '<%=collection%>'
    } 
    Content = {
        months: 
        {  '01': "<fmt:message key="month.0" />",
           '02': "<fmt:message key="month.1" />",
           '03': "<fmt:message key="month.2" />",
           '04': "<fmt:message key="month.3" />",
           '05': "<fmt:message key="month.4" />",
           '06': "<fmt:message key="month.5" />",
           '07': "<fmt:message key="month.6" />",
           '08': "<fmt:message key="month.7" />",
           '09': "<fmt:message key="month.8" />",
           '10': "<fmt:message key="month.9" />",
           '11': "<fmt:message key="month.10" />",
           '12': "<fmt:message key="month.11" />",
        },
    };           
  </script>
  <script src="/js/imagedet.js"></script>

  <script src="@ionic/core/dist/ionic.js"></script>
 

  <script type="text/javascript">
  /*Addthis options share on facebook and twitter*/
    var addthis_config = addthis_config||{};
        addthis_config.data_track_addressbar = false;
        addthis_config.data_track_clickback = false;
  </script>
  <script type="text/javascript" src="//s7.addthis.com/js/300/addthis_widget.js#pubid=ra-5645cdb2e22ca317" async="async"></script>
  <%@include file="include/analytics.jsp" %>
</head>
<body>

  <%@ include file="include/topbar.jsp" %>
  <div id="expandedImageViewers">
    <div id="testViewer1" class="height-vh image-mobile-expanded-div no-outline" tabindex="1">
      <div class="row full-height no-outline">
        <div id="insert-card-1" class="full-height col-sm-8 col-sm-offset-2 col-md-4 col-md-offset-4 text-right"></div>
      </div>
    </div>
  </div>  


</div></div></div>
<%@include file="include/analytics.jsp" %>
<%@include file="include/footer.jsp" %>
</body>
</html>

