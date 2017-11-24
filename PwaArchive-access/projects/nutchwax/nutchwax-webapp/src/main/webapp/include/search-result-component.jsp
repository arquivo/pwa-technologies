<%
   PwaFunctionsWritable functions= PwaFunctionsWritable.parse(nutchConf.get(Global.RANKING_FUNCTIONS));
   int queryMatches = Integer.parseInt(nutchConf.get(Global.MAX_FULLTEXT_MATCHES_RANKED));

   do {
       try {
                long startQueryTime = System.currentTimeMillis();
                hits = bean.search(query, start + hitsPerPage, queryMatches, hitsPerDup, dedupField, sort, reverse, functions, hitsPerVersion);
                queryTime = (int) (System.currentTimeMillis() - startQueryTime);

                hitsLength = hits.getLength();
                hitsTotal = hits.getTotal();
                hitsTotalIsExact = hits.totalIsExact();
       } catch (IOException e) {
          hits = new Hits(0, new Hit[0]);
       }
       // Handle case where start is beyond how many hits we have.
       end = (int)Math.min(hitsLength, start + hitsPerPage);
       if (hitsLength <= 0 || end > start) {
           break;
       }
       while (end <= start && start > 1) {
           start -= hitsPerPage;
           if (start < 1) {
               start = 1;
           }
       }
   } while (true);

%>

<!--
<div id="resultados">
<%--- TODO: updates this values --%>
<fmt:message key='search.results'>
        <fmt:param value='<%=new Long((end==0)?0:(start+1))%>'/>
        <fmt:param value='<%=new Long(end)%>'/>
        <fmt:param value='<%=new Long(hitsTotal)%>'/></fmt:message>
</div>
-->

<%-- Show tip if present --%>
<%--
        <% if (showTip != null) { %>
                <p class="info">
                        <i18n:message key="seeUrlTip">
                                <i18n:messageArg value="<%=allVersions%>"/>
                                <i18n:messageArg value="<%=showTip%>"/>
                        </i18n:message>
                </p>
        <% } %>
--%>

<div class="spell hidden"><fmt:message key="search.spellchecker"/> <span class="suggestion"></span></div>

<%-- Show search tip if the showTip option is active --%>
<% if (showTip != null) { %>
<div id="resultados-url">
        <%-- TODO: change this URL --%>
        <fmt:message key='search.suggestion'><fmt:param value='<%=allVersions%>'/><fmt:param value='<%=showTip%>'/></fmt:message>
</div>
<% } %>

<div id="resultados-lista">
<ul>
<%

   // be responsive
   // TODO: doesn't this work? out.flush();

  int length = end-start;
   int realEnd = (int)Math.min(hitsLength, start + hitsPerPage);

   Hit[] show = hits.getHits(start, realEnd-start);
   HitDetails[] details = bean.getDetails(show);
   Summary[] summaries = bean.getSummary(details, query);
   bean.LOG.debug("total hits: " + hitsTotal);

   int[] positionIndex = new int[show.length];
   int indexPos = 0;
   Hit[] showCopy = show.clone();

   for (int i = 0; i < show.length; i++) {
      if ( showCopy[i] != null) {
        positionIndex[indexPos++] = i;
        showCopy[i] = null;

        String host = show[i].getDedupValue();

        for (int j = i + 1; j < show.length; j++ ) {
                if ( showCopy[j] != null && host.equals( showCopy[j].getDedupValue() ) ) {
                        positionIndex[indexPos++] = j;
                        showCopy[j] = null;
                }
        }
      }
    }
    showCopy = null;


