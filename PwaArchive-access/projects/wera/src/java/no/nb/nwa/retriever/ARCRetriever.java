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
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import javax.servlet.ServletException;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HeaderGroup;
import org.archive.io.arc.ARCReader;
import org.archive.io.arc.ARCReaderFactory;
import org.archive.io.arc.ARCRecord;
import org.archive.io.arc.ARCRecordMetaData;
import org.archive.util.ArchiveUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * 
 * @author John Erik Halse
 *  
 */
public class ARCRetriever extends HttpServlet {
    final static Logger LOGGER = Logger.getLogger(ARCRetriever.class.getName());

    final static Pattern charsetPattern = Pattern
            .compile("^.*charset=([^\\s]+).*$");
    
    private File arcdir = null;
    private Pattern CR = Pattern.compile("\r");

    /**
     * Constructor.
     */
    public ARCRetriever() {
        super();
    }

    public void init(final ServletConfig config) throws ServletException {
        String tmp = config.getInitParameter("arcdir");
        if (tmp != null) {
            this.arcdir = new File(tmp);
        } else {
            throw new ServletException("'arcdir' init param is empty. " +
                "Have you set it in web.xml to point at directory of arcs?");
            
        }
        if (!this.arcdir.exists() || !this.arcdir.canRead()) {
            throw new ServletException(this.arcdir.getAbsolutePath() +
                " does not exist or is not readable.  Have you set " +
                "'arcdir' in the web.xml file to point at directory of arcs?");
        }
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("Passed arc directory is " + this.arcdir);
        }
    }

    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
    throws ServletException, IOException {
        try {
            long now = System.currentTimeMillis();
            String reqtype = request.getParameter("reqtype");
            String aid = request.getParameter("aid");
            if (reqtype == null) {
                throw new ArcRetrieverException(ArcRetrieverException.
                    ERROR_REQTYPE_MISSING);
            } else {
                reqtype = reqtype.intern();
            }

            if (reqtype == "getfile") {
                if (aid == null) {
                    throw new ArcRetrieverException(ArcRetrieverException.
                        ERROR_ARCHIVE_IDENTIFIER_MISSING);
                } else {
                    getDocument(response, new AID(aid));
                }
            } else if (reqtype == "getmeta") {
                if (aid == null) {
                    throw new ArcRetrieverException(ArcRetrieverException.
                        ERROR_ARCHIVE_IDENTIFIER_MISSING);
                } else {
                    getMeta(response, new AID(aid));
                }
            } else if (reqtype == "getfilestatus") {
                if (aid == null) {
                    throw new ArcRetrieverException(ArcRetrieverException.
                        ERROR_ARCHIVE_IDENTIFIER_MISSING);
                } else {
                    getFileStatus(response, new AID(aid));
                }
            } else if (reqtype == "getarchiveinfo") {
                getArchiveInfo(response);
            } else {
                throw new ArcRetrieverException(ArcRetrieverException.
                    ERROR_UNSUPPORTED_REQTYPE);
            }
        } catch (Throwable e) {
            handleException(response, e);
        }
    }

    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
    throws ServletException, IOException {
        this.doGet(request, response);
    }

    public void getFileStatus(HttpServletResponse response, AID aid)
            throws ArcRetrieverException {
        response.setContentType("text/xml; charset=UTF-8");

        String status = "";
        String status_long = "";
        File file = aid.getFile(this.arcdir);
        ARCReader arc = null;
        try {
            arc = ARCReaderFactory.get(file);
            ARCRecord rec = null;
            try {
                rec = arc.get(aid.getOffset());
                status = "online";
                status_long = "Document is available.";
            } catch (IOException e) {
                status = "non-existent";
                status_long = "No document at offset: " + aid.getOffset();
            } finally {
                if (rec != null) rec.close();
            }
        } catch (IOException e) {
            status = "non-existent";
            //            status_long = "File '" + file.getAbsolutePath()
            status_long = "File '" + aid.getFilename()
                    + "' doesn't exist or is not an ARC file.";
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (arc != null) arc.close();
            } catch (IOException e) {
            }
        }

        try {
            Document dom = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder().newDocument();
            Node msg = dom.appendChild(dom.createElement("retrievermessage"));
            Node head = msg.appendChild(dom.createElement("head"));
            addTextElement(head, "reqtype", "getfilestatus");
            addTextElement(head, "aid", aid.toString());
            Node body = msg.appendChild(dom.createElement("body"));
            addTextElement(body, "filestatus", status);
            addTextElement(body, "filestatus_long", status_long);

            Transformer transformer = TransformerFactory.newInstance()
                    .newTransformer();
            transformer.setOutputProperty("indent", "yes");
            Result res = new StreamResult(response.getOutputStream());
            Source source = new DOMSource(dom);
            transformer.transform(source, res);
        } catch (ParserConfigurationException e) {
            throw new ArcRetrieverException(
                    ArcRetrieverException.ERROR_BAD_FUNCTION_ARGUMENT, e);
        } catch (TransformerConfigurationException e) {
            throw new ArcRetrieverException(
                    ArcRetrieverException.ERROR_BAD_FUNCTION_ARGUMENT, e);
        } catch (IOException e) {
            throw new ArcRetrieverException(
                    ArcRetrieverException.ERROR_BAD_FUNCTION_ARGUMENT, e);
        } catch (TransformerException e) {
            throw new ArcRetrieverException(
                    ArcRetrieverException.ERROR_BAD_FUNCTION_ARGUMENT, e);
        }
    }

    public void getArchiveInfo(HttpServletResponse response)
            throws ArcRetrieverException {
        response.setContentType("text/xml; charset=UTF-8");

        try {
            Document dom = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder().newDocument();
            Node msg = dom.appendChild(dom.createElement("retrievermessage"));
            Node head = msg.appendChild(dom.createElement("head"));
            addTextElement(head, "reqtype", "getarchiveinfo");
            Node body = msg.appendChild(dom.createElement("body"));
            addTextElement(body, "info", "ArcRetriever");

            Transformer transformer = TransformerFactory.newInstance()
                    .newTransformer();
            transformer.setOutputProperty("indent", "yes");
            Result res = new StreamResult(response.getOutputStream());
            Source source = new DOMSource(dom);
            transformer.transform(source, res);
        } catch (ParserConfigurationException e) {
            throw new ArcRetrieverException(
                    ArcRetrieverException.ERROR_BAD_FUNCTION_ARGUMENT, e);
        } catch (TransformerConfigurationException e) {
            throw new ArcRetrieverException(
                    ArcRetrieverException.ERROR_BAD_FUNCTION_ARGUMENT, e);
        } catch (IOException e) {
            throw new ArcRetrieverException(
                    ArcRetrieverException.ERROR_BAD_FUNCTION_ARGUMENT, e);
        } catch (TransformerException e) {
            throw new ArcRetrieverException(
                    ArcRetrieverException.ERROR_BAD_FUNCTION_ARGUMENT, e);
        }
    }

    public void getMeta(HttpServletResponse response, AID aid)
            throws ArcRetrieverException {
        ARCReader arc = null;
        ARCRecord rec = null;
        OutputStream out = null;
        try {
            File file = aid.getFile(this.arcdir);
            try {
                arc = ARCReaderFactory.get(file);
                rec = arc.get(aid.getOffset());
            } catch (IOException e) {
                throw new ArcRetrieverException(
                        ArcRetrieverException.ERROR_OBJECT_NOT_ACCESSIBLE);
            } catch (Exception e) {
                throw new ArcRetrieverException(
                        ArcRetrieverException.ERROR_BAD_FUNCTION_ARGUMENT, e);
            }
            ARCRecordMetaData meta = rec.getMetaData();
            out = response.getOutputStream();
            HeaderGroup headers = new HeaderGroup();
            headers.setHeaders(rec.getHttpHeaders());

            response.setContentType("text/xml; charset=UTF-8");

            Document dom = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder().newDocument();
            Node msg = dom.appendChild(dom.createElement("retrievermessage"));
            Node head = msg.appendChild(dom.createElement("head"));
            addTextElement(head, "reqtype", "getmeta");
            addTextElement(head, "aid", aid.toString());
            Node body = msg.appendChild(dom.createElement("body"));
            Node metadata = body.appendChild(dom.createElement("metadata"));
            addTextElement(metadata, "url", meta.getUrl());
            String arcDate = meta.getDate();
            addTextElement(metadata, "archival_time", arcDate);

            try {
                // Trying to parse dates in the following format:
                // Mon, 14 Jun 2004 11:13:02 GMT
                DateFormat df = new SimpleDateFormat(
                        "E, d MMM yyyy HH:mm:ss z", Locale.US);
                String lastModDate = getHttpHeader(headers, "last-modified");
                lastModDate = ArchiveUtils.get14DigitDate(df.parse(lastModDate)
                        .getTime());
                addTextElement(metadata, "last_modified_time", lastModDate);
            } catch (ParseException e) {
                addTextElement(metadata, "last_modified_time", arcDate);
            } catch (NullPointerException e) {
                addTextElement(metadata, "last_modified_time", arcDate);
            }
            addTextElement(metadata, "content_length", getHttpHeader(headers,
                    "content-length"));
            Node contenttype = metadata.appendChild(dom
                    .createElement("contenttype"));
            addTextElement(contenttype, "type", meta.getMimetype());
            String contentTypeString = getHttpHeader(headers, "content-type");
            String charset = "";
            if (contentTypeString != null) {
                Matcher m = charsetPattern.matcher(contentTypeString
                        .toLowerCase());
                if (m.matches()) {
                    charset = m.group(1);
                }
            }
            addTextElement(contenttype, "charset", charset);
            addTextElement(metadata, "filestatus", "online");
            addTextElement(metadata, "filestatus_long", "");
            String header = getAllHeadersAsString(headers);
            //remove illegal XML-characters
            header = header.replaceAll("[\\p{Cc}&&[^\\u0009\\u000A\\u000D]]+",
                    "???");
            header = header.trim();
            rec.close();
            addTextElement(metadata, "content_checksum", meta.getDigest());
            arc.close();

            addTextElement(metadata, "http-header", header);

            Transformer transformer = TransformerFactory.newInstance()
                    .newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            Result res = new StreamResult(out);
            Source source = new DOMSource(dom);
            transformer.transform(source, res);
        } catch (ParserConfigurationException e) {
            throw new ArcRetrieverException(
                    ArcRetrieverException.ERROR_BAD_FUNCTION_ARGUMENT, e);
        } catch (TransformerConfigurationException e) {
            throw new ArcRetrieverException(
                    ArcRetrieverException.ERROR_BAD_FUNCTION_ARGUMENT, e);
        } catch (IOException e) {
            throw new ArcRetrieverException(
                    ArcRetrieverException.ERROR_BAD_FUNCTION_ARGUMENT, e);
        } catch (TransformerException e) {
            throw new ArcRetrieverException(
                    ArcRetrieverException.ERROR_BAD_FUNCTION_ARGUMENT, e);
        } finally {
            try {
                if (out != null) out.close();
            } catch (IOException e) {
            }
        }
    }

    private String getAllHeadersAsString(final HeaderGroup headers) {
        StringBuffer buffer = new StringBuffer();
        for (final Iterator i = headers.getIterator(); i.hasNext();) {
            Header h = (Header)i.next();
            String hdrStr = h.toString(); 
            Matcher m = CR.matcher(hdrStr);
            if (m != null) {
                hdrStr = m.replaceAll(" ");
            }
            buffer.append(hdrStr);
        } 
        return buffer.toString();
    }

    private String getHttpHeader(HeaderGroup headers, String headerName) {
        Header header = headers.getCondensedHeader(headerName);
        return header == null ? "" : header.getValue();
    }

    private void addTextElement(Node parent, String elementName, String value) {
        value = value == null ? "" : value;
        Document dom = parent.getOwnerDocument();
        parent.appendChild(dom.createElement(elementName)).appendChild(
                dom.createTextNode(value));
    }

    private void addCDataElement(Node parent, String elementName,
            String value) {
        value = value == null ? "" : value;
        Document dom = parent.getOwnerDocument();
        parent.appendChild(dom.createElement(elementName)).appendChild(
                dom.createCDATASection(value));
    }

    public void getDocument(HttpServletResponse response, AID aid)
            throws Exception {
        OutputStream out = response.getOutputStream();
        ARCRecord rec = null;
        ARCReader arc = null;
        File file = aid.getFile(this.arcdir);
        arc = ARCReaderFactory.get(file);
        rec = arc.get(aid.getOffset());
        ARCRecordMetaData meta = rec.getMetaData();
        rec.skipHttpHeader();
        HeaderGroup headers = new HeaderGroup();
        headers.setHeaders(rec.getHttpHeaders());
        String contentTypeString = getHttpHeader(headers, "content-type");
        response.setContentType(contentTypeString);
        //response.setContentLength((int) meta.getLength());

        byte[] buf = new byte[1024];
        int c;
        while ((c = rec.read(buf)) != -1) {
            out.write(buf, 0, c);
        }
        out.flush();
        rec.close();
        arc.close();
    }

    private void handleException(HttpServletResponse response, Throwable t)
            throws UnsupportedEncodingException, IOException {
        response.setContentType("text/xml; charset=UTF-8");
        PrintWriter out = new PrintWriter(new OutputStreamWriter(response
                .getOutputStream(), "UTF-8"));
        out.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
        ArcRetrieverException are;
        if (t instanceof ArcRetrieverException) {
            are = (ArcRetrieverException) t;
        } else {
            are = new ArcRetrieverException(7, t);
        }

        out.println("<retrievermessage>");
        out.println("  <head>");
        out.println("    <errorcode>" + are.getErrorCode() + "</errorcode>");
        out.println("    <errormessage>" + are.getLocalizedMessage()
                + "</errormessage>");
        out.println("  </head>");
        if (are.getCause() != null) {
            out.println("\n  <body>");
            out.println("Cause: " + are.getCause().getClass().getName() + ": "
                    + are.getCause().getLocalizedMessage());
            out.println("\nStack trace:");
            StackTraceElement[] trace = are.getCause().getStackTrace();
            for (int i = 0; i < trace.length; i++) {
                out.println(trace[i].toString().replaceAll("<", "&lt;").replaceAll(
                        ">", "&gt;"));
            }
            out.println("  </body>");
        }
        out.println("</retrievermessage>");
        out.flush();
        out.close();
    }
}
