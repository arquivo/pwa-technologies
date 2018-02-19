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
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;
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
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapred.Reporter;
import org.apache.nutch.global.Global;
import org.apache.nutch.html.Entities;
import org.apache.nutch.parse.Parse;
import org.apache.nutch.parse.ParseText;
import org.apache.nutch.searcher.HitContent;
import org.apache.nutch.searcher.Item;
import org.apache.nutch.searcher.Summary.Fragment;
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


/**
 * TextSearch API Back-End - Full Search & URL Search. 
 * Servlet responsible for returning a json object with the results of the query received in the parameter.
 * 
 * @author jnobre
 * @version 1.0
 */
public class TextSearchServlet extends HttpServlet {
  /**
   * Class responsible for:
   * 	Search the indexes Lucene, through the calls to the queryServers.
   *	Search by URL in CDX indexes, through the CDXServer API.
   *
   * Documentation: https://github.com/arquivo/pwa-technologies/wiki/APIs - Full-text search: TestSearch based Arquivo.pt API
   * (The code indentation isn't adequate. Consequence of the NutchWax structure)
   */
	
	
  private static final long serialVersionUID = 1L;
  private static final Log LOG = LogFactory.getLog( TextSearchServlet.class );  
  private static final Map NS_MAP = new HashMap( ); 
  private static PwaFunctionsWritable functions = null;
  private static int nQueryMatches = 0;
  private static String collectionsHost = null;
  private static final SimpleDateFormat FORMAT = new SimpleDateFormat( "yyyyMMddHHmmss" );
  Calendar DATE_END = new GregorianCalendar( );
  private static final String noFrame = "/noFrame/replay";
  private static final String screenShotURL = "/screenshot/?url";
  private static final String textExtracted = "/textextracted?m";
  private NutchBean bean;
  private Configuration conf;
  
  static {
    NS_MAP.put( "serviceName" , "Arquivo.pt - the Portuguese web-archive" );
    NS_MAP.put( "link" , "http://arquivo.pt" ); 
  }  
  
  private static String[ ]  fieldsReponse = {"versionId", "title", "originalURL", "linkToArchive",
		  "tstamp", "contentLength", "digest", "mimeType", "linkToScreenshot",
		  "date", "encoding", "linkToNoFrame", "collection", "snippet", "extractedText"}; //input parameters
  
