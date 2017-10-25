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
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
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

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.parsers.*;


/** Present search results using A9's OpenSearch extensions to RSS, plus a few
 * Nutch-specific extensions. */   
public class OpenSearchServlet extends HttpServlet {
	 	
  /**
	 * 
  */
  private static final long serialVersionUID = 1L;
  private static final Log LOG = LogFactory.getLog(OpenSearchServlet.class);  
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
    NS_MAP.put("serviceName", "Arquivo.pt - the Portuguese web-archive");
    NS_MAP.put("link","http://arquivo.pt");
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

	  LOG.info("[OpenSearchServlet][doGet] query request from " + request.getRemoteAddr());
	  Calendar DATE_END = currentDate();
	  String dateEndString = FORMAT.format( DATE_END.getTime() );
	  int start = 0;
	  int limit = 10;
	  
	  // get parameters from request
	  request.setCharacterEncoding("UTF-8");
	  String queryString = request.getParameter("query");
    
	  if (queryString == null)
		  queryString = "";
	  LOG.info( "[OpenSearchServlet][doGet] queryString=" + queryString );
	  String urlQuery = URLEncoder.encode( queryString , "UTF-8" );
	  urlQuery = URLEncoder.encode( queryString , "UTF-8" );
	  
	  // the query language
	  String queryLang = request.getParameter( "lang" );
    
	  // first hit to display
	  String startString = request.getParameter( "start" );
	  if ( startString != null )
		  start = Integer.parseInt( startString );
    
	  // number of items to display
	  String limitString = request.getParameter( "limit" );
	  if ( limitString != null )
		  limit = Integer.parseInt(limitString);
	  
	  int hitsPerDup = 2;
	  /*****************    'sort' param    ***************************/
	  String sort = request.getParameter("sort"); //relevance or new or old
	  boolean reverse = false;
	  if ( "relevance".equals( sort ) ) {
		  sort = null;
	  } else if ( "new".equals( sort ) ) {
		  sort = "date";
		  reverse = true;
	  } else if ( "old".equals( sort ) ) {
		  sort = "date";
	  } else 
		  sort = null;
	 
	  
      // De-Duplicate handling.  Look for duplicates field and for how many
      // duplicates per results to return. Default duplicates field is 'site'
      // and duplicates per results default is '2'.
      String dedupField = "site";
      
    
      //If 'hitsPerSite' present, use that value.
      String hitsPerSiteString = request.getParameter( "limitPerSite" );
      if ( hitsPerSiteString != null && hitsPerSiteString.length( ) > 0 ) 
    	  hitsPerDup = Integer.parseInt( hitsPerSiteString );
      
