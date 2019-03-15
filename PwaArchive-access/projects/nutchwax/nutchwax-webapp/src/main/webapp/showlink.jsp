<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd" >
<%@ page
	session="true"
	contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"	

	import="java.io.File"
	import="java.util.Calendar"
	import="java.util.Date"
	import="java.util.GregorianCalendar"
	import="java.net.URLEncoder"
	import= "java.net.*"
	import= "java.io.*"	
	import="java.text.DateFormat"
	import="java.util.Calendar"
	import="java.util.TimeZone"
	import="java.util.Date"
	import="java.util.regex.Matcher"
	import="java.util.regex.Pattern"
	import="java.util.GregorianCalendar"
  import="org.apache.hadoop.conf.Configuration"
  import="org.apache.lucene.search.PwaFunctionsWritable"
  import="org.apache.nutch.global.Global"
  import="org.apache.nutch.html.Entities"
  import="org.apache.nutch.metadata.Nutch"
  import="org.apache.nutch.searcher.Hit"
  import="org.apache.nutch.searcher.HitDetails"
  import="org.apache.nutch.searcher.Hits"
  import="org.apache.nutch.searcher.Query"
  import="org.apache.nutch.searcher.Query.Clause"
  import="org.apache.nutch.searcher.NutchBean"
  import="org.apache.nutch.searcher.Summary"
  import="org.apache.nutch.searcher.Summary.Fragment"
  import="org.archive.access.nutch.NutchwaxBean"
  import="org.archive.access.nutch.NutchwaxQuery"
  import="org.archive.access.nutch.NutchwaxConfiguration"
%>
<% // Set the character encoding to use when interpreting request values.
  request.setCharacterEncoding("UTF-8");
%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt_rt" prefix="fmt" %>
<%@ include file="include/logging_params.jsp" %>
<%@ include file="include/i18n.jsp" %>
<fmt:setLocale value="<%=language%>"/>
<%
  String title = request.getParameter("title");
  if (title == null) {
    title = "Arquivo.pt";
  }
  String description = request.getParameter("description");
  if (description == null) {
    description = "Pesquise páginas do passado desde 1996";
  }
  String image = request.getParameter("image");
  if (image == null) {
    image = "//arquivo.pt/img/logoFace.png";
  }

  String url = request.getParameter("url");
  if (url == null) {
    url = "https://arquivo.pt";
  } 

  String serverURL = request.getScheme() + "://" +
             request.getServerName() + 
             ("http".equals(request.getScheme()) && request.getServerPort() == 80 || "https".equals(request.getScheme()) && request.getServerPort() == 443 ? "" : ":" + request.getServerPort() ); 
  
  String shareLink = serverURL + "/oglink.jsp?url="+URLEncoder.encode(url, "UTF-8")+"&title="+URLEncoder.encode(title,"UTF-8")+"&description="+URLEncoder.encode(description,"UTF-8")+"&image="+URLEncoder.encode(image,"UTF-8");

  image = URLDecoder.decode(image.replace("+", "%2B"), "UTF-8");
  url = URLDecoder.decode(url.replace("+", "%2B"), "UTF-8");

%>
<style>
.breakword{
  word-wrap: break-word;
}
.active{
  background-color: lightblue!important;
  border: solid 1px lightgrey!important;
  color: #353839!important;
}
.alert{
    display: none;
    margin-top: 5px;
}
</style>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="pt-PT" lang="pt-PT">
<head>
<%@include file="include/analytics.jsp" %>
  <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css" integrity="sha384-Gn5384xqQ1aoWXA+058RXPxPg6fy4IWvTNh0E263XmFcJlSAwiGgFAW/dAiS6JXm" crossorigin="anonymous">
  <script src="https://code.jquery.com/jquery-3.2.1.slim.min.js" integrity="sha384-KJ3o2DKtIkvYIK3UENzmM7KCkRr/rE9/Qpg6aAZGJwFDMVNA/GpGFF93hXpG5KkN" crossorigin="anonymous"></script>
  <script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.12.9/umd/popper.min.js" integrity="sha384-ApNbgh9B+Y1QKtv3Rn7W3mgPxhU9K/ScQsAP7hUibX39j7fakFPskvXusvfa0b4Q" crossorigin="anonymous"></script>
  <script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/js/bootstrap.min.js" integrity="sha384-JZR6Spejh4U02d8jOt6vLEHfe/JQGiRRSQQxSfFWpi1MquVdAyjUar5+76PVCmYl" crossorigin="anonymous"></script>  
<script type="text/javascript">
  function copyToClipboard() {
    /* Get the text field */
    var copyText = document.getElementById("sharelink");

    /* Select the text field */
    copyText.select();

    /* Copy the text inside the text field */
    document.execCommand("copy");

    /* Alert the copied text */
    $('.alert').show();
  }
</script>  
</head>
   <body>
    <br/>
    <div class="container">
      <div class="row">
        <div class="col-xs-12 offset-md-2 col-md-8">
          <h2 class="text-center text-primary">Share link for social networks</h1>
        </div>
      </div> 
      <br/>
      <div class="row">
        <div class="col-xs-12 offset-md-2 col-md-8">
          <div class="form-group">
            <label for="sharelink">Share URL</label>
            <input type="text" id="sharelink" class="form-control" value="<%=shareLink%> "> 
            <button onclick="copyToClipboard()"  data-dismiss="alert"  class="btn btn-primary" style="margin-top: 5px;">Copy share URL</button> 
            <div class="alert alert-success alert-dismissable">
                Success! link copied successfully.
            </div>            
          </div>
        </div>
      </div>
      <br/>                 
      <div class="row">
        <div class="col-xs-12 offset-md-2 col-md-8 breakword">
          <ul class="list-group">       
             <li class="list-group-item active" ><b>URL</b>
             </li>             
             <li class="list-group-item" ><%=url%>
             </li>       
             <li class="list-group-item active"><b>Title</b>
             </li>    
             <li class="list-group-item">
                <%=title%>
             </li>   
             <li class="list-group-item active"><b>Description</b>
             </li> 
             <li class="list-group-item">
                <%=description%>
             </li>
             <li class="list-group-item active"><b>Image URL</b>
             <li class="list-group-item">
                <%=image%>
             </li>
          </ul>
       </div>
     </div>
   </body>
</html>
