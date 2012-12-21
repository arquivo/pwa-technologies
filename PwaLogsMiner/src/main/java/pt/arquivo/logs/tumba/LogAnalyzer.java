package pt.arquivo.logs.tumba;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Analyze search logs of Portuguese Web Search Engine Tumba!
 * @author Miguel Costa
 * 
 * 1) Reformat the log file to move to the first position the IP and clean the log
 *    java -classpath target/pwalogsminer-1.0.0.jar pt.arquivo.logs.tumba.LogAnalyzer tumba_log2004 reformat > tumba_log2004.reformat 
 * 2) Sort the log file:
 *    cat tumba_log2004.reformat | awk {'print NR" "$0'} | sort -k 2,2 -k 1,1n | cut -f2- -d ' ' > tumba_log2004.sorted 
 * 3) Compute statistics:
 *    java -classpath target/pwalogsminer-1.0.0.jar pt.arquivo.logs.tumba.LogAnalyzer tumba_log2004.sorted  stats > tumba_log2004.stats 2>x2
 *
 */
public class LogAnalyzer {	

	//private final static String entryFilters[]={"Pesquisar.x","Pesquisar.y","Submit.x","Submit.y","Submit2.x","Submit2.y","tumbaSubmit.x","tumbaSubmit.y","dict","I1.x","I1.y","I2.x","I2.y","I3.x","I3.y","I4.x","I4.y","query_id"};
	private final static String queryFilters[]={"","watchdog"};
	private final static String advancedQueryFilters[]={"site:","\"","-"};
	private final static String stopwords[]={"a","e","o","as","os","da","de","do","das","des","dos","em","na","no","nas","nos"};

	private final static String QUERY_KEY="terms";
	private final static String NEW_QUERY_KEY="query";
	private final static String DOCS_KEY="docs";
	private final static String SESSIONID_KEY="query_id=";
	private final static String POS_KEY="pos";

	private final static long   SESSION_TIMEOUT=30*60*1000;
	private final static int    MAX_QUERIES_PER_SESSION=100;
	private final static int    IGNORE_FIRST_SESSIONS=5; // because initial sessions can be incomplete
	private final static int    RESULTS_PER_PAGE=10;
	private final static int    N_MODIFIED_TERMS_RANGE=21;
	private final static int    N_QUERIES_SESSION_RANGE=21;
	private final static int    N_TERMS_QUERY_RANGE=21;
	private final static int    N_PAGES_VIEWED_RANGE=20;
	private final static int    N_DOCS_CLICKED_RANGE=100;
    private final static int    TIME_BINS[]={0,1,5,10,15,30,60,120,180,240};
    private final static int    N_TIME_BINS=TIME_BINS.length;
    private final static int    N_USERS_SESSIONS_RANGE=10;

	private final static SimpleDateFormat dformat = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss z",Locale.US);		

	private static Hashtable<String,Integer> queryPartsMap=new Hashtable<String,Integer>(); // has the number of times a part of the query occurs
	private static int pagesViewedNotFirstPage=0; // number of results pages that are not the first one
	private static int pagesViewedDist[]=new int[N_PAGES_VIEWED_RANGE];  // distribution of the results pages seen
	private static int pagesViewedDistAux[]=new int[N_PAGES_VIEWED_RANGE];
	private static int numberPagesViewedDist[]=new int[N_PAGES_VIEWED_RANGE];  // distribution of the number of results pages seen
    private static int totalPagesViewed=0; // number of pages viewed
	private static int totalModifiedQueries=0; // have at least one equal term to previous query	
	private static int totalIdenticalQueries=0; // the queries equal to previous query	
	private static int totalIdenticalSessionQueries=0; // the queries equal to previous query in the same session
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
	private static int nSessionsWithoutClicks=0; // number of sessions without clicks	
    private static int timeBins[]=new int[N_TIME_BINS];
    private static int totalMinutes=0; // total of minutes spend in sessions
    private static int totalValidSessions=0; // total of valid sessions
    private static int totalValidSessionsMoreThanOneQuery=0; // total of valid sessions	but with more than one query (duration>0)
    private static int maxQueriesPerSession=0;
    private static int minQueriesPerSession=Integer.MAX_VALUE;		

