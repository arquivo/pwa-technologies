<%@ page 
  session="true"
  contentType="text/html; charset=UTF-8"
  pageEncoding="UTF-8"

  import="java.io.*"
  import="java.util.*"
  import="java.text.*"
  import="java.net.URLDecoder"
  import="java.net.URLEncoder"
  import="java.util.regex.Matcher"
  import="java.util.regex.Pattern"

  import="org.apache.nutch.global.Global"
  import="org.apache.nutch.html.Entities"
  import="org.apache.nutch.metadata.Nutch"
  import="org.apache.nutch.searcher.*"
  import="org.apache.nutch.searcher.Summary.Fragment"
  import="org.apache.nutch.plugin.*"
  import="org.apache.nutch.clustering.*"
  import="org.apache.hadoop.conf.Configuration"
  import="org.archive.access.nutch.NutchwaxConfiguration"
  import="org.archive.access.nutch.NutchwaxQuery"
  import="org.archive.access.nutch.NutchwaxBean"
  import="org.archive.util.ArchiveUtils"
  import="org.apache.lucene.search.PwaFunctionsWritable"
  import="org.apache.hadoop.io.Text"
  import="org.apache.nutch.searcher.Query.Clause"
%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%!
  public static final Calendar DATE_START = new GregorianCalendar(1996, 1-1, 1, 0, 0, 0);
  public static final DateFormat FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");
  public static final DateFormat OFFSET_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

  private static final String COLLECTION_KEY = "collection";
  private static final String COLLECTION_QUERY_PARAM_KEY = COLLECTION_KEY + ":";
  
  //The first pattern doesn't support UTF-8 characters in the string. Relevant for IDN urls?
  private static final Pattern URL_PATTERN = Pattern.compile("^.*? ?((https?:\\/\\/)?([a-zA-Z\\d][-\\w\\.]+)\\.([a-z\\.]{2,6})([-\\/\\w\\p{L}\\.~,;:%&=?+$#*]*)*\\/?) ?.*$");
  private static final Pattern OFFSET_PARAMETER = Pattern.compile("(\\d{4})-(\\d{2})-(\\d{2})");

%>
<%@ include file="include/logging_params.jsp" %>

<%-- Get the application beans --%>
<%
  Configuration nutchConf = NutchwaxConfiguration.getConfiguration(application);
  NutchBean bean = NutchwaxBean.get(application, nutchConf);
%>
<%-- Define the default end date --%>
<%
  Calendar DATE_END = new GregorianCalendar();
  DATE_END.set( Calendar.YEAR, DATE_END.get(Calendar.YEAR) );
  DATE_END.set( Calendar.MONTH, 12-1 );
  DATE_END.set( Calendar.DAY_OF_MONTH, 31 );
  DATE_END.set( Calendar.HOUR_OF_DAY, 23 );
  DATE_END.set( Calendar.MINUTE, 59 );
  DATE_END.set( Calendar.SECOND, 59 );


  try {
	String offsetDateString = getServletContext().getInitParameter("embargo-offset");

	Matcher offsetMatcher = OFFSET_PARAMETER.matcher( offsetDateString );
	offsetMatcher.matches();
	int offsetYear = Integer.parseInt(offsetMatcher.group(1));
	int offsetMonth = Integer.parseInt(offsetMatcher.group(2));
	int offsetDay = Integer.parseInt(offsetMatcher.group(3));

	DATE_END.set(Calendar.YEAR, DATE_END.get(Calendar.YEAR) - offsetYear);
	DATE_END.set(Calendar.MONTH, DATE_END.get(Calendar.MONTH) - offsetMonth);
	DATE_END.set(Calendar.DAY_OF_MONTH, DATE_END.get(Calendar.DAY_OF_MONTH) - offsetDay );
  } catch(IllegalStateException e) {
	// Set the default embargo period to: 1 year
	DATE_END.set( Calendar.YEAR, DATE_END.get(Calendar.YEAR) - 1);
	bean.LOG.error("Embargo offset parameter isn't in a valid format");
  } catch(NullPointerException e) {
	// Set the default embargo period to: 1 year
	DATE_END.set( Calendar.YEAR, DATE_END.get(Calendar.YEAR) - 1);
	bean.LOG.error("Embargo offset parameter isn't present");
  }
