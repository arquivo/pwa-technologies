<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd" >
<%@ page
	session="true"
	contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"	

	import="java.io.File"
	import="java.io.IOException"
	import="java.net.URLEncoder"
	import="java.text.DateFormat"
	import="java.util.Calendar"
	import="java.util.Date"
	import="java.util.regex.Matcher"
	import="java.util.regex.Pattern"
	import="java.util.GregorianCalendar"
	import="org.apache.hadoop.conf.Configuration"
	import="org.apache.lucene.search.PwaFunctionsWritable"
	import="org.apache.nutch.global.Global"
	import="org.apache.nutch.html.Entities"
	import="org.apache.nutch.metadata.Nutch"
	import="org.apache.nutch.searcher.Hit"
	import="org.apache.nutch.searcher.HitDetails"
	import="org.apache.nutch.searcher.Hits"
	import="org.apache.nutch.searcher.Query"
	import="org.apache.nutch.searcher.Query.Clause"
	import="org.apache.nutch.searcher.NutchBean"
	import="org.apache.nutch.searcher.Summary"
	import="org.apache.nutch.searcher.Summary.Fragment"
	import="org.archive.access.nutch.NutchwaxBean"
	import="org.archive.access.nutch.NutchwaxQuery"
	import="org.archive.access.nutch.NutchwaxConfiguration"
%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt_rt" prefix="fmt" %>
<%@ include file="include/logging_params.jsp" %>
<%@ include file="include/i18n.jsp" %>
<fmt:setLocale value="<%=language%>"/>

<%!	//To please the compiler since logging need those -- check [search.jsp]
	private static Calendar DATE_START = new GregorianCalendar(1996, 1-1, 1);
	private static final DateFormat FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");
	//TODO: remove dateStart & dateEnd ???
	//private static Calendar dateStart = new GregorianCalendar();
	//private static Calendar dateEnd = new GregorianCalendar();
	private static final DateFormat OFFSET_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	private static final Pattern OFFSET_PARAMETER = Pattern.compile("(\\d{4})-(\\d{2})-(\\d{2})");

	private static final String COLLECTION_KEY = "collection";
	private static final String COLLECTION_QUERY_PARAM_KEY = COLLECTION_KEY + ":";
	private static final Pattern URL_PATTERN = Pattern.compile("^.*? ?((https?:\\/\\/)?([a-zA-Z\\d][-\\w\\.]+)\\.([a-z\\.]{2,6})([-\\/\\w\\p{L}\\.~,;:%&=?+$#*]*)*\\/?) ?.*$");
%>
<%
  DateFormat DISPLAY_FORMAT = new SimpleDateFormat("dd MMMMMMMM, yyyy", new Locale(language));
%>
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


  /** Read the embargo offset value from the configuration page. If not present, default to: -1 year */
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

  /*****************    'hitsPerDup' param      ***************************/
  int hitsPerDup = 2;
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

  /*****************    'sort' param    ***************************/
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
  Calendar dateEnd = (Calendar)DATE_END.clone();                                // Setting current date

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

  /*****************    Offset param    ***************************/
  int start = 0;          // first hit to display
  String startString = request.getParameter("start");
  if (startString != null)
    start = Integer.parseInt(startString);

  /*****************    Hits/page param ***************************/
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

%>
<%---------------------- Start of HTML ---------------------------%>
<%-- TODO: define XML lang --%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="pt-PT" lang="pt-PT">
<head>
	<title><fmt:message key='search.meta.title'><fmt:param><c:out value='${requestScope.query}'/></fmt:param></fmt:message></title>
	<meta http-equiv="Content-Type" content="application/xhtml+xml; charset=UTF-8" />
	<%-- TODO: define META lang --%>
	<meta http-equiv="Content-Language" content="pt-PT" />
	<meta name="Keywords" content="<fmt:message key='search.meta.keywords'/>" />
	<meta name="Description" content="<fmt:message key='search.meta.description'/>" />
	<link rel="shortcut icon" href="img/logo-16.jpg" type="image/x-icon" />
	<link rel="search" type="application/opensearchdescription+xml" title="<fmt:message key='opensearch.title'><fmt:param value='<%=language%>'/></fmt:message>" href="opensearch.jsp?l=<%=language%>" />
	<link rel="stylesheet" title="Estilo principal" type="text/css" href="css/style.css"  media="all" />
	<link rel="stylesheet" type="text/css" href="css/jquery-ui-1.7.2.custom.css" />
	<script type="text/javascript">
                var minDate = new Date(<%=DATE_START.getTimeInMillis()%>);
                var maxDate = new Date(<%=DATE_END.getTimeInMillis()%>);
        </script>
        <script type="text/javascript" src="js/jquery-1.3.2.min.js"></script>
        <script type="text/javascript" src="js/jquery-ui-1.7.2.custom.min.js"></script>
        <script type="text/javascript" src="js/ui.datepicker.js"></script>
        <script type="text/javascript" src="js/ui.datepicker-pt-BR.js"></script>
        <script type="text/javascript" src="js/configs.js"></script>
