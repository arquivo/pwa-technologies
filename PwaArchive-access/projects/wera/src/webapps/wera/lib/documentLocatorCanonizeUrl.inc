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
* This implentation of the Document Locator handles URL canonicalization
* The available rules may be enabled/disabled in config.inc
* There are two types of behaviour with the rules:
* 
* 1. Immediate - The rule si applied to the URL before any location 
*    attempt is carried out
* 
* 2. Try - The rules are applied one by one until a result is returned 
*    from the index (or all the try-rules has been run through) 
*
* $Id: documentLocatorCanonizeUrl.inc 836 2006-03-16 20:06:49Z sverreb $
*/
include_once ($conf_includepath."/documentLocator.inc");

class documentLocatorCanonizeUrl extends documentLocator {

  var $rulesTrail;
  var $canonizeRules;

 /**
  * Initialize
  * 
  * Overrides parent class method, see parent class for details 
  * 
  * @param  object subclass of indexSearch
  * @param  string url of document to find
  * @param  boolean urlextend obsolete
  * @param  string timestamp in number of seconds since the Unix Epoch
  * @param  string query mode i.e. what the document locator should return
  */
  
  function initialize($indexDriver, $url, $urlextend, $time, $querymode) {
    parent :: initialize($indexDriver, $url, $urlextend, $time, $querymode);
    $this->buildRules();
  }

  /**
  * Execute the query
  *
  * Overrides parent class method, see parent class for details
  * 
  * @return integer Number of hits, -1 if something wrong
  */

  function findVersions() {
    $parsed = parse_url($this->getUrl());
    $one_www_rule_applied = false;
    $one_wwwaddslash_rule_applied = false;
    if ($parsed['scheme'] == "http") {
      foreach ($this->canonizeRules as $rule) {
        $parsed = parse_url($this->getUrl());
        switch ($rule['name']) {
          case 'default' :
            $numhits = $this->findVersionsCanonical(unparse_url($parsed), $rule);
            break;
          case 'userinfo' :
            if (isset ($parsed['pass'])) {
              unset ($parsed['pass']);
              unset ($parsed['user']);
              $numhits = $this->findVersionsCanonical(unparse_url($parsed), $rule);
            }
            break;
          case 'sessionids' :
            if (isset ($parsed['query'])) {
              $pattern = "/(^|&)((PHPSESSID|JSESSIONID|sid|ASPSESSIONID)=[^&]+)(&|$)/i";
              $replacement = '$1';
              $parsed['query'] = preg_replace($pattern, $replacement, $parsed['query']);
              $numhits = $this->findVersionsCanonical(unparse_url($parsed), $rule);
            }
            break;
          case 'removefragment' :
            if (isset ($parsed['fragment'])) {
              unset ($parsed['fragment']);
              $numhits = $this->findVersionsCanonical(unparse_url($parsed), $rule);
            }
            break;
          case 'querystrprefix' :
            if (isset ($parsed['query'])) {
              $parsed['query'] = trim($parsed['query'], '&');
              $numhits = $this->findVersionsCanonical(unparse_url($parsed), $rule);
            }
            break;
          case 'addwww' :
            if (!$one_www_rule_applied and substr($parsed['host'], 0, 4) != "www.") {
              $parsed['host'] = "www.".$parsed['host'];
              $numhits = $this->findVersionsCanonical(unparse_url($parsed), $rule);
              $one_www_rule_applied = true;
            }
            break;
          case 'addwwwaddslash' :
            if (!$one_wwwaddslash_rule_applied and substr($parsed['host'], 0, 4) != "www." and substr($parsed['path'], -1, 1) != "/") {
              $parsed['host'] = "www.".$parsed['host'];
              $parsed['path'] .= "/";
              $numhits = $this->findVersionsCanonical(unparse_url($parsed), $rule);
              $one_wwwaddslash_rule_applied = true;
            }
            break;            
          case 'lowercase' :
            $lowercasedurl = strtolower($this->getUrl());
            if (strcmp($lowercasedurl, $this->getUrl()) != 0) {
              $numhits = $this->findVersionsCanonical($lowercasedurl, $rule);
            }
            break;
          case 'stripwww' :
            if (!$one_www_rule_applied and substr($parsed['host'], 0, 4) == "www.") {
              $parsed['host'] = substr($parsed['host'], 4);
              $numhits = $this->findVersionsCanonical(unparse_url($parsed), $rule);
              $one_www_rule_applied = true;
            }
            break;
          case 'stripwwwaddslash' :
            if (!$one_wwwaddslash_rule_applied and substr($parsed['host'], 0, 4) == "www." and substr($parsed['path'], -1, 1) != "/") {
              $parsed['host'] = substr($parsed['host'], 4);
              $parsed['path'] .= "/";
              $numhits = $this->findVersionsCanonical(unparse_url($parsed), $rule);
              $one_wwwaddslash_rule_applied = true;
            }
            break;            
          case 'addslash' :
              if (substr($parsed['path'], -1, 1) != "/") {
                $parsed['path'] .= "/";
                $numhits = $this->findVersionsCanonical(unparse_url($parsed), $rule);
              }
            break;
          case 'stripindex' :
              $pattern = "@(/)index\.[^/]+$@i";
              $replacement = '$1';
              $newpath = preg_replace($pattern, $replacement, $parsed['path']);
              if ($parsed['path'] != $newpath) {
                $parsed['path'] = $newpath;
                $numhits = $this->findVersionsCanonical(unparse_url($parsed), $rule);
              }
            break;            
        }
        if ($numhits > 0) {
          break;
        }
      }
    }

    return $numhits;
  }

