package org.archive.access.nutch.jobs;

import java.io.*;
import java.net.*;
import java.util.*;

import org.apache.commons.lang.ArrayUtils;

/**
 * Expands a url query with its alias
 * @author Miguel Costa
 */
public class EntryPageExpansion {

	private final static String pathOfEntryFiles[]={"","/","/index.html","/index.htm","/index.shtml","/default.html","/default.htm","/index.php","/index.asp","/index.aspx","/index.jsp"};
	private static Hashtable<String,Boolean> pathOfEntryFilesHT=null;
	
	/**
	 * Initialize
	 */
	private static void init() {
		if (pathOfEntryFilesHT==null) {
			pathOfEntryFilesHT=new Hashtable<String,Boolean>();
			for (int i=0;i<pathOfEntryFiles.length;i++) {
				pathOfEntryFilesHT.put(pathOfEntryFiles[i], true);
			}
		}		
	}	
	
	/**
	 * Expand url for all possible combinations
	 * @param surl url
	 * @return
	 */
	public static String[] expand(String surl) {
		// initialize hashtable
		init();
		
		URL url;
		try {
			url = new URL(surl);
		} 
		catch (MalformedURLException e) {
			return new String[]{surl};
		}
		if (surl.startsWith("http:///")) { // url is malformed because it has more than 2 slashes, but it is not detected when calling new URL()
			return new String[]{surl};
		}
				
		String entryFiles[]=null;
		String urlPath=url.getPath();
		String parts[] = urlPath.split("/");
		if (url.getQuery()==null && (parts.length==0 || parts[parts.length-1].indexOf('.')==-1 || pathOfEntryFilesHT.get("/"+parts[parts.length-1])!=null)) { // if doesn't have query and not ends with a file extension		
			entryFiles=new String[pathOfEntryFiles.length*2]; // combinations starting with www and without 
		}
		else {
			entryFiles=new String[2]; // with www and without
		}
		
		// add authority
		StringBuilder prefix1=new StringBuilder();
		prefix1.append(url.getProtocol());
		prefix1.append("://");
		StringBuilder prefix2=new StringBuilder(prefix1.toString());
		if (url.getAuthority().startsWith("www.")) {
			prefix1.append(url.getAuthority());
			prefix2.append(url.getAuthority().substring(4));
		}
		else {
			prefix1.append("www.");
			prefix1.append(url.getAuthority());
			prefix2.append(url.getAuthority());
		}
		
		// add path			
		for (int i=0;i<parts.length;i++) {
			if (!parts[i].equals("") && (i<parts.length-1 || parts[parts.length-1].indexOf('.')==-1 || entryFiles.length==2)) { // ignore last file if it is an entry file
				prefix1.append("/");
				prefix1.append(parts[i]);
				prefix2.append("/");
				prefix2.append(parts[i]);
			}			
		}	
							
		// add file + query
		if (entryFiles.length!=2) {				
			for (int i=0;i<pathOfEntryFiles.length;i++) {
				entryFiles[i*2]=prefix1.toString()+pathOfEntryFiles[i];
				entryFiles[i*2+1]=prefix2.toString()+pathOfEntryFiles[i];
			}
		}
		else {			
			if (urlPath.length()>0 && urlPath.charAt(urlPath.length()-1)=='/') {
				prefix1.append("/");
				prefix2.append("/");
			}
			if (url.getQuery() != null) {
				prefix1.append("?");
				prefix1.append(url.getQuery());
				prefix2.append("?");
				prefix2.append(url.getQuery());
			}
			entryFiles[0]=prefix1.toString();
			entryFiles[1]=prefix2.toString();
		}
		return entryFiles;			
	}
	
	
	/**
	 * Get the radical of this url
	 * @param surl
	 * @return
	 */
	public static String getRadical(String surl) {
		// initialize hashtable
		init();
				
		URL url;
		try {
			url = new URL(surl);
		} 
		catch (MalformedURLException e) {
			return surl;
		}
		/* comment TO DETECT bugs of dumped collections */
		if (surl.startsWith("http:///")) { // url is malformed because it has more than 2 slashes, but it is not detected when calling new URL()
			return surl;
		}		
						
		String urlPath=url.getPath();
		String parts[] = urlPath.split("/");
		boolean isExpand=false;
		if (url.getQuery()==null && (parts.length==0 || parts[parts.length-1].indexOf('.')==-1 || pathOfEntryFilesHT.get("/"+parts[parts.length-1])!=null)) {		
			isExpand=true; 
		}
		
		// add authority
		StringBuilder prefix1=new StringBuilder();
		prefix1.append(url.getProtocol());
		prefix1.append("://");

		if (url.getAuthority().startsWith("www.")) {			
			prefix1.append(url.getAuthority().substring(4));
		}
		else {
			prefix1.append(url.getAuthority());
		}
		
		// add path			
		for (int i=0;i<parts.length;i++) {
			if (!parts[i].equals("") && (i<parts.length-1 || parts[parts.length-1].indexOf('.')==-1 || !isExpand)) { // ignore last file if it is an entry file
				prefix1.append("/");
				prefix1.append(parts[i]);
			}			
		}
				
		// add query
		if (!isExpand) {
			if (urlPath.length()>0 && urlPath.charAt(urlPath.length()-1)=='/') {
				prefix1.append("/");
			}					
			if (url.getQuery()!=null) {
				prefix1.append("?");
				prefix1.append(url.getQuery());
			}
		}
	
		return prefix1.toString();
	}
		
