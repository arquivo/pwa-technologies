/* ExclusionAuthority
 *
 * $Id: ExclusionAuthority.java 1893 2007-07-26 21:42:35Z bradtofel $
 *
 * Created on 3:34:19 PM May 9, 2006.
 *
 * Copyright (C) 2006 Internet Archive.
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
package org.archive.wayback.accesscontrol.ui;


/**
 *
 *
 * @author brad
 * @version $Date: 2007-07-26 22:42:35 +0100 (Thu, 26 Jul 2007) $, $Revision: 1893 $
 */
public interface ExclusionAuthority {
	/**
	 * determines whether userAgent can view urlString for captureDate, 
	 * encapsulating the response in a returned ExclusionResponse object
	 * 
	 * @param userAgent
	 * @param urlString
	 * @param captureDate 
	 * @return ExclusionResponse with answer to the query
	 * @throws Exception 
	 */
	public ExclusionResponse checkExclusion(String userAgent, String urlString,
			String captureDate)	throws Exception;
}
