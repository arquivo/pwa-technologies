// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   MultiReader.java

package org.apache.lucene.index;

import java.io.IOException;
import java.util.*;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.store.Directory;

// Referenced classes of package org.apache.lucene.index:
//            IndexReader, MultiTermEnum, MultiTermDocs, MultiTermPositions, 
//            SegmentReader, SegmentInfos, TermFreqVector, TermEnum, 
//            Term, TermDocs, TermPositions, IndexFileDeleter

public class MultiReader extends IndexReader
{

    public MultiReader(IndexReader subReaders[])
        throws IOException
    {
        super(subReaders.length != 0 ? subReaders[0].directory() : null);
        normsCache = new Hashtable();
        maxDoc = 0;
        numDocs = -1;
        hasDeletions = false;
        initialize(subReaders);
    }

    MultiReader(Directory directory, SegmentInfos sis, boolean closeDirectory, IndexReader subReaders[])
    {
        super(directory, sis, closeDirectory);
        normsCache = new Hashtable();
        maxDoc = 0;
        numDocs = -1;
        hasDeletions = false;
        initialize(subReaders);
    }

    private void initialize(IndexReader subReaders[])
    {
        this.subReaders = subReaders;
        starts = new int[subReaders.length + 1];
        for(int i = 0; i < subReaders.length; i++)
        {
            starts[i] = maxDoc;
            //maxDoc += subReaders[i].maxDoc(); 
            maxDoc += ((SegmentReader)subReaders[i]).maxDocPlus4(); // BUG norm was exchanged by field length
            if(subReaders[i].hasDeletions())
                hasDeletions = true;
        }

        starts[subReaders.length] = maxDoc;
    }

    public TermFreqVector[] getTermFreqVectors(int n)
        throws IOException
    {
        int i = readerIndex(n);
        return subReaders[i].getTermFreqVectors(n - starts[i]);
    }

    public TermFreqVector getTermFreqVector(int n, String field)
        throws IOException
    {
        int i = readerIndex(n);
        return subReaders[i].getTermFreqVector(n - starts[i], field);
    }

    public synchronized int numDocs()
    {
        if(numDocs == -1)
        {
            int n = 0;
            for(int i = 0; i < subReaders.length; i++)
                n += subReaders[i].numDocs();

            numDocs = n;
        }
        return numDocs;
    }

    public int maxDoc()
    {
        return maxDoc;
    }

    public Document document(int n, FieldSelector fieldSelector)
        throws IOException
    {
        int i = readerIndex(n);
        return subReaders[i].document(n - starts[i], fieldSelector);
    }

    public boolean isDeleted(int n)
    {
        int i = readerIndex(n);
        return subReaders[i].isDeleted(n - starts[i]);
    }

    public boolean hasDeletions()
    {
        return hasDeletions;
    }

    protected void doDelete(int n)
        throws IOException
    {
        numDocs = -1;
        int i = readerIndex(n);
        subReaders[i].deleteDocument(n - starts[i]);
        hasDeletions = true;
    }

    protected void doUndeleteAll()
        throws IOException
    {
        for(int i = 0; i < subReaders.length; i++)
            subReaders[i].undeleteAll();

        hasDeletions = false;
        numDocs = -1;
    }

    private int readerIndex(int n)
    {
        int lo = 0;
        int hi;
        for(hi = subReaders.length - 1; hi >= lo;)
        {
            int mid = lo + hi >> 1;
            int midValue = starts[mid];
            if(n < midValue)
                hi = mid - 1;
            else
            if(n > midValue)
            {
                lo = mid + 1;
            } else
            {
                for(; mid + 1 < subReaders.length && starts[mid + 1] == midValue; mid++);
                return mid;
            }
        }

        return hi;
    }

    public boolean hasNorms(String field)
        throws IOException
    {
        for(int i = 0; i < subReaders.length; i++)
            if(subReaders[i].hasNorms(field))
                return true;

        return false;
    }

    private byte[] fakeNorms()
    {
        if(ones == null)
            ones = SegmentReader.createFakeNorms(maxDoc());
        return ones;
    }

    public synchronized byte[] norms(String field)
        throws IOException
    {
        byte bytes[] = (byte[])normsCache.get(field);
        if(bytes != null)
            return bytes;
        if(!hasNorms(field))
            return fakeNorms();
        bytes = new byte[maxDoc()];
        for(int i = 0; i < subReaders.length; i++)
            subReaders[i].norms(field, bytes, starts[i]);

        normsCache.put(field, bytes);
        return bytes;
    }

    public synchronized void norms(String field, byte result[], int offset)
        throws IOException
    {
        byte bytes[] = (byte[])normsCache.get(field);
        if(bytes == null && !hasNorms(field))
            bytes = fakeNorms();
        if(bytes != null)
            System.arraycopy(bytes, 0, result, offset, maxDoc());
        for(int i = 0; i < subReaders.length; i++)
            subReaders[i].norms(field, result, offset + starts[i]);

    }
    
    
    public synchronized byte[] lengths(String field) throws IOException { 
    	return norms(field);
    }
    

    protected void doSetNorm(int n, String field, byte value)
        throws IOException
    {
        normsCache.remove(field);
        int i = readerIndex(n);
        subReaders[i].setNorm(n - starts[i], field, value);
    }

    public TermEnum terms()
        throws IOException
    {
        return new MultiTermEnum(subReaders, starts, null);
    }

    public TermEnum terms(Term term)
        throws IOException
    {
        return new MultiTermEnum(subReaders, starts, term);
    }

    public int docFreq(Term t)
        throws IOException
    {
        int total = 0;
        for(int i = 0; i < subReaders.length; i++)
            total += subReaders[i].docFreq(t);

        return total;
    }

    public TermDocs termDocs()
        throws IOException
    {
        return new MultiTermDocs(subReaders, starts);
    }

    public TermPositions termPositions()
        throws IOException
    {
        return new MultiTermPositions(subReaders, starts);
    }

    protected void setDeleter(IndexFileDeleter deleter)
    {
        this.deleter = deleter;
        for(int i = 0; i < subReaders.length; i++)
            subReaders[i].setDeleter(deleter);

    }

    protected void doCommit()
        throws IOException
    {
        for(int i = 0; i < subReaders.length; i++)
            subReaders[i].commit();

    }

    void startCommit()
    {
        super.startCommit();
        for(int i = 0; i < subReaders.length; i++)
            subReaders[i].startCommit();

    }

    void rollbackCommit()
    {
        super.rollbackCommit();
        for(int i = 0; i < subReaders.length; i++)
            subReaders[i].rollbackCommit();

    }

    protected synchronized void doClose()
        throws IOException
    {
        for(int i = 0; i < subReaders.length; i++)
            subReaders[i].close();

    }

    public Collection getFieldNames(IndexReader.FieldOption fieldNames)
    {
        Set fieldSet = new HashSet();
        for(int i = 0; i < subReaders.length; i++)
        {
            IndexReader reader = subReaders[i];
            Collection names = reader.getFieldNames(fieldNames);
            fieldSet.addAll(names);
        }

        return fieldSet;
    }

    private IndexReader subReaders[];
    private int starts[];
    private Hashtable normsCache;
    private int maxDoc;
    private int numDocs;
    private boolean hasDeletions;
    private byte ones[];
}
