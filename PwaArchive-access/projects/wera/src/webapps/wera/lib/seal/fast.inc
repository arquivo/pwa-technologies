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
* File: fast.inc
*
* This file is used by the fastSearch() class. 
* It has to reside in the $conf_index_path given in config.inc
*
*
* $Id: fast.inc 357 2005-10-05 01:38:18Z sverreb $ 
*/
include("$conf_searchenginepath/indexSearch.inc");
include("$conf_searchenginepath/indexUtils.inc");

function HumanTimeToISOTime($time){
  // Convert time string to ISO8601
  // In YYYYMMDDhhmmss
  // Out YYYY-MM-DDThh:mm:ss
  $isotime = '';
  if (strlen($time) == 14) {
    $isotime = substr($time, 0, 4) . '-' . substr($time, 4, 2) . '-' . substr($time, 6, 2) . 'T' . substr($time, 8, 2) . ':' . substr($time, 10, 2) . ':' . substr($time, 12, 2) . "Z"; 
  }
  return $isotime;
}

/**
* Class for querying FAST Data Search
*
* For further information about constructing 
* queries and setting parameters for the fastSearch
* class see Fast Data Search Documentation
*
*/
class fastSearch extends indexSearch {
  // Fast specific variables
  var $host;
  var $port;
  var $uri;
  var $querytype;
  var $hosturl;
  var $fastadaptedquery;
  var $offset;
  var $hitsperset;
  var $sort = array("byfield" => "", "sortdirection" => "");
  var $resultfieldstring;

  
 /**
  * Constructor. 
  * Set some initial values 
  */
  function fastSearch() {
    include ("fastconfig.inc");    
    $this->host = $fds_conf_host;
    $this->port = $fds_conf_port;
    $this->filter_query = $fds_conf_filter_query;
    $this->query = "";
    $this->numhits = 0;
    $this->hitsperset = 10;
    $this->offset = 0;
    $this->timespent = 0;
    $this->setQueryType('adv');
  }


  
  
  // FAST SPECIFIC FUNCTIONS

  /** 
  * Set query type for search
  *  
  * Set the query type to one of the following
  * all : all terms must be found
  * any : any of the query terms may give a hit
  * phrase : the exact phrase must be found
  * adv : the query is an expression in the Fast advanced query language
  *
  * @param  integer Query type
  */
  function setQueryType($querytype) {
      $this->querytype = $querytype;
  }

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

  function adaptQueryToFastIndex($querystring) {

    /* 
    || FAST specific
    || ------------
    || Used for adapting WERA Search Engine Abstraction Layer queries to FAST Data Search  
    || index structure.
    ||
    */    


#2004-11-22T11:19:18Z;
#2004-11-22-11-19-18

    //print "<!-- QUERY : $querystring -->";

    $querystring = preg_replace("/nwa./", "", $querystring);
    $querystring = preg_replace("/archival_time:/", "archivaltime:", $querystring);
    $querypattern1 = "/(url:|dcformat:|framelinks:|multimedialinks:|links:|archiveidentifier:)([^\ )]*)/e";
    $queryreplacement1 = "'\\1'.index_encode('\\2')";
    $querypattern2 = "/(archivaltime:(\[){0,1})([0-9]*)((;){0,1})([0-9]*)((\]){0,1})/e";
    $queryreplacement2 = "'\\1'.HumanTimeToISOTime('\\3').'\\4'.HumanTimeToISOTime('\\6').'\\7'";
    $querypattern3 = array ("/archiveidentifier:/", "/collection:/", "/INDX2A/");
    $queryreplacement3 = array ("aid:","collectionname:", "*"); 
    $querystring = preg_replace("/%20/", "INDX20", $querystring);
    $querystring = preg_replace("/&amp;/", "&", $querystring);
    $querystring = preg_replace($querypattern1, $queryreplacement1, $querystring);
    $querystring = preg_replace($querypattern2, $queryreplacement2, $querystring);    
    $querystring = preg_replace ($querypattern3, $queryreplacement3, $querystring);
    if ($this->filter_query != "") {
      $querystring = $this->filter_query . " and (" . $querystring . ")";
    }
    return urlencode($querystring);
  }  

  // FUNCTIONS OVERRIDING PARENT CLASS FUNCTIONS
  
  /** 
  * Sets the sortorder 
  * legal values for sortorder is:
  * "ascending" and "descending"
  * any other value will be treated as relevance ranking
  */
  function setSortOrder($sortorder) {
    if ($sortorder == "ascending" or $sortorder == "descending") {
      $this->sort["sortdirection"] = $sortorder;
      $this->sort["byfield"] = "archivaltime";
    }
  } 


  /**
  * Set result set fields
  *
  * Use this to set which fields to include 
  * in the result set returned by the getResultSet 
  * and getXmlResultSet methods.
  *
  * @param  string  Fields in result set
  * @return boolean False if fields not defined as legal result fields .
  */
  function setFieldsInResult($fields) {
    $this->resultfieldstring = " " . str_replace( ",", " ", $fields) . " ";
  }

  
  /**
  * Set query string for search
  * 
  * @param  string  Query string
  */
  function setQuery($querystring) {
    $this->query = $querystring;
    $this->fastadaptedquery = $this->adaptQueryToFastIndex($this->query);
  }

