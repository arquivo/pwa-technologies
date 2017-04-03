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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.InputSplit;


/**
 * Reporter that logs all status passed; a combined Reporter and logger. Only
 * reports home every so often.
 * @author stack
 */
public class ARCReporter implements Reporter {
    public final Log LOG = LogFactory.getLog(this.getClass().getName());
    private final Reporter wrappedReporter;
    private long nextUpdate = 0;
    private long time = System.currentTimeMillis();

    private static final long FIVE_MINUTES = 1000l * 60l * 5l;
    
    public ARCReporter(final Reporter r) {
        this.wrappedReporter = r;
    }
    
    public void setStatus(final String msg) /*throws IOException*/ {
        setStatus(msg, false);
    }
    
    public void setStatus(final String msg, final boolean writeThrough) /*throws IOException*/ {
        LOG.info(msg);
        // Only update tasktracker every ten seconds -- not for every record.
        long now = System.currentTimeMillis();
        if (writeThrough || now > this.nextUpdate) {
            this.wrappedReporter.setStatus(msg);
            this.nextUpdate = now + (10 * 1000);
            this.time = now;
        }
    }
    
    /**
     * Update reporter if its a long time since last log only.
     * @param msg Message to report IF we haven't reported in a long time.
     * @throws IOException
     */
    public void setStatusIfElapse(final String msg) /*throws IOException*/ {
        long now = System.currentTimeMillis();
        if ((now - this.time) > FIVE_MINUTES) {
            setStatus(msg);
        }
    }

    public void progress() /*throws IOException*/ {
        this.wrappedReporter.progress();
    }

    public void incrCounter(Enum e, long c) {
        this.wrappedReporter.incrCounter(e, c);
    }
    
    public InputSplit getInputSplit() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Unsupported Operation");
    }
}
