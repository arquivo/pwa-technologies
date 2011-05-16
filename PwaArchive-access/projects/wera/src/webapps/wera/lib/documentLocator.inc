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
* Class for finding version(s) of a 
* document in the index.
*
* $Id: documentLocator.inc 813 2006-03-09 15:12:31Z sverreb $
*/

class documentLocator {

  var $indexDriver;
  var $url;
  var $urlextend;
  var $time;
  var $querymode;
  var $resultset = array();
  var $errormessage;
  var $query;
  
  /**
  * Initialize
  *
  * The following query modes are valid:
  * 
  *  EXACT - Will produce a result if, and only if exact match on time input
  *  NEAR - Returns the match nearest to the time input
  *  BEFORE - Returns the match nearest before or equal to the time input
  *  AFTER - Returns the match nearest after or equal to the time input
  *  FIRST - Returns the first match in time regardless of time input
  *  LAST - Returns the latest match in time regardless of time input
  *  ALL - Returns all matches regardless of time input (default)
  *
  *
  * @param  object subclass of indexSearch
  * @param  string url of document to find
  * @param  boolean urlextend obsolete
  * @param  string timestamp in number of seconds since the Unix Epoch
  * @param  string query mode i.e. what the document locator should return
  */
  
  function initialize($indexDriver, $url, $urlextend, $time, $querymode) {
    $this->indexDriver = $indexDriver;
    $this->url = $url;
    //$this->urlextend = $urlextend;    
    $this->time = $time;
    $this->querymode = $querymode;
  }
  
  /**
  * Execute the query
  *
  * This method builds the query string, executes 
  * the query and populates the resultset array
  * (resultset[1][archiveurl], resultset[1][date], resultset[1][encoding], resultset[2][archiveurl], ...
  * If the method returns -1 use getErrorMessage to determine why
  *
  * @return integer Number of hits, -1 if something wrong
  */
  
  function findVersions() {
    $query_start = "exacturl:" . rawurlencode($this->url);
    $sizeofresultset = 1;
    
    if ($this->querymode != 'NEAR') {
      switch ($this->querymode) {
        case 'EXACT':
          $query_end =  "date:" . $this->time;
          $sortorder = 'ascending';
          break;
        case 'BEFORE':
          $query_end = "date:00000101000000-" . $this->time;
          $sortorder = 'descending';
          break;
        case 'AFTER':
          $today = getdate();
          $nextyear = $today['year'] + 1;
          $query_end = "date:" . $this->time . "-" . $nextyear . "0101000000";
          $sortorder = 'ascending';
          break;
        case 'FIRST':
          $query_end = "";
          $sortorder = 'ascending';
          break;
        case 'LAST':
          $query_end = "";
          $sortorder = 'descending';
          break;
        default: // 'ALL' or anything else than the above
          $query_end = "";
          $sortorder = 'ascending';
          $sizeofresultset = 1000;
      }
      $this->query = $query_end . " + " . $query_start; 
      $this->indexDriver->setQuery($this->query);
      $this->indexDriver->setSortorder($sortorder);
      $this->indexDriver->setSizeOfResultSet($sizeofresultset);
      $this->indexDriver->setFieldsInResult("date mime url encoding archiveidentifier collection");
      
      if ($this->indexDriver->doQuery()) {
        $numhits = $this->indexDriver->getnumhits();
        $this->resultset = $this->indexDriver->getResultSet();
      }
      else {
        $numhits = -1;
      }
    }
    else { // querymode = 'NEAR'
      $tmpdoc = new documentLocator();
      $tmpdoc->initialize($this->indexDriver, $this->url, false, $this->time, 'BEFORE');
      $numhits_before = $tmpdoc->findVersions();
      if ( $numhits_before > 0) {
        $resultset_before = $tmpdoc->getResultSet();
      }
      $tmpdoc->initialize($this->indexDriver, $this->url, false, $this->time, 'AFTER');
      $numhits_after = $tmpdoc->findVersions();
      if ( $numhits_after > 0) {
        $resultset_after = $tmpdoc->getResultSet();
      }
      $numhits = 1;
      if (($numhits_before == 0) and ($numhits_after == 0)) {
        $numhits = 0;
      } 
      elseif (($numhits_before > 0) and ($numhits_after > 0)) {
        if (($resultset_after[1][date] - $this->time) < ($this->time - $resultset_before[1][date])) {
          $this->resultset = $resultset_after;
        }
        else {
          $this->resultset = $resultset_before;
        }
      }
      elseif ( $numhits_before > 0 ) {
        $this->resultset = $resultset_before;
      }
      elseif ( $numhits_after > 0 ) {
        $this->resultset = $resultset_after;
      }
      else {
        $numhits = -1;
      }
    }  
    return $numhits;
  }
  
  /**
  * Get the archive URI and mime class
  *
  * @return array resultset containing url, date and encoding
  */
  function getResultSet() {
    return $this->resultset;
  }
  
  /**
  * Get error message
  *
  * @return string error message
  */
  function getErrorMessage() {
    return $this->indexDriver->getErrorMessage();
  }
  
  function getQueryUrl() {
  	return $this->indexDriver->queryurl;
  }
  
  function setUrl($url) {
    $this->url = $url;
  }
  
  function getUrl() {
    return $this->url;
  }
    
}
?>
