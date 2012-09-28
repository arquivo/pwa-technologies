package pt.arquivo.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import pt.arquivo.logs.arquivo.SqlOperations;


/**
 * Servlet implementation to classify sessions
 * @author Miguel Costa
 */
public class LogsMinerServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
		
	private static Logger logger=null; 
	private static SqlOperations sqlOp=null;
	private static int totalSessions=0;
	   
    /**
     * @see HttpServlet#HttpServlet()
     */
    public LogsMinerServlet() {
        super();
    }
    
    /**
     * 
     */
    public void init(ServletConfig config) throws ServletException {
        super.init(config);        
        logger = Logger.getLogger(LogsMinerServlet.class.getName());
        
        sqlOp=new SqlOperations();
        try {
			sqlOp.connect("//xxxxx","xxxxx","xxxxx");  // hard coded - TODO: parameterize this
			totalSessions=sqlOp.selectTotalSessions();
		} 
        catch (ClassNotFoundException e) {
			logger.error(e.getMessage());
			throw new ServletException(e.getMessage());
		} 
        catch (SQLException e) {
			logger.error(e.getMessage());
			throw new ServletException(e.getMessage());
		}	                
    }

    /**
     * 
     */
    public void destroy() {       	    	
    	try {
			sqlOp.close();
		} catch (SQLException e) {
			logger.error(e.getMessage());
		}
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {							
		int isession=0;		
		String session=request.getParameter("session");
		String sessionKey=request.getParameter("sessionKey");
		String stype=request.getParameter("type");
		String stopic=request.getParameter("topic");
		String scomments=request.getParameter("comments");
		String sisurl=request.getParameter("isurl");
		String sisbetweendates=request.getParameter("isbetweendates");	

		if (session!=null) {
			isession=Integer.parseInt(session);
			// check limits
			if (isession<0) {
				isession=0;
			}
			if (isession>totalSessions-1) {
				isession=totalSessions-1;
			}
		}	
		try {
			if (stype!=null /*&& !stype.equals("null")*/) {
				int itype=Integer.parseInt(stype);
				sqlOp.updateSessionType(itype,sessionKey);
			}
			if (stopic!=null /*&& !stopic.equals("null")*/) {
				int itopic=Integer.parseInt(stopic);
				sqlOp.updateSessionTopic(itopic,sessionKey);
			}
			if (scomments!=null /*&& !scomments.equals("null")*/) {			
				sqlOp.updateSessionComments(scomments,sessionKey);
			}
			if (sisurl!=null /*&& !scomments.equals("null")*/) {	
				int isurl=Integer.parseInt(sisurl);
				sqlOp.updateSessionIsUrl(isurl,sessionKey);
			}
			if (sisbetweendates!=null /*&& !scomments.equals("null")*/) {	
				int isbetweendates=Integer.parseInt(sisbetweendates);
				sqlOp.updateSessionIsBetweenDates(isbetweendates,sessionKey);
			}
		}  
		catch (SQLException e) {
			throw new ServletException(e.getMessage());
		}		
				
		response.setContentType("text/html; charset=UTF-8");
		response.setCharacterEncoding("UTF-8");
		
		PrintWriter out=response.getWriter();
		out.println("<HTML>");
		out.println("<HEAD>");
		out.println("<META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html\">");
		out.println("</HEAD>");
		out.println("<BODY>");	  
		out.println("TOTAL SESSIONS: "+totalSessions+" <BR>");
							
		String type=null;
		String topic=null;
		String comments=null;
		String isurl=null;
		String isbetweendates=null;
		try {
			int totalSessionsTypeClassified=sqlOp.selectTotalSessionsTypeClassified();
			int totalSessionsTopicClassified=sqlOp.selectTotalSessionsTopicClassified();
			out.println("TOTAL SESSIONS WITH TYPE CLASSIFIED: "+totalSessionsTypeClassified+" <BR>");
			out.println("TOTAL SESSIONS WITH TOPIC CLASSIFIED: "+totalSessionsTopicClassified+" <BR><BR>");
			
			ResultSet results=sqlOp.selectSession(isession);
			if (!results.next()) {
				throw new ServletException("Wrong session: "+isession);
			}
			sessionKey=results.getString(1);
			out.println("SESSION: "+isession+" <BR>");
			out.println("SID: "+results.getString(2)+" <BR>");
			out.println("IP: "+results.getString(3)+" <BR>");
			comments=results.getString(4);
			type=results.getString(5);
			topic=results.getString(6);
			isurl=results.getString(7);
			isbetweendates=results.getString(8);	
			results.close();
		} 
		catch (SQLException e) {
			throw new ServletException(e.getMessage());
		}			
				
		out.println("<BR>");
		out.println("<table border=\"1\">");
		out.println("<tr>");
		out.println("<td> Date: </td>");
		out.println("<td> Action: </td>");
		out.println("<td> Description: </td>");
		out.println("</tr>");
		try {
			ResultSet results=sqlOp.selectSessionEntries(sessionKey);
			while (results.next()) {
				out.println("<tr>");
				out.println("<td>"+results.getString(1)+"</td>");
				out.println("<td>"+results.getString(2)+"</td>");
				out.println("<td>"+results.getString(3)+"</td>");
				out.println("</tr>");
			}
			results.close();
		} 
		catch (SQLException e) {
			throw new ServletException(e.getMessage());
		}		
		out.println("</table>");			
				
		out.println("<BR>");
		out.println("<FORM action=\"LogsMiner\" method=\"get\">");
				
		out.println("TYPE: <select name=\"type\">");		
		try {
			ResultSet results=sqlOp.selectSessionTypes();			
			while (results.next()) {
				if (type!=null && type.equals(""+results.getInt(1))) {
					out.println("<option selected value=\""+results.getInt(1)+"\">"+results.getString(2)+"</option>");	
				}				
				else {
					out.println("<option value=\""+results.getInt(1)+"\">"+results.getString(2)+"</option>");
				}			
			}
			results.close();
		} 
		catch (SQLException e) {
			throw new ServletException(e.getMessage());
		}				
		out.println("</select>");
		out.println("<BR>");
		
		out.println("IS ONLY URL: <select name=\"isurl\">");				
		//out.println("<option "+((isurl==null || isurl.equals("")) ? "selected" : "")+" value=\""+"\">"+""+"</option>");
		out.println("<option "+((isurl!=null && isurl.equals("0")) ? "selected" : "")+" value=\"0"+"\">"+"No"+"</option>");
		out.println("<option "+((isurl!=null && isurl.equals("1")) ? "selected" : "")+" value=\"1"+"\">"+"Yes"+"</option>");			
		out.println("</select>");
		out.println("<BR>");
		
		out.println("IS BETWEEN DATES: <select name=\"isbetweendates\">");		
		//out.println("<option "+((isbetweendates==null || isbetweendates.equals("")) ? "selected" : "")+" value=\""+"\">"+""+"</option>");
		out.println("<option "+((isbetweendates!=null && isbetweendates.equals("0")) ? "selected" : "")+" value=\"0"+"\">"+"No"+"</option>");
		out.println("<option "+((isbetweendates!=null && isbetweendates.equals("1")) ? "selected" : "")+" value=\"1"+"\">"+"Yes"+"</option>");			
		out.println("</select>");
		out.println("<BR>");		
		
		out.println("TOPIC: <select name=\"topic\">");		
		try {
			ResultSet results=sqlOp.selectSessionTopics();
			int i=1;
			while (results.next()) {
				if (topic!=null && topic.equals(""+results.getInt(1))) {
					out.println("<option value=\""+results.getInt(1)+"\" selected>"+results.getString(2)+"</option>");	
				}				
				else {
					out.println("<option value=\""+results.getInt(1)+"\">"+results.getString(2)+"</option>");
				}
			}
			results.close();
		} 
		catch (SQLException e) {
			throw new ServletException(e.getMessage());
		}				
		out.println("</select>");
		out.println("<BR>");
							
		out.println("COMMENTS: "+"<input type=\"text\" maxlength=\"500\" size=\"200\" name=\"comments\" value=\""+((comments!=null) ? comments : "")+"\"/>");
		out.println("<BR>");
		out.println("<input type=\"hidden\" name=\"sessionKey\" value=\""+sessionKey+"\">");
		out.println("<input type=\"hidden\" name=\"session\" value=\""+isession+"\">");
		out.println("<input type=\"submit\" value=\"Save\">");
		out.println("</FORM>");
		out.println("<BR>");
		
		out.println("<a href=\"LogsMiner?session="+(isession-1)+"\">previous session</a>");
		out.println("<a href=\"LogsMiner?session="+(isession+1)+"\">next session</a>");
		
		out.println("</BODY>");
		out.println("</HTML>");	  
	    out.close();
		
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
