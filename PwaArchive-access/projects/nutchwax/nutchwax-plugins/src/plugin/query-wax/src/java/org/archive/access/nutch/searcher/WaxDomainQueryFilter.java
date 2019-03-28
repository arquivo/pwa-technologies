package org.archive.access.nutch.searcher;

import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.searcher.RawFieldQueryFilter;

/**
 * Look for domain field.
 * @author Daniel Bicho
 */
public class WaxDomainQueryFilter extends RawFieldQueryFilter
{
    private Configuration conf;

    public WaxDomainQueryFilter()
    {
        super("domain", true, 0.1f);
    }

    public Configuration getConf()
    {
        return this.conf;
    }

    public void setConf(Configuration conf)
    {
        this.conf = conf;
    }

}
