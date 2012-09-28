package pt.arquivo.logs.arquivo;

import java.sql.*;   // All we need for JDBC
import java.text.*;
import java.io.*;
import java.util.*;

/**
 * Class to handle SQL operations
 * @author Miguel Costa
 */
public class SqlOperations {

	  private static SimpleDateFormat formatterTimestampToString = new SimpleDateFormat("yyyyMMddHHmmss");	  
	  
	  private final static String SQL_INSERT_SESSION="insert into session (id,sid,ip,initialDate,evaltype,evaltopic,isurl,isbetweendates) values (?,?,?,?,?,?,?,?)";	  	  
	  private final static String SQL_INSERT_SESSION_ENTRY="insert into sessionEntry (id,date,action,session,description) values (?,?,?,?,?)";
	  
	  private final static String SQL_DELETE_SESSION="delete from session where id=?";
	  private final static String SQL_DELETE_SESSION_ENTRY="delete from sessionEntry where session=?";
	  	  
	  private final static String SQL_QUERY_TOTAL_SESSIONS="select count(*) from session";
	  private final static String SQL_QUERY_TOTAL_SESSIONS_TYPE_CLASSIFIED="select count(*) from session where evaltype!=0";
	  private final static String SQL_QUERY_TOTAL_SESSIONS_TOPIC_CLASSIFIED="select count(*) from session where evaltopic!=0";
	  
	  //private final static String SQL_QUERY_SESSION="select s.id,s.sid,s.ip,s.comments,sType.type,sTopic.type from session s LEFT JOIN sessionType sType ON evaltype=sType.id LEFT JOIN sessionTopic sTopic ON evaltopic=sTopic.id order by initialDate offset ? limit 1";	  
	  private final static String SQL_QUERY_SESSION="select id,sid,ip,comments,evaltype,evaltopic,isurl,isbetweendates from session order by initialDate offset ? limit 1";
	  private final static String SQL_QUERY_SESSION_ENTRIES="select e.date,a.type,e.description from sessionEntry e, sessionEntryAction a where session=? and e.action=a.id order by e.date";
	  
	  private final static String SQL_QUERY_SESSION_TYPES="select id,type from sessionType";
	  private final static String SQL_QUERY_SESSION_TOPICS="select id,type from sessionTopic";
	  
	  private final static String SQL_UPDATE_SESSION_TYPE="update session set evaltype=? where id=?";
	  private final static String SQL_UPDATE_SESSION_TOPIC="update session set evaltopic=? where id=?";
	  private final static String SQL_UPDATE_SESSION_COMMENTS="update session set comments=? where id=?";
	  private final static String SQL_UPDATE_SESSION_ISURL="update session set isurl=? where id=?";
	  private final static String SQL_UPDATE_SESSION_ISBETWEENDATES="update session set isbetweendates=? where id=?";
	  
	  	  	 
	  private Connection        db;        // A connection to the database
	  private DatabaseMetaData  dbmd;      //
	                    