%>
<%-- Handle the url parameters --%>
<%
  // Set the character encoding to use when interpreting request values 
  request.setCharacterEncoding("UTF-8");

  // get query from request
  String queryString = request.getParameter("query");
  if ( queryString != null ) {
  	queryString = queryString.trim();
  } else { 
  	// Check if the 'query' params exists
	// else check if the advanced params exist and process them
	queryString = "";
	if ( request.getParameter("adv_and") != null && request.getParameter("adv_and") != "") {
		queryString += request.getParameter("adv_and");
		queryString += " ";
	}
	if ( request.getParameter("adv_phr") != null && request.getParameter("adv_phr") != "") {
		queryString += "\""+ request.getParameter("adv_phr").replaceAll("\"", "") +"\"";
		queryString += " ";
	}
	if ( request.getParameter("adv_not") != null && request.getParameter("adv_not") != "") {
		String notStr = request.getParameter("adv_not");
		if (!notStr.startsWith("-")) 
			notStr = "-" + notStr;
		notStr = notStr.replaceAll("[ -]+", " -") +" ";
		queryString += notStr;
	}
	if ( request.getParameter("adv_mime") != null && request.getParameter("adv_mime") != "" ) {
		queryString += "filetype:"+ request.getParameter("adv_mime");
		queryString += " ";
	}
	if (request.getParameter("site") != null && request.getParameter("site") != "") {
		queryString += "site:";
		String siteParameter = request.getParameter("site");
		if (siteParameter.startsWith("http://")) {
			queryString += siteParameter.substring("http://".length()); 
		} else if (siteParameter.startsWith("https://")) {
			queryString += siteParameter.substring("https://".length());
		} else {
			queryString += siteParameter;
		}
		queryString += " ";
	}
	if (request.getParameter("format") != null && request.getParameter("format") != "" && !request.getParameter("format").equals("all")) {
		queryString += "type:" + request.getParameter("format");
		queryString += " ";
	}
  }

  /*****************	'hitsPerDup' param	***************************/
  int hitsPerDup = 2; 
  //int hitsPerDup = 1000;
  String hitsPerDupString = request.getParameter("hitsPerDup");
  if (hitsPerDupString != null && hitsPerDupString.length() > 0) {
    hitsPerDup = Integer.parseInt(hitsPerDupString);
  } else {
    // If 'hitsPerSite' present, use that value.
    String hitsPerSiteString = request.getParameter("hitsPerSite");
    if (hitsPerSiteString != null && hitsPerSiteString.length() > 0) {
      hitsPerDup = Integer.parseInt(hitsPerSiteString);
    }
  }

  /*****************	'sort' param	***************************/
  String sort = null;
  boolean reverse = false;


  if (!queryString.contains("sort:")) {
	sort = request.getParameter("sort");

	if ("relevance".equals(sort)) {
		sort = null;
  	} else if ("new".equals(sort)) {
		sort = "date";
		reverse = true;
		queryString += "sort:new";
		hitsPerDup = 0;
  	} else if ("old".equals(sort)) {
		sort = "date";
		queryString += "sort:old";
		hitsPerDup = 0;
	} else {
		sort = null;
	}
  } else if (queryString.contains("sort:new")) {
	sort = "date";
	reverse = true;
	hitsPerDup = 0;
  } else if (queryString.contains("sort:old")) { 
	sort = "date";
	hitsPerDup = 0;
  }

  // De-Duplicate handling.  Look for duplicates field and for how many
  // duplicates per results to return. Default duplicates field is 'site'
  // and duplicates per results default is '1' (Used to be '2' but now
  // '1' so can have an index with dups not show dups when used doing
  // straight searches).
  String dedupField = request.getParameter("dedupField");
  if (dedupField == null || dedupField.length() == 0) {
    dedupField = "site";
  }

  int hitsPerVersion = 1;
  String hitsPerVersionString = request.getParameter("hitsPerVersion");
  if (hitsPerVersionString != null && hitsPerVersionString.length() > 0) {
	hitsPerVersion = Integer.parseInt(hitsPerVersionString);
  }

  if (queryString.contains("site:")) {
	hitsPerDup = 0;

	queryString = queryString.replaceAll("site:http://", "site:");
	queryString = queryString.replaceAll("site:https://", "site:");
  }

  /***************** Save the query string for further use ***********/
  request.setAttribute("query", queryString.trim());

  /***************** Clean the query for Backend search *************/
  if (queryString.contains("sort:new")) {
  	queryString = queryString.replace("sort:new","");
  } else if (queryString.contains("sort:old")) {
	queryString = queryString.replace("sort:old","");
  }

  /*** Start date ***/
  Calendar dateStart = (Calendar)DATE_START.clone();
  SimpleDateFormat inputDateFormatter = new SimpleDateFormat("dd/MM/yyyy");

  if ( request.getParameter("dateStart") != null && !request.getParameter("dateStart").equals("") ) {
	try {
		dateStart.setTime( inputDateFormatter.parse(request.getParameter("dateStart")) );
	} catch (NullPointerException e) {
		bean.LOG.debug("Invalid Start Date:"+ request.getParameter("dateStart") +"|");
	}
  }

  /*** End date ***/
  Calendar dateEnd = (Calendar)DATE_END.clone();				// Setting current date
  
  if ( request.getParameter("dateEnd") != null && !request.getParameter("dateEnd").equals("") ) {
	try {
		dateEnd.setTime( inputDateFormatter.parse(request.getParameter("dateEnd")) );
		// be sure to set the end date to the very last second of that day.
		dateEnd.set( Calendar.HOUR_OF_DAY, 23 );
		dateEnd.set( Calendar.MINUTE, 59 );
		dateEnd.set( Calendar.SECOND, 59 );
	} catch (NullPointerException e) {
		bean.LOG.debug("Invalid End Date:"+ request.getParameter("dateEnd") +"|");
	}
  }

  /*** Switch dates if start GT end ***/
