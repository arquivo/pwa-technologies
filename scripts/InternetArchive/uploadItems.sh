#!/bin/sh                                          
#author:dgomes            
#USAGE: uploadItems.sh CONFIG_FILE
# IAS3 access keys must be set on environment (~/.bashrc)
# AWS_ACCESS_KEY_ID
# AWS_SECRET_ACCESS
                                            
if [ $# -ne 1 ]; then
  echo "Usage: $0 CONFIG_FILE"
  exit 127
fi

WORKING_DIR=$(pwd)
source "$1"
logfile="$WORKING_DIR/configItems$CRAWL_NAME.upload"
cd "$DIRECTORY_OF_THE_CRAWL"
echo "Upload for $CRAWL_NAME started at "$(date) >> $logfile

#create item and upload files
#input file format ITEMNAME FILENAME MD5  
while read line 
do   
fileprops="$line"
#read file information
export itemname=$(echo "$fileprops"|cut -d " " -f 1)
export arcfilepath=$(echo "$fileprops"|cut -d " " -f 2)
#only ARC file name
export arcfilename=$(echo "$fileprops"|cut -d " " -f 2|sed 's/.*\///g')
export md5=$(echo "$fileprops"|cut -d " " -f 3)

OPTS="-s -m 300s --location --write-out %{http_code}"
UPLOAD="--upload-file $DIRECTORY_OF_THE_CRAWL/$arcfilepath http://s3.us.archive.org/$itemname/$arcfilename"

#upload using curl and had meta-data
#replace access key: check is values are correctly passed on bash
response=$(/usr/bin/curl $OPTS --header "authorization:LOW $AWS_ACCESS_KEY_ID:$AWS_SECRET_ACCESS_KEY" --header 'x-amz-auto-make-bucket:1' --header 'x-archive-meta-mediatype:web' --header "x-archive-meta01-collection: $COLLECTION" --header "x-archive-meta-pwacrawlid: $x_archive_meta_pwacrawlid" --header "x-archive-meta-external-identifier: $x_archive_meta_external_identifier" --header "x-archive-meta-creator:$x_archive_meta_creator"  --header "x-archive-meta-contributor:$x_archive_meta_contributor" --header "x-archive-meta-title:$x_archive_meta_title" --header "x-archive-meta-coverage: $x_archive_meta_coverage" --header "Content-MD5: $md5" --header "x-archive-meta-description: $x_archive_meta_description" --header "x-archive-meta-language: $x_archive_meta_language" --header "x-archive-meta-subject: $x_archive_meta_subject" --header "x-archive-meta-notes: $x_archive_meta_notes" --header "x-archive-meta-credits: $x_archive_meta_credits" --header "x-archive-meta-date: $x_archive_meta_date" $WOUT $UPLOAD)

#Use --header "x-archive-simulate-error:SlowDown" to simuate errors

#check md5 and write log messages
if [ "$response" != "200" ]; then 
# Something went wrong. Wait 5 minutes and then retry.
echo $(date)", Error on upload: sleeping for 300s" >> $logfile
sleep 300s
response=$(/usr/bin/curl -vvv $OPTS --header "authorization:LOW $AWS_ACCESS_KEY_ID:$AWS_SECRET_ACCESS_KEY" --header 'x-amz-auto-make-bucket:1' --header 'x-archive-meta-mediatype:web' --header "x-archive-meta01-collection: $COLLECTION" --header "x-archive-meta-pwacrawlid: $x_archive_meta_pwacrawlid" --header "x-archive-meta-external-identifier: $x_archive_meta_external_identifier" --header "x-archive-meta-creator:$x_archive_meta_creator"  --header "x-archive-meta-contributor:$x_archive_meta_contributor" --header "x-archive-meta-title:$x_archive_meta_title" --header "x-archive-meta-coverage: $x_archive_meta_coverage" --header "Content-MD5: $md5" --header "x-archive-meta-description: $x_archive_meta_description" --header "x-archive-meta-language: $x_archive_meta_language" --header "x-archive-meta-subject: $x_archive_meta_subject" --header "x-archive-meta-notes: $x_archive_meta_notes" --header "x-archive-meta-credits: $x_archive_meta_credits" --header "x-archive-meta-date: $x_archive_meta_date" $WOUT $UPLOAD)
fi

if [ "$response" == "200" ]; then echo $(date)" $itemname $arcfilepath $md5: OK" >> $logfile
else echo $(date)", Error message after retry: $response. RECOVER_ARC_FILE:$itemname $arcfilepath $md5" >> $logfile
fi
 
done < "$OUTPUTFILE"


