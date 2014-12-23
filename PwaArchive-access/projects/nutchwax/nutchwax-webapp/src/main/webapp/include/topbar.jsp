<div id="language">
<!--
	<img src="img/experimental.png" alt="<fmt:message key='topbar.experimental.alt'/>" width="123" height="124" />
-->
	<div class="wrap">
		<ul>
			<li><a href="<c:url value='http://sobre.arquivo.pt/'><c:param name='set_language' value='${language}'/></c:url>" title="<fmt:message key='topbar.help'/>" class="ajuda"><fmt:message key='topbar.help'/></a></li>
		</ul>
		<ul class="langs">
		<c:choose>
			<c:when test="${language eq 'pt'}">
			<li><a href="?l=pt" title="<fmt:message key='topbar.portuguese'/>" class="activo"><fmt:message key='topbar.portuguese'/></a></li>
			<li><a href="?l=en" title="<fmt:message key='topbar.english'/>"><fmt:message key='topbar.english'/></a></li>
			</c:when>
			<c:otherwise>
			<li><a href="?l=pt" title="<fmt:message key='topbar.portuguese'/>"><fmt:message key='topbar.portuguese'/></a></li>
			<li><a href="?l=en" title="<fmt:message key='topbar.english'/>" class="activo"><fmt:message key='topbar.english'/></a></li>
			</c:otherwise>
		</c:choose>
		</ul>
	</div>
</div>