// TODO - check if start date is GT end
  /*** Add dates to nutch query ***/
  if (queryString != null && queryString != "") {
	queryString += " date:"+ FORMAT.format( dateStart.getTime() );
	queryString += "-";
	queryString += FORMAT.format( dateEnd.getTime() );
  } else {
  	queryString = "";
  }

  String dateStartString = inputDateFormatter.format( dateStart.getTime() );
  
  String dateEndString = inputDateFormatter.format( dateEnd.getTime() );

  //--- not needed, since we use fields. String htmlQueryString = Entities.encode(queryString);

  /*****************	Offset param	***************************/
  int start = 0;          // first hit to display
  String startString = request.getParameter("start");
  if (startString != null)
    start = Integer.parseInt(startString);

  /*****************	Hits/page param	***************************/
  int hitsPerPage = 10;          // number of hits to display
  String hitsString = request.getParameter("hitsPerPage");
  if (hitsString != null) {
  	try {
		hitsPerPage = Integer.parseInt(hitsString);
	} catch (NumberFormatException e) {
		bean.LOG.debug("WRONG VALUE of hitsPerPage:"+ hitsString +"|");
	}
  }

  // If a 'collection' parameter present, always add to query.
  String collection = request.getParameter(COLLECTION_KEY);
  if (collection != null && queryString != null && queryString.length() > 0) {
      int collectionIndex = queryString.indexOf(COLLECTION_QUERY_PARAM_KEY);
      if (collectionIndex < 0) {
        queryString = queryString + " " + COLLECTION_QUERY_PARAM_KEY +
            collection;
      }
  }
  
  // Prepare the query values to be presented on the page, preserving the session
  String htmlQueryString = "";

  if ( request.getAttribute("query") != null ) {
  	htmlQueryString = request.getAttribute("query").toString();
  	htmlQueryString = Entities.encode(htmlQueryString);
  }
   
  // Make up query string for use later drawing the 'rss' logo.
  String params = "&hitsPerPage=" + hitsPerPage +
    (sort == null ? "" : "&sort=" + sort + (reverse? "&reverse=true": "") +
    (dedupField == null ? "" : "&dedupField=" + dedupField));
    
  //Only query it there are search terms present
  //This handle the case of opening the page
//  if (request.getParameter("query") == null) {
//	queryString = "";
//  }

  //String requestURI = HttpUtils.getRequestURL(request).toString();

  // URLEncoder.encode the queryString rather than just use htmlQueryString.
  // The former will take care of other than just html entities in case its
  // needed.
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%
  // To prevent the character encoding declared with 'contentType' page
  // directive from being overriden by JSTL (apache i18n), we freeze it
  // by flushing the output buffer. 
  // see http://java.sun.com/developer/technicalArticles/Intl/MultilingualJSP/
  out.flush();

  ServletContext context = getServletContext();
%>

<%-- ***************** i18n Lang attribution ************************** --%>
<%@include file="include/i18n.jsp" %>
<%  DateFormat DISPLAY_FORMAT = new SimpleDateFormat("dd MMMMMMMM, yyyy", new Locale(language)); %>
<i18n:bundle baseName="org.nutch.jsp.search" locale="<%= new Locale(language) %>"/>

<%-------------------- HTML start ----------------------------------------%>
<html xmlns="http://www.w3.org/1999/xhtml" lang="<%=language%>" xml:lang="<%=language%>">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	<meta http-equiv="X-UA-Compatible" content="IE=8" />
	<%@ include file="include/metadata.jsp" %>
	<title><c:choose>
		<c:when test='${requestScope.query ne "" }'>
			<c:out value='${requestScope.query}'/> - <i18n:message key="title"/>
		</c:when>
		<c:otherwise>
			<i18n:message key="AWP"/>
		</c:otherwise>
	</c:choose></title>
	<link rel="shortcut icon" href="img/logo-16.jpg" type="image/x-icon" />
	<link type="text/css" href="css/jquery-ui-1.7.2.custom.css" rel="stylesheet" />
	<link type="text/css" href="css/style.css" rel="stylesheet" />
	<link rel="search" type="application/opensearchdescription+xml" title="<i18n:message key='opensearch.title'><i18n:messageArg value='<%=language%>'/></i18n:message>" href="opensearch.jsp?l=<%=language%>" />
	<script	type="text/javascript" src="js/jquery-1.3.2.min.js"></script>
	<script type="text/javascript" src="js/ui.datepicker.js"></script>
	<% if (language.equals("pt")) { %>
        <script type="text/javascript" src="js/ui.datepicker-pt-BR.js"></script>
	<% } %>
	<script type="text/javascript">
		var minDate = new Date(<%=DATE_START.getTimeInMillis()%>);
		var maxDate = new Date(<%=DATE_END.getTimeInMillis()%>);
	</script>
	<script type="text/javascript" src="js/configs.js"></script>
</head>
<body>
<div id="header">
<%@include file="header.jsp" %>

<%@include file="include/search_form_top.jsp" %>
<%-- EoHeader --%>
</div>
<div id="utilities">
	<a href="<c:url value="">
                        <c:param name='query' value='${param.query}'/>
                        <c:param name='dateStart' value='${param.dateStart}'/>
                        <c:param name='dateEnd' value='${param.dateEnd}'/>
                        <c:if test="${ (empty param.l) || (param.l eq 'pt') }">
                                <c:param name='l' value='en'/>
                        </c:if>
                </c:url>"><i18n:message key="otherLang"/></a>
	|
	<c:choose>
		<c:when test="${ param.l eq 'en' }">
			<a href="http://sobre.arquivo.pt/faq/search-1"><i18n:message key="help"/></a>
		</c:when>
		<c:otherwise>
			<a href="http://sobre.arquivo.pt/perguntas-frequentes/pesquisa"><i18n:message key="help"/></a>
		</c:otherwise>
	</c:choose>