	/**
	 * Expand url to include www. and without www.
	 * @param surl url
	 * @return
	 */
	public static String[] expandwww(String surl) {
		if (surl.startsWith("http://www.")) {
			String newUrl="http://"+surl.substring(11);
			return new String[]{surl,newUrl};
		}
		else if (surl.startsWith("http://")) {
			String newUrl="http://www."+surl.substring(7);
			return new String[]{surl,newUrl};
		}
		else {
			return new String[]{surl};
		}		
	}
	
	/**
	 * Expand url to include www. and without www. and http protocol 
	 * TODO Review method logic, nutchwax doesn't help
	 * @param purl url
	 * @return
	 */
	public static String[] expandhttpAndhttps( String purl ) {
		List< String > urls = new ArrayList< String >( );
		String auxStr = "";
		if( !purl.startsWith("http://") && !purl.startsWith( "https://" ) ) {
			urls.add( "http://".concat( purl ) );
			auxStr = getSlashVersion( "http://".concat( purl )  );
			if( !checkURLinlist( urls , auxStr ) )
				urls.add( auxStr );
			
			if( !purl.startsWith( "www." ) ) {
				urls.add( "http://www.".concat( purl ) );
				auxStr = getSlashVersion( "http://www.".concat( purl ) );
				if( !checkURLinlist( urls , auxStr ) )
					urls.add( auxStr );
			} else {
				urls.add( "http://".concat( purl.replaceFirst( "www." , "" ) ) );
				auxStr = getSlashVersion( "http://".concat( purl.replaceFirst( "www." , "" ) ) );
				if( !checkURLinlist( urls , auxStr ) )
					urls.add( auxStr );
			}
			
			urls.add( "https://".concat( purl ) );
			auxStr = getSlashVersion( "https://".concat( purl )  );
			if( !checkURLinlist( urls , auxStr ) )
				urls.add( auxStr );
			
			if( !purl.startsWith( "www." ) ) {
				urls.add( "https://www.".concat( purl ) );
				auxStr = getSlashVersion(  "https://www.".concat( purl )  );
				if( !checkURLinlist( urls , auxStr ) )
					urls.add( auxStr );
			} else {
				urls.add( "https://".concat( purl.replaceFirst( "www." , "" ) ) );
				auxStr = getSlashVersion(  "https://".concat( purl.replaceFirst( "www." , "" ) ) );
				if( !checkURLinlist( urls , auxStr ) )
					urls.add( auxStr );
			}
			
		} else if( purl.startsWith("http://") && !purl.startsWith( "https://" ) ) { //start with http://
			String urlWithoutHttp = purl.replaceFirst( "http://" , "" ); //without http
			urls.add( purl ); // with http
			auxStr = getSlashVersion(  purl );
			if( !checkURLinlist( urls , auxStr ) )
				urls.add( auxStr );
			
			if( !urlWithoutHttp.startsWith( "www." ) ) {
				urls.add( "http://www.".concat( urlWithoutHttp ) );
				auxStr = getSlashVersion(  "http://www.".concat( urlWithoutHttp ) );
				if( !checkURLinlist( urls , auxStr ) )
					urls.add( auxStr );
			} else {
				urls.add( "http://".concat( urlWithoutHttp.replaceFirst( "www." , "" ) ) );
				auxStr = getSlashVersion(  "http://".concat( urlWithoutHttp.replaceFirst( "www." , "" ) ) );
				if( !checkURLinlist( urls , auxStr ) )
					urls.add( auxStr );
			}
			
			urls.add( "https://".concat( urlWithoutHttp ) );
			auxStr = getSlashVersion( "https://".concat( urlWithoutHttp ) );
			if( !checkURLinlist( urls , auxStr ) )
				urls.add( auxStr );
			
			if( !urlWithoutHttp.startsWith( "www." ) ) {
				urls.add( "https://www.".concat( urlWithoutHttp ) );
				auxStr = getSlashVersion( "https://www.".concat( urlWithoutHttp ) );
				if( !checkURLinlist( urls , auxStr ) )
					urls.add( auxStr );
			} else{
				urls.add( "https://".concat( urlWithoutHttp.replaceFirst( "www." , "" ) ) );
				auxStr = getSlashVersion( "https://".concat( urlWithoutHttp.replaceFirst( "www." , "" ) ) );
				if( !checkURLinlist( urls , auxStr ) )
					urls.add( auxStr );
			}

		} else if( !purl.startsWith("http://") && purl.startsWith( "https://" ) ) { //start with https://
			String urlWithoutHttp = purl.replaceFirst( "https://" , "" ); //without http
			urls.add( purl ); // with http
			auxStr = getSlashVersion( purl );
			if( !checkURLinlist( urls , auxStr ) )
				urls.add( auxStr );
			
			if( !urlWithoutHttp.startsWith( "www." ) ) {
				urls.add( "https://www.".concat( urlWithoutHttp ) );
				auxStr = getSlashVersion( "https://www.".concat( urlWithoutHttp ) );
				if( !checkURLinlist( urls , auxStr ) )
					urls.add( auxStr );
			} else {
				urls.add( "https://".concat( urlWithoutHttp.replaceFirst( "www." , "" ) ) );
				auxStr = getSlashVersion( "https://".concat( urlWithoutHttp.replaceFirst( "www." , "" ) ) );
				if( !checkURLinlist( urls , auxStr ) )
					urls.add( auxStr );
			}
			
			urls.add( "http://".concat( urlWithoutHttp ) );
			auxStr = getSlashVersion( "http://".concat( urlWithoutHttp ) );
			if( !checkURLinlist( urls , auxStr ) )
				urls.add( auxStr );
			if( !urlWithoutHttp.startsWith( "www." ) ) {
				urls.add( "http://www.".concat( urlWithoutHttp ) );
				auxStr = getSlashVersion( "http://www.".concat( urlWithoutHttp ) );
				if( !checkURLinlist( urls , auxStr ) )
					urls.add( auxStr );
			} else {
				urls.add( "http://".concat( urlWithoutHttp.replaceFirst( "www." , "" ) ) );
				auxStr = getSlashVersion( "http://".concat( urlWithoutHttp.replaceFirst( "www." , "" ) ) );
				if( !checkURLinlist( urls , auxStr ) )
					urls.add( auxStr );
			}
		}
		
		String[ ] rURL = new String[ urls.size( ) ];
		rURL = urls.toArray( rURL );
		
		return rURL;
	}
	
