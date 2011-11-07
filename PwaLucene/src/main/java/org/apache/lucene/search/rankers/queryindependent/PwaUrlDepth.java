package org.apache.lucene.search.rankers.queryindependent;

import java.net.*;

import org.apache.lucene.search.rankers.PwaIRankingFunction;


/**
 * Gives a value according to the URL number of slashes
 * @author Miguel Costa
 */
public class PwaUrlDepth implements PwaIRankingFunction {
	
	private double score;
	
	
	/**
	 * Constructor
	 * @param surl url
	 */
	public PwaUrlDepth(String surl) throws MalformedURLException {
		URL url=new URL(surl); // check if it is well formed
		String urlParts[]=surl.split("/");
		int c=2; // http + '' 
		if (urlParts.length==1+c) { // domain
			score=3;
			return;
		}	
		if (urlParts[urlParts.length-1].indexOf(".")!=-1) { // file
			score=0;
			return;
		}
		if (urlParts.length==2+c) { // domain plus one directory
			score=2;
			return;
		}
		score=1; // domain plus more than one directory					 		
	}
	
	/**
	 * Return score
	 */
	public double score() {
		return score;
	}
		
}
