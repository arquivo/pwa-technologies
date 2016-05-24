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

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%!
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
int resultCount = results.getResultsMatching();

//Timestamp searchStartTs = results.getStartTimestamp();
//Timestamp searchEndTs = results.getEndTimestamp();
//String prettySearchStart = results.prettyDateFull(searchStartTs.getDate());
//String prettySearchEnd = results.prettyDateFull(searchEndTs.getDate());

ArrayList<ResultsPartition> partitions = ResultsPartitionsFactory.get(
		results.getResults(),results.getWbRequest());
int numPartitions = partitions.size();
%>
<div id="search_stats">
	<span>
		<fmt:message key="resultsSummary">
			<fmt:param value="<%=resultCount%>"/>
		</fmt:message>
	</span>
</div>
<div id="result_list">

	<c:if test="${not param.hist}">
	<p class="info">
		<%
			String searchUrl = "search.jsp?query=";
			searchUrl += URLEncoder.encode("\""+ searchString +"\"", "UTF-8");
			if (!language.equals("pt")) {
				searchUrl += "&l=" + language;
			}
		%>
		<fmt:message key="alternativeSearchTip">
			<fmt:param value="<%= searchUrl %>"/>
			<fmt:param value="<%= searchString %>"/>
		</fmt:message>
	</p>
	</c:if>

<table border="0" width="100%" style="margin: 20px 0;">
   <tr bgcolor="#CCCCCC">
      <td colspan="<%= numPartitions %>" align="center" class="mainCalendar">
	<fmt:message key="searchResults">
		<fmt:param value="<%=searchStartDate%>" />
		<fmt:param value="<%=searchEndDate%>" />
	</fmt:message>
      </td>
   </tr>

<!--    RESULT COLUMN HEADERS -->
   <tr bgcolor="#CCCCCC">
<%
	int i;
	for(i = 0; i < numPartitions; i++) {
		ResultsPartition partition = partitions.get(i);
		if ( Integer.parseInt(partition.getTitle()) <= embargoYear ) {
		%>
			<td align="center" class="mainBigBody">
		<%} else {%>
			<td align="center" class="mainBigBody" style="background-color: #CEE164">
		<% } %>
		<%= partition.getTitle() %>
		</td>
	<%}%>
   </tr>
<!--    /RESULT COLUMN HEADERS -->



<!--    RESULT COLUMN COUNTS -->
   <tr bgcolor="#CCCCCC">
	<%
	for(i = 0; i < numPartitions; i++) {
		ResultsPartition partition = (ResultsPartition) partitions.get(i);
		if ( Integer.parseInt(partition.getTitle()) <= embargoYear ) {
		%>
			<td align="center" class="mainBigBody">
				<fmt:message key="columnSummary">
					<fmt:param value="<%=partition.resultsCount()%>" />
				</fmt:message>
      			</td>
		<%} else {%>
      			<td align="center" class="mainBigBody" style="background-color: #CEE164">---</td>
		<%}%>
	<%}%>
   </tr>
<!--    /RESULT COLUMN COUNTS -->

<!--    RESULT COLUMN DATA -->
   <tr bgcolor="#EBEBEB">
<%
	int indexColumnWithResults = 0;
	int totalColumnsWithResults = 0;

	// count number of columns/years with results
	for(i = 0; i < numPartitions; i++) {
		ResultsPartition partition = (ResultsPartition) partitions.get(i);
		ArrayList<SearchResult> partitionResults = partition.getMatches();
		
		if(partitionResults.size() != 0) {
			totalColumnsWithResults++;
		}
	}

	// populate table
	for(i = 0; i < numPartitions; i++) {
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
	
	%>
	      <td nowrap class="mainBody" valign="top">
	<%
			if(partitionResults.size() == 0) {
	%>
			         &nbsp;
	<%
			} else {
			  indexColumnWithResults++;
				
			  for(int j = 0; j < partitionResults.size(); j++) {
			  
			  	SearchResult result = partitionResults.get(j);
				String captureDate = result.get(WaybackConstants.RESULT_CAPTURE_DATE);
				Timestamp captureTS = Timestamp.parseBefore(captureDate);
			
				String replayUrl = results.resultToReplayId(result);
	
				replayUrl += "?year="+ partition.getTitle() +"&r_pos="+ (j+1) +"&r_t="+ partition.resultsCount() +"&col_pos="+ indexColumnWithResults +"&col_tot="+ totalColumnsWithResults +"&l="+ language +"&sid="+ sid;
			%>
				<a href="<%= replayURIPrefix %><%= replayUrl %>"><fmt:message key="entryText"><fmt:param value="<%=captureTS.getDate()%>"/></fmt:message></a><br></br>
			<%
			  }
			}
		} else {
		%>
			<td nowrap style="background-color: #CEE164;text-align: center; font-weight: bold" valign="top">
			<c:choose>
                		<c:when test="${ param.l eq 'en' }">
		                        <a href="http://sobre.arquivo.pt/faq/access-to-archived-contents/?searchterm=embargo#section-0"><fmt:message key="embargoColumn"/></a>
                		</c:when>
		                <c:otherwise>
                		        <a href="http://sobre.arquivo.pt/perguntas-frequentes/acesso-a-conteudos-arquivados/?searchterm=embargo#section-0"><fmt:message key="embargoColumn"/></a>
		                </c:otherwise>
		        </c:choose>
      </td>	
		<%
		}
	}
	%>
	      </td>
   </tr>
<!--    /RESULT COLUMN DATA -->
</table>

<%
// show page indicators:
if(results.getNumPages() > 1) {
	int curPage = results.getCurPage();
	%>
	<hr></hr>
	<%
	for(i = 1; i <= results.getNumPages(); i++) {
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
