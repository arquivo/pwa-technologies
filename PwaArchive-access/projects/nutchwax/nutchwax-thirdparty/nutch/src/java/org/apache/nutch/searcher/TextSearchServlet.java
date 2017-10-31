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
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.parsers.*;


/** Present search results using A9's OpenSearch extensions to RSS, plus a few
 * Nutch-specific extensions. */   
public class TextSearchServlet extends HttpServlet {
	 	
  /**
	 * 
  */
  private static final long serialVersionUID = 1L;
  private static final Log LOG = LogFactory.getLog(TextSearchServlet.class);  
  private static final Map NS_MAP = new HashMap( ); 
  private static PwaFunctionsWritable functions = null;
  private static int nQueryMatches = 0;
  private static String collectionsHost=null;
  private static Calendar DATE_START = new GregorianCalendar(1996, 1-1, 1);
  private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");
  private static final SimpleDateFormat FORMATVIEW = new SimpleDateFormat("yyyy/MM/dd");
  SimpleDateFormat inputDateFormatter = new SimpleDateFormat("dd/MM/yyyy");
  private static Pattern URL_PATTERN = Pattern.compile("^.*? ?((https?:\\/\\/)?([a-zA-Z\\d][-\\w\\.]+)\\.([a-z\\.]{2,6})([-\\/\\w\\p{L}\\.~,;:%&=?+$#*]*)*\\/?) ?.*$");
  Calendar DATE_END = new GregorianCalendar();
  private static final String noFrame = "/noFrame/replay";
  private static final String screenShotURL = "/screenshot/?url";
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

	  LOG.info("[doGet] query request from " + request.getRemoteAddr( ) );
	  Calendar DATE_END = currentDate( );
	  String dateEndString = FORMAT.format( DATE_END.getTime( ) );
	  int start = 0;
	  int limit = 10;
	  
	  // get parameters from request
	  request.setCharacterEncoding("UTF-8");
	  StringBuilder queryString = new StringBuilder( );
	  String q = request.getParameter("q");
	  
	  if ( q == null )
		  q = "";
	  queryString.append( q );
	  
	  LOG.info( "[doGet] q=" + q );
	  String urlQuery = URLEncoder.encode( q , "UTF-8" );
	  urlQuery = URLEncoder.encode( q , "UTF-8" );
	  
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
	  String sortParameter = request.getParameter("sort"); //relevance or new or old
	  String sort;
	  boolean reverse = false;
	  if ( "relevance".equals( sortParameter ) ) {
		  sort = null;
	  } else if ( "new".equals( sortParameter ) ) {
		  sort = "date";
		  reverse = true;
	  } else if ( "old".equals( sortParameter ) ) {
		  sort = "date";
	  } else {
		  sort = null;
		  sortParameter = "relevance";
	  }
	 
	  
      // De-Duplicate handling.  Look for duplicates field and for how many
      // duplicates per results to return. Default duplicates field is 'site'
      // and duplicates per results default is '2'.
      String dedupField = "site";
      
      
      //If 'hitsPerSite' present, use that value.
      String hitsPerSiteString = request.getParameter( "limitPerSite" );
      if ( hitsPerSiteString != null && hitsPerSiteString.length( ) > 0 ) 
    	  hitsPerDup = Integer.parseInt( hitsPerSiteString );
      
      // date restriction   
      String dateStart = request.getParameter( "from" );
      if ( dateStart == null || dateStart.length( ) == 0 ) {
    	  dateStart = null;
      }
      String dateEnd = request.getParameter( "to" );
      if ( dateEnd == null || dateEnd.length( ) == 0 ) {
    	  dateEnd = null; 
      }
      
      LOG.info( "[doGet] dtstart["+dateStart+"] dtend["+dateEnd+"]" );
      
      if(dateStart== null && dateEnd != null){
    	  dateStart = "1996-01-01T00:00:00Z"; /*If datestart is not specified set it to 1996*/
      }
      if(dateStart != null && dateEnd == null){
    	  dateEnd = "2029-12-31T00:00:00Z"; /*If dateEnd is not specified set it to 2029*/
      }

      if (dateStart!=null && dateEnd!=null) {    	    	    	
    	  try {
    		  Date dStart = RFC3339Date.parseRFC3339Date( dateStart );
    		  Date dEnd = RFC3339Date.parseRFC3339Date( dateEnd );
    	
    		  DateFormat dOutputFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    		  queryString.append( " date:".concat( dOutputFormat.format(dStart.getTime( ) ) ).concat( "-" ).concat( dOutputFormat.format( dEnd.getTime( ) ) ) );
    	  } catch ( ParseException e ) {
    		  // ignore
    	  } catch ( IndexOutOfBoundsException e ) {
    		  // ignore
    	  }    	
      }
    
      // To support querying opensearch by  url
      // Lucene index format
      String queryStringOpensearchWayback=null;
      boolean isOpensearhWayback=false;
      String urlQueryParam=null;
      
      String siteParameter = request.getParameter( "site" );
      if( siteParameter == null )
    	  siteParameter = "";
      if ( !siteParameter.equals( "" ) ){// if it contains site: is also a full-text search
    	  hitsPerDup = 0;
    	  String site = siteParameter;
    	  site = " site:".concat( siteParameter );
    	  site = site.replaceAll( "site:http://" , "site:" );
    	  site = site.replaceAll( "site:https://" , "site:" );
    	  
    	  queryString.append( site );
      }
   
