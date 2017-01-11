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
  String htmlQueryString = "";

  if ( request.getParameter("query") != null ) {
        htmlQueryString = request.getParameter("query").toString();
        /*htmlQueryString = Entities.encode(htmlQueryString);*/
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
        if ( request.getParameterValues("size") != null) {
                String [] sizes = request.getParameterValues("size");
                String allSizes = "";
                for( String currentSize: sizes){
                  allSizes += currentSize + " ";
                }

                if(!allSizes.contains("icon") || !allSizes.contains("small") || !allSizes.contains("medium")  || !allSizes.contains("large")){ /*the default case is all sizes, no need to add string if all sizes selected*/
                  for( String currentSize: sizes){
                    htmlQueryString += "size:"+ currentSize;
                    htmlQueryString += " ";
                  }
                }
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
        if (request.getParameter("format") != null && request.getParameter("format") != "" && !request.getParameter("format").equals("all")) {
                String [] types = request.getParameterValues("format");
                String allTypes = "";
                for( String currentType: types){
                  allTypes += currentType + " ";
                }

                if(!allTypes.contains("jpeg") || !allTypes.contains("png") || !allTypes.contains("gif")  || !allTypes.contains("tiff")){ /*the default case is all sizes, no need to add string if all sizes selected*/
                  for( String currentType: types){
                    htmlQueryString += "type:"+ currentType;
                    htmlQueryString += " ";
                  }
                }
        }
        if (request.getParameter("sort") != null && request.getParameter("sort") != "" && !request.getParameter("sort").equals("relevance")) {
          String sortCriteria = request.getParameter("sort");
          if(sortCriteria.equals("new") || sortCriteria.equals("old")){
            htmlQueryString += "sort:" + sortCriteria + " ";
          }
        }
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
	  <title><%=htmlQueryString%> — Arquivo.pt</title>
  <% } else { %>
  	  <title><fmt:message key='images.imageTitle'/> — Arquivo.pt</title>
  <% } %>
  <meta http-equiv="Content-Type" content="application/xhtml+xml; charset=UTF-8">
  
  <meta http-equiv="Content-Language" content="pt-PT">
  <meta name="Keywords" content="resultado, pesquisa, buscar, arquivo, Web, português, portuguesa, Portugal">
  <meta name="Description" content="Página de resultados de uma pesquisa feita no Arquivo.pt.">
  <link rel="shortcut icon" href="img/logo-16.jpg" type="image/x-icon">
  <link rel="search" type="application/opensearchdescription+xml" title="Arquivo.pt(pt)" href="opensearch.jsp?l=pt">
  <link rel="stylesheet" title="Estilo principal" type="text/css" href="css/style.css" media="all">
  <link href="css/csspin.css" rel="stylesheet" type="text/css">

  <script type="text/javascript" async="" src="http://www.google-analytics.com/ga.js"></script><script type="text/javascript" async="" src="http://www.google-analytics.com/ga.js"></script><script type="text/javascript" async="" src="http://www.google-analytics.com/ga.js"></script><script type="text/javascript">
                var minDate = new Date(820450800000);
                var maxDate = new Date(1451606399842);
        </script>
        <script type="text/javascript" src="js/jquery-1.3.2.min.js"></script>
        <link rel="stylesheet" type="text/css" href="css/jquery-ui-1.7.2.custom.css">
        <script type="text/javascript" src="js/jquery-ui-1.7.2.custom.min.js"></script>

    <script src="/js/jquery-latest.min.js" type="text/javascript"></script>
    <script>var $j = jQuery.noConflict(true);</script>


        <script type="text/javascript" src="js/ui.datepicker.js"></script>
        
        <script type="text/javascript" src="js/ui.datepicker-pt-BR.js"></script>
        
        <script type="text/javascript" src="js/configs.js"></script>

<!-- Piwik -->
<script type="text/javascript">
  var _paq = _paq || [];
  _paq.push(['trackPageView']);
  _paq.push(['enableLinkTracking']);
  (function() {
    var u="//p27arquivo.piwikpro.com/";
    _paq.push(['setTrackerUrl', u+'piwik.php']);
    _paq.push(['setSiteId', 1]);
    var d=document, g=d.createElement('script'), s=d.getElementsByTagName('script')[0];
    g.type='text/javascript'; g.async=true; g.defer=true; g.src=u+'piwik.js'; s.parentNode.insertBefore(g,s);
  })();
</script>
<noscript><p><img src="//p27arquivo.piwikpro.com/piwik.php?idsite=1" style="border:0;" alt="" /></p></noscript>
<!-- End Piwik Code -->
<%@include file="include/analytics.jsp" %>
</head>
<body>
  <!--?xml version="1.0" encoding="UTF-8"?-->
<script type="text/javascript">
	startPosition = 0; //the default start position to get the images global variable
</script>

<script type="text/javascript">
	$(function() {
	    $("#txtSearch").keypress(function (e) {
	        if ((e.which && e.which == 13) || (e.keyCode && e.keyCode == 13)) {
	        	$('#btnSubmit').click();
	            return false;
	        } else {
	            return true;
	        }
	    });
	});	
</script>

<script type="text/javascript">
function getMoreImages(){
	startPosition += 100;
	$('#moreResults').remove();
	$('#resultsUl').append('<li id="loadingMoreImages" style="text-align:center; margin-right: 1%; margin-bottom:1%; margin-top:4%; width:100%"> <div style="width: 100%; text-align:center"><div style="text-align:center;" class="cp-spinner cp-round"></div></div> </li>');
	
	searchImages(startPosition);
}

function loadingFinished(){
	$("#resultsUl").append('<li id="moreResults" style="width:100%; padding-top: 20px;"><button class="search-submit" style="float:none" onclick="getMoreImages()">Mais Resultados</button></li>');
	$('#loadingDiv').hide();
	$("#resultsUl").show();
	if($('#loadingMoreImages').length){
		$('#loadingMoreImages').remove();
	}

}

totalPosition = 0; //global variable

function doInitialSearch(){
	startPosition = 0;
	searchImages(startPosition);
}

function searchImages(startIndex){
	var safeSearchOption = '<%=safeSearchString%>';

	if( safeSearchOption == "null"){
		safeSearchOption = "yes";
	}
	
    var input = $('#txtSearch').val();
    var tokens = input.split(' ');
    var size = '';
    for (var i= 0; i< tokens.length; i++){
      if(tokens[i].substring(0, 'size:'.length) === 'size:'){
        size += tokens[i].substring('size:'.length) + ' ';
      }
    }
    if( size === ''){
      size = 'all';
    }

    var dateStartWithSlashes = '<%=dateStartString%>';
    var dateEndWithSlashes = '<%=dateEndString%>';

    var dateStartTokenes = dateStartWithSlashes.split("/");
    var dateStartTs = dateStartTokenes[2]+ dateStartTokenes[1] + dateStartTokenes[0]+ "000000";

    var dateEndTokenes = dateEndWithSlashes.split("/");
    var dateEndTs = dateEndTokenes[2]+ dateEndTokenes[1] + dateEndTokenes[0]+ "000000"     ;

    var dateFinal = dateStartTs+"-"+dateEndTs; 
    var showAll = false;

    var minWidth = 0;
    var maxWidth = 0;
    var minHeight = 0;
    var maxHeigth = 0;

    var icon = false;
    var small = false;
    var small = false;
    var large = false;
    var all= false;
    var medium = false;

    if( size.indexOf('icon') !== -1){
       //minWidth = 0;
       //maxWidth = 200; /*Equals to infinite*/
       //minHeight = 0;
       //maxHeigth = 200; /*Equals to infinite*/  
       icon = true;
    }
    if( size.indexOf('small') !== -1){
       //minWidth = 200;
       //maxWidth = 400;
       //minHeight = 200;
       //maxHeigth = 300; 
       small= true;      
    }
    if( size.indexOf('medium') !== -1){
       //minWidth = 300;
       //maxWidth = 1024; 
       //minHeight = 400;
       //maxHeigth = 768;        
       medium = true;
    }
    if( size.indexOf('large') !== -1){
       //minWidth = 1024;
       //maxWidth = 100000; /*Equals to infinite*/
       //minHeight = 768;
       //maxHeigth = 100000; /*Equals to infinite*/
       large= true; 
    } if( icon ===false && small === false && medium ===false && large === false){
       /*Default Case show all images*/
       //minWidth = 0;
       //maxWidth = 100000; /*Equals to infinite*/
       //minHeight = 0;
       //maxHeigth = 100000; /*Equals to infinite*/          
       all = true;
    }

    $(document).ajaxStart(function(){
    	if(startPosition == 0){
    		$('#resultsUl').empty();    		
    		$('#loadingDiv').show();
    	}

	});
    $.ajax({
    // example request to the cdx-server api - 'http://arquivo.pt/pywb/replay-cdx?url=http://www.sapo.pt/index.html&output=json&fl=url,timestamp'
       url: "http://p27.arquivo.pt/getimagesWS",
       data: {
          query: input,
          stamp: dateFinal,
          start: startIndex,
          safeImage: 'all' 
       },
       error: function() {
         console.log("Error In Ajax request to getimages");            
       },
       dataType: 'text',
       success: function(data) {

        var responseJson = $j.parseJSON(data);


        /*responseJson.content[0].url*/
		//$('#imageResults').remove();
		if(startIndex == 0 ){
        	$('#resultsUl').empty();
        	/*$('#resultsUl').css('display', 'none');*/ 
        }
        var totalResults = responseJson.totalResults;
        
        if ( totalResults === 0){
        	loadingFinished();
        }
        else{
	        for (var i=0; i< responseJson.totalResults; i++){
	            var currentImageURL = responseJson.content[i].url;
	            var originalURL = responseJson.content[i].urlOriginal;

	            imageObj = new Image();
	            imageObj.timestamp = responseJson.content[i].timestamp;
	            imageObj.originalURL = originalURL;
	            imageObj.currentImageURL = currentImageURL;
	            imageObj.position = totalPosition;
	            totalPosition = totalPosition + 1;

	            imageObj.src = currentImageURL;

	            var resizeImageHeight = 200;

	            imageObj.onload = function() {
	            	
	            	if( startIndex != 0 &&  totalResults == responseJson.totalResults){
	            		$('#loadingMoreImages').remove();
	            	}

	            	totalResults --;

	                if(    (icon ===true && this.height <= 200 && this.width<= 200) 
	                    || (small===true && this.height >=200 && this.height <= 300 && this.width >= 200 && this.width<=400)
	                    || (medium===true && this.height >=300 && this.height <= 768 && this.width >= 400 && this.width<=1024)
	                    || (large===true && this.height >=768 && this.width >= 1024 )
	                    || all === true){
	                   
	                      if(this.height <= resizeImageHeight){
	                          insertInPosition(this.position, this, this.height, resizeImageHeight, this.height);
	                      }
	                      else{
	                        insertInPosition(this.position, this, resizeImageHeight, resizeImageHeight, this.height);
	                      }
	                } /* ignore images not applying size criteria*/
	                if(totalResults <= 0){
	                	loadingFinished();
	                }
	            }
	            
	           
	            imageObj.onerror = function() {
	                // image did not load
	            	if( startIndex != 0 &&  totalResults == responseJson.totalResults){
	            		$('#loadingMoreImages').remove();
	            	}                
	                totalResults --;
	                console.log("Error loading: " + this.currentImageURL);
	                if(totalResults <= 0){
	                	loadingFinished();
	                }
	            }

	        }
	    }


       },
       type: 'GET'
    });

    $(document).ajaxStop(function(){
    	if(startPosition == 0){    		
    		$('#loadingDiv').hide();
    	}

	});

}
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
    }
};  

