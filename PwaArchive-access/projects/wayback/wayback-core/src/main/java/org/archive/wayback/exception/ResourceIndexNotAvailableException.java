package org.archive.wayback.exception;

import javax.servlet.http.HttpServletResponse;


/**
 * Exception class for queries which fail because the ResourceIndex is
 * presently inaccessible
 *
 * @author brad
 * @version $Date: 2007-03-02 00:40:42 +0000 (Fri, 02 Mar 2007) $, $Revision: 1538 $
 */
public class ResourceIndexNotAvailableException extends WaybackException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected static final String ID = "resourceIndexNotAvailable";

	/**
	 * Constructor
	 * 
	 * @param message
	 */
	public ResourceIndexNotAvailableException(String message) {
		super(message,"Index not available");
		id = ID;
	}
	/**
	 * Constructor with message and details
	 * 
	 * @param message
	 * @param details
	 */
	public ResourceIndexNotAvailableException(String message, String details) {
		super(message,"Index not available",details);
		id = ID;
	}
	/**
	 * @return the HTTP status code appropriate to this exception class.
	 */
	public int getStatus() {
		return HttpServletResponse.SC_SERVICE_UNAVAILABLE;
	}
}
