<%@ page
	session="true"
	contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"	

	import="java.net.URLDecoder"
	import="java.util.Calendar"
	import="java.util.Date"
	import="java.util.GregorianCalendar"
	import="java.util.Locale"
	import="java.util.ResourceBundle"
	import="java.util.regex.Matcher"
	import="java.util.regex.Pattern"
	import="java.text.DateFormatSymbols"
	import="java.text.SimpleDateFormat"
	import="org.apache.nutch.html.Entities"
%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ include file="include/logging_params.jsp" %>
<%@ include file="include/simple_params_processing.jsp" %>
<%@ include file="include/i18n.jsp" %>
<i18n:bundle baseName="org.nutch.jsp.advanced" locale="<%= new Locale(language) %>"/>

<%!	//To please the compiler since logging need those -- check [search.jsp]
	private static int hitsTotal = -10;		// the value -10 will be used to mark as being "advanced search"
	private static Calendar DATE_START = new GregorianCalendar(1996, 1-1, 1);
	private static final Pattern OFFSET_PARAMETER = Pattern.compile("(\\d{4})-(\\d{2})-(\\d{2})");
%>
<%
	SimpleDateFormat paramDateFormat = new SimpleDateFormat("dd/mm/yyyy");
	Calendar dateStart = new GregorianCalendar();
	dateStart.setTimeInMillis( paramDateFormat.parse(dateStartString).getTime() );
	Calendar dateEnd = new GregorianCalendar();
	dateEnd.setTimeInMillis( paramDateFormat.parse(dateEndString).getTime() );
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
  } catch(NullPointerException e) {
        // Set the default embargo period to: 1 year
        DATE_END.set( Calendar.YEAR, DATE_END.get(Calendar.YEAR) - 1);
  }
%>

<%---------------------- Start of HTML ---------------------------%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="<%=language%>" xml:lang="<%=language%>">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	<%@ include file="include/metadata.jsp" %>
	<title><i18n:message key="title"/></title>
	<link rel="shortcut icon" href="img/logo-16.jpg" type="image/x-icon" />
	<link type="text/css" href="css/jquery-ui-1.7.2.custom.css" rel="stylesheet"/>
	<script type="text/javascript" src="js/jquery-1.3.2.min.js"></script>
	<script type="text/javascript" src="js/ui.datepicker.js"></script>
<% if (language.equals("pt")) { %>
	<script type="text/javascript" src="js/ui.datepicker-pt-BR.js"></script>
<% } %>
	<script type="text/javascript">
                var minDate = new Date(<%=DATE_START.getTimeInMillis()%>);
                var maxDate = new Date(<%=DATE_END.getTimeInMillis()%>);
        </script>
	<script type="text/javascript" src="js/configs.js"></script>
	<link rel="stylesheet" type="text/css" href="css/advanced.css" />
</head>
<body class="oneColElsCtrHdr">

<div id="header">
	<%@include file="header.jsp" %>
        <div id="title">
                <h1><i18n:message key="header"/></h1>
        </div>
</div>
<div class="info_bar"></div>
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
                        <a href="http://sobre.arquivo.pt/faq/advanced-search"><i18n:message key="help"/></a>
                </c:when>
                <c:otherwise>
                        <a href="http://sobre.arquivo.pt/perguntas-frequentes/pesquisa-avancada"><i18n:message key="help"/></a>
                </c:otherwise>
        </c:choose>
        |
        <a href="<c:url value='http://sobre.arquivo.pt/'><c:param name='set_language' value='${language}'/></c:url>"><i18n:message key="about"/></a>
</div>
<%--<div id="main_content">--%>

