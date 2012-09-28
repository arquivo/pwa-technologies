package pt.arquivo.logs.arquivo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * Uses Rembrant (xldb.fc.ul.pt/wiki/Rembrandt) to count the percentage of query logs with temporal expressions
 * @author Miguel Costa
 *
 * @note test: curl -d "lg=pt&slg=pt&api_key=db924ad035a9523bcf92358fcb2329dac923bf9c&db=era+uma+vez" http://xldb.di.fc.ul.pt/Rembrandt/api/rembrandt
 */
public class RembrantClient {
	
	private static final String REMBRANT_URL="http://xldb.di.fc.ul.pt/Rembrandt/api/rembrandt";
	private static final String STATIC_PARAMETERS="lg=pt&slg=pt&api_key=db924ad035a9523bcf92358fcb2329dac923bf9c";
	private static final String[] TEMPORAL_TYPES={"DATA","DATE","HOUR","INTERVAL","DURATION","TEMPO","TEMPO_CALEND"};
	private static final String YEAR_PATTERN="(^|.*\\D)[0-9]{4}(\\D.*|$)";			
	private static final String MONTH_PATTERN="(^|.*[^a-z])(janeiro|fevereiro|mar\\p{L}o|abril|maio|junho|julho|agosto|setembro|outubro|novembro|dezembro)([^a-z].*|$)";		           private static final String DAY_PATTERN="(^|.*\\D)[0-9]{2}(\\D.*|$)";			

	
	/**
	 * Post request to Rembrant
	 * @param url
	 * @return response code
	 * @throws IOException
	 */
	public StringBuffer postRequest(String url, String request) throws IOException {			
		// Construct data
		String data = STATIC_PARAMETERS + "&db=" + URLEncoder.encode(request, "UTF-8");			 

		// Send data
		URL urlService = new URL(url);
		URLConnection conn = urlService.openConnection();
		conn.setDoOutput(true);
		OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
		wr.write(data);
		wr.flush();

		// Get the response
		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String line;
		StringBuffer buf=new StringBuffer(); 
		while ((line = rd.readLine()) != null) {
			buf.append(line);
		}
		wr.close();
		rd.close();
		
		return buf;
	}

	/**
	 * 
	 */
	public void analyze(String queriesFile) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(new File(queriesFile)));
		String line;
	
		while ( ( line = br.readLine() ) != null ) {				
			String parts[] = line.split( "\\s" );			
	
			// format: query "tv antena 10" = 1
			StringBuffer query=new StringBuffer();
			for (int i=1; i<parts.length-2; i++) {
				query.append(parts[i]);
				query.append(" ");
	        }
			int nQueries=Integer.parseInt(parts[parts.length-1]);
			
			// get NER response
			/*
			StringBuffer response=postRequest(REMBRANT_URL,query.toString());
			boolean isTemporal=false;
			for (int i=0;!isTemporal && i<TEMPORAL_TYPES.length;i++) {
				if (response.indexOf(TEMPORAL_TYPES[i])!=-1) {
					isTemporal=true;
				}				
			}
				
			if (isTemporal) {
				System.out.println(nQueries+" "+query+" TEMPORAL");
			}
			else 
			*/

			if (query.indexOf("http://")!=-1 || query.indexOf("www.")!=-1) {
			    System.out.println(nQueries+" "+query+" NORMAL");
			}
			else if (query.toString().matches(YEAR_PATTERN)) {
				System.out.println(nQueries+" "+query+" TEMPORAL-YEAR");
			}
			else if (query.toString().matches(MONTH_PATTERN)) {
				System.out.println(nQueries+" "+query+" TEMPORAL-MONTH");
			}
			else if (query.toString().matches(DAY_PATTERN)) {
				System.out.println(nQueries+" "+query+" TEMPORAL-DAY");
			}
			else {
				System.out.println(nQueries+" "+query+" NORMAL");
			}
		}			
		br.close();
	}
	
	/**
	 * Main
	 * @param args arguments
	 */
	public static void main(String[] args) throws Exception {							
		String usage="usage: -f [queries file] OR -e expression";
				
		if (args.length>0 && args[0].equals("-f")) {
			RembrantClient client=new RembrantClient();
			client.analyze(args[1]);
		}
		else if (args.length>0 && args[0].equals("-e")) {
			StringBuffer query=new StringBuffer(); 
			for (String s: args) {
	            query.append(s);
	            query.append(" ");
	        }			
			RembrantClient client=new RembrantClient();
			StringBuffer response=client.postRequest(REMBRANT_URL,query.toString());
			System.out.println(response);
		}
		else {
			System.out.println(usage);
			System.exit(0);
		}						
	}

}

