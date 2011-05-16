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
* Class for querying the Nutch Search Engine
* implementation of http://archive-access.sourceforge.net
* 
* $Id: nutch.inc 498 2005-11-03 13:27:18Z sverreb $
*/
include("$conf_searchenginepath/indexSearch.inc");
include("$conf_searchenginepath/indexUtils.inc");

/**
 * Simple function to replicate PHP 5 behaviour
 */
function microtime_float() {
   return array_sum(explode(" ",microtime()));
}

/**
* Class for querying Nutch
*
* For further information about constructing 
* queries and setting parameters for the nucthSearch
* class see Nutch Documentation
*
*/

class nutchSearch extends indexSearch {
  // Nutch specific variables
  var $searchengineurl;
  var $xml_parser;            // Object Reference to xml parser
  var $xml_elementname;
  var $xml_depth;
  var $hitno;
  var $queryurl;
  var $mime = array();
  var $arc = array();
  var $sort; 
  var $debug;
  var $morepages;
  var $dedupfield;
  var $hitsperdup;
  
  /**
  * Constructor. 
  * Set some initial values 
  */
  function nutchSearch() {
    include ("config.inc");    
    $this->searchengineurl = $conf_searchengine_url;
    $this->debug = $conf_debug;
    $this->query = "";
    $this->numhits = 0;
    $this->hitsperset = 10;
    $this->offset = 0;
    $this->timespent = 0;
    $this->morepages = false;
    $this->setDedup();  
  }

  
  
  // NUTCH SPECIFIC FUNCTIONS
  
  /**
  * Check if ready to execute query (query string non-empty)
  *
  * @return boolean true if query string non-empty, false otherwise
  */
  function isReady() {
    if ($this->query != "") {
      return true;
    }
    else {
      return false;
    }
  }
  
  function adaptQuery($querystring) {
    $querystring = str_replace(" ", "%20", $querystring);
    return $querystring;
  }
 
  
  // FUNCTIONS OVERRIDING PARENT CLASS FUNCTIONS
  
  /** 
  * Sets the sortorder 
  * legal values for sortorder is:
  * "ascending" and "descending"
  * any other value will be treated as relevance ranking
  */
  function setSortOrder($sortorder) {
    # e.g &dedupField=date&hitsPerDup=100&sort=date
    if ($sortorder == "ascending" or $sortorder == "descending") {
    			$this->setDedup(100, "date");
        $this->sort = "&sort=date";
        if ($sortorder == "descending") {
            $this->sort .= "&reverse=true";
        }    
    }
    
  } 
  
  /**
   * Set deduplication
   *  
   * If dedupfield is emty, NutchWax defaults to 'site'
   * To turn off dedup, set hitsperdup to 0
   * 
   * @param  integer  Hits per duplicate
   * @param  string   Field to deduplicate on
   */
  function setDedup($hitsperdup = 0, $dedupfield = "") {
  		$this->hitsperdup = $hitsperdup;
  		$this->dedupfield = $dedupfield;
  }
  
