        <div class="row">
            <div class="col-sm-10 col-sm-offset-1 col-md-8 col-md-offset-2 col-lg-6 col-lg-offset-3 text-right">
                <div class="tabs-services div-underline">
                  <script type="text/javascript">
                    document.write('<a id="pagesTab" href="'+pagesHref+'"><fmt:message key="topbar.menu.pages"/></a>');
                    document.write('<a id="imagesTab" href='+imagesHref+'><fmt:message key="topbar.menu.images"/></a>');
                  </script>
                </div>              
                <form id="searchForm" action="/images.jsp" method="get">
                  <input type="hidden" name="l" value="<%= language %>" />
                  <input id="sizeFormInput" type="hidden" name="size" value="<%=size%>" />
                  <input id="typeFormInput" type="hidden" name="type" value="<%=type%>" />  
                  <input id="toolsFormInput" type="hidden" name="tools" value="<%=tools%>" />  
                  <input id="safeSearchFormInput" type="hidden" name="safeSearch" value="<%=safeSearchString%>" />                    
                <div id="form_container"> 
                    <div class="input-group stylish-input-group">
                        
                            <input id="txtSearch" value="<c:out value = "${htmlQueryString}"/>" name="query" type="search" class="form-control no-radius search-input" placeholder="<fmt:message key='home.search.placeholder'/>"  autocapitalize="off" autocomplete="off" autocorrect="off">
                            <script type="text/javascript"> $('#txtSearch').keypress(function (e) {if (e.which == 13) { $('form').submit(); return false; }});  </script>                               
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
                    <span class="span-start-year"><input class="label-start-year nooutline" id="dateStart" name="dateStart" value="<%=dateStartDay%>/<%=dateStartMonth%>/<%=dateStartYear%>"></input><button onclick="$('#dateStart').click()" class="calendar-anchor-search clean-button-no-fill fleft"><img src="/img/calendar.gif"/></button></span>                         
                    <span class="span-end-year"><input class="label-end-year nooutline" id="dateEnd" name="dateEnd" value="<%=dateEndDay%>/<%=dateEndMonth%>/<%=dateEndYear%>"></input><button onclick="$('#dateEnd').click()" class="calendar-anchor-search clean-button-no-fill"><img src="/img/calendar.gif"/></button></span>                    
                    <input type="hidden" id="l" name="l" value="<%=language%>"/>
                </div>   
<script src="/include/clearForm.js"></script>     
<script type="text/javascript">
  $("#dateStart").change( function() {                                  
    $('.search-button').click(); /*submit form if user changes date on datepicker*/
  });
  $("#dateEnd").change( function() {
    $('.search-button').click(); /*submit form if user changes date on datepicker*/
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
          $('#dateStart').val('<%=dateStartDay%>/<%=dateStartMonth%>/'+values[handle]);
          $('#event-start').attr('value', +values[handle]);
          changed= true;
          console.log('changed true')        
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
<script type="text/javascript">
/*Initialization of Datepickers datestart and dateend */
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
                <ion-modal-controller></ion-modal-controller>
<script type="text/javascript">
  customElements.define('modal-page', class extends HTMLElement {
  connectedCallback() {
    this.innerHTML = `
<ion-header>
  <ion-toolbar>
    <ion-title>Super Modal</ion-title>
  </ion-toolbar>
</ion-header>
<ion-content>
  Content
</ion-content>`;
  }
});

async function presentModal() {
  // initialize controller
  const modalController = document.querySelector('ion-modal-controller');
  await modalController.componentOnReady();

  // present the modal
  const modalElement = await modalController.create({
    component: 'modal-page'
  });
  await modalElement.present();
}
</script>

            </div>            
        </div>
<!-- End SearchHeader -->