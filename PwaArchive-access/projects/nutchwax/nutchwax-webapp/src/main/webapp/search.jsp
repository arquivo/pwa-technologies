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

  String dateStartYear = dateStartString.substring(dateStartString.length()-4);

  String dateEndString = inputDateFormatter.format( dateEnd.getTime() );

  String dateEndYear = dateEndString.substring(dateEndString.length()-4);

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
        htmlQueryString = Entities.encode(htmlQueryString);
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

	<link rel="shortcut icon" href="img/logo-16.jpg" type="image/x-icon" />
	<link rel="search" type="application/opensearchdescription+xml" title="<fmt:message key='opensearch.title'><fmt:param value='<%=language%>'/></fmt:message>" href="opensearch.jsp?l=<%=language%>" />
	<link rel="stylesheet" title="Estilo principal" type="text/css" href="css/newStyle.css"  media="all" />
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
</head>

<body>
    <%@ include file="include/topbar.jsp" %>
    <div class="container-fluid topcontainer" id="headerSearchDiv">
        <div class="row">
            <div class="col-sm-10 col-sm-offset-1 col-md-8 col-md-offset-2 col-lg-6 col-lg-offset-3 text-right">
                <form id="searchForm" action="/search.jsp">
                <div id="form_container"> 
                    <div class="input-group stylish-input-group">
                        
                            <input id="txtSearch" value="<%=htmlQueryString%>" name="query" type="search" class="form-control no-radius search-input" placeholder="<fmt:message key='home.search.placeholder'/>"  autocapitalize="off" autocomplete="off" autocorrect="off">
                            <span class="clear-text"><i class="fa fa-close"></i></span>
                            <span class="input-group-addon no-radius search-button-span">
                                <button class="search-button" type="submit">
                                    <span class="glyphicon glyphicon-search white"></span>
                                </button>  
                            </span>
                        
                    </div>
                </div>
                <!--<a href="/advanced.jsp?l=pt">Pesquisa avançada</a>-->
                
                    
                        <div id="slider-date" class="col-sm-12"></div>
                    
                
                <div id="slider-caption" class="row">
                    <input size="4" maxlength="4" type="number" class="example-val text-center input-start-year" id="event-start" value="<%=dateStartYear%>" min="1996"  max="<%=yearEndNoParameter%>"></input>
                    <input size="4" maxlength="4" type="number" class="example-val text-center input-end-year" id="event-end" value="<%=dateEndYear%>" min="1996" max="<%=yearEndNoParameter%>"></input>
                    <input type="hidden" id="dateStart" name="dateStart" value="01/01/<%=dateStartYear%>"/>
                    <input type="hidden" id="dateEnd" name="dateEnd" value="31/12/<%=dateEndYear%>"/>
                    <input type="hidden" id="l" name="l" value="<%=language%>"/>
                </div>   
<script src="/include/clearForm.js"></script>  
<script>
  document.write("<div id='loadingDiv' class='text-center' style='text-align: center; margin-top: 10%; margin-bottom: 5%;'><div style='text-align: center; display: inline-block;'' class='cp-spinner cp-round'></div></div>");
  $( document ).ready(function() {
    if(typeof(loading)=="undefined" || loading != true){
      $('#loadingDiv').hide();
      $('#conteudo-resultado').show();
      dateSlider.removeAttribute('disabled');
    }
  });
</script>               
<script type="text/javascript">
// Create a new date from a string, return as a timestamp.

dateSlider = document.getElementById('slider-date');

var beginYear = parseInt("<%=dateStartYear%>");
var endYear = parseInt("<%=dateEndYear%>");
var minYear = 1996;
var maxYear = (new Date()).getFullYear() - 1

