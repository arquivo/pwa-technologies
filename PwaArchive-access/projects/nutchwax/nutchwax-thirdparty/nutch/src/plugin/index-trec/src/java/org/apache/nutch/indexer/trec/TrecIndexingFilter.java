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
package org.apache.nutch.indexer.trec;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import org.apache.nutch.metadata.Metadata;
import org.apache.nutch.metadata.Nutch;

import org.apache.nutch.net.protocols.HttpDateFormat;
import org.apache.nutch.net.protocols.Response;

import org.apache.nutch.parse.Parse;

import org.apache.nutch.indexer.IndexingFilter;
import org.apache.nutch.indexer.IndexingException;

import org.apache.nutch.crawl.CrawlDatum;
import org.apache.nutch.crawl.Inlinks;
import org.apache.nutch.crawl.MapWritable;
import org.apache.nutch.parse.ParseData;
import org.apache.nutch.util.mime.MimeType;
import org.apache.nutch.util.mime.MimeTypeException;
import org.apache.nutch.util.mime.MimeTypes;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;



/**
 * Add DOCNO metadata for TREC and the rest metadata similar to WaxIndexingFilter (without arcName and arcOffset)
 *
 * @author Miguel Costa
 */

public class TrecIndexingFilter implements IndexingFilter {
	public static final Log LOG = LogFactory.getLog(TrecIndexingFilter.class);
	public static final String DOCNO_KEY = "DOCNO";
	public static final String CONTENT_TYPE_KEY = "content-type";
	public static final String CONTENT_LENGTH = "contentLength";
	public static final String DATE_KEY = "date";
	public static final String EXACTURL_KEY = "exacturl";
	public static final String COLLECTION = "collection";
    public static final String ARC_NAME = "arcname";

	/** configuration */
	private Configuration conf;
	
	/** digest */
	private MessageDigest md;
	
    /** mimeTypes resolver instance */
	private MimeTypes MIME;


	/**
	 * Constructor
	 * @throws NoSuchAlgorithmException
	 */
	public TrecIndexingFilter() throws NoSuchAlgorithmException {
		super();
		this.md = MessageDigest.getInstance("MD5");
	}
	
	/**
	 * 
	 */
	public Document filter(Document doc, Parse parse, Text url, CrawlDatum datum, Inlinks inlinks) throws IndexingException {

	    if (url == null || url.getLength() <= 0)
	    {
	      LOG.error(doc.toString() + " has no url");	      
	      return doc;
	    }
	    
	    // add date
	    long seconds = datum.getFetchTime() / 1000;
	    if (seconds > Integer.MAX_VALUE)
	    {
	      LOG.warn("Fetch time " + Long.toString(seconds) + " is > Integer.MAX_VALUE. Setting to zero");	        
	      seconds = 0;
	    }	    
	    doc.add(new Field(DATE_KEY, ArchiveUtilsSubset.zeroPadInteger((int) seconds), Field.Store.YES, Field.Index.UN_TOKENIZED));

	    // Add as stored, unindexed, and untokenized. Don't warn if absent.
	    // Its not a tradegy.
	    //add(urlStr, doc, "encoding", parse.getData().getMeta(ENCODING_KEY), false, true, true, false, false);
		
		addMeta(doc, parse.getData(), url.toString(), datum);
		return doc;
	}

	/**
	 * Add metadata to index
	 * @param doc
	 * @param data
	 * @param url
	 * @param datum
	 * @return document with added metadata
	 */
	private Document addMeta(Document doc, ParseData data, String url, CrawlDatum datum) {

		// add docno
		add(url, doc, DOCNO_KEY, data.getMeta(DOCNO_KEY), false, true, true, false);
		// add content length
		add(url, doc, CONTENT_LENGTH, data.getMeta(CONTENT_LENGTH), false, true, false, false);
		// add exact url
		add(url, doc, EXACTURL_KEY, escapeUrl(url.toString()), false, false, true, false);
		// add collection
		add(url, doc, COLLECTION, data.getMeta(COLLECTION), false, true, false, false);
		// add collection
		add(url, doc, ARC_NAME, data.getMeta(ARC_NAME), false, true, false, false);
					
        // add mimetype
  	  	String mimetype = data.getMeta(CONTENT_TYPE_KEY);      
  	  	if (mimetype == null || mimetype.length() == 0) {
  	  		MimeType mt = (MIME.getMimeType(url));        
  	  		if (mt != null) {
  	  			mimetype = mt.getName();
  	  		}
  	  	}
      
  	  	try
  	  	{
  	  		// Test the mimetype makes some sense. If not, don't add.
  	  		mimetype = (new MimeType(mimetype)).getName();
  	  	}
  	  	catch (MimeTypeException e) {
  	  		LOG.error(url + ", mimetype " + mimetype + ": " + e.toString());
  	  		// Clear mimetype because caused exception.
  	  		mimetype = null;
  	  	}

  	  	if (mimetype != null) {
  	  		// wera wants the sub and primary types in index. So they are
  	  		// stored but not searchable. nutch adds primary and subtypes
  	  		// as well as complete type all to one 'type' field.
  	  		final String type = "type";
  	  		add(url, doc, type, mimetype, true, false, true, false);
  	  		int index = mimetype.indexOf('/');
        
  	  		if (index > 0) {
  	  			String tmp = mimetype.substring(0, index);
  	  			add(url, doc, "primaryType", tmp, true, true, false, false);
  	  			add(url, doc, type, tmp, true, false, true, false);
          
  	  			if (index + 1 < mimetype.length()) {
  	  				tmp = mimetype.substring(index + 1);
  	  				add(url, doc, "subType", tmp, true, true, false, false);
  	  				add(url, doc, type, tmp, true, false, true, false);
  	  			}
  	  		}
  	  	}
                 
		return doc;
	}




	private String getMetadataValue(final String key, final ParseData pd,
			final MapWritable mw)
	{
		String v = pd.getMeta(key);

		if (v == null || v.length() == 0 && mw != null)
		{
			Writable w = mw.get(new Text(key));

			if (w != null)
			{
				v = w.toString();
			}
		}

		return v;
	}

	private String escapeUrl(String url)
	{
		this.md.reset();

		return Base32.encode(this.md.digest(url.getBytes()));
	}

	private void add(final String url, final Document doc,
			final String fieldName, final String fieldValue,
			boolean lowerCase, boolean store, boolean index,
			boolean tokenize)
	{
		add(url, doc, fieldName, fieldValue, lowerCase, store, index, tokenize,
				true);
	}

	private void add(final String url, final Document doc,
			final String fieldName, final String fieldValue,
			boolean lowerCase, boolean store, boolean index,
			boolean tokenize, final boolean warn)
	{
		if (fieldValue == null || fieldValue.length() <= 0)
		{
			if (warn)
			{
				LOG.error("No " + fieldName + " for url " + url);
			}

			return;
		}

		doc.add(new Field(fieldName,
				(lowerCase? fieldValue.toLowerCase(): fieldValue),
				store? Field.Store.YES: Field.Store.NO,
						index?
								(tokenize? Field.Index.TOKENIZED: Field.Index.UN_TOKENIZED):
									Field.Index.NO));
	}

	public void setConf(Configuration conf) {
		this.conf = conf;
		MIME = MimeTypes.get(getConf().get("mime.types.file"));
	}

	public Configuration getConf() {
		return this.conf;
	}
	
}
