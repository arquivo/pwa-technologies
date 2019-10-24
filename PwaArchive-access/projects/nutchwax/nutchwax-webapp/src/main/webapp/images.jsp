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
  import="java.util.HashSet"
  import="java.net.MalformedURLException"

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

  //Remove http and https before testing against this url pattern
  private static final Pattern URL_PATTERN = Pattern.compile("^. ?(([a-zA-Z\\d][-\\w\\.]+)\\.([a-zA-Z\\.]{2,6})([-\\/\\w\\p{L}\\.~,;:%&=?+$#*]*)*\\/?) ?.*$");   
%>

<%
  Properties prop = new Properties();
  prop.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("validTLDs/valid.properties"));
  String tldsLine = prop.getProperty("valid.tld");
  String tlds[] = tldsLine.split("\t");
  HashSet<String> validTlds = new HashSet<String>();
  for(String tld:tlds){
    validTlds.add(tld);
  }
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
  // Prepare the query values to be presented on the page, preserving the session
  String htmlQueryString = "";
  String query ="";
  boolean safe =true;
  boolean unsafe = false;
  String safeSearchString ="on";
  String type = ""; /*Default mimetype*/
  String size = "all"; /*Default image size*/
  String tools = "off"; /*Show toolbar*/
  int startPosition = 0;
  String startString = request.getParameter("start");
  if (startString != null)
    startPosition = Integer.parseInt(startString);



  if( request.getParameter("safeSearch") != null && request.getParameter("safeSearch").contains("off") ){
    safeSearchString = "off";
  }
  if ( request.getParameter("size") != null && request.getParameter("size") != "") {
          size = request.getParameter("size");
          if(! "sm".equals(size) && !"md".equals(size) && ! "lg".equals(size)){
            size = "all";
          }
  }

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


  if ( request.getParameter("type") != null && request.getParameter("type") != "") {
          type = request.getParameter("type");
          if(! "jpg".equals(type) && !"png".equals(type) && ! "gif".equals(type) && ! "bmp".equals(type) && ! "webp".equals(type)){
            type = "";
          }
  }



  if( ! "".equals(type) || ! "all".equals(size) || ! "on".equals(safeSearchString)){
    tools = "on";
  }else if ( request.getParameter("tools") != null && request.getParameter("tools") != "") {
    tools = request.getParameter("tools");
    if(! "on".equals(tools)){
      tools = "off";
    }
  }
  

  if ( request.getParameter("query") != null ) {
        htmlQueryString = request.getParameter("query").toString();   
        query= htmlQueryString;
        query = URLEncoder.encode(query, "UTF-8");
  }
  else{
        htmlQueryString = "";
        if ( request.getParameter("adv_and") != null && request.getParameter("adv_and") != "") {
                htmlQueryString += request.getParameter("adv_and");
                htmlQueryString += " ";
        }
        if ( request.getParameter("adv_phr") != null && request.getParameter("adv_phr") != "") {
                htmlQueryString += "\"" +request.getParameter("adv_phr") + "\"";
                htmlQueryString += " ";
        }
        if ( request.getParameter("adv_not") != null && request.getParameter("adv_not") != "") {
                String notStr = request.getParameter("adv_not");
                if (!notStr.startsWith("-"))
                        notStr = "-" + notStr;
                notStr = notStr.replaceAll("[ ]+", " -") +" ";
                htmlQueryString += notStr;
        }
        if ( request.getParameter("adv_mime") != null && request.getParameter("adv_mime") != "" ) {
                htmlQueryString += "filetype:"+ request.getParameter("adv_mime");
                htmlQueryString += " ";
        }
        if (request.getParameter("site") != null && request.getParameter("site") != "") {
                htmlQueryString += "site:";
                String siteParameter = request.getParameter("site"); //here split hostname and put it to lowercase

                if (siteParameter.startsWith("http://")) {
                        URL siteURL = new URL(siteParameter);
                        String siteHost = siteURL.getHost();
                        siteParameter = siteParameter.replace(siteHost, siteHost.toLowerCase()); // hostname to lowercase
                        htmlQueryString += siteParameter.substring("http://".length());
                } else if (siteParameter.startsWith("https://")) {
                        URL siteURL = new URL(siteParameter);
                        String siteHost = siteURL.getHost();
                        siteParameter = siteParameter.replace(siteHost, siteHost.toLowerCase()); // hostname to lowercase
                        htmlQueryString += siteParameter.substring("https://".length());
                } else {
                        URL siteURL = new URL("http://"+siteParameter);
                        String siteHost = siteURL.getHost();
                        siteParameter = siteParameter.replace(siteHost, siteHost.toLowerCase()); // hostname to lowercase
                        htmlQueryString += siteParameter;
                }             
                htmlQueryString += " ";
        }
        if (request.getParameter("type") != null && request.getParameter("type") != "" && !request.getParameter("type").toLowerCase().equals("all")) {
          htmlQueryString += "type:" + request.getParameter("type") + " " ;
        }
        String sizeParam = request.getParameter("size");
        if (sizeParam != null && sizeParam != "") {
          sizeParam = sizeParam.toLowerCase();
          if(sizeParam.equals("sm") || sizeParam.equals("md") || sizeParam.equals("lg")){
            htmlQueryString += "size:" + sizeParam + " " ;
          }
        }
        if (request.getParameter("safeSearch") != null && request.getParameter("safeSearch").toLowerCase().equals("off")) {        
          htmlQueryString += "safe:off ";
        }                

    }
  //htmlQueryString= StringEscapeUtils.escapeHtml(htmlQueryString);
  request.setAttribute("htmlQueryString", htmlQueryString);

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

  // Prepare the query values to be presented on the page, preserving the session
  htmlQueryString = "";

  if ( request.getParameter("query") != null ) {
        bean.LOG.debug("Received Query input");
        htmlQueryString = request.getParameter("query").toString();
        String [] inputWords = htmlQueryString.split("\\s+");
        StringBuilder reconstructedInputString = new StringBuilder();

        for (String word: inputWords){
          bean.LOG.debug("WORD: "+ word);
          if( word.startsWith("https://")){
            word= word.substring(8, word.length());
          }else if (word.startsWith("http://")){
            word = word.substring(7, word.length());
          }
          
          Matcher matcher = URL_PATTERN.matcher(word);

          if (matcher.find()) {
            
            try {       
              bean.LOG.debug("Attempting URL "+ word);
              URL myURL = new URL("http://" + word);
              String[] domainNameParts = myURL.getHost().split("\\.");
                  String tldString ="."+domainNameParts[domainNameParts.length-1].toUpperCase();
                  bean.LOG.debug("TLD:"+ tldString);                        
                  if(validTlds.contains(tldString)){
                    word = "site:" + word;
                  } 
                  else{
                    bean.LOG.debug("Invalid tld in word:"+ word);
                  }                 
            } catch (MalformedURLException e) {

              //NOT a valid URL we will not consider it just add the word without the site:         
            } 
          }
          reconstructedInputString.append(word).append(" ");      
        }
        htmlQueryString = reconstructedInputString.toString().substring(0, reconstructedInputString.toString().length()-1);          
        request.setAttribute("htmlQueryString", htmlQueryString);
  }

  int numrows = 25;
  String homeMessageClass= (htmlQueryString.equals("")) ? "" :  "hidden";
  String loaderDefaultClass = (homeMessageClass.equals("")) ? "hidden" : "";
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

  <link rel="shortcut icon" href="img/logo-16.png" type="image/x-icon"/>
  <link href="css/csspin.css" rel="stylesheet" type="text/css"/>
  <script type="text/javascript">
      var minDate = new Date(820450800000);
      var maxDate = new Date(<%=DATE_END.getTimeInMillis()%>);
      var minYear = minDate.getFullYear();
      var maxYear = maxDate.getFullYear();                
  </script>
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
  </script>

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


  <script src="https://apis.google.com/js/client.js" type="text/javascript"> </script>
  <script type="text/javascript" src="js/ui.datepicker.js"></script>
  <script type="text/javascript" src="js/ui.datepicker-pt-BR.js"></script>
  <!--<script type="text/javascript" src="js/imageConfigs.js"></script>-->

  <% String imageSearchAPI = nutchConf.get("wax.image.search.API", "https://arquivo.pt/imagesearch"); %>
  <script type="text/javascript">
  	imageSearchAPI = "<%=imageSearchAPI%>";
  </script>
  <script type="text/javascript" src="js/images2.js?imageSearch"></script>
  <script type="text/javascript">
    $(".border-mobile").click(function(e) {
       // Do something
       e.stopPropagation();
       console.log("button clicked");
    });    
  </script>

  <!-- NEW - 23.07.19: Call ionic -->
  <script src="../@ionic/core/dist/ionic.js"></script>
  <link rel="stylesheet" href="../@ionic/core/css/ionic.bundle.css">
 
  <script type="text/javascript">
    clickToCopy = '<fmt:message key="images.clickToCopy" />';
    language = '<%= language %>'
  </script>

  <script type="text/javascript">
  /*Addthis options share on facebook and twitter*/
    var addthis_config = addthis_config||{};
        addthis_config.data_track_addressbar = false;
        addthis_config.data_track_clickback = false;
  </script>
      <!-- swiper main menu --> 
   <script type="text/javascript" src="/js/swiper.min.js"></script>
  <script type="text/javascript" src="//s7.addthis.com/js/300/addthis_widget.js#pubid=ra-5645cdb2e22ca317" async="async"></script>
  <%@include file="include/analytics.jsp" %>