noUiSlider.create(dateSlider, {
// Create two timestamps to define a range.
    range: {
        min: [minYear],
        max: [maxYear]
    },
    tooltips: false,
    connect: true,
// Steps of one year
    step: 1,

// Two more timestamps indicate the handle starting positions.
    start: [ beginYear, endYear ],

// No decimals
    format: wNumb({
        decimals: 0
    })
}); 
dateSlider.setAttribute('disabled', true);

</script>
<script type="text/javascript">$('.noUi-tooltip').hide();</script>
<script type="text/javascript">
  $('#event-start').bind('input', function() { 
    var currentInputDate = $(this).val();
    currentInputDateNumber = parseInt(currentInputDate);
    var currentDateEndNumber =  parseInt($('#event-end').attr('value'));
    if( (currentInputDate.length) === 4 && currentInputDateNumber >= 1996 && currentInputDateNumber >= parseInt("<%=yearStartNoParameter%>") && currentInputDateNumber <= currentDateEndNumber){ /*if it is a year after 1996 and eventStartDate <= eventEndDate*/
       /* update the input year of #datestart*/
       var currentDate = $('#dateStart').attr('value');
       var currentDate = currentDate.substring(0, currentDate.length - 4) + currentInputDate.toString();
       dateSlider.noUiSlider.set([parseInt(currentInputDate) ,null]);
    }
    else  if(currentInputDateNumber > parseInt("<%=yearEndNoParameter%>")  ){
     $('#event-start').val(1996); 
     dateSlider.noUiSlider.set([1996 , null]);
    }    
    if((currentInputDate.length) === 4 && currentInputDateNumber >= currentDateEndNumber  ){
      dateSlider.noUiSlider.set([currentDateEndNumber , null]);
      $('#event-start').val(currentDateEndNumber);
    }
});
</script>
<script type="text/javascript">
$("#event-end").blur(function() {
  if( $("#event-end").val().toString().length < 4 ){
    $('#event-end').val(parseInt("<%=yearEndNoParameter%>"));
    dateSlider.noUiSlider.set([null , parseInt("<%=yearEndNoParameter%>")]);
  }
});

$("#event-start").blur(function() {
  if( $("#event-start").val().toString().length < 4 || $("#event-start").val() < 1996 ){
    $('#event-start').val(1996);
    dateSlider.noUiSlider.set([1996 , null]);
  }
});

  $('#event-end').bind('input', function() { 
    var currentInputDate = $(this).val();
    currentInputDateNumber = parseInt(currentInputDate);
    var currentDateStartNumber =  parseInt($('#event-start').attr('value'));
    if( (currentInputDate.length) === 4 && currentInputDateNumber <= parseInt("<%=yearEndNoParameter%>") && currentInputDateNumber >= currentDateStartNumber ){ 
      /*if it is a year*/
       /* update the input year of #dateend*/
       var currentDate = $('#dateEnd').attr('value');
       var currentDate = currentDate.substring(0, currentDate.length - 4) + currentInputDate.toString();
       dateSlider.noUiSlider.set([null , currentInputDateNumber]);
    } 
    if((currentInputDate.length) === 4 && currentInputDateNumber < currentDateStartNumber  ){
      dateSlider.noUiSlider.set([null , currentDateStartNumber]);
      $('#event-end').val(currentDateStartNumber);
    }
    else  if((currentInputDate.length) >= 4 && currentInputDateNumber > parseInt("<%=yearEndNoParameter%>")  ){
     $('#event-end').val(parseInt("<%=yearEndNoParameter%>")); 
     dateSlider.noUiSlider.set([null , parseInt("<%=yearEndNoParameter%>")]);
    }
});
</script>
<script type="text/javascript">
// Create a list of day and monthnames.
var
    weekdays = [
        "Sunday", "Monday", "Tuesday",
        "Wednesday", "Thursday", "Friday",
        "Saturday"
    ],
    months = [
        "January", "February", "March",
        "April", "May", "June", "July",
        "August", "September", "October",
        "November", "December"
    ];

