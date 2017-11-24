<script type="text/javascript">
/*Arquivo.pt specific functions and js code, such as loading constants, cookies, custom html code, etc*/
var MENU = MENU || (function(){
    return {
        init : function() {
        	document.write(''+
				'<div class="swiper-container">'+
					'<div class="swiper-wrapper">'+
						'<div id="mainMenu" class="swiper-slide menu closed">'+
							'<a onclick="ga(\'send\', \'event\', \'MainMenu\', \'Home click\', \''+window.location.href+'\');" href="/index.jsp?l=<%=language%>"><h4><i class="fa fa-home" aria-hidden="true"></i> <fmt:message key='topbar.leftmenu.home'/></h4></a>'+
							'<a onclick="ga(\'send\', \'event\', \'MainMenu\', \' Advanced search click\', \''+window.location.href+'\');" href="/advanced.jsp?l=<%=language%>"><h4><i class="fa fa-search-plus" aria-hidden="true"></i> <fmt:message key='topbar.leftmenu.advanced'/></h4></a>'+
							'<a onclick="ga(\'send\', \'event\', \'MainMenu\', \' Video click\', \''+window.location.href+'\');" href="<fmt:message key='topbar.leftmenu.video.href'/>"><h4><i class="fa fa-youtube-play" aria-hidden="true"></i> <fmt:message key='topbar.leftmenu.video'/></h4></a>'+
							'<a onclick="ga(\'send\', \'event\', \'MainMenu\', \' Examples click\', \''+window.location.href+'\');" href="<fmt:message key='topbar.leftmenu.examples.href'/>"><h4><i class="fa fa-globe" aria-hidden="true"></i> <fmt:message key='topbar.leftmenu.examples'/></h4></a>'+
							'<a onclick="ga(\'send\', \'event\', \'MainMenu\', \' Help click\', \''+window.location.href+'\');" href="<fmt:message key='topbar.leftmenu.help.href'/>"><h4><i class="fa fa-question-circle" aria-hidden="true"></i> <fmt:message key='topbar.leftmenu.help'/></h4></a>'+
							'<a onclick="ga(\'send\', \'event\', \'MainMenu\', \' About click\', \''+window.location.href+'\');" href="<fmt:message key='topbar.leftmenu.about.href'/>"><h4><i class="fa fa-info-circle" aria-hidden="true"></i> <fmt:message key='topbar.leftmenu.about'/></h4></a>'+
						'</div>' +
						'<div class="swiper-slide content"><div id="mainMask"></div>');
	      
        	this.attachMask();

        },
        close: function(){
        	document.write('</div></div></div>');
        },
        attachMask: function(){       
		  $('#mainMask').on('click', function(e){
		    document.querySelector('.swiper-container').swiper.slideNext();
		  }); 	         	
        },	 									 		
    };
}());	
</script>