	  private PreparedStatement psInsertSession;
	  private PreparedStatement psInsertSessionEntry;
	  private PreparedStatement psDeleteSession;
	  private PreparedStatement psDeleteSessionEntry;
	  private PreparedStatement psQueryTotalSessions;
	  private PreparedStatement psQueryTotalSessionsTypeClassified;
	  private PreparedStatement psQueryTotalSessionsTopicClassified;
	  private PreparedStatement psQuerySession;
	  private PreparedStatement psQuerySessionEntries;
	  private PreparedStatement psQuerySessionTypes;
	  private PreparedStatement psQuerySessionTopics;
	  private PreparedStatement psUpdateSessionType;
	  private PreparedStatement psUpdateSessionTopic;
	  private PreparedStatement psUpdateSessionComments;
	  private PreparedStatement psUpdateSessionIsUrl;
	  private PreparedStatement psUpdateSessionIsBetweenDates;
	  
	 
	  /**
	   * Connect to database
	   * @param database
	   * @param username
	   * @param password
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
		  
		  psInsertSession = db.prepareStatement(SQL_INSERT_SESSION);
		  psInsertSessionEntry = db.prepareStatement(SQL_INSERT_SESSION_ENTRY);		  		 		  
		  
		  psDeleteSession = db.prepareStatement(SQL_DELETE_SESSION);
		  psDeleteSessionEntry = db.prepareStatement(SQL_DELETE_SESSION_ENTRY);
		  
		  psQueryTotalSessions = db.prepareStatement(SQL_QUERY_TOTAL_SESSIONS);
		  psQueryTotalSessionsTypeClassified = db.prepareStatement(SQL_QUERY_TOTAL_SESSIONS_TYPE_CLASSIFIED);
		  psQueryTotalSessionsTopicClassified = db.prepareStatement(SQL_QUERY_TOTAL_SESSIONS_TOPIC_CLASSIFIED);
		  
		  psQuerySession = db.prepareStatement(SQL_QUERY_SESSION);
		  psQuerySessionEntries = db.prepareStatement(SQL_QUERY_SESSION_ENTRIES);
		  psQuerySessionTypes = db.prepareStatement(SQL_QUERY_SESSION_TYPES);
		  psQuerySessionTopics = db.prepareStatement(SQL_QUERY_SESSION_TOPICS);
		  
		  psUpdateSessionType = db.prepareStatement(SQL_UPDATE_SESSION_TYPE);
		  psUpdateSessionTopic = db.prepareStatement(SQL_UPDATE_SESSION_TOPIC);
		  psUpdateSessionComments = db.prepareStatement(SQL_UPDATE_SESSION_COMMENTS);
		  psUpdateSessionIsUrl = db.prepareStatement(SQL_UPDATE_SESSION_ISURL);
		  psUpdateSessionIsBetweenDates = db.prepareStatement(SQL_UPDATE_SESSION_ISBETWEENDATES);
	  }
	  
	  /**
	   * Close reources
	   * @throws SQLException
	   */
	  public void close() throws SQLException {
		  psInsertSession.close();
		  psInsertSessionEntry.close();
		  psDeleteSession.close();
		  psDeleteSessionEntry.close();
		  psQueryTotalSessions.close();
		  psQueryTotalSessionsTypeClassified.close();
		  psQueryTotalSessionsTopicClassified.close();
		  psQuerySession.close();
		  psQuerySessionEntries.close();
		  psQuerySessionTypes.close();
		  psQuerySessionTopics.close();
		  psUpdateSessionType.close();
		  psUpdateSessionTopic.close();
		  psUpdateSessionComments.close();
		  psUpdateSessionIsUrl.close();
		  psUpdateSessionIsBetweenDates.close();
		  db.close();
	  }
	  
	  
	  /**
	   * Insert session
	   * @id session key
	   * @firstDate date of first entry of session
	   * @ip ip address
	   * @throws SQLException
	   */
	  public void insertSession(String id, String sid, String ip, Timestamp firstEntryDate) throws SQLException {				  
		  psInsertSession.setString(1,id);
		  psInsertSession.setString(2,sid);
		  psInsertSession.setString(3,ip);
		  psInsertSession.setTimestamp(4,firstEntryDate);
		  psInsertSession.setInt(5,0);
		  psInsertSession.setInt(6,0);
		  psInsertSession.setInt(7,0);
		  psInsertSession.setInt(8,0);
		  int res = psInsertSession.executeUpdate();		  		  
		  if (res==0) {
			  throw new SQLException("0 sessions inserted.");		      
		  } 		  			  							  		  
	  }
	  
	  /**
	   * Delete session and entries of this session
	   * @id session key
	   * @throws SQLException
	   */
	  public void deleteSession(String id) throws SQLException {
		  psDeleteSessionEntry.setString(1,id);
		  int res = psDeleteSessionEntry.executeUpdate();	
		  /*
		  if (res==0) {
			  throw new SQLException("0 session entries deleted.");		      
		  } 
		  */
		  
		  psDeleteSession.setString(1,id);
		  res = psDeleteSession.executeUpdate();			  
		  if (res==0) {
			  throw new SQLException("0 sessions deleted.");		      
		  } 		  		  		  			  							  		 
	  }
	  
