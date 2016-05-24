package org.archive.wayback.archivalurl.requestparser;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.archive.wayback.WaybackConstants;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.requestparser.PathRequestParser;


/**
 * RequestParser implementation that extracts request info from a Replay given the document id.
 * Necessary to resolve BUG 0000155 (wayback search performance)
 * 
 * @author Miguel Costa
 * @version $Date$, $Revision$
 */
public class ReplayIdRequestParser extends PathRequestParser {
	private static final Logger LOGGER = Logger.getLogger(ReplayIdRequestParser.class.getName());
	
	/**
	 * Regex which parses Archival URL replay requests into timestamp + url
	 */
	//private final Pattern WB_REQUEST_REGEX = Pattern.compile("^id(\\d+)index(\\d+)$");
	private final Pattern WB_REQUEST_REGEX = Pattern.compile("^id(\\d+)index(\\d+)(\\?(.*))*$");
	

	public WaybackRequest parse(String requestPath) {
		WaybackRequest wbRequest = null;
		Matcher matcher = WB_REQUEST_REGEX.matcher(requestPath);	
		if (matcher != null && matcher.matches()) {
			wbRequest = new WaybackRequest();
			String docId = matcher.group(1);			
			wbRequest.put(WaybackConstants.REQUEST_DOC_ID,docId);
			String indexId = matcher.group(2);			
			wbRequest.put(WaybackConstants.REQUEST_INDEX_ID,indexId);
			
			wbRequest.put(WaybackConstants.REQUEST_TYPE, WaybackConstants.REQUEST_REPLAY_QUERY);
		}
		return wbRequest;
	}

}