</script>
<script type="text/javascript">
  function getDateSpaceFormated(ts){
    var year = ts.substring(0, 4);
    var month = ts.substring(4, 6);
    month = Content.months[month];
    var day = ts.substring(6, 8);
    if(day.charAt(0) === '0' ){
      day = day.charAt(1);
    }
    return day + " "+ month + ", " +year;
  }  
</script>

<script>
/*Truncates large URL in the replay bar*/
function truncateUrl(url)
{   /*remove https and http*/
    if(url.substring(0, "https://".length) === "https://"){
      url = url.substring(8,url.length);
    }else if (url.substring(0, "http://".length) === "http://"){
      url = url.substring(7,url.length);
    }       
    if (url.length > 20){
            url = url.substring(0, 17) + "...";
            return url;
    }
    else
        return url
}
</script>

<script type="text/javascript">
	lastPosition = -1;
	function expandImage(position){
		if(lastPosition != -1){
			$('#testViewer'+lastPosition).fadeOut();
		}
		if(lastPosition === position){
			//you clicked twice in the same image, image closed no image selected
			lastPosition = -1;
			return;
		}
		$('#testViewer'+position).fadeIn();
		lastPosition = position;
		document.getElementById('imageResults'+position).scrollIntoView();
	}

</script>

<script type="text/javascript">
  function insertInPosition(position, imageObj, imageHeight, maxImageHeight, expandedImageHeight){

  	var maxImageExpandHeight = 400; //for now fix later
  	
  	if(expandedImageHeight > maxImageExpandHeight){
  		expandedImageHeight = maxImageExpandHeight;
  	}

    var centerImage;

  	var centerImage = maxImageExpandHeight/2 - expandedImageHeight/2 ;

    var liMarginTop = maxImageHeight - imageHeight;

    var contentToInsert = '<li position='+position+' id="imageResults'+position+'" style="background:white; margin-right: 1%; margin-bottom:1%; margin-top:'+liMarginTop.toString()+'px;"><h2><a onclick = "expandImage('+position+')" href ="javascript:void(0)"> <img style="padding:0px 0px 4px 0px;" height="'+imageHeight.toString()+'" src="'+imageObj.currentImageURL+'"/> </a></h2> <p class="green" style="font-size:1em!important; text-align: left; padding-left:5px">'+truncateUrl(imageObj.originalURL)+'</p> <p class="green" style="font-size:1em!important; text-align: left; padding-left:5px">'+getDateSpaceFormated(imageObj.timestamp)+'</p>'+
    	'<div id="testViewer'+position+'" class="imageExpandedDiv"><a href="javascript:void(0)" onclick = "expandImage('+position+')" class="expand__close"></a> <div style="width:70%"> <a target="_blank" href="http://arquivo.pt/wayback/'+imageObj.timestamp+'/'+imageObj.originalURL+'"> <img style="margin-top:'+ centerImage+'px; margin-bottom:'+ centerImage+'px"class="imageExpanded" id="ExpandedImg'+position+'" src="'+imageObj.currentImageURL+'"> </a> </div>  </div>'+ '</li>';     

    var lengthofUL = $('#resultsUl li').length;
    if(lengthofUL === 0){ /*list is empty only the hidden li*/
      $j('#resultsUl').prepend(contentToInsert)
    }
    else{
      var inserted = false;
      for (var i = 0 ; i< lengthofUL; i ++){
        var insertedPos = $j('#resultsUl li').eq(i).attr('position');
        if(position < insertedPos ){
          $j('#resultsUl li:eq('+i+')').before(contentToInsert);
          inserted = true;
          break;
        }
      }
      if(inserted === false){
        $j('#resultsUl').append(contentToInsert);
      }
    }
  }  

