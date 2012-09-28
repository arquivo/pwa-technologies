package org.archive.access.nutch.jobs.graph;

import java.io.*;
import it.unimi.dsi.law.rank.PageRank.*;
import it.unimi.dsi.law.rank.*;
import it.unimi.dsi.webgraph.ImmutableGraph;


/**
 * Computes Pagerank
 * @author Miguel Costa
 */
public class Pagerank {

	//private final static float DUMP_FACTOR=0.15f; // 1-DEFAULT_ALPHA (=0.85)
	private final static int   MAX_ITERATIONS=100;  // PageRank.DEFAULT_MAX_ITER
		
	/**
	 * Compute Pagerank and returns array of scores from document 0 to n-1
	 */
	public static double[] compute(ImmutableGraph graph) throws IOException {
			
		PageRankPowerMethod pr=new PageRankPowerMethod(graph);		
		pr.init();
		pr.stepUntil(PageRank.or(new NormDeltaStoppingCriterion(PageRank.DEFAULT_THRESHOLD),
				new IterationNumberStoppingCriterion(MAX_ITERATIONS)));
		return pr.previousRank;
		
		/*
		HashMap<Integer,Float> id2score=new HashMap<Integer,Float>(); // store scores
		for (int i=0;i<prvalues.length;i++) {
			id2score.put(i,(float)prvalues[i]);
		}

		return id2score;
		*/
	}
		
}
