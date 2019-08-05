    <div class="container-fluid topcontainer">
        <div class="row">
            <div class="col-sm-10 col-sm-offset-1 col-md-8 col-md-offset-2 col-lg-6 col-lg-offset-3 text-right">                            
                
                <!-- start search box -->
                <form id="searchForm" action="/search.jsp">                
                <div id="form_container"> 
                    <div id="searchBarBlock" class="input-group stylish-input-group">
                        
                            <input name="query" id="txtSearch" type="search" class="form-control no-radius search-input" placeholder="<fmt:message key='home.search.placeholder'/>" autofocus autocapitalize="off" autocomplete="off" autocorrect="off"> 
                    </div>
                    <!-- starts search lupe and "x" close button -->
                    <div>
                      <span class="clear-text"><i class="fa fa-close"></i></span>                         
                              <span id="buttonSearch" class="input-group-addon no-radius search-button-span">
                                   <button class="search-button" type="submit">
                                      <span class="glyphicon glyphicon-search white"></span>
                                  </button>  
                     </span>                   
                     <!-- starts history range slider -->
                     <ion-item class="ion-no-padding" id="ionSlider" lines="none">
                     <ion-range ion-padding-start style="padding:40px 0 0 0 !important;margin-top:-30px;" id="dual-range" dual-knobs pin color="dark" min="1996" max="2018" step="1">                       
                        <p id="sliderCircleLeft" slot="end"><span><a href="#">2018</a></span></p>
                        <p id="sliderCircleRight" slot="start"><span><a href="#">1996</a></span></p>
                        
                      </ion-range>                  
                      <script>
                        const dualRange = document.querySelector('#dual-range');
                        dualRange.value = { lower: 1996, upper: 2018 };
                      </script>                  
                      </ion-item>
                     <!-- ends history range slider -->
                     </div>
                     <!-- ends search lupe and "x" close button -->
                     <!-- starts Paginas and images links option -->
                     <div id="searchBarButtonsDiv"><br>
                       <a id="BotaoPaginas" class="advancedSearch" href="#"><span>P&aacute;ginas</span></a>
                       <a id="BotaoImagens" class="advancedSearch" href="#"><span>Imagens</span></a>
                       <a id="BotaoPesquisaAvancada" class="advancedSearch "ref="/advanced.jsp?l=pt"><span>Pesquisa Avan&ccedil;ada</span></a>                   
                     </div>
                     <!-- ends Paginas and images links option -->
                    </div>
            
<script type="text/javascript">
/*Initialization of Datepickers datestart and dateend for the advanced search*/
    $(function () {
      var currDate = new Date();
        var curr = currDate.getFullYear();
        var opt = {}
        opt.date = {preset : 'date'};

  
        $('#dateStart').val('<%=dateStartDay%>/<%=dateStartMonth%>/<%=dateStartYear%>').scroller('destroy').scroller($.extend(opt["date"], { 
          theme: "android-ics light",
          dateFormat: 'dd/mm/yy', 
          dateOrder: 'dMyy' ,
          startYear: 1996 , 
          endYear: currDate.getFullYear()-1,
          setText: 'OK',                          
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

        $('#dateEnd').val('<%=dateEndDay%>/<%=dateEndMonth%>/<%=dateEndYear%>').scroller('destroy').scroller($.extend(opt["date"], { 
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
</form>    

<script src="/include/clearForm.js"></script>      
<script type="text/javascript">
  $("#dateStart").change( function() {
    dateSlider.noUiSlider.set([$("#dateStart").val().substr($("#dateStart").val().length-4), null])
    $('.search-button').click(); /*submit form if user changes date on datepicker*/
  });
  $("#dateEnd").change( function() {
    $('.search-button').click(); /*submit form if user changes date on datepicker*/
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
        $('#dateStart').val('<%=dateStartDay%>/<%=dateStartMonth%>/'+values[handle]);
        changed= true;
      }     
    }else{
      if( $('#dateEnd').attr('value').substring(6, 10) != values[handle]){    
       $('#dateEnd').val('<%=dateEndDay%>/<%=dateEndMonth%>/'+values[handle]);
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

            </div>
        </div>

    </div>