package org.apache.lucene.search.filters;

import org.apache.lucene.search.PwaSearchableCommon;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.document.Document;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PwaSpamFilter extends PwaFilter {

    private IndexReader indexReader;
    private static PwaSpamRegex spamRegex;


    public boolean next() throws IOException {
        while (searchable.next()) {
            IndexReader indexReader;
            Document document = this.indexReader.document(searchable.doc());
            String domain = document.getField("domain").stringValue();

            Pattern pattern = Pattern.compile(this.spamRegex.regexPatternString);
            Matcher matcher = pattern.matcher(domain);

            if (!matcher.matches()) {
                return true;
            }
        }
        return false;
    }

    public int doc() {
        return searchable.doc();
    }

    public PwaSpamFilter(IndexReader indexReader){
        this(null, indexReader);
    }

    public PwaSpamFilter(PwaSearchableCommon searchable, IndexReader indexReader) {
        super(searchable);
        this.indexReader = indexReader;
        this.spamRegex = PwaSpamRegex.getInstance();
    }

    public boolean hasDoc() {
        return searchable.hasDoc();
    }
}
