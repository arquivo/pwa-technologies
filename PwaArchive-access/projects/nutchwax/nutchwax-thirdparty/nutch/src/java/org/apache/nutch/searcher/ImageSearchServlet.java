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

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import java.util.Base64;

/**
 * ImageSearch API Back-End. 
 * Servlet responsible for returning a json object with the results of the query received in the parameter.
 * 
 * @author fmelo
 * @version 1.0
 */
public class ImageSearchServlet extends HttpServlet {
	/**
	 * Class responsible for:
	 * 	Search the indexes Lucene, through the calls to the queryServers.
	 *	Search by URL in CDX indexes, through the CDXServer API.
	 *
	 * Documentation: https://github.com/arquivo/pwa-technologies/wiki/APIs - Full-text search: TestSearch based Arquivo.pt API
	 * (The code indentation isn't adequate. Consequence of the NutchWax structure)
	 */


	private static final long serialVersionUID = 1L;
	private static final Log LOG = LogFactory.getLog( ImageSearchServlet.class );  
	private static final Map NS_MAP = new HashMap( ); 
	private static PwaFunctionsWritable functions = null;
	private static int nQueryMatches = 0;
	private static String collectionsHost = null;
	private static String solrHost = null;
	private static final SimpleDateFormat FORMAT = new SimpleDateFormat( "yyyyMMddHHmmss" );
	Calendar DATE_END = new GregorianCalendar( );
	private static final String noFrame = "/noFrame/replay";
	private static final String screenShotURL = "/screenshot/?url";
	private static final String textExtracted = "/textextracted?m";
	private static final String infoMetadata = "/imagesearch?metadata";
	private NutchBean bean;
	private Configuration conf;

	static {
		NS_MAP.put( "serviceName" , "Arquivo.pt - the Portuguese web-archive" );
		NS_MAP.put( "link" , "http://arquivo.pt/images" ); 
	}  

