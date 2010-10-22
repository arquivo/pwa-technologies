package org.apache.lucene.util;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.MapFieldSelector;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.util.Enumeration;


/**
 * Extract field values from index to stdout
 * @author Miguel Costa
 */
public class PwaExtractFieldValues {

	
	/**
	 * Extract field values
	 * @param indexPath index path
	 * @param fieldNames field names to extract
	 */
	public static void extract(String indexPath, String fieldNames[]) throws IOException {  
		Directory idx = FSDirectory.getDirectory(indexPath, false);			
		org.apache.lucene.search.Searcher searcher = new IndexSearcher(idx);
		org.apache.lucene.index.IndexReader reader=IndexReader.open(idx);
		
		int maxDoc=reader.maxDoc();
		for (int i=0;i<maxDoc;i++) {
			if (!reader.isDeleted(i)) {
				Document doc = reader.document(i, new MapFieldSelector(fieldNames));
				int j=0;
				for (Enumeration e=doc.fields();e.hasMoreElements();) {							 
					Field field = (Field)e.nextElement();
					if (j!=0) {
						System.out.print(" ");
					}
					System.out.print(field.stringValue());
					//field.name()
					j++;
				}
				System.out.println();
				
			}
		}
			
		reader.close();
		searcher.close();
	}

	
	/**
	 * Main
	 * @param args
	 */
	public static void main(String[] args) {

		if (args.length<2) {
			System.out.println("arguments: <indexDir> [field1 field2 .. fieldN]");			
			return;
		}
		
		try {
			String fieldNames[]=new String[args.length-1];			
			System.arraycopy(args, 1, fieldNames, 0, args.length-1); 
			PwaExtractFieldValues.extract(args[0],fieldNames);
		}
		catch (Exception e) {		
			e.printStackTrace();
		}
	}	
}




