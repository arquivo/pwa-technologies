<script type="text/javascript">
/*Arquivo.pt specific functions and js code, such as loading constants, cookies, custom html code, etc*/
var MENU = MENU || (function(){
    return {
        init : function() {
        	document.write(''+
				'<div class="swiper-container">'+
					'<div class="swiper-wrapper">'+
						'<div class="swiper-slide content"><div id="mainMask"></div>');     
        	this.attachMask();

        },
        close: function(){
        	document.write( '</div></div></div>');
        	$('.swiper-wrapper').append(
			            	'<div class="swiper-slide menu swiper-slide-prev">' +       
	          					'<a href="/index.jsp?l=<%=language%>" onclick=""><h4><i class="fa fa-search padding-right-menu-icon" aria-hidden="true"></i> <fmt:message key='topbar.menu.home'/></h4></a>' +
	          					'<a href="/advanced.jsp?l=<%=language%>" onclick=""><h4><i class="fa fa-search-plus padding-right-menu-icon" aria-hidden="true"></i> <fmt:message key='topbar.menu.advanced'/></h4></a>' +
	          					'<a id="imagesAnchor"><h4><i class="fa fa-image padding-right-menu-icon" aria-hidden="true"></i> <fmt:message key='topbar.menu.images'/></h4></a>' +	          					
	          					'<a id="shareMenu"><h4><i class="fa fa-share-alt padding-right-menu-icon" aria-hidden="true"></i> <fmt:message key='topbar.menu.share'/><i id="shareCarret" class="fa fa-caret-down iCarret shareCarret pull-right" aria-hidden="true"></i></h4></a>'+	      
	          					'<div id="shareOptions">'+
	          						'<a class="addthis_button_facebook" onclick="" href=""><h4 class="submenu"><i class="fa fa-facebook padding-right-menu-icon" aria-hidden="true"></i> Facebook</h4></a>'+
	          						'<a class="addthis_button_twitter" onclick="" ><h4 class="submenu"><i class="fa fa-twitter padding-right-menu-icon" aria-hidden="true"></i> Twitter</h4></a>'+
			  					'</div>'+
	          					'<a href="<fmt:message key='topbar.menu.examples.href'/>" ><h4><i class="fa fa-globe padding-right-menu-icon" aria-hidden="true"></i> <fmt:message key='topbar.menu.examples'/></h4></a>'+				  					   	  			
	          					'<a href="//sobre.arquivo.pt/<%=language%>" onclick=""><h4><i class="fa fa-info-circle padding-right-menu-icon" aria-hidden="true"></i> <fmt:message key='topbar.menu.about'/></h4></a>'+			  					          
	          					'<a href="<fmt:message key='topbar.menu.help.href'/>" onclick=""><h4><i class="fa fa-question-circle padding-right-menu-icon" aria-hidden="true"></i> <fmt:message key='topbar.menu.help'/></h4></a>'+
	          					'<a href="" id="switchDesktop" onclick=""><h4><i class="fa fa-desktop padding-right-menu-icon" aria-hidden="true"></i> <fmt:message key='topbar.menu.desktop'/></h4></a>'+		          					
	          					'<a id="changeLanguage" ><h4><i class="fa fa-flag padding-right-menu-icon" aria-hidden="true"></i> <fmt:message key='topbar.menu.otherLanguage'/></h4></a>'+
	          				'</div>');
        	this.attachSwitchDesktop();
        	this.attachChangeLanguage();
        	this.attachShare();
        	this.attachImages();
        },
		toggleLanguage: function() {
		    localStorage.setItem("language", "<fmt:message key='topbar.OtherLanguageShort'/>".toUpperCase());			
			/*changes language*/
			key="l"; /*language parameter*/
			sourceURL = window.location.href;
		    var rtn = sourceURL.split("?")[0],
		        param,
		        params_arr = [],
		        queryString = (sourceURL.indexOf("?") !== -1) ? sourceURL.split("?")[1] : "";
		    if (queryString !== "") {
		        params_arr = queryString.split("&");
		        for (var i = params_arr.length - 1; i >= 0; i -= 1) {
		            param = params_arr[i].split("=")[0];
		            if (param === key) {
		                params_arr.splice(i, 1);
		            }
		        }
		        rtn = rtn + "?" + params_arr.join("&");
	        	rtn = rtn +"&l=<fmt:message key='topbar.OtherLanguageShort'/>";
		    }
		    else{
		    	rtn=rtn +"?l=<fmt:message key='topbar.OtherLanguageShort'/>";
		    }
		    return rtn;
		},
 		attachShare: function(){
		  $('#shareMenu').on('click', function(e){
		  	//ga('send', 'event', 'ReplayBarFunctions', 'ShareMenuClick', 'http://arquivo.pt/'+_ts+'/'+_url);
		    $('#shareCarret').toggleClass('fa-caret-up fa-caret-down');
		    $('#shareOptions').slideToggle( "fast", "linear" );
		  }); 	 			
 		}, 			
		attachChangeLanguage: function(){
			$('#changeLanguage').click( function(e) {
					e.preventDefault();
					window.location = MENU.toggleLanguage(); 
					return false; } );
		},
		attachSwitchDesktop: function(){
			$('#switchDesktop').click( function(e) {
					e.preventDefault();
					Cookies.set('forceDesktop', 'true', { domain: window.location.hostname.substr(2, window.location.hostname.length) });
					/*redirect current link from mobile to desktop version i.e. remove the m. from current link*/
					window.location = window.location.href.replace(window.location.hostname , window.location.hostname.substr(2, window.location.hostname.length)) 
					return false; } );			
			
		},
		attachImages: function(){
			$('#imagesAnchor').click( function(e) {
				e.preventDefault();
				queryParam='';
				var txtSearch = $('#txtSearch').attr('value');
				if(txtSearch !='' && txtSearch != undefined){
					queryParam= '&query='+txtSearch;
				}				
				window.location = "/images.jsp?l=<%=language%>"+queryParam;
			});
		},		
        attachMask: function(){       
		  $('#mainMask').on('click', function(e){
		    document.querySelector('.swiper-container').swiper.slideNext();
		  }); 	         	
        },	 									 		
    };
}());	
</script>