	  /**
	   * Create session key
	   * @sid session id
	   * @firstDate date of first entry of session 
	   * @return
	   */
	  public String getSessionKey(String sid, String ip, Timestamp firstEntryDate) {
		  return sid+"-"+ip+"-"+formatterTimestampToString.format(firstEntryDate);
	  }
	  
	  /**
	   * Insert session entry
	   * @firstDate date of first entry of session
	   * @throws SQLException
	   */
	  public void insertSessionEntry(int id, Timestamp date, int action, String session, String description) throws SQLException {				  
		  psInsertSessionEntry.setInt(1,id);
		  psInsertSessionEntry.setTimestamp(2,date);
		  psInsertSessionEntry.setInt(3,action);
		  psInsertSessionEntry.setString(4,session);
		  psInsertSessionEntry.setString(5,description);
		  
		  int res = psInsertSessionEntry.executeUpdate();		  		  
		  if (res==0) {
			  throw new SQLException("0 session entries inserted.");		      
		  } 		  			  							  		  
	  }
	  	  	 
	  /**
	   * Query total number of sessions with type classified
	   * @throws SQLException
	   */
	  public int selectTotalSessionsTypeClassified() throws SQLException {		  		 		 		  
		  ResultSet results = psQueryTotalSessionsTypeClassified.executeQuery();
		  int totalSessions=-1;
		  if (results!=null) {
			  if (results.next()) {
				  totalSessions=results.getInt(1);
			  }
			  results.close();  		      
		  } 		  			  							  		  
		  return totalSessions;
	  }
	  
	  /**
	   * Query total number of sessions with topic classified
	   * @throws SQLException
	   */
	  public int selectTotalSessionsTopicClassified() throws SQLException {		  		 		 		  
		  ResultSet results = psQueryTotalSessionsTopicClassified.executeQuery();
		  int totalSessions=-1;
		  if (results!=null) {
			  if (results.next()) {
				  totalSessions=results.getInt(1);
			  }
			  results.close();  		      
		  } 		  			  							  		  
		  return totalSessions;
	  }
	  
	  /**
	   * Query total number of sessions
	   * @throws SQLException
	   */
	  public int selectTotalSessions() throws SQLException {		  		 		 		  
		  ResultSet results = psQueryTotalSessions.executeQuery();
		  int totalSessions=-1;
		  if (results!=null) {
			  if (results.next()) {
				  totalSessions=results.getInt(1);
			  }
			  results.close();  		      
		  } 		  			  							  		  
		  return totalSessions;
	  }
	   
	  /**
	   * Query session entries
	   * @param sessionKey session key
	   * @throws SQLException
	   */
	  public ResultSet selectSessionEntries(String sessionKey) throws SQLException {		
		  psQuerySessionEntries.setString(1,sessionKey);
		  ResultSet results = psQuerySessionEntries.executeQuery();		    			  							  		 
		  return results;
	  }
	  
	  /**
	   * Query session 
	   * @param isession session number
	   * @throws SQLException
	   */
	  public ResultSet selectSession(int isession) throws SQLException {
		  psQuerySession.setInt(1,isession);
		  ResultSet results = psQuerySession.executeQuery();		    			  							  		 
		  return results;
	  }
	  
	  /**
	   * Query session types
	   * @throws SQLException
	   */
	  public ResultSet selectSessionTypes() throws SQLException {		  
		  ResultSet results = psQuerySessionTypes.executeQuery();		    			  							  		 
		  return results;
	  }
	  
	  /**
	   * Query session topics
	   * @throws SQLException
	   */
	  public ResultSet selectSessionTopics() throws SQLException {		  
		  ResultSet results = psQuerySessionTopics.executeQuery();		    			  							  		 
		  return results;
	  }
	  	  
