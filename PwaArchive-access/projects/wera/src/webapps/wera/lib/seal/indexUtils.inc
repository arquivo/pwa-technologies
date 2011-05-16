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
* Some indexers ignore non-letter characters like : / + etc.
* In order to enable query for words including such
* characters some parts of the documents has 
* to be encoded before indexing is done.
* Encoding then also has to be done to the parts of the 
* query string that queries the encoded indexes.
*
* $Id: indexUtils.inc 590 2005-12-04 16:43:05Z sverreb $
*
*/

/**
* Index encoding
*
* URL-encode according to RFC1738 and encode also "-", "_" and "."
* Replaces '%' with 'INDX'
*
* Make sure that your input is not already urlencoded before using this.
*
* @param  string string to be encoded
* @return string encoded string
*/  

/*
function index_encode($input) {
  $output = rawurlencode($input);
  $output = preg_replace(array ("/%/", "/\./", "/-/", "/_/"), array ("INDX", "INDXDOT", "INDXDASH", "INDXUNDERLINE"), $output);
  return $output;
} 
*/
/**
* Index decoding
*
* Decode Index encoded strings
*
* @param  string string to be decoded
* @return string decoded string
*/
/*
function index_decode($input) {
  $output = preg_replace( array("/INDXDOT/", "/INDXDASH/", "/INDXUNDERLINE/"), array(".", "-", "_"), $input);
  $output = rawurldecode( str_replace("INDX", "%", $output) );
  return $output;
} 
*/
/**
* Time conversion
*
* Converts UTC time of format YYYYMMDDHH24MISS (e.g. 20020101120000)
* to integer i.e. number of seconds since the Unix Epoch (January 1 1970 00:00:00 GMT). 
* Use this function if your search engine supports only 32-bit unsigned integers.
*
* If the length of the time input string is not exactly 14 input is returned unaltered.
*
* @param  time   Time to be converted
* @return string Time in seconds since unix epoch
*/

function convert_time_to_int($time) {
  if (strlen($time) == 14) {
    $retval= gmmktime ( substr($time, 8, 2), substr($time, 10, 2), substr($time, 12, 2), substr($time, 4, 2), substr($time, 6, 2), substr($time, 0, 4));
  } 
  else {
     $retval = $time;
  }
  return $retval;
}

?>
