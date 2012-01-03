package pt.arquivo.processor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import org.archive.io.ArchiveRecordHeader;
import org.archive.mapred.ARCReporter;
import org.archive.access.nutch.NutchwaxConfiguration;
import pt.arquivo.processor.ProcessArcs.Counter;


/**
 * Implementation class
 */
public class ProcessFile extends ProcessArcs {

	//private static final String HTMLCONTENTTYPE = "text/html";
	
	public ProcessFile(){
		super();
	}
	
	
	/**
	 * Processment for each file
	 * @param in content of the file
	 * @param out output to return
	 * @param err errors to return
	 * @param header metadata of the file
	 * @param reporter to reporte statistics
	 */
	@Override
	protected void processor(InputStream in, OutputStream out, OutputStream err, ArchiveRecordHeader header, ARCReporter reporter) {
		// TODO This method should be implemented 
		// This code serves has a small example
		//String url = header.getUrl();
		
		//reporter.incrCounter(Counter.HTMLFILES, 1);
		//try{
		//	reporter.setStatusIfElapse("sending to stats: " + url);
		//}catch (IOException e){
		//	e.printStackTrace((PrintStream) out);
		//}
        
	}
	
	/**
	 * Filter for files
	 * @param mimetype mimetype of the file
	 */
	@Override
	protected boolean filter(String mimetype) {
		// TODO This method should be implemented 
		// This code serves has a small example
        //if (mimetype.compareToIgnoreCase(HTMLCONTENTTYPE) == 0)
        	return true;
        //else
        //	return false;
	}
	
	/**
	 * Main
	 * @param args arguments
	 */
	public static void main(String[] args) throws Exception {		
		int res = new ProcessFile().doMain(NutchwaxConfiguration.getConfiguration(), args);
		System.exit(res);
	}
}
