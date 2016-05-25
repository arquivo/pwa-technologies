package pt.arquivo.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import org.apache.log4j.Logger;

import pt.arquivo.spellchecker.SpellChecker;


/**
 * Servlet implementation for SpellChecker
 * @author Miguel Costa
 */
public class SpellCheckerServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
		
	private static final String FIELD="content";
	private static int minFreq=0;
	private static int timesFreq=0;	 
	private static String dictPath=null;
	
	private static IndexReader reader=null;
	private static Logger logger=null; 
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SpellCheckerServlet() {
        super();
    }
    
    /**
     * 
     */
    public void init(ServletConfig config) throws ServletException {
        super.init(config);        
        logger = Logger.getLogger(SpellCheckerServlet.class.getName());
        
        String indexDir = config.getInitParameter("indexDir");
        minFreq = Integer.parseInt(config.getInitParameter("minFreq"));
        timesFreq = Integer.parseInt(config.getInitParameter("timesFreq"));        
        dictPath = config.getInitParameter("dictPath");
        logger.info("Starting spellchecker with parameters( indexDir:"+indexDir+" minFreq:"+minFreq+" timesFreq:"+timesFreq+" dictPath:"+dictPath+" )");        
        
		try {
			Directory idx = FSDirectory.getDirectory(indexDir, false);			
		  	reader=IndexReader.open(idx);		  
			logger.info("Spellchecker initialized.");			
		} 
		catch (IOException e) {
			logger.error("Problems initializing spellchecker: "+e.getMessage());
			throw new ServletException(e);
		}
								 	    	  					      
    }

    /**
     * 
     */
    public void destroy() {       	    	
    	try {
    		reader.close();
    	} 
		catch (IOException e) {					
		}		
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {			
		boolean morePopular=true;
		
		String query=request.getParameter("query");
		String lang=request.getParameter("lang");
		String terms[]=null;
		String suggestions[]=null;
		if (query!=null && lang!=null) {
			terms=query.split("\\s");
			suggestions=new String[terms.length];
			for (int i=0;i<terms.length;i++) {
				String allSuggestions[]=null;				
				try {
					allSuggestions=SpellChecker.suggestSimilarAspell(terms[i],lang,1,reader,FIELD,minFreq,timesFreq,dictPath);
				}			
				catch (InterruptedException e) {			
					throw new IOException(e);
				}
				
				if (allSuggestions.length>0) {
					suggestions[i]=allSuggestions[0];
				}
				else {
					suggestions[i]=null;
				}
			}
		}
		
		response.setContentType("text/html");
		
		PrintWriter out=response.getWriter();
		out.println("<HTML>");
		out.println("<HEAD>");
		out.println("<META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html\">");
		out.println("</HEAD>");
		out.println("<BODY>");	    
		out.println("QUERY: <FORM action=\"SpellChecker\" method=\"get\">");
		out.println("<input type=\"text\" name=\"query\" />");
		out.println("<select name=\"lang\">");
		out.println("<option value=\"pt_PT\" selected>pt_PT</option>");
		out.println("<option value=\"en_US\">en_US</option>");
		out.println("</select>");
		out.println("</FORM>");
		
		for (int i=0;terms!=null && i<terms.length;i++) {
			if (suggestions[i]==null || suggestions[i].equals(terms[i])) {
				out.print("IGUAL: "+terms[i]+"<BR>");		
			}
			else {
				out.print("SUGESTAO: "+suggestions[i]+"<BR>");
			}
		}
					
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
