$Id: README.txt 1450 2007-01-22 21:15:40Z stack-sf $

This project contains extensions to nutch to allow indexing of
collections used in the TREC conference (primarily focusing on the
.GOV2 collection and Terabyte track) and searching of those
collections in a format compatible with trec_eval. 

To build you need a JDK and ant and nutch sources. The ant buildfile
assumes the nutch sources are in nutch/. So if you have a checkout of
the nutch subversion repository:

	$ cd ${ARCHIVE_ACCESS}/projects/nutch-trec
	$ ln -s ${NUTCH_SVN}/trunk nutch
	$ ant

The parser is based on JavaCC, if you wish to rebuild the JavaCC
source .java files from the .jj javacc file you need a copy of JavaCC
in JavaCC/, eg:

	$ cd ${ARCHIVE_ACCESS}/projects/nutch-trec
	$ ln -s ${JAVACC_HOME} JavaCC
	$ ant javacc

When built a nutch-trec.jar will be created in build/, a link will
also be created to allow nutch to run classes withing the nutch-trec.jar: 
	
	$ ln -s build/nutch-trec.jar ${NUTCH_SVN}/trunk/build

To index a collection you need the bin/nutch script patched to accept
Hadoop job jars (see: https://issues.apache.org/jira/browse/NUTCH-352). 

	$ ${NUTCH_HOME}/bin/nutch jar ${NUTCH-TREC_HOME}/build/nutch-trec.jar \
	  /input/directory /output/directory

Where /input/directory is an existing directory containing text files
detailing the locations of collection files to be indexed. Where
collection files can be local or remote over http, uncompressed or
gzipped, and conform to the format described in
http://ir.dcs.gla.ac.uk/test_collections/samples/GOV_sampleDoc

The above step results in at least one /output/directory/segments/timestamp/
being created. Nutch itself is then used to build an index:

	$ ${NUTCH_HOME}/bin/nutch updatedb /output/directory/crawldb \
	  /output/directory/segments/timestamp
	$ ${NUTCH_HOME}/bin/nutch invertlinks /output/directory/linkdb \
	  /output/directory/segments/timestamp
	$ ${NUTCH_HOME}/bin/nutch index /output/directory/indexes \
	  /output/directory/crawldb /output/directory/linkdb \
	  /output/directory/segments/timestamp

To query the index you need a text file containing a list of queryid:query
like: http://trec.nist.gov/data/terabyte/05/05.efficiency_topics.gz

Then:
	$ ln -s /output/directory crawl
	$ ${NUTCH_HOME}/bin/nutch org.archive.nutch.trec.TRECBean \
	  query.txt runid limit

Where runid is a string describing the run and limit is the maximum
number of documents to return (defaults to 20).

If you wish to use this project in Eclipse, do an ant build import
from the ant build.xml and make sure your compiler compliance level is
set to 5.0. 

To run the JUnit test from ant you need to have the junit.jar added
to your ${ANT_HOME}/lib as outlined in:
http://ant.apache.org/manual/OptionalTasks/junit.html
