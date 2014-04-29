== Uploading Portuguese Web Archive crawls to the Internet Archive ==

This scripts were developed to facilitated the upload of Heritrix crawls to the Internet Archive using the IAS3 API. Follow these steps:

1. Define the crawl meta-data at the config file (check example at /svnGCodeInternetArchive/crawlConfigFiles/configItemsAWP4.cfg). 

If a crawl was performed in parallel by several machines or has ARC files on different directories must generate a different config upload file for each subcrawl to avoid itemname conflicts (e.g. configItemsAWP4.cfg  for ARC files in /AWP4-PT-20090520152848009 and configItemsAWP4chkpt5.cfg for ARC files in AWP4-PT-Recuperacao-chkpt5-20090622153514762). The ARC files of a given crawl will be aggregate by the custom field "pwacrawlid".
e.g. configItemsAWP4.cfg for ARCS in /svnGCodeInternetArchive/crawlConfigFiles/configItemsAWP4.cfg

1.1. Store config file for later access (e.g. on svn)
e.g. #svn add /svnGCodeInternetArchive/crawlConfigFiles/configItemsAWP4.cfg

1.2. Set .bashrc environment variables for IAS3 login. 
export AWS_ACCESS_KEY_ID
export AWS_SECRET_ACCESS_KEY

2. Generate 10GB items for the crawl using generateItems.sh (100 ARCs of 100 MB in each item)
e.g. #./svnGCodeInternetArchive/generateItems.sh ./svnGCodeInternetArchive/crawlConfigFiles/configItemsAWP4.cfg

2.1. Check if the nr. of items on $OUTPUTFILE matches the number of files on /tmp/crawlFiles.txt and nr. of ARC files on $DIRECTORY_OF_THE_CRAWL nr. of ARC files on documentation about crawl meta-data  (column Número de arcs on http://wiki.priv.fccn.pt/Recolhas). 
e.g. 
#cat itemsForAWP4 | grep "arc.gz" |wc; cat itemsForAWP4|wc; cat /tmp/crawlFiles.txt|wc. 
http://wiki.priv.fccn.pt/Recolhas; forth line.

3. Upload the items to the Internet Archive using uploadItems.sh.
e.g. #./svnGCodeInternetArchive/uploadItems.sh ./svnGCodeInternetArchive/crawlConfigFiles/configItemsAWP4.cfg >configItemsAWP4.upload &

3.1 Verify that all ARC files were uploaded OK
# cat configItemsAWP4.upload|grep OK|wc
Nr. of ARC files in documentation (private: http://wiki.priv.fccn.pt/Recolhas)

3.1.1 In case of errors, extract failed Items and ARC files to be recovered
# cat configItemsAWP4chkpt5.upload |grep 'RECOVER_ARC_FILE:'| sed 's/.*RECOVER_ARC_FILE://g' > itemsForAWP4chkpt5.recover
# change config file to new OUTPUTFILE=/shareT2/scripts/IAExchange/itemsForAWP4AWP4chkpt5.recover
# repeat uploadItems.sh

3.3 Count errors and compare with recover file
# cat configItemsAWP4chkpt5.upload|grep Error|wc; cat itemsForAWP4AWP4chkpt5.recover|wc

4. Compare number of uploaded items with 
e.g. https://archive.org/search.php?query=pwacrawlid%3AAWP4
#cat itemsForAWP4| cut -d " " -f 1|sort -u|wc

4.1. Update documentation on Wiki (http://wiki.priv.fccn.pt/Recolhas; column "Replica on Internet Archive"). Insert link to IA query with custom field "pwacrawlid:$x_archive_meta_pwacrawlid" that returns all items of the crawl through https://archive.org/advancedsearch.php.
e.g. "Done. [https://archive.org/advancedsearch.php?q=pwacrawlid%3AAWP4&fl%5B%5D=identifier&sort%5B%5D=&sort%5B%5D=&sort%5B%5D=&rows=50&page=1&callback=callback&save=yes&output=tables List of the NUMBER_OF_ITEMS that compose AWP4 crawl. Query custom field: "pwacrawlid:AWP4"]"

== Downloading Portuguese Web Archive content from the Internet Archive ==

Crawled content can be downloaded using one of the following methods. Remember that each crawl is composed by several items and each item contains at most 100 ARC files.

Download one item:
- ia Python command to download files from each item: IN TEST 
	https://github.com/jjjake/ia-wrapper/blob/master/README.rst
	- E.g. to download item with identifier portuguese-web-archive-AWP42009-20090522035123
	# ia download portuguese-web-archive-AWP42009-20090522035123
- Torrent files to download files from each item
	- A torrent file exists for each and every item of a crawl. The URL to retrieve the torrent files for the items is formatted like so: https://archive.org/download/{ITEM_NAME}/{ITEM_NAME}_archive.torrent 

Download an item list:
- Python script to download files from all items in the item list (contribution from Zeynep Pehlivan)
	- Link to script on Google Code.
	
Download content based on meta-data. If you just have the "pwacrawlid" or other metadata field and you want to identify relevant items to download:
- Wget to download in bulk: http://blog.archive.org/2012/04/26/downloading-in-bulk-using-wget/
- ia Pyhon command and parallel: IN TEST
	- E.g. # ia search 'subject:(pwacrawlid:AWP4)' | parallel 'ia download {}'

	
Additional documentation:

IAS3
- IA Access API:
	- JSON API for archive.org services and metadata: http://archive.org/help/json.php
	- Examples: https://archive.org/metadata/pwa-test-warc-20140128, https://archive.org/metadata/pwa-test-warc-20140128/files
	- Direct HTTP access
		- Item JSON meta-data: https://archive.org/metadata/$ITEM_NAME
		- Item JSON files: https://archive.org/metadata/$ITEM_NAME/files 
		- Item download: https://archive.org/download/$ITEM_NAME/$FILE_NAME 
- IA S3 API:
	- https://github.com/vmbrasseur/IAS3API/blob/master/metadata.md#collection
	- https://archive.org/help/abouts3.txt
	- https://github.com/vmbrasseur/IAS3API
	- https://github.com/jjjake/ia-wrapper (Python Library/CLI useful)
	- https://github.com/vmbrasseur/IAS3API/blob/master/headers.md (HEADERS)
	- https://github.com/vmbrasseur/IAS3API/blob/master/metadata.md (IA Meta-data)
	- IA command
		- https://github.com/jjjake/ia-wrapper/blob/master/README.rst#uploading
		ia upload <identifier> file1 file2 --metadata="title:foo" --metadata="blah:arg"
	- Python API
		- http://ia-wrapper.readthedocs.org/en/latest/
- IA S3 catalog tasks
	- https://archive.org/catalog.php?mode=s3
	- https://archive.org/catalog.php?&justme=1
	- Slow down upload if there are more than ~1500 global S3 tasks running or waiting to run. 

IA Meta-data API
	 - http://archive.org/advancedsearch.php
		- Example to get all items that compose crawl EAWP1: https://archive.org/advancedsearch.php?q=pwacrawlid%3AEAWP1&fl%5B%5D=identifier&sort%5B%5D=&sort%5B%5D=&sort%5B%5D=&rows=50&page=1&callback=callback&save=yes&output=tables
     - http://archive.org/help/json.php 
	 - http://blog.archive.org/2013/07/04/metadata-api/
IA How Archive.org items are structured
	 http://blog.archive.org/2011/03/31/how-archive-org-items-are-structured/

		
Portuguese Web Archive collection
	- https://archive.org/details/portuguese-web-archive
		
Have fun!

Daniel Gomes
www.archive.pt