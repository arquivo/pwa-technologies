package org.archive.access.nutch.jobs.graph;

import it.unimi.dsi.webgraph.*;
import it.unimi.dsi.fastutil.ints.*;	    
import java.io.*;
import java.util.*;


public class GraphManager { 	
	
	private final static int DEFAULT_KEY_NOT_EXIST=-1;
/*
	private final static String GRAPH_LIST_FILE="graphList.txt";
	private final static String GRAPH_BV_FILE="graphBv.txt";
	private final static String SCORES_FILE="scoresx.txt";
*/
	
	/** The number of nodes. */
	private int nodes;
	
	/** <code>succ[x]</code> is the array of successors of <code>x</code>. */
	//private int succ[][];
	
	/** The basename. */
	//private CharSequence basename;
	
	/* arcs - id to sucessors set */
	//private Int2ObjectOpenHashMap<IntSet> arcs;
	
	/* map from hash(url) to id */
	private Int2IntOpenHashMap urlHash2id;
	
	
	/**
	 * Constructor
	 * @param nExpectedDocuments number of expected documents
	 */
	public GraphManager() {
		//arcs=new Int2ObjectOpenHashMap<IntSet>();
		nodes=0;
		urlHash2id=new Int2IntOpenHashMap();
		urlHash2id.defaultReturnValue(DEFAULT_KEY_NOT_EXIST);		
	}	

	public int numNodes() {
		return nodes;
	}
	/*
	public long numArcs() {		
		long c = 0;
		for (int i=0; i<numNodes(); i++)
			c += outdegree(i);
		return c;
	}

	public int outdegree( int x ) {				
		//return succ[x].length;
		return arcs.get(x).size();
	}

	public int[] successorArray( int x ) {			
		return succ[x];		
	}

	public CharSequence basename() {
		return basename;
	}
	
	public ImmutableGraph copy() {
		return null;		
	}
	
	public boolean randomAccess() {
		return false;
	}
	*/
	
	public int getId(String url) {
		int hash=url.hashCode(); // TODO change by other hash function: mash - http://www.hpl.hp.com/techreports/2008/HPL-2008-91R1.pdf
		int id;
		if ((id=urlHash2id.get(hash))==urlHash2id.defaultReturnValue()) { // if not exist
			id=nodes;
			/*
			try {
			*/
			urlHash2id.put(hash,id);
			/*
			}
			catch (java.lang.OutOfMemoryError err ) { // TODO remove
				System.out.println("TODO MC IA size:"+urlHash2id.size());
			}
			*/
			nodes++;
		}
		return id;
	}	
	
	public boolean hasId(String url) {
		int hash=url.hashCode(); // TODO
		return urlHash2id.get(hash)!=urlHash2id.defaultReturnValue();
	}
	
	/**
	 * Reset hashmap from url to id
	 */	
	public void resetIds() {
		urlHash2id=new Int2IntOpenHashMap();
		urlHash2id.defaultReturnValue(DEFAULT_KEY_NOT_EXIST);
	}	
	
	
	/*
	public void addArc(String fromUrl, String toUrl) {
		int idFrom=getId(fromUrl);
		int idTo=getId(toUrl);
	
		IntSet set=(IntSet)arcs.get(idFrom);
		if (set==null) {
			set=new IntAVLTreeSet();
		}
		set.add(Integer.valueOf(idTo));
		arcs.put(Integer.valueOf(idFrom), set);
	}
	*/
	
	/**
	 * Build the successors structure
	 */
	/*
	public void completeStructure() {		
		succ = new int[numNodes()][];
		for (int i=0; i<numNodes(); i++) {
			IntSet set = arcs.get(i);
			if (set == null) {
				succ[i] = new int[0];
				continue;
			}
			succ[i] = new int[set.size()];			
			set.toArray(succ[i]);
		}
		
		arcs=null; // free arcs mem
	}
	*/
	