</div>

<div id="results">
<%----------------------------------------------------------
// Check to see which of the 3 mode is presented:
// (1) result list
// (2) wayback document's grid
// (3) result list with tip
----------------------------------------------------------%>
<%
Matcher urlMatch = null;
String urlQuery = null;
boolean showList = false;
String showTip = null;			// tip to show
String allVersions = null;
int end = -1;
Hits hits = null;
int hitsLength = 0;
long hitsTotal = 0;
boolean hitsTotalIsExact = false;
Query query = null;
   
String collectionsHost = nutchConf.get("wax.host", "examples.com");
pageContext.setAttribute("collectionsHost", collectionsHost);

if ( request.getAttribute("query") != null && !request.getAttribute("query").toString().equals("") ) {

	if ( (urlMatch = URL_PATTERN.matcher( request.getAttribute("query").toString() )).matches() ) {
		urlQuery = urlMatch.group(1);
		String urlQueryParam = urlQuery;
		int urlLength = urlQuery.length();

		if (!urlQuery.startsWith("http://") && !urlQuery.startsWith("https://") ) { 
			urlQueryParam = "http://" + urlQueryParam;
		}

		pageContext.setAttribute("urlQueryParam", urlQueryParam);
	
		allVersions = "search.jsp?query="+ URLEncoder.encode(urlQueryParam, "UTF-8");
		if (!language.equals("pt")) {
			allVersions += "&l="+ language;
		}

		if ( request.getParameter("query") != null && urlLength == request.getParameter("query").trim().length() ) {
			// option: (2)
			showList = false;
			usedWayback = true;
	
			pageContext.setAttribute("dateStartWayback", FORMAT.format( dateStart.getTime() ) );
			pageContext.setAttribute("dateEndWayback", FORMAT.format( dateEnd.getTime() ) );

			long startQueryTime = System.currentTimeMillis();		// for logging
%>

			<%-- #search_stats & #result_list for this case are generated by WB --%>
			<%
				boolean seeHistory = false;		// This variable is used to indicate that link to see the history was clicked
				if( request.getParameter("pos") != null) {
					seeHistory = true;
				}
				pageContext.setAttribute("seeHistory", seeHistory);
			%>
			<c:catch var="exception">
				<c:import url="http://${collectionsHost}/newquery">
					<c:param name="type" value="urlquery" />
					<c:param name="url" value="${urlQueryParam}" />
					<c:param name="aliases" value="true" />
					<c:param name="multiDet" value="true" />
					<c:param name="l" value="${language}" />
					<c:param name="startdate" value ="${dateStartWayback}"/>
					<c:param name="enddate" value="${dateEndWayback}"/>
					<c:param name="hist" value="${pageScope.seeHistory}"/>
					<c:param name="sid" value="${pageContext.session.id}"/>
				</c:import>
				<% hitsTotal = 1; %>
			</c:catch>
			<c:if test="${not empty exception}">
				<%-- paint the empty stats bar --%>
				<div id="search_stats"></div>
			</c:if>

			<% queryTime = (int) (System.currentTimeMillis() - startQueryTime); //for logging %>

<%	
		} else {
			// option: (3)
			showList = true;
			showTip = urlMatch.group(1);
			query = NutchwaxQuery.parse(queryString, nutchConf);	//create the query object
			bean.LOG.debug("query: " + query.toString());
		}
	} else {
		// option: (1)
		query = NutchwaxQuery.parse(queryString, nutchConf);		//create the query object
		bean.LOG.debug("query: " + query.toString());

		showList = true;
	}
%>
<% } %>

