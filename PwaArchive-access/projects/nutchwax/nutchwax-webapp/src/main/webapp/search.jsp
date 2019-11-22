
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd" >
<%@page import="java.net.URL"%>
<%@ page
  session="true"
  contentType="text/html; charset=UTF-8"
  pageEncoding="UTF-8"  

  import="java.io.File"
  import="java.io.IOException"
  import= "java.net.*"
  import= "java.io.*"
  import="java.net.URLEncoder"
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
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ include file="include/logging_params.jsp" %>
<%@ include file="include/i18n.jsp" %>
<fmt:setLocale value="<%=language%>"/>

<%! //To please the compiler since logging need those -- check [searchBootstrap.jsp]
  private static Calendar DATE_START = new GregorianCalendar(1996, 1-1, 1);
  private static final DateFormat FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");
  //TODO: remove dateStart & dateEnd ???
  //private static Calendar dateStart = new GregorianCalendar();
  //private static Calendar dateEnd = new GregorianCalendar();
  private static final DateFormat OFFSET_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
  private static final Pattern OFFSET_PARAMETER = Pattern.compile("(\\d{4})-(\\d{2})-(\\d{2})");

  private static final String COLLECTION_KEY = "collection";
  private static final String COLLECTION_QUERY_PARAM_KEY = COLLECTION_KEY + ":";
  private static final Pattern URL_PATTERN = Pattern.compile("^.*? ?((https?:\\/\\/)?([a-zA-Z\\d][-\\w\\.]+)\\.([a-zA-Z\\.]{2,6})([-\\/\\w\\p{L}\\.~,;:%&=?+$#*]*)*\\/?) ?.*$");
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
  Calendar DATE_END = new GregorianCalendar();
  DATE_END.set( Calendar.YEAR, DATE_END.get(Calendar.YEAR) );
  DATE_END.set( Calendar.MONTH, 12-1 );
  DATE_END.set( Calendar.DAY_OF_MONTH, 31 );
  DATE_END.set( Calendar.HOUR_OF_DAY, 23 );
  DATE_END.set( Calendar.MINUTE, 59 );
  DATE_END.set( Calendar.SECOND, 59 );
  int queryStringParameter= 0;

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
  } catch(IllegalStateException e) {
        // Set the default embargo period to: 1 year
        DATE_END.set( Calendar.YEAR, DATE_END.get(Calendar.YEAR) - 1);
        bean.LOG.error("Embargo offset parameter isn't in a valid format");
  } catch(NullPointerException e) {
        // Set the default embargo period to: 1 year
        DATE_END.set( Calendar.YEAR, DATE_END.get(Calendar.YEAR) - 1);
        bean.LOG.error("Embargo offset parameter isn't present");
  }
%>
<%-- Handle the url parameters --%>
<%
  // get query from request
  
  String queryString = request.getParameter("query");
