/*
 *  This file is part of The NWA Toolset.
 *
 *  Copyright (C) 2001-2002 Royal Library in Stockholm,
 *                          Royal Library in Copenhagen,
 *                          Helsinki University Library of Finland, 
 *                          National Library of Norway,
 *                          National and University Library of Iceland.
 *
 *  The NWA Toolset is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  The NWA Toolset is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with The NWA Toolset; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package no.nb.nwa.retriever;


/**
 * 
 * @author John Erik Halse
 *
 */
public class ArcRetrieverException extends Exception {
    public final static int ERROR_REQTYPE_MISSING = 1;
    public final static int ERROR_ARCHIVE_IDENTIFIER_MISSING = 2;
    public final static int ERROR_UNSUPPORTED_REQTYPE = 3;
    public final static int ERROR_UNABLE_TO_PARSE_ARCHIVE_IDENTIFIER = 4;
    public final static int ERROR_DOCUMENT_ROOT_NOT_SET = 5;
    public final static int ERROR_OBJECT_NOT_ACCESSIBLE = 6;
    public final static int ERROR_BAD_FUNCTION_ARGUMENT = 7;

    private final static String[] msg = {
        "",
        "Reqtype missing",
        "Archive Identifier missing",
        "Unsupported reqtype",
        "Unable to parse Archive Identifier",
        "Document Root not set",
        "Object not accessible",
        "Bad function argument"
    };
    
    private final int errorCode;
    
    /**
     * 
     */
    private ArcRetrieverException() {
        this.errorCode = 0;
    }

    /**
     * @param type
     */
    public ArcRetrieverException(int type) {
        super(msg[type]);
        this.errorCode = type;
    }

    /**
     * @param type
     * @param cause
     */
    public ArcRetrieverException(int type, Throwable cause) {
        super(msg[type], cause);
        this.errorCode = type;
    }
    
    public int getErrorCode() {
        return errorCode;
    }
}
