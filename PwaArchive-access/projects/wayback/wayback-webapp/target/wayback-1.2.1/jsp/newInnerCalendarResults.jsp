<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8"%>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.net.URLDecoder" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Calendar" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.GregorianCalendar" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.util.regex.Matcher" %>
<%@ page import="java.util.regex.Pattern" %>
<%@ page import="org.archive.wayback.WaybackConstants" %>
<%@ page import="org.archive.wayback.archivalurl.ArchivalUrlResultURIConverter" %>
<%@ page import="org.archive.wayback.core.SearchResult" %>
<%@ page import="org.archive.wayback.core.Timestamp" %>
<%@ page import="org.archive.wayback.core.UIResults" %>
<%@ page import="org.archive.wayback.query.UIQueryResults" %>
<%@ page import="org.archive.wayback.query.resultspartitioner.ResultsPartitionsFactory" %>
<%@ page import="org.archive.wayback.query.resultspartitioner.ResultsPartition" %>
<%@ page import="org.archive.wayback.util.StringFormatter" %>
<%@ page import="org.archive.wayback.webapp.AccessPoint" %>
<%@ page import="org.springframework.beans.factory.xml.XmlBeanFactory" %>
<%@ page import="org.springframework.core.io.FileSystemResource" %>
<%@ page import="org.springframework.core.io.Resource" %>
<%@ page import="org.apache.log4j.Logger" %>
<%@ page import="org.apache.log4j.Level" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%!
	static Logger logger = Logger.getLogger("newInnerCalendar.jsp");

	private static final Pattern OFFSET_PARAMETER = Pattern.compile("(\\d{4})-(\\d{2})-(\\d{2})");

        /* Get the replay URI prefix during servelet initialization */
        static String replayURIPrefix = null;
        
        public void jspInit() {
                Resource res = new FileSystemResource( getServletContext().getRealPath("")+"/WEB-INF/wayback.xml");
                XmlBeanFactory factory = new XmlBeanFactory(res);
                AccessPoint ap = (AccessPoint) factory.getBean("80:wayback");
        
                replayURIPrefix = ((ArchivalUrlResultURIConverter)ap.getUriConverter()).getReplayURIPrefix();
        }
%>

<%-- Obtain the embargo date offset configuration parameter --%>
<%-- WARNING: only year offset is supported --%>
<%
	int embargoYear;	

	try {
		String offsetDateString = getServletContext().getInitParameter("embargo-offset");

		Matcher offsetMatcher = OFFSET_PARAMETER.matcher( offsetDateString );
	        offsetMatcher.matches();
        	int offsetYear = Integer.parseInt(offsetMatcher.group(1));

		GregorianCalendar current = new GregorianCalendar();
		embargoYear = current.get(Calendar.YEAR) - offsetYear;
	} catch(IllegalStateException e) {
		// Catch exception here
		embargoYear = (new GregorianCalendar()).get(Calendar.YEAR);
	} catch(NullPointerException e) {
		// Catch exception here
		embargoYear = (new GregorianCalendar()).get(Calendar.YEAR);
	}
%>

<%-- lang parameter handling / i18n processing --%>
<%
        String language = "pt";

        String langParam = request.getParameter("l");
        if (langParam != null) {
                if (langParam.equals("en")) {
                        language = langParam;
                } else { /* keep default */ };
        }
        
        String sid = request.getParameter("sid"); // session id
%>
<fmt:setLocale value="<%= language %>"/>
<%
UIQueryResults results = (UIQueryResults) UIResults.getFromRequest(request);

StringFormatter fmt = results.getFormatter();
String searchString = results.getSearchUrl();

Date searchStartDate = results.getStartTimestamp().getDate();
Date searchEndDate = results.getEndTimestamp().getDate();
int totalResults = results.getResultsMatching();

/*** Logging of request parameters ***/
if (logger.isEnabledFor(Level.DEBUG)) {
    logger.debug("--- DEBUG VALUES ---");
	logger.debug("RESULT COUNT: \t"+ totalResults);
	logger.debug("LANG: \t\t"+ language);
	logger.debug("START DATE: \t"+ searchStartDate);
	logger.debug("END DATE: \t\t"+ searchEndDate);
	logger.debug("SID: \t\t"+ sid);
	logger.debug("--- -------------- ---");
}
/*** end of logging ***/