	/**
	 * HttpServlet init method.
	 * @param config: nutchwax configuration
	 * @return void
	 */
	public void init( ServletConfig config ) throws ServletException {
		try {
			this.conf = NutchConfiguration.get( config.getServletContext( ) );
			bean = NutchBean.get( config.getServletContext( ), this.conf );     
			collectionsHost = this.conf.get( "wax.host", "arquivo.pt" );
			solrHost = this.conf.get( "wax.solrserver", "http://p63.arquivo.pt:8983/solr/SAFE" );
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

		response.addHeader("Access-Control-Allow-Origin", "*");
		response.addHeader("Access-Control-Allow-Methods", "GET, HEAD");

		Calendar DATE_END = currentDate( );
		String dateEndString = FORMAT.format( DATE_END.getTime( ) );
		int start = 0;
		int limit = 50; /*Default number of results*/

		long startTime;
		long endTime;
		long duration;

		ImageSearchResponse imgSearchResponse=null;
		ImageSearchResults imgSearchResults=null;
		String safeSearch = "";
		
		ArrayList<String> fqStrings = new ArrayList<String>();
		String flString =""; /*limit response fields*/
		String jsonSolrResponse="";

		// get parameters from request
		request.setCharacterEncoding("UTF-8");
		StringBuilder queryString = new StringBuilder( );
		String q = request.getParameter("q");

		if ( q == null )
			q = "";

		queryString.append( q );


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
			limit = 2000; //Max Number of Results 2000 in one request?

		// date restriction   
		String dateStart = request.getParameter( "from" );
		if ( dateStart == null || dateStart.length( ) == 0 ) {
			dateStart = "19960101000000";
		}
		String dateEnd = request.getParameter( "to" );
		if ( dateEnd == null || dateEnd.length( ) == 0 ) {
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
			} catch ( ParseException e ) {
				// ignore
				LOG.error( "Parse Exception: " , e );
			} catch ( IndexOutOfBoundsException e ) {
				// ignore
				LOG.error( "Parse Exception: " , e );
			}    	
		}
		fqStrings.add("imgTstamp:["+dateStart + " TO "+ dateEnd+"]");
		safeSearch = request.getParameter("safeSearch");
		if(! "off".equals(safeSearch)){
			fqStrings.add("safe:[0 TO 0.49]"); /*Default behaviour is to limit safe score from 0 -> 0.49; else show all images*/
		}

		String typeParameter = request.getParameter( "type" );
	      if( typeParameter == null )
	    	  typeParameter = "";
	      if( !typeParameter.equals( "" ) ){
	    	  if(typeParameter.toLowerCase().equals("jpeg") || typeParameter.toLowerCase().equals("jpg") ){
	    		  fqStrings.add("imgMimeType:image/jpeg OR imgMimeType:image/jpg");
	    	  }
	    	  else{
	    		  fqStrings.add("imgMimeType: image/"+ typeParameter);
	    	  }
	      }		
	      String sizeParameter = request.getParameter( "size" );
	      if( sizeParameter == null )
	    	  sizeParameter = "";
	      if( !sizeParameter.equals( "" ) ){
	    	  if(sizeParameter.equals("sm")){
	    		  fqStrings.add("{!frange u=65536 }product(imgHeight,imgWidth)"); /*images up to 65536pixels² of area - i.e. max square size of 256x256px*/
	    	  }else if(sizeParameter.equals("md")){
	    		  fqStrings.add("{!frange l=65537 u=810000 }product(imgHeight,imgWidth)"); /*images between 65537pixels² of area , up to  810000px² of area - i.e. max square size of 900x900px*/ 
	    	  }else if(sizeParameter.equals("lg")){
	    		  fqStrings.add("{!frange l=810001}product(imgHeight,imgWidth)"); /*images bigger than 810000px² of area*/
	    	  }
	      }
	      if( request.getParameter( "fields" ) != null ){
	    	  flString = request.getParameter( "fields" );
	      }
	      
		//Pretty print in output message 
		String prettyPrintParameter = request.getParameter( "prettyPrint" );
		boolean prettyOutput = false;
		if( prettyPrintParameter != null && prettyPrintParameter.equals( "true" ) ) 
			prettyOutput = true;

		startTime = System.nanoTime( );
		//execute the query    
		try {
			LOG.info("SOLR HOST:" + solrHost);
			SolrClient solr = new HttpSolrClient.Builder(solrHost).build();
			SolrQuery solrQuery = new SolrQuery();
			solrQuery.setQuery(q);
			LOG.info("FilterQuery Strings:" + fqStrings);
			
			for(String fq : fqStrings){
				solrQuery.addFilterQuery(fq);
			}
			
			solrQuery.set("defType", "edismax");
			solrQuery.set("qf", "imgTitle^4 imgAlt^3 imgSrcTokens^2 pageTitle pageURLTokens");
			solrQuery.set("pf", "imgTitle^4000 imgAlt^3000 imgSrcTokens^2000 pageTitle^1000 pageURLTokens^1000");
			solrQuery.set("ps", 1);
			solrQuery.set("pf2", "imgTitle^400 imgAlt^300 imgSrcTokens^200 pageTitle^100 pageURLTokens^100");
			solrQuery.set("ps2", 2);
			solrQuery.set("pf3", "imgTitle^40 imgAlt^30 imgSrcTokens^20 pageTitle^10 pageURLTokens^10");
			solrQuery.set("ps3", 3);
			solrQuery.setRows(limit); 

			solrQuery.setStart(start);
			
			if(!flString.equals("")){
				solrQuery.set("fl", flString);
			}
			
			QueryResponse responseSolr = null;
			try{
				responseSolr = solr.query(solrQuery);
			}catch (SolrServerException e){
				LOG.info( "Solr Server Exception : "+ e );
			}
			int invalidDocs = 0;
			SolrDocumentList documents = new SolrDocumentList();
			for(SolrDocument doc : responseSolr.getResults()){ /*Iterate Results*/
				if(flString.equals("") || flString.contains("imgSrcBase64")){
					byte[] bytesImgSrc64 = (byte[]) doc.getFieldValue("imgSrcBase64");
					if(bytesImgSrc64 == null){
						LOG.info("Null image");
						invalidDocs++;
						continue;
					}
					byte[] encodedImgSrc64 = Base64.getEncoder().encode(bytesImgSrc64);
					String imgSrc64 = new String(encodedImgSrc64);
					doc.setField("imgSrcBase64", imgSrc64); 
					documents.add(doc);
				}
				else{
					documents.add(doc);
				}
			}
			
			imgSearchResults = new ImageSearchResults(responseSolr.getResults().getNumFound(),limit- invalidDocs ,responseSolr.getResults().getStart() ,documents);
			imgSearchResponse = new ImageSearchResponse(responseSolr.getResponseHeader(), imgSearchResults );			   		  
		} catch ( IOException e ) {
			LOG.warn("Search Error", e);    	
		}

		endTime = System.nanoTime( );
		duration = ( endTime - startTime );
		try {

			String jsonObject;
			if( prettyOutput ){
				Gson gson = new GsonBuilder().setPrettyPrinting().create();
				jsonSolrResponse = gson.toJson(imgSearchResponse); 
			}
			else {
				Gson gson = new Gson();
				jsonSolrResponse = gson.toJson(imgSearchResponse); 
			}
			//TODO:: callback option and setting jsonp content type in that case
			response.setContentType( "application/json" ); //json

			// Get the printwriter object from response to write the required json object to the output stream      
			PrintWriter out = response.getWriter( );  
			out.print( jsonSolrResponse );
			out.flush( );

		} catch ( JsonParseException e ) {
			throw new ServletException( e );
		}

	}



	/*************************************************************/
	/********************* AUXILIARY METHODS *********************/
	/************************************************************/

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
	 * Check if str is defined
	 * @param str
	 * @return
	 */
	private static boolean isDefined( String str ) {
		return str == null ? false : "".equals( str ) ? false : true;  
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
