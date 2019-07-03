package org.apache.lucene.search.caches;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

public class PwaSpamFilterCache {
    public static final Log LOG = LogFactory.getLog(PwaSpamFilterCache.class);

    private static PwaSpamFilterCache pwaSpamFilterCache = null;
    public static Cache<Integer, Boolean> cache;


    private PwaSpamFilterCache(){
        LOG.info("Initializing PwaSpamFilterCache...");
        // concurrency level should be near the number of handlers
        this.cache = CacheBuilder.newBuilder().concurrencyLevel(20).initialCapacity(10000).recordStats().build();
    }

    public static PwaSpamFilterCache getInstance(){
        if(pwaSpamFilterCache == null){
            pwaSpamFilterCache = new PwaSpamFilterCache();
        }
        LOG.info("Cache Hit Rate: " + PwaSpamFilterCache.cache.stats().hitRate());
        return pwaSpamFilterCache;
    }

    public Boolean get(Integer key, Callable<Boolean> callable) throws ExecutionException {
        return this.cache.get(key, callable);
    }

}
