<%@ page import="java.util.regex.Pattern" %>
<%@ page import="java.util.regex.Matcher" %>
<%@ page import="java.text.DateFormat" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Date" %>

<%!
    /**
     * Time last new server was introduced.
     */
    private static long newServers = System.currentTimeMillis();

    /**
     * Interval between introduction of new servers.
     */
    private static final long CHANGE = 1000 * 10;

    /**
     * How many links per page?
     */
    private static final int LINKS_PER_PAGE = 10;

    /**
     * Maximum links to give out.
     * Use this if you want to do a fixed size crawl.
     * If -1, we give out links till cows come home.
     */
    private static final int MAX_LINKS = -1;

    private static int linksCount = 0;

    /**
     * Pattern for cutting up URLs.
     * See the DateFormat pattern below.
     */
    private static final Pattern pattern = Pattern.
        compile("(http://)(?:(?:(?:\\d\\d-){3}\\d{1,3}\\.)+)?([^/]+)/?.*");

    /**
     * Calendar instance used to format links.
     */
    private static final DateFormat df = new SimpleDateFormat("HH-mm-ss-S"); 

    /**
     * @param now Current time.
     * @result True if time to introduce a new server.
     */
    private synchronized boolean doNewServer(long now) {
        boolean result = false;
        if ((now - newServers) > CHANGE) {
            newServers = now;
            result = true;
        }
        return result;
    }
 %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
		<title>Generate Infinite URLs</title>
		<meta name="author" content="Debian User" >
		<meta name="generator" content="screem 0.11.2" >
		<meta name="description" content="" >
		<meta name="keywords" content="" >
		<meta http-equiv="content-type"
            content="text/html; charset=ISO-8859-1" >
		<meta http-equiv="Content-Script-Type" content="text/javascript" >
		<meta http-equiv="Content-Style-Type" content="text/css" >
	</head>
	<body>
		<p><b>Generated URLs</b>.</p>  
        <p>Incoming request was 
        <i><%=request.getRequestURL()%></i>.
        </p>
<%
    // See if time to introduce a new server.
    Date now = new Date();
    String prefix = "";
    String nowFormatted = null;
    synchronized (df) {
        nowFormatted = df.format(now); 
    };
    boolean newServer = doNewServer(now.getTime());
    if (newServer) {
        String url= request.getRequestURL().toString();
        Matcher m = pattern.matcher(url);
        if (m != null && m.matches()) {
            prefix = m.group(1) + nowFormatted + "." + m.group(2);
        %>
            <p>Introducing a new server <%=prefix%>.</p>
            <p>Group 1 <%=m.group(1)%>.</p>
            <p>Group 2 <%=m.group(2)%>.</p>
        <%
        }
    } else {
        long last = (now.getTime() - newServers);
    %>
        <p>A new server is introduced every <%=CHANGE%> milliseconds (Last
                new server was introduced <%=last%> milliseconds ago).</p>
    <%
    }

    if (MAX_LINKS >= 0 && this.linksCount > MAX_LINKS) {
        // We've hit maximum links.  Stop giving them out.
    } else {
        // Print out links.
        for (int i = 0; i < LINKS_PER_PAGE; i++) {
            String url = prefix + request.getContextPath() + "/infinity/" +
                i + ((!newServer)? "-" + nowFormatted: "") + ".html";
    %>
        <a href="<%=url%>"><%=i%></a><%
        if (i < (LINKS_PER_PAGE - 1)) {
            // Print a comma separator.
        %>, <%
            }
            this.linksCount++;
            if (MAX_LINKS >= 0 && this.linksCount > MAX_LINKS) {
                break;
            }
        }
    }
%>
	</body>
</html>
