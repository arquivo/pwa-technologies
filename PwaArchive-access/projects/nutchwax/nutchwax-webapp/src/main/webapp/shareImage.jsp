<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd" >
<%@ page
	session="true"
	contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"	

	import="java.io.File"
	import="java.util.Calendar"
	import="java.util.Date"
	import="java.util.GregorianCalendar"
	import="java.net.URLEncoder"
	import= "java.net.*"
	import= "java.io.*"	
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
%>
<% // Set the character encoding to use when interpreting request values.
  request.setCharacterEncoding("UTF-8");
%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt_rt" prefix="fmt" %>
<%@ include file="include/logging_params.jsp" %>
<%@ include file="include/i18n.jsp" %>
<fmt:setLocale value="<%=language%>"/>

<%!	//To please the compiler since logging need those -- check [search.jsp]
	private static int hitsTotal = -10;		// the value -10 will be used to mark as being "advanced search"
	private static Calendar DATE_START = new GregorianCalendar(1996, 1-1, 1);
	private static Calendar dateStart = new GregorianCalendar();
	private static Calendar dateEnd = new GregorianCalendar();
%>

<%-- Get the application beans --%>
<%
  Configuration nutchConf = NutchwaxConfiguration.getConfiguration(application);
  NutchBean bean = NutchwaxBean.get(application, nutchConf);
%>

<%
  // Prepare the query values to be presented on the page, preserving the session

  String imageURL = "";
  String imgRefURL = "http://arquivo.pt";
  String previousURL ="http://arquivo.pt";
  String imgRefTs ="20100000000000";
  String imgRes ="";
  String imageURLDecoded= "";

  String imageTitle = "";
  String imageWidth ="";
  String imageHeight = "";
  String [] tokens ;


  String htmlQueryString = "";

  if(request.getParameter("imgurl") != null){
    imageURL = request.getParameter("imgurl");
    imageURLDecoded = URLDecoder.decode(imageURL.replace("+", "%2B"), "UTF-8");
  }

  if(request.getParameter("imgrefurl") != null && request.getParameter("imgrefts") != null ){
    previousURL = request.getParameter("imgrefurl");
    imgRefTs = request.getParameter("imgrefts");
    imgRefURL = "/wayback/" + imgRefTs + "/" + previousURL;
  }

  if(request.getParameter("imgres") != null){
    imgRes = request.getParameter("imgres");
    tokens = imgRes.split("x");
    imageWidth = tokens[0];
    imageHeight = tokens[1];
  }

  if(request.getParameter("query") != null){
    imageTitle += " - " + request.getParameter("query");
  }

  if ( request.getParameter("query") != null ) {
        htmlQueryString = request.getParameter("query").toString();
        /*htmlQueryString = Entities.encode(htmlQueryString);*/
  }
  //htmlQueryString= htmlQueryString.trim();
  htmlQueryString = Entities.encode(htmlQueryString);

 /*** Start date ***/
  Calendar dateStart = (Calendar)DATE_START.clone();
  Calendar DATE_END = new GregorianCalendar();
  DATE_END.set( Calendar.YEAR, DATE_END.get(Calendar.YEAR) - 1 );
  DATE_END.set( Calendar.MONTH, 12-1 );
  DATE_END.set( Calendar.DAY_OF_MONTH, 31 );
  DATE_END.set( Calendar.HOUR_OF_DAY, 23 );
  DATE_END.set( Calendar.MINUTE, 59 );
  DATE_END.set( Calendar.SECOND, 59 );
  SimpleDateFormat inputDateFormatter = new SimpleDateFormat("dd/MM/yyyy");

  if ( request.getParameter("dateStart") != null && !request.getParameter("dateStart").equals("") ) {
        try {
                dateStart.setTime( inputDateFormatter.parse(request.getParameter("dateStart")) );
        } catch (NullPointerException e) {
              /*TODO:: log this*/
        }
  }
  /*** End date ***/
  Calendar dateEnd = (Calendar)DATE_END.clone();                                // Setting current date

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

  String dateEndString = inputDateFormatter.format( dateEnd.getTime() );


  String safeSearchString = request.getParameter("safeSearch");

