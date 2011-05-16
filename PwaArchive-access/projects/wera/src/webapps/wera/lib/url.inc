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
* $Id: url.inc 590 2005-12-04 16:43:05Z sverreb $
*/

//  Various functions for manipulating urls 
//  string stripPort(string $url) - strips portnumber from url if it is 80 (default)
//  string stripProtocol(string $url) - strips protocol from url http:// only
//  boolean isAbsolute(string $url) - returns true if url is absolute
//  boolean isRelative(string $url) - returns true if relative to path, false if relative to domain
//  string getDocname(string $url) - gets document name from url
//  string getDomain(string $url) - get domain from url
//  string getPath(string $url) - get path from url
//  string combineUrl(string $orig_url, string $url) - combines an absolute url with a relative link

// Strips portnumber if 80
function stripPort($url) {
	$url = str_replace(":80/", "/", $url);
	return $url;
}
// Strips http://
function stripProtocol($url) {
	$url = eregi_replace("^http://", "", $url);
	return $url;
}

// Example url: http://www.domain.com/path/index.html
// returns true if it is :]
function isAbsolute($url) {
	if (eregi("^http://", $url)) {
		return true;
	} else {
		return false;
	}
}

// Returns document name: index.html
function getDocname($url) {
	eregi("[^/]*$", $url, $matches);
	if ($matches != "") {
		#print "Matches: " . $matches[0] . "<br>\n";
		return $matches[0];
	} else
		return false;
}

// Returns domain-name: www.domain.com
// !! Only if the URL starts with http://
function getDomain($url) {
	if (isAbsolute($url)) {
		$url = eregi_replace("^http://", "", $url);
		eregi("^[^/]*", $url, $matches);
		return $matches[0];
	} else {
		return false;
	}
}

// Returns path: /path/ usage: getDomain("someurl"[,true|false][,true|false])
// Set nostartslash to true if you don't want the start slash returned
// Set noendslash to true if you don't want the end slash returned
// Otherwise, don't pass the the two last parameters 
function getPath($url, $nostartslash = FALSE, $noendslash = FALSE) {
		//If url field is empty - return a null field.

	if ($url == "") {
		return $url;
	}

	$domain = getDomain($url);
	$docname = getDocname($url);
	#print "Domain: $domain<br>\n";
	#print "Docname: $docname<br>\n";
	$url = eregi_replace("^http://", "", $url);
	$url = eregi_replace("$domain", "", $url);
	if ($docname)
		$url = eregi_replace("$docname", "", $url);
	if ($nostartslash) {
		$url = eregi_replace("^/*", "", $url);
	}
	if ($noendslash) {
		$url = eregi_replace("/*$", "", $url);
	}
	#print "Returned path: $url<br>\n";
	return $url;
}

// Returns:
// -1 If absolute
// 0 If url is relative to domain (starts with /)
// 1 If url is relative to domain/path (doesn't start with /)
function isRelative($url) {
	if (isAbsolute($url)) {
		return -1;
	}
	elseif (eregi("^/", $url)) {
		return false;
	} else {
		return true;
	}
}

// Takes original url and a relative url and combines them to an absolute url
function combineUrl($l_url, $o_url) {
	$query = $l_url;
	if (!isAbsolute($l_url)) // If relative
		{
		// Her m� det gj�res litt av hvert for � f� rett streng til s�ket.
		if (isRelative($l_url)) // Relative to domain/path/
			{
			$domain = getDomain($o_url);
			#print "Parameter for getPath: $o_url<br>\n";
			$orig_path = getPath($o_url);
			#print "Orig_path: $orig_path<br>\n";
			while (eregi("^\.\.\/", $l_url)) {
				#print $orig_path;
				$orig_path = eregi_replace("[^/]*\/$", "", $orig_path);
				$orig_path = $orig_path."/";
				$orig_path = eregi_replace("\/{2}", "/", $orig_path);
				$l_url = eregi_replace("^\.\.\/", "", $l_url);
				#print " -> " . $orig_path;
			}
			$l_url = eregi_replace("^(\.\/)(\.\/)*", "", $l_url);
			$query = "http://".$domain.$orig_path.$l_url;
			#print $query;
		} else // It is relative to domain only
			{
			$domain = getDomain($o_url);
			$orig_path = getPath($o_url);
			$query = "http://".$domain.$l_url;
		}
	}

	return $query;

}
?>