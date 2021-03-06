<form class="search_form" action="search.jsp" method="get">
	<div class="queryField">
		<input id="query_top" name="query" value="<%=htmlQueryString%>" />
		<% if (!language.equals("pt")) { %>
			<input type="hidden" name="l" value="<%= language %>" />
		<% } %>
		<input type="hidden" name="hitsPerPage" value="<%=hitsPerPage%>" />
		<% if (collection != null) { %>
			<input type="hidden" name="collection" value="<%=collection%>" />
		<% } %>
		<%---- Hides the 'sort' and 'reverse' params
		<% if (sort == null) { %>
			<input type="hidden" name="sort" value="<%=sort%>" />
			<input type="hidden" name="reverse" value="<%=reverse%>" />
		<% } %>
		-----%>
	</div>
	<div class="dateStartField">
		<label class="labelDateStart" for="dateStart_top"><i18n:message key="intervalStart"/></label>
		<div class="withTip">
			<input type="text" id="dateStart_top" name="dateStart" value="<%=dateStartString%>" />
			<br />
			<span class="tip"><i18n:message key="tip"/></span>
		</div>
	</div>
	<div class="dateEndField">
		<label class="labelDateEnd" for="dateEnd_top"><i18n:message key="intervalEnd"/></label>
		<div class="withTip">
			<input type="text" id="dateEnd_top" name="dateEnd" value="<%=dateEndString%>" />
			<br />
			<span class="tip"><i18n:message key="tip"/></span>
		</div>
	</div>
	<div>
		<input class="submit" type="submit" value="<i18n:message key="search"/>" />
	</div>
	<div class="advanced">
        <%
                StringBuilder advUrl = new StringBuilder();
                advUrl.append("advanced.jsp?"); 

		if (htmlQueryString != null && !htmlQueryString.equals("")) {
                	advUrl.append("query=");
	                advUrl.append( URLEncoder.encode(htmlQueryString, "UTF-8") );
		}

                advUrl.append("&dateStart=");
                advUrl.append(dateStartString);
                advUrl.append("&dateEnd=");
                advUrl.append(dateEndString);

                advUrl.append("&hitsPerPage=");
                advUrl.append(hitsPerPage);     

		advUrl.append("&l=");
		advUrl.append(language);

                if (sort != null) {
                        advUrl.append("&sort=");
                        advUrl.append(sort);
                        if (reverse) {
                                advUrl.append("&reverse=");
                                advUrl.append(reverse);
                        }
                }
        %>
        	<a href="<%= advUrl.toString() %>"><i18n:message key="advancedSearch"/></a>
	</div>

</form>
