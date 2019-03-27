<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd" >
<%@ page
	session="true"
	contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"	

	import="java.io.File"
	import="java.net.URLDecoder"
	import="java.util.Calendar"
	import="java.util.Date"
	import="java.util.GregorianCalendar"
	import="java.util.regex.Pattern"
	import="java.util.regex.Matcher"
%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt_rt" prefix="fmt" %>
<%@ include file="include/logging_params.jsp" %>
<%@ include file="include/i18n.jsp" %>
<%@ include file="include/simple-params-processing.jsp" %>
<fmt:setLocale value="<%=language%>"/>

<%!	//To please the compiler since logging need those -- check [search.jsp]
	private static int hitsTotal = -10;		// the value -10 will be used to mark as being "advanced search"
	private static Calendar DATE_START = new GregorianCalendar(1996, 1-1, 1);
	private static final Pattern OFFSET_PARAMETER = Pattern.compile("(\\d{4})-(\\d{2})-(\\d{2})");
%>
<%
	SimpleDateFormat paramDateFormat = new SimpleDateFormat("dd/mm/yyyy");
	Calendar dateStart = new GregorianCalendar( );
	dateStart.setTimeInMillis( paramDateFormat.parse(dateStartString).getTime() );
	Calendar dateEnd = new GregorianCalendar();
	dateEnd.setTimeInMillis( paramDateFormat.parse(dateEndString).getTime() );
%>
<%-- Define the default end date --%>
<%
  Calendar DATE_END = new GregorianCalendar();
  DATE_END.set( Calendar.YEAR, DATE_END.get(Calendar.YEAR) );
  DATE_END.set( Calendar.MONTH, 12-1 );
  DATE_END.set( Calendar.DAY_OF_MONTH, 10 );
  DATE_END.set( Calendar.HOUR_OF_DAY, 23 );
  DATE_END.set( Calendar.MINUTE, 59 );
  DATE_END.set( Calendar.SECOND, 59 );

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
  } catch(NullPointerException e) {
        // Set the default embargo period to: 1 year
        DATE_END.set( Calendar.YEAR, DATE_END.get(Calendar.YEAR) - 1);
  }
%>

<%---------------------- Start of HTML ---------------------------%>

