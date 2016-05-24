package org.archive.wayback.util;

import java.io.UnsupportedEncodingException;
import java.util.Hashtable;


/**
 * Changes the URL escape characters
 * @author Miguel Costa
 *
 */
public class EscapeDecoder {

	private final static String URL_ESCAPE[][]= {{";","%3B"},{"?","%3F"},{"/","%2F"},{":","%3A"},{"#","%23"},
		{"&","%26"},{"=","%3D"},{"+","%2B"},{"$","%24"},{",","%2C"},	
		{"%20","%20"},{"%","%25"},{"<","%3C"},{">","%3E"},{"~","%7E"}, // the "%20" code is not converted, neither the "+"
		{",","%7B"},{"}","%7D"},{"|","%7C"},{"\\","%5C"},{"^","%5E"},
		{"'","%60"},{"[","%5B"},{"]","%5D"},{"@","%40"}};
		
	private static Hashtable<String,String> hUrlEscape=null;
	
	
	/**
	 * Convert the url escape codes by their characters 
	 * @param s url string
	 * @return url converted
	 */
	public static String urlUnescape(String s) {
	    StringBuffer sbuf=new StringBuffer();
	    StringBuffer sbufAux=null;
	    String saux=null;
	    int length=s.length() ;
	    
	    if (hUrlEscape==null) { // initialize
	    	hUrlEscape=new Hashtable<String,String>();
	    	for (int i=0;i<URL_ESCAPE.length;i++) {
	    		hUrlEscape.put(URL_ESCAPE[i][1], URL_ESCAPE[i][0]);
	    	}
	    }
	    
	    for (int i=0;i<length;i++) {
	    	switch (s.charAt(i)) {
	    	case '%':
	    		if (i+2<length) {
	    			sbufAux=new StringBuffer(3);
	    			sbufAux.append(s.charAt(i));
	    			sbufAux.append(s.charAt(i+1));
	    			sbufAux.append(s.charAt(i+2));
	    			if ((saux=hUrlEscape.get(sbufAux.toString()))!=null) {
	    				sbuf.append(saux);
	    				i+=2;
	    			}
	    			else {
	    				sbuf.append(s.charAt(i));	    				
	    			}
	    		}
	    		else {
	    			sbuf.append(s.charAt(i));
	    		}
	    		break;
	    	case '+':
	    		sbuf.append("+");	  		  	
	  		  	break ;
	  		default:	
	  			sbuf.append(s.charAt(i));	  		  	
  		  		break ;
	    	}
	    }
	    
	    return sbuf.toString();
	}
	
	/**
	 * Convert the url uf8 characters displayed in ISO8859-1, to UTF8 escape codes 
	 * @param s url string
	 * @return url converted
	 * @throws UnsupportedEncodingException 
	 */
	public static String urlUtf8Escape(String s) {
		String sconverted=null;
		try {
			sconverted=new String(s.getBytes("ISO8859-1"),"UTF8");
		} 
		catch (UnsupportedEncodingException e1) {
			return s;
		}
		StringBuffer sbuf=new StringBuffer();
		
		for (int i=0;i<sconverted.length();i++) {
			if (s.charAt(i)==sconverted.charAt(i)) {
				sbuf.append(sconverted.charAt(i));
			}
			else {
				try {
					sbuf.append(java.net.URLEncoder.encode(""+sconverted.charAt(i), "UTF-8"));					
				} 
				catch (UnsupportedEncodingException e) {
					return s;
				}											
			}
		}
		
		return sbuf.toString();
	}
}
