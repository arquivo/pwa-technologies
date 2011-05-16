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

include_once ($conf_includepath."/time.inc");
include_once ($conf_index_file);
include_once ($conf_includepath."/documentLocatorCanonizeUrl.inc");

/**
* Class for generating the necessary data for displaying a timeline 
* with the versions (with links) marked along the line
*
* $Id: timeline.inc 813 2006-03-09 15:12:31Z sverreb $
*/

class timeline {
	var $dates_to_display = array (), $chosen_date, $timeline_array = array (), $resolution, $url, $encoded_url, $conf_index_class, $previous_version, $next_version, $first_version, $last_version, $number_of_versions;
	var $error = "";

	/**
	* Constructor
	*
	* @param   string  Resolution. Legal values: 6 (auto), 5 (years), 4 (months), 3 (days), 2 (hours), 1 (minutes), 0 (seconds)
	* @param   string  The chosen date on the timeline. Format: 'YYYYMMDDHHMISS'
	* @param   array   Url of the document to find all versions of
	*/
	function timeline($resolution = 6, $chosen_date, $url, $index_class) {

		$this->conf_index_class = $index_class;
		$this->url = $url;
		#$this->encoded_url = index_encode($this->url);
		$this->encoded_url = $this->url;
		$this->resolution = $resolution;

		$versions = $this->getVersions();
		if ($versions != -1) {
			$retval = true;
			$this->first_version = $versions[1][date];
			$this->last_version = $versions[$this->number_of_versions][date];
	
			if ($chosen_date == "") {
				$chosen_date = $this->last_version;
			}
	
			if ($resolution == 6) {
				$this->autoresolution = 'on';
				$this->resolution = 5;
			}
	
			do {
				$buildstatus = $this->buildTimeline($chosen_date, $versions);
				if ($buildstatus == "drilldown" and $this->autoresolution == 'on') {
					$this->resolution--;
				}
			} while ($buildstatus == "drilldown" and $this->autoresolution == 'on');
		}
	}

	/**
	* Get data necessary to display the timeline
	*
	* @return array  Timeline data
	*/
	function getTimelineData() {
		if ($this->error != "") {
			$retval = false;
		}
		else {
			$retval = $this->timeline_array;
		}
		return $retval;
	}

	/**
	* Get current resolution
	* If resolution is set to auto when instanciating the timeline
	* the method will return the resolution that the constructor drilled 
	* down to.
	*
	* @return integer  Resolution
	*/
	function getResolution() {
		return $this->resolution;
	}

	/**
	* Get number of versions
	*
	* @return integer  Number of versions
	*/
	function getNumberOfVersions() {
		return $this->number_of_versions;
	}

	/**
	* Get the timestamp of the version before the chosen timestamp
	*
	* @return string timestamp (Format: 'YYYYMMDDHHMISS')
	*/
	function getPreviousVersionTimestamp() {
		return $this->previous_version;
	}

	/**
	* Get the timestamp of the version after the chosen timestamp
	*
	* @return string timestamp (Format: 'YYYYMMDDHHMISS')
	*/
	function getNextVersionTimestamp() {
		return $this->next_version;
	}

	/**
	* Get the timestamp of the first version
	*
	* @return string timestamp (Format: 'YYYYMMDDHHMISS')
	*/
	function getFirstVersionTimestamp() {
		return $this->first_version;
	}

	/**
	* Get the timestamp of the last version
	*
	* @return string timestamp (Format: 'YYYYMMDDHHMISS')
	*/
	function getLastVersionTimestamp() {
		return $this->last_version;
	}

	/**
	* Get the key of the version nearest before or equal to the 
	* chosen timestamp
	*
	* @return string timestamp (Format: 'YYYYMMDDHHMISS')
	*/
	function getKeyOfCurrentVersion() {
		return $this->key_of_version_nearest_before;
	}

	// Private methods

	/**
	* Get all veresions of a given URL 
	* 
	*
	* @return array  Resultset
	*/
	function getVersions() {
    	global $conf_url_canonize_rules;
		$search = new $this->conf_index_class();
		$docloc = new documentLocatorCanonizeUrl();
		$docloc->initialize($search, $this->url, false, 0, 'ALL');
		$this->number_of_versions = $docloc->findVersions();
		if ($this->number_of_versions != -1) {
			$retval = $docloc->getResultSet();
		}
		else {
			$retval = -1;
			$this->error = "<b>" . $docloc->getErrorMessage() . "</b> (<a href=\"" . $docloc->getQueryUrl() . "\" TARGET=\"_top\">" . $docloc->getQueryUrl() . "</a>)";
		}
		return $retval;
	}
	
	function getError() {
		return $this->error;
	}

	/**
	* Calculates the number of units on the timeline
	*
	* @return array  Units to display on the timeline (units before the chosen date, and units after the chosen date)
	*/
	function getNumberOfUnitsOnTimeline() {
		//adapt this one to use parameters from a config file instead of the hardcoded values(?)
		switch ($this->resolution) {
			case '4' : //months
				$units[before] = 30;
				$units[after] = 30;
				break;
			case '3' : // days
				$units[before] = 30;
				$units[after] = 30;
				break;
			case '2' : // hours
				$units[before] = 30;
				$units[after] = 30;
				break;
			case '1' : // minutes
				$units[before] = 30;
				$units[after] = 30;
				break;
			case '0' : //seconds
				$units[before] = 30;
				$units[after] = 30;
				break;
			default : // years
				$units[before] = 5;
				$units[after] = 5;
		}
		return $units;
	}

