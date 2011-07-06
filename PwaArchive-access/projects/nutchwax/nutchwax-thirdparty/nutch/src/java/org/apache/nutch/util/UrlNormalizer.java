package org.apache.access.nutch.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.*;

import org.apache.hadoop.util.ToolBase;
import org.apache.nutch.net.URLNormalizers;
import org.apache.nutch.util.NutchConfiguration;


/**
 * Normalize URLs using nutchwax normalizers
 * @author Miguel Costa 
 */
public class UrlNormalizer extends ToolBase {

	private final static String TOKEN=",";
	private static URLNormalizers urlNormalizers;	
	
	
	public UrlNormalizer() {		
	}
	
	public String normalize(String url) throws IOException {
		if (urlNormalizers==null) {
			urlNormalizers=new URLNormalizers(getConf(),URLNormalizers.SCOPE_FETCHER);
		}
		return urlNormalizers.normalize(url,URLNormalizers.SCOPE_FETCHER);
	}
			
	public static void main(String[] args) throws Exception {
		if (args.length!=4) {
		    System.out.println("arguments: <filename input> <filename output> <number fields> <index of field to normalize>");
		    return;
		}
		
		int res = new UrlNormalizer().doMain(NutchConfiguration.create(),args);
		System.exit(res);
	}
	  
	public int run(String[] args) throws Exception {
	    							
		int nFields=Integer.parseInt(args[2]);
		int indexField=Integer.parseInt(args[3]);
		
		System.out.println("Start normalizing "+args[0]+" file.");
				
		BufferedReader br = new BufferedReader(new FileReader(args[0]) );
		PrintWriter pw=new PrintWriter(new File(args[1]));
		String line;	
		String parts[]=null;
		StringBuffer sbuf=null;
		StringBuffer surl=null;
//		StringBuffer lastKey=null; // to eliminate duplicated keys: date + url normalized
//		StringBuffer key=null;
		while ( ( line = br.readLine() ) != null ) {				
			parts = line.split(TOKEN);
		
			if (parts.length!=nFields) { 
				//throw new IOException("ERROR: wrong line:"+line);
				//System.out.println("Possible wrong line:"+line);
			}	
			
//			key=new StringBuffer(500);
			sbuf=new StringBuffer(500);
			surl=new StringBuffer(500);
			try {
				for (int i=0;i<parts.length;i++) {
					if (i>=indexField && i<=parts.length-nFields+indexField) {
						if (i!=0) { 
							if (i==indexField) {
								sbuf.append(TOKEN);
							}
							else {
								surl.append(TOKEN);
							}
						}
						surl.append(parts[i]);
						if (i==parts.length-nFields+indexField) {
							sbuf.append("\""+normalize(surl.substring(1,surl.length()-1))+"\"");
//							key.append("\""+normalize(surl.substring(1,surl.length()-1))+"\""); // append url normalized
						}
					}
					else {
						if (i!=0) { 
							sbuf.append(TOKEN);						
						}
						else {
//							key.append(parts[i]).append(TOKEN); // append time
						}
						sbuf.append(parts[i]);
					}					
				}	
//				if (lastKey==null || !key.toString().equals(lastKey.toString())) {
				pw.println(sbuf);
//				}
//				lastKey=key;
			}
			catch (MalformedURLException e) {
				System.out.println("MalformedURLException for url "+surl.substring(1,surl.length()-1));
			}																				
		}
		br.close();
		pw.close();
		return 0;	
	}

}