<%-- Start of result_list <ol> --%>
<% if (showList) { %>
<%
   PwaFunctionsWritable functions= PwaFunctionsWritable.parse(nutchConf.get(Global.RANKING_FUNCTIONS)); 
   int queryMatches = Integer.parseInt(nutchConf.get(Global.MAX_FULLTEXT_MATCHES_RANKED));

   do {
       try {
		long startQueryTime = System.currentTimeMillis();
		hits = bean.search(query, start + hitsPerPage, queryMatches, hitsPerDup, dedupField, sort, reverse, functions, hitsPerVersion);
		queryTime = (int) (System.currentTimeMillis() - startQueryTime);

		hitsLength = hits.getLength();
		hitsTotal = hits.getTotal();
		hitsTotalIsExact = hits.totalIsExact();
       } catch (IOException e) {
          hits = new Hits(0, new Hit[0]);	
       }
       // Handle case where start is beyond how many hits we have.
       end = (int)Math.min(hitsLength, start + hitsPerPage);
       if (hitsLength <= 0 || end > start) {
           break;
       }
       while (end <= start && start > 1) {
           start -= hitsPerPage;
           if (start < 1) {
               start = 1;
           }
       }
   } while (true);

   // be responsive
   out.flush();

   int length = end-start;
   int realEnd = (int)Math.min(hitsLength, start + hitsPerPage);

   Hit[] show = hits.getHits(start, realEnd-start);
   HitDetails[] details = bean.getDetails(show);
   Summary[] summaries = bean.getSummary(details, query);
   bean.LOG.debug("total hits: " + hitsTotal);

   int[] positionIndex = new int[show.length];
   int indexPos = 0;
   Hit[] showCopy = show.clone();

   for (int i = 0; i < show.length; i++) {
      if ( showCopy[i] != null) {
	positionIndex[indexPos++] = i;
	showCopy[i] = null;

	String host = show[i].getDedupValue();

	for (int j = i + 1; j < show.length; j++ ) {
		if ( showCopy[j] != null && host.equals( showCopy[j].getDedupValue() ) ) {
			positionIndex[indexPos++] = j;
			showCopy[j] = null;
		}
	}
      }
    }
    showCopy = null;	
%>
	<div id="search_stats">

	<% if (hitsTotal > 0) { %>
		<span><i18n:message key="hits">
		  <i18n:messageArg value="<%=new Long((end==0)?0:(start+1))%>"/>
		  <i18n:messageArg value="<%=new Long(end)%>"/>
		  <i18n:messageArg value="<%=new Long(hitsTotal)%>"/>
		</i18n:message></span>
	<% } %>
	</div>

	<p class="spell hidden"><i18n:message key="didYouMean"/> <span class="suggestion"></span></p>

	<%-- Notices Bar: presents notices, request, ads --%>
	<div id="notices">
		<div class="notice">
			<h4><a href="<c:url value="${pageContext.request.scheme}://${pageContext.request.serverName}/quiz/Quiz"><c:param name="sid" value="${pageContext.session.id}"/><c:param name="l" value="${language}"/></c:url>"><i18n:message key="surveyTitle"/></a></h3>
			<i18n:message key="surveySubtitle"/><br />
		</div>
	</div>

	<%-- Result List --%>
	<div id="result_list">

	<%-- Show tip if present --%>
	<% if (showTip != null) { %>
		<p class="info">
			<i18n:message key="seeUrlTip">
				<i18n:messageArg value="<%=allVersions%>"/>
				<i18n:messageArg value="<%=showTip%>"/>
			</i18n:message>
		</p>
	<% } %>

	<ol>
	<%
	  // Saves information about the previous result's host so same-host results can be grouped.
	  String previous_host = "";
	
	  //Format the results
	  for (int i = 0; i < length; i++) {      // display the hits
	    Hit hit = show[ positionIndex[i] ];
	    HitDetails detail = details[ positionIndex[i] ];
	    String title = detail.getValue("title").trim();
	    String current_host = hit.getDedupValue();
	    int position = start + i + 1;
	    pageContext.setAttribute("position", position);
	
	    String caching = detail.getValue("cache");
	    boolean showSummary = true;
	    if (caching != null) {
	      showSummary = !caching.equals(Nutch.CACHING_FORBIDDEN_ALL);
	    }
	
	    Date date = new Date(Long.valueOf(detail.getValue("date")).longValue()*1000);
	    String archiveDate = FORMAT.format(date);
	    String archiveDisplayDate = DISPLAY_FORMAT.format(date);
	    String archiveCollection = detail.getValue("collection");
	    String url = detail.getValue("url");

	    if (archiveDisplayDate.startsWith("0") )
		archiveDisplayDate = archiveDisplayDate.substring(1);
	
	    // If the collectionsHost includes a path do not add archiveCollection.
	    // See http://sourceforge.net/tracker/index.php?func=detail&aid=1288990&group_id=118427&atid=681140.
	    String target = "http://"+ collectionsHost +"/id"+ hit.getIndexDocNo() +"index"+ hit.getIndexNo();
	    pageContext.setAttribute("target", target);
	    allVersions = "search.jsp?query="+ URLEncoder.encode(url, "UTF-8") +"&dateStart="+ dateStartString + "&dateEnd="+ dateEndString +"&pos="+ String.valueOf(position);	    

	    if (!language.equals("pt")) {
		allVersions += "&l="+ language;
	    }

	    /*** Result's Title ***/
	    // Process the result's title:
	    // - Try to match and highlight the words that are both in the title and query
	    // - Control the size of the title

	    // use url for docs w/o title
	    if (title == null || title.equals("")) {
	      title = url;
	    }

	    // match and highlight the words
	    String[] splittedTitle = title.split(" ");
	    StringBuilder newTitle = new StringBuilder();
	 
	    final int TITLE_MAX_LENGTH = 60;
	    int tagLengthCount = 0;   
 
	    outerLoop:
	    for ( String s : splittedTitle ) {
	    
		if (newTitle.length() > 0) {
			newTitle.append(" ");
		}

		innerLoop: {
			for ( Clause clause : query.getClauses() ) {
				if ( clause.isRequired() && !clause.isPhrase() ) {
					if ( s.compareToIgnoreCase( clause.toString() ) == 0 ) {
						// Check we don't go over the title max size (without counting the tags size)
						if ( (newTitle.length() + s.length() - tagLengthCount) < TITLE_MAX_LENGTH ) {
							newTitle.append("<em>");
							newTitle.append( s );
							newTitle.append("</em>");
		
							tagLengthCount += 9;

							// Proceed to the next title word
							break innerLoop;
						} else {
							// Over the title max size, we can finish
							newTitle.append("...");
							break outerLoop;
						}
					} 
				}
			}
			newTitle.append( s );	
		}
	    }

	    title = newTitle.toString();

	    // Cut the title if it is too long
	    if ( title.length() - tagLengthCount >= TITLE_MAX_LENGTH ) {
		title = title.substring(0, TITLE_MAX_LENGTH + tagLengthCount) + "<b>...</b>";
	    }
	
	    if ( url.length() > 80) {
	        url = url.substring(0, 77) + "...";
	    }
	    
	    // Build the summary
	    StringBuffer sum = new StringBuffer();
	    Fragment[] fragments = summaries[ positionIndex[i] ].getFragments();
	    for (int j=0; j<fragments.length; j++) {
	      if (fragments[j].isHighlight()) {
	        sum.append("<span class=\"highlight\">")
	           .append(Entities.encode(fragments[j].getText()))
	           .append("</span>");
	      } else if (fragments[j].isEllipsis()) {
	        sum.append("<span class=\"ellipsis\"> ... </span>");
	      } else {
	        sum.append(Entities.encode(fragments[j].getText()));
	      }
	    }
		
	    String summary = sum.toString();
	
	/* TODO remove - resolve uns, estraga outros - tem de ser caso a caso para title e snippet
	    byte b[]=title.getBytes("ISO8859-1");
	    title=new String(b,"UTF8");
	*/

	    // do not show unless we have something
	    boolean showMore = false;
	
	    // Content-Type
	    String primaryType = detail.getValue("primaryType");
	    String subType = detail.getValue("subType");
	
	    String contentType = subType;
	    if (contentType == null)
	      contentType = primaryType;
	    if (contentType != null) {
	      contentType = "[<span class=\"contentType\">" + contentType + "</span>]";
	      showMore = true;
	    } else {
	      contentType = "";
	    }
	
	    // Last-Modified
	    String lastModified = detail.getValue("lastModified");
	    if (lastModified != null) {
	      Calendar cal = new GregorianCalendar();
	      cal.setTimeInMillis(new Long(lastModified).longValue());
	      lastModified = cal.get(Calendar.YEAR)
	                  + "." + (1+cal.get(Calendar.MONTH)) // it is 0-based
	                  + "." + cal.get(Calendar.DAY_OF_MONTH);
	      showMore = true;
	    } else {
	      lastModified = "";
	    }
	
	    %>
		<% if (hitsPerDup > 0 && current_host.equals( previous_host )) {%>
			<li class="grouped">
		<% } else { %>
			<li>
		<% previous_host = current_host; } %>
			
		<% if (showMore) {
			if (!"text".equalsIgnoreCase(primaryType)) {
				if ( contentType.lastIndexOf('-') != -1) {
					contentType = "[" + contentType.substring( contentType.lastIndexOf('-') + 1);
				}
				contentType = contentType.toUpperCase(); %>
				<span class="mime"><%=contentType%></span>
		<%} }%>
	

	    <h3><a href="<c:url value='${target}'><c:param name='pos' value='${position}'/><c:param name='l' value='${language}'/><c:param name='sid' value='${pageContext.session.id}'/></c:url>"><%=title%></a></h3> - <span class="date"> <%=archiveDisplayDate%>	</span> - <a class="history" href="<%=allVersions%>"><i18n:message key="otherVersions"/></a>
	    <% showSummary=true; //to show always summaries %> 
	    <% if (!"".equals(summary) && showSummary) { %>
	    <br /><%=summary%>
	    <% } %>
	    <br />
	    <span class="url"><%= url %></span>
<%--
	    -
	    <a class="history" href="<%=allVersions%>"><i18n:message key="otherVersions"/></a>
--%>

	    </li>
	<% } %>
	
	<%--
	   Debugging info
	<table border="1">
	<tr>
	<td>isExact:<%=hitsTotalIsExact%></td>
	<td>total:<%=hitsTotal%></td>
	<td>getLength:<%=hitsLength%></td>
	<td>start:<%=start%></td>
	<td>end:<%=end%></td>
	<td>hitsPerPage:<%=hitsPerPage%></td>
	</tr>
	</table>
	--%>
	
</ol>

</div><%-- End of #result_list block --%>

<% } %>
<%-- End of [showList] --%>
	
	<%
	if (hitsLength >= end || hitsLength > start) {
		long pagesAvailable = (long) (hitsTotal / hitsPerPage) ;
			if ((hitsTotal % hitsPerPage) != 0) {
				pagesAvailable++;
			}

			// Check if we are in the last page
			if (hitsLength == end && hitsPerDup != 0) {
				pagesAvailable = (long) (hitsLength / hitsPerPage);
				if ((hitsLength % hitsPerPage) != 0) {
					pagesAvailable++;
				}
			}
	
	    long currentPage = (long) ((start + 1) / hitsPerPage + 1) ;
	    int maxPagesToShow = 10;
	    long displayMin = (long) (currentPage - (0.5 * maxPagesToShow) );
	
	    if (displayMin < 1) {
	      displayMin = 1;
	    }
	
	    long displayMax = displayMin + maxPagesToShow - 1 ;
	    if (displayMax > pagesAvailable) {
	      displayMax = pagesAvailable;
	    }
	%>

<%-- ---------------- --%>
<%-- No results presentend --%>
<%-- ---------------- --%>
<% if ( hitsTotal == 0) { %>

<%
	// Intial page / empty query
	if ( request.getAttribute("query").equals("") ) { 
%>
	<div id="search_stats"></div>
	<div id="no_results">
		<h2><i18n:message key="introTitle"/></h2>
		<p class="spacing"><c:choose>
			<c:when test="${ param.l eq 'en' }">
				<i18n:message key="introDescription">
					<i18n:messageArg value="http://sobre.arquivo.pt/faq/search-1/?searchterm=1996#section-3"/>
					<i18n:messageArg value="<%=DATE_START.getTime()%>"/>
					<i18n:messageArg value="http://sobre.arquivo.pt/faq/access-to-archived-contents/?searchterm=embargo#section-0"/>
					<i18n:messageArg value="<%=DATE_END.getTime()%>"/>
				</i18n:message>
	                </c:when>
			<c:otherwise>
				<i18n:message key="introDescription">
					<i18n:messageArg value="http://sobre.arquivo.pt/perguntas-frequentes/pesquisa/?searchterm=1996#section-3"/>
					<i18n:messageArg value="<%=DATE_START.getTime()%>"/>
					<i18n:messageArg value="http://sobre.arquivo.pt/perguntas-frequentes/acesso-a-conteudos-arquivados/?searchterm=embargo#section-0"/>
					<i18n:messageArg value="<%=DATE_END.getTime()%>"/>
				</i18n:message>
			</c:otherwise>
			</c:choose></p>
		<h3 class="small_spacing"><i18n:message key="introExample"/></h3>
		<ol>
			<li class="column1"><a href="../wayback/wayback/id4705954index0?l=<%=language%>" class="column1"><i18n:message key="introExample1"/></a></li>
			<li class="column1"><a href="../wayback/wayback/id4390263index3?l=<%=language%>" class="column1"><i18n:message key="introExample2"/></a></li>
			<li class="column1"><a href="../wayback/wayback/id39index1?l=<%=language%>" class="column1"><i18n:message key="introExample3"/></a></li>
			<li class="column1"><a href="../wayback/wayback/id94index1?l=<%=language%>" class="column1"><i18n:message key="introExample4"/></a></li>
			<li class="column2 reset"><a href="../wayback/wayback/id87775634index0?l=<%=language%>" class="column2" reset><i18n:message key="introExample5"/></a></li>
			<li class="column2"><a href="../wayback/wayback/id4447index1?l=<%=language%>" class="column2"><i18n:message key="introExample6"/></a></li>
			<li class="column2"><a href="../wayback/wayback/id311176index0?l=<%=language%>" class="column2"><i18n:message key="introExample7"/></a></li>
			<li class="column2"><a href="../wayback/wayback/id1101166index0?l=<%=language%>" class="column2"><i18n:message key="introExample8"/></a></li>
		</ol>
	</div>
	<% } else { %>
	<div id="no_results">
		<%-- No results found --%>
		<h3><i18n:message key="noResultsTitle"><i18n:messageArg value="<%=htmlQueryString%>"/></i18n:message></h3>
		<p><i18n:message key="noResultsTips"/></p>	
		<ol>
			<li><i18n:message key="noResultsTip1"/></li>
			<li><i18n:message key="noResultsTip2"/></li>
			<li><i18n:message key="noResultsTip3"/></li>
			<li><i18n:message key="noResultsTip4"/></li>
			<% if ( usedWayback) { %>
				<% String urlInternetArchive = "http://web.archive.org/web/*/" + urlQuery; %>
				<li><i18n:message key="noResultsInternetArchive"><i18n:messageArg value="<%= urlInternetArchive %>"/></i18n:message></li>
				<li><i18n:message key="noResultsSuggest"><i18n:messageArg value="<%=htmlQueryString%>"/></i18n:message></li>
			<% } %>
		</ol>
	</div>
<%
	}
%>

<%  } else if (showList && hitsTotal != 0 && hitsPerDup != 0 && currentPage >= pagesAvailable && hitsTotal > end) {
   long previousPageStartForDup = (currentPage - 2) * hitsPerPage;
    String previousPageUrlForDup = request.getContextPath() + "search.jsp?" +
      "query=" + htmlQueryString +
      "&dateStart="+ dateStartString + 
      "&dateEnd="+ dateEndString + 
      "&start=" + previousPageStartForDup + 
      "&hitsPerPage=" + hitsPerPage +
      "&hitsPerDup=" + 0 +
      "&dedupField=" + dedupField;
    if (sort != null) {
      previousPageUrlForDup = previousPageUrlForDup + 
      "&sort=" + sort +
      "&reverse=" + reverse;
    }

 
    String noDupQuery = request.getContextPath() + "/search.jsp?" +
      "query=" + htmlQueryString +
      "&dateStart="+ dateStartString +  
      "&dateEnd="+ dateEndString + 
      "&start=" + 0 +
      "&hitsPerPage=" + hitsPerPage +
      "&hitsPerDup=" + 0 +
      "&dedupField=" + dedupField +
      "&l="+ language;
      if (sort != null) {
        previousPageUrlForDup = previousPageUrlForDup +
        "&sort=" + sort +
        "&reverse=" + reverse;
      }
%>
    <p class="info"><i18n:message key="omittedResults"><i18n:messageArg value="<%=noDupQuery%>"/></i18n:message>.</p>
<% } %>

<%--
   Debugging info
<table border="1">
<tr>
<td>hits number:<%=hitsTotal%></td>
<td>pagesAvailable:<%=pagesAvailable%></td>
<td>currentPage:<%=currentPage%></td>
<td>displayMin:<%=displayMin%></td>
<td>displayMax:<%=displayMax%></td>
</tr>
</table>
--%>
<div id="pager">
<% if (hitsTotal > 1) { %>		<%-- Start Pager IF --%>
<% 
  if (currentPage > 1) {
    long previousPageStart = (currentPage - 2) * hitsPerPage;
    String previousPageUrl = "search.jsp?" +
      "query=" + URLEncoder.encode(request.getAttribute("query").toString(), "UTF-8") +
      "&dateStart="+ dateStartString + 
      "&dateEnd="+ dateEndString + 
      "&pag=prev" +				// mark as 'previous page' link 
      "&start=" + previousPageStart + 
      "&hitsPerPage=" + hitsPerPage +
      "&hitsPerDup=" + hitsPerDup +
      "&dedupField=" + dedupField +
      "&l="+ language;
    if (sort != null) {
      previousPageUrl = previousPageUrl + 
      "&sort=" + sort +
      "&reverse=" + reverse;
    }
%><a href="<%=previousPageUrl%>"><b><i18n:message key="previous"/></b></a>&nbsp;
<% } %>

<%
  for (long pageIndex = displayMin; pageIndex <= displayMax; pageIndex++) {
    long pageStart = (pageIndex - 1) * hitsPerPage;
    String pageUrl = "search.jsp?" +
      "query=" + URLEncoder.encode(request.getAttribute("query").toString(), "UTF-8") +
      "&dateStart="+ dateStartString + 
      "&dateEnd="+ dateEndString +
      "&pag=" + pageIndex +
      "&start=" + pageStart + 
      "&hitsPerPage=" + hitsPerPage +
      "&hitsPerDup=" + hitsPerDup +
      "&dedupField=" + dedupField +
      "&l="+ language;
    if (sort != null) {
      pageUrl = pageUrl + 
      "&sort=" + sort +
      "&reverse=" + reverse;
    }
    if (pageIndex != currentPage) {
%>
    <a href="<%=pageUrl%>"><%=pageIndex%></a>&nbsp;
<%
    }
        else {
%>
    <b><%=pageIndex%></b>
<% 
    } 
  }
%>

<%
  if (currentPage < pagesAvailable) {
    long nextPageStart = currentPage * hitsPerPage;
    String nextPageUrl = "search.jsp?" +
      "query=" + URLEncoder.encode(request.getAttribute("query").toString(), "UTF-8") +
      "&dateStart="+ dateStartString + 
      "&dateEnd="+ dateEndString + 
      "&pag=next" + 
      "&start=" + nextPageStart +
      "&hitsPerPage=" + hitsPerPage +
      "&hitsPerDup=" + hitsPerDup +
      "&dedupField=" + dedupField +
      "&l="+ language;
    if (sort != null) {
      nextPageUrl = nextPageUrl +
      "&sort=" + sort +
      "&reverse=" + reverse;
    }
%>

<a href="<%=nextPageUrl%>"><b><i18n:message key="next"/></b></a>&nbsp;

<% } %>
<% } %>			<%-- End of pager IF --%>

</div>

<%
  }