	/**
	* Explode human date into an array
	* Typical retrun value (the resolution in the example below was 3):
	*
	*   [base] => 20010924131555
	*   [second] => 55
	*   [minute] => 15
	*   [hour] => 13
	*   [day] => 24
	*   [month] => 09
	*   [year] => 2001
	*   [resolution_dependent_format] => 2001-09-24
	*
	* @param string  Timeline resolution
	* @param string  Date to explode (Format: 'YYYYMMDDHHMISS')
	* @return array  Exploded date
	*/
	function dateToArray($resolution, $indate) {

		$return_date[base] = $indate;
		$return_date[second] = substr($indate, 12, 2);
		$return_date[minute] = substr($indate, 10, 2);
		$return_date[hour] = substr($indate, 8, 2);
		$return_date[day] = substr($indate, 6, 2);
		$return_date[month] = substr($indate, 4, 2);
		$return_date[year] = substr($indate, 0, 4);

		switch ($resolution) {
			case '4' : // months
				$return_date[resolution_dependent_format] = $return_date[year]."-".$return_date[month];
				break;
			case '3' : // days
				$return_date[resolution_dependent_format] = $return_date[year]."-".$return_date[month]."-".$return_date[day];
				break;
			case '2' : // hours
				$return_date[resolution_dependent_format] = $return_date[year]."-".$return_date[month]."-".$return_date[day]." ".$return_date[hour];
				break;
			case '1' : // minutes
				$return_date[resolution_dependent_format] = $return_date[year]."-".$return_date[month]."-".$return_date[day]." ".$return_date[hour].":".$return_date[minute];
				break;
			case '0' : // seconds
				$return_date[resolution_dependent_format] = $return_date[year]."-".$return_date[month]."-".$return_date[day]." ".$return_date[hour].":".$return_date[minute].":".$return_date[second];
				break;
			default : // years
				$return_date[resolution_dependent_format] = $return_date[year];
		}
		return $return_date;
	}

	/**
	* Build the timeline
	*
	* @param    string Chosen date
	* @versions array  All the versions for the given URL
	* @return   array  Timeline data
	*/
	function buildTimeline($chosen_date, $versions) {

		unset ($this->chosen_date);
		unset ($this->timeline_array);
		$this->dates_to_display = $versions;
		$this->chosen_date = $this->dateToArray($this->resolution, $chosen_date);
		if (!empty ($versions)) {
			foreach ($this->dates_to_display as $key => $val) {

				$this->dates_to_display[$key] = $this->dateToArray($this->resolution, $this->dates_to_display[$key][date]);
			}

			$units = $this->getNumberOfUnitsOnTimeline();

			for ($n = - $units[before]; $n <= $units[after]; $n ++) {
				switch ($this->resolution) {
					case '4' : // months
						$unixtime = gmmktime(0, 0, 0, $this->chosen_date[month] + $n, 01, $this->chosen_date[year]);
						$timeformat = 'Y-m';
						break;
					case '3' : // days
						$unixtime = gmmktime(0, 0, 0, $this->chosen_date[month], $this->chosen_date[day] + $n, $this->chosen_date[year]);
						$timeformat = 'Y-m-d';
						break;
					case '2' : // hours
						$unixtime = gmmktime($this->chosen_date[hour] + $n, 0, 0, $this->chosen_date[month], $this->chosen_date[day], $this->chosen_date[year]);
						$timeformat = 'Y-m-d H';
						break;
					case '1' : // minutes
						$unixtime = gmmktime($this->chosen_date[hour], $this->chosen_date[minute] + $n, 0, $this->chosen_date[month], $this->chosen_date[day], $this->chosen_date[year]);
						$timeformat = 'Y-m-d H:i';
						break;
					case '0' : // seconds
						$unixtime = gmmktime($this->chosen_date[hour], $this->chosen_date[minute], $this->chosen_date[second] + $n, $this->chosen_date[month], $this->chosen_date[day], $this->chosen_date[year]);
						$timeformat = 'Y-m-d H:i:s';
						break;
					default : // years
						$unixtime = gmmktime(0, 0, 0, 01, 01, $this->chosen_date[year] + $n);
						$timeformat = 'Y';
				}

				$this->timeline_array[$n][resolution_dependent_format] = gmdate($timeformat, $unixtime);
				$this->timeline_array[$n][linkvalue] = gmdate('YmdHis', $unixtime);

				$this->timeline_array[$n][versions] = 0;

				foreach ($this->dates_to_display as $key => $val) {

					if ($this->dates_to_display[$key][resolution_dependent_format] == $this->timeline_array[$n][resolution_dependent_format]) {
						// There are one or more versions in this timeframe 
						$this->timeline_array[$n][versions] = $this->timeline_array[$n][versions] + 1;
						$this->timeline_array[$n][version][$key] = $this->dates_to_display[$key][base]; // svb 
						$this->timeline_array[$n][linkvalue] = $this->dates_to_display[$key][base];

						if ($this->dates_to_display[$key][resolution_dependent_format] <= $this->chosen_date[resolution_dependent_format]) {
							$this->previous_version = $this->dates_to_display[$key -1][base];
							$this->next_version = $this->dates_to_display[$key +1][base];
							$this->key_of_version_nearest_before = $key;
						}
					}
					if ($this->timeline_array[$n][versions] > 1 and $this->timeline_array[$n][resolution_dependent_format] == $this->chosen_date[resolution_dependent_format]) {
						$retval = "drilldown";
					}
				}
			}
			if ($timeline_array[$n][versions] > 0) {
				$this->timeline_array[$n][linkvalue] = $this->timeline_array[$n][version][1]; //svb
			}

		}

		if ($chosen_date < $this->dates_to_display[1]['base']) {
			$this->previous_version = "";
			if ($this->number_of_versions > 1) {
				$this->next_version = $this->dates_to_display[2][base];
			} else {
				$this->next_version = "";
			}
			$this->key_of_version_nearest_before = "";
		}

		return $retval;
	}
}
?>


