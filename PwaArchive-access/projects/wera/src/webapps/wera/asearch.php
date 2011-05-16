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
 * File: asearch.php
 *
 * $Id: asearch.php 357 2005-10-05 01:38:18Z sverreb $
*/


header("Content-Type: text/html; charset=UTF-8");


//if register_globals is off
 $query = $_REQUEST['query'];
 $querytype = $_REQUEST['querytype'];
 $year_from = $_REQUEST['year_from'];
 $month_from = $_REQUEST['month_from'];
 $day_from = $_REQUEST['day_from'];
 $year_to = $_REQUEST['year_to'];
 $month_to = $_REQUEST['month_to'];
 $day_to = $_REQUEST['day_to'];
 $field = $_REQUEST['field'];
 $collection = $_REQUEST['collection'];
 $language = $_REQUEST['language'];
 $format = $_REQUEST['format'];
 $start = $_REQUEST['start']; 
 
 
include_once("lib/config.inc");
include($conf_index_file);
include($conf_includepath . "/time.inc");
include($conf_includepath . "/url.inc");
//include($conf_includepath . "/asearch_collections.inc");
//include($conf_includepath . "/asearch_sites.inc");
include($conf_includepath . "/asearch_languages.inc");
include_once($conf_result_list);

$myself = $_SERVER['PHP_SELF'];

/* ------------------------------------------------------------------------------------- */

if ($year_from == "" and $year_to == "") {
  $query_time = "";
  $month_from = "";
  $day_from = "";  
  $month_to = "";
  $day_to = "";  
}
else {
  $firstyear = "2002";
  if ($year_from == "") {
    $from = "";
    $month_from = "";
    $day_from = "";
  }
  else {
    //Has to be exact four digits
    $from = "000" . $year_from;
    $from = substr($from, -4);
    if ($from < $firstyear) {
      $from = $firstyear;
      $year_from = $firstyear;
    }
    if ($month_from != "") {
      $month_from = "0" . $month_from;
      $month_from = substr($month_from, -2);
      if ($month_from > 12) {
        $month_from = "12";
      }
    }
    else {
      $month_from = "01";
    }
    
    if ($day_from != "") {
      $day_from = "0" . $day_from;
      $day_from = substr($day_from, -2);
      if ($day_from > 31) {
        $day_from = "31";
      }
    }
    else {
      $day_from = "01";
    }
    $from = $from . $month_from . $day_from . "000000";
  }
  if ($year_to == "") {
    $to = "";
    $month_to = "";
    $day_to = "";  
  }
  else {
    //Has to be exact four digits
    $to = "000" . $year_to;
    $to = substr($to, -4);
    $today = getdate();
    if ($to < $firstyear) {
      $to = $firstyear + 1;
    }
    elseif ($to > $today['year']) {
      $to = $today['year'] + 1;
      $year_to = $to;
    }
    
    if ($month_to != "") {
      $month_to = "0" . $month_to;
      $month_to = substr($month_to, -2);
      if ($month_to > 12) {
        $month_to = "12";
      }
    }
    else {
      $month_to = "01";
    }
    
    if ($day_to != "") {
      if ($day_to > 31) {
        $day_to = "31";
      }
      $day_to = "0" . $day_to;
      $day_to = substr($day_to, -2);
    }
    else {
      $day_to = "01";
    }    
    $to = date("Ymd", mktime(0, 0, 0, $month_to , $day_to, $to)) . "000000";
  }
  
  if ($year_to != "" and $year_from > $year_to) {
    $from = "";
    $year_from = "";
  }
  $time_search = $from . ";" . $to;
  $query_time = "date:[$time_search] ";
}

/* ------------------------------------------------------------------------------------- */


?>
<HTML>
<HEAD>
<link rel="stylesheet" href="<?php print $conf_gui_style;?>" type="text/css">
<META HTTP-EQUIV="Cache-Control" Content="must-revalidate">
<META Http-Equiv="Pragma" Content="no-cache">
<META Http-Equiv="Expires" Content="0">
<META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=UTF-8">
<TITLE><?php print(nls("NWA search"));?></TITLE>
<script language="javascript">

