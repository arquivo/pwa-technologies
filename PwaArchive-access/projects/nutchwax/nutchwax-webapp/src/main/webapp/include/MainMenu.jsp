<script type="text/javascript">
String.prototype.replaceAll = String.prototype.replaceAll || function(needle, replacement) {
    return this.split(needle).join(replacement);
}; 
	
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
			            		'<button class="clean-button" onclick="MENU.copyLink();"><h4><i class="fa fa-link padding-right-menu-icon" aria-hidden="true"></i> Copiar Link</h4></button>' +
	          					'<button class="clean-button" id="pagesMenu" onclick="MENU.pagesClick();"><h4><i class="fa fa-globe padding-right-menu-icon" aria-hidden="true"></i> Paginas<i id="pagesCarret" class="fa fa-caret-down iCarret shareCarret pull-right" aria-hidden="true"></i></h4></button>'+	 	
	          					'<div id="pageOptions">'+	          							            		
	          						'<a href="/index.jsp?l=<%=language%>" onclick=""><h4 class="submenu"><i class="fa fa-search padding-right-menu-icon" aria-hidden="true"></i> <fmt:message key='topbar.menu.home'/></h4></a>' +
	          						'<button class="clean-button" id="advancedSearch" onclick="MENU.advancedPagesClick();"><h4 class="submenu"><i class="fa fa-search-plus padding-right-menu-icon" aria-hidden="true"></i> <fmt:message key='topbar.menu.advanced'/></h4></button>' +         					
	          					'</div>'+
	          					'<button class="clean-button" id="imagesMenu" onclick="MENU.imagesClick();"><h4><i class="fa fa-image padding-right-menu-icon" aria-hidden="true"></i> Imagens<i id="imagesCarret" class="fa fa-caret-down iCarret shareCarret pull-right" aria-hidden="true"></i></h4></button>'+
	          					'<div id="imageOptions">'+	          							            		
	          						'<a href="/images.jsp?l=<%=language%>" onclick=""><h4 class="submenu"><i class="fa fa-search padding-right-menu-icon" aria-hidden="true"></i> <fmt:message key='topbar.menu.home'/></h4></a>' +
	          						'<button class="clean-button" id="advancedImages" onclick="MENU.advancedImagesClick();"><h4 class="submenu"><i class="fa fa-search-plus padding-right-menu-icon" aria-hidden="true"></i> <fmt:message key='topbar.menu.advanced'/></h4></button>' +   			
	          					'</div>'+	          							          						  		
	          					'<button class="clean-button" id="switchDesktop" onclick="MENU.switchDesktop();"><h4><i class="fa fa-desktop padding-right-menu-icon" aria-hidden="true"></i> <fmt:message key='topbar.menu.desktop'/></h4></button>'+
	          					'<button class="clean-button" id="reportBug" onclick="MENU.reportBug();"><h4><i class="fa fa-bug padding-right-menu-icon" aria-hidden="true"></i> <fmt:message key='topbar.menu.report'/></h4></button>'+	 	        
	          					'<a href="//sobre.arquivo.pt/<%=language%>" onclick=""><h4><i class="fa fa-info-circle padding-right-menu-icon" aria-hidden="true"></i> <fmt:message key='topbar.menu.about'/></h4></a>'+	         				
	          					'<button class="clean-button" id="changeLanguage" onclick="changeLanguage();" ><h4><i class="fa fa-flag padding-right-menu-icon" aria-hidden="true"></i> <fmt:message key='topbar.menu.otherLanguage'/></h4></button>'+
	          				'</div>'); 
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
		changeLanguage: function(){					
					window.location = MENU.toggleLanguage(); 
					return false; 
		},
		switchDesktop: function(){
					Cookies.set('forceDesktop', 'true', { domain: window.location.hostname.substr(2, window.location.hostname.length) });
					/*redirect current link from mobile to desktop version i.e. remove the m. from current link*/
					window.location = window.location.href.replace(window.location.hostname , window.location.hostname.substr(2, window.location.hostname.length)) 
					return false;  			
			
		},
		advancedPagesClick: function(){
				queryParam='';
				var txtSearch = $('#txtSearch').attr('value');
				if(txtSearch !='' && txtSearch != undefined){
					queryParam= '&query='+txtSearch;
				}				
				window.location = "/advanced.jsp?l=<%=language%>"+queryParam;
		},	
		advancedImagesClick: function(){
				queryParam='';
				var txtSearch = $('#txtSearch').attr('value');
				if(txtSearch !='' && txtSearch != undefined){
					queryParam= '&query='+txtSearch;
				}				
				window.location = "/advancedImages.jsp?l=<%=language%>"+queryParam;
		},			
		reportBug: function(){
				window.location = '<fmt:message key="topbar.menu.bug" />'+window.location.href.replaceAll('&', '%26');
		},							
        attachMask: function(){       
		  $('#mainMask').on('click', function(e){
		    document.querySelector('.swiper-container').swiper.slideNext();
		  }); 	         	
        },	 
        copyLink: function(){
			var dummy = document.createElement('input'),
			    text = window.location.href;

			document.body.appendChild(dummy);
			dummy.value = text;
			dummy.select();
			document.execCommand('copy');
			document.body.removeChild(dummy);
			$('body').append('<div id="alertCopy" class="alert alert-success alertCopy"><strong>Link Copiado!</strong></div>');
			$('#alertCopy').show().delay(1500).fadeOut();
			setTimeout(function(){
  			$('#alertCopy').remove();
			}, 2000); /*time to show the notification plus the time to do the fadeout effect*/
        },
        pagesClick: function(){
		    $('#pagesCarret').toggleClass('fa-caret-up fa-caret-down');
		    $('#pageOptions').slideToggle( "fast", "linear" );
        },		
        imagesClick: function(){
		    $('#imagesCarret').toggleClass('fa-caret-up fa-caret-down');
		    $('#imageOptions').slideToggle( "fast", "linear" );
        },	        							 		
    };
}());	
</script>