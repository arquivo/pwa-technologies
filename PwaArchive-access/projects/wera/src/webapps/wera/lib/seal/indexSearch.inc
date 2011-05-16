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
* The IndexSearch class defines methods
* for interfacing "any" search engine
*
* To interface your own search engine
* use this class as the superclass.
*
* $Id: indexSearch.inc 362 2005-10-05 22:42:47Z sverreb $
*/

class indexSearch {
  var $query;
  var $numhits;
  var $numhitstotal;
  var $hitsperset;
  var $offset;
  var $timespent;
  var $resultfields = array();
  var $resultset = array();
  var $sortorder;
  var $errormsg;
 
  /**
  * Set the result set size
  *
  * Sets the number of hits returned by the 
  * getResultSet and getXmlResultSet methods
  *
  * @param  integer  Number of hits to return
  */  
  function setSizeOfResultSet($hits) {
    $this->hitsperset = $hits;
  }
  
  /**
  * Set the offset 
  *
  * Sets the offset for the result set. An offset of 10
  * and a result set size of 10 will give a result 
  * set with hits 11-20 (assuming the hitnumbers 
  * start with 1)
  *
  * @param  integer  Offset
  */  
  function setOffset($offset) {
    $this->offset=$offset;
  }
  
  /** 
  * Set the sort order 
  *
  * @param  string  Sort order
  */
  function setSortOrder($sortorder) {
    $this->sortorder=$sortorder;
  } 

  /**
  * Get the query response time
  * 
  * @return  float  Time spent on query
  */
  function getTimeSpent() {
    return $this->timespent;
  } 

  /** 
  * Get number of hits in resultset
  *
  * @return number of hits
  */
  function getNumHits() {
    return $this->numhits;
  }

  /** 
  * Get the total number of hits
  *
  * @return number of hits
  */
  function getNumHitsTotal() {
    return $this->numhitstotal;
  }
  
  
  /**
  * Set query string for search
  *
  * @param  string  Query string
  */
  function setQuery($querystring) {
    $this->query = $querystring;
  }

  /**
  * Sets which fields to include in the result
  * set returned by the getResultSet and 
  * getXmlResultSet methods.
  *
  * $fields : a comma, or whitespace limited 
  * list of result fields.
  *
  */
  function setFieldsInResult($fields) {
  }
  
  /**
  * Executes the query.
  *
  * This method will populate the result set array
  * Returns an array of the following form:
  *   Array([0]=>Array([date]=>"20011014203201"[title]=>"Some title")
  *         [1]=>Array([date]=>"20010816121407"[title]=>"Another title"))
  * the fields returned in the array is determined by the 
  * setResultFields method (e.g. setResultFields("date, title")
  *
  * @return boolean False if error
  */
  function doQuery() {
    return true;
  }
  
  /**
  * Get the result set
  *
  * @return  array  Result set
  */
  function getResultSet() {
    return $this->resultset;
  }  

  /**
  * If the doQuery method returned false
  * use this to fetch the error message string
  *
  * @return  string Error message
  */
  function getErrorMessage() {
    return $this->errormsg;
  }  
}
?>
