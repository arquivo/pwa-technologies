<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd" >
<%@ page
	session="true"
	contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"	

	import="java.io.File"
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
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt_rt" prefix="fmt" %>
<%@ include file="include/logging_params.jsp" %>
<%@ include file="include/i18n.jsp" %>
<fmt:setLocale value="<%=language%>"/>

<%!	//To please the compiler since logging need those -- check [search.jsp]
    private static final Pattern OFFSET_PARAMETER = Pattern.compile("(\\d{4})-(\\d{2})-(\\d{2})");
	private static int hitsTotal = -10;		// the value -10 will be used to mark as being "advanced search"
	private static Calendar DATE_START = new GregorianCalendar(1996, 1-1, 1);
    Calendar dateStart = (Calendar)DATE_START.clone();
    SimpleDateFormat inputDateFormatter = new SimpleDateFormat("dd/MM/yyyy");
	/*private static Calendar dateStart = new GregorianCalendar();*/
	private static Calendar dateEnd = new GregorianCalendar();
    String dateStartString = inputDateFormatter.format( dateStart.getTime() );
    String dateStartYear = dateStartString.substring(dateStartString.length()-4);

    String yearStartNoParameter = "1996";    


%>

<%-- Get the application beans --%>
<%
  Configuration nutchConf = NutchwaxConfiguration.getConfiguration(application);
  NutchBean bean = NutchwaxBean.get(application, nutchConf);

  Calendar DATE_END = new GregorianCalendar();
  DATE_END.set( Calendar.YEAR, DATE_END.get(Calendar.YEAR) );
  DATE_END.set( Calendar.MONTH, 12-1 );
  DATE_END.set( Calendar.DAY_OF_MONTH, 31 );
  DATE_END.set( Calendar.HOUR_OF_DAY, 23 );
  DATE_END.set( Calendar.MINUTE, 59 );
  DATE_END.set( Calendar.SECOND, 59 );
  int queryStringParameter= 0;
  String dateEndString="";
  String dateEndYear="";
  /** Read the embargo offset value from the configuration page. If not present, default to: -1 year */
  try {
        String offsetDateString = getServletContext().getInitParameter("embargo-offset");

        Matcher offsetMatcher = OFFSET_PARAMETER.matcher( offsetDateString );
        offsetMatcher.matches();
        int offsetYear = Integer.parseInt(offsetMatcher.group(1));
        int offsetMonth = Integer.parseInt(offsetMatcher.group(2));
        int offsetDay = Integer.parseInt(offsetMatcher.group(3));

        DATE_END.set(Calendar.YEAR, DATE_END.get(Calendar.YEAR) - offsetYear);
        DATE_END.set(Calendar.MONTH, DATE_END.get(Calendar.MONTH) - offsetMonth);
        DATE_END.set(Calendar.DAY_OF_MONTH, DATE_END.get(Calendar.DAY_OF_MONTH) - offsetDay );
        dateEndString = inputDateFormatter.format( DATE_END.getTime() );
    	dateEndYear = dateEndString.substring(dateEndString.length()-4);
  } catch(IllegalStateException e) {
        // Set the default embargo period to: 1 year
        DATE_END.set( Calendar.YEAR, DATE_END.get(Calendar.YEAR) - 1);
        bean.LOG.error("Embargo offset parameter isn't in a valid format");
        dateEndString = inputDateFormatter.format( DATE_END.getTime() );
    	dateEndYear = dateEndString.substring(dateEndString.length()-4);
  } catch(NullPointerException e) {
        // Set the default embargo period to: 1 year
        DATE_END.set( Calendar.YEAR, DATE_END.get(Calendar.YEAR) - 1);
        dateEndString = inputDateFormatter.format( DATE_END.getTime() );
    	dateEndYear = dateEndString.substring(dateEndString.length()-4);
        bean.LOG.error("Embargo offset parameter isn't present");
  }
%>



<%---------------------- Start of HTML ---------------------------%>