  function doQuery() {
    $retval = true;  
    $this->uri = "http://" . $this->host . ":" . $this->port . "/cgi-bin/asearch?type=" . $this->querytype . "&query=" . $this->fastadaptedquery . "&offset=" . $this->offset . "&hits=" . $this->hitsperset;
    if ($this->sort["byfield"] != "") {
      $this->uri .= "&sortby=" . $this->sort["byfield"] . "&sortdirection=" . $this->sort["sortdirection"];
    }
    //print "<br>uri-en : $this->uri <br>";
    $lines = @file ($this->uri);
    if ($lines) {
      //print "<pre>";
      //print_r($lines);
      //print "</pre>";
      $linestartstring = substr($lines[0], 0, 4);
      $linenumber =  0;
      
      if ($linestartstring == "#ERC") {
        $errcode = substr($lines[0], 5);
        $errtext = substr($lines[1], 5);
        $this->errormsg = $errcode . " : " . $errtext;
        $retval = false;
      }
      else { 
        do {
          $linestartstring = substr($lines[$linenumber], 0, 4);
          $restofline = trim(substr($lines[$linenumber], 5));

          switch ($linestartstring) {
            case "#HTS":
              $this->numhits = (int)$restofline;
              break;
            case "#CNT":
              $this->numhitstotal = (int)$restofline;
              break;
            case "#TIM":
              $this->timespent = $restofline;
              break;
            case "#FIR":
              $firsthit = (int)$restofline;
              break;
            case "#LAS":
              $lasthit = (int)$restofline;
              break;
          }
          
          $linenumber++;
          if ($linestartstring == "###/") {
            break;
          }

        } while(1);
        
        if ($this->numhits > 0) {
          for ($i = $firsthit; $i <= $lasthit; $i++) {
            do {
              $first4chars = substr($lines[$linenumber], 0, 4);
              $firstspacepos = strpos($lines[$linenumber], " ");
              
              $fieldname = substr($lines[$linenumber], 1, $firstspacepos-1);
              $restofline = trim(substr($lines[$linenumber], $firstspacepos));
              switch ($fieldname) {
                case "###":
                  break;
                case "##/":
                  break;
                case "docvector":
                  break;
                case "docid":
                  break;
                case "collection":
                  break;
                case "aid":
                  break;
                case "framelinks":
                  break;
                case "multimedialinks":
                  break;
                case "links":
                  break;
                case "dcidentifier":
                  break;
                case "dcformat";
                  break;
                case "url";
                  break;
                  case "aidview":
                  if (stristr($this->resultfieldstring, 'archiveidentifier')) {
                    $this->resultset[$i]['archiveidentifier'] = $restofline;
                  }
                  break;
                case "urlview":
                  if (stristr($this->resultfieldstring, 'url')) {
                    $this->resultset[$i]['url'] = $restofline;
                  }
                  break;
                case "collectionname":
                  if (stristr($this->resultfieldstring, 'collection')) {
                    $this->resultset[$i]['collection'] = $restofline;
                  }
                  break;
                case "dcdate":
                  if (stristr($this->resultfieldstring, 'dcdate')) {           
                    $tmp = str_replace('-', '', $restofline);
                    $tmp = str_replace(':', '', $tmp);
                    $tmp = str_replace('Z', '', $tmp);
                    $this->resultset[$i]['dcdate'] = str_replace('T', '', $tmp);                    
                  }
                  break;
                case "archivaltime":
                  if (stristr($this->resultfieldstring, 'archival_time')) {                
                    $tmp = str_replace('-', '', $restofline);
                    $tmp = str_replace(':', '', $tmp);
                    $tmp = str_replace('Z', '', $tmp);
                    $this->resultset[$i]['archival_time'] = str_replace('T', '', $tmp);
                  }
                  break;
                case "dcidentifierview":
                  if (stristr($this->resultfieldstring, 'dcidentifier')) {                
                    $this->resultset[$i]['dcidentifier'] = $restofline;
                  }
                  break;
                case "dcformatview":
                  if (stristr($this->resultfieldstring, 'dcformat')) {                
                    $this->resultset[$i]['dcformat'] = str_replace('&#10;', '', $restofline);
                  }
                  break;
                case "linksview":
                  if (stristr($this->resultfieldstring, ' links ')) {                
                    $this->resultset[$i]['links'] = $restofline;
                  }
                  break;                  
                case "framelinksview":
                  if (stristr($this->resultfieldstring, 'framelinks')) {                
                    $this->resultset[$i]['framelinks'] = $restofline;
                  }
                  break;                  
                case "multimedialinksview":
                  if (stristr($this->resultfieldstring, 'multimedialinks')) {                
                    $this->resultset[$i]['multimedialinks'] = $restofline;
                  }
                  break;
                case "encodingview":
                  if (stristr($this->resultfieldstring, 'encoding')) {                
                    $this->resultset[$i]['encoding'] = $restofline;
                  }
                  break;                  
                default:
                  if (stristr($this->resultfieldstring, $fieldname)) {                
                    $this->resultset[$i][$fieldname] = $restofline;
                  }
              }
              $linenumber++;              
              if ($first4chars == "###/") {
                break;
              }
            } while(1);
          }
        }
      }
    } 
    else {
      $this->errormsg = "Could not connect to server: " . $this->host . ":" . $this->port;
      $retval = false;
    }
    //print "<!--";
    //print_r($this->resultset);
    //print "-->";
    return $retval;
  }

}
?>
