package org.apache.nutch.indexer;

/**
 * HeapSort (in descending order)
 * code from http://www.inf.fh-flensburg.de/lang/algorithmen/sortieren/heap/heapen.htm
 * @author Miguel Costa
 */
public class HeapSorter {
    private static Comparable[] a;
    private static int n;

    public static void sort(Comparable[] a0) {
        a=a0;
        n=a.length;
        heapsort();
    }
    
    public static void sort(Comparable[] a0, int size) {
        a=a0;
        n=size;
        heapsort();
    }

    private static void heapsort() {
        buildheap();
        while (n>1)
        {
            n--;
            exchange (0, n);
            downheap (0);
        } 
    }

    private static void buildheap() {
        for (int v=n/2-1; v>=0; v--)
            downheap(v);
    }

    private static void downheap(int v) {
        int w=2*v+1;    // first descendant of v
        while (w<n)
        {
            if (w+1<n)    // is there a second descendant?
                //if (a[w+1]>a[w]) w++;
            	if (a[w+1].compareTo(a[w])>0) w++;
            // w is the descendant of v with maximum label

            //if (a[v]>=a[w]) return;  // v has heap property
            if (a[v].compareTo(a[w])>=0) return;  // v has heap property
            // otherwise
            exchange(v, w);  // exchange labels of v and w
            v=w;        // continue
            w=2*v+1;
        }
    }

    private static void exchange(int i, int j)
    {
    	Comparable t=a[i];
        a[i]=a[j];
        a[j]=t;
    }

}    // end class HeapSorter
