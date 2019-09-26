<script src="js/swiper.min.js"></script>
<script type="text/javascript">MENU.close()</script>
<script type="text/javascript">
	<!-- Initialize Swiper -->
    var menuButton = document.querySelector('#menuButton');
    var openMenu = function () {
      console.log('open');
      $('.logo-main-div').css("position:fixed!important; width:initial;");
      $('#menuWrapper').removeClass('transform-none');
      $('#menuSwiperSlide').removeClass('hidden');           
	    swiper.allowSlidePrev = true;    	
      swiper.slidePrev();
    };
    var swiper = new Swiper('.swiper-container', {
      slidesPerView: 'auto',
      initialSlide: 1,
      resistanceRatio: 0,
      slideToClickedSlide: true,
      draggable: false,      
      on: {
        slideChangeTransitionStart: function () {    
          var slider = this;
          if (slider.activeIndex === 0) { /*open menu*/
          	this.allowSlidePrev = true;
          	$('#mainMask').fadeIn('fast');
            menuButton.classList.add('cross');
            $('.swiper-container').removeClass('swiper-no-swiping');
            // required because of slideToClickedSlide
            menuButton.removeEventListener('click', openMenu, true);
          } else { /*close menu*/
          	 this.allowSlidePrev = false;
          	$('.swiper-container').addClass('swiper-no-swiping');
          	$('#mainMask').fadeOut('fast');
            menuButton.classList.remove('cross');
          }
        }
        , slideChangeTransitionEnd: function () {
          var slider = this;
          if (slider.activeIndex === 1) {
            menuButton.addEventListener('click', openMenu, true);
          }
        },
      }
    });
    swiper.allowSlidePrev = false;
    /*$( "#menuButton" ).click(function() {
      openMenu();
    });*/
</script>