</script>


<script type="text/javascript" src="/js/js.cookie.js"></script>


  <%@ include file="include/topbar.jsp" %>

  <div class="wrap" id="firstWrap" style="min-height: 0">
    <div id="main">
      <div id="header" style="min-height: 0">
        <div id="logo">
    <a href="/index.jsp?l=pt" title="Ir para a página principal">
            <img src="/img/logo-pt.png" alt="Logo Arquivo.pt" width="125" height="90">
    </a>
</div>

        <div id="search-header">
            <form id="loginForm" action="images.jsp" name="imageSearchForm" method="get">
              <input type="hidden" name="l" value="<%= language %>" />
              <fieldset id="pesquisar">
                <label for="txtSearch">&nbsp;</label>
                <input class="search-inputtext" type="text" size="15"  value="<%=htmlQueryString%>" onfocus="" onblur="" name="query" id="txtSearch" accesskey="t" />
                <input type="reset" src="img/search-resetbutton.html" value="" alt="reset" class="search-resetbutton" name="btnReset" id="btnReset" accesskey="r" onclick="{document.getElementById('txtSearch').setAttribute('value','');}" />
                
                <button type="submit" value="<fmt:message key='search.submit'/>" alt="<fmt:message key='search.submit'/>" class="search-submit" name="btnSubmit" id="btnSubmit" accesskey="e" ><fmt:message key='search.submit'/></button>
                <a href="advancedImages.jsp?l=<%=language%>" onclick="{document.getElementById('pesquisa-avancada').setAttribute('href',document.getElementById('pesquisa-avancada').getAttribute('href')+'&query='+encodeHtmlEntity(document.getElementById('txtSearch').value))}" title="<fmt:message key='images.advancedSearch'/>" id="pesquisa-avancada"><fmt:message key='images.advancedSearch'/></a>
                <script type="text/javascript">
                  String.prototype.replaceAll = String.prototype.replaceAll || function(needle, replacement) {
                      return this.split(needle).join(replacement);
                  };
                </script>
                <script type="text/javascript">
                    function encodeHtmlEntity(str) {

                        str = str.replaceAll('ç','%26ccedil%3B')
                                 .replaceAll('Á','%26Aacute%3B')
                                 .replaceAll('á','%26aacute%3B')
                                 .replaceAll('À','%26Agrave%3B')
                                 .replaceAll('Â','%26Acirc%3B')
                                 .replaceAll('à','%26agrave%3B')
                                 .replaceAll('â','%26acirc%3B')
                                 .replaceAll('Ä','%26Auml%3B')
                                 .replaceAll('ä','%26auml%3B')
                                 .replaceAll('Ã','%26Atilde%3B')
                                 .replaceAll('ã','%26atilde%3B')
                                 .replaceAll('Å','%26Aring%3B')
                                 .replaceAll('å','%26aring%3B')
                                 .replaceAll('Æ','%26Aelig%3B')
                                 .replaceAll('æ','%26aelig%3B')
                                 .replaceAll('Ç','%26Ccedil%3B')
                                 .replaceAll('Ð','%26Eth%3B')
                                 .replaceAll('ð','%26eth%3B')
                                 .replaceAll('É','%26Eacute%3B')
                                 .replaceAll('é','%26eacute%3B')
                                 .replaceAll('È','%26Egrave%3B')
                                 .replaceAll('è','%26egrave%3B')
                                 .replaceAll('Ê','%26Ecirc%3B')
                                 .replaceAll('ê','%26ecirc%3B')
                                 .replaceAll('Ë','%26Euml%3B')
                                 .replaceAll('ë','%26euml%3B')
                                 .replaceAll('Í','%26Iacute%3B')
                                 .replaceAll('í','%26iacute%3B')
                                 .replaceAll('Ì','%26Igrave%3B')
                                 .replaceAll('ì','%26igrave%3B')
                                 .replaceAll('Î','%26Icirc%3B')
                                 .replaceAll('î','%26icirc%3B')
                                 .replaceAll('Ï','%26Iuml%3B')
                                 .replaceAll('ï','%26iuml%3B')
                                 .replaceAll('Ñ','%26Ntilde%3B')
                                 .replaceAll('ñ','%26ntilde%3B')
                                 .replaceAll('Ó','%26Oacute%3B')
                                 .replaceAll('ó','%26oacute%3B')
                                 .replaceAll('Ò','%26Ograve%3B')
                                 .replaceAll('ò','%26ograve%3B')
                                 .replaceAll('Ô','%26Ocirc%3B')
                                 .replaceAll('ô','%26ocirc%3B')
                                 .replaceAll('Ö','%26Ouml%3B')
                                 .replaceAll('ö','%26ouml%3B')
                                 .replaceAll('Õ','%26Otilde%3B')
                                 .replaceAll('õ','%26otilde%3B')
                                 .replaceAll('Ø','%26Oslash%3B')
                                 .replaceAll('ø','%26oslash%3B')
                                 .replaceAll('ß','%26szlig%3B')
                                 .replaceAll('Þ','%26Thorn%3B')
                                 .replaceAll('þ','%26thorn%3B')
                                 .replaceAll('Ú','%26Uacute%3B')
                                 .replaceAll('ú','%26uacute%3B')
                                 .replaceAll('Ù','%26Ugrave%3B')
                                 .replaceAll('ù','%26ugrave%3B')
                                 .replaceAll('Û','%26Ucirc%3B')
                                 .replaceAll('û','%26ucirc%3B')
                                 .replaceAll('Ü','%26Uuml%3B')
                                 .replaceAll('ü','%26uuml%3B')
                                 .replaceAll('Ý','%26Yacute%3B')
                                 .replaceAll('ý','%26yacute%3B')
                                 .replaceAll('ÿ','%26yuml%3B')
                                 .replaceAll('©','%26copy%3B')
                                 .replaceAll('®','%26reg%3B')
                                 .replaceAll('™','%26trade%3B')
                                 .replaceAll('&','%26amp%3B')
                                 .replaceAll('<','%26lt%3B')
                                 .replaceAll('>','%26gt%3B')
                                 .replaceAll('€','%26euro%3B')
                                 .replaceAll('¢','%26cent%3B')
                                 .replaceAll('£','%26pound%3B')
                                 .replaceAll('\"','%26quot%3B')
                                 .replaceAll('‘','%26lsquo%3B')
                                 .replaceAll('’','%26rsquo%3B')
                                 .replaceAll('“','%26ldquo%3B')
                                 .replaceAll('”','%26rdquo%3B')
                                 .replaceAll('«','%26laquo%3B')
                                 .replaceAll('»','%26raquo%3B')
                                 .replaceAll('—','%26mdash%3B')
                                 .replaceAll('–','%26ndash%3B')
                                 .replaceAll('°','%26deg%3B')
                                 .replaceAll('±','%26plusmn%3B')
                                 .replaceAll('¼','%26frac14%3B')
                                 .replaceAll('½','%26frac12%3B')
                                 .replaceAll('¾','%26frac34%3B')
                                 .replaceAll('×','%26times%3B')
                                 .replaceAll('÷','%26divide%3B')
                                 .replaceAll('α','%26alpha%3B')
                                 .replaceAll('β','%26beta%3B')
                                 .replaceAll('∞','%26infin%3B')
                                 .replaceAll(' ','+');


                        return str;
                    }
                </script>

              </fieldset>
              <fieldset id="search-date">
                <div id="search-label-data">
                  <label id="search-dateStart_top" for="dateStart_top"><fmt:message key='search.query-form.from'/></label>
                  <div class="search-withTip">
                    <input type="text" id="dateStart_top" name="dateStart" value="<%=dateStartString%>" />
                  </div>
                  <label id="search-labelDateEnd" for="dateEnd_top"><fmt:message key='search.query-form.to'/></label>
                  <div class="withTip">
                    <input type="text" id="dateEnd_top" name="dateEnd" value="<%=dateEndString%>" />
                  </div>
                </div>                
              </fieldset>

