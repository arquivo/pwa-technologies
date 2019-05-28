<script src="js/swiper.min.js"></script>
<script type="text/javascript">MENU.close()</script>
<script type="text/javascript">
		    var ignoreInitialTransitionEnd = 0;
		    var toggleMenu = function(){
		      if (swiper.previousIndex == 0)
		        swiper.slidePrev()
		    }
		    swiper = new Swiper('.swiper-container', {
		      slidesPerView: 'auto'
		      , initialSlide: 1
		      , resistanceRatio: .00000000000001
		      , slideToClickedSlide: true
		      , allowSlideNext: false
		      , noSwiping: true
		      , noSwipingClass: 'swiper-no-swiping'
		      , draggable: false,
		    })

			swiper.on('slideChangeTransitionEnd', function () {
			  $('#mainMask').fadeOut(150);
			});

			window.onresize = function(event) {
				        $('.swiper-wrapper').css('-webkit-transition', 'all 0s linear' );
				        $('.swiper-wrapper').css('-moz-transition', 'all 0s linear' );
				        $('.swiper-wrapper').css('-o-transition', 'all 0s linear' );
				        $('.swiper-wrapper').css('-ms-transition', 'all 0s linear' );
				        $('.swiper-wrapper').css('transition', 'all 0s linear' );
				        $('.swiper-wrapper').css('transform', 'translate3d(0px, 0px, 0px)' );
				        $('.swiper-wrapper').css('-webkit-transform', 'translate3d(0px, 0px, 0px)' );		
			};

	        $('.swiper-wrapper').css('-webkit-transition', 'all 0s linear' );
	        $('.swiper-wrapper').css('-moz-transition', 'all 0s linear' );
	        $('.swiper-wrapper').css('-o-transition', 'all 0s linear' );
	        $('.swiper-wrapper').css('-ms-transition', 'all 0s linear' );
	        $('.swiper-wrapper').css('transition', 'all 0s linear' );
	        $('.swiper-wrapper').css('transform', 'translate3d(0px, 0px, 0px)' );
	        $('.swiper-wrapper').css('-webkit-transform', 'translate3d(0px, 0px, 0px)' );	
			
			$('#mainMask').on('click', function (e) { 
		      $('.swiper-wrapper').css('-webkit-transition', 'all 0.3s linear' );
		      $('.swiper-wrapper').css('-moz-transition', 'all 0.3s linear' );
		      $('.swiper-wrapper').css('-o-transition', 'all 0.3s linear' );
		      $('.swiper-wrapper').css('-ms-transition', 'all 0.3s linear' );
		      $('.swiper-wrapper').css('transition', 'all 0.3s linear' );
		      $('.swiper-wrapper').css('transform', 'translate3d(0px, 0px, 0px)' );
		      $('.swiper-wrapper').css('-webkit-transform', 'translate3d(0px, 0px, 0px)' );
		      $('.swiper-wrapper').removeClass('active');
		      $('#mainMask').fadeOut(); 
			});  

			$('#menuButton').on('click', function(e){
				if(parseInt($('.swiper-wrapper').css('transform').split(',')[4]) >= 0){
					console.log('open menu click');
					ga('send', 'event', 'ReplayBarFunctions', 'MainMenuClick', 'arquivo.pt/');
					$('.swiper-wrapper').addClass('active');
					$('.swiper-wrapper').css('-webkit-transition', 'all 0.3s linear' );
					$('.swiper-wrapper').css('-moz-transition', 'all 0.3s linear' );
					$('.swiper-wrapper').css('-o-transition', 'all 0.3s linear' );
					$('.swiper-wrapper').css('-ms-transition', 'all 0.3s linear' );
					$('.swiper-wrapper').css('transition', 'all 0.3s linear' );
					$('.swiper-wrapper').css('transform', 'translate3d(-'+$('.menu').width()+'px, 0px, 0px)' );
					$('.swiper-wrapper').css('-webkit-transform', 'translate3d(-'+$('.menu').width()+'px, 0px, 0px)' );					
					$('#mainMask').show();
				}
				else {
					console.log('close menu click');
					$('.swiper-wrapper').removeClass('active');
					$('.swiper-wrapper').css('-webkit-transition', 'all 0.3s linear' );
					$('.swiper-wrapper').css('-moz-transition', 'all 0.3s linear' );
					$('.swiper-wrapper').css('-o-transition', 'all 0.3s linear' );
					$('.swiper-wrapper').css('-ms-transition', 'all 0.3s linear' );
					$('.swiper-wrapper').css('transition', 'all 0.3s linear' );
					$('.swiper-wrapper').css('transform', 'translate3d(0px, 0px, 0px)' );
					$('.swiper-wrapper').css('-webkit-transform', 'translate3d(0px, 0px, 0px)' );
					$('#mainMask').fadeOut(); 					
				}  
			});	        	    
</script>