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
  import="org.apache.commons.lang.StringEscapeUtils"
%>
<% // Set the character encoding to use when interpreting request values.
  request.setCharacterEncoding("UTF-8");
%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt_rt" prefix="fmt" %>
<%@ include file="include/logging_params.jsp" %>
<%@ include file="include/i18n.jsp" %>
<fmt:setLocale value="<%=language%>"/>

<%! //To please the compiler since logging need those -- check [search.jsp]
  private static int hitsTotal = -10;   // the value -10 will be used to mark as being "advanced search"
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
  boolean safe =true;
  boolean unsafe = false;
  String safeSearchString ="on";
  String type = ""; /*Default mimetype*/
  String size = "all"; /*Default image size*/
  String tools = "off"; /*Show toolbar*/
  if( request.getParameter("safeSearch") != null && request.getParameter("safeSearch").contains("off") ){
    safeSearchString = "off";
  }
  if ( request.getParameter("size") != null && request.getParameter("size") != "") {
          size = request.getParameter("size");
          if(! "sm".equals(size) && !"md".equals(size) && ! "lg".equals(size)){
            size = "all";
          }
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
    }
  //htmlQueryString= StringEscapeUtils.escapeHtml(htmlQueryString);
  request.setAttribute("htmlQueryString", htmlQueryString);

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

%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="pt-PT" lang="pt-PT"><head>
  <title><fmt:message key='images.imageTitle'/>:&nbsp; <c:out value = "${htmlQueryString}"/> &nbsp;  &mdash; Arquivo.pt</title>
  <meta http-equiv="Content-Type" content="application/xhtml+xml; charset=UTF-8"/>
  
  <meta http-equiv="Content-Language" content="pt-PT"/>
  <meta name="Keywords" content="resultado, pesquisa, buscar, arquivo, Web, português, portuguesa, Portugal"/>
  <meta name="Description" content="Página de resultados de uma pesquisa de imagens feita no Arquivo.pt."/>
  <link rel="shortcut icon" href="img/logo-16.png" type="image/x-icon"/>
  <link rel="search" type="application/opensearchdescription+xml" title="Arquivo.pt(pt)" href="opensearch.jsp?l=pt"/>
  <link rel="stylesheet" title="Estilo principal" type="text/css" href="css/style.css" media="all"/>
  <link href="css/csspin.css" rel="stylesheet" type="text/css"/>

  <script type="text/javascript" async="" src="//www.google-analytics.com/ga.js"></script>
  <script type="text/javascript">
                var minDate = new Date(820450800000);
                var maxDate = new Date(<%=DATE_END.getTimeInMillis()%>);
  </script>
  <script type="text/javascript">
    calendarBegin = '<fmt:message key="calendar.begin" />'.replace("calendario", "calendário");
    calendarEnd = '<fmt:message key="calendar.end" />'.replace("calendario", "calendário");
  </script>
  <script  src="/js/jquery-latest.min.js" type="text/javascript"></script>
  <link rel="stylesheet" type="text/css" href="css/jquery-ui-1.7.2.custom.css"/>
  <script type="text/javascript" src="https://code.jquery.com/ui/1.12.1/jquery-ui.js"></script>
  <script src='js/jquery.dialogOptions.js'></script>
  <script src="https://apis.google.com/js/client.js" type="text/javascript"> </script>
  <script type="text/javascript" src="js/ui.datepicker.js"></script>
  <script type="text/javascript" src="js/ui.datepicker-pt-BR.js"></script>
  <script type="text/javascript" src="js/imageConfigs.js"></script>
  <script type="text/javascript" src="js/images2.js"></script>
 
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
  <script type="text/javascript" src="//s7.addthis.com/js/300/addthis_widget.js#pubid=ra-5645cdb2e22ca317" async="async"/>
  <script type="text/javascript">
  /*Google Analytics*/
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

</head>
<body>
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
</script>