<div style="padding-top: 20px;">
	<label style="font-size: 1.2em"> Pesquisa Segura: </label>
    <select name ="safeSearch" id="safeSearch" style="font-size: 1.2em">
    <% if (safeSearchString ==null || safeSearchString.equals("")){ %>
    	<option name ="safeSearch" selected= "selected" value="yes">Mostrar Seguras</option>
    	<option name ="safeSearch" value="all">Mostrar Todas</option>
    	<option name ="safeSearch" value="no">Mostrar Inseguras</option>
    <% } else { %>

		<% if (safeSearchString.equals("yes")){%>
			<option name ="safeSearch" selected= "selected" value="yes">Mostrar Seguras</option>
		<% } else { %>
			<option name ="safeSearch" value="yes">Mostrar Seguras</option>
		<%} %>    
       <% if (safeSearchString.equals("all")){%>
       	<option name ="safeSearch"  selected= "selected" value="all">Mostrar Todas</option>
       <%} else{ %>
       	<option name ="safeSearch" value="all">Mostrar Todas</option>
       <%} %>
		<% if (safeSearchString.equals("no")){%>
			<option name ="safeSearch" selected= "selected" value="no">Mostrar Inseguras</option>
		<% } else { %>
			<option name ="safeSearch" value="no">Mostrar Inseguras</option>
		<%} %>	
	<%} %>
	</select>
