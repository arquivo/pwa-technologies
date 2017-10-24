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

package org.apache.nutch.searcher;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.global.Global;
import org.apache.nutch.parse.Parse;
import org.apache.nutch.util.NutchConfiguration;
import org.apache.nutch.util.RFC3339Date;
import org.apache.lucene.search.PwaFunctionsWritable;
import org.w3c.dom.*;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.parsers.*;


/** Present search results using A9's OpenSearch extensions to RSS, plus a few
 * Nutch-specific extensions. */   
public class OpenSearchServletInternal extends HttpServlet {
	 	
  private static final Log LOG = LogFactory.getLog(OpenSearchServletInternal.class);  
  private static final Map NS_MAP = new HashMap();  
  private static PwaFunctionsWritable functions = null;
  private static int nQueryMatches = 0;
  private static String collectionsHost=null;
  private static Calendar DATE_START = new GregorianCalendar(1996, 1-1, 1);
  private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");
  private static final SimpleDateFormat FORMATVIEW = new SimpleDateFormat("yyyy/MM/dd");
  SimpleDateFormat inputDateFormatter = new SimpleDateFormat("dd/MM/yyyy");
  private static Pattern URL_PATTERN = Pattern.compile("^.*? ?((https?:\\/\\/)?([a-zA-Z\\d][-\\w\\.]+)\\.([a-z\\.]{2,6})([-\\/\\w\\p{L}\\.~,;:%&=?+$#*]*)*\\/?) ?.*$");
  Calendar DATE_END = new GregorianCalendar();
  static {
    NS_MAP.put("opensearch", "http://a9.com/-/spec/opensearch/1.1/");
    NS_MAP.put("time","http://a9.com/-/opensearch/extensions/time/1.0/");
//    NS_MAP.put("nutch", "http://www.nutch.org/opensearchrss/1.0/");
    NS_MAP.put("pwa","http://arquivo.pt/opensearchrss/1.0/");
  }  

  private static final Set SKIP_DETAILS = new HashSet(); // skip these fields always
  static {
    SKIP_DETAILS.add("url");                   // redundant with RSS link
    SKIP_DETAILS.add("title");                 // redundant with RSS title
    SKIP_DETAILS.add("boost");                
    SKIP_DETAILS.add("pagerank");
    SKIP_DETAILS.add("inlinks");
    SKIP_DETAILS.add("outlinks");      
    SKIP_DETAILS.add("domain");    
  }
  
  private static final Set SKIP_DETAILS_USER = new HashSet(); // skip these fields when the request is not made by wayback
  static {
    SKIP_DETAILS_USER.add("segment");       
    SKIP_DETAILS_USER.add("date");
    SKIP_DETAILS_USER.add("encoding");    
    SKIP_DETAILS_USER.add("collection");
    SKIP_DETAILS_USER.add("arcname");
    SKIP_DETAILS_USER.add("arcoffset");        
  }
    
