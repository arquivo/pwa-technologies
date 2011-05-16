package org.archive.wayback.util;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;


/**
 * Extend functionalities of File class
 * @author Miguel Costa
 *
 */
public class FileExt {

	/**
	 * List files from directory 
	 * @param f source directory
	 * @param filter filter extensions
	 */
    public static ArrayList<File> listFiles(File f, FileFilter filter) {
    	ArrayList<File> files = new ArrayList<File>();
    	if (f.isDirectory()) {
            for (File faux : f.listFiles()) {
            	if (filter.accept(faux)) {
            		files.add(faux);		
            	}
            }
        }
    	return files;
    }
	
	/**
	 * List files from directory recursively
	 * @param f source directory
	 * @param filter filter extensions
	 */
    public static ArrayList<File> listFilesRecursively(File f, FileFilter filter) {
    	ArrayList<File> files = new ArrayList<File>();
    	listFilesRecursivelyAux(f, 0, filter, files);
    	return files;
    }
	
	/**
	 * Auxiliary to list files from directory recursively
	 * @param f source directory
	 * @param depth depth
	 * @param filter filter extensions
	 * @param files files found
	 */
	private static void listFilesRecursivelyAux(File f, int depth, FileFilter filter, ArrayList<File> files) {
               
        if (f.isDirectory()) {
            for (File faux : f.listFiles()) {
                listFilesRecursivelyAux(faux, depth+1, filter, files);
            }
        }
//        else if (f.isFile() && filter. f.getName().toLowerCase().endsWith(filterExt)) {
        else if (filter.accept(f)) {
        	files.add(f);
        }
    }
	
	/**
	 * Get arc file filter
	 * @return arc file filter
	 */
	public static FileFilter getArcFileFilter() {
		FileFilter filter = new FileFilter() {
			public boolean accept(File f) {
				return f.isFile() && 
				(f.getName().endsWith(".arc.gz") ||
					f.getName().endsWith(".gz") ||
					f.getName().endsWith(".warc.gz"));
			}
		};		
		return filter;	
	}		
}
