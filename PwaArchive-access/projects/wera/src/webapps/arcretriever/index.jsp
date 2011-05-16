<%@ page language="java"%>
<!DOCTYPE HTML PUBLIC "-//w3c//dtd html 4.0 transitional//en">
<html>
<head>
 <TITLE>arcretriever</TITLE>
 <LINK REL="STYLESHEET" TYPE="text/css" HREF="style.css">
</head>
<body bgcolor="#FFFFFF">
  <H1><img src="./images/logo.jpg" border=0 alt="Nordic Web Archive">ARC
    Retriever</H1>
  <p>This application is
   capable of delivering web documents from archives in the ARC format.</p>
  <H2>Configuration</H2>
  <p>Set the <i>arcdir</i> in the <i>WEB-INF/web.xml</i> to point at the
  directory that holds ARC files and then redeploy this webapp.
            Be aware that changing this value in the web.xml of an 
            <i>arcretriever</i> sitting under a containers' webapp directory
            can prove frustrating.  The container usually notices your
            change and redeploys.  But if you are not careful, 
            you will lose your edit.
            If you remove the WAR file version, the container will
            subsequently 'cleanup' the lone webapp directory.
            Best to unjar outside of the container webapp directory and
            copy the unjarred WAR under the webapp dir.
  </p>
  <h2>Request Parameters</h2>
  <p>This webapp takes the following request parameters.
    <ul>
        <li><b>reqtype</b>: Possible values include: <i>getfile</i>,
        <i>getmeta</i>, <i>getfilestatus</i>, and <i>getarchiveinfo</i>.</li>
        <li><b>aid</b>: The archive identifier.  Its format is
        <i>OFFSET '/' ARCNAME</i>.</li>
    </ul>
  </p>

  <H2>wera</H2>
   You can find information about wera at
   <A href='http://archive-access.sourceforge.net/projects/wera/'>this page</A>.
  <H3>Configuring wera for using ARC retriever</H3>
  <P>In the wera <i>lib/config.inc</i> file set the value of
  $document_retriever to <A HREF="arcretriever">
  <%=request.getRequestURL().substring(0, request.getRequestURL().lastIndexOf("/")).concat("/arcretriever")%>
   </A></P>

  <H2>License Information</H2>
   The arcretriever is open source software.
   Read <A href="LICENSE.txt">more</A> about the license information.
</body>
</html>