String[] queryString_splitted=null;

  
  if ( queryString != null ) {
        queryString = queryString.trim();
        
       
  } else {
        // Check if the 'query' params exists
        // else check if the advanced params exist and process them
        queryString = "";
        if ( request.getParameter("adv_and") != null && request.getParameter("adv_and") != "") {
                queryString += request.getParameter("adv_and");
                queryString += " ";
        }
        if ( request.getParameter("adv_phr") != null && request.getParameter("adv_phr") != "") {
                queryString += "\""+ request.getParameter("adv_phr").replaceAll("\"", "") +"\"";
                queryString += " ";
        }
        if ( request.getParameter("adv_not") != null && request.getParameter("adv_not") != "") {
                String notStr = request.getParameter("adv_not");
                if (!notStr.startsWith("-"))
                        notStr = "-" + notStr;
                notStr = notStr.replaceAll("[ ]+", " -") +" ";
                queryString += notStr;
        }
        if ( request.getParameter("adv_mime") != null && request.getParameter("adv_mime") != "" ) {
                queryString += "filetype:"+ request.getParameter("adv_mime");
                queryString += " ";
        }
        if (request.getParameter("site") != null && request.getParameter("site") != "") {
                queryString += "site:";
                String siteParameter = request.getParameter("site"); //here split hostname and put it to lowercase

                if (siteParameter.startsWith("http://")) {
                        URL siteURL = new URL(siteParameter);
                        String siteHost = siteURL.getHost();
                        siteParameter = siteParameter.replace(siteHost, siteHost.toLowerCase()); // hostname to lowercase
                        queryString += siteParameter.substring("http://".length());
                } else if (siteParameter.startsWith("https://")) {
                        URL siteURL = new URL(siteParameter);
                        String siteHost = siteURL.getHost();
                        siteParameter = siteParameter.replace(siteHost, siteHost.toLowerCase()); // hostname to lowercase
                        queryString += siteParameter.substring("https://".length());
                } else {
                        URL siteURL = new URL("http://"+siteParameter);
                        String siteHost = siteURL.getHost();
                        siteParameter = siteParameter.replace(siteHost, siteHost.toLowerCase()); // hostname to lowercase
                        queryString += siteParameter;
                }
                /*queryStringParameter = queryString.length();
                if (siteParameter.startsWith("http://") && siteParameter.startsWith("https://")) {
                  queryString +=NutchwaxQuery.encodeExacturl("exacturlexpand:"+siteParameter);
                } else {
                   queryString +=NutchwaxQuery.encodeExacturl("exacturlexpand:http://"+siteParameter);
                       // queryString += "exacturlexpand:http://"+siteParameter;
                }
                String aux = request.getParameter("site");
                bean.LOG.debug("\nQueryString : "+ queryString+"\n*****************************\n");
                String aux_ ="exacturlexpand:http://"+aux;
                aux = NutchwaxQuery.encodeExacturl(aux_);*/
                
                bean.LOG.debug("\nQueryString exactExpand URL: "+ siteParameter+"\n*****************************\n");
                queryString += " ";
        }
        if (request.getParameter("format") != null && request.getParameter("format") != "" && !request.getParameter("format").equals("all")) {
                queryString += "type:" + request.getParameter("format");
                queryString += " ";
        }
  }

  /*****************    'hitsPerDup' param      ***************************/
  int hitsPerDup = 2;
  String hitsPerDupString = request.getParameter("hitsPerDup");
  if (hitsPerDupString != null && hitsPerDupString.length() > 0) {
    hitsPerDup = Integer.parseInt(hitsPerDupString);
  } else {
    // If 'hitsPerSite' present, use that value.
    String hitsPerSiteString = request.getParameter("hitsPerSite");
    if (hitsPerSiteString != null && hitsPerSiteString.length() > 0) {
      hitsPerDup = Integer.parseInt(hitsPerSiteString);
    }
  }

  /*****************    'sort' param    ***************************/
  String sort = null;
  boolean reverse = false;


  if (!queryString.contains("sort:")) {
        sort = request.getParameter("sort");

        if ("relevance".equals(sort)) {
                sort = null;
        } else if ("new".equals(sort)) {
                sort = "date";
                reverse = true;
                queryString += "sort:new";
                hitsPerDup = 0;
        } else if ("old".equals(sort)) {
                sort = "date";
                queryString += "sort:old";
                hitsPerDup = 0;
        } else {
                sort = null;
        }
  } else if (queryString.contains("sort:new")) {
        sort = "date";
        reverse = true;
        hitsPerDup = 0;
  } else if (queryString.contains("sort:old")) {
        sort = "date";
        hitsPerDup = 0;
  }

  // De-Duplicate handling.  Look for duplicates field and for how many
  // duplicates per results to return. Default duplicates field is 'site'
  // and duplicates per results default is '1' (Used to be '2' but now
  // '1' so can have an index with dups not show dups when used doing
  // straight searches).
  String dedupField = request.getParameter("dedupField");
  if (dedupField == null || dedupField.length() == 0) {
    dedupField = "site";
  }

  int hitsPerVersion = 1;
  String hitsPerVersionString = request.getParameter("hitsPerVersion");
  if (hitsPerVersionString != null && hitsPerVersionString.length() > 0) {
        hitsPerVersion = Integer.parseInt(hitsPerVersionString);
  }

  if (queryString.contains("site:")) {
        hitsPerDup = 0;

        queryString = queryString.replaceAll("site:http://", "site:");
        queryString = queryString.replaceAll("site:https://", "site:");
  }

  /***************** Save the query string for further use ***********/
  request.setAttribute("query", queryString.trim());

  /***************** Clean the query for Backend search *************/
  if (queryString.contains("sort:new")) {
        queryString = queryString.replace("sort:new","");
  } else if (queryString.contains("sort:old")) {
        queryString = queryString.replace("sort:old","");
  }

  /*** Start date ***/
  Calendar dateStart = (Calendar)DATE_START.clone();
  SimpleDateFormat inputDateFormatter = new SimpleDateFormat("dd/MM/yyyy");

  if ( request.getParameter("dateStart") != null && !request.getParameter("dateStart").equals("") ) {
        try {
                dateStart.setTime( inputDateFormatter.parse(request.getParameter("dateStart")) );
        } catch (NullPointerException e) {
                bean.LOG.debug("Invalid Start Date:"+ request.getParameter("dateStart") +"|");
        }
  }

  /*** End date ***/
  Calendar dateEnd = (Calendar)DATE_END.clone();  

  String dateEndNoParameter = inputDateFormatter.format( dateEnd.getTime() );
  String yearEndNoParameter =dateEndNoParameter.substring(dateEndNoParameter.length()-4);
  String yearStartNoParameter = "1996";

  // Setting current date

  if ( request.getParameter("dateEnd") != null && !request.getParameter("dateEnd").equals("") ) {
        try {
                dateEnd.setTime( inputDateFormatter.parse(request.getParameter("dateEnd")) );
                // be sure to set the end date to the very last second of that day.
                dateEnd.set( Calendar.HOUR_OF_DAY, 23 );
                dateEnd.set( Calendar.MINUTE, 59 );
                dateEnd.set( Calendar.SECOND, 59 );
        } catch (NullPointerException e) {
                bean.LOG.debug("Invalid End Date:"+ request.getParameter("dateEnd") +"|");
        }
  }

  /*** Switch dates if start GT end ***/
    if(dateStart.getTime().compareTo(dateEnd.getTime())>0){
      Calendar auxCal = dateStart;
      dateStart = dateEnd;
      dateEnd = auxCal; 
    }
  /**/

  /*** Add dates to nutch query ***/
  if (queryString != null && queryString != "") {
        queryString += " date:"+ FORMAT.format( dateStart.getTime() );
        queryString += "-";
        queryString += FORMAT.format( dateEnd.getTime() );
  } else {
        queryString = "";
  }

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

  //--- not needed, since we use fields. String htmlQueryString = Entities.encode(queryString);

  /*****************    Offset param    ***************************/
  int start = 0;          // first hit to display
  String startString = request.getParameter("start");
  if (startString != null)
    start = Integer.parseInt(startString);

  /*****************    Hits/page param ***************************/
  int hitsPerPage = 10;          // number of hits to display
  String hitsString = request.getParameter("hitsPerPage");
  if (hitsString != null) {
        try {
                hitsPerPage = Integer.parseInt(hitsString);
        } catch (NumberFormatException e) {
                bean.LOG.debug("WRONG VALUE of hitsPerPage:"+ hitsString +"|");
        }
  }

  // If a 'collection' parameter present, always add to query.
  String collection = request.getParameter(COLLECTION_KEY);
  if (collection != null && queryString != null && queryString.length() > 0) {
      int collectionIndex = queryString.indexOf(COLLECTION_QUERY_PARAM_KEY);
      if (collectionIndex < 0) {
        queryString = queryString + " " + COLLECTION_QUERY_PARAM_KEY +
            collection;
      }
  }
  
  // Prepare the query values to be presented on the page, preserving the session
  String htmlQueryString = "";

  if ( request.getAttribute("query") != null ) {
        htmlQueryString = request.getAttribute("query").toString();
        request.setAttribute("htmlQueryString", htmlQueryString);
  }

  // Make up query string for use later drawing the 'rss' logo.
  String params = "&hitsPerPage=" + hitsPerPage +
    (sort == null ? "" : "&sort=" + sort + (reverse? "&reverse=true": "") +
    (dedupField == null ? "" : "&dedupField=" + dedupField));