    private static String oldQuery=new String(); // last query
	private static HashMap<String,Integer> newTerms=new HashMap<String,Integer>(); // record the terms to analyze modified queries
	//private static HashMap<String,String> entryFiltersMap=new HashMap<String,String>();
	private static HashMap<String,String> queryFiltersMap=new HashMap<String,String>();
	private static HashMap<String,String> stopwordsMap=new HashMap<String,String>();
	
	private static HashSet<String> sessionQueries=new HashSet<String>(); // stores the queries in a session
	private static HashMap<String,Integer> nSessionsIP=new HashMap<String,Integer>(); // number of sessions per IP 
	
	/**
	 * Parse log file and computes statistics
	 * @param logfile log file
	 * @throws IOException 
	 */
	public static void readStats(String logfile) throws IOException {													

		//System.out.println("Reading log file "+logfile+".");
		BufferedReader br = new BufferedReader( new FileReader(logfile) );
		String line;		
		boolean ignoreEntry;
		boolean isClickEntry;
		String oldSessionid="";
		String ip=null;
		String oldIp="";
		Date date=null;
		Date firstDate=null;
		Date lastDate=null;		
		int totalSessions=0; // all sessions
		//int nEntriesSession=0;
		Vector<LogEntry> vlines=new Vector<LogEntry>();
		Vector<String> vqueries=new Vector<String>();		

		// init maps		
		/*
		for (int i=0;i<entryFilters.length;i++) {
			entryFiltersMap.put(entryFilters[i], null);
		}
		*/		
		for (int i=0;i<queryFilters.length;i++) {
			queryFiltersMap.put(queryFilters[i], null);
		}			
		for (int i=0;i<stopwords.length;i++) {
			stopwordsMap.put(stopwords[i], null);
		}
		
		while ( ( line = br.readLine() ) != null ) {				
			String parts[]=line.split("\\s");
			ip=parts[0];			
			String sessionid=parts[2];			
			String method=parts[5];						
			if (!method.equals("\"GET")) {
				continue;
			}

			String sdate=parts[3].substring(1)+" "+parts[4].substring(0,parts[4].length()-1);			
			try {
				date=dformat.parse(sdate);
			} 
			catch (ParseException e) {
				throw new IOException(e);
			}

			String query=parts[6];
			ignoreEntry=false;
			isClickEntry=false;
			if (query.startsWith("/pesquisa?pag=")) {
				query=query.substring("/pesquisa?pag=".length());
				isClickEntry=true;
			}
			else if (query.startsWith("/pesquisa?")) {
				query=query.substring("/pesquisa?".length());
			}
			else if (query.startsWith("/termos?")) {
				query=query.substring("/termos?".length());
			}
			else { 
				ignoreEntry=true;
			}					

			if (!ignoreEntry) {
				// count sessions
				if (oldIp.equals("") || /*!oldSessionid.equals(sessionid) ||*/ !oldIp.equals(ip) || date.getTime()-lastDate.getTime()>SESSION_TIMEOUT) { // new session if one of this rules occur		
					
					if (totalSessions>=IGNORE_FIRST_SESSIONS) {		
																													
						// compute stats adding session lines
					    if (!oldIp.equals("")) {
							
							// reset pages view counter
							for (int i=0;i<pagesViewedDistAux.length;i++) {
								pagesViewedDistAux[i]=0;								
							}
							for (int i=0;i<docsClickedDistAux.length;i++) {
								docsClickedDistAux[i]=0;								
							}														
							totalClicksAux=0;							
														
							// count log entries
							for (int i=0;i<vlines.size();i++) {
								if (vlines.get(i).isClick()) {
									addClickEntry(vlines.get(i).getQuery());
								}
								else {
									String submittedQuery=addQueryEntry(vlines.get(i).getQuery());
									if (submittedQuery!=null) { // query on the first SERP
										vqueries.add(submittedQuery);	
									}																	
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
							long minutes=((lastDate.getTime()-firstDate.getTime())/1000/60); // compute session duration
							if (nQueriesSession>0 && nQueriesSession<=MAX_QUERIES_PER_SESSION && minutes>=0) { // session discarded if has no queries or too many queries or that session has a negative time (this strange case occurs probability to a system date change)
								
								// set queries stats
								for (int i=0;i<vqueries.size();i++) {
									statsForQuery(vqueries.get(i));
									//System.out.println("ALLQUERIES: "+vqueries.get(i)+" "+oldIp);
								}
								
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
								if (totalClicksAux==0) {
								    nSessionsWithoutClicks++;
								}
								totalClicks+=totalClicksAux;															
								
								totalValidSessions++;
								if (minutes>0) {
									totalValidSessionsMoreThanOneQuery++;
								}
								numberPagesViewedDist[numberPagesViewed>=N_PAGES_VIEWED_RANGE-1 ? numberPagesViewed=N_PAGES_VIEWED_RANGE-1 : numberPagesViewed]++;
								nQueriesSessionDist[nQueriesSession>nQueriesSessionDist.length-1 ? nQueriesSessionDist.length-1 : nQueriesSession]++;
								
								// set session duration
								//if (lastDate.getTime()!=firstDate.getTime()) { to remove all sessions with only one interaction
								boolean stop=false;
								for (int j=0;j<N_TIME_BINS && !stop;j++) {
									if (j==N_TIME_BINS-1 && minutes>=TIME_BINS[j]) {
									    timeBins[j]++;
									    stop=true;
									}
									else if (minutes>=TIME_BINS[j] && minutes<TIME_BINS[j+1]) {
									    timeBins[j]++;
									    stop=true;
									}
								}
								if (!stop) { // sanity check
								   	throw new IOException("Wrong bin for "+minutes+" minutes with IP "+oldIp+".");
								}
								totalMinutes+=minutes;															
								
								// add session to IP
								Integer nSessions=nSessionsIP.get(oldIp);
								if (nSessions!=null) {
									nSessionsIP.put(oldIp,nSessions+1);
								}
								else {
									nSessionsIP.put(oldIp,1);
								}																
							}
							else {
							    if (minutes<0) {
							    	System.err.println("Wrong bin for "+minutes+" minutes with IP "+oldIp+".");
							    }
							    else if (nQueriesSession>MAX_QUERIES_PER_SESSION) {
							    	System.err.println("IP "+oldIp+" on date "+date+" with too many queries:"+nQueriesSession);
							    }
							    else {
							    	System.err.println("IP "+oldIp+" on date "+date+" without queries:"+nQueriesSession);
							    }
							}							
						}					  
																						
						/* TODO remove
						if (oldSessionid.equals(sessionid)) {
							System.err.println("oldSessionid igual com diferentes IPs!! "+oldSessionid+" "+oldIp+" "+ip);
						}
						*/
						
						oldSessionid=sessionid;
						oldIp=ip;	
						firstDate=date;															
						vlines=new Vector<LogEntry>();
						vqueries=new Vector<String>();
						//nEntriesSession=0;
						newTerms=new HashMap<String,Integer>();		
						sessionQueries=new HashSet<String>();
						oldQuery=new String();
						nQueriesSession=0;
					}
					totalSessions++;
				}					
				
				vlines.add(new LogEntry(query,isClickEntry));					
				lastDate=date;
			}			
		}				
		br.close();
		// discard last session by discarding last vlines
	}
	
	/**
	 * Computes statistics
	 * @throws IOException totalUsers
	 */
	public static void stats() throws IOException {		
		// sort entries, such as queries
		Object entriesArray[] = queryPartsMap.entrySet().toArray();
		Arrays.sort(entriesArray, new Comparator(){
			public int compare(Object o1,Object o2) {	
				Map.Entry<String,Integer> entry1=(Map.Entry<String,Integer>)o1;
				Map.Entry<String,Integer> entry2=(Map.Entry<String,Integer>)o2;

				String key1=entry1.getKey().split("\\s")[0];
				String key2=entry2.getKey().split("\\s")[0];

				int comp=key1.compareTo(key2);
				if (comp!=0) { // sort by key then by value (frequency)
					return comp;
				}

				if (entry1.getValue()>entry2.getValue()) {
					return -1;
				}	    		
				return 1;
			}
		});	 
					
		// count users and users searching in only one session
		int totalUsers=nSessionsIP.size();
		int totalUsersSessionsDist[]=new int[N_USERS_SESSIONS_RANGE];		
		for (Iterator<Integer> iter=nSessionsIP.values().iterator();iter.hasNext();) {
			int nSessions=iter.next();
			totalUsersSessionsDist[nSessions>totalUsersSessionsDist.length-1 ? totalUsersSessionsDist.length-1 : nSessions]++;	
		}			
		
		// write entries stats
		Hashtable<String,Integer> termsMap=new Hashtable<String,Integer>(); // has the number of times a term occur
		int totalQueries=0;
		int totalUniqueQueries=0;
		int totalOnlyOnceQueries=0; // occurs only once in logs
		int totalAdvQueries=0;
		int advQueries[]=new int[advancedQueryFilters.length];		
		int totalTerms=0;
		int totalUniqueTerms=0;
		int totalOnlyOnceTerms=0; // occurs only once in logs
		String terms[];
		String saux;	
		int nQueries;		
		Integer value=null;
		int maxTermsPerQuery=0;
		int minTermsPerQuery=Integer.MAX_VALUE;
		int totalChars=0;
		int maxCharsPerTerm=0;
		int minCharsPerTerm=Integer.MAX_VALUE;
					
		for (int i=0;i<entriesArray.length;++i) {
			System.out.println(((Map.Entry)entriesArray[i]).getKey()  + " = " + (((Map.Entry)entriesArray[i]).getValue()));
						
			// compute term stats
			saux=(String)((Map.Entry)entriesArray[i]).getKey();
			terms=saux.split("\\s");
			if (terms[0].equals(NEW_QUERY_KEY)) {
				nQueries=(Integer)((Map.Entry)entriesArray[i]).getValue();
				if (nQueries==1) {
					totalOnlyOnceQueries++;
				}
				totalQueries+=nQueries;		
				totalUniqueQueries++;

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
					// min and max chars per term
					if (terms[j].length()>maxCharsPerTerm) {
						maxCharsPerTerm=terms[j].length();
					}
					if (terms[j].length()<minCharsPerTerm) {
						minCharsPerTerm=terms[j].length();
					}	
					//}

					String termOriginal=terms[j];
					terms[j]=terms[j].replaceAll("\\\"",""); // remove quotes from terms
					if ((value=termsMap.get(terms[j]))==null) {
						termsMap.put(terms[j],nQueries);
					}
					else {
						termsMap.put(terms[j],value+nQueries);				
					}

					// count advanced terms
					boolean isAdv=false;
					for (int k=0;k<advancedQueryFilters.length;k++) {
						if (termOriginal.startsWith(advancedQueryFilters[k])) {
							advQueries[k]+=nQueries;
							if (!isAdv) { // is counted only once
								totalAdvQueries+=nQueries;
								isAdv=true;
							}
							//System.out.println("Adv:"+saux);
						}
					}
				}	
															
				// termsPerQuery is 0 if the query has only the not operator ("-")
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
		System.out.println("-----------------------");

		// sort terms
		Object termsArray[] = termsMap.entrySet().toArray();
		Arrays.sort(termsArray, new Comparator(){
			public int compare(Object o1,Object o2) {	
				Map.Entry<String,Integer> entry1=(Map.Entry<String,Integer>)o1;
				Map.Entry<String,Integer> entry2=(Map.Entry<String,Integer>)o2;	    		 	    		 	    

				if (entry1.getValue()>entry2.getValue()) {
					return -1;
				}	    		
				return 1;
			}
		});
		// write terms stats
		for(int i=0;i<termsArray.length;++i) {
			System.out.println("term "+((Map.Entry)termsArray[i]).getKey()  + " = " + (((Map.Entry)termsArray[i]).getValue()));   
			value=(Integer)((Map.Entry)termsArray[i]).getValue();
			if (value==1) {
				totalOnlyOnceTerms++;
			}
			totalUniqueTerms++;
		}
		System.out.println("-----------------------");

		// write global stats				
		System.out.println("Total users (IPs): "+totalUsers);
		System.out.println("Total users (IPs) per # sessions distribution:");
		for (int i=0;i<totalUsersSessionsDist.length;i++) {
			System.out.println(" "+i+": "+totalUsersSessionsDist[i]+" %:"+(float)totalUsersSessionsDist[i]/(float)totalUsers);
		}			
		System.out.println("Total sessions: "+totalValidSessions);
		System.out.println("Total queries first page: "+totalQueries);
		System.out.println("Total queries next pages: "+pagesViewedNotFirstPage);
		System.out.println("Stats for first pages:");				
		System.out.println(" Queries with only one occurrence: "+totalOnlyOnceQueries);
		System.out.println(" Unique queries (variations): "+totalUniqueQueries);
		System.out.println(" Modified queries (ignoring stopwords): "+totalModifiedQueries);
		System.out.println(" Queries with equal terms (with stopwords): "+totalEqualTermsQueries);
		System.out.println(" Queries with equal terms and same order (with stopwords): "+totalEqualTermsOrderQueries);
		System.out.println(" Queries with equal terms, but all stopwords: "+totalEqualTermsStopwordsQueries);
		System.out.println(" Identical queries (exactly equal): "+totalIdenticalQueries);
		System.out.println(" Identical queries in session (exactly equal): "+totalIdenticalSessionQueries);
		System.out.println( "Modified queries, terms exchanged distribution:");
		for (int k=0;k<nModifiedTerms.length;k++) {
			System.out.println("  "+(k-(N_MODIFIED_TERMS_RANGE/2))+": "+nModifiedTerms[k]);
		}			
		System.out.println(" Total advanced queries: "+totalAdvQueries+" %Total queries:"+(float)totalAdvQueries/(float)totalQueries);	    
		for (int k=0;k<advancedQueryFilters.length;k++) {
			System.out.println("  "+advancedQueryFilters[k]+": "+advQueries[k]+" %Advanced queries:"+(float)advQueries[k]/(float)totalAdvQueries+" %Total queries:"+(float)advQueries[k]/(float)totalQueries);
		}	    
		System.out.println(" Avg. Queries per session: "+(float)totalQueries/(float)totalValidSessions);
		System.out.println(" Min. Queries per session: "+minQueriesPerSession);
		System.out.println(" Max. Queries per session: "+maxQueriesPerSession);
 		System.out.println(" Stdev. Queries per session: ?");
		System.out.println(" Total  Terms: "+totalTerms);
		System.out.println(" Avg. Terms per query: "+(float)totalTerms/(float)totalQueries);
		System.out.println(" Min. Terms per query: "+minTermsPerQuery);
		System.out.println(" Max. Terms per query: "+maxTermsPerQuery);
		System.out.println(" Stdev. Terms per query: ?");
		System.out.println(" Avg. Characters per term: "+(float)totalChars/(float)totalTerms);
		System.out.println(" Min. Characters per term: "+minCharsPerTerm);
		System.out.println(" Max. Characters per term: "+maxCharsPerTerm);
		System.out.println(" Stdev. Characters per term: ?");
		System.out.println(" Queries per session distribution:"); 
		for (int i=0;i<nQueriesSessionDist.length;i++) {
			System.out.println("  "+i+": "+nQueriesSessionDist[i]);
		}
		System.out.println( "Terms per query distribution:"); 
		for (int i=0;i<nTermsQueryDist.length;i++) {
			System.out.println("  "+i+": "+nTermsQueryDist[i]);
		}
		System.out.println("Total SERPs viewed: "+totalPagesViewed);
		System.out.println("Avg. SERPs viewed per session: "+(float)totalPagesViewed/(float)totalValidSessions);
		System.out.println("Avg. SERPs viewed per query: "+(float)totalPagesViewed/(float)totalQueries);
		System.out.println("SERPs viewed distribution:");
		for (int i=0;i<pagesViewedDist.length;i++) {
			System.out.println(" "+(i+1)+": "+pagesViewedDist[i]);
		}
		System.out.println("Number of SERPs viewed per session distribution:");
		for (int i=0;i<numberPagesViewedDist.length;i++) {
			System.out.println(" "+i+": "+numberPagesViewedDist[i]);
		}
		System.out.println("Total clicks: "+totalClicks);
		System.out.println("Avg. Clicks per session: "+(float)totalClicks/(float)totalValidSessions);
		System.out.println("Avg. Clicks per query: "+(float)totalClicks/(float)totalQueries);
		System.out.println("Sessions without clicks: "+nSessionsWithoutClicks+"  %:"+(float)nSessionsWithoutClicks/(float)totalValidSessions);
		System.out.println("Clicks distribution:");
		for (int i=0;i<docsClickedDist.length;i++) {
			System.out.println(" "+(i+1)+": "+docsClickedDist[i]);
		}		
		System.out.println("Avg. Time per Session: "+(float)totalMinutes/(float)totalValidSessions);
		System.out.println("Avg. Time per Session More than 1 Query: "+(float)totalMinutes/(float)totalValidSessionsMoreThanOneQuery);		
		System.out.println("Session Time distribution:");
		for (int i=0;i<N_TIME_BINS;i++) {
		    if (i==N_TIME_BINS-1) {
		    	System.out.print(" ["+TIME_BINS[i]+",inf[ ");
		    }
		    else {
		    	System.out.print(" ["+TIME_BINS[i]+","+TIME_BINS[i+1]+"[ ");
		    }
		    System.out.println(""+timeBins[i]);
		}
		System.out.println();
	}

	/**
	 * Add log entry to statistics
	 * @param query log entry
	 * @return submitted query 
	 * @throws IOException 
	 */
	private static String addQueryEntry(String query) throws IOException {
				
		String submittedQuery=null;
		String queryParts[]=query.split("&"); // get parameters from query		
		boolean isNextPage=false;
		String key=null;
		Integer value=null;
		boolean docsProcessed=false; // identifies if 'docs' occurs more than once in the query		
		boolean termsProcessed=false; // identifies if 'terms' occurs more than once in the query
		
		// identify if it is a next page
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
						    throw new IOException("Error of index=0 on a next page. ipage:"+ipage);
						}
						pagesViewedDistAux[index]++;
					}
					docsProcessed=true;				
				}						
			}
			else if (queryPartsFields[0].equals(QUERY_KEY)) {
				termsProcessed=true;
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
		
		// count only first page
		docsProcessed=false;
		for (int i=0;i<queryParts.length;i++) {									
			String queryPartsFields[]=queryParts[i].split("=",2);			
			queryPartsFields[0]=queryPartsFields[0].trim();
			
			if (!queryPartsFields[0].equals(QUERY_KEY) || queryPartsFields.length!=2) {
				continue;
			}
			
			// normalize query			
			try {
				queryPartsFields[1]=decodeStrings(decodeNCR(java.net.URLDecoder.decode(queryPartsFields[1],"ISO8859-1").toLowerCase()));																		
			}
			catch(IllegalArgumentException e) {
				queryPartsFields[1]=decodeStrings(decodeNCR(queryPartsFields[1].toLowerCase()));
			}
			// remove spaces
			String terms[]=queryPartsFields[1].split("\\s");
			queryPartsFields[1]="";
			for (int j=0,k=0;j<terms.length;j++) {					
				if (terms[j].equals("")) {
					continue;
				}					
				if (k>0) {
					queryPartsFields[1]+=" ";	
				}
				queryPartsFields[1]+=terms[j];					
				k++;
			}											
			// filter queries					
			if (queryFiltersMap.containsKey(queryPartsFields[1]) || queryPartsFields[1].startsWith("cache%3") || queryPartsFields[1].equals("")) {				
				continue;
			}								
			// set first page viewed
			if (!docsProcessed) {
				pagesViewedDistAux[0]++;
				docsProcessed=true;						
			}
			
			//key=queryPartsFields[0]+" "+queryPartsFields[1];
			key=NEW_QUERY_KEY+" "+queryPartsFields[1];			
			if ((value=queryPartsMap.get(key))==null) {
				queryPartsMap.put(key,1);
			}
			else {
				queryPartsMap.put(key,value+1);
			}
			
			// set submitted query
			if (submittedQuery!=null) { // sanity check
				throw new IOException("Submitted query already set.");
			}
			submittedQuery=queryPartsFields[1];
				
			// count queries per session	
			nQueriesSession++;
		}
		return submittedQuery;
	}
	
