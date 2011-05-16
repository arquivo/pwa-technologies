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

import java.io.File;

/**
 * 
 * @author John Erik Halse
 *  
 */
public class AID {

    private final String aid;

    private final String filename;

    private final long offset;

    /**
     * @throws ArcRetrieverException
     *  
     */
    public AID(String aid) throws ArcRetrieverException {
        try {
            this.aid = aid;
            int filenameOffset = aid.indexOf('/');
            this.filename = aid.substring(filenameOffset);
            this.offset = Long.parseLong(aid.substring(0, filenameOffset));
        } catch (StringIndexOutOfBoundsException e) {
            throw new ArcRetrieverException(ArcRetrieverException.
                ERROR_UNABLE_TO_PARSE_ARCHIVE_IDENTIFIER);
        } catch (Exception e) {
            throw new ArcRetrieverException(ArcRetrieverException.
                ERROR_UNABLE_TO_PARSE_ARCHIVE_IDENTIFIER, e);
        }
    }

    /**
     * @param Full path to the directory of arcs.
     * @return Full path to arc file.
     */
    public File getFile(final File arcdir) {
        return new File(arcdir, getFilename());
    }

    /**
     * @return Returns the filename (If no suffix, appends arc.gz).
     */
    public String getFilename() {
        return filename.endsWith(".arc.gz")? this.filename:
            filename.endsWith(".arc")? this.filename:
                this.filename + ".arc.gz";
    }

    /**
     * @return Returns the offset.
     */
    public long getOffset() {
        return offset;
    }

    public String toString() {
        return aid;
    }
}
