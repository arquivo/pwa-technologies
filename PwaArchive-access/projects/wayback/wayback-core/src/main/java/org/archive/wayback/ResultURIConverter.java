/* ReplayURI
 *
 * $Id: ResultURIConverter.java 1868 2007-07-25 00:29:19Z bradtofel $
 *
 * Created on 5:20:43 PM Nov 1, 2005.
 *
 * Copyright (C) 2005 Internet Archive.
 *
 * This file is part of wayback.
 *
 * wayback is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback;

/**
 * 
 * 
 * @author brad
 * @version $Date: 2007-07-25 01:29:19 +0100 (Wed, 25 Jul 2007) $, $Revision: 1868 $
 */
public interface ResultURIConverter {
	/**
	 * return an absolute URL that will replay URL url at time datespec.
	 * 
	 * @param datespec
	 * @param url
	 * @return absolute replay URL
	 */
	public String makeReplayURI(final String datespec, final String url);
}
