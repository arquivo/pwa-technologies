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
 * File: top.php
 *
 * This file is the top-frame of the WERA version browser. This script implements
 * currents the timeline.
 * The top.php is only referenced from the result.php script
 *
 * $Id: top.php 845 2006-03-17 14:40:26Z sverreb $
 */
header("Content-Type: text/html; charset=UTF-8");

include_once ("lib/config.inc");
include ($conf_includepath."/timeline.inc");
include_once ($conf_searchenginepath."/indexUtils.inc");

//if register_globals is off:
$level = $_REQUEST['level'];
$manlevel = $_REQUEST['manlevel'];
$time = $_REQUEST['time'];
$url = $_REQUEST['url'];
$encoded_url = urlencode($url);
$meta = $_REQUEST['meta'];
$auto = $_REQUEST['auto'];
$query = $_REQUEST['query'];

$levels = array (0 => "Seconds", 1 => "Minutes", 2 => "Hours", 3 => "Days", 4 => "Months", 5 => "Years", 6 => "Auto");

?>
<?php


$images['first'] = array ('name' => "images/first.jpg", 'height' => 20, 'width' => 19);
$images['prev'] = array ('name' => "images/prev.jpg", 'height' => 20, 'width' => 13);
$images['next'] = array ('name' => "images/next.jpg", 'height' => 20, 'width' => 13);
$images['last'] = array ('name' => "images/last.jpg", 'height' => 20, 'width' => 19);

$images['line'] = array ('name' => "images/line.jpg", 'height' => 16, 'width' => 7);
$images['line_sep'] = array ('name' => "images/linemark.jpg", 'height' => 16, 'width' => 1);
$images['mark_one'] = array ('name' => "images/mark_one.jpg", 'height' => 20, 'width' => 7);
$images['mark_several'] = array ('name' => "images/mark_several.jpg", 'height' => 20, 'width' => 7);
$images['middle'] = array ('name' => "images/mark.jpg", 'height' => 6, 'width' => 8);

if ($auto != "on") {
  $level = $manlevel;
}
if (!($level >= 0 and $level <= 5) || (!isset ($level) || $level == "")) {
	$level = 6;
	$resolution = $level;
}

$result_page = $conf_result_page;
$timeline = new timeline($level, $time, $url, $conf_index_class);
$timeline_data = $timeline->getTimelineData();

if ($timeline_data == false) {
	$error = $timeline->getError();
	if ($error) {
		include ($conf_includepath."/header.inc");
		print $error;
		include ($conf_includepath."/footer.inc");
		die();
	}
	# else, no hits ...
}

if ($level != 6) {
	$resolution = $timeline->getResolution();
}

$number_of_versions = $timeline->getNumberOfVersions();
$next_version = $timeline->getNextVersionTimestamp();
$previous_version = $timeline->getPreviousVersionTimestamp();
$first_version = $timeline->getFirstVersionTimestamp();
$last_version = $timeline->getLastVersionTimestamp();
if ($time == "") {
	$time = $last_version;
}
$key_of_version_nearest_before = $timeline->getKeyOfCurrentVersion();

$first_version_display = make_display_time($first_version, -1);
$previous_version_display = make_display_time($previous_version, -1);
$next_version_display = make_display_time($next_version, -1);
$last_version_display = make_display_time($last_version, -1);
$current_version_display = make_display_time($time, -1);

/* Builds a date based on the resolution
 *
 * A resolution of -1 (or any value not corresponding
 * to a defined resolution type) will give a full date.
 */
function make_display_time($timestring, $resolution) {
	$year = substr($timestring, 0, 4);
	$month = substr($timestring, 4, 2);
	$day = substr($timestring, 6, 2);
	$hour = substr($timestring, 8, 2);
	$minute = substr($timestring, 10, 2);
	$second = substr($timestring, 12, 2);

	switch ($resolution) {
		case 0 : //Seconds
			return ($hour.":".$minute.":".$second);
		case 1 : //Minutes
			return ($hour.":".$minute);
		case 2 : //Hours
			return (day_format($day)." ".$hour.":".$minute);
		case 3 : //Days
			return (month_number_to_name($month).". ".day_format($day));
		case 4 : //Months
			return (month_number_to_name($month).". ".$year);
		case 5 : //Years
			return ($year);
	}
	// else full date is return (without seconds)
	return (month_number_to_name($month).". ".day_format($day)." ".$year.", ".$hour.":".$minute);
}

function day_format($day) {
	$temp = $day;
	$zeroleading = true;

	while ($temp > 10) {
		$temp = $temp -10;
		$zeroleading = false;
	}

	if ($zeroleading) {
		$day = substr($day, 1, 1);
	}

	if ($temp == 1) {
		return $day."st";
	}
	if ($temp == 2) {
		return $day."nd";
	}
	if ($temp == 3) {
		return $day."rd";
	}
	return $day."th";
}