%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="pt-PT" lang="pt-PT"><head>
  <% if (htmlQueryString.length() > 0) { %>	
	  <title><fmt:message key='shareImage.image'/> <%=imageTitle%> — Arquivo.pt</title>
  <% } else { %>
  	  <title><fmt:message key='shareImage.image'/> — Arquivo.pt</title>
  <% } %>
  <meta http-equiv="Content-Type" content="application/xhtml+xml; charset=UTF-8"/>
  
  <meta http-equiv="Content-Language" content="pt-PT"/>
  <meta name="Keywords" content="resultado, pesquisa, buscar, arquivo, Web, português, portuguesa, Portugal"/>
  <meta name="Description" content="Página de resultados de uma pesquisa de imagens feita no Arquivo.pt."/>
  <link rel="shortcut icon" href="img/logo-16.jpg" type="image/x-icon"/>
  <link rel="search" type="application/opensearchdescription+xml" title="Arquivo.pt(pt)" href="opensearch.jsp?l=pt"/>
  <link rel="stylesheet" title="Estilo principal" type="text/css" href="css/style.css" media="all"/>
  <link href="css/csspin.css" rel="stylesheet" type="text/css"/>

  <meta property="og:title" content="<fmt:message key='shareImage.image'/> <%=imageTitle%> — Arquivo.pt" />
  <meta property="og:description" content="<fmt:message key='shareImage.foundInArquivo' />" />
  <meta property="og:image:width" content="<%=imageWidth%>" />
  <meta property="og:image:height" content="<%=imageHeight%>" />
  <meta property="og:image" content="<%=imageURLDecoded%>" />

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

  </script>
  <script src="https://apis.google.com/js/client.js" type="text/javascript"> </script>
  <script type="text/javascript" async="" src="http://www.google-analytics.com/ga.js"></script>
  <script type="text/javascript">
                var minDate = new Date(820450800000);
                var maxDate = new Date(1451606399842);
  </script>
  <script  src="/js/jquery-latest.min.js" type="text/javascript"></script>
  <link rel="stylesheet" type="text/css" href="//code.jquery.com/ui/1.12.1/themes/base/jquery-ui.css"/>
  <script type="text/javascript" src="https://code.jquery.com/ui/1.12.1/jquery-ui.js"></script>
  <script src='http://rawgit.com/jasonday/jQuery-UI-Dialog-extended/master/jquery.dialogOptions.js'></script>
  <script type="text/javascript" src="js/shareImages.js"></script>
  <script type="text/javascript" src="js/ui.datepicker.js"></script>    
  <script type="text/javascript" src="js/ui.datepicker-pt-BR.js"></script>
  <script type="text/javascript">
    var addthis_config = addthis_config||{};
        addthis_config.data_track_addressbar = false;
        addthis_config.data_track_clickback = false;
  </script>
  <script type="text/javascript" src="//s7.addthis.com/js/300/addthis_widget.js#pubid=ra-5645cdb2e22ca317" async="async"/>

<%@include file="include/analytics.jsp" %>
</head>
<body style="background: #222">

<!--?xml version="1.0" encoding="UTF-8"?-->
<a href="/images.jsp" class="expand__close" title="<fmt:message key="images.close"/>"></a>

  <div style="width: 60%; float: left; height:100vh;">  
    <div class="img-container" style="width: 60%">
      <script type="text/javascript">
        document.write ('<a target="_blank" href="'+decodeURIComponent("<%=imgRefURL%>")+'">' );
        document.write('<img style="max-height: 90%; max-width: 90%" src="'+ decodeURIComponent("<%=imageURL%>") +'"/> ');
        document.write('</a>');
      </script>
    </div>
  </div>  
  <div style="width: 37%; padding-left: 2%; float: left; border-left: solid 1px #454545; height: 100vh; display: table;">
      <div id="imageInfo" style="display: table-cell; vertical-align: middle;"> 
       <h2 style="color:white; word-wrap: break-word;">
        <script type="text/javascript">
          document.write(''+ decodeURIComponent("<%=previousURL%>") );
        </script>
       </h2>
       <br> 
       <h2 style="color:white; font-weight:bold;word-wrap: break-word;">
         <script type="text/javascript">
           document.write(''+ getDateSpaceFormated("<%=imgRefTs%>"));
         </script>
       </h2>
       <div style="padding-top:20px; padding-bottom:50px;">
        <h2 style="color:white;word-wrap: break-word;"> <fmt:message key='shareImage.size'/> <%=imgRes%> </h2>
       </div>
       <div style="display: inline; white-space: nowrap; overflow: hidden;">
          <script type="text/javascript">
            document.write('<a class="imageViewerAnchor" target="_blank" href="'+decodeURIComponent("<%=imgRefURL%>")+'">');
          </script>
            <span class="imageViewerButton"><fmt:message key='shareImage.visitPage'/></span>
          </a>

          <script type="text/javascript">
            document.write('<a target="_blank" class="imageViewerAnchor" style="margin-left: 20px" href="'+decodeURIComponent("<%=imageURL%>") +'">');
          </script>

            <span class="imageViewerButton"><fmt:message key='shareImage.showImage'/></span>
          </a>
         <button class="imageViewerAnchor" id="dButton" style="margin-left: 20px" >
          <span class="imageViewerButton" style="line-height:25px;"><fmt:message key='shareImage.share'/></span>
         </button>
       </div> 
      </div>  
  </div>
  <div id="dialog"  class="content_dialog">
          <h1 style="color:black; padding-top: 10px;"><fmt:message key='shareImage.share'/></h1>
          <button id="dialogClose" href="" class="expand__close__mini" title="Fechar"></button>

          <a class="addthis_button_facebook" style="text-decoration: none;"><h2 style="color:black; padding-top: 30px;"> <img width="40px" style="vertical-align: middle" src="/img/FB-f-Logo__blue_144.png"> <span style="padding-left: 10px;">Facebook</span></h2></a>
          <a class="addthis_button_twitter" style="text-decoration: none;"><h2 style="color:black; padding-top: 30px;"> <img width="40px" style="vertical-align: middle" src="/img/Twitter_Logo_White_On_Blue.png"> <span style="padding-left: 10px;">Twitter</span></h2></a>

          <button data-clipboard-target="#shortURL" id="btnCopy"><h2 style="color:grey; padding-top: 40px;" id="h2Copy"  > <fmt:message key='shareImage.clickToCopy'/></h2></button>
          <h2 id="shortURL" style="padding-top: 10px;padding-bottom: 30px;"> <script> shortenCurrentURL();</script> </h2>
  </div>
  
  <script  src="/js/clipboard.min.js" type="text/javascript"></script>
   <script type="text/javascript">
    var linkCopied =   '<fmt:message key="shareImage.linkCopied"/>';
    initClipboard(linkCopied);
  </script>     
</body>
</html>
<%@include file="include/logging.jsp" %>
