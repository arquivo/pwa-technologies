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

import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.io.ObjectWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapRunnable;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.RecordReader;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.util.ReflectionUtils;
import org.archive.io.ArchiveReader;
import org.archive.io.ArchiveReaderFactory;
import org.archive.io.arc.ARCConstants;
import org.archive.io.arc.ARCRecord;

/**
 * MapRunner that passes an ARCRecord to configured mapper.
 * Configured mapper must be implementation of {@link ARCMapRunner}.
 * @author stack
 */
public class ARCMapRunner implements MapRunnable {
    public final Log LOG = LogFactory.getLog(this.getClass().getName());
    protected ARCRecordMapper mapper;
    private enum Counter {ARCS_COUNT, ARCRECORDS_COUNT,
            BAD_ARC_PARSE_COUNT, ARC_FAILED_DOWNLOAD, LONG_ARCRECORDS_COUNT}

    /**
     * How long to spend indexing.
     */
    protected long maxtime;
    
    public void configure(JobConf job) {
      this.mapper = (ARCRecordMapper)ReflectionUtils.
          newInstance(job.getMapperClass(), job);
      // Value is in minutes.
      this.maxtime = job.getLong("wax.index.timeout", 60) * 60 * 1000;
    }
    
    public void run(RecordReader input, OutputCollector output,
            Reporter reporter)
    throws IOException {
        try {
            WritableComparable key = input.createKey();
            Writable value = input.createValue();
            while (input.next(key, value)) {
                doArchive(value.toString(), output, new ARCReporter(reporter));
            }
        } finally {
            this.mapper.close();
        }
    }
    
    protected void doArchive(final String arcurl, final  OutputCollector output,
            final ARCReporter reporter)
    throws IOException {
        if ((arcurl == null) || arcurl.endsWith("work")) {
            reporter.setStatus("skipping " + arcurl, true);
            return;
        }

        // Run indexing in a thread so I can cover it with a timer.
        final Thread thread = new IndexingThread(arcurl, output, reporter);
        startIndexingThread(thread, reporter);
    }
    
