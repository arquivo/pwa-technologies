package org.archive.access.nutch.jobs.sql;

import java.sql.*;
import java.text.*;


/**
 * SQL operation to create virtual snapshots
 * @author Miguel Costa
 */
public class SqlSearcher {
	  
	  private final  SimpleDateFormat formatterTimestampToString = new SimpleDateFormat("yyyyMMdd");
	  private final  String SQL_QUERY=
		"select abs(extract(epoch FROM date-?::timestamp)) as duration, date " +
	    "from files " +
	    "where url=? " +
	    "order by duration asc " +
	    "offset 0 limit 1";
	  	  	 
	  private Connection        db;        // A connection to the database
	  private DatabaseMetaData  dbmd;      // This is basically info the driver delivers
	                              // about the DB it just connected to. I use
	                              // it to get the DB version to confirm the
	  private PreparedStatement ps;
	  
	  /**
	   * Connect to database
	   * @param database database string
	   * @param username username
	   * @param password password
	   * @throws ClassNotFoundException
	   * @throws SQLException
	   */
	  public void connect(String database, String username, String password) throws ClassNotFoundException, SQLException {
		  Class.forName("org.postgresql.Driver"); //load the driver
		  db = DriverManager.getConnection("jdbc:postgresql:"+database,
		                                     username,
		                                     password); //connect to the db
		  dbmd = db.getMetaData(); //get MetaData to confirm connection
		  System.out.println("Connection to "+dbmd.getDatabaseProductName()+" "+
		                       dbmd.getDatabaseProductVersion()+" successful.\n");
		  
		  ps = db.prepareStatement(SQL_QUERY);
	  }
	  
	  /**
	   * Close connection to database
	   * @throws SQLException
	   */
	  public void close() throws SQLException {
		  ps.close();
		  db.close();
	  }
	  
	  /**
	   * Select near timestamp
	   * @param url url from outlink document
	   * @param timestamp timestamp with 14 digits format, such as "20070831100222"
	   * @throws SQLException
	   */
	  public String selectNearTimestamp(String url, String timestamp) throws SQLException {		  		 		 
		  ps.setString(1,convertTimestampString(timestamp));
		  ps.setString(2,url);
		  ResultSet results = ps.executeQuery();		  
		  Timestamp restimestamp=null;
		  if (results!=null) {
			  if (results.next()) {
				  restimestamp=results.getTimestamp(2);
			  }
			  results.close();  		      
		  } 		  			  							  
		  if (restimestamp==null) {
			  return null;
		  }
		  return convertTimestamp(restimestamp);		 
	  }
	      	  	
	  /**
	   * Build new collection name with collection plus timestamp
	   * @param collection collection name
	   * @param timestamp timestamp in format "20070831100222"
	   * @return
	   */	  
	  public static String getCollectionNameWithTimestamp(String collection, String timestamp) {
		  return collection+timestamp.substring(0,8);
	  }
	  
	  /**
	   * Return the original collection name
	   * @param collection composed collection name with timestamp
	   * @return
	   */
	  public static String getCollectionNameOriginal(String collection) {
		  return collection.substring(0,collection.length()-8);
	  }
	  
	  /**
	   * Return the original timestamp 
	   * @param collection composed collection name with timestamp
	   * @return
	   */
	  public static String getTimestampOriginal(String collection) {
		  return collection.substring(collection.length()-8);
	  }
	  	  
	  /**
	   * Convert strings from format "20070831" into format "2007-08-31"
	   * @param timestamp timestamp
	   * @return
	   */	  
	  private String convertTimestampString(String timestamp) {
		  return timestamp.substring(0,4)+"-"+timestamp.substring(4,6)+"-"+timestamp.substring(6,8);		 
	  }
	  
	  /**
	   * Convert timestamp in string with format "20070831" 
	   * @param timestamp
	   * @return
	   */
	  private String convertTimestamp(Timestamp timestamp) {
		  return formatterTimestampToString.format(timestamp);
	  }
    
	  /**
	   * Main
	   * @param args
	   */
	  public static void main (String args[]) {
		  
		  if (args.length !=5) {
			  System.out.println("arguments: <database> <username> <password> <url> <timestamp>");
			  return;
		  }
    
		  try {
			  SqlSearcher searcher = new SqlSearcher();
			  searcher.connect(args[0], args[1], args[2]);
			  searcher.selectNearTimestamp(args[3], args[4]);
			  searcher.close();
		  }
		  catch (Exception ex) {			 
			  ex.printStackTrace();
		  }
	  }
 
}