</head>
<body id="homeImages">
<!--?xml version="1.0" encoding="UTF-8"?-->
<script type="text/javascript">
function searchImages(startIndex){
    var dateStartWithSlashes = '<%=dateStartString%>';
    var dateEndWithSlashes = '<%=dateEndString%>';
    var safeSearchOption = '<%=safeSearchString%>';
    searchImagesJS(dateStartWithSlashes, dateEndWithSlashes, safeSearchOption,startIndex);
}
</script>
<script type="text/javascript">
  var sizeVar = "<%=size%>";
  var typeVar = "<%=type%>";
</script>
<script type="text/javascript">
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
  share  = '<fmt:message key="images.share" />';
  close ='<fmt:message key="images.close"/>';
  leftArrow='<fmt:message key="images.leftArrow"/>';
  rightArrow='<fmt:message key="images.rightArrow"/>';
  resolution='<fmt:message key="images.size" />';
  imageType='<fmt:message key="images.type" />';
  imageTitle ='<fmt:message key="images.imageTitle" />';
  visitPage='<fmt:message key="images.visitPage" />';
  showImage='<fmt:message key="images.showImage" />';
  imageUndefined='<fmt:message key="images.undefined" />';
  showDetails = '<fmt:message key="images.showDetails" />';
  fieldDescription = '<fmt:message key="images.field.description" />';
  pageString =  '<fmt:message key="images.page" />';
  imageString = '<fmt:message key="images.image" />';
  titleString= '<fmt:message key="images.title" />';
  resolutionString= '<fmt:message key="images.resolution" />';
  nameString= '<fmt:message key="images.name" />';
  notFoundTitle = '<fmt:message key="search.no-results.title"/>';
  noResultsSuggestions = '<fmt:message key="search.no-results.suggestions"/>';
  noResultsWellWritten = '<fmt:message key="search.no-results.suggestions.well-written"/>';
  noResultsInterval = '<fmt:message key="search.no-results.suggestions.time-interval"/>';
  noResultsKeywords = '<fmt:message key="search.no-results.suggestions.keywords"/>';
  noResultsGenericWords = '<fmt:message key="search.no-results.suggestions.generic-words"/>';
  startPosition = "<%=startPosition%>";
  numrows ="<%=numrows%>"; /*Number of Images to show by default*/
