
package pt.arquivo.logs.arquivo;

import java.io.*;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Analyze search logs from the Portuguese Web Archive
 * @author Miguel Costa
 * Usage:
 * 
 * 1) Reformat the log file to move to the first position the IP and clean the log
 *    java -classpath ~/workspace/PwaLogsMiner/target/classes/ pt.arquivo.logs.arquivo.LogAnalyzer arquivo_log2010 reformat > arquivo_log2010.reformat 
 * 2) Sort the log file by session id:
 *    cat arquivo_log2010.reformat | awk {'print NR" "$0'} | sort -k 4,4 -k 2,2 -k 1,1n | cut -f2- -d ' ' > arquivo_log2010.sorted 
 * 3) Compute statistics:
 *    java -classpath ~/workspace/PwaLogsMiner/target/classes/ pt.arquivo.logs.arquivo.LogAnalyzer arquivo_log2010.sorted stats > arquivo_log2010.stats 2>x2
 *     or java -classpath ~/workspace/PwaLogsMiner/target/classes/:/home/nutchwax/.m2/repository/postgresql/postgresql/8.3-604.jdbc4/postgresql-8.3-604.jdbc4.jar pt.arquivo.logs.arquivo.LogAnalyzer arquivo_log2010.sorted stats > arquivo_log2010.stats
 */
public class LogAnalyzer {	
    private static enum StatsComputed {ALL, FULL_TEXT, URL};
    private static StatsComputed statsComputed=StatsComputed.ALL; // TODO parameterize
    //private final static String dateRangeFilter[]={"01/Apr/2011","01/Jul/2011"}; // TODO parameterize filter
    private final static String dateRangeFilter[]={"10/Jan/2011","10/Jul/2011"}; // TODO parameterize filter
	
	// SQL variables
	private final static boolean LOGS_TO_SQL=false; // create entries in database	
	private final static int     LIMIT_LEN_FIELD=500; // maximum length of field
	private final static String  DUPLICATE_KEY_MESSAGE="duplicate key violates unique constraint";
	
	private final static SimpleDateFormat dformat = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss z",Locale.US);
	private final static SimpleDateFormat dsimpleformat = new SimpleDateFormat("dd/MMM/yyyy",Locale.US);
	private static final String URL_QUERY_PATTERN_WITH_TERMS = "(^|.+ +)((https?:\\/\\/)?([\\da-z\\.-]+)\\.([a-z\\.]{2,6})([-\\/\\w\\p{L}\\.~,;:%&=?+$#]*)*\\/?)($| +).*"; // implementation used on PWA - David
	private static final String URL_QUERY_PATTERN = "^[\\s]*((https?|ftp|file)://)?([-a-zA-Z0-9_]+\\.)+([a-zA-Z]{2,6})(:[0-9]{2,5})?(\\/[-a-zçãõâêôA-ZÇÃÕÂÊÔ0-9+&@#/%=~_|!:,;*?$.]*)*[\\s]*$";
	private static final String URL_QUERY_PATTERN_WITH_QUOTES = "^[\\s]*\"[\\s]*((https?|ftp|file)://)?([-a-zA-Z0-9_]+\\.)+([a-zA-Z]{2,6})(:[0-9]{2,5})?(\\/[-a-zçãõâêôA-ZÇÃÕÂÊÔ0-9+&@#/%=~_|!:,;*?$.]*)*[\\s]*\"[\\s]*$";		
		
	// filters
	//private final static String entryFilters[]={"Pesquisar.x","Pesquisar.y","Submit.x","Submit.y","Submit2.x","Submit2.y","tumbaSubmit.x","tumbaSubmit.y","dict","I1.x","I1.y","I2.x","I2.y","I3.x","I3.y","I4.x","I4.y","query_id"};
	private final static String queryFilters[]={"","watchdog","monit","fccn","expo98","euro 2004","eleições","http://www.ul.pt/","www.fccn.pt","http://www.fccn.pt",
		"http://arquivo-web.fccn.pt/tools/history-of-this-page-button/history-of-this-page-installation-firefox","http://arquivo-web.fccn.pt/tools/history-of-this-page-button/history-of-this-page-button?set_language=en"};
	private final static String toolsQueryFilters[]={"history-of-this-page-button","botao-historico-desta-pagina","bookmarklet-pro-firefox"};	
	private final static String advancedQueryFilters[]={"site:","\"","-","type:","sort:new","sort:old"};
	private final static String stopwords[]={"a","e","o","as","os","da","de","do","das","des","dos","em","na","no","nas","nos"};
    private final static String ipDomainFilters[]={"193.136.44.","193.136.192.","193.136.7."}; // domains: corp, machines, nagios    //
    private final static String ipFilters[]={}; // bots not identified        
    private final static String botsFilters[]={"crawler","spider","bot"};
    private final static String browsersFilters[]={"internet explorer","msie","mozilla","firefox","opera","chrome","safari"};                                       
	
	// string keys
	private final static String QUERY_KEY="query";
	private final static String ADV_QUERY_KEY="adv_and";	
	private final static String QUERY_TIME_KEY="t_q"; // query time
	private final static String QUERY_TOTAL_TIME_KEY="t_t"; // query time + presentation time
	private final static String NEW_QUERY_KEY="query";
	private final static String DOCS_KEY="start";
	//private final static String SESSIONID_KEY="query_id=";
	private final static String SERP_POS_KEY="pos";
	private final static String WAYBACK_POS_KEY="r_pos";
	private final static String WAYBACK_POS_TOTAL_KEY="r_t";
	private final static String WAYBACK_YEAR="year";
	private final static String WAYBACK_COL_WITH_RESULTS="col_pos"; // index of column with results
	private final static String WAYBACK_COL_TOTAL_RESULTS="col_tot";
	private final static String SHOW_VERSIONS_POS_KEY="pos";
	private final static String SHOW_VERSIONS_KEY="hist";
	private final static String LANG_KEY="l";
	private final static String DEFAULT_LANG="pt";
	private final static String START_DATE_CHANGED_KEY="str_date_changed";
	private final static String END_DATE_CHANGED_KEY="end_date_changed";
	private final static String START_DATE_KEY="datestart";
	private final static String END_DATE_KEY="dateend";
	private final static String NUMBER_RESULTS="num_res";

	// constants
	private final static long   SESSION_TIMEOUT=30*60*1000;
	private final static int    MAX_QUERIES_PER_SESSION=100;
	private final static int    MAX_WAYBACK_CLICKS_PER_SESSION=20;
	private final static int    IGNORE_FIRST_SESSIONS=5; // because initial sessions can be incomplete
	private final static int    RESULTS_PER_PAGE=10;
	private final static int    N_MODIFIED_TERMS_RANGE=21;
	private final static int    N_QUERIES_SESSION_RANGE=21;
	private final static int    N_TERMS_QUERY_RANGE=21;
	private final static int    N_PAGES_VIEWED_RANGE=20;
	private final static int    N_DOCS_CLICKED_RANGE=100;
    private final static int    SESSION_TIME_BINS[]={0,1,5,10,15,30,60,120,180,240};
    private final static int    N_SESSION_TIME_BINS=SESSION_TIME_BINS.length;
    private final static int    N_USERS_SESSIONS_RANGE=10;
    private final static int    FIRST_YEAR=1996;
    private final static int    LAST_YEAR=2010;
    private final static int    FIRST_MONTH=1;
    private final static int    LAST_MONTH=12;
    private final static int    FIRST_DAY=1;
    private final static int    LAST_DAY=31;
    private final static int    N_QUERY_TIME_BINS=30;    

    // counters
	private static int pagesViewedNotFirstPage=0; // number of results pages that are not the first one
	private static int pagesViewedDist[]=new int[N_PAGES_VIEWED_RANGE];  // distribution of the results pages seen
	private static int pagesViewedDistAux[]=new int[N_PAGES_VIEWED_RANGE];
	private static int numberPagesViewedDist[]=new int[N_PAGES_VIEWED_RANGE];  // distribution of the number of results pages seen
    private static int totalPagesViewed=0; // number of pages viewed
	private static int totalModifiedQueries=0; // have at least one equal term to previous query	
	private static int totalIdenticalQueries=0; // the queries equal to previous query	
	private static int totalIdenticalSessionQueries=0; // the queries equal to previous query in the same session	
	private static int totalQueriesURLs=0; // the queries to show all versions of a page given only a URL
	private static int totalQueriesURLsAux=0; // auxiliary
	private static int totalQueriesURLsWithQuotes=0; // the queries with a URL in quotes
	private static int totalQueriesURLsWithQuotesAux=0; // auxiliary	
	private static int totalQueriesURLsWithTerms=0; // the queries with a URL at least and terms	
	private static int totalQueriesURLsWithTermsAux=0; // auxiliary
	private static int totalAdvQueriesOverlapping=0; // total number of advanced queries with operators overlapping (one per operator overlapping)
	private static int totalAdvQueriesOverlappingAux=0; // auxiliary	
	private static int totalEqualTermsQueries=0; // the query terms are the same to previous query
    private static int totalEqualTermsOrderQueries=0; // the query terms are the same to previous query and have the same order
    private static int totalEqualTermsStopwordsQueries=0; // the query has only stopwords and is equal to previous query
	private static int nModifiedTerms[]=new int [N_MODIFIED_TERMS_RANGE]; // number of modified terms range
	private static int nTermsQueryDist[]=new int [N_TERMS_QUERY_RANGE]; // number of modified terms range
	private static int nQueriesSessionDist[]=new int [N_QUERIES_SESSION_RANGE]; // distribution of number of queries per session
	private static int nQueriesSession=0; // number of queries in session	
	private static int docsClickedDist[]=new int[N_DOCS_CLICKED_RANGE];  // distribution of clicks
	private static int docsClickedDistAux[]=new int[N_DOCS_CLICKED_RANGE];  // distribution of clicks
	private static int totalClicks=0; // number of clicks 
	private static int totalClicksAux=0; // number of clicks in session
	private static int totalSessionsWithoutClicksFulltext=0; // number of sessions without any type of click
	private static int totalSessionsWithoutClicksUrl=0; // number of URL sessions without any type of click on versions
	private static int docsClickedHistoricDist[]=new int[N_DOCS_CLICKED_RANGE];  // distribution of clicks in historic to show all versions
	private static int docsClickedHistoricDistAux[]=new int[N_DOCS_CLICKED_RANGE];  // distribution of clicks in historic to show all versions
	private static int totalClicksHistoric=0; // number of clicks in historic to show all versions
	private static int totalClicksHistoricAux=0; // number of clicks in session in historic to show all versions
    private static int sessionTimeBins[]=new int[N_SESSION_TIME_BINS]; // total of sessions per time bin
    private static int totalSeconds=0; // total of seconds spend in sessions
    private static int totalValidSessions=0; // total of valid sessions
    private static int totalValidSessionsMoreThanOneQuery=0; // total of valid sessions	but with more than one query (duration>0)
    private static int totalValidSessionsOnlyUrl=0; // total of valid sessions with only URL queries
    private static int totalValidSessionsOnlyFulltext=0; // total of valid sessions with only full-text queries    
    private static int maxQueriesPerSession=0;
    private static int minQueriesPerSession=Integer.MAX_VALUE;
    private static int totalQueriesCheck=0; // total number of queries; check with the other totalQueries
    private static int totalStartDateChanged=0; // total number of queries where the start date was changed
    private static int totalEndDateChanged=0; // total number of queries where the end date was changed
    private static int totalStartEndDateChanged=0; // total number of queries where the start and end date were changed
    private static int totalAnyDateChanged=0; // total number of queries with date restriction
    private static int totalStartDateChangedAux=0; // total number of queries where the start date was changed
    private static int totalEndDateChangedAux=0; // total number of queries where the end date was changed
    private static int totalStartEndDateChangedAux=0; // total number of queries where the start and end date were changed    
    private static int totalAnyDateChangedAux=0; //    
    private static int totalSessionsWithoutClicksResults0Fulltext=0; // total of full-text sessions without clicks due to 0 results returned in all queries
    private static int totalSessionsWithoutClicksResults0Url=0; // total of URL sessions without clicks due to 0 results returned in all queries
    private static Clicks clicks=new Clicks(); // record click information
    
