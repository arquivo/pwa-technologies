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
import java.net.URI;
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

/**
 * TextSearch API Back-End - Full Search. Responsible for processing the input parameters, sending a query to the QueryServers, and responding in json.
 * @author jnobre
 *
 */
public class TextSearchServlet extends HttpServlet {
  
  private static final long serialVersionUID = 1L;
  private static final Log LOG = LogFactory.getLog( TextSearchServlet.class );  
  private static final Map NS_MAP = new HashMap( ); 
  private static PwaFunctionsWritable functions = null;
  private static int nQueryMatches = 0;
  private static String collectionsHost = null;
  private static final SimpleDateFormat FORMAT = new SimpleDateFormat( "yyyyMMddHHmmss" );
  Calendar DATE_END = new GregorianCalendar();
  private static final String noFrame = "/noFrame/replay";
  private static final String screenShotURL = "/screenshot/?url";
  static {
    NS_MAP.put( "serviceName" , "Arquivo.pt - the Portuguese web-archive" );
    NS_MAP.put( "link" , "http://arquivo.pt" );
  }  
  private NutchBean bean;
  private Configuration conf;
  private static String[ ]  fieldsReponse = {"versionId", "versionTitle", "source", "linkToArchive",
		  "tstamp", "contentLength", "digest", "primaryType", "subType", "downloadImage",
		  "date", "encoding", "noFrameLink"};
  
