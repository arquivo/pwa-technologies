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

	<link rel="shortcut icon" href="img/logo-16.png" type="image/x-icon" />
	<link rel="search" type="application/opensearchdescription+xml" title="<fmt:message key='opensearch.title'><fmt:param value='<%=language%>'/></fmt:message>" href="opensearch.jsp?l=<%=language%>" />
	<link rel="stylesheet" title="Estilo principal" type="text/css" href="css/newStyle.css?v=winners2019"  media="all" />
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
    <div class="container-fluid topcontainer">
        <div class="row">
            <div class="col-sm-10 col-sm-offset-1 col-md-8 col-md-offset-2 col-lg-6 col-lg-offset-3 text-right">
                <form id="searchForm" action="/search.jsp">
                <div id="form_container"> 
                    <div class="input-group stylish-input-group">
                        
                            <input name="query" id="txtSearch" type="search" class="form-control no-radius search-input" placeholder="<fmt:message key='home.search.placeholder'/>" autofocus autocapitalize="off" autocomplete="off" autocorrect="off">
                            <span class="clear-text"><i class="fa fa-close"></i></span>                            
                            <span class="input-group-addon no-radius search-button-span">
                                <button class="search-button" type="submit">
                                    <span class="glyphicon glyphicon-search white"></span>
                                </button>  
                            </span>
                        
                    </div>
                </div>
                <div id="slider-date" class="col-sm-12"></div>
                <div id="slider-caption" class="row">
                    <input size="4" maxlength="4" type="number" class="example-val text-center input-start-year" id="event-start" value="<%=dateStartYear%>" min="1996"  max="<%=dateEndYear%>"></input>
                    <input size="4" maxlength="4" type="number" class="example-val text-center input-end-year" id="event-end" value="<%=dateEndYear%>" min="1996" max="<%=dateEndYear%>"></input>
                    <input type="hidden" id="dateStart" name="dateStart" value="01/01/<%=dateStartYear%>"/>
                    <input type="hidden" id="dateEnd" name="dateEnd" value="31/12/<%=dateEndYear%>"/>
                    <input type="hidden" id="l" name="l" value="<%=language%>"/>
                </div>                  
                </form>
<script src="/include/clearForm.js"></script>      
<script type="text/javascript">
// Create a new date from a string, return as a timestamp.

var dateSlider = document.getElementById('slider-date');

var beginYear = parseInt("<%=dateStartYear%>");
var endYear = parseInt("<%=dateEndYear%>");
var minYear = 1996;
maxYear = (new Date()).getFullYear() - 1;

noUiSlider.create(dateSlider, {
// Create two timestamps to define a range.
    range: {
        min: [minYear],
        max: [maxYear]
    },
    tooltips:false,
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
    else  if(currentInputDateNumber > parseInt("<%=dateEndYear%>")  ){
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
    $('#event-end').val(parseInt("<%=dateEndYear%>"));
    dateSlider.noUiSlider.set([null , parseInt("<%=dateEndYear%>")]);
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
    if( (currentInputDate.length) === 4 && currentInputDateNumber <= parseInt("<%=dateEndYear%>") && currentInputDateNumber >= currentDateStartNumber ){ 
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
    else  if((currentInputDate.length) >= 4 && currentInputDateNumber > parseInt("<%=dateEndYear%>")  ){
     $('#event-end').val(parseInt("<%=dateEndYear%>")); 
     dateSlider.noUiSlider.set([null , parseInt("<%=dateEndYear%>")]);
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

initial = 0; /*do not show tooltips when slider is initialized i.e. when initial < 2*/
dateSlider.noUiSlider.on('update', function( values, handle ) {
	if(initial > 1){
    	$(".noUi-handle[data-handle='"+handle.toString()+"'] .noUi-tooltip").show().delay(1000).fadeOut();
    }
    else{
    	initial += 1;
    }
    if(handle==0){
     $('#dateStart').attr('value', '01/01/'+values[handle]);
     $('#event-start').attr('value', +values[handle]);
    }else{
     $('#dateEnd').attr('value', '31/12/'+values[handle]);
     $('#event-end').attr('value', +values[handle]);
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
<div class="text-center prizes horizontalMarginsAuto">
  <a href="<fmt:message key='home.prizes.url'/>"><img width="90%" alt="<fmt:message key='home.prizes.alt'/>" title="<fmt:message key='home.prizes.title'/>" src="/img/badge-premiosarquivo-<%=language%>.png?v=premiovencedores2019"/></a>
</div>
            </div>
        </div>

    </div>

<%@include file="include/analytics.jsp" %>
<%@include file="include/footer.jsp" %>
</body>
</html>

<%@include file="include/logging.jsp" %>
