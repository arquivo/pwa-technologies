package org.apache.lucene.search.memcached;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.BinaryConnectionFactory;
import net.spy.memcached.MemcachedClient;

import java.io.IOException;
import java.util.concurrent.TimeUnit;


/**
 * Memcached client
 * @author Miguel Costa
 */
public class Memcached {
			
	private MemcachedClient client = null;
		
	
	/**
	 * 
	 * @param addresses
	 * @throws IOException 
	 */
	public Memcached(String addresses) throws IOException {				
		client = new MemcachedClient(new BinaryConnectionFactory(), AddrUtil.getAddresses(addresses));		
	              //AddrUtil.getAddresses("server1:11211 server2:11211"));	    	    
	    System.out.println("Connected to servers at "+addresses);
	}
	
	/**
	 * Close connection
	 */
	public void close() {
		client.shutdown(10, TimeUnit.SECONDS);
	}
	
	/**
	 * Set object in cache with an expiration timeout
	 * @param key key
	 * @param exp timeout
	 * @param o object
	 */
	public void set(String key, int exp, final Object o) {			
		client.set(key, exp, o);
	}
	
	/**
	 * Set object in cache without an expiration timeout
	 * @param key key
	 * @param exp timeout
	 * @param o object
	 */
	public void set(String key, final Object o) {			
		client.set(key, 0, o);
	}

	/**
	 * Add object in cache with an expiration timeout iff it does not exist already
	 * @param key key
	 * @param exp timeout
	 * @param o object
	 */
	public void add(String key, int exp, final Object o) {			
		client.add(key, exp, o);
	}
	
	/**
	 * Add object in cache without an expiration timeout iff it does not exist already
	 * @param key key
	 * @param exp timeout
	 * @param o object
	 */
	public void add(String key, final Object o) {			
		client.add(key, 0, o);
	}

	/**
	 * Add UrlRow in cache without an expiration timeout iff it does not exist already
	 * @param key key
	 * @param exp timeout
	 * @param o object
	 */
	public void addRow(String key, final UrlRow o) {		
	    int rowArr[]={o.getNVersions(),o.getMin(),o.getMax()};	
		client.add(key, 0, rowArr);
	}

	/**
	 * Replace object in cache with an expiration timeout 
	 * @param key key
	 * @param exp timeout
	 * @param o object
	 */
	public void replace(String key, int exp, final Object o) {			
		client.replace(key, exp, o);
	}
	
	/**
	 * Replace object in cache without an expiration timeout
	 * @param key key
	 * @param exp timeout
	 * @param o object
	 */
	public void replace(String key, final Object o) {			
		client.replace(key, 0, o);
	}

	/**
	 * Replace UrlRow in cache without an expiration timeout
	 * @param key key
	 * @param exp timeout
	 * @param o object
	 */
	public void replaceRow(String key, final UrlRow o) {			 
	    int rowArr[]={o.getNVersions(),o.getMin(),o.getMax()};
		client.replace(key, 0, rowArr);
	}

	/**
	 * 
	 * @param key key
	 * @return object
	 */
	public Object get(String key) {
		return client.get(key);	    	
	}

	/**
	 * 
	 * @param key key
	 * @return UrlRow
	 */
	public UrlRow getRow(String key) {
	    int rowArr[]=(int[])client.get(key);
		if (rowArr==null) {
		    return null;
		}
		return new UrlRow(rowArr[0],rowArr[1],rowArr[2]);
	}

	/**
	 * 
	 * @param key
	 * @return
	 */
	public Object delete(String key) {
		return client.delete(key);
	}	
}



