#!/bin/sh                                          
#author:dgomes            
#USAGE: uploadItems.sh CONFIG_FILE
#input file format ITEMNAME FILENAME MD5                                                          
if [ $# -ne 1 ]; then
  echo "Usage: $0 CONFIG_FILE"
  exit 127
fi
source "$1"
cd "$DIRECTORY_OF_THE_CRAWL"

#create item and upload files
while read line 
do   
fileprops="$line"
#read file information
export itemname=$(echo "$fileprops"|cut -d " " -f 1)
export arcfilename=$(echo "$fileprops"|cut -d " " -f 2)
export md5=$(echo "$fileprops"|cut -d " " -f 3)

OPTS="-s --location --write-out %{http_code}"
UPLOAD="--upload-file $DIRECTORY_OF_THE_CRAWL/$arcfilename http://s3.us.archive.org/$itemname/$arcfilename"

#upload using curl and had meta-data
response=$(/usr/bin/curl $OPTS --header 'authorization:LOW FoGL8D8q90QNVrmV:F5XyGfXXygpxrLVM' --header 'x-amz-auto-make-bucket:1' --header 'x-archive-meta-mediatype:web' --header "x-archive-meta01-collection: $COLLECTION" --header "x-archive-meta-creator:$x_archive_meta_creator" --header "x-archive-meta-adder:$x_archive_meta_adder" --header "x-archive-meta-contributor:$x_archive_meta_contributor" --header "x-archive-meta-title:$x_archive_meta_title" --header "x-archive-meta-coverage: $x_archive_meta_coverage" --header "Content-MD5: $md5" --header "x-archive-meta-description: $x_archive_meta_description" --header "x-archive-meta-language: $x_archive_meta_language" --header "x-archive-meta-subject: $x_archive_meta_subject" $WOUT $UPLOAD)

#check md5 and write log messages
if [ "$response" == "200" ]; then echo "$itemname, $arcfilename: OK"
else echo "ERROR: $response. Item: $itemname; Arcfilename: $arcfilename"
fi

done < "$OUTPUTFILE"


