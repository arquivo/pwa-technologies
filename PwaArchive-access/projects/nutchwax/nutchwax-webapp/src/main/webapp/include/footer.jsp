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
                    <li><fmt:message key="footer.section.social.mailinglist"/></li>
                    <li><fmt:message key="footer.section.social.news"/></li>
                    <li><fmt:message key="footer.section.social.twitter"/></li>
                    <li><a href="http://www.facebook.com/pages/Arquivo-da-Web-Portuguesa/113463705350330"><fmt:message key="footer.section.social.facebook"/></a></li>
                    <li><a href="http://arquivo.pt/rss"><fmt:message key='footer.section.social.rss'/></a></li>
                    <li><fmt:message key="footer.section.social.video"/></li>
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
            <a href="http://www.fccn.pt/" title="<fmt:message key='footer.sponsor.fccn'/>" >
                <img src="<c:out value="${pageContext.servletContext.contextPath}" />/img/logo-fccn.png" alt="<fmt:message key='footer.sponsor.fccn.alt'/>" width="222" height="40" id="fccn" />
            </a>&nbsp;
            <a href="http://www.portugal.gov.pt/pt/ministerios/mctes.aspx" title="<fmt:message key='footer.sponsor.mec'/>"> <img alt="<fmt:message key='footer.sponsor.gov.alt'/>" src="<c:out value="${pageContext.servletContext.contextPath}" />/img/10-Digital_PT_4C_H_FC_MCTES_opt-e1491300980870.png" width="220" height="40"/>
            </a>
            </map>
        </div>
    </div>
</div>