</script>
</HEAD>
<body>
<center>
<table border='0' cellpadding='0' cellspacing='0' width=90%>
  <tr>
    <td class="norm" width="1" colspan="5"><img alt='' height='10' src='images/1px.gif' width="1"></td>
  </tr>
  <tr>
    <td class="norm" width="10"><img alt='' height='1' src='images/1px.gif' width="1"></td>
    <td class="norm" align="left"><img alt="" src="<?php print $conf_logo;?>"></td>
    <td class="norm" align="right">
        <?php 
          print "<a href=\"$conf_simple_search?query=$query&querytype=$querytype&year_from=$year_from&year_to=$year_to\">";
          print nls("Simple search") . "</a>";
        ?>
    </td>    
    <td class="norm" width="10"><img alt='' height='1' src='images/1px.gif' width="1"></td>    
  </tr>
</table>

<table border='0' cellpadding='0' cellspacing='0' width=90%>
  <tr>
    <td class="norm" colspan="5" align="left"><img alt='' height='8' src='images/1px.gif' width='1'></td>
  </tr>
  <tr>
    <td colspan='5' class='border'><img alt='' height='2' src='images/1px.gif' width='1'></td>
  </tr>
  <tr>
    <td colspan='5'><img src='images/1px.gif' width='1' height='5' alt=''></td>
  </tr>
  <tr valign="bottom">
    <td class="shade" width="10"><img alt='' height='1' src='images/1px.gif' width="1"></td>
    <td class="shade" width="5%"><?php print(nls("Match"));?>:</td>
    <td class="shade"><?php print(nls("Query:"));?></td>
    <td class="shade" align="right"><a href="<?php print($conf_helplinks['search']['file']);?>">
    <?php print(nls($conf_helplinks['search']['name']));?></a></td>
    <td class="shade" width="10"><img alt='' height='1' src='images/1px.gif' width="1"></td>
  </tr>
  <tr>
    <td class="shade" width="10"><img alt='' height='1' src='images/1px.gif' width="1"></td>
    <td class="shade"><form name='search' action=<? echo $_SERVER['PHP_SELF']; ?> method='get'>
        <?php
          $query=trim(stripslashes($query));
          if ($querytype=="phrase") {
           $query = str_replace('"', '', $query);
          }
        ?>
        <select name='querytype'>
         <option value=all <?php if ($querytype=="all") print "selected"?>><?php print(nls("All words"));?>
         <option value=any <?php if ($querytype=="any") print "selected"?>><?php print(nls("Any word"));?>
         <option value=phrase <?php if ($querytype=="phrase") print "selected"?>><?php print(nls("Exact phrase"));?>
        </select>&nbsp;
    </td>
    <td class="shade" colspan="3">
      <input type='text' name='query' value='<?php print $query; ?>' size="50"/>
      <input type='submit' value='<?php print(nls("Search"));?>' onClick="submitForm(0);"/>
    </td>
  </tr>

  <tr valign="bottom">
    <td class="shade" width="10" height="20"><img alt='' height='1' src='images/1px.gif' width="1"></td>
    <td class="shade" colspan="1">&nbsp;</td>
    <td class="shade" colspan="3"><?php print(nls("From - To"))?> (<?php print(nls("YYYY MM DD"))?>):</td>
  </tr>
  <tr>
    <td class="shade" width="10"><img alt='' height='1' src='images/1px.gif' width="1"></td>
    <td class="shade" colspan="1">&nbsp;</td>
    <td class="shade" colspan="3">
      <input name='year_from' size="4" maxlength="4" type='text' value='<?php print $year_from; ?>'>
      <input name='month_from' maxlength="2" value='<?php print $month_from; ?>' size=2 type="text">
      <input name='day_from' maxlength="2" value='<?php print $day_from; ?>' size=2 type="text">
      &nbsp;-&nbsp;
      <input name='year_to' maxlength="4" size=4 type="text" value='<?php print $year_to; ?>'>
      <input name='month_to' maxlength="2" value='<?php print $month_to; ?>' size=2 type="text">
      <input name='day_to' maxlength="2" value='<?php print $day_to; ?>' size=2 type="text">
    </td>
  </tr>
    

