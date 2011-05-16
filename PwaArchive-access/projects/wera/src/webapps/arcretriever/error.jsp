<%@
	page contentType="text/xml" pageEncoding="UTF-8" isErrorPage="true"
        import="no.nb.nwa.retriever.*"
%><%
	out.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
	ArcRetrieverException are;
	if (exception instanceof ArcRetrieverException) {
		are = (ArcRetrieverException) exception;
	} else {
		are = new ArcRetrieverException(7, exception);
	}
%>
<retrievermessage>
  <head>
    <errorcode><%=are.getErrorCode()%></errorcode>
    <errormessage><%=are.getLocalizedMessage()%></errormessage>
  </head><%
  	if (are.getCause() != null) {
  		out.println("\n  <body>");
  		out.println("Cause: " + are.getCause().getClass().getName() + ": " + are.getCause().getLocalizedMessage());
  		out.println("\nStack trace:");
		StackTraceElement[] trace = are.getCause().getStackTrace();
  		for (int i=0; i<trace.length; i++) {
  			out.println(trace[i].toString().replace("<", "&lt;").replace(">", "&gt;"));
  		}
  		out.println("  </body>");
  	}%>
</retrievermessage>