<script type="text/javascript" src="/js/js.cookie.js"></script>


  <%@ include file="include/topbar.jsp" %>

  <div class="wrap" id="firstWrap" style="min-height: 0">
    <div id="main">
      <div id="header" style="min-height: 0">
        <div id="logo">
        <a href="/index.jsp?l=pt" title="<fmt:message key='header.logo.link'/>">
                <img src="/img/logo-pt.png" alt="Logo Arquivo.pt" width="125" height="90">
        </a>
  </div>

        <div id="search-header">
            <form id="loginForm" action="images.jsp" name="imageSearchForm" method="get">
              <input type="hidden" name="l" value="<%= language %>" />
              <input id="sizeFormInput" type="hidden" name="size" value="<%=size%>" />
              <input id="typeFormInput" type="hidden" name="type" value="<%=type%>" />  
              <input id="toolsFormInput" type="hidden" name="tools" value="<%=tools%>" />  

              <input id="safeSearchFormInput" type="hidden" name="safeSearch" value="<%=safeSearchString%>" /> 



              <fieldset id="pesquisar">
                <label for="txtSearch">&nbsp;</label>
                <input class="search-inputtext" type="text" size="15"  value="<c:out value = "${htmlQueryString}"/>" onfocus="" onblur="" name="query" id="txtSearch" accesskey="t" />
                <input type="reset" src="img/search-resetbutton.html" value="" alt="reset" class="search-resetbutton" name="btnReset" id="btnReset" accesskey="r" onclick="{document.getElementById('txtSearch').setAttribute('value','');}" />
                
                <button type="submit" value="<fmt:message key='search.submit'/>" alt="<fmt:message key='search.submit'/>" class="search-submit" name="btnSubmit" id="btnSubmit" accesskey="e" ><fmt:message key='search.submit'/></button>
                <a href="advancedImages.jsp?l=<%=language%>" onclick="{document.getElementById('pesquisa-avancada').setAttribute('href',document.getElementById('pesquisa-avancada').getAttribute('href')+'&query='+encodeHtmlEntity(document.getElementById('txtSearch').value))}" title="<fmt:message key='images.advancedSearch'/>" id="pesquisa-avancada"><fmt:message key='images.advancedSearch'/></a>
                <script type="text/javascript">
                  String.prototype.replaceAll = String.prototype.replaceAll || function(needle, replacement) {
                      return this.split(needle).join(replacement);
                  };
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

<!--
<div style="padding-top: 20px;">
  <label style="font-size: 1.2em"> <fmt:message key='images.safeSearch'/> </label>
    <select name ="safeSearch" id="safeSearch" style="font-size: 1.2em">
    <% if (safeSearchString ==null || safeSearchString.equals("")){ %>
      <option name ="safeSearch" selected= "selected" value="yes"><fmt:message key='images.showSafe'/></option>
      <option name ="safeSearch" value="all"><fmt:message key='images.showAll'/></option>
      <option name ="safeSearch" value="no"><fmt:message key='images.showUnsafe'/></option>
    <% } else { %>

    <% if (safeSearchString.equals("yes")){%>
      <option name ="safeSearch" selected= "selected" value="yes"><fmt:message key='images.showSafe'/></option>
    <% } else { %>
      <option name ="safeSearch" value="yes"><fmt:message key='images.showSafe'/></option>
    <%} %>    
       <% if (safeSearchString.equals("all")){%>
        <option name ="safeSearch"  selected= "selected" value="all"><fmt:message key='images.showAll'/></option>
       <%} else{ %>
        <option name ="safeSearch" value="all"><fmt:message key='images.showAll'/></option>
       <%} %>
    <% if (safeSearchString.equals("no")){%>
      <option name ="safeSearch" selected= "selected" value="no"><fmt:message key='images.showUnsafe'/></option>
    <% } else { %>
      <option name ="safeSearch" value="no"><fmt:message key='images.showUnsafe'/></option>
    <%} %>  
  <%} %>
  </select>
