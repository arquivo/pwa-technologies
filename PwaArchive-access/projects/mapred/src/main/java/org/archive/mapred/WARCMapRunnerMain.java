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
import org.archive.io.ArchiveRecord;
import org.archive.io.warc.WARCReader;
import org.archive.io.warc.WARCReaderFactory;
import org.archive.io.warc.WARCRecord;

import java.io.IOException;
import java.util.Iterator;


/**
 * MapRunner that passes an ARCRecord to configured mapper.
 * Configured mapper must be implementation of {@link WARCMapRunnerMain}.
 * @author stack
 */
public class WARCMapRunnerMain {
    public final Log LOG = LogFactory.getLog(this.getClass().getName());
    protected WARCRecordMapper mapper;


    protected long maxtime;
    private static String location = "/WEB-20180403125333048-00000-6654_p84.arquivo.pt_8443.warc.gz";

    public static void main(String []args){
        run(location);
    }

    /**
     * How long to spend indexing.
     */




    protected void doArchive(final String warcurl)
            throws IOException {
        if ((warcurl == null) || warcurl.endsWith("work")) {
            System.out.println("skipping " + warcurl);
            return;
        }
    }

    protected void  startIndexingThread(Thread thread, WARCReporter reporter)
            throws IOException {
        thread.setDaemon(true);
        thread.start();
        cleanup(thread, reporter);

    }

    protected static void cleanup(final Thread thread, final WARCReporter reporter)
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
            Thread.currentThread().interrupt();  // set interrupt flag

        }
        if (thread.isAlive()) {

        }
    }


    /**
     * @return Null if fails download.
     */
    protected static WARCReader getArchiveReader(String location) {
        System.out.println("gettting reader");
        WARCReader warc = null;
        final int sleeptime = 1000 * 60;
        // Need a thread that will keep updating TaskTracker during long
        // downloads else tasktracker will kill us.
        Thread reportingDuringDownload = null;
        try {
            reportingDuringDownload = new Thread("reportDuringDownload") {
                public void run() {
                    while (!this.isInterrupted()) {
                        try {
                            synchronized (this) {
                                wait( sleeptime );
                                //sleep( sleeptime ); // Sleep a minute.
                            }
                        } catch (final InterruptedException e) {
                            Thread.currentThread().interrupt();  // JN - set interrupt flag
                            // Interrupt flag is cleared. Just fall out.
                            break;
                        }
                    }
                }
            };
            reportingDuringDownload.setDaemon(true);
            reportingDuringDownload.start();
            warc = WARCReaderFactory.get(location);
            int x= 0;
        } catch (final Exception e) {
            System.out.println("ERROR" + e);
            //try {

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
        return warc;
    }

    public static void run(String location) {

        WARCReader warc = getArchiveReader(location);
        if (warc == null) {
            return;
        }

        try {
            /*  WARCMapRunnerMain.this.mapper.onWARCOpen();*/
            int numberOfRecords = 0 ;
            // Iterate over each WARCRecord.
            for ( Iterator<ArchiveRecord> i = warc.iterator(); i.hasNext();) {
                final WARCRecord rec = (WARCRecord)i.next();
                System.out.println(rec);
                System.out.println(rec.getHeader());

                numberOfRecords++;
                System.out.println((String) rec.getHeader().getHeaderValue("WARC-Target-URI") );
                //  LOG.info("New WARC RECORD!!! " + numberOfRecords);
              /*      try {
                        LOG.info("New WARC RECORD!!! " + numberOfRecords);
                        LOG.info("WARCRecord: " + rec);
                        LOG.info("WARCHEADER: " + rec.getHeader());
                        LOG.info("WARCHEADERURI:" + (String) rec.getHeader().getHeaderValue("WARC-Target-URI") );
              
                        WARCMapRunner.this.mapper.map(
                            new Text((String) rec.getHeader().getHeaderValue("WARC-Target-URI")),
                            new ObjectWritable(rec), this.output,
                            this.reporter);
                        final long b = rec.getHeader().getContentBegin();
                        final long l = rec.getHeader().getLength();
                        final long recordLength = (l > b)? (l - b): l;
                        if (recordLength >
                                WARCConstants.DEFAULT_MAX_WARC_FILE_SIZE) {
                            LOG.info("WARCSIZE: WARCLENGTH > "+ WARCConstants.DEFAULT_MAX_WARC_FILE_SIZE);
                            // Now, if the content length is larger than a
                            // standard WARC, then it is most likely the last
                            // record in the WARC because WARC is closed after we
                            // exceed 100MB (DEFAULT_MAX_WARC...). Calling
                            // hasNext above will make us read through the
                            // whole record, even if its a 1.7G video. On a
                            // loaded machine, this might cause us timeout with
                            // tasktracker -- so, just skip out here.
                            this.reporter.setStatus("skipping " +
                                this.location + " -- very long record " +
                                rec);
                            this.reporter.
                                incrCounter(Counter.LONG_WARCRECORDS_COUNT, 1);
                            break;
                        }
                    } catch (final Exception e) {
                        // Failed parse of record. Keep going.
                        LOG.info("Error processing " + rec, e);
                        LOG.warn("Error processing " + rec, e);
                    }*/
            }

        } catch (final Exception e) {
            // Problem parsing warc file.
            e.printStackTrace();
        }
        finally {
            try {
                warc.close();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
    }
}