<!--- Start adding selections in order to narrow the search ------>
<!--
  <tr valign="bottom">
    <td class="shade" width="10" height="30"><img alt='' height='1' src='images/1px.gif' width="1"></td>  
    <td class="shade" align="right"><?php print(nls("Field"))?>&nbsp;</td>
    <td  colspan="3"><select name="field">

<option value=all selected <?php if ($field=="all") print "selected"?>><?php echo nls("All");?></option>
<?php
foreach ($fields as $name => $code) {
  echo "<option value=\"$code\"";
  if ($code == $field) {
    echo " selected ";
  }
  echo ">" . nls($name) . "</option>";
}
?>

    </select>
    </td>
  </tr>
  <tr valign="bottom">
    <td class="shade" width="10" height="30"><img alt='' height='1' src='images/1px.gif' width="1"></td>  
    <td class="shade" align="right"><?php echo nls("Collection");?>&nbsp;</td>
    <td class="shade" colspan="3"><select name="collection" class="shade">
      <option value=all><?php echo nls("All collections")?></option>

      <?php

foreach ($collections as $name => $code) {
  echo "<option value=\"$code\"";
  if ($code == $collection) {
    echo " selected ";
  }
  echo ">" . nls($name) . "</option>";
}

?>

      </select>
    </td>
  </tr>-->
  <tr valign="bottom">
    <td class="shade" width="10" height="30"><img alt='' height='1' src='images/1px.gif' width="1"></td>  
    <td class="shade" align="right"><?php echo nls("Site");?>&nbsp;</td>
    <td class="shade" colspan="3">
    <!--<select name="site" class="shade">
      <option value=all><?php echo nls("All sites")?></option>-->
      <input type='text' name='site' value='<?php print $site; ?>' size="30"/>
      <?php
/*
foreach ($sites as $name => $code) {
  echo "<option value=\"$code\"";
  if ($code == $site) {
    echo " selected ";
  }
  echo ">" . $name . "</option>";
}
*/
?>

      <!--</select>-->
    </td>
  </tr>
  <!--<tr valign="bottom">
    <td class="shade" width="10" height="30"><img alt='' height='1' src='images/1px.gif' width="1"></td>  
    <td class="shade" align="right"><?php echo nls("Language")?>&nbsp;</td>
    <td class="shade" colspan="3"><select name="language" class="shade">
      <option value="all"><?php echo nls("All languages")?></option>-->
      <?php
/*
foreach ($languages as $name => $code) {
  echo "<option value=\"$code\"";
  if ($code == $language) {
    echo " selected ";
  }
  echo ">" . nls($name) . "</option>";
  
}*/

      ?>
    <!--  </select>
    </td>
  </tr>-->


  <tr valign="bottom">
    <td class="shade" width="10" height="30"><img alt='' height='1' src='images/1px.gif' width="1"></td>  
    <td class="shade" align="right"><?php echo nls("File format");?>&nbsp;</td>
    <td class="shade" colspan="3">
      <select name="format" class="shade">
        <option value=all <?php if ($format=="all") print "selected"?>><?php echo nls("All file formats");?></option>
        <option value=text <?php if ($format=="text") print "selected"?>><?php echo nls("Text files");?></option>
        <option value=pdf <?php if ($format=="pdf") print "selected"?>><?php echo nls("PDF files");?></option>        
        <option value=image <?php if ($format=="image") print "selected"?>><?php echo nls("Images");?></option>
        <option value=sound <?php if ($format=="sound") print "selected"?>><?php echo nls("Sound and music files");?></option>
      </select>
    </td>
  </tr>

</form>

