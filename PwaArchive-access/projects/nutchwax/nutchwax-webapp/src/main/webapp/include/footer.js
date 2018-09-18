<div id="footer">
    <div id="links">
        <%-- TODO: remove uneeded div#wrap-footer --%>
        <div class="wrap-footer">
   		<div class="links-content">
                <p class="links-title"><fmt:message key="footer.section.about"/></p>
                <ul>
                    <li><fmt:message key="footer.section.about.objectives"/></li>
                    <li><fmt:message key="footer.section.about.publications"/></li>
                    <li><fmt:message key="footer.section.about.pages-examples"/></li>
                    <li><fmt:message key="footer.section.about.press"/></li>
                    <li><fmt:message key="footer.section.about.services"/></li>
                    <li><fmt:message key='footer.section.about.terms-conditions'/></li>
                </ul>
            </div>
 		<div class="links-content">
                <p class="links-title"><fmt:message key="footer.section.social"/></p>
                <ul>
                    <li><fmt:message key="footer.section.social.news"/></li>
                    
                    <li><fmt:message key="footer.section.social.twitter"/></li>
                    <li><a href="//www.facebook.com/pages/Arquivo-da-Web-Portuguesa/113463705350330"><fmt:message key="footer.section.social.facebook"/></a></li>
                    <li><a href="//arquivo.pt/rss"><fmt:message key='footer.section.social.rss'/></a></li>
                </ul>
            </div>

    		<div class="links-content">
                <p class="links-title"><fmt:message key="footer.section.collaboration"/></p>
                <ul>
                    <li><fmt:message key="footer.section.collaboration.suggest-website"/></li>
                    <li><fmt:message key="footer.section.collaboration.divulgation"/></li>
                    <li><fmt:message key="footer.section.collaboration.recommendations"/></li>
                    <li><fmt:message key="footer.section.collaboration.giving"/></li>
                    <li><fmt:message key="footer.section.collaboration.projects"/></li>
                </ul>
            </div>

            <div class="links-content">
                <p class="links-title"><fmt:message key="footer.section.help"/></p>
                <ul>
                    <li><fmt:message key='footer.section.help.search'/></li>
                    <li><fmt:message key='footer.section.help.advanced-search'/></li>
                    <li><fmt:message key='footer.section.help.access'/></li>
                    <li><fmt:message key='footer.section.help.crawl'/></li>
                    <li><fmt:message key='footer.section.help.faq'/></li>
                    <li><fmt:message key='footer.section.help.contact'/></li>
                </ul>
            </div>
        </div>
    </div>
    <div id="empresa">
        <div class="wrap-footer">
            <a href="//www.fccn.pt/" title="<fmt:message key='footer.sponsor.fccn'/>" >
                <img src="<c:out value="${pageContext.servletContext.contextPath}" />/img/logo-fccn.png" alt="<fmt:message key='footer.sponsor.fccn.alt'/>" width="183" height="46" id="fccn" />
            </a>&nbsp;
            <img usemap="#logomap" alt="<fmt:message key='footer.sponsor.gov.alt'/>" src="<c:out value="${pageContext.servletContext.contextPath}" />/img/mec-web.png" width="243" height="40"/>
            <map id="logomap" name="logomap">
                <area title="<fmt:message key='footer.sponsor.gov'/>" href="//www.portugal.gov.pt/" coords="0,0,138,40" shape="rect"/>
                <area title="<fmt:message key='footer.sponsor.mec'/>" href="//www.portugal.gov.pt/pt/os-ministerios/ministerio-da-educacao-e-ciencia.aspx" coords="141,0,243,40" shape="rect"/>
            </map>
        </div>
    </div>
</div>
