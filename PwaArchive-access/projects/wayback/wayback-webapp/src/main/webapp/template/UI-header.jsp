<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page import="org.archive.wayback.core.UIResults" %>
<%@ page import="org.archive.wayback.util.StringFormatter" %>
<%@ page import="java.net.URLDecoder" %>

<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8"%>
<%
UIResults results = UIResults.getFromRequest(request);
StringFormatter fmt = results.getFormatter();
String contextRoot = results.getContextPrefix();
String serverRoot = results.getServerPrefix();
%>
<!-- HEADER -->
<html xmlns="http://www.w3.org/1999/xhtml">

	<head>
		<meta http-equiv="content-type" content="text/html; charset=utf-8">
		      
		<link rel="stylesheet" type="text/css" 
			href="<%= contextRoot %>css/styles.css"
			src="<%= contextRoot %>css/styles.css">
		<link rel="shortcut icon" href="images/logo-16.jpg" type="image/x-icon"/> <! -- TODO MC -->
		<title><%= fmt.format("UIGlobal.pageTitle") %></title>
		<base target="_top">
	</head>

	<body onload="document.getElementById('query_field').focus()" bgcolor="white" alink="red" vlink="#0000aa" link="blue" 
		style="font-family: Arial; font-size: 10pt">

		<table width="100%" border="0" cellpadding="0" cellspacing="5">

			<tr>

				<!-- WAYBACK LOGO -->
				
				<td width="26%"><a href="<%= contextRoot %>"><img src="<%= contextRoot %>images/wayback_logo_sm.gif" border="0"></a></td> <!-- TODO MC -->

				<!-- /WAYBACK LOGO -->
			
				<!-- COLLECTION-EMPTYLOGO -->

				<td width="70%" align="right"></td>

				<!-- /COLLECTION-EMPTY LOGO -->

			</tr>

			<!-- GREEN BANNER -->
			<tr> 
				<td colspan="2" height="30" align="center" class="mainSecHeadW"> 
					<table width="100%" border="0" cellspacing="0" cellpadding="0">

						<tr class="mainBColor">
							<td colspan="2">
								<table border="0" width="80%" align="center">


									<!-- URL FORM -->
									<form action="<%= contextRoot %>query" method="get">


										<tr>
											<td nowrap align="center"><img src="<%= contextRoot %>images/shim.gif" width="1" height="20"> 
	
											       <%
											        String url = request.getParameter("url");
												if (url==null) {
												        url="http://";
												}
									
												url=new String(url.getBytes("ISO8859-1"),"UTF-8"); // TODO MC charset BUG
                                                                                                                                                                                              
                                                                                                
											        String date = request.getParameter("date");
												if (date==null) {
												        date=fmt.format("UIGlobal.selectYearAll");
												}
											        String aliases = request.getParameter("aliases");
											        String multDet = request.getParameter("multDet");
												if (multDet==null) {
													multDet="true";
												}
 											       %>

												<b class="mainBodyW">
													<font size="2" color="#FFFFFF" face="Arial, Helvetica, sans-serif">
														<%= fmt.format("UIGlobal.enterWebAddress") %>
													</font> 
													<input type="hidden" name="type" value="urlquery"/>
													<input type="hidden" name="multDet" value="<%= multDet %>"/>
													<input type="text" name="url" value="http://" size="24" maxlength="256"/>
													&nbsp;
												</b> 
												<select name="date" size="1">
													<option value="" <%= (date==null || date.equals("")) ? "SELECTED" : "" %> ><%= fmt.format("UIGlobal.selectYearAll") %></option>
													<%
													  for (int i=2009;i>=1996;i--) {
													%>
														<option <%= (date!=null && date.equals(""+i)) ? "SELECTED" : "" %> ><%= i %></option>
													<%
													  }
													%>
												</select>
												&nbsp;
												<font size="2" color="#FFFFFF" face="Arial, Helvetica, sans-serif">
													<%= fmt.format("UIGlobal.aliases") %>
												</font> 
												<input type="checkbox" name="aliases" value="true" <%= (aliases!=null && aliases.equals("true")) ? "CHECKED" : "" %> >
												&nbsp;
												<input type="submit" name="Submit" value="<%= fmt.format("UIGlobal.urlSearchButton") %>" align="absMiddle">
												&nbsp;
												<a href="<%= contextRoot %>advanced_search.jsp" style="color:white;font-size:11px">
													<%= fmt.format("UIGlobal.advancedSearchLink") %>
												</a>

											</td>
										</tr>


									</form>
									<!-- /URL FORM -->
									  
								</table>
							</td>
						</tr>
					</table>
				</td>
			</tr>
			<!-- /GREEN BANNER -->
		</table>
<!-- /HEADER -->
