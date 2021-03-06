/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.nutch.parse;

// Commons Logging imports
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.hadoop.io.*;
import org.apache.hadoop.io.SequenceFile.CompressionType;
import org.apache.nutch.crawl.CrawlDatum;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.mapred.*;
import org.apache.nutch.scoring.ScoringFilterException;
import org.apache.nutch.scoring.ScoringFilters;
import org.apache.nutch.util.StringUtil;
import org.apache.nutch.metadata.Nutch;
import org.apache.nutch.net.*;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.hadoop.util.Progressable;

/* Parse content in a segment. */
public class ParseOutputFormat implements OutputFormat {
  private static final Log LOG = LogFactory.getLog(ParseOutputFormat.class);

  private URLNormalizers urlNormalizers;
  private URLFilters filters;
  private ScoringFilters scfilters;

  public void checkOutputSpecs(FileSystem fs, JobConf job) throws IOException {
    if (fs.exists(new Path(job.getOutputPath(), CrawlDatum.PARSE_DIR_NAME)))
      throw new IOException("Segment already parsed!");
  }

  public RecordWriter getRecordWriter(FileSystem fs, JobConf job,
                                      String name, Progressable progress) throws IOException {

    this.urlNormalizers = new URLNormalizers(job, URLNormalizers.SCOPE_OUTLINK);
    this.filters = new URLFilters(job);
    this.scfilters = new ScoringFilters(job);
    final float interval = job.getFloat("db.default.fetch.interval", 30f);
    final boolean ignoreExternalLinks = job.getBoolean("db.ignore.external.links", false);
    
    Path text =
      new Path(new Path(job.getOutputPath(), ParseText.DIR_NAME), name);
    Path data =
      new Path(new Path(job.getOutputPath(), ParseData.DIR_NAME), name);
    Path crawl =
      new Path(new Path(job.getOutputPath(), CrawlDatum.PARSE_DIR_NAME), name);
    
    final MapFile.Writer textOut =
      new MapFile.Writer(job, fs, text.toString(), Text.class, ParseText.class, CompressionType.RECORD);
    
    final MapFile.Writer dataOut =
      new MapFile.Writer(job, fs, data.toString(), Text.class,ParseData.class);
    
    final SequenceFile.Writer crawlOut =
      SequenceFile.createWriter(fs, job, crawl, Text.class, CrawlDatum.class);
    
    return new RecordWriter() {


        public void write(WritableComparable key, Writable value)
          throws IOException {
          
          Parse parse = (Parse)value;
          String fromUrl = key.toString();
          String fromHost = null; 
          String toHost = null;          
          textOut.append(key, new ParseText(parse.getText()));
          
          ParseData parseData = parse.getData();
          // recover the signature prepared by Fetcher or ParseSegment
          String sig = parseData.getContentMeta().get(Nutch.SIGNATURE_KEY);
          if (sig != null) {
            byte[] signature = StringUtil.fromHexString(sig);
            if (signature != null) {
              // append a CrawlDatum with a signature
              CrawlDatum d = new CrawlDatum(CrawlDatum.STATUS_SIGNATURE, 0.0f);
              d.setSignature(signature);
              crawlOut.append(key, d);
            }
          }

          // collect outlinks for subsequent db update
          Outlink[] links = parseData.getOutlinks();
          if (ignoreExternalLinks) {
            try {
              fromHost = new URL(fromUrl).getHost().toLowerCase();
            } catch (MalformedURLException e) {
              fromHost = null;
            }
          } else {
            fromHost = null;
          }

          String[] toUrls = new String[links.length];
          int validCount = 0;
          for (int i = 0; i < links.length; i++) {
            String toUrl = links[i].getToUrl();
            try {
              toUrl = urlNormalizers.normalize(toUrl, URLNormalizers.SCOPE_OUTLINK); // normalize the url
              toUrl = filters.filter(toUrl);   // filter the url
            } catch (Exception e) {
              toUrl = null;
            }
            // ignore links to self (or anchors within the page)
            if (fromUrl.equals(toUrl)) toUrl = null;
            if (toUrl != null) validCount++;
            toUrls[i] = toUrl;
          }
          CrawlDatum adjust = null;
          // compute score contributions and adjustment to the original score
          for (int i = 0; i < toUrls.length; i++) {
            if (toUrls[i] == null) continue;
            if (ignoreExternalLinks) {
              try {
                toHost = new URL(toUrls[i]).getHost().toLowerCase();
              } catch (MalformedURLException e) {
                toHost = null;
              }
              if (toHost == null || !toHost.equals(fromHost)) { // external links
                continue; // skip it
              }
            }
            CrawlDatum target = new CrawlDatum(CrawlDatum.STATUS_LINKED, interval);
            Text targetUrl = new Text(toUrls[i]);
            adjust = null;
            try {
              adjust = scfilters.distributeScoreToOutlink((Text)key, targetUrl,
                      parseData, target, null, links.length, validCount);
            } catch (ScoringFilterException e) {
              if (LOG.isWarnEnabled()) {
                LOG.warn("Cannot distribute score from " + key + " to " +
                         targetUrl + " - skipped (" + e.getMessage());
              }
              continue;
            }
            crawlOut.append(targetUrl, target);
            if (adjust != null) crawlOut.append(key, adjust);
          }
          dataOut.append(key, parseData);
        }
        
        public void close(Reporter reporter) throws IOException {
          textOut.close();
          dataOut.close();
          crawlOut.close();
        }
        
      };
    
  }

}
