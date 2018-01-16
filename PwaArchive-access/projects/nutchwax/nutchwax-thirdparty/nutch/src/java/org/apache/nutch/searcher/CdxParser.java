package org.apache.nutch.searcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jettison.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;


public class CdxParser {
	
	private static final Log LOG = LogFactory.getLog( CdxParser.class ); 
	private static final String cdxServer = "/wayback/-cdx?";
	private final String equalOP = "=";
	private final String andOP = "&";
	private final String outputCDX = "json";
	private final String keyUrl 		= "url";
	private final String keyDigest 		= "digest";
	private final String keyMimeType 	= "mime";
	private final int timeoutConn = 3000;
	private final int timeoutreadConn = 5000;
	private String collectionsHost;

	
	public CdxParser( String collectionsHost ) {
		this.collectionsHost = collectionsHost;
	}
	
	
	public List< ItemCDX > getResults( String url , String from , String to, int limit, int start ) {
		Gson gson = new Gson( );
		List< ItemCDX > cdxList = new ArrayList< ItemCDX >( );
		String urlCDX = getLink( url , from , to );
		int counter = 0;
		LOG.info( "[getResults] urlCDX["+urlCDX+"]" );
		try{
			List< JsonObject > jsonValues = readJsonFromUrl( urlCDX );
			if( jsonValues == null ) 
				return null;
			limit = limit + start;
			for( int i = 0 ; i < jsonValues.size( ) ; i++ ) { //convert cdx result into object
				if( i < start ) continue;
				ItemCDX item = gson.fromJson( jsonValues.get( i ) , ItemCDX.class );
				if( cdxList.contains( item ) ) continue;
				cdxList.add( item );
				
				if( i > limit )
					break;
			}
			
			return cdxList;
			
		} catch( Exception e ){
			LOG.debug( "[getResults] URL["+urlCDX+"] e " , e );
			return new ArrayList< ItemCDX >( );
		}
	}
	
	/**
	 * build CDXServer url
	 * @param url
	 * @param timestamp
	 * @return
	 */
	private String getLink( String url , String from , String to ) {
		LOG.info( "[CDXParser][getLink] url["+url+"] from["+from+"] to["+to+"]" );
		return "http://".concat( "arquivo.pt" ).concat( cdxServer ) 
					.concat( "url" )
					.concat( equalOP )
					.concat( url )
					.concat( andOP )
					.concat( "output" )
					.concat( equalOP )
					.concat( outputCDX )
					.concat( andOP )
					.concat( "from" )
					.concat( equalOP )
					.concat( from )
					.concat( andOP )
					.concat( "to" )
					.concat( equalOP )
					.concat( to );
					//.concat( Constants.equalOP )
					//.concat( timestamp )
	}
	
	
	/**
	 * Connect and get response to the CDXServer
	 * @param strurl
	 * @return
	 */
	private ArrayList< JsonObject > readJsonFromUrl( String strurl ) {
		InputStream is = null;
		ArrayList< JsonObject >  jsonResponse = new ArrayList< JsonObject >( );
		try {
			URL url = new URL( strurl );
			URLConnection con = url.openConnection( );
			con.setConnectTimeout( timeoutConn ); //3 sec
			con.setReadTimeout( timeoutreadConn );//5 sec
			is = con.getInputStream( );
			BufferedReader rd = new BufferedReader( new InputStreamReader( is , Charset.forName( "UTF-8" ) ) );
			jsonResponse = readAll( rd );
		    return jsonResponse;
		} catch( Exception e ) {
			LOG.error( "[readJsonFromUrl]" + e );
			return null;
		} finally {
			if( is != null ) {
				try { is.close( ); } catch( IOException e1 ) {  LOG.error( "[readJsonFromUrl] Close Stream: " + e1 ); }
			}
		}
	 }
	
	/**
	 * build json struture with CDXServer response
	 * @param rd
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 */
	private  ArrayList< JsonObject > readAll( BufferedReader rd ) throws IOException  {
		ArrayList< JsonObject > json = new ArrayList< JsonObject >( );
		String line;
		while ( ( line = rd.readLine( ) ) != null ) {
			JsonParser parser = new JsonParser();
			JsonObject o = parser.parse( line ).getAsJsonObject( );
			json.add( o );
		}
		return json;
	}
}