	/**
	 * Build text graph to file
	 * @param inputDir directory where are files containing parts of graph
	 * @param outputDir 
	 * @throws IOException
	 */
	/*
	public static GraphMG4J buildTextGraph(String inputDir, String outputDir) throws IOException {
		GraphMG4J graph=new GraphMG4J();	
		
		File fdir=new File(inputDir);
		if (!fdir.isDirectory()) {
			throw new IOException("ERROR: "+inputDir+" is not a directory.");
		}

		PrintWriter pw=new PrintWriter(new File(outputDir,GRAPH_LIST_FILE));
		
		for (File f : fdir.listFiles()) {
			if (f.getName().startsWith("part-")) {      
				System.out.println("reading file "+f);
			
				BufferedReader br = new BufferedReader( new FileReader(f) );
				String line;					
		
				while ( ( line = br.readLine() ) != null ) {
					String parts[] = line.split( "\\s" );
				
					if (parts.length!=2) { // wrong line
						//System.err.println("ERROR: wrong line:"+line);
						continue; 
					}
								
					//graph.addArc(parts[0], parts[1]);
					pw.println(graph.getId(parts[0])+" "+graph.getId(parts[1]));
				}
				br.close();
			}				
		}
		pw.close();

		//graph.completeStructure();
	
		return graph;
	}	
	*/
	
	/*
	public static ImmutableGraph load( CharSequence basename ) throws IOException {
		return null;
	}
	
	public static ImmutableGraph load( CharSequence b, ProgressLogger pm ) throws IOException {
		return load( b );
	}

	public static ImmutableGraph loadSequential( CharSequence b, ProgressLogger pm ) throws IOException {
		return load( b );
	}

	public static ImmutableGraph loadSequential( CharSequence b ) throws IOException {
		return load( b );
	}
	
	public static ImmutableGraph loadOffline( CharSequence b, ProgressLogger pm ) throws IOException {
		return load( b );
	}

	public static ImmutableGraph loadOffline( CharSequence b ) throws IOException {
		return load( b );
	}
	*/

	
	/**
	 * Write file with pagerank scores 
	 * @param outputFile
	 * @param scores
	 * @param graph	 
	 */
	/*
	public static void writeFileScores(double scores[], GraphMG4J graph) throws FileNotFoundException {
								
		// sorting by value
		ArcEntry entriesArray[]=new ArcEntry[scores.length];
		for (int i=0;i<scores.length;i++) {
			entriesArray[i]=new ArcEntry(i,scores[i]);
		}					
        Arrays.sort(entriesArray);
        	
		//write scores to file
        PrintWriter pw=new PrintWriter(new File(SCORES_FILE));
        for (int i=0;i<entriesArray.length;i++) {
        	//pw.println(graph.getUrl(entriesArray[i].getPos())+" "+entriesArray[i].getScore());		
        	pw.println(entriesArray[i].getId()+" "+entriesArray[i].getScore());
        }		
		pw.close();
	}
	*/
	
	
	/**
	 * Main 
	 * @param arg
	 * @throws IOException
	 */
	/*
	public static void main( String arg[] ) throws IOException {
		
		if (arg.length!=2) {
			throw new IOException("NutchwaxPagerank <input dir> <output file>");
		}
		
		GraphMG4J graphText=GraphMG4J.buildTextGraph(arg[0],arg[1]);				
		ImmutableGraph graphArcList = ArcListASCIIGraph.load(GRAPH_LIST_FILE);
		ImmutableGraph.store(BVGraph.class, graphArcList, GRAPH_BV_FILE);
		graphArcList=null;
		BVGraph graphBv=BVGraph.load(GRAPH_BV_FILE); 		
		double scores[]=Pagerank.compute(graphBv,null);
		GraphMG4J.writeFileScores(scores, graphText);
		
		System.out.println("done...");		
	}
	*/	
	
	
//	public class GraphMap extends LongArrayBitVector {
//		private final static long DEFAULT_CAPACITY=1000000;			
//
//		public GraphMap() {
//			super(DEFAULT_CAPACITY);
//		}
//		
//		public void reset() {
//			super.clear();
//		}
//		
//		public boolean hasId(long index) {
//			boolean bol;
//			try {
//				bol=super.getBoolean(index);
//			}
//			catch (ArrayIndexOutOfBoundsException e) {
//				return false;
//			}
//			return bol;
//		}
//		
//		/**
//		 * Get hash code from url
//		 * @param url
//		 * @return
//		 */
//		public long getId(String url) {
//			return Math.abs(url.hashCode()); // TODO change by other hash function: mash - http://www.hpl.hp.com/techreports/2008/HPL-2008-91R1.pdf
//		}
//		
//		/**
//		 * Mark as true
//		 * @param index
//		 */
//		public void setId(long index) {
//			super.add(index, true);
//		}
//		
//		/**
//		 * Number of bits set to true
//		 */
//		public long count() {
//			return super.count();
//		}		
//	}
}