  /**
   * HttpServlet init method.
   * @param config: nutchwax configuration
   * @return void
   */
  public void init( ServletConfig config ) throws ServletException {
    try {
      this.conf = NutchConfiguration.get( config.getServletContext( ) );
      bean = NutchBean.get( config.getServletContext( ), this.conf );
      
      
      functions = PwaFunctionsWritable.parse( this.conf.get( Global.RANKING_FUNCTIONS ) );            
      nQueryMatches=Integer.parseInt( this.conf.get( Global.MAX_FULLTEXT_MATCHES_RANKED ) );
      
      collectionsHost = this.conf.get( "wax.host", "examples.com" );
	  TimeZone zone = TimeZone.getTimeZone( "GMT" );
	  FORMAT.setTimeZone( zone );
	  
    } catch ( IOException e ) {
      throw new ServletException( e );
    }
  }
  
  
  /**
   * HttpServlet doGet method
   * @param request - type HttpServletRequest
   * @param response - type HttpServletResponse
   * 
   */
  public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {

	  LOG.info("[doGet] query request from " + request.getRemoteAddr( ) );
	  Calendar DATE_END = currentDate( );
	  String dateEndString = FORMAT.format( DATE_END.getTime( ) );
	  int start = 0;
	  int limit = 50;
	  Hits hits;
	  LuceneParser luceneProcessor = new LuceneParser( );
	  MessageDigest md = null;
	  String urlQuery 		= "";
	  String queryLang 		= "";
	  String versionHistory = "";
	  String metadataParam 	= "";
	  String qURL 			= "";
	  String qExactURL 		= "";
	  String[ ] urlParams 	= null;
	  int hitsPerDup = 2;
	  long startTime;
	  long endTime;
	  long duration;
	  boolean metadataQuery = false;
	  boolean versionHistoryQuery = false;
	  boolean fulltextQuery = false;
	  
	  // get parameters from request
	  request.setCharacterEncoding("UTF-8");
	  StringBuilder queryString = new StringBuilder( );
	  String q = request.getParameter("q");
	  
	  try {
		  md = MessageDigest.getInstance( "MD5" );
	  } catch ( NoSuchAlgorithmException e ) {
		  LOG.error( "Failed to get md5 digester: " + e.getMessage( ) );
	  }
	  
	  if ( q == null )
		  q = "";
	  queryString.append( q );
	  
	  urlQuery = URLEncoder.encode( q , "UTF-8" ); //Encode query  in utf8
	  
	  queryLang 	 = request.getParameter( "lang" ); // the query language
	  versionHistory = request.getParameter( "versionHistory" ); // get versiobnHistory parameter
	  metadataParam  = request.getParameter( "metadata" ); //get metadata parameter
	  
	  if( isDefined( versionHistory ) ) {
		 urlParams = versionHistory.split( " " );
		 qURL = urlParams[ 0 ];
	  }
	  
	  // first hit to display
	  String startString = request.getParameter( "offset" );
	  if ( startString != null )
		  start = parseToIntWithDefault( startString , 0 );
	  
	  // number of items to display
	  String limitString = request.getParameter( "maxItems" );
	  if ( limitString != null )
		  limit = parseToIntWithDefault( limitString , 50 );
	  
	  if( limit < 0 )
		  limit = 0;
	  
	  if( limit > 2000 )
		  limit = 2000;
	  
	  String limitPerSiteS = request.getParameter( "itemsPerSite" );
	  if( limitPerSiteS != null )
		  hitsPerDup = parseToIntWithDefault( limitPerSiteS , 2 );
	  
	  //Define 'sort' param 
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
	 
	  // De-Duplicate handling. Look for duplicates field and for how many
      // duplicates per results to return. Default duplicates field is 'site'
      // and duplicates per results default is '2'.
      String dedupField = "site";
      
      //If 'hitsPerSite' present, use that value.
      String hitsPerSiteString = request.getParameter( "itemsPerSite" );
      if ( hitsPerSiteString != null && hitsPerSiteString.length( ) > 0 )
    	  hitsPerDup = parseToIntWithDefault( hitsPerSiteString , 2 );
      
      // date restriction   
      String dateStart = request.getParameter( "from" );
      if ( dateStart == null || dateStart.length( ) == 0 ) {
    	  dateStart = null;
      }
      String dateEnd = request.getParameter( "to" );
      if ( dateEnd == null || dateEnd.length( ) == 0 ) {
    	  dateEnd = null; 
      }
      
      //Set the timestamp based on the input parameters from and to
      if( dateStart == null && dateEnd != null ){
    	  dateStart = "19960101000000"; /*If datestart is not specified set it to 1996*/
      }
      if( dateStart != null && dateEnd == null ){
    	  Calendar dateEND = currentDate( );
    	  dateEnd = FORMAT.format( dateEND.getTime( ) );
      }
      
      if (dateStart!=null && dateEnd!=null) { //Logic to accept pages with yyyy and yyyyMMddHHmmss format 

    	  try {
    		  DateFormat dOutputFormatTimestamp = new SimpleDateFormat("yyyyMMddHHmmss");
    		  dOutputFormatTimestamp.setLenient( false );
    		  DateFormat dOutputFormatYear = new SimpleDateFormat("yyyy");
    		  dOutputFormatYear.setLenient( false );
    		  String dateFinal = "";
    		  if( tryParse( dOutputFormatTimestamp , dateStart )  ) {
    			  Date dStart = dOutputFormatTimestamp.parse( dateStart );
    			  dateStart = dOutputFormatTimestamp.format( dStart.getTime( ) );
    		  } else if( tryParse( dOutputFormatYear , dateStart )  ) {
    			  String extensionStart = "0101000000"; 
    			  dateStart = dateStart.concat( extensionStart );
    			  if( tryParse( dOutputFormatTimestamp , dateStart )  ) {
    				  Date dStart = dOutputFormatTimestamp.parse( dateStart );
    				  dateStart = dOutputFormatTimestamp.format( dStart.getTime( ) );
    			  }
    		  } else {
    			  dateStart="19960101000000";
    		  }
    			  
    		  if( tryParse( dOutputFormatTimestamp , dateEnd ) ) {
    			  Date dEnd = dOutputFormatTimestamp.parse( dateEnd );
    			  dateEnd = dOutputFormatTimestamp.format( dEnd.getTime( ) );
    		  } else if( tryParse( dOutputFormatYear , dateEnd ) ) {
    			  String extensionEnd = "1231235959";
    			  dateEnd = dateEnd.concat( extensionEnd );
    			  if( tryParse( dOutputFormatTimestamp , dateEnd )  ) {
    				  Date dEnd = dOutputFormatTimestamp.parse( dateEnd );
    				  dateEnd = dOutputFormatTimestamp.format( dEnd.getTime( ) );
    			  }
    		  } else {
    			  Calendar dateEND = currentDate( );
    	    	  dateEnd = FORMAT.format( dateEND.getTime( ) );
    		  }
    		   
    		  if( dateStart == null && dateEnd != null ) {
    			  dateStart = "19960101000000";
    		  }
    		  
    		  if( dateStart != null && dateEnd == null ) {
    			  Calendar dateEND = currentDate( );
    			  dateEnd = FORMAT.format( dateEND.getTime( ) );
    		  }
    		  
    		  if( dateStart != null && dateEnd != null ) {
    			  dateFinal = " date:".concat( dateStart ).concat( "-" ).concat( dateEnd );
    			  queryString.append( dateFinal );
    		  }
    		  
    	  } catch ( ParseException e ) {
    		  // ignore
    		  LOG.error( "Parse Exception: " , e );
    	  } catch ( IndexOutOfBoundsException e ) {
    		  // ignore
    		  LOG.error( "Parse Exception: " , e );
    	  }    	
      }
      
      //Full-text search on specified web site only
      String siteParameter = request.getParameter( "siteSearch" );
      if( siteParameter == null )
    	  siteParameter = "";
      if ( !siteParameter.equals( "" ) ){ // if it contains site: is also a full-text search
    	  hitsPerDup = 0;
    	  String site = siteParameter;
    	  site = " site:".concat( siteParameter );
    	  site = site.replaceAll( "site:http://" , "site:" );
    	  site = site.replaceAll( "site:https://" , "site:" );
    	  queryString.append( site );
      }
      
      //Full-text search on specified type documents
      String typeParameter = request.getParameter( "type" );
      if( typeParameter == null )
    	  typeParameter = "";
      if( !typeParameter.equals( "" ) ){
    	  String type = " type:".concat( typeParameter );
    	  queryString.append( type );
      }
      
      //Pretty print in output message 
      String prettyPrintParameter = request.getParameter( "prettyPrint" );
      boolean prettyOutput = false;
      if( prettyPrintParameter != null && prettyPrintParameter.equals( "true" ) ) 
    	  prettyOutput = true;
      
      //fields parameter
      String fieldsParam = "";
	  if( request.getParameter( "fields" ) != null )
		  fieldsParam = request.getParameter( "fields" );
	  String[ ] fields = null;
	  if( !fieldsParam.equals( "" ) )
		  fields = fieldsParam.split( "," );
	  
	  List< Item > itens = new ArrayList< Item >( );
	  TextSearchResponse responseObject = new TextSearchResponse( );
	  TextSearchRequestParameters requestParameters = new TextSearchRequestParameters( );
	  
	  //Set Service Name and link to service in response 
	  responseObject.setServiceName( ( String ) NS_MAP.get( "serviceName" ) );
	  responseObject.setLinkToService( ( String ) NS_MAP.get( "link" ) );

	  
	  if( limit == 0 ) { //TODO review code 
		  
		  //Set input parameters to send in response
		  if( limitString != null && !limitString.equals( "" ) )
			  requestParameters.setLimit( limitString );
		  if( limitPerSiteS != null && !limitPerSiteS.equals( "" ) )
			  requestParameters.setLimitPerSite( limitPerSiteS );
		  if( startString != null && !startString.equals( "" ) )
			  requestParameters.setStart( startString );
		  requestParameters.setFrom( dateStart );
		  requestParameters.setTo( dateEnd );
		  if( !typeParameter.equals( "" ) )
			  requestParameters.setType( typeParameter );
		  requestParameters.setPrettyPrint( prettyPrintParameter );
		  if( !siteParameter.equals( "" ) )
			  requestParameters.setSite( siteParameter );
		  
		  if( q != null && !"".equals( q ) )
			  requestParameters.setQueryTerms( q );
		  
		  if( requestParameters != null )
			  responseObject.setRequestParameters( requestParameters );
		  
		  // generate json results
		  if( itens != null && itens.size( ) > 0 )
			  responseObject.setItens( itens );
		  else
			  responseObject.setItens( new ArrayList< Item >( ) );
		  
		  String jsonObject;
		  if( prettyOutput )
			  jsonObject = toPrettyFormat( responseObject );
		  else {
			  Gson gson = new GsonBuilder( ).disableHtmlEscaping( ).create( );
			  jsonObject = gson.toJson( responseObject );
		  }
		  
		  String callBackJavaScripMethodName = "";
		  if( request.getParameter( "callback" ) != null && !request.getParameter( "callback" ).equals( "" ) ) {
			  callBackJavaScripMethodName = request.getParameter( "callback" );
			  jsonObject = callBackJavaScripMethodName + "("+ jsonObject + ");";  
		  }
		  
		  if( !callBackJavaScripMethodName.equals( "" ) )
			  response.setContentType( "text/javascript" ); //jsonp
		  else
			  response.setContentType( "application/json" ); //json
		  
		  // Get the printwriter object from response to write the required json object to the output stream      
		  PrintWriter out = response.getWriter( );  
		  out.print( jsonObject );
		  out.flush( );
		  return;
	  }
	  
      String totalItems = "";
      
      if( metadataParam != null && !metadataParam.equals( "" ) ) { //metadata query
    	  LOG.info( "Metadata query" );
    	  String rversionId = metadataParam.split( " " )[ 0 ];
    	  int indx = rversionId.lastIndexOf( "/" );
    	  
    	  if( indx > 0 ) {
        	  String[ ] versionIdsplited = { rversionId.substring( 0, indx ), rversionId.substring( indx + 1) };
        	  if( metadataValidator( versionIdsplited ) ) { //valid URL
        		 LOG.debug( "Version ["+metadataParam+"] is correct" );
        		 if( urlValidator( versionIdsplited[ 0 ] ) ) { //valid URL
        			 dateStart = "19960101000000";
        			 Calendar dateEND = currentDate( );
        			 dateEnd = FORMAT.format( dateEND.getTime( ) );
        			
           		  	 String[ ] urlEncoded = metadataParam.split( " " );
    				 
        			 StringBuilder sbExactURL = new StringBuilder( ); //build extacurl info
           		  	 for( int i = 1 ; i < urlEncoded.length ; i++ ) {
           		  		 sbExactURL.append( urlEncoded[ i ].concat( " " ) );
           		  	 }
           		  	 qExactURL = sbExactURL.toString( );
           		  	 LOG.debug(" [Metadata] qExactURL["+qExactURL+"]");
           		  	 
           		  	 hitsPerDup = 1;
           		  	 CdxParser cdxProcessor = new CdxParser( collectionsHost );
           		  	 //get cdx index fields
           		  	 List< ItemCDX > resultsCDX = cdxProcessor.getResults( versionIdsplited[ 0 ], dateStart, dateEnd, -1, 0 );
           		  	 if( resultsCDX != null )
           		  		 totalItems = String.valueOf( resultsCDX.size( ) );
           		  	 else
           		  		 totalItems = "0";
           		  	 
           		  	 List< ItemCDX > mresultsCDX = selectCDXItem( resultsCDX , versionIdsplited[ 1 ] );
           		  	 
           		  	 if( mresultsCDX != null ) {
           		  		 itens = getResponseValues( mresultsCDX , fields , qExactURL, queryLang, start, limit, hitsPerDup, dedupField, sort, reverse, true );
           		  	 } else 
           		  		 itens = new ArrayList< Item >( );
           	  	}
        		 
        	  } else {
        		  LOG.warn( "VersionID ["+metadataParam+"] NOT correct" );
        		  itens = new ArrayList< Item >( );
        	  }
    		  
    	  } else {
    		  LOG.warn( "VersionID ["+metadataParam+"] NOT correct" );
    		  itens = new ArrayList< Item >( );
    	  }
    	  
    	  metadataQuery = true;
    		  
      } else if( isDefined( qURL ) ) { //URL query
    	  LOG.info( "URL query" );
    	  
    	  if( urlValidator( qURL ) ) { //valid URL
    		  if( dateStart == null && dateEnd == null ) { //if date is null
    			  dateStart = "19960101000000";
    			  Calendar dateEND = currentDate( );
    			  dateEnd = FORMAT.format( dateEND.getTime( ) );
    		  }
    		  StringBuilder sbExactURL = new StringBuilder( ); //build extacurl info
    		  for( int i = 1 ; i < urlParams.length ; i++ ) {
    			  sbExactURL.append( urlParams[ i ].concat( " " ) );
    		  }
    		  qExactURL = sbExactURL.toString( );
    		  hitsPerDup = 1;
    		  
    		  startTime = System.nanoTime( );
    		  CdxParser cdxProcessor = new CdxParser( collectionsHost );
    		  //get cdx index fields
    		  List< ItemCDX > resultsCDX = cdxProcessor.getResults( qURL, dateStart, dateEnd, limit, start );
    		  if( resultsCDX != null ) {
    			  int sizeItems = cdxProcessor.getTotal( qURL, dateStart, dateEnd, limit, start );
    			  totalItems = String.valueOf( sizeItems );
    		  }
    		  else 
    			  totalItems = "0";
    		  endTime = System.nanoTime( );
    		  duration = ( endTime - startTime );
    		  
    		  LOG.info( "[URL-Query] CDX API Response Time: " + duration + " milliseconds");
    		  startTime = System.nanoTime( );
    		  
    		  itens = getResponseValues( resultsCDX , fields , qExactURL, queryLang, start, limit, hitsPerDup, dedupField, sort, reverse, false );
    		  
    		  endTime = System.nanoTime( );
    		  duration = ( endTime - startTime );
    		  LOG.info( "[URL-Query] Match between CDX results and Lucene index information Response Time: " + duration + " milliseconds" );
    		  
    	  }
    	  versionHistoryQuery = true;
    	  
      } else { //full-text query
    	  LOG.info( "Full-text Query" );
    	  /** Lucene index fields **/
    	  Query query = null;
    	  HitDetails[ ] details = null;
    	  Hit[ ] show = null; 
    	  Summary[ ] summaries = null;
    	  byte[ ][ ] contents = null;
    	  ParseText[ ] parseTexts = null;
    	  
    	  String qRequest = queryString.toString( );
    	  query = Query.parse( qRequest , queryLang , this.conf );
    	  
    	  startTime = System.nanoTime( );
    	  //execute the query    
    	  try {
    		  int hitsPerVersion = 1;    		
    		  hits = bean.search(query, start + limit, nQueryMatches, hitsPerDup, dedupField, sort, reverse, functions, hitsPerVersion);
    	  } catch ( IOException e ) {
    		  LOG.warn("Search Error", e);    	
    		  hits = new Hits( 0 ,new Hit[ 0 ] );	
    	  }
    	  LOG.info( "query:" + qRequest + " & numHits:"+ (start+limit) + " & searcherMaxHits:" + nQueryMatches + " & maxHitsPerDup:" + hitsPerDup + " & dedupField:" + dedupField + " & sortField:" + sort + " & reverse:" + reverse + " & functions:" +  this.conf.get( Global.RANKING_FUNCTIONS ) + " & maxHitsPerVersion:1 =  Total Hits: " + hits.getTotal( ) + " & Length: " +hits.getLength( ) );
    	  
    	  totalItems = String.valueOf( hits.getTotal( ) );
    	  int end = ( int )Math.min( hits.getLength( ), start + limit );
    	  int length = end-start;

    	  if( hits != null && hits.getLength( ) > 0 ) {
    		  show = hits.getHits( start , end-start );        
    		  details = bean.getDetails( show ); //get details
    		  summaries = bean.getSummary( details, query ); //get snippet
    	  }

    	  itens = luceneQueryProcessor( length, fields, details, request.getParameter( "details" ), show , summaries, query, parseTexts );
    	  endTime = System.nanoTime( );
    	  duration = ( endTime - startTime );
    	  LOG.info( "[Full-Text Query] Call response time to the query server and subsequent processing of the lucene index information: " + duration + " milliseconds." );
    	  fulltextQuery = true;
      }
     
      try {
    	  
    	  String requestUrl = request.getRequestURL( ).toString( );
    	  String base = requestUrl.substring( 0 , requestUrl.lastIndexOf( '/' ) );
    	  if( !metadataQuery ) {
    		
	    	  //*** calculate the offset of the next & previous results. ***
			  int offsetNextPage = start + limit; // offset next page
			  LOG.debug( "offsetNextPage = " + offsetNextPage );
			  int offsetPreviousPage;
			  if( start == 0 )
				  offsetPreviousPage = 0;
			  else {
				  offsetPreviousPage = start - limit;
				  offsetPreviousPage = (offsetPreviousPage < 0 ? -offsetPreviousPage : offsetPreviousPage);
			  }
			  LOG.debug( "offsetPreviousPage = " + offsetPreviousPage );
			  
			  StringBuffer requestURL = request.getRequestURL( );
			  if (request.getQueryString( ) != null) {
			      String parameterURL = request.getQueryString( ); 
				  String pattern = "&offset=([^&]+)";
			      if( parameterURL.contains( "&offset=" ) ) {
			    	  parameterURL = parameterURL.replaceAll(pattern,  "&offset=" + offsetNextPage );
			      } else {
			    	  parameterURL = parameterURL.concat( "&offset=" + offsetNextPage );
			      }
			      requestURL.append( "?" ).append( parameterURL );
			  }
			  LOG.debug( "Next_page = " + requestURL.toString( ) );
			  if( itens != null && itens.size( ) > 0 ) 
				  responseObject.setNext_page( requestURL.toString( ) );
			  
			  requestURL = request.getRequestURL( );
			  if (request.getQueryString( ) != null) {
			      String parameterURL = request.getQueryString( ); 
			      LOG.debug( "getQueryString 2 ="+ request.getQueryString( ) );
				  String pattern = "&offset=([^&]+)";
			      if( parameterURL.contains( "&offset=" ) ) {
			    	  parameterURL = parameterURL.replaceAll(pattern,  "&offset="+offsetPreviousPage );
			      } else {
			    	  parameterURL = parameterURL.concat( "&offset=" + offsetPreviousPage );
			      }
			      requestURL.append( "?" ).append( parameterURL );
			  }
			  LOG.debug( "Previous_page = " + requestURL.toString( ) );
			  if( itens != null && itens.size( ) > 0 ) 
				  responseObject.setPrevious_page( requestURL.toString( ) );
			  //** end **
			  
			  if( totalItems != null )
				  responseObject.setTotalItems( totalItems );
			  
			  //if( sortParameter != null && !"".equals( sortParameter ) )
				  //requestParameters.setSort( sortParameter );
    	  }
    	  
    	  //Set input parameters to send in response
		  if( !metadataQuery ) {
			  if( limitString != null && !limitString.equals( "" ) )
				  requestParameters.setLimit( limitString );
			  if( startString != null && !startString.equals( "" ) )
				  requestParameters.setStart( startString );
			  requestParameters.setFrom( dateStart );
			  requestParameters.setTo( dateEnd );
		  }
		  
    	  if( fulltextQuery ) {
    		  
    		  if( limitPerSiteS != null && !limitPerSiteS.equals( "" ) )
    			  requestParameters.setLimitPerSite( limitPerSiteS );
    		  if( !typeParameter.equals( "" ) )
    			  requestParameters.setType( typeParameter );
    	  }
    		  
    	  requestParameters.setPrettyPrint( prettyPrintParameter );
		  if( !siteParameter.equals( "" ) )
			  requestParameters.setSite( siteParameter );
		  
		  if( q != null && !"".equals( q ) )
			  requestParameters.setQueryTerms( q );
		  
		  if( requestParameters != null )
			  responseObject.setRequestParameters( requestParameters );
		  
    	  // generate json results
		  if( itens != null && itens.size( ) > 0 )
			  responseObject.setItens( itens );
		  else
			  responseObject.setItens( new ArrayList< Item >( ) );
		  
		  String jsonObject;
		  if( prettyOutput )
			  jsonObject = toPrettyFormat( responseObject );
		  else {
			  Gson gson = new GsonBuilder( ).disableHtmlEscaping( ).create( );
			  jsonObject = gson.toJson( responseObject );
		  }
		  
		  String callBackJavaScripMethodName = "";
		  if( request.getParameter( "callback" ) != null && !request.getParameter( "callback" ).equals( "" ) ) {
			  callBackJavaScripMethodName = request.getParameter( "callback" );
			  jsonObject = callBackJavaScripMethodName + "("+ jsonObject + ");";  
		  }
		  
		  if( !callBackJavaScripMethodName.equals( "" ) )
			  response.setContentType( "text/javascript" ); //jsonp
		  else
			  response.setContentType( "application/json" ); //json
		  
		  //response.setHeader("Content-Encoding", "gzip"); //TODO
		  
		  // Get the printwriter object from response to write the required json object to the output stream      
		  PrintWriter out = response.getWriter( );  
		  out.print( jsonObject );
		  out.flush( );

	  } catch ( JsonParseException e ) {
		  throw new ServletException( e );
	  }
	  
  }
  

