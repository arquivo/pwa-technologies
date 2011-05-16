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
 * Document dispatcher
 * @version $Id: documentDispatcher.php 845 2006-03-17 14:40:26Z sverreb $
 * @module documentDispatcher
 * @modulegroup browser
 *
 * Purpose is to find reference to document thats the closest match to what
 * is asked for, and based on the mime-type direct execution to a document-
 * handler.
 *
 * Input parameters:
 * url  - original URL of document to be parsed
 * time - a timestamp
 * mime - mime-type (mime-class[/mime-type]) optional
 * mode - See description for document_locator
 *
 * Outputs result to the web client
 */

include_once('lib/config.inc');
include('documentLocatorCanonizeUrl.inc');
include('httpUtils.inc');
include($conf_index_file);


$url = $_REQUEST['url'];
$time = $_REQUEST['time'];
$mime = $_REQUEST['mime'];
$mode = $_REQUEST['mode'];

/*
echo "url" . $url;
echo "time" . $time;
echo "mime" . $mime;
echo "mode" . $mode;
*/

// Check input parameters
if(!isset($url)) {
  // ERROR: This script needs an url to function
  exit(1);
}

#if(strpos($url, 'INDXDOT') !== FALSE) {
#	$url = index_decode($url);
#}


if(!isset($time)) {
  // ERROR: This script needs a timestamp to function
  exit(1);
}

if(!isset($mode)) {
  $mode = 'standalone';
}

if(!isset($mime)) {
  $mime = '';
}

// Find right version of the document


$document = document_locator($url, $time, $mime, $mode);

list($handler_url, $handler_has_links) = explode(" ", type_resolver($document, $mime));
$handler_has_links = $handler_has_links == "parselinks" ? true : false;

  
// 
if ($document['collection'] == "") {
  // If the indexed data for some reason does not contain a collection
  $document['collection'] = $conf_location_code;
}

// Is document located at different site?
if($mode != 'external' && $document['collection'] != $conf_location_code) {
//if($mode != 'external' && !stristr($conf_location_code, $document['collection'])) {
  if (!isset($conf_locations[$document['collection']])) {
    print "<HTML>\n<HEAD>\n<link rel=\"stylesheet\" href=\"$conf_gui_style\" type=\"text/css\">\n</HEAD>\n<BODY>\n<p>";
    print 'The variable <b>$conf_locations[' . $document['collection'] . "]</b> is not defined. Please check settings in config.inc";
    print "\n</p>\n</BODY>\n</HTML>";
    exit();
  }
  $handler_url = $conf_locations[$document['collection']] . "?$QUERY_STRING&mode=external";
  if($handler_has_links) {
    rewrite_document($handler_url, $conf_result_page, $js);
  } else {
    if ($fd = @fopen ($handler_url, "r")) {
      fpassthru($fd);
    }
  }
  exit();
}

parse_document($handler_url, $document, $handler_has_links, $js);

/**
 * document_locator
 *
 * Tries to find meta information about the document that best matches
 * the url and timestamp combination.
 *
 * @param $url URL for the document as it was when the document was harvested.
 * @param $timestamp Documents creation time (or harvest time)
 * @param $mime_hint What mime-type or mime-class to expect
 * @param $mode One of the following:
 *             inline (e.g. images or frame content)
 *             standalone (e.g. html-page or pdf-document) (default)
 *
 * @return array with metadata about document:
 *                [date]              = datestamp
 *                [mime]              = mime-type
 *                [encoding]          = charcter encoding
 *                [archiveidentifier] = uri to document in the archive
 */
function document_locator($url, $timestamp, $mime_hint, $mode = 'standalone') {
  global $conf_index_class;


  $searchEngine = new $conf_index_class();
  $locator = new documentLocatorCanonizeUrl();
  if ($timestamp != "") {
  	$doclocmode = 'NEAR';
  }
  else { // if no time given show the latest version
  	$doclocmode = 'LAST';
  }
  
  $locator->initialize($searchEngine, $url, false, $timestamp, $doclocmode);
  $numhits = $locator->findVersions();
  if($numhits <= 0) { // No document found
    header("HTTP/1.0 404 Not Found");
  	include('lib/config.inc');
    include($conf_includepath . "/header.inc");
    print "</HEAD><BODY><center><font face=\"helvetica,arial,sans-serif\">";
    print "<table align=\"center\" class=\"resultsborder\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"90%\">";  
    print "<tr><td>";  
    print nls("Sorry, no documents with the given url, $url were found");
    if ($conf_url_canonize_debug_on) {
      print "<br><h1>URL Canonicalization</h1>";
      print $locator->getrulesTrail();
    }
    print "</td></tr></table></center>";
    include($conf_includepath . "/footer.inc");
    exit();
  }

  $result = $locator->getResultSet();
  $document = $result[1];
  
  #print_r($result);
  #die("");
  $document['mime'] = $document['mime'];
  if($document['mime'] == '' && $mime_hint != '') {
    $document['mime'] = $mime_hint;
  }
  return $document;
}

