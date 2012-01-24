package org.apache.lucene.search.memcached;

import net.spy.memcached.BinaryConnectionFactory;

/**
 * PwaBinaryConnectionFactory class extends BinaryConnectionFactory to increase the operation timeout
 * @author Miguel Costa
 */
public class PwaBinaryConnectionFactory extends BinaryConnectionFactory {

	/**
     * Default operation timeout in milliseconds.
     */
    public static final long DEFAULT_OPERATION_TIMEOUT = 5000;
}
