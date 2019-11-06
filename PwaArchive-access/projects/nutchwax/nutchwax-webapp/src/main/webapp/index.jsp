
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
    String dateStartDay = dateStartString.substring(0,2);

    String dateStartMonth = dateStartString.substring(3,5);

    String dateStartYear = dateStartString.substring(dateStartString.length()-4);

    String dateStartStringIonic =  dateStartYear + "-" + dateStartMonth + "-" + dateStartDay;
    
    String dateEndString = inputDateFormatter.format( dateEnd.getTime() );

    String dateEndDay = dateEndString.substring(0,2);

    String dateEndMonth = dateEndString.substring(3,5);

    String dateEndYear = dateEndString.substring(dateEndString.length()-4);

    String dateEndStringIonic =  dateEndYear + "-" + dateEndMonth + "-" + dateEndDay;

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
    
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <!-- <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1 viewport-fit=cover">-->
  <meta http-equiv="Content-Type" content="application/xhtml+xml; charset=UTF-8" />
	<%-- TODO: define META lang --%>
	<meta http-equiv="Content-Language" content="pt-PT" />
	<meta name="Keywords" content="<fmt:message key='home.meta.keywords'/>" />
	<meta name="Description" content="<fmt:message key='home.meta.description'/>" />

  <meta property="og:title" content="<fmt:message key='home.meta.title'/>"/>
  <meta property="og:description" content="<fmt:message key='home.meta.description'/>"/>
    <% String arquivoHostName = nutchConf.get("wax.webhost", "arquivo.pt"); %>
  <meta property="og:image" content="//<%=arquivoHostName%>/img/logoFace.png"/>
  <meta name="theme-color" content="#1a73ba" />
  <!-- Windows Phone -->
  <meta name="msapplication-navbutton-color" content="#1a73ba" />
  <!-- iOS Safari -->   
  <meta name="apple-mobile-web-app-capable" content="yes" />
  <meta name="apple-mobile-web-app-status-bar-style" content="#1a73ba" />  


  <script type="text/javascript">
    var minDate = new Date(<%=DATE_START.getTimeInMillis()%>);
    var maxDate = new Date(<%=DATE_END.getTimeInMillis()%>);
    var minYear = minDate.getFullYear();
    var maxYear = maxDate.getFullYear();
  </script>     

	<link href="https://fonts.googleapis.com/css?family=Roboto&amp;display&equals;swap" rel="stylesheet" />
  <link href="https://fonts.googleapis.com/css?family=Open+Sans&display&equals;swap" rel="stylesheet" />
  <link rel="shortcut icon" href="img/logo-16.png" type="image/x-icon" />
	<link rel="stylesheet" title="Estilo principal" type="text/css" href="css/newStyle.css?build=<c:out value='${initParam.buildTimeStamp}'/>"  media="all" />
    <!-- font awesome -->
  <link rel="stylesheet" href="css/font-awesome.min.css" />

  <!-- Google fonts -->
  <link href="https://fonts.googleapis.com/css?family=Roboto&display=swap" rel="stylesheet" />
    
  <!-- bootstrap -->
  <link rel="stylesheet" href="/css/bootstrap.min.css" />
  <script type="text/javascript" src="/js/jquery-latest.min.js"></script>
  <script type="text/javascript" src="/js/bootstrap.min.js"></script>
  <script type="text/javascript" src="/js/js.cookie.js"></script>
  <script type="text/javascript" src="/js/swiper.min.js"></script>
  

  <script type="text/javascript" src="/js/wNumb.js"></script>
  <!-- end slider dependencies -->

  <!-- left menu dependencies -->
  <link rel="stylesheet" href="css/leftmenu.css" />
    <!-- end left menu dependencies -->

  <!--Includes mobiscroll (calendars for setting day month and year)-->
  <link href="css/mobiscroll.custom-2.6.2.min.css" rel="stylesheet" type="text/css" />
  <script src="js/mobiscroll.custom-2.6.2.min.js" type="text/javascript"></script>
    
	<script type="text/javascript" src="//s7.addthis.com/js/300/addthis_widget.js#pubid=ra-5645cdb2e22ca317"></script> 
	<!-- end addthis for sharing on social media --> 

  <!-- starts New style to override less styles -->
  <script type="text/javascript">
  	$('input,textarea').focus(function(){
       $(this).removeAttr('placeholder');
    })
  </script>

 <!-- starts closing Welcome blue div on homepage -->
 <script type="text/javascript">
 var language =  localStorage.language;
 advancedHref = "advanced.jsp?l=<%=language%>";

 $(document).ready(function(){
  $("#closeMessage").click(function(){
    $('#welcomeMessage').hide();
    localStorage.setItem('welcomeMessage', 'false')
  });

  $('.pageLink, .imageLink ').click(function() {
    
    var pageClass = $(this).hasClass("pageLink");
    var imageClass = $(this).hasClass("imageLink");
    
    var pagesHref = (imageClass === true ? "/images.jsp?l=<%=language%>" : "/search.jsp?l=<%=language%>");

    var query = $('#txtSearch').val();
    var dateStart = $('#dateStart_top').val();
    var dateEnd = $('#dateEnd_top').val();
    
    var newUrl = addParameters(query, dateStart, dateEnd, pagesHref);

    if(newUrl)
      window.location.href = newUrl;

  }); //end PageButton click 

  });  //end document ready