%>


<%---------------------- Start of HTML ---------------------------%>

<%-- TODO: define XML lang --%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="pt-PT" lang="pt-PT">
<head>
  <title><fmt:message key='home.meta.title'/></title>
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <meta http-equiv="Content-Type" content="application/xhtml+xml; charset=UTF-8" />
  <%-- TODO: define META lang --%>
  <meta http-equiv="Content-Language" content="pt-PT" />
  <meta name="Keywords" content="<fmt:message key='home.meta.keywords'/>" />
  <meta name="Description" content="<fmt:message key='home.meta.description'/>" />

    <meta property="og:title" content="<fmt:message key='home.meta.title'/>"/>
    <meta property="og:description" content="<fmt:message key='home.meta.description'/>"/>
    <% String arquivoHostName = nutchConf.get("wax.webhost", "arquivo.pt"); %>
    <meta property="og:image" content="//<%=arquivoHostName%>/img/logoFace.png"/>
    <meta name="theme-color" content="#1a73ba">
    <!-- Windows Phone -->
    <meta name="msapplication-navbutton-color" content="#1a73ba">
    <!-- iOS Safari -->
    <meta name="apple-mobile-web-app-status-bar-style" content="#1a73ba"> 
    <script type="text/javascript">
      var minDate = new Date(<%=DATE_START.getTimeInMillis()%>);
      var maxDate = new Date(<%=DATE_END.getTimeInMillis()%>);
      var minYear = minDate.getFullYear();
      var maxYear = maxDate.getFullYear();
    </script>     
  <link rel="shortcut icon" href="img/logo-16.png" type="image/x-icon" />
  <link rel="stylesheet" title="Estilo principal" type="text/css" href="css/newStyle.css?build=<c:out value='${initParam.buildTimeStamp}'/>"  media="all" />
    <!-- font awesome -->
    <link rel="stylesheet" href="css/font-awesome.min.css">
    <!-- bootstrap -->
    <link rel="stylesheet" href="/css/bootstrap.min.css">
    <script src="/js/jquery-latest.min.js"></script>
    <script src="/js/bootstrap.min.js"></script>

  <link href="https://fonts.googleapis.com/css?family=Roboto&display=swap" rel="stylesheet">
  <link href="https://fonts.googleapis.com/css?family=Roboto:400,900&display=swap" rel="stylesheet">
  <link href="https://fonts.googleapis.com/css?family=Open+Sans&display=swap" rel="stylesheet">
    <!-- dual slider dependencies -->
    <script type="text/javascript" src="/js/nouislider.min.js"></script>
    <link rel="stylesheet" href="/css/nouislider.min.css">
    <script type="text/javascript" src="/js/wNumb.js"></script>
    <!-- CSS loading spiner -->
  <link href="css/csspin.css" rel="stylesheet" type="text/css">
  <script type="text/javascript" src="//s7.addthis.com/js/300/addthis_widget.js#pubid=ra-5645cdb2e22ca317"></script> 
  <!-- end addthis for sharing on soc
    ial media --> 
  <script type="text/javascript" src="js/configs.js"></script>
  <script type="text/javascript" src="/js/js.cookie.js"></script>
  <!-- swiper main menu --> 
   <script type="text/javascript" src="/js/swiper.min.js"></script>
  <!-- NEW - 23.07.19: Call ionic -->
  <script src="../@ionic/core/dist/ionic.js"></script>
  <link rel="stylesheet" href="../@ionic/core/css/ionic.bundle.css">

  <script src="/js/uglipop.min.js"></script>

</head>