<div id="container">

  <div id="mainContent" class="spacing">
    <form method="get" action="search.jsp">
	<input type="hidden" name="l" value="<%=language%>" />
    	<div id="sub-title">
    	    <h3><i18n:message key="formTitle"/></h3>
		    <input type="submit" value="<i18n:message key="submit"/>" />
        </div>
    	<div>
        	<fieldset id="words">
            	<legend><i18n:message key="words"/></legend>
		<div>
	                <label for="adv_and"><i18n:message key="withWords"/></label>
			<div class="withTip">
	        	       	<input type="text" id="adv_and" name="adv_and" value="<%=and.toString()%>" />
				<br />
				<span class="tip"><i18n:message key="withWordsTip"/></span>
			</div>
			<div style="clear: both"></div>
		</div>
		<div class="line-spacing spacing">
	                <label for="adv_phr"><i18n:message key="withPhrase"/></label>
			<div class="withTip">
	                	<input type="text" id="adv_phr" name="adv_phr" value="<%=phrase.toString()%>" />
				<br />
				<span class="tip"><i18n:message key="withPhraseTip"/></span>
			</div>
			<div style="clear: both"></div>
		</div>
		<div class="spacing">
	                <label for="adv_not"><i18n:message key="withoutWords"/></label>
			<div class="withTip">
                		<input type="text" id="adv_not" name="adv_not" value="<%=not.toString()%>" />
				<br />
				<span class="tip"><i18n:message key="withoutWordsTip"/></span>
			</div>
		</div>
            </fieldset>
        </div>
        <div>
        	<fieldset id="date">
            	<legend><i18n:message key="date"/></legend>
                <label for="dateStart_top"><i18n:message key="intervalStart"/></label>
                <div class="withTip">
                	<input type="text" id="dateStart_top" name="dateStart" value="<%=dateStartString%>" /><br />
    	            <span class="tip"><i18n:message key="tip"/></span>
                </div>
                <label id="labelDateEnd" for="dateEnd_top"><i18n:message key="intervalEnd"/></label>
                <div class="withTip">
	                <input type="text" id="dateEnd_top" name="dateEnd" value="<%=dateEndString%>" /> <br />
	                <span class="tip"><i18n:message key="tip"/></span>
                </div>
                <div class="spacing"></div>
                <div>
                <label for="sort"><i18n:message key="sort"/></label>
		<select id="sort" name="sort">
			<%	
			if (sortType == null) {			//use the default sorting behavior - relevance %>
				<option value="relevance" selected="selected"><i18n:message key="relevance"/></option>
			<% } else { %>
				<option value="relevance"><i18n:message key="relevance"/></option>
			<% }
				if ("date".equals(sortType) && sortReverse) {
			%>
				<option value="new" selected="selected"><i18n:message key="newFirst"/></option>	
				<% } else { %>
				<option value="new"><i18n:message key="newFirst"/></option>
				<% }
				if ("date".equals(sortType) && !sortReverse) {
				%>
				<option value="old" selected="selected"><i18n:message key="oldFirst"/></option>
				<% } else { %>
				<option value="old"><i18n:message key="oldFirst"/></option>
				<% } %>
		</select>
                </div>
            </fieldset>
        </div>
        <div>
        	<fieldset id="format">	
            	<legend><i18n:message key="format"/></legend>
		<label for="formatType"><i18n:message key="formatLabel"/></label>
		<select id="formatType" name="format">
		<%
			String[] mimeList = {"pdf", "ps", "html", "xls", "ppt", "doc", "rtf"};
			String[] mimeListDetail = {"Adobe PDF (.pdf)", "Adobe PostScript (.ps)",
				"HTML (.htm, .html)", "Microsoft Excel (.xls)", "Microsoft PowerPoint (.ppt)",
				"Microsoft Word (.doc)", "Rich Text Format (.rtf)"};

			if ("relevance".equals(format)) {
			%>
				<option value="all" selected="selected"><i18n:message key="allFormats"/></option>
			<% } else { %>
				<option value="all"><i18n:message key="allFormats"/></option>
			<%		
			}

			for (int i = 0; i < mimeList.length; i++) {
				if (mimeList[i].equals(format)) {
					out.print("<option value=\""+ mimeList[i] +"\" selected=\"selected\">"+ mimeListDetail[i] +"</option>");
				} else {
					out.print("<option value=\""+ mimeList[i] +"\">"+ mimeListDetail[i] +"</option>");
				}
			}
		%>
		</select>
            </fieldset>
        </div>
        <div>
        	<fieldset id="domains" class="no-margin">
            	<legend><i18n:message key="website"/></legend>
                <label for="site"><i18n:message key="withAddress"/></label>
                <div class="withTip">
                	<input type="url" id="site" name="site" value="<%=site%>"/><br />
	                <span class="tip"><i18n:message key="addressExample"/></span>
                </div>
            </fieldset>
        </div>
	<div>
		<fieldset id="num_result_fieldset">
			<legend><i18n:message key="resultsNumber"/></legend>
			<label for="num_result"><i18n:message key="resultsNumberShow"/></label>
			<select id="num_result" name="hitsPerPage">
			<% int[] hitsPerPageValues = {10, 20, 30, 50, 100};
				for (int i = 0; i < hitsPerPageValues.length; i++) {
					if (hitsPerPage == hitsPerPageValues[i]) {
						out.print("<option selected=\"selected\">"+ hitsPerPageValues[i]+"</option>");
					} else {
						out.print("<option>"+ hitsPerPageValues[i]+ "</option>");
					}	
				}
			%>
			</select>
			<i18n:message key="resultsNumberLabel"/>
		</fieldset>
	</div>
        <div id="bottom-submit">
		<input type="submit" value="<i18n:message key="submit"/>" />
        </div>
    </form>
	<!-- end #mainContent --></div>
    <div style="padding-bottom: 2em"></div>
    <hr class="spacing" />
<!-- end #container --></div>
<%-- Google Analytics tracking code --%>
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
