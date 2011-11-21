package org.apache.nutch.global;

/**
 * Contains the parameter names set in the configuration file at $NUTCH_HOME/conf/wax-default.xml
 * @author Miguel Costa
 */
public class Global {
	 
	public static final String COLLECTION_TYPE="collection.type";
	public static final String COLLECTION_TYPE_MULTIPLE="multiple";
	public static final String COLLECTION_TYPE_NORMAL="normal";
	public static final String COLLECTION_TYPE_TREC="trec";

	public static final String DATABASE_CONNECTION="database.conection";
	public static final String DATABASE_USERNAME="database.username";
	public static final String DATABASE_PASSWORD="database.password";
	
	public static final String NUMBER_HANDLERS="number.handlers";
	
	public static final String IPC_VERBOSE="ipc.verbose";
	
	public static final String TIMEOUT_INDEX_SERVERS_ALIVE="timeout.index.servers.alive";
	public static final String TIMEOUT_INDEX_SERVERS_RESPONSE="timeout.index.servers.response";
	public static final String TIMEOUT_INDEXING_DOCUMENT="timeout.indexing.document";	
		
	public static final String MAX_FULLTEXT_MATCHES_RETURNED="max.fulltext.matches.returned";
	public static final String MAX_FULLTEXT_MATCHES_RANKED="max.fulltext.matches.ranked";
	
	public static final String MAX_QUERY_TERMS="max.query.terms";
	public static final String MAX_QUERY_EXTRA_TERMS="max.query.extra.terms";
	
	public static final String RANKING_FUNCTIONS="ranking.functions";	
}