  private NutchBean bean;
  private Configuration conf;
  public void init(ServletConfig config) throws ServletException {
    try {
      this.conf = NutchConfiguration.get(config.getServletContext());
      bean = NutchBean.get(config.getServletContext(), this.conf);

      functions=PwaFunctionsWritable.parse(this.conf.get(Global.RANKING_FUNCTIONS));            
      nQueryMatches=Integer.parseInt(this.conf.get(Global.MAX_FULLTEXT_MATCHES_RANKED));
      
      collectionsHost = this.conf.get("wax.host", "examples.com");
    } 
    catch (IOException e) {
      throw new ServletException(e);
    }
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {

    //if (NutchBean.LOG.isInfoEnabled()) {
      LOG.debug("query request from " + request.getRemoteAddr());
    //}
      
      Calendar DATE_END = new GregorianCalendar();
      DATE_END.set( Calendar.YEAR, DATE_END.get(Calendar.YEAR) );
      DATE_END.set( Calendar.MONTH, 12-1 );
      DATE_END.set( Calendar.DAY_OF_MONTH, 31 );
      DATE_END.set( Calendar.HOUR_OF_DAY, 23 );
      DATE_END.set( Calendar.MINUTE, 59 );
      DATE_END.set( Calendar.SECOND, 59 );
      String dateEndString = FORMAT.format( DATE_END.getTime() );
      
      
      
    // get parameters from request
    request.setCharacterEncoding("UTF-8");
    
   
    String queryString = request.getParameter("query");
    
    if (queryString == null)
      queryString = "";
   
  
   
    
    
   String urlQuery = URLEncoder.encode(queryString, "UTF-8");
   urlQuery= URLEncoder.encode(queryString,"UTF-8");
    // the query language
    String queryLang = request.getParameter("lang");
    
    // first hit to display
    int start = 0;                                
    String startString = request.getParameter("start");
    if (startString != null)
      start = Integer.parseInt(startString);
    
    // number of hits to display
    int hitsPerPage = 10;                         
    String hitsString = request.getParameter("hitsPerPage");
    if (hitsString != null)
      hitsPerPage = Integer.parseInt(hitsString);

    String sort = request.getParameter("sort");
    boolean reverse =
      sort!=null && "true".equals(request.getParameter("reverse"));

    // De-Duplicate handling.  Look for duplicates field and for how many
    // duplicates per results to return. Default duplicates field is 'site'
    // and duplicates per results default is '2'.
    String dedupField = request.getParameter("dedupField");
    if (dedupField == null || dedupField.length() == 0) {
        dedupField = "site";
    }
    int hitsPerDup = 2;
    String hitsPerDupString = request.getParameter("hitsPerDup");
    if (hitsPerDupString != null && hitsPerDupString.length() > 0) {
        hitsPerDup = Integer.parseInt(hitsPerDupString);
    } else {
        // If 'hitsPerSite' present, use that value.
        String hitsPerSiteString = request.getParameter("hitsPerSite");
        if (hitsPerSiteString != null && hitsPerSiteString.length() > 0) {
            hitsPerDup = Integer.parseInt(hitsPerSiteString);
        }
    }     
    
    // date restriction   
    String dateStart = request.getParameter("dtstart");
    if (dateStart == null || dateStart.length() == 0) {
    	dateStart = null;
    }
    String dateEnd = request.getParameter("dtend");
    if (dateEnd == null || dateEnd.length() == 0) {
    	dateEnd = null; 
    }

    if(dateStart== null && dateEnd != null){
      dateStart = "1996-01-01T00:00:00Z"; /*If datestart is not specified set it to 1996*/
    }
    if(dateStart != null && dateEnd == null){
      dateEnd = "2029-12-31T00:00:00Z"; /*If dateEnd is not specified set it to 2029*/
    }

    if (dateStart!=null && dateEnd!=null) {    	    	    	
    	try {
    		Date dStart=RFC3339Date.parseRFC3339Date(dateStart);
    		Date dEnd=RFC3339Date.parseRFC3339Date(dateEnd);
    	
    		DateFormat dOutputFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    		queryString += " date:"+ dOutputFormat.format(dStart.getTime()) + "-" + dOutputFormat.format(dEnd.getTime());
    	}
    	catch (ParseException e) {
    		// ignore
    	}    	
    	catch (IndexOutOfBoundsException e) {
    		// ignore
    	}    	
    }
    
    // wayback parameters
    boolean multipleDetails = request.getParameter("multDet")!=null && request.getParameter("multDet").equals("true"); // indicates that it requests multiple details instead of one at the time
    String sId = request.getParameter("id");
    String sIndex = request.getParameter("index");       
    boolean waybackQuery = request.getParameter("waybackQuery")!=null && request.getParameter("waybackQuery").equals("true"); // indicates that is a wayback request
    
    
    // To support querying opensearch by  url
    // Lucene index format
    String queryStringOpensearchWayback=null;
    boolean isOpensearhWayback=false;
    int urlLength =queryString.length();
    boolean urlMatch = false;
    urlMatch= URL_PATTERN.matcher(queryString.toString()).matches();
    String urlQueryParam=null;
    
    if (!waybackQuery && urlMatch  && !queryString.contains("site:")) {
    	if (!queryString.startsWith("http://") && !queryString.startsWith("https://") ) {
            urlQueryParam = "http://" + queryString;
    	}
    	else
    		urlQueryParam=queryString;
    	
    	String s = "date:19960101000000-"+dateEndString+" exacturlexpand:"+urlQueryParam;
    	queryStringOpensearchWayback= request.getParameter(s);
    	isOpensearhWayback=true;
    }
    else if (queryString.contains("site:")){// if it contains site: is also a full-text search
    	hitsPerDup = 0;
    	queryString= queryString.replaceAll("site:http://", "site:");
    	queryString = queryString.replaceAll("site:https://", "site:");
    }
    // Make up query string for use later drawing the 'rss' logo.
    String params = "&hitsPerPage=" + hitsPerPage +
        (queryLang == null ? "" : "&lang=" + queryLang) +
        (sort == null ? "" : "&sort=" + sort + (reverse? "&reverse=true": "") +
        (dedupField == null ? "" : "&dedupField=" + dedupField)) +
        (multipleDetails==false ? "" : "&multDet=true") +
        (sId==null ? "" : "&id="+sId) +
        (sIndex==null ? "" : "&index="+sIndex) +
        (waybackQuery==false ? "" : "&waybackQuery=true");

    Hits hits;
    if (sId!=null && sIndex!=null) { // only want the details of this document with this id in this index
    	Hit[] oneHit=new Hit[1];
    	oneHit[0]=new Hit(Integer.parseInt(sIndex),Integer.parseInt(sId));
    	hits = new Hits(1,oneHit);
    }
    else { // search hits
    	Query query=null;
    	if (isOpensearhWayback){
    		query = Query.parse(queryStringOpensearchWayback, queryLang, this.conf);
    		LOG.debug("query: " + queryStringOpensearchWayback);	
    		sort = "relevance";
    	}
    	else{
    		query = Query.parse(queryString, queryLang, this.conf);
    		LOG.debug("query: " + queryString);
    	}
    	    	

    	// execute the query    
    	try {    		
    		if (waybackQuery) { // wayback (URL) query
    			
    			hits = bean.search(query, start + hitsPerPage, hitsPerDup, dedupField, sort, reverse, true); 
    		}
    		else { // nutchwax (full-text) query    			    			
    			int hitsPerVersion = 1;    		
    			hits = bean.search(query, start + hitsPerPage, nQueryMatches, hitsPerDup, dedupField, sort, reverse, functions, hitsPerVersion);
    			
    		}
    	} 
    	catch (IOException e) {
   			LOG.warn("Search Error", e);    	
    		hits = new Hits(0,new Hit[0]);	
    	}

   		LOG.debug("total hits: " + hits.getTotal());
    }
    
    // generate xml results
    int end = (int)Math.min(hits.getLength(), start + hitsPerPage);
    int length = end-start;

    Hit[] show = hits.getHits(start, end-start);        
    HitDetails[] details = null;
           
    if (!multipleDetails) { // normal case
    	details = bean.getDetails(show);
    }
    else { // BUG wayback 0000155 - send only the fields necessary to presentation
    	PwaRequestDetailsWritable detailsWritable=new PwaRequestDetailsWritable();
    	//detailsWritable.setFields(null);
    	detailsWritable.setFields(new String[]{"digestDiff","tstamp"});
    	detailsWritable.setHits(show);
    	details = bean.getDetails(detailsWritable);
    }
    
    String requestUrl = request.getRequestURL().toString();
    String base = requestUrl.substring(0, requestUrl.lastIndexOf('/'));
      

    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      Document doc = factory.newDocumentBuilder().newDocument();
 
      Element rss = addNode(doc, doc, "rss");
      addAttribute(doc, rss, "version", "2.0");
      addAttribute(doc, rss, "xmlns:opensearch",(String)NS_MAP.get("opensearch"));
      addAttribute(doc, rss, "xmlns:time",(String)NS_MAP.get("time"));
      addAttribute(doc, rss, "xmlns:pwa",(String)NS_MAP.get("pwa"));       
      
      /*
      addAttribute(doc, rss, "xmlns:nutch", (String)NS_MAP.get("nutch"));
      */

      Element channel = addNode(doc, rss, "channel");
    
      addNode(doc, channel, "title", "PWA Search Engine");
      addNode(doc, channel, "description", "PWA search results for query: " + queryString);
      addNode(doc, channel, "link", "http://archive.pt");
      
      /*
      addNode(doc, channel, "link",
              base+"/search.jsp"
              +"?query="+urlQuery
              +"&start="+start
              +"&hitsPerDup="+hitsPerDup
              +params);
      */
      addNode(doc, channel, "opensearch", "totalResults", ""+hits.getTotal());
      addNode(doc, channel, "opensearch", "startIndex", ""+start);
      addNode(doc, channel, "opensearch", "itemsPerPage", ""+hitsPerPage);
      Element queryElem=addNode(doc, channel, "opensearch", "Query", "");
      addAttribute(doc, queryElem, "role", "request");
      addAttribute(doc, queryElem, "searchTerms", queryString);
      addAttribute(doc, queryElem, "startPage", "1");      
    
      /*
      if ((hits.totalIsExact() && end < hits.getTotal()) // more hits to show
          || (!hits.totalIsExact() && (hits.getLength() > start+hitsPerPage))){
        addNode(doc, channel, "nutch", "nextPage", requestUrl
                +"?query="+urlQuery
                +"&start="+end
                +"&hitsPerDup="+hitsPerDup
                +params);
      }
      */

      /*
      if ((!hits.totalIsExact() && (hits.getLength() <= start+hitsPerPage))) {
        addNode(doc, channel, "nutch", "showAllHits", requestUrl
                +"?query="+urlQuery
                +"&hitsPerDup="+0
                +params);
      }
      */

      for (int i = 0; i < length; i++) {
        Hit hit = show[i];
        HitDetails detail = details[i];
        String title = detail.getValue("title");
        
        String url = detail.getValue("url");
        String arcname = detail.getValue("arcname");
        String arcoffset = detail.getValue("arcoffset");
        Element item = addNode(doc, channel, "item");
        
        if (title == null || title.equals("")) {   // use url for docs w/o title
        	title = url;
        }
        addNode(doc, item, "title", title);
                                     
        //addNode(doc, item, "description", /*summaries[i].toHtml(false)*/""); // BUG wayback 0000155 - this is unnecessary
        String date = detail.getValue("tstamp");
        Date datet= null;
        		try{
        			datet = FORMAT.parse(date);
        	        
        	    }
        	    catch ( ParseException e ){
        	    	LOG.error(e);
        	    }
        if (url!=null) {
        	// Lucene index format
        	String infoIndex = "http://"+ collectionsHost +"/id"+ hit.getIndexDocNo() +"index"+ hit.getIndexNo();
        	
        	LOG.info("Index Information " + infoIndex);
        	queryElem=addNode(doc, item, "source", "Original URL of "+title);
            addAttribute(doc, queryElem, "url", url);
            String target = "http://"+ collectionsHost +"/"+ FORMAT.format(datet).toString()  +"/"+ url;
            if(isOpensearhWayback)
            	addNode(doc, item, "title", FORMATVIEW.format(datet).toString());
            addNode(doc, item, "link", target);
        }

        /*
        addNode(doc, item, "nutch", "site", hit.getDedupValue());        
        addNode(doc, item, "nutch", "cache", base+"/cached.jsp?"+id);
        addNode(doc, item, "nutch", "explain", base+"/explain.jsp?"+id
                +"&query="+urlQuery+"&lang="+queryLang);
        */
        
        // BUG wayback 0000155 - add docId and index id to use in wayback search to see a page
        addNode(doc, item, "pwa", "id", ""+hit.getIndexDocNo());
        addNode(doc, item, "pwa", "index", ""+hit.getIndexNo());
        addNode(doc, item, "pwa", "arcname", ""+arcname);
        addNode(doc, item, "pwa", "arcoffset", ""+arcoffset	);
        /*
        if (hit.moreFromDupExcluded()) {
          addNode(doc, item, "nutch", "moreFromSite", requestUrl
                  +"?query="
                  +URLEncoder.encode("site:"+hit.getDedupValue()
                                     +" "+queryString, "UTF-8")
                  +"&hitsPerSite="+0
                  +params);
        }
        */
       
        for (int j = 0; j < detail.getLength(); j++) { // add all from detail
        	String field = detail.getField(j);
        	if ((waybackQuery && !SKIP_DETAILS.contains(field)) || 
        		  (!waybackQuery && !SKIP_DETAILS_USER.contains(field) && !SKIP_DETAILS.contains(field))) {
        		addNode(doc, item, "pwa", field, detail.getValue(j));
        	}          
        }              
      }

      // dump DOM tree

      DOMSource source = new DOMSource(doc);
      TransformerFactory transFactory = TransformerFactory.newInstance();
      Transformer transformer = transFactory.newTransformer();
      transformer.setOutputProperty("indent", "yes");
      StreamResult result = new StreamResult(response.getOutputStream());      
      response.setContentType("application/rss+xml; charset=UTF-8");      
      transformer.transform(source, result);

    } catch (javax.xml.parsers.ParserConfigurationException e) {
      throw new ServletException(e);
    } catch (javax.xml.transform.TransformerException e) {
      throw new ServletException(e);
    }
      
  }

