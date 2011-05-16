<SCRIPT language="Javascript">
<!-- 
   // $Id: html_javascript.php.js 646 2006-01-12 20:21:19Z sverreb $
   // Script inserted by WERA to ensure that links point to
   // WERA rather than out to the Internet
   
   // This script was contributed by the Internet Archive
   // Some minor WERA adaptations has been made
   
   function xResolveUrl(url) {
      var image = new Image();
      image.src = url;
      return image.src;
   }
   function xLateUrl(aCollection, sProp, mode) {
      var i = 0;
      for(i = 0; i < aCollection.length; i++) {
        if (typeof(aCollection[i][sProp]) == "string") { 
          if (aCollection[i][sProp].indexOf("mailto:") == -1 && aCollection[i][sProp].indexOf("javascript:") == -1) {
               aCollection[i]["target"] = "_top";
               if(aCollection[i][sProp].indexOf("http") == 0) {
                 aCollection[i][sProp] = sWayBackCGI + "&mode=" + mode + "&url=" + encodeURIComponent(aCollection[i][sProp]);
               } else {
                 aCollection[i][sProp] = sWayBackCGI + "&mode=" + mode + "&url="  + encodeURIComponent(xResolveUrl(aCollection[i][sProp]));
               }
         }
        }
      }
   }

   xLateUrl(document.getElementsByTagName("IMG"),"src","inline");
   xLateUrl(document.getElementsByTagName("A"),"href","standalone");
   xLateUrl(document.getElementsByTagName("AREA"),"href","standalone");
   xLateUrl(document.getElementsByTagName("OBJECT"),"codebase","inline");
   xLateUrl(document.getElementsByTagName("OBJECT"),"data","inline");
   xLateUrl(document.getElementsByTagName("APPLET"),"codebase","inline");
   xLateUrl(document.getElementsByTagName("APPLET"),"archive","inline");
   xLateUrl(document.getElementsByTagName("EMBED"),"src","inline");
   xLateUrl(document.getElementsByTagName("BODY"),"background","inline");
   var forms = document.getElementsByTagName("FORM","inline");
   if (forms) {
       var j = 0;
       for (j = 0; j < forms.length; j++) {
              f = forms[j];
              if (typeof(f.action)  == "string") {
                 if(typeof(f.method)  == "string") {
                     if(typeof(f.method) != "post") {
                        f.action = sWayBackCGI + "&url="  + encodeURIComponent(f.action);
                     }
                  }
              }
        }
    }

   var interceptRunAlready = false;
   function intercept_js_href_iawm(destination) {
     if(!interceptRunAlready &&top.location.href != destination) {
       interceptRunAlready = true;
       top.location.href = sWayBackCGI+xResolveUrl(destination);
     }
   } 
   // ie triggers
   href_iawmWatcher = document.createElement("a");
   top.location.href_iawm = top.location.href;
   if(href_iawmWatcher.setExpression) {
     href_iawmWatcher.setExpression("dummy","intercept_js_href_iawm(top.location.href_iawm)");
   }
   // mozilla triggers
   function intercept_js_moz(prop,oldval,newval) {
     intercept_js_href_iawm(newval);
     return newval;
   }
   if(top.location.watch) {
     top.location.watch("href_iawm",intercept_js_moz);
   }

   var notice = 
     "<div style='" +
     "position:relative;z-index:99999;"+
     "border:1px solid;color:black;background-color:lightYellow;font-size:10px;font-family:sans-serif;padding:5px'>" + 
     weraNotice +
  	 " [ <a style='color:blue;font-size:10px;text-decoration:underline' href=\"javascript:void(top.disclaimElem.style.display='none')\">" + weraHideNotice + "</a> ]" +
     "</div>";

    function getFrameArea(frame) {
      if(frame.innerWidth) return frame.innerWidth * frame.innerHeight;
      if(frame.document.documentElement && frame.document.documentElement.clientHeight) return frame.document.documentElement.clientWidth * frame.document.documentElement.clientHeight;
      if(frame.document.body) return frame.document.body.clientWidth * frame.document.body.clientHeight;
      return 0;
    }

    function disclaim() {
      if(top!=self) {
        largestArea = 0;
        largestFrame = null;
        for(i=0;i<top.frames.length;i++) {
          frame = top.frames[i];
          area = getFrameArea(frame);
          if(area > largestArea) {
            largestFrame = frame;
            largestArea = area;
          }
        }
        if(self!=largestFrame) {
          return;
        }
      }
     disclaimElem = document.createElement('div');
     disclaimElem.innerHTML = notice;
     top.disclaimElem = disclaimElem;
     document.body.insertBefore(disclaimElem,document.body.firstChild);
    }
    disclaim();

-->
</SCRIPT>    