</head>
<body>
	<%@ include file="include/topbar.jsp" %>
<%-- TODO: add loading feedback --%>
	<div class="wrap">
		<div id="main">
			<div id="header">
				<div id="logo">
					<a href="index.jsp" title="<fmt:message key='header.logo.link'/>">
						<img src="img/logo-2.gif" alt="<fmt:message key='header.logo.alt'/>" width="122" height="90" />
					</a>
				</div>
				<div id="search-header">
						<form id="loginForm" action="search.jsp" name="loginForm" method="get">
							<input type="hidden" name="l" value="<%= language %>" />
							<fieldset id="pesquisar">
								<label for="txtSearch">&nbsp;</label>
								<input class="search-inputtext" type="text" size="15" value="<%=htmlQueryString%>" name="query" id="txtSearch" accesskey="t" />
								<input type="reset" src="img/search-resetbutton.html" value="" alt="reset" class="search-resetbutton" name="btnReset" id="btnReset" accesskey="r" />
								<input type="submit" value="<fmt:message key='search.submit'/>" alt="<fmt:message key='search.submit'/>" class="search-submit" name="btnSubmit" id="btnSubmit" accesskey="e" />
								<%
									StringBuilder advUrl = new StringBuilder();
									advUrl.append("advanced.jsp?");

									if (htmlQueryString != null && !htmlQueryString.equals("")) {
										advUrl.append("query=");
										advUrl.append( URLEncoder.encode(htmlQueryString, "UTF-8") );
									}

									advUrl.append("&dateStart=");
									advUrl.append(dateStartString);
									advUrl.append("&dateEnd=");
									advUrl.append(dateEndString);

									advUrl.append("&hitsPerPage=");
									advUrl.append(hitsPerPage);

									advUrl.append("&l=");
									advUrl.append(language);

									if (sort != null) {
										advUrl.append("&sort=");
										advUrl.append(sort);
										if (reverse) {
											advUrl.append("&reverse=");
											advUrl.append(reverse);
										}
									}
								%>
								<a href="<%=advUrl.toString()%>" title="<fmt:message key='search.advanced'/>" id="pesquisa-avancada"><fmt:message key='search.advanced'/></a>
							</fieldset>
							<fieldset id="search-date">
								<div id="search-label-data">
									<label id="search-dateStart_top" for="dateStart_top"><fmt:message key='search.query-form.from'/></label>
									<div class="search-withTip">
										<input type="text" id="dateStart_top" name="dateStart" value="<%=dateStartString%>" />
									</div>
									<label id="search-labelDateEnd" for="dateEnd_top"><fmt:message key='search.query-form.to'/></label>
									<div class="withTip">
										<input type="text" id="dateEnd_top" name="dateEnd" value="<%=dateEndString%>" />
									</div>
								</div>
							</fieldset>
						</form>
				</div>
			</div>
<%-- END OF HEADER --%>
	
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
				String showTip = null;                  // tip to show
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

                        long startQueryTime = System.currentTimeMillis();               // for logging
%>

			</div> <%-- closes #main --%>
			</div> <%-- closes .wrap --%>
			
                        <%-- #search_stats & #result_list for this case are generated by WB --%>
                        <%
                                boolean seeHistory = false;             // This variable is used to indicate that link to see the history was clicked
                                if( request.getParameter("pos") != null) {
                                        seeHistory = true;
                                }
                                pageContext.setAttribute("seeHistory", seeHistory);
                        %>
                        <c:catch var="exception">
                                <c:import url="http://${collectionsHost}/query">
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
				<% bean.LOG.error("Error while accessing to wayback"); %>
				<div id="conteudo-resultado"> <%-- START OF: conteudo-resultado --%>
				<div id="first-column">
				        &nbsp;
				</div>
				<div id="second-column">
					<div id="search_stats"></div>
                        </c:if>

                        <% queryTime = (int) (System.currentTimeMillis() - startQueryTime); //for logging %>
				



