#!/bin/sh             
#Destroys and updates the metadata for all the items of a crawl                             
#author:dgomes            
#USAGE: updateItemsMetaData.sh CONFIG_FILE
#https://github.com/vmbrasseur/IAS3API/blob/master/examples/curl-update_metadata.md
                                      
if [ $# -ne 1 ]; then
  echo "Usage: $0 CONFIG_FILE"
  exit 127
fi
source "$1"
#get itemname list for crawl
cat "$OUTPUTFILE" | cut -d " " -f 1| sort -u > itemNamesForCrawl

#create item and upload files
while read line 
do   
#read file information
#input file format ITEMNAME   
itemname="$line"

# check opts
OPTS="-s --location --write-out %{http_code}"
UPLOAD="--upload-file /dev/null http://s3.us.archive.org/$itemname"

#upload using curl and had meta-data
#replace access key
response=$(/usr/bin/curl $OPTS --header "authorization:LOW $AWS_ACCESS_KEY_ID:$AWS_SECRET_ACCESS_KEY" --header 'x-archive-ignore-preexisting-bucket:1' --header 'x-archive-meta-mediatype:web' --header "x-archive-meta01-collection: $COLLECTION" --header "x-archive-meta-pwacrawlid: $x_archive_meta_pwacrawlid" --header "x-archive-meta-creator:$x_archive_meta_creator"  --header "x-archive-meta-contributor:$x_archive_meta_contributor" --header "x-archive-meta-title:$x_archive_meta_title" --header "x-archive-meta-coverage: $x_archive_meta_coverage" --header "Content-MD5: $md5" --header "x-archive-meta-description: $x_archive_meta_description" --header "x-archive-meta-language: $x_archive_meta_language" --header "x-archive-meta-subject: $x_archive_meta_subject" --header "x-archive-meta-notes: $x_archive_meta_notes" --header "x-archive-meta-credits: $x_archive_meta_credits" --header "x-archive-meta-date: $x_archive_meta_date" $WOUT $UPLOAD)

#echo "/usr/bin/curl $OPTS --header "authorization:LOW $AWS_ACCESS_KEY_ID:$AWS_SECRET_ACCESS_KEY" --header 'x-archive-ignore-preexisting-bucket:1' --header 'x-archive-meta-mediatype:web' --header "x-archive-meta01-collection: $COLLECTION" --header "x-archive-meta-pwacrawlid: $x_archive_meta_pwacrawlid" --header "x-archive-meta-creator:$x_archive_meta_creator"  --header "x-archive-meta-contributor:$x_archive_meta_contributor" --header "x-archive-meta-title:$x_archive_meta_title" --header "x-archive-meta-coverage: $x_archive_meta_coverage" --header "Content-MD5: $md5" --header "x-archive-meta-description: $x_archive_meta_description" --header "x-archive-meta-language: $x_archive_meta_language" --header "x-archive-meta-subject: $x_archive_meta_subject" --header "x-archive-meta-notes: $x_archive_meta_notes" --header "x-archive-meta-credits: $x_archive_meta_credits" --header "x-archive-meta-date: $x_archive_meta_date" $WOUT $UPLOAD"

#check md5 and write log messages
if [ "$response" == "200" ]; then echo "$itemname: update OK"
else echo "ERROR on update: $response. Item: $itemname"
fi

done < itemNamesForCrawl