/**
 * type_resolver
 *
 * Returns an url to the documentHandler that best handles
 * the mime-type for the document.
 *
 * @param $document
 * @return $string containing url to document handler
 */
function type_resolver($document) {
  global $conf_document_handler;
  $format = $document['mime'];
  if($conf_document_handler[$format]) {
    return $conf_document_handler[$format];
  }
  $format = @split($format,'/');
  if($conf_document_handler[$format[0]]) {
    return $conf_document_handler[$format[0]];
  }
  return $conf_document_handler['default'];
}

/**
 * parse_document
 *
 * Redirects handling of document to the document handler pointed to by
 * handler_url. The result from the handler is sent to the user's browser.
 *
 * @param $handler_url URL to documenthandler
 * @param $document Array of metadata for document as returned by the documentLocator
 * @param $handler_has_links Indicates that this document has links that the handler will parse
 */
function parse_document($handler_url, $document, $handler_has_links, $js) {
  global $conf_document_retriever, $conf_result_page, $mode;
  $handler_url .= '?aid='.urlencode($conf_document_retriever . $document['archiveidentifier']).'&time='.$document['date'].'&mime='.$document['mime'].'&url='.urlencode($document['url']);
  if ($document['encoding']) {
  	Header("content-type: " . $document['mime'] . "; charset=" . $document['encoding'], false);
  }
  else {
  	Header("content-type: " . $document['mime'], false);
  }  
  if($handler_has_links && $mode != "external") {
    rewrite_document($handler_url, $conf_result_page, $js);
  } else {
    fetchAndPrintUrl($handler_url);
  }
}

/**
 * rewrite_document
 *
 * Rewrites the linkprefix, that the document-handler inserts, into real urls
 *
 * @param $handler_url URL to documenthandler. Preformatted with all needed parameters.
 * @param $result_page The URL that the linkprefix should be rewritten to.
 */
 function rewrite_document($handler_url, $result_page, $js) {

  $locales = nls_getlocale();
  $handler_url = $handler_url . "&locale=" . $locales[0];

  $fp = fopen ($handler_url, "r");
  if($fp) {
    while(!feof($fp)) {
      $content .= fread($fp,1024);
    }

    $regex = '/\"##P#R#E#F#I#X##TIME#(\d*)##MODE#(standalone|inline)##URL#([^\"]*)##\"/e';
    $replace = "format_url('$result_page', '$3', '$1', '$2')";
    $content = preg_replace($regex, $replace, $content);

    // In case of using the document handler that inserts Javascript (client side link replacement):
    $content = preg_replace("/##P#R#E#F#I#X##TIME#/", "$result_page?time=", $content);    
    //if ( $js == "off" ){
    if (1 == 0) { // disabled this !!
	  /*
	  *  $js_regex = "'<script[^>]*?>.*?</script>'si";
	  *  $js_replace = "<script language=\"Javascript\"><!-- WERA HAS BEEN CONFIGURED TO DISABLE THIS JAVASCRIPT SECTION. --></script>";
	  *  $content_nojs = preg_replace($js_regex,$js_replace,$content);
	  *  print($js. $content_nojs);
	  */
     $js_regex = array("'<script[^>]*?>.*?</script>'si",
    "'onLoad=\".*?\"'si",
    "'onAbort=\".*?\"'si",
    "'onBlur=\".*?\"'si",
    "'onChange=\".*?\"'si",
    "'onClick=\".*?\"'si",
    "'onError=\".*?\"'si",
    "'onFocus=\".*?\"'si",
    "'onMouseOver=\".*?\"'si",
    "'onMouseOut=\".*?\"'si",
    "'onSelect=\".*?\"'si",
    "'onSubmit=\".*?\"'si",
    "'onUnload=\".*?\"'si");


    $js_replace = array("<script language=\"Javascript\"><!-- WERA HAS BEEN CONFIGURED TO DISABLE THIS JAVASCRIPT SECTION - FOR THIS PAGE. --></script>",
    "",
    "",
    "",
    "",
    "",
    "",
    "",
    "",
    "",
    "",
    "",
    "");


    $content_nojs = preg_replace($js_regex,$js_replace,$content);
    print($content_nojs);


    }else
    {
	    print($content);
    }


    fclose($fp);
  }
}

function format_url($result_page, $url, $time, $mode) {
  #$res = "\"$result_page?url=" . index_encode($url) . "&time=$time&mode=$mode\"";
  $res = "\"$result_page?url=" . $url . "&time=$time&mode=$mode\"";
  if($mode == 'standalone') {
    $res .= ' target="_top"';
  }
  return $res;
}
?>