<%

						} else {
							// option: (3)
				                        showList = true;
				                        showTip = urlMatch.group(1);
				                        query = NutchwaxQuery.parse(queryString, nutchConf);    //create the query object
				                        bean.LOG.debug("query: " + query.toString());
						}
					} else {
						// option: (1)
				                query = NutchwaxQuery.parse(queryString, nutchConf);            //create the query object
				                bean.LOG.debug("query: " + query.toString());
						
						showList = true;
					}
				}
				%>
					
			
<% if (showList) { %>

<div id="conteudo-resultado"> <%-- START OF: conteudo-resultado --%>
<div id="first-column">
	&nbsp;
</div>
<div id="second-column">
<h1><fmt:message key='search.query'><fmt:param><c:out value='${requestScope.query}'/></fmt:param></fmt:message></h1>

<%@include file="include/search-result-component.jsp"%>

<% } %> <%-- END OF: showList --%>

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
        // When empty query → intro page
        if ( request.getAttribute("query").equals("") ) {
%>
        <div id="search_stats"></div>
        <div id="no_results">
		<c:redirect url='index.jsp'>
			<c:param name='l' value='${language}'/>
		</c:redirect>
	</div>
        <% } else { %>
	<div id="conteudo-pesquisa-erro">
		<h2><fmt:message key='search.no-results.title'/></h2>
		<h3><%=htmlQueryString%></h3>

		<div id="sugerimos-que">
				<p><fmt:message key='search.no-results.suggestions'/></p>
			<ul>
				<li><fmt:message key='search.no-results.suggestions.well-written'/></li>
				<li><fmt:message key='search.no-results.suggestions.time-interval'/></li>
				<li><fmt:message key='search.no-results.suggestions.keywords'/></li>
				<li><fmt:message key='search.no-results.suggestions.generic-words'/></li>
				<%-- Show specific suggestions for URL queries --%>
				<% if ( usedWayback) { %>
				<li><fmt:message key='search.no-results.suggestions.internet-archive'><fmt:param value='<%=urlQuery%>'/></fmt:message></li>
				<li><fmt:message key='search.no-results.suggestions.suggest'><fmt:param value='<%=urlQuery%>'/></fmt:message></li>
				<% } %>
			</ul>
		</div>
		<div class="voltar-erro"><a href="<%= request.getHeader("Referer")%>">&larr; <fmt:message key='search.no-results.go-back'/></a></div>
	</div>

<%
        }

   } else if (showList && hitsTotal != 0 && hitsPerDup != 0 && currentPage >= pagesAvailable && hitsTotal > end) {
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
	<div class="omitted-results">
		<fmt:message key="search.results.omitted"><fmt:param value="<%=noDupQuery%>"/></fmt:message>.
	</div>
<% } %>


<% if (hitsTotal >= 1 && !usedWayback) { %>              <%-- Start Pager IF --%>
<div class="pagination">

<ul>
<%
if (currentPage > 1) {
long previousPageStart = (currentPage - 2) * hitsPerPage;
    String previousPageUrl = "search.jsp?" +
      "query=" + URLEncoder.encode(request.getAttribute("query").toString(), "UTF-8") +
      "&dateStart="+ dateStartString +
      "&dateEnd="+ dateEndString +
      "&pag=prev" +                             // mark as 'previous page' link 
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
%>
	<li class="previous"><a href="<%=previousPageUrl%>" title="<fmt:message key='search.pager.previous'/>"><fmt:message key='search.pager.previous'/></a></li>
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
    <li><a href="<%=pageUrl%>"><%=pageIndex%></a></li>
<%
    }
        else {
%>
    <li class="current"><%=pageIndex%></li>
<%
    }
  }

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
    <li class="next"><a href="<%=nextPageUrl%>" title="<fmt:message key='search.pager.next'/>"><fmt:message key='search.pager.next'/></a></li>
<% } %>

</ul>

</div>
<% } %>                 <%-- End of pager IF --%>
<% } %>
</div>

			</div>  <!-- FIM #conteudo-resultado  --> 
		</div>
	</div>
<%@include file="include/footer.jsp" %>
<%@include file="include/analytics.jsp" %>
</body>
</html>

<%@include file="include/logging.jsp" %>