	/**
	 * Compute statistics when a query is added
	 * @param query submitted query
	 * @return 
	 * @throws IOException 
	 */
	private static void statsForQuery(String query) {
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
			   		if (stopwordsMap.containsKey(terms[j])) { // match if it is stopword
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
	private static void addClickEntry(String query) throws IOException {
		
		String queryParts[]=query.split("&"); // get parameters from query			
		for (int i=0;i<queryParts.length;i++) { 
			String queryPartsFields[]=queryParts[i].split("=",2);			
			queryPartsFields[0]=queryPartsFields[0].trim();
			if (queryPartsFields.length>1) {
				queryPartsFields[1]=queryPartsFields[1].trim();
			}
			
			if (queryPartsFields[0].equals(POS_KEY) && queryPartsFields.length>1 && !queryPartsFields[1].equals("")) {			
				int ipage;
				try {
					ipage=Integer.parseInt(queryPartsFields[1]);
					if (ipage>=docsClickedDistAux.length) {
						System.err.println("Clicked on position:"+ipage);
						ipage=docsClickedDistAux.length-1;						
					}
					docsClickedDistAux[ipage]++;
					totalClicksAux++;
				}
				catch (NumberFormatException e) {
					ipage=-1;
				}
			}
		}
	}



	/**
	 * Reformat log file for sessionid sort
	 * @param logfile log file
	 * @throws IOException 
	 * @note then sort @logfile by sessionid before using @stats
	 */
	public static void reformat(String logfile) throws IOException {		
		
		//System.out.println("Reformat log file "+logfile+".");		
		//Hashtable<String,String> ipSessionidMap=new Hashtable<String,String>();
		String line;
		//String s1, s2, s3;
		String queryId;
		int index1;
		int index2;
		int LIMIT_PARTS=7;
		//boolean endsInAmp;		
		BufferedReader br = new BufferedReader( new FileReader(logfile) );
		//BufferedReader br = new BufferedReader( new InputStreamReader(new FileInputStream(new File(logfile)),"ISO8859-1") );
		while ( ( line = br.readLine() ) != null ) {		
		/*
		FileInputStream fin = new FileInputStream(logfile);         
        DataInputStream din = new DataInputStream(fin);
        while (din.available() > 0) { 
        	line = din.readLine();
        */            			
			String parts[]=line.split("\\s",LIMIT_PARTS);
			if (parts.length<LIMIT_PARTS) {
				System.err.println("Error in reformating line:"+line);
				continue;
			}
			String ip=parts[0];			
			String method=parts[5];					
			if (!method.equals("\"GET")) {
				continue;
			}
			String query=parts[6];
			if (!query.startsWith("/pesquisa?") && !query.startsWith("/termos?")) {
				continue;
			}		
						
			//endsInAmp=true;
			index1=line.indexOf(SESSIONID_KEY);
			if (index1!=-1) {
				index2=line.indexOf('&', index1);
				if (index2==-1) {
					index2=line.indexOf(' ', index1);
					//endsInAmp=false;
				}
				try {
					//s1=line.substring(0,index1);
					queryId=line.substring(index1+SESSIONID_KEY.length(),index2);					
					//s3=line.substring(index2+ (endsInAmp ? 1 : 0));
					//System.out.println(s2+" "+s1+s3);
					
					//ipSessionidMap.put(ip,s2);
				}
				catch (StringIndexOutOfBoundsException e) {
					throw new IOException(e);
					//queryId=null;
					//System.err.println("Error in substring for line: "+line);
				}									
			}			
			else {
				queryId="0";
				/*
				if ((s2=ipSessionidMap.get(ip))==null) {
					//System.err.println("IP "+ip+" without a session.");
				}
				else {
					System.out.println(s2+" "+line);
				}
				*/
			}			
			System.out.print(ip+" "+parts[1]+" "+queryId);
			for (int i=3; i<LIMIT_PARTS; i++) {
				System.out.print(" "+parts[i]);
			}
			System.out.println();
		}
		br.close();
        //din.close();
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
	 * @param args
	 */
	public static void main(String[] args) {					

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
	}

}

class LogEntry {
	String query;
	boolean isClick;
	
	public LogEntry(String query, boolean isClick) {
		this.query=query;
		this.isClick=isClick;
	}
	
	public String getQuery() {
		return query;
	}
	public void setQuery(String query) {
		this.query = query;
	}
	public boolean isClick() {
		return isClick;
	}
	public void setClick(boolean isClick) {
		this.isClick = isClick;
	}	
}