  private static Element addNode(Document doc, Node parent, String name) {
    Element child = doc.createElement(name);
    parent.appendChild(child);
    return child;
  }

  private static Element addNode(Document doc, Node parent,
                              String name, String text) {
    Element child = doc.createElement(name);
    child.appendChild(doc.createTextNode(getLegalXml(text)));
    parent.appendChild(child);
    return child;
  }

  private static Element addNode(Document doc, Node parent,
                              String ns, String name, String text) {
    Element child = doc.createElementNS((String)NS_MAP.get(ns), ns+":"+name);
    child.appendChild(doc.createTextNode(getLegalXml(text)));
    parent.appendChild(child);
    return child;
  }

  private static void addAttribute(Document doc, Element node,
                                   String name, String value) {
    Attr attribute = doc.createAttribute(name);
    attribute.setValue(getLegalXml(value));
    node.getAttributes().setNamedItem(attribute);
  }

  /*
   * Ensure string is legal xml.
   * @param text String to verify.
   * @return Passed <code>text</code> or a new string with illegal
   * characters removed if any found in <code>text</code>.
   * @see http://www.w3.org/TR/2000/REC-xml-20001006#NT-Char
   */
  protected static String getLegalXml(final String text) {
      if (text == null) {
          return null;
      }
      StringBuffer buffer = null;
      for (int i = 0; i < text.length(); i++) {
        char c = text.charAt(i);
        if (!isLegalXml(c)) {
	  if (buffer == null) {
              // Start up a buffer.  Copy characters here from now on
              // now we've found at least one bad character in original.
	      buffer = new StringBuffer(text.length());
              buffer.append(text.substring(0, i));
          }
        } else {
           if (buffer != null) {
             buffer.append(c);
           }
        }
      }
      return (buffer != null)? buffer.toString(): text;
  }
 
  private static boolean isLegalXml(final char c) {
    return c == 0x9 || c == 0xa || c == 0xd || (c >= 0x20 && c <= 0xd7ff)
        || (c >= 0xe000 && c <= 0xfffd) || (c >= 0x10000 && c <= 0x10ffff);
  }

}
