package pt.arquivo.logs.tumba;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


/**
 * Create file for tag cloud generator
 * @author Miguel Costa
 * 
 * usage: java -classpath ../target/classes/ pt.arquivo.logs.tumba.TagCloudSetup tumba_log2004.stats
 */
public class TagCloudSetup {

	
	/**
	 * Create file for tag cloud generator
	 * @param logfile log file
	 * @throws IOException 
	 */
	public static void create(String logfile) throws IOException {									
		String line;			
		BufferedReader br = new BufferedReader( new FileReader(logfile) );

		while ( ( line = br.readLine() ) != null ) {	
											
			String parts[]=line.split("\\s");
			int times=Integer.parseInt(parts[parts.length-1]);
			if (times<10) {
				return;
			}
			for (int k=0;k<times;k++) {
				for (int i=1;i<parts.length-2;i++) {
					if (i>1) {
						System.out.print("~");
					}
					System.out.print(parts[i]);	
				}
				System.out.println();
			}			
		}
		br.close();
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {											
		String  errorMsg="arguments: <stats file>";
		if (args.length!=1) {
			System.err.println(errorMsg);			
			return;
		}

		try {
			TagCloudSetup.create(args[0]);
		} 
		catch (IOException e) {			
			e.printStackTrace();
		}		
	}
	
}
