package org.archive.access.nutch.jobs;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 * Thread pool
 * @author Miguel Costa
 */
public class TimeoutParsingThreadPool {
			 
	private HashMap<Long,TimeoutParsingThread> mapThreads=new HashMap<Long,TimeoutParsingThread>(); // each map key is associated with a parsing thread
	

	/**
	 * Constructor
	 */
    public TimeoutParsingThreadPool() {    
    }

    /**
     * Return thread with id @id
     * @param id thread id
     * @param timeoutIndexingDocument maximum time to index a document
     * @return
     */
    public TimeoutParsingThread getThread(long id, int timeoutIndexingDocument) {
    	TimeoutParsingThread thread=null;
    	
    	synchronized(this) {    	
    		if ((thread=mapThreads.get(id))!=null && thread.isAlive() && !thread.isToKill()) {    			
    			return thread;
    		}
    		if (thread!=null && thread.isToKill()) {
    			thread=null;
    		}    		
    	
    		thread=new TimeoutParsingThread(id,timeoutIndexingDocument);
    		thread.start();
    		mapThreads.put(id,thread);
    	}    	
    	return thread;
    }
    
    /**
     * Close all threads alive
     */
    public void closeAll() {
    	Set set=mapThreads.entrySet();
        Iterator it = set.iterator();
    	while (it.hasNext()) {
    		Map.Entry<Long,TimeoutParsingThread> entry = (Map.Entry)it.next();    		
    		TimeoutParsingThread thread = entry.getValue();
    		thread.close(0);
    		thread=null;
    	}
    	mapThreads=new HashMap<Long,TimeoutParsingThread>();
    }
    
    /**
     * Close all threads alive
     * @param id thread id
     */
    public void closeThread(long id) {
    	TimeoutParsingThread thread=mapThreads.get(id);
    	if (thread==null) {
    		return;
    	}
    	
    	thread.close(0);
		thread=null;
		mapThreads.remove(id);		
    }
}
