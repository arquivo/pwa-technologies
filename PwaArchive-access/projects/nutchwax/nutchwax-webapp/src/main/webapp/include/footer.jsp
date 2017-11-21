<script src="js/swiper.min.js"></script>
<script type="text/javascript">MENU.close()</script>
<script type="text/javascript">
	        	var menuButton = document.querySelector('.menu-button');
			    var swiper = new Swiper('.swiper-container', {
			      slidesPerView: 'auto',
			      initialSlide: 1,
			      resistanceRatio: 0,
			      slideToClickedSlide: true,
			      on: {
			        init: function () {
			          /*Initialize slider in the right position i.e. menu closed, then set element visible*/
				      $('.swiper-wrapper').css('-webkit-transition', 'all 0s linear' );
				      $('.swiper-wrapper').css('-moz-transition', 'all 0s linear' );
				      $('.swiper-wrapper').css('-o-transition', 'all 0s linear' );
				      $('.swiper-wrapper').css('-ms-transition', 'all 0s linear' );
				      $('.swiper-wrapper').css('transition', 'all 0s linear' );
				      $('.swiper-wrapper').css('transform', 'translate3d(-'+$('#mainMenu').width()+', 0px, 0px)' );
				      $('.swiper-wrapper').css('-webkit-transform', 'translate3d(-'+$('#mainMenu').width()+', 0px, 0px)' );   	
				      $('.swiper-wrapper').css('visibility', 'visible');

				      /*alternate between the closed and open menu states on click*/
			          menuButton.addEventListener('click', function () {		            
					  var slider = document.querySelector('.swiper-container').swiper;
					  slider.activeIndex == 0 ? (slider.slideNext()) : (slider.slidePrev());  
			          }, true);
			        },
			        slideChange: function () {
			          var slider = this;
			          if (slider.activeIndex === 0) {
			            /*If Menu is active*/
			            /* scroll to the top of menu*/
			            $("html, body").animate({ scrollTop: 0 }, "medium");
			            /*do not allow scroll down in menu open state*/
			            $('body').css('overflow-y', 'hidden');
			            $('#mainMask').fadeIn("fast");
			          } else {
			            /*If Menu closed*/
			            /*Allow scrolldown again*/
			            $('body').css('overflow-y', 'auto');
			            $('#mainMask').fadeOut("fast");
			          }
			        },
			      }
    			});
</script>