 /**
  * Private findVersionCanonical called repeatedly by findVersions
  * for each rule.
  * 
  * @param  String Url
  * @param  Array  The rule to apply and its type
  */ 
  
  function findVersionsCanonical($url, $rule) {
    if ($rule['type'] == 'try' or $rule['name'] == 'default') {
      $cache_url = $this->getUrl();
      $this->setUrl($url);
      $numhits = parent :: findVersions();
      $this->rulesTrail .= "<li>".$rule['name']." (".$rule['type']."): ".$this->getUrl();
    } else {
      $this->rulesTrail .= "<li>Applied rule <b>".$rule['name']."</b> for the (future) default query. Resulting url: ".$url;
      $this->setUrl($url);
    }

    if ($rule['type'] == 'try') {
      $this->setUrl($cache_url);
    }
    return $numhits;
  }

 /**
  * Build rules
  * 
  * Load the rules from config and build an array containing all the rules
  * in the correct order. Immediate rules first, then the default rule and 
  * finally the try-rules (the default rule is not specified in the config 
  * file, but introduced here for convenience) 
  */
  function buildRules() {

    global $conf_url_canonize_rules_immediate;
    global $conf_url_canonize_rules_try;

    $rules = explode("|", $conf_url_canonize_rules_immediate);
    $rules_try = explode("|", $conf_url_canonize_rules_try);

    $i = 0;
    foreach ($rules as $rule) {
      $this->canonizeRules[$i] = array ('name' => $rule, 'type' => 'immediate');
      $i ++;
    }
    $this->canonizeRules[$i] = array ('name' => 'default', 'type' => 'try');

    foreach ($rules_try as $rule) {
      $i ++;
      $this->canonizeRules[$i] = array ('name' => $rule, 'type' => 'try');
    }
  }

 /*
  * Get info on the rules applied and the resulting url(s)
  * 
  * @return string 
  */
  function getRulesTrail() {
    return ("<ul>" . $this->rulesTrail . "</ul>");
  }
}

function unparse_url($parsed) {
  $url .= $parsed[scheme].'://';
  if (isset ($parsed['pass'])) {
    $url .= "$parsed[user]:$parsed[pass]@";
  }
  elseif (isset ($parsed['user'])) {
    $url .= "$parsed[user]@";
  }

  if (isset ($parsed['host']))
    $url .= $parsed['host'];
  if (isset ($parsed['port']))
    $url .= ":$parsed[port]";
  if (isset ($parsed['path']))
    $url .= $parsed['path'];
  if (isset ($parsed['query']) and $parsed['query'] != "")
    $url .= "?$parsed[query]";
  if (isset ($parsed['fragment']))
    $url .= "#$parsed[fragment]";

  return $url;

}
?>