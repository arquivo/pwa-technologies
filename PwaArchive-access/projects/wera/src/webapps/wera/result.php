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
 *
 * $Id: result.php 852 2006-03-20 11:52:54Z sverreb $
 *
 * This script gets variables from top.php and document-dispatcher and distributes to the two scripts.
 * top.php gets $time and $url
 * Document-dispatcher gets $timenum and $url
*/

include_once("lib/config.inc");
include($conf_includepath . '/httpUtils.inc');
include($conf_includepath . '/url.inc');

if (isset($_REQUEST['mode'])){
	$mode = $_REQUEST['mode'];
}

if (isset($_REQUEST['query'])){
  $query = $_REQUEST['query'];
}


if (isset($_REQUEST['time'])){
	$time = $_REQUEST['time'];
}

if (isset($_REQUEST['mime'])){
	$mime = $_REQUEST['mime'];
}

if (isset($_REQUEST['url'])){
	$url = $_REQUEST['url'];
	$url = stripport(trim($url));
	# NutchWax replaces whitespace in urls with %20
	# so wera also need to do this
	$url = str_replace(" ", "%20", $url);
	$url = urlencode($url);
}

if (isset($_REQUEST['meta'])){
	$meta = $_REQUEST['meta'];
}

if (isset($_REQUEST['level'])){
	$level = $_REQUEST['level'];
}

if (isset($_REQUEST['manlevel'])){
	$manlevel = $_REQUEST['manlevel'];
}

if (isset($_REQUEST['auto'])){
	$auto = $_REQUEST['auto'];
}
else {
  $auto = "on";
}

if (isset($_REQUEST['debug'])){
	$debug = $_REQUEST['debug'];
}


if($mode == "inline" || $mode == "external") {
  fetchAndPrintUrl("$conf_document_dispatcher?url=" . $url . "&time=$time&mime=$mime");
  exit();
}

include($conf_includepath . "/time.inc");


$js = $_COOKIE[nwabrowser];

?>

<html>
<head><title>WERA-@VERSION@</title><head>

<?php

print "<FRAMESET frameborder=no border=0 framespacing=5 marginheight=0 marginwidth=0 bordercolor=black rows=\"100,*\">";

if($frame!='true'){
  print "<frame scrolling=no src=\"top.php?url=" . $url . "&time=$time&level=$level&manlevel=$manlevel&auto=$auto&debug=$debug&meta=$meta&query=$query\" name=\"nwa_top\"/>";
}
if ( $meta == "on" ) {
	print "<frame src=\"metadata.php?url=" . $url . "&time=$time\" name=\"nwa_content\" />";
}
else { 
	print "<frame src=\"$conf_document_dispatcher?url=" . $url . "&time=$time&js=$js&from=result.php\" name=\"nwa_content\" />";
}
print "</frameset>";


?>
<noframes>
You need a browser that supports frames to see this, well not exactly _this_, but what would have been here if you had a frames-capable browser.
</noframes>
</html>