var dateValues = [
    document.getElementById('event-start'),
    document.getElementById('event-end')
];
changed = false;
initial = 0; /*do not show tooltips when slider is initialized i.e. when initial < 2*/
dateSlider.noUiSlider.on('update', function( values, handle ) {

  if(initial > 1){
      $(".noUi-handle[data-handle='"+handle.toString()+"'] .noUi-tooltip").show().delay(1000).fadeOut();
    }
    else{
      initial += 1;
    }
    if(handle==0){

      if( $('#dateStart').attr('value').substring(6, 10) != values[handle]){
        $('#dateStart').attr('value', '01/01/'+values[handle]);
        $('#event-start').attr('value', +values[handle]);
        changed= true;
        console.log('changed true')        
      }     
    }else{
      if( $('#dateEnd').attr('value').substring(6, 10) != values[handle]){    
       $('#dateEnd').attr('value', '31/12/'+values[handle]);
       $('#event-end').attr('value', +values[handle]);
       changed=true
      }
    }
});     

dateSlider.noUiSlider.on('set', function( values, handle ) {
  if(changed){
    changed=false;
    $('.search-button').click();
  }
});

// Append a suffix to dates.
// Example: 23 => 23rd, 1 => 1st.
function nth (d) {
  if(d>3 && d<21) return 'th';
  switch (d % 10) {
        case 1:  return "st";
        case 2:  return "nd";
        case 3:  return "rd";
        default: return "th";
    }
}

// Create a string representation of the date.
function formatDate ( date ) {
    return weekdays[date.getDay()] + ", " +
        date.getDate() + nth(date.getDate()) + " " +
        months[date.getMonth()] + " " +
        date.getFullYear();
}    

</script>

                </form>
            </div>
        </div>
    </div>
<!-- End SearchHeader -->

  
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