	  /**
	   * Update session type
	   * @param type session type
	   * @param sessionKey session key
	   * @throws SQLException
	   */
	  public void updateSessionType(int type, String sessionKey) throws SQLException {
		  psUpdateSessionType.setInt(1,type);
		  psUpdateSessionType.setString(2,sessionKey);
		  int res = psUpdateSessionType.executeUpdate();		  		  
		  if (res==0) {
			  throw new SQLException("0 session entries updated.");		      
		  } 		  			  				  
	  }
	  
	  /**
	   * Update session topic
	   * @param topic session topic
	   * @param sessionKey session key
	   * @throws SQLException
	   */
	  public void updateSessionTopic(int topic, String sessionKey) throws SQLException {
		  psUpdateSessionTopic.setInt(1,topic);
		  psUpdateSessionTopic.setString(2,sessionKey);
		  int res = psUpdateSessionTopic.executeUpdate();		  		  
		  if (res==0) {
			  throw new SQLException("0 session entries updated.");		      
		  } 		  			  				  
	  }
	  
	  /**
	   * Update session comments
	   * @param comments session comments
	   * @param sessionKey session key
	   * @throws SQLException
	   */
	  public void updateSessionComments(String comments, String sessionKey) throws SQLException {
		  psUpdateSessionComments.setString(1,comments);
		  psUpdateSessionComments.setString(2,sessionKey);
		  int res = psUpdateSessionComments.executeUpdate();		  		  
		  if (res==0) {
			  throw new SQLException("0 session entries updated.");		      
		  } 		  			  				  
	  }
	  
	  
	  /**
	   * Update session isurl field
	   * @param isurl session isurl field
	   * @param sessionKey session key
	   * @throws SQLException
	   */
	  public void updateSessionIsUrl(int isurl, String sessionKey) throws SQLException {
		  psUpdateSessionIsUrl.setInt(1,isurl);
		  psUpdateSessionIsUrl.setString(2,sessionKey);
		  int res = psUpdateSessionIsUrl.executeUpdate();		  		  
		  if (res==0) {
			  throw new SQLException("0 session entries updated.");		      
		  } 		  			  				  
	  }
	  
	  
	  /**
	   * Update session isbetweendates field
	   * @param isurl session isbetweendates field
	   * @param sessionKey session key
	   * @throws SQLException
	   */
	  public void updateSessionIsBetweenDates(int isbetweendates, String sessionKey) throws SQLException {
		  psUpdateSessionIsBetweenDates.setInt(1,isbetweendates);
		  psUpdateSessionIsBetweenDates.setString(2,sessionKey);
		  int res = psUpdateSessionIsBetweenDates.executeUpdate();		  		  
		  if (res==0) {
			  throw new SQLException("0 session entries updated.");		      
		  } 		  			  				  
	  }
   
  
	  public static void main (String args[]) {
		  
		  if (args.length!=3) {
			  System.out.println("arguments: <database> <username> <password>");
			  return;
		  }
    
		  try {
			  SqlOperations op = new SqlOperations();
			  op.connect(args[0], args[1], args[2]);
			  
			  // some tests
			  String sessionKey=op.getSessionKey("5BBB90668FF2D73BF0CEFFE1301ACD39","193.136.173.43",new Timestamp(System.currentTimeMillis()));
			  op.insertSession(sessionKey,"5BBB90668FF2D73BF0CEFFE1301ACD39", "193.136.7.2",new Timestamp(System.currentTimeMillis()));
			  op.insertSessionEntry(1, new Timestamp(System.currentTimeMillis()), 1, sessionKey, "query miguel costa");
			  
			  op.close();
		  }
		  catch (SQLException ex) {			 
			  ex.printStackTrace();
		  } 
		  catch (ClassNotFoundException e) {			
			e.printStackTrace();
		}
	  }
 
}