<!--------------------------------------------------------->

  <tr valign="bottom">
    <td class="shade" width="10" height="20"><img alt='' height='1' src='images/1px.gif' width="1"></td>  
    <td class="shade" height="10" colspan="3"><hr></td>
    <td class="shade" width="10"><img alt='' height='1' src='images/1px.gif' width="1"></td>    
  </tr>

  <tr>
    <td class="shade" width="10"><img alt='' height='1' src='images/1px.gif' width="1"></td>  
    <td class="shade" colspan="4" height="10"><?php print nls("Go directly to a document with this URL");?></td>
  </tr>

  <tr>
    <td class="shade" width="10"><img alt='' height='1' src='images/1px.gif' width="1"></td>
    <td class="shade" colspan="4">
    <?php
     $time = date("YmdGis"); // Current data and time
    ?>
      <form name="loc" method="GET" target="_top" action="result.php">
                    <input name="url" type="text" size="50" value="<?php print $url; ?>"
                    <input type="hidden" name="time" value="<?php print($time);?>">  
                    <input type='submit' value='<?php print(nls("&nbsp;Go&nbsp;"));?>'>
       </form>
       </td>
    </tr>

  <tr><td height="20" colspan="5"></td></tr>
    
  </table>

<table align="center" class="greyborder" border="0" cellspacing="0" cellpadding="1" width="90%">
  <tr>
    <td>
      <table align="center" class="resultsborder" border="0" cellspacing="0" cellpadding="10" width="100%">
        <tr>
          <td>
<?php
  
  $morequery = "";

  if ($collection != "" and strcmp($collection,"all")!=0){
    $morequery = " collection:\"$collection\"";
  }
  if ($site != "" and strcmp($site,"all") != 0){
    $morequery = $morequery . " site:$site";
  }
  if ($language != "" and strcmp($language,"all")!=0){
    $morequery = $morequery . " language:$language";
  }
  if (strcmp($format,"text")==0){
    $morequery = $morequery . " primarytype:text";
  }
  if (strcmp($format,"pdf")==0){
    $morequery = $morequery . " subtype:application/pdf";
  }

  if (strcmp($format,"image")==0){
    $morequery = $morequery .
    " primarytype:image";
  }
  if (strcmp($format,"sound")==0){
     $morequery = $morequery . " primarytype:audio primarytype:sound";
  }
  if ($query_time != "") {
    $morequery = $morequery . " " . $query_time;
  }

  
  if ($query == "" and $morequery == "") {
      include("./info.php");
  }
  elseif ($query!="") {
    if ($querytype=="phrase") {
      $query = '"' . $query . '"';
      $parsedquery = "$query";
    }
    elseif ($querytype == "any" ) {
      $parsedquery = "($query)";
    }
    else {
      $parsedquery = "$query";
    }

    if ($morequery != "") {
      $querystring = $parsedquery . " " . $morequery;
    }
    else {
      $querystring = $parsedquery;
    }
  }
  
  if (empty($start)) {
    $start = 1;
  }

  $fields = "url date title collection";

  if (isset($size)) {
    $sizeofresultset = $size;
  }
  else {
    $sizeofresultset = 10;
  }

  $sortorder = "relevance";
  if ( $conf_debug = 1 ) {
    print "Querystring : " . $querystring;

    print_r($curtime);
  }
  
  include($conf_includepath . "/searchresult.inc");
//}
?>

          </td>
        </tr>
      </table>
    </td>
  </tr>
</table>
<table border="0" class="resultsborder" width="90%" cellpadding="10">                                                                          
  <tr>                                                                                                                                         
    <td align="right" class="norm"> 
      Manual : <a href="./manual/manual.html">HTML</a> - <a href="./manual/manual.pdf">pdf</a>&nbsp;&nbsp;|&nbsp;&nbsp;<a 
href="./RELEASE-NOTES">Release Notes</a>
    </td>  
</tr>                                                                                                                                        
</table> 
</center>
</body>
</html>
