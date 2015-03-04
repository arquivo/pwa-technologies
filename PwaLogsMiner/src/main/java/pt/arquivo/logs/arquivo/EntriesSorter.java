package pt.arquivo.logs.arquivo;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Sort entries
 * @author Miguel Costa
 */
public class EntriesSorter {
	/**
	 * Sort queries by repetition
	 */
	static Object[] sortQueries(HashMap<String,Integer> queryMap) {
		Object entriesArray[] = queryMap.entrySet().toArray();
		Arrays.sort(entriesArray, new Comparator(){
			public int compare(Object o1,Object o2) {	
                            Map.Entry<String,Integer> entry1=(Map.Entry<String,Integer>)o1;
                            Map.Entry<String,Integer> entry2=(Map.Entry<String,Integer>)o2;

                            String key1=entry1.getKey().split("\\s")[0];
                            String key2=entry2.getKey().split("\\s")[0];

                            int comp=key1.compareTo(key2);
                            if (comp!=0) { // sort by key then by value (frequency)
                                    return comp;
                            }

                            if (entry1.getValue() > entry2.getValue()) {
                                return -1;
                            }
                            else if (entry1.getValue().equals(entry2.getValue())){
                                return 0;
                            }
                            else{
                                return 1;   
                            }  
			}
		});	 
		return entriesArray;
	}
	
	/**
	 * Sort terms by repetition
	 */
	static Object[] sortTerms(HashMap<String,Integer> termsMap) {
		Object entriesArray[] = termsMap.entrySet().toArray();
		Arrays.sort(entriesArray, new Comparator(){
			public int compare(Object o1,Object o2) {	
				Map.Entry<String,Integer> entry1=(Map.Entry<String,Integer>)o1;
				Map.Entry<String,Integer> entry2=(Map.Entry<String,Integer>)o2;	    		 	    		 	    

				if (entry1.getValue() > entry2.getValue()) {
                                    return -1;
				}
                                else if (entry1.getValue().equals(entry2.getValue())){
                                    return 0;
                                }
                                else {
                                    return 1;   
                                }				
			}
		});
		return entriesArray;
	}
}
