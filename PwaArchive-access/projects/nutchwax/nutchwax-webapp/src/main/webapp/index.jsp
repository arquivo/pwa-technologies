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
	private static int hitsTotal = -10;		// the value -10 will be used to mark as being "advanced search"
	private static Calendar DATE_START = new GregorianCalendar(1996, 1-1, 1);
    Calendar dateStart = (Calendar)DATE_START.clone();
    SimpleDateFormat inputDateFormatter = new SimpleDateFormat("dd/MM/yyyy");
	/*private static Calendar dateStart = new GregorianCalendar();*/
	private static Calendar dateEnd = new GregorianCalendar();
    String dateStartString = inputDateFormatter.format( dateStart.getTime() );
    String dateStartYear = dateStartString.substring(dateStartString.length()-4);


   /* dateEnd.set( Calendar.YEAR, dateEnd.get(Calendar.YEAR) - 1);*/
    /*DATE_END.set( Calendar.YEAR, DATE_END.get(Calendar.YEAR) - 1);*/
    String dateEndNoParameter =""+ (Calendar.getInstance().get(Calendar.YEAR) - 1);
    String yearEndNoParameter =dateEndNoParameter.substring(dateEndNoParameter.length()-4);
    String yearStartNoParameter = "1996";    


    String dateEndString = inputDateFormatter.format( dateEnd.getTime() );
    String dateEndYear = dateEndString.substring(dateEndString.length()-4);

%>

<%-- Get the application beans --%>
<%
  Configuration nutchConf = NutchwaxConfiguration.getConfiguration(application);
  NutchBean bean = NutchwaxBean.get(application, nutchConf);
%>


<%---------------------- Start of HTML ---------------------------%>

<%-- TODO: define XML lang --%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="pt-PT" lang="pt-PT">
<head>
  <%@ include file="include/checkDesktop.jsp" %>
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
    <meta property="og:image" content="http://<%=arquivoHostName%>/img/logoFace.png"/>

	<link rel="shortcut icon" href="img/logo-16.jpg" type="image/x-icon" />
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


</head>
<body>
    <%@ include file="include/topbar.jsp" %>
    <div class="container-fluid topcontainer">
        <div class="row">
            <div class="col-xs-8 col-xs-offset-2 col-lg-6 col-lg-offset-3 ">
                <img src="/img/logo-home-pt_nospaces.png" alt="Logo Arquivo.pt" class="img-responsive center-block" />
            </div>
        </div>
        <div class="row">
            <div class="col-sm-10 col-sm-offset-1 col-md-8 col-md-offset-2 col-lg-6 col-lg-offset-3 text-right">
                <form id="searchForm" action="/search.jsp">
                <div id="form_container"> 
                    <div class="input-group stylish-input-group">
                        
                            <input name="query" type="search" class="form-control no-radius" placeholder="<fmt:message key='home.search.placeholder'/>" autofocus autocapitalize="off" autocomplete="off" autocorrect="off">
                            <span class="input-group-addon no-radius">
                                <button type="submit">
                                    <span class="glyphicon glyphicon-search"></span>
                                </button>  
                            </span>
                        
                    </div>
                </div>
                <div id="slider-date" class="col-sm-12"></div>
                <div id="slider-caption" class="row">
                    <input size="4" maxlength="4" type="number" class="example-val text-center input-start-year text-bold" id="event-start" value="<%=dateStartYear%>" min="1996"  max="<%=yearEndNoParameter%>"></input>
                    <input size="4" maxlength="4" type="number" class="example-val text-center text-bold input-end-year" id="event-end" value="<%=dateEndYear%>" min="1996" max="<%=yearEndNoParameter%>"></input>
                    <input type="hidden" id="dateStart" name="dateStart" value="01/01/<%=dateStartYear%>"/>
                    <input type="hidden" id="dateEnd" name="dateEnd" value="31/12/<%=dateEndYear%>"/>
                </div>                  
                </form>

<script type="text/javascript">
    $('#searchForm').submit(function() 
    {
        if ($.trim($(".form-control").val()) === "") {
            /*TODO:: Do something when user enters empty input?*/
        return false;
        }
    });    