//----- What does this do? if ((!hitsTotalIsExact && (hitsLength <= start+hitsPerPage))) {
%>
<%--    <form action="search.jsp" method="get">
    <input type="hidden" name="query" value="<%=htmlQueryString%>" />
    <input type="hidden" name="start" value="<%=end%>" />
    <input type="hidden" name="hitsPerPage" value="<%=hitsPerPage%>" />
    <input type="hidden" name="hitsPerDup" value="<%=hitsPerDup%>" />
    <input type="hidden" name="dedupField" value="<%=dedupField%>" />
    <input type="submit" value="<i18n:message key="next"/>" />
<% if (sort != null) { %>
    <input type="hidden" name="sort" value="<%=sort%>" />
    <input type="hidden" name="reverse" value="<%=reverse%>" />
<% } %>
    </form>
<%
    }
%>
----------%>

<div id="after_results">
<% if (hitsTotal > 7) { %>
	<%@include file="include/search_form_bottom.jsp" %>
<% } %>
</div>

<div id="footer_utilities">
	<a href="<c:url value='http://sobre.arquivo.pt/'><c:param name='set_language' value='${language}'/></c:url>"><i18n:message key="about"/></a> | 
	<a href="<c:url value="terms_conditions.jsp"><c:param name='l' value='${language}'/></c:url>"><i18n:message key="utilityTermsConditions"/></a> | <a href="<c:choose>
<c:when test="${ param.l eq 'en' }">
	<c:out value="http://sobre.arquivo.pt/about-the-archive/how-does-it-work/technology"/>
</c:when>
<c:otherwise>
	<c:out value="http://sobre.arquivo.pt/sobre-o-arquivo/funcionamento/tecnologias/"/>
</c:otherwise>
</c:choose>"><i18n:message key="utilityTechnologies"/></a>
</div>

<%@include file="/footer.jsp" %>

</div>

</div>
<%-- Google Analytics code --%>
<script type="text/javascript">
	var _gaq = _gaq || [];
	_gaq.push(['_setAccount', 'UA-21825027-1']);
	_gaq.push(['_setDomainName', '.arquivo.pt']);
	_gaq.push(['_trackPageview']);
	(function() {
		var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
		ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
		var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
	})();
</script>
</body>
</html>

<%@include file="include/logging.jsp" %>
