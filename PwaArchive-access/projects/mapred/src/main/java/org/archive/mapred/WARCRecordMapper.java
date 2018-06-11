/*
 * $Id: ImportArcs.java 1494 2007-02-15 17:47:58Z stack-sf $
 *
 * Copyright (C) 2007 Internet Archive.
 *
 * This file is part of the archive-access tools project
 * (http://sourceforge.net/projects/archive-access).
 *
 * The archive-access tools are free software; you can redistribute them and/or
 * modify them under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or any
 * later version.
 *
 * The archive-access tools are distributed in the hope that they will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License along with
 * the archive-access tools; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.archive.mapred;

import org.apache.hadoop.mapred.Mapper;

import java.io.IOException;

/**
 * Like {@link Mapper} but adds signaling of WARC open and close.
 * @author stack
 */
public interface WARCRecordMapper extends Mapper {
    /**
     * Called after WARC open but before we call
     * {@link #map(String, WARCRecord, OutputCollector, Reporter)}
     * @throws IOException
     */
    public void onWARCOpen() throws IOException;

    /**
     * Called on WARC close.
     * @throws IOException
     */
    public void onWARCClose() throws IOException;
}
