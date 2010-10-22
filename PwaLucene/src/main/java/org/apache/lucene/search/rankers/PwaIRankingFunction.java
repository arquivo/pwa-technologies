package org.apache.lucene.search.rankers;

/**
 * Ranking functions interface
 * @author Miguel Costa
 */
public interface PwaIRankingFunction {
	/* ranking functions used */
	/*
	public final static String functions[]={"TFxIDF-content",
										"BM25-content",
										"TFxIDF-url",              
										"BM25-url",                
										"TFxIDF-host",             
										"BM25-host",               
										"TFxIDF-anchor",           
										"BM25-anchor",             
										"TFxIDF-title",            
										"BM25-title",              
										"Lucene",                  
										"MinSpanCovOrd-content",   
										"MinSpanCovUnord-content", 
										"MinPairDist-content",     
										"MinSpanCovOrd-url",       
										"MinSpanCovUnord-url",     
										"MinPairDist-url",         
										"MinSpanCovOrd-host",      
										"MinSpanCovUnord-host",    
										"MinPairDist-host",        
										"MinSpanCovOrd-anchor",    
										"MinSpanCovUnord-anchor",  
										"MinPairDist-anchor",      
										"MinSpanCovOrd-title",     
										"MinSpanCovUnord-title",   
										"MinPairDist-title",       
										"UrlDepth",                
										"LinPagerank",             
										"LinInlinks",              
										"OPIC"};                    
	*/
	
	public double score();
}
