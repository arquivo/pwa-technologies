<%@ page 
	import="java.util.Date"
	import="java.util.GregorianCalendar"
	import="java.util.Locale"
	import="java.text.SimpleDateFormat"
%>
<%@ taglib uri="http://jakarta.apache.org/taglibs/log-1.0" prefix="log" %>

<%
	totalTime = System.currentTimeMillis() - totalTime;
%>

<%
  SimpleDateFormat logDateFormatter = new SimpleDateFormat("dd/MMM/yyyy:hh:mm:ss Z", Locale.ENGLISH);
  String referer = request.getHeader("referer") != null ? request.getHeader("referer"):"-";

  boolean startDateChanged = false;
  boolean endDateChanged = false;

  StringBuilder logEntry = new StringBuilder();
  logEntry.append( "#session#" );
  logEntry.append( " " );
  logEntry.append( request.getRemoteHost() );
  logEntry.append( " - " );
  logEntry.append( session.getId() );
  logEntry.append( " [" );
  logEntry.append( logDateFormatter.format(new Date(session.getLastAccessedTime())) );
  logEntry.append( "] \"");
  logEntry.append( request.getMethod() );
  logEntry.append( " " );
  logEntry.append( request.getServletPath() );
	
  logEntry.append( "?" );

  if ( hitsTotal > -1 && ( (request.getParameter("query") != null) || (request.getParameter("adv_and") != null) ) ) {
	logEntry.append( request.getQueryString() );

	if ( dateStart.get(Calendar.YEAR) != DATE_START.get(Calendar.YEAR)) {
		startDateChanged = true;
	} else if ( dateStart.get(Calendar.MONTH) != DATE_START.get(Calendar.MONTH)) {
		startDateChanged = true;
	} else if ( dateStart.get(Calendar.DAY_OF_YEAR) != DATE_START.get(Calendar.DAY_OF_YEAR)) {
		startDateChanged = true;
	}
	
	if ( dateEnd.get(Calendar.YEAR) != logDateEnd.get(Calendar.YEAR)) {
		endDateChanged = true;
	} else if ( dateEnd.get(Calendar.MONTH) != logDateEnd.get(Calendar.MONTH)) {
		endDateChanged = true;
	} else if ( dateEnd.get(Calendar.DAY_OF_YEAR) != logDateEnd.get(Calendar.DAY_OF_YEAR)) {
		endDateChanged = true;
	}
	
	logEntry.append( "&str_date_changed=" );
	logEntry.append( startDateChanged );
	logEntry.append( "&end_date_changed=" );
	logEntry.append( endDateChanged );
	
	logEntry.append( "&hist=" );
	logEntry.append( usedWayback );
	logEntry.append( "&num_res=" );
	logEntry.append( hitsTotal);
	logEntry.append( "&t_q=" );
	logEntry.append( queryTime );
  }	
  
  logEntry.append( "&t_t=" );
  logEntry.append( totalTime );

  logEntry.append( " " );
  logEntry.append( request.getProtocol() );
  logEntry.append( "\" 200 -1 \"" );
  logEntry.append( referer );
  logEntry.append( "\" \"" );
  logEntry.append( request.getHeader("User-Agent") );

%>
<log:info>
	<%= logEntry.toString() %>
</log:info>
