<%@ page import="java.util.regex.Pattern" %>
<%@ page import="java.util.regex.Matcher" %>

<%!
    /**
     * Number of unique seeds.
     */
    private static final int SEED_COUNT = 200;

    /**
     * Pattern for cutting up URLs.
     * See the DateFormat pattern below.
     */
    private static final Pattern pattern =
        Pattern.compile("(http://)([^/]+).*");
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
		<title>Seeds Page</title>
		<meta name="author" content="St.Ack" >
		<meta name="generator" content="screem 0.11.2" >
		<meta name="description" content="This is home page of the Infinite URLs Application.  Will return infinite relative URLs." >
		<meta name="keywords" content="Infinite URLs" >
		<meta http-equiv="content-type" content="text/html; charset=ISO-8859-1" >
		<meta http-equiv="Content-Script-Type" content="text/javascript" >
		<meta http-equiv="Content-Style-Type" content="text/css" >
	</head>
	<body>
		<h1>Seeds</h1>
<%
    // Cut up the URL that got us here.
    String baseurl= request.getRequestURL().toString();
    Matcher m = pattern.matcher(baseurl);
    if (m != null && m.matches()) {
        // Print out seeds. 
        for (int i = 0; i < SEED_COUNT; i++) {
            String url = m.group(1) + i + "." + m.group(2) +
                request.getContextPath() + "/infinity/";
    %>  
            <a href="<%=url%>"><%=i%></a><%
                if (i < (SEED_COUNT - 1)) {
                // Print a comma separator.
        %>, <%
                }
        }
    }
%>  
	</body>
</html>
