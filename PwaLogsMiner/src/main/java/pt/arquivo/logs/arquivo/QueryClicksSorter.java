package pt.arquivo.logs.arquivo;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Sort QueryClicks entries
 * @author Miguel Costa
 */
public class QueryClicksSorter {
	/**
	 * Sort queries by repetition
	 */
	static Object[] sortQueryClicks(HashMap<String,QueryClicks> queryMap) {
		Object entriesArray[] = queryMap.entrySet().toArray();
		Arrays.sort(entriesArray, new Comparator(){
			public int compare(Object o1,Object o2) {	
				Map.Entry<String,QueryClicks> entry1=(Map.Entry<String,QueryClicks>)o1;
				Map.Entry<String,QueryClicks> entry2=(Map.Entry<String,QueryClicks>)o2;

				int value1=entry1.getValue().getNumClicks();
				int value2=entry2.getValue().getNumClicks();

				if (value1>value2) {
					return -1;
				}				
				return 1;
			}
		});	 
		return entriesArray;
	}	
}