<body id="home-search">
    <%@ include file="include/topbar.jsp" %>
    <div class="container-fluid topcontainer" id="headerSearchDiv">
    <script type="text/javascript">
      var language = localStorage.language;
      pagesHref = window.location.href;
      imagesHref = window.location.href.toString().replace("search.jsp", "images.jsp");  /*TODO remove from this href parameters that are only appliable to text search*/
    </script>
    <script type="text/javascript" src="/js/encodeHTML.js"></script> 
    <%@ include file="include/searchHeaderMobile.jsp" %>

    <script type="text/javascript">
      document.write("<div id='loadingDiv' class='text-center lds-ring' style='text-align: center; margin-top: 10%; margin-bottom: 5%;'><div></div><div></div><div></div><div></div></div>");
      $( document ).ready(function() {
        if(typeof(loading)=="undefined" || loading != true){
          $('#loadingDiv').hide();
          $('#conteudo-resultado').show();
        }
      $("#txtSearch").on('mousedown touchstart', function (e) {
            e.stopPropagation();
       });          
      });
    </script>      
    <script type="text/javascript">$('#pagesTab').addClass('selected');$('#pagesTab').addClass('primary-underline');</script>
    <script type="text/javascript" src="/js/searchHeaderMobile.js"></script><!-- In progress-->

  
        <%----------------------------------------------------------
        // Check to see which of the 3 mode is presented:
        // (1) result list
        // (2) wayback document's grid
        // (3) result list with tip
        ----------------------------------------------------------%>
        <%
        Matcher urlMatch = null;
        String urlQuery = null;
        boolean showList = false;
        String showTip = null;                  // tip to show
        String allVersions = null;
        int end = -1;
        Hits hits = null;
        int hitsLength = 0;
        long hitsTotal = 0;
        boolean hitsTotalIsExact = false;
        Query query = null;
        String queryExactExpand=null;
        String collectionsHost = nutchConf.get("wax.host", "examples.com");
        pageContext.setAttribute("collectionsHost", collectionsHost);

              String hostArquivo = nutchConf.get("wax.webhost", "arquivo.pt");


        if ( request.getAttribute("query") != null && !request.getAttribute("query").toString().equals("") ) {

                if ( (urlMatch = URL_PATTERN.matcher( request.getAttribute("query").toString() )).matches() ) {
                        urlQuery = urlMatch.group(1);
                        String urlQueryParam = urlQuery;
                        int urlLength = urlQuery.length();
          
                        if (!urlQuery.startsWith("http://") && !urlQuery.startsWith("https://") ) {
                                urlQueryParam = "http://" + urlQueryParam;
                        }
                pageContext.setAttribute("urlQueryParam", urlQueryParam);

                allVersions = "search.jsp?query="+ URLEncoder.encode(urlQueryParam, "UTF-8");
                if (!language.equals("pt")) {
                        allVersions += "&l="+ language;
                }
                    /*
                hostname is not case sensitive, thereby it has to be written with lower case
                the bellow provide a solution to this problem
                arquivo.PT will be equal to arquivo.pt
                Converts hostname to small letters
                */

                URL url_queryString=new URL(urlQueryParam);
                String path=url_queryString.getPath();
                String hostname=url_queryString.getHost().toLowerCase();

                String protocol=url_queryString.getProtocol();
                String fileofUrl = url_queryString.getFile();

                boolean validTLD = false;

                for(String tld:tlds){ //
                  if(hostname.endsWith(tld.toLowerCase())){
                    validTLD = true;
                  }
                }



            if ( request.getParameter("query") != null && urlLength == request.getParameter("query").trim().length() && validTLD) {
                                // option: (2)
                                showList = false;
                                usedWayback = true;
                                


                            urlQueryParam= protocol+"://"+hostname+fileofUrl;
                            
                          /*************************************/
                            queryString=urlQueryParam; //Querying wayback servlet
                            urlQuery=urlQueryParam; //Querying pyWB
                            urlQuery = StringEscapeUtils.escapeHtml(urlQuery);
                            request.setAttribute("urlQuery", urlQuery);
                        
                            /*************************************************/
                    pageContext.setAttribute("urlQueryParam", urlQueryParam);
                    allVersions = "search.jsp?query="+ URLEncoder.encode(urlQueryParam, "UTF-8");
              pageContext.setAttribute("dateStartWayback", FORMAT.format( dateStart.getTime() ) );
                        pageContext.setAttribute("dateEndWayback", FORMAT.format( dateEnd.getTime() ) );

                        long startQueryTime = System.currentTimeMillis();               // for logging
%>

      </div> <%-- closes #main --%>
    </div> <%-- closes .wrap --%> 
                        <%-- #search_stats & #result_list for this case are generated by WB --%>
                        <%
                                boolean seeHistory = false;             // This variable is used to indicate that link to see the history was clicked
                                if( request.getParameter("pos") != null) {
                                        seeHistory = true;
                                }
                                pageContext.setAttribute("seeHistory", seeHistory);
                        %>
                        <c:catch var="exception">
                                <% hitsTotal = 1; %>
                        </c:catch>
<script>
      var language =   localStorage.language;
      if( language == 'EN'){
          document.write('<script type="text/javascript" language="JavaScript" src="//<%=hostArquivo%>/js/properties/ConstantsEN.js"><\/script>');
      }
      else{
          document.write('<script type="text/javascript" language="JavaScript" src="//<%=hostArquivo%>/js/properties/ConstantsPT.js"><\/script>');
      }
</script>
<script type="text/javascript">

function getYearTs(ts){
  return ts.substring(0, 4);
}

function getMonthTs(ts){
  return ts.substring(4,6);
}


function getYearPosition(ts){
  return parseInt(getYearTs(ts)) - 1996;
}

function getDateSpaceFormatedWithoutYear(ts){
  var month = ts.substring(4, 6);
  month = Content.months[month];
  var day = ts.substring(6, 8);
  if( day[0] === '0'){
    day = day[1];
  }
  var hours = ts.substring(8,10);
  var minutes = ts.substring(10,12);

  return day + " "+ month + " " + Content.at + " " + hours+":"+minutes;
}

function getDateSpaceFormated(ts){
  var year = ts.substring(0, 4);
  var month = ts.substring(4, 6);
  month = Content.months[month];
  var day = ts.substring(6, 8);
  if( day[0] === '0'){
    day = day[1];
  }
  var hours = ts.substring(8,10);
  var minutes = ts.substring(10,12);

  return day + " "+ month + " " +year+ " " +" " + Content.at + " " + hours+":"+minutes;
}

function getShortDateSpaceFormated(ts){
  var year = ts.substring(0, 4);
  var month = ts.substring(4, 6);
  month = Content.shortMonths[month];
  var day = ts.substring(6, 8);
  if(day.charAt(0) == '0'){
    day = day.charAt(1);
  }  
  return day + " "+ month;
}

function createMatrixTable(versionsArray, versionsURL){
  var today = new Date();
  numberofVersions = yyyy - 1996;
  var yyyy = today.getFullYear();
  var numberofVersions = yyyy - 1996;
  var matrix = new Array(numberofVersions);
  for (var i = 0; i < matrix.length; i++) {
    matrix[i] = [];
    var yearStr = (1996+i).toString();
    // add the headers for each year
    $("#years").append('<th id="th_'+yearStr+'" class="thTV">'+yearStr+'</th>');
  }

  for (var i = 0; i < versionsArray.length; i++) {
    var timestamp = versionsArray[i];
    var timestampStr = timestamp.toString();
    var url = versionsURL[i];
    var pos = getYearPosition(timestampStr);
    var dateFormated = getDateSpaceFormated(timestampStr);
    var shortDateFormated= getShortDateSpaceFormated(timestampStr);       
    var tdtoInsert = '<td class="tdTV"><a href="//<%=collectionsHost%>/'+timestampStr+'/'+url+'" title="'+dateFormated+'">'+shortDateFormated+'</a></td>';
    matrix[pos].push(tdtoInsert);  
  }

  //find which is the biggest number of versions per year and create empty tds in the other years
  var maxLength = 0;
  var lengthi =0;
  for (var i = 0; i < matrix.length; i++) {
    lengthi = matrix[i].length;
    var yearStr = (1996+i).toString();
    if(lengthi == 0){
      $("#th_"+yearStr).addClass("inactivo");
    }

    if(lengthi > maxLength){
      maxLength = lengthi;
    }
  }
  //iterate again to create empty tds
  for (var i = 0; i < matrix.length; i++) {
    lengthi = matrix[i].length;
    if(maxLength > lengthi){
      for(var j=0; j<(maxLength - lengthi); j++){
        matrix[i].push('<td class="tdTV">&nbsp;</td>');
      }
    }
  }
  //create each row of the table
  for (var i=0; i<maxLength; i++){
    rowString ="";
    for (var j = 0; j < matrix.length; j++) {
      rowString+= matrix[j][i];
    }
    var rowId = (i+1).toString()
    $("#tableBody").append('<tr class="trTV" id="'+rowId+'">'+rowString+'<tr>');
  }
  
  //if($('#1 td:nth-child('+String(matrix.length)+')').html() ==='&nbsp;'){ /*If last year in the table doesn't have versions show embargo message*/
  //  $('#1 td:nth-child('+String(matrix.length)+')').attr('rowspan', '999');
  //  $('#1 td:nth-child('+String(matrix.length)+')').attr('class', 'td-embargo')
  //  $('#1 td:nth-child('+String(matrix.length)+')').html('<a href="'+Content.embargoUrl+'">'+Content.embargo+'</a>');
  //}
}
function resizeResultsPageHeight(){
      $('#resultados-lista').css('height', ($(window).height() - $('#resultados-lista').offset().top)*0.95 );
}

function createResultsTable(numberOfVersions, inputURL){
    scrollLeftPosition = 0; 
    /*where the scroll should start in left of table*/
    scrollOffset = 200; /*distance in px of each scroll*/

    $('<div id="resultados-url"></div>'+
      '<div id="layoutTV">'+
        '<h4 class="leftArrow"><button onclick="scrollTableLeft()" class="clean-button-no-fill"><i class="fa fa-caret-left" aria-hidden="true"></i></ion-icon></button></h4>'+      
        '<h4 class="text-bold"><i class="fa fa-table"></i> <fmt:message key='table'/> </h4>'+
        '<button class="clean-button-no-fill anchor-color faded" onclick="localStorage.setItem(\'isList\', \'true\');window.location.reload()"><h4><i class="fa fa-list"></i> <fmt:message key='list'/></h4></button>'+
        '<h4 class="rightArrow"><button onclick="scrollTableRight()" class="clean-button-no-fill"><i class="fa fa-caret-right" aria-hidden="true"></i></ion-icon></button></h4>'+
      '</div>'+
      '<div class="wrap">' +
             '  <div id="intro">' +
             '    <h4 class="texto-1" style="text-align: center;padding-bottom: 15px;">'+ formatNumberOfVersions(numberOfVersions.toString()) +' '+ 
               (numberOfVersions===1 ?  Content.versionPage : Content.versionsPage )+
               ' '+ inputURL+
                '</h4>' +
             '  </div>' +
             '</div>' + 
       '<div id="conteudo-versoes" class="swiper-no-swiping">'+
             '  <div id="resultados-lista" class="swiper-no-swiping" style="overflow: auto; min-height: 200px!important;">'+             
             '    <table id="resultsTable" class="tabela-principal swiper-no-swiping">'+
             '      <tbody id="tableBody" class="swiper-no-swiping">'+
                    '<tr id="years" class="swiper-no-swiping trTV"></tr>'+
             '      </tbody>'+
             '    </table>'+
             '  </div>'+
             '</div>'        ).insertAfter("#headerSearchDiv");

    $( document ).ready(function() {
      resizeResultsPageHeight();
      $("table").on('mousedown touchstart', function (e) {
            e.stopPropagation();
       });            

    });
    
    window.onresize = resizeResultsPageHeight;

}

function scrollTableLeft(){

   scrollLeftPosition -= scrollOffset;
   if(scrollLeftPosition <= 0) {scrollLeftPosition = 0;}
   $('#resultados-lista').animate({scrollLeft: scrollLeftPosition}, 800);
  
}
function scrollTableRight(){
   scrollLeftPosition += scrollOffset;
   /*Verify if scrollOffset+scrollLeftPosition is bigger than width of table*/
   if(scrollOffset+scrollLeftPosition >  $('#resultsTable').width() ){
     /*Maximum scroll right*/
     scrollLeftPosition = $('#resultsTable').width() - scrollOffset;
   }
  
   $('#resultados-lista').animate({scrollLeft: scrollLeftPosition}, 800);
  
}


function createMatrixList(versionsArray, versionsURL){
  var today = new Date();
  numberofVersions = yyyy - 1996;
  var yyyy = today.getFullYear();
  var numberofVersions = yyyy - 1996;
  var matrix = new Array(numberofVersions);
  for (var i = 0; i < matrix.length; i++) {
    matrix[i] = [];
    var yearStr = (1996+i).toString();
    // add the headers for each year
    $("#years").append('<div class="yearUl row" id="th_'+yearStr+'"><div class="col-xs-6 text-left yearText"><h4>'+yearStr+'</h4></div></div>');
  }

  for (var i = 0; i < versionsArray.length; i++) {
    var timestamp = versionsArray[i];
    var timestampStr = timestamp.toString();
    var currentYear = getYearTs(timestampStr);
    var currentMonth = getMonthTs(timestampStr);
    var currentMonthVersions = 0;
    var url = versionsURL[i];

    var dateFormated = getDateSpaceFormated(timestampStr);

    var tdtoInsert = '<a onclick="ga(\'send\', \'event\', \'Versions List\', \'Version Click\', \'//<%=collectionsHost%>/'+timestampStr+'/'+url+'\');" class="day-version-div text-center" id="'+timestampStr+'" href="//<%=collectionsHost%>/'+timestampStr+'/'+url+'" title="'+dateFormated+'">'+getDateSpaceFormatedWithoutYear(timestampStr)+'</a>';

     if(! $('#'+currentYear+'_'+currentMonth).length )  /*Add month if it doesn't exist already*/
    {
         $("#th_"+currentYear.toString()).append('<div class="month-version-div row" id="'+currentYear+'_'+currentMonth+'"><h4 class="month-left month-margins col-xs-6 text-left">'+Content.months[currentMonth]+'</h4><h4 class="month-margins col-xs-6 text-right month-right" ><span id="month_'+currentYear+'_'+currentMonth+'">1 <fmt:message key='search.version'/></span> <i class="fa fa-caret-down iCarret monthCarret" aria-hidden="true"></i></h4></div>');
         currentMonthVersions = 1;
    }
    $("#"+currentYear+'_'+currentMonth).append(tdtoInsert);

    if(currentMonthVersions === 0 ){
      currentMonthVersions = $('#'+currentYear+'_'+currentMonth + '> a').length;
      $('#month_'+currentYear+'_'+currentMonth).html(currentMonthVersions+ " <fmt:message key='search.versions'/>");
     
    }


  }

  //find which is the biggest number of versions per year and create empty tds in the other years
  var lengthi =0;
  for (var i = 0; i < matrix.length; i++) {
    lengthi = matrix[i].length;
    var yearStr = (1996+i).toString();
    var numberOfVersionsCurrentYear = $("#th_"+yearStr+" .day-version-div").length;
    if(numberOfVersionsCurrentYear > 1){
      $("#th_"+yearStr+" div:first-child").after('<div class="col-xs-6 numberVersions no-padding-left text-right"><h4>'+numberOfVersionsCurrentYear.toString() + ' <fmt:message key='search.versions'/>    <i class="fa fa-caret-down iCarret yearCarret" aria-hidden="true"></i></h4></div>');
    }else if(numberOfVersionsCurrentYear === 1 ){
      $("#th_"+yearStr+" div:first-child").after('<div class="col-xs-6 numberVersions no-padding-left text-right"><h4>'+numberOfVersionsCurrentYear.toString() + ' <fmt:message key='search.version'/>    <i class="fa fa-caret-down iCarret yearCarret" aria-hidden="true"></i></h4></div>');
    }else{
      /*Year with no versions maybe delete if we don't want to present empty years?*/
      $("#th_"+yearStr+" div:first-child").after('<div class="numberVersions no-padding-left text-right"><h4>'+numberOfVersionsCurrentYear.toString() + ' <fmt:message key='search.versions'/>    <i class="fa fa-caret-down iCarretDisabled yearCarret" aria-hidden="true"></i></h4></div>'); 
       $("#th_"+yearStr).addClass("noVersions");     
    }
  }

}



function createResultsList(numberOfVersions, inputURL){

    $('<div id="resultados-url">'+Content.resultsQuestion+' \'<a href="searchMobile.jsp?query=%22'+inputURL+'%22">'+inputURL+'</a>\'</div>'+
      '<div id="layoutTV">'+
        '<button class="clean-button-no-fill anchor-color faded" onclick="localStorage.setItem(\'isList\', \'false\');window.location.reload();"><h4><i class="fa fa-table"></i> <fmt:message key='table'/> </h4></button>'+
        '<h4 class="text-bold"><i class="fa fa-list"></i> <fmt:message key='list'/></h4>'+
      '</div>'+
          '<div class="wrap">' +
             '<div id="intro">' +
               '<h4 class="texto-1" style="text-align: center;padding-bottom: 15px;">'+ formatNumberOfVersions(numberOfVersions.toString()) +' '+ 
                   (numberOfVersions===1 ?  Content.versionPage : Content.versionsPage )+
                   ' '+ inputURL+
               '</h4>' +
             '</div>' +
          '</div>' + 
          '<div id="years" class="container-fluid col-sm-offset-1 col-sm-10 col-md-offset-2 col-md-8 col-lg-offset-3 col-lg-6 col-xl-offset-4 col-xl-4 ">' +
          '</div>' +
        '</div>' +
      '</div>').insertAfter("#headerSearchDiv");
}

function isList(){ 
  if( $(window).width() < 1024 ){
    return true /*show horizontal list of versions for small screens*/
  }
};

function formatNumberOfVersions( numberofVersionsString){
  formatedNumberOfVersionsString = '';
  for (var i = 0, len = numberofVersionsString.length; i < len; i++) {
    if( (len-i)%3 === 0 ){
      formatedNumberOfVersionsString+= ' ';
    }
    formatedNumberOfVersionsString+= numberofVersionsString[i];
  }
  return formatedNumberOfVersionsString;
}

function createErrorPage(){
  $('<div id="conteudo-resultado-url" class="container-fluid col-sm-offset-1 col-sm-10 col-md-offset-2 col-md-8 col-lg-offset-3 col-lg-6 col-xl-offset-4 col-xl-4">'+
           '  <div id="first-column">&nbsp;</div>'+
           '  <div id="second-column">'+
           '    <div id="search_stats"></div>'+
           '    <div id="conteudo-pesquisa-erro">'+
                '<div class="alert alert-danger col-xs-12 my-alert break-word"><p>'+Content.noResultsFound+' <span class="text-bold"><%=urlQuery%></span></p></div>'+
                '<div id="sugerimos-que" class="col-xs-12 no-padding-left suggestions-no-results">'+
                    '<p class="text-bold">'+Content.suggestions+'</p>'+
                  '<ul>'+
                    '<li>'+Content.checkSpelling+'</li>'+
                    '<li><a class="no-padding-left" href="'+Content.suggestUrl+'<%=urlQuery%>">'+Content.suggest+'</a> '+Content.suggestSiteArchived+'</li>'+                    
                    '<li><a class="no-padding-left" href="http://timetravel.mementoweb.org/list/1996/<%=urlQuery%>">'+Content.mementoFind+'</a>.</li>'+                    
                  '</ul>'+
                '</div>'+
                '</div>'+
              '</div>'+
           '</div>').insertAfter("#headerSearchDiv"); 
}


    var urlsource = "<%=urlQuery%>" ;
    var startDate = "<%=dateStartString%>";
    var startYear = startDate.substring(6,10)
    var startMonth = startDate.substring(3,5);
    var startDay = startDate.substring(0,2);
    var startTs = startYear+startMonth+startDay+'000000';

    var endDate = "<%=dateEndString%>";
    var endYear = endDate.substring(6,10)
    var endMonth = endDate.substring(3,5);
    var endDay = endDate.substring(0,2);
    var endTs = endYear+endMonth+endDay+'000000';   

    //var requestURL = "http://p27.arquivo.pt/wayback/-cdx";
    var requestURL = "//<%=collectionsHost%>/" + "cdx";
    var versionsArray = [];
    var versionsURL = [];

    var inputURL = document.getElementById('txtSearch').value;
    var notFoundURLSearch = false;

  loading = false;
  $( document ).ajaxStart(function() {
    loading = true;
    $( "#loadingDiv").show();
  });
  $( document ).ajaxStop(function() {
    loading = false;
    $( "#loadingDiv").hide();
  });
  $( document ).ajaxComplete(function() {
    loading = false;
    $( "#loadingDiv").hide();
  });

    $.ajax({
    // example request to the cdx-server api - 'http://arquivo.pt/pywb/replay-cdx?url=http://www.sapo.pt/index.html&output=json&fl=url,timestamp'
       url: requestURL,
       cache: true,
       data: {
          output: 'json',
          url: urlsource,
          fl: 'url,timestamp,status',
          filter: '!~status:4|5',
          from: startTs,
          to: endTs
       },
       error: function() {
         // Apresenta que não tem resultados!
         createErrorPage();
       },
       dataType: 'text',
       success: function(data) {
          versionsArray = []
          if( data ) { 
             
            var tokens = data.split('\n')
            $.each(tokens, function(e){
                if(this != ""){
                    var version = JSON.parse(this);
                    if(version.status[0] === '4' || version.status[0] === '5'){ /*Ignore 400's and 500's*/
                      /*empty on purpose*/ 
                    } 
                    else{
                      versionsArray.push(version.timestamp);
                      versionsURL.push(version.url);
                    }
                     
                }
                
            }); 
            

            if( localStorage.getItem('isList') === null){
              if(isList()){
                createResultsList(tokens.length-1, inputURL);
                createMatrixList(versionsArray, versionsURL);     
              }
              else{
                createResultsTable(tokens.length-1, inputURL);
                createMatrixTable(versionsArray, versionsURL);    
              }
            }
            else{
              if(localStorage.getItem('isList') == 'true'){
                createResultsList(tokens.length-1, inputURL);
                createMatrixList(versionsArray, versionsURL);               
              }
              else{
                createResultsTable(tokens.length-1, inputURL);
                createMatrixTable(versionsArray, versionsURL);                
              }
            }
            attachClicks();
           
          } else {
              createErrorPage();
          } 
            
       },
      
       type: 'GET'
    });
</script>
<script type="text/javascript">
function attachClicks(){
  /*Action to show/hide versions on click*/
  touched = false;
  $(".day-version-div").click(function() {
    touched = true;
  });

  $(".month-version-div").click(function() {
    if(touched === false){
      $(this).children(".day-version-div").toggleClass("show-day-version");
      $(this).find(".monthCarret").toggleClass('fa-caret-up fa-caret-down');
      $(this).toggleClass("preventMonth");
      touched = true;
    }
  });

    $(".yearUl").click(function() {
      if(touched === false){
        $(this).children(".month-version-div").toggle();
        $(this).find(".yearCarret").toggleClass('fa-caret-up fa-caret-down');
        $(this).toggleClass("preventYear");
      }
      touched=false;  
    });
}
</script> 


        <c:if test="${not empty exception}">
        <% bean.LOG.error("Error while accessing to wayback: "+ pageContext.getAttribute("exception")); %>
        <div id="conteudo-resultado" class="container-fluid display-none col-sm-offset-1 col-sm-10 col-md-offset-2 col-md-8 col-lg-offset-3 col-lg-6 col-xl-offset-4 col-xl-4"> <%-- START OF: conteudo-resultado --%>
        <div id="second-column">
          <div id="search_stats"></div>
                        </c:if>

                        <% queryTime = (int) (System.currentTimeMillis() - startQueryTime); //for logging %>
<%

            } else {
              // option: (3)
              showList = true;                    
              showTip = urlMatch.group(1);
              String queryString_expanded="";
              bean.LOG.debug("[search.jsp] query input: " + queryString );
              queryString_splitted = queryString.split(" ");

              if( queryString.contains( "site:" ) || queryString.contains( "date:" ) ) {
                String buildTerm = ""; 
                for ( int i =0 ; i < queryString_splitted.length ; i++ ){
                  buildTerm = queryString_splitted[ i ];
                  queryString_expanded += " " + buildTerm; 
                }
              }
              
              bean.LOG.debug( "[FRONT-END] query input: " + queryString_expanded );
              query = NutchwaxQuery.parse( queryString_expanded , nutchConf );    //create the query object
              bean.LOG.debug( "[FRONT-END] query output: " + query.toString( ) );
            }
          } else {
            // option: (1)
            
                        query = NutchwaxQuery.parse(queryString, nutchConf);            //create the query object
                        bean.LOG.debug("query: " + query.toString());
            
            showList = true;
          }
        }
        %>
          
      
