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
 * File: en_help.ihtml
 *
 * $Id: en_help.php 591 2005-12-07 14:35:33Z sverreb $
*/
?>
<table border='0' cellpadding='0' cellspacing='0' width=90%>
  <tr>
    <td class="norm" width="1" colspan="5"><img alt='' height='10' src='/images/1px.gif' width="1"></td>
  </tr>
  <tr>
    <td class="norm" width="10"><img alt='' height='1' src='/images/1px.gif' width="1"></td>
    <td class="norm" align="left"><img alt="" src="<?php print $conf_logo;?>"></td>
    <td class="norm" align="right">
        <a href="<?php echo $conf_simple_search; ?>">Search</a>&nbsp;
        <!--<a href="<?php echo $conf_advanced_search; ?>">Advanced search</a></td>-->    
    <td class="norm" width="10"><img alt='' height='1' src='/images/1px.gif' width="1"></td>    
  </tr>
</table>      
      
<table border='0' cellpadding='0' cellspacing='0' width=90%>
  <tr>
    <td class="norm" colspan="5" align="left"><img alt='' height='8' src='/images/1px.gif' width='1'></td>
  </tr>
  <tr>

    <td colspan='5' class='border'><img alt='' height='2' src='/images/1px.gif' width='1'></td>
  </tr>
  <tr>
    <td class="norm" colspan='5'><img src='/images/1px.gif' width='1' height='5' alt=''></td>
  </tr>
</table>
    
<table align="center" class="greyborder" border="0" cellspacing="0" cellpadding="1" width="90%">

  <tr>
    <td>
      <table align="center" class="resultsborder" border="0" cellspacing="0" cellpadding="10" width="100%">
        <tr>
          <td>
    <!-- ********************************************************************************* -->

<h1>Search</h1>

<p><b>Query string</b><br>
<p>
Type one or more search terms. Wera will present the results matching <b>all</b> of the search terms you type in.
<ul>
<li>Search for a phrase: ["term<sub>1</sub> .. term<sub>n</sub>"]</li>
<li>Searching for all documents of type text/html: [type:text type:html]</li>
<li>Search for a specific url: [exacturl:http://www.nb.no/]</li>
</ul>
</p>

<p><b>Search period</b><br>
To limit the the results for a specific time period fill in <i>Year from</i> and/or <i>to</i>.</p>

<p><b>Result list</b><br>
After executing a query, a result list is presented. The result list contains hits with links to the <i>Timeline view</i> and the <i>Overview</i> (see below).</p>

<h1>Overview</h1>
For each hit in the search result list there is a link to the <i>Overview</i>. The overview shows all the dates for the versions found for a given URL. Click one of the dates to view a specific version in the <i>Timeline</i> view.
    
<h1>Timeline View</h1>
The Timeline view shows the different versions of a given URI displayed graphically along a timeline. When entering the Timeline View the latest version available is displayed. The actual archived web page is shown below the Timeline. All links and inline references are altered before the web page is transmitted to user (i.e. the users browser).

<p><b>Url</b><br>
When navigating from the Overview or the result list of a search interface, the Url of the chosen version is passed along and shown in the Url field. You may also enter a Url manually, but the Url has to be entered exactly as stored in the archive when the web page was harvested. 
</p>

<p><b>Timeline</b><br>
The different versions (dates) of a Url are displayed graphically along the timeline. You may navigate between the different versions by directly clicking a specific point on the timeline, or by using the arrown first, previous, next and last.

<p><b>Resolution</b><br>
When entering the timeline view the resolution is set to <i>auto</i>. This means that the timeline automatically drills down to the resolution needed to display single versions along the line. The <i>Auto</i> checkbox may be unchecked in order to manually choose the resolution (choosing a different resolution when in auto also disables auto resolution).</p>

<p><b>Metadata</b><br>
Checking the Metadata checkbox will activate a metadata viewer below the timeline area, instead of the default view of the archived web page..</p>


<p><b>Search</b><br>
To quickly perform a simple search type in a query term and press <i>Go</i>.</p> 
    

          </td>
        </tr>

      </table>
    </td>
  </tr>
</table>

