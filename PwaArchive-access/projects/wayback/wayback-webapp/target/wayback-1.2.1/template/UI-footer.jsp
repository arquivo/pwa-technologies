<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8"%>
<%@ page import="org.archive.wayback.core.UIResults" %>
<%@ page import="org.archive.wayback.util.StringFormatter" %>
<%
UIResults results = UIResults.getFromRequest(request);
StringFormatter fmt = results.getFormatter();
String contextRoot = results.getContextPrefix();
String serverRoot = results.getServerPrefix();
%>
<!-- FOOTER -->
		<div id="footer" align="center">
			<p>
				<a href="<%= contextRoot %>">
					<%= fmt.format("UIGlobal.homeLink") %>
				</a> |
				<a href="<%= contextRoot %>help.jsp">
					<%= fmt.format("UIGlobal.helpLink") %>
				</a>
			</p>
		</div>
		<div id="sponsor" align="center">
			<a href="http://www.fccn.pt"><img src="<%= contextRoot %>images/logo_fccn_small_.jpg" alt="FCCN - Fundação para a Computação Científica Nacional" title="FCCN - Fundação para a Computação Científica Nacional" border="0" /></a>
			<a href="http://www.posc.mctes.pt"><img src="<%= contextRoot %>images/logo_posc_small_.jpg" alt="POSC - Programa Operacional Sociedade do Conhecimento" title="POSC - Programa Operacional Sociedade do Conhecimento" border="0" /></a>
			<a href="http://www.umic.pt"><img src="<%= contextRoot %>images/logo_umic_small_.jpg" alt="UMIC - Agência para a Sociedade do Conhecimento" title="UMIC - Agência para a Sociedade do Conhecimento" border="0" /></a>
			<a href="http://ec.europa.eu/regional_policy/funds/feder/index_pt.htm"><img src="<%= contextRoot %>images/ue_feder_.png" alt="FEDER - Fundo Europeu de Desenvolvimento Regional" title="FEDER" border="0" /></a>
		</div>
		<!-- /FOOTER -->
	</body>
</html>


