<script type="text/javascript">
String.prototype.replaceAll = String.prototype.replaceAll || function(needle, replacement) {
    return this.split(needle).join(replacement);
};
if (!String.prototype.startsWith) {
  String.prototype.startsWith = function(searchString, position) {
    position = position || 0;
    return this.indexOf(searchString, position) === position;
  };
} 


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
			            		'<button class="clean-button" onclick="MENU.copyLink();"><h4><i class="fa fa-link padding-right-menu-icon" aria-hidden="true"></i> <fmt:message key='topbar.menu.copy'/></h4></button>' +
	          					'<button class="clean-button" id="pagesMenu" onclick="MENU.pagesClick();"><h4><i class="fa fa-globe padding-right-menu-icon" aria-hidden="true"></i> <fmt:message key='topbar.menu.pages'/><i id="pagesCarret" class="fa fa-caret-down iCarret shareCarret pull-right" aria-hidden="true"></i></h4></button>'+	 	
	          					'<div id="pageOptions">'+	          							            		
	          						'<a href="/index.jsp?l=<%=language%>" onclick=""><h4 class="submenu"><i class="fa fa-search padding-right-menu-icon" aria-hidden="true"></i> <fmt:message key='topbar.menu.home'/></h4></a>' +
	          						'<button class="clean-button" id="advancedSearch" onclick="MENU.advancedPagesClick();"><h4 class="submenu"><i class="fa fa-search-plus padding-right-menu-icon" aria-hidden="true"></i> <fmt:message key='topbar.menu.advanced'/></h4></button>' +         					
	          					'</div>'+
	          					'<button class="clean-button" id="imagesMenu" onclick="MENU.imagesClick();"><h4><i class="fa fa-image padding-right-menu-icon" aria-hidden="true"></i> <fmt:message key='topbar.menu.images'/><i id="imagesCarret" class="fa fa-caret-down iCarret shareCarret pull-right" aria-hidden="true"></i></h4></button>'+
	          					'<div id="imageOptions">'+	          							            		
	          						'<a href="/images.jsp?l=<%=language%>" onclick=""><h4 class="submenu"><i class="fa fa-search padding-right-menu-icon" aria-hidden="true"></i> <fmt:message key='topbar.menu.home'/></h4></a>' +
	          						'<button class="clean-button" id="advancedImages" onclick="MENU.advancedImagesClick();"><h4 class="submenu"><i class="fa fa-search-plus padding-right-menu-icon" aria-hidden="true"></i> <fmt:message key='topbar.menu.advanced'/></h4></button>' +   			
	          					'</div>'+	          							          						  		
	          					'<button class="clean-button" id="switchDesktop" onclick="MENU.switchDesktop();"><h4><i class="fa fa-desktop padding-right-menu-icon" aria-hidden="true"></i> <fmt:message key='topbar.menu.desktop'/></h4></button>'+
	          					'<button class="clean-button" id="reportBug" onclick="MENU.reportBug();"><h4><i class="fa fa-bug padding-right-menu-icon" aria-hidden="true"></i> <fmt:message key='topbar.menu.report'/></h4></button>'+	 	        
	          					'<a href="//sobre.arquivo.pt/<%=language%>" onclick=""><h4><i class="fa fa-info-circle padding-right-menu-icon" aria-hidden="true"></i> <fmt:message key='topbar.menu.about'/></h4></a>'+	         				
	          					'<button class="clean-button" id="changeLanguage" onclick="MENU.changeLanguage();" ><h4><i class="fa fa-flag padding-right-menu-icon" aria-hidden="true"></i> <fmt:message key='topbar.menu.otherLanguage'/></h4></button>'+
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
					queryParam= '&query='+encodeHtmlEntity(txtSearch.toString());
				}				
				window.location = "/advanced.jsp?l=<%=language%>"+queryParam;
		},	
		advancedImagesClick: function(){
				queryParam='';
				var txtSearch = $('#txtSearch').attr('value');
				if(txtSearch !='' && txtSearch != undefined){
					queryParam= '&query='+encodeHtmlEntity(txtSearch.toString());
					console.log('escaped: ' + queryParam);
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
			var dummy = document.createElement('input')			    
			var urlToCopy;
			if( window.location.pathname.startsWith("/images.jsp") && lastPosition != -1 ){ /*Image expanded OR details expanded*/
				imageObj = imageObjs[lastPosition];
				if($('#card'+lastPosition).is(":visible")){ /*share a custom link to the image expanded*/					 
					 urlToCopy =  window.location.origin+"/imageexp.jsp?"+
					 			"imgSrc="+ encodeURIComponent(imageObj.imgSrc)+"&"+
					 			"imgTstamp="+ imageObj.timestamp+"&"+
					 			"imgWidth="+ parseInt(imageObj.expandedWidth)+"&"+
					 			"imgHeight="+ parseInt(imageObj.expandedHeight) +"&"+
    (imageObj.titleFull != "" ? "imgTitle="+imageObj.titleFull+"&"  : "") +			
    (imageObj.imgAltFull != "" ? "imgAlt="+imageObj.imgAltFull+"&"  : "") +	
    							"imgMimeType="+imageObj.imgMimeType+ "&" +
					 			"pageURL="+encodeURIComponent(imageObj.pageURL)+ "&"+
					 			"pageTitle="+imageObj.pageTitle+ "&"+
					 			"pageTstamp="+imageObj.pageTstamp+ "&"+
					 			"backURL="+encodeURIComponent(window.location.href);
				}	
				else{ /*share a custom link to the details card of the current image*/
					 urlToCopy =  window.location.origin+"/imagedet.jsp?"+
					 			"imgSrc="+ encodeURIComponent(imageObj.imgSrc)+"&"+
					 			"imgTstamp="+ imageObj.timestamp+"&"+
					 			"imgWidth="+ parseInt(imageObj.expandedWidth)+"&"+
					 			"imgHeight="+ parseInt(imageObj.expandedHeight) +"&"+
    (imageObj.titleFull != "" ? "imgTitle="+imageObj.titleFull+"&"  : "") +			
    (imageObj.imgAltFull != "" ? "imgAlt="+imageObj.imgAltFull+"&"  : "") +	
    							"imgMimeType="+imageObj.imgMimeType+ "&" +
					 			"pageURL="+encodeURIComponent(imageObj.pageURL)+ "&"+
					 			"pageTitle="+encodeURIComponent(imageObj.pageTitleFull)+ "&"+
					 			"pageTstamp="+imageObj.pageTstamp+ "&"+
					 			"safe="+imageObj.safe+"&"+
					 			"collection="+imageObj.collection+"&"+
					 			"backURL="+encodeURIComponent(window.location.href);					
				}
			}
			else{ /*Default case copy current url*/
				urlToCopy = window.location.href;
			}

			document.body.appendChild(dummy);
			dummy.value = urlToCopy;
			dummy.select();
			document.execCommand('copy');
			document.body.removeChild(dummy);
			$('body').append('<div id="alertCopy" class="alert alert-success alertCopy"><strong><fmt:message key='topbar.link.copied'/></strong></div>');
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