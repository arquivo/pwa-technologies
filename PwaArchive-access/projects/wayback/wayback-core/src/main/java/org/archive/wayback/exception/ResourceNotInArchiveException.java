package org.archive.wayback.exception;

import javax.servlet.http.HttpServletResponse;

/**
 * Exception class for queries which result in no index matches
 *
 * @author brad
 * @version $Date: 2007-03-02 00:40:42 +0000 (Fri, 02 Mar 2007) $, $Revision: 1538 $
 */
public class ResourceNotInArchiveException extends WaybackException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected static final String ID = "resourceNotInArchive";

	/**
	 * Constructor
	 * 
	 * @param message
	 */
	public ResourceNotInArchiveException(String message) {
		super(message,"Not in Archive");
		id = ID;
	}
	/**
	 * Constructor with message and details
	 * 
	 * @param message
	 * @param details
	 */
	public ResourceNotInArchiveException(String message,String details) {
		super(message,"Not in Archive",details);
		id = ID;
	}
	/**
	 * @return the HTTP status code appropriate to this exception class.
	 */
	public int getStatus() {
		return HttpServletResponse.SC_NOT_FOUND;
	}
}