<%-- TODO: define XML lang --%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="pt-PT" lang="pt-PT">
<head>
	<title><fmt:message key='advanced.meta.title'/></title>
	<meta http-equiv="Content-Type" content="application/xhtml+xml; charset=UTF-8" />
	<%-- TODO: define META lang --%>
	<meta name="viewport" content="width=device-width, initial-scale=1">
	<meta http-equiv="Content-Language" content="pt-PT" />
	<meta name="Keywords" content="<fmt:message key='advanced.meta.keywords'/>" />
	<meta name="Description" content="<fmt:message key='advanced.meta.description'/>" />
	<link rel="shortcut icon" href="img/logo-16.png" type="image/x-icon" />
	<meta name="theme-color" content="#252525">
    <!-- Windows Phone -->
    <meta name="msapplication-navbutton-color" content="#252525">
    <!-- iOS Safari -->
    <meta name="apple-mobile-web-app-status-bar-style" content="#252525">  	
	<script type="text/javascript">
		var minDate = new Date(<%=DATE_START.getTimeInMillis()%>);
		var maxDate = new Date(<%=DATE_END.getTimeInMillis()%>);
	</script>
	<link rel="stylesheet" title="Estilo principal" type="text/css" href="css/newStyle.css"  media="all" />
    <!-- font awesome -->
    <link rel="stylesheet" href="css/font-awesome.min.css">
    <!-- bootstrap -->
    <link rel="stylesheet" href="/css/bootstrap.min.css">
    <script src="/js/jquery-latest.min.js"></script>
    <script src="/js/bootstrap.min.js"></script>
    <!-- cookies for language selection -->
    <script type="text/javascript" src="/js/js.cookie.js"></script>
    <!-- dual slider dependencies -->
    <script type="text/javascript" src="/js/nouislider.min.js"></script>
    <link rel="stylesheet" href="/css/nouislider.min.css">
    <script type="text/javascript" src="/js/wNumb.js"></script>
    <!-- left menu dependencies -->
    <link rel="stylesheet" href="css/leftmenu.css">
    <!-- end left menu dependencies -->    
	<script type="text/javascript" src="js/configs.js"></script>
    <!--Includes mobiscroll (calendars for advanced search)-->
	<link href="css/mobiscroll.custom-2.6.2.min.css" rel="stylesheet" type="text/css" />
	<script src="js/mobiscroll.custom-2.6.2.min.js" type="text/javascript"></script>
    <script type="text/javascript">
    /*Initialization of Datepickers datestart and dateend for the advanced search*/
        $(function () {
        	var currDate = new Date();
            var curr = currDate.getFullYear();
            var opt = {}
            opt.date = {preset : 'date'};

      
            $('#dateStart_top').val('01/01/1996').scroller('destroy').scroller($.extend(opt["date"], { 
            	theme: "android-ics light",
            	dateFormat: 'dd/mm/yy', 
            	dateOrder: 'dMyy' ,
            	startYear: 1996 , 
            	endYear: currDate.getFullYear()-1,            	            
            	monthNamesShort : ['<fmt:message key="smonth.0" />'.toLowerCase(), 
            	                   '<fmt:message key="smonth.1" />'.toLowerCase(),
            	                   '<fmt:message key="smonth.2" />'.toLowerCase(),
            	                   '<fmt:message key="smonth.3" />'.toLowerCase(),
            	                   '<fmt:message key="smonth.4" />'.toLowerCase(), 
            	                   '<fmt:message key="smonth.5" />'.toLowerCase(),
            	                   '<fmt:message key="smonth.6" />'.toLowerCase(), 
            	                   '<fmt:message key="smonth.7" />'.toLowerCase(),
            	                   '<fmt:message key="smonth.8" />'.toLowerCase(),
            	                   '<fmt:message key="smonth.9" />'.toLowerCase(),
            	                   '<fmt:message key="smonth.10" />'.toLowerCase(), 
            	                   '<fmt:message key="smonth.11" />'.toLowerCase()],
            	mode: "scroller" , display: "modal", lang: '<fmt:message key="advanced.datepicker.lang" />' 
            }));

            $('#dateEnd_top').val('31/12/'+(currDate.getFullYear()-1).toString()).scroller('destroy').scroller($.extend(opt["date"], { 
            	theme: "android-ics light",
            	dateFormat: 'dd/mm/yy', 
            	dateOrder: 'dMyy' ,
            	startYear: 1996 , 
            	endYear: (new Date()).getFullYear()-1,            	            
            	monthNamesShort : ['<fmt:message key="smonth.0" />'.toLowerCase(), 
            	                   '<fmt:message key="smonth.1" />'.toLowerCase(),
            	                   '<fmt:message key="smonth.2" />'.toLowerCase(),
            	                   '<fmt:message key="smonth.3" />'.toLowerCase(),
            	                   '<fmt:message key="smonth.4" />'.toLowerCase(), 
            	                   '<fmt:message key="smonth.5" />'.toLowerCase(),
            	                   '<fmt:message key="smonth.6" />'.toLowerCase(), 
            	                   '<fmt:message key="smonth.7" />'.toLowerCase(),
            	                   '<fmt:message key="smonth.8" />'.toLowerCase(),
            	                   '<fmt:message key="smonth.9" />'.toLowerCase(),
            	                   '<fmt:message key="smonth.10" />'.toLowerCase(), 
            	                   '<fmt:message key="smonth.11" />'.toLowerCase()],
            	mode: "scroller" , display: "modal", lang: '<fmt:message key="advanced.datepicker.lang" />' 
            }));
        });
    </script>
    <script type="text/javascript" src="//s7.addthis.com/js/300/addthis_widget.js#pubid=ra-5645cdb2e22ca317"></script> 
