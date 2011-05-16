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

/*
 * File: help.php
 *
 * $Id: help.php 390 2005-10-12 12:59:54Z sverreb $
*/

include_once("lib/config.inc");
$locale_array = nls_getlocale();
$language = nls_getlanguage($locale_array[0]);
$helpfile = "$conf_rootpath/help/" . $language . "_help.php";
if (!file_exists($helpfile)) {
  $language = "en";
  $helpfile = "$conf_rootpath/help/" . $language . "_help.php";
}
Header("content-type: text/html; charset=UTF-8", false);
?>

<HTML>
<HEAD>
<link rel="stylesheet" href="<?php print $conf_gui_style;?>" type="text/css">
<META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=UTF-8">
<TITLE>WERA help</TITLE>
</HEAD>

<body><center>

<?php
  include ($helpfile); 
?>
</center></body>
</html>
