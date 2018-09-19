<%@ page 
  session="false"
  contentType="text/html; charset=UTF-8"
  pageEncoding="UTF-8"

  import="java.io.*"
  import="java.util.*"
  import="java.text.*"
  import="java.net.*"
  import="java.util.regex.Pattern"

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

%><%!
  public static final DateFormat FORMAT =
    new SimpleDateFormat("yyyyMMddHHmmss");
  public static final DateFormat DISPLAY_FORMAT =
    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  private static final String COLLECTION_KEY = "collection";
  private static final String COLLECTION_QUERY_PARAM_KEY = COLLECTION_KEY + ":";
%><%

  // Set the character encoding to use when interpreting request values 
  request.setCharacterEncoding("UTF-8");


  String file  = request.getParameter("file");
  String docno = request.getParameter("docno");
  if (file!=null && docno!=null) {
	
	try {
		File f=new File(file);
		FileInputStream fin=new FileInputStream(f);
		byte buf[]=new byte[(int)f.length()];
		fin.read(buf, 0, (int)f.length());
		fin.close();

		StringBuffer sbuf=new StringBuffer(new String(buf));
		int indexDocno=sbuf.indexOf(docno);
		int indexStart=sbuf.indexOf("</DOCHDR>",indexDocno)+10;
		int indexEnd=sbuf.indexOf("</DOC>",indexStart);
		String sdoc=sbuf.substring(indexStart,indexEnd);
%>		
		<%= sdoc %>
<%
	}
	catch (FileNotFoundException e) {
%>
                <%= e.getMessage() %>
<%	}
	catch (IOException e) {
%>
                <%= e.getMessage() %>
<% 
	}
  }
  else {
%>
	MISSING PARAMETERS
<% 
  }
%>

</body>
</html>