</div>
-->         
            </form>
        </div>
      </div> 
      <div id="conteudo-resultado"> 
        <div id="first-column">
          &nbsp;
        </div>
        <div id="second-column">        
        <div id="resultados">
          <script type="text/javascript">
            document.write('<a href="/search.jsp?l=<%=language%>&query='+"<c:out value = "${htmlQueryString}"/>"+'&dateStart='+$('#dateStart_top').attr("value")+'&dateEnd='+$('#dateEnd_top').attr("value")+'" class="search-anchor">Web</a>');
          </script>
           <span  class="image-span" href="/images.jsp"><em><fmt:message key='images.images'/></em></span>
           <div class="fright">
             <a class="tools-anchor">Ferramentas</a>
          </div>  
        </div> 
          <div id ="tools">
              <select id="sizeSelect" class="safe-search" >
                <option selected value="all" class="safe-search-option"><fmt:message key='images.tools.size'/></option> 
                <option value="sm" class="safe-search-option"><fmt:message key='images.tools.sm'/></option>
                <option value="md" class="safe-search-option"><fmt:message key='images.tools.md'/></option>
                <option value="lg" class="safe-search-option"><fmt:message key='images.tools.lg'/></option>                 
              </select>
              <select id="typeSelect" class="safe-search" >
                <option selected value="" class="safe-search-option"><fmt:message key='images.tools.format'/></option> 
                <option value="jpg" class="safe-search-option">JPEG</option>
                <option value="png" class="safe-search-option">PNG</option>
                <option value="gif" class="safe-search-option">GIF</option>
                <option value="bmp" class="safe-search-option">BMP</option>
                <option value="webp" class="safe-search-option">WEBP</option>
              </select>              
              <select id="safeSearch" class="safe-search" >
              <% if (safeSearchString.equals("on")) { %>                
                <option selected value="on" class="safe-search-option"><fmt:message key='images.safeOnLabel'/></option>
                <option value="off" class="safe-search-option"><fmt:message key='images.safeOffLabel'/></option>
              <%} else {%>
                <option selected value="off" class="safe-search-option"><fmt:message key='images.safeOffLabel'/></option>
                <option  value="on" class="safe-search-option"><fmt:message key='images.safeOnLabel'/></option>
              <%}%>                              
              </select>
              <select id="width_tmp_select">
                <option class="safe-search-option" id="width_tmp_option"></option>
              </select>              
            <script type="text/javascript">
            if($('#toolsFormInput').attr('value') === 'on'){
              $('#tools').show();  
            }
            $("#sizeSelect").val('<%=size%>');
            if($('#sizeSelect option:selected').text() !== ''){
              $("#width_tmp_option").html($('#sizeSelect option:selected').text()); 
              if($("#width_tmp_select").width() > 0 )
                $("#sizeSelect").width($("#width_tmp_select").width()+25);
            }
            $("#width_tmp_option").html($('#safeSearch option:selected').text());
            if($("#width_tmp_select").width() > 0 ) 
              $("#safeSearch").width($("#width_tmp_select").width()+43);  
            if('<%=type%>'!==''){
              $("#typeSelect").val('<%=type%>');
              $("#width_tmp_option").html($('#typeSelect option:selected').text()); 
              if($("#width_tmp_select").width() > 0 )
                $("#typeSelect").width($("#width_tmp_select").width()+18);              
            }
            $('.tools-anchor').on('click', function(e) {
              $("#tools").slideToggle('slow', function(){
                if($("#tools").is(":visible")){
                   $('#toolsFormInput').attr('value', 'on');
                }
                else{
                  $('#toolsFormInput').attr('value', 'off');
                }
              });            
            });
            $( "#sizeSelect" ).change(function() {
              $('#sizeFormInput').attr('value', $('#sizeSelect').find(":selected").attr("value"));
              $("#width_tmp_option").html($('#sizeSelect option:selected').text()); 
              $(this).width($("#width_tmp_select").width()+25);                
              $('#btnSubmit').click();
            });
            $( "#typeSelect" ).change(function() {
              $('#typeFormInput').attr('value', $('#typeSelect').find(":selected").attr("value"));
              $("#width_tmp_option").html($('#typeSelect option:selected').text()); 
              $(this).width($("#width_tmp_select").width()+18);                 
              $('#btnSubmit').click();
            });                                        
            $( "#safeSearch" ).change(function() {
              $('#safeSearchFormInput').attr('value', $('#safeSearch').find(":selected").attr("value"));
              $("#width_tmp_option").html($('#safeSearch option:selected').text()); 
              $(this).width($("#width_tmp_select").width()+43);              
              $('#btnSubmit').click();
            });              
            </script>            
          </div>            
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
<img src="/img/experimental.png" class="fleft">  



<div id="resultados" style="width: 100%" class="no-border"></div>