function createMatrix(versionsArray, versionsURL){
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



function createResultsPage(numberOfVersions, inputURL){

    $('<div id="resultados-url">'+Content.resultsQuestion+' \'<a href="searchMobile.jsp?query=%22'+inputURL+'%22">'+inputURL+'</a>\'</div>'+
/*        '<div>' +
               '    <h3 class="texto-1 text-center">'+ formatNumberOfVersions(numberOfVersions.toString()) +' '+ 
                 (numberOfVersions===1 ?  Content.versionPage : Content.versionsPage )+
                 ' '+'<strong>'+ inputURL+'</strong>'+'</h3>' +
        '</div>' + */
          '<div id="years" class="container-fluid col-sm-offset-1 col-sm-10 col-md-offset-2 col-md-8 col-lg-offset-3 col-lg-6 col-xl-offset-4 col-xl-4 ">' +
          '</div>' +
        '</div>' +
      '</div>').insertAfter("#headerSearchDiv");
     
}

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
  $('<div id="conteudo-resultado" class="container-fluid col-sm-offset-1 col-sm-10 col-md-offset-2 col-md-8 col-lg-offset-3 col-lg-6 col-xl-offset-4 col-xl-4">'+
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

  loading = false;
	$( document ).ajaxStart(function() {
    loading = true;
	  $( "#loadingDiv").show();
	});
	$( document ).ajaxStop(function() {
    loading = false;
	  $( "#loadingDiv").hide();
    dateSlider.removeAttribute('disabled');
	});
  $( document ).ajaxComplete(function() {
    loading = false;
    $( "#loadingDiv").hide();
    dateSlider.removeAttribute('disabled');
  });

    $.ajax({
    // example request to the cdx-server api - 'http://arquivo.pt/pywb/replay-cdx?url=http://www.sapo.pt/index.html&output=json&fl=url,timestamp'
       url: requestURL,
       cache: true,
       data: {
          output: 'json',
          url: urlsource,
          fl: 'url,timestamp,status',
          filter: '!status:4|5',
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
          createResultsPage(tokens.length-1, inputURL);
          createMatrix(versionsArray, versionsURL);
          attachClicks();
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
    /*$j(this).find("i").toggleClass('fa-caret-up fa-caret-down');*/
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
             // String queryString_expanded=""; TODO bug: search subdomains with site operator 
              /*if (queryString.contains("site:")){ // It expands an URL since it is an advanced search
                queryString_splitted = queryString.split(" ");
                String queryString_expanded="";
                for (int i =0; i<queryString_splitted.length;i++){
                 if (queryString_splitted[i].contains("site:")){
                  queryString_splitted[i] = queryString_splitted[i].replace("site:", "");


                  URL queryStringURL = new URL("http://"+queryString_splitted[i]);
                  String queryStringHost = queryStringURL.getHost();
                  queryString_splitted[i] = queryString_splitted[i].replace(queryStringHost, queryStringHost.toLowerCase()); // hostname to lowercase
                  queryString_splitted[i]= NutchwaxQuery.encodeExacturl("exacturlexpand:http://"+queryString_splitted[i]); //TODO: SPLIT HOSTNAME

                 }
                 queryString_expanded+=" "+queryString_splitted[i];
                }
        
                      query = NutchwaxQuery.parse(queryString_expanded, nutchConf);    //create the query object
              }
              else {*/
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
<!--<h1><fmt:message key='search.query'><fmt:param><c:out value='${requestScope.query}'/></fmt:param></fmt:message></h1>-->

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

<% if ( hitsTotal == 0) { %>

<%
        // When empty query → intro page
        if ( request.getAttribute("query").equals("") ) {
%>
        <div id="search_stats"></div>
        <div id="no_results">
    <c:redirect url='index.jsp'>
      <c:param name='l' value='${language}'/>
    </c:redirect>
  </div>
        <% } else { %>
  <div id="conteudo-pesquisa-erro">
    <div class="alert alert-danger break-word col-xs-12 my-alert">
      <p><fmt:message key='search.no-results.title'/> <span class="text-bold"><%=htmlQueryString%></span></p>
    </div>
    <div id="sugerimos-que" class="col-xs-12 no-padding-left">
        <p class="text-bold"><fmt:message key='search.no-results.suggestions'/></p>
      <ul class="suggestions-no-results">
        <li><fmt:message key='search.no-results.suggestions.well-written'/></li>
        <li><fmt:message key='search.no-results.suggestions.time-interval'/></li>
        <li><fmt:message key='search.no-results.suggestions.keywords'/></li>
        <li><fmt:message key='search.no-results.suggestions.generic-words'/></li>
        <%-- Show specific suggestions for URL queries --%>
        <% if ( usedWayback) { %>
        <li><fmt:message key='search.no-results.suggestions.internet-archive'><fmt:param value='<%=urlQuery%>'/></fmt:message></li>
        <li><fmt:message key='search.no-results.suggestions.suggest'><fmt:param value='<%=urlQuery%>'/></fmt:message></li>
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
%>
  <li class="previous"><a onclick="ga('send', 'event', 'Full-text search', 'Previous page', document.location.href );" class="myButtonStyle text-center right10" role="button" href="<%=previousPageUrl%>" title="<fmt:message key='search.pager.previous'/>"><fmt:message key='search.pager.previous'/></a></li>
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
%>
    <li class="next"><a onclick="ga('send', 'event', 'Full-text search', 'Next page', document.location.href );" class="myButtonStyle text-center" role="button" href="<%=nextPageUrl%>" title="<fmt:message key='search.pager.next'/>"><fmt:message key='search.pager.next'/></a></li>
<% } %>

</ul>

</div>
<% } %>                 <%-- End of pager IF --%>
<% } %>
</div>

      </div>  <!-- FIM #conteudo-resultado  --> 
    </div>
  </div>
           
<%@include file="include/analytics.jsp" %>
<%@include file="include/footer.jsp" %>
</body>
</html>

<%@include file="include/logging.jsp" %>