      String typeParameter = request.getParameter( "type" );
      if( typeParameter == null )
    	  typeParameter = "";
      if( !typeParameter.equals( "" ) ){
    	  String type = " type:".concat( typeParameter );
    	  queryString.append( type );
      }
      String prettyPrintParameter = request.getParameter( "prettyPrint" );
      boolean prettyOutput = false;
      if( prettyPrintParameter != null && prettyPrintParameter.equals( "true" ) ) 
    	  prettyOutput = true;
    	
      
      // Make up query strintargetg for use later drawing the 'rss' logo.
      String params = "&hitsPerPage=" + limit +
    		  (queryLang == null ? "" : "&lang=" + queryLang) +
    		  (sort == null ? "" : "&sort=" + sort + (reverse? "&reverse=true": "") +
    				  (dedupField == null ? "" : "&dedupField=" + dedupField));

      Hits hits;

      Query query = null;
      String qRequest = queryString.toString( );
	  query = Query.parse( qRequest , queryLang , this.conf );
	  LOG.info( "query: " + qRequest );

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
		  
		  TextSearchResponse responseObject = new TextSearchResponse( );
		  TextSearchRequestParameters requestParameters = new TextSearchRequestParameters( );
		  
		  responseObject.setServiceName( ( String ) NS_MAP.get( "serviceName" ) );
		  responseObject.setLinkToService( ( String ) NS_MAP.get( "link" ) );
		  
		  requestParameters.setLimit( limit );
		  requestParameters.setLimitPerSite( hitsPerDup );
		  requestParameters.setStart( start );
		  requestParameters.setFrom( dateStart );
		  requestParameters.setTo( dateEnd );
		  requestParameters.setType( typeParameter );
		  requestParameters.setPrettyPrint( prettyPrintParameter );
		  requestParameters.setSite( siteParameter );
		  
		  if( sortParameter != null && !"".equals( sortParameter ) )
			  requestParameters.setSort( sortParameter );
		  if( q != null && !"".equals( q ) )
			  requestParameters.setQueryTerms( q );
		  
		  if( requestParameters != null )
			  responseObject.setRequestParameters( requestParameters );
		  
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
                	String infoIndex = "http://" + collectionsHost + "/id" + hit.getIndexDocNo( ) + "index" + hit.getIndexNo( );
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
                String encoding = detail.getValue( "encoding" );
                item.setEncoding( encoding );
                
                //http://arquivo.pt/noFrame/replay/19980205082901/http://www.caleida.pt/saramago/
                if( url != null ) {
                	//TODO test collectionsHost
                	String urlNoFrame = "http://".concat( "arquivo.pt" ).concat( noFrame ).concat( "/" ).concat( FORMAT.format( datet ).toString( ) ).concat( "/" ).concat( url );
                    LOG.info( "[TextSearchServlet][doGet] urlNoFrame =" + urlNoFrame );
                	String urlEncode = URLEncoder.encode( urlNoFrame , "UTF-8" );
                	LOG.info( "[TextSearchServlet][doGet] urlEncode =" + urlEncode );
                	String screenShotLink = "http://".concat( "arquivo.pt" ).concat( screenShotURL ).concat( "=" ).concat( urlEncode );
                    item.setScreenShotLink( screenShotLink );
                }
                
                String itemText = "NOT IMPLEMENTED"; //TODO not implemented
                item.setItemText( itemText );
                
                String detailsCheck = request.getParameter( "details" );
                
                if( detailsCheck != null && detailsCheck.equals( "true" ) ) {
                	int idDoc = hit.getIndexDocNo( );
                	int index = hit.getIndexNo( );
                	String arcname 		= detail.getValue( "arcname" );
                    String arcoffset 	= detail.getValue( "arcoffset" );
                    String segment 		= detail.getValue( "segment" );
                    String collection 	= detail.getValue( "collection" );
                	item.setIdDoc( String.valueOf( idDoc ) );
                	item.setIndex( String.valueOf( index ) );
                	if( arcname != null )
                		item.setArcname( arcname );
                	else
                		item.setArcname( "" );
                	if( arcoffset != null )
                		item.setArcoffset( arcoffset );
                	else 
                		item.setArcoffset( "" );
                	if( segment != null )
                		item.setSegment( segment );
                	else
                		item.setSegment( "" );
                	if( collection != null )
                		item.setCollection( collection );
                	else 
                		item.setCollection( "" );
                }
                
                if( item != null ) {
                	itens.add( item );
                }
          }
		  
		  if( itens != null && itens.size( ) > 0 )
			  responseObject.setItens( itens );
		  else
			  responseObject.setItens( new ArrayList< Item >( ) );
		  
		  String jsonObject;
		  if( prettyOutput )
			  jsonObject = toPrettyFormat( responseObject );
		  else {
			  Gson gson = new Gson( );
			  jsonObject = gson.toJson( responseObject );
		  }
		  
		  response.setContentType( "application/json" );
		  // Get the printwriter object from response to write the required json object to the output stream      
		  PrintWriter out = response.getWriter( );  
		  out.print( jsonObject );
		  out.flush( );


	  } catch ( JsonParseException e ) {
		  throw new ServletException( e );
	  }
	  
  }

  
  /**
   * Returns the current date in the format (YYYYMMDDHHMMSS)
   * @return
   */
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
  
  /**
   * Convert a JSON string to pretty print version
   * @param jsonString
   * @return
   */
  private static String toPrettyFormat( TextSearchResponse responseObject ) {
	  Gson gson = new GsonBuilder( ).setPrettyPrinting( ).create( );
      String prettyJson = gson.toJson( responseObject );
      
      return prettyJson;
  }
  

}