      // date restriction   
      String dateStart = request.getParameter("dtstart");
      if ( dateStart == null || dateStart.length( ) == 0 ) {
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
    
    
    
      // To support querying opensearch by  url
      // Lucene index format
      String queryStringOpensearchWayback=null;
      boolean isOpensearhWayback=false;
      int urlLength =queryString.length();
      boolean urlMatch = false;
      urlMatch= URL_PATTERN.matcher(queryString.toString()).matches();
      String urlQueryParam=null;
    
      if (queryString.contains("site:")){// if it contains site: is also a full-text search
    	  hitsPerDup = 0;
    	  queryString= queryString.replaceAll("site:http://", "site:");
    	  queryString = queryString.replaceAll("site:https://", "site:");
      }
   
      // Make up query strintargetg for use later drawing the 'rss' logo.
      String params = "&hitsPerPage=" + limit +
    		  (queryLang == null ? "" : "&lang=" + queryLang) +
    		  (sort == null ? "" : "&sort=" + sort + (reverse? "&reverse=true": "") +
    				  (dedupField == null ? "" : "&dedupField=" + dedupField));

      Hits hits;

      Query query=null;
 
	  query = Query.parse(queryString, queryLang, this.conf);
	  LOG.debug("query: " + queryString);
  
	    	

	  //execute the query    
	  try {    		
		  int hitsPerVersion = 1;    		
		  hits = bean.search(query, start + limit, nQueryMatches, hitsPerDup, dedupField, sort, reverse, functions, hitsPerVersion);
	  } 
	  catch (IOException e) {
		  LOG.warn("Search Error", e);    	
		  hits = new Hits( 0 ,new Hit[ 0 ] );	
	  }

	  LOG.info( "total hits: " + hits.getTotal( ) );

    
	  // generate json results
	  int end = ( int )Math.min( hits.getLength( ), start + limit );
	  int length = end-start;

	  Hit[ ] show = hits.getHits( start , end-start );        
	  HitDetails[ ] details = null;
           
	  details = bean.getDetails( show ); //get details
	  	
	  String requestUrl = request.getRequestURL( ).toString( );
	  String base = requestUrl.substring( 0 , requestUrl.lastIndexOf( '/' ) );
      

	  try {
		  
		  List< Item > itens = new ArrayList< Item >( );
		  
		  OpenSearchResponse responseObject = new OpenSearchResponse( );
		  
		  responseObject.setServiceName( ( String ) NS_MAP.get( "serviceName" ) );
		  responseObject.setLinkToService( ( String ) NS_MAP.get( "link" ) );
		  responseObject.setLimit( limit );
		  responseObject.setLimitPerSite( hitsPerDup );
		  responseObject.setStart( start );
		  responseObject.setSort( sort );
		  Item item = null;
		  for ( int i = 0 ; i < length ; i++ ) {
			  	Hit hit = show[ i ];
		        HitDetails detail = details[ i ];
		        item = new Item( );
		        String title = detail.getValue( "title" );
		        String url 	= detail.getValue( "url" );
		        String date = detail.getValue( "tstamp" );
		        item.setTitle( title );
		        item.setSource( url );
		        
		        Date datet = null;
        		try{
        			datet = FORMAT.parse( date );
        			item.setTstamp( FORMAT.format( datet ).toString( ) );
        	    }catch ( ParseException e ){
        	    	LOG.error( e );
        	    }
                if ( url != null ) {
                	// Lucene index format
                	String infoIndex = "htJsonParseExceptiontp://" + collectionsHost + "/id" + hit.getIndexDocNo( ) + "index" + hit.getIndexNo( );
                	LOG.info( "Index Information " + infoIndex );
                	String target = "http://"+ collectionsHost +"/"+ FORMAT.format(datet).toString()  +"/"+ url;
                    item.setLink( target );
                }
                /**** TODO: I stayed here ****/
                String contentLength = detail.getValue( "contentLength" );
                item.setContentLength( contentLength );
                String digest = detail.getValue( "digest" );
                item.setDigest( digest );
                String dateEpoch = detail.getValue( "date" );
                item.setDate( dateEpoch );
                String primaryType = detail.getValue( "primaryType" );
                item.setPrimaryType( primaryType );
                String subType = detail.getValue( "subType" );
                item.setSubType( subType );
                String screenShotLink = url; //TODO not implemented
                item.setScreenShotLink( screenShotLink );
                String itemText = "TESTE1"; //TODO not implemented
                item.setItemText( itemText );
                
                if( item != null ) {
                	itens.add( item );
                }
                
		  }
		  
		  if( itens != null && itens.size( ) > 0 )
			  responseObject.setItens( itens );
		  else
			  responseObject.setItens( new ArrayList< Item >( ) );
		  
		  String jsonObject = new Gson( ).toJson( responseObject );
    	
		  response.setContentType( "application/json" );
		  // Get the printwriter object from response to write the required json object to the output stream      
		  PrintWriter out = response.getWriter( );
		  // Assuming your json object is **jsonObject**, perform the following, it will return your json object  
		  out.print( jsonObject );
		  out.flush( );
    

	  } catch ( JsonParseException e ) {
		  throw new ServletException( e );
	  }
	  
  }

  private static Calendar currentDate( ) {
      Calendar DATE_END = new GregorianCalendar();
      DATE_END.set( Calendar.YEAR, DATE_END.get(Calendar.YEAR) );
      DATE_END.set( Calendar.MONTH, 12-1 );
      DATE_END.set( Calendar.DAY_OF_MONTH, 31 );
      DATE_END.set( Calendar.HOUR_OF_DAY, 23 );
      DATE_END.set( Calendar.MINUTE, 59 );
      DATE_END.set( Calendar.SECOND, 59 );
      return DATE_END;
  }

}