  public void init( ServletConfig config ) throws ServletException {
    try {
      this.conf = NutchConfiguration.get( config.getServletContext( ) );
      bean = NutchBean.get( config.getServletContext( ), this.conf );

      functions = PwaFunctionsWritable.parse( this.conf.get( Global.RANKING_FUNCTIONS ) );            
      nQueryMatches=Integer.parseInt( this.conf.get( Global.MAX_FULLTEXT_MATCHES_RANKED ) );
      
      collectionsHost = this.conf.get( "wax.host", "examples.com" );
    } 
    catch ( IOException e ) {
      throw new ServletException( e );
    }
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {

	  LOG.info("[doGet] query request from " + request.getRemoteAddr( ) );
	  Calendar DATE_END = currentDate( );
	  String dateEndString = FORMAT.format( DATE_END.getTime( ) );
	  int start = 0;
	  int limit = 50;
	  
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
	  String startString = request.getParameter( "offset" );
	  if ( startString != null )
		  start = Integer.parseInt( startString );
    
	  // number of items to display
	  String limitString = request.getParameter( "maxItems" );
	  if ( limitString != null )
		  limit = Integer.parseInt(limitString);
	  
	  if( limit > 2000 )
		  limit = 2000;
	  
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
      String hitsPerSiteString = request.getParameter( "itemsPerSite" );
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
      
      LOG.debug( "[doGet] dtstart["+dateStart+"] dtend["+dateEnd+"]" );
      
      if( dateStart== null && dateEnd != null ){
    	  dateStart = "19960101000000"; /*If datestart is not specified set it to 1996*/
      }
      if( dateStart != null && dateEnd == null ){
    	  Calendar dateEND = currentDate( );
    	  dateEnd = FORMAT.format( dateEND.getTime( ) );
      }

      if (dateStart!=null && dateEnd!=null) {  	    	
    	  try {
    		  String startDate = null;
    		  String endDate = null;
    		  
    		  DateFormat dOutputFormatTimestamp = new SimpleDateFormat("yyyyMMddHHmmss");
    		  dOutputFormatTimestamp.setLenient( false );
    		  DateFormat dOutputFormatYear = new SimpleDateFormat("yyyy");
    		  dOutputFormatYear.setLenient( false );
    		  String dateFinal = "";
    		  if( tryParse( dOutputFormatTimestamp , dateStart )  ) {
    			  Date dStart = dOutputFormatTimestamp.parse( dateStart );
    			  startDate = dOutputFormatTimestamp.format( dStart.getTime( ) );
    		  } else if( tryParse( dOutputFormatYear , dateStart )  ) {
    			  String extensionStart = "0101000000"; 
    			  dateStart = dateStart.concat( extensionStart );
    			  if( tryParse( dOutputFormatTimestamp , dateStart )  ) {
    				  Date dStart = dOutputFormatTimestamp.parse( dateStart );
        			  startDate = dOutputFormatTimestamp.format( dStart.getTime( ) );
    			  }
    		  }
    			  
    		  if( tryParse( dOutputFormatTimestamp , dateEnd ) ) {
    			  Date dEnd = dOutputFormatTimestamp.parse( dateEnd );
    			  endDate = dOutputFormatTimestamp.format( dEnd.getTime( ) );
    		  } else if( tryParse( dOutputFormatYear , dateEnd ) ) {
    			  String extensionEnd = "1231235959";
    			  dateEnd = dateEnd.concat( extensionEnd );
    			  if( tryParse( dOutputFormatTimestamp , dateEnd )  ) {
    				  Date dEnd = dOutputFormatTimestamp.parse( dateEnd );
    				  endDate = dOutputFormatTimestamp.format( dEnd.getTime( ) );
    			  }
    		  }
    		   
    		  if( startDate == null && endDate != null ) {
    			  startDate = "19960101000000";
    		  }
    		  
    		  if( startDate != null && endDate == null ) {
    			  Calendar dateEND = currentDate( );
    			  endDate = FORMAT.format( dateEND.getTime( ) );
    		  }
    		  
 
    		  
    		  if( startDate != null && endDate != null ) {
    			  dateFinal = " date:".concat( startDate ).concat( "-" ).concat( endDate );
    			  queryString.append( dateFinal );
    		  }
    		  
    	  } catch ( ParseException e ) {
    		  // ignore
    		  LOG.error( "Parse Exception: " , e );
    	  } catch ( IndexOutOfBoundsException e ) {
    		  // ignore
    		  LOG.error( "IndexOutOfBoundsException: " , e );
    	  }    	
      }
      
      // To support querying opensearch by  url
      // Lucene index format
      String queryStringOpensearchWayback=null;
      boolean isOpensearhWayback=false;
      String urlQueryParam=null;
      
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
    	
     
      Hits hits;

      Query query = null;
      String qRequest = queryString.toString( );
	  query = Query.parse( qRequest , queryLang , this.conf );
	  LOG.info( "query:" + qRequest + " & numHits:"+ (start+limit) + " & searcherMaxHits:" + nQueryMatches + " & maxHitsPerDup:" + hitsPerDup + " & dedupField:" + dedupField + " & sortField:" + sort + " & reverse:" + reverse + " & functions:" +  this.conf.get( Global.RANKING_FUNCTIONS ) + " & maxHitsPerVersion:1" );

	  //execute the query    
	  try {    		
		  int hitsPerVersion = 1;    		
		  hits = bean.search(query, start + limit, nQueryMatches, hitsPerDup, dedupField, sort, reverse, functions, hitsPerVersion);
	  } 
	  catch (IOException e) {
		  LOG.warn("Search Error", e);    	
		  hits = new Hits( 0 ,new Hit[ 0 ] );	
	  }

	  LOG.info( "total hits: " + hits.getTotal( ) + " & length: " +hits.getLength( ) );

	  // generate json results
	  int end = ( int )Math.min( hits.getLength( ), start + limit );
	  int length = end-start;

	  HitDetails[ ] details = null;
	  Hit[ ] show = null; 
	  
	  if( hits != null && hits.getLength( ) > 0 ) {
		  show = hits.getHits( start , end-start );        
		  details = bean.getDetails( show ); //get details
	  }

	  String requestUrl = request.getRequestURL( ).toString( );
	  String base = requestUrl.substring( 0 , requestUrl.lastIndexOf( '/' ) );
      
	  try {
		  String fieldsParam = "";
		  if( request.getParameter( "fields" ) != null )
			  fieldsParam = request.getParameter( "fields" );
		  String[ ] fields = null;
		  if( !fieldsParam.equals( "" ) )
			  fields = fieldsParam.split( "," );
		  
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
		  if( !typeParameter.equals( "" ) )
			  requestParameters.setType( typeParameter );
		  requestParameters.setPrettyPrint( prettyPrintParameter );
		  if( !siteParameter.equals( "" ) )
			  requestParameters.setSite( siteParameter );
		  
		  int offsetNextPage = start + limit;
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
		  if( hits != null && hits.getLength( ) > 0 ) 
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
		  if( hits != null && hits.getLength( ) > 0 ) 
			  responseObject.setPrevious_page( requestURL.toString( ) );
		  
		  
		  /*if( sortParameter != null && !"".equals( sortParameter ) )
			  requestParameters.setSort( sortParameter );*/
		  
		  if( q != null && !"".equals( q ) )
			  requestParameters.setQueryTerms( q );
		  
		  if( requestParameters != null )
			  responseObject.setRequestParameters( requestParameters );
		  
		  boolean fieldsCorrect = true; 
		  if( fields != null && fields.length > 0 )	  
			  fieldsCorrect = checkFields( fields );
			  
		  Item item = null;
		  for ( int i = 0 ; i < length && fieldsCorrect ; i++ ) {
			  	Hit hit = show[ i ];
		        HitDetails detail = details[ i ];
		        item = new Item( );
		        
		        
		        
		        String title = detail.getValue( "title" );
		        String url 	= detail.getValue( "url" );
		        String date = detail.getValue( "tstamp" );
		        if( FieldExists( fields , "versionTitle" ) )
		        	item.setTitle( title );	
		        if( FieldExists( fields , "source" ) )
		        	item.setSource( url );	
		        
		        Date datet = null;
		        String tstamp = "";
        		try{
        			datet = FORMAT.parse( date );
        			if( FieldExists( fields , "tstamp" ) ) {
        				tstamp = FORMAT.format( datet ).toString( );
        				item.setTstamp( tstamp );
        			}
        	    }catch ( ParseException e ){
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
                
                URL source = new URL( url );
                String domainHost = source.getHost( );
                String id = domainHost.concat( "/" ).concat( tstamp );
                if( FieldExists( fields , "versionId" ) )
                	item.setKey( id );
                String contentLength = detail.getValue( "contentLength" );
                if( FieldExists( fields , "ContentLength" ) )
                	item.setContentLength( contentLength );
                String digest = detail.getValue( "digest" );
                if( FieldExists( fields , "digest" ) )
                	item.setDigest( digest );
                String dateEpoch = detail.getValue( "date" );
                if( FieldExists( fields , "Date" ) )
                	item.setDate( dateEpoch );
                String primaryType = detail.getValue( "primaryType" );
                if( FieldExists( fields , "primaryType" ) )
                	item.setPrimaryType( primaryType );
                String subType = detail.getValue( "subType" );
                if( FieldExists( fields , "subtype" ) )
                	item.setSubType( subType );
                String encoding = detail.getValue( "encoding" );
                if( FieldExists( fields , "encoding" ) )
                	item.setEncoding( encoding );
                
                if( url != null ) {
                	String urlNoFrame = "http://".concat( "arquivo.pt" ).concat( noFrame ).concat( "/" ).concat( FORMAT.format( datet ).toString( ) ).concat( "/" ).concat( url );
                    LOG.debug( "[TextSearchServlet][doGet] urlNoFrame =" + urlNoFrame );
                	String urlEncode = URLEncoder.encode( urlNoFrame , "UTF-8" );
                	LOG.debug( "[TextSearchServlet][doGet] urlEncode =" + urlEncode );
                	String screenShotLink = "http://".concat( "arquivo.pt" ).concat( screenShotURL ).concat( "=" ).concat( urlEncode );
                	if( FieldExists( fields , "ScreenShotLink" ) )
                		item.setScreenShotLink( screenShotLink );
                	if( FieldExists( fields , "noFrameLink" ) )
                		item.setNoFrameLink( urlNoFrame );
                }
                
               /* String itemText = "NOT IMPLEMENTED"; //TODO not implemented
                if( FieldExists( fields , "ItemText" ) )
                	item.setItemText( itemText );*/
                
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


	  } catch ( JsonParseException e ) {
		  throw new ServletException( e );
	  }
	  
  }

  private static boolean checkFields( String[ ] fields ) {
	  for( String fieldInput : fields ) {
		  for( String fieldResponse: fieldsReponse ){
			  if( fieldInput.equals( fieldResponse ) )
				  return true;
		  }
	  }
	  return false;
  }
  
  private static Boolean tryParse( DateFormat df, String s ) {
	    Boolean valid = false;
	    try {
	        Date d = df.parse( s );
	        LOG.debug( "[tryParse] s[" + s + "] valid = true" );
	        valid = true;
	    } catch ( ParseException e ) {
	    	LOG.debug( "[tryParse] s[" + s + "] valid = false" );
         	valid = false;
	    }
	    return valid;
  }
  
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