  /**
   * Get the CDXServer values and insert into the final response
   * @param resultsCDX
   * @param fields
   * @param qExactURL
   * @param queryLang
   * @param start
   * @param limit
   * @param hitsPerDup
   * @param dedupField
   * @param sort
   * @param reverse
   * @return List
   */
  public List< Item > getResponseValues( List< ItemCDX > resultsCDX , String[ ] fields , String qExactURL, String queryLang, int start, int limit, int hitsPerDup, String dedupField, String sort, boolean reverse, boolean detailsInfo ) {
	  List< Item > responseFields = new ArrayList< Item >( );
	  Item item 	= null;
	  Hits hits 	= null;
	  Query query 	= null;
	  String tstamp = "";
	  String domainService = collectionsHost.substring( 0 , collectionsHost.indexOf( "/" ) );
	  
	  if( resultsCDX == null || resultsCDX.size( ) == 0 )
		  return new ArrayList< Item >( );
	  
	  for( ItemCDX itemcdx : resultsCDX ) { //originalURL, contentLength, digest, mimeType, tstamp, statusCode 
		  
		  item = new Item( );
		  if( FieldExists(  fields , "originalURL" ) ) 
			  item.setSource( itemcdx.getUrl( ) );
		  if( FieldExists( fields , "contentLength" ) )
			  item.setContentLength( itemcdx.getLength( ) );
		  if( FieldExists( fields , "digest" ) )
			  item.setDigest( itemcdx.getDigest( ) );
		  if( FieldExists( fields , "mimeType" ) )
			  item.setMimeType( itemcdx.getMime( ) );
		  
		  Date datet = null;
		  try{
			  datet  = FORMAT.parse( itemcdx.getTimestamp( ) );
			  tstamp = FORMAT.format( datet ).toString( );
			  if( FieldExists( fields , "tstamp" ) ) {
				item.setTstamp( tstamp );
			  }
		  } catch ( ParseException e ) {
			  LOG.error( e );
		  }
		  
		  if( FieldExists( fields , "statusCode" ) )
			  item.setStatus( itemcdx.getStatus( ) );
		  
		  String epochDate = "";
		  Long epochDatel = datet.getTime( );
		  if( epochDatel != null && FieldExists( fields , "date" ) ) {
			  epochDate = String.valueOf( epochDatel );
			  
			  if( epochDate != null && !epochDate.equals( "" ) ){
				  epochDate = epochDate.substring( 0 , epochDate.length( ) - 3 ); //remove 000
				  item.setDate( epochDate );
			  }
		  }
		  
		  if( itemcdx.getUrl( ) != null ) {
			  //Lucene index format
			  String target = "http://"+ collectionsHost +"/"+ FORMAT.format( datet ).toString()  +"/"+ itemcdx.getUrl( );
			  if( FieldExists( fields , "linkToArchive" ) )
				  item.setLink( target );
			  String urlNoFrame = "http://".concat( domainService ).concat( noFrame ).concat( "/" ).concat( FORMAT.format( datet ).toString( ) ).concat( "/" ).concat( itemcdx.getUrl( ) );
			  String urlEncode = "";
			  try{
				  urlEncode = URLEncoder.encode( urlNoFrame , "UTF-8" );
			  } catch( UnsupportedEncodingException e ) {
				  LOG.error( e );
				  continue;
			  }
			  String screenShotLink = "http://".concat( domainService ).concat( screenShotURL ).concat( "=" ).concat( urlEncode );
			  if( FieldExists( fields , "linkToScreenshot" ) )
				  item.setScreenShotLink( screenShotLink );
			  if( FieldExists( fields , "linkToNoFrame" ) )
				  item.setNoFrameLink( urlNoFrame );
          }
		  
		  String domainHost = "";
		  String id = "";
          try{
        	  URL source = new URL( itemcdx.getUrl( ) );
        	  id = itemcdx.getUrl( ).concat( "/" ).concat( tstamp );
              if( FieldExists( fields , "versionId" ) )
              	item.setKey( id );
          } catch( MalformedURLException e ) {
        	  if( item != null )
        		  responseFields.add( item );
        	  LOG.error( e );
        	  continue;
          }
          
		  query = null;
		  
		  String dateLucene = "date:".concat( tstamp ).concat( " " ); //format:ddmmyyyhhmmss-ddmmyyyhhmmss
		  String qLucene = dateLucene.concat( qExactURL );
		  
		  try{
			  query = Query.parse( qLucene, queryLang, this.conf );
		  } catch( IOException e ) {
			  query = null;
			  LOG.error( e );
			  if( item != null )
				  responseFields.add( item );
			  continue;
		  }
		  
    	  LOG.debug( "[URLSearch] query:" + query.toString( ) + " & numHits:"+ (start+limit) + " & searcherMaxHits:" + nQueryMatches + " & maxHitsPerDup:" + hitsPerDup + " & dedupField:" + dedupField + " & sortField:" + sort + " & reverse:" + reverse + " & maxHitsPerVersion:1" );
		  
    	  //execute the query    
    	  try {
    		  int hitsPerVersion = 1;    		
    		  hits = bean.search(query, 1, hitsPerDup, dedupField, sort, reverse, true);
    	  } catch ( IOException e ) {
    		  LOG.error("Search Error", e);    	
    		  hits = new Hits( 0 ,new Hit[ 0 ] );	
    	  }

    	  HitDetails details = null;
    	  Hit show = null; 
    	  LOG.debug( "hits.length = " + hits.getLength( )+ " hits.total = " + hits.getTotal( ) );
    	  if( hits != null && hits.getLength( ) > 0 ) {
    		  
    		  show = hits.getHit( 0 );        
    		  try{
    			  details = bean.getDetails( show ); //get details  
    		  } catch( IOException e ) {
    			  LOG.error( e );
    			  if( item != null )
    				  responseFields.add( item );
    			  continue;
    		  }
    		  
    		  Hit hit = show;
    		  HitDetails detail = details;

    		  if( FieldExists( fields , "extractedText" ) ) {
    			  String urlEncoded = "";
    			  try{
    				  urlEncoded = URLEncoder.encode( id, "UTF-8" );
    			  } catch( UnsupportedEncodingException un ) {
    				  LOG.error( un );
    				  urlEncoded = id; 
    			  }
    			  String textContent = "http://".concat( domainService ).concat( textExtracted ).concat( "=" ).concat( urlEncoded );
    			  item.setParseText( textContent );
          	  }
        	  
    		  LOG.debug( "CDXServer["+tstamp+"] Lucene["+detail.getValue( "tstamp" )+"]" );
    		  String url = detail.getValue( "url" );
    		  String title = detail.getValue( "title" );
    		  if ( title == null || title.equals("") ) {   // use url for docs w/o title
    			  title = url;
    		  }
    		  if( FieldExists( fields , "title" ) )
    			  item.setTitle( title );	
    		  String date 	= detail.getValue( "tstamp" );
    		  
    		  String encoding = detail.getValue( "encoding" );
              if( FieldExists( fields , "encoding" ) )
              	item.setEncoding( encoding );
              
              if( item.getContentLength( ) == null || item.getContentLength( ).equals( "" )  || item.getContentLength( ).equalsIgnoreCase( "0" ) ) {
            	  String contentLength = detail.getValue( "contentLength" );
                  if( FieldExists( fields , "contentLength" ) )
                  	item.setContentLength( contentLength );
              }
              
              String mimeType = detail.getValue( "primaryType" ).concat( "/" ).concat( detail.getValue( "subType" ) ); 
              
              String dateEpoch = detail.getValue( "date" );
              if( FieldExists( fields , "date" ) ) {
            	  item.setDate( dateEpoch );
              }
              if( FieldExists( fields , "mimetype" ) )
              	item.setMimeType( mimeType );
              String collection = detail.getValue( "collection" );
              if( FieldExists( fields , "collection" ) )
            	item.setCollection( collection );
              
              if( detailsInfo == true ) {
            	  int idDoc = hit.getIndexDocNo( );
            	  int index = hit.getIndexNo( );
            	  String arcname 	= detail.getValue( "arcname" );
            	  String arcoffset 	= detail.getValue( "arcoffset" );
            	  //String segment 	= detail.getValue( "segment" );
            	  
            	  if( FieldExists( fields , "filename" ) ) {
            		  if( itemcdx.getFilename( ) != null )
            			  item.setArcname( itemcdx.getFilename( ) );
            		  else if( arcname != null )
            			  item.setArcname(  arcname );
            	  }
            	  
            	  if( FieldExists( fields , "offset" ) ) {
            		  if( arcoffset != null )
            			  item.setArcoffset( arcoffset );
            		  else if( itemcdx.getOffset( ) != null )
            			  item.setArcoffset( itemcdx.getOffset( ) );
            		  
            	  }
            	  
              }
              
    	  } else {
                if( FieldExists( fields , "date" )  )
                	if( item.getDate( ) == null || item.getDate( ).equals( "" ) )
                		item.setDate( "" );
                if( FieldExists( fields , "collection" ) )
                	item.setCollection( "" );
                if( FieldExists( fields , "encoding" ) )
                  	item.setEncoding( "" );
                if( FieldExists( fields , "title" ) ) {
                	if( item.getSource() != null )
                		item.setTitle( item.getSource( ) );
                	else
                		item.setTitle( "" );
                }
                if( FieldExists( fields , "mimetype" ) )
                	if( item.getMimeType( ) == null || item.getMimeType( ).equals( "" ) )
                		item.setMimeType( "" );
                
                if( detailsInfo == true ) {
              	  
              	  if( FieldExists( fields , "filename" ) ) {
              		  if( itemcdx.getFilename( ) != null )
              			  item.setArcname( itemcdx.getFilename( ) );
              		  else
              			  item.setArcname( "" );
              	  }
              	  
              	  if( FieldExists( fields , "offset" ) ) {
              		  if( itemcdx.getOffset( ) != null )
              			  item.setArcoffset( itemcdx.getOffset( ) );
              		  else
              			item.setArcoffset( "" );
              		  
              	  }
              	  
                }
                
                if( FieldExists( fields , "extractedText" ) ) {
                	item.setParseText( "" );
                }
                
    	  }
    	  //if( hits.getTotal( ) == 0 || hits.getLength( ) == 0 )
    	  //	LOG.info( "[URLSearch] query[" + query.toString( ) + "] ts[" + item.getTstamp( ) + "] url[" + itemcdx.getUrl( ) + "]  0 hits." ); 
    	  
    	  LOG.debug( "[URLSearch] total hits: " + hits.getTotal( ) + " & length: " +hits.getLength( ) );
    	  
		  if( item != null )
			  responseFields.add( item );
	  }
	  return responseFields;
  }
  