function month_number_to_name($month) {
	switch ($month) {
		case 1 :
			return "Jan";
		case 2 :
			return "Feb";
		case 3 :
			return "Mar";
		case 4 :
			return "Apr";
		case 5 :
			return "May";
		case 6 :
			return "Jun";
		case 7 :
			return "Jul";
		case 8 :
			return "Aug";
		case 9 :
			return "Sep";
		case 10 :
			return "Okt";
		case 11 :
			return "Nov";
		case 12 :
			return "Des";
	}
	return "";
}

if ($conf_debug == 1) {
	print "<!--";
	print "\nResolution (in) : ".$level;
	print "\nTime (in) : ".$time;
	print "\nURL (in) : ".$url;
	print "\nResolution : ".$timeline->getResolution();
	print "\nNumber of versions : ".$number_of_versions;
	print "\nNext version : ".$next_version;
	print "\nPrevious version : ".$previous_version;
	print "\nFirst version : ".$first_version;
	print "\nLast version : ".$last_version;
	print "\n".'$key_of_version_nearest_before '.$key_of_version_nearest_before;
	print "\nTimeline array:\n";
	print_r($timeline_data);
	print "\nVersions:\n";
	print "-->";
}

/* The following is a quick fix way of limiting the size of the timeline based on the resolution.
 * Ideally this should be done at a lower level where the timeline info is first gathered.
 * This however makes changes to the interface simpler to implement.
 *
 * If there are fewer items in the timeline than $resolution_sizes specifies for that resolution
 * then the smaller timeline will be displayed without problems.
 *
 * The timeline is trimmed equally from both ends.  We assume that the number of return variables
 * is odd and that the selected item is the center item.
 */

$resolution_sizes = array (0 => 31, 1 => 31, 2 => 25, 3 => 31, 4 => 25, 5 => 13);

if ($resolution_sizes[$timeline->getResolution()] < count($timeline_data)) {
	$first = ((count($timeline_data) - $resolution_sizes[$timeline->getResolution()]) / 2) + 1;
	$last = ($first -1 + $resolution_sizes[$timeline->getResolution()]);
} else {
	$first = 1;
	$last = count($timeline_data);
}

// Create the timeline
$i = 1;
if (!empty ($timeline_data)) {
	foreach ($timeline_data as $key => $val) {
		if ($i >= $first && $i <= $last) {
			$link = $result_page."?url=".$encoded_url."&time=".$timeline_data[$key][linkvalue]."&auto=".$auto."&meta=".$meta."&query=".urlencode($query)."&level=";
			if ($i != $first) {
				$timelinestring .= "<!-- $i --><img border=0 width=".$images['line_sep']['width']." height=".$images['line_sep']['height']." src=".$images['line_sep']['name'].">";
			}

			if ($timeline_data[$key][versions] == 0) {
				$timelinestring .= "<a target=_top href=".$link.$resolution."><img border=0 width=".$images['line']['width']." height=".$images['line']['height']." src=".$images['line']['name']." title=\"".$timeline_data[$key][resolution_dependent_format]."\"></a>";
			}
			elseif ($timeline_data[$key][versions] == 1) {
				$timelinestring .= "<a target=_top href=".$link.$resolution."><img border=0 width=".$images['mark_one']['width']." height=".$images['mark_one']['height']." src=".$images['mark_one']['name']." title=\"".make_display_time($timeline_data[$key][linkvalue], -1)."\"></a>";
			} else {
				if ($resolution == 6) {
					$tmp_resolution = $resolution;
				} else {
					$tmp_resolution = $resolution -1;
				}
				$timelinestring .= "<a target=_top href=".$link.$tmp_resolution."><img border=0 width=".$images['mark_several']['width']." height=".$images['mark_several']['height']." src=".$images['mark_several']['name']." title=\"".$timeline_data[$key][resolution_dependent_format]." (".$timeline_data[$key][versions]." versions)\"></a>";
			}

			if ($i == $first) {
				$firstItem = make_display_time($timeline_data[$key][linkvalue], $timeline->getResolution());
			}
			if ($i == $last) {
				$lastItem = make_display_time($timeline_data[$key][linkvalue], $timeline->getResolution());
			}
		}
		$i = $i +1;
	}
}
include ($conf_includepath."/header.inc");
?>

