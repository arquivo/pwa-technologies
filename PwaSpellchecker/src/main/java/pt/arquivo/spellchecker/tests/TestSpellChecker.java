package pt.arquivo.spellchecker.tests;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.spell.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;

import pt.arquivo.spellchecker.SpellChecker;
import pt.arquivo.spellchecker.StringDifferences;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import junit.framework.TestCase;


/**
 * Test case for spellchecker
 * @author Miguel Costa
 */
public class TestSpellChecker extends TestCase {

	private String indexDir="/data/arcs/outputsIA/index";
	private String field="content";	  
	private String termsFileDataset1Train="/home/nutchwax/workspace/ArquivoWebSpellchecker/data/erros_pt.txt.JOAO.train";
	private String termsFileDataset1Test="/home/nutchwax/workspace/ArquivoWebSpellchecker/data/erros_pt.txt.JOAO.test";
	private String termsFileDataset2Train="/home/nutchwax/workspace/ArquivoWebSpellchecker/data/errosmedeiros.txt.train";	
	private String termsFileDataset2Test="/home/nutchwax/workspace/ArquivoWebSpellchecker/data/errosmedeiros.txt.test";
	private String dictPath="/home/nutchwax/tools/hunspell-1.2.9/dictionaries";
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}
		
    /**
     * Test spellchecker algorithms 
     * @throws IOException
     */
	public void testSpellChecker() throws IOException {
		Directory idx = FSDirectory.getDirectory(indexDir, false);					 	    
		org.apache.lucene.index.IndexReader reader=IndexReader.open(idx);		
		System.out.println("Starting spellchecker...");		
									
		int minFreqArr[]={5000,10000,25000,50000,100000,500000,1000000};		
		int nTimesHigherArr[]={1,3,5,10};
		
		for (int minFreq=0;minFreq<minFreqArr.length;minFreq++) {			
			for (int nTimesHigher=0;nTimesHigher<nTimesHigherArr.length;nTimesHigher++) {										
		
				long time=System.currentTimeMillis();	
				int NUM_ALGS=1;
				int algMatches[]=new int[NUM_ALGS];
				int algNotRespod[]=new int[NUM_ALGS];
				
				//BufferedReader br = new BufferedReader( new FileReader(termsFileDataset2Train) );		 
				BufferedReader br = new BufferedReader( new FileReader(termsFileDataset2Test) );				
				String line=null;
				int counter=0;				
				while ( ( line = br.readLine() ) != null ) {
					String terms[]=line.split(";");
					if (terms.length!=2) { 
						continue;
					}
					terms[0]=terms[0].toLowerCase();
					
					String suggestions[]=null;
					for (int i=0;i<NUM_ALGS;i++) {
						try {
							switch(i) {
								case 0: suggestions = SpellChecker.suggestSimilarAspell(terms[1],"pt_PT",1,reader,field,minFreqArr[minFreq],nTimesHigherArr[nTimesHigher],dictPath); break;
								case 1: suggestions = SpellChecker.suggestSimilarHunspell(terms[1],"pt_PT",1,reader,field,minFreqArr[minFreq],nTimesHigherArr[nTimesHigher],dictPath); break;
							}
						}
						catch (InterruptedException e) {			
							e.printStackTrace();
						}
						
						boolean match=true;
					    if (suggestions.length==0 || suggestions[0].equals(terms[1])) {
							algNotRespod[i]++;
						}
						else if (suggestions[0].equals(terms[0])) {
							algMatches[i]++;						
						}
						else {
							match=false;
						}
					    
					    System.out.println("alg"+i+" error:"+terms[1]+" sugestion:"+(suggestions.length==0 ? "null" : suggestions[0])+" correct:"+terms[0]+" "+((!match) ? "*** FAIL ***" : ""));	 					    
					}		
					counter++;
				}			
				br.close();
				idx.close();

				// print statistics
				for (int i=0;i<NUM_ALGS;i++) {
					System.out.println("Statistics for algorithm: "+i);		 
					System.out.println("Total tests per algorithm: "+counter);
					System.out.println("Minimum frequency: "+minFreqArr[minFreq]);
					System.out.println("Number of Times Higher: "+nTimesHigherArr[nTimesHigher]);
					System.out.println("match:"+((float)algMatches[i]/(float)counter)+" notRespond:"+((float)algNotRespod[i]/(float)counter)+" fail:"+((float)1-(algMatches[i]+(float)algNotRespod[i])/(float)counter));
					System.out.println("Total time: "+(System.currentTimeMillis()-time)/1000+" seconds.");
					System.out.println("Time per term (for all algorithms): "+(float)(System.currentTimeMillis()-time)/(float)1000/(float)counter+" seconds.");
					System.out.println("----------------------------");	
				}
			}			
		}
	}	
		
	
}
