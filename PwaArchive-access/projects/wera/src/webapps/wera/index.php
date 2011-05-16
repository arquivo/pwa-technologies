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
 * File: index.php
 *
 * $Id: index.php 648 2006-01-12 20:25:57Z sverreb $
*/

header("Content-Type: text/html; charset=UTF-8");

include_once ("lib/config.inc");
include ($conf_index_file);

//if register_globals is off
$time_search = $_REQUEST['time_search'];
$year_from = $_REQUEST['year_from'];
$year_to = $_REQUEST['year_to'];
$query = $_REQUEST['query'];
$querytype = $_REQUEST['querytype'];
$start = $_REQUEST['start'];
$debug = $_REQUEST['debug'];
$showall = $_REQUEST['showall'];

if (strpos($query, 'site:') !== false) {
	$showall = TRUE;
}
	
include($conf_includepath . "/header.inc");

?>
</HEAD><body><center>
<?php

if ($year_from == "" and $year_to == "") {
	$query_time = "";
} else {
	$first_year = "0000";
	$today = getdate();
	$next_year = $today['year'] + 1;
	if ($year_from == "") {
		$year_from = $first_year."0101000000";
	}
	if ($year_to == "") {
		$year_to = $next_year;
	}
	if ($year_from < $first_year) {
		$year_from = $first_year;
	}
	if ($year_to < $first_year) {
		$year_to = $first_year +1;
	}
	elseif ($year_to > $next_year) {
		$year_to = $next_year;
	}
	if ($year_from > $year_to) {
		$year_from = $first_year;
	}
	$query_time = "date:".$year_from."0101000000-".$year_to."0101000000 ";
}
?>

<table border='0' cellpadding='0' cellspacing='0' width=90%>
  <tr>
    <td class="norm" width="1" colspan="5"><img alt='' height='10' src='/images/1px.gif' width="1"></td>
  </tr>
  <tr>
    <td class="norm" width="10"><img alt='' height='1' src='/images/1px.gif' width="1"></td>
    <td class="norm" colspan="5" align="left"><img alt="" src="<?php print $conf_logo;?>"></td>
  </tr>
</table>
      
<table border='0' cellpadding='0' cellspacing='0' width=90%>
  <tr>
    <td class="norm" colspan="4" align="left"><img alt='' height='8' src='/images/1px.gif' width='1'></td>
  </tr>
  <tr>
    <td colspan='4' class='border'><img alt='' height='2' src='/images/1px.gif' width='1'></td>
  </tr>
  <tr>
    <td colspan='4'><img src='/images/1px.gif' width='1' height='5' alt=''></td>
  </tr>
  <tr>
    <td class="shade" width="10"><img alt='' height='1' src='/images/1px.gif' width="1"></td>
    <td class="shade"><?php print(nls("Query:"));?></td>
    <td class="shade" align="right"><a href="<?php print($conf_helplinks['search']['file']);?>">
    <?php print(nls($conf_helplinks['search']['name']));?></a></td>
    <td class="shade" width="10"><img alt='' height='1' src='/images/1px.gif' width="1"></td>
  </tr>
  <tr>
    <td class="shade" width="10"><img alt='' height='1' src='/images/1px.gif' width="1"></td>
<?php
$query = trim(stripslashes($query));
?>

    <td colspan="3">
      <form name='search' action=<? echo $_SERVER['PHP_SELF']; ?> method='get'>
      <input type='text' name='query' value='<?php print $query; ?>' class="searchtext" size="50"/>
      <input type='submit' value='<?php print(nls("Search"));?>' class="searchbutton"/>
    </td>
  </tr>
  <tr>
    <td class="shade" height="30">&nbsp;</td>
    <td colspan="3" class="shade" valign="bottom"><?php print(nls("Year"));?> <?php print(nls("(from - to)"))?></td>
  </tr>
  <tr>
    <td class="shade" height="30">&nbsp;</td>
    <td colspan="3">
      <input name='year_from' size=4 maxlength="4" type="text" value='<?php print $year_from;?>'/>&nbsp;-&nbsp;
      <input name='year_to' maxlength="4" value='<?php print $year_to; ?>' size=4 type="text"/>
<?php

if (isset ($debug)) {
	print "<input type=\"hidden\" name=\"debug\" value=\"$debug\">";
}

?>
    </td>
  </tr>
</form>

  <tr><td height="20" colspan="5"></td></tr>
</table>
    
