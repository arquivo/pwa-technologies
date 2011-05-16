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

package org.apache.nutch.parse.trec;

import org.apache.nutch.protocol.Content;
import org.apache.nutch.parse.*;
import org.apache.nutch.util.*;
import org.apache.nutch.protocol.ProtocolFactory;
import org.apache.nutch.protocol.Protocol;
import org.apache.nutch.protocol.Content;
import org.apache.nutch.protocol.ProtocolException;
import org.apache.nutch.parse.ParseUtil;
import org.apache.nutch.parse.ParseException;
import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.util.NutchConfiguration;
import org.apache.hadoop.io.Text;
import org.apache.nutch.crawl.CrawlDatum;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileOutputStream;
import org.apache.nutch.protocol.Content;
import org.apache.nutch.metadata.Metadata;
import org.apache.hadoop.conf.Configuration;
import java.io.StringReader;
import java.io.BufferedReader;
import java.lang.StringBuffer;
import java.io.IOException;


/**
 * Parser for documents in TREC collections (GOV1 and GOV2) from which the text were extracted (pdf, word, ps)
 * @author Miguel Costa
 */
public class TrecParser implements Parser {
 
  private final static int LIMIT_TOKENS=15; 	
  private Configuration conf;

  public Parse getParse(Content content) {

    // ParseData parseData = new ParseData(ParseStatus.STATUS_SUCCESS, "", new
    // Outlink[0], metadata);

    String encoding = StringUtil.parseCharacterEncoding(content
        .getContentType());
    String text;
    if (encoding != null) { // found an encoding header
      try { // try to use named encoding
        text = new String(content.getContent(), encoding);
      } catch (java.io.UnsupportedEncodingException e) {
        return new ParseStatus(e).getEmptyParse(getConf());
      }
    } else {
      // FIXME: implement charset detector. This code causes problem when
      // character set isn't specified in HTTP header.
      text = new String(content.getContent()); // use default encoding
    }
    Metadata meta=content.getMetadata();
    String title=getTitle(text);
    meta.set(Metadata.TITLE, title);
    content.setMetadata(meta);
        
    ParseData parseData = new ParseData(ParseStatus.STATUS_SUCCESS, "",
        OutlinkExtractor.getOutlinks(text, getConf()), meta);
    parseData.setConf(this.conf);
    return new ParseImpl(text, parseData);    
  }

  public void setConf(Configuration conf) {
    this.conf = conf;
  }

  public Configuration getConf() {
    return this.conf;
  }

  /**
   * Extract title from plain text (first paragrapth until the maximum of tokens with LIMIT_TOKENS length)
   * @param text text
   * @return title from @text
   */
  private String getTitle(String text) {	  
	  StringBuffer buf=new StringBuffer();
	  BufferedReader reader=new BufferedReader(new StringReader(text));
	  int i=0;
	  boolean hasText=false;
	  boolean stop=false;
	  int ntokens=0;
	  String saux=null;
	  String tokens[]=null;
	  
	  try {
		  while (!stop && (saux=reader.readLine())!=null) {
			  saux=saux.trim();		  
		  		 
			  if (saux.equals("")) {
				  if (hasText) {
					  stop=true;
				  }
			  }
			  else {
				  hasText=true;
				  
				  tokens=saux.split("[^a-zA-Z_0-9‡¿·¡È…Ë»ÌÕÏÃÛ”Ú”˙⁄˘Ÿ„√ı’‚¬Í Ù‘˚€Á«]"); // a non-word character
				  for (int j=0; ntokens<LIMIT_TOKENS && j<tokens.length; j++) {
					  if (!tokens[j].equals("")) {
						  if (ntokens!=0) {
							  buf.append(' ');
						  }	
						  buf.append(tokens[j]);
						  ntokens++;						  
					  }
				  }
				  if (ntokens==LIMIT_TOKENS) {
					  stop=true;
				  }			  
			  }
		  		  
			  i++;
		  }
	  }
	  catch (IOException e) {
		  e.printStackTrace();
		  return "";
	  }
	  
	  return buf.toString();
  }

  /**                                                                                                                                                                                                                                       
   * Main for testing.                                                                                                                                                                                    
   */
  public static void main(String args[]) {
        String file = args[0];
        System.out.println("File="+file);

        try {
            Configuration conf = NutchConfiguration.create();

            byte[] raw = getRawBytes(new File(file));
            Metadata meta = new Metadata();
            Content content = new Content(file, file, raw, "trec/plain", meta, conf);

            //Protocol protocol = new ProtocolFactory(conf).getProtocol(file);                                                                                                                                                              
            //Content content = protocol.getProtocolOutput(new Text(file), new CrawlDatum()).getContent();                                                                                                                                  
            //Parse parse = new ParseUtil(conf).parseByExtensionId("parse-pdf", content);                                                                                                                                                   

            TrecParser parser=new TrecParser();
            System.out.println("TEXT:\n"+parser.getParse(content).getText());
            //System.out.println("TEXT:\n"+parse.getText());                                                                                                                                                                                
            System.out.println("METADATA:\n"+meta);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
  }

  /**
   * Get bytes from file
   * @param f
   * @return
   */
  private final static byte[] getRawBytes(File f) {
      try {
          if (!f.exists())
              return null;
          FileInputStream fin = new FileInputStream(f);
          byte[] buffer = new byte[(int) f.length()];
          fin.read(buffer);
          fin.close();
          return buffer;
      } catch (Exception err) {
          err.printStackTrace();
          return null;
      }
  }

}
