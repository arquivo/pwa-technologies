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
 * Documenthandler that lets the document pass trough unchanged.
 *
 * $Id: passthrough.php 357 2005-10-05 01:38:18Z sverreb $
 *
 * Input parameters:
 * aid - The document's identifier in the archive
 * time - The document's timestamp
 * mime - The document's mime-type
 * url - The document's original url
 */
$aid = $_REQUEST['aid'];
$time = $_REQUEST['time'];
$mime = $_REQUEST['mime'];
$url = $_REQUEST['url'];
if ($fd = @fopen ($aid, "r")) {
    if (isset($mime)) {
      Header("content-type: $mime", false);
    }
    fpassthru($fd);
}
?>