</script>

<script type="text/javascript" src="/js/js.cookie.js"></script>


  <%@ include file="include/topbar.jsp" %>
  <div class="container-fluid topcontainer" id="headerSearchDiv">
  <script type="text/javascript">
    imagesHref = window.location.href;
    pagesHref = window.location.href.toString().replace("images.jsp", "search.jsp"); /*TODO remove from this href parameters that are only appliable to image search*/
    advancedHref = window.location.href.toString().replace("images.jsp", "advancedImages.jsp");
  </script>    
  <%@ include file="include/imageHeaderMobile.jsp" %>
  <script type="text/javascript">$('#imagesTab').addClass('selected');$('#imagesTab').addClass('primary-underline');</script>


  <div class="row image-container">
    <script>
      document.write("<div id='loadingDiv' class='text-center lds-ring' style='text-align: center; margin-top: 10%; margin-bottom: 5%;display:block'><div></div><div></div><div></div><div></div></div>");
      $( document ).ready(function() {
        if(typeof(loading)=="undefined" || loading != true){
          $('#loadingDiv').hide();
          $('#conteudo-resultado').show();
        }
        $("#txtSearch").on('mousedown touchstart', function (e) {
          e.stopPropagation();
        });            
      });

    var displayResults;

    </script>   

       
    <% if ( (request.getParameter("query") == null || request.getParameter("query").equals("")) &&
            (request.getParameter("adv_and") == null || request.getParameter("adv_and").equals("")) &&
            (request.getParameter("adv_phr") == null || request.getParameter("adv_phr").equals("")) &&
            (request.getParameter("adv_not") == null || request.getParameter("adv_not").equals("")) &&
            (request.getParameter("type") == null || request.getParameter("type").equals("") || request.getParameter("type").toLowerCase().equals("all") ) &&
            (request.getParameter("size") == null || request.getParameter("size").equals("") || request.getParameter("size").toLowerCase().equals("all") ) &&
            (request.getParameter("safeSearch") == null || request.getParameter("safeSearch").equals("") || request.getParameter("safeSearch").toLowerCase().equals("on") ) &&
            (request.getParameter("site") == null || request.getParameter("site").equals(""))
     ){ 
    %>
      <%@ include file="include/intro.jsp" %>
      <section id="photos" style="display:none;">
      <script type="text/javascript">
        displayResults = true;
      </script>  
    <% } else { %>
      <section id="photos">
      <script type="text/javascript">
        displayResults = false;
      </script>
    <% } %>

  </section>    
    <div class="pagesNextPrevious text-center">
    <script type="text/javascript">   
      if(displayResults) {    
        document.write("<div class=\"pagesNextPrevious text-center\" style=\"display:none\">");   
      } else {    
        document.write("<div class=\"pagesNextPrevious text-center\">");    
      }   
    </script>   


      <ul class="next-previous-ul">
      <%
      if (startPosition >= numrows) {
          int previousPageStart = startPosition - numrows;
          if(previousPageStart <0){previousPageStart=0;}
          String previousPageUrl = "images.jsp?" + "query=" + query +
            "&dateStart="+ dateStartString +
            "&dateEnd="+ dateEndString +
            "&pag=prev" +                             // mark as 'previous page' link 
            "&start=" + previousPageStart +
            "&l="+ language;
          previousPageUrl = StringEscapeUtils.escapeHtml(previousPageUrl);
      %>
        <li class="previous previous-image" id="previousImage"><a onclick="ga('send', 'event', 'Image search mobile', 'Previous page', document.location.href );" class="myButtonStyle text-center right10" role="button" href="<%=previousPageUrl%>" title="<fmt:message key='search.pager.previous'/>">&larr; <fmt:message key='search.pager.previous'/></a></li>
      <% } %>

      <%
        if (true) { /*TODO:: add condition check if there are more results */
           long nextPageStart = startPosition + numrows;
           String nextPageUrl = "images.jsp?" +
            "query=" + query +
            "&dateStart="+ dateStartString +
            "&dateEnd="+ dateEndString +
            "&pag=next" +
            "&start=" + nextPageStart +
            "&l="+ language;
          nextPageUrl = StringEscapeUtils.escapeHtml(nextPageUrl);
      %>
          <li class="next next-image" id="nextImage">
            <a onclick="ga('send', 'event', 'Image search mobile', 'Next page', document.location.href );" class="myButtonStyle text-center" role="button" href="<%=nextPageUrl%>" title="<fmt:message key='search.pager.next'/>"><fmt:message key='search.pager.next'/> &rarr;</a>
          </li>
      <% } %>

      </ul>

    </div>  
  </div>

