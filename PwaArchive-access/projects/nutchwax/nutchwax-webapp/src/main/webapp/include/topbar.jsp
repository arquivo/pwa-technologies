<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd" >
<%@page import="java.net.URL"%>

<%@ page
	session="true"
	contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"	

	import="java.io.File"
	import="java.io.IOException"
	import="java.net.URLEncoder"
	import="java.text.DateFormat"
	import="java.util.Calendar"
	import="java.util.Date"
	import="java.util.regex.Matcher"
	import="java.util.regex.Pattern"
	import="java.util.GregorianCalendar"
	import="org.apache.hadoop.conf.Configuration"
	import="org.apache.lucene.search.PwaFunctionsWritable"
	import="org.apache.nutch.global.Global"
	import="org.apache.nutch.html.Entities"
	import="org.apache.nutch.metadata.Nutch"
	import="org.apache.nutch.searcher.Hit"
	import="org.apache.nutch.searcher.HitDetails"
	import="org.apache.nutch.searcher.Hits"
	import="org.apache.nutch.searcher.Query"
	import="org.apache.nutch.searcher.Query.Clause"
	import="org.apache.nutch.searcher.NutchBean"
	import="org.apache.nutch.searcher.Summary"
	import="org.apache.nutch.searcher.Summary.Fragment"
	import="org.archive.access.nutch.NutchwaxBean"
	import="org.archive.access.nutch.NutchwaxQuery"
	import="org.archive.access.nutch.NutchwaxConfiguration"
%>

<%
  Configuration nutchConfAlt = NutchwaxConfiguration.getConfiguration(application);
  NutchBean bean2 = NutchwaxBean.get(application, nutchConfAlt);
%>
<%String arquivoHost = nutchConfAlt.get("wax.webhost", "arquivo.pt"); %>
<script type="text/javascript" src="/js/js.cookie.js"></script>

<!-- Main Menu Dependencies -->
<link rel="stylesheet" href="css/swiper.min.css">
<link rel="stylesheet" href="css/MainMenu.css">
<%@ include file="MainMenu.jsp" %>
<script type="text/javascript">MENU.init()</script> 


<div class="container-fluid">
	<div class="row">
		<div id="topBarFixed" class="col-xs-12 navbar  top-bar-background">
			<div class="col-xs-6">
			<h3 style="color:black; margin: 0"><a id="menuButton" class="menu-button"><i class="fa fa-bars" aria-hidden="true"></i></a></h3></div>
			<div class="col-xs-6 text-right">
				<h4 style="margin-top: 3px; margin-bottom:0px;"><a id="languageSelection" href="#"><fmt:message key='topbar.otherLanguage'/></a></h4>
			</div> 
		</div>
	</div>
</div>
<script type="text/javascript">
$('#languageSelection').click( function(e) {
		e.preventDefault();
		window.location = toggleLanguage(); 
		return false; } );
</script>

<script type="text/javascript">
function toggleLanguage() {
	/*returns current window href without the specified parameter*/
	key="l"; /*language parameter*/
	sourceURL = window.location.href;
	var foundParameter = false;
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
                foundParameter = true;
            }
        }
        rtn = rtn + "?" + params_arr.join("&");
        if(foundParameter){
        	if( rtn.substr(rtn.length - 1) ==="?"){
        		rtn = rtn.substr(0, rtn.length-1);
        	}
        }
        else{
        	rtn = rtn +"&l=en";
        }
    }
    else{
    	rtn=rtn +"?&l=en"
    }
    return rtn;
}
</script>