<script language="javascript">

  function enableAuto() {
    document.timeline.level.value = 6;		
    document.timeline.auto.value="on"
    document.timeline.submit()
  }

  function disableAuto() {
    document.timeline.level.value = document.timeline.autolevel.value;		
    document.timeline.auto.value="off"
    document.timeline.submit()
  }
  
  function enableMeta() {
    if (document.timeline.auto.value == "on") { 
      document.timeline.level.value = 6
    }
    document.timeline.meta.value="on"
    document.timeline.submit()
  }

  function disableMeta() {
    if (document.timeline.auto.value == "on") { 
      document.timeline.level.value = 6
    }
    document.timeline.meta.value="off"
    document.timeline.submit()
  }      
  
  function changeResolution() {
    document.timeline.auto.value="off"
    document.timeline.submit()
  }

</script>

  </head>

  <body>
<table width="100%" border='0' cellpadding='0' cellspacing='0'>

  <tr>
    <td colspan='2' class='border'><img alt='' height='2' src='images/1px.gif' width='1'></td>
  </tr>
  <tr>
    <td colspan='2'><img src='images/1px.gif' width='1' height='5' alt=''></td>
  </tr>
    
      <tr>
        <td width="1"></td>
        <td>
          <table cellspacing="0" border="0" cellpadding="0" width="100%">
            <tr>
              <td width="1"  class="shade"></td>
              <td align="left" valign="middle">&nbsp;&nbsp;<span class="caption">Uri:</span>&nbsp;&nbsp;</td>
              <!--<form name="loc" method="GET" target="_top" action="result.php">-->
              <form name="loc" method="GET" target="_top" action="<?php print $result_page; ?>">
              <td align="left" valign="middle">
                    <input name="url" type="text" size="70" value="<?php print $url; ?>" class="searchtext">
                    <input type="hidden" name="level" value="<?php print($level);?>">
                    <input type="hidden" name="time" value="<?php print($time);?>">
                    <input type="hidden" name="auto" value="<?php print($auto);?>">
                    <input type="hidden" name="meta" value="<?php print($meta);?>">
                    <input type="hidden" name="query" value="<?php print($query);?>">
               </td>
               </form>

              <td align="left" valign="middle">
                <input type='button' onClick="document.loc.submit()" value='<?php print nls("Go");?>'/>
              </td>
              <td width="50%" nowrap></td>
              <td align="left" valign="middle">&nbsp;<span class="caption"><?php print nls("Search");?></span>&nbsp;&nbsp;</td>
              <form name="search" target="_top" action="<?php print $conf_simple_search ?>">
              <td align="left" valign="middle" nowrap>

                  <input type="text" size="30" name="query" value="<?php print $query;?>">
                  <input type="hidden" name="querytype" value="all">
                  <input type='submit' value='<?php print(nls("Go"));?>'/>
              </td>
              </form>
            </tr>
          </table>
        </td>
      </tr>





      <tr>
        <td width="1" nowrap></td>
        <td>
          <table cellspacing="0" border="0" cellpadding="0"  width="100%">
            <tr>
              <td width="1" nowrap></td>
              <td>
                <!-- Viewing -->
                <table cellspacing="0" border="0" cellpadding="0" width="100%">
                  <tr>
                    <td>
                      <span class="smallboldfont">
                      <?php


if ($number_of_versions > 0) {
	print nls("Viewing")." ".nls("version")." ";
	//if (empty($key_of_version_nearest_before)) {
	if ($previous_version == "") {
		print 1;
	} else {
		print $key_of_version_nearest_before;
	}
	print " ".nls("of")." ".$number_of_versions;
}
?>&nbsp;
                      </span>
                    </td>
                  </tr>
                  <tr>
                    <td nowrap>
                      <span class="title"><?php print $current_version_display; ?></span>&nbsp;&nbsp;
                    </td>
                  </tr>
                </table>
              </td>
              <td width="400">
                <!-- Timeline -->
                <table cellspacing="0" border="0" cellpadding="0" width="100%">
                  <tr>
                    <td width="50%"></td>
                    <td>
                      <table cellspacing="0" border="0" cellpadding="0"  width="100%">
                        <tr>
                          <td width="48%" nowrap><span class="smallfont"><?php print $firstItem ?></span></td>
                          <td align="center" valign="bottom" nowrap><img src="<?php print $images['middle']['name'];?>"></td>
                          <td width="48%" nowrap align="right"><span class="smallfont"><?php print $lastItem ?></span></td>
                        </tr>
                      </table>
                    </td>
                    <td width="50%"></td>
                  </tr>
                  <tr>
                    <td nowrap align="right">
                      <?php


