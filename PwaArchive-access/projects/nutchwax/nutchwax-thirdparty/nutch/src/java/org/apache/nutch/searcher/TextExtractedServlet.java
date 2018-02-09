package org.apache.nutch.searcher;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;

import org.apache.nutch.global.Global;
import org.apache.nutch.html.Entities;
import org.apache.nutch.parse.Parse;
import org.apache.nutch.parse.ParseText;
import org.apache.nutch.searcher.HitContent;
import org.apache.nutch.searcher.Summary.Fragment;
import org.apache.nutch.util.NutchConfiguration;


/**
 * Servlet responsible for downloading text extracted from a TextSearchServlet item.
 * 
 * @author jnobre
 */
public class TextExtractedServlet extends HttpServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int BYTES_DOWNLOAD = 1024;
	private static final Log LOG = LogFactory.getLog( TextSearchServlet.class );  
	private static final Map NS_MAP = new HashMap( ); 
	private static String collectionsHost = null;
	private static final SimpleDateFormat FORMAT = new SimpleDateFormat( "yyyyMMddHHmmss" );
	Calendar DATE_END = new GregorianCalendar( );
	private static final String noFrame = "/noFrame/replay";
	private static final String screenShotURL = "/screenshot/?url";
	private NutchBean bean;
	private Configuration conf;
	  
	static {
		NS_MAP.put( "serviceName" , "Arquivo.pt - the Portuguese web-archive" );
		NS_MAP.put( "link" , "http://arquivo.pt" ); 
	}  
	  
	private static String[ ]  fieldsReponse = {"versionId", "title", "originalURL", "linkToArchive",
			  "tstamp", "contentLength", "digest", "mimeType", "linkToScreenshot",
			  "date", "encoding", "noFrameLink", "collection", "snippet", "extractedText"}; //input parameters
	  
	/**
	   * HttpServlet init method.
	   * @param config: nutchwax configuration
	   * @return void
	   */
	public void init( ServletConfig config ) throws ServletException {
		try {
			this.conf = NutchConfiguration.get( config.getServletContext( ) );
			bean = NutchBean.get( config.getServletContext( ), this.conf );
        
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
		String metadataParam 	= "";
		String qExactURL = "";
		String queryLang = "";
		String dedupField = "site";
		Hits hits;
		String sort = "relevance";
		boolean reverse = false;
		String dateStart;
		String dateEnd;
		String fileName = "";
		String fileTypeError = "text/plain";
		String errorM = "";
		
		// get parameters from request
		request.setCharacterEncoding("UTF-8");
		//get metadata parameter
		metadataParam  = request.getParameter( "m" ); 
			
		if( metadataParam != null && !metadataParam.equals( "" ) ) { //metadata query
	    	  LOG.info( "[Text Extacted] Download File" );
	    	  String metadataParamD = "";
	    	  try{	  
	    		  metadataParamD =  URLDecoder.decode(metadataParam, "UTF-8");
	    	  } catch( UnsupportedEncodingException un ) {
	    		  LOG.error( un );
	    		  metadataParamD = metadataParam; 
	    	  }
	    	  String rversionId = metadataParamD.split( " " )[ 0 ];
	    	  LOG.info( "rversionId["+rversionId+"] metadataParam["+metadataParamD+"]" );
	    	  int indx = rversionId.lastIndexOf( "/" );
	    	  LOG.info( "indx["+indx+"]" );
	    	  if( indx > 0 ) {
	        	  String[ ] versionIdsplited = { rversionId.substring( 0, indx ), rversionId.substring( indx + 1) };
	        	  if( metadataValidator( versionIdsplited ) ) { //valid URL
	        		 LOG.info( "Version ["+rversionId+"] is correct" );
	        		 if( urlValidator( versionIdsplited[ 0 ] ) ) { //valid URL
	        			 dateStart = "19960101000000";
	        			 Calendar dateEND = currentDate( );
	        			 dateEnd = FORMAT.format( dateEND.getTime( ) );
	        			 
	           		  	 String[ ] urlEncoded = metadataParamD.split( " " );
	    				 
	        			 StringBuilder sbExactURL = new StringBuilder( ); //build extacurl info
	           		  	 for( int i = 1 ; i < urlEncoded.length ; i++ ) {
	           		  		 sbExactURL.append( urlEncoded[ i ].concat( " " ) );
	           		  	 }
	           		  	 qExactURL = sbExactURL.toString( );
	           		  	 LOG.info(" [Text Extacted] qExactURL["+qExactURL+"]");
	           		  	 int hitsPerDup = 1;
	           		  	 
	           		  	 Query query = null;
	           		  	 String dateLucene = "date:".concat( versionIdsplited[ 1 ] ).concat( " " ); //format:ddmmyyyhhmmss-ddmmyyyhhmmss
	           		  	 String qLucene = dateLucene.concat( qExactURL );
	           		  	 LOG.info( "qLucene = " + qLucene );
	           		  	 try{
	           		  		 query = Query.parse( qLucene, queryLang, this.conf );  
	           		  	 } catch( IOException e ) {
	           		  		 query = null;
	           		  		 errorM = "Error parsing the query";
	           		  		 LOG.error( e );
	           		  	 }
	           		  	 
	           		  	 //execute the query
	           		  	 try {
	           		  		 LOG.info( "Search for query["+query.toString()+"] hitsPerDup["+hitsPerDup+"] dedupField["+dedupField+"] sort["+sort+"] reverse["+reverse+"]" );
	           		  		 int hitsPerVersion = 1;    		
	           		  		 hits = bean.search(query, 1, hitsPerDup, dedupField, sort, reverse, true);
	           		  	 } catch ( IOException e ) {
	           		  		 LOG.error("Search Error", e);    	
	           		  		 hits = new Hits( 0 ,new Hit[ 0 ] );
	           		  	 }
	           		  	 
	           		  	 HitDetails[ ] details = null;
	           		  	 Hit[ ] show = null; 
	           		  	 LOG.info( "hits.length = " + hits.getLength( )+ " hits.total = " + hits.getTotal( ) );
	           		  	 if( hits != null && hits.getLength( ) > 0 ) {
	           		  		 show = hits.getHits( 0 , 1 );     
	           		  		 try{
	           		  			 details = bean.getDetails( show ); //get details  
	           		  		 } catch( IOException e ) {
	           		  			 LOG.error( e );
	           		  		 }
	           		  		 
		           		  	 ParseText[ ] textContent = null;
		           		  	 if( details != null && details.length > 0 ) {
		           		  		 try{
		           		  			 LOG.info( "hits SIZE = " +  details.length );
		           		  			 textContent = bean.getParseText( details );
		           		  		 } catch( IOException e ) {
		           		  			 LOG.error( e );
		           		  		 }
		                		  
		           		  		 if( textContent != null && textContent[ 0 ] != null ) {
		           		  			 LOG.info( "[getResponseValues] textContent["+textContent[0]+"] defined" );
		           		  			 String parseText = textContent[ 0 ].getText( ); 
		           		  			 LOG.info( "[getResponseValues] ParseText = " + parseText );
		           		  			 fileName = versionIdsplited[ 0 ] + "-" + versionIdsplited[ 1 ]; 
		           		  			 response.setContentType( "text/plain" );
		           		  			 if( fileName.startsWith( "https://" ) )
		           		  				 fileName = fileName.replace( "https://" , "" );
		           		  		     if( fileName.startsWith( "http://" ) )	 
		           		  		    	 fileName = fileName.replace( "https://" , "" );
		           		  			 fileName = fileName.replace( "/" , "-");
		           		  			 response.setHeader( "Content-disposition","attachment; filename=" + fileName + ".txt" );
		           		  			 
		           		  			 InputStream input = new ByteArrayInputStream( parseText.getBytes( "UTF8" ) );
		           		  		     int read = 0;
		           		  		     byte[ ] bytes = new byte[ BYTES_DOWNLOAD ];
		           		  		     OutputStream os = response.getOutputStream( );
		           		  		     while ( ( read = input.read( bytes ) ) != -1 ) {
		           		  		    	 os.write( bytes, 0, read );
		           		  		     }
		           		  		     os.flush( );
		           		  		     os.close( );
		           		  		     
		           		  		     return;
		           		  		     
		           		  		 } else {
		           		  			 LOG.info( "[getResponseValues] textContent NULL" );
		           		  			 errorM = "Empty file - ";
		           		  			 
		           		  		 }
		                	  } 
	           		  	 }
	           		 }
	        		 
	        	  } else {
	        		  LOG.info( "Version 1 ["+metadataParamD+"] NOT correct" );
	        	  }
	    		  
	    	  }
		}
		
		// You must tell the browser the file type you are going to send
        // for example application/pdf, text/plain, text/html, image/jpg
        //TODO error - response.setContentType(fileType);
		
		

	
	}
	  
	/**
	 * Check if parameter versionId is format url/tstamp
	 * @param versionId
	 * @return
	 */
	private static boolean metadataValidator( String[ ] versionIdsplited ) {
		LOG.info( "metadata versionId[0]["+versionIdsplited[ 0 ]+"] versionId[1]["+versionIdsplited[ 1 ]+"]" );
		if( urlValidator( versionIdsplited[ 0 ] ) && versionIdsplited[ 1 ].matches( "[0-9]+" ) ) 
			return true;
		else
			return false;
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
	  
	
	
}
