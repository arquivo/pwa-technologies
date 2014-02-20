#!/bin/sh
#author: dgomes
#USAGE: uploadCrawl.sh CRAWL_NAME DIRECTORY_OF_THE_CRAWL COLLECTION

# split in two scripts?
# generateItems.sh
# uploadItems.sh

#verifies number of arguments
if [ $# -ne 3 ]; then
  echo Usage: $0 CRAWL_NAME DIRECTORY_OF_THE_CRAWL COLLECTION
  exit 127
fi

CRAWL_NAME=$1
DIRECTORY_OF_THE_CRAWL=$2
COLLECTION=$3

#list arc files
ls -X $DIRECTORY_OF_THE_CRAWL| tr '\n' '\n' $1/*.arc.gz > crawlFiles.txt

newitem=0
n=0

#create item
cat crawlFiles.txt | while read FILENAME
do {
	if newitem{
	#get date for item name
	#this depends on the crawl file format 
	ITEMDATE=`echo $FILENAME|cut -d "-" -f 2`
	#item name
	ITEMNAME=`echo $COLLECTION"-"$CRAWL_NAME"-"$ITEMDATE` 
	#create item using ia command or curl
	#if I cannot create an empty item, just change the name when newitem
	curl -v --location --header 'x-amz-auto-make-bucket:1' --header 'x-archive-meta01-collection:'$COLLECTION --header 'x-archive-meta-mediatype:web' --header 'x-archive-meta-title:'$ITEMNAME --header "authorization: LOW
	FoGL8D8q90QNVrmV:F5XyGfXXygpxrLVM" 
	newitem=false
	}
}

#generate md5
MD5=md5sum "$FILENAME"

#set header Content-MD5
#upload FILENAME
# use x-archive-ignore-preexisting-bucket:1? What is the advantage?
response=$(curl -v --write-out %{http_code} --location --header 'x-amz-auto-make-bucket:1' --header 'x-archive-meta01-collection:'$COLLECTION --header 'x-archive-meta-mediatype:web' --header 'x-archive-meta-title: '$CRAWL_NAME --header "authorization: LOW FoGL8D8q90QNVrmV:F5XyGfXXygpxrLVM" --header 'Content-MD5: '$MD5 --header 'x-archive-queue-derive:0' --upload-file $FILENAME http://s3.us.archive.org/$ITEMNAME/$FILENAME)

if $response=200 echo "$ITEMNAME", "FILE: "$FILENAME:"OK"
else echo "ITEM: "$ITEMNAME", FILE: "$FILENAME:$ERROR

nuploadedfiles++

if nuploadedfiles>=100 newitem=1

done