    private static int queryYearsDist[]=new int[LAST_YEAR-FIRST_YEAR+1]; // total number of queries including this year
    private static int queryYearsDistAux[]=new int[LAST_YEAR-FIRST_YEAR+1];
    private static int totalWaybackClicks=0; // total number of clicks in wayback
    private static int waybackClickYearsDist[]=new int[LAST_YEAR-FIRST_YEAR+1]; // total number of clicks in wayback including this year
    private static int waybackClickYearsDistAux[]=new int[LAST_YEAR-FIRST_YEAR+1];
    private static int waybackClickColumnsWithResultsDist[][]=new int[5][(LAST_YEAR-1)-FIRST_YEAR+1]; // total number of pairs of clicks in wayback columns with results and the number of versions in each column
    private static int waybackClickColumnsWithResultsDistAux[]=new int[(LAST_YEAR-1)-FIRST_YEAR+1];
    private static int waybackClickColumnsWithResultsTotalRowsDistAux[]=new int[(LAST_YEAR-1)-FIRST_YEAR+1]; // total of rows with entries in a year column
    private static int waybackClickColumnsWithResultsTotalColumnsDistAux[]=new int[(LAST_YEAR-1)-FIRST_YEAR+1]; // total of columns with entries in a year column
    private static int queryTimeBins[]=new int[N_QUERY_TIME_BINS]; // total of sessions per tie bin
    private static int presentationTimeBins[]=new int[N_QUERY_TIME_BINS]; // total of sessions per tie bin
    private static int waybackQueryTimeBins[]=new int[N_QUERY_TIME_BINS]; // total of sessions per tie bin
    private static int waybackPresentationTimeBins[]=new int[N_QUERY_TIME_BINS]; // total of sessions per tie bin
    private static int queryTimeBinsAux[]=new int[N_QUERY_TIME_BINS]; // total of sessions per tie bin
    private static int presentationTimeBinsAux[]=new int[N_QUERY_TIME_BINS]; // total of sessions per tie bin
    private static int waybackQueryTimeBinsAux[]=new int[N_QUERY_TIME_BINS]; // total of sessions per tie bin
    private static int waybackPresentationTimeBinsAux[]=new int[N_QUERY_TIME_BINS]; // total of sessions per tie bin
    private static int totalAdvInterfaceQueries=0;
    private static int totalAdvQueries=0; // total queries with advanced operators		
    private static int totalAdvQueriesAux=0; // auxiliary
    private static int advQueriesDist[]=new int[advancedQueryFilters.length];
    private static int advQueriesDistAux[]=new int[advancedQueryFilters.length];
    private static boolean isSessionOnlyFullTextQueries; // indicates if the session has only full-text queries
    private static boolean isSessionOnlyUrlQueries; // indicates if the session has only URL queries    
    private static int wayback_col_pos_log=0; // position of year with results
    private static int wayback_col_total_log=0; // total of years with results
    private static int wayback_pos_total_log=0; // total of versions from this year
    private static int urlDepthDist[]=new int[5]; // URL depth distribution
    
    private static String oldQuery=new String(); // last query
	private static HashMap<String,Integer> queryPartsMap=new HashMap<String,Integer>(); // has the number of times a part of the query occurs
	private static HashMap<String,Integer> queryPartsMapAux=new HashMap<String,Integer>(); // has the number of times a part of the query occurs - auxiliary
	private static HashMap<String,Integer> newTerms=new HashMap<String,Integer>(); // record the terms to analyze modified queries
	private static HashSet<String> queryFiltersMap=new HashSet<String>(); // queries to filter
	private static HashSet<String> stopwordsMap=new HashSet<String>();	// stopwords to filter
	private static HashSet<String> sessionQueries=new HashSet<String>(); // stores the queries in a session
	private static HashSet<String> ipDomainFiltersMap=new HashSet<String>(); // IP domains to filter
	private static HashSet<String> ipFiltersMap=new HashSet<String>(); // IPs to filter
	private static HashMap<String,Integer> nSessionsIP=new HashMap<String,Integer>(); // number of sessions per IP 	
	private static HashMap<String,Integer> nQueriesLang=new HashMap<String,Integer>(); // number of queries per lang
	private static HashMap<String,Integer> nQueriesLangAux=new HashMap<String,Integer>(); // number of queries per lang	
	private static HashMap<String,Integer> waybackPagesSaw=new HashMap<String,Integer>(); // archived pages saw
	private static HashMap<String,Integer> waybackPagesSawAux=new HashMap<String,Integer>(); // aux
	
		
	static enum UserAction {QUERY, ADV_QUERY, SERP_CLICK, HIST_CLICK, WAYBACK_CLICK};
	
	// IClickProcessor implementations to handle clicks
	// ?pos=1&l=pt&sid=12445C7988FE5AA9350C8BEA62D78255     - TODO: I should add year to logs to compute the distribution? Results depend of the presentation and ranking of results. I ignore for now.
	static class ISerpClickProcessor implements IClickProcessor {
		public ClickEntry exec(String queryPartsField0, String queryPartsField1) {		
			if (queryPartsField0.equals(SERP_POS_KEY) && !queryPartsField1.equals("")) {
				int ipos;
				try {
					ipos=Integer.parseInt(queryPartsField1);
					if (ipos>=docsClickedDistAux.length) {
						System.err.println("Clicked on position:"+ipos);
						ipos=docsClickedDistAux.length-1;						
					}
					docsClickedDistAux[ipos]++;
					totalClicksAux++;
				}
				catch (NumberFormatException e) {
					return null;
				}
				return new ClickEntry(ipos);
			}	
			
			return null;
		}
	}
	
	// ?query=http%3A%2F%2Fwww.expo98.pt%2F&dateStart=01/01/1996&dateEnd=01/12/2009&pos=1&str_date_changed=false&end_date_changed=false&hist=true&num_res=1&t_q=2684&t_t=2687 
	static class IHistClickProcessor implements IClickProcessor {
		public ClickEntry exec(String queryPartsField0, String queryPartsField1) {			
			if (queryPartsField0.equals(SHOW_VERSIONS_POS_KEY) && !queryPartsField1.equals("")) {
				int ipos;
				try {
					ipos=Integer.parseInt(queryPartsField1);
					if (ipos>=docsClickedHistoricDistAux.length) {
						System.err.println("Clicked on position to show versions:"+ipos);
						ipos=docsClickedHistoricDistAux.length-1;						
					}
					docsClickedHistoricDistAux[ipos]++;
					totalClicksHistoricAux++;									
				}
				catch (NumberFormatException e) {
					return null;
				}
				return new ClickEntry(ipos);
			}
			else if (queryPartsField0.equals(QUERY_TIME_KEY) && !queryPartsField1.equals("")) { // group queries by time spent
				int time=Integer.parseInt(queryPartsField1)/1000;
				if (time>=N_QUERY_TIME_BINS) {
					time=N_QUERY_TIME_BINS-1;
				}
				waybackQueryTimeBinsAux[time]++;				
			}
			else if (queryPartsField0.equals(QUERY_TOTAL_TIME_KEY) && !queryPartsField1.equals("")) { // group queries by time spent
				int time=Integer.parseInt(queryPartsField1)/1000;
				if (time>=N_QUERY_TIME_BINS) {
					time=N_QUERY_TIME_BINS-1;
				}
				waybackPresentationTimeBinsAux[time]++;
			}
			
			return null;
		}
	}
	