  /**
  * Set result set fields
  *
  * Use this to set which summary fields to include 
  * in the result set returned by the getResultSet 
  * and getXmlResultSet methods.
  *
  * @param  string  Fields in result set
  */
  function setFieldsInResult($fields) {
    $this->resultfields = preg_split ("/[\s,]+/", trim($fields));
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
  * Execute the query
  *
  * @return boolean False if empty query
  */ 

  function doQuery() {
	$retval = true;
    unset($this->resultset);
    $time_start = microtime_float();
    
    $this->queryurl = $this->searchengineurl . "?query=" . $this->adaptQuery($this->query) . "&start=" . $this->offset . "&hitsPerPage=" . $this->hitsperset . "&hitsPerDup=" . $this->hitsperdup . "&dedupField=" . $this->dedupfield;
    
    if ($this->sort != "") {
      $this->queryurl .= $this->sort;
    }
    
    if ($this->isReady()) {
      $this->hitno = $this->offset;
      $this->xml_parser = xml_parser_create();
      xml_set_object($this->xml_parser, $this);
      xml_set_element_handler($this->xml_parser, "startElement", "endElement");
      xml_set_character_data_handler($this->xml_parser, "characterData");
      $data =  @file_get_contents($this->queryurl);
      if ($data) {
	      if (!xml_parse($this->xml_parser, $data)) {
	        $retval = false; 
	        $this->errormsg = sprintf("XML error: %s at line %d", xml_error_string(xml_get_error_code($this->xml_parser)), xml_get_current_line_number($this->xml_parser));
	      }
	      xml_parser_free($this->xml_parser);
	      if ($retval) {
		      if ($this->numhitstotal > 0) {
		        foreach ($this->resultset as $key => $val) {
		            if (in_array("mime", $this->resultfields)) {
		                $this->resultset[$key]['mime'] = $this->mime[$key]['primary'] . "/" . $this->mime[$key]['sub'];
		            }
		            if (in_array("archiveidentifier", $this->resultfields)) {
		                $this->resultset[$key]['archiveidentifier'] = $this->arc[$key]['offset'] . "/" . $this->arc[$key]['name'];
		            }
		        }
		      }
		      
		      $this->timespent = (microtime_float() - $time_start);
	      }
      }
      else {
      	$retval = false;
      	$this->errormsg = "Error : Failed to open stream!";
      }
      return $retval;
    }
  }
  
  function startElement($parser, $name, $attrs) {
     $this->xml_depth[$parser]++;
     $this->xml_elementname = $name;
     if ($this->xml_elementname =="ITEM") {
       $this->hitno++;
       $this->numhits = $this->hitno;
     }
  }
  
  function endElement($parser, $name) {
    $this->xml_depth[$parser]--;
  }
  
  function characterData($parser, $data) {
    if (trim($data) != "") {
      switch ($this->xml_elementname) {
        case "OPENSEARCH:TOTALRESULTS":
          $this->numhitstotal = trim($data);
          break;
        case "OPENSEARCH:STARTINDEX":
          $this->offset = trim($data);
          break;
        case "NUTCH:NEXTPAGE":
        	$this->morepages = true;
      }
    }
  
    if ($this->xml_depth[$parser] == 4) {
      switch ($this->xml_elementname) {  
        case "TITLE":
          if (in_array("title", $this->resultfields)) {
            $this->resultset[$this->hitno]['title'] .= $data;
          }
          break;
        case "NUTCH:ARCDATE":
          if (in_array("date", $this->resultfields)) {
            $this->resultset[$this->hitno]['date'] .= $data;
          }
          break;
        case "DESCRIPTION":
          if (in_array("description", $this->resultfields)) {
            $this->resultset[$this->hitno]['description'] .= $data;
          }
          break;          
        case "LINK":
          if (in_array("url", $this->resultfields)) {        
            $this->resultset[$this->hitno]['url'] .= $data;
          }
          break;
        case "NUTCH:ARCNAME":
          if (in_array("archiveidentifier", $this->resultfields)) {
            $this->arc[$this->hitno]['name'] .= $data;
            #$this->resultset[$this->hitno]['archiveidentifier'] .= $data;
          }
          break;
        case "NUTCH:ARCOFFSET":
          if (in_array("archiveidentifier", $this->resultfields)) {
            $this->arc[$this->hitno]['offset'] .= $data;
          }
          break;
        case "NUTCH:PRIMARYTYPE":
          if (in_array("mime", $this->resultfields)) {
            $this->mime[$this->hitno]['primary'] .= $data;
          }
          break;          
        case "NUTCH:SUBTYPE":
          if (in_array("mime", $this->resultfields)) {
            $this->mime[$this->hitno]['sub'] .= $data;
          }
          break; 
        case "NUTCH:ENCODING":
          if (in_array("encoding", $this->resultfields)) {
            $this->resultset[$this->hitno]['encoding'] .= $data;
          }
          break;
        case "NUTCH:SITE":
          if (in_array("site", $this->resultfields)) {
            $this->resultset[$this->hitno]['site'] .= $data;
          }
          break;                                       
      }
    }
  }
}
?>
