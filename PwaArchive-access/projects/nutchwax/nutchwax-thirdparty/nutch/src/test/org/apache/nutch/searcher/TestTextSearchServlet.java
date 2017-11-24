package test.org.apache.nutch.searcher;

import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.*;  // for non-hamcrest core matchers
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.nutch.searcher.TextSearchServlet;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class TestTextSearchServlet {
	
	private TextSearchServlet servlet;
	private HttpServletRequest request;
	private HttpServletResponse response;
	private StringWriter response_writer;
	private Map< String, String > parameters;
	
	
	@Before
	public void setUp() throws IOException {
		parameters = new HashMap< String , String >( );
		servlet = new TextSearchServlet( );
		request = mock( HttpServletRequest.class );
		response = mock( HttpServletResponse.class );
		
		response_writer = new StringWriter( );
		when( request.getProtocol( ) ).thenReturn( "HTTP/1.1" );
		when( request.getParameter( anyString( ) ) ).thenAnswer( new Answer< String >( ) {
			public String answer(InvocationOnMock invocation) throws Throwable{
				return parameters.get((String) invocation.getArguments()[0]);
			}
		});
		when( response.getWriter( ) ).thenReturn( new PrintWriter( response_writer ) );
	}
	
	
	@Test
	public void testGet1( ) throws Exception {
		System.out.println( "[TestTestSearchServlet] [testGest1]" );
		parameters.put( "q", "Antonio" );
		servlet.doGet( request, response );
		System.out.println( "response = " + response_writer.toString( ) );
		
		assertThat( response_writer.toString( ),
			// a non-hamcrest core matcher
			containsString( "Arquivo.pt - the Portuguese web-archive" ) );
	}
  
  
  
}