  /**
   * Processes the values obtained from the lucene indexes and inserts into the response object
   * @param length
   * @param fields
   * @param details
   * @param detailsCheck
   * @param show
   * @param summaries
   * @param query
   * @param parseTexts
   * @return
   */
  private List< Item > luceneQueryProcessor( int length, String[ ] fields, HitDetails[ ] details, String detailsCheck, Hit[ ] show, Summary[ ] summaries, Query query, ParseText[ ] parseTexts ){
	  
	  Item item = null;
	  List< Item > items = new ArrayList< Item >( );
	  
	  boolean fieldsCorrect = true; 
	  if( fields != null && fields.length > 0 )	
		  fieldsCorrect = checkFields( fields ); 
	  String domainService = collectionsHost.substring( 0 , collectionsHost.indexOf( "/" ) ); 
	  
	  for ( int i = 0 ; i < length && fieldsCorrect ; i++ ) { 
		  	Hit hit = show[ i ];
	        HitDetails detail = details[ i ];
	        item = new Item( );
	        String title = detail.getValue( "title" );
	        String url 	= detail.getValue( "url" );
	        String date = detail.getValue( "tstamp" );
	        if( date != null && !date.equals( "" ) )
	        	date = date.substring( 0 , date.length() - 3 );
	        if ( title == null || title.equals("") ) {   // use url for docs w/o title
	        	title = url;
	        }
	        if( FieldExists( fields , "title" ) )
	        	item.setTitle( title );	
	        if( FieldExists( fields , "originalURL" ) )
	        	item.setSource( url );	

	        Date datet = null;
	        String tstamp = "";
    		try{
    			datet = FORMAT.parse( date );
    			tstamp = FORMAT.format( datet ).toString( );
    			if( FieldExists( fields , "tstamp" ) ) {
    				item.setTstamp( tstamp  );
    			}
    	    } catch ( ParseException e ) {
    	    	LOG.error( e );
    	    }
    		
            if( url != null ) {
            	// Lucene index format
            	String infoIndex = "http://" + collectionsHost + "/id" + hit.getIndexDocNo( ) + "index" + hit.getIndexNo( );
            	LOG.debug( "Index Information " + infoIndex );
            	String target = "http://"+ collectionsHost +"/"+ FORMAT.format(datet).toString()  +"/"+ url;
            	if( FieldExists( fields , "linkToArchive" ) )
            		item.setLink( target );
            }
            
            String mimeType = detail.getValue( "primaryType" ).concat( "/" ).concat( detail.getValue( "subType" ) ); 
            String id = "";
            try{
            	URL source = new URL( url );
            	id = url.concat( "/" ).concat( tstamp );
                if( FieldExists( fields , "versionId" ) )
                	item.setKey( id );
            } catch( MalformedURLException e ) {
            	LOG.error( e );
            	continue;
            }
            
            String contentLength = detail.getValue( "contentLength" );
            if( FieldExists( fields , "ContentLength" ) )
            	item.setContentLength( contentLength );
            String digest = detail.getValue( "digest" );
            if( FieldExists( fields , "digest" ) )
            	item.setDigest( digest );
            String dateEpoch = detail.getValue( "date" );
            if( FieldExists( fields , "date" ) )
            	item.setDate( dateEpoch );
            
            if( FieldExists( fields , "mimetype" ) )
            	item.setMimeType( mimeType );
            String encoding = detail.getValue( "encoding" );
            if( FieldExists( fields , "encoding" ) )
            	item.setEncoding( encoding );
            String collection = detail.getValue( "collection" );
            if( FieldExists( fields , "collection" ) )
            	item.setCollection( collection );
            
            if( url != null ) {
            	String urlNoFrame = "http://".concat( domainService ).concat( noFrame ).concat( "/" ).concat( FORMAT.format( datet ).toString( ) ).concat( "/" ).concat( url );
                String urlEncode = "";
                try{
            		urlEncode = URLEncoder.encode( urlNoFrame , "UTF-8" );
            	} catch( UnsupportedEncodingException e ) {
            		LOG.error( e );
            		continue;
            	}
            	String screenShotLink = "http://".concat( domainService ).concat( screenShotURL ).concat( "=" ).concat( urlEncode );
            	if( FieldExists( fields , "linkToScreenshot" ) )
            		item.setScreenShotLink( screenShotLink );
            	if( FieldExists( fields , "linkToNoFrame" ) )
            		item.setNoFrameLink( urlNoFrame );
            	
            	// Build the summary
                if( summaries != null ) {
                	StringBuffer sum = new StringBuffer( );
                    Fragment[ ] fragments = summaries[ i ].getFragments( );
                    for ( int j = 0 ; j < fragments.length ; j++ ) {
                      //LOG.info( "Fragment["+j+"] Text[" + fragments[ j ].getText( ) + "] isHighlight[" + fragments[ j ].isHighlight( ) + "] " );
                      if ( fragments[ j ].isHighlight( ) ) {
                        sum.append( "<em>" )
                           .append( Entities.encode( fragments[ j ].getText( ) ) )
                           .append( "</em>" );
                      } else if ( fragments[ j ].isEllipsis( ) ) {
                        sum.append( "<span class=\"ellipsis\"> ... </span>" );
                      } else {
                        sum.append( Entities.encode( fragments[ j ].getText( ) ) );
                      }
                    }
                    String summary = sum.toString( );
                    if( FieldExists( fields , "snippet" ) )
                    	item.setSnippetForTerms( summary );
                } else {
                	if( FieldExists( fields , "snippet" ) )
                		item.setSnippetForTerms( "" );
                }
                
                
            	if( FieldExists( fields , "extractedText" ) ) {
            		String urlEncoded = "";
            		//String urlDecoder = "";
            		try{
            			//urlDecoder = URLDecoder.decode( id, "UTF-8" );
            			urlEncoded = URLEncoder.encode( id, "UTF-8" );
            		} catch( UnsupportedEncodingException un ) {
            			LOG.error( un );
            			urlEncoded = id; 
            		}
            		String textContent = "http://".concat( domainService ).concat( textExtracted ).concat( "=" ).concat( urlEncoded );
            		item.setParseText( textContent );
            	} 

            }
            
            //Details info - parameter details
            if( detailsCheck != null && detailsCheck.equals( "true" ) ) {
            	int idDoc = hit.getIndexDocNo( );
            	int index = hit.getIndexNo( );
            	String arcname 		= detail.getValue( "arcname" );
                String arcoffset 	= detail.getValue( "arcoffset" );
                String segment 		= detail.getValue( "segment" );
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
            }
            
            if( item != null ) 
            	items.add( item );
      }
	  return items;
  }
  