%>
<%-- TODO: spellchecker + suggestion --%>
<%
          // Saves information about the previous result's host so same-host results can be grouped.
          String previous_host = "";

          //Format the results
          for (int i = 0; i < length; i++) {      // display the hits
            Hit hit = show[ positionIndex[i] ];
            HitDetails detail = details[ positionIndex[i] ];
            String title = detail.getValue("title").trim();
            String current_host = hit.getDedupValue();
            int position = start + i + 1;
            pageContext.setAttribute("position", position);

            String caching = detail.getValue("cache");
            boolean showSummary = true;
            if (caching != null) {
              showSummary = !caching.equals(Nutch.CACHING_FORBIDDEN_ALL);
            }

            Date archiveDate = new Date(Long.valueOf(detail.getValue("date")).longValue()*1000);
            String archiveCollection = detail.getValue("collection");
            String url = detail.getValue("url");
            SimpleDateFormat ft = new SimpleDateFormat ("yyyyMMddHHmmss");
            TimeZone zone = TimeZone.getTimeZone("GMT");
            ft.setTimeZone(zone);
	    // If the collectionsHost includes a path do not add archiveCollection.
            // See http://sourceforge.net/tracker/index.php?func=detail&aid=1288990&group_id=118427&atid=681140.
            //String target = "http://"+ collectionsHost +"/id"+ hit.getIndexDocNo() +"index"+ hit.getIndexNo();
	    // Changed to return in wayback query format
              String target = "http://"+ collectionsHost +"/"+ ft.format(archiveDate)  +"/"+ url;
            pageContext.setAttribute("target", target);
            allVersions = "search.jsp?query="+ URLEncoder.encode(url, "UTF-8") +"&dateStart="+ dateStartString + "&dateEnd="+ dateEndString +"&pos="+ String.valueOf(position);

            if (!language.equals("pt")) {
                allVersions += "&l="+ language;
            }

            /*** Result's Title ***/
            // Process the result's title:
            // - Try to match and highlight the words that are both in the title and query
            // - Control the size of the title

            // use url for docs w/o title
            if (title == null || title.equals("")) {
              title = url;
            }

            // match and highlight the words
            String[] splittedTitle = title.split(" ");
            StringBuilder newTitle = new StringBuilder();

            final int TITLE_MAX_LENGTH = 60;
            int tagLengthCount = 0;

	    outerLoop: // JN: spaghetti-code!!!
            for ( String s : splittedTitle ) {

                if (newTitle.length() > 0) {
                        newTitle.append(" ");
                }

                innerLoop: {
                        for ( Clause clause : query.getClauses() ) {
                                if ( clause.isRequired() && !clause.isPhrase() ) {
                                        if ( s.compareToIgnoreCase( clause.toString() ) == 0 ) {
                                                // Check we don't go over the title max size (without counting the tags size)
                                                if ( (newTitle.length() + s.length() - tagLengthCount) < TITLE_MAX_LENGTH ) {
                                                        newTitle.append("<em>");
                                                        newTitle.append( s );
                                                        newTitle.append("</em>");

                                                        tagLengthCount += 9;

                                                        // Proceed to the next title word
                                                        break innerLoop;
                                                } else {
                                                        // Over the title max size, we can finish
                                                        newTitle.append("...");
                                                        break outerLoop;
                                                }
                                        }
                                }
                        }
                        newTitle.append( s );
                }
            }

            title = newTitle.toString();

	    // Cut the title if it is too long
            if ( title.length() - tagLengthCount >= TITLE_MAX_LENGTH ) {
                title = title.substring(0, TITLE_MAX_LENGTH + tagLengthCount) + "<b>...</b>";
            }

            if ( url.length() > 80) {
                String newUrl = url.substring(0, 40) + "..."+ url.substring((url.length()-37),url.length());
                url = newUrl;
            }

            // Build the summary
            StringBuffer sum = new StringBuffer();
            Fragment[] fragments = summaries[ positionIndex[i] ].getFragments();
            for (int j=0; j<fragments.length; j++) {
              if (fragments[j].isHighlight()) {
                sum.append("<em>")
                   .append(Entities.encode(fragments[j].getText()))
                   .append("</em>");
              } else if (fragments[j].isEllipsis()) {
                sum.append("<span class=\"ellipsis\"> ... </span>");
              } else {
                sum.append(Entities.encode(fragments[j].getText()));
              }
            }

            String summary = sum.toString();

	    // do not show unless we have something
            boolean showMore = false;

            // Content-Type
            String primaryType = detail.getValue("primaryType");
            String subType = detail.getValue("subType");

            String contentType = subType;
            if (contentType == null)
              contentType = primaryType;
            if (contentType != null) {
              contentType = "[<span class=\"contentType\">" + contentType + "</span>]";
              showMore = true;
            } else {
              contentType = "";
            }

            // Last-Modified
            String lastModified = detail.getValue("lastModified");
            if (lastModified != null) {
              Calendar cal = new GregorianCalendar();
              cal.setTimeInMillis(new Long(lastModified).longValue());
              lastModified = cal.get(Calendar.YEAR)
                          + "." + (1+cal.get(Calendar.MONTH)) // it is 0-based
                          + "." + cal.get(Calendar.DAY_OF_MONTH);
              showMore = true;
            } else {
              lastModified = "";
            }

	    %>
                <% if (hitsPerDup > 0 && current_host.equals( previous_host )) {%>
			<%-- TODO: check if "grouped" style exist --%>
                        <li class="grouped">
                <% } else { %>
                        <li>
                <% previous_host = current_host; } %>

                <% if (showMore) {
                        if (!"text".equalsIgnoreCase(primaryType)) {
                                if ( contentType.lastIndexOf('-') != -1) {
                                        contentType = "[" + contentType.substring( contentType.lastIndexOf('-') + 1);
                                }
                                contentType = contentType.toUpperCase(); %>
                                <span class="mime"><%=contentType%></span>
                <%} }%>


            <!-- <h2><a href="<c:url value='${target}'><c:param name='pos' value='${position}'/><c:param name='l' value='${language}'/><c:param name='sid' value='${pageContext.session.id}'/></c:url>"><%=title%></a></h2> -->
            <!-- Changed to return in wayback query format -->
            <div class="urlBlock">
              <h2>
                <a onclick="ga('send', 'event', 'Full-text search', 'Click on version', '<c:url value='${target}'></c:url>');" href="<c:url value='${target}'></c:url>"><%=title%></a>
              </h2>
              <div class="url"><a class="url" onclick="ga('send', 'event', 'Full-text search', 'Click on version', '<c:url value='${target}'></c:url>');" href="<c:url value='${target}'></c:url>"><%= url %></a></div>
            </div>  
		<%-- TODO: don't use "archiveDisplayDate" delegate to FMT --%>
            <% showSummary=true; //to show always summaries %>
            <% if (!"".equals(summary) && showSummary) { %>
            <span class="resumo"><%=summary%></span><br />
            <span class="date"><fmt:message key='search.result.date'><fmt:param value='<%= archiveDate%>'/></fmt:message></span>
            <!--<div><a class="outras-datas" href="<%=allVersions%>"><fmt:message key='search.result.history'/></a></div>-->
            <div><a onclick="ga('send', 'event', 'Full-text search', 'List Versions', '/wayback/*/<%= url %>');" class="outras-datas" href="/wayback/*/<%= url %>"><fmt:message key='search.allVersions'/></a></div>            
            <% } %>
<%--
            -
            <a class="history" href="<%=allVersions%>"><fmt:message key="otherVersions"/></a>
--%>

            </li>
        <% } %>
</ul>
</div> <!-- FIM #resultados-lista  --> 