// Previous versions arrows
if ($previous_version != "") {
	print "<a target=_top href=".$result_page."?url=".$encoded_url."&time=".$first_version."&auto=".$auto."&meta=".$meta."&query=".urlencode($query)."&level=".$level.">";
	print "<img border=0 width=".$images['first']['width']." height=".$images['first']['height']." src=\"".$images['first']['name']."\" title=\"First version (".$first_version_display.")\"></a>";
	print "<a target=_top href=".$result_page."?url=".$encoded_url."&time=".$previous_version."&auto=".$auto."&meta=".$meta."&query=".urlencode($query)."&level=".$level.">";
	print "<img border=0 width=".$images['prev']['width']." height=".$images['prev']['height']." src=\"".$images['prev']['name']."\" title=\"Previous version (".$previous_version_display.")\"></a>";

} else {
	print "<img border=0 width=\"".$images['first']['width']."\" height=\"".$images['first']['height']."\" src=\"".$images['first']['name']."\">";
	print "<img border=0 width=\"".$images['prev']['width']."\" height=\"".$images['prev']['height']."\" src=\"".$images['prev']['name']."\">";

}
?>
                      
                    </td>
                    <td>
                      <?php print $timelinestring ?>
                    </td>
                    <td nowrap>
                      
                      <?php


// Next versions arrows
if ($next_version != "") {
	print "<a target=_top href=".$result_page."?url=".$encoded_url."&time=".$next_version."&auto=".$auto."&meta=".$meta."&query=".urlencode($query)."&level=".$level.">";
	print "<img border=0 width=".$images['next']['width']." height=".$images['next']['height']." src=\"".$images['next']['name']."\" title=\"Next version (".$next_version_display.")\"></a>";
	print "<a target=_top href=".$result_page."?url=".$encoded_url."&time=".$last_version."&auto=".$auto."&meta=".$meta."&query=".urlencode($query)."&level=".$level.">";
	print $lastarrow = "<img border=0 width=".$images['last']['width']." height=".$images['last']['height']." src=\"".$images['last']['name']."\" title=\"Last version (".$last_version_display.")\"></a>";
} else {
	print "<img border=0 width=".$images['next']['width']." height=".$images['next']['height']." src=\"".$images['next']['name']."\">";
	print "<img border=0 width=".$images['last']['width']." height=".$images['last']['height']." src=\"".$images['last']['name']."\">";

}
?>
                    </td>
                  </tr>
                </table>
              </td>
              <td align="right">
                <!-- Resolution -->
                      <form name="timeline" method="GET" target="_top" action="<?php print $result_page; ?>">
												<input type="hidden" name="auto" value="<?php print $auto; ?>">
												<input type="hidden" name="meta" value="<?php print $meta; ?>">  
                        <input type="hidden" name="query" value="<?php print($query);?>">                    
                        <input type="hidden" name="url" value="<?php print $url; ?>">
                        <input type="hidden" name="time" value="<?php print $time; ?>">
                        <input type="hidden" name="level" value="<?php print $resolution; ?>">
                        <input type="hidden" name="autolevel" value="<?php print $timeline->getResolution(); ?>">
                        <?php print nls("Resolution");?>:
                        <select NAME="manlevel" SIZE="1" onChange="changeResolution()">
                      <?php


//for ($i = 1; $i <= 5; $i++) {
for ($i = 5; $i >= 1; $i --) {
	print "<option ";
	if ($resolution != 6) {
		if ($resolution == $i) {
			print "selected ";
		}
	} else {
		if ($timeline->getResolution() == $i) {
			print "selected ";
		}
	}
	print "value=\"$i\">".nls($levels[$i]);
}
print "</select>\n";
print '&nbsp;Auto:';
if ($auto == "on") {
	print '<input type="checkbox" name="autocheckbox" value="1" onClick="disableAuto()" checked>';
}
else {
	print '<input type="checkbox" name="autocheckbox" value="1" onClick="enableAuto()">';
}

print '&nbsp;Metadata:';
if ($meta == "on") {
	print '<input type="checkbox" name="metacheckbox" value="1" onClick="disableMeta()" checked>';
}
else {
	print '<input type="checkbox" name="metacheckbox" value="1" onClick="enableMeta()">';
}

print "&nbsp<a href=\"".$conf_helplinks['search']['file']."\" target=\"_top\">";
print (nls($conf_helplinks['search']['name'])."</a>");
?>

             </form>
                    
              </td>
              <td><img alt='' height='1' src='images/1px.gif' width='5'>
              </td>
            </tr>
          </table>
        </td>
        </tr>
        <tr>
        <td colspan='2' class='shade'><img alt='' height='5' src='images/1px.gif' width='1'></td>
        </tr>
        <tr>
    <td colspan='2' class='border'><img alt='' height='2' src='images/1px.gif' width='1'></td>
        </tr>

    </table>
<?php


include ($conf_includepath."/footer.inc");
?>