function addParameters(query, dateStart, dateEnd, pageToLink) {
    var oldUrl =  location.protocol + '//' + location.host;
    if( oldUrl.substr(oldUrl.length - 1) === '/') {
      oldUrl = oldUrl.substr(0, oldUrl.length - 1);
    }
    
    var newUrl = updateQueryStringParameter(oldUrl + pageToLink, "query", encodeURI(query));
    newUrl = updateQueryStringParameter(newUrl, "dateStart", encodeURIComponent(dateStart));
    newUrl = updateQueryStringParameter(newUrl, "dateEnd", encodeURIComponent(dateEnd));
    
    return newUrl;
}


function updateQueryStringParameter(uri, key, value) {
  var re = new RegExp("([?&])" + key + "=.*?(&|$)", "i");
  var separator = uri.indexOf('?') !== -1 ? "&" : "?";
  if (uri.match(re)) {
    return uri.replace(re, '$1' + key + "=" + value + '$2');
  }
  else {
    return uri + separator + key + "=" + value;
  }
}

 </script>
 <!-- ends closing Welcome blue div on homepage -->
<link href="css/csspin.css" rel="stylesheet" type="text/css">
  
  <script type="text/javascript" src="//s7.addthis.com/js/300/addthis_widget.js#pubid=ra-5645cdb2e22ca317"></script> 

  <script type="text/javascript" src="js/configs.js"></script>
  <script type="text/javascript" src="/js/js.cookie.js"></script>

  <script type="module" src="@ionic/core/dist/ionic/ionic.esm.js"></script>
  <script type="text/javascript" nomodule="" src="@ionic/core/dist/ionic/ionic.js"></script>

  <!--<script src="@ionic/core/dist/ionic.js"></script>--> 

  <link rel="stylesheet" href="@ionic/core/css/ionic.bundle.css" />
  <!-- ends New style to override less styles -->

  <!-- starts sticky search bar styles -->
  <link rel="stylesheet" href="/css/scroll-fixed-content.css" />
  <!-- ends sticky search bar styles -->

</head>
<body id="homepage-landing">
  <%@ include file="include/topbar.jsp" %>
  
  <div class="container-fluid topcontainer" id="headerSearchDiv">
    <%@ include file="include/homepageHeaderMobile.jsp" %>
    <script type="text/javascript">$('#pagesTab').addClass('selected');$('#pagesTab').addClass('primary-underline');</script>
  </div>

  <%@ include file="include/intro.jsp" %>

  <%@include file="include/analytics.jsp" %>
  <%@include file="include/footer.jsp" %>
</body>
</html>

<%@include file="include/logging.jsp" %>
