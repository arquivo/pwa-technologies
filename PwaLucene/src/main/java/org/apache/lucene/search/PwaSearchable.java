package org.apache.lucene.search;

import java.io.IOException;
import java.util.Vector;


/**
 * Generic interface for document search
 * @author Miguel Costa
 */
public abstract class PwaSearchable implements PwaSearchableCommon {
			
	protected Vector<PwaPositionsManager> posmanagers;
	
	
	/**
	 * Skips to document @doc or superior
	 * @param doc document id
	 * @return true if skip to document @doc or superior; false otherwise
	 */
	public abstract boolean skipTo(int doc) throws IOException;
		
	/**
	 * Skips to document @doc or superior from start
	 * @param doc document id
	 * @return true if skip to document @doc or superior from start; false otherwise
	 */
	public abstract boolean skipToFromStart(int doc) throws IOException;	
	
	/**
	 * Collect ranking features from this document
	 * @param doc document id
	 * @param collector features collector 
	 */
	public abstract void collectFeatures(int doc, PwaRawFeatureCollector collector) throws IOException;
	
	/**
	 * Set ranking features with empty values for this document
	 * @param doc document id
	 * @param collector features collector
	 */
	public abstract void collectEmptyFeatures(int doc, PwaRawFeatureCollector collector) throws IOException;
		
	/**
	 * Indicates if this document should be excluded
	 * @return true if this document should be excluded; false otherwise
	 */
	public abstract boolean isExclude();
	
	
	/**
	 * Set PositionsManagera
	 * @param posmanagers
	 */
	public void	setPositionsManager(Vector<PwaPositionsManager> posmanagers) {
		this.posmanagers=posmanagers;
	}
	
	/**
	 * Get PositionsManager
	 * @param posmanagers
	 */
	public Vector<PwaPositionsManager> getPositionsManager() {
		return posmanagers;
	}	
}
