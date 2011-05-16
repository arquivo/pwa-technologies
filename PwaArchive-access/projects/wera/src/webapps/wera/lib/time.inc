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
* $Id: time.inc 346 2005-10-04 22:59:28Z stack-sf $
*
* function(s) for converting time-variables
*
* converts a string of format
* YYYY/MM/DD/HH/MM/SS-YYYY/MM/DD/HH/SS
* into an ..array containing
* 2 numbers.(unix-time)
*/

function time2one($string)
	{
	$time_array = explode ("-",$string);
	return $time_array;
	}
function time2num($string)
	{
	$time_array  = explode ("/",$string);
        return mktime($time_array[3],$time_array[4],$time_array[5],$time_array[1],$time_array[2],$time_array[0]);
	}
	
// deprecated. for historical reasons :]
function time2numeric($string)
	{
	$two_dates = time2one($string);
	$date_one = time2num($two_dates[0]);
	$date_two = time2num($two_dates[1]);
	$return_array = array($date_one, $date_two);
	return $return_array;
	}

function time2str($unixtime)
	{
	return date("D, d M Y H:i:s \C\E\T", $unixtime);
	}
function time2str_field($unixtime)
	{
	return date("Y/m/d", $unixtime);
	}
function unix2human($unixtime)
  {
  return date("Y/m/d H:i:s", $unixtime);
  }
