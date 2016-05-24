package org.archive.access.nutch.jobs;

import java.lang.Thread;

import org.apache.nutch.parse.*;
import org.apache.nutch.protocol.Content;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Thread to parse documents, but with a timeout
 * @author Miguel Costa
 *
 */
public class TimeoutParsingThread extends Thread {
	
	private final Log LOG = LogFactory.getLog(TimeoutParsingThread.class);
	private final Integer lock = 999; // TODO: don't use global objects, since they can be shared by the compiler; use unique objects
		
	private boolean done; // if thread should finish 	
	private long id; // thread id
	private String url; // url been parsed
	private boolean isWaiting; // indicates if thread is waiting
	private boolean isSignalled2Start;
	private boolean isSignalled2Finish; 
	private Parse parse;
	private ParseStatus parseStatus;
	private ParseUtil parseUtil;
	private Content content;
	private int timeoutIndexingDocument;
	
	
	/**
	 * Constructor
	 * @param id thread id
	 * @param timeoutIndexingDocument maximum time to index a document
	 */
	public TimeoutParsingThread(long id, int timeoutIndexingDocument) {
		super();
		this.setDaemon(true); // necessary to finish with an interrupt	
		this.id=id;
		this.timeoutIndexingDocument=timeoutIndexingDocument;
		this.url=null;		
		this.done=false;
		this.isWaiting=false;	
		this.isSignalled2Start=false;
		this.isSignalled2Finish=false;
		this.parse=null;
		this.parseStatus=null;
		this.parseUtil=null;
		this.content=null;		
		
		LOG.info("Thread created for Mapper with id "+id);
	}
	
	/**
	 * 
	 */
	public void run() {				
		
   	    while (!done) {    	    	
   	    	synchronized(lock) {
   	    		isWaiting=true;
   	    		while (!isSignalled2Start){ // to avoid spurious (false) wakeups 
   	    			try {   	         	      	    		
   	    				lock.wait(); // wait until notified
   	    			}            
   	    			catch (InterruptedException ignored) {
   	    				isSignalled2Start=true;
   	    			}
   	    		}
   	    		isSignalled2Start=false;
   	    		isWaiting=false;
   	    	}
   	    	   	    	
   	    	if (!done) {   	    		
   	    		parsing();   	    		
   	    		if (!done) {   	    		
   	    			synchronized(lock) {
   	    				isSignalled2Finish=true;
   	    				lock.notify(); // indicate the end of parsing   	    	   	    		   	    
   	    			}
   	    		}   	    		   	    
   	    	}   	    	   	    	    
   	    }   	       	
	}
	
	/** 
	 * Parsing
	 */
	private void parsing() {
   		try {
   			parse = parseUtil.parse(content);     
   			parseStatus = parse.getData().getStatus();   	    			
   		}   
   		catch (Exception e) { // InterruptedException also caught here
   			parse = null;
   			parseStatus = new ParseStatus(e);		    
   		}
   		catch (Error e) { 
   			parse = null;
   			parseStatus = new ParseStatus(e);		
   			LOG.warn("Thread error for Mapper with id "+id+" and url "+url+". Error "+e.getMessage());
   		}
	}
	
	/**
	 * Wake up thread
	 */
	public void wakeupAndWait() {

		while (!isWaiting) {			
			try {
				Thread.sleep(100); // wait for the other thread 
			} 
			catch (InterruptedException ignored) {	
				return;
			}
		}
				
		long startWaiting=0;
		long totalTime=0;
		synchronized(lock) {						
			// wakeup		
			isSignalled2Start=true;
			lock.notify();			
			
			// wait until notification, interrupt or timeout
			startWaiting=System.currentTimeMillis();
			while (!isSignalled2Finish && totalTime<timeoutIndexingDocument) {
				try {				
					lock.wait(timeoutIndexingDocument-totalTime);
				}            
				catch (InterruptedException ignored) {	  
					isSignalled2Finish=true;
				}	  
	    	   	totalTime=System.currentTimeMillis()-startWaiting;
			}	    	
	    	isSignalled2Finish=false;	    	
		}					
				
		if (totalTime>=timeoutIndexingDocument) { // parsing likely hung; if parsing is ok then results are used and only the thread is restarted
			close(totalTime);
		}
	}
	
	/**
	 * Close thread
	 * @param totalTime total time elapsed
	 */
	public void close(long totalTime) {	
		done=true;
		this.interrupt();
		
		if (totalTime>=timeoutIndexingDocument) {
			LOG.warn("Thread closed for Mapper with id "+id+" and url "+url+". It took "+totalTime+" millisec. Parsing likely hung.");
		}
		else {
			LOG.info("Thread closed for Mapper with id "+id);
		}
	}
		
	
	/**
	 * Set url 
	 */
	public void setUrl(String url) {
		this.url=url;
	}
	
	/**
	 * Set parse util
	 */	
	public void setParseUtil(ParseUtil parseUtil) {
		this.parseUtil=parseUtil;		
	}
	
	/**
	 * Set parse content
	 */	
	public void setContent(Content content) {
		this.content=content;	
	}
	
	/**
	 * Indicates if thread should be killed
	 */	
	public boolean isToKill() {
		return done;		
	}
	
	/**
	 * Return parse
	 */	
	public Parse getParse() {
		return parse;		
	}	
	
	/**
	 * Return parse status
	 */	
	public ParseStatus getParseStatus() {
		return parseStatus;		
	}	
}
