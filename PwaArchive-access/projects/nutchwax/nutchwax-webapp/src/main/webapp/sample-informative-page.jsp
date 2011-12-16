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
%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt_rt" prefix="fmt" %>
<%@ include file="include/logging_params.jsp" %>
<%@ include file="include/i18n.jsp" %>
<fmt:setLocale value="<%=language%>"/>

<%!	//To please the compiler since logging need those -- check [search.jsp]
	private static int hitsTotal = -10;		// the value -10 will be used to mark as being "advanced search"
	private static Calendar DATE_START = new GregorianCalendar(1996, 1-1, 1);
	private static Calendar dateStart = new GregorianCalendar();
	private static Calendar dateEnd = new GregorianCalendar();
%>

<%---------------------- Start of HTML ---------------------------%>

<%-- TODO: define XML lang --%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="pt-PT" lang="pt-PT">
<head>
	<title><fmt:message key='sample.meta.title'/></title>
	<meta http-equiv="Content-Type" content="application/xhtml+xml; charset=UTF-8" />
	<%-- TODO: define META lang --%>
	<meta http-equiv="Content-Language" content="pt-PT" />
	<meta name="Keywords" content="<fmt:message key='sample.meta.keywords'/>" />
	<meta name="Description" content="<fmt:message key='sample.meta.description'/>" />
	<link rel="shortcut icon" href="img/logo-16.jpg" type="image/x-icon" />
	<link rel="stylesheet" title="Estilo principal" type="text/css" href="css/style.css"  media="all" />
	<script type="text/javacsript" src="stream-min.js"></script>
</head>
<body>
	<%@ include file="include/topbar.jsp" %>
	<div class="wrap">
		<div id="main">
			<div id="header">
				<div id="logo">
					<a href="index.jsp" title="<fmt:message key='header.logo.link'/>">
						<img src="img/logo-<%=language%>.png" alt="<fmt:message key='header.logo.alt'/>" width="125" height="90" />
					</a>
				</div>
				<div id="info-texto-termos">
					<h1><fmt:message key='sample.title'/></h1>
					<h2><fmt:message key='sample.subtitle'/></h2>
				</div>
			</div>

			<div id="conteudo-termos">
				<p>Donec cursus suscipit augue curabitur lectus duis sem mi, faucibus ac, varius eu pellentesque ac nisi. Sed fringilla ut elementum eros ac quam. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae, proin elementum mauris aliquam a magna vestibulum nisl venenatis sollicitudin aenean urna augue, rhoncus sed, imperdiet vitae lacinia elit. Sed rhoncus curabitur nisi velit viverra.</p>
				<h3>Ut felis felis venenatis</h3>
				<p>Dapibus nunc eu nulla. In velit lectus, dictum in, egestas id, congue quis, odio cras vel nisl sed mi ullamcorper luctus cras tincidunt sapien at eros donec a lorem. Suspendisse vulputate orci id magna, morbi sodales gravida neque vivamus neque nunc, pulvinar sed, interdum vitae, varius quis nisl.</p>
				<h4>Duis aliquet vestibulum eget nibh at magna.</h4>
				<p>Maecenas nisi ligula, pellentesque aliquam, hendrerit quis, ultricies quis, urna. Etiam congue velit in erat. Proin ac orci. Mauris odio nibh, blandit nec, aliquet vitae, porta sed, libero. Nulla felis. Praesent sit amet ipsum. Donec congue aliquam quam.</p>

				<ul>
					<li>Curabitur non libero.</li>
					<li>Vestibulum dolor mi, dignissim
						<ul>
							<li>Pellentesque mollis,</li>
							<li>Posuere ut sapien.</li>
						</ul>
					</li>
					<li>Praesent sit amet ligula eget urna porttitor congue.</li>
					<li>Vestibulum ante ipsum primis</li>
				</ul>

				<h4>Ut eget felis at sapien congue imperdiet. Nulla eleifend.</h4>

				<p>Fusce quis libero et nibh sollicitudin viverra. Vivamus malesuada nibh at lorem. Mauris venenatis semper leo. Vivamus eros nunc, adipiscing id, fermentum et, volutpat non, nunc.</p>

				<ol>
					<li><p>Nullam quis ipsum.</p></li>
					<li><p>Curabitur viverra nisl ut nisl.</p></li>
					<li><p>Maecenas vitae purus quis ante molestie aliquam</p></li>
					<li><p>Nunc eleifend arcu ut massa.</p></li>
				</ol>

				<p>Quisque nulla lacus, sollicitudin vitae, malesuada semper, hendrerit vel, nisi. Cras ligula libero, mollis id, posuere eu, tincidunt at, dolor. Pellentesque at lacus eleifend <a href="leo-mollis.html" title="Leo mollis gravida">leo mollis gravida</a>. Sed lobortis libero dapibus quam. Nullam et nulla at metus lobortis feugiat. Curabitur laoreet, ipsum vel vulputate placerat, eros lectus semper lacus, nec vulputate leo</p>

			</div>
			
			<div class="ultima-modificacao">
				<hr />
				<%
				String jspPath = getServletContext().getRealPath(request.getServletPath());
				File jspFile = new File(jspPath);
				Date lastModified = new Date(jspFile.lastModified()); 
				%>
				<p><fmt:message key='sample.last-modification'><fmt:param value='<%=lastModified%>'/></fmt:message></p>
			</div>

			<div id="conteudos-relacionados">
				<h5>Conteúdos relacionados:</h5>
				<ul>
					<li>
						<a href="#" title="Quisque nulla lacus, sollicitudin vitae">Quisque nulla lacus, sollicitudin vitae</a>
					</li>
					<li>
						<a href="#" title="Pellentesque at lacus">Pellentesque at lacus</a>
					</li>
					<li class="excel">
						<a href="#" title="Mauris venenatis semper leo">Mauris venenatis semper leo</a><span class="tipo">(XLS, 210kb)</span>
					</li>
					<li class="powerpoint">
						<a href="#" title="Donec congue aliquam">Donec congue aliquam</a>  <span class="tipo">(PPT, 157kb)</span>
					</li>
					<li class="word">
						<a href="#" title="Aenean blandit suscipit">Aenean blandit suscipit</a> <span class="tipo">(DOC, 83kb)</span>
					</li>
					<li class="pdf">
						<a href="#" title="Cras ligula libero">Cras ligula libero</a>  <span class="tipo">(PDF, 171kb)</span>
					</li>
					<li class="download">
						<a href="#" title="Sed lobortis libero dapibus quam">Sed lobortis libero dapibus quam</a>  <span class="tipo">(JPG, 171kb)</span>
					</li>
				</ul>
			</div>
		</div>
	</div>
<%@include file="include/footer.jsp" %>
<%@include file="include/analytics.jsp" %>
</body>
</html>

<%@include file="include/logging.jsp" %>
