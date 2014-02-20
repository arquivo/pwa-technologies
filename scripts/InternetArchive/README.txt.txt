This scripts were developed to facilitated the upload of Heritrix crawls to the Internet Archive using the IAS3 API. Folow these steps

1. Define the crawl meta-data at the config file (check example at configItemsEAWP1.cfg)
2. Generate 10GB item for the crawl using generateItems.sh
3. Upload the items to the Internet Archive using uploadItems.sh.

Check additional documentation at:
- IA API:
	- JSON API for archive.org services and metadata: http://archive.org/help/json.php
	- Examples: https://archive.org/metadata/pwa-test-warc-20140128, https://archive.org/metadata/pwa-test-warc-20140128/files
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
		
Have fun.

Daniel Gomes
www.arhive.pt