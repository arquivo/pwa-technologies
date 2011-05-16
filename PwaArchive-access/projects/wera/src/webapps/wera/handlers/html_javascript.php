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


/**
 * Documenthandler that inserts a javascript into the html file
 * The purpose of the javascript is to make the link rewriting happen
 * in the browser.
 * The javascript is based on one of Internet Archives Wayback Machine
 * installations. A few modifications have been made to adapt it to the 
 * WERA url scheme.
 *
 * Please Note ! 
 * The Javascript must be stored alongside this script, 
 * with the same name as this script and with a '.js' extension
 * 
 * @version $Id: html_javascript.php 838 2006-03-16 20:10:24Z sverreb $
 *
 * Input parameters:
 * aid - The url of the document in the archive (really: $conf_document_retriever preceded by the documents unique identifier in the archive)
 * time - The documents timestamp
 * mime - The documents mime-type
 * url - The documents original url
 */

//if register_globals is off
if (!isset($aid)) $aid = $_REQUEST['aid'];
if (!isset($mime)) $mime = $_REQUEST['mime']; 
if (!isset($time)) $time = $_REQUEST['time']; 
if (!isset($url)) $url = $_REQUEST['url'];
if (!isset($locale)) $locale = $_REQUEST['locale'];

include_once("../lib/config.inc"); // Only needed for nls!
$nls_context = basename($_SERVER['PHP_SELF']);
                                                        
if ($fd = fopen ($aid, "r")) {
  if (isset($mime)) {
    Header("content-type: $mime");
  }
  
  while(!feof($fd)) {
    $document .= fread($fd, 1024);
  }
  fclose($fd);
  
  $hrefstring = "<HEAD>\n<BASE HREF=\"$url\">\n";
  
  // Insert the base url
  if (preg_match_all("/<head>/i", $document, $dummy) > 0) { // if head present
    $document = preg_replace("/<head>/i", $hrefstring, $document, 1);
  }
  elseif (preg_match_all("/<html>/i", $document, $dummy) > 0) { // else, if html present
    $document = preg_replace("/<html>/i", "<HTML>\n" . $hrefstring . "</HEAD>", $document, 1);
  }
  else {
    $document = "<HTML>\n" . $hrefstring . "</HEAD>" . $document;
  }

  $parselinks = (split(" ", $conf_document_handler['text/html']));
  if ($parselinks[1] == 'parselinks') {
    $weraTime = substr($time,0,4) . "-" . substr($time,4,2) . "-" . substr($time,6,2) . " " . substr($time,8,2) . ":" . substr($time,10,2) . ":" . substr($time,12,2);
    $js_to_insert = "<SCRIPT language=\"Javascript\">\n";
    $js_to_insert .= "var sWayBackCGI = \"##P#R#E#F#I#X##TIME#$time\"\n";
    $js_to_insert .= "var weraTime = \"$weraTime\"\n";
    $js_to_insert .= "var weraUrl = \"$url\"\n";
    $js_to_insert .= "var weraNotice = \"" . nls("WERA", $nls_context, $locale) . " - " . nls("External links, forms, and search boxes may not function within this collection", $nls_context, $locale) . ". " . nls("Url", $nls_context, $locale) . ": " . $url . ", " . nls("time", $nls_context, $locale) . ": " . $weraTime . "\"\n";
    $js_to_insert .= "var weraHideNotice = \"" . nls("hide", $nls_context, $locale) . "\"\n";
    $js_to_insert .= "</SCRIPT>\n";
    $js_to_insert .= file_get_contents($_SERVER['SCRIPT_FILENAME'] . ".js");
    $js_to_insert .= "</html>";
  
    if (preg_match_all("/<\/html>/i", $document, $dummy) > 0) { // if </html> present
      $document = preg_replace("/<\/html>/i", $js_to_insert, $document, 1);
    }
    else {
      $document = $document . $js_to_insert;
    }
  }

  print($document);
}
?>
