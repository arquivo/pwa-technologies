    <div class="container-fluid topcontainer">
        <div class="row">
            <div class="col-sm-10 col-sm-offset-1 col-md-8 col-md-offset-2 col-lg-6 col-lg-offset-3 text-right">
                <div class="tabs-services div-underline">
                  <script type="text/javascript">
                    document.write('<a id="pagesTab" href="'+pagesHref+'"><fmt:message key="topbar.menu.pages"/></a>');
                    document.write('<a id="imagesTab" href='+imagesHref+'><fmt:message key="topbar.menu.images"/></a>');
                  </script>
                </div>                  
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

            </div>
        </div>

    </div>