  /*************************************************************/
  /********************* AUXILIARY METHODS *********************/
  /************************************************************/
  
  /**
   * 
   * @param resultsCDX
   * @param tstamp
   * @return
   */
  private static List< ItemCDX > selectCDXItem( List< ItemCDX > resultsCDX , String tstamp ) {
	  List< ItemCDX > items = new ArrayList< ItemCDX >( );
	  for( ItemCDX item : resultsCDX ) {
		  if( item.getTimestamp( ).equals( tstamp ) ) {
			  items.add( item );
			  return items;
		  }
	  }
	  return null;
  }
  
  /**
   * Check if parameter url is URL
   * @param url
   * @return
   */
  private static boolean urlValidator(String url){
	  Pattern URL_PATTERN = Pattern.compile("^.*? ?((https?:\\/\\/)?([a-zA-Z\\d][-\\w\\.]+)\\.([a-z\\.]{2,6})([-\\/\\w\\p{L}\\.~,;:%&=?+$#*]*)*\\/?) ?.*$");
	  return URL_PATTERN.matcher( url ).matches( );
  }
  
  /**
   * Check if parameter versionId is format url/tstamp
   * @param versionId
   * @return
   */
  private static boolean metadataValidator( String[ ] versionIdsplited ) {
	  if( urlValidator( versionIdsplited[ 0 ] ) && versionIdsplited[ 1 ].matches( "[0-9]+" ) ) 
		  return true;
	  else
		  return false;
  }
  
