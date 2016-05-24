package pt.arquivoweb.utils;

import java.util.Comparator;
import org.archive.wayback.core.SearchResult;


public class SearchResultDateComparator implements Comparator<SearchResult> {
       
        public int compare(SearchResult arg0, SearchResult arg1) {
                long l0 = Long.parseLong(arg0.getCaptureDate());
                long l1 = Long.parseLong(arg1.getCaptureDate());
                
                if ( l0 > l1 )
                        return -1;
                else if (l0 < l1)
                        return 1;
                else
                        return 0; 
        }

}
