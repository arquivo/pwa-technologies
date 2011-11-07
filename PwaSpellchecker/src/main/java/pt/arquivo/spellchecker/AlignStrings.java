package pt.arquivo.spellchecker;

/**
 * Align strings by the maximum number of matching characters
 * @author Miguel Costa
 */
public class AlignStrings {
	
	/**
	 * Align strings
	 * @param s1
	 * @param s2
	 * @param numMatchMax number of characters that match (return parameter)
	 * @return offset of the first string relative to the second
	 */
	public static int align(String s1, String s2, Integer numMatchMax) {
		numMatchMax=0;
		int offsetMax=0;
		
		// align string2 for string1
		for (int i=0;i<s2.length();i++) {
			int numMatch=nCharsAligned(s1,s2,i);
			if (numMatch>numMatchMax) {
				numMatchMax=numMatch;
				offsetMax=i;
			}
		}
		// align string1 for string2
		for (int i=0;i<s1.length();i++) {
			int numMatch=nCharsAligned(s2,s1,i);
			if (numMatch>numMatchMax) {
				numMatchMax=numMatch;
				offsetMax=-i;
			}
		}
		
		return offsetMax;
	}
	
	/**
	 * Count the number of characters aligned
	 * @param s1
	 * @param s2
	 * @param offset offset of @s2 string
	 * @return
	 */
	private static int nCharsAligned(String s1, String s2, int offset) {
		int counter=0;
		for (int i=offset;i<s2.length();i++) {
			if (i-offset<s1.length() && s2.charAt(i)==s1.charAt(i-offset)) {
				counter++;		
			}
		}
		return counter;
	}

	
	/**
	 * Main
	 * @param args
	 */
	/*
	public static void main(String[] args) {
		
		 String terms[][]={{"elio","helio"},{"adela","mortandela"},{"helio","elio"},{"ministro","menistro"},{"própria","própia"}};
		 
		 int pad=10;
		 StringDifferences strDiff=new StringDifferences();
		 for (int i=0;i<terms.length;i++) {
			 for (int j=0;j<pad+align(terms[i][0],terms[i][1]);j++) {
				 System.out.print(" ");
			 }			 
			 System.out.println(terms[i][0]);
			 for (int j=0;j<pad;j++) {
				 System.out.print(" ");
			 }			 
			 System.out.println(terms[i][1]);	
			 
			 strDiff.analyzeDifferences(terms[i][0],terms[i][1]);			 
		 }
		 strDiff.stats();		
	}
	*/
}
