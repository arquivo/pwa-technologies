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
 * Created on Aug 29, 2005
 *
 * $Id: findversions.php 357 2005-10-05 01:38:18Z sverreb $
 */
include("../lib/config.inc");
include($conf_includepath . "/header.inc");
include ($conf_includepath . "/documentLocator.inc");
include($conf_index_file);
?>

<form action="findversions.php" method="get">
Url: <input type='text' name='url' value='<?php print $url; ?>' size="50"/><br/>
Time: <input type='text' name='timestamp' value='<?php print $timestamp; ?>' size="14"/><br/>
Mode:
  <select name="mode">
    <option value="NEAR" <?php if ($mode=="NEAR") print "selected"?>>NEAR</option>
    <option value="EXACT" <?php if ($mode=="EXACT") print "selected"?>>EXACT</option>    
    <option value="BEFORE" <?php if ($mode=="BEFORE") print "selected"?>>BEFORE</option>
    <option value="AFTER" <?php if ($mode=="AFTER") print "selected"?>>AFTER</option>
    <option value="FIRST" <?php if ($mode=="FIRST") print "selected"?>>FIRST</option>
    <option value="LAST" <?php if ($mode=="LAST") print "selected"?>>LAST</option>
    <option value="ALL" <?php if ($mode=="ALL") print "selected"?>>ALL</option>
  </select>
  
  <input type="submit" name="name" value="Locate!"/>
</form>  
<pre>  
<?php

$searchEngine = new $conf_index_class();
$locator = new documentLocator();
$locator->initialize($searchEngine, $url, false, $timestamp, $mode);
$numhits = $locator->findVersions();
$result = $locator->getResultSet();
print "\n" . $numhits;
print_r($result);
print "</pre>";


include($conf_includepath . "/footer.inc");

?>

  