	// ?year=2000&r_pos=1&r_t=6&l=pt&sid=12445C7988FE5AA9350C8BEA62D78255
	static class IWaybackClickProcessor implements IClickProcessor {
		public ClickEntry exec(String queryPartsField0, String queryPartsField1) {
			if (queryPartsField0.equals(WAYBACK_POS_KEY) && !queryPartsField1.equals("")) {		
				// TODO		
			}
			else if (queryPartsField0.equals(WAYBACK_POS_TOTAL_KEY) && !queryPartsField1.equals("")) {		
				 wayback_pos_total_log=Integer.parseInt(queryPartsField1);		

				 if (wayback_col_pos_log!=0) {
					 waybackClickColumnsWithResultsTotalRowsDistAux[wayback_col_pos_log-1]+=wayback_pos_total_log;
				 }
			}
			else if (queryPartsField0.equals(WAYBACK_YEAR) && !queryPartsField1.equals("")) {
				int year=Integer.parseInt(queryPartsField1);
				waybackClickYearsDistAux[year-FIRST_YEAR]++;				
			}
			else if (queryPartsField0.equals(WAYBACK_COL_WITH_RESULTS) && !queryPartsField1.equals("")) {				
				wayback_col_pos_log=Integer.parseInt(queryPartsField1);		
				waybackClickColumnsWithResultsDistAux[wayback_col_pos_log-1]++;
				
				if (wayback_pos_total_log!=0) {
					waybackClickColumnsWithResultsTotalRowsDistAux[wayback_col_pos_log-1]+=wayback_pos_total_log;
				}
			}					
			else if (queryPartsField0.equals(WAYBACK_COL_TOTAL_RESULTS) && !queryPartsField1.equals("")) {				 
				 wayback_col_total_log=Integer.parseInt(queryPartsField1);	
				 for (int i=0;i<wayback_col_total_log;i++) {
					 waybackClickColumnsWithResultsTotalColumnsDistAux[i]++; // all columns available
				 }
			}	
			
			return null;
		}	
	}
	
	
	/**
	 * Parse log file and computes statistics
	 * @param logfile log file
	 * @throws IOException 
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public static void readStats(String logfile) throws IOException, ClassNotFoundException, SQLException {													

		BufferedReader br = new BufferedReader( new FileReader(logfile) );
		String line;		
		String oldSessionId="";
		
		String ip=null;
		String oldIp="";		
		Date date=null;
		Date firstSessionDate=null;
		Date lastDate=null;
		Date lastSessionDate=null;		
		int totalSessions=0; // all sessions
		Vector<LogEntry> vlines=new Vector<LogEntry>();
		Vector<ProcessedEntry> vprocessedEntries=new Vector<ProcessedEntry>();	
		SqlOperations sqlOp = new SqlOperations();
		//String lastQuery="";		
		long minDateFilter=0;
		long maxDateFilter=0;			
		try {	
			minDateFilter=dsimpleformat.parse(dateRangeFilter[0]).getTime();
			maxDateFilter=dsimpleformat.parse(dateRangeFilter[1]).getTime();	
		} 
		catch (ParseException e) {
			throw new IOException(e);
		}	
		
		if (LOGS_TO_SQL) {			
			sqlOp.connect("//xxxxx","xxxxx","xxxxx"); // hard coded - TODO: parameterize this			
		}

		// init maps		
		for (int i=0;i<queryFilters.length;i++) {
			queryFiltersMap.add(queryFilters[i]);
		}			
		for (int i=0;i<stopwords.length;i++) {
			stopwordsMap.add(stopwords[i]);
		}
		for (int i=0;i<ipDomainFilters.length;i++) {
			ipDomainFiltersMap.add(ipDomainFilters[i]);
		}
		for (int i=0;i<ipFilters.length;i++) {
			ipFiltersMap.add(ipFilters[i]);
		}
		
		// read all entries from log
		while ( ( line = br.readLine() ) != null ) {
						
			//System.out.println("line:"+line);
			
			// parse request
			String parts[]=line.split("\\s");
			ip=parts[0];			
			String sessionId=parts[2];			
			String method=parts[5];
			String referrer=parts[10];
						
			if (ipDomainFiltersMap.contains(ip.substring(0,ip.lastIndexOf(".")+1))) {
				continue;
			}
			if (ipFiltersMap.contains(ip)) {
				continue;
			}	
			if (!method.equals("\"GET")) {
				continue;
			}			
			
			StringBuffer userAgent=new StringBuffer();
			for (int i=11; i<parts.length; i++) {
				userAgent.append(parts[i].toLowerCase());
				userAgent.append(" ");
	        }						
			// filter bot			
			boolean isBot=false;			
			for (int i=0; !isBot && i<botsFilters.length;i++) {
				if (userAgent.indexOf(botsFilters[i])!=-1) {
					System.err.println("BOT removed: "+userAgent);
					isBot=true;					
				}				
			}			
			if (isBot) {
				continue;
			}			
			// filter user agents not known
			boolean isBrowser=false;
			for (int i=0; !isBrowser && i<browsersFilters.length;i++) {
				if (userAgent.indexOf(browsersFilters[i])!=-1) {					
					isBrowser=true;					
				}				
			}
			if (!isBrowser) {
				System.err.println("BROWSER removed: "+userAgent);
				continue;
			}						
											
			String sdate=parts[3].substring(1)+" "+parts[4].substring(0,parts[4].length()-1);
			if (oldSessionId.equals(sessionId) && oldIp.equals(ip)) {
				lastSessionDate=date;	
			}			
			lastDate=date;
			try {
				date=dformat.parse(sdate);
			} 
			catch (ParseException e) {
				throw new IOException(e);
			}						
			// data range filter
			if (date.getTime()<minDateFilter || date.getTime()>maxDateFilter) {				
				continue;		
			}			 			
		
			// if it is a new session, then make statistics from the last session
			if (oldIp.equals("") || !oldSessionId.equals(sessionId) || !oldIp.equals(ip) || date.getTime()-lastDate.getTime()>SESSION_TIMEOUT) { // creates a new session only if one of this rules occur		
					
				if (totalSessions>=IGNORE_FIRST_SESSIONS) {																															
					// compute stats adding session lines
				    if (!oldIp.equals("")) {
							
						resetCounters();						
														
						// insert session
						String sessionKey=null;
						if (LOGS_TO_SQL) {
							sessionKey=sqlOp.getSessionKey(oldSessionId, oldIp,new Timestamp(firstSessionDate.getTime()));
							try {
								sqlOp.insertSession(sessionKey,oldSessionId,oldIp,new Timestamp(firstSessionDate.getTime()));
							}
							catch (SQLException e) {
								if (e.getMessage().indexOf(DUPLICATE_KEY_MESSAGE)==-1) {								
									throw new SQLException(e);
								}
							}
						}
						
						// count log entries
						int numWaybackClicks=0;						
						for (int i=0;i<vlines.size();i++) {							
							switch (vlines.get(i).getAction()) {
							case QUERY: {
							    QueryEntry entry=addQueryEntry(vlines.get(i).getQuery());
							    if (entry!=null && entry.getQuery()!=null) { // query on the first SERP
							    	vprocessedEntries.add(entry);   		
								    if (LOGS_TO_SQL) {
								    	String submittedQuery=entry.getQuery();
										String saux=new String(submittedQuery.getBytes(),"ISO-8859-1")+" "+getQueryDatesString(vlines.get(i).getQuery());
										if (saux.length()>LIMIT_LEN_FIELD) {
											saux=saux.substring(0,LIMIT_LEN_FIELD);	
										}
										try {
											sqlOp.insertSessionEntry(i,new Timestamp(vlines.get(i).getDate().getTime()),1,sessionKey,saux);
										}
										catch (SQLException e) {
											if (e.getMessage().indexOf(DUPLICATE_KEY_MESSAGE)==-1) {								
												throw new SQLException(e);
											}
										}
									}										
								}									
							}
							break;
							case ADV_QUERY: {							
							    QueryEntry entry=addAdvancedQueryEntry(vlines.get(i).getQuery());
							    if (entry!=null && entry.getQuery()!=null) { // advanced query on the first SERP
							    	vprocessedEntries.add(entry);									  						     
							        if (LOGS_TO_SQL) {
								        String submittedQuery=entry.getQuery();
										String saux=new String(submittedQuery+" "+getQueryDatesString(vlines.get(i).getQuery()));
										if (saux.length()>LIMIT_LEN_FIELD) {
											saux=saux.substring(0,LIMIT_LEN_FIELD);	
										}
										try {
											sqlOp.insertSessionEntry(i,new Timestamp(vlines.get(i).getDate().getTime()),2,sessionKey,saux);
										}
										catch (SQLException e) {
											if (e.getMessage().indexOf(DUPLICATE_KEY_MESSAGE)==-1) {								
												throw new SQLException(e);
											}
										}
									}
								}									
							}
							break;
							case HIST_CLICK: {
								addClickEntry(vlines.get(i).getQuery(),new IHistClickProcessor());								
								if (LOGS_TO_SQL) {
									try {
										sqlOp.insertSessionEntry(i,new Timestamp(vlines.get(i).getDate().getTime()),3,sessionKey,vlines.get(i).getQuery());
									}
									catch (SQLException e) {
										if (e.getMessage().indexOf(DUPLICATE_KEY_MESSAGE)==-1) {								
											throw new SQLException(e);
										}
									}
								}
							}
							break;							
							case SERP_CLICK: {
								ClickEntry entry=addClickEntry(vlines.get(i).getQuery(),new ISerpClickProcessor());
								vprocessedEntries.add(entry);
								if (LOGS_TO_SQL) {
									try {
										sqlOp.insertSessionEntry(i,new Timestamp(vlines.get(i).getDate().getTime()),4,sessionKey,vlines.get(i).getQuery());
									}
									catch (SQLException e) {
										if (e.getMessage().indexOf(DUPLICATE_KEY_MESSAGE)==-1) {								
											throw new SQLException(e);
										}
									}
								}
							}
							break;									
							case WAYBACK_CLICK: {
								// must be referred from wayback
								if (vlines.get(i).getReferrer().startsWith("\"http://www.arquivo.pt") || vlines.get(i).getReferrer().startsWith("\"http://arquivo.pt") || vlines.get(i).getReferrer().startsWith("\"-\"") ) {
									addClickEntry(vlines.get(i).getQuery(),new IWaybackClickProcessor());
									numWaybackClicks++;
								}		
								if (LOGS_TO_SQL) {
									try {
										sqlOp.insertSessionEntry(i,new Timestamp(vlines.get(i).getDate().getTime()),5,sessionKey,vlines.get(i).getQuery());
									}
									catch (SQLException e) {
										if (e.getMessage().indexOf(DUPLICATE_KEY_MESSAGE)==-1) {								
											throw new SQLException(e);
										}
									}
								}
							}
							break;
							}
						}																
												
						// min and max queries per session 
						if (nQueriesSession>maxQueriesPerSession) {
							maxQueriesPerSession=nQueriesSession;
						}
						if (nQueriesSession<minQueriesPerSession) {
							minQueriesPerSession=nQueriesSession;
						}
							
						// process valid session
						long seconds=((lastSessionDate.getTime()-firstSessionDate.getTime())/1000); // compute session duration						
						if (nQueriesSession>0 && nQueriesSession<=MAX_QUERIES_PER_SESSION && numWaybackClicks<=MAX_WAYBACK_CLICKS_PER_SESSION && seconds>=0 && ((statsComputed==StatsComputed.FULL_TEXT && isSessionOnlyFullTextQueries) || (statsComputed==StatsComputed.URL && isSessionOnlyUrlQueries) || (statsComputed==StatsComputed.ALL))) { // session discarded if has no queries, or too many queries, or that session has a negative time (this strange case occurs probability due to a system date change), or the session is of the wrong time																				
							
							// set modified queries stats and clicks stats
							String query=null;
							String url="TODO";
							int numResults=0;
							int rank=0;
							int clickOrderinSession=1;
							int clickOrderinQueryofSession=1;
							boolean isDatesChanged=false;
							for (int i=0;i<vprocessedEntries.size();i++) {
								ProcessedEntry entry=vprocessedEntries.get(i);
								if (entry.getAction()==UserAction.QUERY) {
									query=((QueryEntry)entry).getQuery();
									numResults=((QueryEntry)entry).getNumResults();
									isDatesChanged=((QueryEntry)entry).isDatesChanged();
									statsForModifiedQuery(query);
									clickOrderinQueryofSession=1;		
									System.err.println("QUERY: "+query);
								}
								else if (entry.getAction()==UserAction.SERP_CLICK && !isDatesChanged && numResults>0) {	// only counted queries without temporal restrictions and with results (not URL queries)																															
									rank=((ClickEntry)entry).getRank();
									clicks.addQueryClick(query, numResults, url, rank, oldSessionId, clickOrderinSession, clickOrderinQueryofSession);
									clickOrderinSession++;
									clickOrderinQueryofSession++;
								}
							}							
							
							/* TODO remove */
							int c1=totalSessionsWithoutClicksUrl;
							int c2=totalSessionsWithoutClicksFulltext;
							 /* */
							
							incrementCounters(seconds, oldIp);					
							totalQueriesCheck+=nQueriesSession;

							/* TODO remove */
							if (isSessionOnlyUrlQueries && c1<totalSessionsWithoutClicksUrl) { // it is a session without clicks
								boolean results0=true;
							    for (int i=0;i<vprocessedEntries.size();i++) {							    	
									if (vprocessedEntries.get(i).getAction()==UserAction.QUERY) {
										QueryEntry entry=(QueryEntry)vprocessedEntries.get(i);										
										System.err.println(i+" WITHOUT_CLICKS URL:"+" numResults:"+entry.getNumResults()+" "+entry.getQuery()+" IP:"+oldIp+" sid:"+oldSessionId+" on date:"+lastDate);
										if (entry.getNumResults()>0) {
											results0=false;
										}
									}
							    }	
							    if (results0) {
							    	totalSessionsWithoutClicksResults0Url++;
							    }
							}
							
							if (isSessionOnlyFullTextQueries && c2<totalSessionsWithoutClicksFulltext) { // it is a session without clicks
								boolean results0=true;
							    for (int i=0;i<vprocessedEntries.size();i++) {
							    	if (vprocessedEntries.get(i).getAction()==UserAction.QUERY) {
										QueryEntry entry=(QueryEntry)vprocessedEntries.get(i);	
										System.err.println(i+" WITHOUT_CLICKS FULL-TEXT:"+" numResults:"+entry.getNumResults()+" "+entry.getQuery()+" IP:"+oldIp+" sid:"+oldSessionId+" on date:"+lastDate);
										if (entry.getNumResults()>0) {
											results0=false;
										}
							    	}
							    }
							    if (results0) {							    	
							    	totalSessionsWithoutClicksResults0Fulltext++;
							    }
							}
														
