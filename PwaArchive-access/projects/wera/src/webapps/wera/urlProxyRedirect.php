<?php
/*
 *  This file is part of WERA.
 *
 *  Copyright (C) 2001-2002 Royal Library in Stockholm,
 *                          Royal Library in Copenhagen,
 *                          Helsinki University Library of Finland,
 *                          National Library of Norway,
 *                          National and University Library of Iceland.
 *
 *  WERA is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  WERA is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with WERA; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

/*
 * This script is only in action when Wera is used with a proxy server. 
 * The proxy server must be set up so that incoming requests to all 
 * other destinations (i.e. not this server) is routed to this script.
 *  See manual for details on how to set this for the Apache http server
 *
 * $Id: urlProxyRedirect.php 835 2006-03-16 20:05:44Z sverreb $
 */
include_once("lib/config.inc");
include_once($conf_index_file);
include_once("$conf_searchenginepath/indexUtils.inc");
include ("$conf_includepath/documentLocatorCanonizeUrl.inc");

$url = trim($_REQUEST['url'], "proxy:");
preg_match('@&time=([0-9]{14})@', $_SERVER['HTTP_REFERER'], $matches);
$time = $matches[1];
$urlnotfound = false;
  
$searchEngine = new $conf_index_class();
$locator = new documentLocatorCanonizeUrl();
if (!isset($time)) {
  $locator->initialize($searchEngine, $url, false, $time, 'LAST');
}
else {
  $locator->initialize($searchEngine, $url, false, $time, 'NEAR');
}
$numhits = $locator->findVersions();
if($numhits <= 0) {
  // use this for something ?
  $urlnotfound = true;
}
$result = $locator->getResultSet();
$document = $result[1];
$mime = $document['mime'];
#$aid = $document['archiveidentifier'];



if ($mime != "text/html" or strpos($_SERVER['HTTP_REFERER'], "from=result.php") === FALSE) { // inline
  $redirectUrl = sprintf ("%s?url=%s&mode=%s&time=%s", $conf_result_page, $url, "inline", $time );
  header("Location: $redirectUrl");
  exit;
}
$redirectUrl = sprintf ("%s?url=%s&mode=%s&time=%s", $conf_result_page, $url, "standalone", $time );
$alert_text = nls("An attempt was made to navigate outside WERA (to the internet), redirecting back in!");
if ($conf_proxy_redirect_alert) {
  $alert = "yes";
} 
else {
  $alert = "false";
}

if (!is_int($conf_proxy_redirect_delay)) {
  $conf_proxy_redirect_delay = 0;
}

?>

<html>
<head>
<script type="text/javascript">
  function redirectBackToWera() {
    
    var $alert = "<?php echo $alert;?>";
    var $redirectUrl = "<?php echo $redirectUrl;?>";
    var $alert_text = "<?php echo $alert_text;?>";
     
    if ( $alert == "yes") {
      alert($alert_text);
    }
    top.location = $redirectUrl;
  }
  
  function getBackIn() {
    var $redirectDelay = <?php echo $conf_proxy_redirect_delay;?>;
    setTimeout("redirectBackToWera()",$redirectDelay );
  }
</script>
</head>

<body onload="getBackIn();">
Wera redirecting ..
</body>
</html>
  