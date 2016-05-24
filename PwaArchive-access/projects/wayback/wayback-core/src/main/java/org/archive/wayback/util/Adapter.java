/* Adapter
 *
 * $Id: Adapter.java 1872 2007-07-25 00:34:39Z bradtofel $
 *
 * Created on 2:39:36 PM Aug 17, 2006.
 *
 * Copyright (C) 2006 Internet Archive.
 *
 * This file is part of Wayback.
 *
 * Wayback is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * Wayback is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with Wayback; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.util;

/**
 *
 *
 * @author brad
 * @version $Date: 2007-07-25 01:34:39 +0100 (Wed, 25 Jul 2007) $, $Revision: 1872 $
 * @param <S> 
 * @param <T> 
 */
public interface Adapter<S,T> {
	/**
	 * Transform one object into another
	 * 
	 * @param o
	 * @return new object that is adapted from the old
	 */
	public T adapt(S o);
}