</div>
                <!--<div>
                    <label id="search-dateStart_top" for="dateStart_top" style="padding-top: 20px!important"><fmt:message key='images.imageSize'/></label>
                    <div style="float: left; padding-right: 4px; padding-top: 6px; padding-bottom: 6px;margin-top: 14px; font-size: 1.2em; border: 1px solid #949494;">
                        <select id="imageSize" >
                          <option selected="selected" value="1"><fmt:message key='images.LargeAndMedium'/></option>
                          <option value="2"><fmt:message key='images.showAll'/></option>
                        </select>                
                    </div>    
                </div> -->              
            </form>
        </div>
      </div>      
  <!-- FIM #conteudo-resultado  --> 
    </div>
  </div>


<!-- List Results!-->
<div id="conteudo-resultado" style="width: 100%; max-width: 100%"> 
<div id="first-column" style="width: 100%">
  &nbsp;
</div>
<div id="second-column" style="width: 100%; background-color: #D8DBDF; padding-bottom: 10%">




<div id="resultados" style="width: 100%"></div>




<div class="spell hidden">Será que quis dizer: <span class="suggestion"></span></div>

<div id="loadingDiv" style="text-align: center; display: hidden; margin-top: 10%; margin-bottom: 5%" ><div style="text-align: center; display: inline-block;" class="cp-spinner cp-round"></div></div>
<div id="resultados-lista" style="text-align: center;">
    <ul id="resultsUl" style="list-style-type: none;  display: inline-block; margin-left: 2%; margin-right: 2%; ">
        <li id="imageResults" style="text-align: center"> <h3> <fmt:message key='images.prototype'/> </h3> </li>       
    </ul>
</div> 
<!-- FIM #resultados-lista  --> 

      </div>



 

  








              
                 

</div>
<%@include file="include/footerImages.jsp" %>
<script type="text/javascript">
        var _gaq = _gaq || [];
        _gaq.push(['_setAccount', 'UA-21825027-1']);
        _gaq.push(['_setDomainName', '.arquivo.pt']);
        _gaq.push(['_trackPageview']);
        (function() {
                var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
                ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
                var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
        })();
</script>
<script type="text/javascript"> if($('#txtSearch').val().length){doInitialSearch();}</script>
<div id="ui-datepicker-div" class="ui-datepicker ui-widget ui-widget-content ui-helper-clearfix ui-corner-all ui-helper-hidden-accessible"></div><div id="ui-datepicker-div" class="ui-datepicker ui-widget ui-widget-content ui-helper-clearfix ui-corner-all ui-helper-hidden-accessible"></div><div id="ui-datepicker-div" class="ui-datepicker ui-widget ui-widget-content ui-helper-clearfix ui-corner-all ui-helper-hidden-accessible"></div>

</body>
</html>
<%@include file="include/logging.jsp" %>
