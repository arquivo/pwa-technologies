<form class="search_form" action="search.jsp" method="get">
	<div class="queryField">
		<input id="query_bottom" name="query" value="<%=htmlQueryString%>" />
		<% if (!language.equals("pt")) { %>
			<input type="hidden" name="l" value="<%= language %>" />
		<% } %>
		<input type="hidden" name="hitsPerPage" value="<%=hitsPerPage%>" />
		
		<% if (collection != null) { %>
			<input type="hidden" name="collection" value="<%=collection%>" />
		<% } %>
		<%--------------------
		<% if (sort != null) { %>
			<input type="hidden" name="sort" value="<%=sort%>" />
			<input type="hidden" name="reverse" value="<%=reverse%>" />
		<% } %>
		----------------%>
	</div>	
	<div class="dateStartField">
		<label class="labelDateStart" for="dateStart_bottom"><i18n:message key="intervalStart"/></label>
		<div class="withTip">
			<input type="text" id="dateStart_bottom" name="dateStart" value="<%=dateStartString%>" />
			<br />
			<span class="tip"><i18n:message key="tip"/></span>
		</div>
	</div>
	<div class="dateEndField">
		<label class="labelDateEnd" for="dateEnd_bottom"><i18n:message key="intervalEnd"/></label>
		<div class="withTip">
			<input type="text" id="dateEnd_bottom" name="dateEnd" value="<%=dateEndString%>" />
			<br />
			<span><i18n:message key="tip"/></span>
		</div>
	</div>
	<div>
		<input class="submit" type="submit" value="<i18n:message key="search"/>" />
	</div>
        <div class="advanced">
               <a href="<%= advUrl.toString() %>"><i18n:message key="advancedSearch"/></a>
        </div>

	<div style="clear: both"></div>
</form>