<% if (showList) { %>

<div id="conteudo-resultado" class="container-fluid display-none col-sm-offset-1 col-sm-10 col-md-offset-2 col-md-8 col-lg-offset-3 col-lg-6 col-xl-offset-4 col-xl-4"> <%-- START OF: conteudo-resultado --%>
<div id="second-column">


<%@include file="include/search-result-component.jsp"%>

<% } %> <%-- END OF: showList --%>

<% 
      if (hitsLength >= end || hitsLength > start) {
              long pagesAvailable = (long) (hitsTotal / hitsPerPage) ;
                      if ((hitsTotal % hitsPerPage) != 0) {
                              pagesAvailable++;
                      }

                      // Check if we are in the last page
                      if (hitsLength == end && hitsPerDup != 0) {
                              pagesAvailable = (long) (hitsLength / hitsPerPage);
                              if ((hitsLength % hitsPerPage) != 0) {
                                      pagesAvailable++;
                              }
                      }
      
          long currentPage = (long) ((start + 1) / hitsPerPage + 1) ;
          int maxPagesToShow = 10;
          long displayMin = (long) (currentPage - (0.5 * maxPagesToShow) );
      
          if (displayMin < 1) {
            displayMin = 1; 
          }
      
          long displayMax = displayMin + maxPagesToShow - 1 ;
          if (displayMax > pagesAvailable) {
            displayMax = pagesAvailable;
          }
%>

<%-- ---------------- --%>
<%-- No results presentend --%>
<%-- ---------------- --%>

<% if ( hitsTotal == 0 ) { %>
  
<%
  if (! request.getAttribute( "query" ).equals( "" ) ) {
%>
  <div id="conteudo-pesquisa-erro">
    <div class="alert alert-danger break-word col-xs-12 my-alert">
      <p><fmt:message key='search.no-results.title'/> <span class="text-bold"><c:out value = "${htmlQueryString}"/></span></p>
    </div>
    <div id="sugerimos-que" class="col-xs-12 no-padding-left">
        <p class="text-bold"><fmt:message key='search.no-results.suggestions'/></p>
      <ul class="suggestions-no-results">
        <li><fmt:message key='search.no-results.suggestions.well-written'/></li>
        <li><fmt:message key='search.no-results.suggestions.time-interval'/></li>
        <li><fmt:message key='search.no-results.suggestions.keywords'/></li>        
        <%-- Show specific suggestions for URL queries --%>
        <% if (usedWayback) { %>
        <li><fmt:message key='search.no-results.suggestions.internet-archive'><c:out value = "${urlQuery}"/></fmt:message></li>
        <li><fmt:message key='search.no-results.suggestions.suggest'><c:out value = "${urlQuery}"/></fmt:message></li>
        <% } %>
      </ul>
    </div>
    <!--<div class="voltar-erro"><a href="<%= request.getHeader("Referer")%>">&larr; <fmt:message key='search.no-results.go-back'/></a></div>-->
  </div>

<%
        }

   } else if (showList && hitsTotal != 0 && hitsPerDup != 0 && currentPage >= pagesAvailable && hitsTotal > end) {
   long previousPageStartForDup = (currentPage - 2) * hitsPerPage;
    String previousPageUrlForDup = request.getContextPath() + "search.jsp?" +
      "query=" + htmlQueryString +
      "&dateStart="+ dateStartString +
      "&dateEnd="+ dateEndString +
      "&start=" + previousPageStartForDup +
      "&hitsPerPage=" + hitsPerPage +
      "&hitsPerDup=" + 0 +
      "&dedupField=" + dedupField;
    if (sort != null) {
      previousPageUrlForDup = previousPageUrlForDup +
      "&sort=" + sort +
      "&reverse=" + reverse;
    }
  
  String noDupQuery = request.getContextPath() + "/search.jsp?" +
      "query=" + htmlQueryString +
      "&dateStart="+ dateStartString +
      "&dateEnd="+ dateEndString +
      "&start=" + 0 +
      "&hitsPerPage=" + hitsPerPage +
      "&hitsPerDup=" + 0 +
      "&dedupField=" + dedupField +
      "&l="+ language;
      if (sort != null) {
        previousPageUrlForDup = previousPageUrlForDup +
        "&sort=" + sort +
        "&reverse=" + reverse;
      }

         %>
  <div class="omitted-results">
    <fmt:message key="search.results.omitted"><fmt:param value="<%=noDupQuery%>"/></fmt:message>.
  </div>
<% } %>


<% if (hitsTotal >= 1 && !usedWayback) { %>              <%-- Start Pager IF --%>
<div class="pagesNextPrevious text-center">

<ul>
<%
if (currentPage > 1) {
long previousPageStart = (currentPage - 2) * hitsPerPage;
    String previousPageUrl = "search.jsp?" +
      "query=" + URLEncoder.encode(request.getAttribute("query").toString(), "UTF-8") +
      "&dateStart="+ dateStartString +
      "&dateEnd="+ dateEndString +
      "&pag=prev" +                             // mark as 'previous page' link 
      "&start=" + previousPageStart +
      "&hitsPerPage=" + hitsPerPage +
      "&hitsPerDup=" + hitsPerDup +
      "&dedupField=" + dedupField +
      "&l="+ language;
    if (sort != null) {
      previousPageUrl = previousPageUrl +
      "&sort=" + sort +
      "&reverse=" + reverse;
    }
    previousPageUrl = StringEscapeUtils.escapeHtml(previousPageUrl);
%>
  <li class="previous"><a onclick="ga('send', 'event', 'Full-text search', 'Previous page', document.location.href );" class="myButtonStyle text-center right10" role="button" href="<%=previousPageUrl%>" title="<fmt:message key='search.pager.previous'/>">&larr; <fmt:message key='search.pager.previous'/></a></li>
<% } %>
<%
  if (currentPage < pagesAvailable) {
    long nextPageStart = currentPage * hitsPerPage;
    String nextPageUrl = "search.jsp?" +
      "query=" + URLEncoder.encode(request.getAttribute("query").toString(), "UTF-8") +
      "&dateStart="+ dateStartString +
      "&dateEnd="+ dateEndString +
      "&pag=next" +
      "&start=" + nextPageStart +
      "&hitsPerPage=" + hitsPerPage +
      "&hitsPerDup=" + hitsPerDup +
      "&dedupField=" + dedupField +
      "&l="+ language;
    if (sort != null) {
      nextPageUrl = nextPageUrl +
      "&sort=" + sort +
      "&reverse=" + reverse;
    }
    nextPageUrl = StringEscapeUtils.escapeHtml(nextPageUrl);
%>
    <li class="next"><a onclick="ga('send', 'event', 'Full-text search', 'Next page', document.location.href );" class="myButtonStyle text-center" role="button" href="<%=nextPageUrl%>" title="<fmt:message key='search.pager.next'/>"><fmt:message key='search.pager.next'/> &rarr;</a></li>
<% } %>

</ul>

</div>
<% } %>                 <%-- End of pager IF --%>
<% } %>

</div>
    <% if ( (request.getParameter("query") == null || request.getParameter("query").equals("")) &&
            (request.getParameter("adv_and") == null || request.getParameter("adv_and").equals("")) &&
            (request.getParameter("adv_phr") == null || request.getParameter("adv_phr").equals("")) &&
            (request.getParameter("adv_not") == null || request.getParameter("adv_not").equals("")) &&
            (request.getParameter("format") == null || request.getParameter("format").equals("") ) &&
            (request.getParameter("site") == null || request.getParameter("site").equals(""))
     ){ 
    %>
      <%@ include file="include/intro.jsp" %>
    <% } %>


      </div>  <!-- FIM #conteudo-resultado  --> 
    </div>
  </div>
           
<%@include file="include/analytics.jsp" %>
<%@include file="include/footer.jsp" %>
</body>
</html>

<%@include file="include/logging.jsp" %>