	public static String getURLQuery( String surl ) {
		URL url;
		try {
			url = new URL( surl );
			return url.getQuery();
		} 
		catch ( MalformedURLException e ) {
			return "";
		}
		
	}
	
	public static boolean checkURLinlist( List< String > urls , String auxStr ) {
		for(String str: urls) {
		    if(str.trim( ).contains( auxStr ))
		       return true;
		}
		return false;
	}
	
	public static String getSlashVersion( String purl ) {
		if( !purl.endsWith( "/" ) ) {
			return purl.concat( "/" );
		} else {
			return purl.substring( 0, purl.length( ) - 1 );
		}
	}
	
	// TESTS
	public static void main(String[] args) {
		String surl="";
		String files[]=EntryPageExpansion.expand(surl);
		
		for (int i=0;i<files.length;i++) {
			System.out.println(surl+":: "+files[i]+"  ::  "+getRadical(surl));
		}
		surl="1";
		files=EntryPageExpansion.expand(surl);
		for (int i=0;i<files.length;i++) {
			System.out.println(surl+":: "+files[i]+"  ::  "+getRadical(surl));
		}
		surl="http://www.sapo.pt";
		files=EntryPageExpansion.expand(surl);
		for (int i=0;i<files.length;i++) {
			System.out.println(surl+":: "+files[i]+"  ::  "+getRadical(surl));
		}
		surl="http://www.sapo.pt/";
		files=EntryPageExpansion.expand(surl);
		for (int i=0;i<files.length;i++) {
			System.out.println(surl+":: "+files[i]+"  ::  "+getRadical(surl));
		}
		surl="http://www.sapo.pt/index.jsp";
		files=EntryPageExpansion.expand(surl);
		for (int i=0;i<files.length;i++) {
			System.out.println(surl+":: "+files[i]+"  ::  "+getRadical(surl));
		}	
		surl="http://www.sapo.pt/gertrudes";
		files=EntryPageExpansion.expand(surl);
		for (int i=0;i<files.length;i++) {
			System.out.println(surl+":: "+files[i]+"  ::  "+getRadical(surl));
		}
		surl="http://www.sapo.pt/index";
		files=EntryPageExpansion.expand(surl);
		for (int i=0;i<files.length;i++) {
			System.out.println(surl+":: "+files[i]+"  ::  "+getRadical(surl));
		}
		
		surl="http://sapo.pt";
		files=EntryPageExpansion.expand(surl);
		for (int i=0;i<files.length;i++) {
			System.out.println(surl+":: "+files[i]+"  ::  "+getRadical(surl));
		}
		
		surl="http://sapo.pt/xpto";
		files=EntryPageExpansion.expand(surl);
		for (int i=0;i<files.length;i++) {
			System.out.println(surl+":: "+files[i]+"  ::  "+getRadical(surl));
		}
		surl="http://sapo.pt/xpto/index.php";
		files=EntryPageExpansion.expand(surl);
		for (int i=0;i<files.length;i++) {
			System.out.println(surl+":: "+files[i]+"  ::  "+getRadical(surl));
		}
		surl="http://sapo.pt/xpto/xpto.htm";
		files=EntryPageExpansion.expand(surl);
		for (int i=0;i<files.length;i++) {
			System.out.println(surl+":: "+files[i]+"  ::  "+getRadical(surl));
		}
		surl="http://sapo.pt/xpto.htm";
		files=EntryPageExpansion.expand(surl);
		for (int i=0;i<files.length;i++) {
			System.out.println(surl+":: "+files[i]+"  ::  "+getRadical(surl));
		}
		surl="http://sapo.pt/index.htm?x=123";
		files=EntryPageExpansion.expand(surl);
		for (int i=0;i<files.length;i++) {
			System.out.println(surl+":: "+files[i]+"  ::  "+getRadical(surl));
		}		
		
		surl="http://www.di.fc.ul.pt/sobre/?colaborador&value=27";
		files=EntryPageExpansion.expand(surl);
		for (int i=0;i<files.length;i++) {
			System.out.println(surl+":: "+files[i]+"  ::  "+getRadical(surl));
		}
		surl="http://www.di.fc.ul.pt/sobre/";
		files=EntryPageExpansion.expand(surl);
		for (int i=0;i<files.length;i++) {
			System.out.println(surl+":: "+files[i]+"  ::  "+getRadical(surl));
		}
		
		surl="http://0000.weblog.com.pt/atom.xml";
		files=EntryPageExpansion.expand(surl);
		for (int i=0;i<files.length;i++) {
			System.out.println(surl+":: "+files[i]+"  ::  "+getRadical(surl));
		}
		surl="http://0000.weblog.com.pt/atom.xml?start=0&end=10";
		files=EntryPageExpansion.expand(surl);
		for (int i=0;i<files.length;i++) {
			System.out.println(surl+":: "+files[i]+"  ::  "+getRadical(surl));
		}
		surl="http://www.0000.weblog.com.pt/atom.xml?start=0&end=10";
		files=EntryPageExpansion.expand(surl);
		for (int i=0;i<files.length;i++) {
			System.out.println(surl+":: "+files[i]+"  ::  "+getRadical(surl));
		}
		
			
		
		surl="http://www.lip.pt/index.php";
		files=EntryPageExpansion.expand(surl);
		for (int i=0;i<files.length;i++) {
			System.out.println(surl+":: "+files[i]+"  ::  "+getRadical(surl));
		}
		
		
		surl="http://www.sap.pt/index.php";
		files=EntryPageExpansion.expandwww(surl);
		for (int i=0;i<files.length;i++) {
			System.out.println(surl+":: "+files[i]);
		}

		surl="http://sap.pt/index.php";
		files=EntryPageExpansion.expandwww(surl);
		for (int i=0;i<files.length;i++) {
			System.out.println(surl+":: "+files[i]);
		}
		
		surl="http://www.pedroveiga.nome.pt/formacao.htm";
		files=EntryPageExpansion.expandwww(surl);
		for (int i=0;i<files.length;i++) {
			System.out.println(surl+":: "+files[i]);
		}
		
		surl="http://pedroveiga.nome.pt/formacao.htm";
		files=EntryPageExpansion.expandwww(surl);
		for (int i=0;i<files.length;i++) {
			System.out.println(surl+":: "+files[i]);
		}
		
		surl="ftp://benfica.pt/formacao.htm";
		files=EntryPageExpansion.expandwww(surl);
		for (int i=0;i<files.length;i++) {
			System.out.println(surl+":: "+files[i]);
		}
	
	
		
		/* testing cache file */
		/*
		BufferedReader br;
		String parts[] = null;
		try {
			br = new BufferedReader( new FileReader(new File("/shareT2/sfontes/versions.cache")) );
			String line;					

			while ( ( line = br.readLine() ) != null ) {
				parts = line.split( "\\s" );
		
				//System.out.println(parts[0]+" ");
				//System.out.println(getRadical(parts[0]));
				try {
					getRadical(parts[0]);
				}
				catch (NullPointerException e) {
					System.out.println(parts[0]+" ");
				}
			}
			br.close();
		} 
		catch (IOException e) {					
			e.printStackTrace();
		}		
		*/
	}
	
}