</script>
  
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
    tooltips:true,
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
            </div>
        </div>

    </div>
    <div class="container-fluid">
            <div class="row">
                <div class="col-xs-12 text-center no-padding-horizontal" >
                    <h4><fmt:message key='home.examples.preserved'/></title></h4>
                </div>    
            </div>   
    </div>
    <div class="container-fluid carousel-container no-padding-horizontal" id="container-carousel"> 
        <div class="col-xs-12 col-sm-6 col-sm-offset-3 no-padding-horizontal" >
          <div id="my-slider" class="carousel slide" data-ride="carousel" data-interval="false">
              <!-- Indicators -->
              <!--
              <ol class="carousel-indicators">
                <li data-target="#myCarousel" data-slide-to="0" class="active"></li>
                <li data-target="#myCarousel" data-slide-to="1"></li>
                <li data-target="#myCarousel" data-slide-to="2"></li>
              </ol>
              -->          
            <!-- Wrapper for slides -->
            <div class="carousel-inner center-block" role="listbox">
              <div class="item active">
                <a onclick="ga('send', 'event', 'Homepage', 'Ronaldo', 'Click on link (Exemplos - Ronaldo bola de ouro)');" href="/wayback/20091219061621/http://jogodirecto.blogspot.com/2008/12/ronaldo-ballon-dor.html"><img src="/img/ronaldo2008-e1487678911859.jpg" alt="<fmt:message key='home.examples.ronaldo'/>" class="img-responsive center-block"></a>
                  <div class="text-center">
                        <a onclick="ga('send', 'event', 'Homepage', 'Ronaldo', 'Click on link (Exemplos - Ronaldo bola de ouro)');" href="/wayback/20091219061621/http://jogodirecto.blogspot.com/2008/12/ronaldo-ballon-dor.html"><h5><fmt:message key='home.examples.ronaldo'/></h5></a>
                  </div>
              </div>
            <div class="item">
              <a onclick="ga('send', 'event', 'Homepage', 'Euro2008', 'Click on link (Exemplos - Euro 2008)');" href="/wayback/20081023211542tf_/http://euro2008.publico.clix.pt/noticia.aspx?id=1333952"><img src="/img/espanhaEuro2008-e1488898061641.jpg" class="img-responsive center-block" alt="<fmt:message key='home.examples.spain'/>"></a>
                <div class="text-center">
                  <a onclick="ga('send', 'event', 'Homepage', 'Euro2008', 'Click on link (Exemplos - Euro 2008)');" href="/wayback/20081023211542tf_/http://euro2008.publico.clix.pt/noticia.aspx?id=1333952"><h5><fmt:message key='home.examples.spain'/></h5></a>
                </div>
            </div>
            <div class="item">
              <a onclick="ga('send', 'event', 'Homepage', 'Vasco Ribeiro', 'Click on link (Exemplos - Vasco Ribeiro)');" href="/wayback/20141029233410/http://www.ionline.pt/artigos/surf/vasco-ribeiro-campeao-mundial-surf-juniores"><img src="/img/vasco-12-ok-e1489775252953.jpg" class="img-responsive center-block" alt="<fmt:message key='home.examples.vasco'/>"></a>
                <div class="text-center">
                  <a onclick="ga('send', 'event', 'Homepage', 'Vasco Ribeiro', 'Click on link (Exemplos - Vasco Ribeiro)');" href="/wayback/20141029233410/http://www.ionline.pt/artigos/surf/vasco-ribeiro-campeao-mundial-surf-juniores"><h5><fmt:message key='home.examples.vasco'/></h5></a>
                </div>
            </div> 
            <div class="item">
              <a onclick="ga('send', 'event', 'Homepage', 'Portugal Tenis Mesa', 'Click on link (Exemplos - Portugal Tenis Mesa)');" href="/wayback/20150101012504/http://www.maisfutebol.iol.pt/modalidades/outros-desportos/ultima-hora-portugal-e-campeao-da-europa-de-tenis-de-mesa"><img src="/img/portugalTenisMesa.png" class="img-responsive center-block" alt="<fmt:message key='home.examples.tableTennis'/>"></a>
                <div class="text-center">
                  <a onclick="ga('send', 'event', 'Homepage', 'Portugal Tenis Mesa', 'Click on link (Exemplos - Portugal Tenis Mesa)');" href="/wayback/20150101012504/http://www.maisfutebol.iol.pt/modalidades/outros-desportos/ultima-hora-portugal-e-campeao-da-europa-de-tenis-de-mesa"><h5><fmt:message key='home.examples.tableTennis'/></h5></a>
                </div>
            </div>                         
            </div>
            <!-- Controls or next and prev buttons -->
            <a class="left carousel-control" href="#my-slider" role="button" data-slide="prev">
              <span class="glyphicon glyphicon-chevron-left"></span>
              <span class="sr-only">Previous</span>
            </a>
            <a class="right carousel-control" href="#my-slider" role="button" data-slide="next">
              <span class="glyphicon glyphicon-chevron-right"></span>
              <span class="sr-only">Next</span>
            </a>

          </div>
        </div>
    </div>

<%@include file="include/analytics.jsp" %>
<%@include file="include/footer.jsp" %>
</body>
</html>

<%@include file="include/logging.jsp" %>
