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

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="pt-PT" lang="pt-PT">
<head>
  <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css" integrity="sha384-Gn5384xqQ1aoWXA+058RXPxPg6fy4IWvTNh0E263XmFcJlSAwiGgFAW/dAiS6JXm" crossorigin="anonymous">
  <script src="https://code.jquery.com/jquery-3.2.1.slim.min.js" integrity="sha384-KJ3o2DKtIkvYIK3UENzmM7KCkRr/rE9/Qpg6aAZGJwFDMVNA/GpGFF93hXpG5KkN" crossorigin="anonymous"></script>
  <script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.12.9/umd/popper.min.js" integrity="sha384-ApNbgh9B+Y1QKtv3Rn7W3mgPxhU9K/ScQsAP7hUibX39j7fakFPskvXusvfa0b4Q" crossorigin="anonymous"></script>
  <script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/js/bootstrap.min.js" integrity="sha384-JZR6Spejh4U02d8jOt6vLEHfe/JQGiRRSQQxSfFWpi1MquVdAyjUar5+76PVCmYl" crossorigin="anonymous"></script>  
<%@include file="include/analytics.jsp" %>
</head>
<body>
      <br />
      <div class="container">
        <div class="row">
          <div class="col-xs-12 offset-md-2 col-md-8">
            <h1 class="text-center text-primary">Share on social networks</h1>                      
          </div>
        </div>       
        <div class="row">
          <div class="col-xs-12 offset-md-2 col-md-8">
            <form action = "showlink.jsp" method = "GET">
               <div class="form-group">
                <label for="url">URL</label>
                <textarea class="form-control" id="url" rows="1" name="url" ></textarea>     
                <small id="emailHelp" class="form-text text-muted">The url you want to share - the final link you want to redirect the users to</small>
               </div>
               <div class="form-group">
                <label for="title">Title</label>
                <textarea rows="1" name="title"  class="form-control"  id="title"></textarea>
                <small id="titleHelp" class="form-text text-muted">The title you want to share - a short text</small> 
               </div> 
               <div class="form-group">
                <label for="description">Description</label>
                <textarea rows="4" name="description"  class="form-control" id="description"></textarea>  
                <small id="descriptionHelp" class="form-text text-muted">A description you want to share - recommended size 14 or less words</small>       
               </div>
               <div class="form-group">
                <label for="image">Image URL</label>
                <textarea class="form-control" id="image" rows="1" name="image" ></textarea>
                <small id="imageHelp" class="form-text text-muted">The URL for the image you want to share</small>  
               </div> 
               <input type = "submit" class="btn btn-primary" value = "Generate share URL" />
            </form>
          </div>
         </div>   
        </div> 
</body>
</html>
