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
 * Functions for National Language Support (NLS)
 *
 * $Id: nls.inc 346 2005-10-04 22:59:28Z stack-sf $
 */


if(!isset($NLS_LOCALE)) {
  $NLS_LOCALE = "";
}
if(!isset($NLS_CONTEXT)) {
  $NLS_CONTEXT = "";
}
if(!isset($NLS_PATH)) {
  $NLS_PATH = "$conf_includepath/nls";
}


/**
 * Translates a string to the current locale.
 * 
 * @param string String to translate
 * @param context Overrides current context.
 * @param locale Overrides current locale.
 * @returns Returns the translated string or $string if no translation was found.
 */
function nls ($string, $context="", $locale="") {
  global $NLS_LOCALE, $NLS_PATH, $NLS_LANGUAGE_ARRAY;
  $context = nls_getcontext($context);
  $localelist = nls_getlocale($locale);
  $localenum = count($localelist);

  for($i=0; $i<$localenum; $i++) {
    $NLS_FILES[$i] = $NLS_PATH . "/" . $localelist[$i] . "/" . $context . ".nls";
    $arrayindex[$i] = $context . '�' . $localelist[$i];
  }
  for($i=0; $i<$localenum; $i++) {
    if ($NLS_LANGUAGE_ARRAY[$arrayindex[$i]] == "" && file_exists($NLS_FILES[$i])) {
      eval("\$NLS_LANGUAGE_ARRAY['" . $arrayindex[$i] . "'] = array (" . (join(', ', array_unique(file($NLS_FILES[$i])))) . ");");
    }

    if (isset($NLS_LANGUAGE_ARRAY[$arrayindex[$i]][$string])) {
      return $NLS_LANGUAGE_ARRAY[$arrayindex[$i]][$string];
    }
  }
  if($i == $localenum) {
    return $string;
  }
}


/**
 * Translates a string to the current locale.
 * 
 * @param string String to translate
 * @param varlist array of parameters to substitute for placeholders
 * @param context Overrides current context.
 * @param locale Overrides current locale.
 * @returns Returns the translated string or $string if no translation was found.
 */
function nlsf ($string, $varlist, $context="", $locale="") {
  $string = nls($string, $context, $locale);
  $pattern = '/(?<!\\\)%([^;]*);/e';
  $subst = "_format_data('$1', \$varlist)";
  $string = preg_replace($pattern, $subst, $string);
  return $string;
}


/**
 * Format = %itx;
 *   i = index
 *   t = type (s = string, d = date, n = number, c = currency)
 *   x = extra data
 */
function _format_data($command, $varlist) {
  preg_match('/([0-9]*)(\w)(.*)/', $command, $parts);
  if($parts[1] == '' || $parts[2] == '') {
    return ("ERROR");
  }
  $value = $varlist[$parts[1]-1];
  switch($parts[2]) {
  case 's': // String
    $result = $value;
    break;
  case 'n': // Number
    $result = $value;
    break;
  default:
    $result = 'ERROR';
  }
  return $result;
}


/**
* Find current locale.
* Locales are searched for in the following order:
*  * parameter to this function.
*  * parameter set with nls_setlocale().
*  * locale set in browser.
*/
function nls_getlocale ($locale="") {
  global $NLS_LOCALE, $HTTP_SERVER_VARS;
  if ($locale != "") {
    $result = $locale;
  } elseif ($NLS_LOCALE != "") {
    $result = $NLS_LOCALE;
  } else {
    $result = $HTTP_SERVER_VARS["HTTP_ACCEPT_LANGUAGE"];
  }
  $result = explode(',', $result);

  $locale = array();
  while($element = array_shift($result)) {
    $element = substr($element, 0, strcspn($element, ';'));
    $locale[] = $element;
    $language = nls_getlanguage($element);
    if($element != $language) {
      $locale[] = $language;
    }
  }
  return $locale;
}


/**
 * Set which locale to use
 */
function nls_setlocale ($locale) {
  global $NLS_LOCALE;
  $NLS_LOCALE = $locale;
}


/**
 * Returns the lowercase language code (ISO 639) from a locale
 */
function nls_getlanguage ($locale) {
  return strtolower(substr($locale, 0, 2));
}


/**
 * Returns the uppercase country code (ISO 3166) from a locale
 */
function nls_getcountry  ($locale) {
  return strtolower(substr($locale, 3, 2));
}


/**
 *
 */
function nls_getcontext ($context="") {
  global $NLS_CONTEXT;
  if ($context != "") {
    return $context;
  } elseif ($NLS_CONTEXT != "") {
    return $NLS_CONTEXT;
  } else {
    return basename($_SERVER['PHP_SELF']);
  }
}


/**
 *
 */
function nls_setcontext ($context) {
  global $NLS_CONTEXT;
  $NLS_CONTEXT = $context;
}

?>
