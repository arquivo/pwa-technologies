package org.apache.lucene.search.filters;

import com.google.common.cache.CacheStats;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.search.PwaSearchableCommon;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.caches.PwaSpamFilterCache;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PwaSpamFilter extends PwaFilter {

    public static final Log LOG = LogFactory.getLog(PwaSpamFilter.class);

    private IndexReader indexReader;
    private static PwaSpamRegex spamRegex;
    private static PwaSpamFilterCache filterCache;

    private class SpamFilterCallable implements Callable<Boolean> {

        private IndexReader indexReader;
        private PwaSpamRegex spamRegex;

        private SpamFilterCallable(IndexReader indexReader, PwaSpamRegex spamRegex){
            this.indexReader = indexReader;
            this.spamRegex = spamRegex;
        }

        public Boolean call() throws Exception {
            Document document = this.indexReader.document(searchable.doc());
            String domain = document.getField("domain").stringValue();

            Pattern pattern = Pattern.compile(this.spamRegex.regexPatternString);
            Matcher matcher = pattern.matcher(domain);

            if (!matcher.matches()) {
                return true;
            }
            return false;
        }
    }

    public boolean next() throws IOException {
        while (searchable.next()) {
            try {
                return filterCache.get(searchable.doc(), new SpamFilterCallable(this.indexReader, this.spamRegex));
            } catch (ExecutionException e) {
                LOG.error("Problem arose when searching for DocID at cache.", e);
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
        this.filterCache = PwaSpamFilterCache.getInstance();
    }

    public boolean hasDoc() {
        return searchable.hasDoc();
    }
}
