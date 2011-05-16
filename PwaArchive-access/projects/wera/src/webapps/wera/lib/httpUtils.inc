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
*
* $Id: httpUtils.inc 936 2006-04-25 10:50:51Z sverreb $
*/

/**
* This function is meant for url fetching and display. 
* Ensures that the mime type is relayed properly, 
* which is not the case when using fopen and print.
*
* $Id: httpUtils.inc 936 2006-04-25 10:50:51Z sverreb $
*/

function fetchAndPrintUrl( $url ) {
   $url = str_replace(" ", "%20", $url);
   $url_parsed = parse_url($url);
   $host = $url_parsed["host"];
   $port = $url_parsed["port"];

   if ($port==0) {
     $port = 80;
   }
   $path = $url_parsed["path"];
   if ($url_parsed["query"] != "") {
     $path .= "?".$url_parsed["query"];
   }
   $out = "GET $path HTTP/1.0\r\nHost: $host:$port\r\n\r\n";
   $fp = fsockopen($host, $port, $errno, $errstr, 30);
   fwrite($fp, $out);
   $body = false;
   while (!feof($fp)) {
     $s = fgets($fp, 1024);
     if ( $body ) {
       $in .= $s;
     }
     else {
       if (stristr($s, 'content-type')) {
         header($s);
       }
       if (stristr($s, '404 Not Found')) {
        header($s);
       }
     }
     if ( $s == "\r\n" ) {
       $body = true;
     }
   }
   fclose($fp);
   print $in;
}
?>