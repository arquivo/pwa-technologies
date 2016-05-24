package org.archive.nutch.trec;

import junit.framework.TestCase;
import java.util.Date;
import java.lang.Long;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;


public class TRECParserTest extends TestCase {

	/*
	 * Test method for 'org.archive.nutch.trec.TRECParser.parseDate(String)'
	 */
	public final void testParseDate() throws ParseException {
		Map<String, String> dateMap = new HashMap<String, String>();
		//Test these patterns
	    //PATTERN_RFC1123 = "EEE, dd MMM yyyy HH:mm:ss zzz";
	    //PATTERN_RFC1036 = "EEEE, dd-MMM-yy HH:mm:ss zzz";
	    //PATTERN_ASCTIME = "EEE MMM d HH:mm:ss yyyy";
		//First some different formats
	    dateMap.put("Wed, 30 Jan 2002 17:00:23 GMT","1012410023000" );
	    dateMap.put("WED, 30 JAN 2002 17:00:23 GMT","1012410023000" );
	    dateMap.put("Wed, 30-Jan-2002 17:00:23 GMT","1012410023000" );
	    dateMap.put("Wed Jan 30 17:00:23 2002","1012410023000" );
	    //Epoch rollover
	    dateMap.put("Tue, 19 Jan 2038 03:14:08 UTC","2147483648000" );
	    //Epoch begins
	    dateMap.put("Thu, 1 Jan 1970 00:00:00 GMT","0");
	    //Date Begins
	    dateMap.put("Sun, 1 Jan 0000 00:00:00 GMT","-62167392000000");
	    for (Iterator it=dateMap.entrySet().iterator(); it.hasNext(); ) {
	    	Map.Entry me = (Map.Entry)it.next();
	    	Date dateObj = TRECParser.parseDate((String)me.getKey());
	    	if( dateObj.getTime() != Long.parseLong( (String)me.getValue() ) ) {
	    		throw new ParseException("BadParse in parseDate: " + dateObj.getTime()
	    				+ ", should be: " + (String)me.getValue());
	    	}
	    }
	}

}
