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
 * File: overview.php
 *
 * $Id: overview.php 591 2005-12-07 14:35:33Z sverreb $
*/


header("Content-Type: text/html; charset=UTF-8");

include_once("lib/config.inc");
include_once($conf_index_file);
include_once("$conf_searchenginepath/indexUtils.inc");


/*
*
* TODO: Handling of more hits than in the initial resultset retrieved
*
*/
include($conf_includepath . "/header.inc");
?>

<script language="javascript">
</script>
</HEAD>
<BODY><center>
<font face="helvetica,arial,sans-serif">

<?php

if (!isset($url)) {
  $url = $_REQUEST['url']; //if register_globals is off
}

if (!isset($query)) {
  $query = $_REQUEST['query'];
}

$encoded_url = urlencode($url);


?>
<table border='0' cellpadding='0' cellspacing='0' width=90%>
  <tr>
    <td class="norm" width="1" colspan="5"><img alt='' height='10' src='/images/1px.gif' width="1"></td>
  </tr>
  <tr>
    <td class="norm" width="10"><img alt='' height='1' src='/images/1px.gif' width="1"></td>
    <td class="norm" align="left"><img alt="" src="<?php print $conf_logo;?>"></td>
    <td class="norm" align="right">
        <?php 
          print "<a href=\"$conf_simple_search\">";
          print nls("New search") . "</a>";
        ?>
    </td>    
    <td class="norm" width="10"><img alt='' height='1' src='/images/1px.gif' width="1"></td>    
  </tr>
</table>

<table border='0' cellpadding='0' cellspacing='0' width=90%>
  <tr>
    <td class="norm" colspan="5" align="left"><img alt='' height='8' src='/images/1px.gif' width='1'></td>
  </tr>
  <tr>
    <td colspan='5' class='border'><img alt='' height='2' src='/images/1px.gif' width='1'></td>
  </tr>
  <tr>
    <td colspan='5' class='norm'><img alt='' height='5' src='/images/1px.gif' width='1'></td>
  </tr>  
  </table>
  
<table align="center" class="resultsborder" border="0" cellspacing="0" cellpadding="0" width="90%">
  <tr>
    <td>

          <table class="resultsborder"><tr>
<?php

$querystring = "exacturl:" . urlencode($url);


$s = new $conf_index_class();
$s->setQuery($querystring);
$s->setSizeOfResultSet(1000);
$s->setFieldsInResult("date");
$s->setSortOrder("ascending");
if ($s->doQuery()) {
	$numhits = $s->getNumHits();
	
	if ($numhits == 0) {
		print "Sorry, the url was not found in the index (" . $url . ")";
	}
	else {
		$total = $s->getNumHitsTotal();
		$rset = $s->getResultSet();
		if ($conf_debug == 1 or $debug) {
			print "DEBUG : Query url : <a href=\"" . $s->queryurl . "\">" . $s->queryurl . "</a><br>&nbsp;<br>";
			print "<br>&nbsp<br>DEBUG : Result set :";
			print "<pre>";
			print_r($rset);
			print "</pre>";
		}
		$timelineurl = $conf_result_page . "?url=" . $encoded_url . "&query=" . $query . "&time=";
		
		
		foreach ($rset as $hit){
		  $prevyear = $year;
		  $year = substr($hit['date'],0,4);
		  if ($year != $prevyear) {
		    if (isset($prevyear)) {
		      print "</td></tr></table></td>";
		    }
		    print "<td valign=\"top\"><table width=\"150\">";
		    print "<tr><td class=\"norm\" align=\"center\"><b>$year</</td></tr><tr><td class=\"norm\" align=\"center\" valign=\"top\">";
		  }
		  print "<b><a target=\"_top\" href=\"" . $timelineurl . $hit['date'] . "\">";
		  print (substr($hit['date'],6,2) . "." . substr($hit['date'],4,2) . "." . $year . '</a><b><br/>');
		
		}
	}
}
else {
	print "<b>" . $s->getErrorMessage() . "</b> (<a href=\"" . $s->queryurl . "\">" . $s->queryurl . "</a>)";
}	
?>
</tr>
</table>
</td>
</tr>
</table>
</td>
</tr>
</table>
</td>
</tr>
</center>
</center>

<?php
  include($conf_includepath . "/footer.inc");
?>