<div class="spell hidden"><fmt:message key='search.spellchecker'/> <span class="suggestion"></span></div>

<div id="loadingDiv" style="text-align: center; display: hidden; margin-top: 10%; margin-bottom: 5%" ><div class="sk-fading-circle"><div class="sk-circle1 sk-circle"></div><div class="sk-circle2 sk-circle"></div><div class="sk-circle3 sk-circle"></div><div class="sk-circle4 sk-circle"></div><div class="sk-circle5 sk-circle"></div><div class="sk-circle6 sk-circle"></div><div class="sk-circle7 sk-circle"></div><div class="sk-circle8 sk-circle"></div><div class="sk-circle9 sk-circle"></div><div class="sk-circle10 sk-circle"></div><div class="sk-circle11 sk-circle"></div><div class="sk-circle12 sk-circle"></div></div></div>
<div id="resultados-lista" style="text-align: center;">
    <ul id="resultsUl" style="list-style-type: none;  display: inline-block; margin-left: 2%; margin-right: 2%; ">
        <li id="imageResults" style="text-align: center"> <h3> <fmt:message key='images.prototype'/> </h3> </li>       
    </ul>
</div> 
<!-- FIM #resultados-lista  --> 

      </div>

</div>
<style>
#newFooterWrapper{
  padding-top:0px!important;
}
</style>
<%@include file="include/footer.jsp" %>
<script type="text/javascript"> if($('#txtSearch').val().length){doInitialSearch();}</script>
<div id="ui-datepicker-div" class="ui-datepicker ui-widget ui-widget-content ui-helper-clearfix ui-corner-all ui-helper-hidden-accessible"></div><div id="ui-datepicker-div" class="ui-datepicker ui-widget ui-widget-content ui-helper-clearfix ui-corner-all ui-helper-hidden-accessible"></div><div id="ui-datepicker-div" class="ui-datepicker ui-widget ui-widget-content ui-helper-clearfix ui-corner-all ui-helper-hidden-accessible"></div>

  <script  src="/js/clipboard.min.js" type="text/javascript"></script>
  <script type="text/javascript">
    var linkCopied =   '<fmt:message key="images.linkCopied"/>';
    initClipboard(linkCopied);
  </script>  



  <!-- Share Popup -->
  <div id="dialog"  class="content_dialog">
          <h1 style="color:black; padding-top: 10px;"><fmt:message key="images.share" /></h1>
          <button id="dialogClose" href="" class="expand__close__mini" title="Fechar"></button>

          <a class="addthis_button_facebook" style="text-decoration: none;"><h2 style="color:black; padding-top: 30px;"> <img width="40px" style="vertical-align: middle" src="/img/FB-f-Logo__blue_144.png"> <span style="padding-left: 10px;">Facebook</span></h2></a>
          <a class="addthis_button_twitter" style="text-decoration: none;"><h2 style="color:black; padding-top: 30px;"> <img width="40px" style="vertical-align: middle" src="/img/Twitter_Logo_White_On_Blue.png"> <span style="padding-left: 10px;">Twitter</span></h2></a>

          <button data-clipboard-target="#shortURL" id="btnCopy"><h2 style="color:grey; padding-top: 40px;" id="h2Copy"  ><fmt:message key="images.clickToCopy" /></h2></button>
          <h2 id="shortURL" style="padding-top: 10px;padding-bottom: 30px;"> </h2>
  </div>
  <div id="detailsDialog"  class="content_dialog">
          <h1 style="color:black; padding-top: 10px;"><fmt:message key="images.details.title" /></h1>
          <button id="detailsDialogClose" href="" class="expand__close__mini" title="Fechar"></button>
          <h2 id="imagePage" style="color:black; padding-top: 30px;"><fmt:message key="images.page" /></h2>
          <div id="imageDetailPageElements"></div> 
          <h2 id="imageImage" style="color:black; padding-top: 15px;"><fmt:message key="images.image" /></h2>
          <div id="imageDetailImageElements"></div>    
          <h2 id="imageDetailCollection" style="color:black; padding-top: 15px;"><fmt:message key="images.collection" /></h2>
          <div id="imageDetailCollectionElements" style="padding-bottom: 20px;"></div>                  
  </div>  
</body>
</html>
<%@include file="include/logging.jsp" %>