  /**
   * Check if str is defined
   * @param str
   * @return
   */
  private static boolean isDefined( String str ) {
	  return str == null ? false : "".equals( str ) ? false : true;  
  }
  
  
  /**
   * Check if at least one field entered per parameter is true
   * @param fields
   * @return
   */
  private static boolean checkFields( String[ ] fields ) {
	  for( String fieldInput : fields ) {
		  for( String fieldResponse: fieldsReponse ){
			  if( fieldInput.equals( fieldResponse ) )
				  return true;
		  }
	  }
	  return false;
  }
  
  /**
   * Converting a string to an integer, if it is not possible, returns a defaultVal value
   * @param number
   * @param defaultVal
   * @return
   */
  public static int parseToIntWithDefault( String number, int defaultVal ) {
	  try {
	    return Integer.parseInt( number );
	  } catch ( NumberFormatException e ) {
	    return defaultVal;
	  }
  }
  
  /**
   * Checks whether it is possible to parse from a string to a format
   * @param df
   * @param s
   * @return
   */
  private static Boolean tryParse( DateFormat df, String s ) {
	    Boolean valid = false;
	    try {
	        Date d = df.parse( s );
	        valid = true;
	    } catch ( ParseException e ) {
         	valid = false;
	    }
	    return valid;
  }
  
  /**
   * 
   * @param fields
   * @param fieldParam
   * @return
   */
  private static boolean FieldExists( String[ ] fields, String fieldParam ) {
	  if( fields == null || fields.length == 0 )
		  return true;
	  
	  for( String field : fields ) {
		  if( field.toUpperCase( ).equals( fieldParam.toUpperCase( ) ) )
			  return true;
	  }
	  return false;
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
	  Gson gson = new GsonBuilder( ).disableHtmlEscaping( ).setPrettyPrinting( ).create( );
	  String prettyJson = gson.toJson( responseObject );
      
      return prettyJson;
  }
  
}