							System.err.println("VALID IP: "+oldIp+" SECONDS: "+seconds+" lastDate: "+dformat.format(lastSessionDate)+" firstDate: "+dformat.format(firstSessionDate));
							System.err.println("NUM CLICKS: "+numWaybackClicks+" "+oldIp);
							/* */														
						}
						else { // wrong session
							// remove session if it is not valid
							if (LOGS_TO_SQL) {
								sqlOp.deleteSession(sessionKey);
							}
							
						    if (seconds<0) {
						    	System.err.println("Wrong bin for "+(seconds/60)+" minutes with IP "+oldIp+".");
						    }
						    else if (nQueriesSession>MAX_QUERIES_PER_SESSION) {
						    	System.err.println("IP "+oldIp+" sid:"+oldSessionId+" on date "+lastDate+" with too many queries:"+nQueriesSession);
						    }
						    else if (nQueriesSession<=0) {
						    	System.err.println("IP "+oldIp+" sid:"+oldSessionId+" on date "+lastDate+" without queries:"+nQueriesSession);
						    }
						    else { 
						    	System.err.println("IP "+oldIp+" sid:"+oldSessionId+" on date "+lastDate+" wrong type of session.");
						    }
						}							
					}					  																										
						
					oldSessionId=sessionId;
					oldIp=ip;	
					firstSessionDate=date;		
					lastSessionDate=date;	
					vlines=new Vector<LogEntry>();
					vprocessedEntries=new Vector<ProcessedEntry>();
					//nEntriesSession=0;
					newTerms=new HashMap<String,Integer>();		
					sessionQueries=new HashSet<String>();
					oldQuery=new String();
					nQueriesSession=0;
					waybackPagesSawAux=new HashMap<String,Integer>();
					nQueriesLangAux=new HashMap<String,Integer>();
					//lastQuery="";
				}
				totalSessions++;
			}					
				
			String query=parts[6];	
			UserAction action=null; 				
			boolean ignore=false;
			if (query.startsWith("/search.jsp?")) { // it is a nutchwax query
				query=query.substring(query.indexOf('?')+1);
								
				if (query.indexOf("&"+SHOW_VERSIONS_KEY+"=true")!=-1 && query.indexOf("&"+SHOW_VERSIONS_POS_KEY+"=")!=-1) { // must have the hist=true and pos=
					action=UserAction.HIST_CLICK;		
				}
				else if (query.matches(".*"+"\\&?"+QUERY_KEY+"="+".*")) {
					action=UserAction.QUERY;
					//lastQuery=query;
				}
				else if (query.matches(".*"+"\\&?"+ADV_QUERY_KEY+"="+".*")) {
					action=UserAction.ADV_QUERY;
				}						
				else {
					System.err.println("Wrong request of query: "+line);					
					ignore=true;					
				}
			}
			else if (query.matches("/wayback/id[0-9]+index[0-9]+.*")) { // it is a wayback click						
								
				Matcher matcher = Pattern.compile("id[0-9]+index[0-9]+").matcher(query);					
				if (!matcher.find()) {
					System.err.println("Wrong result - did not match!:"+query);
					System.exit(1);
				}				
				String page=matcher.group(0);				    							
				query=query.substring(query.indexOf('?')+1);
								
				if (query.matches(".*\\&?"+WAYBACK_POS_KEY+"="+".*") /*&& !lastQuery.equals("")*/) {
					incMapValue(waybackPagesSawAux,page);					
					action=UserAction.WAYBACK_CLICK;
										
					//System.out.println("OUTLIER: "+ip+" "+sessionid+" "+date+" "+query+" "+lastQuery);
				}
				else if (query.matches(".*\\&?"+SERP_POS_KEY+"="+".*")) {
					incMapValue(waybackPagesSawAux,page);				
					action=UserAction.SERP_CLICK;									
				} 			 	
			 	else {
					System.err.println("Wrong request of click: "+line);					
					ignore=true;					
				}
			}
			else {
				System.err.println("Wrong request: "+line);
				ignore=true;				
			}								
			
			if (!ignore) {
				vlines.add(new LogEntry(query,referrer,action,date));
				//System.out.println("REFERRER: "+referrer); TODO remove
			}
			//queryOld=query; TODO remove
		}				
		br.close();
		// discard last session by discarding last vlines
		
		if (LOGS_TO_SQL) {			
			sqlOp.close();
		}
	}		
	
	/**
	 * Reset variable counters
	 */
	private static void resetCounters() {
		// reset counters
		for (int i=0;i<pagesViewedDistAux.length;i++) {
			pagesViewedDistAux[i]=0;								
		}
		for (int i=0;i<docsClickedDistAux.length;i++) {
			docsClickedDistAux[i]=0;								
		}														
		totalClicksAux=0;
		for (int i=0;i<docsClickedHistoricDistAux.length;i++) {
			docsClickedHistoricDistAux[i]=0;								
		}														
		totalClicksHistoricAux=0;						
		for (int i=0;i<queryYearsDistAux.length;i++) {
			queryYearsDistAux[i]=0;								
		}							
		for (int i=0;i<waybackClickYearsDistAux.length;i++) {
			waybackClickYearsDistAux[i]=0;								
		}
		for (int i=0;i<waybackClickColumnsWithResultsDistAux.length;i++) {
			waybackClickColumnsWithResultsDistAux[i]=0;						
		}
		for (int i=0;i<waybackClickColumnsWithResultsTotalRowsDistAux.length;i++) {
			waybackClickColumnsWithResultsTotalRowsDistAux[i]=0;						
		}
		for (int i=0;i<waybackClickColumnsWithResultsTotalColumnsDistAux.length;i++) {
			waybackClickColumnsWithResultsTotalColumnsDistAux[i]=0;						
		}	
		for (int i=0;i<queryTimeBinsAux.length;i++) {
			queryTimeBinsAux[i]=0;								
		}
		for (int i=0;i<presentationTimeBinsAux.length;i++) {
			presentationTimeBinsAux[i]=0;								
		}					
		for (int i=0;i<waybackQueryTimeBinsAux.length;i++) {
			waybackQueryTimeBinsAux[i]=0;								
		}
		for (int i=0;i<waybackPresentationTimeBinsAux.length;i++) {
			waybackPresentationTimeBinsAux[i]=0;								
		}
		for (int k=0;k<advancedQueryFilters.length;k++) {
		    advQueriesDistAux[k]=0;
		}
		totalAdvQueriesAux=0;	      
		totalAdvQueriesOverlappingAux=0;
				
		totalStartDateChangedAux=0;
		totalEndDateChangedAux=0;
		totalStartEndDateChangedAux=0;
		totalAnyDateChangedAux=0;
		isSessionOnlyUrlQueries=true;
		isSessionOnlyFullTextQueries=true;
		
		totalQueriesURLsAux=0;						
		totalQueriesURLsWithQuotesAux=0;									
		totalQueriesURLsWithTermsAux=0;	
		
		queryPartsMapAux=new HashMap<String,Integer>(); 		
	}
	
	/**
	 * Increment counters with data from session
	 */
	private static void incrementCounters(long seconds, String oldIp) throws IOException {
		// set stats of SERPs viewed
		int numberPagesViewed=0;
		for (int i=0;i<pagesViewedDistAux.length;i++) {
			if (pagesViewedDistAux[i]>0) { 
				pagesViewedDist[i]+=pagesViewedDistAux[i];
				totalPagesViewed+=pagesViewedDistAux[i];
				if (i>0) {
				    pagesViewedNotFirstPage+=pagesViewedDistAux[i];
				}
				numberPagesViewed++;
			}									
		}	
			
		// set stats of clicks
		for (int i=0;i<docsClickedDistAux.length;i++) {
			docsClickedDist[i]+=docsClickedDistAux[i];								
		}
		totalClicks+=totalClicksAux;
		// set stats of clicks in historic to show all versions
		for (int i=0;i<docsClickedHistoricDistAux.length;i++) {
			docsClickedHistoricDist[i]+=docsClickedHistoricDistAux[i];								
		}
		totalClicksHistoric+=totalClicksHistoricAux;
		// set sessions without any type of clicks
		if (totalClicksAux==0 && totalClicksHistoricAux==0 && isSessionOnlyFullTextQueries) {
		    totalSessionsWithoutClicksFulltext++;
		}							
			
		// set years distribution from queries							
		for (int i=0;i<queryYearsDistAux.length;i++) {
			queryYearsDist[i]+=queryYearsDistAux[i];												
		}
		// set years distribution from clicks in wayback
		int totalWaybackClicksAux=0;
		for (int i=0;i<waybackClickYearsDistAux.length;i++) {
			waybackClickYearsDist[i]+=waybackClickYearsDistAux[i];	
			totalWaybackClicks+=waybackClickYearsDistAux[i];
			totalWaybackClicksAux+=waybackClickYearsDistAux[i];
		}	
		if (totalWaybackClicksAux==0 && isSessionOnlyUrlQueries) {
		    totalSessionsWithoutClicksUrl++;
		}		
		// set bins with results distribution from click in wayback rate of change
		for (int i=0;i<waybackClickColumnsWithResultsDistAux.length-1;i++) {			
		    waybackClickColumnsWithResultsDist[0][i]+=waybackClickColumnsWithResultsDistAux[i];				       
		    waybackClickColumnsWithResultsDist[1][i]+=waybackClickColumnsWithResultsDistAux[i+1];				       
		    waybackClickColumnsWithResultsDist[2][i]+=(waybackClickColumnsWithResultsDistAux[i]==0) ? waybackClickColumnsWithResultsTotalRowsDistAux[i] : waybackClickColumnsWithResultsTotalRowsDistAux[i]/waybackClickColumnsWithResultsDistAux[i]; // add only one time the total number of versions in this year
		    waybackClickColumnsWithResultsDist[3][i]+=(waybackClickColumnsWithResultsDistAux[i+1]==0) ? waybackClickColumnsWithResultsTotalRowsDistAux[i+1] : waybackClickColumnsWithResultsTotalRowsDistAux[i+1]/waybackClickColumnsWithResultsDistAux[i+1]; // add only one time the total number of versions in this year
		    waybackClickColumnsWithResultsDist[4][i]+=waybackClickColumnsWithResultsTotalColumnsDistAux[i];		    		    		   
		}
			
		// set valid sessions
		totalValidSessions++;
		if (seconds>0) {
			totalValidSessionsMoreThanOneQuery++;
		}	
		if (isSessionOnlyUrlQueries) {
			totalValidSessionsOnlyUrl++;
		}
		if (isSessionOnlyFullTextQueries) {
			totalValidSessionsOnlyFulltext++;		
		}
		
		// set distribtuions of # SERPs and # queries
		numberPagesViewedDist[numberPagesViewed>=N_PAGES_VIEWED_RANGE-1 ? numberPagesViewed=N_PAGES_VIEWED_RANGE-1 : numberPagesViewed]++;
		nQueriesSessionDist[nQueriesSession>nQueriesSessionDist.length-1 ? nQueriesSessionDist.length-1 : nQueriesSession]++;
			
		// set session duration						
		boolean stop=false;
		long minutes=seconds/60;
		for (int j=0;j<N_SESSION_TIME_BINS && !stop;j++) {
			if (j==N_SESSION_TIME_BINS-1 && minutes>=SESSION_TIME_BINS[j]) {
			    sessionTimeBins[j]++;
			    stop=true;
			}
			else if (minutes>=SESSION_TIME_BINS[j] && minutes<SESSION_TIME_BINS[j+1]) {
			    sessionTimeBins[j]++;
			    stop=true;
			}
		}
		if (!stop) { // sanity check
		   	throw new IOException("Wrong bin for "+minutes+" minutes with IP "+oldIp+".");
		}
		totalSeconds+=seconds;															
			
		// set number of sessions per IP
		incMapValue(nSessionsIP,oldIp);		
		
		// set query and presentation times							
		int c=0;
		for (int i=0;i<queryTimeBinsAux.length;i++) {
			queryTimeBins[i]+=queryTimeBinsAux[i];
			c+=queryTimeBinsAux[i];
		}
		if (c!=nQueriesSession) { // sanity check 
			throw new IOException("Wrong distribution of years per query time");
		}
		for (int i=0;i<presentationTimeBinsAux.length;i++) {
			presentationTimeBins[i]+=presentationTimeBinsAux[i];								
		}					
		for (int i=0;i<waybackQueryTimeBinsAux.length;i++) {
			waybackQueryTimeBins[i]+=waybackQueryTimeBinsAux[i];								
		}
		for (int i=0;i<waybackPresentationTimeBinsAux.length;i++) {
			waybackPresentationTimeBins[i]+=waybackPresentationTimeBinsAux[i];								
		}
		
		// set dates changed
		totalStartDateChanged+=totalStartDateChangedAux;
		totalEndDateChanged+=totalEndDateChangedAux;
		totalStartEndDateChanged+=totalStartEndDateChangedAux;		
		totalAnyDateChanged+=totalAnyDateChangedAux;
		
		// set queries with URLs
		totalQueriesURLs+=totalQueriesURLsAux;						
		totalQueriesURLsWithQuotes+=totalQueriesURLsWithQuotesAux;									
		totalQueriesURLsWithTerms+=totalQueriesURLsWithTermsAux;	

		// set advanced operators
		for (int k=0;k<advancedQueryFilters.length;k++) {
		    advQueriesDist[k]+=advQueriesDistAux[k];
		}
		totalAdvQueries+=totalAdvQueriesAux;	      
		totalAdvQueriesOverlapping+=totalAdvQueriesOverlappingAux;

		// set all entries, specially queries		
		for (Map.Entry<String,Integer> entry: queryPartsMapAux.entrySet()) {
		    String keyAux = entry.getKey();
            Integer valueAux = entry.getValue();
            Integer value = queryPartsMap.get(keyAux);
            queryPartsMap.put(keyAux, valueAux+(value!=null ? value : 0));
		}
		
		// set all pages saw		
		for (Map.Entry<String,Integer> entry: waybackPagesSawAux.entrySet()) {
		    String keyAux = entry.getKey();
            Integer valueAux = entry.getValue();
            Integer value = waybackPagesSaw.get(keyAux);
            waybackPagesSaw.put(keyAux, valueAux+(value!=null ? value : 0));
		}
		
		// set all lang	
		for (Map.Entry<String,Integer> entry: nQueriesLangAux.entrySet()) {
		    String keyAux = entry.getKey();
            Integer valueAux = entry.getValue();
            Integer value = nQueriesLang.get(keyAux);
            nQueriesLang.put(keyAux, valueAux+(value!=null ? value : 0));
		}					
	}
		
	/**
	 * Computes statistics
	 * @throws IOException totalUsers
	 */
	public static void stats() throws IOException {		
		// sort entries, such as queries
		Object entriesArray[]=EntriesSorter.sortQueries(queryPartsMap);
					
		// count users and users searching in only one session
		int totalUsers=nSessionsIP.size();
		int totalUsersSessionsDist[]=new int[N_USERS_SESSIONS_RANGE];		
		for (Iterator<Integer> iter=nSessionsIP.values().iterator();iter.hasNext();) {
			int nSessions=iter.next();
			totalUsersSessionsDist[nSessions>totalUsersSessionsDist.length-1 ? totalUsersSessionsDist.length-1 : nSessions]++;	
		}			
		
		// write queries statistics
		HashMap<String,Integer> termsMap=new HashMap<String,Integer>(); // has the number of times a term occur
		int totalQueries=0;
		int totalUniqueFullTextQueries=0; // variations of queries
		int totalOnlyOnceFullTextQueries=0; // occurs only once in logs			
		int totalUniqueUrlQueries=0; // variations of queries
		int totalOnlyOnceUrlQueries=0; // occurs only once in logs
		int totalTerms=0;
		int totalTermsWithoutUrl=0;
		int totalTermsWithoutUrlWithQuotes=0;
		int totalUniqueTerms=0;
		int totalOnlyOnceTerms=0; // occurs only once in logs
		String terms[];
		String saux;	
		int nQueries;		
		Integer value=null;
		int maxTermsPerQuery=0;
		int minTermsPerQuery=Integer.MAX_VALUE;
		int totalChars=0;
		int totalCharsWithoutUrl=0;
		int totalCharsWithoutUrlWithQuotes=0;
		int maxCharsPerTerm=0;
		int minCharsPerTerm=Integer.MAX_VALUE;	
					
		// iterate over all entries 
		for (int i=0;i<entriesArray.length;i++) {
			System.out.println(((Map.Entry)entriesArray[i]).getKey()  + " = " + (((Map.Entry)entriesArray[i]).getValue())); // write query
						
			// compute term stats
			saux=(String)((Map.Entry)entriesArray[i]).getKey();			
			terms=saux.split("\\s");
			if (terms[0].equals(NEW_QUERY_KEY)) {
				// check if it is a url query
				String queryAux="";
				for (int j=1;j<terms.length;j++) {
					queryAux+=terms[j]+" ";
				}
				boolean isQueryUrl=isUrlQuery(queryAux);
				boolean isQueryUrlWithQuotes=isUrlQueryWithQuotes(queryAux);
				
				// count once and unique
				nQueries=(Integer)((Map.Entry)entriesArray[i]).getValue();
				if (nQueries==1) {
					if (!isQueryUrl) {
						totalOnlyOnceFullTextQueries++;
					}
					else {
						totalOnlyOnceUrlQueries++;
					}
				}
				totalQueries+=nQueries;
				if (!isQueryUrl) {
					totalUniqueFullTextQueries++;
				}
				else {
					totalUniqueUrlQueries++;
				}
				
				// count url depth
				if (isQueryUrl) {
					urlDepthDist[UrlDepth.getUrlDepth(queryAux)]+=nQueries;
				}
				
				// count terms		
				int termsPerQuery=0;
				for (int j=1;j<terms.length;j++) {		

					if (terms[j].equals("")) {
						continue;
					}
					
					//if (!terms[j].equals("-")) { // the '-' operator occurs sometimes alone
					totalTerms+=nQueries;
					termsPerQuery++;
					totalChars+=terms[j].length()*nQueries;					
					if (!isQueryUrl) { // only remove url queries, but not queries with terms and urls
						totalTermsWithoutUrl+=nQueries;
						totalCharsWithoutUrl+=terms[j].length()*nQueries;						
					}
					if (!isQueryUrl && !isQueryUrlWithQuotes) { // only remove url queries or url queries with quotes, but not queries with terms and urls
						totalTermsWithoutUrlWithQuotes+=nQueries;
						totalCharsWithoutUrlWithQuotes+=terms[j].length()*nQueries;						
					}
															
					// min and max chars per term
					if (terms[j].length()>maxCharsPerTerm) {
						maxCharsPerTerm=terms[j].length();
					}
					if (terms[j].length()<minCharsPerTerm) {
						minCharsPerTerm=terms[j].length();
					}						
					
					terms[j]=terms[j].replaceAll("\\\"",""); // remove quotes from terms
					if ((value=termsMap.get(terms[j]))==null) {
						termsMap.put(terms[j],nQueries);
					}
					else {
						termsMap.put(terms[j],value+nQueries);				
					}					
				}	
															
				// termsPerQuery is 0 if the query has only the not operator ("-")
				if (!isQueryUrl) {
					nTermsQueryDist[termsPerQuery>nTermsQueryDist.length-1 ? nTermsQueryDist.length-1 : termsPerQuery]+=nQueries;	
				
					// min and max terms per query
					if (termsPerQuery>maxTermsPerQuery) {
						maxTermsPerQuery=termsPerQuery;
					}
					if (termsPerQuery<minTermsPerQuery) {
						minTermsPerQuery=termsPerQuery;
					}
				}
			}		
		}
		System.out.println("-----------------------");

		// sort terms		
		Object termsArray[]=EntriesSorter.sortTerms(termsMap);		
		// write terms stats
		for(int i=0;i<termsArray.length;++i) {
			System.out.println("term "+((Map.Entry)termsArray[i]).getKey()  + " = " + (((Map.Entry)termsArray[i]).getValue()));   
			value=(Integer)((Map.Entry)termsArray[i]).getValue();
			saux=(String)((Map.Entry)termsArray[i]).getKey();		
			
			boolean isQueryUrl=isUrlQuery(saux);
			if (!isQueryUrl) {
				if (value==1) {					
					totalOnlyOnceTerms++;
				}				
				totalUniqueTerms++;
			}
		}
		System.out.println("-----------------------");
		
		
		// sort pages		
		Object pagesArray[]=EntriesSorter.sortTerms(waybackPagesSaw);
		// write page stats
		for(int i=0;i<pagesArray.length;++i) {
			System.out.println("page "+((Map.Entry)pagesArray[i]).getKey()  + " = " + (((Map.Entry)pagesArray[i]).getValue()));   
			value=(Integer)((Map.Entry)pagesArray[i]).getValue();
			saux=(String)((Map.Entry)pagesArray[i]).getKey();							
		}
		System.out.println("-----------------------");			
		

		// write global stats				
		System.out.println("Total users (IPs): "+totalUsers);
		System.out.println("Distribution of users (IPs) returned to the system (# times returned, # IPs returned per # times, # IPs returned per # times / IPs):");
		for (int i=1;i<totalUsersSessionsDist.length;i++) {
			System.out.println(" "+i+", "+totalUsersSessionsDist[i]+", "+(float)totalUsersSessionsDist[i]/(float)totalUsers);
		}			
		System.out.println("Total sessions: "+totalValidSessions);		
		System.out.println("Total sessions with only URL queries (without full-text): "+totalValidSessionsOnlyUrl);		
		System.out.println("Total sessions with only Full-text queries (without URL): "+totalValidSessionsOnlyFulltext);
		System.out.println("Total sessions mixed (with Full-text + URL): "+(totalValidSessions-totalValidSessionsOnlyFulltext-totalValidSessionsOnlyUrl));	
		System.out.println("Total queries (first page): "+totalQueries);		
		System.out.println(" Total queries first page (CHECK): "+totalQueriesCheck);
		System.out.println(" Total Full-text queries: "+(totalQueries-totalQueriesURLs)+" %Total queries:"+(float)(totalQueries-totalQueriesURLs)/(float)totalQueries);
		System.out.println(" Total queries with only URLs (without quotes): "+totalQueriesURLs+" %Total queries:"+(float)totalQueriesURLs/(float)totalQueries);
		System.out.println(" Total queries with only URLs (with quotes): "+totalQueriesURLsWithQuotes+" %Total queries:"+(float)totalQueriesURLsWithQuotes/(float)totalQueries);
		System.out.println(" (IGNORE) Total queries with URLs and terms: "+totalQueriesURLsWithTerms+" %Total queries:"+(float)totalQueriesURLsWithTerms/(float)totalQueries);		
		System.out.println("Total queries/clicks for next pages: "+pagesViewedNotFirstPage);						
		for (Iterator<Map.Entry<String,Integer>> iter=nQueriesLang.entrySet().iterator();iter.hasNext();) { // lang
			Map.Entry<String,Integer> entry = (Map.Entry<String,Integer>) iter.next();
			System.out.println("Total queries of lang "+entry.getKey()+": "+entry.getValue()+" %Total queries: "+(float)entry.getValue()/(float)totalQueries);					
		}
		System.out.println("Stats for first pages:");				
		System.out.println(" Full-text Queries never repeated: "+totalOnlyOnceFullTextQueries);
		System.out.println(" Full-text Unique queries (variations): "+totalUniqueFullTextQueries);
		System.out.println(" URL Queries never repeated: "+totalOnlyOnceUrlQueries);
		System.out.println(" URL Unique queries (variations): "+totalUniqueUrlQueries);		
		System.out.println(" Modified queries (ignoring stopwords): "+totalModifiedQueries);
		System.out.println(" Queries with equal terms (with stopwords): "+totalEqualTermsQueries);
		System.out.println(" Queries with equal terms and same order (with stopwords): "+totalEqualTermsOrderQueries);
		System.out.println(" Queries with equal terms, but all stopwords: "+totalEqualTermsStopwordsQueries);
		System.out.println(" Identical queries (exactly equal): "+totalIdenticalQueries);
		System.out.println(" Identical queries within all session (exactly equal): "+totalIdenticalSessionQueries);
		System.out.println(" Terms never repeated: "+totalOnlyOnceTerms);
		System.out.println(" Unique terms (variations): "+totalUniqueTerms);
		System.out.println(" Modified queries, terms exchanged distribution:");
		for (int k=0;k<nModifiedTerms.length;k++) {
			System.out.println("  "+(k-(N_MODIFIED_TERMS_RANGE/2))+": "+nModifiedTerms[k]);
		}			
		
		totalAdvQueries+=/*totalQueriesURLs+totalStartDateChanged+totalEndDateChanged*/-totalAdvQueriesOverlapping; 
		System.out.println(" Total advanced queries: "+totalAdvQueries+" %Total queries:"+(float)totalAdvQueries/(float)totalQueries);	    
		for (int k=0;k<advancedQueryFilters.length;k++) {
			System.out.println("  "+advancedQueryFilters[k]+": "+advQueriesDist[k]+" %Advanced queries:"+(float)advQueriesDist[k]/(float)totalAdvQueries+" %Total queries:"+(float)advQueriesDist[k]/(float)totalQueries);
		}	    										
		System.out.println(" Total queries with advanced operators overlapped: "+totalAdvQueriesOverlapping);
		System.out.println(" Total queries from advanced interface: "+totalAdvInterfaceQueries+" %Total queries:"+(float)totalAdvInterfaceQueries/(float)totalQueries);
		
		System.out.println(" Total queries with start date changed: "+totalStartDateChanged+" %Total queries:"+(float)totalStartDateChanged/(float)totalQueries);
		System.out.println(" Total queries with end date changed: "+totalEndDateChanged+" %Total queries:"+(float)totalEndDateChanged/(float)totalQueries);
		System.out.println(" Total queries with start and end date changed: "+totalStartEndDateChanged+" %Total queries:"+(float)totalStartEndDateChanged/(float)totalQueries);		
		System.out.println(" Total queries with any date resctriction: "+totalAnyDateChanged+" %Total queries:"+(float)totalAnyDateChanged/(float)totalQueries);
		//System.out.println("  totalQueriesURLsAndDatesChanged: "+totalQueriesURLsAndDatesChanged);  
		
		//int totalAdvClicks=0;
		//System.out.println(" Total advanced clicks: "+totalAdvClicks+" %Total clicks:"+(float)totalAdvQueries/(float)totalQueries);		
		//System.out.println("  clicks/queries to show versions: "+totalQueriesShowVersions+" %Advanced queries:"+(float)totalQueriesShowVersions/(float)totalAdvQueries+" %Total queries:"+(float)totalQueriesShowVersions/(float)totalQueries);			
		
		System.out.println(" Avg. Queries per session: "+(float)totalQueries/(float)totalValidSessions);
		System.out.println(" Min. Queries per session: "+minQueriesPerSession);
		System.out.println(" Max. Queries per session: "+maxQueriesPerSession);
 		System.out.println(" Stdev. Queries per session: ?");
		System.out.println(" Total Terms: "+totalTerms);
		System.out.println(" Total Terms without URL queries: "+totalTermsWithoutUrl);		
		System.out.println(" Total Terms without URL queries and URL queries in quotes: "+totalTermsWithoutUrlWithQuotes);		
		System.out.println(" Avg. Terms per query: "+(float)totalTerms/(float)totalQueries);
		System.out.println(" Min. Terms per query: "+minTermsPerQuery);
		System.out.println(" Max. Terms per query: "+maxTermsPerQuery);
		System.out.println(" Stdev. Terms per query: ?");
		System.out.println(" Avg. Characters per term: "+(float)totalChars/(float)totalTerms);
		System.out.println(" Avg. Characters per term without URL queries: "+(float)totalCharsWithoutUrl/(float)totalTermsWithoutUrl);
		System.out.println(" Avg. Characters per term without URL queries and URL queries in quotes: "+(float)totalCharsWithoutUrlWithQuotes/(float)totalTermsWithoutUrlWithQuotes);
		System.out.println(" Min. Characters per term: "+minCharsPerTerm);
		System.out.println(" Max. Characters per term: "+maxCharsPerTerm);
		System.out.println(" Stdev. Characters per term: ?");			
		System.out.println(" Queries per session distribution:"); 
		for (int i=1;i<nQueriesSessionDist.length;i++) {
			System.out.println("  "+i+": "+nQueriesSessionDist[i]);
		}
		System.out.println( "Terms per query distribution:"); 
		for (int i=1;i<nTermsQueryDist.length;i++) {
			System.out.println("  "+i+": "+nTermsQueryDist[i]);
		}
		System.out.println("Total SERPs viewed: "+totalPagesViewed);
		System.out.println("Avg. SERPs viewed per session: "+(float)totalPagesViewed/(float)totalValidSessionsOnlyFulltext);
		System.out.println("Avg. SERPs viewed per query: "+(float)totalPagesViewed/(float)(totalQueries-totalQueriesURLs));
		System.out.println("SERPs viewed distribution:");
		for (int i=0;i<pagesViewedDist.length;i++) {
			System.out.println(" "+(i+1)+": "+pagesViewedDist[i]);
		}
		System.out.println("Number of SERPs viewed per session distribution:");
		for (int i=1;i<numberPagesViewedDist.length;i++) {
			System.out.println(" "+i+": "+numberPagesViewedDist[i]);
		}
		
		System.out.println("Total full-text clicks: "+totalClicks);
		System.out.println("Avg. Clicks per full-text session: "+(float)totalClicks/(float)totalValidSessionsOnlyFulltext);
		System.out.println("Avg. Clicks per full-text query: "+(float)totalClicks/(float)(totalQueries-totalQueriesURLs));
		System.out.println("Full-text Sessions without any type of click: "+totalSessionsWithoutClicksFulltext+"  %:"+(float)totalSessionsWithoutClicksFulltext/(float)totalValidSessionsOnlyFulltext);			
		System.out.println(" Full-text Sessions without any type of click due to 0 results in all queries: "+totalSessionsWithoutClicksResults0Fulltext+"  %:"+(float)totalSessionsWithoutClicksResults0Fulltext/(float)totalValidSessionsOnlyFulltext);
		System.out.println(" Full-text Sessions without any type of click with at least one query >0 results: "+(totalSessionsWithoutClicksFulltext-totalSessionsWithoutClicksResults0Fulltext)+"  %:"+(float)(totalSessionsWithoutClicksFulltext-totalSessionsWithoutClicksResults0Fulltext)/(float)totalValidSessionsOnlyFulltext);
		
		System.out.println("Clicks distribution in full-text queries (rank, total clicks):");
		for (int i=1;i<docsClickedDist.length;i++) {
			System.out.println(" "+i+": "+docsClickedDist[i]);
		}
		
		System.out.println("Total clicks to show all versions: "+totalClicksHistoric);
		System.out.println("Avg. Clicks per session to show all versions: "+(float)totalClicksHistoric/(float)totalValidSessionsOnlyFulltext);
		System.out.println("Avg. Clicks per query to show all versions: "+(float)totalClicksHistoric/(float)(totalQueries-totalQueriesURLs));
		System.out.println("Clicks distribution to show all versions:");
		for (int i=1;i<docsClickedHistoricDist.length;i++) {
			System.out.println(" "+i+": "+docsClickedHistoricDist[i]);
		}		
		
		System.out.println("Years distribution from queries:");
		for (int i=0;i<queryYearsDist.length;i++) {							
			System.out.println(" "+(i+FIRST_YEAR)+": "+queryYearsDist[i]);
		}
		System.out.println("Total of URL clicks: "+totalWaybackClicks);
		System.out.println("Avg. clicks per URL session: "+(float)totalWaybackClicks/(float)totalValidSessionsOnlyUrl);
		System.out.println("Avg. clicks per URL query: "+(float)totalWaybackClicks/(float)totalQueriesURLs);		
		System.out.println("URL Sessions without any type of click: "+totalSessionsWithoutClicksUrl+"  %:"+(float)totalSessionsWithoutClicksUrl/(float)totalValidSessionsOnlyUrl);
		System.out.println(" URL Sessions without any type of click due to 0 results in all queries: "+totalSessionsWithoutClicksResults0Url+"  %:"+(float)totalSessionsWithoutClicksResults0Url/(float)totalValidSessionsOnlyUrl);
		System.out.println(" URL Sessions without any type of click with at least one query >0 results: "+(totalSessionsWithoutClicksUrl-totalSessionsWithoutClicksResults0Url)+"  %:"+(float)(totalSessionsWithoutClicksUrl-totalSessionsWithoutClicksResults0Url)/(float)totalValidSessionsOnlyUrl);
					
		System.out.println("Years distribution from wayback clicks:");
		for (int i=0;i<waybackClickYearsDist.length;i++) {							
			System.out.println(" "+(i+FIRST_YEAR)+": "+waybackClickYearsDist[i]);
		}
		System.out.println("Columns with results distribution from wayback clicks:");
		for (int i=0;i<waybackClickColumnsWithResultsDist[0].length;i++) {							
			System.out.println(" "+(i+1)+": clicks: "+waybackClickColumnsWithResultsDist[0][i]+", "+waybackClickColumnsWithResultsDist[1][i]+" total rows: "+waybackClickColumnsWithResultsDist[2][i]+", "+waybackClickColumnsWithResultsDist[3][i]+" total columns: "+waybackClickColumnsWithResultsDist[4][i]);
		}						

		System.out.println("Avg. Time per Session (sec): "+(float)totalSeconds/(float)totalValidSessions);
		System.out.println("Avg. Time per Session More than 1 Query (sec): "+(float)totalSeconds/(float)totalValidSessionsMoreThanOneQuery);					
		System.out.println("Session Time distribution (min):");
		for (int i=0;i<N_SESSION_TIME_BINS;i++) {
		    if (i==N_SESSION_TIME_BINS-1) {
		    	System.out.print(" ["+SESSION_TIME_BINS[i]+",inf[ ");
		    }
		    else {
		    	System.out.print(" ["+SESSION_TIME_BINS[i]+","+SESSION_TIME_BINS[i+1]+"[ ");
		    }
		    System.out.println(""+sessionTimeBins[i]);
		}
		
		System.out.println("Nutchwax query time distribution for all queries (sec):");
		for (int i=0;i<N_QUERY_TIME_BINS;i++) {
			System.out.println(" ["+i+","+(i+1)+"[ " + queryTimeBins[i]);
		}
		System.out.println("Nutchwax query+presentation time distribution for all queries (sec):");
		for (int i=0;i<N_QUERY_TIME_BINS;i++) {
			System.out.println(" ["+i+","+(i+1)+"[ " + presentationTimeBins[i]);
		}
		System.out.println("Wayback query time distribution of clicks to see historic (sec):");
		for (int i=0;i<N_QUERY_TIME_BINS;i++) {
			System.out.println(" ["+i+","+(i+1)+"[ " + waybackQueryTimeBins[i]);
		}
		System.out.println("Wayback query+presentation time distribution of clicks to see historic (sec):");
		for (int i=0;i<N_QUERY_TIME_BINS;i++) {
			System.out.println(" ["+i+","+(i+1)+"[ " + waybackPresentationTimeBins[i]);
		}

		System.out.println("URL depth of URL queries:");
		for (int i=0;i<urlDepthDist.length;i++) {
			System.out.println(" depth:"+i+": "+urlDepthDist[i]);
		}			
		
		clicks.writeStats();
		
		System.out.println();
	}

	/**
	 * Add log entry to statistics
	 * @param query log entry
	 * @return submitted query and number of query results
	 * @throws IOException 
	 */
    private static QueryEntry addQueryEntry(String query) throws IOException {			
		String submittedQuery=null;		
		boolean isNextPage=false;
		boolean isStartDateChanged=false;	
		boolean isEndDateChanged=false;	
		int startYear=FIRST_YEAR;
		int endYear=LAST_YEAR;
		int startMonth=FIRST_MONTH;
		int endMonth=LAST_MONTH;
		int startDay=FIRST_DAY;
		int endDay=LAST_DAY;
		String key=null;
		boolean docsProcessed=false; // identifies if 'docs' occurs more than once in the query		
		boolean termsProcessed=false; // identifies if 'terms' occurs more than once in the query    
		String queryParts[]=normalizeQuery(query).split("&"); // get parameters from query		
		boolean isUrlQuery=false;		
		int numResults=-1;
		
		// identify if it is a URL query
		for (int i=0;i<queryParts.length;i++) { // verifies if it is a next page
			String queryPartsFields[]=queryParts[i].split("=",2);			
			queryPartsFields[0]=queryPartsFields[0].trim();
			if (queryPartsFields.length>1) {
				queryPartsFields[1]=queryPartsFields[1].trim();
			}	
					
			if (queryPartsFields[0].equals(QUERY_KEY) && queryPartsFields.length>1 && !queryPartsFields[1].equals("")) {
				if (isUrlQuery(queryPartsFields[1])) {
				    isUrlQuery=true;
				    isSessionOnlyFullTextQueries=false;
				}
				else {
				    isSessionOnlyUrlQueries=false;
				}
			}
		}		
		// check if it is a valid query
		if (statsComputed==StatsComputed.FULL_TEXT && isUrlQuery) {
		    return null;
		}
		if (statsComputed==StatsComputed.URL && !isUrlQuery) {
		    return null;
		}
		
		// identify if it is a next page request
		for (int i=0;i<queryParts.length;i++) { // verifies if it is a next page
			String queryPartsFields[]=queryParts[i].split("=",2);			
			queryPartsFields[0]=queryPartsFields[0].trim();
			if (queryPartsFields.length>1) {
				queryPartsFields[1]=queryPartsFields[1].trim();
			}
			
			if (queryPartsFields[0].equals(DOCS_KEY) && queryPartsFields.length>1 && !queryPartsFields[1].equals("")) {			
				int ipage;
				try {
					ipage=Integer.parseInt(queryPartsFields[1]);
				}
				catch (NumberFormatException e) {
					ipage=0;
				}
				
				if (ipage!=0) { // regist only pages>0 							
					isNextPage=true;			
					if (ipage%RESULTS_PER_PAGE!=0) {
						System.err.println("Page results not multiple of "+RESULTS_PER_PAGE+": "+ipage);
						ipage+=ipage%RESULTS_PER_PAGE;
					}
					if (!docsProcessed) {
						int index=ipage/RESULTS_PER_PAGE>pagesViewedDistAux.length-1 ? pagesViewedDistAux.length-1 : ipage/RESULTS_PER_PAGE;			
						if (index==0) { // sanity check
						    throw new IOException("Error of index=0 in a next page. ipage:"+ipage);
						}
						pagesViewedDistAux[index]++;						
					}
					docsProcessed=true;				
				}						
			}
			else if (queryPartsFields[0].equals(QUERY_KEY) && queryPartsFields.length>1 && !queryPartsFields[1].equals("")) {
				// filter queries					
				boolean ignore=false;
				for (int j=0;!ignore && j<toolsQueryFilters.length;j++) {
					if (queryPartsFields[1].indexOf(toolsQueryFilters[j])!=-1) {
						ignore=true;
					}						
				}
				if (!ignore && !queryFiltersMap.contains(queryPartsFields[1])) {				 
					termsProcessed=true;
				}
			}			
		}
		// check wrong entries
		if (docsProcessed && !termsProcessed) {
			System.err.println("Error of query with docs without terms: "+query);
			return null;
		}				
		if (isNextPage) {
			return null;
		}
		if (!termsProcessed) { // empty query
			return null;
		}
		
		// count only first page
		docsProcessed=false;
		boolean isLang=false;
		boolean isAdvQuery=false;
		for (int i=0;i<queryParts.length;i++) {									
			String queryPartsFields[]=queryParts[i].split("=",2);			
			queryPartsFields[0]=queryPartsFields[0].trim();		
			
			if (queryPartsFields.length!=2 || queryPartsFields[1].equals("")) {
				continue;
			}																																			   
			if (queryPartsFields[0].equals(QUERY_KEY) && queryPartsFields.length==2) { 					
				// set submitted query			
				if (submittedQuery!=null) { // sanity check
					throw new IOException("Submitted query already set.");
				}
				
				// normalize query
				submittedQuery=queryPartsFields[1];						
				if (isUrlQuery(submittedQuery)) { // has only URL in query
					totalQueriesURLsAux++;	
				}
				if (isUrlQueryWithQuotes(submittedQuery)) { // has only URL with quotes
					totalQueriesURLsWithQuotesAux++;						
				}		
				/* it has a bug in the pattern
				if (hasUrlInQuery(submittedQuery)) { // has URL in query with other terms 
					totalQueriesURLsWithTermsAux++;						
				}
				*/
								
				// count advanced queries		
				String terms[]=submittedQuery.split("\\s");
				for (int j=0;j<terms.length;j++) {		
					if (terms[j].equals("")) {
						continue;
					}
				
					// count advanced terms						
					for (int k=0;k<advancedQueryFilters.length;k++) {
						if (terms[j].startsWith(advancedQueryFilters[k])) {
							advQueriesDistAux[k]++;
							totalAdvQueriesAux++;
							if (isAdvQuery) {
								totalAdvQueriesOverlappingAux++;
							}
							isAdvQuery=true;
						}
					}
				}				
					
				// count queries by query text 
				key=NEW_QUERY_KEY+" "+queryPartsFields[1];			
				incMapValue(queryPartsMapAux,key);								
			
				// count queries per session	
				nQueriesSession++;
				
				// set first page viewed
				pagesViewedDistAux[0]++;
			}
			else if (queryPartsFields[0].equals(START_DATE_CHANGED_KEY) && queryPartsFields[1].equals("true")) { // count start date			       
				isStartDateChanged=true;
			}
			else if (queryPartsFields[0].equals(END_DATE_CHANGED_KEY) && queryPartsFields[1].equals("true")) { // count end date			    
				isEndDateChanged=true;
			}		
			else if (queryPartsFields[0].equals(START_DATE_KEY)) { // get start date
				boolean err=false;
				try {
					startYear=Integer.parseInt(queryPartsFields[1].substring(6,10));
					startMonth=Integer.parseInt(queryPartsFields[1].substring(3,5));
					startDay=Integer.parseInt(queryPartsFields[1].substring(0,2));
				}
				catch (NumberFormatException e) {
					err=true;	
				}
				catch (StringIndexOutOfBoundsException e) {
					err=true;	
				}
				
				if (err || startYear<FIRST_YEAR) {
					System.err.println("Wrong year in query: "+startYear+" query:"+query);
					startYear=FIRST_YEAR;
				}				
			}
			else if (queryPartsFields[0].equals(END_DATE_KEY)) { // get end date
				boolean err=false;
				try {
					endYear=Integer.parseInt(queryPartsFields[1].substring(6,10));
					endMonth=Integer.parseInt(queryPartsFields[1].substring(3,5));
					endDay=Integer.parseInt(queryPartsFields[1].substring(0,2));
				}
				catch (NumberFormatException e) {
					err=true;					
				}
				catch (StringIndexOutOfBoundsException e) {
					err=true;
				}
												
				if (err || endYear>LAST_YEAR) {
					System.err.println("Wrong year in query: "+endYear+" query:"+query);
					endYear=LAST_YEAR;
				}
			}
			else if (queryPartsFields[0].equals(LANG_KEY)) { // group queries by lang
				if (!isLang) {
					incMapValue(nQueriesLangAux,queryPartsFields[1]);  // TODO: make it auxiliary
				}
				isLang=true;				
			}	
			else if (queryPartsFields[0].equals(QUERY_TIME_KEY)) { // group queries by time spent
				int time=Integer.parseInt(queryPartsFields[1])/1000;
				if (time>=N_QUERY_TIME_BINS) {
					time=N_QUERY_TIME_BINS-1;
				}
				queryTimeBinsAux[time]++;	
				
				if (time>3) {
					System.out.println("Slow queries (>3 sec): "+normalizeQuery(query));
				}
			}
			else if (queryPartsFields[0].equals(QUERY_TOTAL_TIME_KEY)) { // group queries by time spent
				int time=Integer.parseInt(queryPartsFields[1])/1000;
				if (time>=N_QUERY_TIME_BINS) {
					time=N_QUERY_TIME_BINS-1;
				}
				presentationTimeBinsAux[time]++;
			}
			else if (queryPartsFields[0].equals(NUMBER_RESULTS)) { 
 			    numResults=Integer.parseInt(queryPartsFields[1]);
			}
			else { // TODO remove
				key=queryPartsFields[0]+" "+queryPartsFields[1];
				incMapValue(queryPartsMapAux,key);
			}							
		}
		// I ignore last session on purpose because it can be incomplete
		
		// set default lang if not specified in request
		if (!isLang) {
			incMapValue(nQueriesLangAux,DEFAULT_LANG);			
		}
		
		// set years distribution from queries
		if (isStartDateChanged || isEndDateChanged) {					
			for (int i=startYear;i<=endYear;i++) {
				queryYearsDistAux[i-FIRST_YEAR]++;				
			}
			totalAnyDateChangedAux++;					
		}
		if (isStartDateChanged && isEndDateChanged) {
			totalStartEndDateChangedAux++;
		}
		else if (isStartDateChanged) {
		    totalStartDateChangedAux++;  
		}
		else if (isEndDateChanged) {
		    totalEndDateChangedAux++;  
		}

				
		/* TODO remove
		if (isStartDateChanged && isEndDateChanged) {
			totalStartEndDateQueriesOverlapping++;
		}
		*/
		
		//boolean isDatesChanged=isStartDateChanged || isEndDateChanged; // TODO bug in the logging of the endDate that records always true
		boolean isDatesChanged= !((startYear==FIRST_YEAR && startMonth==FIRST_MONTH && startDay==FIRST_DAY) && (endYear==LAST_YEAR && endMonth==LAST_MONTH && endDay==LAST_DAY)); 					
		//System.err.println("YYYYY: "+isDatesChanged+" start: "+startYear+" "+startMonth+" "+startDay+" end: "+endYear+" "+endMonth+" "+endDay);		
		return new QueryEntry(submittedQuery,numResults,isDatesChanged);
	}
	
	/**
	 * Add log entry to statistics
	 * @param query log entry
	 * @return submitted query and number of query results
	 * @throws IOException 
	 */
    private static QueryEntry addAdvancedQueryEntry(String query) throws IOException {
		totalAdvInterfaceQueries++;
		
		// TODO - all hard coded - change this
		
		// convert query from advanced interface to advanced query - easier to be accounted by statistics
		String queryParts[]=query.split("&"); // get parameters from query
		StringBuffer sbuf=new StringBuffer();
		StringBuffer squery=new StringBuffer();
		for (int i=0;i<queryParts.length;i++) {
			String queryPartsFields[]=queryParts[i].split("=",2);
			
			if (queryPartsFields[1].equals("")) {
				continue;
			}			
			queryPartsFields[0]=queryPartsFields[0].trim();
			
			if (queryPartsFields[0].equals("adv_and")) {
				squery.append(queryPartsFields[1]+" ");
			}
			else if (queryPartsFields[0].equals("adv_phr")) {
				squery.append("\""+queryPartsFields[1]+"\" ");
			} 
			else if (queryPartsFields[0].equals("adv_not")) {
				squery.append("-"+queryPartsFields[1]+" ");
			}
			else if (queryPartsFields[0].equals("sort")) {
				if (!queryPartsFields[1].equals("relevance")) {
					squery.append("sort:"+queryPartsFields[1]+" ");	
				}				
			}
			else if (queryPartsFields[0].equals("site")) {
				squery.append("site:"+queryPartsFields[1]+" ");								
			}
			else if (queryPartsFields[0].equals("format")) {
				if (!queryPartsFields[1].equals("all")) {
					squery.append("type:"+queryPartsFields[1]+" ");
				}
			}
			else {
				sbuf.append(queryParts[i]+"&");
			}			
		}
		sbuf.append("query="+squery);
		return addQueryEntry(sbuf.toString());
	}	

	/**
	 * Verifies if it is a query with a URL to present wayback results
	 * @param query query
	 * @return
	 */
	public static boolean isUrlQuery(String query) throws IOException {				
		return query.matches(URL_QUERY_PATTERN);
	}	
	
	/**
	 * Verifies if it is a query with a URL with quotes
	 * @param query query
	 * @return
	 */
	public static boolean isUrlQueryWithQuotes(String query) throws IOException {				
		return query.matches(URL_QUERY_PATTERN_WITH_QUOTES);
	}
	
	/**
	 * Verifies if it is a query with a URL to present wayback results
	 * @param query query
	 * @return
	 */
	public static boolean hasUrlInQuery(String query) throws IOException {				
		return query.matches(URL_QUERY_PATTERN_WITH_TERMS);
	}
	
	/**
	 * Increment one unit the map value giving a @key
	 * @param map map
	 * @param key map key
	 */
	public static void incMapValue(HashMap<String,Integer> map, String key) {
		Integer i=map.get(key);
		if (i!=null) {
			map.put(key,i+1);
		}
		else {
			map.put(key,1);
		}
	}
	
	/**
	 * Compute statistics for modified queries when a query is added
	 * @param query submitted query
	 * @return 
	 * @throws IOException 
	 */
	private static void statsForModifiedQuery(String query) {
		HashMap<String,Integer> oldTerms=newTerms; // record the terms to analyze modified queries
		newTerms=new HashMap<String,Integer>();
				
		String terms[]=query.split("\\s");				
		int matchingTerms=0;
		int matchingTermsStopwords=0;
		int matchingTermsOrder=0;
		for (int j=0;j<terms.length;j++) {
			if (!terms[j].equals("")) {
			   	if (oldTerms.containsKey(terms[j])) {
			   		matchingTerms++;					    
			   		if (stopwordsMap.contains(terms[j])) { // match if it is stopword
			   			matchingTermsStopwords++;
			   		}
			   		if (oldTerms.get(terms[j])==j) { // match if it has the same order
			   			matchingTermsOrder++;
			   		}
			   	}
			   	newTerms.put(terms[j],j);
			}
		}	

		if (newTerms.size()==oldTerms.size() && matchingTerms==oldTerms.size() && matchingTerms>0) { // the queries have the same terms
		    totalEqualTermsQueries++;
				    				    						    				  				    
		    if (matchingTermsStopwords==oldTerms.size()) {
		    	totalEqualTermsStopwordsQueries++;
		    }
		    if (matchingTermsOrder==oldTerms.size()) {
		    	totalEqualTermsOrderQueries++;
		    }
		    if (query.equals(oldQuery)) {
		    	totalIdenticalQueries++;
		    }
		    if (sessionQueries.contains(query)) {
		    	totalIdenticalSessionQueries++;
		    }
		}
		else if (matchingTerms-matchingTermsStopwords>0) { // the queries have at least one term equal and diferent from a stopword
		    totalModifiedQueries++;
		    int index=newTerms.size()-oldTerms.size()+N_MODIFIED_TERMS_RANGE/2;
		    if (index<0) {
		    	index=0;
		    }
		    else if (index>N_MODIFIED_TERMS_RANGE-1) {
		    	index=N_MODIFIED_TERMS_RANGE-1;
		    }
		    nModifiedTerms[index]++;
		}
		oldQuery=query;    
						
		// store session query
		sessionQueries.add(query);
	}							
								
	/**
	 * Add log entry of a click to statistics
	 * @param query log entry
	 * @throws IOException 
	 */
	private static ClickEntry addClickEntry(String query, IClickProcessor proc) throws IOException {					
		wayback_col_pos_log=0; // TODO remove
		wayback_col_total_log=0; // TODO remove
		wayback_pos_total_log=0; // TODO remove
		ClickEntry entryAux=null;
		ClickEntry entry=null;
		
		String queryParts[]=query.split("&"); // get parameters from query			
		for (int i=0;i<queryParts.length;i++) { 
			String queryPartsFields[]=queryParts[i].split("=",2);			
			queryPartsFields[0]=queryPartsFields[0].trim();
			if (queryPartsFields.length>1) {
				queryPartsFields[1]=queryPartsFields[1].trim();
			}
			
			if (queryPartsFields.length>1) {
				entryAux=proc.exec(queryPartsFields[0],queryPartsFields[1]);	
				if (entryAux!=null) {
					entry=entryAux;
				}
			}
		}
		
		return entry;
	}



	/**
	 * Reformat log file for sessionid sort
	 * @param logfile log file
	 * @throws IOException 
	 * @note then sort @logfile by sessionid before using @stats
	 */
	public static void reformat(String logfile) throws IOException {								
		String nutchwaxIdStr="\"GET /search.jsp";
		String waybackIdStr="\"GET /wayback";
		int ignoreFirstAttributes=0;
		
		String line;			
		BufferedReader br = new BufferedReader( new FileReader(logfile) );
		//BufferedReader br = new BufferedReader( new InputStreamReader(new FileInputStream(new File(logfile)),"ISO8859-1") );
		while ( ( line = br.readLine() ) != null ) {	
			
			if (line.indexOf(nutchwaxIdStr)!=-1) {
				ignoreFirstAttributes=6;				
			}
			else if (line.indexOf(waybackIdStr)!=-1) {
				ignoreFirstAttributes=1;				
			}
			else {				
				continue;
			}			
			
			String parts[]=line.split("\\s");
			//String ip=parts[IGNORE_FIRST_ATTRIBUTES];			
			String method=parts[ignoreFirstAttributes+6];					
			if (!method.equals("\"GET")) {
				continue;
			}
			String sessionId=parts[ignoreFirstAttributes+3];					
			if (sessionId.equals("") || sessionId.equals("null")) {
				continue;
			}
			
			for (int i=ignoreFirstAttributes+1; i<parts.length; i++) {
				if (i!=ignoreFirstAttributes+1) {
					System.out.print(" ");
				}
				System.out.print(parts[i]);
			}
			System.out.println();
		}
		br.close();
	}

	/**
	 * Decode Numeric Character Reference. It is the term often used to designate a character written in hexadecimal or decimal format in XML
	 * @param str string
	 * @return decoded string
	 */
	public static String decodeNCR(String str) {
		int i;				    	
		while ((i=str.indexOf("\\x"))!=-1) {
			StringBuffer buf=new StringBuffer();
			buf.append(str.substring(0,i));
			buf.append((char)Integer.parseInt(str.substring(i+2,i+4),16));
			buf.append(str.substring(i+4));
			str=buf.toString();
		}
		return str;
	}

	/**
	 * Decode some strings
	 * @param str string
	 * @return decoded string
	 */
	public static String decodeStrings(String str) {
		return str.replaceAll("\\\\\"","\\\"").replaceAll("[+]", " "); // remove "\"" and "+"
	}

	/**
	 * Normalize query request
	 * @param query query
	 * @return normalized query request
	 * @throws IOException
	 */
	public static String normalizeQuery(String query) throws IOException {
		try {																		
			query=decodeStrings(decodeNCR(java.net.URLDecoder.decode(query,"UTF-8").toLowerCase()));
		}
		catch(IllegalArgumentException e) {
			query=decodeStrings(decodeNCR(query.toLowerCase()));
		}		
		
		// remove spaces
		String terms[]=query.split("\\s");
		query="";
		for (int j=0,k=0;j<terms.length;j++) {					
			if (terms[j].equals("")) {
				continue;
			}					
			if (k>0) {
				query+=" ";	
			}
			query+=terms[j];					
			k++;
		}
		
		return query;
	}
	
	/**
	 * Get query dates in string
	 * @param query query
	 * @return query dates string
	 * @throws IOException
	 */
	public static String getQueryDatesString(String query) throws IOException {
		String normalizedQuery=normalizeQuery(query);
		String dateStart=null;
		String dateEnd=null;
		String isDateStart=null;
		String isDateEnd=null;
		
		int indexDateStart=normalizedQuery.indexOf("datestart");
		if (indexDateStart!=-1) {
			dateStart=normalizedQuery.substring(indexDateStart+10,indexDateStart+20);
		}
		int indexDateEnd=normalizedQuery.indexOf("dateend");
		if (indexDateEnd!=-1) {
			dateEnd=normalizedQuery.substring(indexDateEnd+8,indexDateEnd+18);
		}
		
		int indexIsDateStartChanged=normalizedQuery.indexOf("str_date_changed");
		if (indexIsDateStartChanged!=-1) {
			isDateStart=normalizedQuery.substring(indexIsDateStartChanged+17,indexIsDateStartChanged+21);
		}
		int indexIsDateEndChanged=normalizedQuery.indexOf("end_date_changed");
		if (indexIsDateEndChanged!=-1) {
			isDateEnd=normalizedQuery.substring(indexIsDateEndChanged+17,indexIsDateEndChanged+21);
		}
		
		if (isDateStart.equals("fals") && isDateEnd.equals("fals")) {
			return "";
		}
		return dateStart+" (changed:"+isDateStart+") "+dateEnd+" (changed:"+isDateEnd+")";
	}								
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		/*
		try {
			String x="http://blog_it.blogs.sapo.pt/data/rss";
			System.out.println(x+" "+isUrlQuery(x));
			x="http://blog-it.blogs.sapo.pt 4";
			System.out.println(x+" "+isUrlQuery(x));
			x="http://blog_it.blogs.sapo.pt/";
			System.out.println(x+" "+isUrlQuery(x));
			x="http://passamos_como_o-rio.blogs.sapo.pt/tag/expo98";
			System.out.println(x+" "+isUrlQuery(x));
			x="http://blog_it.blogs.sapo.pt/197418.html";
			System.out.println(x+" "+isUrlQuery(x));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		*/							
		
		String  errorMsg="arguments: <log file> <stats or reformat>";
		if (args.length!=2) {
			System.err.println(errorMsg);			
			return;
		}

		try {
			if (args[1].equals("stats")) {
				LogAnalyzer.readStats(args[0]);
				LogAnalyzer.stats();
			}
			else if (args[1].equals("reformat")) {
				LogAnalyzer.reformat(args[0]);
			} 
			else {
				System.err.println(errorMsg);
			}
		} 
		catch (IOException e) {			
			e.printStackTrace();
		} 
		catch (ClassNotFoundException e) {		
			e.printStackTrace();
		} 
		catch (SQLException e) {		
			e.printStackTrace();
		}
	}

}



