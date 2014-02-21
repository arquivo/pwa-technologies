This scripts were developed to facilitated the upload of Heritrix crawls to the Internet Archive using the IAS3 API. Folow these steps:

1. Define the crawl meta-data at the config file (check example at configItemsEAWP1.cfg)
2. Generate 10GB item for the crawl using generateItems.sh
3. Upload the items to the Internet Archive using uploadItems.sh.

Additional documentation:

IAS3
- IA Access API:
	- JSON API for archive.org services and metadata: http://archive.org/help/json.php
	- Examples: https://archive.org/metadata/pwa-test-warc-20140128, https://archive.org/metadata/pwa-test-warc-20140128/files
	- Direct HTTP access
		- Item JSON meta-data: https://archive.org/metadata/$ITEMNAME
		- Item JSON files: https://archive.org/metadata/$ITEMNAME/files 
		- Item download: https://archive.org/download/$ITEMNAME/$FILENAME 
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

IA Meta-data API
	 - http://archive.org/advancedsearch.php
		- Example to get all items that compose crawl EAWP1: https://archive.org/advancedsearch.php?q=pwacrawlid%3AEAWP1&fl%5B%5D=identifier&sort%5B%5D=&sort%5B%5D=&sort%5B%5D=&rows=50&page=1&callback=callback&save=yes&output=tables
     - http://archive.org/help/json.php 
	 - http://blog.archive.org/2013/07/04/metadata-api/

		
Portuguese Web Archive collection
	- https://archive.org/details/portuguese-web-archive
		
Have fun!

Daniel Gomes
www.archive.pt