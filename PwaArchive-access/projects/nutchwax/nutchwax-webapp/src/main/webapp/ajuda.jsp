<%@ page
	contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"

	import="java.util.Locale"
%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@include file="include/i18n.jsp" %>
<i18n:bundle baseName="org.nutch.jsp.help" locale="<%=new Locale(language)%>" />
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN">
<html>
<head>
	<link type="image/x-icon" href="img/logo-16.jpg" rel="shortcut icon"/>
	<link href="css/style.css" type="text/css" rel="stylesheet">
	<title>Ajuda de Pesquisa</title>
</head>
<body>
<div id="header">
	<%@include file="header.jsp" %>
	<div id="title">
		<h1><i18n:message key="title"/></h1>
	</div>
</div>
<div id="utilities">
	<a href="<c:url value="http://t33.tomba.fccn.pt/nutchwax/ajuda.jsp">
		<c:if test="${ (empty param.l) || (param.l eq 'pt') }">
			<c:param name='l' value='en'/>
		</c:if>
	</c:url>"><i18n:message key="otherLang"/></a>
	|
	<span class="focused"><i18n:message key="help"/></span>
        |
	<a href="http://arquivo-web.fccn.pt/"><i18n:message key="about"/></a>
</div>
<div id="main_content">
	<div class="info_bar">
		<span><a href="#nutchwax">NutchWax</a> | <a href="#wayback">Wayback</a></span>
	</div>

	<div>
	<h2><a name="nutchwax">Nutchwax</a></h2>
	Pesquisa sobre os documentos:
	<ul>
		<li>Operador <b>AND</b> - contento todos os termos (ex. <kbd>arquivo web</kbd>).</li>
		<li>Operador <b>NOT</b> - contento todos os termos, excepto alguns: (ex. <kbd>arquivo web -pesquisa</kbd>)</li>
		<li>Operador <b>EXACT PHRASE</b> - contendo a frase (ex. <kbd>"arquivo da web"</kbd>)</li>
		<li>Operador <b>SITE</b> - contendo os documentos do site (ex. <kbd>site:www.record.pt</kbd>)
		<li>Operador <b>FILETYPE</b> - contendo o tipo (ex. <kbd>type:pdf</kbd> ou <kbd>type:text/html</kbd>)</li>
		<li>Operador <b>EXACTURL</b> - contendo o url (ex. <kbd>exacturl:http://www.fccn.pt/</kbd>)</li>
		<li>Operador <b>DATE_RANGE</b> - contendo os documentos entre duas datas com o formato <em>yyyymmddHHMMSS</em> (ex. <kbd>date:20041212000000-20051213000000</kbd>)</li>
		<li>Operador <b>SORT</b> - ordenar os resultados por data (ex. no browser adicionar <code>&sort=date</code>) <em>NOTA</em>: este processo e lento</li>
		<li>Operador <b>HITS_PAGE</b> - numero de hits por pagina (ex. no browser adicionar <code>&hitsPerPage=10</code>)</li>
		<li>Operador <b>HITS_SITE</b> - numero maximo de hits por site (ex. no browser adicionar <code>&hitsPerSite=2</code>)</li>
		<li>E as combinacoes entre estes operadores.</li>
	</ul>
	</div>
	<div>
	<h2><a name="wayback">Wayback</a></h2>
	Pesquisa sobre os documentos:
	<ul>
		<li>Operador <b>URL</b> - contendo o url (ex. <kbd>http://www.sapo.pt</kbd>).</li>
		<li>Operador <b>AliASES</b> - contendo o url com todos os aliases, por exemplo com e sem / no final, com e sem index.html, etc (eg.<code> &aliases=true</code>).</li>
		<li>Operador <b>DATE</b> - contendo a data (ex. <code>&date=2004</code> ou <code>&date=20070624</code>)</li>
		<li>Operador <b>DATE_RANGE</b> - contendo os documentos entre duas datas com o formato <em>yyyymmddHHMMSS</em> (ex. no browser adicionar <code>&startdate=20031212000000&enddate=20081213000000</code>)</li>  
		<li>E as combinacoes entre estes operadores.</li>
	</ul>
	</div>
</div>

</body>
</html>