<!-- end addthis for sharing on social media --> 	
</head>
<body>
	<%@ include file="include/topbar.jsp" %>
	<div class="wrap">
    <div class="container-fluid topcontainer col-sm-offset-1 col-sm-10 col-md-offset-2 col-md-8 col-lg-offset-3 col-lg-6 col-xl-offset-4 col-xl-4 " id="headerSearchDiv" >
		<div id="info-texto-termos" class="row">
		</div>  
		<!-- Formulario -->
		<div id="main" class="main-form-advanced">
			<div id="conteudo-pesquisa">
				<form method="get" action="images.jsp">
					<input type="hidden" name="l" value="<%= language %>" />
		            <div class="expandable-div">   
						<fieldset id="words">
							<legend><fmt:message key='advanced.terms'/><i class="fa iCarret yearCarret fa-caret-down pull-right right-15" aria-hidden="true"></i></legend>
							<div class="box-content container-fluid hidden">
								<div id="label-palavras-1">
									<label for="adv_and" class="row  col-xs-12 no-padding-left label-padding-top"><fmt:message key='advanced.terms.all'/></label>
									<div class="withTip ">
										<input type="text" id="adv_and" class="row  col-xs-10" name="adv_and" value="<%=and.toString()%>" />
										<div class="row  col-xs-10 no-padding-left">
											<span class="tip"><fmt:message key='advanced.terms.all.hint'/></span>
										</div>										
									</div>
								</div>

								<div id="label-palavras-2">
									<label class="row  col-xs-12 no-padding-left label-padding-top" for="adv_phr"><fmt:message key='advanced.terms.phrase'/></label>
									<div class="withTip">
										<input type="text" class="row  col-xs-10" id="adv_phr" name="adv_phr" value="<%=phrase.toString()%>" />
										<div class="row  col-xs-10 no-padding-left">
											<span class="tip"><fmt:message key='advanced.terms.phrase.hint'/></span>
										</div>											
									</div>
								</div>

								<div id="label-palavras-3">
									<label class="row  col-xs-12 no-padding-left label-padding-top" for="adv_not"><fmt:message key='advanced.terms.not'/></label>
									<div class="withTip">
										<input type="text" class="row  col-xs-10" id="adv_not" name="adv_not" value="<%=not.toString()%>" />
										<div class="row  col-xs-10 no-padding-left">
											<span class="tip"><fmt:message key='advanced.terms.not.hint'/></span>
										</div>	
									</div>
								</div>
							</div>
						</fieldset>
					</div>	

					<div class="expandable-div">
						<fieldset id="date">
							<legend><fmt:message key='advanced.date'/><i class="fa iCarret yearCarret fa-caret-down pull-right right-15" aria-hidden="true"></i></legend>
							<div class="box-content container-fluid hidden">
								<div id="label-data-1">

									<label class="row  col-xs-12 no-padding-left label-padding-top" for="dateStart_top"><fmt:message key='advanced.date.from'/></label>
									<div class="withTip">
										<input size="10" class="row  date-advanced no-padding-left" type="text" id="dateStart_top" name="dateStart" value="<%=dateStartString%>" /><a class="calendar-anchor-advanced" id="startDateCalendarAnchor"><img src="/img/calendar.gif"/></a>
									</div>

									<label id="labelDateEnd" class="row  col-xs-12 no-padding-left label-padding-top" for="dateEnd_top"><fmt:message key='advanced.date.to'/></label>
									<div class="withTip">
										<input type="text" class="row  date-advanced no-padding-left" id="dateEnd_top" name="dateEnd" size="10" value="<%=dateEndString%>" /><a class="calendar-anchor-advanced" id="endDateCalendarAnchor"><img src="/img/calendar.gif"/></a>
									</div>
								</div>
							</div>
						</fieldset>
					</div>	
					<div class="expandable-div">
						<legend><fmt:message key='advancedImages.size'/><i class="fa iCarret yearCarret fa-caret-down pull-right right-15" aria-hidden="true"></i></legend>
							<div class="box-content container-fluid hidden">
								<div id="label-format-1">
									<label class="row  col-xs-12 no-padding-left label-padding-top" for="formatType"><fmt:message key='advanced.format.label'/></label>
									<select id="formatType" name="type" class="row  col-xs-10 no-padding-left select-style">
									<%
										String[] mimeList = {"jpg", "png", "gif", "bmp", "webp"};
										String[] mimeListDetail = {"Joint Photographic Experts Group (.jpeg)", "Portable Network Graphics (.png)", "Graphics Interchange Format (.gif)", "Bitmap Image File (.bmp)", "WEBP (.webp)"};

										if (format == null || "all".equals(format)) {%>
											<option value="all" selected="selected"><fmt:message key='advanced.format.all'/></option>
										<%} else {%>
											<option value="all"><fmt:message key='advanced.format.all'/></option>
										<%}

										for (int i=0; i < mimeList.length; i++) {
											if (mimeList[i].equals(format)) {
												out.print("<option value=\""+ mimeList[i] +"\" selected=\"selected\">"+ mimeListDetail[i] +"</option>");
											} else {
												out.print("<option value=\""+ mimeList[i] +"\">"+ mimeListDetail[i] +"</option>");
											}
										}
									%>
									</select>
									<label for="size" class="row  col-xs-12 no-padding-left label-padding-top"><fmt:message key='images.size'/></label>
									<select id="size" name="size" class="row  col-xs-10 no-padding-left select-style">
										<option value="all" selected="selected"><fmt:message key='images.safeOffLabel'/></option>
										<option value="sm"><fmt:message key='images.tools.sm'/></option>
										<option value="md"><fmt:message key='images.tools.md'/></option>
										<option value="lg"><fmt:message key='images.tools.lg'/></option>
									</select>
									<label for="safeSearch" class="row  col-xs-12 no-padding-left label-padding-top"><fmt:message key='images.safeSearch'/></label>
									<select id="safeSearch" name="safeSearch" class="row  col-xs-10 no-padding-left select-style">
										<option value="on" selected="selected"><fmt:message key='images.safeOnLabel'/></option>
										<option value="off"><fmt:message key='images.safeOffLabel'/></option>
									</select>										

								</div>							
							</div>						

					</div>
					<div class="expandable-div">
						<fieldset id="domains">
							<legend><fmt:message key='advanced.website'/><i class="fa iCarret yearCarret fa-caret-down pull-right right-15" aria-hidden="true"></i></legend>
							<div class="box-content container-fluid hidden">
								<div id="label-domains-1">
									<label class="row  col-xs-12 no-padding-left label-padding-top" for="site"><fmt:message key='advanced.website.label'/></label>
									<div class="withTip">
										<input class="row  col-xs-10 no-padding-left" type="text" id="site" name="site" value="<%=site%>" />
										<span class="row  col-xs-10 no-padding-left tip"><fmt:message key='advanced.website.hint'/></span>
									</div>
								</div>
							</div>
						</fieldset>
					</div>	

					<div id="bottom-submit" class="text-center button-advanced">
						<button type="submit" value="<fmt:message key='advanced.submit'/>" alt="<fmt:message key='advanced.submit'/>" class="myButtonStyle col-xs-offset-3 col-xs-6" name="btnSubmitBottom" id="btnSubmitBottom" accesskey="e" >
						<fmt:message key='advanced.search'/>
						<span class="glyphicon glyphicon-search padding-left-5"></span>
						</button>
					</div>


				</form>
                        </div>
                </div>		
		<!-- Fim formulário -->      
    </div>    

<script>
$(".expandable-div legend").click(function() {
    $(this).children("i").toggleClass('fa-caret-up fa-caret-down');
    $(this).next().toggleClass("hidden");
});
</script>

<%-- end copy --%>
	</div>
<script>
	$('#startDateCalendarAnchor').click( function(e) {
	  e.preventDefault();
	  $('#dateStart_top').trigger('click');
	});    
</script>		
<script>
	$('#endDateCalendarAnchor').click( function(e) {
	  e.preventDefault();
	  $('#dateEnd_top').trigger('click');
	});    
</script>
<%@ include file="include/footer.jsp" %>
<%@include file="include/analytics.jsp" %>
</body>
</html>

<%@include file="include/logging.jsp" %>
