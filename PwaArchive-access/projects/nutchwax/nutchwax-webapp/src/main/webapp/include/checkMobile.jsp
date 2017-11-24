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
        if(mobilecheck()){
            currentURL = window.location.href;
            window.location.href = 'http://m.'+currentURL.substr(7,currentURL.length); /*Redirect to mobile version*/
        }   
    </script>