<%-- TODO: define XML lang --%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="pt-PT" lang="pt-PT">
<head>
	<title><fmt:message key='home.meta.title'/></title>
    
    <meta name="viewport" content="width=device-width, initial-scale=1, minimum-scale=1, maximum-scale=1">
	<meta http-equiv="Content-Type" content="application/xhtml+xml; charset=UTF-8" />
	<%-- TODO: define META lang --%>
	<meta http-equiv="Content-Language" content="pt-PT" />
	<meta name="Keywords" content="<fmt:message key='home.meta.keywords'/>" />
	<meta name="Description" content="<fmt:message key='home.meta.description'/>" />

    <meta property="og:title" content="<fmt:message key='home.meta.title'/>"/>
    <meta property="og:description" content="<fmt:message key='home.meta.description'/>"/>
    <% String arquivoHostName = nutchConf.get("wax.webhost", "arquivo.pt"); %>
    <meta property="og:image" content="//<%=arquivoHostName%>/img/logoFace.png"/>
    <meta name="theme-color" content="#252525">
    <!-- Windows Phone -->
    <meta name="msapplication-navbutton-color" content="#252525">
    <!-- iOS Safari -->
    <meta name="apple-mobile-web-app-status-bar-style" content="#252525">      

	<link rel="shortcut icon" href="img/logo-16.png" type="image/x-icon" />
	<link rel="search" type="application/opensearchdescription+xml" title="<fmt:message key='opensearch.title'><fmt:param value='<%=language%>'/></fmt:message>" href="opensearch.jsp?l=<%=language%>" />
	<link rel="stylesheet" title="Estilo principal" type="text/css" href="css/newStyle.css"  media="all" />
    <!-- font awesome -->
    <link rel="stylesheet" href="css/font-awesome.min.css">
    
    <!-- bootstrap -->
    <link rel="stylesheet" href="/css/bootstrap.min.css">
    <script src="/js/jquery-latest.min.js"></script>
    <script src="/js/bootstrap.min.js"></script>
    <script type="text/javascript" src="/js/js.cookie.js"></script>
    <!-- dual slider dependencies -->
    <script type="text/javascript" src="/js/nouislider.min.js"></script>
    <link rel="stylesheet" href="/css/nouislider.min.css">
    <script type="text/javascript" src="/js/wNumb.js"></script>
    <!-- end slider dependencies -->

    <!-- left menu dependencies -->
    <link rel="stylesheet" href="css/leftmenu.css">
    <!-- end left menu dependencies -->
	<script type="text/javascript" src="//s7.addthis.com/js/300/addthis_widget.js#pubid=ra-5645cdb2e22ca317"></script> 
	<!-- end addthis for sharing on social media --> 


</head>
<body>
  <%@ include file="include/topbar.jsp" %>
  <script type="text/javascript">
    pagesHref = window.location.href;
    imagesHref = "/images.jsp?l=<%=language%>"  /*TODO remove from this href parameters that are only appliable to text search*/
  </script>  
  <%@ include file="include/homepageHeader.jsp" %>
  <script type="text/javascript">$('#pagesTab').addClass('selected');$('#pagesTab').addClass('primary-underline');</script>

  <div class="text-center prizes">
	<a class="home-anchor" href="<fmt:message key='home.prizes.url'/>" alt="<fmt:message key='home.prizes.alt'/>" title="<fmt:message key='home.prizes.title'/>"  target="_blank">
		<i class="fa fa-trophy home-icon" aria-hidden="true"></i>
		<h3 class="top-home-label"><fmt:message key='home.prizes.title'/></h3>
	</a>	
  </div>
  <div class="text-center prizes">
  	<a class="home-anchor" href="<fmt:message key='home.video'/>" target="_blank">
		<i class="fa fa-youtube home-icon" aria-hidden="true"></i>
		<h3 class="top-home-label"><fmt:message key='home.intro'/></h3>
	</a>		
  </div>
  <div class="text-center prizes">
  	<a class="home-anchor" href="<fmt:message key='home.pages.href'/>" target="_blank">
		<i class="fa fa-files-o home-icon" aria-hidden="true"></i>
		<h3 class="top-home-label"><fmt:message key='home.pages'/></h3>
	</a>	
  </div>  
  <div class="text-center prizes">
  	<a class="home-anchor" href="<fmt:message key='home.exhibitions.href'/>" target="_blank">
		<i class="fa fa-th-large home-icon" aria-hidden="true"></i>
		<h3 class="top-home-exhibitions-label"><fmt:message key='home.exhibitions'/></h3>
	</a>	
  </div>
  <div class="text-center prizes">
  	<a class="home-anchor" href="<fmt:message key='home.testimonials.href'/>" target="_blank">
		<i class="fa fa-users home-icon" aria-hidden="true"></i>
		<h3 class="top-home-label last-label"><fmt:message key='home.testimonials'/></h3>
	</a>	
  </div>

  <%@include file="include/analytics.jsp" %>
  <%@include file="include/footer.jsp" %>
</body>
</html>

<%@include file="include/logging.jsp" %>