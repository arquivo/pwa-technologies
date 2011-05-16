package org.archive.access.nutch.jobs.graph;

import it.unimi.dsi.webgraph.*;

import java.io.*;

/**
 * Extends ArcListASCIIGraph from webgraph library
 * @author Miguel Costa
 */
public class ArcListASCIIGraphExt extends  ArcListASCIIGraph {

	private int numNodes;
	
	public ArcListASCIIGraphExt(InputStream is, int shift) throws NumberFormatException, IOException {
		super(is,shift);
		numNodes=-1;
	}
	
	public void setNumNodes(int numNodes) {
		this.numNodes=numNodes;
	}
	
	public int numNodes() {
		if (numNodes==-1) 
			throw new UnsupportedOperationException( "The number of nodes is unknown (you need to complete a traversal)" );
		return numNodes;
	}
	
	public static ArcListASCIIGraphExt loadOnce( final InputStream is ) throws IOException {
		return new ArcListASCIIGraphExt( is, 0 );
	}

	public static ArcListASCIIGraphExt loadOnce( final InputStream is, final int shift ) throws IOException {
		return new ArcListASCIIGraphExt( is, shift );
	}	
}