<table align="center" class="greyborder" border="0" cellspacing="0" cellpadding="1" width="90%">
  <tr>
    <td>
      <table align="center" class="resultsborder" border="0" cellspacing="0" cellpadding="10" width="100%">
        <tr>
          <td>
    
<?php


$time_str = "Date range";

if ($query == "") {
	include ("./info.php");
} else {
	if ($querytype == "phrase") {
		$query = '"'.$query.'"';
	}
	$parsedquery = $query;
	if ($query_time != "") {
		if ($parsedquery == "") {
			$querystring = trim($query_time);
		} else {
			$querystring = $parsedquery." ".trim($query_time);
		}
	} else {
		$querystring = $parsedquery;
	}
	if (empty ($start)) {
		$start = 1;
	}

	$fields = "url date title dctitle";

	if (isset ($size)) {
		$sizeofresultset = $size;
	} else {
		$sizeofresultset = 10;
	}

	$sortorder = "relevance";

	if ($querystring != "") {
		$search = new $conf_index_class ();
		$search->setQuery(urlencode($querystring));
		$search->setSortorder("relevance");
		$search->setSizeOfResultSet($sizeofresultset);
		$search->setOffset($start -1);
		$search->setFieldsInResult("title url description archiveidentifier site");
		if ($showall) {
			#$search->setDedup(0);
      $search->setDedup(1,'exacturl');
		}
		else {
			$search->setDedup(1);
		}

		if ($search->doQuery()) {
			$numhits = $search->getnumhits();
			$total = $search->getNumHitsTotal();
			$results = $search->getResultSet();
			if ($conf_debug or $debug) {
				print "DEBUG : Query url : <a href=\"" . $search->queryurl . "\">" . $search->queryurl . "</a><br>&nbsp;<br>";
			}
			if ($total > 0) {
				print (nls("Total number of versions found")." : <b>$total</b>. ");
				if ($showall) {
					print (nls("Displaying URL's"));
				}
				else {
					print (nls("Displaying highest ranked URL from each site") . ", ");
				}
				print " <b>$start-$numhits</b>";
				print "</td></tr>";
				print "<tr><td class=\"norm\">";
				$hits_in_set = 0;
				foreach ($results as $key => $value) {
					$hits_in_set++; 
					print "<b>".$key.". ".$value['title']."</b> ";
					print "(".$value['url'].")<br>";
					print "(".$value['description'].")<br>";
					
					if ($conf_show_num_verions && $showall) {
						$search2 = new $conf_index_class ();
						$search->setDedup(0);
						$search2->setSortorder("descending");
						$search2->setSizeOfResultSet(1);
						$search2->setOffset(0);
						$search2->setFieldsInResult(date);						
						$numversions_text1 = "";
						$numversions_text2 = "";

						if ($conf_show_num_verions_matching_query and !strstr($querystring, "exacturl:")) {
							$vquery = urlencode($querystring . " exacturl:" . $value["url"]);
							$search2->setQuery($vquery);
							if ($search2->doQuery()) {
								$versions = $search2->getResultSet();
								$numversions = $search2->getNumHitsTotal();
								if ($conf_debug or $debug) {
									$count_versions_matching_queryurl = $search2->queryurl;
								}
							}
							else {
								$numversions =  "<b>? <a href=\"" . $search2->queryurl . "\">" . $search2->getErrorMessage() . "</a></b>";
							}
							$numversions_text1 = "(" . nls("matching query")."/". nls(total) . ")";
							$numversions_text2 = $numversions."/"; 
						}

						
						$search2->setQuery("exacturl:" . urlencode($value["url"]));
						if ($search2->doQuery()) {
							$totalversions = $search2->getNumHitsTotal();
							if ($conf_debug or $debug) {
								$count_versions_total_queryurl = $search2->queryurl;
							}							
						}
						else {
							$totalversions =  "<b>? <a href=\"" . $search2->queryurl . "\">" . $search2->getErrorMessage() . "</a></b>";
						}
						
						print nls("Versions") . " ";
						print $numversions_text1 . " ";
						print $numversions_text2 . $totalversions."<br>";
						if ($conf_debug or $debug) {
							if (isset($count_versions_matching_queryurl)) {
								print "DEBUG : Url for counting versions matching query : <a href=\"" . $count_versions_matching_queryurl. "\">" . $count_versions_matching_queryurl . "</a><br/>"; 
							}
							if (isset($count_versions_total_queryurl)) {
								print "DEBUG : Url for counting versions total : <a href=\"" . $count_versions_total_queryurl. "\">" . $count_versions_total_queryurl . "</a><br/>"; 
							}							
						}
					}


					$linkstring = "<a href=\"result.php?time=".$versions[1]['date']."&url=".urlencode($value["url"])."&query=".$query."\">".nls("Timeline")."</a>";
					$overview = "<a href=\"overview.php?url=".urlencode($value["url"])."&query=".$query."\" >".nls("Overview")."</a>";
					$morefromsite ="<a href=\"index.php?query=" . urlencode("site:". $value['site'] . " " . $query) . "\" >".nls("More from this site")."</a>";
					print "<b> $linkstring | $overview";
					if(!$showall) {
						print " | $morefromsite";
					} 
					print "</b>";
					print "<br>&nbsp;<br>";
					$last_hit = $key;
					unset($search2);
				}

				$next_start = $start + $sizeofresultset;

				print "<b>Results: ";

				$url_querypart = "query=".urlencode($query);
				$url_querypart .= "&querytype=".$querytype;
				$url_querypart .= "&year_from=".$year_from;
				$url_querypart .= "&month_from=".$month_from;
				$url_querypart .= "&day_from=".$day_from;
				$url_querypart .= "&year_to=".$year_to;
				$url_querypart .= "&month_to=".$month_to;
				$url_querypart .= "&day_to=".$day_to;
				$url_querypart .= "&site=".$site;
				$url_querypart .= "&language=".$language;
				$url_querypart .= "&format".$format;

				if ($start > 1) {
					$prev_start = $start - $sizeofresultset;
					print "  <a href=\"".$_SERVER['PHP_SELF']."?".$url_querypart."&start=".$prev_start."&showall=".$showall." \">&lt;&lt; ".nls("Prev")."</a> | ";
				}

				for ($i = 1;; $i ++) {
					$high_lim = $i * $sizeofresultset;
					$low_lim = $high_lim +1 - $sizeofresultset;

					if ($high_lim >= $last_hit) {
						if ($start == $low_lim) {
							print $low_lim."-".$last_hit." | ";
						} else {
							print "  <a href=\"".$_SERVER['PHP_SELF']."?".$url_querypart."&start=".$low_lim."&showall=".$showall." \">".$low_lim."-".$last_hit."</a> | ";
						}
						break;
					} else {
						if ($start == $low_lim) {
							print $low_lim."-".$high_lim." | ";
						} else {
							print "  <a href=\"".$_SERVER['PHP_SELF']."?".$url_querypart."&start=".$low_lim."&showall=".$showall." \">".$low_lim."-".$high_lim."</a> | ";
						}
					}
				}
				if ($hits_in_set == $sizeofresultset and $search->morepages) {
					print "  <a href=\"".$_SERVER['PHP_SELF']."?".$url_querypart."&start=".$next_start."&showall=".$showall."\">".nls("Next")." &gt;&gt;</a>";
				}
				if (!$showall) {
					print "  |  <a href=\"".$_SERVER['PHP_SELF']."?".$url_querypart."&start=".$start."&showall=true\">".nls("Show all")."</a>";
				}
				print "</b>";

			} else {
				print (nls("No hits") . "!");
			}
			if ($conf_debug or $debug) {
				print "<br>&nbsp<br>DEBUG : Result set :";
				print "<pre>";
				print_r($results);
				print "</pre>";
			}
		
		}
		else {
			print "<b>" . $search->getErrorMessage() . "</b> (<a href=\"" . $search->queryurl . "\">" . $search->queryurl . "</a>)";
		}
	}
}
?>

          </td>
        </tr>

      </table>
    </td>
  </tr>
</table>
<table border="0" class="resultsborder" width="90%" cellpadding="10">
  <tr>
    <td align="left" class="norm">
     <a href="http://netpreserve.org"><img alt='' border='0' src='<?php print $conf_http_host;?>/images/iipc.png'></a>
    </td>      
    <td align="right" class="norm"> 
      <a href="./articles/manual.html"><?php print(nls("Manual"));?></a>&nbsp;&nbsp;|&nbsp;&nbsp;<a href="./articles/releasenotes.html"><?php print(nls("Release Notes"));?></a>&nbsp;&nbsp;|&nbsp;&nbsp;<a href="http://sourceforge.net/tracker/?group_id=118427&atid=681137"><?php print(nls("Report bugs"));?></a>
    </td>
  </tr>
</table>
</center>
<?php
  include($conf_includepath . "/footer.inc");
?>