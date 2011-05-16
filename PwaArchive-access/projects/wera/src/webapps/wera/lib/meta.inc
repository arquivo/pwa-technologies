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
* Class for parsing XML metadata record returned from Retriever 
* 
* $Id: meta.inc 383 2005-10-10 11:23:40Z sverreb $
*/

/**
* Class for parsing XML metadata record returned from Retriever
*
*/

class metaParser {
	var $retrieverurl;
	var $xml_parser; // Object Reference to xml parser
	var $aid;
	var $retriever_url;
	var $errormsg;
	var $metadata = array();
	var $xml_parser_in = array
				( "aid" => false, 
					"url" => false,
					"type" => false,
					"archival_time" => false,
					"last_modified_time" => false,
					"content_length" => false,
					"type" => false,
					"charset" => false,
					"filestatus" => false,
					"filestatus_long" => false,
					"content_checksum" => false,
					"http-header" => false,
					"errormessage" => false
				);

		

	/**
	* Constructor. 
	* Set some initial values 
	*/
	function metaParser($aid) {
		include ("config.inc");
		$this->aid = $aid;
		$this->retriever_url = $document_retriever . "?reqtype=getmeta&aid=" . $this->aid."&reqtype=getmeta";

	}

  /**
  * If the doParseMeta method returned false
  * use this to fetch the error message string
  *
  * @return  string Error message
  */
  function getErrorMessage() {
    return $this->errormsg;
  } 
  
  /**
  * Get the metadata array
  *
  * @return  array metadata
  */
  function getMetadata() {
    return $this->metadata;
  }
  
	/**
	* Fetch and parse the metadata record returned from retriever
	*
	* @return boolean False if empty query
	*/

	function doParseMeta() {
		$retval = true;
		$this->xml_parser = xml_parser_create();
		xml_set_object($this->xml_parser, $this);
		xml_set_element_handler($this->xml_parser, "startElement", "endElement");
		xml_set_character_data_handler($this->xml_parser, "characterData");
		
		$data = @ file_get_contents($this->retriever_url);
		if ($data) {
			if (!xml_parse($this->xml_parser, $data)) {
				$retval = false;
				$this->errormsg = sprintf("XML error: %s at line %d", xml_error_string(xml_get_error_code($this->xml_parser)), xml_get_current_line_number($this->xml_parser));
			}
			xml_parser_free($this->xml_parser);
		}
		else {
			$retval = false;
			$this->errormsg = "Error : Failed to open stream!";
		}
		
		if ($this->metadata['errormessage']) {
			$this->errormsg = "Error from retriever : <a href=\"" . $this->retriever_url . "\">" . $this->metadata['errormessage'] . "</a>";
			$retval = false;
		}
		return $retval;
	}

	function startElement($parser, $name, $attrs) {
		if ($name == "AID") {
    	$this->xml_parser_in["aid"] = true;
		}
		elseif ($name == "URL") {
			$this->xml_parser_in["url"] = true;
		}
		elseif ($name == "TYPE") {
			$this->xml_parser_in["type"] = true;
		}
		elseif ($name == "ARCHIVAL_TIME") {
			$this->xml_parser_in["archival_time"] = true;
		}
		elseif ($name == "LAST_MODIFIED_TIME") {
			$this->xml_parser_in["last_modified_time"] = true;
		}
		elseif ($name == "CONTENT_LENGTH") {
			$this->xml_parser_in["content_length"] = true;
		}
		elseif ($name == "TYPE") {
			$this->xml_parser_in["type"] = true;
		}
		elseif ($name == "CHARSET") {
			$this->xml_parser_in["charset"] = true;
		}
		elseif ($name == "FILESTATUS") {
			$this->xml_parser_in["filestatus"] = true;
		}
		elseif ($name == "FILESTATUS_LONG") {
			$this->xml_parser_in["filestatus_long"] = true;
		}
		elseif ($name == "CONTENT_CHECKSUM") {
			$this->xml_parser_in["content_checksum"] = true;
		}
		elseif ($name == "HTTP-HEADER") {
			$this->xml_parser_in["http-header"] = true;
		}		
		elseif ($name == "ERRORMESSAGE") {
			$this->xml_parser_in["errormessage"] = true;
		}		

	}

	function endElement($parser, $name) {
		if ($name == "AID") {
    	$this->xml_parser_in["aid"] = false;
		}
		elseif ($name == "URL") {
			$this->xml_parser_in["url"] = false;
		}
		elseif ($name == "TYPE") {
			$this->xml_parser_in["type"] = false;
		}
		elseif ($name == "ARCHIVAL_TIME") {
			$this->xml_parser_in["archival_time"] = false;
		}
		elseif ($name == "LAST_MODIFIED_TIME") {
			$this->xml_parser_in["last_modified_time"] = false;
		}
		elseif ($name == "CONTENT_LENGTH") {
			$this->xml_parser_in["content_length"] = false;
		}
		elseif ($name == "TYPE") {
			$this->xml_parser_in["type"] = false;
		}
		elseif ($name == "CHARSET") {
			$this->xml_parser_in["charset"] = false;
		}
		elseif ($name == "FILESTATUS") {
			$this->xml_parser_in["filestatus"] = false;
		}
		elseif ($name == "FILESTATUS_LONG") {
			$this->xml_parser_in["filestatus_long"] = false;
		}
		elseif ($name == "CONTENT_CHECKSUM") {
			$this->xml_parser_in["content_checksum"] = false;
		}
		elseif ($name == "HTTP-HEADER") {
			$this->xml_parser_in["http-header"] = false;
		}
		elseif ($name == "ERRORMESSAGE") {
			$this->xml_parser_in["errormessage"] = true;
		}		
				
	}

	function characterData($parser, $data) {
		//print_r($this->xml_parser_in);
		//print $data;
    if ($this->xml_parser_in["aid"]) {
			$this->metadata['aid'] .= $data;
		}
    if ($this->xml_parser_in["url"]) {
			$this->metadata['url'] .= $data;
		}
		elseif ($this->xml_parser_in["type"]) {
			$this->metadata['type'] .= $data;		
		}
		elseif ($this->xml_parser_in["archival_time"]) {
			$this->metadata['archival_time'] .= $data;
		}
		elseif ($this->xml_parser_in["last_modified_time"]) {
			$this->metadata['last_modified_time'] .= $data;
		}
		elseif ($this->xml_parser_in["content_length"]) {
			$this->metadata['content_length'] .= $data;
		}
		elseif ($this->xml_parser_in["type"]) { 
			$this->metadata['type'] .= $data;
		}
		elseif ($this->xml_parser_in["charset"]) {
			$this->metadata['charset'] .= $data;
		}
		elseif ($this->xml_parser_in["filestatus"]) {
			$this->metadata['filestatus'] .= $data;
		}
		elseif ($this->xml_parser_in["filestatus_long"]) {
			$this->metadata['filestatus_long'] .= $data;
		}
		elseif ($this->xml_parser_in["content_checksum"]) {
			$this->metadata['content_checksum'] .= $data;
		}
		elseif ($this->xml_parser_in["http-header"]) {
			$this->metadata['http-header'] .= $data;
		}
		elseif ($this->xml_parser_in["errormessage"]) {
			$this->metadata['errormessage'] .= $data;
		}		
  }
}
?>