</div>  



<script type="text/javascript">
  String.prototype.replaceAll = String.prototype.replaceAll || function(needle, replacement) {
      return this.split(needle).join(replacement);
  };
</script>

<div id="ui-datepicker-div" class="ui-datepicker ui-widget ui-widget-content ui-helper-clearfix ui-corner-all ui-helper-hidden-accessible"></div><div id="ui-datepicker-div" class="ui-datepicker ui-widget ui-widget-content ui-helper-clearfix ui-corner-all ui-helper-hidden-accessible"></div><div id="ui-datepicker-div" class="ui-datepicker ui-widget ui-widget-content ui-helper-clearfix ui-corner-all ui-helper-hidden-accessible"></div>

</div></div></div>

  <script type="text/javascript">
    function rafAsync() {
        return new Promise(resolve => {
            requestAnimationFrame(resolve); //faster than set time out
        });
    }

    function checkElement(selector) {
        if (document.querySelector(selector) === null) {
            return rafAsync().then(() => checkElement(selector));
        } else {
            return Promise.resolve(true);
        }
    }
  </script>
  <script type="text/javascript">
    $('<div id="showSlides"><button onclick="previousImage()" class="left-image-viewer-arrow clean-button-no-fill"> <ion-icon name="ios-arrow-back" class="left-icon"></ion-icon></button><button onclick="nextImage()" class="right-image-viewer-arrow clean-button-no-fill"><ion-icon name="ios-arrow-forward" class="right-icon"></ion-icon></button><ion-slides id="expandedImageViewers" onload=slidesLoaded();></ion-slides></div>').insertBefore('.curve-background');

    checkElement('#expandedImageViewers > .swiper-wrapper') 
    .then((element) => {      
      if($('#txtSearch').val().length){doInitialSearch();}     
    });
  </script>  


  <script type="text/javascript">
    $('#showSlides').hide();
  </script>

<%@include file="include/analytics.jsp" %>
<%@include file="include/footer.jsp" %>
</body>
</html>

