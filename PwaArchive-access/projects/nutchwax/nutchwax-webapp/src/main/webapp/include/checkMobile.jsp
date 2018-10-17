    <script type="text/javascript" src="/js/js.cookie.js"></script>
    <script type="text/javascript">
        /*Detect Mobile*/
        window.mobilecheck = function() {
          var width = Math.min(window.innerWidth || Infinity, screen.width);
          /*Lumia phones have a bug where they get a width of 1024*/
          if(navigator.userAgent.indexOf('Lumia') !== -1 || navigator.userAgent.indexOf('lumia') !== -1){
            return true;
          }
          return (width < 768);
        };
        if(mobilecheck() && Cookies.get('forceDesktop')!== 'true' ){
            currentURL = window.location.href;
            window.location.href = '//m.'+currentURL.substr(window.location.protocol.length+2,currentURL.length); /*Redirect to mobile version*/
        }   
    </script>
