<?php
header("Content-Type: text/html; charset=UTF-8");
include_once("lib/config.inc");
include($conf_includepath . "/header.inc");
?>
</HEAD>
<BODY><center>
<font face="helvetica,arial,sans-serif">
  
<table align="center" class="resultsborder" border="0" cellspacing="0" cellpadding="0" width="90%">
  <tr>
    <td>  

<?php


include_once($conf_index_file);
include_once("$conf_searchenginepath/indexUtils.inc");
include ("$conf_includepath/meta.inc");
include ("$conf_includepath/documentLocatorCanonizeUrl.inc");

$url = $_REQUEST['url'];
$time = $_REQUEST['time'];
$aid = $_REQUEST['aid'];
$urlnotfound = false;

if (!isset($aid)) {
	$searchEngine = new $conf_index_class();
  $locator = new documentLocatorCanonizeUrl();
  if (!isset($time)) {
  	$locator->initialize($searchEngine, $url, false, $time, 'LAST');
  }
  else {
  	$locator->initialize($searchEngine, $url, false, $time, 'NEAR');
  }
  $numhits = $locator->findVersions();
  if($numhits <= 0) {
		$urlnotfound = true;
  }

  $result = $locator->getResultSet();
  $document = $result[1];
  $aid = $document['archiveidentifier'];
}
	
if ($urlnotfound) {
    print nls("Sorry, no documents with the given url, $url were found");
    if ($conf_url_canonize_debug_on) {
      print "<br><h1>URL Canonicalization</h1>";
      print $locator->getrulesTrail();
    }
}
else {
  if ($conf_url_canonize_debug_on) {
    print "<h1>URL Canonicalization</h1>";
    print $locator->getrulesTrail();
  }
	$names = array (	"aid" => nls("Archive Identifier (aid)"), 
										"url" => nls("Url"),
										"archival_time" => nls("Time of archival"),
										"last_modified_time" => nls("Last modified time"),
										"type" => nls("Mime-type"),
										"charset" => nls("Character Encoding"),
										"filestatus" => nls("File Status"),
										"content_checksum" => nls("Content Checksum"),
										"http-header" => nls("HTTP Header") );
	
	$metaParser = new metaParser($aid);
	if ($metaParser->doParseMeta()) {
		$metadata = $metaParser->getMetadata();
		print "<h1>Metadata</h1>\n";
	 	print "<table class=\"resultsborder\">";
	 	foreach ($metadata as $k => $v) {
	   	echo "<tr><td><b>";
	   	echo	$names[$k];
			print "</b></td></tr><tr><td>&nbsp;&nbsp;$v</td></tr>";
		}
		print "</table>";
	}
	else {
		print $metaParser->getErrorMessage();
	}
}
?>
</td>
</tr>
</table>
</center>

<?php
  include($conf_includepath . "/footer.inc");
?>
