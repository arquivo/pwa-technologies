package org.apache.access.nutch.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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

		while ( ( line = br.readLine() ) != null ) {				
			parts = line.split(TOKEN);
		
			/*
			if (parts.length!=nFields) { 
				throw new IOException("ERROR: wrong line:"+line);
				System.out.println("Possible wrong line:"+line);
			}	
			*/
			
			sbuf=new StringBuffer(500);
			surl=new StringBuffer(500);			
			try {
				for (int i=0;i<parts.length;i++) {
										
					if (i==0) { // check data
						
					}
					
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
						}
					}
					else {
						if (i==0) {							
							// check date
							String sdate=parts[0].substring(1,parts[0].length()-1);						
							SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");	
							try {
								Date date = sdf.parse(sdate); // throw ParseException
							}
							catch (ParseException e) {				
								sdate+="00"; // for errors such as these "2000-11-19 16:28:"
								Date date = sdf.parse(sdate); // throw ParseException							
							}								
							if (sdate.indexOf("-00")!=-1) { // for errors such as these - date/time field value out of range: "2000-00-10 01:23:52"
								throw new ParseException("Wrong date in this version: "+line, 0);
							}
							
							sbuf.append("\""+sdate+"\"");
						}
						else {						
							sbuf.append(TOKEN);
							sbuf.append(parts[i]);
						}						
					}				
				}	

				pw.println(sbuf);
			}
			catch (MalformedURLException e) {
				System.out.println("MalformedURLException for url "+surl.substring(1,surl.length()-1));
			}					
			catch (ParseException e) {				
				System.out.println("Wrong date in this version: "+line);
			}		
		}
		br.close();
		pw.close();
		return 0;	
	}

}
