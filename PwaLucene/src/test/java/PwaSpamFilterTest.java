import junit.framework.TestCase;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.filters.PwaSpamFilter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import javax.management.Query;
import java.net.URL;

public class PwaSpamFilterTest extends TestCase {

    private Directory idx;
    private Searcher searcher;
    private IndexReader reader;

    @Override
    protected void setUp() throws Exception {
        URL indexFolder = PwaSpamFilterTest.class.getClassLoader().getResource("index");
        this.idx = FSDirectory.getDirectory(indexFolder.getPath(), false);
        this.searcher = new IndexSearcher(idx);
        this.reader = IndexReader.open(idx);
    }

    protected void tearDown() throws Exception {
        // close all
        searcher.close();
        reader.close();
    }

    public void testSpamFilter(){
        // perform query
        Query query = new QueryParser();
        searcher.search()

        // measure results
    }
}