ArrayList<ResultsPartition> partitions = ResultsPartitionsFactory.get(
		results.getResults(),results.getWbRequest());
int numPartitions = partitions.size();
/* Reduce the number of partitions to reflect the embargo year */
for (int i = 0; i < numPartitions; i++) {
	ResultsPartition partition = partitions.get(i);
	if (Integer.parseInt(partition.getTitle()) > embargoYear) {
		numPartitions = i;
	}
}

%>
<c:if test="${not param.hist}">
	<%
	String searchUrl = "search.jsp?query=";
	searchUrl += URLEncoder.encode("\""+ searchString +"\"", "UTF-8");
	if (!language.equals("pt")) {
		searchUrl += "&l=" + language;
	}
	%>
	<div class="clear">&nbsp;</div>
	<div id="resultados-url">
	<fmt:message key='grid.suggestion'><fmt:param value='<%=searchUrl%>'/><fmt:param value='<%=searchString%>'/></fmt:message>
	</div>
</c:if>

<div class="wrap">
	<div id="intro">
	<h1><fmt:message key='grid.title'/></h1>
        <span class="texto-1"><fmt:message key='grid.subtitle'><fmt:param value='<%=totalResults%>'/><fmt:param value='<%=searchString%>'/><fmt:param value="<%=searchStartDate%>"/><fmt:param value="<%=searchEndDate%>"/></fmt:message></span>
	</div>
</div>


<div id="conteudo-versoes">
	<div id="resultados-lista">
        	<table class="tabela-principal">
<!--	RESULT COLUMN HEADERS -->
                	<thead>
                	
			<tr>
<%
	for(int i = 0; i < numPartitions; i++) {
		ResultsPartition partition = partitions.get(i);
		if ( partition.resultsCount() > 0 ) {
		%>
			<th>
		<%} else {%>
			<th class="inactivo">
		<% } %>
		<%= partition.getTitle() %><span class="versoes-num"><%=partition.resultsCount()%></span>
			</th>
	<%}%>
	
		<th class="mais-resultados-title"><fmt:message key='grid.embargo.title'/></th>
	
			</tr>
			</thead>
<!--    /RESULT COLUMN HEADERS -->

