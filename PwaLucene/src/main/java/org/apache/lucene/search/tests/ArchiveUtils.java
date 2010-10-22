/*
 * ArchiveUtils
 *
 * $Header: /cvsroot/archive-crawler/ArchiveOpenCrawler/src/java/org/archive/util/ArchiveUtils.java,v 1.38 2007/01/23 00:29:48 gojomo Exp $
 *
 * Created on Jul 7, 2003
 *
 * Copyright (C) 2003 Internet Archive.
 *
 * This file is part of the Heritrix web crawler (crawler.archive.org).
 *
 * Heritrix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * Heritrix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with Heritrix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package org.apache.lucene.search.tests;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Miscellaneous useful methods.
 *
 * @author gojomo & others
 */
public class ArchiveUtils {

    /**
     * Arc-style date stamp in the format yyyyMMddHHmm and UTC time zone.
     */
    private static final ThreadLocal<SimpleDateFormat> 
        TIMESTAMP12 = threadLocalDateFormat("yyyyMMddHHmm");;
    
    /**
     * Arc-style date stamp in the format yyyyMMddHHmmss and UTC time zone.
     */
    private static final ThreadLocal<SimpleDateFormat> 
       TIMESTAMP14 = threadLocalDateFormat("yyyyMMddHHmmss");
    /**
     * Arc-style date stamp in the format yyyyMMddHHmmssSSS and UTC time zone.
     */
    private static final ThreadLocal<SimpleDateFormat> 
        TIMESTAMP17 = threadLocalDateFormat("yyyyMMddHHmmssSSS");

    /**
     * Log-style date stamp in the format yyyy-MM-dd'T'HH:mm:ss.SSS'Z'
     * UTC time zone is assumed.
     */
    private static final ThreadLocal<SimpleDateFormat> 
        TIMESTAMP17ISO8601Z = threadLocalDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    
    /**
     * Log-style date stamp in the format yyyy-MM-dd'T'HH:mm:ss'Z'
     * UTC time zone is assumed.
     */
    private static final ThreadLocal<SimpleDateFormat>
        TIMESTAMP14ISO8601Z = threadLocalDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    
    /**
     * Default character to use padding strings.
     */
    private static final char DEFAULT_PAD_CHAR = ' ';

    /** milliseconds in an hour */ 
    private static final int HOUR_IN_MS = 60 * 60 * 1000;
    /** milliseconds in a day */
    private static final int DAY_IN_MS = 24 * HOUR_IN_MS;
    
    private static ThreadLocal<SimpleDateFormat> threadLocalDateFormat(final String pattern) {
        ThreadLocal<SimpleDateFormat> tl = new ThreadLocal<SimpleDateFormat>() {
            protected SimpleDateFormat initialValue() {
                SimpleDateFormat df = new SimpleDateFormat(pattern);
                df.setTimeZone(TimeZone.getTimeZone("GMT"));
                return df;
            }
        };
        return tl;
    }
    
    public static int MAX_INT_CHAR_WIDTH =
        Integer.toString(Integer.MAX_VALUE).length();
    
       
    /**
     * @param i Integer to add prefix of zeros too.  If passed
     * 2005, will return the String <code>0000002005</code>. String
     * width is the width of Integer.MAX_VALUE as a string (10
     * digits).
     * @return Padded String version of <code>i</code>.
     */
    public static String zeroPadInteger(int i) {
        return ArchiveUtils.padTo(Integer.toString(i),
                MAX_INT_CHAR_WIDTH, '0');
    }

    /** 
     * Convert an <code>int</code> to a <code>String</code>, and pad it to
     * <code>pad</code> spaces.
     * @param i the int
     * @param pad the width to pad to.
     * @return String w/ padding.
     */
    public static String padTo(final int i, final int pad) {
        String n = Integer.toString(i);
        return padTo(n, pad);
    }
    
    /** 
     * Pad the given <code>String</code> to <code>pad</code> characters wide
     * by pre-pending spaces.  <code>s</code> should not be <code>null</code>.
     * If <code>s</code> is already wider than <code>pad</code> no change is
     * done.
     *
     * @param s the String to pad
     * @param pad the width to pad to.
     * @return String w/ padding.
     */
    public static String padTo(final String s, final int pad) {
        return padTo(s, pad, DEFAULT_PAD_CHAR);
    }

    /** 
     * Pad the given <code>String</code> to <code>pad</code> characters wide
     * by pre-pending <code>padChar</code>.
     * 
     * <code>s</code> should not be <code>null</code>. If <code>s</code> is
     * already wider than <code>pad</code> no change is done.
     *
     * @param s the String to pad
     * @param pad the width to pad to.
     * @param padChar The pad character to use.
     * @return String w/ padding.
     */
    public static String padTo(final String s, final int pad,
            final char padChar) {
        String result = s;
        int l = s.length();
        if (l < pad) {
            StringBuffer sb = new StringBuffer(pad);
            while(l < pad) {
                sb.append(padChar);
                l++;
            }
            sb.append(s);
            result = sb.toString();
        }
        return result;
    }

  }

