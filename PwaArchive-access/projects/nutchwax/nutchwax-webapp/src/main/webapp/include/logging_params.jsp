<%
  boolean usedWayback = false;
  int numResults = 0;
  int queryTime = 0;					// Time taken by the search backend to return a query result
  long totalTime = System.currentTimeMillis();		// Total time taken by the page
  Calendar logDateEnd = new GregorianCalendar();	// The end date used in logging to see if the user changed the end date
  logDateEnd.set( Calendar.YEAR, logDateEnd.get(Calendar.YEAR)-1 );         // Set current year as _embargo_
  logDateEnd.set( Calendar.MONTH, 12-1 );
  logDateEnd.set( Calendar.DAY_OF_MONTH, 1 );
%>