<!--    RESULT COLUMN DATA -->
<tbody>
<%
	int totalColumnsWithResults = 0;

	// count number of columns/years with results
	// TODO: i'm not sure we will still need this block
	for(int i = 0; i < numPartitions; i++) {
		ResultsPartition partition = (ResultsPartition) partitions.get(i);
		ArrayList<SearchResult> partitionResults = partition.getMatches();
		
		if(partitionResults.size() != 0) {
			totalColumnsWithResults++;
		}
	}

	/*** Logging of response variables ***/
	if (logger.isEnabledFor(Level.DEBUG)) {
		logger.debug("--- DEBUG VALUES ---");
		logger.debug("# OF COLUMNS: \t"+ numPartitions);
		logger.debug("COLs WITH RESULTS: \t"+ totalColumnsWithResults);
		logger.debug("--- ------------ ---");
	}
	/*** ***************************** ***/

	// Sort each year results and put them in grid object
	ArrayList<ArrayList<SearchResult>> gridPartitionResults = new ArrayList<ArrayList<SearchResult>>(numPartitions);

	for(int i = 0; i < numPartitions; i++) {
		ResultsPartition partition = (ResultsPartition) partitions.get(i);
		if ( Integer.parseInt(partition.getTitle()) <= embargoYear) {
			ArrayList<SearchResult> partitionResults = partition.getMatches();
	
			// sort results/dates
			SearchResult[] arrResults=partitionResults.toArray(new SearchResult[partitionResults.size()]);		
			Arrays.sort(arrResults, new SearchResult());
			partitionResults=new ArrayList<SearchResult>();
		        for (int j=0;j<arrResults.length;j++) {
	  			partitionResults.add(arrResults[j]);
			}
			gridPartitionResults.add(partitionResults);
		}
	}

	/*** Debug info: values for grid filling ***/
	if (logger.isEnabledFor(Level.DEBUG)) {
		logger.debug("--- FILLING GRID ---");
		logger.debug("--- Presenting "+ totalResults +" results in "
			+ totalColumnsWithResults +" of "+ numPartitions +" columns.");
	}

	int resultCounter = 0;			// track the number of versions already processed
	int line = 0;				    // track the grid line we are processing
	while (resultCounter < totalResults) {
		%><tr><%

		/*** Debug info: filling grid line ***/
		if (logger.isEnabledFor(Level.DEBUG)) {
			logger.debug(resultCounter +" versions already displayed. ");
			logger.debug("STARTING LINE: "+ line);
		}

		int indexColumnWithResults = 0;
		for (int i = 0; i < numPartitions; i++) {
			ResultsPartition partition = (ResultsPartition) partitions.get(i);
			ArrayList<SearchResult> column = gridPartitionResults.get(i);

			if (column.size() > 0)
				indexColumnWithResults++;

			if (column.size() > 0 && column.size() > line) {
				SearchResult result = column.get(line);

				String captureDate = result.get(WaybackConstants.RESULT_CAPTURE_DATE);
                                Timestamp captureTS = Timestamp.parseBefore(captureDate);
		
				String replayUrl = results.resultToReplayUrl(result);
				// Add URL parameters for HTTP log
// 				replayUrl += "?year="+ partition.getTitle()		// the column of the selected result
// 					+"&r_pos="+ (line+1)	 			// the result's position on the column
// 					+"&r_t="+ partition.resultsCount() 		// how many results in the column
// 					+"&col_pos="+ indexColumnWithResults		// Which of the columns with results we are on
// 					+"&col_tot="+ totalColumnsWithResults		// How many year columns have results
// 					+"&l="+ language
// 					+"&sid="+ sid;					// Session ID
				%>
				<!-- <td><a href="<%=replayURIPrefix%><%=replayUrl%>" title="<fmt:message key='grid.result.link.title'><fmt:param value='<%=captureTS.getDate()%>'/></fmt:message>"><fmt:message key='grid.result.link.text'><fmt:param value='<%=captureTS.getDate()%>'/></fmt:message></a></td> -->
				<td><a href="<%=replayUrl%>" title="<fmt:message key='grid.result.link.title'><fmt:param value='<%=captureTS.getDate()%>'/></fmt:message>"><fmt:message key='grid.result.link.text'><fmt:param value='<%=captureTS.getDate()%>'/></fmt:message></a></td>
				<%
				resultCounter++;
			} else {
				%><td>&nbsp;</td><%
			}
			
		}

		if (resultCounter == totalResults){ %>
	
		<%-- More --%>
		
<td valign="top" class="mais-resultados">
		
			<span>
			<fmt:message key='grid.embargo.info'/>
			<c:choose>
                                <c:when test="${ param.l eq 'en' }">
                                        <a href="http://sobre.arquivo.pt/faq/access-to-archived-contents/?searchterm=embargo#section-0"><fmt:message key="grid.embargo.link.text"/></a>
                                </c:when>
                                <c:otherwise>
                                        <a href="http://sobre.arquivo.pt/perguntas-frequentes/acesso-a-conteudos-arquivados/?searchterm=embargo#section-0"><fmt:message key="grid.embargo.link.text"/></a>
                                </c:otherwise>
                        </c:choose>
			</span>
		</td>
		<%
			}
	%></tr><%
		line++;
	 } %>
</tbody>
</table>


<%
// show page indicators:
if(results.getNumPages() > 1) {
	int curPage = results.getCurPage();
	%>
	<hr></hr>
	<%
	for(int i = 1; i <= results.getNumPages(); i++) {
		if(i == curPage) {
			%>
			<b><%= i %></b>
			<%		
		} else {
			%>
			<a href="<%= results.urlForPage(i) %>"><%= i %></a>
			<%
		}
	}
}
%>