    protected void startIndexingThread(Thread thread, ARCReporter reporter)
    throws IOException {
        thread.setDaemon(true);
        thread.start();
        final long start = System.currentTimeMillis();
        try {
            for (long period = this.maxtime; thread.isAlive() && (period > 0);
                period = this.maxtime - (System.currentTimeMillis() - start)) {
                try {
                    thread.join(period);
                } catch (final InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } finally {
            cleanup(thread, reporter);
        }
    }
    
    protected void cleanup(final Thread thread, final ARCReporter reporter)
            throws IOException {
        if (!thread.isAlive()) {
            return;
        }
        reporter.setStatus("Killing indexing thread " + thread.getName(), true);
        thread.interrupt();
        try {
            // Give it some time to die.
            thread.join(1000);
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
        if (thread.isAlive()) {
            LOG.info(thread.getName() + " will not die");
        }
    }

    protected class IndexingThread extends Thread {
        protected final String location;
        protected final OutputCollector output;
        protected final ARCReporter reporter;
        

        public IndexingThread(final String loc, final OutputCollector o,
                final ARCReporter r) {
            // Name this thread same as ARC location.
            super(loc);
            this.location = loc;
            this.output = o;
            this.reporter = r;
        }
        
        /**
         * @return Null if fails download.
         */
        protected ArchiveReader getArchiveReader() {
            ArchiveReader arc = null;
            // Need a thread that will keep updating TaskTracker during long
            // downloads else tasktracker will kill us.
            Thread reportingDuringDownload = null;
            try {
                this.reporter.setStatus("opening " + this.location, true);
                reportingDuringDownload = new Thread("reportDuringDownload") {
                    public void run() {
                        while (!this.isInterrupted()) {
                            try {
                                synchronized (this) {
                                    sleep(1000 * 60); // Sleep a minute.
                                }
                                reporter.setStatus("downloading " +
                                    location);
                            /* TODO MC - to be compitable with hadoop 0.14    
                            } catch (final IOException e) {
                                e.printStackTrace();
                                // No point hanging around if we're failing
                                // status.
                                break;
                            */    
                            } catch (final InterruptedException e) {
                                // Interrupt flag is cleared. Just fall out.
                                break;
                            }
                        }
                    }
                };
                reportingDuringDownload.setDaemon(true);
                reportingDuringDownload.start();
                arc = ArchiveReaderFactory.get(this.location);
            } catch (final Throwable e) {
                //try {
                    final String msg = "Error opening " + this.location
                        + ": " + e.toString();
                    this.reporter.setStatus(msg, true);
                    this.reporter.incrCounter(Counter.ARC_FAILED_DOWNLOAD, 1);
                    LOG.info(msg);
                /* TODO MC - to be compitable with hadoop 0.14
                } catch (final IOException ioe) {
                    LOG.warn(this.location, ioe);
                }
                */
            } finally {
                if ((reportingDuringDownload != null)
                        && reportingDuringDownload.isAlive()) {
                    reportingDuringDownload.interrupt();
                }
            }
            return arc;
        }

        public void run() {
            if (this.location == null || this.location.length() <= 0) {
                return;
            }
              
            ArchiveReader arc = getArchiveReader();
            if (arc == null) {
                return;
            }

            try {
                ARCMapRunner.this.mapper.onARCOpen();
                this.reporter.incrCounter(Counter.ARCS_COUNT, 1);              
                
                // Iterate over each ARCRecord.
                for (final Iterator i = arc.iterator();
                        i.hasNext() && !currentThread().isInterrupted();) {
                    final ARCRecord rec = (ARCRecord)i.next();
                    this.reporter.incrCounter(Counter.ARCRECORDS_COUNT, 1);
                    
                    
                    try {
                        ARCMapRunner.this.mapper.map(
                            new Text(rec.getMetaData().getUrl()),
                            new ObjectWritable(rec), this.output,
                            this.reporter);
                        
                        final long b = rec.getMetaData().getContentBegin();
                        final long l = rec.getMetaData().getLength();
                        final long recordLength = (l > b)? (l - b): l;
                        if (recordLength >
                                ARCConstants.DEFAULT_MAX_ARC_FILE_SIZE) {
                            // Now, if the content length is larger than a
                            // standard ARC, then it is most likely the last
                            // record in the ARC because ARC is closed after we
                            // exceed 100MB (DEFAULT_MAX_ARC...). Calling
                            // hasNext above will make us read through the
                            // whole record, even if its a 1.7G video. On a
                            // loaded machine, this might cause us timeout with
                            // tasktracker -- so, just skip out here.
                            this.reporter.setStatus("skipping " +
                                this.location + " -- very long record " +
                                rec.getMetaData());
                            this.reporter.
                                incrCounter(Counter.LONG_ARCRECORDS_COUNT, 1);
                            break;
                        }
                    } catch (final Throwable e) {
                        // Failed parse of record. Keep going.
                        LOG.warn("Error processing " + rec.getMetaData(), e);
                    }
                }
                if (currentThread().isInterrupted()) {
                    LOG.info(currentThread().getName() + " interrupted");
                }                
                this.reporter.setStatus("closing " + this.location, true);
                
            } catch (final Throwable e) {
                // Problem parsing arc file.
                this.reporter.incrCounter(Counter.BAD_ARC_PARSE_COUNT, 1);
                final String msg = "Error parsing " + this.location;
                //try {
                    this.reporter.setStatus(msg, true);
                /* TODO MC - to be compitable with hadoop 0.14
                } catch (final IOException ioe) {
                    ioe.printStackTrace();
                }
                */
                LOG.warn("ARCMapRunner - Throwable:"+ msg, e);            
            } 
	    finally {
                try {
                    arc.close();
                    ARCMapRunner.this.mapper.onARCClose();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}