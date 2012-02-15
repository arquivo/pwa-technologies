package pt.arquivo.logs.arquivo;

import java.net.*;


/**
 * Gives a value according to the URL number of slashes
 * @author Miguel Costa
 */
public class UrlDepth {
	
	/**
	 * Constructor
	 * @param surl URL
	 * @return 0 if domain, 1 if domain plus one directory, 2 if domain plus more than one directory, 3 if file, 4 if not a valid URL
	 */
	public static int getUrlDepth(String surl) {
		if (!surl.startsWith("http://") && !surl.startsWith("https://") && !surl.startsWith("ftp://")) {
			surl="http://"+surl;
		}
		
		try {
			URL url=new URL(surl);
		} 
		catch (MalformedURLException e) {
			return 4;
		}
		String urlParts[]=surl.split("/");			
		int c=2; // http + '' 
		if (urlParts.length==1+c) { // domain			
			return 0;
		}	
		if (urlParts[urlParts.length-1].indexOf(".")!=-1) { // file
			return 3;			
		}
		if (urlParts.length==2+c) { // domain plus one directory
			return 1;			
		}
		return 2; // domain plus more than one directory					 		
	}
		
}

