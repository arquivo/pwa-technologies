    <script type="text/javascript">
        /*Detect Mobile*/
        window.mobilecheck = function() {
          var width = window.innerWidth || document.documentElement.clientWidth || document.body.clientWidth;
          return (width < 768);
        };
        if(mobilecheck()){
            currentURL = window.location.href;
            window.location.href = 'http://m.'+currentURL.substr(7,currentURL.length); /*Redirect to mobile version*/
        }    
    </script>