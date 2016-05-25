package pt.arquivo.spellchecker;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Analyze the string differences
 * @author Miguel Costa
 */
public class StringDifferences {
	
	private final static int MAX_DIFF=2;
	private final static int MAX_STR_PART=4;
	
	private HashMap<String,Integer> substitutions=new HashMap<String,Integer>();
	private HashMap<String,Integer> additionsBeggining=new HashMap<String,Integer>();

	
	/**
	 * Analyze and count the string differences
	 * @param s1
	 * @param s2
	 */
	public void analyzeDifferences(String s1, String s2) {
		analyzeDifferencesAux(s1, s2, 0);
	}

	/**
	 * Analyze and count the string differences
	 * @param s1
	 * @param s2
	 * @param level recursivity level
	 */
	private void analyzeDifferencesAux(String s1, String s2, int level) {
		 Integer numMatchMax=null;
		 Integer offset=AlignStrings.align(s1,s2,numMatchMax);
		 if (numMatchMax==0) {
			 String subst=null;					
			 if (s1.compareTo(s2)>0) {
				subst=s1+" "+s2;
			}
			else {
				subst=s2+" "+s1;
			}
			Integer iobj=substitutions.get(subst);
			substitutions.put(subst,((iobj==null) ? 1 : iobj+1));
			return;
		 }
		 if (offset<0) { // swap strings
			 String s3=s1;
			 s1=s2;
			 s2=s3;
			 offset*=-1;
		 }
		 if (offset>0 && level==0) {
			 String addit=s2.substring(0,offset);
			 if (s2.length()<=MAX_STR_PART && addit.length()<=MAX_DIFF && !s1.startsWith(addit)) {			 			 
				 Integer iobj=additionsBeggining.get(addit);
				 additionsBeggining.put(addit,((iobj==null) ? 1 : iobj+1));
				 //System.out.println("add:"+addit+" "+s2+" "+s1);
			 }
		 }
		 String diff1="";
		 String diff2="";		 
		 for (int i=offset;i<s2.length();i++) {
			 if (i-offset<s1.length() && s2.charAt(i)==s1.charAt(i-offset)) { // equal
				if (diff1.length()>0) {			
					if (diff1.length()>MAX_DIFF || diff2.length()>MAX_DIFF) {
						analyzeDifferencesAux(diff1,s2.substring(0,offset)+diff2,level+1);
					}
					else {					
						String subst=null;					
						if (diff1.compareTo(diff2)>0) {
							subst=diff1+" "+diff2;
						}
						else {
							subst=diff2+" "+diff1;
						}
						Integer iobj=substitutions.get(subst);
						substitutions.put(subst,((iobj==null) ? 1 : iobj+1));
						// System.out.println("diff;"+diff1+" "+diff2);
					}
					
					diff1="";
					diff2="";	
				}									
			}
			else if (i-offset<s1.length() && s2.charAt(i)!=s1.charAt(i-offset)) {
				diff1+=s1.charAt(i-offset);
				diff2+=s2.charAt(i);				
			}
		 }		
		 if (offset+s2.length()<s1.length()) {
			 diff1+=s1.substring(offset+s2.length());
		 }			 
		 if (diff1.length()>0 || diff2.length()>0) {
			 if (diff1.length()>MAX_DIFF || diff2.length()>MAX_DIFF) {
				 analyzeDifferencesAux(diff1,s2.substring(0,offset)+diff2,level+1);
			 }
			 else {
				 String subst=null;					
				 if (diff1.compareTo(diff2)>0) {
					 subst=diff1+" "+diff2;
				 }
				 else {
					 subst=diff2+" "+diff1;
				 }
				 Integer iobj=substitutions.get(subst);
				 substitutions.put(subst,((iobj==null) ? 1 : iobj+1));
				 //	System.out.println("diff;"+diff1+" "+diff2);
			 }
		 }		
	}
	
	/**
	 * Print stats of string differences
	 */
	public void stats() {
		Object entriesArray[] = substitutions.entrySet().toArray();
		Arrays.sort(entriesArray, new SortComparator());			
		for (int i=0;i<entriesArray.length;i++) {
			System.out.println("substitution:"+((Map.Entry)entriesArray[i]).getKey()  + " = " + (((Map.Entry)entriesArray[i]).getValue()));					
		}
		
		entriesArray = additionsBeggining.entrySet().toArray();
		Arrays.sort(entriesArray, new SortComparator());	
		
		for (int i=0;i<entriesArray.length;i++) {
			System.out.println("addition:"+((Map.Entry)entriesArray[i]).getKey()  + " = " + (((Map.Entry)entriesArray[i]).getValue()));					
		}			
	}
	
	/**
	 * Comparator by decreasing frequency	 
	 */
	private class SortComparator implements Comparator {
		
		public SortComparator() {};
		
		public int compare(Object o1,Object o2) {	
			Map.Entry<String,Integer> entry1=(Map.Entry<String,Integer>)o1;
			Map.Entry<String,Integer> entry2=(Map.Entry<String,Integer>)o2;
		
			if (entry1.getValue()>entry2.getValue()) {
				return -1;
			}	    		
			return 1;
		}
	}
}
